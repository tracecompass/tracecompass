/**********************************************************************
 * Copyright (c) 2017, 2020 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License 2.0 which
 * accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 **********************************************************************/

package org.eclipse.tracecompass.tmf.core.model.timegraph;

import java.util.List;

/**
 * Implementation of {@link ITimeGraphRowModel}.
 *
 * @author Simon Delisle
 * @since 4.0
 */
public class TimeGraphRowModel implements ITimeGraphRowModel {
    private final long fEntryID;
    private final List<ITimeGraphState> fStates;

    /**
     * Constructor
     *
     * @param entryID
     *            Entry model's ID for this row
     * @param states
     *            List of {@link TimeGraphState}
     */
    public TimeGraphRowModel(long entryID, List<ITimeGraphState> states) {
        fEntryID = entryID;
        fStates = states;
    }

    @Override
    public List<ITimeGraphState> getStates() {
        return fStates;
    }

    @Override
    public long getEntryID() {
        return fEntryID;
    }

    @Override
    public String toString() {
        return String.format("Row Model: entryId: %d, states size: %d", fEntryID, fStates.size()); //$NON-NLS-1$
    }
}
