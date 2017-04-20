/*******************************************************************************
 * Copyright (c) 2014, 2015 Ecole Polytechnique de Montreal
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Florian Wininger - Initial API and implementation
 ******************************************************************************/

package org.eclipse.tracecompass.internal.tmf.analysis.xml.core.model;

import java.util.List;
import java.util.Set;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.tracecompass.internal.tmf.analysis.xml.core.Activator;
import org.eclipse.tracecompass.internal.tmf.analysis.xml.core.module.IXmlStateSystemContainer;
import org.eclipse.tracecompass.internal.tmf.analysis.xml.core.pattern.stateprovider.XmlPatternStateProvider;
import org.eclipse.tracecompass.internal.tmf.analysis.xml.core.stateprovider.XmlStateProvider;
import org.eclipse.tracecompass.statesystem.core.ITmfStateSystem;
import org.eclipse.tracecompass.statesystem.core.exceptions.AttributeNotFoundException;
import org.eclipse.tracecompass.statesystem.core.exceptions.StateValueTypeException;
import org.eclipse.tracecompass.statesystem.core.exceptions.TimeRangeException;
import org.eclipse.tracecompass.statesystem.core.statevalue.ITmfStateValue;
import org.eclipse.tracecompass.statesystem.core.statevalue.TmfStateValue;
import org.eclipse.tracecompass.tmf.analysis.xml.core.module.TmfXmlStrings;
import org.eclipse.tracecompass.tmf.analysis.xml.core.module.TmfXmlUtils;
import org.eclipse.tracecompass.tmf.core.event.ITmfEvent;
import org.eclipse.tracecompass.tmf.core.event.ITmfEventField;
import org.eclipse.tracecompass.tmf.core.event.aspect.TmfCpuAspect;
import org.eclipse.tracecompass.tmf.core.trace.TmfTraceUtils;
import org.w3c.dom.Element;

/**
 * This Class implements a State Value in the XML-defined state system, along
 * with the path to get to the value (either a list of state attributes or an
 * event field)
 *
 * <pre>
 * Example:
 *   <stateAttribute type="location" value="CurrentThread" />
 *   <stateAttribute type="constant" value="System_call" />
 *   <stateValue type="null" />
 * </pre>
 *
 * @author Florian Wininger
 */
public abstract class TmfXmlStateValue implements ITmfXmlStateValue {

    private final TmfXmlStateValueBase fStateValue;

    /* Path in the State System */
    private final List<ITmfXmlStateAttribute> fPath;
    /* Event field to match with this state value */
    private final @Nullable String fEventField;
    /* State value ID */
    private final @Nullable String fID;
    /* Whether this state value is an increment of the previous value */
    private final boolean fIncrement;
    /* Whether to update the current attribute or create a new state */
    private final boolean fUpdate;
    /* Stack value */
    private final ValueTypeStack fStackType;
    /* Forced value type */
    private final ITmfStateValue.Type fForcedType;

    private final IXmlStateSystemContainer fContainer;

    private final String fMappingGroup;

    /**
     * Different behaviors of an attribute that is to be stacked
     */
    protected enum ValueTypeStack {
        /** Not stacked */
        NULL,
        /** Peek at the value at the top of the stack */
        PEEK,
        /** Take the value at the top of the stack */
        POP,
        /** Pops all the values from the stack */
        POP_ALL,
        /** Push the value on the stack */
        PUSH;

        /**
         * Get the type stack value corresponding to a string
         *
         * @param input
         *            The string to match to a value
         * @return The ValueTypeStack value
         */
        public static ValueTypeStack getTypeFromString(String input) {
            switch (input) {
            case TmfXmlStrings.STACK_PUSH:
                return PUSH;
            case TmfXmlStrings.STACK_POP:
                return POP;
            case TmfXmlStrings.STACK_PEEK:
                return PEEK;
            case TmfXmlStrings.STACK_POPALL:
                return POP_ALL;
            default:
                return NULL;
            }
        }
    }

