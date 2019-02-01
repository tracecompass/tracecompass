/*******************************************************************************
 * Copyright (c) 2017 École Polytechnique de Montréal
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

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.tracecompass.internal.tmf.analysis.xml.core.fsm.model.DataDrivenEventHandler;
import org.eclipse.tracecompass.internal.tmf.analysis.xml.core.fsm.model.DataDrivenAction;
import org.eclipse.tracecompass.tmf.analysis.xml.core.module.TmfXmlStrings;
import org.eclipse.tracecompass.tmf.analysis.xml.core.module.TmfXmlUtils;
import org.w3c.dom.Element;

/**
 * The compilation unit for XML event handlers from the state provider analysis
 *
 * @author Geneviève Bastien
 * @author Florian Wininger
 */
public class TmfXmlEventHandlerCu implements IDataDrivenCompilationUnit {

    private final List<TmfXmlActionCu> fStateChanges;
    private final String fEventName;

    /**
     * Constructor
     *
     * Package-private because only classes from this package can build this
     *
     * @param eventName
     *            The name of the event to handler
     * @param stateChanges
     *            The list of state change compilation units
     */
    TmfXmlEventHandlerCu(String eventName, List<TmfXmlActionCu> stateChanges) {
        fStateChanges = stateChanges;
        fEventName = eventName;
    }

    @Override
    public DataDrivenEventHandler generate() {
        List<DataDrivenAction> collect = fStateChanges.stream()
                .map(TmfXmlActionCu::generate)
                .collect(Collectors.toList());
        return new DataDrivenEventHandler(fEventName, collect);
    }

    /**
     * Compile an event handler compilation unit from an XML element
     *
     * @param analysisData
     *            The analysis data already compiled
     * @param eventHandler
     *            The event handler XML element
     * @return The event handler compilation unit, or <code>null</code> if there was
     *         compilation errors
     */
    public static @Nullable TmfXmlEventHandlerCu compile(AnalysisCompilationData analysisData, Element eventHandler) {
        String eventName = eventHandler.getAttribute(TmfXmlStrings.HANDLER_EVENT_NAME);

        List<TmfXmlActionCu> stateChanges = new ArrayList<>();
        List<@NonNull Element> childElements = TmfXmlUtils.getChildElements(eventHandler, TmfXmlStrings.STATE_CHANGE);
        /* load state changes */
        for (Element childElem : childElements) {
            TmfXmlActionCu compile = TmfXmlActionCu.compile(analysisData, childElem);
            if (compile == null) {
                return null;
            }
            stateChanges.add(compile);
        }
        return new TmfXmlEventHandlerCu(eventName, stateChanges);
    }

}
