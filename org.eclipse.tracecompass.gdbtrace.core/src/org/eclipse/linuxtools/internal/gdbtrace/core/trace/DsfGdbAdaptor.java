/*******************************************************************************
 * Copyright (c) 2011, 2014 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Marc Dumais - Initial implementation
 *   Francois Chouinard - Misc improvements, DSF signal handling, dynamic experiment
 *   Patrick Tasse - Updated for TMF 2.0
 *   Bernd Hufmann - Fixed deadlock during shutdown
 *******************************************************************************/

package org.eclipse.linuxtools.internal.gdbtrace.core.trace;

import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.eclipse.cdt.dsf.concurrent.DataRequestMonitor;
import org.eclipse.cdt.dsf.concurrent.DsfExecutor;
import org.eclipse.cdt.dsf.concurrent.IDsfStatusConstants;
import org.eclipse.cdt.dsf.concurrent.Query;
import org.eclipse.cdt.dsf.datamodel.DMContexts;
import org.eclipse.cdt.dsf.datamodel.IDMContext;
import org.eclipse.cdt.dsf.debug.service.IBreakpoints;
import org.eclipse.cdt.dsf.debug.service.IBreakpoints.IBreakpointsTargetDMContext;
import org.eclipse.cdt.dsf.debug.service.IRunControl.IContainerDMContext;
import org.eclipse.cdt.dsf.debug.service.command.ICommandControlService;
import org.eclipse.cdt.dsf.debug.service.command.ICommandControlService.ICommandControlDMContext;
import org.eclipse.cdt.dsf.gdb.IGdbDebugPreferenceConstants;
import org.eclipse.cdt.dsf.gdb.internal.GdbPlugin;
import org.eclipse.cdt.dsf.gdb.launching.GdbLaunch;
import org.eclipse.cdt.dsf.gdb.service.GDBTraceControl_7_2.TraceRecordSelectedChangedEvent;
import org.eclipse.cdt.dsf.gdb.service.IGDBTraceControl;
import org.eclipse.cdt.dsf.gdb.service.IGDBTraceControl.ITraceRecordDMContext;
import org.eclipse.cdt.dsf.gdb.service.IGDBTraceControl.ITraceRecordDMData;
import org.eclipse.cdt.dsf.gdb.service.IGDBTraceControl.ITraceRecordSelectedChangedDMEvent;
import org.eclipse.cdt.dsf.gdb.service.IGDBTraceControl.ITraceStatusDMData;
import org.eclipse.cdt.dsf.gdb.service.IGDBTraceControl.ITraceTargetDMContext;
import org.eclipse.cdt.dsf.mi.service.IMICommandControl;
import org.eclipse.cdt.dsf.mi.service.IMIProcesses;
import org.eclipse.cdt.dsf.mi.service.MIBreakpointDMData;
import org.eclipse.cdt.dsf.mi.service.MIBreakpoints;
import org.eclipse.cdt.dsf.mi.service.command.CommandFactory;
import org.eclipse.cdt.dsf.mi.service.command.output.MIBreakListInfo;
import org.eclipse.cdt.dsf.mi.service.command.output.MIBreakpoint;
import org.eclipse.cdt.dsf.service.DsfServiceEventHandler;
import org.eclipse.cdt.dsf.service.DsfServicesTracker;
import org.eclipse.cdt.dsf.service.DsfSession;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;
import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.ILaunchConfigurationType;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.debug.core.ILaunchesListener2;
import org.eclipse.debug.internal.ui.viewers.model.provisional.ITreeModelViewer;
import org.eclipse.debug.ui.AbstractDebugView;
import org.eclipse.debug.ui.DebugUITools;
import org.eclipse.debug.ui.IDebugUIConstants;
import org.eclipse.debug.ui.contexts.DebugContextEvent;
import org.eclipse.debug.ui.contexts.IDebugContextListener;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.linuxtools.internal.gdbtrace.core.Activator;
import org.eclipse.linuxtools.internal.gdbtrace.core.GdbTraceCorePlugin;
import org.eclipse.linuxtools.internal.gdbtrace.core.event.GdbTraceEvent;
import org.eclipse.linuxtools.internal.gdbtrace.core.event.GdbTraceEventContent;
import org.eclipse.linuxtools.tmf.core.event.ITmfEventType;
import org.eclipse.linuxtools.tmf.core.event.TmfEventField;
import org.eclipse.linuxtools.tmf.core.event.TmfEventType;
import org.eclipse.linuxtools.tmf.core.timestamp.TmfTimestamp;
import org.eclipse.linuxtools.tmf.core.trace.ITmfTrace;
import org.eclipse.linuxtools.tmf.core.trace.TmfExperiment;
import org.eclipse.linuxtools.tmf.ui.editors.ITmfTraceEditor;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IEditorReference;
import org.eclipse.ui.IFileEditorInput;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.ide.IDE;

