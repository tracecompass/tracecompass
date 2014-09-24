/*******************************************************************************
 * Copyright (c) 2013 École Polytechnique de Montréal
 *
 * All rights reserved. This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Geneviève Bastien - Initial implementation and API
 *******************************************************************************/

package org.eclipse.linuxtools.tmf.core.event.matching;


/**
 * Interface for matching trace events
 *
 * @author Geneviève Bastien
 * @since 3.0
 */
public interface ITmfEventMatching {

    /**
     * Method that start the process of matching events
     *
     * @return Whether the match was completed correctly or not
     */
    boolean matchEvents();

}
