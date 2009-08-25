/*******************************************************************************
 * Copyright (c) 2009 Ericsson
 * 
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *   Francois Chouinard (fchouinard@gmail.com) - Initial API and implementation
 *******************************************************************************/

package org.eclipse.linuxtools.tmf.eventlog;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.Collections;
import java.util.Vector;

import org.eclipse.linuxtools.tmf.event.TmfEvent;
import org.eclipse.linuxtools.tmf.event.TmfTimeWindow;
import org.eclipse.linuxtools.tmf.event.TmfTimestamp;

/**
 * <b><u>TmfEventStream</u></b>
 * <p>
 * FIXME: Preliminary version that works only for a single file
 * FIXME: Need to handle more generic streams (sockets, pipes, ...), directories, ...
 * FIXME: Will also need to update properly the set of bookmarks for streaming data
 */
public abstract class TmfEventStream extends RandomAccessFile implements ITmfStreamLocator {

    // ========================================================================
    // Constants
    // ========================================================================

    // The default number of events to cache
    public static final int DEFAULT_CACHE_SIZE = 1000;
    
    // ========================================================================
    // Attributes
    // ========================================================================

    // The file parser
    private final ITmfEventParser fParser;

    // The cache size
    private final int fCacheSize;

    // The set of event stream bookmarks (for random access)
    private Vector<TmfStreamBookmark> fBookmarks = new Vector<TmfStreamBookmark>();

    // The number of events collected
    private int fNbEvents = 0;

    // The time span of the event stream 
    private TmfTimeWindow fTimeRange = new TmfTimeWindow(TmfTimestamp.BigBang, TmfTimestamp.BigBang);
    
    // ========================================================================
    // Constructors
    // ========================================================================

    /**
     * @param filename
     * @param parser
     * @throws FileNotFoundException
     */
    public TmfEventStream(String filename, ITmfEventParser parser) throws FileNotFoundException {
        this(filename, parser, DEFAULT_CACHE_SIZE);
    }

    /**
     * @param filename
     * @param parser
     * @param cacheSize
     * @throws FileNotFoundException
     */
    public TmfEventStream(String filename, ITmfEventParser parser, int cacheSize) throws FileNotFoundException {
        super(filename, "r");
        assert(parser != null);
        fParser = parser;
        fCacheSize = cacheSize;
        bookmark();
    }

    // ========================================================================
    // Accessors
    // ========================================================================

    /**
     * @return
     */
    public int getCacheSize() {
        return fCacheSize;
    }

    /**
     * @return
     */
    public int getNbEvents() {
        return fNbEvents;
    }

    /**
     * @return
     */
    public TmfTimeWindow getTimeRange() {
        return fTimeRange;
    }

    // ========================================================================
    // Operators
    // ========================================================================

    /**
     * Positions the stream at the first event with timestamp. If there is no
     * such event, positions the stream at the next event, if any.
     * 
     * @param timestamp 
     * @return 
     * @throws IOException 
     */
    public synchronized TmfEvent seek(TmfTimestamp timestamp) throws IOException {

        // First, find the right bookmark
        // TODO: Check the performance of bsearch on ordered Vector<>. Should be OK but...
        int index = Collections.binarySearch(fBookmarks, new TmfStreamBookmark(timestamp, 0));

        // In the very likely event that the bookmark was not found, bsearch
        // returns its negated would-be location (not an offset...). From that
        // index, we can then position the stream and locate the event.  
        if (index < 0) { 
            index = Math.max(0, -(index + 2));
        }

        seekLocation(fBookmarks.elementAt(index).getLocation());
        TmfEvent event;
        do {
            event = getNextEvent();
        } while (event != null && event.getTimestamp().compareTo(timestamp, false) < 0);

        return event;
    }

    /**
     * @return
     */
    public synchronized TmfEvent getNextEvent() {
        try {
            return fParser.getNextEvent(this);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    // ========================================================================
    // Helper functions
    // ========================================================================

    /**
     * Parse the file and store bookmarks at every fCacheSize event.
     * Done once at the creation of the event stream.
     * 
     * TODO: Consider a Job with progress bar, etc...
     */
    protected synchronized void bookmark() {
        try {
            seek(0);
            TmfTimestamp startTime = new TmfTimestamp();
            TmfTimestamp lastTime  = new TmfTimestamp();
            Object location = getCurrentLocation();

            TmfEvent event = getNextEvent();
            if (event != null) {
                startTime = event.getTimestamp();
                while (event != null) {
                    lastTime = event.getTimestamp();
                    if ((fNbEvents++ % fCacheSize) == 0) {
                        TmfStreamBookmark bookmark = new TmfStreamBookmark(lastTime, location);
                        fBookmarks.add(bookmark);
                    }
                    location = getCurrentLocation();
                    event = getNextEvent();
                }
                fTimeRange = new TmfTimeWindow(startTime, lastTime);
            }
            seek(0);
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

}