/**
 * Adaptor to access GDB Tracepoint frames, previously collected and saved in a
 * file by GDB.  One instance of this maps to a single DSF-GDB session.
 * <p>
 * This class offers the functions of starting a post-mortem GDB session with a
 * tracepoint data file, navigate the data frames and return the data contained
 * in a given tracepoint frame.
 * <p>
 * Note: GDB 7.2 or later is required to handle tracepoints
 *
 * @author Marc Dumais
 * @author Francois Chouinard
 */
@SuppressWarnings("restriction")
public class DsfGdbAdaptor {

    private GdbTrace fGdbTrace;

    private int fNumberOfFrames = 0;
    private boolean fIsTimeoutEnabled;
    private int fTimeout;

    private ILaunch fLaunch;
    private boolean isTerminating;
    private DsfSession fDsfSession = null;
    private String fSessionId;

    private String tracedExecutable = ""; //$NON-NLS-1$

    private String gdb72Executable = ""; //$NON-NLS-1$
    private String fTraceFilePath = ""; //$NON-NLS-1$
    private String fTraceFile = ""; //$NON-NLS-1$
    private String sourceLocator = ""; //$NON-NLS-1$

    // To save tracepoints detailed info.  The key is the rank of the
    // breakpoint (tracepoint is a kind of breakpoint)
    private  Map<Integer, MIBreakpointDMData> fTpInfo = new HashMap<>();

    private TmfEventType tmfEventType = new TmfEventType(ITmfEventType.DEFAULT_CONTEXT_ID, "GDB Tracepoint", TmfEventField.makeRoot(new String[] { "Content" })); //$NON-NLS-1$ //$NON-NLS-2$

    {
        new DsfGdbPlatformEventListener();
    }

    /**
     * <b><u>DsfGdbPlatformEventListener</u></b>
     * <p>
     * Listens to platform and DSF-GDB events that announce important events
     * about the launchers or a change in debug context that we might need to
     * react-to.
     * <p>
     * @author Francois Chouinard
     */
    private class DsfGdbPlatformEventListener implements
    ILaunchesListener2, IDebugContextListener {

        /**
         *
         */
        public DsfGdbPlatformEventListener() {
            Display.getDefault().syncExec(new Runnable() {
                @Override
                public void run() {
                    DebugPlugin.getDefault().getLaunchManager().addLaunchListener(DsfGdbPlatformEventListener.this);
                    IWorkbench wb = PlatformUI.getWorkbench();
                    IWorkbenchWindow win = wb.getActiveWorkbenchWindow();
                    DebugUITools.getDebugContextManager().getContextService(win).addDebugContextListener(DsfGdbPlatformEventListener.this);
                }
            });
        }

        @Override
        public synchronized void launchesRemoved(ILaunch[] launches) {
        }

        @Override
        public synchronized void launchesAdded(ILaunch[] launches) {
        }

        @Override
        public synchronized void launchesChanged(ILaunch[] launches) {
        }

        @Override
        public synchronized void launchesTerminated(ILaunch[] launches) {
            for (ILaunch launch : launches) {
                String sessionId = ((GdbLaunch) launch).getSession().getId();
                closeGdbTraceEditor(sessionId);
            }
        }

        private String fCurrentSessionId = ""; //$NON-NLS-1$
        @Override
        public void debugContextChanged(DebugContextEvent event) {
            ISelection selection = event.getContext();
            if (selection instanceof IStructuredSelection) {
                List<?> eventContextList = ((IStructuredSelection) selection).toList();
                for (Object eventContext : eventContextList) {
                    if (eventContext instanceof IAdaptable) {
                        IDMContext context = (IDMContext) ((IAdaptable) eventContext).getAdapter(IDMContext.class);
                        if (context != null) {
                            String sessionId;
                            synchronized(fCurrentSessionId) {
                                sessionId = context.getSessionId();
                                if (sessionId.equals(fCurrentSessionId)) {
                                    return;
                                }
                            }
                            fCurrentSessionId = sessionId;
                            // Get the current trace record
                            final DsfExecutor executor = DsfSession.getSession(sessionId).getExecutor();
                            final DsfServicesTracker tracker = new DsfServicesTracker(GdbTraceCorePlugin.getBundleContext(), sessionId);
                            Query<ITraceRecordDMContext> getCurrentRecordQuery = new Query<ITraceRecordDMContext>() {
                                @Override
                                public void execute(final DataRequestMonitor<ITraceRecordDMContext> queryRm) {
                                    final IGDBTraceControl traceControl = tracker.getService(IGDBTraceControl.class);
                                    final ICommandControlService commandControl = tracker.getService(ICommandControlService.class);
                                    if (traceControl != null && commandControl != null) {
                                        ITraceTargetDMContext traceContext = (ITraceTargetDMContext) commandControl.getContext();
                                        traceControl.getCurrentTraceRecordContext(traceContext,    queryRm);
                                    } else {
                                        queryRm.done();
                                    }
                                }
                            };
                            try {
                                executor.execute(getCurrentRecordQuery);
                                ITraceRecordDMContext record;
                                if (DsfGdbAdaptor.this.fIsTimeoutEnabled) {
                                    record = getCurrentRecordQuery.get(fTimeout, TimeUnit.MILLISECONDS);
                                } else {
                                    record = getCurrentRecordQuery.get();
                                }
                                // If we get a trace record, it means that this can be used
                                if (record != null && record.getRecordId() != null) {
                                    int recordId = Integer.parseInt(record.getRecordId());
                                    selectGdbTraceEditor(sessionId, recordId);
                                    break;
                                }
                            } catch (InterruptedException e) {
                                Activator.logError("Interruption exception", e); //$NON-NLS-1$
                            } catch (ExecutionException e) {
                                Activator.logError("GDB exception", e); //$NON-NLS-1$
                            } catch (RejectedExecutionException e) {
                                Activator.logError("Request rejected exception", e); //$NON-NLS-1$
                            } catch (TimeoutException e) {
                                Activator.logError("Timeout", e); //$NON-NLS-1$
                            } finally {
                                tracker.dispose();
                            }
                            // else not DSF-GDB or GDB < 7.2
                        }
                    }
                    // else not DSF
                }
            }
        }
    } //  class DsfGdbPlatformEventListener

