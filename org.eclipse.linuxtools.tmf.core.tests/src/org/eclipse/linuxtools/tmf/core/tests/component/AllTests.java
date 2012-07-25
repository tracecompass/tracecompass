package org.eclipse.linuxtools.tmf.core.tests.component;

import junit.framework.Test;
import junit.framework.TestSuite;

/**
 * <b><u>AllTests</u></b>
 * <p>
 * Implement me. Please.
 * <p>
 */
public class AllTests {

	/**
	 * @return the TMF Core Component test suite
	 */
	public static Test suite() {
		TestSuite suite = new TestSuite(AllTests.class.getName());
		//$JUnit-BEGIN$
		suite.addTestSuite(TmfProviderManagerTest.class);
		suite.addTestSuite(TmfEventProviderTest.class);
		//$JUnit-END$
		return suite;
	}

}
