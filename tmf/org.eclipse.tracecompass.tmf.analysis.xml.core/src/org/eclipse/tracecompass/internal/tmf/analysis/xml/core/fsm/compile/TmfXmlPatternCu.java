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

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.tracecompass.internal.tmf.analysis.xml.core.Activator;
import org.eclipse.tracecompass.internal.tmf.analysis.xml.core.fsm.model.DataDrivenMappingGroup;
import org.eclipse.tracecompass.internal.tmf.analysis.xml.core.fsm.module.pattern.DataDrivenPattern;
import org.eclipse.tracecompass.internal.tmf.analysis.xml.core.pattern.stateprovider.ISegmentListener;
import org.eclipse.tracecompass.tmf.analysis.xml.core.module.TmfXmlStrings;
import org.eclipse.tracecompass.tmf.analysis.xml.core.module.TmfXmlUtils;
import org.eclipse.tracecompass.tmf.core.trace.ITmfTrace;
import org.w3c.dom.Element;

/**
 * Compilation unit for pattern analyses
 *
 * @author Geneviève Bastien
 */
public class TmfXmlPatternCu {

    private final String fProviderId;
    private final int fVersion;
    private final List<TmfXmlMappingGroupCu> fMappingGroups;
    private final TmfXmlPatternEventHandlerCu fHandlerCu;
    /** Map for stored values */
    private final Map<String, String> fStoredFields;

    private TmfXmlPatternCu(String providerId, int version, List<TmfXmlMappingGroupCu> mapGroups, TmfXmlPatternEventHandlerCu handlerCu, Map<String, String> storedFields) {
        fProviderId = providerId;
        fVersion = version;
        fMappingGroups = mapGroups;
        fHandlerCu = handlerCu;
        fStoredFields = storedFields;
    }

    /**
     * Generate a pattern from this compilation unit
     *
     * @param trace
     *            The trace for which to generate the state provider
     * @param listener
     *            The segment listener
     * @return The data driven pattern
     */
    public DataDrivenPattern generate(ITmfTrace trace, ISegmentListener listener) {
        List<DataDrivenMappingGroup> mappingGroups = fMappingGroups.stream()
                .map(TmfXmlMappingGroupCu::generate)
                .collect(Collectors.toList());
        return new DataDrivenPattern(trace, fProviderId, fVersion, fHandlerCu.generate(), mappingGroups, listener, fStoredFields);
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
    public static @Nullable TmfXmlPatternCu compile(Path file, String providerId) {
        Element spEl = TmfXmlUtils.getElementInFile(file.toAbsolutePath().toString(), TmfXmlStrings.PATTERN, providerId);
        if (spEl == null) {
            // TODO: Validation message here
            Activator.logError("XML pattern: Cannot find pattern element in file " + file); //$NON-NLS-1$
            return null;
        }
        return compile(spEl, providerId);
    }

    /**
     * Compile a state provider from an XML element
     *
     * @param patternEl
     *            The pattern element
     * @return The compilation unit corresponding to this state provider
     */
    public static @Nullable TmfXmlPatternCu compile(Element patternEl) {
        String id = patternEl.getAttribute(TmfXmlStrings.ID);
        if (id.isEmpty()) {
            // TODO: Validation message here
            Activator.logError("XML pattern: The pattern must have an 'id' attribute"); //$NON-NLS-1$
            return null;
        }
        return compile(patternEl, id);
    }

    /**
     * Compile a state provider from an XML element
     *
     * @param patternEl
     *            The pattern element
     * @param providerId
     *            The ID of the provider to build
     * @return The compilation unit corresponding to this state provider
     */
    private static @Nullable TmfXmlPatternCu compile(Element patternEl, String providerId) {

        AnalysisCompilationData analysisData = new AnalysisCompilationData();

        int version;
        try {
            version = Integer.parseInt(patternEl.getAttribute(TmfXmlStrings.VERSION));
        } catch (NumberFormatException e) {
            // TODO: Validation message here
            Activator.logError("XML pattern: The version is not a parseable integer"); //$NON-NLS-1$
            return null;
        }

        /* parser for defined Values */
        List<Element> childElements = TmfXmlUtils.getChildElements(patternEl, TmfXmlStrings.DEFINED_VALUE);
        for (Element element : childElements) {
            analysisData.addDefinedValue(element.getAttribute(TmfXmlStrings.NAME), element.getAttribute(TmfXmlStrings.VALUE));
        }

        /* parser for defined Fields */
        childElements = TmfXmlUtils.getChildElements(patternEl, TmfXmlStrings.STORED_FIELDS);
        Map<String, String> storedFields = new HashMap<>();
        for (Element element : childElements) {
            String key = element.getAttribute(TmfXmlStrings.ALIAS);
            String id = element.getAttribute(TmfXmlStrings.ID);
            storedFields.put(id, key.isEmpty() ? id : key);
        }

        /* parser for the locations */
        childElements = TmfXmlUtils.getChildElements(patternEl, TmfXmlStrings.LOCATION);
        for (Element element : childElements) {
            TmfXmlLocationCu.compile(analysisData, element);
        }

        /* parser for the mapping groups */
        List<TmfXmlMappingGroupCu> mapGroups = new ArrayList<>();
        childElements = TmfXmlUtils.getChildElements(patternEl, TmfXmlStrings.MAPPING_GROUP);
        for (Element map : childElements) {
            TmfXmlMappingGroupCu compile = TmfXmlMappingGroupCu.compile(analysisData, map);
            if (compile == null) {
                return null;
            }
            mapGroups.add(compile);
        }

        /* parser for the event handlers */
        childElements = TmfXmlUtils.getChildElements(patternEl, TmfXmlStrings.PATTERN_HANDLER);
        if (childElements.size() != 1) {
            // TODO: Validation message here
            Activator.logError("XML pattern: Only one pattern handler should be present"); //$NON-NLS-1$
            return null;
        }
        TmfXmlPatternEventHandlerCu handlerCu = TmfXmlPatternEventHandlerCu.compile(analysisData, childElements.get(0));
        if (handlerCu == null) {
            return null;
        }

        return new TmfXmlPatternCu(providerId, version, mapGroups, handlerCu, storedFields);
    }


}
