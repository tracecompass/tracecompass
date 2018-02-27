/*******************************************************************************
 * Copyright (c) 2014, 2015 Ecole Polytechnique de Montreal and others
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Florian Wininger - Initial API and implementation
 *   Naser Ezzati - Add the comparison operators
 *   Patrick Tasse - Add message to exceptions
 *   Jean-Christian Kouame - Add comparison between two state values
 ******************************************************************************/

package org.eclipse.tracecompass.internal.tmf.analysis.xml.core.model;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.tracecompass.common.core.NonNullUtils;
import org.eclipse.tracecompass.internal.tmf.analysis.xml.core.Activator;
import org.eclipse.tracecompass.internal.tmf.analysis.xml.core.module.IXmlStateSystemContainer;
import org.eclipse.tracecompass.internal.tmf.analysis.xml.core.module.XmlUtils;
import org.eclipse.tracecompass.statesystem.core.ITmfStateSystem;
import org.eclipse.tracecompass.statesystem.core.exceptions.AttributeNotFoundException;
import org.eclipse.tracecompass.statesystem.core.statevalue.ITmfStateValue;
import org.eclipse.tracecompass.tmf.analysis.xml.core.module.TmfXmlStrings;
import org.eclipse.tracecompass.tmf.analysis.xml.core.module.TmfXmlUtils;
import org.eclipse.tracecompass.tmf.core.event.ITmfEvent;
import org.w3c.dom.Element;

/**
 * This Class implement a condition tree in the XML-defined state system.
 *
 * <pre>
 * example:
 * <and>
 *   <condition>
 *       <stateAttribute type="location" value="CurrentThread" />
 *       <stateAttribute type="constant" value="System_call" />
 *       <stateValue type="null" />
 *   </condition>
 *   <condition>
 *      <stateValue type="long" value="2" />
 *      <stateValue type="long" value="5" />
 *   </condition>
 * </and>
 * </pre>
 *
 * @author Florian Wininger
 */
public class TmfXmlCondition implements ITmfXmlCondition {

    private final List<TmfXmlCondition> fConditions = new ArrayList<>();
    private final List<ITmfXmlStateValue> fStateValues;
    private final LogicalOperator fOperator;
    private final IXmlStateSystemContainer fContainer;
    private final ConditionOperator fConditionOperator;
    private ConditionType fType;
    private @Nullable TmfXmlTimestampCondition fTimeCondition;

    private enum LogicalOperator {
        NONE,
        NOT,
        AND,
        OR
    }

    private enum ConditionOperator {
        NONE,
        EQ,
        NE,
        GE,
        GT,
        LE,
        LT
    }

    // TODO The XmlCondition needs to be split into several classes of condition
    // instead of using an enum
    private enum ConditionType {
        DATA,
        TIME,
        NONE
    }

    /**
     * Factory to create {@link TmfXmlCondition}
     *
     * @param modelFactory
     *            The factory used to create XML model elements
     * @param node
     *            The XML root of this condition
     * @param container
     *            The state system container this condition belongs to
     * @return The new {@link TmfXmlCondition}
     */
    public static TmfXmlCondition create(ITmfXmlModelFactory modelFactory, Element node, IXmlStateSystemContainer container) {
        Element rootNode = node;
        /* Process the conditions: in each case, only process Element nodes */
        List<@Nullable Element> childElements = XmlUtils.getChildElements(rootNode);

        /*
         * If the node is an if, take the child as the root condition
         *
         * FIXME: Maybe the caller should do this instead.
         */
        if (node.getNodeName().equals(TmfXmlStrings.IF)) {
            if (childElements.isEmpty()) {
                throw new IllegalArgumentException("TmfXmlCondition constructor: IF node with no child element"); //$NON-NLS-1$
            }
            rootNode = NonNullUtils.checkNotNull(childElements.get(0));
            childElements = XmlUtils.getChildElements(rootNode);
        }

        List<@NonNull TmfXmlCondition> conditions = new ArrayList<>();
        switch (rootNode.getNodeName()) {
        case TmfXmlStrings.CONDITION:
            return createPatternCondition(modelFactory, container, rootNode, childElements);
        case TmfXmlStrings.NOT:
            return createMultipleCondition(modelFactory, container, childElements, LogicalOperator.NOT, conditions);
        case TmfXmlStrings.AND:
            return createMultipleCondition(modelFactory, container, childElements, LogicalOperator.AND, conditions);
        case TmfXmlStrings.OR:
            return createMultipleCondition(modelFactory, container, childElements, LogicalOperator.OR, conditions);
        default:
            throw new IllegalArgumentException("TmfXmlCondition constructor: XML node " + rootNode.getNodeName() + " is of the wrong type"); //$NON-NLS-1$ //$NON-NLS-2$
        }
    }

