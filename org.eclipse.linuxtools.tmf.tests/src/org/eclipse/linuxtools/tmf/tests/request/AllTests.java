package org.eclipse.linuxtools.tmf.tests.request;

import junit.framework.Test;
import junit.framework.TestSuite;

import org.eclipse.linuxtools.tmf.TmfCorePlugin;

public class AllTests {

	public static Test suite() {
		TestSuite suite = new TestSuite("Test suite for " + TmfCorePlugin.PLUGIN_ID + ".request"); //$NON-NLS-1$);
		//$JUnit-BEGIN$
		suite.addTestSuite(TmfDataRequestTest.class);
		suite.addTestSuite(TmfEventRequestTest.class);
		suite.addTestSuite(TmfCoalescedDataRequestTest.class);
		//$JUnit-END$
		return suite;
	}

}
