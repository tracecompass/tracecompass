/*******************************************************************************
 * Copyright (c) 2012, 2013 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Alexandre Montplaisir - Initial API and implementation
 *   Alexandre Montplaisir - Port to JUnit4
 ******************************************************************************/

package org.eclipse.linuxtools.tmf.core.tests.statesystem;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;

/**
 * Test suite for org.eclipse.linuxtools.tmf.core.statesystem
 */
@RunWith(Suite.class)
@Suite.SuiteClasses({
    StateSystemPushPopTest.class,
    StateSystemAnalysisModuleTest.class
})
public class AllTests {

}