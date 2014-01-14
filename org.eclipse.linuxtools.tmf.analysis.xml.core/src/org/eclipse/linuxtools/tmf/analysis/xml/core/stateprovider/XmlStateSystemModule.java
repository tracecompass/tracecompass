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

import org.eclipse.core.runtime.IPath;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.linuxtools.tmf.analysis.xml.core.module.IXmlModuleMetadata;
import org.eclipse.linuxtools.tmf.analysis.xml.core.module.XmlHeadInfo;
import org.eclipse.linuxtools.tmf.core.statesystem.ITmfStateProvider;
import org.eclipse.linuxtools.tmf.core.statesystem.TmfStateSystemAnalysisModule;

/**
 * Analysis module for the data-driven state systems, defined in XML.
 *
 * @author Geneviève Bastien
 * @since 3.0
 */
public class XmlStateSystemModule extends TmfStateSystemAnalysisModule
        implements IXmlModuleMetadata {

    private IPath fXmlFile;
    private XmlHeadInfo fHeadInfo = null;

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
        String name = fHeadInfo.getName();
        if (name == null) {
            name = getId();
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

    @Override
    public void setHeadInfo(XmlHeadInfo headInfo) {
        fHeadInfo = headInfo;
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
