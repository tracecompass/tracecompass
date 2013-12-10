/*******************************************************************************
 * Copyright (c) 2011, 2013 Ericsson, Ecole Polytechnique de Montreal and others
 *
 * All rights reserved. This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Matthew Khouzam - Initial API and implementation
 *     Alexandre Montplaisir - Initial API and implementation
 *     Simon Delisle - Replace LinkedList by TreeSet in callsitesByName attribute
 *******************************************************************************/

package org.eclipse.linuxtools.ctf.core.trace;

import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.Serializable;
import java.nio.ByteOrder;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.channels.FileChannel.MapMode;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeSet;
import java.util.UUID;

import org.eclipse.linuxtools.ctf.core.event.CTFCallsite;
import org.eclipse.linuxtools.ctf.core.event.CTFClock;
import org.eclipse.linuxtools.ctf.core.event.EventDefinition;
import org.eclipse.linuxtools.ctf.core.event.IEventDeclaration;
import org.eclipse.linuxtools.ctf.core.event.io.BitBuffer;
import org.eclipse.linuxtools.ctf.core.event.types.ArrayDefinition;
import org.eclipse.linuxtools.ctf.core.event.types.Definition;
import org.eclipse.linuxtools.ctf.core.event.types.IDefinitionScope;
import org.eclipse.linuxtools.ctf.core.event.types.IntegerDefinition;
import org.eclipse.linuxtools.ctf.core.event.types.StructDeclaration;
import org.eclipse.linuxtools.ctf.core.event.types.StructDefinition;
import org.eclipse.linuxtools.internal.ctf.core.event.CTFCallsiteComparator;
import org.eclipse.linuxtools.internal.ctf.core.event.metadata.exceptions.ParseException;

/**
 * A CTF trace on the file system.
 *
 * Represents a trace on the filesystem. It is responsible of parsing the
 * metadata, creating declarations data structures, indexing the event packets
 * (in other words, all the work that can be shared between readers), but the
 * actual reading of events is left to TraceReader.
 *
 * @author Matthew Khouzam
 * @version $Revision: 1.0 $
 */
public class CTFTrace implements IDefinitionScope {

    @SuppressWarnings("nls")
    @Override
    public String toString() {
        /* Only for debugging, shouldn't be externalized */
        return "CTFTrace [path=" + path + ", major=" + major + ", minor="
                + minor + ", uuid=" + uuid + "]";
    }

    /**
     * The trace directory on the filesystem.
     */
    private final File path;

    /**
     * Major CTF version number
     */
    private Long major;

    /**
     * Minor CTF version number
     */
    private Long minor;

    /**
     * Trace UUID
     */
    private UUID uuid;

    /**
     * Trace byte order
     */
    private ByteOrder byteOrder;

    /**
     * Packet header structure declaration
     */
    private StructDeclaration packetHeaderDecl = null;

    /**
     * The clock of the trace
     */
    private CTFClock singleClock;

    /**
     * Packet header structure definition
     *
     * This is only used when opening the trace files, to read the first packet
     * header and see if they are valid trace files.
     */
    private StructDefinition packetHeaderDef;

    /**
     * Collection of streams contained in the trace.
     */
    private final Map<Long, Stream> streams = new HashMap<Long, Stream>();

    /**
     * Collection of environment variables set by the tracer
     */
    private final Map<String, String> environment = new HashMap<String, String>();

    /**
     * Collection of all the clocks in a system.
     */
    private final Map<String, CTFClock> clocks = new HashMap<String, CTFClock>();

    /** FileInputStreams to the streams */
    private final List<FileInputStream> fileInputStreams = new LinkedList<FileInputStream>();

    /** Handlers for the metadata files */
    private static final FileFilter METADATA_FILE_FILTER = new MetadataFileFilter();
    private static final Comparator<File> METADATA_COMPARATOR = new MetadataComparator();

    /** Callsite helpers */
    private CTFCallsiteComparator ctfCallsiteComparator = new CTFCallsiteComparator();

