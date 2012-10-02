/*******************************************************************************
 * Copyright (c) 2012 Ericsson
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

package org.eclipse.linuxtools.tmf.core.tests.ctfadaptor;

import org.junit.runner.JUnitCore;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;

/**
 * The class <code>TestAll</code> builds a suite that can be used to run all
 * of the tests within its package as well as within any subpackages of its
 * package.
 *
 * @author ematkho
 * @version 1.0
 */
@RunWith(Suite.class)
@Suite.SuiteClasses({
    CtfIteratorTest.class,
    CtfLocationDataTest.class,
    CtfLocationTest.class,
    CtfTmfEventFieldTest.class,
    CtfTmfEventTest.class,
    CtfTmfEventTypeTest.class,
    CtfTmfTimestampTest.class,
    CtfTmfTraceTest.class,
    CtfTmfLightweightContextTest.class
})
public class TestAll {

    /**
     * Launch the test.
     *
     * @param args
     *            the command line arguments
     */
    public static void main(String[] args) {
        JUnitCore.runClasses(new Class[] { TestAll.class });
    }
}