    /**
     * Constructor
     *
     * @param modelFactory
     *            The factory used to create XML model elements
     * @param node
     *            The state value XML element
     * @param container
     *            The state system container this state value belongs to
     * @param eventField
     *            The event field where to get the value
     * @param attributes
     *            The attributes representing the path to this value
     */
    protected TmfXmlStateValue(ITmfXmlModelFactory modelFactory, Element node, IXmlStateSystemContainer container, List<ITmfXmlStateAttribute> attributes, @Nullable String eventField) {
        fPath = attributes;
        fContainer = container;
        fEventField = eventField;
        if (!node.getNodeName().equals(TmfXmlStrings.STATE_VALUE)) {
            throw new IllegalArgumentException("TmfXmlStateValue constructor: Element is not a stateValue"); //$NON-NLS-1$
        }

        /*
         * fID is set to null if the XML state value element doesn't have an id
         * attribute. If specified it shouldn't be empty
         */
        String id = node.getAttribute(TmfXmlStrings.ID);
        fID = id.isEmpty() ? null : id;

        /* Check if there is an increment for the value */
        fIncrement = Boolean.parseBoolean(node.getAttribute(TmfXmlStrings.INCREMENT));

        /* Check if this value is an update of the ongoing state */
        fUpdate = Boolean.parseBoolean(node.getAttribute(TmfXmlStrings.UPDATE));

        /* Process the XML Element state value */
        fStateValue = initializeStateValue(modelFactory, node);

        /*
         * Forced type allows to convert the value to a certain type : For
         * example, a process's TID in an event field may arrive with a LONG
         * format but we want to store the data in an INT
         */
        String forcedTypeName = node.getAttribute(TmfXmlStrings.FORCED_TYPE);
        fForcedType = forcedTypeName.isEmpty() ? ITmfStateValue.Type.NULL : TmfXmlUtils.getTmfStateValueByName(forcedTypeName);

        /*
         * Stack Actions : allow to define a stack with PUSH/POP/PEEK methods
         */
        String stack = node.getAttribute(TmfXmlStrings.ATTRIBUTE_STACK);
        fStackType = ValueTypeStack.getTypeFromString(stack);

        fMappingGroup = node.getAttribute(TmfXmlStrings.MAPPING_GROUP);
    }

    /**
     * Initialize a {@link TmfXmlStateValueBase} object for the type and value
     *
     * @param modelFactory
     *            The factory used to create XML model elements
     * @param node
     *            The state value XML element
     * @return The internal state value type corresponding to this state value
     */
    protected TmfXmlStateValueBase initializeStateValue(ITmfXmlModelFactory modelFactory, Element node) {
        return new TmfXmlStateValueNull();
    }

    /**
     * Return the state value ID.
     *
     * @return The state value ID
     */
    public @Nullable String getID() {
        return fID;
    }

    /**
     * Return the state system container this class is attached to
     *
     * @return The state system container
     */
    protected IXmlStateSystemContainer getSsContainer() {
        return fContainer;
    }

    /**
     * Get the state system associated with this value's container
     *
     * @return The state system associated with the state system container
     */
    protected @Nullable ITmfStateSystem getStateSystem() {
        return fContainer.getStateSystem();
    }

    /**
     * Return whether this value is an increment of the previous value
     *
     * @return <code>true</code> if the value is an increment
     */
    protected boolean isIncrement() {
        return fIncrement;
    }

    /**
     * Return whether this value should replace the current value of the
     * attribute or if a new state should be created.
     *
     * @return <code>true</code> if the value is to replace the current one
     */
    protected boolean isUpdate() {
        return fUpdate;
    }

    /**
     * Get the stack type of this attribute. If the attribute is to be pushed or
     * popped to a stack. The behavior of the stack attribute will depend on the
     * implementation of the model.
     *
     * @return The stack type of the attribute
     */
    protected ValueTypeStack getStackType() {
        return fStackType;
    }

    /**
     * Get the forced type of the value. For example, if the value obtained from
     * the attributes is not in this forced type, it will be converted to this.
     *
     * @return The desired type of the value
     */
    protected ITmfStateValue.Type getForcedType() {
        return fForcedType;
    }

    @Override
    public ITmfStateValue getValue(@Nullable ITmfEvent event, @Nullable TmfXmlScenarioInfo scenarioInfo) throws AttributeNotFoundException {
        return getMappedValue(event, scenarioInfo, fStateValue.getValue(event, scenarioInfo));
    }

