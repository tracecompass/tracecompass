/*******************************************************************************
 * Copyright (c) 2013 École Polytechnique de Montréal
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Geneviève Bastien - Initial API and implementation
 *******************************************************************************/

package org.eclipse.linuxtools.tmf.core.tests.analysis;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;

/**
 * Unit tests for the analysis package.
 */
@RunWith(Suite.class)
@Suite.SuiteClasses({
        AnalysisModuleTest.class,
        AnalysisModuleHelperTest.class,
        AnalysisManagerTest.class,
        AnalysisParameterProviderTest.class,
        AnalysisRequirementTest.class
})
public class AllTests {

}
