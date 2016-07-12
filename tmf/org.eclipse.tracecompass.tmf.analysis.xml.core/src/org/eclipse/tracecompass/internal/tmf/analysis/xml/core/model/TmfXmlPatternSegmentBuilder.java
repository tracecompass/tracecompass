/*******************************************************************************
 * Copyright (c) 2016 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.eclipse.tracecompass.internal.tmf.analysis.xml.core.model;

import static org.eclipse.tracecompass.common.core.NonNullUtils.checkNotNull;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.tracecompass.internal.tmf.analysis.xml.core.Activator;
import org.eclipse.tracecompass.internal.tmf.analysis.xml.core.module.IXmlStateSystemContainer;
import org.eclipse.tracecompass.internal.tmf.analysis.xml.core.pattern.stateprovider.XmlPatternStateProvider;
import org.eclipse.tracecompass.internal.tmf.analysis.xml.core.segment.TmfXmlPatternSegment;
import org.eclipse.tracecompass.internal.tmf.analysis.xml.core.stateprovider.TmfXmlStrings;
import org.eclipse.tracecompass.statesystem.core.exceptions.AttributeNotFoundException;
import org.eclipse.tracecompass.statesystem.core.exceptions.StateValueTypeException;
import org.eclipse.tracecompass.statesystem.core.statevalue.ITmfStateValue;
import org.eclipse.tracecompass.statesystem.core.statevalue.TmfStateValue;
import org.eclipse.tracecompass.tmf.core.event.ITmfEvent;
import org.eclipse.tracecompass.tmf.core.timestamp.ITmfTimestamp;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

/**
 * This class defines a pattern segment builder. It will use the XML description
 * of the pattern segment to generate it at runtime.
 *
 * @author Jean-Christian Kouame
 */
public class TmfXmlPatternSegmentBuilder {

    /**
     * The string unknown
     */
    public static final String UNKNOWN_STRING = "unknown"; //$NON-NLS-1$
    /**
     * Prefix for the pattern segment name
     */
    public static final String PATTERN_SEGMENT_NAME_PREFIX = "seg_"; //$NON-NLS-1$
    private final ITmfXmlModelFactory fModelFactory;
    private final IXmlStateSystemContainer fContainer;
    private final List<TmfXmlPatternSegmentField> fFields = new ArrayList<>();
    private final TmfXmlPatternSegmentType fSegmentType;

    /**
     * @param modelFactory
     *            The factory used to create XML model elements
     * @param node
     *            XML element of the pattern segment builder
     * @param parent
     *            The state system container this pattern segment builder
     *            belongs to
     */
    public TmfXmlPatternSegmentBuilder(ITmfXmlModelFactory modelFactory, Element node, IXmlStateSystemContainer parent) {
        fModelFactory = modelFactory;
        fContainer = parent;

        //Set the XML type of the segment
        NodeList nodesSegmentType = node.getElementsByTagName(TmfXmlStrings.SEGMENT_TYPE);
        Element element = (Element) nodesSegmentType.item(0);
        if (element == null) {
            throw new IllegalArgumentException();
        }
        fSegmentType = new TmfXmlPatternSegmentType(element);

        //Set the XML content of the segment
        NodeList nodesSegmentContent = node.getElementsByTagName(TmfXmlStrings.SEGMENT_CONTENT);
        Element fContentElement = (Element) nodesSegmentContent.item(0);
        if (fContentElement != null) {
            NodeList nodesSegmentField = fContentElement.getElementsByTagName(TmfXmlStrings.SEGMENT_FIELD);
            for (int i = 0; i < nodesSegmentField.getLength(); i++) {
                fFields.add(new TmfXmlPatternSegmentField(checkNotNull((Element) nodesSegmentField.item(i))));
            }
        }
    }

