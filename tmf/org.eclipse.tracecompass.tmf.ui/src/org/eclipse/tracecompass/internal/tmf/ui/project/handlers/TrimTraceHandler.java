/*******************************************************************************
 * Copyright (c) 2016, 2018 EfficiOS Inc., Alexandre Montplaisir and others
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/

package org.eclipse.tracecompass.internal.tmf.ui.project.handlers;

import static org.eclipse.tracecompass.common.core.NonNullUtils.checkNotNull;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.SubMonitor;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.TreeSelection;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.tracecompass.internal.tmf.ui.Activator;
import org.eclipse.tracecompass.internal.tmf.ui.project.operations.NewExperimentOperation;
import org.eclipse.tracecompass.internal.tmf.ui.project.operations.TmfWorkspaceModifyOperation;
import org.eclipse.tracecompass.statesystem.core.ITmfStateSystem;
import org.eclipse.tracecompass.statesystem.core.snapshot.StateSnapshot;
import org.eclipse.tracecompass.tmf.core.analysis.IAnalysisModule;
import org.eclipse.tracecompass.tmf.core.component.ITmfEventProvider;
import org.eclipse.tracecompass.tmf.core.project.model.TmfTraceType;
import org.eclipse.tracecompass.tmf.core.project.model.TraceTypeHelper;
import org.eclipse.tracecompass.tmf.core.statesystem.ITmfAnalysisModuleWithStateSystems;
import org.eclipse.tracecompass.tmf.core.timestamp.TmfTimeRange;
import org.eclipse.tracecompass.tmf.core.trace.ITmfTrace;
import org.eclipse.tracecompass.tmf.core.trace.TmfTraceManager;
import org.eclipse.tracecompass.tmf.core.trace.trim.ITmfTrimmableTrace;
import org.eclipse.tracecompass.tmf.ui.project.model.ITmfProjectModelElement;
import org.eclipse.tracecompass.tmf.ui.project.model.TmfCommonProjectElement;
import org.eclipse.tracecompass.tmf.ui.project.model.TmfExperimentElement;
import org.eclipse.tracecompass.tmf.ui.project.model.TmfExperimentFolder;
import org.eclipse.tracecompass.tmf.ui.project.model.TmfOpenTraceHelper;
import org.eclipse.tracecompass.tmf.ui.project.model.TmfProjectElement;
import org.eclipse.tracecompass.tmf.ui.project.model.TmfProjectRegistry;
import org.eclipse.tracecompass.tmf.ui.project.model.TmfTraceElement;
import org.eclipse.tracecompass.tmf.ui.project.model.TmfTraceFolder;
import org.eclipse.tracecompass.tmf.ui.project.model.TmfTraceTypeUIUtils;
import org.eclipse.tracecompass.tmf.ui.project.model.TraceUtils;
import org.eclipse.tracecompass.tmf.ui.project.wizards.TrimTraceDialog;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.handlers.HandlerUtil;
import org.eclipse.ui.handlers.IHandlerService;

import com.google.common.collect.Lists;

/**
 * Handler for the Trace Trim operation.
 *
 * @author Alexandre Montplaisir
 */
@NonNullByDefault
public class TrimTraceHandler extends AbstractHandler {

    private static final class TraceToTrim {
        private ITmfTrace fTrace;
        private @Nullable ITmfTrimmableTrace fTrimmable;
        private Path fDestinationPath;
        private Collection<TraceToTrim> fChildren = new ArrayList<>();
        private TmfCommonProjectElement fElement;
        private @Nullable IFolder fFolder = null;
        private @Nullable ITmfProjectModelElement fDestElement = null;
        private TmfCommonProjectElement fSourceElement;

        public static @Nullable TraceToTrim create(TmfCommonProjectElement element, Path destination) {
            ITmfTrace trace = element.getTrace();
            if (trace != null) {
                return new TraceToTrim(trace, element, element, destination);
            }
            if (element instanceof TmfTraceElement) {
                TmfTraceElement traceElement = (TmfTraceElement) element;
                trace = traceElement.getTrace();

                if (trace != null) {
                    return new TraceToTrim(trace, element, element, destination);
                }
            }
            return null;
        }

