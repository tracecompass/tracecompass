/*******************************************************************************
 * Copyright (c) 2016 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/

package org.eclipse.tracecompass.internal.analysis.timing.ui.views.segmentstore;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jface.action.Action;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.tracecompass.internal.analysis.timing.ui.Activator;
import org.eclipse.tracecompass.tmf.ui.dialog.TmfFileDialogFactory;

/**
 * The export to TSV abstract action
 *
 * TODO: improve testing when there is a way to test native widgets
 *
 * @author Matthew Khouzam
 */
public abstract class ExportToTsvAction extends Action {

    private static final String[] EXTENSTIONS = { "*.tsv", "*.*" };//$NON-NLS-1$//$NON-NLS-2$

    /**
     * Gets the extension of TSV
     *
     * @return the extension of TSV
     */
    protected String[] getExtension() {
        return EXTENSTIONS;
    }

    @Override
    public String getText() {
        return String.valueOf(Messages.AbstractSegmentStoreTableView_exportToTsv);
    }

    @Override
    public String getToolTipText() {
        return String.valueOf(Messages.ExportToTsvAction_exportToTsvToolTip);
    }

    @Override
    public void run() {
        Shell shell = getShell();
        if (shell == null) {
            return;
        }
        FileDialog fd = TmfFileDialogFactory.create(shell);
        fd.setFilterExtensions(getExtension());
        String fileName = fd.open();
        if (fileName == null) {
            return;
        }
        try (FileOutputStream fos = new FileOutputStream(fileName)) {
            exportToTsv(fos);
        } catch (IOException e) {
            Activator.getDefault().logError("IO Error " + fileName, e); //$NON-NLS-1$
        }
    }

    /**
     * Get the shell to open the file dialog
     *
     * @return the shell
     */
    protected abstract @Nullable Shell getShell();

    /**
     * Export a given items's TSV
     *
     * @param stream
     *            an output stream to write the TSV to
     */
    protected abstract void exportToTsv(OutputStream stream);
}
