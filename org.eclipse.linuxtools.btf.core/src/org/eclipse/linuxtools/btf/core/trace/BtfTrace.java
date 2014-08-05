/*******************************************************************************
 * Copyright (c) 2014 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Matthew Khouzam - Initial API and implementation
 *******************************************************************************/

package org.eclipse.linuxtools.btf.core.trace;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.linuxtools.btf.core.Activator;
import org.eclipse.linuxtools.btf.core.event.BtfEvent;
import org.eclipse.linuxtools.btf.core.event.BtfEventType;
import org.eclipse.linuxtools.tmf.core.event.ITmfEvent;
import org.eclipse.linuxtools.tmf.core.event.ITmfEventField;
import org.eclipse.linuxtools.tmf.core.exceptions.TmfTraceException;
import org.eclipse.linuxtools.tmf.core.io.BufferedRandomAccessFile;
import org.eclipse.linuxtools.tmf.core.parsers.custom.CustomTxtTraceContext;
import org.eclipse.linuxtools.tmf.core.timestamp.TmfTimestamp;
import org.eclipse.linuxtools.tmf.core.trace.ITmfContext;
import org.eclipse.linuxtools.tmf.core.trace.ITmfEventParser;
import org.eclipse.linuxtools.tmf.core.trace.ITmfTraceProperties;
import org.eclipse.linuxtools.tmf.core.trace.TmfContext;
import org.eclipse.linuxtools.tmf.core.trace.TmfTrace;
import org.eclipse.linuxtools.tmf.core.trace.TraceValidationStatus;
import org.eclipse.linuxtools.tmf.core.trace.indexer.ITmfPersistentlyIndexable;
import org.eclipse.linuxtools.tmf.core.trace.indexer.ITmfTraceIndexer;
import org.eclipse.linuxtools.tmf.core.trace.indexer.TmfBTreeTraceIndexer;
import org.eclipse.linuxtools.tmf.core.trace.indexer.checkpoint.ITmfCheckpoint;
import org.eclipse.linuxtools.tmf.core.trace.indexer.checkpoint.TmfCheckpoint;
import org.eclipse.linuxtools.tmf.core.trace.location.ITmfLocation;
import org.eclipse.linuxtools.tmf.core.trace.location.TmfLongLocation;

import com.google.common.collect.ImmutableMap;

/**
 * BTF reader. Reads Best Trace Format traces.
 *
 * @author Matthew Khouzam
 */
public class BtfTrace extends TmfTrace implements ITmfEventParser, ITmfPersistentlyIndexable, ITmfTraceProperties, AutoCloseable {

    private static final String VERSION = "#version"; //$NON-NLS-1$
    private static final String CREATOR = "#creator"; //$NON-NLS-1$
    private static final String CREATIONDATE = "#creationDate"; //$NON-NLS-1$
    private static final String INPUTFILE = "#inputFile"; //$NON-NLS-1$
    private static final String TIMESCALE = "#timeScale"; //$NON-NLS-1$
    private static final String ENTITYTYPE = "#entityType"; //$NON-NLS-1$
    private static final String ENTITYTABLE = "#entityTable"; //$NON-NLS-1$
    private static final String ENTITYTYPETABLE = "#entityTypeTable"; //$NON-NLS-1$

    // lower-case helpers
    private static final String lCREATIONDATE = "#creationdate"; //$NON-NLS-1$
    private static final String lINPUTFILE = "#inputfile"; //$NON-NLS-1$
    private static final String lTIMESCALE = "#timescale"; //$NON-NLS-1$
    private static final String lENTITYTYPE = "#entitytype"; //$NON-NLS-1$
    private static final String lENTITYTABLE = "#entitytable"; //$NON-NLS-1$
    private static final String lENTITYTYPETABLE = "#entitytypetable"; //$NON-NLS-1$

    private static final TmfLongLocation NULL_LOCATION = new TmfLongLocation(-1L);

    private static final SimpleDateFormat ISO8601DATEFORMAT = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssX"); //$NON-NLS-1$

