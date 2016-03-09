/*******************************************************************************
 * Copyright (c) 2014, 2015 École Polytechnique de Montréal and others.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Geneviève Bastien - Initial API and implementation
 *******************************************************************************/

package org.eclipse.tracecompass.internal.tmf.analysis.xml.ui.views.xychart;

import static org.eclipse.tracecompass.common.core.NonNullUtils.checkNotNull;

import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.tracecompass.internal.tmf.analysis.xml.ui.Activator;
import org.eclipse.tracecompass.internal.tmf.analysis.xml.ui.TmfXmlUiStrings;
import org.eclipse.tracecompass.internal.tmf.analysis.xml.ui.views.XmlViewInfo;
import org.eclipse.tracecompass.statesystem.core.ITmfStateSystem;
import org.eclipse.tracecompass.statesystem.core.exceptions.AttributeNotFoundException;
import org.eclipse.tracecompass.statesystem.core.exceptions.StateSystemDisposedException;
import org.eclipse.tracecompass.statesystem.core.exceptions.StateValueTypeException;
import org.eclipse.tracecompass.statesystem.core.exceptions.TimeRangeException;
import org.eclipse.tracecompass.statesystem.core.statevalue.ITmfStateValue;
import org.eclipse.tracecompass.tmf.analysis.xml.core.model.ITmfXmlModelFactory;
import org.eclipse.tracecompass.tmf.analysis.xml.core.model.ITmfXmlStateAttribute;
import org.eclipse.tracecompass.tmf.analysis.xml.core.model.TmfXmlLocation;
import org.eclipse.tracecompass.tmf.analysis.xml.core.model.readonly.TmfXmlReadOnlyModelFactory;
import org.eclipse.tracecompass.tmf.analysis.xml.core.module.IXmlStateSystemContainer;
import org.eclipse.tracecompass.tmf.analysis.xml.core.module.XmlUtils;
import org.eclipse.tracecompass.tmf.analysis.xml.core.stateprovider.TmfXmlStrings;
import org.eclipse.tracecompass.tmf.core.statesystem.ITmfAnalysisModuleWithStateSystems;
import org.eclipse.tracecompass.tmf.core.trace.ITmfTrace;
import org.eclipse.tracecompass.tmf.core.trace.TmfTraceUtils;
import org.eclipse.tracecompass.tmf.ui.viewers.xycharts.linecharts.TmfCommonXLineChartViewer;
import org.w3c.dom.Element;

/**
 * Main viewer to display XML-defined xy charts. It uses an XML
 * {@link TmfXmlUiStrings#XY_VIEW} element from an XML file. This element
 * defines which entries from the state system will be shown and also gives
 * additional information on the presentation of the view.
 *
 * @author Geneviève Bastien
 */
public class XmlXYViewer extends TmfCommonXLineChartViewer {

    private static final String SPLIT_STRING = "/"; //$NON-NLS-1$
    /** Timeout between updates in the updateData thread */
    private static final long BUILD_UPDATE_TIMEOUT = 500;

    private static final Pattern WILDCARD_PATTERN = Pattern.compile("\\*"); //$NON-NLS-1$

    private final ITmfXmlModelFactory fFactory = TmfXmlReadOnlyModelFactory.getInstance();
    private final Map<Integer, SeriesData> fSeriesData = new HashMap<>();

    private final XmlViewInfo fViewInfo;

    /** XML Model elements to use to create the series */
    private @Nullable ITmfXmlStateAttribute fDisplay;
    private @Nullable ITmfXmlStateAttribute fSeriesName;
    private @Nullable XmlXYEntry fEntry;

    private enum DisplayType {
        ABSOLUTE,
        DELTA
    }

    /**
     * The information related to one series on the chart
     */
    private class SeriesData {

        private final double[] fYValues;
        private final double @Nullable [] fYAbsoluteValues;
        private final Integer fDisplayQuark;
        private final String fName;
        private final DisplayType fType;

        public SeriesData(int length, int attributeQuark, String seriesName, DisplayType type) {
            fYValues = new double[length];
            fDisplayQuark = attributeQuark;
            fName = seriesName;
            fType = type;
            switch (fType) {
            case DELTA:
                fYAbsoluteValues = new double[length];
                break;
            case ABSOLUTE:
            default:
                fYAbsoluteValues = null;
                break;
            }

        }

        public double[] getYValues() {
            return fYValues;
        }

        public Integer getDisplayQuark() {
            return fDisplayQuark;
        }

        public String getSeriesName() {
            return fName;
        }

