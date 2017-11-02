/*******************************************************************************
 * Copyright (c) 2017 École Polytechnique de Montréal
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/

package org.eclipse.tracecompass.internal.tmf.analysis.xml.core.fsm.model.values;

import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.tracecompass.internal.tmf.analysis.xml.core.Activator;
import org.eclipse.tracecompass.internal.tmf.analysis.xml.core.fsm.model.DataDrivenScenarioInfo;
import org.eclipse.tracecompass.internal.tmf.analysis.xml.core.fsm.module.IAnalysisDataContainer;
import org.eclipse.tracecompass.statesystem.core.ITmfStateSystem;
import org.eclipse.tracecompass.statesystem.core.statevalue.ITmfStateValue;
import org.eclipse.tracecompass.tmf.core.event.ITmfEvent;
import org.eclipse.tracecompass.tmf.core.statesystem.TmfAttributePool;

/**
 * A value that resolves to an available attribute in an attribute pool
 *
 * @author Geneviève Bastien
 */
public class DataDrivenValuePool extends DataDrivenValue {

    /**
     * Constructor
     */
    public DataDrivenValuePool() {
        super(null, ITmfStateValue.Type.NULL);
    }

    @Override
    protected @Nullable Object resolveValue(int baseQuark, IAnalysisDataContainer container) {
        throw new IllegalArgumentException("Attribute type pool: attribute pools can only be used in a context of scenarios."); //$NON-NLS-1$
    }

    @Override
    protected @Nullable Object resolveValue(ITmfEvent event, int baseQuark, DataDrivenScenarioInfo scenarioInfo, IAnalysisDataContainer container) {
        TmfAttributePool pool = container.getAttributePool(baseQuark);
        if (pool == null) {
            Activator.logWarning("Attribute type pool: No pool was assigned for quark"); //$NON-NLS-1$
            return null;
        }
        int quark = scenarioInfo.getAttributeFromPool(pool);
        ITmfStateSystem ss = container.getStateSystem();
        return ss.getAttributeName(quark);
    }

    @Override
    public String toString() {
        return "DataDrivenValuePool"; //$NON-NLS-1$
    }

}