    private static final int CACHE_SIZE = 256;
    private static final int MAX_CONFIDENCE = 100;
    private static final int MAX_LINES = 100;

    private static int fCheckpointSize = -1;

    private final Map<String, String> fProperties = new HashMap<>();

    private final Map<Integer, String> fEntityTable = new TreeMap<>();
    private final Map<BtfEventType, String> fEntityTypeTable = new HashMap<>();
    private final Map<Integer, BtfEventType> fEntityTypes = new TreeMap<>();

    private String fVersion;
    private String fCreator;
    private String fCreationDate;
    private String fInputFile;
    // default unit is ns
    private BtfTimstampFormat fTsFormat = BtfTimstampFormat.NS;

    private File fFile;
    private RandomAccessFile fFileInput;
    private long fDataOffset;
    private long fTsOffset = 0;

    /**
     * Default constructor
     */
    public BtfTrace() {
        super();
        setCacheSize(CACHE_SIZE);
        fProperties.put(TIMESCALE, fTsFormat.toString());
    }

    private void parseHeader(RandomAccessFile input) throws IOException {
        String line = input.readLine();
        long pos = 0;
        while (line != null && line.startsWith("#")) { //$NON-NLS-1$
            String[] tokens = line.split(" ", 2); //$NON-NLS-1$
            /*
             * please note that the examples we were given and the spec are NOT
             * consistent, so we are ignoring the case to avoid issues
             */
            switch (tokens[0].toLowerCase()) {
            case VERSION:
                fVersion = tokens[1];
                fProperties.put(VERSION, fVersion);
                break;
            case CREATOR:
                fCreator = tokens[1];
                fProperties.put(CREATOR, fCreator);
                break;
            case lCREATIONDATE:
                fCreationDate = tokens[1];
                fProperties.put(CREATIONDATE, fCreationDate);

                try {
                    Date dateTime = ISO8601DATEFORMAT.parse(fCreationDate);
                    fTsOffset = dateTime.getTime() * 1000000L;
                } catch (ParseException e) {
                    Activator.logWarning("Creation date error: " + e.getMessage()); //$NON-NLS-1$
                }
                break;
            case lINPUTFILE:
                fInputFile = tokens[1];
                fProperties.put(INPUTFILE, fInputFile);
                break;
            case lTIMESCALE:
                fTsFormat = BtfTimstampFormat.parse(tokens[1]);
                fProperties.put(TIMESCALE, fTsFormat.toString());
                break;
            case lENTITYTYPE:
                pos = fFileInput.getFilePointer();
                line = fFileInput.readLine();
                while (line.startsWith("#-")) { //$NON-NLS-1$
                    String tempLine = line.substring(1);
                    String[] elements = tempLine.split(" ", 2); //$NON-NLS-1$
                    fEntityTypes.put(Integer.parseInt(elements[0]), BtfEventTypeFactory.parse(elements[1]));
                    pos = fFileInput.getFilePointer();
                    line = fFileInput.readLine();
                }
                fFileInput.seek(pos);
                fProperties.put(ENTITYTYPE, fEntityTypes.toString());
                break;
            case lENTITYTABLE:
                pos = fFileInput.getFilePointer();
                line = fFileInput.readLine();
                while (line.startsWith("#-")) { //$NON-NLS-1$
                    String tempLine = line.substring(1);
                    String[] elements = tempLine.split(" ", 2); //$NON-NLS-1$
                    fEntityTable.put(Integer.parseInt(elements[0]), elements[1]);
                    pos = fFileInput.getFilePointer();
                    line = fFileInput.readLine();
                }
                fProperties.put(ENTITYTABLE, fEntityTable.toString());
                fFileInput.seek(pos);
                break;
            case lENTITYTYPETABLE:
                pos = fFileInput.getFilePointer();
                line = fFileInput.readLine();
                while (line.startsWith("#-")) { //$NON-NLS-1$
                    String tempLine = line.substring(1);
                    String[] elements = tempLine.split(" ", 2); //$NON-NLS-1$
                    fEntityTypeTable.put(BtfEventTypeFactory.parse(elements[0]), elements[1]);
                    pos = fFileInput.getFilePointer();
                    line = fFileInput.readLine();
                }
                fFileInput.seek(pos);
                fProperties.put(ENTITYTYPETABLE, fEntityTypeTable.toString());
                break;
            default:
                break;
            }
            fDataOffset = input.getFilePointer();
            line = input.readLine();
        }
        fTsOffset = (long) (fTsOffset * fTsFormat.getScaleFactor());
    }

