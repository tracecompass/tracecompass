/**********************************************************************
 * Copyright (c) 2013, 2019 Ericsson, École Polytechnique de Montréal
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Matthew Khouzam - Initial API and implementation
 *   Patrick Tasse - Update open trace and add open experiment
 *   Geneviève Bastien - Merge methods to open trace and experiments
 *   Bernd Hufmann - Updated handling of directory traces
 **********************************************************************/

package org.eclipse.tracecompass.tmf.ui.project.model;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.URIUtil;
import org.eclipse.core.runtime.jobs.IJobChangeEvent;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.core.runtime.jobs.JobChangeAdapter;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jface.util.OpenStrategy;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.tracecompass.common.core.log.TraceCompassLog;
import org.eclipse.tracecompass.common.core.log.TraceCompassLogUtils.FlowScopeLog;
import org.eclipse.tracecompass.common.core.log.TraceCompassLogUtils.FlowScopeLogBuilder;
import org.eclipse.tracecompass.common.core.log.TraceCompassLogUtils.ScopeLog;
import org.eclipse.tracecompass.internal.tmf.ui.Activator;
import org.eclipse.tracecompass.internal.tmf.ui.project.model.TmfImportHelper;
import org.eclipse.tracecompass.tmf.core.TmfCommonConstants;
import org.eclipse.tracecompass.tmf.core.event.ITmfEvent;
import org.eclipse.tracecompass.tmf.core.exceptions.TmfTraceException;
import org.eclipse.tracecompass.tmf.core.project.model.TmfTraceImportException;
import org.eclipse.tracecompass.tmf.core.project.model.TmfTraceType;
import org.eclipse.tracecompass.tmf.core.project.model.TraceTypeHelper;
import org.eclipse.tracecompass.tmf.core.trace.ITmfTrace;
import org.eclipse.tracecompass.tmf.core.trace.experiment.TmfExperiment;
import org.eclipse.tracecompass.tmf.ui.editors.TmfEditorInput;
import org.eclipse.tracecompass.tmf.ui.editors.TmfEventsEditor;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IEditorReference;
import org.eclipse.ui.IReusableEditor;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.ide.IDE;
import org.eclipse.ui.part.FileEditorInput;

/**
 * Open trace helper
 *
 * Helper class for opening trace resources and loading them to a tracing
 * project.
 *
 * @author Matthew Khouzam
 */
public class TmfOpenTraceHelper {

    private static final @NonNull Logger LOGGER = TraceCompassLog.getLogger(TmfOpenTraceHelper.class);
    private static final @NonNull String LOCAL_CATEGORY = "TmfOpenTraceHelper"; //$NON-NLS-1$
    private static final String ENDL = System.getProperty("line.separator"); //$NON-NLS-1$
    private static final Set<TmfCommonProjectElement> fOpening = Collections.synchronizedSet(new HashSet<>());

    private TmfOpenTraceHelper() {
        // do nothing
    }


    /**
     * Opens a trace from a path while importing it to the destination folder. The
     * trace is linked as a resource.
     *
     * @param destinationFolder
     *            The destination trace folder
     * @param path
     *            the file to import
     * @param shell
     *            the shell to use for dialogs
     * @return IStatus OK if successful
     * @throws CoreException
     *             core exceptions if something is not well set up in the back end
     */
    public static IStatus openTraceFromPath(TmfTraceFolder destinationFolder, String path, Shell shell) throws CoreException {
        return openTraceFromPath(destinationFolder, path, shell, null);
    }

