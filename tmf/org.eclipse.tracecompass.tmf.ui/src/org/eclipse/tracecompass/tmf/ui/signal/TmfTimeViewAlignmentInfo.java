/*******************************************************************************
 * Copyright (c) 2015 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License 2.0 which
 * accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Marc-Andre Laperle - Initial API and implementation
 *******************************************************************************/

package org.eclipse.tracecompass.tmf.ui.signal;

import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Shell;

/**
 * The responsibility of this class is to provide information necessary to
 * decide whether or not views should be time-aligned with each other and at
 * what offset.
 *
 * @see TmfTimeViewAlignmentSignal
 *
 * @since 1.0
 */
public class TmfTimeViewAlignmentInfo {
    private final Point fViewLocation;
    private final int fTimeAxisOffset;
    private final Shell fShell;

    /**
     * Constructs a new TmfTimeViewAlignmentInfo.
     *
     * @param shell
     *            used to determine whether or not views should be aligned
     *            together
     * @param viewLocation
     *            location of the view, used to determine whether or not views
     *            should be aligned together
     * @param timeAxisOffset
     *            offset relative to the view. This offset will be communicated
     *            to the other views
     */
    public TmfTimeViewAlignmentInfo(Shell shell, Point viewLocation, int timeAxisOffset) {
        fShell = shell;
        fViewLocation = viewLocation;
        fTimeAxisOffset = timeAxisOffset;
    }

    /**
     * Get the shell containing this alignment.
     *
     * @return the shell
     */
    public Shell getShell() {
        return fShell;
    }

    /**
     * Get the absolute view location. This value is only valid at the time of
     * the TmfTimeViewAlignmentInfo creation so extra care must be given in
     * cases where the particular view might have been resized, moved, etc.
     *
     * @return the absolute view location
     */
    public Point getViewLocation() {
        return fViewLocation;
    }

    /**
     * Offset relative to the view corresponding to the start of the time axis.
     *
     * @return the offset in pixels
     */
    public int getTimeAxisOffset() {
        return fTimeAxisOffset;
    }
}
