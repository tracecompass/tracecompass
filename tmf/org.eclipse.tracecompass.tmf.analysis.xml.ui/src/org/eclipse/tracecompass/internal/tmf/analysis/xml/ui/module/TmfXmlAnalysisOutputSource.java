/*******************************************************************************
 * Copyright (c) 2016 École Polytechnique de Montréal and others
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License 2.0 which
 * accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Geneviève Bastien - Initial API and implementation
 *******************************************************************************/

package org.eclipse.tracecompass.internal.tmf.analysis.xml.ui.module;

import java.util.Collection;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.tracecompass.internal.tmf.analysis.xml.core.module.XmlOutputElement;
import org.eclipse.tracecompass.internal.tmf.analysis.xml.core.module.XmlUtils;
import org.eclipse.tracecompass.internal.tmf.analysis.xml.core.pattern.stateprovider.XmlPatternAnalysis;
import org.eclipse.tracecompass.internal.tmf.analysis.xml.ui.views.latency.PatternDensityView;
import org.eclipse.tracecompass.internal.tmf.analysis.xml.ui.views.latency.PatternLatencyTableView;
import org.eclipse.tracecompass.internal.tmf.analysis.xml.ui.views.latency.PatternScatterGraphView;
import org.eclipse.tracecompass.internal.tmf.analysis.xml.ui.views.latency.PatternStatisticsView;
import org.eclipse.tracecompass.internal.tmf.analysis.xml.ui.views.timegraph.XmlTimeGraphView;
import org.eclipse.tracecompass.internal.tmf.analysis.xml.ui.views.xychart.XmlXYView;
import org.eclipse.tracecompass.tmf.analysis.xml.core.module.TmfXmlStrings;
import org.eclipse.tracecompass.tmf.core.analysis.IAnalysisModule;
import org.eclipse.tracecompass.tmf.core.analysis.IAnalysisOutput;
import org.eclipse.tracecompass.tmf.core.analysis.ITmfNewAnalysisModuleListener;
import org.eclipse.tracecompass.tmf.core.statesystem.ITmfAnalysisModuleWithStateSystems;

import com.google.common.collect.Iterables;
import com.google.common.collect.Multimap;

/**
 * This class searches all XML files to find outputs applicable to the newly
 * created analysis
 *
 * @author Geneviève Bastien
 */
public class TmfXmlAnalysisOutputSource implements ITmfNewAnalysisModuleListener {

    private static final String LATENCY_STRING = "Latency"; //$NON-NLS-1$
    /** String separating data elements for the output properties */
    public static final @NonNull String DATA_SEPARATOR = ";;;"; //$NON-NLS-1$

    /**
     * Enum to match the name of a view's XML element to its view ID.
     */
    public static enum ViewType {
        /**
         * Time graph view element
         */
        TIME_GRAPH_VIEW(XmlUtils.OutputType.TIME_GRAPH, XmlTimeGraphView.ID),
        /**
         * XY chart view element
         */
        XY_VIEW(XmlUtils.OutputType.XY, XmlXYView.ID);

        private final XmlUtils.OutputType fOutputType;
        private final String fViewId;

        private ViewType(XmlUtils.OutputType outputType, String viewId) {
            fOutputType = outputType;
            fViewId = viewId;
        }

        /**
         * Get the XML element corresponding to this view type
         *
         * @return The XML element corresponding to this type
         */
        public @NonNull String getXmlElem() {
            return fOutputType.getXmlElem();
        }

        private String getViewId() {
            return fViewId;
        }
    }

    /**
     * Enum for latency view type.
     *
     * @author Jean-Christian Kouame
     */
    public static enum LatencyViewType {

        /**
         * Latency Table View type
         */
        LATENCY_TABLE(PatternLatencyTableView.ID, Messages.TmfXmlAnalysisOutputSource_LatencyTable),

        /**
         * Latency Scatter View type
         */
        SCATTER_GRAPH(PatternScatterGraphView.ID, Messages.TmfXmlAnalysisOutputSource_ScatterGraphTitle),

        /**
         * Latency Density View type
         */
        DENSITY_VIEW(PatternDensityView.ID, Messages.TmfXmlAnalysisOutputSource_DensityChartTitle),

        /**
         * Latency Statistic View type
         */
        STATISTIC_VIEW(PatternStatisticsView.ID, Messages.TmfXmlAnalysisOutputSource_LatencyStatisticsTitle);

        private @NonNull String fLatencyViewId;
        private String fLatencyViewLabel;

        private LatencyViewType(@NonNull String viewId, String label) {
            fLatencyViewId = viewId;
            fLatencyViewLabel = label;
        }

        /**
         * Get the ID of the latency view
         *
         * @return The ID
         */
        public String getViewId() {
            return fLatencyViewId;
        }

        /**
         * Get the label of the view
         *
         * @return The label
         */
        public String getLabel() {
            return fLatencyViewLabel;
        }
    }

    @Override
    public void moduleCreated(IAnalysisModule module) {

        if (module instanceof ITmfAnalysisModuleWithStateSystems) {
            Multimap<String, XmlOutputElement> outputs = XmlUtils.getXmlOutputElements();
            for (Collection<XmlOutputElement> elements : outputs.asMap().values()) {
                for (ViewType viewType : ViewType.values()) {
                    Iterable<XmlOutputElement> filteredElements = Iterables.filter(elements, element -> (element.getXmlElem().equals(viewType.getXmlElem()) && element.getAnalyses().contains(module.getId())));
                    String viewId = viewType.getViewId();

                    for (XmlOutputElement element : filteredElements) {
                        IAnalysisOutput output = new TmfXmlViewOutput(viewId, viewType);
                        output.setOutputProperty(TmfXmlStrings.XML_OUTPUT_DATA, element.getId() + DATA_SEPARATOR + element.getPath() + DATA_SEPARATOR + element.getLabel(), false);
                        module.registerOutput(output);
                    }
                }
            }
        }

        // Add the latency views for pattern analysis
        if (module instanceof XmlPatternAnalysis) {
            for (LatencyViewType viewType : LatencyViewType.values()) {
                String viewLabelPrefix = ((XmlPatternAnalysis) module).getViewLabelPrefix();
                String label = viewLabelPrefix.isEmpty() ? viewType.getLabel() : viewType.getLabel().replaceFirst(LATENCY_STRING, viewLabelPrefix);
                IAnalysisOutput output = new TmfXmlLatencyViewOutput(viewType.getViewId(), label);
                output.setOutputProperty(TmfXmlStrings.XML_LATENCY_OUTPUT_DATA, module.getId() + DATA_SEPARATOR + output.getName(), false);
                module.registerOutput(output);
            }
        }
    }
}
