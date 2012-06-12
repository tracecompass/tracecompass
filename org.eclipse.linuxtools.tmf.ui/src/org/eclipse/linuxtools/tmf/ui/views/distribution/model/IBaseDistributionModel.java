/*******************************************************************************
 * Copyright (c) 2011, 2012 Ericsson
 * 
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *   Bernd Hufmann - Initial API and implementation
 *   Francois Chouinard - Moved from LTTng to TMF
 *******************************************************************************/

package org.eclipse.linuxtools.tmf.ui.views.distribution.model;

/**
 * Base distribution model interface. 
 * 
 * Distribution models such histograms need to implement this interface. 
 * 
 * @version 1.0
 * @author Bernd Hufmann
 *
 */
public interface IBaseDistributionModel {
    /**
     * Complete the model (all data received)
     */
    public void complete();
    
    /**
     * Clear the model (delete all data). 
     */
    public void clear();
}