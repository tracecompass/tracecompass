/**********************************************************************
 * Copyright (c) 2017 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 **********************************************************************/

package org.eclipse.tracecompass.tmf.analysis.xml.core.module;

import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Set;
import java.util.regex.Pattern;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.tracecompass.internal.provisional.tmf.core.model.AbstractTmfTraceDataProvider;
import org.eclipse.tracecompass.internal.provisional.tmf.core.model.TmfCommonXAxisResponseFactory;
import org.eclipse.tracecompass.internal.provisional.tmf.core.model.filters.TimeQueryFilter;
import org.eclipse.tracecompass.internal.provisional.tmf.core.model.xy.ITmfCommonXAxisResponse;
import org.eclipse.tracecompass.internal.provisional.tmf.core.model.xy.ITmfXYDataProvider;
import org.eclipse.tracecompass.internal.provisional.tmf.core.model.xy.IYModel;
import org.eclipse.tracecompass.internal.tmf.analysis.xml.core.Messages;
import org.eclipse.tracecompass.internal.tmf.analysis.xml.core.model.ITmfXmlModelFactory;
import org.eclipse.tracecompass.internal.tmf.analysis.xml.core.model.ITmfXmlStateAttribute;
import org.eclipse.tracecompass.internal.tmf.analysis.xml.core.model.TmfXmlLocation;
import org.eclipse.tracecompass.internal.tmf.analysis.xml.core.model.readonly.TmfXmlReadOnlyModelFactory;
import org.eclipse.tracecompass.internal.tmf.analysis.xml.core.module.IXmlStateSystemContainer;
import org.eclipse.tracecompass.internal.tmf.core.model.YModel;
import org.eclipse.tracecompass.statesystem.core.ITmfStateSystem;
import org.eclipse.tracecompass.statesystem.core.exceptions.StateSystemDisposedException;
import org.eclipse.tracecompass.statesystem.core.interval.ITmfStateInterval;
import org.eclipse.tracecompass.tmf.core.statesystem.ITmfAnalysisModuleWithStateSystems;
import org.eclipse.tracecompass.tmf.core.trace.ITmfTrace;
import org.eclipse.tracecompass.tmf.core.trace.TmfTraceUtils;
import org.w3c.dom.Element;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Iterables;

/**
 * This data provider will return a XY model (wrapped in a response) based on a
 * query filter. The model can be used afterwards by any viewer to draw charts.
 * Model returned is for XML analysis
 *
 * @author Yonni Chen
 * @since 2.3
 */
@NonNullByDefault
@SuppressWarnings("restriction")
public class XmlXYDataProvider extends AbstractTmfTraceDataProvider implements ITmfXYDataProvider {

    private static final String SPLIT_STRING = "/"; //$NON-NLS-1$
    private static final Pattern WILDCARD_PATTERN = Pattern.compile("\\*"); //$NON-NLS-1$

    private static class XmlXYEntry implements IXmlStateSystemContainer {

        private final ITmfStateSystem fStateSystem;
        private final String fPath;
        private final DisplayType fType;

        public XmlXYEntry(ITmfStateSystem stateSystem, String path, Element entryElement) {
            fStateSystem = stateSystem;
            fPath = path;
            switch (entryElement.getAttribute(TmfXmlStrings.DISPLAY_TYPE)) {
            case TmfXmlStrings.DISPLAY_TYPE_DELTA:
                fType = DisplayType.DELTA;
                break;
            case TmfXmlStrings.DISPLAY_TYPE_ABSOLUTE:
            default:
                fType = DisplayType.ABSOLUTE;
                break;
            }
        }

        @Override
        public @Nullable String getAttributeValue(@Nullable String name) {
            // Method must be overridden
            return name;
        }

        @Override
        public ITmfStateSystem getStateSystem() {
            return fStateSystem;
        }

        @Override
        public @NonNull Iterable<@NonNull TmfXmlLocation> getLocations() {
            return Collections.emptySet();
        }

        public DisplayType getType() {
            return fType;
        }

        public List<Integer> getQuarks() {
            /* This is an attribute tree path and not a file path */
            String[] paths = fPath.split(SPLIT_STRING);
            /* Get the list of quarks to process with this path */
            List<Integer> quarks = Collections.singletonList(IXmlStateSystemContainer.ROOT_QUARK);

            for (String path : paths) {
                List<Integer> subQuarks = new LinkedList<>();
                /* Replace * by .* to have a regex string */
                String name = WILDCARD_PATTERN.matcher(path).replaceAll(".*"); //$NON-NLS-1$
                for (int relativeQuark : quarks) {
                    subQuarks.addAll(fStateSystem.getSubAttributes(relativeQuark, false, name));
                }
                quarks = subQuarks;
            }
            return quarks;
        }
    }

    private enum DisplayType {
        ABSOLUTE, DELTA
    }

    /** XML Model elements to use to create the series */
    private final ITmfXmlStateAttribute fDisplay;
    private final XmlXYEntry fXmlEntry;

    /**
     * Constructor
     */
    private XmlXYDataProvider(ITmfTrace trace, XmlXYEntry entry, ITmfXmlStateAttribute display) {
        super(trace);
        fXmlEntry = entry;
        fDisplay = display;
    }

