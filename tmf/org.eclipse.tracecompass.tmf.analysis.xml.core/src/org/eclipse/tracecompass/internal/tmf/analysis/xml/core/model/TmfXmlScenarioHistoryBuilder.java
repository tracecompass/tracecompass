/*******************************************************************************
 * Copyright (c) 2016 Ecole Polytechnique de Montreal, Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.eclipse.tracecompass.internal.tmf.analysis.xml.core.model;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.tracecompass.common.core.NonNullUtils;
import org.eclipse.tracecompass.internal.tmf.analysis.xml.core.Activator;
import org.eclipse.tracecompass.internal.tmf.analysis.xml.core.module.IXmlStateSystemContainer;
import org.eclipse.tracecompass.statesystem.core.ITmfStateSystemBuilder;
import org.eclipse.tracecompass.statesystem.core.exceptions.StateSystemDisposedException;
import org.eclipse.tracecompass.statesystem.core.exceptions.StateValueTypeException;
import org.eclipse.tracecompass.statesystem.core.interval.ITmfStateInterval;
import org.eclipse.tracecompass.statesystem.core.statevalue.ITmfStateValue;
import org.eclipse.tracecompass.statesystem.core.statevalue.TmfStateValue;
import org.eclipse.tracecompass.tmf.analysis.xml.core.module.TmfXmlStrings;
import org.eclipse.tracecompass.tmf.core.event.ITmfEvent;
import org.eclipse.tracecompass.tmf.core.statesystem.TmfAttributePool;

import com.google.common.collect.BiMap;
import com.google.common.collect.ImmutableBiMap;

/**
 * This class is responsible for creating scenarios, updating their status and
 * data, and saving the scenario data to the state system
 */
public class TmfXmlScenarioHistoryBuilder {

    /** The string 'status' */
    public static final String STATUS = "status"; //$NON-NLS-1$
    /** The string for "nbScenarios" */
    public static final String SCENARIO_COUNT = "nbScenarios"; //$NON-NLS-1$

    /** The string for start time */
    private static final String START_TIME = "startTime"; //$NON-NLS-1$
    /** Error message */
    private static final String ERROR_MESSAGE = "The state system is null"; //$NON-NLS-1$

    private final Map<String, TmfAttributePool> fFsmPools = new HashMap<>();

    /**
     * All possible types of status for a scenario
     */
    public enum ScenarioStatusType {
        /**
         * scenario pending for start point
         */
        PENDING,
        /**
         * scenario in progress
         */
        IN_PROGRESS,
        /**
         * scenario abandoned
         */
        ABANDONED,
        /**
         * scenario match with the pattern
         */
        MATCHED
    }

    /**
     * Cache the available status in a map
     */
    protected static final BiMap<ScenarioStatusType, ITmfStateValue> STATUS_MAP = ImmutableBiMap.of(
            ScenarioStatusType.PENDING, TmfStateValue.newValueInt(0),
            ScenarioStatusType.IN_PROGRESS, TmfStateValue.newValueInt(1),
            ScenarioStatusType.MATCHED, TmfStateValue.newValueInt(2),
            ScenarioStatusType.ABANDONED, TmfStateValue.newValueInt(3));

    /**
     * Get the scenario matched process start time
     *
     * @param container
     *            The state system container this class use
     * @param info
     *            The scenario details
     * @param event
     *            The current event
     *
     * @return The start time of the matching process for the specified scenario
     */
    public long getStartTime(final IXmlStateSystemContainer container, final TmfXmlScenarioInfo info, final ITmfEvent event) {
        ITmfStateSystemBuilder ss = (ITmfStateSystemBuilder) container.getStateSystem();
        long ts = getTimestamp(event, ss);
        try {
            int attributeQuark = getQuarkRelativeAndAdd(ss, info.getQuark(), START_TIME);
            ITmfStateInterval state = ss.querySingleState(ts, attributeQuark);
            return state.getStartTime();
        } catch (StateSystemDisposedException e) {
            Activator.logError("failed to get the start time of the scenario", e); //$NON-NLS-1$
        }
        return -1L;
    }

