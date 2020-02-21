/*******************************************************************************
 * Copyright (c) 2018 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License 2.0 which
 * accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/
package org.eclipse.tracecompass.internal.tmf.analysis.xml.core.module;

import java.util.Set;

import org.eclipse.jdt.annotation.NonNull;

/**
 * Represents an xml analyses output
 *
 * @author Jean-Christian Kouame
 */
public class XmlOutputElement {

    private String fPath;
    private String fXmlElem;
    private String fId;
    private String fLabel;
    private Set<String> fAnalyses;

    /**
     * Constructor
     *
     * @param path
     *            The absolute xml file path
     * @param xmlElem
     *            The xml output element type
     * @param id
     *            The id of the xml outout
     * @param label
     *            The xml output label
     * @param analyses
     *            The analyses the output element applies to
     */
    public XmlOutputElement(String path, @NonNull String xmlElem, String id, String label, Set<String> analyses) {
        fPath = path;
        fXmlElem = xmlElem;
        fId = id;
        fLabel = label;
        fAnalyses = analyses;
    }

    /**
     * Get the xml file absolute path
     *
     * @return The path
     */
    public String getPath() {
        return fPath;
    }

    /**
     * The xml output element
     *
     * @return xml output element
     */
    public String getXmlElem() {
        return fXmlElem;
    }

    /**
     * Get the id of this output element
     *
     * @return The output element id
     */
    public String getId() {
        return fId;
    }

    /**
     * Get the output element label
     *
     * @return The label
     */
    public String getLabel() {
        return fLabel;
    }

    /**
     * Get the analyses the output element applies to
     *
     * @return The analyses
     */
    public Set<String> getAnalyses() {
        return fAnalyses;
    }
}