    /**
     * Constructor for DsfGdbAdaptor.  This is used when we want to launch a
     * DSF-GDB session and use it as source in our tracing perspective.
     * i.e. when launching from the Project Explorer
     *
     * @param trace  the GDB trace
     * @param gdbExec  GDB executable.  Must be version 7.2 or later.
     * @param traceFile  previously generated GDB tracepoint file
     * @param tracedExecutable  executable that was used to generate the tracefile
     *  workspace, where the traced executable was taken from.
     */
    public DsfGdbAdaptor(GdbTrace trace, String gdbExec, String traceFile, String tracedExecutable) {
        this.fGdbTrace = trace;
        this.gdb72Executable = gdbExec;
        this.fTraceFilePath = traceFile;
        this.fTraceFile = traceFile.substring(traceFile.lastIndexOf(IPath.SEPARATOR) + 1);
        this.tracedExecutable = tracedExecutable;

        try {
            launchDGBPostMortemTrace();
        } catch (CoreException e) {
            e.printStackTrace();
        }
    }

    /**
     * Builds a launcher and launches a Post-mortem GDB session, based on a
     * previously-gathered GDB Tracepoint file.  The information used to
     * create the launcher is provided to the constructor of this class,
     * at instantiation time.
     * <p>
     * Note: Requires GDB 7.2 or later
     */
    private void launchDGBPostMortemTrace() throws CoreException {
        fIsTimeoutEnabled = Platform.getPreferencesService().getBoolean(GdbPlugin.PLUGIN_ID, IGdbDebugPreferenceConstants.PREF_COMMAND_TIMEOUT, false, null);
        if (fIsTimeoutEnabled) {
            fTimeout = Platform.getPreferencesService().getInt(GdbPlugin.PLUGIN_ID, IGdbDebugPreferenceConstants.PREF_COMMAND_TIMEOUT_VALUE, IGdbDebugPreferenceConstants.COMMAND_TIMEOUT_VALUE_DEFAULT, null);
        }

        ILaunchConfigurationType configType = DebugPlugin
                .getDefault()
                .getLaunchManager()
                .getLaunchConfigurationType("org.eclipse.cdt.launch.postmortemLaunchType"); //$NON-NLS-1$
        ILaunchConfigurationWorkingCopy wc = configType.newInstance(null, fTraceFile);

        wc.setAttribute("org.eclipse.cdt.dsf.gdb.DEBUG_NAME", gdb72Executable); //$NON-NLS-1$
        wc.setAttribute("org.eclipse.cdt.dsf.gdb.POST_MORTEM_TYPE", "TRACE_FILE"); //$NON-NLS-1$ //$NON-NLS-2$
        wc.setAttribute("org.eclipse.cdt.launch.ATTR_BUILD_BEFORE_LAUNCH_ATTR", 0); //$NON-NLS-1$
        wc.setAttribute("org.eclipse.cdt.launch.COREFILE_PATH", fTraceFilePath); //$NON-NLS-1$
        wc.setAttribute("org.eclipse.cdt.launch.DEBUGGER_START_MODE", "core"); //$NON-NLS-1$ //$NON-NLS-2$
        wc.setAttribute("org.eclipse.cdt.launch.PROGRAM_NAME", tracedExecutable); //$NON-NLS-1$
        // So that the GDB launch is synchronous
        wc.setAttribute("org.eclipse.debug.ui.ATTR_LAUNCH_IN_BACKGROUND", false); //$NON-NLS-1$

        if (!sourceLocator.isEmpty()) {
            wc.setAttribute("org.eclipse.debug.core.source_locator_memento", sourceLocator); //$NON-NLS-1$
        }

        // Launch GDB session
        fLaunch = wc.doSave().launch("debug", null); //$NON-NLS-1$
        isTerminating = false;

        if (fLaunch instanceof GdbLaunch) {
            fSessionId = ((GdbLaunch) fLaunch).getSession().getId();
        }

        fDsfSession = ((GdbLaunch) fLaunch).getSession();
        fDsfSession.addServiceEventListener(this, null);

        // Find the number of frames contained in the tracepoint file
        fNumberOfFrames = findNumFrames();
    }

