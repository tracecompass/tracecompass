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

import java.io.File;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.tracecompass.tmf.analysis.xml.core.module.TmfAnalysisModuleHelperXml;
import org.eclipse.tracecompass.tmf.core.analysis.IAnalysisModule;
import org.w3c.dom.Element;

/**
 * A stub XML module helper
 *
 * @author Geneviève Bastien
 */
@NonNullByDefault
public class OtherModuleHelperStub extends TmfAnalysisModuleHelperXml {

    /**
     * Constructor
     *
     * @param xmlFile
     *            The XML file containing the extra module
     * @param node
     *            The corresponding XML element
     */
    public OtherModuleHelperStub(File xmlFile, Element node) {
        super(xmlFile, node, XmlAnalysisModuleType.OTHER);
    }

    @Override
    protected IAnalysisModule createOtherModule(String analysisid, String name) {
        Element sourceElement = getSourceElement();
        ExternalAnalysisModuleStub module = null;
        switch (sourceElement.getTagName()) {
        case "my":
            module = new ExternalAnalysisModuleStub(getSourceFile(), "my");
            break;
        case "abc":
            module = new ExternalAnalysisModuleStub(getSourceFile(), "abc");
            break;
        default:
            break;
        }
        if (module == null) {
            throw new IllegalArgumentException("This element should not have parsed");
        }
        module.setId(analysisid);
        module.setName(name);
        return module;
    }

}
