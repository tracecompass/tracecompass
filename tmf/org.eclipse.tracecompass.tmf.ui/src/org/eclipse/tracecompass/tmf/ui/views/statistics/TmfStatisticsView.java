/*******************************************************************************
 * Copyright (c) 2011, 2014 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License 2.0 which
 * accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Mathieu Denis <mathieu.denis@polymtl.ca> - Generalized version based on LTTng
 *   Bernd Hufmann - Updated to use trace reference in TmfEvent and streaming
 *   Mathieu Denis - New request added to update the statistics from the selected time range
 *   Mathieu Denis - Generalization of the view to instantiate a viewer specific to a trace type
 *   Bernd Hufmann - Class moved to internal package under name TmfStatisticsViewImpl
 *******************************************************************************/

package org.eclipse.tracecompass.tmf.ui.views.statistics;

import org.eclipse.jdt.annotation.NonNull;

/**
 * The generic Statistics View displays statistics for any kind of traces.
 */
public class TmfStatisticsView {

    /**
     * The ID corresponds to the package in which this class is embedded.
     */
    public static final @NonNull String ID = "org.eclipse.linuxtools.tmf.ui.views.statistics"; //$NON-NLS-1$;

}
