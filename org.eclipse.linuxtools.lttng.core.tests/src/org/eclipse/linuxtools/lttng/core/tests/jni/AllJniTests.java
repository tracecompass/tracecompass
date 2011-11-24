
package org.eclipse.linuxtools.lttng.core.tests.jni;

import junit.framework.TestCase;
import junit.framework.TestSuite;

@SuppressWarnings("nls")
public final class AllJniTests extends TestCase  {
    public static TestSuite suite() {
        TestSuite suite = new TestSuite("Testing JNI");
        
        suite.addTestSuite(JniTraceTest.class);
        suite.addTestSuite(JniTracefileTest.class);
        suite.addTestSuite(JniEventTest.class);
        suite.addTestSuite(JniMarkerTest.class);
        suite.addTestSuite(JniMarkerFieldTest.class);
        
        return suite;
    }
}