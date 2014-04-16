/*******************************************************************************
 * Copyright (c) 2010, 2014 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Francois Chouinard - Initial API and implementation
 *   Patrick Tasse - Add support for folder elements
 *******************************************************************************/

package org.eclipse.linuxtools.tmf.ui.project.model;

import org.eclipse.jface.viewers.LabelProvider;

/**
 * Label provider implementation for trace folders for viewers that display
 * the content of a trace folder.
 * <p>
 *
 * @version 1.0
 * @author Francois Chouinard
 */
public class TraceFolderLabelProvider extends LabelProvider {

    @Override
    public String getText(Object element) {
        if (element instanceof TmfTraceElement) {
            TmfTraceElement entry = (TmfTraceElement) element;
            return entry.getElementPath();
        }
        return null;
    }

}
