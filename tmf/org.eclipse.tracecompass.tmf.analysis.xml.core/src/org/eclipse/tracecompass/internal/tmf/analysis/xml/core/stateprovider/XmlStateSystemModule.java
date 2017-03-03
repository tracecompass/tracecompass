/*******************************************************************************
 * Copyright (c) 2014, 2015 École Polytechnique de Montréal
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Geneviève Bastien - Initial API and implementation
 *******************************************************************************/

package org.eclipse.tracecompass.internal.tmf.analysis.xml.core.stateprovider;

import static org.eclipse.tracecompass.common.core.NonNullUtils.checkNotNull;

import java.nio.file.Path;
import java.util.List;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.tracecompass.internal.tmf.analysis.xml.core.module.XmlUtils;
import org.eclipse.tracecompass.tmf.analysis.xml.core.module.TmfXmlStrings;
import org.eclipse.tracecompass.tmf.analysis.xml.core.module.TmfXmlUtils;
import org.eclipse.tracecompass.tmf.core.statesystem.ITmfStateProvider;
import org.eclipse.tracecompass.tmf.core.statesystem.TmfStateSystemAnalysisModule;
import org.w3c.dom.Element;

/**
 * Analysis module for the data-driven state systems, defined in XML.
 *
 * @author Geneviève Bastien
 */
public class XmlStateSystemModule extends TmfStateSystemAnalysisModule {

    private @Nullable Path fXmlFile;

    @Override
    protected StateSystemBackendType getBackendType() {
        return StateSystemBackendType.FULL;
    }

    @Override
    @NonNull
    protected ITmfStateProvider createStateProvider() {
        return new XmlStateProvider(checkNotNull(getTrace()), getId(), fXmlFile);
    }

    @Override
    public String getName() {
        String id = getId();
        Path xmlFile = fXmlFile;
        if (xmlFile == null) {
            return id;
        }
        Element doc = XmlUtils.getElementInFile(xmlFile.toAbsolutePath().toString(), TmfXmlStrings.STATE_PROVIDER, id);
        /* Label may be available in XML header */
        List<Element> head = TmfXmlUtils.getChildElements(doc, TmfXmlStrings.HEAD);
        String name = null;
        if (head.size() == 1) {
            List<Element> labels = TmfXmlUtils.getChildElements(head.get(0), TmfXmlStrings.LABEL);
            if (!labels.isEmpty()) {
                name = labels.get(0).getAttribute(TmfXmlStrings.VALUE);
            }
        }
        return (name == null) ? id : name;
    }

    /**
     * Sets the file path of the XML file containing the state provider
     *
     * @param file
     *            The absolute path to the XML file
     */
    public void setXmlFile(Path file) {
        fXmlFile = file;
    }

    /**
     * Get the path to the XML file containing this state provider definition.
     *
     * @return XML file path
     */
    public Path getXmlFile() {
        return fXmlFile;
    }

}