    private ITmfStateValue getMappedValue(@Nullable ITmfEvent event, @Nullable TmfXmlScenarioInfo scenarioInfo, ITmfStateValue value) {
        try {
            Set<TmfXmlMapEntry> group = null;
            if (fContainer instanceof XmlPatternStateProvider) {
                group = ((XmlPatternStateProvider) fContainer).getMappingGroup(fMappingGroup);
            } else if (fContainer instanceof XmlStateProvider) {
                group = ((XmlStateProvider) fContainer).getMappingGroup(fMappingGroup);
            }
            if (group != null) {
                for (TmfXmlMapEntry entry : group) {
                    if (entry.getKey().getValue(event, scenarioInfo).equals(value)) {
                        return entry.getValue().getValue(event, scenarioInfo);
                    }
                }
            }
            return value;
        } catch (AttributeNotFoundException e) {
            Activator.logError("Unable to map the state value"); //$NON-NLS-1$
            // FIXME maybe we should return the raw state value instead of a
            // null state value
            return TmfStateValue.nullValue();
        }
    }

    /**
     * Get the value of the event field that is the path of this state value
     *
     * @param event
     *            The current event
     * @return the value of the event field
     */
    @Override
    public ITmfStateValue getEventFieldValue(@NonNull ITmfEvent event) {
        String eventField = fEventField;
        if (eventField == null) {
            throw new IllegalStateException();
        }
        return getEventFieldValue(event, eventField);
    }

    /**
     * Get the value of an event field
     *
     * @param event
     *            The current event
     * @param fieldName
     *            The name of the field of which to get the value
     * @return The value of the event field
     */
    protected ITmfStateValue getEventFieldValue(ITmfEvent event, String fieldName) {

        ITmfStateValue value = TmfStateValue.nullValue();

        final ITmfEventField field = event.getContent().getField(fieldName);

        Object fieldValue = null;

        /* If the field does not exist, see if it's a special case */
        if (field == null) {
            if (fieldName.equalsIgnoreCase(TmfXmlStrings.CPU)) {
                /* A "CPU" field will return the CPU aspect if available */
                Integer cpu = TmfTraceUtils.resolveIntEventAspectOfClassForEvent(event.getTrace(), TmfCpuAspect.class, event);
                if (cpu != null) {
                    return TmfStateValue.newValueInt(cpu.intValue());
                }
            } else if (fieldName.equalsIgnoreCase(TmfXmlStrings.TIMESTAMP)) {
                /*
                 * Exception also for "TIMESTAMP", returns the timestamp of this
                 * event
                 */
                return TmfStateValue.newValueLong(event.getTimestamp().getValue());
            } else if (fieldName.equalsIgnoreCase(TmfXmlStrings.HOSTID)) {
                /* Return the host ID of the trace containing the event */
                return TmfStateValue.newValueString(event.getTrace().getHostId());
            }
            // This will allow to use any column as input
            fieldValue = TmfTraceUtils.resolveAspectOfNameForEvent(event.getTrace(), fieldName, event);
            if (fieldValue == null) {
                return value;
            }
        } else {
            fieldValue = field.getValue();
        }

        /*
         * Try to find the right type. The type can be forced by
         * "forcedType" argument.
         */
        value = TmfXmlUtils.newTmfStateValueFromObjectWithForcedType(fieldValue, fForcedType);

        return value;
    }

    /**
     * Get the list of state attributes, the path to the state value
     *
     * @return the list of Attribute to have the path in the State System
     */
    @Override
    public List<ITmfXmlStateAttribute> getAttributes() {
        return fPath;
    }

