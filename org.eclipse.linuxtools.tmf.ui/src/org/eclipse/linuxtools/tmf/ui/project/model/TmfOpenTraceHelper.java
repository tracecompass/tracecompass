/**********************************************************************
 * Copyright (c) 2013, 2014 Ericsson, École Polytechnique de Montréal
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
import org.eclipse.core.runtime.URIUtil;
import org.eclipse.linuxtools.internal.tmf.ui.Activator;
import org.eclipse.linuxtools.internal.tmf.ui.project.model.TmfImportHelper;
import org.eclipse.linuxtools.tmf.core.TmfCommonConstants;
import org.eclipse.linuxtools.tmf.core.event.ITmfEvent;
import org.eclipse.linuxtools.tmf.core.exceptions.TmfTraceException;
import org.eclipse.linuxtools.tmf.core.project.model.TmfTraceImportException;
import org.eclipse.linuxtools.tmf.core.project.model.TraceTypeHelper;
import org.eclipse.linuxtools.tmf.core.trace.ITmfTrace;
import org.eclipse.linuxtools.tmf.core.trace.TmfExperiment;
import org.eclipse.linuxtools.tmf.ui.editors.TmfEditorInput;
import org.eclipse.linuxtools.tmf.ui.editors.TmfEventsEditor;
import org.eclipse.osgi.util.NLS;
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
            traceTypeToSet = TmfTraceTypeUIUtils.selectTraceType(path, null, tracetypeHint);
        } catch (TmfTraceImportException e) {
            MessageBox mb = new MessageBox(shell);
            mb.setMessage(e.getMessage());
            mb.open();
            return new Status(IStatus.ERROR, Activator.PLUGIN_ID, e.getMessage());
        }
        IProject project = ResourcesPlugin.getWorkspace().getRoot().getProject(projectRoot);
        IFolder folder = project.getFolder(TmfTraceFolder.TRACE_FOLDER_NAME);
        String traceName = getTraceName(path, folder);
        if (traceExists(path, folder)) {
            return openTraceFromProject(projectRoot, traceName);
        }
        final IPath pathString = Path.fromOSString(path);
        IResource linkedTrace = TmfImportHelper.createLink(folder, pathString, traceName);

        if (linkedTrace == null || !linkedTrace.exists()) {
            return new Status(IStatus.ERROR, Activator.PLUGIN_ID,
                    Messages.TmfOpenTraceHelper_LinkFailed);
        }

        String sourceLocation = URIUtil.toUnencodedString(new File(path).toURI());
        linkedTrace.setPersistentProperty(TmfCommonConstants.SOURCE_LOCATION, sourceLocation);

        // No trace type was determined.
        if (traceTypeToSet == null) {
            return Status.OK_STATUS;
        }

        IStatus ret = TmfTraceTypeUIUtils.setTraceType(linkedTrace, traceTypeToSet);
        if (ret.isOK()) {
            ret = openTraceFromProject(projectRoot, traceName);
        }
        return ret;
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
            return new Status(IStatus.ERROR, Activator.PLUGIN_ID, NLS.bind(Messages.TmfOpenTraceHelper_TraceNotFound, traceName));
        }
        openTraceFromElement(found);
        return Status.OK_STATUS;
    }

    private static ITmfTrace openTraceElement(final TmfTraceElement traceElement) {
        final ITmfTrace trace = traceElement.instantiateTrace();
        final ITmfEvent traceEvent = traceElement.instantiateEvent();
        if ((trace == null) || (traceEvent == null)) {
            TraceUtils.displayErrorMsg(NLS.bind(Messages.TmfOpenTraceHelper_OpenElement, traceElement.getTypeName()),
                    NLS.bind(Messages.TmfOpenTraceHelper_NoTraceOrExperimentType, traceElement.getTypeName()));
            if (trace != null) {
                trace.dispose();
            }
            return null;
        }

        try {
            trace.initTrace(traceElement.getResource(), traceElement.getLocation().getPath(), traceEvent.getClass());
        } catch (final TmfTraceException e) {
            TraceUtils.displayErrorMsg(NLS.bind(Messages.TmfOpenTraceHelper_OpenElement, traceElement.getTypeName()),
                    Messages.TmfOpenTraceHelper_InitError + ENDL + ENDL + e);
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

            ITmfTrace trace = openTraceElement(element);

            if (trace == null) {
                for (int j = 0; j < i; j++) {
                    traces[j].dispose();
                }
                return null;
            }
            cacheSize = Math.min(cacheSize, trace.getCacheSize());

            traces[i] = trace;
        }

        // Create the experiment
        experiment.initExperiment(ITmfEvent.class, experimentElement.getName(), traces, cacheSize, experimentElement.getResource());

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
     * Open a trace (or experiment) from a project element. If the trace is already opened, its
     * editor is activated and brought to top.
     *
     * @param traceElement
     *            the {@link TmfTraceElement} to open
     * @since 3.0
     */
    public static void openTraceFromElement(final TmfCommonProjectElement traceElement) {

        final IFile file;
        try {
            file = traceElement.createBookmarksFile();
        } catch (final CoreException e) {
            Activator.getDefault().logError(NLS.bind(Messages.TmfOpenTraceHelper_ErrorOpeningElement, traceElement.getTypeName()) + ' ' + traceElement.getName());
            TraceUtils.displayErrorMsg(NLS.bind(Messages.TmfOpenTraceHelper_OpenElement, traceElement.getTypeName()),
                    NLS.bind(Messages.TmfOpenTraceHelper_ErrorElement, traceElement.getTypeName()) + ENDL + ENDL + e.getMessage());
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

                final ITmfTrace trace = openProjectElement(traceElement);
                if (trace == null) {
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
                            TraceUtils.displayErrorMsg(NLS.bind(Messages.TmfOpenTraceHelper_OpenElement, traceElement.getTypeName()),
                                    NLS.bind(Messages.TmfOpenTraceHelper_ErrorOpeningElement, traceElement.getTypeName()) + ENDL + ENDL + e.getMessage());
                            Activator.getDefault().logError(NLS.bind(Messages.TmfOpenTraceHelper_ErrorOpeningElement, traceElement.getTypeName()) + ' ' + traceElement.getName());
                            trace.dispose();
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
     * Reopen a trace or experiment from a project element in the provided
     * editor
     *
     * @param traceElement
     *            the {@link TmfTraceElement} to open
     * @param editor
     *            the reusable editor
     * @since 3.0
     */
    public static void reopenTraceFromElement(final TmfCommonProjectElement traceElement, final IReusableEditor editor) {

        final IFile file;
        try {
            file = traceElement.createBookmarksFile();
        } catch (final CoreException e) {
            Activator.getDefault().logError(NLS.bind(Messages.TmfOpenTraceHelper_ErrorOpeningElement, traceElement.getTypeName()) + ' ' + traceElement.getName());
            TraceUtils.displayErrorMsg(NLS.bind(Messages.TmfOpenTraceHelper_OpenElement, traceElement.getTypeName()),
                    NLS.bind(Messages.TmfOpenTraceHelper_ErrorElement, traceElement.getTypeName()) + ENDL + ENDL + e.getMessage());
            return;
        }

        Thread thread = new Thread() {
            @Override
            public void run() {

                final ITmfTrace trace = openProjectElement(traceElement);
                if (trace == null) {
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

}