    /**
     * Opens a trace from a path while importing it to the destination folder. The
     * trace is linked as a resource.
     *
     * @param destinationFolder
     *            The destination trace folder
     * @param path
     *            the file to import
     * @param shell
     *            the shell to use for dialogs
     * @param tracetypeHint
     *            The trace type id, can be null
     * @return IStatus OK if successful
     * @throws CoreException
     *             core exceptions if something is not well set up in the back end
     */
    public static IStatus openTraceFromPath(TmfTraceFolder destinationFolder, String path, Shell shell, String tracetypeHint) throws CoreException {
        final String pathToUse = checkTracePath(path);
        TraceTypeHelper traceTypeToSet = null;
        try (ScopeLog scopeLog = new ScopeLog(LOGGER, Level.FINE, "TmfOpenTraceHelper#openTraceFromPath", "Get trace type")) { //$NON-NLS-1$//$NON-NLS-2$
            traceTypeToSet = TmfTraceTypeUIUtils.selectTraceType(pathToUse, null, tracetypeHint);
        } catch (TmfTraceImportException e) {
            MessageBox mb = new MessageBox(shell);
            mb.setMessage(e.getMessage());
            mb.open();
            return new Status(IStatus.ERROR, Activator.PLUGIN_ID, e.getMessage());
        }

        IFolder folder = destinationFolder.getResource();
        String traceName = getTraceName(pathToUse, folder);
        if (traceExists(pathToUse, folder)) {
            return openTraceFromFolder(destinationFolder, traceName);
        }
        final IPath pathString = Path.fromOSString(pathToUse);
        IResource linkedTrace = TmfImportHelper.createLink(folder, pathString, traceName);

        if (linkedTrace == null || !linkedTrace.exists()) {
            return new Status(IStatus.ERROR, Activator.PLUGIN_ID,
                    Messages.TmfOpenTraceHelper_LinkFailed);
        }

        String sourceLocation = URIUtil.toUnencodedString(pathString.toFile().toURI());
        linkedTrace.setPersistentProperty(TmfCommonConstants.SOURCE_LOCATION, sourceLocation);

        // No trace type was determined.
        if (traceTypeToSet == null) {
            return Status.OK_STATUS;
        }

        IStatus ret = TmfTraceTypeUIUtils.setTraceType(linkedTrace, traceTypeToSet);
        if (ret.isOK()) {
            ret = openTraceFromFolder(destinationFolder, traceName);
        }
        return ret;
    }

    /**
     * Checks whether the parent or grandparent of given path to a file is a valid
     * directory trace. If it is a directory trace then return the parent or
     * grandparent path.
     *
     * @param path
     *            the path to check
     * @return path to use for trace type validation.
     */
    private static String checkTracePath(String path) {
        File file = new File(path);
        if (file.exists() && !file.isDirectory()) {
            // First check parent
            File parent = file.getParentFile();
            String pathToUse = parent.getAbsolutePath();
            if (TmfTraceType.isDirectoryTrace(pathToUse)) {
                return pathToUse;
            }
            // Second check grandparent
            File grandParent = parent.getParentFile();
            if (grandParent != null) {
                pathToUse = grandParent.getAbsolutePath();
                if (TmfTraceType.isDirectoryTrace(pathToUse)) {
                    return pathToUse;
                }
            }
        }
        return path;
    }

    private static boolean traceExists(String path, IFolder folder) {
        String val = getTraceName(path, folder);
        return (folder.findMember(val) != null);
    }

    private static boolean isWrongMember(IFolder folder, String name, final File traceFile) {
        final IResource candidate = folder.findMember(name);
        if (candidate != null) {
            final IPath rawLocation = candidate.getRawLocation();
            File file = rawLocation.toFile();
            try {
                file = file.getCanonicalFile();
            } catch (IOException e) {
                /* just use original file path */
            }
            return !file.equals(traceFile);
        }
        return false;
    }

    /**
     * Gets the display name, either "filename" or "filename(n)" if there is already
     * a filename existing where n is the next unused integer starting from 2
     *
     * @param path
     *            the file path
     * @param folder
     *            the folder to import to
     * @return the filename
     */
    private static String getTraceName(String path, IFolder folder) {
        String name;
        File traceFile = new File(path);
        try {
            traceFile = traceFile.getCanonicalFile();
        } catch (IOException e) {
            /* just use original file path */
        }
        name = traceFile.getName();
        for (int i = 2; isWrongMember(folder, name, traceFile); i++) {
            name = traceFile.getName() + '(' + i + ')';
        }
        return name;
    }

    /**
     * Open a trace from a trace folder
     *
     * @param destinationFolder
     *            The destination trace folder
     * @param traceName
     *            the trace name
     * @return success or error
     */
    private static IStatus openTraceFromFolder(TmfTraceFolder destinationFolder, String traceName) {
        final List<ITmfProjectModelElement> elements = destinationFolder.getChildren();
        TmfTraceElement traceElement = null;
        for (ITmfProjectModelElement element : elements) {
            if (element instanceof TmfTraceElement && element.getName().equals(traceName)) {
                traceElement = (TmfTraceElement) element;
            }
        }
        if (traceElement == null) {
            return new Status(IStatus.ERROR, Activator.PLUGIN_ID, NLS.bind(Messages.TmfOpenTraceHelper_TraceNotFound, traceName));
        }
        openTraceFromElement(traceElement);
        return Status.OK_STATUS;
    }

