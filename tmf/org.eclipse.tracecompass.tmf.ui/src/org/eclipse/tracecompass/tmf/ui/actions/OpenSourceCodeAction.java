/*******************************************************************************
 * Copyright (c) 2019 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License 2.0 which
 * accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/

package org.eclipse.tracecompass.tmf.ui.actions;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.eclipse.core.filesystem.EFS;
import org.eclipse.core.filesystem.IFileStore;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceVisitor;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.ActionContributionItem;
import org.eclipse.jface.action.IContributionItem;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.tracecompass.internal.tmf.ui.Messages;
import org.eclipse.tracecompass.tmf.core.event.lookup.ITmfCallsite;
import org.eclipse.tracecompass.tmf.core.event.lookup.ITmfSourceLookup;
import org.eclipse.tracecompass.tmf.ui.project.model.TraceUtils;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.dialogs.ListDialog;
import org.eclipse.ui.ide.IDE;
import org.eclipse.ui.texteditor.ITextEditor;

/**
 * Open source code action. Original version was in events table, but it is more
 * decoupled now to be able to be used in other views.
 *
 * @author Matthew Khouzam
 * @since 5.1
 */
public class OpenSourceCodeAction extends Action {

    private static final String EMPTY_STRING = ""; //$NON-NLS-1$
    private final ITmfCallsite fCallsite;
    private final Shell fShell;

    /**
     * Builder
     *
     * @param actionText
     *            "open" message recommended to be "lookup" if the location can be
     *            erroneous and open if it's accurate.
     * @param sourceLookup
     *            the source code to lookup
     * @param shell
     *            the parent shell for source file dialog
     * @return an contribution item to open a callsite or null if invalid
     */
    public static IContributionItem create(String actionText, ITmfSourceLookup sourceLookup, Shell shell) {
        List<ITmfCallsite> cs = sourceLookup.getCallsites();
        if (cs == null) {
            return null;
        }
        List<ITmfCallsite> callsites = cs.stream().filter(callstack -> callstack.getLineNo() != null).collect(Collectors.toList());
        if (callsites.isEmpty()) {
            /* Not enough information to provide a full callsite */
            return null;
        }
        if (callsites.size() == 1) {
            return new ActionContributionItem(new OpenSourceCodeAction(actionText, callsites.get(0), shell));
        }

        MenuManager mgr = new MenuManager(actionText);
        for (ITmfCallsite callsite : callsites) {
            mgr.add(new OpenSourceCodeAction(callsite.toString(), callsite, shell));
        }
        return mgr;
    }

    /**
     * Open Source Code Action Constructor
     *
     * @param text
     *            text to display
     * @param callsite
     *            the callsite
     * @param shell
     *            the shell
     * @since 5.2
     */
    public OpenSourceCodeAction(String text, ITmfCallsite callsite, Shell shell) {
        super(text);
        fCallsite = callsite;
        fShell = shell;
    }

    @Override
    public void run() {
        ITmfCallsite cs = fCallsite;
        if (cs == null) {
            return;
        }
        String fileName = cs.getFileName();
        Long lineNo = cs.getLineNo();
        if (lineNo == null) {
            /* Not enough information to provide a full callsite */
            return;
        }
        final String trimmedPath = fileName.replaceAll("\\.\\./", EMPTY_STRING); //$NON-NLS-1$
        File fileToOpen = new File(trimmedPath);

        try {
            if (fileToOpen.exists() && fileToOpen.isFile()) {
                /*
                 * The path points to a "real" file, attempt to open that
                 */
                IFileStore fileStore = EFS.getLocalFileSystem().getStore(fileToOpen.toURI());
                IWorkbenchPage page = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();

                IEditorPart editor = IDE.openEditorOnFileStore(page, fileStore);
                if (editor instanceof ITextEditor) {
                    /*
                     * Calculate the "document offset" corresponding to the line
                     * number, then seek there.
                     */
                    ITextEditor textEditor = (ITextEditor) editor;
                    int lineNumber = lineNo.intValue();
                    IDocument document = textEditor.getDocumentProvider().getDocument(textEditor.getEditorInput());

                    IRegion region = document.getLineInformation(lineNumber - 1);
                    if (region != null) {
                        textEditor.selectAndReveal(region.getOffset(), region.getLength());
                    }
                }

            } else {
                /*
                 * The file was not found on disk, attempt to find it in the
                 * workspace instead.
                 */
                IMarker marker = null;
                final ArrayList<IFile> files = new ArrayList<>();
                IPath p = new Path(trimmedPath);
                ResourcesPlugin.getWorkspace().getRoot().accept(new IResourceVisitor() {
                    @Override
                    public boolean visit(IResource resource) throws CoreException {
                        if (resource instanceof IFile && resource.getFullPath().toString().endsWith(p.lastSegment())) {
                            files.add((IFile) resource);
                        }
                        return true;
                    }
                });
                IFile file = null;
                if (files.size() > 1) {
                    ListDialog dialog = new ListDialog(fShell);
                    dialog.setContentProvider(ArrayContentProvider.getInstance());
                    dialog.setLabelProvider(new LabelProvider() {
                        @Override
                        public String getText(Object element) {
                            return ((IFile) element).getFullPath().toString();
                        }
                    });
                    dialog.setInput(files);
                    dialog.setTitle(Messages.TmfSourceLookup_OpenSourceCodeSelectFileDialogTitle);
                    dialog.setMessage(Messages.TmfSourceLookup_OpenSourceCodeSelectFileDialogTitle + '\n' + cs.toString());
                    dialog.open();
                    Object[] result = dialog.getResult();
                    if (result != null && result.length > 0) {
                        file = (IFile) result[0];
                    }
                } else if (files.size() == 1) {
                    file = files.get(0);
                }
                if (file != null) {
                    marker = file.createMarker(IMarker.MARKER);
                    marker.setAttribute(IMarker.LINE_NUMBER, lineNo.intValue());
                    IDE.openEditor(PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage(), marker);
                    marker.delete();
                } else if (files.isEmpty()) {
                    TraceUtils.displayWarningMsg(new FileNotFoundException('\'' + cs.toString() + '\'' + '\n' + Messages.TmfSourceLookup_OpenSourceCodeNotFound));
                }
            }
        } catch (BadLocationException | CoreException e) {
            TraceUtils.displayErrorMsg(e);
        }
    }
}
