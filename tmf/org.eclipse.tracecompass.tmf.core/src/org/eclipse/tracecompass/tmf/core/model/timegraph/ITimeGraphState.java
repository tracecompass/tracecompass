/**********************************************************************
 * Copyright (c) 2017, 2019 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License 2.0 which
 * accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 **********************************************************************/

package org.eclipse.tracecompass.tmf.core.model.timegraph;

import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.tracecompass.tmf.core.model.ITimeElement;

/**
 * Represents a time graph state.
 *
 * @author Simon Delisle
 * @since 4.0
 */
public interface ITimeGraphState extends ITimeElement {

    /**
     * Gets the state label
     *
     * @return Label
     */
    @Nullable String getLabel();
}