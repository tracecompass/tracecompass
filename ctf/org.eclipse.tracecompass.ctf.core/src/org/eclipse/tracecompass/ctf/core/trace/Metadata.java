/*******************************************************************************
 * Copyright (c) 2011, 2014 Ericsson, Ecole Polytechnique de Montreal and others
 *
 * All rights reserved. This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Matthew Khouzam - Initial API and implementation
 *     Simon Marchi - Initial API and implementation
 *     Matthew Khouzam - Update for live trace reading support
 *     Bernd Hufmann - Add method to copy metadata file
 *******************************************************************************/

package org.eclipse.tracecompass.ctf.core.trace;

import java.io.BufferedReader;
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
import java.nio.charset.Charset;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.UUID;

import org.antlr.runtime.ANTLRReaderStream;
import org.antlr.runtime.CommonTokenStream;
import org.antlr.runtime.RecognitionException;
import org.antlr.runtime.tree.CommonTree;
import org.antlr.runtime.tree.RewriteCardinalityException;
import org.eclipse.tracecompass.common.core.NonNullUtils;
import org.eclipse.tracecompass.ctf.core.CTFException;
import org.eclipse.tracecompass.ctf.parser.CTFLexer;
import org.eclipse.tracecompass.ctf.parser.CTFParser;
import org.eclipse.tracecompass.ctf.parser.CTFParser.parse_return;
import org.eclipse.tracecompass.internal.ctf.core.event.metadata.CtfAntlrException;
import org.eclipse.tracecompass.internal.ctf.core.event.metadata.IOStructGen;
import org.eclipse.tracecompass.internal.ctf.core.event.metadata.ParseException;
import org.eclipse.tracecompass.internal.ctf.core.trace.Utils;

/**
 * The CTF trace metadata TSDL file
 *
 * @version 1.0
 * @author Matthew Khouzam
 * @author Simon Marchi
 */
public class Metadata {

    // ------------------------------------------------------------------------
    // Constants
    // ------------------------------------------------------------------------

    private static final Charset ASCII_CHARSET = Charset.forName("ASCII"); //$NON-NLS-1$

    private static final String TEXT_ONLY_METADATA_HEADER_PREFIX = "/* CTF"; //$NON-NLS-1$

    private static final int PREVALIDATION_SIZE = 8;

    private static final int BITS_PER_BYTE = Byte.SIZE;

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
     * Byte order as detected when reading the TSDL magic number.
     */
    private ByteOrder fDetectedByteOrder = null;

    /**
     * The trace file to which belongs this metadata file.
     */
    private final CTFTrace fTrace;