    /**
     * Create an instance of {@link XmlXYDataProvider}. Returns null if statesystem
     * is null.
     *
     * @param trace
     *            A trace on which we are interested to fetch a model
     * @param analysisIds
     *            A list of analysis ids used for retrieving Analysis objects
     * @param entryElement
     *            An XML entry element
     * @return A XmlDataProvider
     */
    public static @Nullable XmlXYDataProvider create(ITmfTrace trace, Set<String> analysisIds, Element entryElement) {
        ITmfStateSystem ss = getStateSystemFromAnalyses(analysisIds, trace);
        if (ss == null) {
            return null;
        }

        /*
         * Initialize state attributes. There should be only one entry element for XY
         * charts.
         */
        ITmfXmlModelFactory fFactory = TmfXmlReadOnlyModelFactory.getInstance();
        String path = entryElement.getAttribute(TmfXmlStrings.PATH);
        if (path.isEmpty()) {
            path = TmfXmlStrings.WILDCARD;
        }
        XmlXYEntry entry = new XmlXYEntry(ss, path, entryElement);

        /* Get the display element to use */
        List<@NonNull Element> displayElements = TmfXmlUtils.getChildElements(entryElement, TmfXmlStrings.DISPLAY_ELEMENT);
        if (displayElements.isEmpty()) {
            return null;
        }
        Element displayElement = displayElements.get(0);
        ITmfXmlStateAttribute display = fFactory.createStateAttribute(displayElement, entry);

        /* Get the series name element to use */
        List<Element> seriesNameElements = TmfXmlUtils.getChildElements(entryElement, TmfXmlStrings.NAME_ELEMENT);
        ITmfXmlStateAttribute seriesName = null;
        if (!seriesNameElements.isEmpty()) {
            Element seriesNameElement = seriesNameElements.get(0);
            seriesName = fFactory.createStateAttribute(seriesNameElement, entry);
        }

        if (seriesName != null) {
            return new XmlXYDataProvider(trace, entry, display);
        }
        return null;
    }

    @Override
    public ITmfCommonXAxisResponse fetchXY(TimeQueryFilter filter, @Nullable IProgressMonitor monitor) {
        ITmfXmlStateAttribute display = fDisplay;
        XmlXYEntry entry = fXmlEntry;

        ITmfStateSystem ss = entry.getStateSystem();
        List<Integer> quarks = entry.getQuarks();

        /* Series are lazily created in the HashMap */
        Map<String, double[]> tempModel = new HashMap<>();
        long[] xValues = filter.getTimesRequested();

        try {
            for (int i = 0; i < xValues.length; i++) {
                long time = xValues[i];
                if (ss.getStartTime() <= time && time <= ss.getCurrentEndTime()) {
                    List<@NonNull ITmfStateInterval> full = ss.queryFullState(time);
                    for (int quark : quarks) {
                        String seriesName = (String) full.get(quark).getValue();
                        if (seriesName != null && !seriesName.isEmpty()) {
                            Object value = full.get(display.getAttributeQuark(quark, null)).getValue();

                            double[] yValues = tempModel.get(seriesName);
                            if (yValues != null) {
                                setYValue(i, yValues, extractValue(value), entry.getType());
                            } else {
                                yValues = new double[xValues.length];
                                setYValue(i, yValues, extractValue(value), entry.getType());
                                tempModel.put(seriesName, yValues);
                            }
                        }
                    }
                }
            }
        } catch (StateSystemDisposedException e) {
            return TmfCommonXAxisResponseFactory.createFailedResponse(e.getMessage());
        }

        ImmutableMap.Builder<String, IYModel> ySeries = ImmutableMap.builder();
        for (Entry<String, double[]> tempEntry : tempModel.entrySet()) {
            ySeries.put(tempEntry.getKey(), new YModel(tempEntry.getKey(), tempEntry.getValue()));
        }

        boolean complete = ss.waitUntilBuilt(0);
        long currentEnd = ss.getCurrentEndTime();
        return TmfCommonXAxisResponseFactory.create(Objects.requireNonNull(Messages.XmlDataProvider_DefaultXYTitle), xValues, ySeries.build(), currentEnd, complete);
    }

    private static void setYValue(int index, double[] y, double value, DisplayType type) {
        if (type.equals(DisplayType.DELTA)) {
            y[index] = value;
            /*
             * At the first timestamp, the delta value should be 0 since we do not have the
             * previous values
             */
            double prevValue = value;
            if (index > 0) {
                prevValue = y[index - 1];
            }
            y[index] = value - prevValue;
        } else {
            /* ABSOLUTE by default */
            y[index] = value;
        }
    }

    private static double extractValue(@Nullable Object val) {
        if (val instanceof Number) {
            return ((Number) val).doubleValue();
        }
        return 0;
    }

    private static @Nullable ITmfStateSystem getStateSystemFromAnalyses(Set<String> analysisIds, ITmfTrace trace) {
        List<ITmfAnalysisModuleWithStateSystems> stateSystemModules = new LinkedList<>();
        if (analysisIds.isEmpty()) {
            /*
             * No analysis specified, take all state system analysis modules
             */
            for (ITmfAnalysisModuleWithStateSystems module : TmfTraceUtils.getAnalysisModulesOfClass(trace, ITmfAnalysisModuleWithStateSystems.class)) {
                stateSystemModules.add(module);
            }
        } else {
            for (String moduleId : analysisIds) {
                ITmfAnalysisModuleWithStateSystems module = TmfTraceUtils.getAnalysisModuleOfClass(trace, ITmfAnalysisModuleWithStateSystems.class, moduleId);
                if (module != null) {
                    stateSystemModules.add(module);
                }
            }
        }

        /* Schedule all state systems */
        for (ITmfAnalysisModuleWithStateSystems module : stateSystemModules) {
            module.schedule();

            /* If module succeeded, we get the first statesystem */
            if (module.waitForInitialization()) {
                return Iterables.getFirst(module.getStateSystems(), null);
            }
        }

        return null;
    }
}
