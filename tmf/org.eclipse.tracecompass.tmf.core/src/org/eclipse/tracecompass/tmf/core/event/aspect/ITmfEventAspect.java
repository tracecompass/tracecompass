/*******************************************************************************
 * Copyright (c) 2014, 2015 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0 which
 * accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Alexandre Montplaisir - Initial API and implementation
 *   Patrick Tasse - Added base aspect list
 *******************************************************************************/

package org.eclipse.tracecompass.tmf.core.event.aspect;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.tracecompass.tmf.core.event.ITmfEvent;

/**
 * An aspect is a piece of information that can be extracted, directly or
 * indirectly, from a trace event.
 *
 * Simple examples could be timestamp, or event fields. But it could also be
 * something like a state system request, at the timestamp of the given event.
 *
 * The aspect can then be used to populate event table columns, to filter
 * on to only keep certain events, to plot XY charts, etc.
 *
 * @author Alexandre Montplaisir
 * @param <T> Type of the return value of the {@link #resolve} method
 */
public interface ITmfEventAspect<T> {

    /**
     * Static definition of an empty string. You can use this instead of 'null'!
     */
    String EMPTY_STRING = ""; //$NON-NLS-1$

    /**
     * Get the name of this aspect. This name will be user-visible and, as such,
     * should be localized.
     *
     * @return The name of this aspect.
     */
    String getName();

    /**
     * Return a descriptive help text of what this aspect does. This could then
     * be shown in tooltip or in option dialogs for instance. It should also be
     * localized.
     *
     * You can return {@link #EMPTY_STRING} if you judge that the aspect name
     * makes it obvious.
     *
     * @return The help text of this aspect
     */
    String getHelpText();

    /**
     * The "functor" representing this aspect. Basically, what to do for an
     * event that is passed in parameter.
     *
     * Note to implementers:
     *
     * The parameter type here is {@link ITmfEvent}. This is because you could
     * receive any type of event here. Do not assume you will only receive
     * events of your own trace type. It is perfectly fine to return
     * {@link #EMPTY_STRING} for event types you don't support.
     *
     * @param event
     *            The event to process
     * @return The resulting tidbit of information for this event.
     */
    @Nullable T resolve(ITmfEvent event);

    /**
     * This method will return the same result as {@link #resolve(ITmfEvent)},
     * but it allows to specify whether to wait until the requested information
     * is available.
     *
     * @param event
     *            The event to process
     * @param block
     *            Whether to block if the requested information is not yet
     *            available but will be later.
     * @param monitor
     *            The progress monitor, to be used by implementation to verify
     *            the cancellation of the current thread
     * @return The resulting tidbit of information for this event.
     * @throws InterruptedException
     *             If any thread has interrupted the current thread
     * @since 2.0
     */
    default @Nullable T resolve(ITmfEvent event, boolean block, IProgressMonitor monitor) throws InterruptedException {
        return resolve(event);
    }

    /**
     * This method will return a hint if this aspect should be displayed by default
     *
     * @return if the aspect should be hidden by default
     * @since 3.1
     */
    default boolean isHiddenByDefault() {
        return false;
    }
}
