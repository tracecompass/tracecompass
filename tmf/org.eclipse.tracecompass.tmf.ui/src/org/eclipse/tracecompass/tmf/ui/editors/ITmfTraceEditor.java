/*******************************************************************************
 * Copyright (c) 2010, 2014 Ericsson
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

package org.eclipse.tracecompass.tmf.ui.editors;

import org.eclipse.tracecompass.tmf.core.trace.ITmfTrace;

/**
 * The trace editor interface
 *
 * @version 1.0
 * @author Patrick Tasse
 */
public interface ITmfTraceEditor {

    /**
     * Get the trace to which this editor is assigned
     *
     * @return The trace
     */
    ITmfTrace getTrace();
}
