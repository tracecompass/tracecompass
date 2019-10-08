/**********************************************************************
 * Copyright (c) 2017, 2019 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
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