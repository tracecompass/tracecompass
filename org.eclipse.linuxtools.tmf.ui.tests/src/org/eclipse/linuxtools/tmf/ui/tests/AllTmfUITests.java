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
 *   Bernd Hufmann - Add UML2SD tests
 *   Mathieu Denis (mathieu.denis@polymtl.ca) - Add Statistics test
 *******************************************************************************/

package org.eclipse.linuxtools.tmf.ui.tests;

import junit.framework.Test;
import junit.framework.TestSuite;

/**
 * <b><u>AllTmfUITests</u></b>
 * <p>
 * Master test suite for TMF UI Core.
 */
public class AllTmfUITests {

    /**
     * @return the TMF UI test suite
     */
	public static Test suite() {
		TestSuite suite = new TestSuite(AllTmfUITests.class.getName());
		//$JUnit-BEGIN$
		suite.addTest(org.eclipse.linuxtools.tmf.ui.tests.statistics.AllTests.suite());
        suite.addTest(org.eclipse.linuxtools.tmf.ui.tests.views.uml2sd.dialogs.AllTests.suite());
        suite.addTest(org.eclipse.linuxtools.tmf.ui.tests.views.uml2sd.loader.AllTests.suite());
        suite.addTest(org.eclipse.linuxtools.tmf.ui.tests.views.uml2sd.load.AllTests.suite());
        suite.addTest(org.eclipse.linuxtools.tmf.ui.tests.histogram.AllTests.suite());
		//$JUnit-END$
		return suite;
	}
}
