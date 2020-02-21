/*******************************************************************************
 * Copyright (c) 2015 École Polytechnique de Montréal
 *
 * All rights reserved. This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0 which
 * accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/

package org.eclipse.tracecompass.analysis.graph.core.base;

/**
 * This exception indicates that a cycle was detected while traversing the graph.
 *
 * @author Francis Giraldeau
 *
 */
public class CycleDetectedException extends RuntimeException {

    /**
     * Serial version
     */
    private static final long serialVersionUID = 8906101447850670255L;

}
