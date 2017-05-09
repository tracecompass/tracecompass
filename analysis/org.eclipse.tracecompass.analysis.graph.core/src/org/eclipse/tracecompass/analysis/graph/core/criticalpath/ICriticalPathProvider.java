/*******************************************************************************
 * Copyright (c) 2017 École Polytechnique de Montréal
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/

package org.eclipse.tracecompass.analysis.graph.core.criticalpath;

import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.tracecompass.analysis.graph.core.base.TmfGraph;

/**
 * @author Geneviève Bastien
 * @since 1.1
 */
public interface ICriticalPathProvider {

    /**
     * Get the critical path
     *
     * @return The critical path
     */
    public @Nullable TmfGraph getCriticalPath();

}