    /**
     * Generate a pattern segment
     *
     * @param event
     *            The active event
     * @param start
     *            Start time of the pattern segment to generate
     * @param end
     *            End time of the pattern segment to generate
     * @param scenarioInfo
     *            The active scenario details. Or <code>null</code> if there is
     *            no scenario.
     * @return The pattern segment generated
     */
    public TmfXmlPatternSegment generatePatternSegment(ITmfEvent event, ITmfTimestamp start, ITmfTimestamp end, @Nullable TmfXmlScenarioInfo scenarioInfo) {
        int scale = event.getTimestamp().getScale();
        long startValue = start.toNanos();
        long endValue = end.toNanos();
        String segmentName = getPatternSegmentName(event, scenarioInfo);
        Map<String, ITmfStateValue> fields = new HashMap<>();
        setPatternSegmentContent(event, fields, scenarioInfo);
        TmfXmlPatternSegment segment = new TmfXmlPatternSegment(startValue, endValue, scale, segmentName, fields);
        if (fContainer instanceof XmlPatternStateProvider) {
            ((XmlPatternStateProvider) fContainer).getListener().onNewSegment(segment);
        }
        return segment;
    }

    /**
     * Get the pattern segment name
     *
     * @param event
     *            The active event
     * @param scenarioInfo
     *            The active scenario details. Or <code>null</code> if there is
     *            no scenario.
     * @return The name of the segment
     */
    private String getPatternSegmentName(ITmfEvent event, @Nullable TmfXmlScenarioInfo scenarioInfo) {
        return fSegmentType.getName(event, scenarioInfo);
    }

    /**
     * Compute all the fields and their values for this pattern segment. The
     * fields could be constant values or values queried from the state system.
     *
     * @param event
     *            The current event
     * @param fields
     *            The map that will contained all the fields
     * @param scenarioInfo
     *            The active scenario details. Or <code>null</code> if there is
     *            no scenario.
     */
    private void setPatternSegmentContent(ITmfEvent event, Map<String, ITmfStateValue> fields, @Nullable TmfXmlScenarioInfo scenarioInfo) {
        for (TmfXmlPatternSegmentField field : fFields) {
            fields.put(field.getName().intern(), field.getValue(event, scenarioInfo));
        }
        if (scenarioInfo != null) {
            addStoredFieldsContent(event, fields, scenarioInfo);
        }
    }

    /**
     * Query the stored fields path and add them to the content of the pattern
     * segment. This is specific to pattern analysis.
     *
     * @param event
     *            The active event
     * @param fields
     *            The segment fields
     * @param info
     *            The active scenario details
     */
    protected void addStoredFieldsContent(ITmfEvent event, Map<String, ITmfStateValue> fields, final TmfXmlScenarioInfo info) {
        if (fContainer instanceof XmlPatternStateProvider) {
            for (Entry<String, String> entry : ((XmlPatternStateProvider) fContainer).getStoredFields().entrySet()) {
                ITmfStateValue value = ((XmlPatternStateProvider) fContainer).getHistoryBuilder().getStoredFieldValue(fContainer, entry.getValue(), info, event);
                if (!value.isNull()) {
                    fields.put(entry.getValue().intern(), value);
                }
            }
        }
    }

    private static ITmfStateValue getStateValueFromConstant(String constantValue, String type) {
        switch (type) {
        case TmfXmlStrings.TYPE_INT:
            return TmfStateValue.newValueInt(Integer.parseInt(constantValue));
        case TmfXmlStrings.TYPE_LONG:
            return TmfStateValue.newValueLong(Long.parseLong(constantValue));
        case TmfXmlStrings.TYPE_STRING:
            return TmfStateValue.newValueString(constantValue);
        case TmfXmlStrings.TYPE_NULL:
            return TmfStateValue.nullValue();
        default:
            throw new IllegalArgumentException("Invalid type of field : " + type); //$NON-NLS-1$
        }
    }

    private static void getNameFromXmlStateValue(ITmfEvent event, StringBuilder builder, ITmfXmlStateValue xmlStateValue, @Nullable TmfXmlScenarioInfo scenarioInfo) {
        try {
            ITmfStateValue value = xmlStateValue.getValue(event, scenarioInfo);
            switch (value.getType()) {
            case DOUBLE:
                builder.append(value.unboxDouble());
                break;
            case INTEGER:
                builder.append(value.unboxInt());
                break;
            case LONG:
                builder.append(value.unboxLong());
                break;
            case NULL:
                builder.append(UNKNOWN_STRING);
                break;
            case STRING:
                builder.append(value.unboxStr());
                break;
            case CUSTOM:
            default:
                throw new StateValueTypeException("Invalid type of state value"); //$NON-NLS-1$
            }
        } catch (AttributeNotFoundException e) {
            Activator.logInfo("Impossible to get the state value", e); //$NON-NLS-1$
        }
    }

