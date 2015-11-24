/*******************************************************************************
 * Copyright (c) 2015 EfficiOS Inc., Alexandre Montplaisir
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/

package org.eclipse.tracecompass.common.core.collect;

import static org.eclipse.tracecompass.common.core.NonNullUtils.checkNotNull;

import java.util.function.Function;
import java.util.stream.Stream;

/**
 * Generic utility class to "flatten" a data structure using the {@link Stream}
 * API.
 *
 * @author Alexandre Montplaisir
 *
 * @param <T>
 *            The type of container, or "node" in the tree
 * @since 2.0
 */
public class StreamFlattener<T> {

    private final Function<T, Stream<T>> fGetChildrenFunction;

    /**
     * Constructor
     *
     * @param getChildrenFunction
     *            The function to use to get each element's children. Should
     *            return a {@link Stream} of those children.
     */
    public StreamFlattener(Function<T, Stream<T>> getChildrenFunction) {
        fGetChildrenFunction = getChildrenFunction;
    }

    /**
     * Do an in-order flattening of the data structure, starting at the given
     * element (or node).
     *
     * @param element
     *            The tree node or similar from which to start
     * @return A unified Stream of all the children that were found,
     *         recursively.
     */
    public Stream<T> flatten(T element) {
        Stream<T> ret = Stream.concat(
                Stream.of(element),
                fGetChildrenFunction.apply(element).flatMap(this::flatten));
        return checkNotNull(ret);
    }
}
