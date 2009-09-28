
package org.eclipse.linuxtools.lttng.jni;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;

@RunWith(Suite.class)
@Suite.SuiteClasses({
    TraceTest.class,
    TracefileTest.class,
    EventTest.class,
    MarkerTest.class,
    MarkerFieldTest.class
})

public class AllJniTests {

}
