/*******************************************************************************
 * Copyright (c) 2015 Ericsson
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   France Lapointe Nguyen - Initial API and implementation
 *******************************************************************************/

package org.eclipse.tracecompass.tmf.ui.viewers.table;

import java.util.Comparator;

import org.eclipse.jface.viewers.ILazyContentProvider;

/**
 * Interface that allows sorting in a ILazyContentProvider.
 *
 * @author France Lapointe Nguyen
 * @since 2.0
 */
public interface ISortingLazyContentProvider extends ILazyContentProvider {

    /**
     * Sets the sort order for this content provider. This sort order takes
     * priority over anything that was supplied to the <code>TableViewer</code>.
     *
     * @param comparator
     *            New comparator. The comparator must be able to support being
     *            used in a background thread.
     */
    void setSortOrder(Comparator<?> comparator);
}
