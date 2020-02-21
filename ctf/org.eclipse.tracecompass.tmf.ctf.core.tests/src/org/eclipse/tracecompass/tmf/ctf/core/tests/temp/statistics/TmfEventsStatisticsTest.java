/*******************************************************************************
 * Copyright (c) 2012, 2014 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License 2.0 which
 * accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Alexandre Montplaisir - Initial API and implementation
 ******************************************************************************/

package org.eclipse.tracecompass.tmf.ctf.core.tests.temp.statistics;

import org.eclipse.tracecompass.tmf.core.statistics.TmfEventsStatistics;
import org.eclipse.tracecompass.tmf.ctf.core.tests.shared.CtfTmfTestTraceUtils;
import org.junit.BeforeClass;

/**
 * Unit tests for the {@link TmfEventsStatistics}
 *
 * @author Alexandre Montplaisir
 */
public class TmfEventsStatisticsTest extends TmfStatisticsTest {

    /**
     * Set up the fixture once for all tests.
     */
    @BeforeClass
    public static void setUpClass() {
        backend = new TmfEventsStatistics(CtfTmfTestTraceUtils.getTrace(testTrace));
    }
}