        private static final @Nullable TraceToTrim create(TraceToTrim parent, TmfTraceElement traceElement) {
            TmfTraceElement elementUnderTraceFolder = traceElement.getElementUnderTraceFolder();
            ITmfTrace trace = traceElement.getTrace();
            if (trace != null) {
                List<String> elems = new ArrayList<>();
                ITmfProjectModelElement tempTraceElement = elementUnderTraceFolder;
                while (tempTraceElement != null) {
                    elems.add(Objects.requireNonNull(tempTraceElement.getName()));
                    tempTraceElement = tempTraceElement.getParent();
                }
                TraceTypeHelper traceType = TmfTraceType.getTraceType(elementUnderTraceFolder.getTraceType());
                String end = elems.remove(0);
                elems = Lists.reverse(elems);
                TmfTraceFolder tracesFolder = elementUnderTraceFolder.getProject().getTracesFolder();
                if (tracesFolder == null) {
                    return null;
                }
                IFolder folder = tracesFolder.getResource();
                // remove project name
                elems.remove(0);
                // remove "Traces"
                elems.set(0, String.valueOf(parent.fDestinationPath.getFileName()));
                for (String elem : elems) {
                    folder = folder.getFolder(elem);
                    if (!folder.exists()) {
                        try {
                            folder.create(true, true, new NullProgressMonitor());
                        } catch (CoreException e) {
                            return null;
                        }
                    }
                }
                folder = folder.getFolder(end);
                if (!traceType.isDirectoryTraceType() && !folder.exists()) {
                    try {
                        folder.create(true, true, new NullProgressMonitor());
                        folder.refreshLocal(IResource.DEPTH_INFINITE, new NullProgressMonitor());
                    } catch (CoreException e) {
                        return null;
                    }
                }
                TmfTraceElement te = new TmfTraceElement(elementUnderTraceFolder.getName(), folder, tracesFolder) {
                    @Override
                    public String getTraceType() {
                        return String.valueOf(traceElement.getTraceType());
                    }

                    @Override
                    public void refreshTraceType() {
                        // Do nothing, we have the tracetype above
                    }

                    @Override
                    public IResource[] getSupplementaryResources() {
                        return traceElement.getSupplementaryResources();
                    }
                };
                return new TraceToTrim(trace, elementUnderTraceFolder, te, (new File(te.getName())).toPath());
            }
            return null;
        }

        public TraceToTrim(ITmfTrace trace, TmfCommonProjectElement sourceElement, TmfCommonProjectElement element, Path destination) {
            fTrace = trace;
            fSourceElement = sourceElement;
            fElement = element;
            if (trace instanceof ITmfTrimmableTrace) {
                fTrimmable = (ITmfTrimmableTrace) trace;
            } else {
                fTrimmable = null;
            }
            fFolder = (IFolder) element.getResource();
            // Note: getParent.getLocation is almost never a link. The
            // folder.getLocation may be a link.
            fDestinationPath = fFolder != null ? fFolder.getParent().getLocation().append(destination.toString()).toFile().toPath() : destination;
            // go through children
            for (TmfTraceElement childElement : element.getTraces()) {
                TraceToTrim create = TraceToTrim.create(this, childElement);
                if (create != null) {
                    fChildren.add(create);
                }
            }
        }

