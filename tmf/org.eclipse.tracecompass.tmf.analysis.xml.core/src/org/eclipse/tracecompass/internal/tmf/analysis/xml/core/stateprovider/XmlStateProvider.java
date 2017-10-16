/*******************************************************************************
 * Copyright (c) 2014, 2015 École Polytechnique de Montréal
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Florian Wininger - Initial API and implementation
 *******************************************************************************/

package org.eclipse.tracecompass.internal.tmf.analysis.xml.core.stateprovider;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.tracecompass.internal.tmf.analysis.xml.core.model.ITmfXmlModelFactory;
import org.eclipse.tracecompass.internal.tmf.analysis.xml.core.model.TmfXmlEventHandler;
import org.eclipse.tracecompass.internal.tmf.analysis.xml.core.model.TmfXmlLocation;
import org.eclipse.tracecompass.internal.tmf.analysis.xml.core.model.TmfXmlMapEntry;
import org.eclipse.tracecompass.internal.tmf.analysis.xml.core.model.readwrite.TmfXmlReadWriteModelFactory;
import org.eclipse.tracecompass.internal.tmf.analysis.xml.core.module.IXmlStateSystemContainer;
import org.eclipse.tracecompass.statesystem.core.ITmfStateSystem;
import org.eclipse.tracecompass.statesystem.core.ITmfStateSystemBuilder;
import org.eclipse.tracecompass.tmf.analysis.xml.core.module.TmfXmlStrings;
import org.eclipse.tracecompass.tmf.analysis.xml.core.module.TmfXmlUtils;
import org.eclipse.tracecompass.tmf.core.event.ITmfEvent;
import org.eclipse.tracecompass.tmf.core.statesystem.AbstractTmfStateProvider;
import org.eclipse.tracecompass.tmf.core.statesystem.TmfAttributePool;
import org.eclipse.tracecompass.tmf.core.statesystem.TmfAttributePool.QueueType;
import org.eclipse.tracecompass.tmf.core.trace.ITmfTrace;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

/**
 * This is the state change input plug-in for TMF's state system which handles
 * the XML Format
 *
 * @author Florian Wininger
 */
public class XmlStateProvider extends AbstractTmfStateProvider implements IXmlStateSystemContainer {

    private final Path fFilePath;
    private final @NonNull String fStateId;

    /** List of all Event Handlers */
    private final List<TmfXmlEventHandler> fEventHandlers = new ArrayList<>();

    /** List of all Locations */
    private final @NonNull Set<@NonNull TmfXmlLocation> fLocations;

    /** Map for defined values */
    private final Map<String, String> fDefinedValues = new HashMap<>();
    /** Map for attribute pools */
    private final Map<Integer, TmfAttributePool> fAttributePools = new HashMap<>();

    private final @NonNull Map<@NonNull String, @NonNull Set<@NonNull TmfXmlMapEntry>> fMappingGroups;

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
    public XmlStateProvider(@NonNull ITmfTrace trace, @NonNull String stateid, Path file) {
        super(trace, stateid);
        fStateId = stateid;
        fFilePath = file;
        Element doc = TmfXmlUtils.getElementInFile(file.toAbsolutePath().toString(), TmfXmlStrings.STATE_PROVIDER, fStateId);
        if (doc == null) {
            throw new IllegalArgumentException("XmlStateProvider: Cannot find state provider element in file " + file); //$NON-NLS-1$
        }

        ITmfXmlModelFactory modelFactory = TmfXmlReadWriteModelFactory.getInstance();
        /* parser for defined Values */
        NodeList definedStateNodes = doc.getElementsByTagName(TmfXmlStrings.DEFINED_VALUE);
        for (int i = 0; i < definedStateNodes.getLength(); i++) {
            Element element = (Element) definedStateNodes.item(i);
            fDefinedValues.put(element.getAttribute(TmfXmlStrings.NAME), element.getAttribute(TmfXmlStrings.VALUE));
        }

        /* parser for the locations */
        List<Element> childElements = TmfXmlUtils.getChildElements(doc, TmfXmlStrings.LOCATION);
        Set<@NonNull TmfXmlLocation> locations = new HashSet<>();
        for (Element element : childElements) {
            if (element == null) {
                continue;
            }
            TmfXmlLocation location = modelFactory.createLocation(element, this);
            locations.add(location);
        }
        fLocations = Collections.unmodifiableSet(locations);

        /* parser for the mapping groups */
        final @NonNull Map<@NonNull String, @NonNull Set<@NonNull TmfXmlMapEntry>> mapGroups = new HashMap<>();
        NodeList mapNodes = doc.getElementsByTagName(TmfXmlStrings.MAPPING_GROUP);
        for (int i = 0; i < mapNodes.getLength(); i++) {
            Element map = (Element) mapNodes.item(i);
            String id = map.getAttribute(TmfXmlStrings.ID);

            Set<@NonNull TmfXmlMapEntry> entrySet = mapGroups.get(id);
            if (entrySet == null) {
                entrySet = new HashSet<>();
                mapGroups.put(id, entrySet);
            }

            NodeList entryNodes = map.getElementsByTagName(TmfXmlStrings.ENTRY);
            for (int j = 0; j < entryNodes.getLength(); j++) {
                Element entryElement = (Element) entryNodes.item(j);
                if (entryElement == null) {
                    continue;
                }
                TmfXmlMapEntry entry = modelFactory.createMapEntry(entryElement, this);
                entrySet.add(entry);
            }
        }
        fMappingGroups = Collections.unmodifiableMap(mapGroups);

        /* parser for the event handlers */
        childElements = TmfXmlUtils.getChildElements(doc, TmfXmlStrings.EVENT_HANDLER);
        for (Element element : childElements) {
            if (element == null) {
                continue;
            }
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
        Element ssNode = TmfXmlUtils.getElementInFile(fFilePath.toAbsolutePath().toString(), TmfXmlStrings.STATE_PROVIDER, fStateId);
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
        for (TmfXmlEventHandler eventHandler : fEventHandlers) {
            eventHandler.handleEvent(event);
        }
    }

    @Override
    public ITmfStateSystem getStateSystem() {
        return getStateSystemBuilder();
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

    /**
     * Get the list of state value handlers defined in this top level element
     *
     * @param id
     *            The mapping group id
     * @return The set of {@link TmfXmlMapEntry}
     */
    public @Nullable Set<@NonNull TmfXmlMapEntry> getMappingGroup(@NonNull String id) {
        return fMappingGroups.get(id);
    }

    @Override
    public @Nullable TmfAttributePool getAttributePool(int startNodeQuark) {
        ITmfStateSystem ss = getStateSystem();
        if (!(ss instanceof ITmfStateSystemBuilder)) {
            throw new IllegalStateException("The state system hasn't been initialized yet"); //$NON-NLS-1$
        }
        TmfAttributePool pool = fAttributePools.get(startNodeQuark);
        if (pool == null) {
            pool = new TmfAttributePool((ITmfStateSystemBuilder) ss, startNodeQuark, QueueType.PRIORITY);
            fAttributePools.put(startNodeQuark, pool);
        }
        return pool;
    }
}