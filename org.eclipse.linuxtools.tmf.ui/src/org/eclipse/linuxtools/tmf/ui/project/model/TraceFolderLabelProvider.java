/*******************************************************************************
 * Copyright (c) 2010, 2011, 2012 Ericsson
 * 
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *   Francois Chouinard - Initial API and implementation
 *******************************************************************************/

package org.eclipse.linuxtools.tmf.ui.project.model;

import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.swt.graphics.Image;

/**
 * Label provider implementation for trace folders for tree viewers that display
 * the content of a trace folder.
 * <p>
 * 
 * @version 1.0
 * @author Francois Chouinard 
 */
public class TraceFolderLabelProvider extends LabelProvider implements ITableLabelProvider {

    /*
     * (non-Javadoc)
     * @see org.eclipse.jface.viewers.ITableLabelProvider#getColumnImage(java.lang.Object, int)
     */
    @Override
    public Image getColumnImage(Object element, int columnIndex) {
        return null;
    }

    /*
     * (non-Javadoc)
     * @see org.eclipse.jface.viewers.ITableLabelProvider#getColumnText(java.lang.Object, int)
     */
    @Override
    public String getColumnText(Object element, int columnIndex) {
        if (element instanceof TmfTraceElement) {
            TmfTraceElement entry = (TmfTraceElement) element;
            switch (columnIndex) {
                case 0:
                    return entry.getName();
                default:
                    return null;
            }
        }
        return null;
    }

}
