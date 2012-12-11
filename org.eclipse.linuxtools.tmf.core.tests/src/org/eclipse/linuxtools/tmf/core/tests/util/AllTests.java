package org.eclipse.linuxtools.tmf.core.tests.util;

import junit.framework.Test;
import junit.framework.TestSuite;

import org.eclipse.linuxtools.internal.tmf.core.Activator;

/**
 * <b><u>AllTests</u></b>
 * <p>
 * <p>
 */
@SuppressWarnings({ "nls" })
public class AllTests {

	/**
	 * @return the test suite
	 */
	public static Test suite() {
		TestSuite suite = new TestSuite("Test suite for " + Activator.PLUGIN_ID + ".util"); //$NON-NLS-1$);
		//$JUnit-BEGIN$
		suite.addTestSuite(PairTest.class);
		//$JUnit-END$
		return suite;
	}
}