/*******************************************************************************
 * Copyright (c) 2014 École Polytechnique de Montréal
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Florian Wininger - Initial API and implementation
 *******************************************************************************/

package org.eclipse.linuxtools.tmf.analysis.xml.core.stateprovider;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.eclipse.core.runtime.IPath;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.linuxtools.statesystem.core.ITmfStateSystem;
import org.eclipse.linuxtools.tmf.analysis.xml.core.model.ITmfXmlModelFactory;
import org.eclipse.linuxtools.tmf.analysis.xml.core.model.TmfXmlEventHandler;
import org.eclipse.linuxtools.tmf.analysis.xml.core.model.TmfXmlLocation;
import org.eclipse.linuxtools.tmf.analysis.xml.core.model.readwrite.TmfXmlReadWriteModelFactory;
import org.eclipse.linuxtools.tmf.analysis.xml.core.module.IXmlStateSystemContainer;
import org.eclipse.linuxtools.tmf.analysis.xml.core.module.XmlUtils;
import org.eclipse.linuxtools.tmf.core.event.ITmfEvent;
import org.eclipse.linuxtools.tmf.core.statesystem.AbstractTmfStateProvider;
import org.eclipse.linuxtools.tmf.core.trace.ITmfTrace;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

/**
 * This is the state change input plug-in for TMF's state system which handles
 * the XML Format
 *
 * @author Florian Wininger
 */
public class XmlStateProvider extends AbstractTmfStateProvider implements IXmlStateSystemContainer {

    private final IPath fFilePath;
    @NonNull private final String fStateId;

    /** List of all Event Handlers */
    private final Set<TmfXmlEventHandler> fEventHandlers = new HashSet<>();

    /** List of all Locations */
    private final Set<TmfXmlLocation> fLocations;

    /** Map for defined values */
    private final Map<String, String> fDefinedValues = new HashMap<>();

    // ------------------------------------------------------------------------
    // Constructor
    // ------------------------------------------------------------------------

    /**
     * Instantiate a new state provider plug-in.
     *
     * @param trace
     *            The trace
     * @param stateid
     *            The state system id, corresponding to the analysis_id
     *            attribute of the state provider element of the XML file
     * @param file
     *            Path to the XML file containing the state provider definition
     */
    public XmlStateProvider(ITmfTrace trace, @NonNull String stateid, IPath file) {
        super(trace, ITmfEvent.class, stateid);
        fStateId = stateid;
        fFilePath = file;
        Element doc = XmlUtils.getElementInFile(fFilePath.makeAbsolute().toOSString(), TmfXmlStrings.STATE_PROVIDER, fStateId);
        if (doc == null) {
            fLocations = new HashSet<>();
            return;
        }

        ITmfXmlModelFactory modelFactory = TmfXmlReadWriteModelFactory.getInstance();
        /* parser for defined Values */
        NodeList definedStateNodes = doc.getElementsByTagName(TmfXmlStrings.DEFINED_VALUE);
        for (int i = 0; i < definedStateNodes.getLength(); i++) {
            Element element = (Element) definedStateNodes.item(i);
            fDefinedValues.put(element.getAttribute(TmfXmlStrings.NAME), element.getAttribute(TmfXmlStrings.VALUE));
        }

        /* parser for the locations */
        NodeList locationNodes = doc.getElementsByTagName(TmfXmlStrings.LOCATION);
        Set<TmfXmlLocation> locations = new HashSet<>();
        for (int i = 0; i < locationNodes.getLength(); i++) {
            Element element = (Element) locationNodes.item(i);
            TmfXmlLocation location = modelFactory.createLocation(element, this);
            locations.add(location);
        }
        fLocations = Collections.unmodifiableSet(locations);

        /* parser for the event handlers */
        NodeList nodes = doc.getElementsByTagName(TmfXmlStrings.EVENT_HANDLER);
        for (int i = 0; i < nodes.getLength(); i++) {
            Element element = (Element) nodes.item(i);
            TmfXmlEventHandler handler = modelFactory.createEventHandler(element, this);
            fEventHandlers.add(handler);
        }
    }

    /**
     * Get the state id of the state provider
     *
     * @return The state id of the state provider
     */
    @NonNull
    public String getStateId() {
        return fStateId;
    }

    // ------------------------------------------------------------------------
    // IStateChangeInput
    // ------------------------------------------------------------------------

    @Override
    public int getVersion() {
        Element ssNode = XmlUtils.getElementInFile(fFilePath.makeAbsolute().toOSString(), TmfXmlStrings.STATE_PROVIDER, fStateId);
        if (ssNode != null) {
            return Integer.parseInt(ssNode.getAttribute(TmfXmlStrings.VERSION));
        }
        /*
         * The version attribute is mandatory and XML files that don't validate
         * with the XSD are ignored, so this should never happen
         */
        throw new IllegalStateException("The state provider XML node should have a version attribute"); //$NON-NLS-1$
    }

    @Override
    public XmlStateProvider getNewInstance() {
        return new XmlStateProvider(this.getTrace(), getStateId(), fFilePath);
    }

    @Override
    protected void eventHandle(ITmfEvent event) {
        if (event == null) {
            return;
        }
        for (TmfXmlEventHandler eventHandler : fEventHandlers) {
            eventHandler.handleEvent(event);
        }
    }

    @Override
    public ITmfStateSystem getStateSystem() {
        return ss;
    }

    // ------------------------------------------------------------------------
    // Operations
    // ------------------------------------------------------------------------

    @Override
    public Iterable<TmfXmlLocation> getLocations() {
        return fLocations;
    }

    /**
     * Get the defined value associated with a constant
     *
     * @param constant
     *            The constant defining this value
     * @return The actual value corresponding to this constant
     */
    public String getDefinedValue(String constant) {
        return fDefinedValues.get(constant);
    }

    @Override
    public String getAttributeValue(String name) {
        String attribute = name;
        if (attribute.startsWith(TmfXmlStrings.VARIABLE_PREFIX)) {
            /* search the attribute in the map without the fist character $ */
            attribute = getDefinedValue(attribute.substring(1));
        }
        return attribute;
    }

}