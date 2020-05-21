/*******************************************************************************
 * Copyright (c) 2019, 2020 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License 2.0 which
 * accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/

package org.eclipse.tracecompass.tmf.core.analysis.callsite;

import java.util.Objects;

import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.tracecompass.tmf.core.event.lookup.ITmfCallsite;

/**
 * Class to store callsite and the time when it was called.
 *
 * @author Bernd Hufmann
 * @since 5.2
 */
public class TimeCallsite {
    private final long fTime;
    private final ITmfCallsite fCallsite;

    /**
     * Constructor
     *
     * @param callsite
     *            The callsite implementation
     * @param time
     *            when callsite was called
     */
    public TimeCallsite(ITmfCallsite callsite, long time) {
        fCallsite = callsite;
        fTime = time;
    }

    /**
     * Gets the callsite object.
     *
     * @return the callsite object.
     */
    public ITmfCallsite getCallsite() {
        return fCallsite;
    }

    /**
     * Gets the time when callsite was called.
     *
     * @return the time when the callsite was called.
     */
    public long getTime() {
        return fTime;
    }

    @Override
    public int hashCode() {
        return Objects.hash(fCallsite, fTime);
    }

    @Override
    public boolean equals(@Nullable Object obj) {
        return Objects.equals(fCallsite, obj) && (obj != null) && (fTime == ((TimeCallsite) obj).fTime);
    }

    @Override
    public String toString() {
        return String.format("%s@%d", fCallsite, fTime); //$NON-NLS-1$
    }
}