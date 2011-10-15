/*******************************************************************************
 * Copyright (c) 2010, 2011 Ericsson
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

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.Viewer;

/**
 * <b><u>TraceFolderContentProvider</u></b>
 * <p>
 * TODO: Implement me. Please.
 */
public class TraceFolderContentProvider implements IStructuredContentProvider {

    @Override
    public Object[] getElements(Object inputElement) {
        if (inputElement instanceof TmfTraceFolder) {
            TmfTraceFolder folder = (TmfTraceFolder) inputElement;
            List<ITmfProjectModelElement> elements = new ArrayList<ITmfProjectModelElement>();
            for (ITmfProjectModelElement element : folder.getChildren()) {
                if (element instanceof TmfTraceElement) {
                    TmfTraceElement trace = (TmfTraceElement) element;
                    if (trace.getTraceType() != null) {
                        elements.add(trace);
                    }
                }
            }
            return elements.toArray();
        }
        return null;
    }

    @Override
    public void dispose() {
    }

    @Override
    public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
    }

}
