/**********************************************************************
 * Copyright (c) 2017, 2018 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 **********************************************************************/

package org.eclipse.tracecompass.internal.provisional.tmf.core.model.timegraph;

import org.eclipse.tracecompass.internal.provisional.tmf.core.model.tree.ITmfTreeDataModel;

/**
 * Represents a time graph entry model. These objects are typically returned by
 * {@link ITimeGraphDataProvider#fetchTree}
 *
 * @author Simon Delisle
 */
public interface ITimeGraphEntryModel extends ITmfTreeDataModel {

    /**
     * Gets the entry start time
     *
     * @return Start Time
     */
    long getStartTime();

    /**
     * Gets the entry end time
     *
     * @return End time
     */
    long getEndTime();

}
