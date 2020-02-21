/**********************************************************************
 * Copyright (c) 2017, 2018 Ericsson
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
 * Represents a time graph row model. Typically returned by
 * {@link ITimeGraphDataProvider#fetchRowModel}.
 *
 * @author Simon Delisle
 * @since 4.0
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