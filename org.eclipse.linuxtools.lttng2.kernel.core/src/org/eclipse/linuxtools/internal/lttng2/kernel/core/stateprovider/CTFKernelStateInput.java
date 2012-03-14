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

import java.io.IOException;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

import org.eclipse.linuxtools.tmf.core.ctfadaptor.CtfIterator;
import org.eclipse.linuxtools.tmf.core.ctfadaptor.CtfTmfEvent;
import org.eclipse.linuxtools.tmf.core.ctfadaptor.CtfTmfTrace;
import org.eclipse.linuxtools.tmf.core.statesystem.StateSystem;
import org.eclipse.linuxtools.tmf.core.statesystem.helpers.IStateChangeInput;

/**
 * This is the state change input plugin for TMF's state system which handles
 * the LTTng 2.0 kernel traces in CTF format.
 * 
 * It uses the reference handler defined in CTFKernelHandler.java.
 * 
 * @author alexmont
 * 
 */
public class CTFKernelStateInput implements IStateChangeInput {

    final static int EVENTS_QUEUE_SIZE = 10000;

    private final BlockingQueue<CtfTmfEvent> eventsQueue;

    private final CtfIterator traceReader;
    private final CTFKernelHandler eventHandler;

    private final Thread eventHandlerThread;

    private boolean ssAssigned;

    /**
     * Instantiate a new state provider plugin.
     * 
     * @param traceFile
     *            The LTTng 2.0 kernel trace directory
     * @throws IOException
     *             If the directory was not found, or not recognized as a CTF
     *             trace.
     */
    public CTFKernelStateInput(CtfTmfTrace trace) {
        eventsQueue = new ArrayBlockingQueue<CtfTmfEvent>(EVENTS_QUEUE_SIZE);
        traceReader = new CtfIterator(trace);
        eventHandler = new CTFKernelHandler(eventsQueue);
        ssAssigned = false;

        eventHandlerThread = new Thread(eventHandler,
                "CTF Kernel Event Handler"); //$NON-NLS-1$
    }

    @Override
    public void run() {
        if (!ssAssigned) {
            System.err.println("Cannot start Input thread without a target state system"); //$NON-NLS-1$
            return;
        }

        CtfTmfEvent currentEvent;

        eventHandlerThread.start();

        try {
            currentEvent = traceReader.getCurrentEvent();
            while (currentEvent != null) {
                traceReader.advance();
                eventsQueue.put(currentEvent);
                currentEvent = traceReader.getCurrentEvent();
            }
            /*
             * We're done reading the trace, insert a null event in the queue to
             * stop the handler
             */
            eventsQueue.put(CtfTmfEvent.getNullEvent());
            eventHandlerThread.join();

        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void assignTargetStateSystem(StateSystem ss) {
        eventHandler.assignStateSystem(ss);
        ssAssigned = true;
    }

    @Override
    public StateSystem getStateSystem() {
        return eventHandler.getStateSystem();
    }

    @Override
    public long getStartTime() {
        return traceReader.getCtfTmfTrace().getStartTime().getValue();
    }
}
