/*******************************************************************************
 * Copyright (c) 2011 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Bernd Hufmann - Initial API and implementation
 *******************************************************************************/
package org.eclipse.linuxtools.lttng2.ui.tests.control.model.component;

import junit.framework.Test;
import junit.framework.TestSuite;

@SuppressWarnings("javadoc")
public class AllTests {

    public static Test suite() {

        TestSuite suite = new TestSuite(AllTests.class.getName());
        //$JUnit-BEGIN$
        suite.addTestSuite(TraceControlComponentTest.class);
        suite.addTestSuite(TraceControlTreeModelTest.class);
        suite.addTestSuite(TraceControlKernelProviderTests.class);
        suite.addTestSuite(TraceControlUstProviderTests.class);
        suite.addTestSuite(TraceControlKernelSessionTests.class);
        suite.addTestSuite(TraceControlUstSessionTests.class);
        suite.addTestSuite(TraceControlPropertiesTest.class);
        //$JUnit-END$
        return new ModelImplTestSetup(suite);
    }
}
