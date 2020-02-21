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
