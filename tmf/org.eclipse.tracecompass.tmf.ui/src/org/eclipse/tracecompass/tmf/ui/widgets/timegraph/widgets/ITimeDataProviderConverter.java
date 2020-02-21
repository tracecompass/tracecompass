/*******************************************************************************
 * Copyright (c) 2014 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License 2.0 which
 * accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Patrick Tasse - Initial API and implementation
 *******************************************************************************/

package org.eclipse.tracecompass.tmf.ui.widgets.timegraph.widgets;

/**
 * Time data provider that converts between time data units used internally and
 * time in display units used by the caller.
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
