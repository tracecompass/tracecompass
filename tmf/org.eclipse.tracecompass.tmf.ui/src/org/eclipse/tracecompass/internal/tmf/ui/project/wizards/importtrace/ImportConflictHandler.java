/*******************************************************************************
 * Copyright (c) 2014, 2017 Ericsson.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Bernd Hufmann  - Initial API and implementation
 *******************************************************************************/
package org.eclipse.tracecompass.internal.tmf.ui.project.wizards.importtrace;

import java.util.List;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.tracecompass.tmf.ui.project.model.TmfTraceElement;
import org.eclipse.tracecompass.tmf.ui.project.model.TmfTraceFolder;

/**
 * Handler to check for name clashes during import operations. It will allow
 * users to select renaming, overwriting or skipping of a given trace as well
 * as upcoming traces by keeping track of the user selection. In case of
 * overwriting the original trace will be deleted.
 *
 * See {@link ImportConfirmation} for users selection choices.
 *
 * @author Bernd Hufmann
 */
public class ImportConflictHandler {

    // ------------------------------------------------------------------------
    // Attributes
    // ------------------------------------------------------------------------
    private Shell fShell;
    private TmfTraceFolder fTraceFolderElement;
    private ImportConfirmation fConfirmationMode;

    // ------------------------------------------------------------------------
    // Constructor(s)
    // ------------------------------------------------------------------------
    /**
     * @param shell
     *              shell to display confirmation dialog
     * @param folder
     *              Target folder for the traces
     * @param initialMode
     *              Initial confirmation mode
     */
    public ImportConflictHandler(Shell shell, TmfTraceFolder folder, ImportConfirmation initialMode) {
        fShell = shell;
        fTraceFolderElement = folder;
        fConfirmationMode = initialMode;
    }

    // ------------------------------------------------------------------------
    // Operation(s)
    // ------------------------------------------------------------------------
    /**
     * It checks for name clashes. In case of a name clash it will open a
     * confirmation dialog where the use can rename, overwrite or skip
     * the trace. The user has also the choice to rename, overwrite or
     * skip all traces of subsequent calls to this method. This class will
     * keep track about the {@link ImportConfirmation} mode selected by the
     * user.
     *
     * In case of {@link ImportConfirmation#RENAME} or
     * {@link ImportConfirmation#RENAME_ALL} a new name will be return by
     * adding sequence number surrounded by (), e.g. (1) or (2).
     *
     * In case of {@link ImportConfirmation#OVERWRITE} or
     * {@link ImportConfirmation#OVERWRITE_ALL} the original trace will be
     * deleted and the original name will be returned.
     *
     * In case the dialog {@link ImportConfirmation#SKIP} or
     * {@link ImportConfirmation#SKIP_ALL} it will return null to indicate
     * the skipping.
     *
     * @param tracePath
     *                The trace to check
     * @param monitor
     *                The progress monitor
     * @return the trace name to use or null
     * @throws InterruptedException
     *                If the dialog box was cancelled
     * @throws CoreException
     *                If an error during deletion occurred
     */
    public String checkAndHandleNameClash(IPath tracePath, IProgressMonitor monitor) throws InterruptedException, CoreException {
        ImportConfirmation mode = checkForNameClash(tracePath);
        switch (mode) {
        case RENAME:
        case RENAME_ALL:
            return rename(tracePath);
        case OVERWRITE:
        case OVERWRITE_ALL:
            delete(tracePath, monitor);
            //$FALL-THROUGH$
        case CONTINUE:
            return tracePath.lastSegment();
        case SKIP:
        case SKIP_ALL:
        default:
            return null;
        }
    }

    // ------------------------------------------------------------------------
    // Helper methods
    // ------------------------------------------------------------------------
    private ImportConfirmation checkForNameClash(IPath tracePath) throws InterruptedException {
        // handle rename
        if (getExistingResource(tracePath) != null) {
            if ((fConfirmationMode == ImportConfirmation.RENAME_ALL) ||
                    (fConfirmationMode == ImportConfirmation.OVERWRITE_ALL) ||
                    (fConfirmationMode == ImportConfirmation.SKIP_ALL)) {
                return fConfirmationMode;
            }

            int returnCode = promptForOverwrite(tracePath);
            if (returnCode < 0) {
                // Cancel
                throw new InterruptedException();
            }
            fConfirmationMode = ImportConfirmation.values()[returnCode];
            return fConfirmationMode;
        }
        return ImportConfirmation.CONTINUE;
    }

    private int promptForOverwrite(IPath tracePath) {
        final MessageDialog dialog = new MessageDialog(fShell,
                Messages.ImportTraceWizard_MessageTitle, null, NLS.bind(Messages.ImportTraceWizard_TraceAlreadyExists, tracePath.makeRelativeTo(fTraceFolderElement.getProject().getPath())),
                MessageDialog.QUESTION, new String[] {
                        ImportConfirmation.RENAME.getInName(),
                        ImportConfirmation.RENAME_ALL.getInName(),
                        ImportConfirmation.OVERWRITE.getInName(),
                        ImportConfirmation.OVERWRITE_ALL.getInName(),
                        ImportConfirmation.SKIP.getInName(),
                        ImportConfirmation.SKIP_ALL.getInName(),
                }, 4) {
            @Override
            protected int getShellStyle() {
                return super.getShellStyle() | SWT.SHEET;
            }
        };

        final int[] returnValue = new int[1];
        fShell.getDisplay().syncExec(new Runnable() {

            @Override
            public void run() {
                returnValue[0] = dialog.open();
            }
        });
        return returnValue[0];
    }

    private static String rename(IPath tracePath) {
        IResource existingResource = getExistingResource(tracePath);
        if (existingResource == null) {
            return tracePath.lastSegment();
        }

        // Not using IFolder on purpose to leave the door open to import
        // directly into an IProject
        IContainer folder = existingResource.getParent();

        int i = 2;
        while (true) {
            String name = existingResource.getName() + '(' + Integer.toString(i++) + ')';
            IResource resource = folder.findMember(name);
            if (resource == null) {
                return name;
            }
        }
    }

    private void delete(IPath tracePath, IProgressMonitor monitor) throws CoreException {
        IResource existingResource = getExistingResource(tracePath);
        if (existingResource == null) {
            return;
        }
        TmfTraceElement existingTraceElement = getExistingTrace(tracePath);
        if (existingTraceElement != null) {
            // Delete existing TmfTraceElement
            existingTraceElement.delete(monitor, true);
            return;
        }

        // Delete resource existing in workspace
        existingResource.delete(true, monitor);
    }

    private TmfTraceElement getExistingTrace(IPath tracePath) {
        List<TmfTraceElement> traces = fTraceFolderElement.getTraces();
        for (TmfTraceElement t : traces) {
            if (t.getPath().equals(tracePath)) {
                return t;
            }
        }
        return null;
    }

    private static IResource getExistingResource(IPath tracePath) {
        // Look for existing resource
        return ResourcesPlugin.getWorkspace().getRoot().findMember(tracePath);
    }
}

