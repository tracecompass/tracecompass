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

package org.eclipse.tracecompass.tmf.analysis.xml.core.module;

import java.io.File;
import java.util.Collection;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.tracecompass.tmf.core.analysis.IAnalysisModuleHelper;
import org.w3c.dom.Document;

/**
 * An interface to be implemented by classes that parse XML files to additional
 * analyses provided by schemas advertised through the extension point. Each
 * additional XSD schema should be accompanied by at least one schema parser
 * class.
 *
 * @author Geneviève Bastien
 * @since 2.2
 */
@NonNullByDefault
public interface ITmfXmlSchemaParser {

    /**
     * Parses the XML document to get any additional analysis module it may
     * contain. The helper may extend {@link TmfAnalysisModuleHelperXml} or can
     * implement their own helper.
     *
     * @param xmlFile
     *            The XML file from which the document was read
     * @param doc
     *            The XML document to get the modules from
     * @return The list of modules parsed by this parser
     */
    Collection<? extends IAnalysisModuleHelper> getModuleHelpers(File xmlFile, Document doc);

}