    /**
     * This class represents the segment fields described in the XML. The real
     * value of the field will be set at runtime using the active event.
     *
     * @author Jean-Christian Kouame
     *
     */
    private class TmfXmlPatternSegmentField {
        private final String fName;
        private final String fType;
        private final @Nullable ITmfStateValue fStateValue;
        private final @Nullable ITmfXmlStateValue fXmlStateValue;

        /**
         * Constructor
         *
         * @param element
         *            The pattern segment field node
         */
        public TmfXmlPatternSegmentField(Element element) {
            // The name, the type and the value of each field could respectively
            // be found from the attributes name, type and value. If the value
            // attribute is not available, try to find it from the child state
            // value.
            fName = element.getAttribute(TmfXmlStrings.NAME);
            fType = element.getAttribute(TmfXmlStrings.TYPE);
            String constantValue = element.getAttribute(TmfXmlStrings.VALUE);
            if (constantValue.isEmpty() && !fType.equals(TmfXmlStrings.TYPE_NULL)) {
                fStateValue = null;
                Element elementFieldStateValue = (Element) element.getElementsByTagName(TmfXmlStrings.STATE_VALUE).item(0);
                if (elementFieldStateValue == null) {
                    throw new IllegalArgumentException("The value of the field " + fName + " is missing"); //$NON-NLS-1$ //$NON-NLS-2$
                }
                fXmlStateValue = fModelFactory.createStateValue(elementFieldStateValue, fContainer, new ArrayList<>());
            } else {
                fStateValue = getStateValueFromConstant(constantValue, fType);
                fXmlStateValue = null;
            }
        }

        /**
         * Get the real value of the XML pattern segment field
         *
         * @param event
         *            The active event
         * @return The state value representing the value of the XML pattern
         *         segment field
         * @param scenarioInfo
         *            The active scenario details. Or <code>null</code> if there
         *            is no scenario.
         */
        public ITmfStateValue getValue(ITmfEvent event, @Nullable TmfXmlScenarioInfo scenarioInfo) {
            if (fStateValue != null) {
                return fStateValue;
            }
            try {
                return checkNotNull(fXmlStateValue).getValue(event, scenarioInfo);
            } catch (AttributeNotFoundException e) {
                Activator.logError("Failed to get the state value", e); //$NON-NLS-1$
            }
            throw new IllegalStateException("Failed to get the value for the segment field " + fName); //$NON-NLS-1$
        }

        /**
         * Get the name of the XML pattern segment field
         *
         * @return The name
         */
        public String getName() {
            return fName;
        }
    }

    /**
     * This class represents the segment type described in XML.
     *
     * @author Jean-Christian Kouame
     *
     */
    private class TmfXmlPatternSegmentType {
        private final String fSegmentNameAttribute;
        private final @Nullable ITmfXmlStateValue fNameStateValue;

        /**
         * Constructor
         *
         * @param element
         *            The pattern segment type node
         */
        public TmfXmlPatternSegmentType(Element element) {
            // Try to find the segment name from the name attribute. If
            // attribute not available, try to find it from the child state value
            fSegmentNameAttribute = element.getAttribute(TmfXmlStrings.SEGMENT_NAME);
            if (!fSegmentNameAttribute.isEmpty()) {
                fNameStateValue = null;
            } else {
                Element elementSegmentNameStateValue = (Element) element.getElementsByTagName(TmfXmlStrings.STATE_VALUE).item(0);
                if (elementSegmentNameStateValue == null) {
                    throw new IllegalArgumentException("Failed to get the segment name. A state value is needed."); //$NON-NLS-1$
                }
                fNameStateValue = fModelFactory.createStateValue(elementSegmentNameStateValue, fContainer, new ArrayList<>());
            }
        }

        /**
         * Get the name of the segment
         *
         * @param event
         *            The active event
         * @param scenarioInfo
         *            The active scenario details. Or <code>null</code> if there
         *            is no scenario.
         * @return The segment name
         */
        public String getName(ITmfEvent event, @Nullable TmfXmlScenarioInfo scenarioInfo) {
            StringBuilder name = new StringBuilder(PATTERN_SEGMENT_NAME_PREFIX);
            if (fNameStateValue != null) {
                getNameFromXmlStateValue(event, name, fNameStateValue, scenarioInfo);
            } else {
                name.append(fSegmentNameAttribute);
            }
            return name.toString().intern();
        }
    }
}
