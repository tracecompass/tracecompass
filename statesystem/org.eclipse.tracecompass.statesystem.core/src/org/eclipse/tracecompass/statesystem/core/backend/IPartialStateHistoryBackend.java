/*******************************************************************************
 * Copyright (c) 2022 École Polytechnique de Montréal
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License 2.0 which
 * accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/

package org.eclipse.tracecompass.statesystem.core.backend;

import org.eclipse.tracecompass.internal.provisional.datastore.core.condition.IntegerRangeCondition;
import org.eclipse.tracecompass.internal.provisional.datastore.core.condition.TimeRangeCondition;

/**
 * This interface extends IStateHistoryBackend in order to add some methods
 * specific for the partial state system.
 *
 * @author Abdellah Rahmani
 * @since 5.2
 */
public interface IPartialStateHistoryBackend extends IStateHistoryBackend {

    /**
     * Passes the updated values of the attributes (quarks) condition to the
     * PartialInMemoryBackend.
     *
     * @param quarksRangeCondition
     *            The range of the required attributes by the query2D()
     *
     * @since 5.1
     */
    void updateRangeCondition(IntegerRangeCondition quarksRangeCondition);

    /**
     * Passes the updated values of the requested times to the
     * PartialInMemoryBackend.
     *
     * @param timeConditionrange
     *            The range of the required attributes by the query2D()
     *
     * @since 5.1
     */
    void updateTimeCondition(TimeRangeCondition timeConditionrange);

    /**
     * Tells the PartialInMemoryBackend if the actual query is a 2D or not.
     *
     * @param type
     *            The boolean parameter that if equals "true" then the query is
     *            a 2D
     *
     * @since 5.1
     */
    void updateQueryType(boolean type);

}
