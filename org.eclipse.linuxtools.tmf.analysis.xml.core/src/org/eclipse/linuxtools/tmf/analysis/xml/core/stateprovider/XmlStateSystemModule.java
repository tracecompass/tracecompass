/*******************************************************************************
 * Copyright (c) 2014 École Polytechnique de Montréal
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Geneviève Bastien - Initial API and implementation
 *******************************************************************************/

package org.eclipse.linuxtools.tmf.analysis.xml.core.stateprovider;

import java.util.List;

import org.eclipse.core.runtime.IPath;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.linuxtools.tmf.analysis.xml.core.module.XmlUtils;
import org.eclipse.linuxtools.tmf.core.statesystem.ITmfStateProvider;
import org.eclipse.linuxtools.tmf.core.statesystem.TmfStateSystemAnalysisModule;
import org.w3c.dom.Element;

/**
 * Analysis module for the data-driven state systems, defined in XML.
 *
 * @author Geneviève Bastien
 * @since 3.0
 */
public class XmlStateSystemModule extends TmfStateSystemAnalysisModule {

    private @Nullable IPath fXmlFile;

    @Override
    protected StateSystemBackendType getBackendType() {
        return StateSystemBackendType.FULL;
    }

    @Override
    @NonNull
    protected ITmfStateProvider createStateProvider() {
        return new XmlStateProvider(getTrace(), getId(), fXmlFile);
    }

    @Override
    public String getName() {
        String name = getId();
        IPath xmlFile = fXmlFile;
        if (xmlFile == null) {
            return name;
        }
        Element doc = XmlUtils.getElementInFile(xmlFile.makeAbsolute().toString(), TmfXmlStrings.STATE_PROVIDER, getId());
        /* Label may be available in XML header */
        List<Element> head = XmlUtils.getChildElements(doc, TmfXmlStrings.HEAD);
        if (head.size() == 1) {
            List<Element> labels = XmlUtils.getChildElements(head.get(0), TmfXmlStrings.LABEL);
            if (!labels.isEmpty()) {
                name = labels.get(0).getAttribute(TmfXmlStrings.VALUE);
            }
        }

        return name;
    }

    /**
     * Sets the file path of the XML file containing the state provider
     *
     * @param file
     *            The full path to the XML file
     */
    public void setXmlFile(IPath file) {
        fXmlFile = file;
    }

    /**
     * Get the path to the XML file containing this state provider definition.
     *
     * @return XML file path
     */
    public IPath getXmlFile() {
        return fXmlFile;
    }

}
