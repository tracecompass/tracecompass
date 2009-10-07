package org.eclipse.linuxtools.lttng.event;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;

@RunWith(Suite.class)
@Suite.SuiteClasses({
    LttngTimestampTest.class,
    LttngEventFieldTest.class,
    LttngEventFormatTest.class,
    LttngEventContentTest.class,
    LttngEventReferenceTest.class,
    LttngEventTypeTest.class,
    LttngEventTest.class
})

public class AllLttngTests {

}

