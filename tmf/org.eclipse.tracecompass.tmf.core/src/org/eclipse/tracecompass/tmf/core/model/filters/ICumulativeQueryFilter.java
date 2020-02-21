/**********************************************************************
 * Copyright (c) 2017 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License 2.0 which
 * accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 **********************************************************************/

package org.eclipse.tracecompass.tmf.core.model.filters;

/**
 * This represents a query filter interface used by data some providers. Its
 * purpose is to know if we want to fetch the model in cumulative or
 * differential mode
 *
 * @author Yonni Chen
 * @since 4.0
 */
public interface ICumulativeQueryFilter {

    /**
     * To know if we want to fetch model as cumulative or differential
     *
     * @return True if cumulative, false either
     */
    boolean isCumulative();
}
