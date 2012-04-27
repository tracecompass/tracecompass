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

package org.eclipse.linuxtools.internal.lttng2.kernel.core.stateprovider;

import org.eclipse.linuxtools.tmf.core.ctfadaptor.CtfIterator;
import org.eclipse.linuxtools.tmf.core.ctfadaptor.CtfTmfEvent;
import org.eclipse.linuxtools.tmf.core.ctfadaptor.CtfTmfTrace;
import org.eclipse.linuxtools.tmf.core.statesystem.StateSystem;
import org.eclipse.linuxtools.tmf.core.statesystem.helpers.IStateChangeInput;

/**
 * "Dummy" version of the CTF event input plugin. This one only reads events
 * (and creates the Event/EventWrapper objects) but discards them instead of
 * inserting them in the Queue.
 * 
 * Only useful for benchmarking purposes.
 * 
 * @author alexmont
 * 
 */
public class CtfDummyInput implements IStateChangeInput {

    private final CtfIterator iterator;

    /**
     * Create a new dummy CTF state change input.
     * 
     * @param traceFile
     *            The CTF trace to read from (can be any type of CTF trace)
     */
    public CtfDummyInput(CtfTmfTrace trace) {
        this.iterator = new CtfIterator(trace);

    }

    @SuppressWarnings("unused")
    @Override
    public void run() {
        /* We know currentEvent is unused here, it's by design! */
        CtfTmfEvent currentEvent;
        currentEvent = iterator.getCurrentEvent();
        while (iterator.advance()) {
            currentEvent = iterator.getCurrentEvent();
        }
    }

    @Override
    public long getStartTime() {
        return iterator.getLocation().getLocation();
    }

    /**
     * This dummy input does not insert any state changes anywhere, so this
     * method does nothing.
     */
    @Override
    public void assignTargetStateSystem(StateSystem ss) {
        //
    }

    /**
     * Since there is no target state system in the dummy input, this always
     * returns null.
     */
    @Override
    public StateSystem getStateSystem() {
        return null;
    }

}