    /**
     * Save the stored fields
     *
     * @param container
     *            The state system container this class use
     * @param attributeName
     *            The name of the attribute to save
     * @param value
     *            The value of the attribute to save
     * @param info
     *            The scenario details
     * @param event
     *            The current event
     */
    public void updateStoredFields(final IXmlStateSystemContainer container, final String attributeName, final ITmfStateValue value, final TmfXmlScenarioInfo info, final ITmfEvent event) {
        ITmfStateSystemBuilder ss = (ITmfStateSystemBuilder) container.getStateSystem();
        long ts = getTimestamp(event, ss);
        try {
            int attributeQuark = getQuarkRelativeAndAdd(ss, info.getQuark(), TmfXmlStrings.STORED_FIELDS, attributeName);
            ss.modifyAttribute(ts, value.unboxValue(), attributeQuark);
        } catch (StateValueTypeException e) {
            Activator.logError("failed to save the stored field " + attributeName, e); //$NON-NLS-1$
        }
    }

    /**
     * Clear the special fields
     *
     * @param container
     *            The state system container this class use
     * @param attributeName
     *            The name of the attribute to save
     * @param info
     *            The scenario details
     * @param event
     *            The current event
     */
    public void resetStoredFields(final IXmlStateSystemContainer container, final String attributeName, final TmfXmlScenarioInfo info, final ITmfEvent event) {
        ITmfStateSystemBuilder ss = (ITmfStateSystemBuilder) container.getStateSystem();
        long ts = getTimestamp(event, ss);
        try {
            int attributeQuark = getQuarkRelativeAndAdd(ss, info.getQuark(), TmfXmlStrings.STORED_FIELDS, attributeName);
            ss.modifyAttribute(ts, (Object) null, attributeQuark);
        } catch (StateValueTypeException e) {
            Activator.logError("failed to clear the stored fields", e); //$NON-NLS-1$
        }
    }

    /**
     * Get the value of a special field in the state system
     *
     * @param container
     *            The state system container this class use
     * @param attributeName
     *            The attribute name of the special field
     * @param info
     *            The scenario details
     * @param event
     *            The current event
     *
     * @return The value of a special field saved into the state system
     */
    public ITmfStateValue getStoredFieldValue(IXmlStateSystemContainer container, String attributeName, final TmfXmlScenarioInfo info, ITmfEvent event) {
        ITmfStateSystemBuilder ss = (ITmfStateSystemBuilder) container.getStateSystem();
        long ts = event.getTimestamp().toNanos();
        ITmfStateInterval state = null;
        try {
            int attributeQuark = getQuarkRelativeAndAdd(ss, info.getQuark(), TmfXmlStrings.STORED_FIELDS, attributeName);
            state = ss.querySingleState(ts, attributeQuark);
        } catch (StateSystemDisposedException e) {
            Activator.logError("failed to get the value of the stored field " + attributeName, e); //$NON-NLS-1$
        }
        return (state != null) ? NonNullUtils.checkNotNull(state.getStateValue()) : TmfStateValue.nullValue();
    }

    /**
     * Get the attribute pool for this fsm
     *
     * @param container
     *            The state system container
     * @param fsmId
     *            The ID of the FSM
     * @return The attribute pool associated with this FSM
     */
    protected TmfAttributePool getPoolFor(IXmlStateSystemContainer container, String fsmId) {
        TmfAttributePool pool = fFsmPools.get(fsmId);
        if (pool != null) {
            return pool;
        }
        ITmfStateSystemBuilder ss = NonNullUtils.checkNotNull((ITmfStateSystemBuilder) container.getStateSystem());
        String[] fsmPath = new String[] { TmfXmlStrings.SCENARIOS, fsmId };
        int quark = getQuarkAbsoluteAndAdd(ss, fsmPath);
        pool = new TmfAttributePool(ss, quark);
        fFsmPools.put(fsmId, pool);
        return pool;
    }

    /**
     * Get the scenario quark
     *
     * @param container
     *            The state system container this class use
     * @param fsmId
     *            Id of the fsm this scenario is associated to
     * @return The scenario quark
     */
    public int assignScenarioQuark(IXmlStateSystemContainer container, String fsmId) {
        TmfAttributePool pool = getPoolFor(container, fsmId);
        return pool.getAvailable();
    }

