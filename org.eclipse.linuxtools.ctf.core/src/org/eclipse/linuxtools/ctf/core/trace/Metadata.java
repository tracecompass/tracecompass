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
 *     Simon Marchi - Initial API and implementation
 *******************************************************************************/

package org.eclipse.linuxtools.ctf.core.trace;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.channels.FileChannel;
import java.util.Arrays;
import java.util.UUID;

import org.antlr.runtime.ANTLRReaderStream;
import org.antlr.runtime.CommonTokenStream;
import org.antlr.runtime.MismatchedTokenException;
import org.antlr.runtime.RecognitionException;
import org.antlr.runtime.tree.CommonTree;
import org.eclipse.linuxtools.ctf.parser.CTFLexer;
import org.eclipse.linuxtools.ctf.parser.CTFParser;
import org.eclipse.linuxtools.ctf.parser.CTFParser.parse_return;
import org.eclipse.linuxtools.internal.ctf.core.event.metadata.IOStructGen;
import org.eclipse.linuxtools.internal.ctf.core.event.metadata.exceptions.CtfAntlrException;
import org.eclipse.linuxtools.internal.ctf.core.event.metadata.exceptions.ParseException;

/**
 * The CTF trace metadata file
 *
 * @version 1.0
 * @author Matthew Khouzam
 * @author Simon Marchi
 */
public class Metadata {

    // ------------------------------------------------------------------------
    // Constants
    // ------------------------------------------------------------------------

    /**
     * Name of the metadata file in the trace directory
     */
    private static final String METADATA_FILENAME = "metadata"; //$NON-NLS-1$

    /**
     * Size of the metadata packet header, in bytes, computed by hand.
     */
    private static final int METADATA_PACKET_HEADER_SIZE = 37;

    // ------------------------------------------------------------------------
    // Attributes
    // ------------------------------------------------------------------------

    /**
     * Reference to the metadata file
     */
    private File metadataFile = null;

    /**
     * Byte order as detected when reading the TSDL magic number.
     */
    private ByteOrder detectedByteOrder = null;

    /**
     * The trace file to which belongs this metadata file.
     */
    private CTFTrace trace = null;

    // ------------------------------------------------------------------------
    // Constructors
    // ------------------------------------------------------------------------

    /**
     * Constructs a Metadata object.
     *
     * @param trace
     *            The trace to which belongs this metadata file.
     */
    public Metadata(CTFTrace trace) {
        this.trace = trace;

        /* Path of metadata file = trace directory path + metadata filename */
        String metadataPath = trace.getTraceDirectory().getPath()
                + Utils.SEPARATOR + METADATA_FILENAME;

        /* Create a file reference to the metadata file */
        metadataFile = new File(metadataPath);
    }

    // ------------------------------------------------------------------------
    // Getters/Setters/Predicates
    // ------------------------------------------------------------------------

    /**
     * Returns the ByteOrder that was detected while parsing the metadata.
     *
     * @return The byte order.
     */
    public ByteOrder getDetectedByteOrder() {
        return detectedByteOrder;
    }

    // ------------------------------------------------------------------------
    // Operations
    // ------------------------------------------------------------------------

