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

import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.tracecompass.internal.tmf.analysis.xml.core.Activator;
import org.eclipse.tracecompass.internal.tmf.analysis.xml.core.fsm.model.DataDrivenMappingGroup;
import org.eclipse.tracecompass.internal.tmf.analysis.xml.core.fsm.model.DataDrivenMappingGroup.DataDrivenMappingEntry;
import org.eclipse.tracecompass.tmf.analysis.xml.core.module.TmfXmlStrings;
import org.eclipse.tracecompass.tmf.analysis.xml.core.module.TmfXmlUtils;
import org.w3c.dom.Element;

/**
 * The compilation unit for an XML mapping group element
 *
 * @author Geneviève Bastien
 * @author Jean-Christian Kouame
 */
public class TmfXmlMappingGroupCu implements IDataDrivenCompilationUnit {

    /** Class for the entries */
    private static class TmfXmlMappingEntryCu {
        private final TmfXmlStateValueCu fKey;
        private final TmfXmlStateValueCu fVal;

        public TmfXmlMappingEntryCu(TmfXmlStateValueCu key, TmfXmlStateValueCu val) {
            fKey = key;
            fVal = val;
        }

        public DataDrivenMappingEntry generate() {
            return new DataDrivenMappingEntry(fKey.generate(), fVal.generate());
        }

    }

    private final String fId;
    private final List<TmfXmlMappingEntryCu> fEntries;

    /**
     * Constructor
     *
     * Package-private because only classes from this package can build this
     *
     * @param id
     *            The ID of the mapping group
     * @param entries
     *            The list of entries
     */
    TmfXmlMappingGroupCu(String id, List<TmfXmlMappingEntryCu> entries) {
        fEntries = entries;
        fId = id;
    }

    @Override
    public DataDrivenMappingGroup generate() {
        List<DataDrivenMappingEntry> collect = fEntries.stream()
                .map(TmfXmlMappingEntryCu::generate)
                .collect(Collectors.toList());
        DataDrivenMappingGroup mapGroup = new DataDrivenMappingGroup(fId, collect);
        return mapGroup;
    }

    /**
     * Get the ID of this mapping group
     *
     * @return The ID of the mapping group
     */
    public String getId() {
        return fId;
    }

    /**
     * @param analysisData
     *            The analysis data already compiled
     * @param mapGroupEl
     *            The XML element corresponding to this mapping group
     * @return The mapping group compilation unit or <code>null</code> if there was
     *         compilation errors.
     */
    public static @Nullable TmfXmlMappingGroupCu compile(AnalysisCompilationData analysisData, Element mapGroupEl) {
        String id = mapGroupEl.getAttribute(TmfXmlStrings.ID);

        List<Element> entries = TmfXmlUtils.getChildElements(mapGroupEl, TmfXmlStrings.ENTRY);
        List<TmfXmlMappingEntryCu> entriesCu = new ArrayList<>();
        for (Element entryElement : entries) {
            List<Element> svElements = TmfXmlUtils.getChildElements(entryElement, TmfXmlStrings.STATE_VALUE);
            if (svElements.size() != 2) {
                // TODO: Validation message here
                Activator.logError("TmfXmlMappingGroupCu: There should be 2 children state values. There were " + svElements.size()); //$NON-NLS-1$
                return null;
            }
            TmfXmlStateValueCu keySv = TmfXmlStateValueCu.compileValue(analysisData, svElements.get(0));
            if (keySv == null) {
                // TODO: Validation message here
                Activator.logError("TmfXmlMappingGroupCu: Invalid key"); //$NON-NLS-1$
                return null;
            }
            TmfXmlStateValueCu valueSv = TmfXmlStateValueCu.compileValue(analysisData, svElements.get(1));
            if (valueSv == null) {
                // TODO: Validation message here
                Activator.logError("TmfXmlMappingGroupCu: Invalid value"); //$NON-NLS-1$
                return null;
            }
            TmfXmlMappingEntryCu mapEntry = new TmfXmlMappingEntryCu(keySv, valueSv);
            entriesCu.add(mapEntry);
        }
        TmfXmlMappingGroupCu mappingGroup = new TmfXmlMappingGroupCu(id, entriesCu);
        analysisData.addMappingGroup(id, mappingGroup);
        return mappingGroup;
    }

}
