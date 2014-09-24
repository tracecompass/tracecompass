/*******************************************************************************
 * Copyright (c) 2014 École Polytechnique de Montréal
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Geneviève Bastien - Initial API and implementation
 *******************************************************************************/

package org.eclipse.linuxtools.tmf.analysis.xml.core.module;

import org.eclipse.linuxtools.statesystem.core.ITmfStateSystem;
import org.eclipse.linuxtools.tmf.analysis.xml.core.model.TmfXmlLocation;

/**
 * Interface that all XML defined objects who provide, use or contain state
 * system must implement in order to use the state provider model elements in
 * {@link org.eclipse.linuxtools.tmf.analysis.xml.core.model} package
 *
 * @author Geneviève Bastien
 */
public interface IXmlStateSystemContainer extends ITmfXmlTopLevelElement {

    /** Root quark, to get values at the root of the state system */
    static final int ROOT_QUARK = -1;
    /**
     * Error quark, value taken when a state system quark query is in error.
     *
     * FIXME: Originally in the code, the -1 was used for both root quark and
     * return errors, so it has the same value as root quark, but maybe it can
     * be changed to something else -2? A quark can never be negative
     */
    static final int ERROR_QUARK = -1;

    /**
     * Get the state system managed by this XML object
     *
     * @return The state system
     */
    ITmfStateSystem getStateSystem();

    /**
     * Get the list of locations defined in this top level XML element
     *
     * @return The list of {@link TmfXmlLocation}
     */
    Iterable<TmfXmlLocation> getLocations();

}
