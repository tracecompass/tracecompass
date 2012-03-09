/*******************************************************************************
 * Copyright (c) 2012 Ericsson
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

import junit.framework.Test;
import junit.framework.TestSuite;

/**
 * <b><u>AllLTTngCoreTests</u></b>
 * <p>
 * LTTng Core test suite.
 * <p>
 */
public class AllLTTngCoreTests {

    public static Test suite() {
        TestSuite suite = new TestSuite(AllLTTngCoreTests.class.getName());
        // $JUnit-BEGIN$
        // $JUnit-END$
        return suite;
    }

}
