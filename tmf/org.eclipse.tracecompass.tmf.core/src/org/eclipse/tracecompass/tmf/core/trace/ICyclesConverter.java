/*******************************************************************************
 * Copyright (c) 2017 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Patrick Tasse - Initial API and implementation
 *******************************************************************************/

package org.eclipse.tracecompass.tmf.core.trace;

/**
 * An adapter interface for traces, which allows the trace to provide conversion
 * functions between cycles and nanoseconds.
 *
 * @since 2.3
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
