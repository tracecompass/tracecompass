/*******************************************************************************
 * Copyright (c) 2014, 2016 École Polytechnique de Montréal and others.
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

package org.eclipse.tracecompass.internal.tmf.analysis.xml.ui.views.timegraph;

import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.tracecompass.internal.tmf.analysis.xml.core.model.ITmfXmlModelFactory;
import org.eclipse.tracecompass.internal.tmf.analysis.xml.core.model.ITmfXmlStateAttribute;
import org.eclipse.tracecompass.internal.tmf.analysis.xml.core.model.TmfXmlLocation;
import org.eclipse.tracecompass.internal.tmf.analysis.xml.core.model.readonly.TmfXmlReadOnlyModelFactory;
import org.eclipse.tracecompass.internal.tmf.analysis.xml.core.module.IXmlStateSystemContainer;
import org.eclipse.tracecompass.internal.tmf.analysis.xml.ui.TmfXmlUiStrings;
import org.eclipse.tracecompass.statesystem.core.ITmfStateSystem;
import org.eclipse.tracecompass.statesystem.core.StateSystemUtils;
import org.eclipse.tracecompass.statesystem.core.interval.ITmfStateInterval;
import org.eclipse.tracecompass.tmf.analysis.xml.core.module.TmfXmlUtils;
import org.eclipse.tracecompass.tmf.core.trace.ITmfTrace;
import org.eclipse.tracecompass.tmf.ui.widgets.timegraph.model.ITimeEvent;
import org.eclipse.tracecompass.tmf.ui.widgets.timegraph.model.TimeGraphEntry;
import org.w3c.dom.Element;

/**
 * An XML-defined entry, or row, to display in the XML state system view
 *
 * @author Florian Wininger
 */
public class XmlEntry extends TimeGraphEntry implements IXmlStateSystemContainer, Comparable<XmlEntry> {

    private static final String EMPTY_STRING = ""; //$NON-NLS-1$

    /** Type of resource */
    public static enum EntryDisplayType {
        /** Entries without events to display (filler rows, etc.) */
        NULL,
        /** Entries with time events */
        DISPLAY
    }

    private final @NonNull ITmfTrace fTrace;
    private final EntryDisplayType fType;
    private final int fBaseQuark;
    private final int fDisplayQuark;
    private final String fParentId;
    private final String fId;
    private final @NonNull ITmfStateSystem fSs;
    private final @Nullable Element fElement;

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
    public XmlEntry(int baseQuark, int displayQuark, @NonNull ITmfTrace trace, String name, long startTime, long endTime, EntryDisplayType type, @NonNull ITmfStateSystem ss, Element entryElement) {
        super(name, startTime, endTime);
        fTrace = trace;
        fType = type;
        fBaseQuark = baseQuark;
        fDisplayQuark = displayQuark;
        fSs = ss;
        fElement = entryElement;

        /* Get the parent if specified */
        List<Element> elements = TmfXmlUtils.getChildElements(fElement, TmfXmlUiStrings.PARENT_ELEMENT);
        if (elements.size() > 0) {
            fParentId = getFirstValue(elements.get(0));
        } else {
            fParentId = EMPTY_STRING;
        }

        /* Get the name of this entry */
        elements = TmfXmlUtils.getChildElements(fElement, TmfXmlUiStrings.NAME_ELEMENT);
        if (elements.size() > 0) {
            String nameFromSs = getFirstValue(elements.get(0));
            if (!nameFromSs.isEmpty()) {
                setName(nameFromSs);
            }
        }

        /* Get the id of this entry */
        elements = TmfXmlUtils.getChildElements(fElement, TmfXmlUiStrings.ID_ELEMENT);
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
    public XmlEntry(int baseQuark, @NonNull ITmfTrace trace, String name, @NonNull ITmfStateSystem ss) {
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
        if (stateAttribute == null) {
            throw new IllegalArgumentException();
        }

        ITmfXmlModelFactory factory = TmfXmlReadOnlyModelFactory.getInstance();
        ITmfXmlStateAttribute display = factory.createStateAttribute(stateAttribute, this);
        int quark = display.getAttributeQuark(fBaseQuark, null);
        if (quark != IXmlStateSystemContainer.ERROR_QUARK) {
            ITmfStateInterval firstInterval = StateSystemUtils.queryUntilNonNullValue(fSs, quark, getStartTime(), getEndTime());
            if (firstInterval != null) {
                return firstInterval.getStateValue().toString();
            }
        }
        return EMPTY_STRING;
    }

    /**
     * Get the trace this entry was taken from
     *
     * @return the entry's trace
     */
    public @NonNull ITmfTrace getTrace() {
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

    @Override
    public Iterator<@NonNull ITimeEvent> getTimeEventsIterator() {
        return super.getTimeEventsIterator();
    }

    @Override
    public boolean matches(@NonNull Pattern pattern) {
        if (pattern.matcher(getName()).find()) {
            return true;
        }
        if (pattern.matcher(fId).find()) {
            return true;
        }
        return pattern.matcher(fParentId).find();
    }

    @Override
    public int compareTo(XmlEntry other) {
        // First compare by type
        int cmp = getType().compareTo(other.getType());
        if (cmp != 0) {
            return cmp;
        }
        // For equal type, then compare by element's attribute (to not mix
        // different element's entries)
        Element element = fElement;
        String attrib = (element == null) ? StringUtils.EMPTY : element.getAttribute(TmfXmlUiStrings.PATH);
        element = other.fElement;
        String otherAttrib = (element == null) ? StringUtils.EMPTY : element.getAttribute(TmfXmlUiStrings.PATH);
        cmp = attrib.compareTo(otherAttrib);
        if (cmp != 0) {
            return cmp;
        }
        // Then compare by name
        return getName().compareTo(other.getName());
    }

}
