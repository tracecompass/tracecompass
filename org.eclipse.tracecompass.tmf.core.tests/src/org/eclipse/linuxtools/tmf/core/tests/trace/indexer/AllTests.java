/*******************************************************************************
 * Copyright (c) 2013 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Marc-Andre Laperle - Initial API and implementation
 *******************************************************************************/

package org.eclipse.linuxtools.tmf.core.tests.trace.indexer;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;

/**
 * Test suite indexer classes
 *
 * @author Marc-Andre Laperle
 */
@RunWith(Suite.class)
@Suite.SuiteClasses({
    BTreeTest.class,
    FlatArrayTest.class,
    TmfMemoryIndexTest.class
})
public class AllTests {

}