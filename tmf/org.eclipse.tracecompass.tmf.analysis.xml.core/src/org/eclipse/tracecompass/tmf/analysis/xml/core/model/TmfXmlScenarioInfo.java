/*******************************************************************************
 * Copyright (c) 2016 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.eclipse.tracecompass.tmf.analysis.xml.core.model;

/**
 * This class gives basic details about a scenario (quark, scenarioName, ...)
 *
 * @author Jean-Christian Kouame
 * @since 2.0
 */
public class TmfXmlScenarioInfo {
    private final String fScenarioName;
    private final int fQuark;
    private String fActiveState;

    /**
     * Constructor
     *
     * @param scenarioName
     *            The scenario name
     * @param activeState
     *            The active state
     * @param quark
     *            The scenario quark
     */
    public TmfXmlScenarioInfo(String scenarioName, String activeState, int quark) {
        fScenarioName = scenarioName;
        fActiveState = activeState;
        fQuark = quark;
    }

    /**
     * Set the active state
     *
     * @param activeState
     *            The active state
     */
    public void setActiveState(String activeState) {
        fActiveState = activeState;
    }

    /**
     * Get the scenario quark
     *
     * @return The quark
     */
    public int getQuark() {
        return fQuark;
    }

    /**
     * Get the scenario name
     *
     * @return The name
     */
    public String getScenarioName() {
        return fScenarioName;
    }

    /**
     * Get the scenario active state
     *
     * @return The active state
     */
    public String getActiveState() {
        return fActiveState;
    }
}
