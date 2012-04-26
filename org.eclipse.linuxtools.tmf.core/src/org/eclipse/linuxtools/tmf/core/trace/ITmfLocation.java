/*******************************************************************************
 * Copyright (c) 2009, 2010, 2012 Ericsson
 * 
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *   Francois Chouinard - Initial API and implementation
 *   Francois Chouinard - Updated as per TMF Trace Model 1.0
 *******************************************************************************/

package org.eclipse.linuxtools.tmf.core.trace;


/**
 * An ITmfLocation is the equivalent of a random-access file position, holding
 * enough information to allow the positioning of the trace 'pointer' to read an
 * arbitrary event.
 * <p>
 * This location is trace-specific but must be comparable.
 * 
 * @since 1.0
 * @version 1.0
 * @author Francois Chouinard
 *
 * @see TmfLocation
 */
public interface ITmfLocation<L extends Comparable<?>> extends Cloneable {

    // ------------------------------------------------------------------------
    // Getters
    // ------------------------------------------------------------------------

    /**
     * @return the location
     */
    public L getLocation();

    // ------------------------------------------------------------------------
    // Cloneable
    // ------------------------------------------------------------------------

    /**
     * @return a clone of the location
     */
    public ITmfLocation<L> clone();

}
