/*******************************************************************************
 * Copyright (c) 2017 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License 2.0 which
 * accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/

package org.eclipse.tracecompass.internal.util;

import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Class that tracks whether the MappedByteBuffer needs to be cleared
 * using a System.gc() call (see Java bug JDK-4724038).
 *
 * This class is not intended to be used by clients.
 *
 * @author Bernd Hufmann
 */
public class ByteBufferTracker {
    private static AtomicBoolean fIsMarked = new AtomicBoolean(false);

    private ByteBufferTracker() {
        // Do nothing
    }

    /**
     * Sets the marker
     */
    public static void setMarked() {
        fIsMarked.set(true);
    }

    /**
     * Returns whether the marker is set and resets the marker.
     *
     * @return true if it is marked.
     */
    public static boolean getAndReset() {
        return fIsMarked.getAndSet(false);
    }
}