    /**
     * This method terminates the current DSF-GDB session
     */
    public void dispose() {
        if (fLaunch != null && fLaunch.canTerminate() && !isTerminating) {
            isTerminating = true;
            try {
                fLaunch.terminate();
            } catch (DebugException e) {
                e.printStackTrace();
            }
            fLaunch = null;
        }
    }

    /**
     * This method will try (once per call) to get the number of GDB tracepoint
     * frames for the current session, from DSF-GDB, until it succeeds at
     * getting an amount different than zero.
     *
     * @return The number of frames in current session or zero if unsuccessful
     */
    public int getNumberOfFrames() {
        if (fNumberOfFrames == 0) {
            fNumberOfFrames = findNumFrames();
        }
        return fNumberOfFrames;
    }


    /**
     * Wrapper around the selecting of a frame and the reading of its
     * information. this is a work-around for the potential problem of
     * concurrent access to these functions by more than one thread,
     * where two clients might interfere with each other.
     * <p>
     * Note: We also try to get the tracepoint info here, if it's not
     * already filled-in.
     *
     * @param  rank a long corresponding to the number of the frame to be
     * selected and read
     * @return A GdbTraceEvent object, or null in case of failure.
     */
    public synchronized GdbTraceEvent selectAndReadFrame(final long rank) {
        // lazy init of tracepoints info
        if(fTpInfo.isEmpty()) {
            getTracepointInfo();
        }
        if (selectDataFrame(rank, false)) {
            GdbTraceEvent event = getTraceFrameData(rank);
            long ts = event.getTimestamp().getValue();
            if (ts == rank) {
                return event;
            }
        }
        return null;
    }


