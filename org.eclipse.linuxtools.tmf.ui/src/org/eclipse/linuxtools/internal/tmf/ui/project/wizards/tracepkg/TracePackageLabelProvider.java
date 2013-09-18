/*******************************************************************************
 * Copyright (c) 2013 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Marc-Andre Laperle - Initial API and implementation
 *******************************************************************************/

package org.eclipse.linuxtools.internal.tmf.ui.project.wizards.tracepkg;

import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Display;

/**
 * A label provider for the export trace tree.
 *
 * @author Marc-Andre Laperle
 */
public class TracePackageLabelProvider extends ColumnLabelProvider {

    @Override
    public void addListener(ILabelProviderListener listener) {
    }

    @Override
    public void dispose() {
    }

    @Override
    public boolean isLabelProperty(Object element, String property) {
        return false;
    }

    @Override
    public void removeListener(ILabelProviderListener listener) {
    }

    @Override
    public Image getImage(Object element) {
        return ((TracePackageElement) element).getImage();
    }

    @Override
    public String getText(Object element) {
        return ((TracePackageElement) element).getText();
    }

    @Override
    public Color getForeground(Object element) {
        if (!((TracePackageElement) element).isEnabled()) {
            return Display.getDefault().getSystemColor(SWT.COLOR_GRAY);
        }
        return null;
    }

    @Override
    public Color getBackground(Object element) {
        return null;
    }

}