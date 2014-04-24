/*******************************************************************************
 * Copyright (c) 2011, 2013 Ericsson
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

package org.eclipse.linuxtools.lttng2.control.ui.tests.model.component;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;

/**
 * Runner for the model.component unit tests.
 */
@RunWith(Suite.class)
@Suite.SuiteClasses({
    TraceControlComponentTest.class,
    TraceControlCreateSessionTests.class,
    TraceControlKernelProviderTests.class,
    TraceControlKernelSessionTests.class,
    TraceControlPropertiesTest.class,
    TraceControlTreeModelNoProvidersTest.class,
    TraceControlTreeModelTest.class,
    TraceControlUstProviderTests.class,
    TraceControlUstSessionTests.class,
    TraceControlUstSessionTests2.class,
    TraceControlSnapshotSessionTests.class
})
public class AllTests {

}
