/**********************************************************************
 * Copyright (c) 2017, 2018 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 **********************************************************************/

package org.eclipse.tracecompass.tmf.core.model.timegraph;

import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.tracecompass.tmf.core.model.timegraph.IElementResolver;
import org.eclipse.tracecompass.tmf.core.model.timegraph.IPropertyCollection;

/**
 * Represents a time graph state.
 *
 * @author Simon Delisle
 * @since 4.0
 */
public interface ITimeGraphState extends IElementResolver, IPropertyCollection {

    /**
     * Gets the state start time
     *
     * @return Start time
     */
    long getStartTime();

    /**
     * Gets the state duration
     *
     * @return Duration
     */
    long getDuration();

    /**
     * Get the state value
     *
     * @return State value
     */
    int getValue();

    /**
     * Gets the state label
     *
     * @return Label
     */
    @Nullable String getLabel();

}