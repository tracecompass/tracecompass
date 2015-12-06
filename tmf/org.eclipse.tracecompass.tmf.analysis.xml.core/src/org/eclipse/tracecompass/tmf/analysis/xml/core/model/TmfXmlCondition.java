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
 *   Naser Ezzati - Add the comparison operators
 *   Patrick Tasse - Add message to exceptions
 ******************************************************************************/

package org.eclipse.tracecompass.tmf.analysis.xml.core.model;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.tracecompass.statesystem.core.ITmfStateSystem;
import org.eclipse.tracecompass.statesystem.core.exceptions.AttributeNotFoundException;
import org.eclipse.tracecompass.statesystem.core.statevalue.ITmfStateValue;
import org.eclipse.tracecompass.tmf.analysis.xml.core.module.IXmlStateSystemContainer;
import org.eclipse.tracecompass.tmf.analysis.xml.core.module.XmlUtils;
import org.eclipse.tracecompass.tmf.analysis.xml.core.stateprovider.TmfXmlStrings;
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
 *   </condition>
 * </and>
 * </pre>
 *
 * @author Florian Wininger
 */
public class TmfXmlCondition {

    private final List<TmfXmlCondition> fConditions = new ArrayList<>();
    private final @Nullable ITmfXmlStateValue fStateValue;
    private final LogicalOperator fOperator;
    private final IXmlStateSystemContainer fContainer;
    private final ConditionOperator fConditionOperator;

    private enum LogicalOperator {
        NONE,
        NOT,
        AND,
        OR,
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

    /**
     * Constructor
     *
     * @param modelFactory
     *            The factory used to create XML model elements
     * @param node
     *            The XML root of this condition
     * @param container
     *            The state system container this condition belongs to
     */
    public TmfXmlCondition(ITmfXmlModelFactory modelFactory, Element node, IXmlStateSystemContainer container) {
        fContainer = container;

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
                throw new IllegalArgumentException("TmfXmlCondition constructor: IF node has no child element"); //$NON-NLS-1$
            }
            rootNode = childElements.get(0);
            childElements = XmlUtils.getChildElements(rootNode);
        }

        if (rootNode == null) {
            throw new IllegalArgumentException();
        }

