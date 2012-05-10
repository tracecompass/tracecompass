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

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.linuxtools.internal.lttng2.kernel.core.stateprovider.CtfKernelStateInput;
import org.eclipse.linuxtools.tmf.core.ctfadaptor.CtfTmfTrace;
import org.eclipse.linuxtools.tmf.core.exceptions.TmfTraceException;
import org.eclipse.linuxtools.tmf.core.statesystem.HistoryBuilder;
import org.eclipse.linuxtools.tmf.core.statesystem.IStateChangeInput;
import org.eclipse.linuxtools.tmf.core.statesystem.IStateHistoryBackend;
import org.eclipse.linuxtools.tmf.core.statesystem.backend.historytree.HistoryTreeBackend;
import org.eclipse.linuxtools.tmf.core.statesystem.backend.historytree.ThreadedHistoryTreeBackend;

/**
 * This is the specification of CtfTmfTrace for use with LTTng 2.x kernel
 * traces. It uses the CtfKernelStateInput to generate the state history.
 * 
 * @author alexmont
 * 
 */
public class CtfKernelTrace extends CtfTmfTrace {

    /** Size of the blocking queue to use when building a state history */
    private final static int QUEUE_SIZE = 10000;

    public CtfKernelTrace() {
        super();
    }

    @Override
    public boolean validate(final IProject project, final String path) {
        if (!super.validate(project, path)) {
            return false;
        }
        /* Add extra checks specific to kernel traces here */
        return true;
    }

    @Override
    protected void buildStateSystem() throws TmfTraceException {
        /* Set up the path to the history tree file we'll use */
        IResource resource = getResource();
        String name = '.' + resource.getName() + ".ht"; //$NON-NLS-1$
        IFolder folder = (IFolder)resource.getParent();
        IFile file = folder.getFile(name);

        final File htFile = new File(file.getLocationURI());
        
        IStateHistoryBackend htBackend;
        IStateChangeInput htInput;
        HistoryBuilder builder;

        /* If the target file already exists, do not rebuild it uselessly */
        // TODO for now we assume it's complete. Might be a good idea to check
        // at least if its range matches the trace's range.
        if (htFile.exists()) {
            /* Load an existing history */
            try {
                htBackend = new HistoryTreeBackend(htFile);
                this.ss = HistoryBuilder.openExistingHistory(htBackend);
                return;
            } catch (IOException e) {
                /*
                 * There was an error opening the existing file. Perhaps it was
                 * corrupted, perhaps it's an old version? We'll just
                 * fall-through and try to build a new one from scratch instead.
                 */
            }
        }

        /* Create a new state history from scratch */
        htInput = new CtfKernelStateInput(this);

        try {
            htBackend = new ThreadedHistoryTreeBackend(htFile,
                    htInput.getStartTime(), QUEUE_SIZE);
            builder = new HistoryBuilder(htInput, htBackend);
        } catch (IOException e) {
            /* 
             * If it fails here however, it means there was a problem writing
             * to the disk, so throw a real exception this time.
             */
            throw new TmfTraceException(e.getMessage());
        }

        this.ss = builder.getStateSystemQuerier();
        builder.run(); /* Start the construction of the history */
    }
}