    /**
     * Parse the metadata file.
     *
     * @throws CTFReaderException
     *             If there was a problem parsing the metadata
     */
    public void parse() throws CTFReaderException {
        CTFReaderException tempException = null;

        FileInputStream fis = null;
        FileChannel metadataFileChannel = null;

        /*
         * Reader. It will contain a StringReader if we are using packet-based
         * metadata and it will contain a FileReader if we have text-based
         * metadata.
         */
        Reader metadataTextInput = null;

        try {
            fis = new FileInputStream(metadataFile);
            metadataFileChannel = fis.getChannel();

            /* Check if metadata is packet-based */
            if (isPacketBased(metadataFileChannel)) {
                /* Create StringBuffer to receive metadata text */
                StringBuffer metadataText = new StringBuffer();

                /*
                 * Read metadata packet one by one, appending the text to the
                 * StringBuffer
                 */
                MetadataPacketHeader packetHeader = readMetadataPacket(
                        metadataFileChannel, metadataText);
                while (packetHeader != null) {
                    packetHeader = readMetadataPacket(metadataFileChannel,
                            metadataText);
                }

                /* Wrap the metadata string with a StringReader */
                metadataTextInput = new StringReader(metadataText.toString());
            } else {
                /* Wrap the metadata file with a FileReader */
                metadataTextInput = new FileReader(metadataFile);
            }

            CommonTree tree = createAST(metadataTextInput);

            /* Generate IO structures (declarations) */
            IOStructGen gen = new IOStructGen(tree, trace);
            gen.generate();

        } catch (FileNotFoundException e) {
            tempException = new CTFReaderException("Cannot find metadata file!"); //$NON-NLS-1$
        } catch (IOException e) {
            /* This would indicate a problem with the ANTLR library... */
            tempException = new CTFReaderException(e);
        } catch (ParseException e) {
            tempException = new CTFReaderException(e);
        } catch (MismatchedTokenException e) {
            tempException = new CtfAntlrException(e);
        } catch (RecognitionException e) {
            tempException = new CtfAntlrException(e);
        }

        /* Ghetto resource management. Java 7 will deliver us from this... */
        if (metadataTextInput != null) {
            try {
                metadataTextInput.close();
            } catch (IOException e) {
                // Do nothing
            }
        }
        if (metadataFileChannel != null) {
            try {
                metadataFileChannel.close();
            } catch (IOException e) {
                // Do nothing
            }
        }
        if (fis != null) {
            try {
                fis.close();
            } catch (IOException e) {
                // Do nothing
            }
        }

        if (tempException != null) {
            throw tempException;
        }
    }

    private static CommonTree createAST(Reader metadataTextInput) throws IOException,
            RecognitionException {
        /* Create an ANTLR reader */
        ANTLRReaderStream antlrStream;
        antlrStream = new ANTLRReaderStream(metadataTextInput);

        /* Parse the metadata text and get the AST */
        CTFLexer ctfLexer = new CTFLexer(antlrStream);
        CommonTokenStream tokens = new CommonTokenStream(ctfLexer);
        CTFParser ctfParser = new CTFParser(tokens, false);

        parse_return pr = ctfParser.parse();
        return (CommonTree) pr.getTree();
    }

    /**
     * Determines whether the metadata file is packet-based by looking at the
     * TSDL magic number. If it is packet-based, it also gives information about
     * the endianness of the trace using the detectedByteOrder attribute.
     *
     * @param metadataFileChannel
     *            FileChannel of the metadata file.
     * @return True if the metadata is packet-based.
     * @throws CTFReaderException
     */
    private boolean isPacketBased(FileChannel metadataFileChannel)
            throws CTFReaderException {
        /*
         * Create a ByteBuffer to read the TSDL magic number (default is big
         * endian)
         */
        ByteBuffer magicByteBuffer = ByteBuffer.allocate(Utils.TSDL_MAGIC_LEN);

        /* Read without changing file position */
        try {
            metadataFileChannel.read(magicByteBuffer, 0);
        } catch (IOException e) {
            throw new CTFReaderException("Unable to read metadata file channel.", e); //$NON-NLS-1$
        }

        /* Get the first int from the file */
        int magic = magicByteBuffer.getInt(0);

        /* Check if it matches */
        if (Utils.TSDL_MAGIC == magic) {
            detectedByteOrder = ByteOrder.BIG_ENDIAN;
            return true;
        }

        /* Try the same thing, but with little endian */
        magicByteBuffer.order(ByteOrder.LITTLE_ENDIAN);
        magic = magicByteBuffer.getInt(0);

        if (Utils.TSDL_MAGIC == magic) {
            detectedByteOrder = ByteOrder.LITTLE_ENDIAN;
            return true;
        }

        return false;
    }