    /**
     * Get the scenario status quark
     *
     * @param container
     *            The state system container this class use
     * @param scenarioQuark
     *            The scenario quark
     * @return The scenario quark
     */
    public int getScenarioStatusQuark(IXmlStateSystemContainer container, int scenarioQuark) {
        ITmfStateSystemBuilder ss = (ITmfStateSystemBuilder) container.getStateSystem();
        return getQuarkRelativeAndAdd(ss, scenarioQuark, STATUS);
    }

    /**
     * Get the start time of a specific state of the scenario
     *
     * @param container
     *            The state system container this class use
     * @param stateName
     *            The name of the current state of the scenario
     * @param info
     *            The scenario details
     * @param event
     *            The current event
     *
     * @return The start time for the specified state
     */
    public long getSpecificStateStartTime(final IXmlStateSystemContainer container, final String stateName, final TmfXmlScenarioInfo info, final ITmfEvent event) {
        long ts = event.getTimestamp().getValue();
        ITmfStateSystemBuilder ss = (ITmfStateSystemBuilder) container.getStateSystem();
        try {
            int attributeQuark = getQuarkRelativeAndAdd(ss, info.getQuark(), TmfXmlStrings.STATE, stateName, START_TIME);
            ITmfStateInterval state = ss.querySingleState(ts, attributeQuark);
            return state.getStartTime();
        } catch (StateSystemDisposedException e) {
            Activator.logError("failed the start time of the state " + stateName, e); //$NON-NLS-1$
        }
        return -1l;
    }

    /**
     * Basic quark-retrieving method. Pass an attribute in parameter as an array
     * of strings, the matching quark will be returned. If the attribute does
     * not exist, it will add the quark to the state system if the context
     * allows it.
     *
     * See {@link ITmfStateSystemBuilder#getQuarkAbsoluteAndAdd(String...)}
     *
     * @param ss
     *            The state system the attribute belongs to
     * @param path
     *            Full path to the attribute
     * @return The quark for this attribute
     */
    private static int getQuarkAbsoluteAndAdd(@Nullable ITmfStateSystemBuilder ss, String... path) {
        if (ss == null) {
            throw new NullPointerException(ERROR_MESSAGE);
        }
        return ss.getQuarkAbsoluteAndAdd(path);
    }

    /**
     * Quark-retrieving method, but the attribute is queried starting from the
     * startNodeQuark. If the attribute does not exist, it will add it to the
     * state system if the context allows it.
     *
     * See {@link ITmfStateSystemBuilder#getQuarkRelativeAndAdd(int, String...)}
     *
     ** @param ss
     *            The state system the attribute belongs to
     * @param startNodeQuark
     *            The quark of the attribute from which 'path' originates.
     * @param path
     *            Relative path to the attribute
     * @return The quark for this attribute
     */
    private static int getQuarkRelativeAndAdd(@Nullable ITmfStateSystemBuilder ss, int startNodeQuark, String... path) {
        if (ss == null) {
            throw new NullPointerException(ERROR_MESSAGE);
        }
        return ss.getQuarkRelativeAndAdd(startNodeQuark, path);
    }

    /**
     * Update the scenario internal data
     *
     * @param container
     *            The state system container this class use
     * @param info
     *            The scenario details
     * @param event
     *            The current event
     */
    public void update(final IXmlStateSystemContainer container, final TmfXmlScenarioInfo info, final @Nullable ITmfEvent event) {
        updateScenarioSpecificStateStartTime(event, container, info);
        updateScenarioState(event, container, info);
        updateScenarioStatus(event, container, info);
    }

    private static void updateScenarioStatus(@Nullable ITmfEvent event, IXmlStateSystemContainer container, final TmfXmlScenarioInfo info) {
        ITmfStateSystemBuilder ss = (ITmfStateSystemBuilder) container.getStateSystem();
        long ts = getTimestamp(event, ss);
        ITmfStateValue value;
        try {
            // save the status
            switch (info.getStatus()) {
            case IN_PROGRESS:
                value = STATUS_MAP.get(ScenarioStatusType.IN_PROGRESS);
                break;
            case ABANDONED:
                value = STATUS_MAP.get(ScenarioStatusType.ABANDONED);
                break;
            case MATCHED:
                value = STATUS_MAP.get(ScenarioStatusType.MATCHED);
                break;
            case PENDING:
                value = STATUS_MAP.get(ScenarioStatusType.PENDING);
                break;
            default:
                value = TmfStateValue.nullValue();
                break;
            }
            ss.modifyAttribute(ts, NonNullUtils.checkNotNull(value).unboxValue(), info.getStatusQuark());
        } catch (StateValueTypeException e) {
            Activator.logError("failed to update scenario status"); //$NON-NLS-1$
        }
    }

