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

package org.eclipse.linuxtools.internal.tmf.analysis.xml.core.stateprovider.model;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.linuxtools.internal.tmf.analysis.xml.core.Activator;
import org.eclipse.linuxtools.tmf.analysis.xml.core.module.XmlUtils;
import org.eclipse.linuxtools.tmf.analysis.xml.core.stateprovider.TmfXmlStrings;
import org.eclipse.linuxtools.tmf.analysis.xml.core.stateprovider.XmlStateProvider;
import org.eclipse.linuxtools.tmf.core.event.ITmfEvent;
import org.eclipse.linuxtools.tmf.core.event.ITmfEventField;
import org.eclipse.linuxtools.tmf.core.exceptions.AttributeNotFoundException;
import org.eclipse.linuxtools.tmf.core.exceptions.StateValueTypeException;
import org.eclipse.linuxtools.tmf.core.exceptions.TimeRangeException;
import org.eclipse.linuxtools.tmf.core.statesystem.ITmfStateSystem;
import org.eclipse.linuxtools.tmf.core.statesystem.ITmfStateSystemBuilder;
import org.eclipse.linuxtools.tmf.core.statevalue.ITmfStateValue;
import org.eclipse.linuxtools.tmf.core.statevalue.TmfStateValue;
import org.w3c.dom.Element;

