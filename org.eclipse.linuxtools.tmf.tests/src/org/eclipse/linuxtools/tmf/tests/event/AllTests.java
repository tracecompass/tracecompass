package org.eclipse.linuxtools.tmf.tests.event;

import junit.framework.Test;
import junit.framework.TestSuite;

public class AllTests {

	public static Test suite() {
		TestSuite suite = new TestSuite(AllTests.class.getName());
		//$JUnit-BEGIN$
		suite.addTestSuite(TmfEventFieldTest.class);
		suite.addTestSuite(TmfEventContentTest.class);
		suite.addTestSuite(TmfEventTypeTest.class);
		suite.addTestSuite(TmfEventSourceTest.class);
		suite.addTestSuite(TmfTraceEventTest.class);
		suite.addTestSuite(TmfEventReferenceTest.class);
		suite.addTestSuite(TmfTimestampTest.class);
		suite.addTestSuite(TmfTimeRangeTest.class);
		suite.addTestSuite(TmfEventTest.class);
		//$JUnit-END$
		return suite;
	}

}
