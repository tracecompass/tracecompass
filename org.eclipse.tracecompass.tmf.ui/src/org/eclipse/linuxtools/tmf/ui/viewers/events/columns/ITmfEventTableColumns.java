/*******************************************************************************
 * Copyright (c) 2014 Ericsson
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Alexandre Montplaisir - Initial API and implementation
 *******************************************************************************/

package org.eclipse.linuxtools.tmf.ui.viewers.events.columns;

import java.util.Collection;

import org.eclipse.jdt.annotation.NonNull;

/**
 * This class allows a trace type to specify which columns it wants to display
 * in the Event Table. The "function" to populate the column for any given event
 * is defined in each {@link TmfEventTableColumn}.
 *
 * @author Alexandre Montplaisir
 * @since 3.2
 */
public interface ITmfEventTableColumns {

    /**
     * Return the columns specified by this trace type.
     *
     * The iteration order of the returned collection will correspond to the
     * initial order of these columns in the view (from left to right).
     * <p>
     * Note to implementers:
     * <p>
     * Even if many traces of the same type can be opened at the same time, the
     * column objects can (and probably should) be singleton instances. This
     * means you do not need to create new column objects every time this method
     * is called.
     *
     * @return The Event Table columns advertised by this trace type
     */
    @NonNull Collection<? extends TmfEventTableColumn> getEventTableColumns();
}
