/*******************************************************************************
 * Copyright (c) 2017 École Polytechnique de Montréal
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License 2.0 which
 * accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/

package org.eclipse.tracecompass.tmf.analysis.xml.core.tests.stubs;

import java.nio.file.Path;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.tracecompass.tmf.core.analysis.TmfAbstractAnalysisModule;
import org.eclipse.tracecompass.tmf.core.exceptions.TmfAnalysisException;

/**
 * A stub analysis module provided externally by an XML analysis using an
 * extension XSD file
 *
 * @author Geneviève Bastien
 */
public class ExternalAnalysisModuleStub extends TmfAbstractAnalysisModule {

    private final Path fXmlFile;
    private final String fType;

    /**
     * Constructor
     *
     * @param file
     *            The XML file where this module is defined
     * @param type
     *            The type of the module
     */
    public ExternalAnalysisModuleStub(Path file, String type) {
        fXmlFile = file;
        fType = type;
    }

    @Override
    protected boolean executeAnalysis(@NonNull IProgressMonitor monitor) throws TmfAnalysisException {
        System.out.println("Executing analysis " + getId() + " of type " + fType + " from XML file " + fXmlFile);
        return true;
    }

    @Override
    protected void canceling() {

    }

}
