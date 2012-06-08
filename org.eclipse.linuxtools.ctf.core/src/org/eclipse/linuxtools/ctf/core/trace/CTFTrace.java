/*******************************************************************************
 * Copyright (c) 2011-2012 Ericsson, Ecole Polytechnique de Montreal and others
 *
 * All rights reserved. This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors: Matthew Khouzam - Initial API and implementation
 * Contributors: Alexandre Montplaisir - Initial API and implementation
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
import java.util.UUID;

import org.eclipse.linuxtools.ctf.core.event.CTFClock;
import org.eclipse.linuxtools.ctf.core.event.EventDeclaration;
import org.eclipse.linuxtools.ctf.core.event.EventDefinition;
import org.eclipse.linuxtools.ctf.core.event.types.ArrayDefinition;
import org.eclipse.linuxtools.ctf.core.event.types.Definition;
import org.eclipse.linuxtools.ctf.core.event.types.IDefinitionScope;
import org.eclipse.linuxtools.ctf.core.event.types.IntegerDefinition;
import org.eclipse.linuxtools.ctf.core.event.types.StructDeclaration;
import org.eclipse.linuxtools.ctf.core.event.types.StructDefinition;
import org.eclipse.linuxtools.internal.ctf.core.event.io.BitBuffer;
import org.eclipse.linuxtools.internal.ctf.core.event.metadata.exceptions.ParseException;
import org.eclipse.linuxtools.internal.ctf.core.trace.Stream;
import org.eclipse.linuxtools.internal.ctf.core.trace.StreamInput;
import org.eclipse.linuxtools.internal.ctf.core.trace.StreamInputPacketIndex;

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

    // ------------------------------------------------------------------------
    // Attributes
    // ------------------------------------------------------------------------

    /*
     * (non-Javadoc)
     *
     * @see java.lang.Object#toString()
     */
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
     * The metadata parsing object.
     */
    private final Metadata metadata;

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
     * Packet header structure definition
     *
     * This is only used when opening the trace files, to read the first packet
     * header and see if they are valid trace files.
     */
    private StructDefinition packetHeaderDef;

    /**
     * Collection of streams contained in the trace.
     */
    private final HashMap<Long, Stream> streams;

    /**
     * Collection of environment variables set by the tracer
     */
    private final HashMap<String, String> environment;

    /**
     * Collection of all the clocks in a system.
     */
    private final HashMap<String, CTFClock> clocks;

    /** FileChannels to the streams */
    private final List<FileChannel> streamFileChannels;

    /** Handlers for the metadata files */
    private final static FileFilter metadataFileFilter = new MetadataFileFilter();
    private final static Comparator<File> metadataComparator = new MetadataComparator(); // $codepro.audit.disable
                                                                                         // fieldJavadoc

    /** map of all the event types */
    private final HashMap<Long,HashMap<Long, EventDeclaration>> eventDecs;
    /** map of all the event types */
    private final HashMap<StreamInput,HashMap<Long, EventDefinition>> eventDefs;
    /** map of all the indexes */
    private final HashMap<StreamInput, StreamInputPacketIndex> indexes;



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
        this.metadata = new Metadata(this);

        /* Set up the internal containers for this trace */
        streams = new HashMap<Long, Stream>();
        environment = new HashMap<String, String>();
        clocks = new HashMap<String, CTFClock>();
        streamFileChannels = new LinkedList<FileChannel>();
        eventDecs = new HashMap<Long, HashMap<Long, EventDeclaration>>();
        eventDefs = new HashMap<StreamInput, HashMap<Long, EventDefinition>>();

        if (!this.path.isDirectory()) {
            throw new CTFReaderException("Path must be a valid directory"); //$NON-NLS-1$
        }

        /* Open and parse the metadata file */
        metadata.parse();

        /* Open all the trace files */
        /* Create the definitions needed to read things from the files */
        if (packetHeaderDecl != null) {
            packetHeaderDef = packetHeaderDecl.createDefinition(this,
                    "packet.header"); //$NON-NLS-1$
        }

        /* List files not called metadata and not hidden. */
        File[] files = path.listFiles(metadataFileFilter);
        Arrays.sort(files, metadataComparator);
        indexes = new HashMap<StreamInput, StreamInputPacketIndex>();
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
                Iterator<Entry<Long, EventDeclaration>> it = s.getStream()
                        .getEvents().entrySet().iterator();
                while (it.hasNext()) {
                    Map.Entry<Long, EventDeclaration> pairs = it.next();
                    Long eventNum = pairs.getKey();
                    EventDeclaration eventDec = pairs.getValue();
                    getEvents(s.getStream().getId()).put(eventNum, eventDec);
                }

                /*
                 * index the trace
                 */
                s.setupIndex();
            }
        }
    }

    @Override
    protected void finalize() throws Throwable {
        /* If this trace gets closed, release the descriptors to the streams */
        for (FileChannel fc : streamFileChannels) {
            if (fc != null) {
                try {
                    fc.close();
                } catch (IOException e) {
                    // do nothing it's ok, we tried to close it.
                }
            }
        }
        super.finalize();

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
     */
    public HashMap<Long, EventDeclaration> getEvents(Long streamId) {
        return eventDecs.get(streamId);
    }

    /**
     * Gets an index for a given StreamInput
     * @param id the StreamInput
     * @return The index
     */
    public StreamInputPacketIndex getIndex(StreamInput id){
        if(! indexes.containsKey(id)){
            indexes.put(id, new StreamInputPacketIndex());
        }
        return indexes.get(id);
    }

    /**
     * Gets an event Declaration hashmap for a given StreamInput
     * @param id the StreamInput
     * @return the hashmap with the event definitions
     */
    public HashMap<Long, EventDefinition> getEventDefs(StreamInput id) {
        if(! eventDefs.containsKey(id)){
            eventDefs.put(id, new HashMap<Long, EventDefinition>());
        }
        return eventDefs.get(id);
    }

    /**
     * Get an event by it's ID
     *
     * @param streamId
     *            The ID of the stream from which to read
     * @param id
     *            the ID of the event
     * @return the event declaration
     */
    public EventDeclaration getEventType(long streamId, long id) {
        return getEvents(streamId).get(id);
    }

    /**
     * Method getStream gets the stream for a given id
     *
     * @param id
     *            Long the id of the stream
     * @return Stream the stream that we need
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
     * Method majortIsSet is the major version number set?
     *
     * @return boolean is the major set?
     */
    public boolean majortIsSet() {
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
     */
    public boolean UUIDIsSet() {
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
    public ByteOrder getByteOrder() {
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
            fc = new FileInputStream(streamFile).getChannel();
            streamFileChannels.add(fc);

            /* Map one memory page of 4 kiB */
            byteBuffer = fc.map(MapMode.READ_ONLY, 0, 4096);
        } catch (IOException e) {
            /* Shouldn't happen at this stage if every other check passed */
            throw new CTFReaderException();
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

            /* Read stream ID */
            // TODO: it hasn't been checked that the stream_id field exists and
            // is an unsigned
            // integer
            IntegerDefinition streamIDDef = (IntegerDefinition) packetHeaderDef
                    .lookupDefinition("stream_id"); //$NON-NLS-1$
            assert (streamIDDef != null);

            long streamID = streamIDDef.getValue();

            /* Get the stream to which this trace file belongs to */
            stream = streams.get(streamID);
        } else {
            /* No packet header, we suppose there is only one stream */
            stream = streams.get(null);
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
         * If the stream we try to add has the null key, it must be the onl         * one. Thus, if the streams container is not empty, it is not valid.
         */
        if ((stream.getId() == null) && (streams.size() != 0)) {
            throw new ParseException("Stream without id with multiple streams"); //$NON-NLS-1$
        }

        /* If a stream with the same ID already exists, it is not valid. */
        if (streams.get(stream.getId()) != null) {
            throw new ParseException("Stream id already exists"); //$NON-NLS-1$
        }

        /* It should be ok now. */
        streams.put(stream.getId(), stream);
        eventDecs.put(stream.getId(), new HashMap<Long,EventDeclaration>());
    }

    /**
     * gets the Environment variables from the trace metadata (See CTF spec)
     * @return the environment variables in a hashmap form (key value)
     */
    public HashMap<String, String> getEnvironment() {
        return environment;
    }

    /**
     * Look up a specific environment variable
     * @param key the key to look for
     * @return the value of the variable, can be null.
     */
    public String lookupEnvironment(String key) {
        return environment.get(key);
    }

    /**
     * Add a variable to the environment variables
     * @param varName the name of the variable
     * @param varValue the value of the variable
     */
    public void addEnvironmentVar(String varName, String varValue) {
        environment.put(varName, varValue);
    }

    /**
     * Add a clock to the clock list
     * @param nameValue the name of the clock (full name with scope)
     * @param ctfClock the clock
     */
    public void addClock(String nameValue, CTFClock ctfClock) {
        clocks.put(nameValue, ctfClock);
    }

    /**
     * gets the clock with a specific name
     * @param name the name of the clock.
     * @return the clock
     */
    public CTFClock getClock(String name) {
        return clocks.get(name);
    }

    private CTFClock singleClock;
    private long singleOffset;

    /**
     * gets the clock if there is only one. (this is 100% of the use cases as of June 2012)
     * @return the clock
     */
    public final CTFClock getClock() {
        if (clocks.size() == 1) {
            if (singleClock == null) {
                singleClock = clocks.get(clocks.keySet().toArray()[0]);
                singleOffset = (Long) getClock().getProperty("offset"); //$NON-NLS-1$
            }
            return singleClock;
        }
        return null;
    }

    /**
     * gets the time offset of a clock with respect to UTC in nanoseconds
     * @return the time offset of a clock with respect to UTC in nanoseconds
     */
    public final long getOffset() {
        if (getClock() == null) {
            return 0;
        }
        return singleOffset;
    }

    /**
     * Does a given stream contain any events?
     * @param id the stream ID
     * @return true if the stream has events.
     */
    public boolean hasEvents(Long id){
        return eventDecs.containsKey(id);
    }

    /**
     * Add an event declaration map to the events map.
     * @param id the id of a stream
     * @return the hashmap containing events.
     */
    public HashMap<Long, EventDeclaration> createEvents(Long id){
        HashMap<Long, EventDeclaration> value = eventDecs.get(id);
        if( value == null ) {
            value = new HashMap<Long, EventDeclaration>();
            eventDecs.put(id, value);
        }
        return value;
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
