/**********************************************************************
 * Copyright (c) 2015 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License 2.0 which
 * accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Bernd Hufmann - Initial API and implementation
 **********************************************************************/

package org.eclipse.tracecompass.internal.lttng2.control.ui.views.preferences;

import java.io.File;

import org.eclipse.jface.viewers.CheckboxTreeViewer;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.tracecompass.internal.lttng2.control.core.LttngProfileManager;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.PlatformUI;

/**
 * Class to create LTTng Profiles viewer (CheckboxTreeViewer).
 */
public class LTTngProfileViewer  {

    private LTTngProfileViewer() {
        // Do nothing, private constructor
    }

    /**
     * Creates a CheckboxTreeViewer for selection available LTTng profiles.
     * @param parent
     *                A parent composite
     * @param style
     *                The style bits
     * @return LTTng Profiles CheckboxTreeViewer
     */
    public static CheckboxTreeViewer createLTTngProfileViewer(Composite parent, int style) {
        CheckboxTreeViewer fFolderViewer = new CheckboxTreeViewer(parent, style);
        fFolderViewer.setContentProvider(new ProfileContentProvider());
        fFolderViewer.setLabelProvider(new ProfileLabelProvider());
        fFolderViewer.setInput(getViewerInput());
        return fFolderViewer;
    }

    /**
     * Gets the viewer input
     *
     * @return the viewer input
     */
    public static File[] getViewerInput() {
        return LttngProfileManager.getProfiles();
    }

    /**
     * Helper class for the contents of a folder in a tracing project
     */
    public static class ProfileContentProvider implements ITreeContentProvider {
        @Override
        public Object[] getChildren(Object o) {
            File store = (File) o;
            if (store.isDirectory()) {
                return store.listFiles();
            }
            return new Object[0];
        }

        @Override
        public Object getParent(Object element) {
            if (element instanceof File) {
                return ((File) element).getParent();
            }
            return null;
        }

        @Override
        public void dispose() {
            // Do nothing
        }

        @Override
        public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
            // Do nothing
        }

        @Override
        public Object[] getElements(Object inputElement) {
            if (inputElement instanceof File[]) {
                return (File[]) inputElement;
            }
            return getChildren(inputElement);
        }

        @Override
        public boolean hasChildren(Object element) {
            return ((File) element).isDirectory();
        }
    }

    /**
     * Helper label provider for LTTng profiles.
     */
    public static class ProfileLabelProvider extends LabelProvider {
        @Override
        public String getText(Object element) {
            return ((File) element).getName();
        }

        @Override
        public Image getImage(Object element) {
            if (((File) element).isDirectory()) {
                return PlatformUI.getWorkbench().getSharedImages().getImage(ISharedImages.IMG_OBJ_FOLDER);
            }
            return PlatformUI.getWorkbench().getSharedImages().getImage(ISharedImages.IMG_OBJ_FILE);
        }
    }

}