    private static ITmfTrace openTraceElement(final TmfTraceElement traceElement) {
        final ITmfTrace trace = traceElement.instantiateTrace();
        final ITmfEvent traceEvent = traceElement.instantiateEvent();
        if ((trace == null) || (traceEvent == null)) {
            TraceUtils.displayErrorMsg(NLS.bind(Messages.TmfOpenTraceHelper_OpenElement, traceElement.getTypeName()),
                    Messages.TmfOpenTraceHelper_NoTraceType);
            if (trace != null) {
                trace.dispose();
            }
            return null;
        }

        try {
            trace.initTrace(traceElement.getResource(), traceElement.getResource().getLocation().toOSString(), traceEvent.getClass(), traceElement.getElementPath(), traceElement.getTraceType());
        } catch (final TmfTraceException e) {
            TraceUtils.displayErrorMsg(NLS.bind(Messages.TmfOpenTraceHelper_OpenElement, traceElement.getTypeName()),
                    Messages.TmfOpenTraceHelper_InitError + ENDL + ENDL + e, e.getCause());
            trace.dispose();
            return null;
        }
        return trace;
    }

    private static ITmfTrace openExperimentElement(final TmfExperimentElement experimentElement) {
        /* Experiment element now has an experiment type associated with it */
        final TmfExperiment experiment = experimentElement.instantiateTrace();
        if (experiment == null) {
            TraceUtils.displayErrorMsg(NLS.bind(Messages.TmfOpenTraceHelper_OpenElement, experimentElement.getTypeName()),
                    NLS.bind(Messages.TmfOpenTraceHelper_NoTraceOrExperimentType, experimentElement.getTypeName()));
            return null;
        }

        // Instantiate the experiment's traces
        final List<TmfTraceElement> traceEntries = experimentElement.getTraces();
        int cacheSize = Integer.MAX_VALUE;
        final ITmfTrace[] traces = new ITmfTrace[traceEntries.size()];
        for (int i = 0; i < traceEntries.size(); i++) {
            TmfTraceElement element = traceEntries.get(i);

            // Since trace is under an experiment, use the original trace from
            // the traces folder
            element = element.getElementUnderTraceFolder();

            ITmfTrace trace = null;
            if (element.getParent() instanceof TmfExperimentElement) {
                TraceUtils.displayErrorMsg(NLS.bind(Messages.TmfOpenTraceHelper_OpenElement, element.getTypeName()),
                        NLS.bind(Messages.TmfOpenTraceHelper_TraceNotFound, element.getElementPath()));
            } else {
                trace = openTraceElement(element);
            }

            if (trace == null) {
                for (int j = 0; j < i; j++) {
                    traces[j].dispose();
                }
                experiment.dispose();
                return null;
            }
            cacheSize = Math.min(cacheSize, trace.getCacheSize());

            traces[i] = trace;
        }

        // Create the experiment
        experiment.initExperiment(ITmfEvent.class, experimentElement.getName(), traces, cacheSize, experimentElement.getResource(), experimentElement.getTraceType());

        return experiment;
    }

    private static ITmfTrace openProjectElement(final TmfCommonProjectElement element) {
        ITmfTrace trace = null;
        if (element instanceof TmfTraceElement) {
            trace = openTraceElement((TmfTraceElement) element);
        } else if (element instanceof TmfExperimentElement) {
            trace = openExperimentElement((TmfExperimentElement) element);
        }
        return trace;
    }

