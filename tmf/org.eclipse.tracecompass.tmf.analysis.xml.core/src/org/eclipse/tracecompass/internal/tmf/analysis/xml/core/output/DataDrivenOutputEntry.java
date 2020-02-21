/*******************************************************************************
 * Copyright (c) 2019 École Polytechnique de Montréal
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License 2.0 which
 * accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/

package org.eclipse.tracecompass.internal.tmf.analysis.xml.core.output;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.tracecompass.internal.tmf.analysis.xml.core.fsm.model.DataDrivenMappingGroup;
import org.eclipse.tracecompass.internal.tmf.analysis.xml.core.fsm.model.DataDrivenStateSystemPath;
import org.eclipse.tracecompass.internal.tmf.analysis.xml.core.fsm.model.IDataDrivenRuntimeObject;
import org.eclipse.tracecompass.internal.tmf.analysis.xml.core.fsm.module.IAnalysisDataContainer;
import org.eclipse.tracecompass.internal.tmf.analysis.xml.core.output.DataDrivenOutputEntryModel.EntryBuilder;
import org.eclipse.tracecompass.internal.tmf.analysis.xml.core.output.DataDrivenXYDataProvider.DisplayType;
import org.eclipse.tracecompass.statesystem.core.ITmfStateSystem;
import org.eclipse.tracecompass.statesystem.core.StateSystemUtils;
import org.eclipse.tracecompass.statesystem.core.exceptions.StateSystemDisposedException;
import org.eclipse.tracecompass.statesystem.core.interval.ITmfStateInterval;
import org.eclipse.tracecompass.tmf.core.model.timegraph.TimeGraphEntryModel;
import org.eclipse.tracecompass.tmf.core.statesystem.TmfStateSystemAnalysisModule;
import org.eclipse.tracecompass.tmf.core.trace.ITmfTrace;

/**
 * A data driven time graph entry
 *
 * @author Geneviève Bastien
 */
public class DataDrivenOutputEntry implements IDataDrivenRuntimeObject {

    /**
     * Integer to get an ID from a state system and quark
     */
    @FunctionalInterface
    public static interface IdGetter {

        /**
         * Get and long ID from a state system and a quark
         *
         * @param ss
         *            The state system where the quark resides
         * @param quark
         *            The quark to fetch
         * @return A long unique ID for this state system and quark
         */
        long getIdFor(ITmfStateSystem ss, int quark);
    }

    /**
     * An interface for the function that registers which state system quark to
     * display for a given entry ID
     */
    public static interface QuarkCallback {
        /**
         * Register a state system quark with an ID
         *
         * @param id
         *            The ID of the entry
         * @param ss
         *            The state system to query
         * @param quark
         *            The quark to use to get the data
         * @param displayType
         *            The way to compute the data to show
         */
        void registerQuark(long id, ITmfStateSystem ss, int quark, DisplayType displayType);
    }

    private static class DataContainer implements IAnalysisDataContainer {

        private final ITmfStateSystem fStateSystem;

        public DataContainer(ITmfStateSystem ss) {
            fStateSystem = ss;
        }

        @Override
        public ITmfStateSystem getStateSystem() {
            return fStateSystem;
        }

        @Override
        public DataDrivenMappingGroup getMappingGroup(String id) {
            throw new UnsupportedOperationException("Mapping groups are not supported with views"); //$NON-NLS-1$
        }

    }

    private static final String SPLIT_STRING = "/"; //$NON-NLS-1$
    private static final Map<ITmfStateSystem, DataContainer> SS_TO_CONTAINER = new WeakHashMap<>();

    private final List<DataDrivenOutputEntry> fChildrenEntries;
    private final String fPath;
    private final @Nullable String fAnalysisId;
    private final boolean fDisplayText;
    private final @Nullable DataDrivenStateSystemPath fDisplay;
    private final @Nullable DataDrivenStateSystemPath fId;
    private final @Nullable DataDrivenStateSystemPath fParent;
    private final @Nullable DataDrivenStateSystemPath fName;
    private final DisplayType fDisplayType;