    private Map<String, TreeSet<CTFCallsite>> callsitesByName = new HashMap<String, TreeSet<CTFCallsite>>();

    /** Callsite helpers */
    private TreeSet<CTFCallsite> callsitesByIP = new TreeSet<CTFCallsite>();

    // ------------------------------------------------------------------------
    // Constructors
    // ------------------------------------------------------------------------

    /**
     * Trace constructor.
     *
     * @param path
     *            Filesystem path of the trace directory
     * @throws CTFReaderException
     *             If no CTF trace was found at the path
     */
    public CTFTrace(String path) throws CTFReaderException {
        this(new File(path));

    }

    /**
     * Trace constructor.
     *
     * @param path
     *            Filesystem path of the trace directory.
     * @throws CTFReaderException
     *             If no CTF trace was found at the path
     */
    public CTFTrace(File path) throws CTFReaderException {
        this.path = path;
        final Metadata metadata = new Metadata(this);

        /* Set up the internal containers for this trace */
        if (!this.path.exists()) {
            throw new CTFReaderException("Trace (" + path.getPath() + ") doesn't exist. Deleted or moved?"); //$NON-NLS-1$ //$NON-NLS-2$
        }

        if (!this.path.isDirectory()) {
            throw new CTFReaderException("Path must be a valid directory"); //$NON-NLS-1$
        }

        /* Open and parse the metadata file */
        metadata.parse();

        /* Open all the trace files */
        /* Create the definitions needed to read things from the files */
        if (packetHeaderDecl != null) {
            packetHeaderDef = packetHeaderDecl.createDefinition(this, "packet.header"); //$NON-NLS-1$
        }

        /* List files not called metadata and not hidden. */
        File[] files = path.listFiles(METADATA_FILE_FILTER);
        Arrays.sort(files, METADATA_COMPARATOR);
        /* Try to open each file */
        for (File streamFile : files) {
            openStreamInput(streamFile);
        }

        /* Create their index */
        for (Map.Entry<Long, Stream> stream : streams.entrySet()) {
            Set<StreamInput> inputs = stream.getValue().getStreamInputs();
            for (StreamInput s : inputs) {
                /*
                 * Copy the events
                 */
                Iterator<Entry<Long, IEventDeclaration>> it = s.getStream()
                        .getEvents().entrySet().iterator();
                while (it.hasNext()) {
                    Entry<Long, IEventDeclaration> pairs = it.next();
                    Long eventNum = pairs.getKey();
                    IEventDeclaration eventDec = pairs.getValue();
                    getEvents(s.getStream().getId()).put(eventNum, eventDec);
                }

                /*
                 * index the trace
                 */
                s.setupIndex();
            }
        }
    }

    /**
     * Dispose the trace
     *
     * @since 2.0
     */
    public void dispose() {
        for (FileInputStream fis : fileInputStreams) {
            if (fis != null) {
                try {
                    fis.close();
                } catch (IOException e) {
                    // do nothing it's ok, we tried to close it.
                }
            }
        }
        // Invoke GC to release MappedByteBuffer objects (Java bug JDK-4724038)
        System.gc();
    }

    // ------------------------------------------------------------------------
    // Getters/Setters/Predicates
    // ------------------------------------------------------------------------

    /**
     * Gets an event declaration hash map for a given streamID
     *
     * @param streamId
     *            The ID of the stream from which to read
     * @return The Hash map with the event declarations
     * @since 2.0
     */
    public Map<Long, IEventDeclaration> getEvents(Long streamId) {
        return streams.get(streamId).getEvents();
    }

    /**
     * Gets an event Declaration hashmap for a given StreamInput
     *
     * @param id
     *            the StreamInput
     * @return an empty hashmap, please see deprecated
     * @since 2.0
     * @deprecated You should be using
     *             {@link StreamInputReader#getEventDefinitions()} instead.
     */
    @Deprecated
    public Map<Long, EventDefinition> getEventDefs(StreamInput id) {
        return new HashMap<Long, EventDefinition>();
    }

