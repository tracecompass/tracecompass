/*******************************************************************************
 * Copyright (c) 2015 École Polytechnique de Montréal
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Geneviève Bastien - Initial API and implementation
 *******************************************************************************/

package org.eclipse.tracecompass.analysis.graph.core.tests.graph;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;

/**
 * Test suite for graph package
 */
@RunWith(Suite.class)
@Suite.SuiteClasses({
    TmfGraphTest.class,
    TmfGraphBuilderModuleTest.class
})
public class AllTests {

}
