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
import java.util.Collections;
import java.util.List;

import org.eclipse.core.runtime.Path;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.linuxtools.internal.tmf.analysis.xml.ui.Activator;
import org.eclipse.linuxtools.tmf.analysis.xml.core.module.XmlUtils;
import org.eclipse.linuxtools.tmf.analysis.xml.core.stateprovider.TmfXmlStrings;
import org.eclipse.linuxtools.tmf.analysis.xml.core.stateprovider.XmlStateSystemModule;
import org.eclipse.linuxtools.tmf.core.analysis.IAnalysisModule;
import org.eclipse.linuxtools.tmf.core.analysis.IAnalysisModuleHelper;
import org.eclipse.linuxtools.tmf.core.analysis.TmfAnalysisManager;
import org.eclipse.linuxtools.tmf.core.analysis.TmfAnalysisRequirement;
import org.eclipse.linuxtools.tmf.core.exceptions.TmfAnalysisException;
import org.eclipse.linuxtools.tmf.core.project.model.TmfTraceType;
import org.eclipse.linuxtools.tmf.core.project.model.TraceTypeHelper;
import org.eclipse.linuxtools.tmf.core.trace.ITmfTrace;
import org.osgi.framework.Bundle;
import org.w3c.dom.Element;

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
    }

    @Override
    public String getId() {
        return fSourceElement.getAttribute(TmfXmlStrings.ID);
    }

    @Override
    public String getName() {
        String name = null;
        /* Label may be available in XML header */
        List<Element> head = XmlUtils.getChildElements(fSourceElement, TmfXmlStrings.HEAD);
        if (head.size() == 1) {
            List<Element> labels = XmlUtils.getChildElements(head.get(0), TmfXmlStrings.LABEL);
            if (!labels.isEmpty()) {
                name = labels.get(0).getAttribute(TmfXmlStrings.VALUE);
            }
        }

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
    public String getHelpText(@NonNull ITmfTrace trace) {
        return ""; //$NON-NLS-1$
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
    public boolean appliesToTraceType(Class<? extends ITmfTrace> traceClass) {
        /* Trace types may be available in XML header */
        List<Element> head = XmlUtils.getChildElements(fSourceElement, TmfXmlStrings.HEAD);
        if (head.size() != 1) {
            return true;
        }
        /*
         * TODO: Test with custom trace types
         */
        List<Element> elements = XmlUtils.getChildElements(head.get(0), TmfXmlStrings.TRACETYPE);
        if (elements.isEmpty()) {
            return true;
        }

        for (Element element : elements) {
            String traceTypeId = element.getAttribute(TmfXmlStrings.ID);
            TraceTypeHelper helper = TmfTraceType.getTraceType(traceTypeId);
            if ((helper != null) && helper.getTrace().getClass().isAssignableFrom(traceClass)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public Iterable<Class<? extends ITmfTrace>> getValidTraceTypes() {
        return Collections.EMPTY_SET;
    }

    @Override
    public Iterable<TmfAnalysisRequirement> getAnalysisRequirements() {
        return Collections.EMPTY_SET;
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

            /*
             * FIXME: There is no way to know if a module is automatic, so we
             * default to true
             */
            ssModule.setAutomatic(true);

            break;
        default:
            break;

        }
        if (module != null) {
            module.setTrace(trace);
            TmfAnalysisManager.analysisModuleCreated(module);
        }

        return module;
    }

}