    /**
     * Get an event by it's ID
     *
     * @param streamId
     *            The ID of the stream from which to read
     * @param id
     *            the ID of the event
     * @return the event declaration
     * @since 2.0
     */
    public IEventDeclaration getEventType(long streamId, long id) {
        return getEvents(streamId).get(id);
    }

    /**
     * Method getStream gets the stream for a given id
     *
     * @param id
     *            Long the id of the stream
     * @return Stream the stream that we need
     * @since 2.0
     */
    public Stream getStream(Long id) {
        return streams.get(id);
    }

    /**
     * Method nbStreams gets the number of available streams
     *
     * @return int the number of streams
     */
    public int nbStreams() {
        return streams.size();
    }

    /**
     * Method setMajor sets the major version of the trace (DO NOT USE)
     *
     * @param major
     *            long the major version
     */
    public void setMajor(long major) {
        this.major = major;
    }

    /**
     * Method setMinor sets the minor version of the trace (DO NOT USE)
     *
     * @param minor
     *            long the minor version
     */
    public void setMinor(long minor) {
        this.minor = minor;
    }

    /**
     * Method setUUID sets the UUID of a trace
     *
     * @param uuid
     *            UUID
     */
    public void setUUID(UUID uuid) {
        this.uuid = uuid;
    }

    /**
     * Method setByteOrder sets the byte order
     *
     * @param byteOrder
     *            ByteOrder of the trace, can be little-endian or big-endian
     */
    public void setByteOrder(ByteOrder byteOrder) {
        this.byteOrder = byteOrder;
    }

    /**
     * Method setPacketHeader sets the packet header of a trace (DO NOT USE)
     *
     * @param packetHeader
     *            StructDeclaration the header in structdeclaration form
     */
    public void setPacketHeader(StructDeclaration packetHeader) {
        this.packetHeaderDecl = packetHeader;
    }

    /**
     * Method majorIsSet is the major version number set?
     *
     * @return boolean is the major set?
     * @since 3.0
     */
    public boolean majorIsSet() {
        return major != null;
    }

    /**
     * Method minorIsSet. is the minor version number set?
     *
     * @return boolean is the minor set?
     */
    public boolean minorIsSet() {
        return minor != null;
    }

    /**
     * Method UUIDIsSet is the UUID set?
     *
     * @return boolean is the UUID set?
     * @since 2.0
     */
    public boolean uuidIsSet() {
        return uuid != null;
    }

    /**
     * Method byteOrderIsSet is the byteorder set?
     *
     * @return boolean is the byteorder set?
     */
    public boolean byteOrderIsSet() {
        return byteOrder != null;
    }

    /**
     * Method packetHeaderIsSet is the packet header set?
     *
     * @return boolean is the packet header set?
     */
    public boolean packetHeaderIsSet() {
        return packetHeaderDecl != null;
    }

    /**
     * Method getUUID gets the trace UUID
     *
     * @return UUID gets the trace UUID
     */
    public UUID getUUID() {
        return uuid;
    }

    /**
     * Method getMajor gets the trace major version
     *
     * @return long gets the trace major version
     */
    public long getMajor() {
        return major;
    }

    /**
     * Method getMinor gets the trace minor version
     *
     * @return long gets the trace minor version
     */
    public long getMinor() {
        return minor;
    }

    /**
     * Method getByteOrder gets the trace byte order
     *
     * @return ByteOrder gets the trace byte order
     */
    public final ByteOrder getByteOrder() {
        return byteOrder;
    }

    /**
     * Method getPacketHeader gets the trace packet header
     *
     * @return StructDeclaration gets the trace packet header
     */
    public StructDeclaration getPacketHeader() {
        return packetHeaderDecl;
    }

    /**
     * Method getTraceDirectory gets the trace directory
     *
     * @return File the path in "File" format.
     */
    public File getTraceDirectory() {
        return path;
    }

    /**
     * Method getStreams get all the streams in a map format.
     *
     * @return Map<Long,Stream> a map of all the streams.
     */
    public Map<Long, Stream> getStreams() {
        return streams;
    }

