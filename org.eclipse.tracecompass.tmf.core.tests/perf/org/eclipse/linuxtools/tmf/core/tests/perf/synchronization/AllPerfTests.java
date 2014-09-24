/*******************************************************************************
 * Copyright (c) 2014 École Polytechnique de Montréal
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Geneviève Bastien - Initial API and implementation
 *******************************************************************************/

package org.eclipse.linuxtools.tmf.core.tests.perf.synchronization;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;

/**
 * Performance tests for the synchronization classes
 *
 * @author Geneviève Bastien
 */
@RunWith(Suite.class)
@Suite.SuiteClasses({
        TimestampTransformBenchmark.class
})
public class AllPerfTests {

}
