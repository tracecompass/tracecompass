package org.eclipse.linuxtools.lttng.tests.event;

import junit.framework.TestCase;
import junit.framework.TestSuite;
import org.eclipse.linuxtools.lttng.tests.trace.LTTngTextTraceTest;;

@SuppressWarnings("nls")
public final class AllLttngTests extends TestCase  {
    public static TestSuite suite() {
        TestSuite suite = new TestSuite("Testing LTTng (trace in text format)");
        
        suite.addTestSuite(LTTngTextTraceTest.class);
        suite.addTestSuite(LttngTimestampTest.class);
        suite.addTestSuite(LttngEventFieldTest.class);
        suite.addTestSuite(LttngEventContentTest.class);
        suite.addTestSuite(LttngEventReferenceTest.class);
        suite.addTestSuite(LttngEventTypeTest.class);
        suite.addTestSuite(LttngEventTest.class);
        
        return suite;
    }
}

