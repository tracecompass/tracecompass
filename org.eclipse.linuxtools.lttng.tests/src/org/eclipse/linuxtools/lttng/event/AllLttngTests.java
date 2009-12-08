package org.eclipse.linuxtools.lttng.event;

import junit.framework.TestCase;
import junit.framework.TestSuite;

public final class AllLttngTests extends TestCase  {
    public static TestSuite suite() {
        TestSuite suite = new TestSuite("Testing JNI");
        
        suite.addTestSuite(LttngTimestampTest.class);
        suite.addTestSuite(LttngEventFieldTest.class);
        suite.addTestSuite(LttngEventContentTest.class);
        suite.addTestSuite(LttngEventReferenceTest.class);
        suite.addTestSuite(LttngEventTypeTest.class);
        suite.addTestSuite(LttngEventTest.class);
        
        return suite;
    }
}

