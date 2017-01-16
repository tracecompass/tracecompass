/*******************************************************************************
 * Copyright (c) 2017 École Polytechnique de Montréal
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/

package org.eclipse.tracecompass.analysis.timing.core.tests.statistics;

import java.util.Collection;

import org.eclipse.jdt.annotation.NonNull;

/**
 * Statistics test with values of Long type
 *
 * @author Geneviève Bastien
 */
public class LongStatisticsTest extends AbstractStatisticsTest<@NonNull Long> {

    /**
     * Constructor
     */
    public LongStatisticsTest() {
        super(null);
    }

    @Override
    protected Collection<@NonNull Long> createElementsWithValues(Collection<@NonNull Long> longFixture) {
        return longFixture;
    }

}
