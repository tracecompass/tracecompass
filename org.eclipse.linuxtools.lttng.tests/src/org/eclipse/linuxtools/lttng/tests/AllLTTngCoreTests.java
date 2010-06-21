package org.eclipse.linuxtools.lttng.tests;

import junit.framework.Test;
import junit.framework.TestSuite;

import org.eclipse.linuxtools.lttng.control.LTTngSyntheticEventProviderTest;
import org.eclipse.linuxtools.lttng.control.LTTngSyntheticEventProviderTextTest;
import org.eclipse.linuxtools.lttng.model.LTTngTreeNodeTest;
import org.eclipse.linuxtools.lttng.state.experiment.StateExperimentManagerTextTest;
import org.eclipse.linuxtools.lttng.state.resource.LTTngStateResourceTest;
import org.eclipse.linuxtools.lttng.tests.event.LttngEventContentTest;
import org.eclipse.linuxtools.lttng.tests.event.LttngEventFieldTest;
import org.eclipse.linuxtools.lttng.tests.event.LttngEventReferenceTest;
import org.eclipse.linuxtools.lttng.tests.event.LttngEventTest;
import org.eclipse.linuxtools.lttng.tests.event.LttngEventTypeTest;
import org.eclipse.linuxtools.lttng.tests.event.LttngTimestampTest;
import org.eclipse.linuxtools.lttng.tests.jni.JniEventTest;
import org.eclipse.linuxtools.lttng.tests.jni.JniMarkerFieldTest;
import org.eclipse.linuxtools.lttng.tests.jni.JniMarkerTest;
import org.eclipse.linuxtools.lttng.tests.jni.JniTraceTest;
import org.eclipse.linuxtools.lttng.tests.jni.JniTracefileTest;
import org.eclipse.linuxtools.lttng.tests.trace.LTTngTextTraceTest;
import org.eclipse.linuxtools.lttng.tests.trace.LTTngTraceTest;

public class AllLTTngCoreTests {

	public static Test suite() {
		TestSuite suite = new TestSuite(AllLTTngCoreTests.class.getName());
		//$JUnit-BEGIN$

        suite.addTestSuite(LttngTimestampTest.class);
        suite.addTestSuite(LttngEventFieldTest.class);
        suite.addTestSuite(LttngEventContentTest.class);
        suite.addTestSuite(LttngEventReferenceTest.class);
        suite.addTestSuite(LttngEventTypeTest.class);
        suite.addTestSuite(LttngEventTest.class);
		
        suite.addTestSuite(JniTraceTest.class);
        suite.addTestSuite(JniTracefileTest.class);
        suite.addTestSuite(JniEventTest.class);
        suite.addTestSuite(JniMarkerTest.class);
        suite.addTestSuite(JniMarkerFieldTest.class);
        
        suite.addTestSuite(LTTngTextTraceTest.class);
        suite.addTestSuite(LTTngTraceTest.class);

		suite.addTestSuite(LTTngSyntheticEventProviderTest.class);
		suite.addTestSuite(LTTngSyntheticEventProviderTextTest.class);
		suite.addTestSuite(LTTngTreeNodeTest.class);
		suite.addTestSuite(StateExperimentManagerTextTest.class);
		suite.addTestSuite(LTTngStateResourceTest.class);
		//$JUnit-END$
		return suite;
	}

}
