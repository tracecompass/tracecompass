package org.eclipse.linuxtools.tmf.tests;

import junit.framework.Test;
import junit.framework.TestSuite;

import org.eclipse.linuxtools.tmf.tests.component.TmfEventProviderTest;
import org.eclipse.linuxtools.tmf.tests.component.TmfProviderManagerTest;
import org.eclipse.linuxtools.tmf.tests.event.TmfEventContentTest;
import org.eclipse.linuxtools.tmf.tests.event.TmfEventFieldTest;
import org.eclipse.linuxtools.tmf.tests.event.TmfEventReferenceTest;
import org.eclipse.linuxtools.tmf.tests.event.TmfEventSourceTest;
import org.eclipse.linuxtools.tmf.tests.event.TmfEventTest;
import org.eclipse.linuxtools.tmf.tests.event.TmfEventTypeTest;
import org.eclipse.linuxtools.tmf.tests.event.TmfTimeRangeTest;
import org.eclipse.linuxtools.tmf.tests.event.TmfTimestampTest;
import org.eclipse.linuxtools.tmf.tests.event.TmfTraceEventTest;
import org.eclipse.linuxtools.tmf.tests.request.TmfCoalescedDataRequestTest;
import org.eclipse.linuxtools.tmf.tests.request.TmfCoalescedEventRequestTest;
import org.eclipse.linuxtools.tmf.tests.request.TmfDataRequestTest;
import org.eclipse.linuxtools.tmf.tests.request.TmfEventRequestTest;
import org.eclipse.linuxtools.tmf.tests.trace.TmfExperimentTest;
import org.eclipse.linuxtools.tmf.tests.trace.TmfMultiTraceExperimentTest;
import org.eclipse.linuxtools.tmf.tests.trace.TmfTraceTest;

public class AllTmfCoreTests {

	public static Test suite() {
		TestSuite suite = new TestSuite(AllTmfCoreTests.class.getName());
		//$JUnit-BEGIN$
		suite.addTestSuite(TmfCorePluginTest.class);

		suite.addTestSuite(TmfEventFieldTest.class);
		suite.addTestSuite(TmfEventContentTest.class);
		suite.addTestSuite(TmfEventTypeTest.class);
		suite.addTestSuite(TmfEventSourceTest.class);
		suite.addTestSuite(TmfTraceEventTest.class);
		suite.addTestSuite(TmfEventReferenceTest.class);
		suite.addTestSuite(TmfTimestampTest.class);
		suite.addTestSuite(TmfTimeRangeTest.class);
		suite.addTestSuite(TmfEventTest.class);

		suite.addTestSuite(TmfDataRequestTest.class);
		suite.addTestSuite(TmfEventRequestTest.class);
		suite.addTestSuite(TmfCoalescedDataRequestTest.class);
		suite.addTestSuite(TmfCoalescedEventRequestTest.class);
//		suite.addTestSuite(TmfRequestExecutorTest.class);

		suite.addTestSuite(TmfEventProviderTest.class);
		suite.addTestSuite(TmfProviderManagerTest.class);

		suite.addTestSuite(TmfTraceTest.class);
		suite.addTestSuite(TmfExperimentTest.class);
		suite.addTestSuite(TmfMultiTraceExperimentTest.class);
		//$JUnit-END$
		return suite;
	}

}
