package org.eclipse.linuxtools.tmf.trace;

import junit.framework.Test;
import junit.framework.TestSuite;

public class AllTests {

	public static Test suite() {
		TestSuite suite = new TestSuite(AllTests.class.getName());
		//$JUnit-BEGIN$
		suite.addTestSuite(TmfTraceTest.class);
		suite.addTestSuite(TmfExperimentTest.class);
		//$JUnit-END$
		return suite;
	}

}