    /**
     * Open a trace (or experiment) from a project element. If the trace is already
     * opened, its editor is activated and brought to top.
     *
     * @param traceElement
     *            the {@link TmfTraceElement} to open
     */
    public static void openTraceFromElement(final TmfCommonProjectElement traceElement) {
        try (FlowScopeLog flow = new FlowScopeLogBuilder(LOGGER, Level.FINE, "openTraceFromElement").setCategory(LOCAL_CATEGORY).build()) { //$NON-NLS-1$
            AtomicReference<IFile> bookmarksFile = new AtomicReference<>();
            try (FlowScopeLog bmFlow = new FlowScopeLogBuilder(LOGGER, Level.FINE, "createBookmarkFile").setParentScope(flow).build()) { //$NON-NLS-1$
                PlatformUI.getWorkbench().getProgressService().busyCursorWhile(monitor -> {
                    try {
                        traceElement.refreshSupplementaryFolder(monitor);
                        bookmarksFile.set(traceElement.createBookmarksFile(monitor));
                    } catch (OperationCanceledException e) {
                    } catch (CoreException e) {
                        throw new InvocationTargetException(e);
                    }
                });
            } catch (InterruptedException e) {
                return;
            } catch (InvocationTargetException e) {
                Activator.getDefault().logError(NLS.bind(Messages.TmfOpenTraceHelper_ErrorOpeningElement, traceElement.getTypeName()) + ' ' + traceElement.getName());
                TraceUtils.displayErrorMsg(NLS.bind(Messages.TmfOpenTraceHelper_OpenElement, traceElement.getTypeName()),
                        NLS.bind(Messages.TmfOpenTraceHelper_ErrorElement, traceElement.getTypeName()) + ENDL + ENDL + e.getTargetException().getMessage(), e.getTargetException());
                return;
            }
            IFile file = bookmarksFile.get();
            if (file == null) {
                return;
            }
            final IWorkbench wb = PlatformUI.getWorkbench();
            IWorkbenchWindow window = wb.getActiveWorkbenchWindow();
            if (window == null) {
                return;
            }
            final IWorkbenchPage activePage = window.getActivePage();
            final IEditorPart editor = findEditor(new FileEditorInput(file), true);
            if (editor != null) {
                activePage.activate(editor);
                return;
            }

            // If a trace type is not set then delegate it to the eclipse platform
            if ((traceElement instanceof TmfTraceElement) && (traceElement.getResource() instanceof IFile) && (traceElement.getTraceType() == null)) {
                try (FlowScopeLog bmFlow = new FlowScopeLogBuilder(LOGGER, Level.FINE, "OpenEditor").setParentScope(flow).build()) { //$NON-NLS-1$
                    boolean activate = OpenStrategy.activateOnOpen();
                    // only local open is supported
                    IDE.openEditor(activePage, file, activate);
                } catch (PartInitException e) {
                    TraceUtils.displayErrorMsg(NLS.bind(Messages.TmfOpenTraceHelper_OpenElement, traceElement.getTypeName()),
                            NLS.bind(Messages.TmfOpenTraceHelper_ErrorOpeningElement, traceElement.getElementPath()) + ENDL + ENDL + e.getMessage(), e);
                }
                return;
            }
            if (!fOpening.add(traceElement)) {
                /* element is already opening */
                return;
            }
            OpenProjectElementJob job = new OpenProjectElementJob(traceElement, file);
            job.fParentScope = flow;
            job.addJobChangeListener(new JobChangeAdapter() {
                @Override
                public void done(IJobChangeEvent event) {
                    fOpening.remove(traceElement);
                }
            });
            job.schedule();
        }
    }

    /**
     * Job that creates a new trace instance for the specified project element and
     * opens its associated editor.
     *
     * @since 3.2
     */
    public static class OpenProjectElementJob extends Job {

        private final TmfCommonProjectElement fTraceElement;
        private final IFile fFile;
        private ITmfTrace fTrace = null;
        private @Nullable FlowScopeLog fParentScope;

        /**
         * Constructor
         *
         * @param traceElement
         *            the trace element
         * @param file
         *            the editor file
         */
        public OpenProjectElementJob(TmfCommonProjectElement traceElement, IFile file) {
            super("Opening " + traceElement.getName()); //$NON-NLS-1$
            setSystem(true);
            this.fTraceElement = traceElement;
            this.fFile = file;
        }

        @Override
        public IStatus run(IProgressMonitor monitor) {

            FlowScopeLog parentScope = fParentScope;
            FlowScopeLogBuilder flowScopeLogBuilder = new FlowScopeLogBuilder(LOGGER, Level.FINE, "OpenProjectElementJob"); //$NON-NLS-1$
            try (FlowScopeLog log = parentScope == null ? flowScopeLogBuilder.setCategory(LOCAL_CATEGORY).build()
                    : flowScopeLogBuilder.setParentScope(parentScope).build();) {
                fTrace = openProjectElement(fTraceElement);
                if (fTrace == null) {
                    return Status.OK_STATUS;
                }

                // Get the editor id from the extension point
                String traceEditorId = fTraceElement.getEditorId();
                final String editorId = (traceEditorId != null) ? traceEditorId : TmfEventsEditor.ID;
                final IEditorInput editorInput = new TmfEditorInput(fFile, fTrace);

                Display.getDefault().syncExec(() -> {
                    try (FlowScopeLog displayLog = new FlowScopeLogBuilder(LOGGER, Level.FINE, "OpenEditor").setParentScope(log).build();) { //$NON-NLS-1$

                        IWorkbenchWindow window = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
                        if (window == null) {
                            return;
                        }
                        final IWorkbenchPage activePage = window.getActivePage();
                        if (activePage == null) {
                            return;
                        }
                        try {
                            activePage.openEditor(editorInput, editorId);
                            IDE.setDefaultEditor(fFile, editorId);
                            // editor should dispose the trace on close
                        } catch (final PartInitException e) {
                            TraceUtils.displayErrorMsg(NLS.bind(Messages.TmfOpenTraceHelper_OpenElement, fTraceElement.getTypeName()),
                                    NLS.bind(Messages.TmfOpenTraceHelper_ErrorOpeningElement, fTraceElement.getTypeName()) + ENDL + ENDL + e.getMessage(), e);
                            Activator.getDefault().logError(NLS.bind(Messages.TmfOpenTraceHelper_ErrorOpeningElement, fTraceElement.getTypeName()) + ' ' + fTraceElement.getName());
                            fTrace.dispose();
                        }
                    }
                });
                return Status.OK_STATUS;
            }
        }

