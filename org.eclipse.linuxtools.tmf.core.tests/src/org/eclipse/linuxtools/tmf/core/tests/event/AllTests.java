/*******************************************************************************
 * Copyright (c) 2009, 2012 Ericsson
 * 
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *   Francois Chouinard - Initial API and implementation
 *   Francois Chouinard - Adjusted for new Event Model
 *******************************************************************************/

package org.eclipse.linuxtools.tmf.core.tests.event;

import junit.framework.Test;
import junit.framework.TestSuite;

import org.eclipse.linuxtools.tmf.core.TmfCorePlugin;

/**
 * <b><u>AllTests</u></b>
 * <p>
 */
@SuppressWarnings("nls")
public class AllTests {

	public static Test suite() {
		TestSuite suite = new TestSuite("Test suite for " + TmfCorePlugin.PLUGIN_ID + ".event"); //$NON-NLS-1$;
		//$JUnit-BEGIN$
		suite.addTestSuite(TmfTimestampTest.class);
        suite.addTestSuite(TmfSimpleTimestampTest.class);
		suite.addTestSuite(TmfTimeRangeTest.class);
		suite.addTestSuite(TmfEventFieldTest.class);
        suite.addTestSuite(TmfEventTypeTest.class);
		suite.addTestSuite(TmfEventTest.class);
        suite.addTestSuite(TmfEventTypeManagerTest.class);
		//$JUnit-END$
		return suite;
	}

}
