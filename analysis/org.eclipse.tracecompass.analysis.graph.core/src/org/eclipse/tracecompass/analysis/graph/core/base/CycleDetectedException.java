/*******************************************************************************
 * Copyright (c) 2015 École Polytechnique de Montréal
 *
 * All rights reserved. This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
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
