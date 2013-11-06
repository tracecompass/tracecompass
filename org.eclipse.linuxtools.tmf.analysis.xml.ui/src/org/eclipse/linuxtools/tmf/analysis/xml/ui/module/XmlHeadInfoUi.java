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

import java.util.List;

import org.eclipse.linuxtools.tmf.analysis.xml.core.module.XmlHeadInfo;
import org.eclipse.linuxtools.tmf.analysis.xml.core.stateprovider.TmfXmlStrings;
import org.eclipse.linuxtools.tmf.core.project.model.TmfTraceType;
import org.eclipse.linuxtools.tmf.core.project.model.TraceTypeHelper;
import org.eclipse.linuxtools.tmf.core.trace.ITmfTrace;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

/**
 * Class to store and interpret the header information of XML-defined components
 *
 * TODO: When trace types are moved to the o.e.l.tmf.core plug-in, there will be
 * no need for this class anymore, move to o.e.l.tmf.analysis.xml.core plug-in.
 *
 * @author Geneviève Bastien
 */
public class XmlHeadInfoUi extends XmlHeadInfo {

    /**
     * Constructor
     *
     * @param item
     *            The XML node corresponding to this header
     */
    public XmlHeadInfoUi(Node item) {
        super(item);
    }

    @Override
    public boolean checkTraceType(Class<? extends ITmfTrace> traceClass) {
        /*
         * TODO: This wouldn't work for custom traces since {@link
         * TmfTraceType#getTraceType(String)} has no helper for those traces
         */
        List<Element> elements = getElements(TmfXmlStrings.TRACETYPE);
        if (elements.isEmpty()) {
            return true;
        }

        for (Element element : elements) {
            String traceTypeId = element.getAttribute(TmfXmlStrings.ID);
            TraceTypeHelper helper = TmfTraceType.getInstance().getTraceType(traceTypeId);
            if ((helper != null) && helper.getTrace().getClass().isAssignableFrom(traceClass)) {
                return true;
            }
        }
        return false;
    }

}
