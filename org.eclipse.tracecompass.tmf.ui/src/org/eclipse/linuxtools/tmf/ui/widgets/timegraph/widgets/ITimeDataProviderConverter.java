/*******************************************************************************
 * Copyright (c) 2014 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Patrick Tasse - Initial API and implementation
 *******************************************************************************/

package org.eclipse.linuxtools.tmf.ui.widgets.timegraph.widgets;

/**
 * Time data provider that converts between time data units used internally and
 * time in display units used by the caller.
 *
 * @since 3.2
 */
public interface ITimeDataProviderConverter extends ITimeDataProvider {

    /**
     * Convert a time in time data provider units to a time in display units.
     *
     * @param time the time in time data provider units
     *
     * @return the time in display units
     */
    long convertTime(long time);
}