    /**
     * This class implements a best-effort look-up of the detailed tracepoint
     * information (source code filename, line number, etc...).
     */
    private void getTracepointInfo() {

        // Get the latest executor/service tracker
        final DsfExecutor executor = DsfSession.getSession(fSessionId).getExecutor();
        final DsfServicesTracker tracker = new DsfServicesTracker(GdbTraceCorePlugin.getBundleContext(), fSessionId);

        Query<Object> selectRecordQuery = new Query<Object>() {
            @Override
            public void execute(final DataRequestMonitor<Object> drm) {

                // A breakpoint is no longer GDB-global but tied to a specific process
                // So we need to find our process and the ask for its breakpoints
                IMIProcesses procService = tracker.getService(IMIProcesses.class);
                final ICommandControlService cmdControl = tracker.getService(ICommandControlService.class);
                if (procService == null || cmdControl == null) {
                    drm.setStatus(new Status(IStatus.ERROR, GdbPlugin.PLUGIN_ID, IDsfStatusConstants.INTERNAL_ERROR, "Could not find necessary services", null)); //$NON-NLS-1$
                    drm.done();
                    return;
                }

                ITraceTargetDMContext context = (ITraceTargetDMContext) cmdControl.getContext();
                ICommandControlDMContext cmdControlDMC = DMContexts.getAncestorOfType(context, ICommandControlDMContext.class);

                procService.getProcessesBeingDebugged(
                        cmdControlDMC,
                        new DataRequestMonitor<IDMContext[]>(executor, drm) {
                            @Override
                            protected void handleSuccess() {
                                assert getData() != null;
                                assert getData().length == 1;
                                if (getData() == null || getData().length < 1) {
                                    drm.done();
                                    return;
                                }

                                // Choose the first process for now, until gdb can tell
                                // us which process the trace record is associated with.
                                IContainerDMContext containerDMC = (IContainerDMContext)(getData()[0]);
                                IBreakpointsTargetDMContext bpTargetDMC = DMContexts.getAncestorOfType(containerDMC , IBreakpointsTargetDMContext.class);

                                CommandFactory cmdFactory = tracker.getService(IMICommandControl.class).getCommandFactory();
                                IBreakpoints bpService = tracker.getService(MIBreakpoints.class);
                                if (cmdFactory == null || bpService == null) {
                                    drm.setStatus(new Status(IStatus.ERROR, GdbPlugin.PLUGIN_ID, IDsfStatusConstants.INTERNAL_ERROR, "Could not find necessary services", null)); //$NON-NLS-1$
                                    drm.done();
                                    return;
                                }

                                // Execute the command
                                cmdControl.queueCommand(cmdFactory.createMIBreakList(bpTargetDMC),
                                        new DataRequestMonitor<MIBreakListInfo>(executor, drm) {
                                    @Override
                                    protected void handleSuccess() {
                                        MIBreakpoint[] breakpoints = getData().getMIBreakpoints();
                                        for (int i = 0; i < breakpoints.length; i++) {
                                            MIBreakpointDMData breakpoint = new MIBreakpointDMData(breakpoints[i]);
                                            String type = breakpoint.getBreakpointType();
                                            // Only save info if the current breakpoint is of type tracepoint
                                            if(type.compareTo(MIBreakpoints.TRACEPOINT) == 0 ) {
                                                fTpInfo.put(new Integer(breakpoint.getReference()), breakpoint);
                                            }
                                        }
                                        drm.done();
                                    }
                                });
                            }
                        });
            }
        };
        try {
            executor.execute(selectRecordQuery);
            if (fIsTimeoutEnabled) {
                selectRecordQuery.get(fTimeout, TimeUnit.MILLISECONDS); // blocks until time out
            } else {
                selectRecordQuery.get(); // blocks
            }
        } catch (InterruptedException e) {
            Activator.logError("Interruption exception", e); //$NON-NLS-1$
        } catch (ExecutionException e) {
            Activator.logError("GDB exception", e); //$NON-NLS-1$
        } catch (RejectedExecutionException e) {
            Activator.logError("Request rejected exception", e); //$NON-NLS-1$
        } catch (TimeoutException e) {
            Activator.logError("Timeout", e); //$NON-NLS-1$
        } finally {
            tracker.dispose();
        }
    }

    /**
     * Returns the number of frames contained in currently loaded tracepoint GDB
     * session.
     * <p>
     * Note: A postmortem GDB session must be started before calling
     *         this method
     *
     * @return the number of frames contained in currently loaded tracepoint GDB
     *         session or zero in case of error
     */
    private synchronized int findNumFrames() {
        int frameNum = 0;

        if (DsfSession.getSession(fSessionId) == null) {
            return 0;
        }

        final DsfExecutor executor = DsfSession.getSession(fSessionId)
                .getExecutor();
        final DsfServicesTracker tracker = new DsfServicesTracker(
                GdbTraceCorePlugin.getBundleContext(), fSessionId);

        Query<ITraceStatusDMData> selectRecordQuery = new Query<ITraceStatusDMData>() {
            @Override
            public void execute(
                    final DataRequestMonitor<ITraceStatusDMData> queryRm) {
                final IGDBTraceControl traceControl = tracker
                        .getService(IGDBTraceControl.class);

                final ICommandControlService commandControl = tracker
                        .getService(ICommandControlService.class);
                final ITraceTargetDMContext dmc = (ITraceTargetDMContext) commandControl
                        .getContext();

                if (traceControl != null) {
                    traceControl.getTraceStatus(dmc, queryRm);
                } else {
                    queryRm.done();
                }
            }
        };
        try {
            executor.execute(selectRecordQuery);
            ITraceStatusDMData data;
            if (fIsTimeoutEnabled) {
                data = selectRecordQuery.get(fTimeout, TimeUnit.MILLISECONDS); // blocks until time out
            } else {
                data = selectRecordQuery.get(); // blocks
            }

            frameNum = data.getNumberOfCollectedFrame();
        } catch (InterruptedException e) {
            Activator.logError("Interruption exception", e); //$NON-NLS-1$
        } catch (ExecutionException e) {
            Activator.logError("GDB exception", e); //$NON-NLS-1$
        } catch (RejectedExecutionException e) {
            Activator.logError("Request rejected exception", e); //$NON-NLS-1$
        } catch (TimeoutException e) {
            Activator.logError("Timeout", e); //$NON-NLS-1$
        } finally {
            tracker.dispose();
        }
        return frameNum;
    }