    /**
     * Constructor
     *
     * @param entries
     *            The children entries
     * @param path
     *            The path in the state system of the entry to display in the
     *            view
     * @param analysisId
     *            The name of the analysis or state system to read. If
     *            <code>null</code>, the default state system for the view will
     *            be used
     * @param displayText
     *            Whether to display text in the view's label
     * @param display
     *            The display path
     * @param id
     *            The state system path for the ID of the entry
     * @param parent
     *            The state system path for the parent of the entry. The parent
     *            is the ID of another entry at the same level
     * @param name
     *            The state ssytem path for the name of the entry
     * @param displayType
     *            The type of value to display, whether absolute or relative to
     *            previous value
     */
    public DataDrivenOutputEntry(List<DataDrivenOutputEntry> entries, String path,
            @Nullable String analysisId, boolean displayText,
            @Nullable DataDrivenStateSystemPath display,
            @Nullable DataDrivenStateSystemPath id,
            @Nullable DataDrivenStateSystemPath parent,
            @Nullable DataDrivenStateSystemPath name,
            DisplayType displayType) {
        fChildrenEntries = entries;
        fPath = path;
        fAnalysisId = analysisId;
        fDisplayText = displayText;
        fDisplay = display;
        fId = id;
        fParent = parent;
        fName = name;
        fDisplayType = displayType;
    }

    /**
     * Build the actual entries from this entry descriptor
     *
     * @param ssq
     *            The base state system to use
     * @param parentEntryId
     *            The ID of the parent time graph entry
     * @param trace
     *            The trace these entries are for, used to get any additional
     *            state system
     * @param prevBaseQuark
     *            The base quark of the parent
     * @param prevRegex
     *            With recursive entries, this is the result of the previous
     *            regex that may contain placeholders whose value need to be
     *            replace in current entry's regex
     * @param currentEnd
     *            The current end time
     * @param idGenerator
     *            A callback used to generate the entry's unique ID
     * @param callback
     *            A callback to register an ID with the state system and quark
     *            pair
     * @return All the newly created entries
     */
    public List<TimeGraphEntryModel> buildEntries(ITmfStateSystem ssq, long parentEntryId,
            ITmfTrace trace, int prevBaseQuark, String prevRegex,
            long currentEnd, IdGetter idGenerator, QuarkCallback callback) {

        // Get the state system to use to populate those entries, by default, it
        // is the same as the parent
        String specificSs = fAnalysisId;
        ITmfStateSystem parentSs = ssq;
        ITmfStateSystem ss = parentSs;
        int baseQuark = prevBaseQuark;
        if (specificSs != null) {
            ss = TmfStateSystemAnalysisModule.getStateSystem(trace, specificSs);
            baseQuark = ITmfStateSystem.ROOT_ATTRIBUTE;
            if (ss == null) {
                return Collections.emptyList();
            }
        }
        DataContainer container = SS_TO_CONTAINER.computeIfAbsent(ss, s -> new DataContainer(s));

        // Replace any place holders in the path.
        // FIXME: Figure out what this really does and better document it
        Pattern pattern = Pattern.compile(prevRegex);
        String attributePath = prevBaseQuark > 0 ? parentSs.getFullAttributePath(prevBaseQuark) : StringUtils.EMPTY;
        Matcher matcher = pattern.matcher(attributePath);
        String path = fPath;
        if (matcher.find()) {
            path = matcher.replaceFirst(path);
        }
        /* Replace * by .* to have a regex string */
        String regexName = path.replaceAll("\\*", "(.*)"); //$NON-NLS-1$//$NON-NLS-2$

        /* Get the list of quarks to process with this path */
        String[] paths = regexName.split(SPLIT_STRING);
        int i = 0;
        List<Integer> quarks = Collections.singletonList(baseQuark);

        while (i < paths.length) {
            List<Integer> subQuarks = new ArrayList<>();
            String name = paths[i];
            for (int relativeQuark : quarks) {
                subQuarks.addAll(ss.getSubAttributes(relativeQuark, false, name));
            }
            quarks = subQuarks;
            i++;
        }

        /* Process each quark */
        DataDrivenStateSystemPath displayPath = fDisplay;
        DataDrivenStateSystemPath namePath = fName;
        DataDrivenStateSystemPath idPath = fId;
        DataDrivenStateSystemPath parentPath = fParent;
        Map<String, DataDrivenOutputEntryModel.EntryBuilder> entryMap = new HashMap<>();
        List<DataDrivenOutputEntryModel.EntryBuilder> entries = new ArrayList<>();
        List<TimeGraphEntryModel> entryList = new ArrayList<>();
        for (int quark : quarks) {

            long id = idGenerator.getIdFor(ss, quark);
            // Get the quark containing the data to display, else there is no display
            int displayQuark = ITmfStateSystem.INVALID_ATTRIBUTE;
            long entryStart = ss.getStartTime();
            long entryEnd = currentEnd;
            if (displayPath != null) {
                displayQuark = displayPath.getQuark(quark, container);
                if (displayQuark == ITmfStateSystem.INVALID_ATTRIBUTE) {
                    // The entry has no display quark, do not display
                    continue;
                }
                callback.registerQuark(id, ss, displayQuark, fDisplayType);

                try {

                    ITmfStateInterval oneInterval = ss.querySingleState(entryStart, displayQuark);
                    /* The entry start is the first non-null interval */
                    while (oneInterval.getStateValue().isNull()) {
                        long ts = oneInterval.getEndTime() + 1;
                        if (ts > currentEnd) {
                            break;
                        }
                        oneInterval = ss.querySingleState(ts, displayQuark);
                    }
                    entryStart = oneInterval.getStartTime();

                    /* The entry end is the last non-null interval */
                    oneInterval = ss.querySingleState(entryEnd - 1, displayQuark);
                    while (oneInterval.getStateValue().isNull()) {
                        long ts = oneInterval.getStartTime() - 1;
                        if (ts < ss.getStartTime()) {
                            break;
                        }
                        oneInterval = ss.querySingleState(ts, displayQuark);
                    }
                    entryEnd = Math.min(oneInterval.getEndTime() + 1, currentEnd);

                } catch (StateSystemDisposedException e) {
                }
            }

            // Get the name of this entry
            String name = StringUtils.EMPTY;
            if (namePath != null) {
                name = getFirstValue(quark, namePath, container);
            }
            if (name.isEmpty()) {
                // Default, use the attribute name as name
                name = ss.getAttributeName(quark);
            }

            // Get the XML internal ID of this entry
            String xmlId;
            if (idPath != null) {
                xmlId = getFirstValue(quark, idPath, container);
            } else {
                // Use the name as ID
                xmlId = name;
            }

            // Get the parent's internal ID of the entry
            String xmlParentId = StringUtils.EMPTY;
            if (parentPath != null) {
                xmlParentId = getFirstValue(quark, parentPath, container);
            }

            EntryBuilder entryBuilder = new DataDrivenOutputEntryModel.EntryBuilder(id, parentEntryId, displayQuark, name, xmlId, xmlParentId, entryStart, entryEnd, fDisplayText, fDisplayType);
            entryMap.put(xmlId, entryBuilder);
            entries.add(entryBuilder);

            /* Process the children entry of this entry */
            for (DataDrivenOutputEntry subEntry : fChildrenEntries) {
                @NonNull String regex = prevRegex.isEmpty() ? regexName : prevRegex + '/' + regexName;
                entryList.addAll(subEntry.buildEntries(ss, entryBuilder.getId(), trace, quark, regex, currentEnd, idGenerator, callback));
            }
        }
        // At this point, the parent has been set, so we can build the entries
        buildTree(entryMap);

        for (EntryBuilder b : entries) {
            entryList.add(b.build());
        }
        return entryList;
    }

