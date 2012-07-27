/*******************************************************************************
 * Copyright (c) 2011 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Bernd Hufmann - Initial API and implementation
 *******************************************************************************/
package org.eclipse.linuxtools.tmf.ui.tests.views.uml2sd.load;

import junit.framework.Test;
import junit.framework.TestSuite;

/**
 *  Test suite for testing loader manager of UML2SD extension point.
 */
public class AllTests {

    /**
     * @return the test suite.
     */
    public static Test suite() {

        TestSuite suite = new TestSuite(AllTests.class.getName());
        //$JUnit-BEGIN$
        suite.addTestSuite(LoadersManagerTest.class);
        //$JUnit-END$
        return suite;
    }
}