/**
 * This Class implements a State Value in the XML state provider, along with the
 * path to get to the value (either a list of state attributes or an event
 * field)
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
public class TmfXmlStateValue {

    private final TmfXmlStateValueType fStateValue;

    /* Path in the State System */
    private final List<TmfXmlStateAttribute> fPath;
    /* Event field to match with this state value */
    private final String fEventField;

    /* Whether this state value is an increment of the previous value */
    private final boolean fIncrement;
    /* Stack value */
    private final ValueTypeStack fStack;
    /* Forced value type */
    private final ValueForcedType fForcedType;

    private final XmlStateProvider fProvider;

    private enum ValueTypeStack {
        NULL,
        PEEK,
        POP,
        PUSH,
    }

    private enum ValueForcedType {
        NULL,
        STRING,
        INT,
        LONG
    }

    /**
     * Constructor where the path to the value is a list of state attributes
     *
     * @param node
     *            The state value XML element
     * @param provider
     *            The state provider this state value belongs to
     * @param attributes
     *            The attributes representing the path to this value
     */
    public TmfXmlStateValue(Element node, XmlStateProvider provider, List<TmfXmlStateAttribute> attributes) {
        this(node, provider, attributes, null);
        if (attributes.isEmpty()) {
            throw new IllegalArgumentException("TmfXmlStateValue constructor: the list of attributes should not be empty"); //$NON-NLS-1$
        }
    }

    /**
     * Constructor where the path to the value is an event field
     *
     * @param node
     *            The state value XML element
     * @param provider
     *            The state provider this state value belongs to
     * @param eventField
     *            The event field where to get the value
     */
    public TmfXmlStateValue(Element node, XmlStateProvider provider, String eventField) {
        this(node, provider, new ArrayList<TmfXmlStateAttribute>(), eventField);
    }

    private TmfXmlStateValue(Element node, XmlStateProvider provider, List<TmfXmlStateAttribute> attributes, String eventField) {
        fPath = attributes;
        fProvider = provider;
        fEventField = eventField;
        if (!node.getNodeName().equals(TmfXmlStrings.STATE_VALUE)) {
            throw new IllegalArgumentException("TmfXmlStateValue constructor: Element is not a stateValue"); //$NON-NLS-1$
        }

        /* Process the XML Element state value */
        String type = node.getAttribute(TmfXmlStrings.TYPE);
        String value = fProvider.getAttributeValue(node.getAttribute(TmfXmlStrings.VALUE));
        /* Check if there is an increment for the value */
        fIncrement = Boolean.valueOf(node.getAttribute(TmfXmlStrings.INCREMENT));

        switch (type) {
        case TmfXmlStrings.TYPE_INT: {
            /* Integer value */
            ITmfStateValue stateValue = TmfStateValue.newValueInt(Integer.parseInt(value));
            fStateValue = new TmfXmlStateValueTmf(stateValue);
            break;
        }
        case TmfXmlStrings.TYPE_LONG: {
            /* Long value */
            ITmfStateValue stateValue = TmfStateValue.newValueLong(Long.parseLong(value));
            fStateValue = new TmfXmlStateValueTmf(stateValue);
            break;
        }
        case TmfXmlStrings.TYPE_STRING: {
            /* String value */
            ITmfStateValue stateValue = TmfStateValue.newValueString(value);
            fStateValue = new TmfXmlStateValueTmf(stateValue);
            break;
        }
        case TmfXmlStrings.TYPE_NULL: {
            /* Null value */
            ITmfStateValue stateValue = TmfStateValue.nullValue();
            fStateValue = new TmfXmlStateValueTmf(stateValue);
            break;
        }
        case TmfXmlStrings.EVENT_FIELD:
            /* Event field */
            fStateValue = new TmfXmlStateValueEventField(value);
            break;
        case TmfXmlStrings.TYPE_EVENT_NAME:
            /* The value is the event name */
            fStateValue = new TmfXmlStateValueEventName();
            break;
        case TmfXmlStrings.TYPE_DELETE:
            /* Deletes the value of an attribute */
            fStateValue = new TmfXmlStateValueDelete();
            break;
        case TmfXmlStrings.TYPE_QUERY:
            /* Value is the result of a query */
            List<Element> children = XmlUtils.getChildElements(node);
            List<TmfXmlStateAttribute> childAttributes = new ArrayList<>();
            for (Element child : children) {
                TmfXmlStateAttribute queryAttribute = new TmfXmlStateAttribute(child, fProvider);
                childAttributes.add(queryAttribute);
            }
            fStateValue = new TmfXmlStateValueQuery(childAttributes);
            break;
        default:
            throw new IllegalArgumentException(String.format("TmfXmlStateValue constructor: unexpected element %s for stateValue type", type)); //$NON-NLS-1$
        }

        /*
         * Forced type allows to convert the value to a certain type : For
         * example, a process's TID in an event field may arrive with a LONG
         * format but we want to store the data in an INT
         */
        switch (node.getAttribute(TmfXmlStrings.FORCED_TYPE)) {
        case TmfXmlStrings.TYPE_STRING:
            fForcedType = ValueForcedType.STRING;
            break;
        case TmfXmlStrings.TYPE_INT:
            fForcedType = ValueForcedType.INT;
            break;
        case TmfXmlStrings.TYPE_LONG:
            fForcedType = ValueForcedType.LONG;
            break;
        default:
            fForcedType = ValueForcedType.NULL;
        }

        /*
         * Stack Actions : allow to define a stack with PUSH/POP/PEEK methods
         */
        String stack = node.getAttribute(TmfXmlStrings.ATTRIBUTE_STACK);
        switch (stack) {
        case TmfXmlStrings.STACK_PUSH:
            fStack = ValueTypeStack.PUSH;
            break;
        case TmfXmlStrings.STACK_POP:
            fStack = ValueTypeStack.POP;
            break;
        case TmfXmlStrings.STACK_PEEK:
            fStack = ValueTypeStack.PEEK;
            break;
        default:
            fStack = ValueTypeStack.NULL;
        }
    }

    /**
     * Get the current {@link ITmfStateValue} of this state value for an event.
     * It does not increment the value and does not any other processing of the
     * value.
     *
     * @param event
     *            The current event.
     * @return the {@link ITmfStateValue}
     * @throws AttributeNotFoundException
     *             May be thrown by the state system during the query
     */
    public ITmfStateValue getValue(ITmfEvent event)
            throws AttributeNotFoundException {
        return fStateValue.getValue(event);

    }

    /**
     * Get the value of the event field that is the path of this state value
     *
     * @param event
     *            The current event
     * @return the value of the event field
     */
    public ITmfStateValue getEventField(@NonNull ITmfEvent event) {
        return getEventField(event, fEventField);
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
    private ITmfStateValue getEventField(ITmfEvent event, String fieldName) {

        ITmfStateValue value = TmfStateValue.nullValue();

        final ITmfEventField content = event.getContent();

        /* Exception for "CPU", returns the source of this event */
        /* FIXME : Nameclash if a eventfield have "cpu" for name. */
        if (fieldName.equals(TmfXmlStrings.CPU)) {
            value = TmfStateValue.newValueInt(Integer.valueOf(event.getSource()));
        } else {

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
                case INT:
                    value = TmfStateValue.newValueInt(Integer.parseInt(fieldString));
                    break;
                case LONG:
                    value = TmfStateValue.newValueLong(Long.parseLong(fieldString));
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
                case INT:
                    value = TmfStateValue.newValueInt(fieldLong.intValue());
                    break;
                case STRING:
                    value = TmfStateValue.newValueString(fieldLong.toString());
                    break;
                case LONG:
                case NULL:
                default:
                    value = TmfStateValue.newValueLong(fieldLong);
                    break;
                }
            } else if (field instanceof Integer) {
                Integer fieldInterger = (Integer) field;

                switch (fForcedType) {
                case LONG:
                    value = TmfStateValue.newValueLong(fieldInterger.longValue());
                    break;
                case STRING:
                    value = TmfStateValue.newValueString(fieldInterger.toString());
                    break;
                case INT:
                case NULL:
                default:
                    break;

                }
            }
        }
        return value;
    }

    /**
     * Get the list of state attributes, the path to the state value
     *
     * @return the list of Attribute to have the path in the State System
     */
    public List<TmfXmlStateAttribute> getAttributes() {
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
    public void handleEvent(ITmfEvent event) throws AttributeNotFoundException, StateValueTypeException, TimeRangeException {
        int quark = XmlStateProvider.ROOT_QUARK;

        for (TmfXmlStateAttribute attribute : fPath) {
            quark = attribute.getAttributeQuark(event, quark);
            /* the query is not valid, we stop the state change */
            if (quark == XmlStateProvider.ERROR_QUARK) {
                throw new AttributeNotFoundException();
            }
        }

        long ts = event.getTimestamp().getValue();
        fStateValue.handleEvent(event, quark, ts);
    }

    /*
     * Base class for all state value. Contain default methods to handle event,
     * process or increment the value
     */
    private abstract class TmfXmlStateValueType {

        public abstract ITmfStateValue getValue(ITmfEvent event) throws AttributeNotFoundException;

        /**
         * Execute the state change
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
        public final void handleEvent(ITmfEvent event, int quark, long timestamp) throws StateValueTypeException, TimeRangeException, AttributeNotFoundException {
            if (fIncrement) {
                incrementValue(event, quark, timestamp);
            } else {
                ITmfStateValue value = getValue(event);
                processValue(quark, timestamp, value);
            }
        }

        protected void processValue(int quark, long timestamp, ITmfStateValue value) throws AttributeNotFoundException, TimeRangeException, StateValueTypeException {
            ITmfStateSystemBuilder ss = fProvider.getAssignedStateSystem();
            switch (fStack) {
            case POP:
                ss.popAttribute(timestamp, quark);
                break;
            case PUSH:
                ss.pushAttribute(timestamp, value, quark);
                break;
            case NULL:
            case PEEK:
            default:
                ss.modifyAttribute(timestamp, value, quark);
                break;
            }
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
        protected void incrementValue(ITmfEvent event, int quark, long timestamp) throws StateValueTypeException, TimeRangeException, AttributeNotFoundException {
            ITmfStateSystemBuilder ss = fProvider.getAssignedStateSystem();
            ss.incrementAttribute(timestamp, quark);
        }
    }

    /* This state value uses a constant value, defined in the XML */
    private class TmfXmlStateValueTmf extends TmfXmlStateValueType {

        private final ITmfStateValue fValue;

        public TmfXmlStateValueTmf(ITmfStateValue value) {
            fValue = value;
        }

        @Override
        public ITmfStateValue getValue(ITmfEvent event) {
            return fValue;
        }

        @Override
        public void incrementValue(ITmfEvent event, int quark, long timestamp) throws StateValueTypeException, TimeRangeException, AttributeNotFoundException {
            ITmfStateSystemBuilder ss = fProvider.getAssignedStateSystem();
            switch (fValue.getType()) {
            case LONG: {
                long incrementLong = fValue.unboxLong();
                long currentValue = ss.queryOngoingState(quark).unboxLong();
                ITmfStateValue value = TmfStateValue.newValueLong(incrementLong + currentValue);
                processValue(quark, timestamp, value);
                return;
            }
            case INTEGER: {
                int increment = fValue.unboxInt();
                int currentValue = ss.queryOngoingState(quark).unboxInt();
                ITmfStateValue value = TmfStateValue.newValueInt(increment + currentValue);
                processValue(quark, timestamp, value);
                break;
            }
            case DOUBLE:
            case NULL:
            case STRING:
            default:
                Activator.logWarning("TmfXmlStateValue: The increment value is not a number type"); //$NON-NLS-1$
                break;
            }
        }
    }

    /* The state value uses the value of an event field */
    private class TmfXmlStateValueEventField extends TmfXmlStateValueType {

        private final String fFieldName;

        public TmfXmlStateValueEventField(String field) {
            fFieldName = field;
        }

        @Override
        public ITmfStateValue getValue(ITmfEvent event) {
            return getEventField(event, fFieldName);
        }

        @Override
        public void incrementValue(ITmfEvent event, int quark, long timestamp) throws StateValueTypeException, TimeRangeException, AttributeNotFoundException {
            ITmfStateSystemBuilder ss = fProvider.getAssignedStateSystem();
            ITmfStateValue incrementValue = getValue(event);
            switch (incrementValue.getType()) {
            case INTEGER: {
                int increment = incrementValue.unboxInt();
                int currentValue = ss.queryOngoingState(quark).unboxInt();
                ITmfStateValue value = TmfStateValue.newValueInt(increment + currentValue);
                processValue(quark, timestamp, value);
                break;
            }
            case LONG: {
                long incrementLong = incrementValue.unboxLong();
                long currentValue = ss.queryOngoingState(quark).unboxLong();
                ITmfStateValue value = TmfStateValue.newValueLong(incrementLong + currentValue);
                processValue(quark, timestamp, value);
                break;
            }
            case DOUBLE:
            case NULL:
            case STRING:
            default:
                Activator.logWarning(String.format("TmfXmlStateValue: The event field increment %s is not a number type but a %s", fFieldName, incrementValue.getType())); //$NON-NLS-1$
                break;
            }
        }
    }

    /* The state value is the event name */
    private class TmfXmlStateValueEventName extends TmfXmlStateValueType {

        @Override
        public ITmfStateValue getValue(ITmfEvent event) {
            return TmfStateValue.newValueString(event.getType().getName());
        }

    }

    /* The state value deletes an attribute */
    private class TmfXmlStateValueDelete extends TmfXmlStateValueType {

        @Override
        public ITmfStateValue getValue(ITmfEvent event) throws AttributeNotFoundException {
            return TmfStateValue.nullValue();
        }

        @Override
        protected void processValue(int quark, long timestamp, ITmfStateValue value) throws TimeRangeException, AttributeNotFoundException {
            ITmfStateSystemBuilder ss = fProvider.getAssignedStateSystem();
            ss.removeAttribute(timestamp, quark);
        }

    }

    /* The state value uses the result of a query */
    private class TmfXmlStateValueQuery extends TmfXmlStateValueType {

        private final List<TmfXmlStateAttribute> fQueryValue;

        public TmfXmlStateValueQuery(List<TmfXmlStateAttribute> attributes) {
            fQueryValue = attributes;
        }

        @Override
        public ITmfStateValue getValue(ITmfEvent event) throws AttributeNotFoundException {
            /* Query the state system for the value */
            ITmfStateValue value = TmfStateValue.nullValue();
            int quarkQuery = XmlStateProvider.ROOT_QUARK;
            ITmfStateSystem ss = fProvider.getAssignedStateSystem();

            for (TmfXmlStateAttribute attribute : fQueryValue) {
                quarkQuery = attribute.getAttributeQuark(event, quarkQuery);
                if (quarkQuery == XmlStateProvider.ERROR_QUARK) {
                    /* the query is not valid, we stop the state change */
                    break;
                }
            }
            /*
             * the query can fail : for example, if a value is requested but has
             * not been set yet
             */
            if (quarkQuery != XmlStateProvider.ERROR_QUARK) {
                value = ss.queryOngoingState(quarkQuery);
            }
            return value;
        }

        @Override
        public void incrementValue(ITmfEvent event, int quark, long timestamp) throws StateValueTypeException, TimeRangeException, AttributeNotFoundException {
            ITmfStateSystem ss = fProvider.getAssignedStateSystem();
            ITmfStateValue incrementValue = getValue(event);
            switch (incrementValue.getType()) {
            case INTEGER: {
                int increment = incrementValue.unboxInt();
                int currentValue = ss.queryOngoingState(quark).unboxInt();
                ITmfStateValue value = TmfStateValue.newValueInt(increment + currentValue);
                processValue(quark, timestamp, value);
                break;
            }
            case LONG: {
                long incrementLong = incrementValue.unboxLong();
                long currentValue = ss.queryOngoingState(quark).unboxLong();
                ITmfStateValue value = TmfStateValue.newValueLong(incrementLong + currentValue);
                processValue(quark, timestamp, value);
                break;
            }
            case DOUBLE:
            case NULL:
            case STRING:
            default:
                Activator.logWarning("TmfXmlStateValue: The query result increment is not a number type"); //$NON-NLS-1$
                break;
            }
        }
    }
}