    /**
     * Reads a metadata packet from the given metadata FileChannel, do some
     * basic validation and append the text to the StringBuffer.
     *
     * @param metadataFileChannel
     *            Metadata FileChannel
     * @param metadataText
     *            StringBuffer to which the metadata text will be appended.
     * @return A structure describing the header of the metadata packet, or null
     *         if the end of the file is reached.
     * @throws CTFReaderException
     */
    private MetadataPacketHeader readMetadataPacket(
            FileChannel metadataFileChannel, StringBuffer metadataText)
            throws CTFReaderException {
        /* Allocate a ByteBuffer for the header */
        ByteBuffer headerByteBuffer = ByteBuffer.allocate(METADATA_PACKET_HEADER_SIZE);

        /* Read the header */
        int nbBytesRead;
        try {
            nbBytesRead = metadataFileChannel.read(headerByteBuffer);
        } catch (IOException e) {
            throw new CTFReaderException("Error reading the metadata header.", e); //$NON-NLS-1$
        }

        /* Return null if EOF */
        if (nbBytesRead < 0) {
            return null;
        }

        /* Set ByteBuffer's position to 0 */
        headerByteBuffer.position(0);

        /* Use byte order that was detected with the magic number */
        headerByteBuffer.order(detectedByteOrder);

        assert (nbBytesRead == METADATA_PACKET_HEADER_SIZE);

        MetadataPacketHeader header = new MetadataPacketHeader();

        /* Read from the ByteBuffer */
        header.magic = headerByteBuffer.getInt();
        headerByteBuffer.get(header.uuid);
        header.checksum = headerByteBuffer.getInt();
        header.contentSize = headerByteBuffer.getInt();
        header.packetSize = headerByteBuffer.getInt();
        header.compressionScheme = headerByteBuffer.get();
        header.encryptionScheme = headerByteBuffer.get();
        header.checksumScheme = headerByteBuffer.get();
        header.ctfMajorVersion = headerByteBuffer.get();
        header.ctfMinorVersion = headerByteBuffer.get();

        /* Check TSDL magic number */
        if (header.magic != Utils.TSDL_MAGIC) {
            throw new CTFReaderException("TSDL magic number does not match"); //$NON-NLS-1$
        }

        /* Check UUID */
        UUID uuid = Utils.makeUUID(header.uuid);
        if (!trace.uuidIsSet()) {
            trace.setUUID(uuid);
        } else {
            if (!trace.getUUID().equals(uuid)) {
                throw new CTFReaderException("UUID mismatch"); //$NON-NLS-1$
            }
        }

        /* Extract the text from the packet */
        int payloadSize = ((header.contentSize / 8) - METADATA_PACKET_HEADER_SIZE);
        if (payloadSize < 0) {
            throw new CTFReaderException("Invalid metadata packet payload size."); //$NON-NLS-1$
        }
        int skipSize = (header.packetSize - header.contentSize) / 8;

        /* Read the payload + the padding in a ByteBuffer */
        ByteBuffer payloadByteBuffer = ByteBuffer.allocateDirect(payloadSize
                + skipSize);
        try {
            metadataFileChannel.read(payloadByteBuffer);
        } catch (IOException e) {
            throw new CTFReaderException("Error reading metadata packet payload.", e); //$NON-NLS-1$
        }
        payloadByteBuffer.rewind();

        /* Read only the payload from the ByteBuffer into a byte array */
        byte payloadByteArray[] = new byte[payloadByteBuffer.remaining()];
        payloadByteBuffer.get(payloadByteArray, 0, payloadSize);

        /* Convert the byte array to a String */
        String str = new String(payloadByteArray, 0, payloadSize);

        /* Append it to the existing metadata */
        metadataText.append(str);

        return header;
    }

    private static class MetadataPacketHeader {

        public int magic;
        public byte uuid[] = new byte[16];
        public int checksum;
        public int contentSize;
        public int packetSize;
        public byte compressionScheme;
        public byte encryptionScheme;
        public byte checksumScheme;
        public byte ctfMajorVersion;
        public byte ctfMinorVersion;

        @Override
        public String toString() {
            /* Only for debugging, shouldn't be externalized */
            /* Therefore it cannot be covered by test cases */
            return "MetadataPacketHeader [magic=0x" //$NON-NLS-1$
                    + Integer.toHexString(magic) + ", uuid=" //$NON-NLS-1$
                    + Arrays.toString(uuid) + ", checksum=" + checksum //$NON-NLS-1$
                    + ", contentSize=" + contentSize + ", packetSize=" //$NON-NLS-1$ //$NON-NLS-2$
                    + packetSize + ", compressionScheme=" + compressionScheme //$NON-NLS-1$
                    + ", encryptionScheme=" + encryptionScheme //$NON-NLS-1$
                    + ", checksumScheme=" + checksumScheme //$NON-NLS-1$
                    + ", ctfMajorVersion=" + ctfMajorVersion //$NON-NLS-1$
                    + ", ctfMinorVersion=" + ctfMinorVersion + ']'; //$NON-NLS-1$
        }

    }
}
