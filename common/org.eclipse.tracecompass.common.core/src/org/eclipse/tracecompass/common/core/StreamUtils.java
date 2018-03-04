/*******************************************************************************
 * Copyright (c) 2016 EfficiOS Inc., Alexandre Montplaisir
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/

package org.eclipse.tracecompass.common.core;

import static org.eclipse.tracecompass.common.core.NonNullUtils.checkNotNull;

import java.util.Iterator;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.function.Function;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

/**
 * Common utilities for {@link Stream}.
 *
 * @author Alexandre Montplaisir
 * @since 2.0
 */
@Deprecated
public final class StreamUtils {

    private StreamUtils() {}

    /**
     * Get a sequential {@link Stream} from an {@link Iterable}.
     *
     * @param iterable
     *            Any iterable
     * @return The stream on the elements of the iterable
     */
    public static <T> Stream<T> getStream(Iterable<T> iterable) {
        return StreamSupport.stream(iterable.spliterator(), false);
    }

    /**
     * Get a {@link Stream} from a generic {@link Iterator}.
     *
     * Depending on the type of terminal operation used on the stream, the
     * iterator may or may not have some elements remaining. Be wary if you
     * re-use the same iterator afterwards.
     *
     * @param iterator
     *            The iterator to wrap
     * @return A stream containing the iterator's elements
     * @since 2.2
     */
    public static <T> Stream<T> getStream(Iterator<T> iterator) {
        Spliterator<T> spliterator = Spliterators.spliteratorUnknownSize(iterator, 0);
        return StreamSupport.stream(spliterator, false);
    }

    /**
     * Generic utility class to "flatten" a data structure using the
     * {@link Stream} API.
     *
     * @param <T>
     *            The type of container, or "node" in the tree
     */
    public static class StreamFlattener<T> {

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
}