    /**
     * This method uses the DSF-GDB interface to select a given frame number
     * in the current GDB tracepoint session.
     *
     * @param rank the rank of the tracepoint frame to select.
     * @param update true if visualization should be updated
     * @return boolean true if select worked.
     */
    public boolean selectDataFrame(final long rank, final boolean update) {
        boolean status = true;

        final DsfSession dsfSession = DsfSession.getSession(fSessionId);
        if (dsfSession == null) {
            return false;
        }

        if (update) {
            /*
             * Clear the selection to ensure that the new selection is not
             * prevented from overriding the current selection by the DSF
             * selection policy. This could be removed when DSF provides
             * an API to force the trace record selection in the Debug view.
             */
            Display.getDefault().syncExec(new Runnable() {
                @Override
                public void run() {
                    for (IWorkbenchWindow wbWindow : PlatformUI.getWorkbench().getWorkbenchWindows()) {
                        for (IWorkbenchPage wbPage : wbWindow.getPages()) {
                            IViewPart vp = wbPage.findView(IDebugUIConstants.ID_DEBUG_VIEW);
                            if (vp instanceof AbstractDebugView) {
                                Viewer viewer = ((AbstractDebugView) vp).getViewer();
                                if (viewer instanceof ITreeModelViewer) {
                                    ((ITreeModelViewer) viewer).setSelection(StructuredSelection.EMPTY, false, true);
                                }
                            }
                        }
                    }
                }
            });
        }

        final DsfExecutor executor = dsfSession.getExecutor();
        final DsfServicesTracker tracker = new DsfServicesTracker(GdbTraceCorePlugin.getBundleContext(), fSessionId);

        Query<Object> selectRecordQuery = new Query<Object>() {
            @Override
            public void execute(final DataRequestMonitor<Object> queryRm) {
                final IGDBTraceControl traceControl = tracker.getService(IGDBTraceControl.class);

                final ICommandControlService commandControl = tracker.getService(ICommandControlService.class);
                final ITraceTargetDMContext dmc = (ITraceTargetDMContext) commandControl.getContext();

                if (traceControl != null) {
                    ITraceRecordDMContext newCtx = traceControl.createTraceRecordContext(dmc, Integer.toString((int) rank));
                    if (update) {
                        dsfSession.dispatchEvent(new TraceRecordSelectedChangedEvent(newCtx), new Hashtable<String, String>());
                    }
                    traceControl.selectTraceRecord(newCtx, queryRm);
                } else {
                    queryRm.done();
                }
            }
        };
        try {
            executor.execute(selectRecordQuery);
            if (fIsTimeoutEnabled) {
                selectRecordQuery.get(fTimeout, TimeUnit.MILLISECONDS); // blocks until time out
            } else {
                selectRecordQuery.get(); // blocks
            }
        } catch (InterruptedException e) {
            status = false;
            Activator.logError("Interruption exception", e); //$NON-NLS-1$
        } catch (ExecutionException e) {
            status = false;
            Activator.logError("GDB exception", e); //$NON-NLS-1$
        } catch (RejectedExecutionException e) {
            status = false;
            Activator.logError("Request rejected exception", e); //$NON-NLS-1$
        } catch (TimeoutException e) {
            status = false;
            Activator.logError("Timeout", e); //$NON-NLS-1$
        } finally {
            tracker.dispose();
        }
        return status;
    }

