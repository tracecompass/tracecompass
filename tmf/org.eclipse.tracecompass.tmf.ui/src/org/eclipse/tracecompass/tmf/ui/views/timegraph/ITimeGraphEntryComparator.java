/*******************************************************************************
 * Copyright (c) 2016 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.eclipse.tracecompass.tmf.ui.views.timegraph;

import java.util.Comparator;

import org.eclipse.tracecompass.tmf.ui.widgets.timegraph.model.ITimeGraphEntry;

/**
 * Comparator interface for for sorting of time graph entries.
 *
 * Use this comparator interface for sorting of time graph entries where a different
 * behavior is required depending on the sort direction.
 *
 * @author Bernd Hufmann
 * @since 2.0
 *
 */
public interface ITimeGraphEntryComparator extends Comparator<ITimeGraphEntry> {

    /**
     * Sets the sort direction.
     *
     * @param direction
     *            The sort direction
     */
    void setDirection(int direction);
}
