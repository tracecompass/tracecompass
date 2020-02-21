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

package org.eclipse.tracecompass.internal.tmf.analysis.xml.core.fsm.compile;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.tracecompass.internal.tmf.analysis.xml.core.Activator;
import org.eclipse.tracecompass.tmf.analysis.xml.core.module.TmfXmlStrings;
import org.eclipse.tracecompass.tmf.analysis.xml.core.module.TmfXmlUtils;
import org.w3c.dom.Element;

/**
 * An XML location compilation unit. The location is a shortcut to a path in the
 * state system, so it will be replaced at compiled time by the values it points
 * to. This class is thus not meant to be generated.
 *
 * @author Geneviève Bastien
 * @author Florian Wininger
 */
public class TmfXmlLocationCu {

    private final List<TmfXmlStateValueCu> fValues;

    /**
     * Constructor
     *
     * Package-private because only classes from this package can build this
     *
     * @param values
     *            The values this location points to
     */
    TmfXmlLocationCu(List<TmfXmlStateValueCu> values) {
        fValues = values;
    }

    /**
     * Get the list of values this location points to
     *
     * @return The values this location points to. The array is non-empty.
     */
    public List<TmfXmlStateValueCu> getValues() {
        return fValues;
    }

    /**
     * Compile a location from an XML element
     *
     * @param data
     *            The analysis data compiled so far
     * @param locationEl
     *            The XML element for this location
     * @return The location compilation unit or <code>null</code> if the location
     *         did not compile correctly
     */
    public static @Nullable TmfXmlLocationCu compile(AnalysisCompilationData data, Element locationEl) {
        String id = locationEl.getAttribute(TmfXmlStrings.ID);

        List<Element> childElements = TmfXmlUtils.getChildElements(locationEl, TmfXmlStrings.STATE_ATTRIBUTE);
        List<TmfXmlStateValueCu> children = new ArrayList<>();
        for (Element attribute : childElements) {
            List<TmfXmlStateValueCu> compiled = TmfXmlStateValueCu.compileAttribute(data, attribute);
            children.addAll(compiled);
        }
        if (children.isEmpty()) {
            // TODO: Validation message here
            Activator.logError("Location " + id + " is empty."); //$NON-NLS-1$ //$NON-NLS-2$
            return null;
        }
        TmfXmlLocationCu location = new TmfXmlLocationCu(children);
        data.addLocation(id, location);
        return location;
    }

}
