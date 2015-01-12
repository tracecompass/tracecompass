/*******************************************************************************
 * Copyright (c) 2011-2014 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Bernd Hufmann - Initial API and implementation
 *   Bernd Hufmann - Add UML2SD tests
 *   Mathieu Denis <mathieu.denis@polymtl.ca> - Add Statistics test
 *   Alexandre Montplaisir - Port to JUnit4
 *******************************************************************************/

package org.eclipse.tracecompass.tmf.ui.tests;

import org.eclipse.tracecompass.tmf.core.tests.shared.DebugSuite;
import org.junit.runner.RunWith;

/**
 * Master test suite for TMF UI Core.
 */
@RunWith(DebugSuite.class)
@DebugSuite.SuiteClasses({
        org.eclipse.tracecompass.tmf.ui.tests.histogram.AllTests.class,
        org.eclipse.tracecompass.tmf.ui.tests.project.model.AllTests.class,
        org.eclipse.tracecompass.tmf.ui.tests.statistics.AllTests.class,
        org.eclipse.tracecompass.tmf.ui.tests.views.uml2sd.dialogs.AllTests.class,
        org.eclipse.tracecompass.tmf.ui.tests.views.uml2sd.load.AllTests.class,
        org.eclipse.tracecompass.tmf.ui.tests.views.uml2sd.loader.AllTests.class
})
public class AllTmfUITests {

}