        public void setYValue(int i, double yvalue) {
            switch (fType) {
            case DELTA:
                double[] absoluteVals = fYAbsoluteValues;
                if (absoluteVals == null) {
                    throw new IllegalStateException();
                }
                absoluteVals[i] = yvalue;
                /*
                 * At the first timestamp, the delta value should be 0 since we
                 * do not have the previous values
                 */
                double prevValue = yvalue;
                if (i > 0) {
                    prevValue = absoluteVals[i - 1];
                }
                fYValues[i] = yvalue - prevValue;
                break;
            case ABSOLUTE:
            default:
                fYValues[i] = yvalue;
                break;
            }

        }
    }

    private static class XmlXYEntry implements IXmlStateSystemContainer {

        private final ITmfStateSystem fStateSystem;
        private final String fPath;
        private final DisplayType fType;

        public XmlXYEntry(ITmfStateSystem stateSystem, String path, Element entryElement) {
            fStateSystem = stateSystem;
            fPath = path;
            switch (entryElement.getAttribute(TmfXmlUiStrings.DISPLAY_TYPE)) {
            case TmfXmlUiStrings.DISPLAY_TYPE_DELTA:
                fType = DisplayType.DELTA;
                break;
            case TmfXmlUiStrings.DISPLAY_TYPE_ABSOLUTE:
            default:
                fType = DisplayType.ABSOLUTE;
                break;
            }
        }

        @Override
        public @Nullable String getAttributeValue(@Nullable String name) {
            return name;
        }

        @Override
        public ITmfStateSystem getStateSystem() {
            return fStateSystem;
        }

        @Override
        public @NonNull Iterable<@NonNull TmfXmlLocation> getLocations() {
            return Collections.EMPTY_SET;
        }

        public DisplayType getType() {
            return fType;
        }

        public List<Integer> getQuarks() {
            /* Get the list of quarks to process with this path */
            String[] paths = fPath.split(SPLIT_STRING);
            List<Integer> quarks = Collections.singletonList(IXmlStateSystemContainer.ROOT_QUARK);

            try {
                for (String path : paths) {
                    List<Integer> subQuarks = new LinkedList<>();
                    /* Replace * by .* to have a regex string */
                    String name = WILDCARD_PATTERN.matcher(path).replaceAll(".*"); //$NON-NLS-1$
                    for (int relativeQuark : quarks) {
                        subQuarks.addAll(fStateSystem.getSubAttributes(relativeQuark, false, name));
                    }
                    quarks = subQuarks;
                }
            } catch (AttributeNotFoundException e) {
                /*
                 * We get all attributes from the state system itself, this
                 * should not happen.
                 */
                throw new IllegalStateException();
            }
            return quarks;
        }
    }

    /**
     * Constructor
     *
     * @param parent
     *            parent view
     * @param viewInfo
     *            The view info object
     */
    public XmlXYViewer(@Nullable Composite parent, XmlViewInfo viewInfo) {
        super(parent, Messages.XmlXYViewer_DefaultViewerTitle, Messages.XmlXYViewer_DefaultXAxis, Messages.XmlXYViewer_DefaultYAxis);
        fViewInfo = viewInfo;
    }

