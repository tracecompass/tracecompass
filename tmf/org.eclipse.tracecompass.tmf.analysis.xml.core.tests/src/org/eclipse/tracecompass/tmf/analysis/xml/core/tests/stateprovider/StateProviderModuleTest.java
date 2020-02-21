/*******************************************************************************
 * Copyright (c) 2013, 2016 École Polytechnique de Montréal
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License 2.0 which
 * accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Geneviève Bastien - Initial API and implementation
 *******************************************************************************/

package org.eclipse.tracecompass.tmf.analysis.xml.core.tests.stateprovider;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.tracecompass.testtraces.ctf.CtfTestTrace;
import org.eclipse.tracecompass.tmf.analysis.xml.core.module.TmfXmlStrings;
import org.eclipse.tracecompass.tmf.analysis.xml.core.tests.common.TmfXmlTestFiles;

/**
 * Test suite for the XmlStateSystemModule Test. It tests the reading of the
 * file, the header and the module's proper functioning as a module, but not the
 * state system building, which is covered by another test suite.
 *
 * @author Geneviève Bastien
 */
public class StateProviderModuleTest extends XmlModuleTestBase {

    @Override
    protected String getAnalysisId() {
        return "kernel.linux.sp";
    }

    @Override
    protected String getAnalysisName() {
        return "Xml kernel State System";
    }

    @Override
    protected TmfXmlTestFiles getXmlFile() {
        return TmfXmlTestFiles.VALID_FILE;
    }

    @Override
    protected @NonNull String getAnalysisNodeName() {
        return TmfXmlStrings.STATE_PROVIDER;
    }

    @Override
    protected @NonNull CtfTestTrace getTrace() {
        return CtfTestTrace.KERNEL;
    }

}
