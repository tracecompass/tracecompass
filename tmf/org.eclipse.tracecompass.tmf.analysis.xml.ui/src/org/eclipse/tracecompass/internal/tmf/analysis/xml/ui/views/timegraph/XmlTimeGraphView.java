/*******************************************************************************
 * Copyright (c) 2014, 2016 École Polytechnique de Montréal
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

import static org.eclipse.tracecompass.common.core.NonNullUtils.checkNotNull;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.tracecompass.common.core.NonNullUtils;
import org.eclipse.tracecompass.internal.tmf.analysis.xml.core.model.ITmfXmlModelFactory;
import org.eclipse.tracecompass.internal.tmf.analysis.xml.core.model.ITmfXmlStateAttribute;
import org.eclipse.tracecompass.internal.tmf.analysis.xml.core.model.readonly.TmfXmlReadOnlyModelFactory;
import org.eclipse.tracecompass.internal.tmf.analysis.xml.core.module.IXmlStateSystemContainer;
import org.eclipse.tracecompass.internal.tmf.analysis.xml.core.module.XmlUtils;
import org.eclipse.tracecompass.internal.tmf.analysis.xml.core.stateprovider.TmfXmlStrings;
import org.eclipse.tracecompass.internal.tmf.analysis.xml.ui.Activator;
import org.eclipse.tracecompass.internal.tmf.analysis.xml.ui.TmfXmlUiStrings;
import org.eclipse.tracecompass.internal.tmf.analysis.xml.ui.views.XmlViewInfo;
import org.eclipse.tracecompass.internal.tmf.analysis.xml.ui.views.timegraph.XmlEntry.EntryDisplayType;
import org.eclipse.tracecompass.statesystem.core.ITmfStateSystem;
import org.eclipse.tracecompass.statesystem.core.StateSystemUtils;
import org.eclipse.tracecompass.statesystem.core.exceptions.AttributeNotFoundException;
import org.eclipse.tracecompass.statesystem.core.exceptions.StateSystemDisposedException;
import org.eclipse.tracecompass.statesystem.core.exceptions.StateValueTypeException;
import org.eclipse.tracecompass.statesystem.core.exceptions.TimeRangeException;
import org.eclipse.tracecompass.statesystem.core.interval.ITmfStateInterval;
import org.eclipse.tracecompass.statesystem.core.statevalue.ITmfStateValue;
import org.eclipse.tracecompass.tmf.core.statesystem.ITmfAnalysisModuleWithStateSystems;
import org.eclipse.tracecompass.tmf.core.trace.ITmfTrace;
import org.eclipse.tracecompass.tmf.core.trace.TmfTraceUtils;
import org.eclipse.tracecompass.tmf.ui.views.timegraph.AbstractTimeGraphView;
import org.eclipse.tracecompass.tmf.ui.widgets.timegraph.ITimeGraphPresentationProvider2;
import org.eclipse.tracecompass.tmf.ui.widgets.timegraph.model.ILinkEvent;
import org.eclipse.tracecompass.tmf.ui.widgets.timegraph.model.ITimeEvent;
import org.eclipse.tracecompass.tmf.ui.widgets.timegraph.model.ITimeGraphEntry;
import org.eclipse.tracecompass.tmf.ui.widgets.timegraph.model.NullTimeEvent;
import org.eclipse.tracecompass.tmf.ui.widgets.timegraph.model.TimeEvent;
import org.eclipse.tracecompass.tmf.ui.widgets.timegraph.model.TimeGraphEntry;
import org.w3c.dom.Element;

import com.google.common.collect.Iterables;

/**
 * This view displays state system data in a time graph view. It uses an XML
 * {@link TmfXmlUiStrings#TIME_GRAPH_VIEW} element from an XML file. This
 * element defines which entries from the state system will be shown and also
 * gives additional information on the presentation of the view (states, colors,
 * etc)
 *
 * @author Florian Wininger
 */
public class XmlTimeGraphView extends AbstractTimeGraphView {

    /** View ID. */
    public static final @NonNull String ID = "org.eclipse.linuxtools.tmf.analysis.xml.ui.views.timegraph"; //$NON-NLS-1$

    private static final String[] DEFAULT_COLUMN_NAMES = new String[] {
            Messages.XmlTimeGraphView_ColumnName,
            Messages.XmlTimeGraphView_ColumnId,
            Messages.XmlTimeGraphView_ColumnParentId,
    };

