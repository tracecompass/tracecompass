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

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.linuxtools.internal.tmf.analysis.xml.ui.TmfXmlUiStrings;
import org.eclipse.linuxtools.tmf.analysis.xml.core.module.XmlUtils;
import org.eclipse.linuxtools.tmf.analysis.xml.core.stateprovider.TmfXmlStrings;
import org.eclipse.linuxtools.tmf.ui.analysis.TmfAnalysisViewOutput;
import org.w3c.dom.Element;

/**
 * Class overriding the default analysis view output for XML views. These views
 * may have labels defined in the XML element and those label will be used as
 * the name of the view
 *
 * @author Geneviève Bastien
 *
 *         TODO: We shouldn't have to do a new class here, we should be able to
 *         set the name in the parent instead
 */
public class TmfXmlViewOutput extends TmfAnalysisViewOutput {

    private String fLabel = null;

    /**
     * Constructor
     *
     * @param viewid
     *            id of the view to display as output
     */
    public TmfXmlViewOutput(String viewid) {
        super(viewid);
    }

    @Override
    public String getName() {
        if (fLabel == null) {
            return super.getName();
        }
        return fLabel;
    }

    @Override
    public void setOutputProperty(@NonNull String key, String value, boolean immediate) {
        super.setOutputProperty(key, value, immediate);
        /* Find the label of the view */
        if (key.equals(TmfXmlUiStrings.XML_OUTPUT_DATA)) {
            String[] idFile = value.split(TmfXmlAnalysisOutputSource.DATA_SEPARATOR);
            String viewId = (idFile.length > 0) ? idFile[0] : null;
            String filePath = (idFile.length > 1) ? idFile[1] : null;
            if ((viewId == null) || (filePath == null)) {
                return;
            }
            Element viewElement = XmlUtils.getElementInFile(filePath, TmfXmlUiStrings.STATE_PROVIDER_VIEW, viewId);
            if (viewElement == null) {
                return;
            }
            List<Element> heads = XmlUtils.getChildElements(viewElement, TmfXmlStrings.HEAD);
            if (heads.size() != 1) {
                return;
            }
            Element headElement = heads.get(0);
            List<Element> label = XmlUtils.getChildElements(headElement, TmfXmlStrings.LABEL);
            if (label.isEmpty()) {
                return;
            }
            Element labelElement = label.get(0);
            fLabel = labelElement.getAttribute(TmfXmlStrings.VALUE);
        }
    }
}
