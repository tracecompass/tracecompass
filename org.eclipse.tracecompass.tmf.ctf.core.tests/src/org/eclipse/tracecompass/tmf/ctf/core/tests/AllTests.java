/*******************************************************************************
 * Copyright (c) 2012, 2013 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Matthew Khouzam - Initial generation with CodePro tools
 *   Alexandre Montplaisir - Clean up, consolidate redundant tests
 *******************************************************************************/

package org.eclipse.tracecompass.tmf.ctf.core.tests;

import org.eclipse.tracecompass.tmf.core.tests.shared.DebugSuite;
import org.junit.runner.RunWith;

/**
 * The class <code>TestAll</code> builds a suite that can be used to run all
 * of the tests within its package as well as within any subpackages of its
 * package.
 *
 * @author ematkho
 */
@RunWith(DebugSuite.class)
@DebugSuite.SuiteClasses({
    CtfIteratorTest.class,
    CtfLocationDataTest.class,
    CtfLocationTest.class,
    CtfTmfContextTest.class,
    CtfTmfEventFieldTest.class,
    CtfTmfEventTest.class,
    CtfTmfEventTypeTest.class,
    CtfTmfLostEventsTest.class,
    CtfTmfTimestampTest.class,
    CtfTmfTraceTest.class,
    EventContextTest.class,
    FunkyTraceTest.class,

    /* Tests in other packages (that are there because of CTF) */
    org.eclipse.tracecompass.tmf.ctf.core.tests.request.AllTests.class,
    org.eclipse.tracecompass.tmf.ctf.core.tests.statistics.AllTests.class,
    org.eclipse.tracecompass.tmf.ctf.core.tests.tracemanager.AllTests.class
})
public class AllTests {

}
