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

import org.eclipse.linuxtools.internal.tmf.analysis.xml.core.Activator;
import org.eclipse.linuxtools.tmf.analysis.xml.core.module.XmlUtils;
import org.eclipse.linuxtools.tmf.analysis.xml.core.stateprovider.TmfXmlStrings;
import org.eclipse.linuxtools.tmf.analysis.xml.core.stateprovider.XmlStateProvider;
import org.eclipse.linuxtools.tmf.core.event.ITmfEvent;
import org.eclipse.linuxtools.tmf.core.event.ITmfEventField;
import org.eclipse.linuxtools.tmf.core.exceptions.AttributeNotFoundException;
import org.eclipse.linuxtools.tmf.core.exceptions.StateValueTypeException;
import org.eclipse.linuxtools.tmf.core.statesystem.ITmfStateSystemBuilder;
import org.eclipse.linuxtools.tmf.core.statevalue.ITmfStateValue;
import org.eclipse.linuxtools.tmf.core.statevalue.TmfStateValue;
import org.w3c.dom.Element;

/**
 * This Class implements a single attribute value
 *
 * <pre>
 * Examples:
 * <stateAttribute type="constant" value="Threads" />
 * <stateAttribute type="query" />
 *      <stateAttribute type="constant" value="CPUs" />
 *      <stateAttribute type="eventField" value="cpu" />
 *      <stateAttribute type="constant" value="Current_thread" />
 * </attribute>
 * </pre>
 *
 * @author Florian Wininger
 */
public class TmfXmlStateAttribute {

    private enum StateAttributeType {
        NONE,
        CONSTANT,
        EVENTFIELD,
        QUERY,
        LOCATION
    }

    /** Type of attribute */
    private final StateAttributeType fType;

    /** Attribute's name */
    private final String fName;

    /** List of attributes for a query */
    private final List<TmfXmlStateAttribute> fQueryList = new ArrayList<>();

    private final XmlStateProvider fProvider;

    /**
     * Constructor
     *
     * @param attribute
     *            XML element of the attribute
     * @param provider
     *            The state provider this state attribute belongs to
     */
    public TmfXmlStateAttribute(Element attribute, XmlStateProvider provider) {
        fProvider = provider;

        switch (attribute.getAttribute(TmfXmlStrings.TYPE)) {
        case TmfXmlStrings.TYPE_CONSTANT:
            fType = StateAttributeType.CONSTANT;
            fName = fProvider.getAttributeValue(attribute.getAttribute(TmfXmlStrings.VALUE));
            break;
        case TmfXmlStrings.EVENT_FIELD:
            fType = StateAttributeType.EVENTFIELD;
            fName = fProvider.getAttributeValue(attribute.getAttribute(TmfXmlStrings.VALUE));
            break;
        case TmfXmlStrings.TYPE_LOCATION:
            fType = StateAttributeType.LOCATION;
            fName = fProvider.getAttributeValue(attribute.getAttribute(TmfXmlStrings.VALUE));
            break;
        case TmfXmlStrings.TYPE_QUERY:
            List<Element> childElements = XmlUtils.getChildElements(attribute);
            for (Element subAttributeNode : childElements) {
                TmfXmlStateAttribute subAttribute = new TmfXmlStateAttribute(subAttributeNode, fProvider);
                fQueryList.add(subAttribute);
            }
            fType = StateAttributeType.QUERY;
            fName = null;
            break;
        case TmfXmlStrings.NULL:
            fType = StateAttributeType.NONE;
            fName = null;
            break;
        default:
            throw new IllegalArgumentException("TmfXmlStateAttribute constructor: The XML element is not of the right type"); //$NON-NLS-1$
        }
    }

