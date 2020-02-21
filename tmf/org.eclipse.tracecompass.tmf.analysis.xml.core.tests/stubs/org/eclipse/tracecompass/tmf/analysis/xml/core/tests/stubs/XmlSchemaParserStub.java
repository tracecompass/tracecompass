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
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.tracecompass.tmf.analysis.xml.core.module.ITmfXmlSchemaParser;
import org.eclipse.tracecompass.tmf.core.analysis.IAnalysisModuleHelper;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

/**
 * Schema parser stub for the extension.xsd extra schema
 *
 * @author Geneviève Bastien
 */
public class XmlSchemaParserStub implements ITmfXmlSchemaParser {

    private static final @NonNull String MY_MODULE = "my";
    private static final @NonNull String ABC_MODULE = "abc";

    @Override
    public @NonNull Collection<? extends @NonNull IAnalysisModuleHelper> getModuleHelpers(File xmlFile, Document doc) {
        List<@NonNull IAnalysisModuleHelper> modules = new ArrayList<>();
        /* get the "my" modules */
        NodeList xmlNodes = doc.getElementsByTagName(MY_MODULE);
        for (int i = 0; i < xmlNodes.getLength(); i++) {
            Element node = (Element) xmlNodes.item(i);

            IAnalysisModuleHelper helper = new OtherModuleHelperStub(xmlFile, node);
            modules.add(helper);
        }

        /* get the "abc" modules */
        xmlNodes = doc.getElementsByTagName(ABC_MODULE);
        for (int i = 0; i < xmlNodes.getLength(); i++) {
            Element node = (Element) xmlNodes.item(i);

            IAnalysisModuleHelper helper = new OtherModuleHelperStub(xmlFile, node);
            modules.add(helper);
        }

        return modules;
    }

}
