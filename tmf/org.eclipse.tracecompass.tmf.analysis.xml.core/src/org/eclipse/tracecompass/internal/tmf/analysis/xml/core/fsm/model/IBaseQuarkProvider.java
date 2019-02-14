/*******************************************************************************
 * Copyright (c) 2018 École Polytechnique de Montréal
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/

package org.eclipse.tracecompass.internal.tmf.analysis.xml.core.fsm.model;

import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.tracecompass.internal.tmf.analysis.xml.core.fsm.model.runtime.DataDrivenScenarioInfo;

import java.util.Objects;

/**
 * @author gbastien
 *
 */
@FunctionalInterface
public interface IBaseQuarkProvider {

    /**
     * Get a base quark for a scenario, given a base quark
     *
     * @param quark
     *            The base quark from which to get the quark
     * @param scenarioInfo
     *            The scenario data information
     * @return The base quark
     */
    int getBaseQuark(int quark, @Nullable DataDrivenScenarioInfo scenarioInfo);

    /**
     * Base quark provider returning the base quark as the base
     */
    static IBaseQuarkProvider IDENTITY_BASE_QUARK = (q, s) -> q;

    /**
     * Base quark provider that returning the current scenario's quark
     */
    static IBaseQuarkProvider CURRENT_SCENARIO_BASE_QUARK = (q, s) -> Objects.requireNonNull(s).getQuark();
}
