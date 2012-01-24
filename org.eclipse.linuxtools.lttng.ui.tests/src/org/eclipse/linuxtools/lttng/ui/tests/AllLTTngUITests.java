package org.eclipse.linuxtools.lttng.ui.tests;

import junit.framework.Test;
import junit.framework.TestSuite;

public class AllLTTngUITests  {
    public static Test suite() {
        TestSuite suite = new TestSuite(AllLTTngUITests.class.getName());
        //$JUnit-BEGIN$
        suite.addTest(org.eclipse.linuxtools.lttng.ui.tests.distribution.AllTests.suite());
        suite.addTest(org.eclipse.linuxtools.lttng.ui.tests.histogram.AllTests.suite());
        //$JUnit-END$
        return suite;
    }
}
