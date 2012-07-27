/*******************************************************************************
 * Copyright (c) 2011, 2012 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Bernd Hufmann - Initial API and implementation
 *******************************************************************************/
package org.eclipse.linuxtools.tmf.ui.tests.views.uml2sd.dialogs;

import junit.framework.Test;
import junit.framework.TestSuite;

/**
 * Test suite of UML2SD dialog tests.
 */
public class AllTests {

    /**
     * @return the test suite of UML2SD dialog tests
     */
    public static Test suite() {

        TestSuite suite = new TestSuite(AllTests.class.getName());
        //$JUnit-BEGIN$
        suite.addTestSuite(CriteriaTest.class);
        //$JUnit-END$
        return suite;
    }
}