    @Override
    protected void updateData(long start, long end, int nb, @Nullable IProgressMonitor monitor) {

        ITmfXmlStateAttribute display = fDisplay;
        ITmfXmlStateAttribute seriesNameAttrib = fSeriesName;
        XmlXYEntry entry = fEntry;
        if (getTrace() == null || display == null || entry == null) {
            return;
        }
        ITmfStateSystem ss = entry.getStateSystem();

        double[] xvalues = getXAxis(start, end, nb);
        setXAxis(xvalues);

        boolean complete = false;
        long currentEnd = start;

        while (!complete && currentEnd < end) {
            if (monitor != null && monitor.isCanceled()) {
                return;
            }

            complete = ss.waitUntilBuilt(BUILD_UPDATE_TIMEOUT);
            currentEnd = ss.getCurrentEndTime();
            try {
                List<Integer> quarks = entry.getQuarks();
                long traceStart = getStartTime();
                long traceEnd = getEndTime();
                long offset = this.getTimeOffset();

                /* Initialize quarks and series names */
                for (int quark : quarks) {
                    String seriesName = null;
                    if (seriesNameAttrib == null) {
                        seriesName = ss.getAttributeName(quark);
                    } else {
                        int seriesNameQuark = seriesNameAttrib.getAttributeQuark(quark);
                        try {
                            ITmfStateValue seriesNameValue = ss.querySingleState(start, seriesNameQuark).getStateValue();
                            if (!seriesNameValue.isNull()) {
                                seriesName = seriesNameValue.toString();
                            }
                            if (seriesName == null || seriesName.isEmpty()) {
                                seriesName = ss.getAttributeName(quark);
                            }
                        } catch (TimeRangeException e) {
                            /*
                             * The attribute did not exist at this point, simply
                             * use attribute name as series name
                             */
                            seriesName = ss.getAttributeName(quark);
                        }
                    }
                    fSeriesData.put(quark, new SeriesData(xvalues.length, display.getAttributeQuark(quark), seriesName, entry.getType()));
                }
                double yvalue = 0.0;
                for (int i = 0; i < xvalues.length; i++) {
                    if (monitor != null && monitor.isCanceled()) {
                        return;
                    }
                    double x = xvalues[i];
                    long time = (long) x + offset;
                    // make sure that time is in the trace range after double to
                    // long conversion
                    time = time < traceStart ? traceStart : time;
                    time = time > traceEnd ? traceEnd : time;

                    for (int quark : quarks) {
                        SeriesData data = checkNotNull(fSeriesData.get(quark));
                        try {
                            yvalue = ss.querySingleState(time, data.getDisplayQuark()).getStateValue().unboxLong();
                            data.setYValue(i, yvalue);
                        } catch (TimeRangeException e) {
                            data.setYValue(i, 0);
                        }
                    }
                }
                for (int quark : quarks) {
                    SeriesData data = checkNotNull(fSeriesData.get(quark));
                    setSeries(data.getSeriesName(), data.getYValues());
                }
                updateDisplay();
            } catch (AttributeNotFoundException | StateValueTypeException e) {
                Activator.logError("Error updating the data of XML XY view", e); //$NON-NLS-1$
            } catch (StateSystemDisposedException e) {
                return;
            }
        }

    }

    @Override
    protected void initializeDataSource() {
        super.initializeDataSource();

        ITmfTrace trace = this.getTrace();
        if (trace == null) {
            return;
        }

        Element viewElement = fViewInfo.getViewElement(TmfXmlUiStrings.XY_VIEW);
        if (viewElement == null) {
            return;
        }

        Iterable<String> analysisIds = fViewInfo.getViewAnalysisIds(viewElement);

        List<ITmfAnalysisModuleWithStateSystems> stateSystemModules = new LinkedList<>();
        if (!analysisIds.iterator().hasNext()) {
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

        /** Initialize the data */
        fDisplay = null;
        fSeriesName = null;
        ITmfStateSystem ss = null;
        fEntry = null;

        /* Schedule all state systems */
        for (ITmfAnalysisModuleWithStateSystems module : stateSystemModules) {
            IStatus status = module.schedule();
            if (!status.isOK()) {
                return;
            }
            if (!module.waitForInitialization()) {
                return;
            }
            for (ITmfStateSystem ssq : module.getStateSystems()) {
                ss = ssq;
                break;
            }
        }
        if (ss == null) {
            return;
        }

        /*
         * Initialize state attributes. There should be only one entry element
         * for XY charts.
         */
        List<Element> entries = XmlUtils.getChildElements(viewElement, TmfXmlUiStrings.ENTRY_ELEMENT);
        Element entryElement = entries.get(0);
        String path = entryElement.getAttribute(TmfXmlUiStrings.PATH);
        if (path.isEmpty()) {
            path = TmfXmlStrings.WILDCARD;
        }
        XmlXYEntry entry = new XmlXYEntry(ss, path, entryElement);
        fEntry = entry;

        /* Get the display element to use */
        List<@NonNull Element> displayElements = XmlUtils.getChildElements(entryElement, TmfXmlUiStrings.DISPLAY_ELEMENT);
        if (displayElements.isEmpty()) {
            Activator.logWarning(String.format("XML view: entry for %s should have a display element", path)); //$NON-NLS-1$
            return;
        }
        Element displayElement = displayElements.get(0);
        fDisplay = fFactory.createStateAttribute(displayElement, entry);

        /* Get the series name element to use */
        List<Element> seriesNameElements = XmlUtils.getChildElements(entryElement, TmfXmlUiStrings.NAME_ELEMENT);
        if (!seriesNameElements.isEmpty()) {
            Element seriesNameElement = seriesNameElements.get(0);
            fSeriesName = fFactory.createStateAttribute(seriesNameElement, entry);
        }

    }

    /**
     * Tells the viewer that the view info has been updated and the viewer needs
     * to be reinitialized
     */
    public void viewInfoUpdated() {
        reinitialize();
    }

}
