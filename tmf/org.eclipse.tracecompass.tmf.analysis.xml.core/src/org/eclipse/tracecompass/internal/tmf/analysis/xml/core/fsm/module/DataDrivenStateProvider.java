/*******************************************************************************
 * Copyright (c) 2017 École Polytechnique de Montréal
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/

package org.eclipse.tracecompass.internal.tmf.analysis.xml.core.fsm.module;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import javax.script.ScriptEngine;

import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.tracecompass.internal.tmf.analysis.xml.core.fsm.model.DataDrivenEventHandler;
import org.eclipse.tracecompass.internal.tmf.analysis.xml.core.fsm.model.DataDrivenMappingGroup;
import org.eclipse.tracecompass.internal.tmf.analysis.xml.core.fsm.model.DataDrivenScenarioInfo;
import org.eclipse.tracecompass.statesystem.core.ITmfStateSystem;
import org.eclipse.tracecompass.statesystem.core.ITmfStateSystemBuilder;
import org.eclipse.tracecompass.tmf.core.event.ITmfEvent;
import org.eclipse.tracecompass.tmf.core.statesystem.AbstractTmfStateProvider;
import org.eclipse.tracecompass.tmf.core.statesystem.ITmfStateProvider;
import org.eclipse.tracecompass.tmf.core.statesystem.TmfAttributePool;
import org.eclipse.tracecompass.tmf.core.statesystem.TmfAttributePool.QueueType;
import org.eclipse.tracecompass.tmf.core.trace.ITmfTrace;

/**
 * A state provider for data-driven analyses
 *
 * @author Geneviève Bastien
 */
public class DataDrivenStateProvider extends AbstractTmfStateProvider implements IAnalysisDataContainer {

    private final List<DataDrivenEventHandler> fEventHandlers;
    private final Map<String, DataDrivenMappingGroup> fMappingGroups = new HashMap<>();
    private Map<String, ScriptEngine> fScriptengine = new HashMap<>();
    private final String fId;
    private final int fVersion;

    /** Map for attribute pools */
    private final Map<Integer, TmfAttributePool> fAttributePools = new HashMap<>();

    /**
     * Constructor
     *
     * @param trace
     *            The trace to run this state provider on
     * @param providerId
     *            The ID of the provider
     * @param version
     *            The version of this state provider
     * @param eventHandlers
     *            The handlers for the events
     * @param mappingGroups
     *            The mapping groups used in this analysis
     */
    public DataDrivenStateProvider(ITmfTrace trace, String providerId, int version, List<DataDrivenEventHandler> eventHandlers, Collection<DataDrivenMappingGroup> mappingGroups) {
        super(trace, providerId);
        fEventHandlers = eventHandlers;
        mappingGroups.forEach(mg -> fMappingGroups.put(mg.getId(), mg));
        fId = providerId;
        fVersion = version;
    }

    @Override
    public int getVersion() {
        return fVersion;
    }

    @Override
    public ITmfStateProvider getNewInstance() {
        return new DataDrivenStateProvider(getTrace(), fId, fVersion, fEventHandlers, fMappingGroups.values());
    }

    @Override
    protected void eventHandle(ITmfEvent event) {
        fEventHandlers.forEach(handler -> handler.handleEvent(event, DataDrivenScenarioInfo.DUMMY_SCENARIO, this));
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
    public void addFutureState(long time, @Nullable Object state, int quark) {
        this.addFutureEvent(time, state, quark);
    }

    @Override
    public void setScriptengine(String name, ScriptEngine engine) {
        fScriptengine.put(name, engine);
    }

    @Override
    public @Nullable ScriptEngine getScriptEngine(String name) {
        return fScriptengine.get(name);
    }
}
