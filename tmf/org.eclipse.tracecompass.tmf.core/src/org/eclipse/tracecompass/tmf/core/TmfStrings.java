/*******************************************************************************
 * Copyright (c) 2019 École Polytechnique de Montréal
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/

package org.eclipse.tracecompass.tmf.core;

import java.util.Objects;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.tracecompass.internal.tmf.core.Messages;

/**
 * Get externalized strings to identify various core TMF concepts. These strings
 * can be used in views and analyses to have a common term to identify often
 * used concepts, or as keys for searches and filters by the user.
 * <p>
 * These strings are meant to be used and viewed by the user. Example: views
 * will display Time in English, Poh in Klingon, searches for a time would be
 * written Time < 'formatted time' in English, Poh < 'formatted time' in
 * Klingon.
 *
 * @author Geneviève Bastien
 * @since 5.1
 */
public final class TmfStrings {

    private TmfStrings()  {
        // Do nothing
    }

    /**
     * Get the string for the start time
     *
     * @return The externalized label for start time
     */
    public static @NonNull String startTime() {
        return Objects.requireNonNull(Messages.TmfStrings_StartTime);
    }

    /**
     * Get the string for the end time
     *
     * @return The externalized label for end time
     */
    public static @NonNull String endTime() {
        return Objects.requireNonNull(Messages.TmfStrings_EndTime);
    }

    /**
     * Get the string for the time
     *
     * @return The externalized label for time
     */
    public static @NonNull String time() {
        return Objects.requireNonNull(Messages.TmfStrings_Time);
    }

    /**
     * Get the string for the duration
     *
     * @return The externalized label for duration
     */
    public static @NonNull String duration() {
        return Objects.requireNonNull(Messages.TmfStrings_Duration);
    }

}
