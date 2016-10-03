/*******************************************************************************
 * Copyright (c) 2016 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/

package org.eclipse.tracecompass.tmf.ui.dialog;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.eclipse.core.runtime.Path;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.swt.SWT;
import org.eclipse.swt.SWTException;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Shell;

import com.google.common.annotations.VisibleForTesting;

/**
 * A file dialog factory.
 * <p>
 * This allows file dialogs to be stubbed out for SWTBot tests.
 *
 * @author Matthew Khouzam
 * @since 2.2
 */
public final class TmfFileDialogFactory {
    private static @Nullable String[] fOverridePaths = null;

    /**
     * File dialog factory, creates a {@link FileDialog}.
     * <p>
     * Constructs a new instance of this class given only its parent.
     * </p>
     * <p>
     * If the factory is overridden with {@link #setOverrideFiles(String...)},
     * the FileDialog will return the set String when open is called instead of
     * opening a system window
     * </p>
     *
     * @param parent
     *            a shell which will be the parent of the new instance
     * @return the {@link FileDialog}
     *
     * @exception IllegalArgumentException
     *                <ul>
     *                <li>ERROR_NULL_ARGUMENT - if the parent is null</li>
     *                </ul>
     * @exception SWTException
     *                <ul>
     *                <li>ERROR_THREAD_INVALID_ACCESS - if not called from the
     *                thread that created the parent</li>
     *                <li>ERROR_INVALID_SUBCLASS - if this class is not an
     *                allowed subclass</li>
     *                </ul>
     */
    public static FileDialog create(Shell parent) {
        return create(parent, SWT.APPLICATION_MODAL);
    }

    /**
     * File dialog factory, creates a {@link FileDialog}.
     * <p>
     * Constructs a new instance of this class given its parent and a style
     * value describing its behavior and appearance.
     * </p>
     * <p>
     * The style value is either one of the style constants defined in class
     * <code>SWT</code> which is applicable to instances of this class, or must
     * be built by <em>bitwise OR</em>'ing together (that is, using the
     * <code>int</code> "|" operator) two or more of those <code>SWT</code>
     * style constants. The class description lists the style constants that are
     * applicable to the class. Style bits are also inherited from superclasses.
     * </p>
     * <p>
     * If the factory is overridden with {@link #setOverrideFiles(String[])},
     * the FileDialog will return the set String when open is called instead of
     * opening a system window
     * </p>
     *
     * @param parent
     *            a shell which will be the parent of the new instance
     * @param style
     *            the style of dialog to construct
     * @return the {@link FileDialog}
     *
     * @exception IllegalArgumentException
     *                <ul>
     *                <li>ERROR_NULL_ARGUMENT - if the parent is null</li>
     *                </ul>
     * @exception SWTException
     *                <ul>
     *                <li>ERROR_THREAD_INVALID_ACCESS - if not called from the
     *                thread that created the parent</li>
     *                <li>ERROR_INVALID_SUBCLASS - if this class is not an
     *                allowed subclass</li>
     *                </ul>
     *
     * @see SWT#SAVE
     * @see SWT#OPEN
     * @see SWT#MULTI
     */
    public static FileDialog create(Shell parent, int style) {
        String[] overridePath = fOverridePaths;
        if (overridePath != null) {
            fOverridePaths = null;
            return createNewFileDialog(parent, style, Arrays.asList(overridePath));
        }
        return new FileDialog(parent, style);
    }

    /**
     * Set the override string name that will be returned for the next
     * {@link FileDialog}. Must be called before creating the dialogs.
     *
     * This is a method aimed for testing, This should not be used in product
     * code.
     *
     * @param paths
     *            the paths to override the {@link FileDialog}. They must be
     *            absolute. One or many absolute paths may be entered. When many
     *            paths are entered, it return an input of a multi-select action
     *            if paths is null, it will undo overriding, if paths is a zero
     *            length array, it will behave as if the dialog was cancelled.
     */
    @VisibleForTesting
    @SuppressWarnings("null")
    public static void setOverrideFiles(String... paths) {
        fOverridePaths = paths;
    }

    private static FileDialog createNewFileDialog(Shell parent, int style, List<String> overridePaths) {
        return new FileDialog(parent, style) {
            @Override
            public String open() {
                return !overridePaths.isEmpty() ? overridePaths.get(0) : null;
            }

            @Override
            protected void checkSubclass() {
                /*
                 * do nothing, allow this class to be overridden without
                 * throwing a runtime exception
                 */
            }

            @Override
            public String getFileName() {
                return !overridePaths.isEmpty() ? getFileName(overridePaths.get(0)) : ""; //$NON-NLS-1$
            }

            @Override
            public String[] getFileNames() {
                List<String> outStrings = new ArrayList<>();
                for (String entry : overridePaths) {
                    outStrings.add(getFileName(entry));
                }
                return outStrings.toArray(new String[outStrings.size()]);
            }

            @Override
            public String getFilterPath() {
                return !overridePaths.isEmpty() ? new Path(overridePaths.get(0)).removeLastSegments(1).toString() : ""; //$NON-NLS-1$
            }

            private String getFileName(String path) {
                return new Path(path).lastSegment();
            }
        };
    }
}
