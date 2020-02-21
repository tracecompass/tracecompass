/*******************************************************************************
 * Copyright (c) 2017 École Polytechnique de Montréal
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License 2.0 which
 * accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/

package org.eclipse.tracecompass.internal.tmf.analysis.xml.core.fsm.compile;

import org.eclipse.tracecompass.internal.tmf.analysis.xml.core.fsm.model.IDataDrivenRuntimeObject;

/**
 * This interface should be implemented by classes that will transform the XML
 * structure into a intermediary structure before creating the analysis. The
 * semantic validation of the analysis will be done on this structure and it
 * will have to generate the actual analysis classes that will execute the
 * analysis on the trace.
 *
 * @author Geneviève Bastien
 */
public interface IDataDrivenCompilationUnit {

    /**
     * Generate a data-driven runtime object. At this point, the analysis should
     * have been fully validated and all required objects are available.
     *
     * @return The data-driven runtime object
     */
    public IDataDrivenRuntimeObject generate();
}
