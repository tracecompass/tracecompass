/*******************************************************************************
 * Copyright (c) 2017 École Polytechnique de Montréal
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License 2.0 which
 * accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/

package org.eclipse.tracecompass.tmf.chart.core.tests.stubs;

/**
 * A stub object to use by this stub
 *
 * @author Geneviève Bastien
 */
public class StubObject {
    private final String fString;
    private final Integer fInt;
    private final Long fLong;
    private final Double fDbl;

    /**
     * Constructor
     *
     * @param string
     *            A string value
     * @param intVal
     *            An int value
     * @param longVal
     *            A long value
     * @param dblVal
     *            A double value
     */
    public StubObject(String string, Integer intVal, Long longVal, Double dblVal) {
        fString = string;
        fInt = intVal;
        fLong = longVal;
        fDbl = dblVal;
    }

    /**
     * Get the string value
     *
     * @return The string value
     */
    public String getString() {
        return fString;
    }

    /**
     * Get the int value
     *
     * @return the int value
     */
    public Integer getInt() {
        return fInt;
    }

    /**
     * Get the long value
     *
     * @return The long value
     */
    public Long getLong() {
        return fLong;
    }

    /**
     * Get the double value
     *
     * @return the double value
     */
    public Double getDbl() {
        return fDbl;
    }
}
