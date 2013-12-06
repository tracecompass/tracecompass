/*******************************************************************************
 * Copyright (c) 2013 École Polytechnique de Montréal
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Geneviève Bastien - Initial API and implementation
 *******************************************************************************/

package org.eclipse.linuxtools.tmf.core.statesystem;

import java.util.Map;

/**
 * Interface for analysis modules providing state systems
 *
 * @author Geneviève Bastien
 * @since 3.0
 */
public interface ITmfStateSystemAnalysisModule {

    /**
     * Return a map of all state systems this analysis is owner of. The key is
     * the ID of the state system and the value is the state system itself.
     *
     * @return A map of state sytems
     */
    Map<String, ITmfStateSystem> getStateSystems();

}
