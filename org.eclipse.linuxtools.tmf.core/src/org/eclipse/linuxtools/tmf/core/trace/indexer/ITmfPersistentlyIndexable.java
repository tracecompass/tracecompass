/*******************************************************************************
 * Copyright (c) 2013 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Marc-Andre Laperle - Initial API and implementation
 *******************************************************************************/

package org.eclipse.linuxtools.tmf.core.trace.indexer;

import java.nio.ByteBuffer;

import org.eclipse.linuxtools.tmf.core.trace.location.ITmfLocation;

/**
 * A trace implementing this interface can be indexed and its index can be
 * persisted to disk.
 *
 * @author Marc-Andre Laperle
 */
public interface ITmfPersistentlyIndexable {

    /**
     * Instantiate a ITmfLocation from a ByteBuffer, typically from disk.
     *
     * @param bufferIn
     *            the buffer to read from
     * @return the instantiated location
     *
     * @since 3.0
     */
    ITmfLocation restoreLocation(ByteBuffer bufferIn);

    /**
     * Get the checkpoint size for this trace
     *
     * @return the checkpoint size
     *
     * @since 3.0
     */
    public int getCheckpointSize();
}