    private static TmfXmlCondition createPatternCondition(ITmfXmlModelFactory modelFactory, IXmlStateSystemContainer container, Element rootNode, List<@Nullable Element> childElements) {
        ArrayList<ITmfXmlStateValue> stateValues;
        ConditionOperator conditionOperator;
        TmfXmlTimestampCondition timeCondition = null;
        int size = TmfXmlUtils.getChildElements(rootNode, TmfXmlStrings.STATE_VALUE).size();
        if (size != 0) {
            stateValues = new ArrayList<>(size);
            if (size == 1) {
                conditionOperator = getConditionOperator(rootNode);
                getStateValuesForXmlCondition(modelFactory, NonNullUtils.checkNotNull(childElements), stateValues, container);
            } else {
                // No need to test if the childElements size is actually 2.
                // The XSD validation do this check already.
                conditionOperator = ConditionOperator.EQ;
                stateValues.add(modelFactory.createStateValue(NonNullUtils.checkNotNull(childElements.get(0)), container, new ArrayList<ITmfXmlStateAttribute>()));
                stateValues.add(modelFactory.createStateValue(NonNullUtils.checkNotNull(childElements.get(1)), container, new ArrayList<ITmfXmlStateAttribute>()));
            }
            return new TmfXmlCondition(ConditionType.DATA, stateValues, LogicalOperator.NONE, conditionOperator, null, new ArrayList<>(), container);
        }
        final Element firstElement = NonNullUtils.checkNotNull(childElements.get(0));
        timeCondition = modelFactory.createTimestampsCondition(firstElement, container);
        return new TmfXmlCondition(ConditionType.TIME, new ArrayList<>(), LogicalOperator.NONE, ConditionOperator.EQ, timeCondition, new ArrayList<>(), container);
    }

    private static TmfXmlCondition createMultipleCondition(ITmfXmlModelFactory modelFactory, IXmlStateSystemContainer container, List<@Nullable Element> childElements, LogicalOperator op,
            List<@NonNull TmfXmlCondition> conditions) {
        for (Element condition : childElements) {
            if (condition == null) {
                continue;
            }
            conditions.add(modelFactory.createCondition(condition, container));
        }
        return new TmfXmlCondition(ConditionType.NONE, new ArrayList<>(), op, ConditionOperator.NONE, null, conditions, container);
    }

    private TmfXmlCondition(ConditionType type, ArrayList<@NonNull ITmfXmlStateValue> stateValues, LogicalOperator operator, ConditionOperator conditionOperator, @Nullable TmfXmlTimestampCondition timeCondition, List<@NonNull TmfXmlCondition> conditions,
            IXmlStateSystemContainer container) {
        fType = type;
        fStateValues = stateValues;
        fOperator = operator;
        fTimeCondition = timeCondition;
        fContainer = container;
        fConditions.addAll(conditions);
        fConditionOperator = conditionOperator;
    }

    private static void getStateValuesForXmlCondition(ITmfXmlModelFactory modelFactory, List<@Nullable Element> childElements, List<ITmfXmlStateValue> stateValues, IXmlStateSystemContainer container) {
        Element stateValueElement = NonNullUtils.checkNotNull(childElements.remove(childElements.size() - 1));
        /*
         * A state value is either preceded by an eventField or a number of
         * state attributes
         */
        final Element firstElement = NonNullUtils.checkNotNull(childElements.get(0));
        if (childElements.size() == 1 && firstElement.getNodeName().equals(TmfXmlStrings.ELEMENT_FIELD)) {
            String attribute = firstElement.getAttribute(TmfXmlStrings.NAME);
            stateValues.add(modelFactory.createStateValue(stateValueElement, container, attribute));
        } else {
            List<ITmfXmlStateAttribute> attributes = new ArrayList<>();
            for (Element element : childElements) {
                if (element == null) {
                    throw new NullPointerException("There should be at list one element"); //$NON-NLS-1$
                }
                if (!element.getNodeName().equals(TmfXmlStrings.STATE_ATTRIBUTE)) {
                    throw new IllegalArgumentException("TmfXmlCondition: a condition either has a eventField element or a number of TmfXmlStateAttribute elements before the state value"); //$NON-NLS-1$
                }
                ITmfXmlStateAttribute attribute = modelFactory.createStateAttribute(element, container);
                attributes.add(attribute);
            }
            stateValues.add(modelFactory.createStateValue(stateValueElement, container, attributes));
        }
    }

    private static ConditionOperator getConditionOperator(Element rootNode) {
        String equationType = rootNode.getAttribute(TmfXmlStrings.OPERATOR);
        switch (equationType) {
        case TmfXmlStrings.EQ:
            return ConditionOperator.EQ;
        case TmfXmlStrings.NE:
            return ConditionOperator.NE;
        case TmfXmlStrings.GE:
            return ConditionOperator.GE;
        case TmfXmlStrings.GT:
            return ConditionOperator.GT;
        case TmfXmlStrings.LE:
            return ConditionOperator.LE;
        case TmfXmlStrings.LT:
            return ConditionOperator.LT;
        case TmfXmlStrings.NULL:
            return ConditionOperator.EQ;
        default:
            throw new IllegalArgumentException("TmfXmlCondition: invalid comparison operator."); //$NON-NLS-1$
        }
    }

