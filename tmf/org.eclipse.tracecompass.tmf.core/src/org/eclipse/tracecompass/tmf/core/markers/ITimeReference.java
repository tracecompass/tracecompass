/*******************************************************************************
 * Copyright (c) 2021 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License 2.0 which
 * accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/

package org.eclipse.tracecompass.tmf.core.markers;

/**
 * Time reference interface, allows one to get a time and an index. This is
 * useful for marker locations or any correlation between gantt charts and other
 * views.
 *
 * @author Matthew Khouzam
 * @since 7.0
 */
public interface ITimeReference {

    /**
     * Zero reference at index 0 and time 0
     */
    ITimeReference ZERO = new TimeReference(0, 0);

    /**
     * Gets the reference marker time.
     *
     * @return the reference marker time
     */
    long getTime();

    /**
     * Gets the reference marker index.
     *
     * @return the reference marker index
     */
    long getIndex();

}
