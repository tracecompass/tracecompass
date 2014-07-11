/*******************************************************************************
 * Copyright (c) 2014 École Polytechnique de Montréal
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Florian Wininger - Initial API and implementation
 *   Geneviève Bastien - Review of the initial implementation
 *******************************************************************************/

package org.eclipse.linuxtools.tmf.analysis.xml.ui.views.timegraph;

import java.util.Collections;
import java.util.List;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.linuxtools.internal.tmf.analysis.xml.ui.TmfXmlUiStrings;
import org.eclipse.linuxtools.statesystem.core.ITmfStateSystem;
import org.eclipse.linuxtools.statesystem.core.exceptions.AttributeNotFoundException;
import org.eclipse.linuxtools.statesystem.core.exceptions.StateSystemDisposedException;
import org.eclipse.linuxtools.statesystem.core.interval.ITmfStateInterval;
import org.eclipse.linuxtools.tmf.analysis.xml.core.model.ITmfXmlModelFactory;
import org.eclipse.linuxtools.tmf.analysis.xml.core.model.ITmfXmlStateAttribute;
import org.eclipse.linuxtools.tmf.analysis.xml.core.model.TmfXmlLocation;
import org.eclipse.linuxtools.tmf.analysis.xml.core.model.readonly.TmfXmlReadOnlyModelFactory;
import org.eclipse.linuxtools.tmf.analysis.xml.core.module.IXmlStateSystemContainer;
import org.eclipse.linuxtools.tmf.analysis.xml.core.module.XmlUtils;
import org.eclipse.linuxtools.tmf.core.trace.ITmfTrace;
import org.eclipse.linuxtools.tmf.ui.widgets.timegraph.model.TimeGraphEntry;
import org.w3c.dom.Element;

/**
 * An XML-defined entry, or row, to display in the XML state system view
 *
 * @author Florian Wininger
 */
public class XmlEntry extends TimeGraphEntry implements IXmlStateSystemContainer {

    private static final String EMPTY_STRING = ""; //$NON-NLS-1$

    /** Type of resource */
    public static enum EntryDisplayType {
        /** Entries without events to display (filler rows, etc.) */
        NULL,
        /** Entries with time events */
        DISPLAY
    }

    private final ITmfTrace fTrace;
    private final EntryDisplayType fType;
    private final int fBaseQuark;
    private final int fDisplayQuark;
    private final String fParentId;
    private final String fId;
    private final @NonNull ITmfStateSystem fSs;
    private final Element fElement;

    /**
     * Constructor
     *
     * @param baseQuark
     *            The quark matching this entry, or <code>-1</code> if no quark
     * @param displayQuark
     *            The quark containing the value to display. It was needed by
     *            the caller to get the start and end time of this entry, so we
     *            receive it as parameter from him.
     * @param trace
     *            The trace on which we are working (FIXME: is this parameter
     *            useful?)
     * @param name
     *            The name of this entry. It will be overridden if a "name" XML
     *            tag is specified in the entryElement. It will also be used as
     *            the ID of this entry if no "id" XML tag is specified. It
     *            typically is the attribute name corresponding the the base
     *            quark.
     * @param startTime
     *            The start time of this entry lifetime
     * @param endTime
     *            The end time of this entry
     * @param type
     *            The display type of this entry
     * @param ss
     *            The state system this entry belongs to
     * @param entryElement
     *            The XML element describing this entry. This element will be
     *            used to determine, if available, the parent, ID, name and
     *            other display option of this entry
     */
    public XmlEntry(int baseQuark, int displayQuark, ITmfTrace trace, String name, long startTime, long endTime, EntryDisplayType type, @NonNull ITmfStateSystem ss, Element entryElement) {
        super(name, startTime, endTime);
        fTrace = trace;
        fType = type;
        fBaseQuark = baseQuark;
        fDisplayQuark = displayQuark;
        fSs = ss;
        fElement = entryElement;

        /* Get the parent if specified */
        List<Element> elements = XmlUtils.getChildElements(fElement, TmfXmlUiStrings.PARENT_ELEMENT);
        if (elements.size() > 0) {
            fParentId = getFirstValue(elements.get(0));
        } else {
            fParentId = EMPTY_STRING;
        }

        /* Get the name of this entry */
        elements = XmlUtils.getChildElements(fElement, TmfXmlUiStrings.NAME_ELEMENT);
        if (elements.size() > 0) {
            String nameFromSs = getFirstValue(elements.get(0));
            if (!nameFromSs.isEmpty()) {
                setName(nameFromSs);
            }
        }

        /* Get the id of this entry */
        elements = XmlUtils.getChildElements(fElement, TmfXmlUiStrings.ID_ELEMENT);
        if (elements.size() > 0) {
            fId = getFirstValue(elements.get(0));
        } else {
            fId = name;
        }

    }

