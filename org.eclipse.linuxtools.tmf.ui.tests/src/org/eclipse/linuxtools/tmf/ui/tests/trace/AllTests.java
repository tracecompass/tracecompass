package org.eclipse.linuxtools.tmf.ui.tests.trace;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;

/**
 * Test suite for Xml parser validation
 * @author Matthew Khouzam
 *
 */
@RunWith(Suite.class)
@Suite.SuiteClasses({
        CustomXmlTraceInvalidTest.class,
        CustomXmlTraceBadlyFormedTest.class,
        CustomXmlTraceValidTest.class
})
public class AllTests {
}