        switch (rootNode.getNodeName()) {
        case TmfXmlStrings.CONDITION:
            fOperator = LogicalOperator.NONE;
            /* Read comparison type */
            String equationType = rootNode.getAttribute(TmfXmlStrings.OPERATOR);

            switch (equationType) {
            case TmfXmlStrings.EQ:
                fConditionOperator = ConditionOperator.EQ;
                break;
            case TmfXmlStrings.NE:
                fConditionOperator = ConditionOperator.NE;
                break;
            case TmfXmlStrings.GE:
                fConditionOperator = ConditionOperator.GE;
                break;
            case TmfXmlStrings.GT:
                fConditionOperator = ConditionOperator.GT;
                break;
            case TmfXmlStrings.LE:
                fConditionOperator = ConditionOperator.LE;
                break;
            case TmfXmlStrings.LT:
                fConditionOperator = ConditionOperator.LT;
                break;
            case TmfXmlStrings.NULL:
                fConditionOperator = ConditionOperator.EQ;
                break;
            default:
                throw new IllegalArgumentException("TmfXmlCondition: invalid comparison operator."); //$NON-NLS-1$
            }
            /* The last element is a state value node */
            Element stateValueElement = childElements.remove(childElements.size() - 1);
            if (stateValueElement == null) {
                throw new IllegalStateException();
            }

            /*
             * A state value is either preceded by an eventField or a number of
             * state attributes
             */
            @Nullable Element firstChild = childElements.get(0);
            if (firstChild == null) {
                throw new IllegalStateException();
            }

            if (childElements.size() == 1 && firstChild.getNodeName().equals(TmfXmlStrings.ELEMENT_FIELD)) {
                String attribute = firstChild.getAttribute(TmfXmlStrings.NAME);
                fStateValue = modelFactory.createStateValue(stateValueElement, fContainer, attribute);
            } else {
                List<ITmfXmlStateAttribute> attributes = new ArrayList<>();
                for (Element element : childElements) {
                    if (element == null || !element.getNodeName().equals(TmfXmlStrings.STATE_ATTRIBUTE)) {
                        throw new IllegalArgumentException("TmfXmlCondition: a condition either has a eventField element or a number of TmfXmlStateAttribute elements before the state value"); //$NON-NLS-1$
                    }
                    ITmfXmlStateAttribute attribute = modelFactory.createStateAttribute(element, fContainer);
                    attributes.add(attribute);
                }
                fStateValue = modelFactory.createStateValue(stateValueElement, fContainer, attributes);
            }
            break;
        case TmfXmlStrings.NOT:
            fOperator = LogicalOperator.NOT;
            fStateValue = null;
            fConditionOperator = ConditionOperator.NONE;
            Element element = childElements.get(0);
            if (element == null) {
                throw new IllegalArgumentException();
            }
            fConditions.add(modelFactory.createCondition(element, fContainer));
            break;
        case TmfXmlStrings.AND:
            fOperator = LogicalOperator.AND;
            fStateValue = null;
            fConditionOperator = ConditionOperator.NONE;
            for (Element condition : childElements) {
                if (condition == null) {
                    continue;
                }
                fConditions.add(modelFactory.createCondition(condition, fContainer));
            }
            break;
        case TmfXmlStrings.OR:
            fOperator = LogicalOperator.OR;
            fStateValue = null;
            fConditionOperator = ConditionOperator.NONE;
            for (Element condition : childElements) {
                if (condition == null) {
                    continue;
                }
                fConditions.add(modelFactory.createCondition(condition, fContainer));
            }
            break;
        default:
            throw new IllegalArgumentException("TmfXmlCondition constructor: XML node is of the wrong type"); //$NON-NLS-1$
        }
    }

    /**
     * Test the result of the condition for an event
     *
     * @param event
     *            The event on which to test the condition
     * @return Whether the condition is true or not
     * @throws AttributeNotFoundException
     *             The state attribute was not found
     */
    public boolean testForEvent(ITmfEvent event) throws AttributeNotFoundException {
        ITmfStateSystem ss = fContainer.getStateSystem();
        /*
         * The condition is either the equality check of a state value or a
         * boolean operation on other conditions
         */
        if (fStateValue != null) {
            ITmfXmlStateValue filter = fStateValue;
            int quark = IXmlStateSystemContainer.ROOT_QUARK;
            for (ITmfXmlStateAttribute attribute : filter.getAttributes()) {
                quark = attribute.getAttributeQuark(event, quark);
                /*
                 * When verifying a condition, the state attribute must exist,
                 * if it does not, the query is not valid, we stop the condition
                 * check
                 */
                if (quark == IXmlStateSystemContainer.ERROR_QUARK) {
                    throw new AttributeNotFoundException(ss.getSSID() + " Attribute:" + attribute); //$NON-NLS-1$
                }
            }

            /* Get the value to compare to from the XML file */
            ITmfStateValue valueXML;
            valueXML = filter.getValue(event);

            /*
             * The actual value: it can be either queried in the state system or
             * found in the event
             */
            ITmfStateValue valueState = (quark != IXmlStateSystemContainer.ROOT_QUARK) ? ss.queryOngoingState(quark) :
                    filter.getEventFieldValue(event);
            if (valueState == null) {
                throw new IllegalStateException();
            }

            return compare(valueState, valueXML, fConditionOperator);

        } else if (!fConditions.isEmpty()) {
            /* Verify a condition tree */
            switch (fOperator) {
            case AND:
                for (TmfXmlCondition childCondition : fConditions) {
                    if (!childCondition.testForEvent(event)) {
                        return false;
                    }
                }
                return true;
            case NONE:
                break;
            case NOT:
                return !fConditions.get(0).testForEvent(event);
            case OR:
                for (TmfXmlCondition childCondition : fConditions) {
                    if (childCondition.testForEvent(event)) {
                        return true;
                    }
                }
                return false;
            default:
                break;

            }
        } else {
            throw new IllegalStateException("TmfXmlCondition: the condition should be either a state value or be the result of a condition tree"); //$NON-NLS-1$
        }
        return true;
    }

    @Override
    public String toString() {
        return "TmfXmlCondition: " + fOperator + " on " + fConditions; //$NON-NLS-1$ //$NON-NLS-2$
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