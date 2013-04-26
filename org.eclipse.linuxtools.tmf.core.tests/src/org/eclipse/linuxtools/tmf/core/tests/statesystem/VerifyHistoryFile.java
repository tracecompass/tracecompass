/*******************************************************************************
 * Copyright (c) 2012, 2013 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Alexandre Montplaisir - Initial API and implementation
 ******************************************************************************/

package org.eclipse.linuxtools.tmf.core.tests.statesystem;

import java.io.File;
import java.io.IOException;
import java.util.List;

import org.eclipse.linuxtools.internal.tmf.core.statesystem.HistoryBuilder;
import org.eclipse.linuxtools.internal.tmf.core.statesystem.backends.IStateHistoryBackend;
import org.eclipse.linuxtools.internal.tmf.core.statesystem.backends.historytree.HistoryTreeBackend;
import org.eclipse.linuxtools.tmf.core.exceptions.AttributeNotFoundException;
import org.eclipse.linuxtools.tmf.core.exceptions.StateSystemDisposedException;
import org.eclipse.linuxtools.tmf.core.exceptions.TimeRangeException;
import org.eclipse.linuxtools.tmf.core.interval.ITmfStateInterval;
import org.eclipse.linuxtools.tmf.core.statesystem.ITmfStateProvider;
import org.eclipse.linuxtools.tmf.core.statesystem.ITmfStateSystem;

/**
 * Small program to ensure a history file does not contain any "holes".
 * Null-state-values are fine, here we're looking for *real* null's that would
 * trigger NPE's elsewhere in the stack.
 *
 * @author alexmont
 */
@SuppressWarnings("javadoc")
public class VerifyHistoryFile {

    // Enter the .ht file name to test here
    public static final String pathToHistoryFile = "";

    private static File htFile;
    private static IStateHistoryBackend htBackend;
    private static ITmfStateSystem ss;

    private static long startTime;
    private static long endTime;
    private static int nbErrors;

    public static void main(String[] args) throws IOException,
            TimeRangeException, AttributeNotFoundException,
            StateSystemDisposedException {
        htFile = new File(pathToHistoryFile);
        htBackend = new HistoryTreeBackend(htFile, ITmfStateProvider.IGNORE_PROVIDER_VERSION);
        ss = HistoryBuilder.openExistingHistory(htBackend);

        startTime = ss.getStartTime();
        endTime = ss.getCurrentEndTime();
        nbErrors = 0;
        int total = ss.getNbAttributes();

        System.out.println("Starting check of " + total + " attributes.");
        for (int i = 0; i < total; i++) {
            verifyAttribute(i);
        }
        System.out.println("Done, total number of errors: " + nbErrors);
    }

    private static void verifyAttribute(int attribute)
            throws TimeRangeException, AttributeNotFoundException,
            StateSystemDisposedException {
        List<ITmfStateInterval> intervals;

        System.out.print("Checking attribute " + attribute);
        System.out.print(' ' + ss.getFullAttributePath(attribute));

        intervals = ss.queryHistoryRange(attribute, startTime, endTime);
        System.out.println(" (" + intervals.size() + " intervals)");

        /*
         * Compare the start of the history with the start time of the first
         * interval
         */
        verify(attribute, startTime, intervals.get(0).getStartTime());

        /* Compare the end time of each interval with the start of the next one */
        for (int i = 0; i < intervals.size() - 1; i++) {
            verify(attribute, intervals.get(i).getEndTime() + 1,
                    intervals.get(i + 1).getStartTime());
        }
        /*
         * Compare the end time of the last interval against the end time of the
         * history
         */
        verify(attribute, intervals.get(intervals.size() - 1).getEndTime(),
                endTime);
    }

    private static void verify(int a, long t1, long t2) {
        if (t1 != t2) {
            nbErrors++;
            System.err.println("Check failed for attribute " + a + ": " + t1
                    + " vs " + t2);
        }
    }

}
