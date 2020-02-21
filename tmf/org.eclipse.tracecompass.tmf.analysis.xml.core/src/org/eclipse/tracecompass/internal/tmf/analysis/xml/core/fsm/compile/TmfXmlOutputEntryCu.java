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
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.tracecompass.internal.tmf.analysis.xml.core.Activator;
import org.eclipse.tracecompass.internal.tmf.analysis.xml.core.fsm.model.DataDrivenStateSystemPath;
import org.eclipse.tracecompass.internal.tmf.analysis.xml.core.output.DataDrivenOutputEntry;
import org.eclipse.tracecompass.internal.tmf.analysis.xml.core.output.DataDrivenXYDataProvider.DisplayType;
import org.eclipse.tracecompass.tmf.analysis.xml.core.module.TmfXmlStrings;
import org.eclipse.tracecompass.tmf.analysis.xml.core.module.TmfXmlUtils;
import org.w3c.dom.Element;

/**
 * Compilation unit for XML time graph entries
 *
 * @author Geneviève Bastien
 */
public class TmfXmlOutputEntryCu implements IDataDrivenCompilationUnit {

    private final List<TmfXmlOutputEntryCu> fChildrenEntries;
    private final String fPath;
    private final @Nullable String fAnalysisId;
    private final boolean fDisplayText;
    private final @Nullable TmfXmlStateSystemPathCu fDisplayCu;
    private final @Nullable TmfXmlStateSystemPathCu fIdCu;
    private final @Nullable TmfXmlStateSystemPathCu fParentCu;
    private final @Nullable TmfXmlStateSystemPathCu fNameCu;
    private final DisplayType fDisplayType;

    private TmfXmlOutputEntryCu(List<TmfXmlOutputEntryCu> childrenCu, String path,
            @Nullable String analysisId, boolean displayText,
            @Nullable TmfXmlStateSystemPathCu displayCu,
            @Nullable TmfXmlStateSystemPathCu idCu,
            @Nullable TmfXmlStateSystemPathCu parentCu,
            @Nullable TmfXmlStateSystemPathCu nameCu,
            DisplayType displayType) {
        fChildrenEntries = childrenCu;
        fPath = path;
        fAnalysisId = analysisId;
        fDisplayText = displayText;
        fDisplayCu = displayCu;
        fIdCu = idCu;
        fParentCu = parentCu;
        fNameCu = nameCu;
        fDisplayType = displayType;
    }

    @Override
    public DataDrivenOutputEntry generate() {
        List<DataDrivenOutputEntry> entries = fChildrenEntries.stream()
                .map(TmfXmlOutputEntryCu::generate)
                .collect(Collectors.toList());
        DataDrivenStateSystemPath display = fDisplayCu != null ? fDisplayCu.generate() : null;
        DataDrivenStateSystemPath id = fIdCu != null ? fIdCu.generate() : null;
        DataDrivenStateSystemPath parent = fParentCu != null ? fParentCu.generate() : null;
        DataDrivenStateSystemPath name = fNameCu != null ? fNameCu.generate() : null;
        return new DataDrivenOutputEntry(entries, fPath, fAnalysisId, fDisplayText,
                display, id, parent, name, fDisplayType);
    }

