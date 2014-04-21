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

import java.util.List;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.linuxtools.statesystem.core.ITmfStateSystem;
import org.eclipse.linuxtools.statesystem.core.exceptions.AttributeNotFoundException;
import org.eclipse.linuxtools.statesystem.core.exceptions.StateValueTypeException;
import org.eclipse.linuxtools.statesystem.core.exceptions.TimeRangeException;
import org.eclipse.linuxtools.statesystem.core.statevalue.ITmfStateValue;
import org.eclipse.linuxtools.statesystem.core.statevalue.TmfStateValue;
import org.eclipse.linuxtools.tmf.analysis.xml.core.module.IXmlStateSystemContainer;
import org.eclipse.linuxtools.tmf.analysis.xml.core.stateprovider.TmfXmlStrings;
import org.eclipse.linuxtools.tmf.core.event.ITmfEvent;
import org.eclipse.linuxtools.tmf.core.event.ITmfEventField;
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
    private final String fEventField;

    /* Whether this state value is an increment of the previous value */
    private final boolean fIncrement;
    /* Stack value */
    private final ValueTypeStack fStackType;
    /* Forced value type */
    private final ITmfStateValue.Type fForcedType;

    private final IXmlStateSystemContainer fContainer;

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
    protected TmfXmlStateValue(ITmfXmlModelFactory modelFactory, Element node, IXmlStateSystemContainer container, List<ITmfXmlStateAttribute> attributes, String eventField) {
        fPath = attributes;
        fContainer = container;
        fEventField = eventField;
        if (!node.getNodeName().equals(TmfXmlStrings.STATE_VALUE)) {
            throw new IllegalArgumentException("TmfXmlStateValue constructor: Element is not a stateValue"); //$NON-NLS-1$
        }

        /* Check if there is an increment for the value */
        fIncrement = Boolean.parseBoolean(node.getAttribute(TmfXmlStrings.INCREMENT));

        /* Process the XML Element state value */
        fStateValue = initializeStateValue(modelFactory, node);

        /*
         * Forced type allows to convert the value to a certain type : For
         * example, a process's TID in an event field may arrive with a LONG
         * format but we want to store the data in an INT
         */
        switch (node.getAttribute(TmfXmlStrings.FORCED_TYPE)) {
        case TmfXmlStrings.TYPE_STRING:
            fForcedType = ITmfStateValue.Type.STRING;
            break;
        case TmfXmlStrings.TYPE_INT:
            fForcedType = ITmfStateValue.Type.INTEGER;
            break;
        case TmfXmlStrings.TYPE_LONG:
            fForcedType = ITmfStateValue.Type.LONG;
            break;
        default:
            fForcedType = ITmfStateValue.Type.NULL;
        }

        /*
         * Stack Actions : allow to define a stack with PUSH/POP/PEEK methods
         */
        String stack = node.getAttribute(TmfXmlStrings.ATTRIBUTE_STACK);
        fStackType = ValueTypeStack.getTypeFromString(stack);
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
    protected ITmfStateSystem getStateSystem() {
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

    /**
     * Get the current {@link ITmfStateValue} of this state value for an event.
     * It does not increment the value and does not any other processing of the
     * value.
     *
     * @param event
     *            The current event, or <code>null</code> if no event available.
     * @return the {@link ITmfStateValue}
     * @throws AttributeNotFoundException
     *             May be thrown by the state system during the query
     */
    @Override
    public ITmfStateValue getValue(@Nullable ITmfEvent event) throws AttributeNotFoundException {
        return fStateValue.getValue(event);
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
        return getEventFieldValue(event, fEventField);
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
    protected ITmfStateValue getEventFieldValue(@NonNull ITmfEvent event, String fieldName) {

        ITmfStateValue value = TmfStateValue.nullValue();

        final ITmfEventField content = event.getContent();

        /* Exception for "CPU", returns the source of this event */
        /* FIXME : Nameclash if a eventfield have "cpu" for name. */
        if (fieldName.equals(TmfXmlStrings.CPU)) {
            return TmfStateValue.newValueInt(Integer.valueOf(event.getSource()));
        }
        if (content.getField(fieldName) == null) {
            return value;
        }

        Object field = content.getField(fieldName).getValue();

        /*
         * Try to find the right type. The type can be forced by
         * "forcedType" argument.
         */

        if (field instanceof String) {
            String fieldString = (String) field;

            switch (fForcedType) {
            case INTEGER:
                value = TmfStateValue.newValueInt(Integer.parseInt(fieldString));
                break;
            case LONG:
                value = TmfStateValue.newValueLong(Long.parseLong(fieldString));
                break;
            case DOUBLE:
                value = TmfStateValue.newValueDouble(Double.parseDouble(fieldString));
                break;
            case NULL:
            case STRING:
            default:
                value = TmfStateValue.newValueString(fieldString);
                break;
            }
        } else if (field instanceof Long) {
            Long fieldLong = (Long) field;

            switch (fForcedType) {
            case INTEGER:
                value = TmfStateValue.newValueInt(fieldLong.intValue());
                break;
            case STRING:
                value = TmfStateValue.newValueString(fieldLong.toString());
                break;
            case DOUBLE:
                value = TmfStateValue.newValueDouble(fieldLong.doubleValue());
                break;
            case LONG:
            case NULL:
            default:
                value = TmfStateValue.newValueLong(fieldLong);
                break;
            }
        } else if (field instanceof Integer) {
            Integer fieldInteger = (Integer) field;

            switch (fForcedType) {
            case LONG:
                value = TmfStateValue.newValueLong(fieldInteger.longValue());
                break;
            case STRING:
                value = TmfStateValue.newValueString(fieldInteger.toString());
                break;
            case DOUBLE:
                value = TmfStateValue.newValueDouble(fieldInteger.doubleValue());
                break;
            case INTEGER:
            case NULL:
            default:
                value = TmfStateValue.newValueInt(fieldInteger);
                break;
            }
        } else if (field instanceof Double) {
            Double fieldDouble = (Double) field;

            switch (fForcedType) {
            case LONG:
                value = TmfStateValue.newValueLong(fieldDouble.longValue());
                break;
            case STRING:
                value = TmfStateValue.newValueString(fieldDouble.toString());
                break;
            case INTEGER:
                value = TmfStateValue.newValueInt(fieldDouble.intValue());
                break;
            case DOUBLE:
            case NULL:
            default:
                value = TmfStateValue.newValueDouble(fieldDouble);
                break;
            }
        }
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

    /**
     * Handles an event, by setting the value of the attribute described by the
     * state attribute path in the state system.
     *
     * @param event
     *            The event to process
     * @throws AttributeNotFoundException
     *             Pass through the exception it received
     * @throws TimeRangeException
     *             Pass through the exception it received
     * @throws StateValueTypeException
     *             Pass through the exception it received
     */
    @Override
    public void handleEvent(@NonNull ITmfEvent event) throws AttributeNotFoundException, StateValueTypeException, TimeRangeException {
        int quark = IXmlStateSystemContainer.ROOT_QUARK;

        for (ITmfXmlStateAttribute attribute : fPath) {
            quark = attribute.getAttributeQuark(event, quark);
            /* the query is not valid, we stop the state change */
            if (quark == IXmlStateSystemContainer.ERROR_QUARK) {
                throw new AttributeNotFoundException();
            }
        }

        long ts = event.getTimestamp().getValue();
        fStateValue.handleEvent(event, quark, ts);
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
         * @return The state value corresponding to this XML state value
         * @throws AttributeNotFoundException
         *             Pass through the exception it received
         */
        public abstract ITmfStateValue getValue(@Nullable ITmfEvent event) throws AttributeNotFoundException;

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
         * @throws StateValueTypeException
         *             Pass through the exception it received
         * @throws TimeRangeException
         *             Pass through the exception it received
         * @throws AttributeNotFoundException
         *             Pass through the exception it received
         */
        public void handleEvent(@NonNull ITmfEvent event, int quark, long timestamp) throws StateValueTypeException, TimeRangeException, AttributeNotFoundException {
            if (fIncrement) {
                incrementValue(event, quark, timestamp);
            } else {
                ITmfStateValue value = getValue(event);
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
         * @throws StateValueTypeException
         *             Pass through the exception it received
         * @throws TimeRangeException
         *             Pass through the exception it received
         * @throws AttributeNotFoundException
         *             Pass through the exception it received
         */
        @SuppressWarnings("unused")
        protected void incrementValue(ITmfEvent event, int quark, long timestamp) throws StateValueTypeException, TimeRangeException, AttributeNotFoundException {
        }
    }

    /* This state value uses a constant value, defined in the XML */
    private class TmfXmlStateValueNull extends TmfXmlStateValueBase {

        @Override
        public ITmfStateValue getValue(@Nullable ITmfEvent event) throws AttributeNotFoundException {
            return TmfStateValue.nullValue();
        }

    }

}