    @Override
    public void handleEvent(@NonNull ITmfEvent event, @Nullable TmfXmlScenarioInfo scenarioInfo) throws AttributeNotFoundException, StateValueTypeException, TimeRangeException {
        int quark = IXmlStateSystemContainer.ROOT_QUARK;

        for (ITmfXmlStateAttribute attribute : fPath) {
            quark = attribute.getAttributeQuark(event, quark, scenarioInfo);
            /* the query is not valid, we stop the state change */
            if (quark == IXmlStateSystemContainer.ERROR_QUARK) {
                Activator.logError("Not found XML attribute " + attribute); //$NON-NLS-1$
                return;
            }
        }

        long ts = event.getTimestamp().getValue();
        fStateValue.handleEvent(event, quark, ts, scenarioInfo);
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder("TmfXmlStateValue: "); //$NON-NLS-1$
        if (fEventField != null) {
            builder.append("Field=").append(fEventField).append("; "); //$NON-NLS-1$ //$NON-NLS-2$
        } else if (!fPath.isEmpty()) {
            builder.append("Path=").append(fPath).append("; "); //$NON-NLS-1$ //$NON-NLS-2$
        }
        builder.append(fStateValue);
        return builder.toString();
    }

    /**
     * Base class for all state values. Contain default methods to handle event,
     * process or increment the value
     */
    protected abstract class TmfXmlStateValueBase {

        /**
         * Get the value associated with this state value.
         *
         * @param event
         *            The event which can be used to retrieve the value if
         *            necessary. The event can be <code>null</code> if no event
         *            is required.
         * @param scenarioInfo
         *            The active scenario details. The value should be null if
         *            there no scenario.
         * @return The state value corresponding to this XML state value
         * @throws AttributeNotFoundException
         *             Pass through the exception it received
         */
        public abstract ITmfStateValue getValue(@Nullable ITmfEvent event, @Nullable TmfXmlScenarioInfo scenarioInfo) throws AttributeNotFoundException;

        /**
         * Do something with the state value, possibly using an event
         *
         * @param event
         *            The event being handled. If there is no event is
         *            available, use <code>null</code>.
         * @param quark
         *            The quark for this value
         * @param timestamp
         *            The timestamp of the event
         * @param scenarioInfo
         *            The active scenario details. The value should be null if
         *            there no scenario.
         * @throws StateValueTypeException
         *             Pass through the exception it received
         * @throws TimeRangeException
         *             Pass through the exception it received
         * @throws AttributeNotFoundException
         *             Pass through the exception it received
         */
        public void handleEvent(ITmfEvent event, int quark, long timestamp, @Nullable TmfXmlScenarioInfo scenarioInfo) throws StateValueTypeException, TimeRangeException, AttributeNotFoundException {
            if (fIncrement) {
                incrementValue(event, quark, timestamp, scenarioInfo);
            } else {
                ITmfStateValue value = getValue(event, scenarioInfo);
                processValue(quark, timestamp, value);
            }
        }

        /**
         * Set the value of a quark at a given timestamp.
         *
         * @param quark
         *            The quark for this value
         * @param timestamp
         *            The timestamp
         * @param value
         *            The value of this state value
         * @throws TimeRangeException
         *             Pass through the exception it received
         * @throws StateValueTypeException
         *             Pass through the exception it received
         * @throws AttributeNotFoundException
         *             Pass through the exception it received
         */
        @SuppressWarnings("unused")
        protected void processValue(int quark, long timestamp, ITmfStateValue value) throws TimeRangeException, StateValueTypeException, AttributeNotFoundException {
        }

        /**
         * Increments the value of the parameter
         *
         * @param event
         *            The event being handled
         * @param quark
         *            The quark for this value
         * @param timestamp
         *            The timestamp of the event
         * @param scenarioInfo
         *            The active scenario details. The value should be null if
         *            there no scenario.
         * @throws StateValueTypeException
         *             Pass through the exception it received
         * @throws TimeRangeException
         *             Pass through the exception it received
         * @throws AttributeNotFoundException
         *             Pass through the exception it received
         */
        @SuppressWarnings("unused")
        protected void incrementValue(ITmfEvent event, int quark, long timestamp, @Nullable TmfXmlScenarioInfo scenarioInfo) throws StateValueTypeException, TimeRangeException, AttributeNotFoundException {
        }
    }

    /* This state value uses a constant value, defined in the XML */
    private class TmfXmlStateValueNull extends TmfXmlStateValueBase {

        @Override
        public ITmfStateValue getValue(@Nullable ITmfEvent event, @Nullable TmfXmlScenarioInfo scenarioInfo) throws AttributeNotFoundException {
            return TmfStateValue.nullValue();
        }

        @Override
        public String toString() {
            return "NULL"; //$NON-NLS-1$
        }
    }

}
