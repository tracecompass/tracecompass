/*******************************************************************************
 * Copyright (c) 2016 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.eclipse.tracecompass.internal.tmf.analysis.xml.core.pattern.stateprovider;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import javax.script.ScriptEngine;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.tracecompass.common.core.NonNullUtils;
import org.eclipse.tracecompass.internal.tmf.analysis.xml.core.fsm.compile.AnalysisCompilationData;
import org.eclipse.tracecompass.internal.tmf.analysis.xml.core.fsm.compile.TmfXmlLocationCu;
import org.eclipse.tracecompass.internal.tmf.analysis.xml.core.fsm.compile.TmfXmlMappingGroupCu;
import org.eclipse.tracecompass.internal.tmf.analysis.xml.core.fsm.model.DataDrivenMappingGroup;
import org.eclipse.tracecompass.internal.tmf.analysis.xml.core.model.ITmfXmlModelFactory;
import org.eclipse.tracecompass.internal.tmf.analysis.xml.core.model.TmfXmlLocation;
import org.eclipse.tracecompass.internal.tmf.analysis.xml.core.model.TmfXmlPatternEventHandler;
import org.eclipse.tracecompass.internal.tmf.analysis.xml.core.model.TmfXmlScenarioHistoryBuilder;
import org.eclipse.tracecompass.internal.tmf.analysis.xml.core.model.readwrite.TmfXmlReadWriteModelFactory;
import org.eclipse.tracecompass.internal.tmf.analysis.xml.core.module.IXmlStateSystemContainer;
import org.eclipse.tracecompass.statesystem.core.ITmfStateSystem;
import org.eclipse.tracecompass.statesystem.core.ITmfStateSystemBuilder;
import org.eclipse.tracecompass.tmf.analysis.xml.core.module.TmfXmlStrings;
import org.eclipse.tracecompass.tmf.analysis.xml.core.module.TmfXmlUtils;
import org.eclipse.tracecompass.tmf.core.event.ITmfEvent;
import org.eclipse.tracecompass.tmf.core.statesystem.AbstractTmfStateProvider;
import org.eclipse.tracecompass.tmf.core.statesystem.ITmfStateProvider;
import org.eclipse.tracecompass.tmf.core.statesystem.TmfAttributePool;
import org.eclipse.tracecompass.tmf.core.statesystem.TmfAttributePool.QueueType;
import org.eclipse.tracecompass.tmf.core.trace.ITmfTrace;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

/**
 * State provider for the pattern analysis
 *
 * @author Jean-Christian Kouame
 */
public class XmlPatternStateProvider extends AbstractTmfStateProvider implements IXmlStateSystemContainer {

    private final Path fFilePath;

    private final @NonNull String fStateId;

    /** Map for attribute pools */
    private final Map<Integer, TmfAttributePool> fAttributePools = new HashMap<>();

    /** List of all Locations */
    private final @NonNull Set<@NonNull TmfXmlLocation> fLocations;

    private final Map<@NonNull String, @NonNull DataDrivenMappingGroup> fMappingGroups = new HashMap<>();

    /** Map for stored values */
    private final @NonNull Map<@NonNull String, @NonNull String> fStoredFields = new HashMap<>();

    private final TmfXmlPatternEventHandler fHandler;

    private final ISegmentListener fListener;

    private final @NonNull TmfXmlScenarioHistoryBuilder fHistoryBuilder;

    private final AnalysisCompilationData fAnalysisCompilationData;

    private Map<String, ScriptEngine> fScriptengine = new HashMap<>();