    private static final String[] DEFAULT_FILTER_COLUMN_NAMES = new String[] {
            Messages.XmlTimeGraphView_ColumnName,
            Messages.XmlTimeGraphView_ColumnId
    };

    /** The relative weight of the sash */
    private static final int[] fWeight = { 1, 2 };

    private static final String EMPTY_STRING = ""; //$NON-NLS-1$
    private static final @NonNull String SPLIT_STRING = "/"; //$NON-NLS-1$

    private final @NonNull XmlViewInfo fViewInfo = new XmlViewInfo(ID);
    private final ITmfXmlModelFactory fFactory;
    private final Map<String, Integer> fStringValueMap = new HashMap<>();

    // ------------------------------------------------------------------------
    // Constructors
    // ------------------------------------------------------------------------

    /**
     * Default constructor
     */
    public XmlTimeGraphView() {
        super(ID, new XmlPresentationProvider());
        setWeight(fWeight);
        setTreeColumns(DEFAULT_COLUMN_NAMES);
        setTreeLabelProvider(new XmlTreeLabelProvider());
        setFilterColumns(DEFAULT_FILTER_COLUMN_NAMES);
        setFilterLabelProvider(new XmlTreeLabelProvider());
        setEntryComparator(new XmlEntryComparator());
        this.addPartPropertyListener(new IPropertyChangeListener() {
            @Override
            public void propertyChange(PropertyChangeEvent event) {
                if (event.getProperty().equals(TmfXmlUiStrings.XML_OUTPUT_DATA)) {
                    Object newValue = event.getNewValue();
                    if (newValue instanceof String) {
                        String data = (String) newValue;
                        fViewInfo.setViewData(data);
                        loadNewXmlView();
                    }
                }
            }
        });
        fFactory = TmfXmlReadOnlyModelFactory.getInstance();
    }

    @Override
    public void createPartControl(Composite parent) {
        super.createPartControl(parent);
        fViewInfo.setName(NonNullUtils.checkNotNull(getViewSite().getSecondaryId()));
    }

    private void loadNewXmlView() {
        rebuild();
    }

    private void setViewTitle(final String title) {
        Display.getDefault().asyncExec(new Runnable() {
            @Override
            public void run() {
                setPartName(title);
            }
        });

    }

    @Override
    protected String getNextText() {
        return Messages.XmlTimeGraphView_NextText;
    }

    @Override
    protected String getNextTooltip() {
        return Messages.XmlTimeGraphView_NextTooltip;
    }

    @Override
    protected String getPrevText() {
        return Messages.XmlTimeGraphView_PreviousText;
    }

    @Override
    protected String getPrevTooltip() {
        return Messages.XmlTimeGraphView_PreviousInterval;
    }

    /**
     * Default label provider, it shows name, id and parent columns
     *
     * TODO: There should be a way to define columns in the XML
     */
    private static class XmlTreeLabelProvider extends TreeLabelProvider {

        @Override
        public String getColumnText(Object element, int columnIndex) {
            XmlEntry entry = (XmlEntry) element;

            if (DEFAULT_COLUMN_NAMES[columnIndex].equals(Messages.XmlTimeGraphView_ColumnName)) {
                return entry.getName();
            } else if (DEFAULT_COLUMN_NAMES[columnIndex].equals(Messages.XmlTimeGraphView_ColumnId)) {
                return entry.getId();
            } else if (DEFAULT_COLUMN_NAMES[columnIndex].equals(Messages.XmlTimeGraphView_ColumnParentId)) {
                return entry.getParentId();
            }
            return EMPTY_STRING;
        }

    }

    private static class XmlEntryComparator implements Comparator<ITimeGraphEntry> {

        @Override
        public int compare(ITimeGraphEntry o1, ITimeGraphEntry o2) {

            int result = 0;

            if ((o1 instanceof XmlEntry) && (o2 instanceof XmlEntry)) {
                XmlEntry entry1 = (XmlEntry) o1;
                XmlEntry entry2 = (XmlEntry) o2;
                result = entry1.getTrace().getStartTime().compareTo(entry2.getTrace().getStartTime());
                if (result == 0) {
                    result = entry1.getTrace().getName().compareTo(entry2.getTrace().getName());
                }
                if (result == 0) {
                    result = entry1.getName().compareTo(entry2.getName());
                }
            }

            if (result == 0) {
                result = o1.getStartTime() < o2.getStartTime() ? -1 : o1.getStartTime() > o2.getStartTime() ? 1 : 0;
            }

            return result;
        }
    }

