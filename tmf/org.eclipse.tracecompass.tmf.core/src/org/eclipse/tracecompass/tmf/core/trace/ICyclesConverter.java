/*******************************************************************************
 * Copyright (c) 2017 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License 2.0 which
 * accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Patrick Tasse - Initial API and implementation
 *******************************************************************************/

package org.eclipse.tracecompass.tmf.core.trace;

/**
 * An adapter interface for traces, which allows the trace to provide conversion
 * functions between cycles and nanoseconds.
 *
 * @since 3.0
 */
public interface ICyclesConverter {

    /**
     * Convert cycles to nanoseconds
     *
     * @param cycles
     *            the number of cycles
     * @return the number of nanoseconds
     */
    long cyclesToNanos(long cycles);

    /**
     * Convert nanoseconds to cycles
     *
     * @param nanos
     *            the number of nanoseconds
     * @return the number of cycles
     */
    long nanosToCycles(long nanos);

}
