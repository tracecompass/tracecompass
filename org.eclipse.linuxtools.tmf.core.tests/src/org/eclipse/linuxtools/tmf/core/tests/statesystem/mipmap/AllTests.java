/*******************************************************************************
 * Copyright (c) 2013 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Patrick Tasse - Updates to mipmap feature
 *******************************************************************************/

package org.eclipse.linuxtools.tmf.core.tests.statesystem.mipmap;

import org.eclipse.linuxtools.tmf.core.tests.statesystem.mipmap.TmfMipmapStateProviderTest;
import org.eclipse.linuxtools.tmf.core.tests.statesystem.mipmap.TmfMipmapStateProviderWeightedTest;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;

/**
 * Test suite for org.eclipse.linuxtools.tmf.core.tests.statesystem.mipmap
 */
@RunWith(Suite.class)
@Suite.SuiteClasses({
    TmfMipmapStateProviderTest.class,
    TmfMipmapStateProviderWeightedTest.class,
})
public class AllTests {

}
