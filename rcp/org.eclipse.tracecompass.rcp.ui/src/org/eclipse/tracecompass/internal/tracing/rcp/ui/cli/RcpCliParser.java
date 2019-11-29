/**********************************************************************
 * Copyright (c) 2013, 2014 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Matthew Khouzam- Initial API and implementation
 **********************************************************************/

package org.eclipse.tracecompass.internal.tracing.rcp.ui.cli;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.MultiStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Status;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.widgets.Display;
import org.eclipse.tracecompass.internal.provisional.tmf.cli.core.parser.CliCommandLine;
import org.eclipse.tracecompass.internal.provisional.tmf.cli.core.parser.CliOption;
import org.eclipse.tracecompass.internal.provisional.tmf.cli.core.parser.ICliParser;
import org.eclipse.tracecompass.internal.tracing.rcp.ui.TracingRcpPlugin;
import org.eclipse.tracecompass.internal.tracing.rcp.ui.messages.Messages;
import org.eclipse.tracecompass.tmf.core.TmfCommonConstants;
import org.eclipse.tracecompass.tmf.core.component.TmfComponent;
import org.eclipse.tracecompass.tmf.core.project.model.TmfTraceType;
import org.eclipse.tracecompass.tmf.core.project.model.TraceTypeHelper;
import org.eclipse.tracecompass.tmf.core.signal.TmfSignalHandler;
import org.eclipse.tracecompass.tmf.core.signal.TmfTraceOpenedSignal;
import org.eclipse.tracecompass.tmf.core.signal.TmfTraceSelectedSignal;
import org.eclipse.tracecompass.tmf.core.trace.ITmfTrace;
import org.eclipse.tracecompass.tmf.core.trace.TmfTraceManager;
import org.eclipse.tracecompass.tmf.ui.project.model.TmfCommonProjectElement;
import org.eclipse.tracecompass.tmf.ui.project.model.TmfOpenTraceHelper;
import org.eclipse.tracecompass.tmf.ui.project.model.TmfProjectElement;
import org.eclipse.tracecompass.tmf.ui.project.model.TmfProjectRegistry;
import org.eclipse.tracecompass.tmf.ui.project.model.TmfTraceFolder;

/**
 * Command line parser
 *
 * @author Matthew Khouzam
 * @author Bernd Hufmann
 */
@NonNullByDefault
public class RcpCliParser implements ICliParser {

    private static final String OPTION_COMMAND_LINE_OPEN_SHORT = "o"; //$NON-NLS-1$
    private static final String OPTION_COMMAND_LINE_OPEN_LONG = "open"; //$NON-NLS-1$
    private static final String OPTION_COMMAND_LINE_OPEN_DESCRIPTION = Objects.requireNonNull(Messages.CliParser_OpenTraceDescription);

    private static final String OPTION_COMMAND_LINE_LIST_SHORT = "l"; //$NON-NLS-1$
    private static final String OPTION_COMMAND_LINE_LIST_LONG = "list"; //$NON-NLS-1$
    private static final String OPTION_COMMAND_LINE_LIST_DESCRIPTION = Objects.requireNonNull(Messages.CliParser_ListCapabilitiesDescription);

    private final List<CliOption> fOptions;

    /**
     * Constructor
     */
    public RcpCliParser() {
        fOptions = new ArrayList<>();
        fOptions.add(CliOption.createSimpleOption(OPTION_COMMAND_LINE_LIST_SHORT, OPTION_COMMAND_LINE_LIST_LONG, OPTION_COMMAND_LINE_LIST_DESCRIPTION));
        fOptions.add(CliOption.createOptionWithArgs(OPTION_COMMAND_LINE_OPEN_SHORT, OPTION_COMMAND_LINE_OPEN_LONG, OPTION_COMMAND_LINE_OPEN_DESCRIPTION, true, true, "path")); //$NON-NLS-1$
    }

    @Override
    public boolean preStartup(CliCommandLine commandLine) {
        if (commandLine.hasOption(OPTION_COMMAND_LINE_LIST_SHORT)) {
            System.out.println(Messages.CliParser_ListSupportedTraceTypes);
            System.out.println();
            for (TraceTypeHelper helper : TmfTraceType.getTraceTypeHelpers()) {
                System.out.println(helper.getName() + ": " + helper.getTraceTypeId()); //$NON-NLS-1$
                System.out.println();
            }
            return true;
        }
        return false;
    }

