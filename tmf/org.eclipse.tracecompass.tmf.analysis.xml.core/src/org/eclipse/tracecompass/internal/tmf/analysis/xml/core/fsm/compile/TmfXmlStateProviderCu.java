/*******************************************************************************
 * Copyright (c) 2017 École Polytechnique de Montréal
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/

package org.eclipse.tracecompass.internal.tmf.analysis.xml.core.fsm.compile;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.tracecompass.internal.tmf.analysis.xml.core.Activator;
import org.eclipse.tracecompass.internal.tmf.analysis.xml.core.fsm.model.DataDrivenEventHandler;
import org.eclipse.tracecompass.internal.tmf.analysis.xml.core.fsm.model.DataDrivenMappingGroup;
import org.eclipse.tracecompass.internal.tmf.analysis.xml.core.fsm.module.DataDrivenStateProvider;
import org.eclipse.tracecompass.tmf.analysis.xml.core.module.TmfXmlStrings;
import org.eclipse.tracecompass.tmf.analysis.xml.core.module.TmfXmlUtils;
import org.eclipse.tracecompass.tmf.core.trace.ITmfTrace;
import org.w3c.dom.Element;

/**
 * The compilation unit for the state provider XML element.
 *
 * @author Geneviève Bastien
 * @author Florian Wininger
 */
public class TmfXmlStateProviderCu {

    private final List<TmfXmlEventHandlerCu> fEventHandlers;
    private final List<TmfXmlMappingGroupCu> fMapGroups;
    private final String fProviderId;
    private final int fVersion;

    /**
     * Constructor
     *
     * Package-private because only classes from this package can build this
     *
     * @param providerId
     *            The ID of the state provider
     * @param version
     *            The version of the state provider
     * @param mapGroups
     *            The mapping groups compilation units
     * @param eventHandlers
     *            The event handlers compilation units
     */
    TmfXmlStateProviderCu(String providerId, int version, List<TmfXmlMappingGroupCu> mapGroups, List<TmfXmlEventHandlerCu> eventHandlers) {
        fEventHandlers = eventHandlers;
        fMapGroups = mapGroups;
        fProviderId = providerId;
        fVersion = version;
    }

    /**
     * Generate a state provider from this compilation unit
     *
     * @param trace
     *            The for which to generate the state provider
     * @return The data-driven state provider
     */
    public DataDrivenStateProvider generate(ITmfTrace trace) {
        List<DataDrivenEventHandler> eventHandlers = fEventHandlers.stream()
                .map(TmfXmlEventHandlerCu::generate)
                .collect(Collectors.toList());
        List<DataDrivenMappingGroup> mappingGroups = fMapGroups.stream()
                .map(TmfXmlMappingGroupCu::generate)
                .collect(Collectors.toList());
        return new DataDrivenStateProvider(trace, fProviderId, fVersion, eventHandlers, mappingGroups);
    }

    /**
     * Compile a state provider from an XML file
     *
     * @param file
     *            The path to the XML file
     * @param providerId
     *            The ID of the provider to build
     * @return The compilation unit corresponding to this state provider
     */
    public static @Nullable TmfXmlStateProviderCu compile(Path file, String providerId) {
        Element spEl = TmfXmlUtils.getElementInFile(file.toAbsolutePath().toString(), TmfXmlStrings.STATE_PROVIDER, providerId);
        if (spEl == null) {
            // TODO: Validation message here
            Activator.logError("XmlStateProvider: Cannot find state provider element in file " + file); //$NON-NLS-1$
            return null;
        }
        AnalysisCompilationData analysisData = new AnalysisCompilationData();

        int version;
        try {
            version = Integer.parseInt(spEl.getAttribute(TmfXmlStrings.VERSION));
        } catch (NumberFormatException e) {
            // TODO: Validation message here
            Activator.logError("XmlStateProvider: The version is not a parseable integer"); //$NON-NLS-1$
            return null;
        }

        /* parser for defined Values */
        List<Element> childElements = TmfXmlUtils.getChildElements(spEl, TmfXmlStrings.DEFINED_VALUE);
        for (Element element : childElements) {
            analysisData.addDefinedValue(element.getAttribute(TmfXmlStrings.NAME), element.getAttribute(TmfXmlStrings.VALUE));
        }

        /* parser for the locations */
        childElements = TmfXmlUtils.getChildElements(spEl, TmfXmlStrings.LOCATION);
        for (Element element : childElements) {
            TmfXmlLocationCu.compile(analysisData, element);
        }

        /* parser for the mapping groups */
        List<TmfXmlMappingGroupCu> mapGroups = new ArrayList<>();
        childElements = TmfXmlUtils.getChildElements(spEl, TmfXmlStrings.MAPPING_GROUP);
        for (Element map : childElements) {
            TmfXmlMappingGroupCu compile = TmfXmlMappingGroupCu.compile(analysisData, map);
            if (compile == null) {
                return null;
            }
            mapGroups.add(compile);
        }

        /* parser for the event handlers */
        childElements = TmfXmlUtils.getChildElements(spEl, TmfXmlStrings.EVENT_HANDLER);
        List<TmfXmlEventHandlerCu> handlers = new ArrayList<>();
        for (Element element : childElements) {
            TmfXmlEventHandlerCu compile = TmfXmlEventHandlerCu.compile(analysisData, element);
            if (compile == null) {
                return null;
            }
            handlers.add(compile);
        }
        return new TmfXmlStateProviderCu(providerId, version, mapGroups, handlers);
    }

}