        public IStatus trim(TmfTimeRange tr, IProgressMonitor mon) {
            if (mon.isCanceled()) {
                return Status.CANCEL_STATUS;
            }
            SubMonitor monitor = SubMonitor.convert(mon, 3);
            List<@NonNull ITmfAnalysisModuleWithStateSystems> statesystemModules = new ArrayList<>();
            for (IAnalysisModule module : fTrace.getAnalysisModules()) {
                if (module instanceof ITmfAnalysisModuleWithStateSystems) {
                    statesystemModules.add((ITmfAnalysisModuleWithStateSystems) module);
                }
            }
            monitor.worked(1);
            long snapshotTime = tr.getStartTime().toNanos();
            /*
             * Perform the trace-specific trim operation. This should create the
             * trace file(s) in the destination path.
             */
            Path returnPath = fDestinationPath;
            if (fTrimmable != null) {
                try {
                    returnPath = fTrimmable.trim(tr, returnPath, monitor);
                } catch (CoreException e) {
                    return new Status(IStatus.ERROR, Activator.PLUGIN_ID, "An error occurred writing the state systems snapshots", e); //$NON-NLS-1$
                }
            }
            if (returnPath == null) {
                return new Status(IStatus.ERROR, Activator.PLUGIN_ID, "Failed to trim trace. " + fTrimmable); //$NON-NLS-1$
            }
            if (monitor.isCanceled()) {
                return Status.CANCEL_STATUS;
            }
            monitor.worked(1);
            SubMonitor ssMon = SubMonitor.convert(mon, statesystemModules.size());
            /* Write the snapshot files in the new trace's location. */
            try {
                for (ITmfAnalysisModuleWithStateSystems module : statesystemModules) {
                    ssMon.split(1);
                    Map<String, Integer> versions = module.getProviderVersions();
                    Iterable<ITmfStateSystem> sss = module.getStateSystems();
                    for (ITmfStateSystem ss : sss) {
                        Integer version = versions.get(ss.getSSID());
                        long currentEndTime = ss.getCurrentEndTime();
                        if (snapshotTime <= currentEndTime && version != null) {
                            StateSnapshot snapshot = new StateSnapshot(ss, Math.max(snapshotTime, ss.getStartTime()), Math.min(currentEndTime, tr.getEndTime().toNanos()), version);
                            snapshot.write(returnPath);
                        }
                    }
                }

            } catch (IOException e) {
                return new Status(IStatus.ERROR, Activator.PLUGIN_ID, "An error occurred writing the state systems snapshots", e); //$NON-NLS-1$
            }
            for (TraceToTrim child : fChildren) {
                IStatus status = child.trim(tr, monitor);
                if (!status.isOK()) {
                    return status;
                }
            }
            return Status.OK_STATUS;
        }

