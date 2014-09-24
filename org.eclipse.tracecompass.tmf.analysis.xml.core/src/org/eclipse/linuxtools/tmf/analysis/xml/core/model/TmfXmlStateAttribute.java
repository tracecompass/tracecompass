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

import java.util.LinkedList;
import java.util.List;

import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.linuxtools.internal.tmf.analysis.xml.core.Activator;
import org.eclipse.linuxtools.statesystem.core.ITmfStateSystem;
import org.eclipse.linuxtools.statesystem.core.ITmfStateSystemBuilder;
import org.eclipse.linuxtools.statesystem.core.exceptions.AttributeNotFoundException;
import org.eclipse.linuxtools.statesystem.core.exceptions.StateValueTypeException;
import org.eclipse.linuxtools.statesystem.core.statevalue.ITmfStateValue;
import org.eclipse.linuxtools.statesystem.core.statevalue.TmfStateValue;
import org.eclipse.linuxtools.tmf.analysis.xml.core.module.IXmlStateSystemContainer;
import org.eclipse.linuxtools.tmf.analysis.xml.core.module.XmlUtils;
import org.eclipse.linuxtools.tmf.analysis.xml.core.stateprovider.TmfXmlStrings;
import org.eclipse.linuxtools.tmf.core.event.ITmfEvent;
import org.eclipse.linuxtools.tmf.core.event.ITmfEventField;
import org.w3c.dom.Element;