    @Override
    public boolean test(ITmfEvent event, @Nullable TmfXmlScenarioInfo scenarioInfo) {
        ITmfStateSystem ss = fContainer.getStateSystem();
        if (fType == ConditionType.DATA) {
            try {
                return testForEvent(event, NonNullUtils.checkNotNull(ss), scenarioInfo);
            } catch (AttributeNotFoundException e) {
                Activator.logError("Attribute not found", e); //$NON-NLS-1$
                return false;
            }
        } else if (fType == ConditionType.TIME) {
            if (fTimeCondition != null) {
                return fTimeCondition.test(event, scenarioInfo);
            }
        } else if (!fConditions.isEmpty()) {
            /* Verify a condition tree */
            switch (fOperator) {
            case AND:
                for (ITmfXmlCondition childCondition : fConditions) {
                    if (!childCondition.test(event, scenarioInfo)) {
                        return false;
                    }
                }
                return true;
            case NONE:
                break;
            case NOT:
                return !fConditions.get(0).test(event, scenarioInfo);
            case OR:
                for (ITmfXmlCondition childCondition : fConditions) {
                    if (childCondition.test(event, scenarioInfo)) {
                        return true;
                    }
                }
                return false;
            default:
                break;

            }
        }
        return true;
    }

    private boolean testForEvent(ITmfEvent event, ITmfStateSystem ss, @Nullable TmfXmlScenarioInfo scenarioInfo) throws AttributeNotFoundException {
        /*
         * The condition is either the equality check of a state value or a
         * boolean operation on other conditions
         */
        if (fStateValues.size() == 1) {
            ITmfXmlStateValue filter = fStateValues.get(0);
            int quark = IXmlStateSystemContainer.ROOT_QUARK;
            for (ITmfXmlStateAttribute attribute : filter.getAttributes()) {
                quark = attribute.getAttributeQuark(event, quark, scenarioInfo);
                /*
                 * When verifying a condition, the state attribute must exist,
                 * if it does not, the query is not valid, we stop the condition
                 * check
                 */
                if (quark == IXmlStateSystemContainer.ERROR_QUARK) {
                    throw new AttributeNotFoundException(ss.getSSID() + " Attribute:" + attribute); //$NON-NLS-1$
                }
            }

            /*
             * The actual value: it can be either queried in the state system or
             * found in the event
             */
            ITmfStateValue valueState = (quark != IXmlStateSystemContainer.ROOT_QUARK) ? ss.queryOngoingState(quark) : filter.getEventFieldValue(event);

            /* Get the value to compare to from the XML file */
            ITmfStateValue valueXML;
            valueXML = filter.getValue(event, scenarioInfo);
            return compare(valueState, valueXML, fConditionOperator);
        }
        /* Get the two values needed for the comparison */
        ITmfStateValue valuesXML1 = fStateValues.get(0).getValue(event, scenarioInfo);
        ITmfStateValue valuesXML2 = fStateValues.get(1).getValue(event, scenarioInfo);
        return valuesXML1.equals(valuesXML2);
    }

    @Override
    public String toString() {
        StringBuilder output = new StringBuilder("TmfXmlCondition: "); //$NON-NLS-1$
        if (fOperator != LogicalOperator.NONE) {
            output.append(fOperator).append(" on ").append(fConditions); //$NON-NLS-1$
        } else {
            output.append(fConditionOperator).append(" {").append(fStateValues.get(0)); //$NON-NLS-1$
            if (fStateValues.size() == 2) {
                output.append(", ").append(fStateValues.get(1)); //$NON-NLS-1$
            }
            output.append("}"); //$NON-NLS-1$
        }
        return output.toString();
    }

    /**
     * Compare two ITmfStateValues based on the given comparison operator
     *
     * @param source
     *            the state value to compare to
     * @param dest
     *            the state value to be compared with
     * @param comparisonOperator
     *            the operator to compare the inputs
     * @return the boolean result of the comparison
     */
    public boolean compare(ITmfStateValue source, ITmfStateValue dest, ConditionOperator comparisonOperator) {
        switch (comparisonOperator) {
        // TODO The comparison operator should have a compareHelper that calls compare
        case EQ:
            return (source.compareTo(dest) == 0);
        case NE:
            return (source.compareTo(dest) != 0);
        case GE:
            return (source.compareTo(dest) >= 0);
        case GT:
            return (source.compareTo(dest) > 0);
        case LE:
            return (source.compareTo(dest) <= 0);
        case LT:
            return (source.compareTo(dest) < 0);
        case NONE:
        default:
            throw new IllegalArgumentException("TmfXmlCondition: invalid comparison operator."); //$NON-NLS-1$
        }
    }
}