/*******************************************************************************
 * Copyright (c) 2014 École Polytechnique de Montréal
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Geneviève Bastien - Initial API and implementation
 *******************************************************************************/

package org.eclipse.tracecompass.tmf.analysis.xml.core.model.readwrite;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.tracecompass.internal.tmf.analysis.xml.core.Activator;
import org.eclipse.tracecompass.statesystem.core.ITmfStateSystem;
import org.eclipse.tracecompass.statesystem.core.ITmfStateSystemBuilder;
import org.eclipse.tracecompass.statesystem.core.StateSystemBuilderUtils;
import org.eclipse.tracecompass.statesystem.core.exceptions.AttributeNotFoundException;
import org.eclipse.tracecompass.statesystem.core.exceptions.StateValueTypeException;
import org.eclipse.tracecompass.statesystem.core.exceptions.TimeRangeException;
import org.eclipse.tracecompass.statesystem.core.statevalue.ITmfStateValue;
import org.eclipse.tracecompass.statesystem.core.statevalue.TmfStateValue;
import org.eclipse.tracecompass.tmf.analysis.xml.core.model.ITmfXmlModelFactory;
import org.eclipse.tracecompass.tmf.analysis.xml.core.model.ITmfXmlStateAttribute;
import org.eclipse.tracecompass.tmf.analysis.xml.core.model.TmfXmlStateValue;
import org.eclipse.tracecompass.tmf.analysis.xml.core.module.IXmlStateSystemContainer;
import org.eclipse.tracecompass.tmf.analysis.xml.core.module.XmlUtils;
import org.eclipse.tracecompass.tmf.analysis.xml.core.stateprovider.TmfXmlStrings;
import org.eclipse.tracecompass.tmf.core.event.ITmfEvent;
import org.w3c.dom.Element;

/**
 * Implements a state value in a read write mode. See {@link TmfXmlStateValue}
 * for the syntax of the state value.
 *
 * In read/write mode, a state value can be considered as an assignation where
 * the state value is assigned to the quark represented by the state attributes
 *
 * @author Geneviève Bastien
 */
public class TmfXmlReadWriteStateValue extends TmfXmlStateValue {

    private static final String ILLEGAL_STATE_EXCEPTION_MESSAGE = "The state system hasn't been initialized yet"; //$NON-NLS-1$

    /**
     * Constructor where the path to the value is a list of state attributes
     *
     * @param modelFactory
     *            The factory used to create XML model elements
     * @param node
     *            The state value XML element
     * @param container
     *            The state system container this state value belongs to
     * @param attributes
     *            The attributes representing the path to this value
     */
    public TmfXmlReadWriteStateValue(TmfXmlReadWriteModelFactory modelFactory, Element node, IXmlStateSystemContainer container, List<ITmfXmlStateAttribute> attributes) {
        this(modelFactory, node, container, attributes, null);
    }

    /**
     * Constructor where the path to the value is an event field
     *
     * @param modelFactory
     *            The factory used to create XML model elements
     * @param node
     *            The state value XML element
     * @param container
     *            The state system container this state value belongs to
     * @param eventField
     *            The event field where to get the value
     */
    public TmfXmlReadWriteStateValue(TmfXmlReadWriteModelFactory modelFactory, Element node, IXmlStateSystemContainer container, String eventField) {
        this(modelFactory, node, container, new ArrayList<ITmfXmlStateAttribute>(), eventField);
    }

    private TmfXmlReadWriteStateValue(ITmfXmlModelFactory modelFactory, Element node, IXmlStateSystemContainer container, List<ITmfXmlStateAttribute> attributes, @Nullable String eventField) {
        super(modelFactory, node, container, attributes, eventField);
    }

    @Override
    protected @Nullable ITmfStateSystemBuilder getStateSystem() {
        return (ITmfStateSystemBuilder) super.getStateSystem();
    }