    /**
     * Method getPath gets the path of the trace directory
     *
     * @return String the path of the trace directory, in string format.
     * @see java.io.File#getPath()
     */
    @Override
    public String getPath() {
        return path.getPath();
    }

    // ------------------------------------------------------------------------
    // Operations
    // ------------------------------------------------------------------------

    /**
     * Tries to open the given file, reads the first packet header of the file
     * and check its validity.
     *
     * @param streamFile
     *            A trace file in the trace directory.
     * @param index
     *            Which index in the class' streamFileChannel array this file
     *            must use
     * @throws CTFReaderException
     */
    private void openStreamInput(File streamFile) throws CTFReaderException {
        MappedByteBuffer byteBuffer;
        BitBuffer streamBitBuffer;
        Stream stream;
        FileChannel fc;

        if (!streamFile.canRead()) {
            throw new CTFReaderException("Unreadable file : " //$NON-NLS-1$
                    + streamFile.getPath());
        }

        try {
            /* Open the file and get the FileChannel */
            FileInputStream fis = new FileInputStream(streamFile);
            fileInputStreams.add(fis);
            fc = fis.getChannel();

            /* Map one memory page of 4 kiB */
            byteBuffer = fc.map(MapMode.READ_ONLY, 0, (int) Math.min(fc.size(), 4096L));
        } catch (IOException e) {
            /* Shouldn't happen at this stage if every other check passed */
            throw new CTFReaderException(e);
        }

        /* Create a BitBuffer with this mapping and the trace byte order */
        streamBitBuffer = new BitBuffer(byteBuffer, this.getByteOrder());

        if (packetHeaderDef != null) {
            /* Read the packet header */
            packetHeaderDef.read(streamBitBuffer);

            /* Check the magic number */
            IntegerDefinition magicDef = (IntegerDefinition) packetHeaderDef
                    .lookupDefinition("magic"); //$NON-NLS-1$
            int magic = (int) magicDef.getValue();
            if (magic != Utils.CTF_MAGIC) {
                throw new CTFReaderException("CTF magic mismatch"); //$NON-NLS-1$
            }

            /* Check UUID */
            ArrayDefinition uuidDef = (ArrayDefinition) packetHeaderDef
                    .lookupDefinition("uuid"); //$NON-NLS-1$
            if (uuidDef != null) {
                byte[] uuidArray = new byte[Utils.UUID_LEN];

                for (int i = 0; i < Utils.UUID_LEN; i++) {
                    IntegerDefinition uuidByteDef = (IntegerDefinition) uuidDef
                            .getElem(i);
                    uuidArray[i] = (byte) uuidByteDef.getValue();
                }

                UUID otheruuid = Utils.makeUUID(uuidArray);

                if (!this.uuid.equals(otheruuid)) {
                    throw new CTFReaderException("UUID mismatch"); //$NON-NLS-1$
                }
            }

            /* Read the stream ID */
            Definition streamIDDef = packetHeaderDef.lookupDefinition("stream_id"); //$NON-NLS-1$

            if (streamIDDef instanceof IntegerDefinition) { // this doubles as a
                                                            // null check
                long streamID = ((IntegerDefinition) streamIDDef).getValue();
                stream = streams.get(streamID);
            } else {
                /* No stream_id in the packet header */
                stream = streams.get(null);
            }

        } else {
            /* No packet header, we suppose there is only one stream */
            stream = streams.get(null);
        }

        if (stream == null) {
            throw new CTFReaderException("Unexpected end of stream"); //$NON-NLS-1$
        }

        /* Create the stream input */
        StreamInput streamInput = new StreamInput(stream, fc, streamFile);

        /* Add a reference to the streamInput in the stream */
        stream.addInput(streamInput);
    }

    /**
     * Looks up a definition from packet
     *
     * @param lookupPath
     *            String
     * @return Definition
     * @see org.eclipse.linuxtools.ctf.core.event.types.IDefinitionScope#lookupDefinition(String)
     */
    @Override
    public Definition lookupDefinition(String lookupPath) {
        if (lookupPath.equals("trace.packet.header")) { //$NON-NLS-1$
            return packetHeaderDef;
        }
        return null;
    }

