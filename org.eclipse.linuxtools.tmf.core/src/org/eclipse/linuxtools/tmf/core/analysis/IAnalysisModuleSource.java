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

package org.eclipse.linuxtools.tmf.core.analysis;


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
 * @since 3.0
 */
public interface IAnalysisModuleSource {

    /**
     * Get the list of modules helpers provided by this source
     *
     * @return The analysis module helpers in iterable format
     */
    Iterable<IAnalysisModuleHelper> getAnalysisModules();

}
