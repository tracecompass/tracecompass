/*******************************************************************************
 * Copyright (c) 2019 École Polytechnique de Montréal
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/

package org.eclipse.tracecompass.internal.tmf.analysis.xml.core.fsm.module.pattern;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import javax.script.ScriptEngine;

import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.tracecompass.internal.tmf.analysis.xml.core.fsm.model.DataDrivenMappingGroup;
import org.eclipse.tracecompass.internal.tmf.analysis.xml.core.fsm.model.DataDrivenPatternEventHandler;
import org.eclipse.tracecompass.internal.tmf.analysis.xml.core.fsm.model.runtime.DataDrivenRuntimeData;
import org.eclipse.tracecompass.internal.tmf.analysis.xml.core.fsm.module.IAnalysisDataContainer;
import org.eclipse.tracecompass.internal.tmf.analysis.xml.core.pattern.stateprovider.ISegmentListener;
import org.eclipse.tracecompass.internal.tmf.analysis.xml.core.pattern.stateprovider.XmlPatternSegmentStoreModule;
import org.eclipse.tracecompass.statesystem.core.ITmfStateSystem;
import org.eclipse.tracecompass.statesystem.core.ITmfStateSystemBuilder;
import org.eclipse.tracecompass.tmf.core.event.ITmfEvent;
import org.eclipse.tracecompass.tmf.core.statesystem.AbstractTmfStateProvider;
import org.eclipse.tracecompass.tmf.core.statesystem.ITmfStateProvider;
import org.eclipse.tracecompass.tmf.core.statesystem.TmfAttributePool;
import org.eclipse.tracecompass.tmf.core.statesystem.TmfAttributePool.QueueType;
import org.eclipse.tracecompass.tmf.core.trace.ITmfTrace;

/**
 * The main class for data driven pattern
 *
 * FIXME: This has a lot in common with DataDrivenStateProvider, something should
 * be done about it
 *
 * @author Geneviève Bastien
 * @author Jean-Christian Kouamé
 */
public class DataDrivenPattern extends AbstractTmfStateProvider implements IAnalysisDataContainer {

    private final DataDrivenPatternEventHandler fEventHandler;
    private final Map<String, DataDrivenMappingGroup> fMappingGroups = new HashMap<>();
    private final int fVersion;
    private final String fId;
    private final Map<String, ScriptEngine> fScriptEngine = new HashMap<>();
    /** Map for attribute pools */
    private final Map<Integer, TmfAttributePool> fAttributePools = new HashMap<>();
    private final ISegmentListener fListener;
    private final Map<String, String> fStoredFields;

    /* Runtime execution data */
    private final DataDrivenRuntimeData fExecutionData = new DataDrivenRuntimeData();

    /**
     * Constructor
     *
     * @param trace
     *            The trace to run this state provider on
     * @param providerId
     *            The ID of the provider
     * @param version
     *            The version of this state provider
     * @param patternHandler
     *            The handler for the events
     * @param mappingGroups
     *            The mapping groups used in this analysis
     * @param listener
     *            The segment listener
     * @param storedFields The map of stored fields to save
     */
    public DataDrivenPattern(ITmfTrace trace, String providerId, int version, DataDrivenPatternEventHandler patternHandler, Collection<DataDrivenMappingGroup> mappingGroups, ISegmentListener listener, Map<String, String> storedFields) {
        super(trace, providerId);
        fEventHandler = patternHandler;
        mappingGroups.forEach(mg -> fMappingGroups.put(mg.getId(), mg));
        fVersion = version;
        fId = providerId;
        fListener = listener;
        fStoredFields = storedFields;
    }

    @Override
    public ITmfStateSystem getStateSystem() {
        ITmfStateSystem ss = getAssignedStateSystem();
        if (ss == null) {
            throw new NullPointerException("The state system should not be requested at this point, it is null"); //$NON-NLS-1$
        }
        return ss;
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
    public DataDrivenMappingGroup getMappingGroup(String id) {
        return Objects.requireNonNull(fMappingGroups.get(id));
    }

    @Override
    public int getVersion() {
        return fVersion;
    }

    @Override
    public ITmfStateProvider getNewInstance() {
        return new DataDrivenPattern(getTrace(), fId, fVersion, fEventHandler, fMappingGroups.values(), fListener, fStoredFields);
    }

    @Override
    protected void eventHandle(ITmfEvent event) {
        fEventHandler.handleEvent(event, this, fExecutionData);
    }

    @Override
    public void setScriptengine(String name, ScriptEngine engine) {
        fScriptEngine.put(name, engine);
    }

    @Override
    public @Nullable ScriptEngine getScriptEngine(String name) {
        return fScriptEngine.get(name);
    }

    @Override
    public boolean isReadOnlyContainer() {
        return false;
    }

    @Override
    public void dispose() {
        waitForEmptyQueue();
        fListener.onNewSegment(XmlPatternSegmentStoreModule.END_SEGMENT);
        fEventHandler.dispose(fExecutionData);
        super.dispose();
    }

    /**
     * Get the segment listener
     *
     * FIXME: Remove this method
     *
     * @return The new segment listener
     */
    public ISegmentListener getListener() {
        return fListener;
    }

    /**
     * Get the stored fields for this pattern
     *
     * FIXME: These fields should be in the action itself, not in the pattern
     *
     * @return The stored fields
     */
    public Map<String, String> getStoredFields() {
        return fStoredFields;
    }

}
