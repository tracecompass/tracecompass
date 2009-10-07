
package org.eclipse.linuxtools.lttng.jni;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;

@RunWith(Suite.class)
@Suite.SuiteClasses({
    JniTraceTest.class,
    JniTracefileTest.class,
    JniEventTest.class,
    JniMarkerTest.class,
    JniMarkerFieldTest.class
})

public class AllJniTests {

}
