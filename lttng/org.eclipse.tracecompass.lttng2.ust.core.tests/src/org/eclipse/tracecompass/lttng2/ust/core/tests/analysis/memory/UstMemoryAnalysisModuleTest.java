/*******************************************************************************
 * Copyright (c) 2014 École Polytechnique de Montréal
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License 2.0 which
 * accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Guilliano Molaire - Initial API and implementation
 *******************************************************************************/

package org.eclipse.tracecompass.lttng2.ust.core.tests.analysis.memory;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import org.eclipse.tracecompass.internal.lttng2.ust.core.analysis.memory.UstMemoryAnalysisModule;
import org.eclipse.tracecompass.tmf.core.analysis.requirements.TmfAbstractAnalysisRequirement;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

/**
 * Tests for the {@link UstMemoryAnalysisModule}
 *
 * @author Guilliano Molaire
 */
public class UstMemoryAnalysisModuleTest {

    /** The analysis module */
    private UstMemoryAnalysisModule fUstAnalysisModule;

    /**
     * Set-up the test
     */
    @Before
    public void setup() {
        fUstAnalysisModule = new UstMemoryAnalysisModule();
    }

    /**
     * Test for {@link UstMemoryAnalysisModule#getAnalysisRequirements()}
     */
    @Ignore("Need to assign a trace to the module to check the requirements now")
    @Test
    public void testGetAnalysisRequirements() {
        Iterable<TmfAbstractAnalysisRequirement> requirements = fUstAnalysisModule.getAnalysisRequirements();

        assertNotNull(requirements);
        assertTrue(requirements.iterator().hasNext());
    }

}
