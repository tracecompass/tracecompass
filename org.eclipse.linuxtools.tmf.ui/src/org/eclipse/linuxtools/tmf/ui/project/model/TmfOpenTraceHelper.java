/**********************************************************************
 * Copyright (c) 2013 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Matthew Khouzam - Initial API and implementation
 *   Patrick Tasse - Update open trace and add open experiment
 **********************************************************************/

package org.eclipse.linuxtools.tmf.ui.project.model;

import java.io.File;
import java.util.List;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Status;
import org.eclipse.linuxtools.internal.tmf.ui.Activator;
import org.eclipse.linuxtools.internal.tmf.ui.project.model.TmfImportHelper;
import org.eclipse.linuxtools.tmf.core.event.ITmfEvent;
import org.eclipse.linuxtools.tmf.core.exceptions.TmfTraceException;
import org.eclipse.linuxtools.tmf.core.project.model.TmfTraceImportException;
import org.eclipse.linuxtools.tmf.core.project.model.TraceTypeHelper;
import org.eclipse.linuxtools.tmf.core.trace.ITmfTrace;
import org.eclipse.linuxtools.tmf.core.trace.TmfExperiment;
import org.eclipse.linuxtools.tmf.ui.editors.TmfEditorInput;
import org.eclipse.linuxtools.tmf.ui.editors.TmfEventsEditor;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IEditorReference;
import org.eclipse.ui.IReusableEditor;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPage;
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
 * @since 2.1
 */
public class TmfOpenTraceHelper {

    private static final String ENDL = System.getProperty("line.separator"); //$NON-NLS-1$

    /**
     * Opens a trace from a path while importing it to the project
     * "projectRoot". The trace is linked as a resource.
     *
     * @param projectRoot
     *            The project to import to
     * @param path
     *            the file to import
     * @param shell
     *            the shell to use for dialogs
     * @return IStatus OK if successful
     * @throws CoreException
     *             core exceptions if something is not well set up in the back
     *             end
     */
    public IStatus openTraceFromPath(String projectRoot, String path, Shell shell) throws CoreException {
        return openTraceFromPath(projectRoot, path, shell, null);
    }

    /**
     * Opens a trace from a path while importing it to the project
     * "projectRoot". The trace is linked as a resource.
     *
     * @param projectRoot
     *            The project to import to
     * @param path
     *            the file to import
     * @param shell
     *            the shell to use for dialogs
     * @param tracetypeHint
     *            The trace type id, can be null
     * @return IStatus OK if successful
     * @throws CoreException
     *             core exceptions if something is not well set up in the back
     *             end
     *
     * @since 2.2
     */
    public IStatus openTraceFromPath(String projectRoot, String path, Shell shell, String tracetypeHint) throws CoreException {
        TraceTypeHelper traceTypeToSet = null;
        try {
            traceTypeToSet = TmfTraceTypeUIUtils.selectTraceType(path, shell, tracetypeHint);
        } catch (TmfTraceImportException e) {
            MessageBox mb = new MessageBox(shell);
            mb.setMessage(e.getMessage());
            mb.open();
            return new Status(IStatus.ERROR, Activator.PLUGIN_ID, e.getMessage());
        }
        if (traceTypeToSet == null) {
            return Status.CANCEL_STATUS;
        }
        IProject project = ResourcesPlugin.getWorkspace().getRoot().getProject(projectRoot);
        IFolder folder = project.getFolder(TmfTraceFolder.TRACE_FOLDER_NAME);
        String traceName = getTraceName(path, folder);
        if (traceExists(path, folder)) {
            return openTraceFromProject(projectRoot, traceName);
        }
        final IPath tracePath = folder.getFullPath().append(traceName);
        final IPath pathString = Path.fromOSString(path);
        IResource linkedTrace = TmfImportHelper.createLink(folder, pathString, traceName);
        if (linkedTrace != null && linkedTrace.exists()) {
            IStatus ret = TmfTraceTypeUIUtils.setTraceType(tracePath, traceTypeToSet);
            if (ret.isOK()) {
                ret = openTraceFromProject(projectRoot, traceName);
            }
            return ret;
        }
        return new Status(IStatus.ERROR, Activator.PLUGIN_ID,
                Messages.TmfOpenTraceHelper_LinkFailed);
    }

