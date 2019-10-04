/*******************************************************************************
 * Copyright (c) 2013, 2017 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Marc-Andre Laperle - Initial API and implementation
 *******************************************************************************/

package org.eclipse.tracecompass.internal.tmf.core.trace.indexer;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.Files;
import java.text.MessageFormat;

import org.eclipse.tracecompass.internal.tmf.core.Activator;
import org.eclipse.tracecompass.internal.tmf.core.TmfCoreTracer;
import org.eclipse.tracecompass.tmf.core.timestamp.TmfTimeRange;
import org.eclipse.tracecompass.tmf.core.timestamp.TmfTimestamp;
import org.eclipse.tracecompass.tmf.core.trace.indexer.ITmfPersistentlyIndexable;

/**
 * Common implementation of file-based checkpoint collection
 *
 * @author Marc-Andre Laperle
 */
public abstract class AbstractFileCheckpointCollection implements ICheckpointCollection {

    private static final int INVALID_VERSION = -1;
    private static final int VERSION = 3;
    private static final int SUB_VERSION_NONE = -1;

    /**
     * The base file header, can be extended
     */
    protected class CheckpointCollectionFileHeader {
        private static final int SIZE = INT_SIZE +
                INT_SIZE +
                LONG_SIZE +
                LONG_SIZE +
                MAX_TIME_RANGE_SERIALIZE_SIZE;

        /**
         * Get the size of the header in bytes. This should be overridden if the
         * header is augmented with more data
         *
         * @return the size of the header in bytes
         */
        public int getSize() {
            return SIZE;
        }

        /**
         * Get the sub version of this header
         *
         * @return the sub version
         */
        public int getSubVersion() {
            return SUB_VERSION_NONE;
        }

        /**
         * Constructs a new file header for an existing file
         *
         * @param randomAccessFile
         *            the existing file
         * @throws IOException
         *             if an I/O error occurs reading from the file
         */
        public CheckpointCollectionFileHeader(RandomAccessFile randomAccessFile) throws IOException {
            fVersion = randomAccessFile.readInt();
            fSize = randomAccessFile.readInt();
            fNbEvents = randomAccessFile.readLong();
            ByteBuffer b = ByteBuffer.allocate(MAX_TIME_RANGE_SERIALIZE_SIZE);
            b.clear();
            fFileChannel.read(b);
            b.flip();
            fTimeRange = new TmfTimeRange(TmfTimestamp.create(b), TmfTimestamp.create(b));
        }

        /**
         * Constructs a new file header for the given version
         *
         * @param version
         *            the version
         */
        public CheckpointCollectionFileHeader(int version) {
            fVersion = version;
        }

        /**
         * Serialize the header to a file
         *
         * @param randomAccessFile
         *            the existing file
         * @throws IOException
         *             if an I/O error occurs writing to the file
         */
        public void serialize(RandomAccessFile randomAccessFile) throws IOException {
            randomAccessFile.seek(0);
            // Version is written at the very last on dispose
            randomAccessFile.writeInt(INVALID_VERSION);
            randomAccessFile.writeInt(fSize);
            randomAccessFile.writeLong(fNbEvents);

            ByteBuffer b = ByteBuffer.allocate(MAX_TIME_RANGE_SERIALIZE_SIZE);
            b.clear();
            TmfTimestamp.serialize(b, fTimeRange.getStartTime());
            TmfTimestamp.serialize(b, fTimeRange.getEndTime());
            b.rewind();
            fFileChannel.write(b);
        }

        /**
         * The version of the collection. Should be incremented if a binary
         * incompatible change occurs.
         */
        protected final int fVersion;
        /**
         * The size of the collection expressed in a number of checkpoints.
         */
        protected int fSize = 0;
        /**
         * The total number of events in the trace
         */
        protected long fNbEvents;

        /**
         * The time range of the trace.
         */
        protected TmfTimeRange fTimeRange = new TmfTimeRange(TmfTimestamp.ZERO, TmfTimestamp.ZERO);
    }

    /**
     * The size of an int in bytes
     */
    protected static final int INT_SIZE = 4;
    /**
     * The size of a long in bytes
     */
    protected static final int LONG_SIZE = 8;

    /**
     * The maximum size of the serialize buffer when writing the time range
     */
    protected static final int MAX_TIME_RANGE_SERIALIZE_SIZE = 128;

    /**
     * The originating trace
     */
    private ITmfPersistentlyIndexable fTrace;

