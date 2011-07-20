package org.eclipse.linuxtools.tmf.tests.uml2sd;

import junit.framework.Test;
import junit.framework.TestSuite;

import org.eclipse.linuxtools.tmf.TmfCorePlugin;

@SuppressWarnings("nls")
public class AllTests {

	public static Test suite() {
		TestSuite suite = new TestSuite("Test suite for " + TmfCorePlugin.PLUGIN_ID + ".uml2sd"); //$NON-NLS-1$);
		//$JUnit-BEGIN$
		suite.addTestSuite(TmfSyncSequenceDiagramEventTest.class);
		suite.addTestSuite(TmfAsyncSequenceDiagramEventTest.class);
		//$JUnit-END$
		return suite;
	}

}