/*******************************************************************************
 * Copyright (c) 2019 École Polytechnique de Montréal
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/

package org.eclipse.tracecompass.internal.tmf.analysis.xml.core.fsm.compile;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.tracecompass.internal.tmf.analysis.xml.core.output.DataDrivenOutputEntry;
import org.eclipse.tracecompass.internal.tmf.analysis.xml.core.output.DataDrivenXYProviderFactory;
import org.eclipse.tracecompass.tmf.analysis.xml.core.module.TmfXmlStrings;
import org.eclipse.tracecompass.tmf.analysis.xml.core.module.TmfXmlUtils;
import org.w3c.dom.Element;

/**
 * Compilation unit for XY views
 *
 * @author Geneviève Bastien
 */
public class TmfXmlXYViewCu implements IDataDrivenCompilationUnit {

    private final List<TmfXmlOutputEntryCu> fEntries;
    private final Set<String> fAnalysisIds;

    private TmfXmlXYViewCu(List<TmfXmlOutputEntryCu> entriesCu, Set<String> analysisIds) {
        fEntries = entriesCu;
        fAnalysisIds = analysisIds;
    }

    @Override
    public DataDrivenXYProviderFactory generate() {
        List<DataDrivenOutputEntry> entries = fEntries.stream()
                .map(TmfXmlOutputEntryCu::generate)
                .collect(Collectors.toList());
        return new DataDrivenXYProviderFactory(entries, fAnalysisIds);
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
    public static @Nullable TmfXmlXYViewCu compile(AnalysisCompilationData compilationData, Element viewElement) {

        Set<String> analysisIds = TmfXmlUtils.getViewAnalysisIds(viewElement);
        List<Element> entries = TmfXmlUtils.getChildElements(viewElement, TmfXmlStrings.ENTRY_ELEMENT);

        List<TmfXmlOutputEntryCu> entriesCu = new ArrayList<>();
        for (Element entry : entries) {
            TmfXmlOutputEntryCu entryCu = TmfXmlOutputEntryCu.compile(compilationData, entry);
            if (entryCu != null) {
                entriesCu.add(entryCu);
            }
        }

        return new TmfXmlXYViewCu(entriesCu, analysisIds);
    }

}
