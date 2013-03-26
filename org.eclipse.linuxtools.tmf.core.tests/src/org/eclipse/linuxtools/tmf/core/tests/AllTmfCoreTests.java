/*******************************************************************************
 * Copyright (c) 2009, 2013 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Francois Chouinard - Initial API and implementation
 *   Alexandre Montplaisir - Port to JUnit4, enable CTF and statistics tests
 *******************************************************************************/

package org.eclipse.linuxtools.tmf.core.tests;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;

/**
 * Master test suite for TMF Core.
 */
@RunWith(Suite.class)
@Suite.SuiteClasses({
    TmfCorePluginTest.class,
    org.eclipse.linuxtools.tmf.core.tests.component.AllTests.class,
    org.eclipse.linuxtools.tmf.core.tests.ctfadaptor.AllTests.class,
    org.eclipse.linuxtools.tmf.core.tests.event.AllTests.class,
    org.eclipse.linuxtools.tmf.core.tests.request.AllTests.class,
    org.eclipse.linuxtools.tmf.core.tests.signal.AllTests.class,
    org.eclipse.linuxtools.tmf.core.tests.statesystem.AllTests.class,
    org.eclipse.linuxtools.tmf.core.tests.statistics.AllTests.class,
    org.eclipse.linuxtools.tmf.core.tests.trace.AllTests.class,
    org.eclipse.linuxtools.tmf.core.tests.uml2sd.AllTests.class,
    org.eclipse.linuxtools.tmf.core.tests.util.AllTests.class
})
public class AllTmfCoreTests {

}
