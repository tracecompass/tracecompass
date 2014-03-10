/*******************************************************************************
 * Copyright (c) 2014 Ecole Polytechnique de Montreal
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Florian Wininger - Initial API and implementation
 ******************************************************************************/

package org.eclipse.linuxtools.tmf.analysis.xml.core.model;

import java.util.LinkedList;
import java.util.List;

import org.eclipse.linuxtools.tmf.analysis.xml.core.module.IXmlStateSystemContainer;
import org.eclipse.linuxtools.tmf.analysis.xml.core.module.XmlUtils;
import org.eclipse.linuxtools.tmf.analysis.xml.core.stateprovider.TmfXmlStrings;
import org.eclipse.linuxtools.tmf.core.event.ITmfEvent;
import org.w3c.dom.Element;

/**
 * This Class implements a Location in the XML-defined state system, ie a named
 * shortcut to reach a given attribute, used in state attributes
 *
 * <pre>
 * example:
 *  <location id="CurrentCPU">
 *    <stateAttribute type="constant" value="CPUs" />
 *    <stateAttribute type="eventField" name="cpu" />
 *    ...
 *  </location>
 * </pre>
 *
 * @author Florian Wininger
 */
public class TmfXmlLocation {

    /** Path in the State System */
    private final List<ITmfXmlStateAttribute> fPath = new LinkedList<>();

    /** ID : name of the location */
    private final String fId;
    private final IXmlStateSystemContainer fContainer;

    /**
     * Constructor
     *
     * @param modelFactory
     *            The factory used to create XML model elements
     * @param location
     *            XML node element
     * @param container
     *            The state system container this location belongs to
     */
    public TmfXmlLocation(ITmfXmlModelFactory modelFactory, Element location, IXmlStateSystemContainer container) {
        fId = location.getAttribute(TmfXmlStrings.ID);
        fContainer = container;

        List<Element> childElements = XmlUtils.getChildElements(location);
        for (Element attribute : childElements) {
            ITmfXmlStateAttribute xAttribute = modelFactory.createStateAttribute(attribute, fContainer);
            fPath.add(xAttribute);
        }
    }

    /**
     * Get the id of the location
     *
     * @return The id of a location
     */
    public String getId() {
        return fId;
    }

    /**
     * Get the quark for the path represented by this location
     *
     * @param event
     *            The event being handled
     * @param startQuark
     *            The starting quark for relative search, use
     *            {@link IXmlStateSystemContainer#ROOT_QUARK} for the root of
     *            the attribute tree
     * @return The quark at the leaf of the path
     */
    public int getLocationQuark(ITmfEvent event, int startQuark) {
        int quark = startQuark;
        for (ITmfXmlStateAttribute attrib : fPath) {
            quark = attrib.getAttributeQuark(event, quark);
            if (quark == IXmlStateSystemContainer.ERROR_QUARK) {
                break;
            }
        }
        return quark;
    }

    /**
     * Get the quark for the path represented by this location
     *
     * @param startQuark
     *            The starting quark for relative search, use
     *            {@link IXmlStateSystemContainer#ROOT_QUARK} for the root of
     *            the attribute tree
     * @return The quark at the leaf of the path
     */
    public int getLocationQuark(int startQuark) {
        int quark = startQuark;
        for (ITmfXmlStateAttribute attrib : fPath) {
            quark = attrib.getAttributeQuark(quark);
            if (quark == IXmlStateSystemContainer.ERROR_QUARK) {
                break;
            }
        }
        return quark;
    }

}