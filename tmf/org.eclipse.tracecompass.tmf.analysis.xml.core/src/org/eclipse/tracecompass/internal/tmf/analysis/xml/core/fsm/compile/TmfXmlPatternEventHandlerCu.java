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
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.tracecompass.internal.tmf.analysis.xml.core.Activator;
import org.eclipse.tracecompass.internal.tmf.analysis.xml.core.fsm.compile.TmfXmlFsmStateCu.TmfXmlFsmCu;
import org.eclipse.tracecompass.internal.tmf.analysis.xml.core.fsm.model.DataDrivenFsm;
import org.eclipse.tracecompass.internal.tmf.analysis.xml.core.fsm.model.DataDrivenPatternEventHandler;
import org.eclipse.tracecompass.tmf.analysis.xml.core.module.TmfXmlStrings;
import org.eclipse.tracecompass.tmf.analysis.xml.core.module.TmfXmlUtils;
import org.w3c.dom.Element;

/**
 * The compilation unit for XML pattern event handler
 *
 * @author Geneviève Bastien
 */
public class TmfXmlPatternEventHandlerCu implements IDataDrivenCompilationUnit {

    private List<TmfXmlFsmCu> fFsms;
    private List<String> fInitials;

    private TmfXmlPatternEventHandlerCu(List<TmfXmlFsmCu> fsms, List<String> initials) {
        fFsms = fsms;
        fInitials = initials;
    }

    @Override
    public DataDrivenPatternEventHandler generate() {
        Map<String, DataDrivenFsm> fsms = fFsms.stream()
                .map(TmfXmlFsmCu::generate)
                .collect(Collectors.toMap(DataDrivenFsm::getId, fsm -> fsm));
        List<DataDrivenFsm> initials = new ArrayList<>();
        for (String initial : fInitials) {
            initials.add(Objects.requireNonNull(fsms.get(initial)));
        }

        return new DataDrivenPatternEventHandler(fsms.values(), initials);
    }

    /**
     * Compile a pattern event handler compilation unit from an XML element
     *
     * @param analysisData
     *            The analysis data already compiled
     * @param element
     *            The event handler XML element
     * @return The event handler compilation unit, or <code>null</code> if there was
     *         compilation errors
     */
    public static @Nullable TmfXmlPatternEventHandlerCu compile(AnalysisCompilationData analysisData, Element element) {

        List<Element> elements = TmfXmlUtils.getChildElements(element, TmfXmlStrings.TEST);
        for (Element testElement : elements) {
            TmfXmlConditionCu test = TmfXmlConditionCu.compileNamedCondition(analysisData, testElement);
            if (test == null) {
                return null;
            }
        }

        elements = TmfXmlUtils.getChildElements(element, TmfXmlStrings.ACTION);
        for (Element actionElement : elements) {
            TmfXmlActionCu action = TmfXmlActionCu.compileNamedAction(analysisData, actionElement);
            if (action == null) {
                return null;
            }
        }

        elements = TmfXmlUtils.getChildElements(element, TmfXmlStrings.ACTION);
        for (Element actionElement : elements) {
            TmfXmlActionCu action = TmfXmlActionCu.compileNamedAction(analysisData, actionElement);
            if (action == null) {
                return null;
            }
        }

        List<TmfXmlFsmCu> fsms = new ArrayList<>();
        elements = TmfXmlUtils.getChildElements(element, TmfXmlStrings.FSM);
        for (Element fsmElement : elements) {
            TmfXmlFsmCu fsm = TmfXmlFsmStateCu.compileFsm(analysisData, fsmElement);
            if (fsm == null) {
                return null;
            }
            fsms.add(fsm);
        }

        String initialFsm = element.getAttribute(TmfXmlStrings.INITIAL);
        List<String> initials = new ArrayList<>();
        if (!initialFsm.isEmpty()) {
            for (String initial : initialFsm.split(TmfXmlStrings.AND_SEPARATOR)) {
                TmfXmlFsmStateCu fsm = analysisData.getFsm(initial);
                if (fsm == null) {
                    // TODO: Validation message here
                    Activator.logError("XML pattern handler: Undefined initial FSM: " + initial); //$NON-NLS-1$
                    return null;
                }
                initials.add(initial);
            }
        }

        return new TmfXmlPatternEventHandlerCu(fsms, initials);
    }

}
