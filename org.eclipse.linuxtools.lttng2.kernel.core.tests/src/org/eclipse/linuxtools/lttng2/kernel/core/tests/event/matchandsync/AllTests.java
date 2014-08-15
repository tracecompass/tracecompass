/*******************************************************************************
 * Copyright (c) 2013 École Polytechnique de Montréal
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Geneviève Bastien - Initial implementation
 *******************************************************************************/

package org.eclipse.linuxtools.lttng2.kernel.core.tests.event.matchandsync;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;

/**
 * Test suite for org.eclipse.linuxtools.lttng2.kernel.core.tests.event.matching
 */
@RunWith(Suite.class)
@Suite.SuiteClasses({
    ExperimentSyncTest.class,
    MatchAndSyncTest.class
})
public class AllTests {

}