    /**
     * This method uses DSF-GDB to read the currently selected GDB tracepoint
     * data frame.   An object of type GdbTraceEvent is build based on the
     * information contained in the data frame and returned to the caller.
     * <p>
     * NOTE : A frame must be selected before calling this method!
     *
     * @param rank  for internal purposes - does <b>not</b> control which
     * frame will be read!
     * @return parsed tp frame, in the form of a GdbTraceEvent
     */
    private GdbTraceEvent getTraceFrameData(final long rank) {

        if (DsfSession.getSession(fSessionId) == null) {
            return null;
        }

        final DsfExecutor executor = DsfSession.getSession(fSessionId).getExecutor();
        final DsfServicesTracker tracker = new DsfServicesTracker(GdbTraceCorePlugin.getBundleContext(), fSessionId);

        Query<ITraceRecordDMData> getFrameDataQuery = new Query<ITraceRecordDMData>() {
            @Override
            public void execute(final DataRequestMonitor<ITraceRecordDMData> rm) {
                final IGDBTraceControl traceControl = tracker.getService(IGDBTraceControl.class);

                final ICommandControlService commandControl = tracker.getService(ICommandControlService.class);
                final ITraceTargetDMContext dmc = (ITraceTargetDMContext) commandControl.getContext();

                if (traceControl != null) {
                    traceControl.getCurrentTraceRecordContext(dmc,
                            new DataRequestMonitor<ITraceRecordDMContext>(executor, rm) {
                        @Override
                        protected void handleSuccess() {
                            traceControl.getTraceRecordData(getData(), rm);
                        }
                    });
                } else {
                    rm.done();
                }
            }
        };
        try {
            // Execute the above query
            executor.execute(getFrameDataQuery);
            ITraceRecordDMData data;
            if (fIsTimeoutEnabled) {
                data = getFrameDataQuery.get(fTimeout, TimeUnit.MILLISECONDS); // blocking call until time out
            } else {
                data = getFrameDataQuery.get();
            }

            if (data == null) {
                return null;
            }

            String ts = data.getTimestamp();
            if (ts == null) {
                ts = "0"; //$NON-NLS-1$
            }

            // get corresponding TP data
            String tmfEventRef;
            MIBreakpointDMData bp =  fTpInfo.get(Integer.valueOf(data.getTracepointNumber()));
            if (bp != null) {
                tmfEventRef = bp.getFileName() + ":" + bp.getLineNumber() + " :: " + bp.getFunctionName(); //$NON-NLS-1$ //$NON-NLS-2$
            }
            else {
                tmfEventRef = tracedExecutable;
            }

            GdbTraceEventContent evContent = new GdbTraceEventContent(
                    data.getContent(),
                    Integer.parseInt(data.getTracepointNumber()),
                    Integer.parseInt(data.getRecordId()));

            GdbTraceEvent ev = new GdbTraceEvent(fGdbTrace,
                    new TmfTimestamp(Integer.parseInt(data.getRecordId())),
                    "Tracepoint: " + data.getTracepointNumber() + ", Frame: " + data.getRecordId(),  //$NON-NLS-1$ //$NON-NLS-2$
                    tmfEventType,
                    evContent,
                    tmfEventRef);

            return ev;

        } catch (InterruptedException e) {
            return createExceptionEvent(rank, "Interruption exception"); //$NON-NLS-1$
        } catch (java.util.concurrent.ExecutionException e) {
            return createExceptionEvent(rank, "GDB exception"); //$NON-NLS-1$
        } catch (RejectedExecutionException e) {
            return createExceptionEvent(rank, "Request rejected exception"); //$NON-NLS-1$
        } catch (TimeoutException e) {
            return createExceptionEvent(rank, "Timeout"); //$NON-NLS-1$
        }

        finally {
            tracker.dispose();
        }
    }

    /**
     * This is a helper method for getTraceFrameData, to create for it a
     * "best effort" GdbTraceEvent when a problem occurs during the reading.
     *
     * @param rank long containing the number of the frame where the problem occurred
     * @param message String containing a brief explanation of problem.
     * @return a GdbTraceEvent object, filled as best as possible
     */
    private GdbTraceEvent createExceptionEvent(final long rank, final String message) {
        // get corresponding TP data
        String tmfEventRef;
        String tmfEventSrc;
        MIBreakpointDMData bp =  fTpInfo.get(rank);
        if (bp != null) {
            tmfEventRef = bp.getFileName() + ":" + bp.getLineNumber() + " :: " + bp.getFunctionName(); //$NON-NLS-1$ //$NON-NLS-2$
            tmfEventSrc = bp.getFileName() + " :: " + bp.getFunctionName() + ", line: " + bp.getLineNumber(); //$NON-NLS-1$ //$NON-NLS-2$
        }
        else {
            tmfEventRef = tracedExecutable;
            tmfEventSrc = "Tracepoint: n/a"; //$NON-NLS-1$
        }

        GdbTraceEventContent evContent = new GdbTraceEventContent("ERROR: " + message, 0, 0); //$NON-NLS-1$

        GdbTraceEvent ev = new GdbTraceEvent(fGdbTrace,
                new TmfTimestamp(rank),
                tmfEventSrc,
                tmfEventType,
                evContent,
                tmfEventRef);

        return ev;
    }

