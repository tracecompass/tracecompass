/*******************************************************************************
 * Copyright (c) 2013, 2014 École Polytechnique de Montréal
 *
 * All rights reserved. This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0 which
 * accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Geneviève Bastien - Initial implementation and API
 *******************************************************************************/

package org.eclipse.tracecompass.tmf.core.event.matching;


/**
 * Interface for matching trace events
 *
 * @author Geneviève Bastien
 */
public interface ITmfEventMatching {

    /**
     * Method that start the process of matching events
     *
     * @return Whether the match was completed correctly or not
     */
    boolean matchEvents();

}