    @Override
    public IStatus workspaceLoading(CliCommandLine commandLine, IProgressMonitor monitor) {
        if (commandLine.hasOption(OPTION_COMMAND_LINE_OPEN_SHORT)) {
            IProject defaultProject = createDefaultProject();
            IStatus returnStatus = Status.OK_STATUS;
            List<Path> allTracePaths = replaceHomeDir(commandLine.getOptionValues(OPTION_COMMAND_LINE_OPEN_SHORT));
            List<Path> tracePaths = new ArrayList<>();
            // Remove paths that do not exist
            for (Path tracePath : allTracePaths) {
                if (!tracePath.toFile().exists()) {
                    returnStatus = addStatus(returnStatus, new Status(IStatus.ERROR, TracingRcpPlugin.PLUGIN_ID, NLS.bind(Messages.CliParser_TraceDoesNotExist, tracePath)));
                } else {
                    tracePaths.add(tracePath);
                }
            }
            Collection<TmfCommonProjectElement> existingTraceElement = findElements(tracePaths);
            if (monitor.isCanceled()) {
                return Status.CANCEL_STATUS;
            }
            WaitAllTracesOpened service = openTraceIfNecessary(defaultProject, tracePaths, existingTraceElement, monitor);
            returnStatus = addStatus(returnStatus, service.waitForCompletion(monitor));
            service.dispose();
            return returnStatus;
        }
        return Status.OK_STATUS;
    }

    /**
     * Find in the workspace the trace elements that corresponds to the traces
     * to open.
     *
     * @param tracePaths
     *            The path of the trace to open
     * @return The existing trace elements or <code>null</code> if the trace
     *         does not exist in the workspace
     */
    private static Collection<TmfCommonProjectElement> findElements(List<Path> tracePaths) {
        IProject[] projects = ResourcesPlugin.getWorkspace().getRoot().getProjects(0);
        List<TmfCommonProjectElement> wanted = new ArrayList<>();

        for (IProject project : projects) {
            TmfProjectElement pElement = TmfProjectRegistry.getProject(project);
            if (pElement != null) {
                List<TmfCommonProjectElement> tElements = new ArrayList<>();
                TmfTraceFolder tracesFolder = pElement.getTracesFolder();
                if (tracesFolder != null) {
                    tElements.addAll(tracesFolder.getTraces());
                }

                for (TmfCommonProjectElement tElement : tElements) {
                    // If this element is for the same trace, return it
                    Path elementPath = new Path(tElement.getLocation().getPath());
                    if (tracePaths.contains(elementPath)) {
                        wanted.add(tElement);
                        tracePaths.remove(elementPath);

                    }
                }
            }
        }
        return wanted;
    }

    private static List<Path> replaceHomeDir(String[] tracePaths) {
        List<Path> tracesToOpen = new ArrayList<>();
        for (String tracePath : tracePaths) {
            String traceToOpen = tracePath;
            String userHome = System.getProperty("user.home"); //$NON-NLS-1$
            // In case the application was not started on the shell, expand ~ to
            // home directory
            if ((traceToOpen != null) && traceToOpen.startsWith("~/") && (userHome != null)) { //$NON-NLS-1$
                traceToOpen = traceToOpen.replaceFirst("^~", userHome); //$NON-NLS-1$
            }
            tracesToOpen.add(new Path(traceToOpen));
        }
        return tracesToOpen;
    }

    /**
     * A class that handles the traces once they are opened and keep a countdown
     * on the traces still to open.
     *
     * This class needs to be public so the signal manager can access its
     * methods.
     *
     * @author Geneviève Bastien
     */
    public static class WaitAllTracesOpened extends TmfComponent {

        private final List<Path> fTracePath = new ArrayList<>();
        private final CountDownLatch fFinishedLatch;
        private IStatus fStatus = Status.OK_STATUS;

        private WaitAllTracesOpened(List<Path> tracePaths, Collection<TmfCommonProjectElement> existingTraces) {
            super("Waiting for traces opening"); //$NON-NLS-1$
            // See if any of the traces is already opened
            synchronized (fTracePath) {
                fTracePath.addAll(tracePaths);
                for (TmfCommonProjectElement element : existingTraces) {
                    fTracePath.add(new Path(element.getLocation().getPath()));
                }

                fFinishedLatch = new CountDownLatch(fTracePath.size());
                Set<ITmfTrace> openedTraces = TmfTraceManager.getInstance().getOpenedTraces();
                for (ITmfTrace trace : openedTraces) {
                    Path path = new Path(trace.getPath());
                    if (fTracePath.contains(path)) {
                        fTracePath.remove(path);
                        fFinishedLatch.countDown();
                    }
                }
            }
        }

