/*******************************************************************************
 * Copyright (c) 2014 École Polytechnique de Montréal
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License 2.0 which
 * accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
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
