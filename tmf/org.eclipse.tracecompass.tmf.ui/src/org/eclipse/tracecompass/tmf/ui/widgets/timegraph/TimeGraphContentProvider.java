/*******************************************************************************
 * Copyright (c) 2015 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Patrick Tasse - Initial API and implementation
 *******************************************************************************/

package org.eclipse.tracecompass.tmf.ui.widgets.timegraph;

import java.util.List;

import org.eclipse.jface.viewers.Viewer;
import org.eclipse.tracecompass.tmf.ui.widgets.timegraph.model.ITimeGraphEntry;

/**
 * Base provider class for the time graph content provider
 * <p>
 * The default implementation accepts an ITimeGraphEntry[] or a List of
 * ITimeGraphEntry as input element.
 *
 * @author Patrick Tasse
 * @since 1.0
 */

public class TimeGraphContentProvider implements ITimeGraphContentProvider {

    @Override
    public ITimeGraphEntry[] getElements(Object inputElement) {
        if (inputElement instanceof ITimeGraphEntry[]) {
            return (ITimeGraphEntry[]) inputElement;
        } else if (inputElement instanceof List) {
            try {
                return ((List<?>) inputElement).toArray(new ITimeGraphEntry[0]);
            } catch (ClassCastException e) {
            }
        }
        return new ITimeGraphEntry[0];
    }

    @Override
    public boolean hasChildren(Object element) {
        ITimeGraphEntry entry = (ITimeGraphEntry) element;
        return entry.hasChildren();
    }

    @Override
    public ITimeGraphEntry[] getChildren(Object parentElement) {
        ITimeGraphEntry entry = (ITimeGraphEntry) parentElement;
        List<? extends ITimeGraphEntry> children = entry.getChildren();
        return children.toArray(new ITimeGraphEntry[children.size()]);
    }

    @Override
    public ITimeGraphEntry getParent(Object element) {
        ITimeGraphEntry entry = (ITimeGraphEntry) element;
        return entry.getParent();
    }

    @Override
    public void dispose() {
        // Do nothing
    }

    @Override
    public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
        // Do nothing
    }

}
