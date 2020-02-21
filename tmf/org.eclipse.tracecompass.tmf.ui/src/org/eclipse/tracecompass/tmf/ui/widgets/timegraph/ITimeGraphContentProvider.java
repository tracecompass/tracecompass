/*******************************************************************************
 * Copyright (c) 2014, 2015 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License 2.0 which
 * accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Patrick Tasse - Initial API and implementation
 *******************************************************************************/

package org.eclipse.tracecompass.tmf.ui.widgets.timegraph;

import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.tracecompass.tmf.ui.widgets.timegraph.model.ITimeGraphEntry;


/**
 * A content provider mediates between the viewer's model
 * and the viewer itself.
 */
public interface ITimeGraphContentProvider extends ITreeContentProvider {
    /**
     * Returns the time graph entries to display in the viewer when its input is
     * set to the given element.
     *
     * @param inputElement
     *            the input element
     * @return the array of time graph entries to display in the viewer
     */
    @Override
    public ITimeGraphEntry[] getElements(Object inputElement);

    /**
     * @since 1.0
     */
    @Override
    public ITimeGraphEntry[] getChildren(Object parentElement);

    /**
     * @since 1.0
     */
    @Override
    public ITimeGraphEntry getParent(Object element);
}
