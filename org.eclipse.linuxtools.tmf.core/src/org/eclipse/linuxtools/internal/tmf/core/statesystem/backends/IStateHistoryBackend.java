/*******************************************************************************
 * Copyright (c) 2012, 2013 Ericsson
 * Copyright (c) 2010, 2011 École Polytechnique de Montréal
 * Copyright (c) 2010, 2011 Alexandre Montplaisir <alexandre.montplaisir@gmail.com>
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 *******************************************************************************/

package org.eclipse.linuxtools.internal.tmf.core.statesystem.backends;

import java.io.File;
import java.io.FileInputStream;
import java.io.PrintWriter;
import java.util.List;

import org.eclipse.linuxtools.tmf.core.exceptions.AttributeNotFoundException;
import org.eclipse.linuxtools.tmf.core.exceptions.StateSystemDisposedException;
import org.eclipse.linuxtools.tmf.core.exceptions.TimeRangeException;
import org.eclipse.linuxtools.tmf.core.interval.ITmfStateInterval;
import org.eclipse.linuxtools.tmf.core.statevalue.ITmfStateValue;

/**
 * The main difference between StateSystem and StateHistorySystem is that SHS
 * allows 'seeking' back in time to reload a Current State at a previous time.
 * "How to go back in time" is defined by the implementation of the
 * HistoryBackend.
 *
 * A StateHistorySystem contains one and only one HistoryBackend. If you want to
 * use a paradigm with more than one provider (eg. more or less precision
 * depending on what's asked by the user), implement one wrapper HistoryBackend
 * which can then contain your 2-3 other backends underneath.
 *
 * @author alexmont
 *
 */
public interface IStateHistoryBackend {

    /**
     * Get the start time of this state history. This is usually the same as the
     * start time of the originating trace.
     *
     * @return The start time
     */
    long getStartTime();

    /**
     * Get the current end time of the state history. It will change as the
     * history is being built.
     *
     * @return The end time
     */
    long getEndTime();

    /**
     * Main method to insert state intervals into the history.
     *
     * @param stateStartTime
     *            The start time of the interval
     * @param stateEndTime
     *            The end time of the interval
     * @param quark
     *            The quark of the attribute this interval refers to
     * @param value
     *            The StateValue represented by this interval
     * @throws TimeRangeException
     *             If the start or end time are invalid
     */
    // FIXME change to IStateInterval?
    void insertPastState(long stateStartTime, long stateEndTime,
            int quark, ITmfStateValue value) throws TimeRangeException;

    /**
     * Indicate to the provider that we are done building the history (so it can
     * close off, stop threads, etc.)
     *
     * @param endTime
     *            The end time to assign to this state history. It could be
     *            farther in time than the last state inserted, for example.
     * @throws TimeRangeException
     *             If the requested time makes no sense.
     */
    void finishedBuilding(long endTime) throws TimeRangeException;

    /**
     * It is the responsibility of the backend to define where to save the
     * Attribute Tree (since it's only useful to "reopen" an Attribute Tree if
     * we have the matching History).
     *
     * This method defines where to read for the attribute tree when opening an
     * already-existing history. Refer to the file format documentation.
     *
     * @return A FileInputStream object pointing to the correct file/location in
     *         the file where to read the attribute tree information.
     */
    FileInputStream supplyAttributeTreeReader();

    // FIXME change to FOS too?
    /**
     * Supply the File object to which we will write the attribute tree. The
     * position in this file is supplied by -TreeWriterFilePosition.
     *
     * @return The target File
     */
    File supplyAttributeTreeWriterFile();

    /**
     * Supply the position in the file where we should write the attribute tree
     * when asked to.
     *
     * @return The file position (we will seek() to it)
     */
    long supplyAttributeTreeWriterFilePosition();

    /**
     * Delete any generated files or anything that might have been created by
     * the history backend (either temporary or save files). By calling this, we
     * return to the state as it was before ever building the history.
     *
     * You might not want to call automatically if, for example, you want an
     * index file to persist on disk. This could be limited to actions
     * originating from the user.
     */
    void removeFiles();

    /**
     * Notify the state history back-end that the trace is being closed, so it
     * should release its file descriptors, close its connections, etc.
     */
    void dispose();

    // ------------------------------------------------------------------------
    // Query methods
    // ------------------------------------------------------------------------

    /**
     * Complete "give me the state at a given time" method 'currentStateInfo' is
     * an "out" parameter, that is, write to it the needed information and
     * return. DO NOT 'new' currentStateInfo, it will be lost and nothing will
     * be returned!
     *
     * @param currentStateInfo
     *            List of StateValues (index == quark) to fill up
     * @param t
     *            Target timestamp of the query
     * @throws TimeRangeException
     *             If the timestamp is outside of the history/trace
     * @throws StateSystemDisposedException
     *             If the state system is disposed while a request is ongoing.
     */
    void doQuery(List<ITmfStateInterval> currentStateInfo, long t)
            throws TimeRangeException, StateSystemDisposedException;

    /**
     * Some providers might want to specify a different way to obtain just a
     * single StateValue instead of updating the whole list. If the method to
     * use is the same, then feel free to just implement this as a wrapper using
     * doQuery().
     *
     * @param t
     *            The target timestamp of the query.
     * @param attributeQuark
     *            The single attribute for which you want the state interval
     * @return The state interval matching this timestamp/attribute pair
     * @throws TimeRangeException
     *             If the timestamp was invalid
     * @throws AttributeNotFoundException
     *             If the quark was invalid
     * @throws StateSystemDisposedException
     *             If the state system is disposed while a request is ongoing.
     */
    ITmfStateInterval doSingularQuery(long t, int attributeQuark)
            throws TimeRangeException, AttributeNotFoundException,
            StateSystemDisposedException;

    /**
     * Simple check to make sure the requested timestamps are within the borders
     * of this state history. This is used internally, but could also be used by
     * the request sender (to check before sending in a lot of requests for
     * example).
     *
     * @param t
     *            The queried timestamp
     * @return True if the timestamp is within range, false if not.
     */
    boolean checkValidTime(long t);

    /**
     * Debug method to print the contents of the history backend.
     *
     * @param writer
     *            The PrintWriter where to write the output
     */
    void debugPrint(PrintWriter writer);
}
