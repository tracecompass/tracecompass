/*******************************************************************************
 * Copyright (c) 2014, 2015 École Polytechnique de Montréal
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License 2.0 which
 * accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Geneviève Bastien - Initial API and implementation
 *******************************************************************************/

package org.eclipse.tracecompass.internal.tmf.analysis.xml.core.fsm.module;

import java.util.Objects;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.tracecompass.internal.tmf.analysis.xml.core.fsm.compile.TmfXmlStateProviderCu;
import org.eclipse.tracecompass.tmf.core.statesystem.ITmfStateProvider;
import org.eclipse.tracecompass.tmf.core.statesystem.TmfStateSystemAnalysisModule;

/**
 * Analysis module for the data-driven state systems.
 *
 * @author Geneviève Bastien
 */
public class DataDrivenAnalysisModule extends TmfStateSystemAnalysisModule {

    private TmfXmlStateProviderCu fStateProviderCu;

    /**
     * Constructor
     *
     * @param analysisid
     *            The ID of the analysis
     * @param compilationUnit
     *            The state provider compilation unit to use
     */
    public DataDrivenAnalysisModule(String analysisid, TmfXmlStateProviderCu compilationUnit) {
        super();
        setId(analysisid);
        fStateProviderCu = compilationUnit;
    }

    @Override
    protected StateSystemBackendType getBackendType() {
        return StateSystemBackendType.FULL;
    }

    @Override
    protected @NonNull ITmfStateProvider createStateProvider() {
        return fStateProviderCu.generate(Objects.requireNonNull(getTrace()));
    }

}
