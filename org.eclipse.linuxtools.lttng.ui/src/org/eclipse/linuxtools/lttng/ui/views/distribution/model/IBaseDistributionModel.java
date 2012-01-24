/*******************************************************************************
 * Copyright (c) 2011 Ericsson
 * 
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *   Bernd Hufmann - Initial API and implementation
 *******************************************************************************/

package org.eclipse.linuxtools.lttng.ui.views.distribution.model;

public interface IBaseDistributionModel {
    /**
     * Interface to complete the model
     */
    public void complete();
    
    /**
     * Interface to clear the model
     */
    public void clear();
}