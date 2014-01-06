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

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

/**
 * Interface for analysis modules providing state systems.
 *
 * @author Geneviève Bastien
 * @since 3.0
 */
@NonNullByDefault
public interface ITmfAnalysisModuleWithStateSystems {

    /**
     * Return a specific state system provided by this analysis.
     *
     * @param id
     *            The ID of the state system
     * @return The state system corresponding to the given ID, null if there is
     *         no match.
     */
    @Nullable
    ITmfStateSystem getStateSystem(String id);

    /**
     * FIXME The ID's should be saved in the state system themselves
     * (ITmfStateSystem.getId()), so this will eventually not be needed.
     *
     * Return the ID of a given state system.
     *
     * @param ss
     *            The state system for which you want the ID, null if there is
     *            no match.
     * @return The corresponding state system
     */
    @Nullable
    String getStateSystemId(ITmfStateSystem ss);

    /**
     * Return all the state systems provided by this analysis module, in
     * Iterable format.
     *
     * @return The state systems
     */
    Iterable<ITmfStateSystem> getStateSystems();

}