    // ------------------------------------------------------------------------
    // Internal
    // ------------------------------------------------------------------------

    @Override
    protected void buildEntryList(ITmfTrace trace, ITmfTrace parentTrace, IProgressMonitor monitor) {

        if (!fViewInfo.waitForInitialization()) {
            return;
        }
        /*
         * Get the view element from the XML file. If the element can't be
         * found, return.
         */
        Element viewElement = fViewInfo.getViewElement(TmfXmlUiStrings.TIME_GRAPH_VIEW);
        if (viewElement == null) {
            return;
        }
        ITimeGraphPresentationProvider2 pres = this.getPresentationProvider();
        if (pres instanceof XmlPresentationProvider) {
            /*
             * TODO: Each entry of a line could have their own states/color.
             * That will require an update to the presentation provider
             */
            ((XmlPresentationProvider) pres).loadNewStates(viewElement);
        }

        String title = fViewInfo.getViewTitle(viewElement);
        if (title == null) {
            title = Messages.XmlTimeGraphView_DefaultTitle;
        }
        setViewTitle(title);

        // Empty the additional state values
        fStringValueMap.clear();

        Set<String> analysisIds = fViewInfo.getViewAnalysisIds(viewElement);

        List<Element> entries = XmlUtils.getChildElements(viewElement, TmfXmlUiStrings.ENTRY_ELEMENT);
        Set<XmlEntry> entryList = new TreeSet<>(getEntryComparator());
        if (monitor.isCanceled()) {
            return;
        }

        Set<@NonNull ITmfAnalysisModuleWithStateSystems> stateSystemModules = new HashSet<>();
        if (analysisIds.isEmpty()) {
            /*
             * No analysis specified, take all state system analysis modules
             */
            Iterables.addAll(stateSystemModules, TmfTraceUtils.getAnalysisModulesOfClass(trace, ITmfAnalysisModuleWithStateSystems.class));
        } else {
            for (String moduleId : analysisIds) {
                moduleId = checkNotNull(moduleId);
                ITmfAnalysisModuleWithStateSystems module = TmfTraceUtils.getAnalysisModuleOfClass(trace, ITmfAnalysisModuleWithStateSystems.class, moduleId);
                if (module != null) {
                    stateSystemModules.add(module);
                }
            }
        }

        for (ITmfAnalysisModuleWithStateSystems module : stateSystemModules) {
            IStatus status = module.schedule();
            if (!status.isOK()) {
                return;
            }
            if (!module.waitForInitialization()) {
                return;
            }
            for (ITmfStateSystem ssq : module.getStateSystems()) {
                ssq.waitUntilBuilt();

                long startTime = ssq.getStartTime();
                long endTime = ssq.getCurrentEndTime();
                XmlEntry groupEntry = new XmlEntry(-1, trace, trace.getName(), ssq);
                entryList.add(groupEntry);
                setStartTime(Math.min(getStartTime(), startTime));
                setEndTime(Math.max(getEndTime(), endTime));

                /* Add children entry of this entry for each line */
                for (Element entry : entries) {
                    buildEntry(entry, groupEntry, -1);
                }
            }
        }

        addToEntryList(parentTrace, new ArrayList<TimeGraphEntry>(entryList));

        if (parentTrace.equals(getTrace())) {
            refresh();
        }
        for (XmlEntry traceEntry : entryList) {
            if (monitor.isCanceled()) {
                return;
            }
            long startTime = traceEntry.getStateSystem().getStartTime();
            long endTime = traceEntry.getStateSystem().getCurrentEndTime() + 1;
            buildStatusEvent(traceEntry, monitor, startTime, endTime);
        }
    }

