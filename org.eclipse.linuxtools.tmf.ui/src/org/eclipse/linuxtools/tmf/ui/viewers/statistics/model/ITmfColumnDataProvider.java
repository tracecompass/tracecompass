/*******************************************************************************
 * Copyright (c) 2011, 2012 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Mathieu Denis <mathieu.denis@polymtl.ca> - Initial API and Implementation
 *******************************************************************************/

package org.eclipse.linuxtools.tmf.ui.viewers.statistics.model;

import java.util.List;

/**
 * Basic methods that must be implemented in a column data provider. The
 * <code>TmfStatisticsView</code> uses classes implementing this interface to
 * define the columns in the statistics tree viewer.
 *
 * @version 2.0
 * @author Mathieu Denis
 * @since 2.0
 */
public interface ITmfColumnDataProvider {

    /**
     * Return a list of the column created for the view
     *
     * @return columns list
     */
    public List<TmfBaseColumnData> getColumnData();
}