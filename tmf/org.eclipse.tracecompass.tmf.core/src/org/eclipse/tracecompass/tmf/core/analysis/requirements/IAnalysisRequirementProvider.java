/*******************************************************************************
 * Copyright (c) 2014 École Polytechnique de Montréal
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Guilliano Molaire - Initial API and implementation
 *   Mathieu Rail - Initial API and implementation
 *******************************************************************************/

package org.eclipse.tracecompass.tmf.core.analysis.requirements;

/**
 * Interface that provides the necessary methods for an analysis to define its
 * requirements.
 *
 * @author Guilliano Molaire
 * @author Mathieu Rail
 * @since 2.0
 */
public interface IAnalysisRequirementProvider {

    /**
     * Gets the requirements associated with this analysis.
     *
     * @return List of requirement
     */
    Iterable<TmfAbstractAnalysisRequirement> getAnalysisRequirements();
}
