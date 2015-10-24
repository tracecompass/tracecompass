/*******************************************************************************
 * Copyright (c) 2015 École Polytechnique de Montréal
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.eclipse.tracecompass.analysis.graph.core.criticalpath;

/**
 * Critical Path Algorithm Exception
 * @author Matthew Khouzam
 */
public class CriticalPathAlgorithmException extends Exception {
    /**
     * Serial ID for serialization
     */
    private static final long serialVersionUID = -919020777158527567L;

    /**
     * Constructor
     * @param message message
     */
    public CriticalPathAlgorithmException(String message){
        super(message);
    }
}