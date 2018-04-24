/**********************************************************************
 * Copyright (c) 2017, 2018 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 **********************************************************************/

package org.eclipse.tracecompass.internal.provisional.tmf.core.model.timegraph;

import java.util.List;

/**
 * Represents a time graph row model. Typically returned by
 * {@link ITimeGraphDataProvider#fetchRowModel}.
 *
 * @author Simon Delisle
 */
public interface ITimeGraphRowModel {

    /**
     * Gets the {@link ITimeGraphEntryModel} associated with this row
     *
     * @return Entry model ID
     */
    long getEntryID();

    /**
     * Gets the list of associated {@link ITimeGraphState}
     *
     * @return List of states
     */
    List<ITimeGraphState> getStates();

}