/*******************************************************************************
 * Copyright (c) 2018 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Patrick Tasse - Adapted from TmfFileDialogFactory
 *******************************************************************************/

package org.eclipse.tracecompass.tmf.ui.dialog;

import org.eclipse.swt.SWT;
import org.eclipse.swt.SWTException;
import org.eclipse.swt.widgets.DirectoryDialog;
import org.eclipse.swt.widgets.Shell;

import com.google.common.annotations.VisibleForTesting;

/**
 * A directory dialog factory.
 * <p>
 * This allows native directory dialogs to be stubbed out for SWTBot tests.
 *
 * @since 3.3
 */
public final class DirectoryDialogFactory {

    private static String[] fOverridePath = null;

    /**
     * Directory dialog factory, creates a new instance of {@link DirectoryDialog}
     * given only its parent.
     * <p>
     * If the override path was previously set with
     * {@link #setOverridePath(String)}, the DirectoryDialog will return the set
     * String when open() is called instead of opening the native dialog.
     *
     * @param parent
     *            a shell which will be the parent of the new instance
     * @return the {@link DirectoryDialog}
     *
     * @exception IllegalArgumentException
     *                <ul>
     *                <li>ERROR_NULL_ARGUMENT - if the parent is null</li>
     *                </ul>
     * @exception SWTException
     *                <ul>
     *                <li>ERROR_THREAD_INVALID_ACCESS - if not called from the
     *                thread that created the parent</li>
     *                <li>ERROR_INVALID_SUBCLASS - if this class is not an allowed
     *                subclass</li>
     *                </ul>
     */
    public static DirectoryDialog create(Shell parent) {
        return create(parent, SWT.APPLICATION_MODAL);
    }

    /**
     * Directory dialog factory, creates a new instance of {@link DirectoryDialog}
     * given its parent and a style value describing its behavior and appearance.
     * <p>
     * The style value is either one of the style constants defined in class
     * <code>SWT</code> which is applicable to instances of this class, or must be
     * built by <em>bitwise OR</em>'ing together (that is, using the
     * <code>int</code> "|" operator) two or more of those <code>SWT</code> style
     * constants. The class description lists the style constants that are
     * applicable to the class. Style bits are also inherited from superclasses.
     * <p>
     * If the override path was previously set with
     * {@link #setOverridePath(String)}, the DirectoryDialog will return the set
     * String when open() is called instead of opening the native dialog.
     *
     * @param parent
     *            a shell which will be the parent of the new instance
     * @param style
     *            the style of dialog to construct
     * @return the {@link DirectoryDialog}
     *
     * @exception IllegalArgumentException
     *                <ul>
     *                <li>ERROR_NULL_ARGUMENT - if the parent is null</li>
     *                </ul>
     * @exception SWTException
     *                <ul>
     *                <li>ERROR_THREAD_INVALID_ACCESS - if not called from the
     *                thread that created the parent</li>
     *                <li>ERROR_INVALID_SUBCLASS - if this class is not an allowed
     *                subclass</li>
     *                </ul>
     */
    public static DirectoryDialog create(Shell parent, int style) {
        String[] overridePath = fOverridePath;
        if (overridePath != null) {
            fOverridePath = null;
            return new DirectoryDialog(parent, style) {
                @Override
                public String open() {
                    return overridePath[0];
                }

                @Override
                protected void checkSubclass() {
                    /* allow this class to extend DirectoryDialog */
                }
            };
        }
        return new DirectoryDialog(parent, style);
    }

    /**
     * Set the override path that will be returned by the next
     * {@link DirectoryDialog} that is created using this factory when its open()
     * method is called. Must be called before creating the dialog.
     * <p>
     * This is a method aimed for testing, it should not be used in product code.
     *
     * @param path
     *            the path to override the {@link DirectoryDialog}. It must be
     *            absolute. If the path is null, it will behave as if the dialog was
     *            cancelled.
     */
    @VisibleForTesting
    public static void setOverridePath(String path) {
        fOverridePath = new String[] { path };
    }

    /**
     * Clear the override path so that the next {@link DirectoryDialog} opens a
     * normal native dialog. Must be called before creating the dialog. It is not
     * necessary to call this method if the override path was consumed by creating a
     * dialog using this factory.
     * <p>
     * This is a method aimed for testing, it should not be used in product code.
     */
    @VisibleForTesting
    public static void clearOverridePath() {
        fOverridePath = null;
    }
}
