/*******************************************************************************
 * Copyright (c) 2011, 2012 Ericsson
 * 
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *   Bernd Hufmann - Initial API and implementation
 *******************************************************************************/
package org.eclipse.linuxtools.tmf.ui.tests.views.uml2sd.loader;

import junit.framework.Test;
import junit.framework.TestSuite;

public class AllTests {

    public static Test suite() {
        TestSuite suite = new TestSuite(AllTests.class.getName());
        //$JUnit-BEGIN$
        suite.addTestSuite(TmfUml2SDSyncLoaderExpTest.class);
        suite.addTestSuite(TmfUml2SDSyncLoaderPagesTest.class);
        suite.addTestSuite(TmfUml2SDSyncLoaderTimeTest.class);
        suite.addTestSuite(TmfUml2SDSyncLoaderSignalTest.class);
        suite.addTestSuite(TmfUml2SDSyncLoaderFindTest.class);
        suite.addTestSuite(TmfUml2SDSyncLoaderFilterTest.class);
        //$JUnit-END$
        return new Uml2SDTestSetup(suite);
    }
}