    @Override
    public void initTrace(IResource resource, String path, Class<? extends ITmfEvent> type) throws TmfTraceException {
        super.initTrace(resource, path, type);
        fFile = new File(path);
        try {
            fFileInput = new RandomAccessFile(fFile, "r"); //$NON-NLS-1$
            parseHeader(fFileInput);
        } catch (IOException e) {
            throw new TmfTraceException(e.getMessage(), e);
        }

    }

    @Override
    public IStatus validate(IProject project, String path) {
        File file = new File(path);
        if (!file.exists()) {
            return new Status(IStatus.ERROR, Activator.PLUGIN_ID, "File not found: " + path); //$NON-NLS-1$
        }
        if (!file.isFile()) {
            return new Status(IStatus.ERROR, Activator.PLUGIN_ID, "Not a file. It's a directory: " + path); //$NON-NLS-1$
        }
        int confidence = 0;
        try (BufferedRandomAccessFile rafile = new BufferedRandomAccessFile(path, "r")) { //$NON-NLS-1$
            int lineCount = 0;
            int matches = 0;
            String line = rafile.getNextLine();
            while ((line != null) && line.startsWith("#")) { //$NON-NLS-1$
                line = rafile.getNextLine();
            }
            while ((line != null) && (lineCount++ < MAX_LINES)) {
                try {
                    ITmfEvent event = parseLine(0, line);
                    if (event != null) {
                        matches++;
                    }
                } catch (RuntimeException e) {
                    confidence = Integer.MIN_VALUE;
                }

                confidence = MAX_CONFIDENCE * matches / lineCount;
                line = rafile.getNextLine();
            }
        } catch (IOException e) {
            Activator.logError("Error validating file: " + path, e); //$NON-NLS-1$
            return new Status(IStatus.ERROR, Activator.PLUGIN_ID, "IOException validating file: " + path, e); //$NON-NLS-1$
        }

        return new TraceValidationStatus(confidence, Activator.PLUGIN_ID);
    }

    @Override
    public ITmfLocation getCurrentLocation() {
        long temp = -1;
        try {
            temp = fFileInput.getFilePointer();
        } catch (IOException e) {
        }
        return new TmfLongLocation(temp);
    }

    @Override
    public double getLocationRatio(ITmfLocation location) {
        long size = fFile.length() - fDataOffset;
        long pos;
        try {
            pos = fFileInput.getFilePointer() - fDataOffset;
        } catch (IOException e) {
            pos = 0;
        }
        return 1.0 / size * pos;
    }

    @Override
    public ITmfContext seekEvent(ITmfLocation location) {
        final TmfContext context = new TmfContext(NULL_LOCATION, ITmfContext.UNKNOWN_RANK);
        if (NULL_LOCATION.equals(location) || fFile == null) {
            return context;
        }
        try {
            if (location == null) {
                fFileInput.seek(fDataOffset);
            } else if (location.getLocationInfo() instanceof Long) {
                fFileInput.seek((Long) location.getLocationInfo());
            }
            context.setLocation(new TmfLongLocation(fFileInput.getFilePointer()));
            return context;
        } catch (final FileNotFoundException e) {
            Activator.logError("Error seeking event. File not found: " + getPath(), e); //$NON-NLS-1$
            return context;
        } catch (final IOException e) {
            Activator.logError("Error seeking event. File: " + getPath(), e); //$NON-NLS-1$
            return context;
        }
    }

