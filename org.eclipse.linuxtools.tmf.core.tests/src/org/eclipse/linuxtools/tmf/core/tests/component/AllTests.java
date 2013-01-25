/*******************************************************************************
 * Copyright (c) 2013 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Alexandre Montplaisir - Initial API and implementation
 *******************************************************************************/

package org.eclipse.linuxtools.tmf.core.tests.component;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;

/**
 * Unit tests for the component package.
 */
@RunWith(Suite.class)
@Suite.SuiteClasses({
    TmfEventProviderTest.class,
    TmfProviderManagerTest.class
})
public class AllTests {

}
