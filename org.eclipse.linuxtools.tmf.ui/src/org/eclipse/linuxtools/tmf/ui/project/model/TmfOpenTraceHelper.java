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
import org.eclipse.linuxtools.internal.tmf.ui.project.model.TmfTraceImportException;
import org.eclipse.linuxtools.tmf.core.event.ITmfEvent;
import org.eclipse.linuxtools.tmf.core.exceptions.TmfTraceException;
import org.eclipse.linuxtools.tmf.core.trace.ITmfTrace;
import org.eclipse.linuxtools.tmf.ui.editors.TmfEditorInput;
import org.eclipse.linuxtools.tmf.ui.editors.TmfEventsEditor;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
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
        TmfTraceType tt = TmfTraceType.getInstance();
        TraceTypeHelper traceTypeToSet = null;
        try {
            traceTypeToSet = tt.selectTraceType(path, shell);
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
            IStatus ret = TmfTraceType.setTraceType(tracePath, traceTypeToSet);
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
        TmfImportHelper.forceFolderRefresh(project.getFolder(TmfTraceFolder.TRACE_FOLDER_NAME));

        final TmfProjectElement project2 = TmfProjectRegistry.getProject(project, true);
        final TmfTraceFolder tracesFolder = project2.getTracesFolder();
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
        return openTraceFromElement(found);
    }

    /**
     * Open a trace from a TmfTraceElement
     *
     * @param traceElement
     *            the {@link TmfTraceElement} to open
     * @return Status.OK_STATUS
     */
    public static IStatus openTraceFromElement(final TmfTraceElement traceElement) {
        Thread thread = new Thread() {
            @Override
            public void run() {

                final ITmfTrace trace = traceElement.instantiateTrace();
                final ITmfEvent traceEvent = traceElement.instantiateEvent();
                if ((trace == null) || (traceEvent == null)) {
                    if (trace != null) {
                        TraceUtils.displayErrorMsg(Messages.TmfOpenTraceHelper_OpenTrace, Messages.TmfOpenTraceHelper_NoTraceType);
                        trace.dispose();
                    }
                    return;
                }

                // Get the editor_id from the extension point
                String traceEditorId = traceElement.getEditorId();
                final String editorId = (traceEditorId != null) ? traceEditorId : TmfEventsEditor.ID;
                try {
                    trace.initTrace(traceElement.getResource(), traceElement.getLocation().getPath(), traceEvent.getClass());
                } catch (final TmfTraceException e) {
                    TraceUtils.displayErrorMsg(Messages.TmfOpenTraceHelper_OpenTrace, Messages.TmfOpenTraceHelper_InitError + ENDL + ENDL + e);
                    trace.dispose();
                    return;
                }

                final IFile file;
                try {
                    file = traceElement.createBookmarksFile();
                } catch (final CoreException e) {
                    Activator.getDefault().logError(Messages.TmfOpenTraceHelper_ErrorOpeningTrace + traceElement.getName());
                    TraceUtils.displayErrorMsg(Messages.TmfOpenTraceHelper_OpenTrace, Messages.TmfOpenTraceHelper_Error + ENDL + ENDL + e.getMessage());
                    trace.dispose();
                    return;
                }

                Display.getDefault().asyncExec(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            final IEditorInput editorInput = new TmfEditorInput(file, trace);
                            final IWorkbench wb = PlatformUI.getWorkbench();
                            final IWorkbenchPage activePage = wb.getActiveWorkbenchWindow().getActivePage();
                            final IEditorPart editor = activePage.findEditor(new FileEditorInput(file));
                            if ((editor != null) && (editor instanceof IReusableEditor)) {
                                activePage.reuseEditor((IReusableEditor) editor, editorInput);
                                activePage.activate(editor);
                            } else {
                                activePage.openEditor(editorInput, editorId);
                                IDE.setDefaultEditor(file, editorId);
                                // editor should dispose the trace on close
                            }
                        } catch (final PartInitException e) {
                            TraceUtils.displayErrorMsg(Messages.TmfOpenTraceHelper_OpenTrace, Messages.TmfOpenTraceHelper_ErrorOpeningTrace + ENDL + ENDL + e.getMessage());
                            Activator.getDefault().logError(Messages.TmfOpenTraceHelper_ErrorOpeningTrace + traceElement.getName());
                            trace.dispose();
                        }
                    }
                });
            }
        };
        thread.start();
        return Status.OK_STATUS;
    }

}
