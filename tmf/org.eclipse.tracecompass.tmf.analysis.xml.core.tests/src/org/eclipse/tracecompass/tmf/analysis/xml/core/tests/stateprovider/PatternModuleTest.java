/*******************************************************************************
 * Copyright (c) 2016 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.eclipse.tracecompass.tmf.analysis.xml.core.tests.stateprovider;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.tracecompass.testtraces.ctf.CtfTestTrace;
import org.eclipse.tracecompass.tmf.analysis.xml.core.module.TmfXmlStrings;
import org.eclipse.tracecompass.tmf.analysis.xml.core.tests.common.TmfXmlTestFiles;

/**
 * Test the pattern analysis module
 *
 * @author Jean-Christian Kouame
 */
public class PatternModuleTest extends XmlModuleTestBase {

    @Override
    protected String getAnalysisId() {
        return "syscall.analysis";
    }

    @Override
    protected String getAnalysisName() {
        return "XML system call analysis";
    }

    @Override
    protected TmfXmlTestFiles getXmlFile() {
        return TmfXmlTestFiles.VALID_PATTERN_FILE;
    }

    @Override
    protected @NonNull String getAnalysisNodeName() {
        return TmfXmlStrings.PATTERN;
    }

    @Override
    protected @NonNull CtfTestTrace getTrace() {
        return CtfTestTrace.ARM_64_BIT_HEADER;
    }

}