        /**
         * Get the new trace instance.
         *
         * @return the new trace instance
         */
        public ITmfTrace getTrace() {
            return fTrace;
        }
    }

    /**
     * Returns the editor with the specified input. Returns null if there is no
     * opened editor with that input. If restore is requested, the method finds and
     * returns the editor even if it is not restored yet after a restart.
     *
     * @param input
     *            the editor input
     * @param restore
     *            true if the editor should be restored
     * @return an editor with input equals to <code>input</code>
     */
    private static IEditorPart findEditor(IEditorInput input, boolean restore) {
        final IWorkbench wb = PlatformUI.getWorkbench();
        final IWorkbenchPage activePage = wb.getActiveWorkbenchWindow().getActivePage();
        for (IEditorReference editorReference : activePage.getEditorReferences()) {
            try {
                IEditorInput editorInput = editorReference.getEditorInput();
                if (editorInput.equals(input)) {
                    return editorReference.getEditor(restore);
                }
            } catch (PartInitException e) {
                // do nothing
            }
        }
        return null;
    }

    /**
     * Reopen a trace or experiment from a project element in the provided editor
     *
     * @param traceElement
     *            the {@link TmfTraceElement} to open
     * @param editor
     *            the reusable editor
     */
    public static void reopenTraceFromElement(final TmfCommonProjectElement traceElement, final IReusableEditor editor) {

        try (FlowScopeLog flow = new FlowScopeLogBuilder(LOGGER, Level.FINE, "reopenTraceFromElement").setCategory(LOCAL_CATEGORY).build()) { //$NON-NLS-1$
            AtomicReference<IFile> bookmarksFile = new AtomicReference<>();
            try (FlowScopeLog scopeLog = new FlowScopeLogBuilder(LOGGER, Level.FINE, "createBookmarks").setParentScope(flow).build()) { //$NON-NLS-1$
                PlatformUI.getWorkbench().getProgressService().busyCursorWhile(monitor -> {
                    try {
                        traceElement.refreshSupplementaryFolder(monitor);
                        bookmarksFile.set(traceElement.createBookmarksFile(monitor));
                    } catch (OperationCanceledException e) {
                    } catch (CoreException e) {
                        throw new InvocationTargetException(e);
                    }
                });
            } catch (InterruptedException e) {
                return;
            } catch (final InvocationTargetException e) {
                Activator.getDefault().logError(NLS.bind(Messages.TmfOpenTraceHelper_ErrorOpeningElement, traceElement.getTypeName()) + ' ' + traceElement.getName());
                TraceUtils.displayErrorMsg(NLS.bind(Messages.TmfOpenTraceHelper_OpenElement, traceElement.getTypeName()),
                        NLS.bind(Messages.TmfOpenTraceHelper_ErrorElement, traceElement.getTypeName()) + ENDL + ENDL + e.getTargetException().getMessage(), e.getTargetException());
                return;
            }
            IFile file = bookmarksFile.get();
            if (file == null) {
                return;
            }
            Thread thread = new Thread() {
                @Override
                public void run() {
                    try (FlowScopeLog scopeLog = new FlowScopeLogBuilder(LOGGER, Level.FINE, "createThread").setParentScope(flow).build()) { //$NON-NLS-1$
                        final ITmfTrace trace = openProjectElement(traceElement);
                        if (trace == null) {
                            return;
                        }

                        final IEditorInput editorInput = new TmfEditorInput(file, trace);

                        Display.getDefault().asyncExec(() -> {
                            try (FlowScopeLog innerScopeLog = new FlowScopeLogBuilder(LOGGER, Level.FINE, "OpenEditor").setParentScope(flow).build()) { //$NON-NLS-1$
                                final IWorkbench wb = PlatformUI.getWorkbench();
                                IWorkbenchWindow activeWorkbenchWindow = wb.getActiveWorkbenchWindow();
                                if (activeWorkbenchWindow == null) {
                                    return;
                                }

                                final IWorkbenchPage activePage = activeWorkbenchWindow.getActivePage();
                                activePage.reuseEditor(editor, editorInput);
                                activePage.activate(editor);
                            }
                        });
                    }
                }
            };
            thread.start();
        }
    }
}
