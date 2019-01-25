/*******************************************************************************
 * Copyright (c) 2014 Ecole Polytechnique de Montreal
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Florian Wininger - Initial API and implementation
 ******************************************************************************/

package org.eclipse.tracecompass.internal.tmf.analysis.xml.core.model;

import java.util.List;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.tracecompass.internal.tmf.analysis.xml.core.fsm.compile.TmfXmlStateSystemPathCu;
import org.eclipse.tracecompass.internal.tmf.analysis.xml.core.fsm.model.DataDrivenStateSystemPath;
import org.eclipse.tracecompass.internal.tmf.analysis.xml.core.module.IXmlStateSystemContainer;
import org.eclipse.tracecompass.tmf.analysis.xml.core.module.TmfXmlStrings;
import org.eclipse.tracecompass.tmf.analysis.xml.core.module.TmfXmlUtils;
import org.eclipse.tracecompass.tmf.core.event.ITmfEvent;
import org.w3c.dom.Element;

/**
 * This Class implements a Location in the XML-defined state system, ie a named
 * shortcut to reach a given attribute, used in state attributes
 *
 * <pre>
 * example:
 *  <location id="CurrentCPU">
 *    <stateAttribute type="constant" value="CPUs" />
 *    <stateAttribute type="eventField" name="cpu" />
 *    ...
 *  </location>
 * </pre>
 *
 * @author Florian Wininger
 */
public class TmfXmlLocation {

    /** Path in the State System */
    private final DataDrivenStateSystemPath fPath;

    /** ID : name of the location */
    private final String fId;
    private final IXmlStateSystemContainer fContainer;

    /**
     * Constructor
     *
     * @param modelFactory
     *            The factory used to create XML model elements
     * @param location
     *            XML node element
     * @param container
     *            The state system container this location belongs to
     */
    public TmfXmlLocation(ITmfXmlModelFactory modelFactory, Element location, IXmlStateSystemContainer container) {
        String id = location.getAttribute(TmfXmlStrings.ID);
        fId = id;
        fContainer = container;

        List<@NonNull Element> childElements = TmfXmlUtils.getChildElements(location, TmfXmlStrings.STATE_ATTRIBUTE);
        TmfXmlStateSystemPathCu locationCu = TmfXmlStateSystemPathCu.compile(container.getAnalysisCompilationData(), childElements);
        if (locationCu == null) {
            throw new NullPointerException("Problem here, but next patch will remove it"); //$NON-NLS-1$
        }
        fPath = locationCu.generate();
    }

    /**
     * Get the id of the location
     *
     * @return The id of a location
     */
    public String getId() {
        return fId;
    }

    /**
     * Get the quark for the path represented by this location
     *
     * @param event
     *            The event being handled
     *
     * @param startQuark
     *            The starting quark for relative search, use
     *            {@link IXmlStateSystemContainer#ROOT_QUARK} for the root of
     *            the attribute tree
     * @param scenarioInfo
     *            The active scenario details. Or <code>null</code> if there is
     *            no scenario.
     * @return The quark at the leaf of the path
     */
    public int getLocationQuark(@Nullable ITmfEvent event, int startQuark, @Nullable TmfXmlScenarioInfo scenarioInfo) {
        int quark = startQuark;
        return fPath.getQuark(event, quark, scenarioInfo, fContainer);
    }

    /**
     * Get the quark for the path represented by this location
     *
     * @param startQuark
     *            The starting quark for relative search, use
     *            {@link IXmlStateSystemContainer#ROOT_QUARK} for the root of
     *            the attribute tree
     * @param scenarioInfo
     *            The active scenario details. Or <code>null</code> if there is
     *            no scenario.
     * @return The quark at the leaf of the path
     */
    public int getLocationQuark(int startQuark, @Nullable TmfXmlScenarioInfo scenarioInfo) {
        int quark = startQuark;
        return fPath.getQuark(quark, fContainer);
    }

    @Override
    public String toString() {
        return "TmfXmlLocation " + fId + ": " + fPath; //$NON-NLS-1$ //$NON-NLS-2$
    }

}