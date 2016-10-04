/*******************************************************************************
 * Copyright (c) 2017 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/

package org.eclipse.tracecompass.tmf.ui.views;

import java.util.function.Supplier;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jface.action.Action;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.tracecompass.tmf.ui.dialog.TmfFileDialogFactory;
import org.eclipse.tracecompass.tmf.ui.viewers.IImageSave;
import org.eclipse.tracecompass.tmf.ui.views.timegraph.Messages;

/**
 * Save Image action creator
 *
 * @author Matthew Khouzam
 * @since 3.3
 */
@NonNullByDefault
public final class SaveImageUtil {

    private SaveImageUtil() {
        // do nothing
    }

    /**
     * Create a save action to save the contol image
     *
     * @param name
     *            default file name
     * @param controlSupplier
     *            the supplier of the control to take a picture of
     * @return the action.
     */
    public static Action createSaveAction(@Nullable String name, Supplier<@Nullable IImageSave> controlSupplier) {
        Action saveAction = new Action(Messages.AbstractTimeGraphView_ExportImageActionText) {
            @Override
            public void run() {
                IImageSave iImageSave = controlSupplier.get();
                if(iImageSave == null) {
                    return;
                }
                FileDialog dialog = TmfFileDialogFactory.create(new Shell(), SWT.SAVE);
                // the following arrays must be in the same order
                String[] filters = { "*.png", "*.gif", "*.jpg", "*.bmp" }; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
                int[] filterTypes = { SWT.IMAGE_PNG, SWT.IMAGE_GIF, SWT.IMAGE_JPEG, SWT.IMAGE_BMP };
                dialog.setFilterExtensions(filters);
                dialog.setFileName((name == null ? "Untitled" : name) + ".png"); //$NON-NLS-1$ //$NON-NLS-2$
                String ret = dialog.open();
                if ((ret != null) && !ret.isEmpty()) {
                    int index = dialog.getFilterIndex();
                    iImageSave.saveImage(ret, filterTypes[index]);
                }
            }
        };
        saveAction.setToolTipText(Messages.AbstractTimeGraphView_ExportImageToolTipText);
        return saveAction;
    }
}
