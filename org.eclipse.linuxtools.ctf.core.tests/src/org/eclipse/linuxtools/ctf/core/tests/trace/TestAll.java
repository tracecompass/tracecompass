package org.eclipse.linuxtools.ctf.core.tests.trace;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;

/**
 * The class <code>TestAll</code> builds a suite that can be used to run all of
 * the tests within its package as well as within any subpackages of its
 * package.
 *
 * @author ematkho
 * @version $Revision: 1.0 $
 */
@RunWith(Suite.class)
@Suite.SuiteClasses({
    CTFTraceCallsitePerformanceTest.class,
    CTFTraceReaderTest.class,
    CTFTraceTest.class,
    IOstructgenTest.class,
    MetadataTest.class,
    StreamInputPacketIndexEntryTest.class,
    StreamInputPacketIndexTest.class,
    StreamInputReaderComparatorTest.class,
    StreamInputReaderTest.class,
    StreamInputReaderTimestampComparatorTest.class,
    StreamInputTest.class,
    StreamTest.class,
    UtilsTest.class
})
public class TestAll {

}
