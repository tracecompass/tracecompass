/*******************************************************************************
 * Copyright (c) 2017 École Polytechnique de Montréal
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/

package org.eclipse.tracecompass.analysis.timing.core.tests.statistics;

import org.eclipse.jdt.annotation.NonNull;

/**
 * Test class used for statistics test with object and mapper
 *
 * @author Geneviève Bastien
 */
public class StatObjectStub {

    private final @NonNull Long fValue;

    /**
     * Constructor
     *
     * @param value
     *            A long value
     */
    public StatObjectStub(@NonNull Long value) {
        fValue = value;
    }

    /**
     * Get the long value of this object
     *
     * @return The value of this object
     */
    public @NonNull Long getValue() {
        return fValue;
    }
}