    /**
     * Compile a time graph view entry
     *
     * @param compilationData
     *            The analysis compilation data
     * @param entryEl
     *            The XML element
     * @return The time graph entry compilation unit or <code>null</code> if the
     *         entry did not compile properly.
     */
    public static @Nullable TmfXmlOutputEntryCu compile(AnalysisCompilationData compilationData, Element entryEl) {

        // Get the path in the state system
        String path = entryEl.getAttribute(TmfXmlStrings.PATH);
        if (path.isEmpty()) {
            path = TmfXmlStrings.WILDCARD;
        }

        /*
         * Make sure the XML element has either a display attribute or entries,
         * otherwise issue a warning
         */

        List<Element> displayElements = TmfXmlUtils.getChildElements(entryEl, TmfXmlStrings.DISPLAY_ELEMENT);
        List<Element> entryElements = TmfXmlUtils.getChildElements(entryEl, TmfXmlStrings.ENTRY_ELEMENT);

        if (displayElements.isEmpty() && entryElements.isEmpty()) {
            // TODO: Validation message here
            Activator.logWarning(String.format("XML view: entry for %s should have either a display element or entry elements", path)); //$NON-NLS-1$
            return null;
        }

        // Compile children entries
        List<TmfXmlOutputEntryCu> childrenCu = new ArrayList<>();
        for (Element childEl : entryElements) {
            TmfXmlOutputEntryCu childCu = compile(compilationData, childEl);
            if (childCu != null) {
                childrenCu.add(childCu);
            }
        }

        // Compile the entry's specific data
        // The display element
        TmfXmlStateSystemPathCu displayCu = null;
        if (!displayElements.isEmpty()) {
            if (displayElements.size() > 1) {
                Activator.logWarning(String.format("XML view: entry for %s should have at most one 'display' element", path)); //$NON-NLS-1$
            }
            displayCu = TmfXmlStateSystemPathCu.compile(compilationData, Collections.singletonList(displayElements.get(0)));
        }

        // The id element
        TmfXmlStateSystemPathCu idCu = null;
        List<Element> elements = TmfXmlUtils.getChildElements(entryEl, TmfXmlStrings.ID_ELEMENT);
        if (!elements.isEmpty()) {
            if (elements.size() > 1) {
                Activator.logWarning(String.format("XML view: entry for %s should have at most one 'id' element", path)); //$NON-NLS-1$
            }
            idCu = TmfXmlStateSystemPathCu.compile(compilationData, Collections.singletonList(elements.get(0)));
        }

        // The parent element
        TmfXmlStateSystemPathCu parentCu = null;
        elements = TmfXmlUtils.getChildElements(entryEl, TmfXmlStrings.PARENT_ELEMENT);
        if (!elements.isEmpty()) {
            if (elements.size() > 1) {
                Activator.logWarning(String.format("XML view: entry for %s should have at most one 'parent' element", path)); //$NON-NLS-1$
            }
            parentCu = TmfXmlStateSystemPathCu.compile(compilationData, Collections.singletonList(elements.get(0)));
        }

        // The name element
        TmfXmlStateSystemPathCu nameCu = null;
        elements = TmfXmlUtils.getChildElements(entryEl, TmfXmlStrings.NAME_ELEMENT);
        if (!elements.isEmpty()) {
            if (elements.size() > 1) {
                Activator.logWarning(String.format("XML view: entry for %s should have at most one 'name' element", path)); //$NON-NLS-1$
            }
            nameCu = TmfXmlStateSystemPathCu.compile(compilationData, Collections.singletonList(elements.get(0)));
        }

        // Get the state system to use to populate those entries, by default, it
        // is the same as the parent
        String analysisId = entryEl.getAttribute(TmfXmlStrings.ANALYSIS_ID);
        if (analysisId.isEmpty()) {
            analysisId = null;
        }

        // Get whether to display the text, applies to time graphs
        boolean displayText = Boolean.parseBoolean(entryEl.getAttribute(TmfXmlStrings.DISPLAY_TEXT));

        // Get the type of display, applies to XY entries
        String displayTypeStr = entryEl.getAttribute(TmfXmlStrings.DISPLAY_TYPE);
        DisplayType displayType = DisplayType.ABSOLUTE;
        if (displayTypeStr.equalsIgnoreCase(TmfXmlStrings.DISPLAY_TYPE_DELTA)) {
            displayType = DisplayType.DELTA;
        }

        return new TmfXmlOutputEntryCu(childrenCu, path, analysisId, displayText, displayCu, idCu, parentCu, nameCu, displayType);
    }

}
