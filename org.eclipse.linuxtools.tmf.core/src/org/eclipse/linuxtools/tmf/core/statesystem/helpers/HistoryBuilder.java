/*******************************************************************************
 * Copyright (c) 2012 Ericsson
 * Copyright (c) 2010, 2011 École Polytechnique de Montréal
 * Copyright (c) 2010, 2011 Alexandre Montplaisir <alexandre.montplaisir@gmail.com>
 * 
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 *******************************************************************************/

package org.eclipse.linuxtools.tmf.core.statesystem.helpers;

import java.io.IOException;

import org.eclipse.linuxtools.tmf.core.statesystem.StateHistorySystem;
import org.eclipse.linuxtools.tmf.core.statesystem.StateSystem;

/**
 * This is the high-level wrapper around the State History and its input and
 * storage plugins. Just create the object using the constructor then .run()
 * 
 * You can use one HistoryBuilder and it will instantiate everything underneath.
 * If you need more fine-grained control you can still ignore this and
 * instantiate everything manually.
 * 
 * @author alexmont
 * 
 */
public class HistoryBuilder implements Runnable {

    private final IStateChangeInput sci;
    private final StateSystem ss;
    private final IStateHistoryBackend hb;

    private final Thread siThread;

    /**
     * Instantiate a new HistoryBuilder helper.
     * 
     * @param stateChangeInput
     *            The input plugin to use. This is required.
     * @param backend
     *            The backend storage to use. Use "null" here if you want a
     *            state system with no history.
     * @throws IOException
     *             Is thrown if anything went wrong (usually with the storage
     *             backend)
     */
    public HistoryBuilder(IStateChangeInput stateChangeInput,
            IStateHistoryBackend backend) throws IOException {
        assert (stateChangeInput != null);
        /* "backend" can be null, this implies no history */
        sci = stateChangeInput;

        if (backend == null) {
            hb = null;
            ss = new StateSystem();
        } else {
            hb = backend;
            ss = new StateHistorySystem(hb, true);
        }

        sci.assignTargetStateSystem(ss);
        siThread = new Thread(sci, "Input Plugin"); //$NON-NLS-1$
    }

    @Override
    public void run() {
        siThread.start();

        try {
            siThread.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
            assert (false);
        }
    }

    /**
     * Return the StateSystem (or StateHistorySystem) object that was created by
     * this builder. You will need this reference to run queries.
     * 
     * @return The StateSystem that was generated
     */
    public StateSystem getSS() {
        return ss;
    }

}