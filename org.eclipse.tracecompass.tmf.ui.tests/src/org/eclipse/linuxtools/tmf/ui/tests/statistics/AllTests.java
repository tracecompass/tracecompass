/*******************************************************************************
 * Copyright (c) 2011, 2013 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Mathieu Denis <mathieu.denis@polymtl.ca> - Initial API and Implementation
 *   Bernd Hufmann - Fixed suite name
 *   Alexandre Montplaisir - Port to JUnit4
 *******************************************************************************/

package org.eclipse.linuxtools.tmf.ui.tests.statistics;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;

/**
 * Test suite for statistic tests.
 */
@RunWith(Suite.class)
@Suite.SuiteClasses({
    TmfBaseColumnDataProviderTest.class,
    TmfBaseColumnDataTest.class,
    TmfBaseStatisticsDataTest.class,
    TmfStatisticsTest.class,
    TmfStatisticsTreeNodeTest.class,
    TmfStatisticsTreeManagerTest.class,
    TmfTreeContentProviderTest.class
})
public class AllTests {

}
