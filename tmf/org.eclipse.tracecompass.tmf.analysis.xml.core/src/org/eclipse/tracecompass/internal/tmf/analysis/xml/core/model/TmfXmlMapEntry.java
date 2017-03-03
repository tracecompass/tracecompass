/*******************************************************************************
 * Copyright (c) 2016 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.eclipse.tracecompass.internal.tmf.analysis.xml.core.model;

import java.util.Collections;

import org.eclipse.tracecompass.common.core.NonNullUtils;
import org.eclipse.tracecompass.internal.tmf.analysis.xml.core.module.IXmlStateSystemContainer;
import org.eclipse.tracecompass.tmf.analysis.xml.core.module.TmfXmlStrings;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

/**
 * This Class implements a map entry tree for mapping group in the XML-defined state system. A
 * map entry is composed of two state values. The first value is a key to get the
 * second state value.
 *
 * @author Jean-Christian Kouame
 */
public class TmfXmlMapEntry {
    ITmfXmlStateValue fKey;
    ITmfXmlStateValue fValue;

    /**
     * Constructor
     *
     * @param modelFactory
     *            The model factory
     * @param node
     *            The XML element representing this {@link TmfXmlMapEntry}
     * @param container
     *            The state system container this {@link TmfXmlMapEntry}
     *            belongs to
     */
    public TmfXmlMapEntry(ITmfXmlModelFactory modelFactory, Element node, IXmlStateSystemContainer container) {
        NodeList nodesSV = node.getElementsByTagName(TmfXmlStrings.STATE_VALUE);
        if (nodesSV.getLength() != 2) {
            throw new IllegalStateException("A map entry is composed of exactly 2 statevalues. Actual value is : " + nodesSV.getLength()); //$NON-NLS-1$
        }
        fKey = modelFactory.createStateValue((Element) NonNullUtils.checkNotNull(nodesSV.item(0)), container, Collections.EMPTY_LIST);
        fValue = modelFactory.createStateValue((Element) NonNullUtils.checkNotNull(nodesSV.item(1)), container, Collections.EMPTY_LIST);
    }

    /**
     * Get the value for this entry.
     *
     * @return The value
     */
    public ITmfXmlStateValue getValue() {
        return fValue;
    }

    /**
     * Get the key for this value
     *
     * @return The key
     */
    public ITmfXmlStateValue getKey() {
        return fKey;
    }
}
