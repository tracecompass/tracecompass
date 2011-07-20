package org.eclipse.linuxtools.tmf.ui.tests;

import junit.framework.Test;
import junit.framework.TestSuite;

public class AllTmfUITests  {
    public static Test suite() {
        TestSuite suite = new TestSuite(AllTmfUITests.class.getName());
        //$JUnit-BEGIN$
        suite.addTest(org.eclipse.linuxtools.tmf.ui.tests.views.uml2sd.handlers.widgets.AllTests.suite());
        suite.addTest(org.eclipse.linuxtools.tmf.ui.tests.views.uml2sd.impl.AllTests.suite());
        suite.addTest(org.eclipse.linuxtools.tmf.ui.tests.views.uml2sd.load.AllTests.suite());
        //$JUnit-END$
        return suite;
    }

}
