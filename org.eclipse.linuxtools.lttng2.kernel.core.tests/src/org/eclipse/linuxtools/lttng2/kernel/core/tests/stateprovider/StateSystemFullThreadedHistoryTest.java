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

package org.eclipse.linuxtools.lttng2.kernel.core.tests.stateprovider;

import java.io.File;

import org.eclipse.linuxtools.internal.lttng2.kernel.core.stateprovider.CtfKernelStateInput;
import org.eclipse.linuxtools.tmf.core.statesystem.StateHistorySystem;
import org.eclipse.linuxtools.tmf.core.statesystem.backend.historytree.ThreadedHistoryTreeBackend;
import org.eclipse.linuxtools.tmf.core.statesystem.helpers.HistoryBuilder;
import org.eclipse.linuxtools.tmf.core.statesystem.helpers.IStateChangeInput;
import org.eclipse.linuxtools.tmf.core.statesystem.helpers.IStateHistoryBackend;

import org.junit.BeforeClass;
import org.junit.Test;

/**
 * Variant of the StateSystemFullHistoryTest, which uses the threaded event
 * handler instead.
 * 
 * @author alexmont
 * 
 */
public class StateSystemFullThreadedHistoryTest extends
        StateSystemFullHistoryTest {

    /* Hiding the static method in the superclass */
    protected static String getTestFileName() {
        return "/tmp/statefile-threaded.ht"; //$NON-NLS-1$
    }

    @BeforeClass
    public static void initialize() {
        stateFile = new File(getTestFileName());
        stateFileBenchmark = new File(getTestFileName() + ".benchmark"); //$NON-NLS-1$
        try {
            input = new CtfKernelStateInput(CtfTestFiles.getTestTrace());
            hp = new ThreadedHistoryTreeBackend(stateFile,
                    input.getStartTime(), 2000);
            builder = new HistoryBuilder(input, hp);
        } catch (Exception e) {
            e.printStackTrace();
        }
        builder.run();
        shs = (StateHistorySystem) builder.getSS();
        builder.close(); /* Waits for the construction to finish */
    }

    @Override
    @Test
    public void testBuild() {
        HistoryBuilder zebuilder;
        IStateChangeInput zeinput;
        IStateHistoryBackend zehp;

        try {
            zeinput = new CtfKernelStateInput(CtfTestFiles.getTestTrace());
            zehp = new ThreadedHistoryTreeBackend(stateFileBenchmark,
                    zeinput.getStartTime(), 2000);
            zebuilder = new HistoryBuilder(zeinput, zehp);
            zebuilder.run();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
