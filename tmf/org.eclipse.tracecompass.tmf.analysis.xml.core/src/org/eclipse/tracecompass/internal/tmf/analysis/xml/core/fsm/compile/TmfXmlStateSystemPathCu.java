/*******************************************************************************
 * Copyright (c) 2018 École Polytechnique de Montréal
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/

package org.eclipse.tracecompass.internal.tmf.analysis.xml.core.fsm.compile;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.tracecompass.internal.tmf.analysis.xml.core.fsm.model.DataDrivenStateSystemPath;
import org.eclipse.tracecompass.internal.tmf.analysis.xml.core.fsm.model.IBaseQuarkProvider;
import org.eclipse.tracecompass.internal.tmf.analysis.xml.core.fsm.model.values.DataDrivenValue;
import org.eclipse.tracecompass.internal.tmf.analysis.xml.core.fsm.model.values.DataDrivenValueSelf;
import org.w3c.dom.Element;

/**
 * The compilation unit for path in the state system, ie a list of attributes
 * with a base quark
 *
 * @author Geneviève Bastien
 */
public class TmfXmlStateSystemPathCu implements IDataDrivenCompilationUnit {

    private final IBaseQuarkProvider fQuarkProvider;
    private final List<TmfXmlStateValueCu> fAttributes;

    /**
     * Constructor
     *
     * @param attribs
     *            The list of state value whose resolved value will represent a
     *            level of attribute in the state system
     * @param quarkProvider
     *            The base quark provider, ie the quark whose value will be at
     *            the root of the query
     */
    public TmfXmlStateSystemPathCu(List<TmfXmlStateValueCu> attribs, IBaseQuarkProvider quarkProvider) {
        fAttributes = attribs;
        fQuarkProvider = quarkProvider;
    }

    @Override
    public DataDrivenStateSystemPath generate() {
        // Ignore the self value in the path as it returns the quark itself
        List<DataDrivenValue> attributes = fAttributes.stream()
                .map(TmfXmlStateValueCu::generate)
                .filter(v -> (!(v instanceof DataDrivenValueSelf)))
                .collect(Collectors.toList());
        return new DataDrivenStateSystemPath(attributes, fQuarkProvider);
    }

    /**
     * Compile a state system path from a list of XML elements
     *
     * @param analysisData
     *            The analysis data already compiled
     * @param elements
     *            The state attribute XML elements to compile
     * @return The state system path compilation unit or <code>null</code> if
     *         there was a compilation error
     */
    public static @Nullable TmfXmlStateSystemPathCu compile(AnalysisCompilationData analysisData, List<Element> elements) {
        IBaseQuarkProvider quarkProvider = IBaseQuarkProvider.IDENTITY_BASE_QUARK;
        List<TmfXmlStateValueCu> subAttribs = new ArrayList<>();
        for (Element subAttributeNode : elements) {
            List<TmfXmlStateValueCu> subAttrib = TmfXmlStateValueCu.compileAttribute(analysisData, subAttributeNode);
            if (subAttrib == null) {
                return null;
            }
            // Replace any {@link TmfXmlStateValueCu#CURRENT_SCENARIO_QUARK}
            // by a scenario quark provider
            if (subAttrib.size() == 1) {
                if (subAttrib.get(0) == TmfXmlStateValueCu.CURRENT_SCENARIO_QUARK) {
                    quarkProvider = IBaseQuarkProvider.CURRENT_SCENARIO_BASE_QUARK;
                    continue;
                }
            }
            subAttribs.addAll(subAttrib);
        }
        return new TmfXmlStateSystemPathCu(subAttribs, quarkProvider);
    }

}