    /**
     * This method gets the quark for this state attribute in the State System.
     * The method use the ss.getQuarkRelativeAndAdd method in the State System.
     *
     * Unless this attribute is a location, in which case the quark must exist,
     * the quark will be added to the state system.
     *
     * @param event
     *            The current event being handled
     * @param startQuark
     *            root quark, use {@link XmlStateProvider#ROOT_QUARK} to search
     *            the full attribute tree
     * @return the quark described by attribute or
     *         {@link XmlStateProvider#ERROR_QUARK} if quark cannot be found
     */
    public int getAttributeQuark(ITmfEvent event, int startQuark) {
        final ITmfEventField content = event.getContent();

        ITmfStateSystemBuilder ss = fProvider.getAssignedStateSystem();

        try {
            switch (fType) {
            case CONSTANT: {
                int quark;
                if (startQuark == XmlStateProvider.ROOT_QUARK) {
                    quark = ss.getQuarkAbsoluteAndAdd(fName);
                } else {
                    quark = ss.getQuarkRelativeAndAdd(startQuark, fName);
                }
                return quark;
            }
            case EVENTFIELD: {
                int quark = XmlStateProvider.ERROR_QUARK;
                /* special case if field is CPU which is not in the field */
                if (fName.equals(TmfXmlStrings.CPU)) {
                    quark = ss.getQuarkRelativeAndAdd(startQuark, event.getSource());
                } else {
                    /* stop if the event field doesn't exist */
                    if (content.getField(fName) == null) {
                        return XmlStateProvider.ERROR_QUARK;
                    }

                    Object field = content.getField(fName).getValue();

                    if (field instanceof String) {
                        String fieldString = (String) field;
                        quark = ss.getQuarkRelativeAndAdd(startQuark, fieldString);
                    } else if (field instanceof Long) {
                        Long fieldLong = (Long) field;
                        quark = ss.getQuarkRelativeAndAdd(startQuark, fieldLong.toString());
                    } else if (field instanceof Integer) {
                        Integer fieldInterger = (Integer) field;
                        quark = ss.getQuarkRelativeAndAdd(startQuark, fieldInterger.toString());
                    }
                }
                return quark;
            }
            case QUERY: {
                int quark;
                ITmfStateValue value = TmfStateValue.nullValue();
                int quarkQuery = XmlStateProvider.ROOT_QUARK;

                for (TmfXmlStateAttribute attrib : fQueryList) {
                    quarkQuery = attrib.getAttributeQuark(event, quarkQuery);
                    if (quarkQuery == XmlStateProvider.ERROR_QUARK) {
                        break;
                    }
                }

                // the query may fail: for example CurrentThread if there
                // has not been a sched_switch event
                if (quarkQuery != XmlStateProvider.ERROR_QUARK) {
                    value = ss.queryOngoingState(quarkQuery);
                }

                switch (value.getType()) {
                case INTEGER: {
                    int result = value.unboxInt();
                    quark = ss.getQuarkRelativeAndAdd(startQuark, String.valueOf(result));
                    break;
                }
                case LONG: {
                    long result = value.unboxLong();
                    quark = ss.getQuarkRelativeAndAdd(startQuark, String.valueOf(result));
                    break;
                }
                case STRING: {
                    String result = value.unboxStr();
                    quark = ss.getQuarkRelativeAndAdd(startQuark, result);
                    break;
                }
                case DOUBLE:
                case NULL:
                default:
                    quark = XmlStateProvider.ERROR_QUARK; // error
                    break;
                }
                return quark;
            }
            case LOCATION: {
                int quark = startQuark;
                String idLocation = fName;

                /* TODO: Add a fProvider.getLocation(id) method */
                for (TmfXmlLocation location : fProvider.getLocations()) {
                    if (location.getId().equals(idLocation)) {
                        quark = location.getLocationQuark(event, quark);
                        if (quark == XmlStateProvider.ERROR_QUARK) {
                            break;
                        }
                    }
                }
                return quark;
            }
            case NONE:
            default:
                return startQuark;
            }
        } catch (AttributeNotFoundException ae) {
            /*
             * This can be happen before the creation of the node for a query in
             * the state system. Example : current thread before a sched_switch
             */
            return XmlStateProvider.ERROR_QUARK;
        } catch (StateValueTypeException e) {
            /*
             * This would happen if we were trying to push/pop attributes not of
             * type integer. Which, once again, should never happen.
             */
            Activator.logError("StateValueTypeException", e); //$NON-NLS-1$
            return XmlStateProvider.ERROR_QUARK;
        }
    }

}