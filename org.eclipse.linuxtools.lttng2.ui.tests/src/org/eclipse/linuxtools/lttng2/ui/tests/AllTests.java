/*******************************************************************************
 * Copyright (c) 2012 Ericsson
 * 
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *   Francois Chouinard - Initial API and implementation
 *******************************************************************************/

package org.eclipse.linuxtools.lttng2.ui.tests;

import junit.framework.Test;
import junit.framework.TestSuite;

/**
 * <b><u>AllTests</u></b>
 * <p>
 */
public class AllTests {

    public static Test suite() {
        TestSuite suite = new TestSuite(AllTests.class.getName());
        // $JUnit-BEGIN$

        // Plug-in
        suite.addTestSuite(ActivatorTest.class);
        suite.addTest(org.eclipse.linuxtools.lttng2.ui.tests.control.service.AllTests.suite());
        suite.addTest(org.eclipse.linuxtools.lttng2.ui.tests.control.model.component.AllTests.suite());

        // $JUnit-END$
        return suite;
    }
}
