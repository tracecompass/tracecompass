package org.eclipse.linuxtools.tmf.core.tests.ctfadaptor;

import org.junit.runner.JUnitCore;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;

/**
 * The class <code>TestAll</code> builds a suite that can be used to run all
 * of the tests within its package as well as within any subpackages of its
 * package.
 *
 * @generatedBy CodePro at 03/05/12 2:29 PM
 * @author ematkho
 * @version $Revision: 1.0 $
 */
@RunWith(Suite.class)
@Suite.SuiteClasses({
    CtfTmfContentTest.class,
    CtfTmfTimestampTest.class,
    CtfTmfEventFieldTest.class,
    CtfTmfEventTypeTest.class,
    CtfIteratorTest.class,
    CtfLocationTest.class,
    CtfTmfTraceTest.class,
    CtfTmfEventTest.class,
})
public class TestAll {

    /**
     * Launch the test.
     *
     * @param args the command line arguments
     *
     * @generatedBy CodePro at 03/05/12 2:29 PM
     */
    public static void main(String[] args) {
        JUnitCore.runClasses(new Class[] { TestAll.class });
    }
}
