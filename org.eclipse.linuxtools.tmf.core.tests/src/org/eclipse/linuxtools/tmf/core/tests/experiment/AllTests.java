package org.eclipse.linuxtools.tmf.core.tests.experiment;

import junit.framework.Test;
import junit.framework.TestSuite;

import org.eclipse.linuxtools.internal.tmf.core.TmfCorePlugin;

@SuppressWarnings("nls")
public class AllTests {

	public static Test suite() {
		TestSuite suite = new TestSuite("Test suite for " + TmfCorePlugin.PLUGIN_ID + ".experiment"); //$NON-NLS-1$);
		//$JUnit-BEGIN$
		suite.addTestSuite(TmfExperimentTest.class);
		suite.addTestSuite(TmfMultiTraceExperimentTest.class);
		//$JUnit-END$
		return suite;
	}

}