/*******************************************************************************
 * Copyright (c) 2015 EfficiOS Inc. and others
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Alexandre Montplaisir - Initial API and implementation
 *******************************************************************************/

package org.eclipse.tracecompass.alltests.swtbot;

import junit.framework.JUnit4TestAdapter;
import junit.framework.TestSuite;

/**
 * Run the {@link RunAllSWTBotTests} suite a lot of times, to catch flaky tests.
 */
public class SWTBotStressTests extends TestSuite {

    private static final int NB_RUNS = 20;

    /**
     * @return Test suite definition
     */
    public static TestSuite suite() {
        TestSuite s = new TestSuite();
        for (int i = 0; i < NB_RUNS; i++) {
            s.addTest(new JUnit4TestAdapter(RunAllSWTBotTests.class));
        }
        return s;
    }
}