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

package org.eclipse.linuxtools.tmf.core.tests.trace.indexer.checkpoint;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;

/**
 * Test suite for org.eclipse.linuxtools.tmf.core.trace.indexer.checkpoint
 */
@RunWith(Suite.class)
@Suite.SuiteClasses({
    TmfBTreeIndexTest.class,
    TmfCheckpointIndexTest.class,
    TmfCheckpointIndexTest2.class,
    TmfCheckpointTest.class,
    TmfExperimentCheckpointIndexTest.class,
})
public class AllTests {}