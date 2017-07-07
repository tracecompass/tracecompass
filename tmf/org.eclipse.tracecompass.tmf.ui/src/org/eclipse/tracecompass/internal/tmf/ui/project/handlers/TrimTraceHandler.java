/*******************************************************************************
 * Copyright (c) 2016 EfficiOS Inc., Alexandre Montplaisir
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/

package org.eclipse.tracecompass.internal.tmf.ui.project.handlers;

import static org.eclipse.tracecompass.common.core.NonNullUtils.checkNotNull;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.SubMonitor;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.TreeSelection;
import org.eclipse.swt.widgets.DirectoryDialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.tracecompass.internal.tmf.ui.Activator;
import org.eclipse.tracecompass.internal.tmf.ui.project.operations.TmfWorkspaceModifyOperation;
import org.eclipse.tracecompass.statesystem.core.ITmfStateSystem;
import org.eclipse.tracecompass.statesystem.core.snapshot.StateSnapshot;
import org.eclipse.tracecompass.tmf.core.analysis.IAnalysisModule;
import org.eclipse.tracecompass.tmf.core.statesystem.ITmfAnalysisModuleWithStateSystems;
import org.eclipse.tracecompass.tmf.core.timestamp.TmfTimeRange;
import org.eclipse.tracecompass.tmf.core.trace.ITmfTrace;
import org.eclipse.tracecompass.tmf.core.trace.TmfTraceManager;
import org.eclipse.tracecompass.tmf.core.trace.trim.ITmfTrimmableTrace;
import org.eclipse.tracecompass.tmf.ui.dialog.DirectoryDialogFactory;
import org.eclipse.tracecompass.tmf.ui.project.model.TmfOpenTraceHelper;
import org.eclipse.tracecompass.tmf.ui.project.model.TmfProjectElement;
import org.eclipse.tracecompass.tmf.ui.project.model.TmfTraceElement;
import org.eclipse.tracecompass.tmf.ui.project.model.TmfTraceFolder;
import org.eclipse.tracecompass.tmf.ui.project.model.TraceUtils;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.handlers.HandlerUtil;
import org.eclipse.ui.handlers.IHandlerService;

/**
 * Handler for the Trace Trim operation.
 *
 * @author Alexandre Montplaisir
 */
@NonNullByDefault
public class TrimTraceHandler extends AbstractHandler {

    /** Suffix for new trimmed traces, added to the original trace name */
    private static final String TRACE_NAME_SUFFIX = "-trimmed"; //$NON-NLS-1$

    @Override
    public boolean isEnabled() {
        IWorkbench workbench = PlatformUI.getWorkbench();
        if (workbench == null) {
            return false;
        }
        @SuppressWarnings("null")
        IHandlerService service = workbench.getService(IHandlerService.class);
        // we need the current state, and the map, but the command and the trigger are
        // not necessary for getCurrentSelection
        ExecutionEvent executionEvent = new ExecutionEvent(null, Collections.emptyMap(), null, service.getCurrentState());
        final Object element = HandlerUtil.getCurrentSelection(executionEvent);

        if (!(element instanceof TreeSelection)) {
            return false;
        }
        /*
         * plugin.xml should have done type/count verification already
         */
        Object firstElement = ((TreeSelection) element).getFirstElement();
        TmfTraceElement traceElem = (TmfTraceElement) firstElement;
        ITmfTrace trace = traceElem.getTrace();
        if (!(trace instanceof ITmfTrimmableTrace)) {
            return false;
        }

        /* Only enable the action if a time range is currently selected */
        TmfTraceManager tm = TmfTraceManager.getInstance();
        TmfTimeRange selectionRange = tm.getTraceContext(trace).getSelectionRange();
        return !(selectionRange.getStartTime().equals(selectionRange.getEndTime()));
    }