    private static boolean traceExists(String file, IFolder folder) {
        String val = getTraceName(file, folder);
        return (folder.findMember(val) != null);
    }

    private static boolean isWrongMember(IFolder folder, String ret, final File traceFile) {
        final IResource candidate = folder.findMember(ret);
        if (candidate != null) {
            final IPath rawLocation = candidate.getRawLocation();
            final File file = rawLocation.toFile();
            return !file.equals(traceFile);
        }
        return false;
    }

    /**
     * Gets the display name, either "filename" or "filename(n)" if there is
     * already a filename existing where n is the next non-used integer starting
     * from 2
     *
     * @param file
     *            the file with path
     * @param folder
     *            the folder to import to
     * @return the filename
     */
    private static String getTraceName(String file, IFolder folder) {
        String ret;
        final File traceFile = new File(file);
        ret = traceFile.getName();
        for (int i = 2; isWrongMember(folder, ret, traceFile); i++) {
            ret = traceFile.getName() + '(' + i + ')';
        }
        return ret;
    }

    /**
     * Open a trace from a project
     *
     * @param projectRoot
     *            the root of the project
     * @param traceName
     *            the trace name
     * @return success or error
     */
    public static IStatus openTraceFromProject(String projectRoot, String traceName) {
        final IWorkspace workspace = ResourcesPlugin.getWorkspace();
        final IWorkspaceRoot root = workspace.getRoot();
        IProject project = root.getProject(projectRoot);
        final TmfProjectElement projectElement = TmfProjectRegistry.getProject(project, true);
        final TmfTraceFolder tracesFolder = projectElement.getTracesFolder();
        final List<TmfTraceElement> traces = tracesFolder.getTraces();
        TmfTraceElement found = null;
        for (TmfTraceElement candidate : traces) {
            if (candidate.getName().equals(traceName)) {
                found = candidate;
            }
        }
        if (found == null) {
            return new Status(IStatus.ERROR, Activator.PLUGIN_ID, Messages.TmfOpenTraceHelper_ErrorOpeningTrace);
        }
        openTraceFromElement(found);
        return Status.OK_STATUS;
    }

