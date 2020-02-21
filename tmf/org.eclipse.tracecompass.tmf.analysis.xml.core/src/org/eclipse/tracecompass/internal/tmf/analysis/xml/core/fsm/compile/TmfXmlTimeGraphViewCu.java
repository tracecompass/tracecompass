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

package org.eclipse.tracecompass.internal.tmf.analysis.xml.core.fsm.compile;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.tracecompass.internal.tmf.analysis.xml.core.output.DataDrivenOutputEntry;
import org.eclipse.tracecompass.internal.tmf.analysis.xml.core.output.DataDrivenPresentationState;
import org.eclipse.tracecompass.internal.tmf.analysis.xml.core.output.DataDrivenTimeGraphProviderFactory;
import org.eclipse.tracecompass.tmf.analysis.xml.core.module.TmfXmlStrings;
import org.eclipse.tracecompass.tmf.analysis.xml.core.module.TmfXmlUtils;
import org.w3c.dom.Element;

/**
 * A compilation unit for XML-defined time graph views
 *
 * @author Geneviève Bastien
 */
public class TmfXmlTimeGraphViewCu implements IDataDrivenCompilationUnit {

    private final List<DataDrivenPresentationState> fValues;
    private final List<TmfXmlOutputEntryCu> fEntries;
    private final Set<String> fAnalysisIds;

    private TmfXmlTimeGraphViewCu(List<DataDrivenPresentationState> values, List<TmfXmlOutputEntryCu> entriesCu, Set<String> analysisIds) {
        fValues = values;
        fEntries = entriesCu;
        fAnalysisIds = analysisIds;
    }

    @Override
    public DataDrivenTimeGraphProviderFactory generate() {
        List<DataDrivenOutputEntry> entries = fEntries.stream()
                .map(TmfXmlOutputEntryCu::generate)
                .collect(Collectors.toList());
        return new DataDrivenTimeGraphProviderFactory(entries, fAnalysisIds, fValues);
    }

    /**
     * Compile a time graph view XML element
     *
     * @param compilationData
     *            Analysis compilation data
     * @param viewElement
     *            The XML view element
     * @return The time graph compilation unit
     */
    public static @Nullable TmfXmlTimeGraphViewCu compile(AnalysisCompilationData compilationData, Element viewElement) {

        // Compile the defined values
        List<DataDrivenPresentationState> values = new ArrayList<>();
        List<Element> childElements = TmfXmlUtils.getChildElements(viewElement, TmfXmlStrings.DEFINED_VALUE);
        for (Element element : childElements) {
            values.add(new DataDrivenPresentationState(element.getAttribute(TmfXmlStrings.VALUE), element.getAttribute(TmfXmlStrings.NAME), element.getAttribute(TmfXmlStrings.COLOR)));
        }

        Set<@NonNull String> analysisIds = TmfXmlUtils.getViewAnalysisIds(viewElement);
        List<Element> entries = TmfXmlUtils.getChildElements(viewElement, TmfXmlStrings.ENTRY_ELEMENT);

        List<TmfXmlOutputEntryCu> entriesCu = new ArrayList<>();
        for (Element entry : entries) {
            TmfXmlOutputEntryCu entryCu = TmfXmlOutputEntryCu.compile(compilationData, entry);
            if (entryCu != null) {
                entriesCu.add(entryCu);
            }
        }

        return new TmfXmlTimeGraphViewCu(values, entriesCu, analysisIds);
    }

}