    /**
     * Adds a new stream to the trace.
     *
     * @param stream
     *            A stream object.
     * @throws ParseException
     *             If there was some problem reading the metadata
     * @since 2.0
     */
    public void addStream(Stream stream) throws ParseException {

        /*
         * If there is already a stream without id (the null key), it must be
         * the only one
         */
        if (streams.get(null) != null) {
            throw new ParseException("Stream without id with multiple streams"); //$NON-NLS-1$
        }

        /*
         * If the stream we try to add has the null key, it must be the only
         * one. Thus, if the streams container is not empty, it is not valid.
         */
        if ((stream.getId() == null) && (streams.size() != 0)) {
            throw new ParseException("Stream without id with multiple streams"); //$NON-NLS-1$
        }

        /* If a stream with the same ID already exists, it is not valid. */
        if (streams.get(stream.getId()) != null) {
            throw new ParseException("Stream id already exists"); //$NON-NLS-1$
        }

        /* This stream is valid and has a unique id. */
        streams.put(stream.getId(), stream);
    }

    /**
     * Gets the Environment variables from the trace metadata (See CTF spec)
     *
     * @return the environment variables in a map form (key value)
     * @since 2.0
     */
    public Map<String, String> getEnvironment() {
        return environment;
    }

    /**
     * Look up a specific environment variable
     *
     * @param key
     *            the key to look for
     * @return the value of the variable, can be null.
     */
    public String lookupEnvironment(String key) {
        return environment.get(key);
    }

    /**
     * Add a variable to the environment variables
     *
     * @param varName
     *            the name of the variable
     * @param varValue
     *            the value of the variable
     */
    public void addEnvironmentVar(String varName, String varValue) {
        environment.put(varName, varValue);
    }

    /**
     * Add a clock to the clock list
     *
     * @param nameValue
     *            the name of the clock (full name with scope)
     * @param ctfClock
     *            the clock
     */
    public void addClock(String nameValue, CTFClock ctfClock) {
        clocks.put(nameValue, ctfClock);
    }

    /**
     * gets the clock with a specific name
     *
     * @param name
     *            the name of the clock.
     * @return the clock
     */
    public CTFClock getClock(String name) {
        return clocks.get(name);
    }

    /**
     * gets the clock if there is only one. (this is 100% of the use cases as of
     * June 2012)
     *
     * @return the clock
     */
    public final CTFClock getClock() {
        if (clocks.size() == 1) {
            singleClock = clocks.get(clocks.keySet().iterator().next());
            return singleClock;
        }
        return null;
    }

    /**
     * gets the time offset of a clock with respect to UTC in nanoseconds
     *
     * @return the time offset of a clock with respect to UTC in nanoseconds
     */
    public final long getOffset() {
        if (getClock() == null) {
            return 0;
        }
        return singleClock.getClockOffset();
    }

    /**
     * gets the time offset of a clock with respect to UTC in nanoseconds
     *
     * @return the time offset of a clock with respect to UTC in nanoseconds
     */
    private double getTimeScale() {
        if (getClock() == null) {
            return 1.0;
        }
        return singleClock.getClockScale();
    }

    /**
     * Does the trace need to time scale?
     *
     * @return if the trace is in ns or cycles.
     */
    private boolean clockNeedsScale() {
        if (getClock() == null) {
            return false;
        }
        return singleClock.isClockScaled();
    }

    /**
     * the inverse clock for returning to a scale.
     *
     * @return 1.0 / scale
     */
    private double getInverseTimeScale() {
        if (getClock() == null) {
            return 1.0;
        }
        return singleClock.getClockAntiScale();
    }

    /**
     * @param cycles
     *            clock cycles since boot
     * @return time in nanoseconds UTC offset
     * @since 2.0
     */
    public long timestampCyclesToNanos(long cycles) {
        long retVal = cycles + getOffset();
        /*
         * this fix is since quite often the offset will be > than 53 bits and
         * therefore the conversion will be lossy
         */
        if (clockNeedsScale()) {
            retVal = (long) (retVal * getTimeScale());
        }
        return retVal;
    }