    private static void buildTree(Map<String, EntryBuilder> entryMap) {
        for (EntryBuilder entry : entryMap.values()) {
            if (!entry.getXmlParentId().isEmpty()) {
                EntryBuilder parent = entryMap.get(entry.getXmlParentId());
                /*
                 * Associate the parent entry only if their time overlap. A child entry may
                 * start before its parent, for example at the beginning of the trace if a
                 * parent has not yet appeared in the state system. We just want to make sure
                 * that the entry didn't start after the parent ended or ended before the parent
                 * started.
                 */
                if (parent != null &&
                        !(entry.getStartTime() > parent.getEndTime() ||
                                entry.getEndTime() < parent.getStartTime())) {
                    entry.setParentId(parent.getId());
                }
            }
        }
    }

    /* Return the state value of the first interval with a non-null value */
    private static String getFirstValue(int baseQuark, DataDrivenStateSystemPath path, DataContainer container) {

        int quark = path.getQuark(baseQuark, container);
        if (quark >= 0) {
            ITmfStateSystem stateSystem = container.getStateSystem();
            ITmfStateInterval firstInterval = StateSystemUtils.queryUntilNonNullValue(container.getStateSystem(), quark, stateSystem.getStartTime(), stateSystem.getCurrentEndTime());
            if (firstInterval != null) {
                return String.valueOf(firstInterval.getValue());
            }
        }
        return StringUtils.EMPTY;
    }

}
