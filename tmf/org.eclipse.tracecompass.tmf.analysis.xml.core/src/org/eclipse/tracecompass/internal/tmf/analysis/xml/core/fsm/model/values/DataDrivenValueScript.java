/*******************************************************************************
 * Copyright (c) 2017 École Polytechnique de Montréal
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/

package org.eclipse.tracecompass.internal.tmf.analysis.xml.core.fsm.model.values;

import java.util.Map;
import java.util.Objects;
import java.util.Map.Entry;
import java.util.function.Function;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;

import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.tracecompass.internal.tmf.analysis.xml.core.Activator;
import org.eclipse.tracecompass.internal.tmf.analysis.xml.core.fsm.model.DataDrivenScenarioInfo;
import org.eclipse.tracecompass.internal.tmf.analysis.xml.core.fsm.module.IAnalysisDataContainer;
import org.eclipse.tracecompass.statesystem.core.statevalue.ITmfStateValue;
import org.eclipse.tracecompass.statesystem.core.statevalue.TmfStateValue;
import org.eclipse.tracecompass.tmf.core.event.ITmfEvent;

/**
 * A value that resolves to the result of a scripts
 *
 * @author Geneviève Bastien
 * @author Abdelrahmane Berhil
 */
public class DataDrivenValueScript extends DataDrivenValue {

    /** the default script engine */
    public static final String DEFAULT_SCRIPT_ENGINE = "nashorn"; //$NON-NLS-1$
    private final Map<String, DataDrivenValue> fValues;
    private final String fScriptEngine;
    private final String fScript;

    /**
     * Constructor
     *
     * @param mappingGroupId
     *            The ID of the mapping group to use to map the retrieved value to
     *            another value
     * @param forcedType
     *            The desired type of the value
     * @param values
     *            A mapping of keys in the script with the values with which to
     *            replace to keys at runtime
     * @param script
     *            The script to run
     * @param scriptEngine
     *            The script engine. By default, it is {@link #DEFAULT_SCRIPT_ENGINE} (javascript)
     */
    public DataDrivenValueScript(@Nullable String mappingGroupId, ITmfStateValue.Type forcedType, Map<String, DataDrivenValue> values, String script, String scriptEngine) {
        super(mappingGroupId, forcedType);
        fScriptEngine = !scriptEngine.isEmpty() ? scriptEngine : DEFAULT_SCRIPT_ENGINE;
        fValues = values;
        fScript = script;
    }

    @Override
    protected @Nullable Object resolveValue(int quark, IAnalysisDataContainer container) {
        return executeScript(sv -> sv.resolveValue(quark, container));
    }

    @Override
    protected @Nullable Object resolveValue(ITmfEvent event, int quark, DataDrivenScenarioInfo scenarioInfo, IAnalysisDataContainer container) {
        return executeScript(sv -> sv.resolveValue(event, quark, scenarioInfo, container));
    }

    private @Nullable Object executeScript(Function<DataDrivenValue, @Nullable Object> function) {
        Object result = null;
        ScriptEngineManager manager = new ScriptEngineManager();
        ScriptEngine engine = manager.getEngineByName(fScriptEngine);
        if (engine == null) {
            Activator.logWarning("Unknown script engine: " + fScriptEngine); //$NON-NLS-1$
            return null;
        }

        for (Entry<String, DataDrivenValue> entry : fValues.entrySet()) {
            String stateValueId = Objects.requireNonNull(entry.getKey());
            DataDrivenValue stateValue = Objects.requireNonNull(entry.getValue());
            Object value = function.apply(stateValue);
            engine.put(stateValueId, value);
        }

        try {
            result = engine.eval(fScript);
        } catch (ScriptException e) {
            Activator.logError("Script execution failed", e); //$NON-NLS-1$
            return TmfStateValue.nullValue();
        }

        return result;
    }

    @Override
    public String toString() {
        return "TmfXmlValueScript: " + fScript + " -> " + fValues; //$NON-NLS-1$ //$NON-NLS-2$
    }

}
