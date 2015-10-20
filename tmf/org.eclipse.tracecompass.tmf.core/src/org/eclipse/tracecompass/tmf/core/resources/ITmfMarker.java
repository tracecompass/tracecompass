/*******************************************************************************
 * Copyright (c) 2015 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Patrick Tasse - Initial API and implementation
 *******************************************************************************/

package org.eclipse.tracecompass.tmf.core.resources;

/**
 * Interface for TMF-specific marker constants
 *
 * @noimplement This interface is not intended to be implemented by clients.
 * @since 2.0
 */
public interface ITmfMarker {

    /** Color marker attribute. The format is the output of RGBA.toString(). */
    String MARKER_COLOR = "color"; //$NON-NLS-1$

    /** Rank marker attribute. The format is the output of Long.toString(). */
    String MARKER_RANK = "rank"; //$NON-NLS-1$

    /** Time marker attribute. The format is the output of Long.toString(). */
    String MARKER_TIME = "time"; //$NON-NLS-1$

    /** Duration marker attribute. The format is the output of Long.toString(). */
    String MARKER_DURATION = "duration"; //$NON-NLS-1$

}
