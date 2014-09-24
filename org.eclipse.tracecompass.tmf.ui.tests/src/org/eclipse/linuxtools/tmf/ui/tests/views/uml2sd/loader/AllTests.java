/*******************************************************************************
 * Copyright (c) 2011-2013 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Bernd Hufmann - Initial API and implementation
 *   Alexandre Montplaisir - Port to JUnit4
 *******************************************************************************/

package org.eclipse.linuxtools.tmf.ui.tests.views.uml2sd.loader;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;

/**
 *  Test suite for testing the TmfUml2SDSyncLoader class.
 *
 * @author Bernd Hufmann
 */
@RunWith(Suite.class)
@Suite.SuiteClasses({
    TmfUml2SDSyncLoaderExpTest.class,
    TmfUml2SDSyncLoaderFilterTest.class,
    TmfUml2SDSyncLoaderFindTest.class,
    TmfUml2SDSyncLoaderPagesTest.class,
    TmfUml2SDSyncLoaderSignalTest.class,
    TmfUml2SDSyncLoaderTimeTest.class
})
public class AllTests {

}
