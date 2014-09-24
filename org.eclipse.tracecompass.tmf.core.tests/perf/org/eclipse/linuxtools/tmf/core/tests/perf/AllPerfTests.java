/*******************************************************************************
 * Copyright (c) 2014 École Polytechnique de Montréal
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Geneviève Bastien - Initial implementation and API
 *******************************************************************************/

package org.eclipse.linuxtools.tmf.core.tests.perf;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;

/**
 * The class <code>AllPerformanceTests</code> builds a suite that can be used to
 * run all of the performance tests within its package as well as within any
 * subpackages of its package.
 *
 * @author Geneviève Bastien
 */
@RunWith(Suite.class)
@Suite.SuiteClasses({
        org.eclipse.linuxtools.tmf.core.tests.perf.synchronization.AllPerfTests.class
})
public class AllPerfTests {

}
