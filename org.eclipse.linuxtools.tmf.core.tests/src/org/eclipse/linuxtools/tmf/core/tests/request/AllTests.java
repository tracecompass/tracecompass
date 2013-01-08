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

package org.eclipse.linuxtools.tmf.core.tests.request;

import junit.framework.Test;
import junit.framework.TestSuite;

import org.eclipse.linuxtools.internal.tmf.core.Activator;

/**
 * <b><u>AllTests</u></b>
 * <p>
 * Test suite for the package o.e.l.tmf.core.request
 * <p>
 */
@SuppressWarnings({ "nls" })
public class AllTests {

	/**
	 * @return the test suite
	 */
	public static Test suite() {
		TestSuite suite = new TestSuite("Test suite for " + Activator.PLUGIN_ID + ".request"); //$NON-NLS-1$);
		//$JUnit-BEGIN$
        suite.addTestSuite(TmfBlockFilterTest.class);
        suite.addTestSuite(TmfRangeFilterTest.class);
        suite.addTestSuite(TmfEventTypeFilterTest.class);
        suite.addTestSuite(TmfRequestTest.class);
        suite.addTestSuite(TmfCoalescedRequestTest.class);
		suite.addTestSuite(TmfDataRequestTest.class);
		suite.addTestSuite(TmfEventRequestTest.class);
		suite.addTestSuite(TmfRequestExecutorTest.class);
		//$JUnit-END$
		return suite;
	}

}