    private long fCacheMisses = 0;
    private boolean fCreatedFromScratch;
    private boolean fIsDirty = false;

    /**
     * File handle for the file being read/written
     */
    private RandomAccessFile fRandomAccessFile;
    /**
     * File handle for the file being read/written
     */
    private File fFile;

    /**
     * The base file header
     */
    private final CheckpointCollectionFileHeader fHeader;

    // Cached values
    private FileChannel fFileChannel;

    /**
     * Constructs a checkpoint collection for a given trace from scratch or from
     * an existing file. When the checkpoint collection is created from scratch,
     * it is populated by subsequent calls to {@link #insert}.
     *
     * @param file
     *            the file to use as the persistent storage
     * @param trace
     *            the trace
     */
    public AbstractFileCheckpointCollection(File file, ITmfPersistentlyIndexable trace) {
        fTrace = trace;
        fFile = file;
        setCreatedFromScratch(!fFile.exists());

        CheckpointCollectionFileHeader header = null;

        if (!isCreatedFromScratch()) {
            header = tryRestore();
            if (header == null) {
                delete();
            }
        }

        /* delete above may have reset the value */
        if (isCreatedFromScratch()) {
            fIsDirty = true;
            header = initialize();
        }

        fHeader = header;
    }

    /**
     * Creates a new basic file header with the version field initialized. This
     * should be overridden if the file header is extended
     *
     * @return the created file header
     */
    protected CheckpointCollectionFileHeader createHeader() {
        return new CheckpointCollectionFileHeader(VERSION);
    }

    /**
     * Creates a new basic file header for an existing file. This should be
     * overridden if the file header is extended
     *
     * @param randomAccessFile
     *            the existing file
     * @return the created file header
     * @throws IOException
     *             if an I/O error occurs reading from the file
     */
    protected CheckpointCollectionFileHeader createHeader(RandomAccessFile randomAccessFile) throws IOException {
        return new CheckpointCollectionFileHeader(randomAccessFile);
    }

    /**
     * Get the version of the collection.
     *
     * @return the version of the collection.
     */
    protected int getVersion() {
        return VERSION;
    }

    /**
     * Get the sub version of the collection.
     *
     * @return the sub version of the collection.
     */
    protected int getSubVersion() {
        return SUB_VERSION_NONE;
    }

    private CheckpointCollectionFileHeader initialize() {
        CheckpointCollectionFileHeader header = null;
        try {
            fRandomAccessFile = new RandomAccessFile(fFile, "rw"); //$NON-NLS-1$
            fFileChannel = fRandomAccessFile.getChannel();
            header = createHeader();

            // Reserve space for header
            fRandomAccessFile.setLength(header.getSize());
            TmfCoreTracer.traceIndexer(CheckpointCollectionFileHeader.class.getSimpleName() + " initialize " + "nbEvents: " + header.fNbEvents + " fTimeRange: " + header.fTimeRange); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
        } catch (IOException e) {
            Activator.logError(MessageFormat.format(Messages.ErrorOpeningIndex, fFile), e);
            return null;
        }

        return header;
    }

    /**
     * Try to restore the index from disk. Try to open the file and check the
     * version. Returns the loaded header or null if it could not be loaded.
     *
     * @return the loaded header or null if it could not be loaded.
     */
    private CheckpointCollectionFileHeader tryRestore() {
        CheckpointCollectionFileHeader header = null;

        try {
            fRandomAccessFile = new RandomAccessFile(fFile, "rw"); //$NON-NLS-1$
            fFileChannel = fRandomAccessFile.getChannel();
        } catch (FileNotFoundException e) {
            Activator.logError(MessageFormat.format(Messages.ErrorOpeningIndex, fFile), e);
            return null;
        }

        try {
            header = createHeader(fRandomAccessFile);
            if (header.fVersion != VERSION || header.getSubVersion() != getSubVersion()) {
                return null;
            }
            TmfCoreTracer.traceIndexer(CheckpointCollectionFileHeader.class.getSimpleName() + " read " + fFile + " nbEvents: " + header.fNbEvents + " fTimeRange: " + header.fTimeRange); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
        } catch (IOException e) {
            Activator.logError(MessageFormat.format(Messages.IOErrorReadingHeader, fFile), e);
            return null;
        }

        return header;
    }

