package org.eclipse.linuxtools.tmf.tests.trace;

import junit.framework.Test;
import junit.framework.TestSuite;

import org.eclipse.linuxtools.tmf.TmfCorePlugin;

public class AllTests {

	public static Test suite() {
		TestSuite suite = new TestSuite("Test suite for " + TmfCorePlugin.PLUGIN_ID + ".trace"); //$NON-NLS-1$);
		//$JUnit-BEGIN$
		suite.addTestSuite(TmfTraceTest.class);
//		suite.addTestSuite(TmfExperimentTest.class);
		//$JUnit-END$
		return suite;
	}

}
