/*******************************************************************************
 * Copyright (c) 2019 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/

package org.eclipse.tracecompass.tmf.core.analysis.callsite;

import java.util.Iterator;
import java.util.NoSuchElementException;

/**
 * Callsite iterator
 *
 * @author Matthew Khouzam
 * @since 5.2
 */
public interface ITmfCallsiteIterator extends Iterator<TimeCallsite> {

    /**
     * Returns {@code true} if the backwards iteration has more elements. (In
     * other words, returns {@code true} if {@link #previous} would return an
     * element rather than throwing an exception.)
     *
     * @return {@code true} if the backwards iteration has more elements
     */
    boolean hasPrevious();

    /**
     * Returns the previous element in the iteration.
     *
     * @return the previous element in the iteration
     * @throws NoSuchElementException
     *             if the iteration has no more elements
     */
    TimeCallsite previous();

}