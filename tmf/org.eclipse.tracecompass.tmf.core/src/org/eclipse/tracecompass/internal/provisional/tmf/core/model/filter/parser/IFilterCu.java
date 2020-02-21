/*******************************************************************************
 * Copyright (c) 2018 École Polytechnique de Montréal
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License 2.0 which
 * accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/

package org.eclipse.tracecompass.internal.provisional.tmf.core.model.filter.parser;

import org.eclipse.tracecompass.tmf.core.filter.model.ITmfFilterTreeNode;
import org.eclipse.tracecompass.tmf.core.trace.ITmfTrace;

/**
 * Common interface for all filter compilation units
 *
 * @author Geneviève Bastien
 */
public interface IFilterCu {

    /**
     * Get an event filter from this generic filter
     *
     * @param trace
     *            A trace this filter will be applied on
     * @return The event filter corresponding to this regex filter
     */
    ITmfFilterTreeNode getEventFilter(ITmfTrace trace);

}