    @Override
    public ITmfContext seekEvent(double ratio) {
        if (fFile == null) {
            return new TmfContext(NULL_LOCATION, ITmfContext.UNKNOWN_RANK);
        }
        try {
            long pos = Math.round(ratio * fFile.length()) - fDataOffset;
            while (pos > 0) {
                fFileInput.seek(pos - 1);
                if (fFileInput.read() == '\n') {
                    break;
                }
                pos--;
            }
            final ITmfLocation location = new TmfLongLocation(pos);
            final ITmfContext context = seekEvent(location);
            context.setRank(ITmfContext.UNKNOWN_RANK);
            return context;
        } catch (final IOException e) {
            Activator.logError("Error seeking event. File: " + getPath(), e); //$NON-NLS-1$
            return new CustomTxtTraceContext(NULL_LOCATION, ITmfContext.UNKNOWN_RANK);
        }
    }

    @Override
    public ITmfEvent parseEvent(ITmfContext tmfContext) {
        if (fFile == null || (!(tmfContext instanceof TmfContext))) {
            return null;
        }

        final TmfContext context = (TmfContext) tmfContext;
        if (context.getLocation() == null
                || !(context.getLocation().getLocationInfo() instanceof Long)
                || NULL_LOCATION.equals(context.getLocation())) {
            return null;
        }

        return parseLine(context);

    }

    /**
     * Parse a line with a context
     *
     * @param context
     *            the context, has a location
     * @return the event from a given line
     */
    private ITmfEvent parseLine(TmfContext context) {
        try {
            if (!context.getLocation().getLocationInfo().equals(fFileInput.getFilePointer())) {
                seekEvent(context.getLocation());
            }
        } catch (IOException e1) {
            seekEvent(context.getLocation());
        }
        String line;
        try {
            line = fFileInput.readLine();
            return parseLine(context.getRank(), line);

        } catch (IOException e) {
        }

        return null;
    }

    /**
     * Parse a line of text and make an event using it.
     *
     * @param rank
     *            the rank of the event
     * @param line
     *            the raw string of the event
     * @return the event
     */
    private ITmfEvent parseLine(long rank, String line) {
        if (line == null) {
            return null;
        }
        String[] tokens = line.split(",", 7); //$NON-NLS-1$
        long timestamp = Long.parseLong(tokens[0]);
        String source = tokens[1];
        long sourceInstance = Long.parseLong(tokens[2]);
        BtfEventType type = BtfEventTypeFactory.parse(tokens[3]);
        String target = tokens[4];
        long targetInstance = Long.parseLong(tokens[5]);
        String event = tokens[6];

        ITmfEventField content = type.generateContent(event, sourceInstance, targetInstance);

        return new BtfEvent(this, rank,
                fTsFormat.createTimestamp(getTimestampTransform().transform(timestamp + fTsOffset)),
                source,
                type,
                type.getDescription(),
                content,
                target);
    }

    @Override
    public int getCheckpointSize() {
        synchronized (getClass()) {
            if (fCheckpointSize == -1) {
                TmfCheckpoint c = new TmfCheckpoint(TmfTimestamp.ZERO, new TmfLongLocation(0L), 0);
                ByteBuffer b = ByteBuffer.allocate(ITmfCheckpoint.MAX_SERIALIZE_SIZE);
                b.clear();
                c.serialize(b);
                fCheckpointSize = b.position();
            }
        }

        return fCheckpointSize;
    }

    @Override
    public ITmfLocation restoreLocation(ByteBuffer bufferIn) {
        return new TmfLongLocation(bufferIn);
    }

    @Override
    protected ITmfTraceIndexer createIndexer(int interval) {
        return new TmfBTreeTraceIndexer(this, interval);
    }

    @Override
    public Map<String, String> getTraceProperties() {
        return ImmutableMap.copyOf(fProperties);
    }

    @Override
    public void close() throws IOException {
        if (fFileInput != null) {
            fFileInput.close();
        }
    }

}
