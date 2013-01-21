package org.eclipse.linuxtools.tmf.core.tests.request;

import junit.framework.Test;
import junit.framework.TestSuite;

import org.eclipse.linuxtools.internal.tmf.core.Activator;

/**
 * <b><u>AllTests</u></b>
 * <p>
 * Implement me. Please.
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
		suite.addTestSuite(TmfDataRequestTest.class);
		suite.addTestSuite(TmfEventRequestTest.class);
		suite.addTestSuite(TmfCoalescedDataRequestTest.class);
		suite.addTestSuite(TmfCoalescedEventRequestTest.class);
		suite.addTestSuite(TmfRequestExecutorTest.class);
		//$JUnit-END$
		return suite;
	}

}
