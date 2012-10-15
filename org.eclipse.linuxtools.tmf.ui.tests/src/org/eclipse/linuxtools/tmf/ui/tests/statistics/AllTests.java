/*******************************************************************************
 * Copyright (c) 2011 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Mathieu Denis <mathieu.denis@polymtl.ca> - Initial API and Implementation
 *   Bernd Hufmann - Fixed suite name
 *******************************************************************************/
package org.eclipse.linuxtools.tmf.ui.tests.statistics;

import junit.framework.Test;
import junit.framework.TestSuite;


/**
 * Test suite for statistic tests.
 */
public class AllTests {

    /**
     * @return the test suite
     */
    public static Test suite() {
        TestSuite suite = new TestSuite(AllTests.class.getName());
        // $JUnit-BEGIN$
        suite.addTestSuite(TmfBaseColumnDataProviderTest.class);
        suite.addTestSuite(TmfBaseColumnDataTest.class);
        suite.addTestSuite(TmfBaseStatisticsDataTest.class);
        suite.addTestSuite(TmfStatisticsTreeNodeTest.class);
        suite.addTestSuite(TmfStatisticsTreeManagerTest.class);
        suite.addTestSuite(TmfTreeContentProviderTest.class);
        suite.addTestSuite(TmfStatisticsTest.class);
        // $JUnit-END$
        return suite;
    }
}
