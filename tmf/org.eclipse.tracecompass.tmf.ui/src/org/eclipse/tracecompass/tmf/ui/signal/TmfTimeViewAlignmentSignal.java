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

import org.eclipse.tracecompass.tmf.core.signal.TmfSignal;

/**
 * A signal to inform about the state of time alignment. Typically, the emitter
 * will inform the receivers about the position of a sash that separates the
 * time axis on right side and extra information on the left side.
 *
 * @see TmfTimeViewAlignmentInfo
 *
 * @since 1.0
 */
public class TmfTimeViewAlignmentSignal extends TmfSignal {

    private final TmfTimeViewAlignmentInfo fTimeViewAlignmentInfo;
    private final boolean fIsSynchronous;

    /**
     * Creates a new TmfTimeViewAlignmentSignal
     *
     * @param source
     *            the source of the signal
     * @param alignmentInfo
     *            information about the time alignment
     */
    public TmfTimeViewAlignmentSignal(Object source, TmfTimeViewAlignmentInfo alignmentInfo) {
        this(source, alignmentInfo, false);
    }

    /**
     * Creates a new TmfTimeViewAlignmentSignal
     *
     * @param source
     *            the source of the signal
     * @param alignmentInfo
     *            information about the time alignment
     * @param synchronous
     *            whether or not the signal should be processed right away. This
     *            is useful for signals that are sent not repetitively.
     *            For example, a sash being dragged would not be synchronous
     *            because the signal gets fired repeatedly. A view that has
     *            completed computing it's data could send a synchronous signal.
     */
    public TmfTimeViewAlignmentSignal(Object source, TmfTimeViewAlignmentInfo alignmentInfo, boolean synchronous) {
        super(source);
        fTimeViewAlignmentInfo = alignmentInfo;
        fIsSynchronous = synchronous;
    }

    /**
     * Get the time alignment information.
     *
     * @return the time alignment information
     */
    public TmfTimeViewAlignmentInfo getTimeViewAlignmentInfo() {
        return fTimeViewAlignmentInfo;
    }

    @Override
    public String toString() {
        return "[TmfTimeViewAlignmentSignal (" + fTimeViewAlignmentInfo.toString() + ")]"; //$NON-NLS-1$ //$NON-NLS-2$
    }

    /**
     * Get whether or not the signal should be processed right away, without
     * being throttled.
     *
     * @return whether or not the signal should be processed right away
     */
    public boolean IsSynchronous() {
        return fIsSynchronous;
    }
}