    /**
     * @return DSF-GDB session id of the current session.
     */
    public String getSessionId() {
        return fSessionId;
    }

    /**
     * Handler method that catches the DSF "record selected changed" event.
     * It in turn creates a TMF "time sync" signal.
     * @param event TraceRecordSelectedChangedEvent:  The DSF event.
     */
    @DsfServiceEventHandler
    public void handleDSFRecordSelectedEvents(final ITraceRecordSelectedChangedDMEvent event) {
        if (event instanceof TraceRecordSelectedChangedEvent) {
            TraceRecordSelectedChangedEvent traceEvent = (TraceRecordSelectedChangedEvent) event;
            ITraceRecordDMContext context = traceEvent.getDMContext();
            final String reference = context.getRecordId();
            if (reference != null) {
                int recordId = Integer.parseInt(reference);
                selectGdbTraceEditor(context.getSessionId(), recordId);
            }
        }
    }

    private static void closeGdbTraceEditor(final String sessionId) {
        Display.getDefault().asyncExec(new Runnable() {
            @Override
            public void run() {
                for (IWorkbenchWindow wbWindow : PlatformUI.getWorkbench().getWorkbenchWindows()) {
                    for (IWorkbenchPage wbPage : wbWindow.getPages()) {
                        for (IEditorReference editorReference : wbPage.getEditorReferences()) {
                            IEditorPart editor = editorReference.getEditor(false);
                            if (editor instanceof ITmfTraceEditor) {
                                ITmfTrace trace = ((ITmfTraceEditor) editor).getTrace();
                                if (trace instanceof GdbTrace) {
                                    if (((GdbTrace) trace).getDsfSessionId().equals(sessionId)) {
                                        wbPage.closeEditor(editor, false);
                                    }
                                }
                            }
                        }
                    }
                }
            }
        });
    }

    private static void selectGdbTraceEditor(final String sessionId, final int recordId) {
        Display.getDefault().asyncExec(new Runnable() {
            @Override
            public void run() {
                for (IWorkbenchWindow wbWindow : PlatformUI.getWorkbench().getWorkbenchWindows()) {
                    for (IWorkbenchPage wbPage : wbWindow.getPages()) {
                        for (IEditorReference editorReference : wbPage.getEditorReferences()) {
                            IEditorPart editor = editorReference.getEditor(false);
                            if (editor instanceof ITmfTraceEditor) {
                                ITmfTrace trace = ((ITmfTraceEditor) editor).getTrace();
                                if (trace instanceof GdbTrace) {
                                    if (((GdbTrace) trace).getDsfSessionId().equals(sessionId)) {
                                        wbPage.bringToTop(editor);
                                        if (recordId != -1) {
                                            gotoRank(editor, recordId);
                                        }
                                        return;
                                    }
                                } else if (trace instanceof TmfExperiment) {
                                    TmfExperiment experiment = (TmfExperiment) trace;
                                    int nbTraces = experiment.getTraces().length;
                                    for (int i = 0; i < nbTraces; i++) {
                                        GdbTrace gdbTrace = (GdbTrace) experiment.getTraces()[i];
                                        if (gdbTrace.getDsfSessionId().equals(sessionId)) {
                                            wbPage.bringToTop(editor);
                                            if (recordId != -1) {
                                                int rank = recordId * nbTraces + i;
                                                gotoRank(editor, rank);
                                            }
                                            return;
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        });
    }

    private static void gotoRank(IEditorPart editor, int rank) {
        IEditorInput editorInput = editor.getEditorInput();
        if (editorInput instanceof IFileEditorInput) {
            IFile file = ((IFileEditorInput) editorInput).getFile();
            try {
                final IMarker marker = file.createMarker(IMarker.MARKER);
                marker.setAttribute(IMarker.LOCATION, (Integer) rank);
                IDE.gotoMarker(editor, marker);
                marker.delete();
            } catch (CoreException e) {
                e.printStackTrace();
            }
        }
    }
}