    /**
     * Open a trace from a trace element. If the trace is already opened, its
     * editor is activated and brought to top.
     *
     * @param traceElement
     *            the {@link TmfTraceElement} to open
     */
    public static void openTraceFromElement(final TmfTraceElement traceElement) {

        final IFile file;
        try {
            file = traceElement.createBookmarksFile();
        } catch (final CoreException e) {
            Activator.getDefault().logError(Messages.TmfOpenTraceHelper_ErrorOpeningTrace + ' ' + traceElement.getName());
            TraceUtils.displayErrorMsg(Messages.TmfOpenTraceHelper_OpenTrace, Messages.TmfOpenTraceHelper_ErrorTrace + ENDL + ENDL + e.getMessage());
            return;
        }

        final IWorkbench wb = PlatformUI.getWorkbench();
        final IWorkbenchPage activePage = wb.getActiveWorkbenchWindow().getActivePage();
        final IEditorPart editor = findEditor(new FileEditorInput(file), true);
        if (editor != null) {
            activePage.activate(editor);
            return;
        }

        Thread thread = new Thread() {
            @Override
            public void run() {

                final ITmfTrace trace = traceElement.instantiateTrace();
                final ITmfEvent traceEvent = traceElement.instantiateEvent();
                if ((trace == null) || (traceEvent == null)) {
                    TraceUtils.displayErrorMsg(Messages.TmfOpenTraceHelper_OpenTrace, Messages.TmfOpenTraceHelper_NoTraceType);
                    if (trace != null) {
                        trace.dispose();
                    }
                    return;
                }

                try {
                    trace.initTrace(traceElement.getResource(), traceElement.getLocation().getPath(), traceEvent.getClass());
                } catch (final TmfTraceException e) {
                    TraceUtils.displayErrorMsg(Messages.TmfOpenTraceHelper_OpenTrace, Messages.TmfOpenTraceHelper_InitError + ENDL + ENDL + e);
                    trace.dispose();
                    return;
                }

                // Get the editor id from the extension point
                String traceEditorId = traceElement.getEditorId();
                final String editorId = (traceEditorId != null) ? traceEditorId : TmfEventsEditor.ID;
                final IEditorInput editorInput = new TmfEditorInput(file, trace);

                Display.getDefault().asyncExec(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            activePage.openEditor(editorInput, editorId);
                            IDE.setDefaultEditor(file, editorId);
                            // editor should dispose the trace on close
                        } catch (final PartInitException e) {
                            TraceUtils.displayErrorMsg(Messages.TmfOpenTraceHelper_OpenTrace, Messages.TmfOpenTraceHelper_ErrorOpeningTrace + ENDL + ENDL + e.getMessage());
                            Activator.getDefault().logError(Messages.TmfOpenTraceHelper_ErrorOpeningTrace + ' ' + traceElement.getName());
                            trace.dispose();
                        }
                    }
                });
            }
        };
        thread.start();
    }

    /**
     * Open an experiment from an experiment element. If the experiment is
     * already opened, its editor is activated and brought to top.
     *
     * @param experimentElement
     *            the {@link TmfExperimentElement} to open
     */
    public static void openExperimentFromElement(final TmfExperimentElement experimentElement) {

        final IFile file;
        try {
            file = experimentElement.createBookmarksFile();
        } catch (final CoreException e) {
            Activator.getDefault().logError(Messages.TmfOpenTraceHelper_ErrorOpeningExperiment + ' ' + experimentElement.getName());
            TraceUtils.displayErrorMsg(Messages.TmfOpenTraceHelper_OpenExperiment, Messages.TmfOpenTraceHelper_ErrorExperiment + ENDL + ENDL + e.getMessage());
            return;
        }

        final IWorkbench wb = PlatformUI.getWorkbench();
        final IWorkbenchPage activePage = wb.getActiveWorkbenchWindow().getActivePage();
        final IEditorPart editor = findEditor(new FileEditorInput(file), true);
        if (editor != null) {
            activePage.activate(editor);
            return;
        }

        Thread thread = new Thread() {
            @Override
            public void run() {

                /*
                 * Unlike traces, there is no instanceExperiment, so we call
                 * this function here alone. Maybe it would be better to do this
                 * on experiment's element constructor?
                 */
                experimentElement.refreshSupplementaryFolder();

                // Instantiate the experiment's traces
                final List<TmfTraceElement> traceEntries = experimentElement.getTraces();
                final int nbTraces = traceEntries.size();
                int cacheSize = Integer.MAX_VALUE;
                String commonEditorId = null;
                final ITmfTrace[] traces = new ITmfTrace[nbTraces];
                for (int i = 0; i < nbTraces; i++) {
                    TmfTraceElement element = traceEntries.get(i);

                    // Since trace is under an experiment, use the original
                    // trace from the traces folder
                    element = element.getElementUnderTraceFolder();

                    final ITmfTrace trace = element.instantiateTrace();
                    final ITmfEvent traceEvent = element.instantiateEvent();
                    if ((trace == null) || (traceEvent == null)) {
                        TraceUtils.displayErrorMsg(Messages.TmfOpenTraceHelper_OpenExperiment,
                                Messages.TmfOpenTraceHelper_ErrorOpeningTrace + ' ' + element.getName() +
                                        ENDL + Messages.TmfOpenTraceHelper_NoTraceType);
                        for (int j = 0; j < i; j++) {
                            traces[j].dispose();
                        }
                        if (trace != null) {
                            trace.dispose();
                        }
                        return;
                    }
                    try {
                        trace.initTrace(element.getResource(), element.getLocation().getPath(), traceEvent.getClass());
                    } catch (final TmfTraceException e) {
                        TraceUtils.displayErrorMsg(Messages.TmfOpenTraceHelper_OpenExperiment,
                                element.getName() + ':' + ' ' + Messages.TmfOpenTraceHelper_InitError + ENDL + ENDL + e);
                        for (int j = 0; j < i; j++) {
                            traces[j].dispose();
                        }
                        trace.dispose();
                        return;
                    }
                    cacheSize = Math.min(cacheSize, trace.getCacheSize());

                    // If all traces use the same editorId, use it, otherwise
                    // use the default
                    final String editorId = element.getEditorId();
                    if (commonEditorId == null) {
                        commonEditorId = (editorId != null) ? editorId : TmfEventsEditor.ID;
                    } else if (!commonEditorId.equals(editorId)) {
                        commonEditorId = TmfEventsEditor.ID;
                    }
                    traces[i] = trace;
                }

                // Create the experiment
                final TmfExperiment experiment = new TmfExperiment(ITmfEvent.class, experimentElement.getName(), traces, cacheSize, experimentElement.getResource());
                experiment.setBookmarksFile(file);

                final String editorId = commonEditorId;
                final IEditorInput editorInput = new TmfEditorInput(file, experiment);

                Display.getDefault().asyncExec(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            activePage.openEditor(editorInput, editorId);
                            IDE.setDefaultEditor(file, editorId);
                            // editor should dispose the trace on close
                        } catch (final PartInitException e) {
                            TraceUtils.displayErrorMsg(Messages.TmfOpenTraceHelper_OpenExperiment, Messages.TmfOpenTraceHelper_ErrorOpeningExperiment + ENDL + ENDL + e.getMessage());
                            Activator.getDefault().logError(Messages.TmfOpenTraceHelper_ErrorOpeningExperiment + ' ' + experimentElement.getName());
                            experiment.dispose();
                        }
                    }
                });
            }
        };
        thread.start();
    }

    /**
     * Returns the editor with the specified input. Returns null if there is no
     * opened editor with that input. If restore is requested, the method finds
     * and returns the editor even if it is not restored yet after a restart.
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
            }
        }
        return null;
    }

    /**
     * Reopen a trace from a trace element in the provided editor
     *
     * @param traceElement
     *            the {@link TmfTraceElement} to open
     * @param editor
     *            the reusable editor
     */
    public static void reopenTraceFromElement(final TmfTraceElement traceElement, final IReusableEditor editor) {

        final IFile file;
        try {
            file = traceElement.createBookmarksFile();
        } catch (final CoreException e) {
            Activator.getDefault().logError(Messages.TmfOpenTraceHelper_ErrorOpeningTrace + ' ' + traceElement.getName());
            TraceUtils.displayErrorMsg(Messages.TmfOpenTraceHelper_OpenTrace, Messages.TmfOpenTraceHelper_ErrorTrace + ENDL + ENDL + e.getMessage());
            return;
        }

        Thread thread = new Thread() {
            @Override
            public void run() {

                final ITmfTrace trace = traceElement.instantiateTrace();
                final ITmfEvent traceEvent = traceElement.instantiateEvent();
                if ((trace == null) || (traceEvent == null)) {
                    TraceUtils.displayErrorMsg(Messages.TmfOpenTraceHelper_OpenTrace, Messages.TmfOpenTraceHelper_NoTraceType);
                    if (trace != null) {
                        trace.dispose();
                    }
                    return;
                }

                try {
                    trace.initTrace(traceElement.getResource(), traceElement.getLocation().getPath(), traceEvent.getClass());
                } catch (final TmfTraceException e) {
                    TraceUtils.displayErrorMsg(Messages.TmfOpenTraceHelper_OpenTrace, Messages.TmfOpenTraceHelper_InitError + ENDL + ENDL + e);
                    trace.dispose();
                    return;
                }

                final IEditorInput editorInput = new TmfEditorInput(file, trace);

                Display.getDefault().asyncExec(new Runnable() {
                    @Override
                    public void run() {
                        final IWorkbench wb = PlatformUI.getWorkbench();
                        final IWorkbenchPage activePage = wb.getActiveWorkbenchWindow().getActivePage();
                        activePage.reuseEditor(editor, editorInput);
                        activePage.activate(editor);
                    }
                });
            }
        };
        thread.start();
    }

    /**
     * Reopen an experiment from an experiment element in the provided editor
     *
     * @param experimentElement
     *            the {@link TmfExperimentElement} to open
     * @param editor
     *            the reusable editor
     */
    public static void reopenExperimentFromElement(final TmfExperimentElement experimentElement, final IReusableEditor editor) {

        final IFile file;
        try {
            file = experimentElement.createBookmarksFile();
        } catch (final CoreException e) {
            Activator.getDefault().logError(Messages.TmfOpenTraceHelper_ErrorOpeningExperiment + ' ' + experimentElement.getName());
            TraceUtils.displayErrorMsg(Messages.TmfOpenTraceHelper_OpenExperiment, Messages.TmfOpenTraceHelper_ErrorExperiment + ENDL + ENDL + e.getMessage());
            return;
        }

        Thread thread = new Thread() {
            @Override
            public void run() {

                /*
                 * Unlike traces, there is no instanceExperiment, so we call
                 * this function here alone. Maybe it would be better to do this
                 * on experiment's element constructor?
                 */
                experimentElement.refreshSupplementaryFolder();

                // Instantiate the experiment's traces
                final List<TmfTraceElement> traceEntries = experimentElement.getTraces();
                final int nbTraces = traceEntries.size();
                int cacheSize = Integer.MAX_VALUE;
                final ITmfTrace[] traces = new ITmfTrace[nbTraces];
                for (int i = 0; i < nbTraces; i++) {
                    TmfTraceElement element = traceEntries.get(i);

                    // Since trace is under an experiment, use the original
                    // trace from the traces folder
                    element = element.getElementUnderTraceFolder();

                    final ITmfTrace trace = element.instantiateTrace();
                    final ITmfEvent traceEvent = element.instantiateEvent();
                    if ((trace == null) || (traceEvent == null)) {
                        TraceUtils.displayErrorMsg(Messages.TmfOpenTraceHelper_OpenExperiment,
                                Messages.TmfOpenTraceHelper_ErrorOpeningTrace + ' ' + element.getName() +
                                        ENDL + Messages.TmfOpenTraceHelper_NoTraceType);
                        for (int j = 0; j < i; j++) {
                            traces[j].dispose();
                        }
                        if (trace != null) {
                            trace.dispose();
                        }
                        return;
                    }
                    try {
                        trace.initTrace(element.getResource(), element.getLocation().getPath(), traceEvent.getClass());
                    } catch (final TmfTraceException e) {
                        TraceUtils.displayErrorMsg(Messages.TmfOpenTraceHelper_OpenExperiment,
                                element.getName() + ':' + ' ' + Messages.TmfOpenTraceHelper_InitError + ENDL + ENDL + e);
                        for (int j = 0; j < i; j++) {
                            traces[j].dispose();
                        }
                        trace.dispose();
                        return;
                    }
                    cacheSize = Math.min(cacheSize, trace.getCacheSize());

                    traces[i] = trace;
                }

                // Create the experiment
                final TmfExperiment experiment = new TmfExperiment(ITmfEvent.class, experimentElement.getName(), traces, cacheSize, experimentElement.getResource());
                experiment.setBookmarksFile(file);

                final IEditorInput editorInput = new TmfEditorInput(file, experiment);

                Display.getDefault().asyncExec(new Runnable() {
                    @Override
                    public void run() {
                        final IWorkbench wb = PlatformUI.getWorkbench();
                        final IWorkbenchPage activePage = wb.getActiveWorkbenchWindow().getActivePage();
                        activePage.reuseEditor(editor, editorInput);
                        activePage.activate(editor);
                    }
                });
            }
        };
        thread.start();
    }

}
