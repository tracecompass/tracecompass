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
 *   Francois Chouinard - Adjusted for new Trace Model
 *******************************************************************************/

package org.eclipse.linuxtools.tmf.core.tests.trace;

import junit.framework.Test;
import junit.framework.TestSuite;

import org.eclipse.linuxtools.internal.tmf.core.Activator;

/**
 * Test suite for org.eclipse.linuxtools.tmf.core.trace
 */
@SuppressWarnings({ "nls" })
public class AllTests {

	/**
	 * @return the test suite
	 */
	public static Test suite() {
		TestSuite suite = new TestSuite("Test suite for " + Activator.PLUGIN_ID + ".trace"); //$NON-NLS-1$);
		//$JUnit-BEGIN$
		suite.addTestSuite(TmfLocationTest.class);
        suite.addTestSuite(TmfContextTest.class);
		suite.addTestSuite(TmfCheckpointTest.class);
        suite.addTestSuite(TmfCheckpointIndexTest.class);
        suite.addTestSuite(TmfCheckpointIndexTest2.class);
		suite.addTestSuite(TmfTraceTest.class);

        suite.addTestSuite(TmfExperimentCheckpointIndexTest.class);
        suite.addTestSuite(TmfExperimentTest.class);
        suite.addTestSuite(TmfMultiTraceExperimentTest.class);
		//$JUnit-END$
		return suite;
	}

}