    /**
     * Marks the index as dirty. Writes an invalid file header, and sets the dirty
     * flag which will cause the new file header to be serialized when the index is
     * disposed.
     */
    protected void markDirty() {
        if (fIsDirty) {
            return;
        }
        try {
            // Write an invalid version until the very last moment when dispose
            // the index. This is how we know if it's corrupted or not.
            fRandomAccessFile.seek(0);
            fRandomAccessFile.writeInt(INVALID_VERSION);
        } catch (IOException e) {
            Activator.logError(MessageFormat.format(Messages.IOErrorWritingHeader, fFile), e);
        }
        fIsDirty = true;
    }

    /**
     *
     * @return true if the checkpoint collection was created from scratch, false
     *         otherwise
     */
    @Override
    public boolean isCreatedFromScratch() {
        return fCreatedFromScratch;
    }

    /**
     * Set whether or not the collection is created from scratch
     *
     * @param isCreatedFromScratch
     *            whether or not the collection is created from scratch
     */
    protected void setCreatedFromScratch(boolean isCreatedFromScratch) {
        fCreatedFromScratch = isCreatedFromScratch;
    }

    /**
     * @return the number of cache misses.
     */
    public long getCacheMisses() {
        return fCacheMisses;
    }

    /**
     * Increment the number of cache misses.
     */
    protected void incCacheMisses() {
        ++fCacheMisses;
    }

    /**
     * Returns the size of the checkpoint collection expressed as a number of
     * checkpoints.
     *
     * @return the size of the checkpoint collection
     */
    @Override
    public int size() {
        return fHeader.fSize;
    }

    /**
     * Set the trace time range
     *
     * @param timeRange
     *            the trace time range
     */
    @Override
    public void setTimeRange(TmfTimeRange timeRange) {
        if (!fHeader.fTimeRange.equals(timeRange)) {
            markDirty();
        }
        fHeader.fTimeRange = timeRange;
    }

    /**
     * Get the trace time range
     *
     * @return the trace time range
     */
    @Override
    public TmfTimeRange getTimeRange() {
        return fHeader.fTimeRange;
    }

    /**
     * Set the number of events in the trace
     *
     * @param nbEvents
     *            the number of events in the trace
     */
    @Override
    public void setNbEvents(long nbEvents) {
        if (fHeader.fNbEvents != nbEvents) {
            markDirty();
        }
        fHeader.fNbEvents = nbEvents;
    }

    /**
     * Get the number of events in the trace
     *
     * @return the number of events in the trace
     */
    @Override
    public long getNbEvents() {
        return fHeader.fNbEvents;
    }

    /**
     * Get the trace
     *
     * @return the trace
     */
    protected ITmfPersistentlyIndexable getTrace() {
        return fTrace;
    }

    /**
     * Get the random access file currently opened
     *
     * @return the file
     */
    protected RandomAccessFile getRandomAccessFile() {
        return fRandomAccessFile;
    }

    /**
     * Get the file channel currently used for the index
     *
     * @return the file channel
     */
    protected FileChannel getFileChannel() {
        return fRandomAccessFile.getChannel();
    }

    /**
     * Get the file handle for the index
     *
     * @return the file
     */
    protected File getFile() {
        return fFile;
    }

    /**
     * Get the header for this collection
     *
     * @return the header
     */
    public CheckpointCollectionFileHeader getHeader() {
        return fHeader;
    }

    /**
     * Dispose and delete the checkpoint collection
     */
    @Override
    public void delete() {
        dispose(true);
        File file = fFile;
        if (file.exists()) {
            try {
                Files.delete(file.toPath());
            } catch (IOException e) {
                Activator.logError(e.getMessage(), e);
            }
        }
    }

    /**
     * Dispose the collection and its resources
     */
    @Override
    public void dispose() {
        dispose(false);
    }

    private void dispose(boolean deleting) {
        try {
            if (fRandomAccessFile != null) {
                if (!deleting && fIsDirty) {
                    if (fHeader != null) {
                        fHeader.serialize(fRandomAccessFile);
                    }

                    fRandomAccessFile.seek(0);
                    fRandomAccessFile.writeInt(getVersion());
                    fIsDirty = false;
                }

                fRandomAccessFile.close();
            }
            setCreatedFromScratch(true);
            fRandomAccessFile = null;
            String headerTrace = fHeader == null ? "No header" : "nbEvents: " + fHeader.fNbEvents + " timerange:" + fHeader.fTimeRange; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
            TmfCoreTracer.traceIndexer(this.getClass().getSimpleName() + " disposed. " + headerTrace); //$NON-NLS-1$
        } catch (IOException e) {
            Activator.logError(MessageFormat.format(Messages.IOErrorClosingIndex, fFile), e);
        }
    }
}
