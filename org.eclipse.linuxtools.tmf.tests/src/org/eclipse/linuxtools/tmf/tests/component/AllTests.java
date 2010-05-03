package org.eclipse.linuxtools.tmf.tests.component;

import junit.framework.Test;
import junit.framework.TestSuite;

public class AllTests {

	public static Test suite() {
		TestSuite suite = new TestSuite(AllTests.class.getName());
		//$JUnit-BEGIN$
		suite.addTestSuite(TmfProviderManagerTest.class);
		suite.addTestSuite(TmfEventProviderTest.class);
		//$JUnit-END$
		return suite;
	}

}
