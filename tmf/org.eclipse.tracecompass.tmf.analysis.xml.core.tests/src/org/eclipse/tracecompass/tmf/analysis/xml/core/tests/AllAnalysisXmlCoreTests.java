/*******************************************************************************
 * Copyright (c) 2013, 2014 École Polytechnique de Montréal
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Geneviève Bastien - Initial API and implementation
 *******************************************************************************/

package org.eclipse.tracecompass.tmf.analysis.xml.core.tests;

import org.eclipse.tracecompass.tmf.core.tests.shared.DebugSuite;
import org.junit.runner.RunWith;

/**
 * Master test suite for TMF XML Core Analysis plug-in.
 */
@RunWith(DebugSuite.class)
@DebugSuite.SuiteClasses({
        XmlAnalysisCorePluginTest.class,
        org.eclipse.tracecompass.tmf.analysis.xml.core.tests.module.AllTests.class,
        org.eclipse.tracecompass.tmf.analysis.xml.core.tests.stateprovider.AllTests.class
})
public class AllAnalysisXmlCoreTests {

}
