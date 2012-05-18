/*******************************************************************************
 * Copyright (c) 2011 Ericsson
 * 
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *   Mathieu Denis      (mathieu.denis@polymtl.ca)  - Initial API and Implementation
 *******************************************************************************/

package org.eclipse.linuxtools.tmf.ui.views.statistics.model;

import java.util.List;

/**
 * <b><u>ITmfColumnDataProvider</u></b>
 * <p>
 * Basic methods that must be implemented in a column data provider.
 * </p>
 */
public interface ITmfColumnDataProvider {
    /**
     * Return a list of the column created for the view
     * @return columns list
     */
    public List<TmfBaseColumnData> getColumnData();
}