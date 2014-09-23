/*******************************************************************************
 * Copyright (c) 2014 École Polytechnique de Montréal
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Guilliano Molaire - Initial API and implementation
 *******************************************************************************/

package org.eclipse.linuxtools.lttng2.ust.core.tests.analysis.memory;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.Set;

import org.eclipse.linuxtools.internal.lttng2.ust.core.memoryusage.UstMemoryStrings;
import org.eclipse.linuxtools.lttng2.control.core.session.SessionConfigStrings;
import org.eclipse.linuxtools.lttng2.ust.core.analysis.memory.UstMemoryAnalysisModule;
import org.eclipse.linuxtools.tmf.core.analysis.TmfAnalysisRequirement;
import org.junit.Before;
import org.junit.Test;

import com.google.common.collect.ImmutableSet;

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
    @Test
    public void testGetAnalysisRequirements() {
        Iterable<TmfAnalysisRequirement> requirements = fUstAnalysisModule.getAnalysisRequirements();
        assertNotNull(requirements);
        assertTrue(requirements.iterator().hasNext());

        /* There should be the event and domain type */
        TmfAnalysisRequirement eventReq = null;
        TmfAnalysisRequirement domainReq = null;
        int numberOfRequirement = 0;
        for (TmfAnalysisRequirement requirement : requirements) {
            ++numberOfRequirement;
            if (requirement.getType().equals(SessionConfigStrings.CONFIG_ELEMENT_EVENT)) {
                eventReq = requirement;
            } else if (requirement.getType().equals(SessionConfigStrings.CONFIG_ELEMENT_DOMAIN)) {
                domainReq = requirement;
            }
        }
        assertNotNull(eventReq);
        assertNotNull(domainReq);

        /* There should be two requirements */
        assertEquals(2, numberOfRequirement);

        /* Verify the content of the requirements themselves */
        /* Domain should be kernel */
        assertEquals(1, domainReq.getValues().size());
        for (String domain : domainReq.getValues()) {
            assertEquals(SessionConfigStrings.CONFIG_DOMAIN_TYPE_UST, domain);
        }

        /* Events */
        Set<String> expectedEvents = ImmutableSet.of(
                UstMemoryStrings.MALLOC,
                UstMemoryStrings.FREE,
                UstMemoryStrings.CALLOC,
                UstMemoryStrings.REALLOC,
                UstMemoryStrings.MEMALIGN,
                UstMemoryStrings.POSIX_MEMALIGN
                );

        assertEquals(6, eventReq.getValues().size());
        for (String event : eventReq.getValues()) {
            assertTrue("Unexpected event " + event, expectedEvents.contains(event));
        }

        Set<String> infos = eventReq.getInformation();
        assertEquals(2, infos.size());
    }

}
