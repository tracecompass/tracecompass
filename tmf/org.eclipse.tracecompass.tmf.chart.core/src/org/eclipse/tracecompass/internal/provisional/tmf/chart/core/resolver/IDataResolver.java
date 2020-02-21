/*******************************************************************************
 * Copyright (c) 2016 École Polytechnique de Montréal
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License 2.0 which
 * accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/

package org.eclipse.tracecompass.internal.provisional.tmf.chart.core.resolver;

import java.util.function.Function;

import org.eclipse.jdt.annotation.Nullable;

/**
 * This interface is used for mapping data inside a stream of object. Since the
 * stream of data can be any composed object, it is necessary to have a mapper
 * that returns a specific member inside a given object.
 * <p>
 * This class does not extend {@link Function} because a resolver:
 * <ul>
 * <li>can do more than simply mapping values (e.g. it might compare
 * numbers)</li>
 * <li>should be only used in the chart plugin</li>
 * </ul>
 *
 * @param <T>
 *            The type of the input
 * @param <R>
 *            The type of the output when the input is resolved
 *
 * @author Gabriel-Andrew Pollo-Guilbert
 */
public interface IDataResolver<T, R> {

    /**
     * Get a function that maps an object to data to be shown on a chart.
     *
     * @return The mapper function
     */
    Function<T, @Nullable R> getMapper();

}
