/*******************************************************************************
 * Copyright (c) 2013, 2014 École Polytechnique de Montréal
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License 2.0 which
 * accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Geneviève Bastien - Initial API and implementation
 *******************************************************************************/

package org.eclipse.tracecompass.tmf.core.analysis;

import org.eclipse.jdt.annotation.NonNull;

/**
 * Interface that module sources must implement. A module source provides a list
 * of analysis modules. For example, one module source would be the plugin's
 * configuration element through the analysis extension point.
 *
 * Typically, for each module source, there would be an
 * {@link IAnalysisModuleHelper} implementation to create modules from this
 * source.
 *
 * @author Geneviève Bastien
 */
public interface IAnalysisModuleSource {

    /**
     * Get the list of modules helpers provided by this source
     *
     * @return The analysis module helpers in iterable format
     */
    @NonNull Iterable<IAnalysisModuleHelper> getAnalysisModules();

}