    private IOStructGen fTreeParser;

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
        fTrace = trace;
    }

    /**
     * For network streaming
     */
    public Metadata() {
        fTrace = new CTFTrace();
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
        return fDetectedByteOrder;
    }

    /**
     * Gets the parent trace
     *
     * @return the parent trace
     */
    public CTFTrace getTrace() {
        return fTrace;
    }

    // ------------------------------------------------------------------------
    // Operations
    // ------------------------------------------------------------------------

    /**
     * Parse the metadata file.
     *
     * @throws CTFException
     *             If there was a problem parsing the metadata
     */
    public void parseFile() throws CTFException {

        /*
         * Reader. It will contain a StringReader if we are using packet-based metadata
         * and it will contain a FileReader if we have text-based metadata.
         */

        File metadataFile = new File(getMetadataPath());
        ByteOrder byteOrder = CTFTrace.startsWithMagicNumber(metadataFile, Utils.TSDL_MAGIC);
        fDetectedByteOrder = byteOrder;
        try (FileInputStream fis = new FileInputStream(metadataFile);
                FileChannel metadataFileChannel = fis.getChannel();
                /* Check if metadata is packet-based, if not it is text based */
                Reader metadataTextInput = ( byteOrder!= null ? readBinaryMetaData(metadataFileChannel) : new FileReader(metadataFile));) {

            readMetaDataText(metadataTextInput);

        } catch (FileNotFoundException e) {
            throw new CTFException("Cannot find metadata file!", e); //$NON-NLS-1$
        } catch (IOException | ParseException e) {
            throw new CTFException(e);
        } catch (RecognitionException e) {
            throw new CtfAntlrException(e);
        } catch (RewriteCardinalityException e) { /* needs to be separate to avoid casting as exception */
            throw new CtfAntlrException(e);
        }

    }

    private Reader readBinaryMetaData(FileChannel metadataFileChannel) throws CTFException {
        /* Create StringBuffer to receive metadata text */
        StringBuffer metadataText = new StringBuffer();

        /*
         * Read metadata packet one by one, appending the text to the StringBuffer
         */
        MetadataPacketHeader packetHeader = readMetadataPacket(
                metadataFileChannel, metadataText);
        while (packetHeader != null) {
            packetHeader = readMetadataPacket(metadataFileChannel,
                    metadataText);
        }

        /* Wrap the metadata string with a StringReader */
        return new StringReader(metadataText.toString());
    }

    /**
     * Executes a weak validation of the metadata. It checks if a file with name
     * metadata exists and if one of the following conditions are met:
     * <ul>
     * <li>For text-only metadata, the file starts with "/* CTF" (without the
     * quotes)</li>
     * <li>For packet-based metadata, the file starts with correct magic number</li>
     * </ul>
     *
     * @param path
     *            path to CTF trace directory
     * @return <code>true</code> if pre-validation is ok else <code>false</code>
     * @throws CTFException
     *             file channel cannot be created
     * @since 1.0
     */
    public static boolean preValidate(String path) throws CTFException {
        String metadataPath = path + Utils.SEPARATOR + METADATA_FILENAME;
        File metadataFile = new File(metadataPath);
        if (metadataFile.exists() && metadataFile.length() > PREVALIDATION_SIZE) {
            if (CTFTrace.startsWithMagicNumber(metadataFile, Utils.TSDL_MAGIC) != null) {
                return true;
            }
            try (BufferedReader br = new BufferedReader(new FileReader(metadataFile))) {
                String text = br.readLine();
                return text.startsWith(TEXT_ONLY_METADATA_HEADER_PREFIX);
            } catch (IOException e) {
                throw new CTFException(e.getMessage(), e);
            }
        }
        return false;
    }

    /**
     * Read the metadata from a formatted TSDL string
     *
     * @param data
     *            the data to read
     * @throws CTFException
     *             this exception wraps a ParseException, IOException or
     *             CtfAntlrException, three exceptions that can be obtained from
     *             parsing a TSDL file
     */
    public void parseText(String data) throws CTFException {
        Reader metadataTextInput = new StringReader(data);
        try {
            readMetaDataText(metadataTextInput);
        } catch (IOException | ParseException e) {
            throw new CTFException(e);
        } catch (RecognitionException | RewriteCardinalityException e) {
            throw new CtfAntlrException(e);
        }

    }

    private void readMetaDataText(Reader metadataTextInput) throws IOException, RecognitionException, ParseException {
        CommonTree tree = createAST(metadataTextInput);

        /* Generate IO structures (declarations) */
        fTreeParser = new IOStructGen(tree, NonNullUtils.checkNotNull(fTrace));
        fTreeParser.generate();
        /* store locally in case of concurrent modification */
        ByteOrder detectedByteOrder = getDetectedByteOrder();
        if (detectedByteOrder != null && fTrace.getByteOrder() != detectedByteOrder) {
            throw new ParseException("Metadata byte order and trace byte order inconsistent."); //$NON-NLS-1$
        }
    }

    /**
     * Read a metadata fragment from a formatted TSDL string
     *
     * @param dataFragment
     *            the data to read
     * @throws CTFException
     *             this exception wraps a ParseException, IOException or
     *             CtfAntlrException, three exceptions that can be obtained from
     *             parsing a TSDL file
     */
    public void parseTextFragment(String dataFragment) throws CTFException {
        Reader metadataTextInput = new StringReader(dataFragment);
        try {
            readMetaDataTextFragment(metadataTextInput);
        } catch (IOException | ParseException e) {
            throw new CTFException(e);
        } catch (RecognitionException | RewriteCardinalityException e) {
            throw new CtfAntlrException(e);
        }
    }

    private void readMetaDataTextFragment(Reader metadataTextInput) throws IOException, RecognitionException, ParseException {
        CommonTree tree = createAST(metadataTextInput);
        fTreeParser.setTree(tree);
        fTreeParser.generateFragment();
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
        return pr.getTree();
    }

    private String getMetadataPath() {
        /* Path of metadata file = trace directory path + metadata filename */
        if (fTrace.getTraceDirectory() == null) {
            return ""; //$NON-NLS-1$
        }
        return fTrace.getTraceDirectory().getPath()
                + Utils.SEPARATOR + METADATA_FILENAME;
    }

    /**
     * Reads a metadata packet from the given metadata FileChannel, do some basic
     * validation and append the text to the StringBuffer.
     *
     * @param metadataFileChannel
     *            Metadata FileChannel
     * @param metadataText
     *            StringBuffer to which the metadata text will be appended.
     * @return A structure describing the header of the metadata packet, or null if
     *         the end of the file is reached.
     * @throws CTFException
     */
    private MetadataPacketHeader readMetadataPacket(
            FileChannel metadataFileChannel, StringBuffer metadataText)
            throws CTFException {
        /* Allocate a ByteBuffer for the header */
        ByteBuffer headerByteBuffer = ByteBuffer.allocate(METADATA_PACKET_HEADER_SIZE);

        /* Read the header */
        try {
            int nbBytesRead = metadataFileChannel.read(headerByteBuffer);

            /* Return null if EOF */
            if (nbBytesRead < 0) {
                return null;
            }

            if (nbBytesRead != METADATA_PACKET_HEADER_SIZE) {
                throw new CTFException("Error reading the metadata header."); //$NON-NLS-1$
            }

        } catch (IOException e) {
            throw new CTFException("Error reading the metadata header.", e); //$NON-NLS-1$
        }

        /* Set ByteBuffer's position to 0 */
        headerByteBuffer.position(0);

        /* Use byte order that was detected with the magic number */
        headerByteBuffer.order(fDetectedByteOrder);

        MetadataPacketHeader header = new MetadataPacketHeader(headerByteBuffer);

        /* Check TSDL magic number */
        if (!header.isMagicValid()) {
            throw new CTFException("TSDL magic number does not match"); //$NON-NLS-1$
        }

        /* Check UUID */
        if (!fTrace.uuidIsSet()) {
            fTrace.setUUID(header.getUuid());
        } else if (!fTrace.getUUID().equals(header.getUuid())) {
            throw new CTFException("UUID mismatch"); //$NON-NLS-1$
        }

        /* Extract the text from the packet */
        int payloadSize = ((header.getContentSize() / BITS_PER_BYTE) - METADATA_PACKET_HEADER_SIZE);
        if (payloadSize < 0) {
            throw new CTFException("Invalid metadata packet payload size."); //$NON-NLS-1$
        }
        int skipSize = (header.getPacketSize() - header.getContentSize()) / BITS_PER_BYTE;

        /* Read the payload + the padding in a ByteBuffer */
        ByteBuffer payloadByteBuffer = ByteBuffer.allocateDirect(payloadSize
                + skipSize);
        try {
            metadataFileChannel.read(payloadByteBuffer);
        } catch (IOException e) {
            throw new CTFException("Error reading metadata packet payload.", e); //$NON-NLS-1$
        }
        payloadByteBuffer.rewind();

        /* Read only the payload from the ByteBuffer into a byte array */
        byte payloadByteArray[] = new byte[payloadByteBuffer.remaining()];
        payloadByteBuffer.get(payloadByteArray, 0, payloadSize);

        /* Convert the byte array to a String */
        String str = new String(payloadByteArray, 0, payloadSize, ASCII_CHARSET);

        /* Append it to the existing metadata */
        metadataText.append(str);

        return header;
    }

    private static class MetadataPacketHeader {

        private static final int UUID_SIZE = 16;
        private final int fMagic;
        private final UUID fUuid;
        private final int fChecksum;
        private final int fContentSize;
        private final int fPacketSize;
        private final byte fCompressionScheme;
        private final byte fEncryptionScheme;
        private final byte fChecksumScheme;
        private final byte fCtfMajorVersion;
        private final byte fCtfMinorVersion;

        public MetadataPacketHeader(ByteBuffer headerByteBuffer) throws CTFException {
            /* Read from the ByteBuffer */
            fMagic = headerByteBuffer.getInt();
            byte[] uuidBytes = new byte[UUID_SIZE];
            headerByteBuffer.get(uuidBytes);
            fUuid = Utils.makeUUID(uuidBytes);
            fChecksum = headerByteBuffer.getInt();
            fContentSize = headerByteBuffer.getInt();
            fPacketSize = headerByteBuffer.getInt();
            fCompressionScheme = headerByteBuffer.get();
            fEncryptionScheme = headerByteBuffer.get();
            fChecksumScheme = headerByteBuffer.get();
            fCtfMajorVersion = headerByteBuffer.get();
            fCtfMinorVersion = headerByteBuffer.get();
        }

        public boolean isMagicValid() {
            return fMagic == Utils.TSDL_MAGIC;
        }

        public UUID getUuid() {
            return fUuid;
        }

        public int getContentSize() {
            return fContentSize;
        }

        public int getPacketSize() {
            return fPacketSize;
        }

        @Override
        public String toString() {
            /* Only for debugging, shouldn't be externalized */
            /* Therefore it cannot be covered by test cases */
            return "MetadataPacketHeader [magic=0x" //$NON-NLS-1$
                    + Integer.toHexString(fMagic) + ", uuid=" //$NON-NLS-1$
                    + fUuid.toString() + ", checksum=" + fChecksum //$NON-NLS-1$
                    + ", contentSize=" + fContentSize + ", packetSize=" //$NON-NLS-1$ //$NON-NLS-2$
                    + fPacketSize + ", compressionScheme=" + fCompressionScheme //$NON-NLS-1$
                    + ", encryptionScheme=" + fEncryptionScheme //$NON-NLS-1$
                    + ", checksumScheme=" + fChecksumScheme //$NON-NLS-1$
                    + ", ctfMajorVersion=" + fCtfMajorVersion //$NON-NLS-1$
                    + ", ctfMinorVersion=" + fCtfMinorVersion + ']'; //$NON-NLS-1$
        }

    }

    /**
     * Copies the metadata file to a destination directory.
     *
     * @param path
     *            the destination directory
     * @return the path to the target file
     * @throws IOException
     *             if an error occurred
     *
     * @since 1.0
     */
    public Path copyTo(final File path) throws IOException {
        Path source = FileSystems.getDefault().getPath(fTrace.getTraceDirectory().getAbsolutePath(), METADATA_FILENAME);
        Path destPath = FileSystems.getDefault().getPath(path.getAbsolutePath());
        return Files.copy(source, destPath.resolve(source.getFileName()));
    }
}
