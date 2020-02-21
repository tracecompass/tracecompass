/*******************************************************************************
 * Copyright (c) 2016 Ericsson
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
import java.util.stream.Collectors;

import org.eclipse.jdt.annotation.NonNull;

/**
 * Test the segment statistics class with a dummy object and a mapper function
 *
 * @author Geneviève Bastien
 */
public class ObjectStatisticsTest extends AbstractStatisticsTest<@NonNull StatObjectStub> {

    /**
     * Constructor
     */
    public ObjectStatisticsTest() {
        super(e -> e.getValue());
    }

    @Override
    protected Collection<@NonNull StatObjectStub> createElementsWithValues(Collection<@NonNull Long> longFixture) {
        return longFixture.stream()
                .map(l -> new StatObjectStub(l))
                .collect(Collectors.toList());
    }

}