    @Override
    protected TmfXmlStateValueBase initializeStateValue(ITmfXmlModelFactory modelFactory, Element node) {
        TmfXmlStateValueBase stateValueType = null;
        /* Process the XML Element state value */
        String type = node.getAttribute(TmfXmlStrings.TYPE);
        String value = getSsContainer().getAttributeValue(node.getAttribute(TmfXmlStrings.VALUE));
        if (value == null) {
            throw new IllegalStateException();
        }

        switch (type) {
        case TmfXmlStrings.TYPE_INT: {
            /* Integer value */
            ITmfStateValue stateValue = TmfStateValue.newValueInt(Integer.parseInt(value));
            stateValueType = new TmfXmlStateValueTmf(stateValue);
            break;
        }
        case TmfXmlStrings.TYPE_LONG: {
            /* Long value */
            ITmfStateValue stateValue = TmfStateValue.newValueLong(Long.parseLong(value));
            stateValueType = new TmfXmlStateValueTmf(stateValue);
            break;
        }
        case TmfXmlStrings.TYPE_STRING: {
            /* String value */
            ITmfStateValue stateValue = TmfStateValue.newValueString(value);
            stateValueType = new TmfXmlStateValueTmf(stateValue);
            break;
        }
        case TmfXmlStrings.TYPE_NULL: {
            /* Null value */
            ITmfStateValue stateValue = TmfStateValue.nullValue();
            stateValueType = new TmfXmlStateValueTmf(stateValue);
            break;
        }
        case TmfXmlStrings.EVENT_FIELD:
            /* Event field */
            stateValueType = new TmfXmlStateValueEventField(value);
            break;
        case TmfXmlStrings.TYPE_EVENT_NAME:
            /* The value is the event name */
            stateValueType = new TmfXmlStateValueEventName();
            break;
        case TmfXmlStrings.TYPE_DELETE:
            /* Deletes the value of an attribute */
            stateValueType = new TmfXmlStateValueDelete();
            break;
        case TmfXmlStrings.TYPE_QUERY:
            /* Value is the result of a query */
            List<@Nullable Element> children = XmlUtils.getChildElements(node);
            List<ITmfXmlStateAttribute> childAttributes = new ArrayList<>();
            for (Element child : children) {
                if (child == null) {
                    continue;
                }
                ITmfXmlStateAttribute queryAttribute = modelFactory.createStateAttribute(child, getSsContainer());
                childAttributes.add(queryAttribute);
            }
            stateValueType = new TmfXmlStateValueQuery(childAttributes);
            break;
        default:
            throw new IllegalArgumentException(String.format("TmfXmlStateValue constructor: unexpected element %s for stateValue type", type)); //$NON-NLS-1$
        }
        return stateValueType;
    }

    // ----------------------------------------------------------
    // Internal state value classes for the different types
    // ----------------------------------------------------------

    /**
     * Base class for all state value. Contain default methods to handle event,
     * process or increment the value
     */
    protected abstract class TmfXmlStateValueTypeReadWrite extends TmfXmlStateValueBase {

        @Override
        public final void handleEvent(ITmfEvent event, int quark, long timestamp) throws StateValueTypeException, TimeRangeException, AttributeNotFoundException {
            if (isIncrement()) {
                incrementValue(event, quark, timestamp);
            } else {
                ITmfStateValue value = getValue(event);
                processValue(quark, timestamp, value);
            }
        }