    /**
     * Constructor
     *
     * @param baseQuark
     *            The quark matching this entry, or <code>-1</code> if no quark
     * @param trace
     *            The trace on which we are working
     * @param name
     *            The exec_name of this entry
     * @param ss
     *            The state system this entry belongs to
     */
    public XmlEntry(int baseQuark, ITmfTrace trace, String name, @NonNull ITmfStateSystem ss) {
        super(name, ss.getStartTime(), ss.getCurrentEndTime());
        fTrace = trace;
        fType = EntryDisplayType.NULL;
        fBaseQuark = baseQuark;
        fDisplayQuark = baseQuark;
        fSs = ss;
        fElement = null;
        fParentId = EMPTY_STRING;
        fId = name;
    }

    /** Return the state value of the first interval with a non-null value */
    private String getFirstValue(Element stateAttribute) {
        ITmfXmlModelFactory factory = TmfXmlReadOnlyModelFactory.getInstance();
        ITmfXmlStateAttribute display = factory.createStateAttribute(stateAttribute, this);
        int quark = display.getAttributeQuark(fBaseQuark);
        if (quark != IXmlStateSystemContainer.ERROR_QUARK) {
            try {
                /* Find the first attribute with a parent */
                List<ITmfStateInterval> execNameIntervals = fSs.queryHistoryRange(quark, getStartTime(), getEndTime());
                for (ITmfStateInterval execNameInterval : execNameIntervals) {

                    if (!execNameInterval.getStateValue().isNull()) {
                        return execNameInterval.getStateValue().toString();
                    }
                }
            } catch (AttributeNotFoundException | StateSystemDisposedException e) {
            }
        }
        return EMPTY_STRING;
    }

    /**
     * Get the trace this entry was taken from
     *
     * @return the entry's trace
     */
    public ITmfTrace getTrace() {
        return fTrace;
    }

    /**
     * Get the entry Type of this entry. Uses the inner EntryDisplayType enum.
     *
     * @return The entry type
     */
    public EntryDisplayType getType() {
        return fType;
    }

    /**
     * Get the quark from which to get the time event intervals for this entry.
     *
     * @return The attribute quark containing the intervals to display
     */
    public int getDisplayQuark() {
        return fDisplayQuark;
    }

    /**
     * Get this entry's ID
     *
     * @return The id of the entry.
     */
    public String getId() {
        return fId;
    }

    /**
     * Return the entry's parent ID. It corresponds to another entry's ID
     * received from the {@link #getId()} method.
     *
     * @return The parent ID of this entry
     */
    public String getParentId() {
        return fParentId;
    }

    @Override
    public boolean hasTimeEvents() {
        if (fType == EntryDisplayType.NULL) {
            return false;
        }
        return true;
    }

    /**
     * Add a child to this entry of type XmlEntry
     *
     * @param entry
     *            The entry to add
     */
    public void addChild(XmlEntry entry) {
        int index;
        for (index = 0; index < getChildren().size(); index++) {
            XmlEntry other = (XmlEntry) getChildren().get(index);
            if (entry.getType().compareTo(other.getType()) < 0) {
                break;
            } else if (entry.getType().equals(other.getType())) {
                if (entry.getName().compareTo(other.getName()) < 0) {
                    break;
                }
            }
        }

        entry.setParent(this);
        addChild(index, entry);
    }

    /**
     * Return the state system this entry is associated to
     *
     * @return The state system, or <code>null</code> if the state system can't
     *         be found.
     */
    @Override
    @NonNull
    public ITmfStateSystem getStateSystem() {
        return fSs;
    }

    @Override
    public String getAttributeValue(String name) {
        return name;
    }

    @Override
    public Iterable<TmfXmlLocation> getLocations() {
        return Collections.EMPTY_SET;
    }

}
