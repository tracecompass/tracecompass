/*******************************************************************************
 * Copyright (c) 2012 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Alexandre Montplaisir - Initial API and implementation
 ******************************************************************************/

package org.eclipse.linuxtools.lttng2.kernel.core.trace;

import java.io.File;
import java.io.IOException;

import org.eclipse.core.resources.IProject;
import org.eclipse.linuxtools.internal.lttng2.kernel.core.stateprovider.CTFKernelStateInput;
import org.eclipse.linuxtools.tmf.core.ctfadaptor.CtfTmfTrace;
import org.eclipse.linuxtools.tmf.core.exceptions.TmfTraceException;
import org.eclipse.linuxtools.tmf.core.statesystem.StateHistorySystem;
import org.eclipse.linuxtools.tmf.core.statesystem.backend.historytree.HistoryTreeBackend;
import org.eclipse.linuxtools.tmf.core.statesystem.helpers.HistoryBuilder;
import org.eclipse.linuxtools.tmf.core.statesystem.helpers.IStateChangeInput;
import org.eclipse.linuxtools.tmf.core.statesystem.helpers.IStateHistoryBackend;

/**
 * This is the specification of CtfTmfTrace for use with LTTng 2.x kernel
 * traces. It uses the CtfKernelStateInput to generate the state history.
 * 
 * @author alexmont
 * 
 */
public class CtfKernelTrace extends CtfTmfTrace {

    public CtfKernelTrace() {
        super();
    }

    @Override
    public boolean validate(final IProject project, final String path) {
        if (!super.validate(project, path)) {
            return false;
        }
        /* Add extra checks specific to kernel traces here */;
        return true;
    }

    @Override
    protected void buildStateSystem() throws TmfTraceException {
        /* Set up the path to the history tree file we'll use */
        final String htPath = this.getPath() + ".ht";
        final File htFile = new File(htPath);

        IStateHistoryBackend htBackend;
        IStateChangeInput htInput;
        HistoryBuilder builder;

        /* If the target file already exists, do not rebuild it uselessly */
        // TODO for now we assume it's complete. Might be a good idea to check
        // at least if its range matches the trace's range.
        try {
            if (htFile.exists()) {
                /* Load an existing history */
                htBackend = new HistoryTreeBackend(htFile);
                this.ss = new StateHistorySystem(htBackend, false);
            } else {
                /* Create a new state history from scratch */
                htInput = new CTFKernelStateInput(this);
                htBackend = new HistoryTreeBackend(htFile,
                        htInput.getStartTime());
                builder = new HistoryBuilder(htInput, htBackend);

                // TODO this is blocking for now...
                builder.run();
            }
        } catch (IOException e) {
            throw new TmfTraceException(e.getMessage());
        }
    }
}
