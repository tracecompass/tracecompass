/*******************************************************************************
 * Copyright (c) 2020 École Polytechnique de Montréal
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/

package org.eclipse.tracecompass.internal.analysis.graph.core.base;

import java.util.Map;

import org.eclipse.tracecompass.analysis.graph.core.base.TmfEdge.EdgeType;
import org.eclipse.tracecompass.tmf.core.model.OutputElementStyle;

import com.google.common.collect.ImmutableMap;

/**
 * The main color palette for the critical path analysis. When the incubator
 * weighted tree feature is integrated in main Trace Compass, this class should
 * implement IDataPalette.
 *
 * @author Geneviève Bastien
 */
public class CriticalPathPalette {

    private static final Map<String, OutputElementStyle> STATE_MAP;

    static {
        ImmutableMap.Builder<String, OutputElementStyle> builder = new ImmutableMap.Builder<>();
        builder.put(EdgeType.RUNNING.name(), new OutputElementStyle(null, EdgeType.RUNNING.toMap()));
        builder.put(EdgeType.INTERRUPTED.name(), new OutputElementStyle(null, EdgeType.INTERRUPTED.toMap()));
        builder.put(EdgeType.PREEMPTED.name(), new OutputElementStyle(null, EdgeType.PREEMPTED.toMap()));
        builder.put(EdgeType.TIMER.name(), new OutputElementStyle(null, EdgeType.TIMER.toMap()));
        builder.put(EdgeType.BLOCK_DEVICE.name(), new OutputElementStyle(null, EdgeType.BLOCK_DEVICE.toMap()));
        builder.put(EdgeType.NETWORK.name(), new OutputElementStyle(null, EdgeType.NETWORK.toMap()));
        builder.put(EdgeType.USER_INPUT.name(), new OutputElementStyle(null, EdgeType.USER_INPUT.toMap()));
        builder.put(EdgeType.IPI.name(), new OutputElementStyle(null, EdgeType.IPI.toMap()));
        builder.put(EdgeType.BLOCKED.name(), new OutputElementStyle(null, EdgeType.BLOCKED.toMap()));
        builder.put(EdgeType.UNKNOWN.name(), new OutputElementStyle(null, EdgeType.UNKNOWN.toMap()));
        STATE_MAP = builder.build();
    }

    /**
     * Get the map of all styles provided by this palette. These are the base
     * styles, mapping to the key for each style. Styles for object can then
     * refer to those base styles as parents.
     *
     * @return The map of style name to full style description.
     */
    public static Map<String, OutputElementStyle> getStyles() {
        return STATE_MAP;
    }

}