        public IStatus importTrace(IProgressMonitor monitor) {
            try {
                IFolder folder = fFolder;
                if (folder == null) {
                    return new Status(IStatus.ERROR, Activator.PLUGIN_ID, "Folder is null"); //$NON-NLS-1$
                }
                ITmfProjectModelElement destElement;
                for (TraceToTrim child : fChildren) {
                    IStatus status = child.importTrace(monitor);
                    if (!status.isOK()) {
                        return status;
                    }
                }

                TraceTypeHelper traceTypeHelper = TmfTraceType.getTraceType(fElement.getTraceType());
                if (fElement instanceof TmfExperimentElement) {
                    TmfExperimentElement experimentElement = (TmfExperimentElement) fElement;
                    // create an experiment
                    TmfExperimentFolder experimentsFolder = experimentElement.getProject().getExperimentsFolder();
                    if (experimentsFolder == null) {
                        return new Status(IStatus.ERROR, Activator.PLUGIN_ID, "Error getting experiment folder"); //$NON-NLS-1$
                    }
                    List<TmfTraceElement> children = new ArrayList<>();
                    for (TraceToTrim child : fChildren) {
                        ITmfProjectModelElement childDest = child.fDestElement;
                        if (childDest instanceof TmfTraceElement) {
                            children.add((TmfTraceElement) childDest);
                        }
                    }

                    String name = fDestinationPath.getFileName().toString();
                    if (name == null) {
                        return new Status(IStatus.ERROR, Activator.PLUGIN_ID, "could not create element"); //$NON-NLS-1$
                    }
                    NewExperimentOperation newExperimentOperation = new NewExperimentOperation(experimentsFolder, name, null, children);
                    newExperimentOperation.run(monitor);
                    experimentsFolder.refresh();
                    destElement = experimentsFolder.getChild(name);
                    if (destElement == null) {
                        return new Status(IStatus.ERROR, Activator.PLUGIN_ID, "could not create element"); //$NON-NLS-1$
                    }
                    TmfTraceTypeUIUtils.setTraceType(destElement.getResource(), traceTypeHelper);
                    if (destElement instanceof TmfExperimentElement) {
                        TmfExperimentElement expElement = (TmfExperimentElement) destElement;
                        for (TraceToTrim child : fChildren) {
                            ITmfProjectModelElement childDest = child.fDestElement;
                            if (childDest instanceof TmfTraceElement && expElement.getChild(childDest.getName()) == null) {
                                expElement.addTrace((TmfTraceElement) childDest, false);
                            }
                        }
                        fSourceElement.copySupplementaryFolder(destElement.getName());
                        destElement.refresh();
                        fDestElement = destElement;
                    }

                } else if (fElement instanceof TmfTraceElement) {
                    // create a trace
                    String path = (traceTypeHelper.isDirectoryTraceType() ? fDestinationPath : fDestinationPath.getParent()).toAbsolutePath().toString();
                    IPath destinationPath = org.eclipse.core.runtime.Path.fromOSString(path);
                    IResource traceResource = null;
                    String lastSegment = destinationPath.lastSegment();
                    IContainer tempFolder = folder.getParent();
                    tempFolder.refreshLocal(IResource.DEPTH_INFINITE, monitor);
                    // Look for a common parent. In case the folders are oddly
                    // configured.
                    while (tempFolder != null && traceResource == null) {
                        traceResource = tempFolder.findMember(lastSegment);
                        tempFolder = tempFolder.getParent();
                    }
                    if (traceResource == null) {
                        return new Status(IStatus.ERROR, Activator.PLUGIN_ID, "Error finding common parent"); //$NON-NLS-1$
                    }
                    IStatus ret = TmfTraceTypeUIUtils.setTraceType(traceResource, traceTypeHelper);
                    if (!ret.isOK()) {
                        return ret;
                    }
                    ITmfProjectModelElement findElement = TmfProjectRegistry.findElement(traceResource, true);
                    if (findElement instanceof TmfCommonProjectElement) {
                        fSourceElement.copySupplementaryFolder(((TmfCommonProjectElement) findElement).getElementPath());
                    }
                    fDestElement = findElement;
                }
            } catch (CoreException e) {
                return new Status(IStatus.ERROR, Activator.PLUGIN_ID, "Error creating link", e); //$NON-NLS-1$
            }
            if (monitor.isCanceled()) {
                return Status.CANCEL_STATUS;
            }
            return Status.OK_STATUS;
        }

        public void open() {
            if (fDestElement instanceof TmfCommonProjectElement) {
                TmfOpenTraceHelper.openTraceFromElement((TmfCommonProjectElement) fDestElement);
            }
        }

        public void createDir() throws IOException {
            if (fElement instanceof TmfExperimentElement) {
                for (TraceToTrim child : fChildren) {
                    child.createDir();
                }
            } else {
                fDestinationPath.toAbsolutePath().toFile().mkdirs();
            }
        }
    }

    /**
     * Can this trace be trimmed?
     *
     * @param element
     *            the element to test
     * @return <code>true</code> if the trace or all its children can be
     *         trimmed.
     */
    private static boolean isValid(@Nullable Object element) {
        if (element instanceof ITmfTrimmableTrace) {
            return true;
        }
        if (!(element instanceof ITmfTrace)) {
            return false;
        }
        ITmfTrace trace = (ITmfTrace) element;
        if (trace.getChildren().isEmpty()) {
            return false;
        }
        for (ITmfEventProvider child : trace.getChildren()) {
            if (!isValid(child)) {
                return false;
            }
        }
        return true;
    }

