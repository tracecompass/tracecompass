package org.eclipse.linuxtools.tmf.tests.component;

import junit.framework.Test;
import junit.framework.TestSuite;

public class AllTests {

	public static Test suite() {
		TestSuite suite = new TestSuite(AllTests.class.getName());
		//$JUnit-BEGIN$
		suite.addTestSuite(TmfEventProviderTest.class);
		suite.addTestSuite(TmfProviderManagerTest.class);
		//$JUnit-END$
		return suite;
	}

}