    /**
     * @param nanos
     *            time in nanoseconds UTC offset
     * @return clock cycles since boot.
     * @since 2.0
     */
    public long timestampNanoToCycles(long nanos) {
        long retVal;
        /*
         * this fix is since quite often the offset will be > than 53 bits and
         * therefore the conversion will be lossy
         */
        if (clockNeedsScale()) {
            retVal = (long) (nanos * getInverseTimeScale());
        } else {
            retVal = nanos;
        }
        return retVal - getOffset();
    }

    /**
     * Adds a callsite
     *
     * @param eventName
     *            the event name of the callsite
     * @param funcName
     *            the name of the callsite function
     * @param ip
     *            the ip of the callsite
     * @param fileName
     *            the filename of the callsite
     * @param lineNumber
     *            the line number of the callsite
     */
    public void addCallsite(String eventName, String funcName, long ip,
            String fileName, long lineNumber) {
        final CTFCallsite cs = new CTFCallsite(eventName, funcName, ip,
                fileName, lineNumber);
        TreeSet<CTFCallsite> csl = callsitesByName.get(eventName);
        if (csl == null) {
            csl = new TreeSet<CTFCallsite>(ctfCallsiteComparator);
            callsitesByName.put(eventName, csl);
        }

        csl.add(cs);

        callsitesByIP.add(cs);
    }

    /**
     * Gets the set of callsites associated to an event name. O(1)
     *
     * @param eventName
     *            the event name
     * @return the callsite set can be empty
     * @since 3.0
     */
    public TreeSet<CTFCallsite> getCallsiteCandidates(String eventName) {
        TreeSet<CTFCallsite> retVal = callsitesByName.get(eventName);
        if (retVal == null) {
            retVal = new TreeSet<CTFCallsite>(ctfCallsiteComparator);
        }
        return retVal;
    }

    /**
     * The I'm feeling lucky of getCallsiteCandidates O(1)
     *
     * @param eventName
     *            the event name
     * @return the first callsite that has that event name, can be null
     * @since 1.2
     */
    public CTFCallsite getCallsite(String eventName) {
        TreeSet<CTFCallsite> callsites = callsitesByName.get(eventName);
        if (callsites != null) {
            return callsites.first();
        }
        return null;
    }

    /**
     * Gets a callsite from the instruction pointer O(log(n))
     *
     * @param ip
     *            the instruction pointer to lookup
     * @return the callsite just before that IP in the list remember the IP is
     *         backwards on X86, can be null if no callsite is before the IP.
     * @since 1.2
     */
    public CTFCallsite getCallsite(long ip) {
        CTFCallsite cs = new CTFCallsite(null, null, ip, null, 0L);
        return callsitesByIP.ceiling(cs);
    }

    /**
     * Gets a callsite using the event name and instruction pointer O(log(n))
     *
     * @param eventName
     *            the name of the event
     * @param ip
     *            the instruction pointer
     * @return the closest matching callsite, can be null
     */
    public CTFCallsite getCallsite(String eventName, long ip) {
        final TreeSet<CTFCallsite> candidates = callsitesByName.get(eventName);
        final CTFCallsite dummyCs = new CTFCallsite(null, null, ip, null, -1);
        final CTFCallsite callsite = candidates.ceiling(dummyCs);
        if (callsite == null) {
            return candidates.floor(dummyCs);
        }
        return callsite;
    }
}

class MetadataFileFilter implements FileFilter {

    @Override
    public boolean accept(File pathname) {
        if (pathname.isDirectory()) {
            return false;
        }
        if (pathname.isHidden()) {
            return false;
        }
        if (pathname.getName().equals("metadata")) { //$NON-NLS-1$
            return false;
        }
        return true;
    }

}

class MetadataComparator implements Comparator<File>, Serializable {

    private static final long serialVersionUID = 1L;

    @Override
    public int compare(File o1, File o2) {
        return o1.getName().compareTo(o2.getName());
    }
}