    private void buildEntry(Element entryElement, XmlEntry parentEntry, int baseQuark) {
        /* Get the attribute string to display */
        String path = entryElement.getAttribute(TmfXmlUiStrings.PATH);
        if (path.isEmpty()) {
            path = TmfXmlStrings.WILDCARD;
        }

        /*
         * Make sure the XML element has either a display attribute or entries,
         * otherwise issue a warning
         */

        List<Element> displayElements = XmlUtils.getChildElements(entryElement, TmfXmlUiStrings.DISPLAY_ELEMENT);
        List<Element> entryElements = XmlUtils.getChildElements(entryElement, TmfXmlUiStrings.ENTRY_ELEMENT);

        if (displayElements.isEmpty() && entryElements.isEmpty()) {
            Activator.logWarning(String.format("XML view: entry for %s should have either a display element or entry elements", path)); //$NON-NLS-1$
            return;
        }

        ITmfStateSystem ss = parentEntry.getStateSystem();

        /* Get the list of quarks to process with this path */
        String[] paths = path.split(SPLIT_STRING);
        int i = 0;
        List<Integer> quarks = Collections.singletonList(baseQuark);

        while (i < paths.length) {
            List<Integer> subQuarks = new LinkedList<>();
            /* Replace * by .* to have a regex string */
            String name = paths[i].replaceAll("\\*", ".*"); //$NON-NLS-1$ //$NON-NLS-2$
            for (int relativeQuark : quarks) {
                for (int quark : ss.getSubAttributes(relativeQuark, false, name)) {
                    subQuarks.add(quark);
                }
            }
            quarks = subQuarks;
            i++;
        }

        /* Process each quark */
        XmlEntry currentEntry = parentEntry;
        Element displayElement = null;
        Map<String, XmlEntry> entryMap = new HashMap<>();
        if (!displayElements.isEmpty()) {
            displayElement = displayElements.get(0);
        }
        for (int quark : quarks) {
            currentEntry = parentEntry;
            /* Process the current entry, if specified */
            if (displayElement != null) {
                currentEntry = processEntry(entryElement, displayElement, parentEntry, quark, ss);
                entryMap.put(currentEntry.getId(), currentEntry);
            }
            /* Process the children entry of this entry */
            for (Element subEntryEl : entryElements) {
                buildEntry(subEntryEl, currentEntry, quark);
            }
        }
        if (!entryMap.isEmpty()) {
            buildTree(entryMap, parentEntry);
        }
    }

    private XmlEntry processEntry(@NonNull Element entryElement, @NonNull Element displayEl,
            @NonNull XmlEntry parentEntry, int quark, ITmfStateSystem ss) {
        /*
         * Get the start time and end time of this entry from the display
         * attribute
         */
        ITmfXmlStateAttribute display = fFactory.createStateAttribute(displayEl, parentEntry);
        int displayQuark = display.getAttributeQuark(quark, null);
        if (displayQuark == IXmlStateSystemContainer.ERROR_QUARK) {
            return new XmlEntry(quark, parentEntry.getTrace(),
                    String.format("Unknown display quark for %s", ss.getAttributeName(quark)), ss); //$NON-NLS-1$
        }

        long entryStart = ss.getStartTime();
        long entryEnd = ss.getCurrentEndTime();

        try {

            ITmfStateInterval oneInterval = ss.querySingleState(entryStart, displayQuark);

            /* The entry start is the first non-null interval */
            while (oneInterval.getStateValue().isNull()) {
                long ts = oneInterval.getEndTime() + 1;
                if (ts > ss.getCurrentEndTime()) {
                    break;
                }
                oneInterval = ss.querySingleState(ts, displayQuark);
            }
            entryStart = oneInterval.getStartTime();

            /* The entry end is the last non-null interval */
            oneInterval = ss.querySingleState(entryEnd, displayQuark);
            while (oneInterval.getStateValue().isNull()) {
                long ts = oneInterval.getStartTime() - 1;
                if (ts < ss.getStartTime()) {
                    break;
                }
                oneInterval = ss.querySingleState(ts, displayQuark);
            }
            entryEnd = oneInterval.getEndTime();

        } catch (StateSystemDisposedException e) {
        }

        return new XmlEntry(quark, displayQuark, parentEntry.getTrace(), ss.getAttributeName(quark),
                entryStart, entryEnd, EntryDisplayType.DISPLAY, ss, entryElement);
    }

    private void buildStatusEvent(XmlEntry traceEntry, @NonNull IProgressMonitor monitor, long start, long end) {
        long resolution = (end - start) / getDisplayWidth();
        long startTime = Math.max(start, traceEntry.getStartTime());
        long endTime = Math.min(end + 1, traceEntry.getEndTime());
        List<ITimeEvent> eventList = getEventList(traceEntry, startTime, endTime, resolution, monitor);
        if (monitor.isCanceled()) {
            return;
        }
        traceEntry.setEventList(eventList);
        redraw();

        for (ITimeGraphEntry entry : traceEntry.getChildren()) {
            if (monitor.isCanceled()) {
                return;
            }
            XmlEntry xmlEntry = (XmlEntry) entry;
            buildStatusEvent(xmlEntry, monitor, start, end);
        }
    }

