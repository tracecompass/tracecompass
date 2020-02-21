/**********************************************************************
 * Copyright (c) 2019 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License 2.0 which
 * accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 **********************************************************************/

package org.eclipse.tracecompass.tmf.core.model;

/**
 * Interface that represents a time element. A time element is an output
 * element that corresponds to a time or a time range.
 *
 * @author Patrick Tasse
 * @since 5.2
 */
public interface ITimeElement extends IOutputElement {

    /**
     * Get the start time
     *
     * @return Start time
     */
    long getStartTime();

    /**
     * Get the duration
     *
     * @return Duration
     */
    long getDuration();
}
