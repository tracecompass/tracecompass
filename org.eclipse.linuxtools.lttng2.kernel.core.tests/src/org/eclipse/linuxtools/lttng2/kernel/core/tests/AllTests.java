/*******************************************************************************
 * Copyright (c) 2012, 2013 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Francois Chouinard - Initial API and implementation
 *******************************************************************************/

package org.eclipse.linuxtools.lttng2.kernel.core.tests;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;

/**
 * Runner for the lttng2.kernel unit tests.
 */
@RunWith(Suite.class)
@Suite.SuiteClasses({
    ActivatorTest.class,
    org.eclipse.linuxtools.lttng2.kernel.core.tests.stateprovider.TestAll.class,
    org.eclipse.linuxtools.lttng2.kernel.core.tests.event.matchandsync.AllTests.class
})
public class AllTests { }