/**
 * This Class implements a single attribute value in the XML-defined state
 * system.
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
public abstract class TmfXmlStateAttribute implements ITmfXmlStateAttribute {

    private enum StateAttributeType {
        NONE,
        CONSTANT,
        EVENTFIELD,
        QUERY,
        LOCATION,
        SELF
    }

    /** Type of attribute */
    private final StateAttributeType fType;

    /** Attribute's name */
    private final String fName;

    /** List of attributes for a query */
    private final List<ITmfXmlStateAttribute> fQueryList = new LinkedList<>();

    private final IXmlStateSystemContainer fContainer;

    /**
     * Constructor
     *
     * @param modelFactory
     *            The factory used to create XML model elements
     * @param attribute
     *            XML element of the attribute
     * @param container
     *            The state system container this state attribute belongs to
     */
    protected TmfXmlStateAttribute(ITmfXmlModelFactory modelFactory, Element attribute, IXmlStateSystemContainer container) {
        fContainer = container;

        switch (attribute.getAttribute(TmfXmlStrings.TYPE)) {
        case TmfXmlStrings.TYPE_CONSTANT:
            fType = StateAttributeType.CONSTANT;
            fName = fContainer.getAttributeValue(attribute.getAttribute(TmfXmlStrings.VALUE));
            break;
        case TmfXmlStrings.EVENT_FIELD:
            fType = StateAttributeType.EVENTFIELD;
            fName = fContainer.getAttributeValue(attribute.getAttribute(TmfXmlStrings.VALUE));
            break;
        case TmfXmlStrings.TYPE_LOCATION:
            fType = StateAttributeType.LOCATION;
            fName = fContainer.getAttributeValue(attribute.getAttribute(TmfXmlStrings.VALUE));
            break;
        case TmfXmlStrings.TYPE_QUERY:
            List<Element> childElements = XmlUtils.getChildElements(attribute);
            for (Element subAttributeNode : childElements) {
                ITmfXmlStateAttribute subAttribute = modelFactory.createStateAttribute(subAttributeNode, fContainer);
                fQueryList.add(subAttribute);
            }
            fType = StateAttributeType.QUERY;
            fName = null;
            break;
        case TmfXmlStrings.NULL:
            fType = StateAttributeType.NONE;
            fName = null;
            break;
        case TmfXmlStrings.TYPE_SELF:
            fType = StateAttributeType.SELF;
            fName = null;
            break;
        default:
            throw new IllegalArgumentException("TmfXmlStateAttribute constructor: The XML element is not of the right type"); //$NON-NLS-1$
        }
    }

    /**
     * This method gets the quark for this state attribute in the State System.
     *
     * Unless this attribute is a location, in which case the quark must exist,
     * the quark will be added to the state system if the state system is in
     * builder mode.
     *
     * @param startQuark
     *            root quark, use {@link IXmlStateSystemContainer#ROOT_QUARK} to
     *            search the full attribute tree
     * @return the quark described by attribute or
     *         {@link IXmlStateSystemContainer#ERROR_QUARK} if quark cannot be
     *         found
     */
    @Override
    public int getAttributeQuark(int startQuark) {
        return getAttributeQuark(null, startQuark);
    }

    /**
     * Basic quark-retrieving method. Pass an attribute in parameter as an array
     * of strings, the matching quark will be returned. If the attribute does
     * not exist, it will add the quark to the state system if the context
     * allows it.
     *
     * See {@link ITmfStateSystemBuilder#getQuarkAbsoluteAndAdd(String...)}
     *
     * @param path
     *            Full path to the attribute
     * @return The quark for this attribute
     * @throws AttributeNotFoundException
     *             The attribute does not exist and cannot be added
     */
    protected abstract int getQuarkAbsoluteAndAdd(String... path) throws AttributeNotFoundException;

    /**
     * Quark-retrieving method, but the attribute is queried starting from the
     * startNodeQuark. If the attribute does not exist, it will add it to the
     * state system if the context allows it.
     *
     * See {@link ITmfStateSystemBuilder#getQuarkRelativeAndAdd(int, String...)}
     *
     * @param startNodeQuark
     *            The quark of the attribute from which 'path' originates.
     * @param path
     *            Relative path to the attribute
     * @return The quark for this attribute
     * @throws AttributeNotFoundException
     *             The attribute does not exist and cannot be added
     */
    protected abstract int getQuarkRelativeAndAdd(int startNodeQuark, String... path) throws AttributeNotFoundException;

    /**
     * Get the state system associated with this attribute's container
     *
     * @return The state system associated with this state attribute
     */
    protected ITmfStateSystem getStateSystem() {
        return fContainer.getStateSystem();
    }

    /**
     * This method gets the quark for this state attribute in the State System.
     *
     * Unless this attribute is a location, in which case the quark must exist,
     * the quark will be added to the state system if the state system is in
     * builder mode.
     *
     * @param event
     *            The current event being handled, or <code>null</code> if no
     *            event available in the context
     * @param startQuark
     *            root quark, use {@link IXmlStateSystemContainer#ROOT_QUARK} to
     *            search the full attribute tree
     * @return the quark described by attribute or
     *         {@link IXmlStateSystemContainer#ERROR_QUARK} if quark cannot be
     *         found
     */
    @Override
    public int getAttributeQuark(@Nullable ITmfEvent event, int startQuark) {
        ITmfStateSystem ss = getStateSystem();

        try {
            switch (fType) {
            case CONSTANT: {
                int quark;
                if (startQuark == IXmlStateSystemContainer.ROOT_QUARK) {
                    quark = getQuarkAbsoluteAndAdd(fName);
                } else {
                    quark = getQuarkRelativeAndAdd(startQuark, fName);
                }
                return quark;
            }
            case EVENTFIELD: {
                int quark = IXmlStateSystemContainer.ERROR_QUARK;
                if (event == null) {
                    Activator.logWarning("XML State attribute: looking for an event field, but event is null"); //$NON-NLS-1$
                    return quark;
                }
                /* special case if field is CPU which is not in the field */
                if (fName.equals(TmfXmlStrings.CPU)) {
                    quark = getQuarkRelativeAndAdd(startQuark, event.getSource());
                } else {
                    final ITmfEventField content = event.getContent();
                    /* stop if the event field doesn't exist */
                    if (content.getField(fName) == null) {
                        return IXmlStateSystemContainer.ERROR_QUARK;
                    }

                    Object field = content.getField(fName).getValue();

                    if (field instanceof String) {
                        String fieldString = (String) field;
                        quark = getQuarkRelativeAndAdd(startQuark, fieldString);
                    } else if (field instanceof Long) {
                        Long fieldLong = (Long) field;
                        quark = getQuarkRelativeAndAdd(startQuark, fieldLong.toString());
                    } else if (field instanceof Integer) {
                        Integer fieldInterger = (Integer) field;
                        quark = getQuarkRelativeAndAdd(startQuark, fieldInterger.toString());
                    }
                }
                return quark;
            }
            case QUERY: {
                int quark;
                ITmfStateValue value = TmfStateValue.nullValue();
                int quarkQuery = IXmlStateSystemContainer.ROOT_QUARK;

                for (ITmfXmlStateAttribute attrib : fQueryList) {
                    quarkQuery = attrib.getAttributeQuark(event, quarkQuery);
                    if (quarkQuery == IXmlStateSystemContainer.ERROR_QUARK) {
                        break;
                    }
                }

                // the query may fail: for example CurrentThread if there
                // has not been a sched_switch event
                if (quarkQuery != IXmlStateSystemContainer.ERROR_QUARK) {
                    value = ss.queryOngoingState(quarkQuery);
                }

                switch (value.getType()) {
                case INTEGER: {
                    int result = value.unboxInt();
                    quark = getQuarkRelativeAndAdd(startQuark, String.valueOf(result));
                    break;
                }
                case LONG: {
                    long result = value.unboxLong();
                    quark = getQuarkRelativeAndAdd(startQuark, String.valueOf(result));
                    break;
                }
                case STRING: {
                    String result = value.unboxStr();
                    quark = getQuarkRelativeAndAdd(startQuark, result);
                    break;
                }
                case DOUBLE:
                case NULL:
                default:
                    quark = IXmlStateSystemContainer.ERROR_QUARK; // error
                    break;
                }
                return quark;
            }
            case LOCATION: {
                int quark = startQuark;
                String idLocation = fName;

                /* TODO: Add a fContainer.getLocation(id) method */
                for (TmfXmlLocation location : fContainer.getLocations()) {
                    if (location.getId().equals(idLocation)) {
                        quark = location.getLocationQuark(event, quark);
                        if (quark == IXmlStateSystemContainer.ERROR_QUARK) {
                            break;
                        }
                    }
                }
                return quark;
            }
            case SELF:
                return startQuark;
            case NONE:
            default:
                return startQuark;
            }
        } catch (AttributeNotFoundException ae) {
            /*
             * This can be happen before the creation of the node for a query in
             * the state system. Example : current thread before a sched_switch
             */
            return IXmlStateSystemContainer.ERROR_QUARK;
        } catch (StateValueTypeException e) {
            /*
             * This would happen if we were trying to push/pop attributes not of
             * type integer. Which, once again, should never happen.
             */
            Activator.logError("StateValueTypeException", e); //$NON-NLS-1$
            return IXmlStateSystemContainer.ERROR_QUARK;
        }
    }

    @Override
    public String toString() {
        return "TmfXmlStateAttribute " + fType + ": " + fName; //$NON-NLS-1$ //$NON-NLS-2$
    }

}