    @Override
    public boolean isEnabled() {
        IWorkbench workbench = PlatformUI.getWorkbench();
        if (workbench == null) {
            return false;
        }
        @SuppressWarnings("null")
        IHandlerService service = workbench.getService(IHandlerService.class);
        // we need the current state, and the map, but the command and the
        // trigger are
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
        ITmfTrace trace = null;
        if (firstElement instanceof TmfCommonProjectElement) {
            TmfCommonProjectElement traceElem = (TmfCommonProjectElement) firstElement;
            if (traceElem instanceof TmfTraceElement) {
                traceElem = ((TmfTraceElement) traceElem).getElementUnderTraceFolder();
            }
            trace = traceElem.getTrace();
        }
        if (trace == null || !isValid(trace)) {
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
        Object firstElement = ((IStructuredSelection) selection).getFirstElement();
        final TmfCommonProjectElement traceElem = (firstElement instanceof TmfTraceElement) ?
                ((TmfTraceElement) firstElement).getElementUnderTraceFolder() : (TmfCommonProjectElement) firstElement;

        ITmfTrace trace = traceElem.getTrace();

        if (trace == null || !isValid(trace)) {
            /* That trace is not currently opened */
            return null;
        }

        /* Retrieve the current time range */
        final Shell shell = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell();
        TmfTraceManager tm = TmfTraceManager.getInstance();
        TmfTimeRange timeRange = tm.getTraceContext(trace).getSelectionRange();
        if (Objects.equals(timeRange.getStartTime(), timeRange.getEndTime())) {
            MessageDialog.openError(shell, Messages.TrimTraces_InvalidTimeRange_DialogTitle, Messages.TrimTraces_InvalidTimeRange_DialogText);
            return null;
        }

        /* Ensure the time range is in the right direction */
        final TmfTimeRange tr = ((timeRange.getStartTime().compareTo(timeRange.getEndTime()) > 0) ? new TmfTimeRange(timeRange.getEndTime(), timeRange.getStartTime()) : timeRange);

        /*
         * Pop a dialog asking the user to select a parent directory for the new
         * trace.
         */
        TrimTraceDialog dialog = new TrimTraceDialog(shell, traceElem);
        if (dialog.open() != Window.OK) {
            return null;
        }
        Object result = dialog.getFirstResult();
        if (result == null) {
            /* Dialog was cancelled, take no further action. */
            return null;
        }

        /* Verify that the selected path is valid and writeable */
        final Path destinationPath = checkNotNull(Paths.get(result.toString()));

        if (destinationPath.toFile().exists()) {
            MessageDialog.openError(shell, Messages.TrimTraces_InvalidDirectory_DialogTitle, Messages.TrimTraces_InvalidDirectory_DialogText);
            return null;
        }
        TraceToTrim toTrim = TraceToTrim.create(traceElem, destinationPath);
        if (toTrim == null) {
            return null;
        }
        try {
            toTrim.createDir();
        } catch (IOException e) {
            /* Should not happen since we have checked permissions, etc. */
            throw new ExecutionException(e.getMessage(), e);
        }

        TmfWorkspaceModifyOperation trimOperation = new TmfWorkspaceModifyOperation() {
            @Override
            public void execute(@Nullable IProgressMonitor monitor) throws CoreException {
                SubMonitor mon = SubMonitor.convert(monitor, 2);

                toTrim.trim(tr, mon);
                /*
                 * Import the new trace into the current project, at the
                 * top-level.
                 */
                TmfProjectElement currentProjectElement = traceElem.getProject();
                TmfTraceFolder traceFolder = currentProjectElement.getTracesFolder();
                toTrim.importTrace(mon);
                if (mon.isCanceled()) {
                    return;
                }
                if (traceFolder != null) {
                    Display.getDefault().asyncExec(toTrim::open);
                } else {
                    Activator.getDefault().logWarning("Trace folder does not exist: " + toTrim.fDestinationPath); //$NON-NLS-1$
                }
            }
        };

        try {
            PlatformUI.getWorkbench().getProgressService().run(true, true, trimOperation);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        } catch (InvocationTargetException e) {
            TraceUtils.displayErrorMsg(Messages.TrimTraceHandler_failMsg, e.getMessage(), e);
        }

        return null;
    }

}
