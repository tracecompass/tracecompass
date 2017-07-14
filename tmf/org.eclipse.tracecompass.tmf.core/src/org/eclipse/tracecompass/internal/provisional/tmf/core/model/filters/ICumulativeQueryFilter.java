/**********************************************************************
 * Copyright (c) 2017 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 **********************************************************************/

package org.eclipse.tracecompass.internal.provisional.tmf.core.model.filters;

/**
 * This represents a query filter interface used by data some providers. Its
 * purpose is to know if we want to fetch the model in cumulative or
 * differential mode
 *
 * @author Yonni Chen
 * @since 3.1
 */
public interface ICumulativeQueryFilter {

    /**
     * To know if we want to fetch model as cumulative or differential
     *
     * @return True if cumulative, false either
     */
    boolean isCumulative();
}
