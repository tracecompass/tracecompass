/*******************************************************************************
 * Copyright (c) 2016 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
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
