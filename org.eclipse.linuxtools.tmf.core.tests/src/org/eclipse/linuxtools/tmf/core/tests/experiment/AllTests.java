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

package org.eclipse.linuxtools.tmf.core.tests.experiment;

import junit.framework.Test;
import junit.framework.TestSuite;

import org.eclipse.linuxtools.internal.tmf.core.TmfCorePlugin;

/**
 * Test suite for org.eclipse.linuxtools.tmf.core.experiment
 */
@SuppressWarnings("nls")
public class AllTests {

	public static Test suite() {
		TestSuite suite = new TestSuite("Test suite for " + TmfCorePlugin.PLUGIN_ID + ".experiment"); //$NON-NLS-1$);
		//$JUnit-BEGIN$
		suite.addTestSuite(TmfExperimentCheckpointIndexTest.class);
        suite.addTestSuite(TmfExperimentTest.class);
		suite.addTestSuite(TmfMultiTraceExperimentTest.class);
		//$JUnit-END$
		return suite;
	}

}