    /** Build a tree using getParentId() and getId() */
    private static void buildTree(Map<String, XmlEntry> entryMap, XmlEntry rootEntry) {
        for (XmlEntry entry : entryMap.values()) {
            boolean root = true;
            if (!entry.getParentId().isEmpty()) {
                XmlEntry parent = entryMap.get(entry.getParentId());
                /*
                 * Associate the parent entry only if their time overlap. A
                 * child entry may start before its parent, for example at the
                 * beginning of the trace if a parent has not yet appeared in
                 * the state system. We just want to make sure that the entry
                 * didn't start after the parent ended or ended before the
                 * parent started.
                 */
                if (parent != null &&
                        !(entry.getStartTime() > parent.getEndTime() ||
                                entry.getEndTime() < parent.getStartTime())) {
                    parent.addChild(entry);
                    root = false;
                }
            }
            if (root) {
                rootEntry.addChild(entry);
            }
        }
    }

    @Override
    protected List<ITimeEvent> getEventList(TimeGraphEntry entry, long startTime, long endTime, long resolution, IProgressMonitor monitor) {
        if (!(entry instanceof XmlEntry)) {
            return Collections.EMPTY_LIST;
        }
        XmlEntry xmlEntry = (XmlEntry) entry;
        ITmfStateSystem ssq = xmlEntry.getStateSystem();
        final long realStart = Math.max(startTime, entry.getStartTime());
        final long realEnd = Math.min(endTime, entry.getEndTime());
        if (realEnd <= realStart) {
            return null;
        }
        List<ITimeEvent> eventList = null;
        int quark = xmlEntry.getDisplayQuark();

        try {
            if (xmlEntry.getType() == EntryDisplayType.DISPLAY) {

                List<ITmfStateInterval> statusIntervals = StateSystemUtils.queryHistoryRange(ssq, quark, realStart, realEnd - 1, resolution, monitor);
                eventList = new ArrayList<>(statusIntervals.size());
                long lastEndTime = -1;
                for (ITmfStateInterval statusInterval : statusIntervals) {
                    if (monitor.isCanceled()) {
                        return null;
                    }
                    int status = getStatusFromInterval(statusInterval);
                    long time = statusInterval.getStartTime();
                    long duration = statusInterval.getEndTime() - time + 1;
                    if (!statusInterval.getStateValue().isNull()) {
                        if (lastEndTime != time && lastEndTime != -1) {
                            eventList.add(new TimeEvent(entry, lastEndTime, time - lastEndTime));
                        }
                        eventList.add(new TimeEvent(entry, time, duration, status));
                    } else if (lastEndTime == -1 || time + duration >= endTime) {
                        // add null event if it intersects the start or end time
                        eventList.add(new NullTimeEvent(entry, time, duration));
                    }
                    lastEndTime = time + duration;
                }
            }
        } catch (AttributeNotFoundException | TimeRangeException | StateValueTypeException | StateSystemDisposedException e) {
            /* Ignored */
        }
        return eventList;
    }

    private int getStatusFromInterval(ITmfStateInterval statusInterval) {
        ITmfStateValue stateValue = statusInterval.getStateValue();
        int status = -1;
        switch (stateValue.getType()) {
        case INTEGER:
        case NULL:
            status = stateValue.unboxInt();
            break;
        case LONG:
            status = (int) stateValue.unboxLong();
            break;
        case STRING:
            String statusStr = stateValue.unboxStr();
            Integer statusInt = fStringValueMap.get(statusStr);
            if (statusInt != null) {
                status = statusInt;
                break;
            }
            ITimeGraphPresentationProvider2 pres = this.getPresentationProvider();
            if (pres instanceof XmlPresentationProvider) {
                // Add this new state to the presentation provider
                status = ((XmlPresentationProvider) pres).addState(statusStr);
                fStringValueMap.put(statusStr, status);
            }
            break;
        case DOUBLE:
            status = (int) stateValue.unboxDouble();
            break;
        case CUSTOM:
        default:
            break;
        }

        return status;
    }

    @Override
    protected List<ILinkEvent> getLinkList(long startTime, long endTime, long resolution, IProgressMonitor monitor) {
        /* TODO: not implemented yet, need XML to go along */
        return Collections.EMPTY_LIST;
    }

    @Override
    protected @NonNull Iterable<ITmfTrace> getTracesToBuild(@Nullable ITmfTrace trace) {
        /*
         * Return the current trace only. Experiments will return their
         * children's analyses
         */
        return (trace != null) ? Collections.singleton(trace) : Collections.EMPTY_LIST;
    }

}
