/*******************************************************************************
 * Copyright (c) 2015 École Polytechnique de Montréal
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

package org.eclipse.tracecompass.analysis.graph.core.base;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.jdt.annotation.NonNull;

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

    /**
     * Get additional information on this worker at time t. This would be
     * textual information, in the form of key, value pairs, that could be
     * displayed for instance as extra columns for this worker in a graph view.
     *
     * @return A key, value map of information this worker provides.
     * @since 2.0
     */
    default Map<String, String> getWorkerInformation() {
        return Collections.emptyMap();
    }

    /**
     * Get additional information on this worker. Unlike
     * {@link #getWorkerInformation()}, this method returns unformatted data in
     * their original type. It can be used to filter and compare data with other
     * model objects.
     *
     * @return A key, value map of information this worker provides.
     * @since 2.1
     */
    default Map<@NonNull String, @NonNull Object> getWorkerAspects() {
        Map<String, String> workerInformation = getWorkerInformation();
        if (workerInformation.isEmpty()) {
            return Collections.emptyMap();
        }
        Map<String, Object> map = new HashMap<>();
        map.putAll(workerInformation);
        return map;
    }

    /**
     * Get additional information on this worker at time t. This would be
     * textual information, in the form of key, value pairs, that could be
     * displayed for instance as a tooltip in the graph view.
     *
     * @param t
     *            Time at which to get the information
     * @return A key, value map of information this worker provides.
     * @since 2.0
     */
    default Map<String, String> getWorkerInformation(long t) {
        return Collections.emptyMap();
    }

}
