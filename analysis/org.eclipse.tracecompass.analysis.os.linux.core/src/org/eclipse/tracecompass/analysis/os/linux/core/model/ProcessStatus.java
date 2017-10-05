/*******************************************************************************
 * Copyright (c) 2017 École Polytechnique de Montréal
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/

package org.eclipse.tracecompass.analysis.os.linux.core.model;

/**
 * A list of status a thread can be in
 *
 * @author Geneviève Bastien
 * @author Francis Giraldeau
 * @since 2.4
 */
public enum ProcessStatus {

    /** Unknown process status */
    UNKNOWN(0),
    /** Waiting for a fork */
    WAIT_FORK(1),
    /** Waiting for the CPU */
    WAIT_CPU(2),
    /** The thread has exited, but is not dead yet
     * @since 2.3*/
    EXIT(3),
    /** The thread is a zombie thread */
    ZOMBIE(4),
    /** The thread is blocked */
    WAIT_BLOCKED(5),
    /** The thread is running */
    RUN(6),
    /** The thread is dead */
    DEAD(7);
    private final int fValue;

    private ProcessStatus(int value) {
        fValue = value;
    }

    private int value() {
        return fValue;
    }

    /**
     * Get the ProcessStatus associated with a long value
     *
     * @param val
     *            The long value corresponding to a status
     * @return The {@link ProcessStatus} enum value
     */
    static public ProcessStatus getStatus(long val) {
        for (ProcessStatus e : ProcessStatus.values()) {
            if (e.value() == val) {
                return e;
            }
        }
        return UNKNOWN;
    }

}
