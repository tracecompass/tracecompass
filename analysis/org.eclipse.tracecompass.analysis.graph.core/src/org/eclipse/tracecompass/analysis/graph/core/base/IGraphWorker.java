/*******************************************************************************
 * Copyright (c) 2015 École Polytechnique de Montréal
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Geneviève Bastien - Initial API and implementation
 *******************************************************************************/

package org.eclipse.tracecompass.analysis.graph.core.base;

/**
 * Interface that the objects in a graph may implement
 *
 * @author Geneviève Bastien
 */
public interface IGraphWorker {

    /**
     * Get the host ID of the trace this worker belongs to
     *
     * @return The host ID of the trace this worker belongs to
     */
    String getHostId();

}