    @Override
    public @Nullable Object execute(@Nullable ExecutionEvent event) throws ExecutionException {
        ISelection selection = HandlerUtil.getCurrentSelectionChecked(event);
        Object element = ((IStructuredSelection) selection).getFirstElement();
        final TmfTraceElement traceElem = (TmfTraceElement) element;

        ITmfTrace trace = traceElem.getTrace();
        if (!(trace instanceof ITmfTrimmableTrace)) {
            /* That trace is not currently opened */
            return null;
        }
        ITmfTrimmableTrace trimmableTrace = (ITmfTrimmableTrace) trace;

        /* Retrieve the current time range */
        final Shell shell = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell();
        TmfTraceManager tm = TmfTraceManager.getInstance();
        TmfTimeRange timeRange = tm.getTraceContext(trace).getSelectionRange();
        if (timeRange.getStartTime().equals(timeRange.getEndTime())) {
            MessageDialog.openError(shell, Messages.TrimTraces_InvalidTimeRange_DialogTitle, Messages.TrimTraces_InvalidTimeRange_DialogText);
            return null;
        }

        /* Ensure the time range is in the right direction */
        final TmfTimeRange tr = ((timeRange.getStartTime().compareTo(timeRange.getEndTime()) > 0) ? new TmfTimeRange(timeRange.getEndTime(), timeRange.getStartTime()) : timeRange);

        /*
         * Pop a dialog asking the user to select a parent directory for the new trace.
         */
        DirectoryDialog dialog = DirectoryDialogFactory.create(shell);
        dialog.setText(Messages.TrimTraces_DirectoryChooser_DialogTitle);
        String result = dialog.open();
        if (result == null) {
            /* Dialog was cancelled, take no further action. */
            return null;
        }

        /* Verify that the selected path is valid and writeable */
        final Path parentPath = checkNotNull(Paths.get(result));
        if (!parentPath.toFile().isDirectory()) {
            MessageDialog.openError(shell, Messages.TrimTraces_InvalidDirectory_DialogTitle, Messages.TrimTraces_InvalidDirectory_DialogText);
            return null;
        }
        if (!Files.isWritable(parentPath)) {
            MessageDialog.openError(shell, Messages.TrimTraces_InvalidDirectory_DialogTitle, Messages.TrimTraces_NoWriteAccess_DialogText);
            return null;
        }

        /*
         * Create a directory for the new trace. We will pick the next available name,
         * adding -2, -3, etc. as needed.
         */
        String newTraceName = trace.getResource().getName() + TRACE_NAME_SUFFIX;
        Path potentialPath = parentPath.resolve(newTraceName);
        for (int i = 2; potentialPath.toFile().exists(); i++) {
            newTraceName = trace.getResource().getName() + TRACE_NAME_SUFFIX + '-' + String.valueOf(i);
            potentialPath = parentPath.resolve(newTraceName);
        }

        final Path tracePath = checkNotNull(potentialPath);
        try {
            Files.createDirectory(tracePath);
        } catch (IOException e) {
            /* Should not happen since we have checked permissions, etc. */
            throw new ExecutionException(e.getMessage(), e);
        }

        TmfWorkspaceModifyOperation trimOperation = new TmfWorkspaceModifyOperation() {
            @Override
            public void execute(@Nullable IProgressMonitor monitor) throws CoreException {
                SubMonitor mon = SubMonitor.convert(monitor, 2);

                /* Retrieve the state system modules to use */
                long snapshotTime = tr.getStartTime().toNanos();

                List<@NonNull ITmfAnalysisModuleWithStateSystems> statesystemModules = new ArrayList<>();
                for (IAnalysisModule module : trace.getAnalysisModules()) {
                    if (module instanceof ITmfAnalysisModuleWithStateSystems) {
                        statesystemModules.add((ITmfAnalysisModuleWithStateSystems) module);
                    }
                }

                /*
                 * Perform the trace-specific trim operation. This should create the trace
                 * file(s) in the destination path.
                 */
                Path path = trimmableTrace.trim(tr, tracePath, mon.split(1));
                if (path == null) {
                    Activator.getDefault().logWarning("Could not trim trace " + tracePath); //$NON-NLS-1$
                    return;
                }

                SubMonitor ssMon = SubMonitor.convert(mon, statesystemModules.size());
                /* Write the snapshot files in the new trace's location. */
                try {
                    for (ITmfAnalysisModuleWithStateSystems module : statesystemModules) {
                        ssMon.split(1);
                        Map<String, Integer> versions = module.getProviderVersions();
                        Iterable<ITmfStateSystem> sss = module.getStateSystems();
                        for (ITmfStateSystem ss : sss) {
                            Integer version = versions.get(ss.getSSID());
                            if (snapshotTime <= ss.getCurrentEndTime() && version != null) {
                                StateSnapshot snapshot = new StateSnapshot(ss, Math.max(snapshotTime, ss.getStartTime()), version);
                                snapshot.write(tracePath);
                            }
                        }
                    }

                } catch (IOException e) {
                    throw new CoreException(new Status(IStatus.ERROR, Activator.PLUGIN_ID, "An error occured while attempting to save the initial state")); //$NON-NLS-1$
                }

                /* Import the new trace into the current project, at the top-level. */
                TmfProjectElement currentProjectElement = traceElem.getProject();
                TmfTraceFolder traceFolder = currentProjectElement.getTracesFolder();
                if (traceFolder != null) {
                    Display.getDefault().asyncExec(() -> {
                        try {
                            TmfOpenTraceHelper.openTraceFromPath(traceFolder, path.toString(), shell);
                        } catch (CoreException e) {
                            Activator.getDefault().logWarning("Trace " + traceFolder + " failed to open: " + path); //$NON-NLS-1$ //$NON-NLS-2$
                        }
                    });
                } else {
                    Activator.getDefault().logWarning("Trace folder does not exist: " + path); //$NON-NLS-1$
                }
            }
        };

        try {
            PlatformUI.getWorkbench().getProgressService().run(true, true, trimOperation);
        } catch (InterruptedException e) {
            return null;
        } catch (InvocationTargetException e) {
            TraceUtils.displayErrorMsg(Messages.TrimTraceHandler_failMsg, e.getMessage(), e);
            return null;
        }

        return null;
    }

}