    /**
     * @param trace
     *            The active trace
     * @param stateid
     *            The state id, which corresponds to the id of the analysis
     *            defined in the XML file
     * @param file
     *            The XML file
     * @param listener
     *            Listener for segment creation
     */
    public XmlPatternStateProvider(@NonNull ITmfTrace trace, @NonNull String stateid, @Nullable Path file, ISegmentListener listener) {
        super(trace, stateid);
        fStateId = stateid;
        fFilePath = file;
        fListener = listener;
        fHistoryBuilder = new TmfXmlScenarioHistoryBuilder();
        final String pathString = fFilePath.toAbsolutePath().toString();
        Element doc = TmfXmlUtils.getElementInFile(pathString, TmfXmlStrings.PATTERN, fStateId);
        if (doc == null) {
            throw new IllegalArgumentException("XmlPatternStateProvider: Cannot find pattern element in file " + pathString); //$NON-NLS-1$
        }

        /* parser for defined Fields */
        NodeList storedFieldNodes = doc.getElementsByTagName(TmfXmlStrings.STORED_FIELD);
        for (int i = 0; i < storedFieldNodes.getLength(); i++) {
            Element element = (Element) storedFieldNodes.item(i);
            String key = element.getAttribute(TmfXmlStrings.ALIAS);
            fStoredFields.put(element.getAttribute(TmfXmlStrings.ID), key.isEmpty() ? element.getAttribute(TmfXmlStrings.ID) : key);
        }

        AnalysisCompilationData analysisData = new AnalysisCompilationData();
        fAnalysisCompilationData = analysisData;

        /* parser for defined Values */
        List<@NonNull Element> childElements = TmfXmlUtils.getChildElements(doc, TmfXmlStrings.DEFINED_VALUE);
        for (Element element : childElements) {
            analysisData.addDefinedValue(element.getAttribute(TmfXmlStrings.NAME), element.getAttribute(TmfXmlStrings.VALUE));
        }

        /* parser for the locations */
        childElements = TmfXmlUtils.getChildElements(doc, TmfXmlStrings.LOCATION);
        for (Element element : childElements) {
            TmfXmlLocationCu.compile(analysisData, element);
        }

        /* parser for the mapping groups */
        List<TmfXmlMappingGroupCu> mapGroups = new ArrayList<>();
        childElements = TmfXmlUtils.getChildElements(doc, TmfXmlStrings.MAPPING_GROUP);
        for (Element map : childElements) {
            TmfXmlMappingGroupCu compile = TmfXmlMappingGroupCu.compile(analysisData, map);
            if (compile == null) {
                throw new NullPointerException("Problem compiling a mapping group"); //$NON-NLS-1$
            }
            mapGroups.add(compile);

        }
        for (TmfXmlMappingGroupCu mapGroup : mapGroups) {
            DataDrivenMappingGroup group = mapGroup.generate();
            fMappingGroups.put(group.getId(), group);
        }

        // TODO: Replace usages of locations and mapping group to avoid having to do this legacy code
        // FIXME: Redundant legacy code for locations and mapping groups

        ITmfXmlModelFactory modelFactory = TmfXmlReadWriteModelFactory.getInstance();
        /* parser for the locations */
        NodeList locationNodes = doc.getElementsByTagName(TmfXmlStrings.LOCATION);
        final Set<@NonNull TmfXmlLocation> locations = new HashSet<>();
        for (int i = 0; i < locationNodes.getLength(); i++) {
            Element element = (Element) locationNodes.item(i);
            if (element == null) {
                continue;
            }
            TmfXmlLocation location = modelFactory.createLocation(element, this);
            locations.add(location);
        }
        fLocations = Collections.unmodifiableSet(locations);

        /* parser for the event handlers */
        NodeList nodes = doc.getElementsByTagName(TmfXmlStrings.PATTERN_HANDLER);
        fHandler = modelFactory.createPatternEventHandler(NonNullUtils.checkNotNull((Element) nodes.item(0)), this);
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
     * Get the defined value associated with a constant
     *
     * @param constant
     *            The constant defining this value
     * @return The actual value corresponding to this constant
     */
    public String getDefinedValue(String constant) {
        if (constant != null) {
            return fAnalysisCompilationData.getStringValue(constant);
        }
        return null;
    }

    /**
     * Get the stored fiels map
     *
     * @return The map of stored fields
     */
    public @NonNull Map<@NonNull String, @NonNull String> getStoredFields() {
        return fStoredFields;
    }

    @Override
    public int getVersion() {
        return 0;
    }

    @Override
    public @NonNull ITmfStateProvider getNewInstance() {
        return new XmlPatternStateProvider(getTrace(), getStateId(), fFilePath, fListener);
    }

    /**
     * Get the state ID of the provider. It corresponds to the analysis ID.
     *
     * @return the state Id
     */
    public @NonNull String getStateId() {
        return fStateId;
    }

    @Override
    public ITmfStateSystem getStateSystem() {
        ITmfStateSystem ss = getStateSystemBuilder();
        if (ss == null) {
            throw new NullPointerException("The state system should not be requested at this point, it is null"); //$NON-NLS-1$
        }
        return ss;
    }

    @Override
    public @NonNull Iterable<@NonNull TmfXmlLocation> getLocations() {
        return fLocations;
    }

    @Override
    protected void eventHandle(@NonNull ITmfEvent event) {
        fHandler.handleEvent(event);
    }

    /**
     * Get the listerner for segments creation
     *
     * @return The segment listener
     */
    public ISegmentListener getListener() {
        return fListener;
    }

    @Override
    public void dispose() {
        waitForEmptyQueue();
        fListener.onNewSegment(XmlPatternSegmentStoreModule.END_SEGMENT);
        fHandler.dispose();
        super.dispose();
    }

    /**
     * Get the history builder of this analysis
     *
     * @return The history builder
     */
    public @NonNull TmfXmlScenarioHistoryBuilder getHistoryBuilder() {
        return fHistoryBuilder;
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

    @Override
    public AnalysisCompilationData getAnalysisCompilationData() {
        return fAnalysisCompilationData;
    }

    @Override
    public void setScriptengine(String name, ScriptEngine engine) {
        fScriptengine.put(name, engine);
    }

    @Override
    public ScriptEngine getScriptEngine(String name) {
        return fScriptengine.get(name);
    }

    @Override
    public DataDrivenMappingGroup getMappingGroup(String id) {
        return Objects.requireNonNull(fMappingGroups.get(id));
    }
}