        /**
         * Handles a trace opened signal
         *
         * @param signal The trace opened signal
         */
        @TmfSignalHandler
        public synchronized void traceOpened(final TmfTraceOpenedSignal signal) {
            final ITmfTrace trace = signal.getTrace();
            Path path = new Path(trace.getPath());
            if (fTracePath.contains(path)) {
                fTracePath.remove(path);
                fFinishedLatch.countDown();
            }
        }

        /**
         * Handles a trace selected signal
         *
         * FIXME: Is this method necessary? Or doesn't open cover it all
         *
         * @param signal
         *            The trace selected signal
         */
        @TmfSignalHandler
        public synchronized void traceSelected(final TmfTraceSelectedSignal signal) {
            final ITmfTrace trace = signal.getTrace();
            Path path = new Path(trace.getPath());
            if (fTracePath.contains(path)) {
                fTracePath.remove(path);
                fFinishedLatch.countDown();
            }
        }

        private IStatus waitForCompletion(IProgressMonitor monitor) {
            try {
                while (!fFinishedLatch.await(500, TimeUnit.MILLISECONDS)) {
                    if (monitor.isCanceled()) {
                        return addStatus(fStatus, new Status(IStatus.CANCEL, TracingRcpPlugin.PLUGIN_ID, NLS.bind(Messages.CliParser_CancelStillWaiting, fTracePath)));
                    }
                }
            } catch (InterruptedException e) {
                // Do nothing
            }
            return fStatus;
        }

        private void registerException(Path path, Throwable throwable) {
            registerStatus(path, new Status(IStatus.ERROR, TracingRcpPlugin.PLUGIN_ID, getName(), throwable));
        }

        private void registerStatus(Path path, IStatus status) {
            synchronized (fTracePath) {
                if (fTracePath.contains(path)) {
                    fTracePath.remove(path);
                    fFinishedLatch.countDown();
                    fStatus = addStatus(fStatus, status);
                }
            }
        }
    }

    private static IStatus addStatus(IStatus currentStatus, IStatus status) {
        if (currentStatus.isOK()) {
            return status;
        } else if (currentStatus.isMultiStatus()) {
            ((MultiStatus) currentStatus).add(status);
            return currentStatus;
        } else {
            MultiStatus baseStatus = new MultiStatus(TracingRcpPlugin.PLUGIN_ID, 1, Messages.CliParser_OpeningTraces, null);
            baseStatus.add(currentStatus);
            baseStatus.add(status);
            return baseStatus;
        }
    }

    private static WaitAllTracesOpened openTraceIfNecessary(IProject project, List<Path> tracePaths, Collection<TmfCommonProjectElement> existingTraces, IProgressMonitor monitor) {
        WaitAllTracesOpened waitService = new WaitAllTracesOpened(tracePaths, existingTraces);

        if (!existingTraces.isEmpty()) {
            Display.getDefault().asyncExec(() -> {
                for (TmfCommonProjectElement existingTrace : existingTraces) {
                    if (monitor.isCanceled()) {
                        return;
                    }
                    try {
                        IStatus status = TmfOpenTraceHelper.openFromElement(existingTrace);
                        if (!status.isOK() || (status.getCode() != IStatus.OK)) {
                            waitService.registerStatus(new Path(existingTrace.getLocation().getPath()), status);
                        }
                    } catch (Exception e) {
                        // Some other exception occurred, the trace will likely
                        // never be opened, catch the exception and send to
                        // waitService
                        waitService.registerException(new Path(existingTrace.getLocation().getPath()), e);
                    }
                }
            });
        }

        if (tracePaths.isEmpty()) {
            return waitService;
        }

        TmfTraceFolder destinationFolder = TmfProjectRegistry.getProject(project, true).getTracesFolder();
        if (!tracePaths.isEmpty()) {
            Display.getDefault().asyncExec(() -> {
                for (Path tracePath : tracePaths) {
                    if (monitor.isCanceled()) {
                        return;
                    }
                    try {
                        IStatus status = TmfOpenTraceHelper.openTraceFromPath(destinationFolder, tracePath.toOSString(), TracingRcpPlugin.getDefault().getWorkbench().getActiveWorkbenchWindow().getShell());
                        if (!status.isOK() || (status.getCode() != IStatus.OK)) {
                            waitService.registerStatus(tracePath, status);
                        }
                    } catch (CoreException e) {
                        waitService.registerException(tracePath, e);
                    }
                }
            });
        }
        return waitService;
    }

    private static IProject createDefaultProject() {
        return TmfProjectRegistry.createProject(TmfCommonConstants.DEFAULT_TRACE_PROJECT_NAME, null, new NullProgressMonitor());
    }

    @Override
    public List<CliOption> getCmdLineOptions() {
        return fOptions;
    }

}