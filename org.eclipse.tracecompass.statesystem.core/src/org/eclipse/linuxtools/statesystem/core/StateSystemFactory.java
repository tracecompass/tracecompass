/*******************************************************************************
 * Copyright (c) 2014 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Alexandre Montplaisir - Initial API and implementation
 ******************************************************************************/

package org.eclipse.linuxtools.statesystem.core;

import java.io.IOException;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.linuxtools.internal.statesystem.core.StateSystem;
import org.eclipse.linuxtools.statesystem.core.backend.IStateHistoryBackend;

/**
 * Factory to create state systems.
 *
 * Since state system are meant to be accessed using the {@link ITmfStateSystem}
 * and {@link ITmfStateSystemBuilder} interfaces, you can use this factory to
 * instantiate new ones.
 *
 * @author Alexandre Montplaisir
 * @since 3.0
 */
@NonNullByDefault
public final class StateSystemFactory {

    private StateSystemFactory() {}

    /**
     * New-file factory method. For when you build a state system with a new
     * file, or if the back-end does not require a file on disk.
     *
     * @param id
     *            The ID of this statesystem. It should be unique.
     * @param backend
     *            Back-end plugin to use
     * @return The new state system
     */
   public static ITmfStateSystemBuilder newStateSystem(String id,
           IStateHistoryBackend backend) {
       return new StateSystem(id, backend);
   }

    /**
     * General factory method. The backend may try to open or create a file on
     * disk (the file contents and location are defined by the backend).
     *
     * @param id
     *            The ID of this statesystem. It should be unique.
     * @param backend
     *            The "state history storage" back-end to use.
     * @param newFile
     *            Put true if this is a new history started from scratch (any
     *            existing file will be overwritten).
     * @return The new state system
     * @throws IOException
     *             If there was a problem creating the new history file
     */
    public static ITmfStateSystemBuilder newStateSystem(String id,
            IStateHistoryBackend backend, boolean newFile) throws IOException {
        return new StateSystem(id, backend, newFile);
    }

}
