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

package org.eclipse.linuxtools.tmf.analysis.xml.ui.module;

import java.io.File;

import org.eclipse.core.runtime.Path;
import org.eclipse.linuxtools.internal.tmf.analysis.xml.ui.Activator;
import org.eclipse.linuxtools.tmf.analysis.xml.core.stateprovider.TmfXmlStrings;
import org.eclipse.linuxtools.tmf.analysis.xml.core.stateprovider.XmlStateSystemModule;
import org.eclipse.linuxtools.tmf.core.analysis.IAnalysisModule;
import org.eclipse.linuxtools.tmf.core.analysis.IAnalysisModuleHelper;
import org.eclipse.linuxtools.tmf.core.exceptions.TmfAnalysisException;
import org.eclipse.linuxtools.tmf.core.trace.ITmfTrace;
import org.eclipse.linuxtools.tmf.ui.analysis.TmfAnalysisViewOutput;
import org.osgi.framework.Bundle;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

/**
 * Analysis module helpers for modules provided by XML files
 *
 * @author Geneviève Bastien
 */
public class TmfAnalysisModuleHelperXml implements IAnalysisModuleHelper {

    /**
     * The types of analysis that can be XML-defined
     */
    public enum XmlAnalysisModuleType {
        /** Analysis will be of type XmlStateSystemModule */
        STATE_SYSTEM
    }

    private final File fSourceFile;
    private final Element fSourceElement;
    private final XmlAnalysisModuleType fType;
    private final XmlHeadInfoUi fHeadInfo;

    /**
     * Constructor
     *
     * @param xmlFile
     *            The XML file containing the details of this analysis
     * @param node
     *            The XML node element
     * @param type
     *            The type of analysis
     */
    public TmfAnalysisModuleHelperXml(File xmlFile, Element node, XmlAnalysisModuleType type) {
        fSourceFile = xmlFile;
        fSourceElement = node;
        fType = type;

        NodeList head = fSourceElement.getElementsByTagName(TmfXmlStrings.HEAD);
        if (head.getLength() == 1) {
            fHeadInfo = new XmlHeadInfoUi(head.item(0));
        } else {
            fHeadInfo = null;
        }
    }

    @Override
    public String getId() {
        return fSourceElement.getAttribute(TmfXmlStrings.ANALYSIS_ID);
    }

    @Override
    public String getName() {
        String name = fHeadInfo.getName();
        if (name == null) {
            name = getId();
        }
        return name;
    }

    @Override
    public boolean isAutomatic() {
        return false;
    }

    @Override
    public String getHelpText() {
        return new String();
    }

    @Override
    public String getIcon() {
        return null;
    }

    @Override
    public Bundle getBundle() {
        return Activator.getDefault().getBundle();
    }

    @Override
    public boolean appliesToTraceType(Class<? extends ITmfTrace> traceclass) {
        /* Trace types may be available in XML header */
        if (fHeadInfo == null) {
            return true;
        }

        return fHeadInfo.checkTraceType(traceclass);
    }

    @Override
    public IAnalysisModule newModule(ITmfTrace trace) throws TmfAnalysisException {
        String analysisid = getId();
        IAnalysisModule module = null;
        switch (fType) {
        case STATE_SYSTEM:
            module = new XmlStateSystemModule();
            XmlStateSystemModule ssModule = (XmlStateSystemModule) module;
            module.setId(analysisid);
            ssModule.setXmlFile(new Path(fSourceFile.getAbsolutePath()));

            /* Set header information if available */
            ssModule.setHeadInfo(fHeadInfo);

            module.registerOutput(new TmfAnalysisViewOutput("org.eclipse.linuxtools.tmf.ui.views.ssvisualizer")); //$NON-NLS-1$
            break;
        default:
            break;

        }
        if (module != null) {
            module.setTrace(trace);
        }

        return module;
    }

}
