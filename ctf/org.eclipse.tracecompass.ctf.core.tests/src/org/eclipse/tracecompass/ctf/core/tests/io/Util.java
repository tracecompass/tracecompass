/*******************************************************************************
 * Copyright (c) 2014 Ericsson
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Matthew Khouzam - Initial API and implementation
 ******************************************************************************/

package org.eclipse.tracecompass.ctf.core.tests.io;

import java.nio.ByteBuffer;

import org.eclipse.jdt.annotation.NonNull;

/**
 * Helpers for the tests
 *
 * @author Matthew Khouzam
 */
public final class Util {

    private Util() {
    }

    /**
     * Wrapper to make sure the bytebuffer is not null
     *
     * @param buffer
     *            a potentially null byte buffer
     * @return a non-null byte buffer or an illegal state exception
     */
    @NonNull
    public static ByteBuffer testMemory(ByteBuffer buffer) {
        if (buffer == null) {
            throw new IllegalStateException("Failed to alloc");
        }
        return buffer;
    }
}
