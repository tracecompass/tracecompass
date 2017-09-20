/*******************************************************************************
 * Copyright (c) 2017 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.eclipse.tracecompass.tmf.ui.widgets.timegraph;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jface.viewers.ViewerFilter;

/**
 * A listener which is notified when filters are added, removed or changed in
 * the timegraph viewer.
 *
 * @author Jean-Christian Kouame
 * @since 3.2
 *
 */
public interface ITimeGraphViewerFilterListener {

    /**
     * Notifies that filters have been added in the timegraph viewer.
     *
     * @param filters
     *            The added view filters
     */
    void filtersAdded(@NonNull Iterable<ViewerFilter> filters);

    /**
     * Notifies that filters have been removed in the timegraph viewer.
     *
     * @param filters
     *            The removed filters
     */
    void filtersRemoved(@NonNull Iterable<ViewerFilter> filters);

    /**
     * Notifies that filters have been changed in the timegraph viewer.
     *
     * @param filters
     *            The changed filters
     */
    void filtersChanged(@NonNull Iterable<ViewerFilter> filters);
}
