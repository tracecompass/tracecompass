/*******************************************************************************
 * Copyright (c) 2020 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0 which
 * accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/

package org.eclipse.tracecompass.internal.analysis.timing.core.segmentstore;

import java.util.function.Function;

import org.eclipse.jdt.annotation.Nullable;

/**
 * An aspect is a piece of information that can be extracted, directly or
 * indirectly, from an object of type <code>E</code>.
 *
 * The aspect can then be used to populate table columns, to filter on to only
 * keep certain inputs, to plot XY charts, etc.
 *
 * Inspired by ITmfEventAspect implementation.
 *
 * @author Bernd Hufmann
 * @param <E>
 *            The type of object as input for resolving
 */
public interface IDataAspect<E> extends Function<E, @Nullable Object> {

    /**
     * Get the name of this aspect. This name will be user-visible and, as such,
     * should be localized.
     *
     * @return The name of this aspect.
     */
    String getName();

    /**
     * Return a descriptive help text of what this aspect does. This could then
     * be shown in tooltip or in option dialogs for instance. It should also be
     * localized.
     *
     * @return The help text of this aspect
     */
    default String getHelpText() {
        return ""; //$NON-NLS-1$
    }
}
