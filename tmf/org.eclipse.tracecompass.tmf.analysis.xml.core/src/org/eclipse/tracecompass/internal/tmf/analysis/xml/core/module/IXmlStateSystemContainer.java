/*******************************************************************************
 * Copyright (c) 2014 École Polytechnique de Montréal
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Geneviève Bastien - Initial API and implementation
 *******************************************************************************/

package org.eclipse.tracecompass.internal.tmf.analysis.xml.core.module;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.tracecompass.internal.tmf.analysis.xml.core.fsm.compile.AnalysisCompilationData;
import org.eclipse.tracecompass.internal.tmf.analysis.xml.core.fsm.model.DataDrivenMappingGroup;
import org.eclipse.tracecompass.internal.tmf.analysis.xml.core.fsm.module.IAnalysisDataContainer;

/**
 * Interface that all XML defined objects who provide, use or contain state
 * system must implement in order to use the state provider model elements in
 * {@link org.eclipse.tracecompass.internal.tmf.analysis.xml.core.model} package
 *
 * @author Geneviève Bastien
 */
public interface IXmlStateSystemContainer extends ITmfXmlTopLevelElement, IAnalysisDataContainer {

    /** Root quark, to get values at the root of the state system */
    int ROOT_QUARK = -1;
    /**
     * Error quark, value taken when a state system quark query is in error.
     *
     * FIXME: Originally in the code, the -1 was used for both root quark and
     * return errors, so it has the same value as root quark, but maybe it can
     * be changed to something else -2? A quark can never be negative
     */
    int ERROR_QUARK = -1;

    /**
     * Get the compilation data for this analysis. This method should be overridden
     * only by the implementations that exist at compile time. It should not be
     * called in any other context.
     *
     * FIXME: This method is there only for the transition between the original
     * TmfXml** classes code path to the DataDriven** code path.
     *
     * @return The analysis compilation data object
     */
    default @NonNull AnalysisCompilationData getAnalysisCompilationData() {
        throw new UnsupportedOperationException("This method should be overridden by child classes who need those"); //$NON-NLS-1$
    }

    @Override
    default DataDrivenMappingGroup getMappingGroup(String id) {
        throw new UnsupportedOperationException("This method should be overridden by child classes who need those"); //$NON-NLS-1$
    }
}