        @Override
        protected void processValue(int quark, long timestamp, ITmfStateValue value) throws AttributeNotFoundException, TimeRangeException, StateValueTypeException {
            ITmfStateSystemBuilder ss = getStateSystem();
            if (ss == null) {
                throw new IllegalStateException(ILLEGAL_STATE_EXCEPTION_MESSAGE);
            }
            switch (getStackType()) {
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

        @Override
        protected void incrementValue(ITmfEvent event, int quark, long timestamp) throws StateValueTypeException, TimeRangeException, AttributeNotFoundException {
            ITmfStateSystemBuilder ss = getStateSystem();
            if (ss == null) {
                throw new IllegalStateException(ILLEGAL_STATE_EXCEPTION_MESSAGE);
            }
            StateSystemBuilderUtils.incrementAttributeInt(ss, timestamp, quark, 1);
        }
    }

    private static @Nullable ITmfStateValue incrementByType(int quark, ITmfStateSystem ss, ITmfStateValue stateValue) throws AttributeNotFoundException {
        ITmfStateValue value = null;
        switch (stateValue.getType()) {
        case LONG: {
            long incrementLong = stateValue.unboxLong();
            ITmfStateValue currentState = ss.queryOngoingState(quark);
            long currentValue = (currentState.isNull() ? 0 : currentState.unboxLong());
            value = TmfStateValue.newValueLong(incrementLong + currentValue);
            return value;
        }
        case INTEGER: {
            int increment = stateValue.unboxInt();
            ITmfStateValue currentState = ss.queryOngoingState(quark);
            int currentValue = (currentState.isNull() ? 0 : currentState.unboxInt());
            value = TmfStateValue.newValueInt(increment + currentValue);
            return value;
        }
        case DOUBLE:
        case NULL:
        case STRING:
        default:
        }
        return value;
    }

    /* This state value uses a constant value, defined in the XML */
    private class TmfXmlStateValueTmf extends TmfXmlStateValueTypeReadWrite {

        private final ITmfStateValue fValue;

        public TmfXmlStateValueTmf(ITmfStateValue value) {
            fValue = value;
        }

        @Override
        public ITmfStateValue getValue(@Nullable ITmfEvent event) {
            return fValue;
        }

        @Override
        public void incrementValue(ITmfEvent event, int quark, long timestamp) throws StateValueTypeException, TimeRangeException, AttributeNotFoundException {
            ITmfStateSystem ss = getStateSystem();
            if (ss == null) {
                throw new IllegalStateException(ILLEGAL_STATE_EXCEPTION_MESSAGE);
            }
            ITmfStateValue value = incrementByType(quark, ss, fValue);
            if (value != null) {
                processValue(quark, timestamp, value);
            } else {
                Activator.logWarning("TmfXmlStateValue: The increment value is not a number type"); //$NON-NLS-1$
            }
        }

        @Override
        public String toString() {
            return "Value=" + fValue; //$NON-NLS-1$
        }

    }

    /* The state value uses the value of an event field */
    private class TmfXmlStateValueEventField extends TmfXmlStateValueTypeReadWrite {

        private final String fFieldName;

        public TmfXmlStateValueEventField(String field) {
            fFieldName = field;
        }

        @Override
        public ITmfStateValue getValue(@Nullable ITmfEvent event) {
            if (event == null) {
                Activator.logWarning("XML State value: requested an event field, but event is null"); //$NON-NLS-1$
                return TmfStateValue.nullValue();
            }
            return getEventFieldValue(event, fFieldName);
        }

        @Override
        public void incrementValue(ITmfEvent event, int quark, long timestamp) throws StateValueTypeException, TimeRangeException, AttributeNotFoundException {
            ITmfStateSystem ss = getSsContainer().getStateSystem();
            if (ss == null) {
                throw new IllegalStateException(ILLEGAL_STATE_EXCEPTION_MESSAGE);
            }
            ITmfStateValue incrementValue = getValue(event);
            ITmfStateValue value = incrementByType(quark, ss, incrementValue);
            if (value != null) {
                processValue(quark, timestamp, value);
            } else {
                Activator.logWarning(String.format("TmfXmlStateValue: The event field increment %s is not a number type but a %s", fFieldName, incrementValue.getType())); //$NON-NLS-1$
            }
        }

        @Override
        public String toString() {
            return "Event Field=" + fFieldName; //$NON-NLS-1$
        }
    }

    /* The state value is the event name */
    private class TmfXmlStateValueEventName extends TmfXmlStateValueTypeReadWrite {

        @Override
        public ITmfStateValue getValue(@Nullable ITmfEvent event) {
            if (event == null) {
                Activator.logWarning("XML State value: request event name, but event is null"); //$NON-NLS-1$
                return TmfStateValue.nullValue();
            }
            return TmfStateValue.newValueString(event.getName());
        }

        @Override
        public String toString() {
            return "Event name"; //$NON-NLS-1$
        }

    }

    /* The state value deletes an attribute */
    private class TmfXmlStateValueDelete extends TmfXmlStateValueTypeReadWrite {

        @Override
        public ITmfStateValue getValue(@Nullable ITmfEvent event) throws AttributeNotFoundException {
            return TmfStateValue.nullValue();
        }

        @Override
        protected void processValue(int quark, long timestamp, ITmfStateValue value) throws TimeRangeException, AttributeNotFoundException {
            ITmfStateSystem ss = getStateSystem();
            if (!(ss instanceof ITmfStateSystemBuilder)) {
                throw new IllegalStateException("incrementValue should never be called when not building the state system"); //$NON-NLS-1$
            }
            ITmfStateSystemBuilder builder = (ITmfStateSystemBuilder) ss;
            builder.removeAttribute(timestamp, quark);
        }

        @Override
        public String toString() {
            return "Delete"; //$NON-NLS-1$
        }
    }

    /* The state value uses the result of a query */
    private class TmfXmlStateValueQuery extends TmfXmlStateValueTypeReadWrite {

        private final List<ITmfXmlStateAttribute> fQueryValue;

        public TmfXmlStateValueQuery(List<ITmfXmlStateAttribute> childAttributes) {
            fQueryValue = childAttributes;
        }

        @Override
        public ITmfStateValue getValue(@Nullable ITmfEvent event) throws AttributeNotFoundException {
            /* Query the state system for the value */
            ITmfStateValue value = TmfStateValue.nullValue();
            int quarkQuery = IXmlStateSystemContainer.ROOT_QUARK;
            ITmfStateSystem ss = getStateSystem();
            if (ss == null) {
                throw new IllegalStateException(ILLEGAL_STATE_EXCEPTION_MESSAGE);
            }

            for (ITmfXmlStateAttribute attribute : fQueryValue) {
                quarkQuery = attribute.getAttributeQuark(event, quarkQuery);
                if (quarkQuery == IXmlStateSystemContainer.ERROR_QUARK) {
                    /* the query is not valid, we stop the state change */
                    break;
                }
            }
            /*
             * the query can fail : for example, if a value is requested but has
             * not been set yet
             */
            if (quarkQuery != IXmlStateSystemContainer.ERROR_QUARK) {
                value = ss.queryOngoingState(quarkQuery);
            }
            return value;
        }

        @Override
        public void incrementValue(ITmfEvent event, int quark, long timestamp) throws StateValueTypeException, TimeRangeException, AttributeNotFoundException {
            ITmfStateSystem ss = getStateSystem();
            if (ss == null) {
                throw new IllegalStateException(ILLEGAL_STATE_EXCEPTION_MESSAGE);
            }

            ITmfStateValue incrementValue = getValue(event);
            ITmfStateValue value = incrementByType(quark, ss, incrementValue);
            if (value != null) {
                processValue(quark, timestamp, value);
            } else {
                Activator.logWarning("TmfXmlStateValue: The query result increment is not a number type"); //$NON-NLS-1$
            }
        }

        @Override
        public String toString() {
            return "Query=" + fQueryValue; //$NON-NLS-1$
        }
    }

}