    private static long getTimestamp(@Nullable ITmfEvent event, @Nullable ITmfStateSystemBuilder ss) {
        if (event != null) {
            return event.getTimestamp().toNanos();
        }
        if (ss != null) {
            return ss.getCurrentEndTime();
        }
        throw new IllegalArgumentException("Event and state system cannot be null at the same time."); //$NON-NLS-1$
    }

    private static void updateScenarioState(final @Nullable ITmfEvent event, final IXmlStateSystemContainer container, final TmfXmlScenarioInfo info) {
        ITmfStateSystemBuilder ss = (ITmfStateSystemBuilder) container.getStateSystem();
        long ts = getTimestamp(event, ss);
        try {
            // save the status
            int attributeQuark = ss.getQuarkRelativeAndAdd(info.getQuark(), TmfXmlStrings.STATE);
            ss.modifyAttribute(ts, info.getActiveState(), attributeQuark);
        } catch (StateValueTypeException e) {
            Activator.logError("failed to update scenario state"); //$NON-NLS-1$
        }
    }

    /**
     * Update the start time of specified state
     *
     * @param event
     *            The current event
     * @param container
     *            The state system container this class use
     * @param info
     *            The scenario details
     */
    private static void updateScenarioSpecificStateStartTime(final @Nullable ITmfEvent event, final IXmlStateSystemContainer container, final TmfXmlScenarioInfo info) {
        ITmfStateSystemBuilder ss = (ITmfStateSystemBuilder) container.getStateSystem();
        long ts = getTimestamp(event, ss);
        try {
            int stateQuark = ss.getQuarkRelativeAndAdd(info.getQuark(), TmfXmlStrings.STATE);
            String activeState = ss.queryOngoingState(stateQuark).unboxStr();
            if (activeState.compareTo(info.getActiveState()) != 0) {
                int attributeQuark = ss.getQuarkRelativeAndAdd(stateQuark, info.getActiveState(), START_TIME);
                ss.modifyAttribute(ts, ts, attributeQuark);
            }
        } catch (StateValueTypeException e) {
            Activator.logError("failed to update the start time of the state"); //$NON-NLS-1$
        }
    }

    /**
     * Start the scenario, sets the start time for the time of the event
     *
     * @param container
     *            The state system container this class use
     * @param info
     *            The scenario details. The value should be null if there is no
     *            scenario
     * @param event
     *            The active event
     */
    public void startScenario(final IXmlStateSystemContainer container, final TmfXmlScenarioInfo info, final ITmfEvent event) {
        ITmfStateSystemBuilder ss = (ITmfStateSystemBuilder) container.getStateSystem();
        long ts = getTimestamp(event, ss);
        try {
            // save the status
            int attributeQuark = ss.getQuarkRelativeAndAdd(info.getQuark(), START_TIME);
            ss.modifyAttribute(ts, ts, attributeQuark);
        } catch (StateValueTypeException e) {
            Activator.logError("failed to update the start time of the scenario"); //$NON-NLS-1$
        }
    }

    /**
     * Set the end time of the scenario to the time of the event, or current
     * state system end time if null, and recycle the attribute quark
     *
     * @param container
     *            The state system container this class use
     * @param info
     *            The scenario details. The value should be null if there is no
     *            scenario
     * @param event
     *            The active event
     */
    public void completeScenario(final IXmlStateSystemContainer container, final TmfXmlScenarioInfo info, final @Nullable ITmfEvent event) {
        ITmfStateSystemBuilder ss = (ITmfStateSystemBuilder) container.getStateSystem();
        long ts = getTimestamp(event, ss);
        TmfAttributePool pool = getPoolFor(container, info.getFsmId());
        pool.recycle(info.getQuark(), ts);
        info.recycleAttributes(ts);
    }
}
