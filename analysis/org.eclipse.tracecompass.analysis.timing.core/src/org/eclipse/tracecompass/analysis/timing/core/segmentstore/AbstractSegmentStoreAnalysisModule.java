/*******************************************************************************
 * Copyright (c) 2015, 2016 Ericsson
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.eclipse.tracecompass.analysis.timing.core.segmentstore;

import static org.eclipse.tracecompass.common.core.NonNullUtils.checkNotNull;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.attribute.FileTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.ListenerList;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.tracecompass.common.core.NonNullUtils;
import org.eclipse.tracecompass.datastore.core.interval.IHTIntervalReader;
import org.eclipse.tracecompass.internal.analysis.timing.core.Activator;
import org.eclipse.tracecompass.segmentstore.core.ISegment;
import org.eclipse.tracecompass.segmentstore.core.ISegmentStore;
import org.eclipse.tracecompass.segmentstore.core.SegmentStoreFactory;
import org.eclipse.tracecompass.segmentstore.core.SegmentStoreFactory.SegmentStoreType;
import org.eclipse.tracecompass.tmf.core.analysis.TmfAbstractAnalysisModule;
import org.eclipse.tracecompass.tmf.core.exceptions.TmfAnalysisException;
import org.eclipse.tracecompass.tmf.core.segment.ISegmentAspect;
import org.eclipse.tracecompass.tmf.core.trace.ITmfTrace;
import org.eclipse.tracecompass.tmf.core.trace.TmfTraceManager;

/**
 * Abstract analysis module to generate a segment store. It is a base class that
 * can be used as a shortcut by analysis who just need to build a single segment
 * store.
 *
 * @author Bernd Hufmann
 * @since 2.0
 *
 */
public abstract class AbstractSegmentStoreAnalysisModule extends TmfAbstractAnalysisModule implements ISegmentStoreProvider {

    private static final String EXTENSION = ".ss"; //$NON-NLS-1$
    /**
     * {@link ListenerList}s are typed since 4.6 (Neon), type these when support for
     * 4.5 (Mars) is no longer required.
     */
    private final ListenerList fListeners = new ListenerList(ListenerList.IDENTITY);

    private @Nullable ISegmentStore<ISegment> fSegmentStore;

    @Override
    public void addListener(IAnalysisProgressListener listener) {
        fListeners.add(listener);
    }

    @Override
    public void removeListener(IAnalysisProgressListener listener) {
        fListeners.remove(listener);
    }

    /**
     * Returns all the listeners
     *
     * @return latency listeners
     */
    protected Iterable<IAnalysisProgressListener> getListeners() {
        List<IAnalysisProgressListener> listeners = new ArrayList<>();
        for (Object listener : fListeners.getListeners()) {
            if (listener != null) {
                listeners.add((IAnalysisProgressListener) listener);
            }
        }
        return listeners;
    }

    @Override
    public Iterable<ISegmentAspect> getSegmentAspects() {
        return Collections.emptyList();
    }

    /**
     * Returns the file name for storing segment store
     *
     * @return segment store file name
     */
    protected String getDataFileName() {
        return getId() + EXTENSION;
    }

    /**
     * Read an object from the ObjectInputStream.
     *
     * @param ois
     *            the ObjectInputStream to used
     * @return the read object
     * @throws ClassNotFoundException
     *             - Class of a serialized object cannot be found.
     * @throws IOException
     *             - Any of the usual Input/Output related exceptions.
     * @deprecated The segment store analysis modules are either on disk or all
     *             in memory, no in between anymore
     */
    @Deprecated
    protected Object[] readObject(ObjectInputStream ois) throws ClassNotFoundException, IOException {
        return checkNotNull((Object[]) ois.readObject());
    }

    /**
     * Fills the segment store. This is the main method that children classes
     * need to implement to build the segment store. For example, if the
     * segments are found by parsing the events of a trace, the event request
     * would be done in this method.
     *
     * Note: After this method, the segment store should be completed, so it
     * should also close the segment store at the end of the analysis
     *
     * @param segmentStore
     *            The segment store to fill
     * @param monitor
     *            Progress monitor
     * @return Whether the segments was resolved successfully or not
     * @throws TmfAnalysisException
     *             Method may throw an analysis exception
     */
    protected abstract boolean buildAnalysisSegments(ISegmentStore<ISegment> segmentStore, IProgressMonitor monitor) throws TmfAnalysisException;

    /**
     * Get the reader for the segments on disk. If the segment store is not on
     * disk, this method can return null.
     *
     * @return The segment reader
     * @since 3.0
     */
    protected IHTIntervalReader<ISegment> getSegmentReader() {
        throw new UnsupportedOperationException("getSegmentReader: This method should be overriden in classes that saves the segment store on disk"); //$NON-NLS-1$
    }

    /**
     * Get the type of segment store to build. By default it is
     * {@link SegmentStoreType#Fast}
     *
     * @return The type of segment store to build
     * @since 3.0
     */
    protected SegmentStoreType getSegmentStoreType() {
        return SegmentStoreType.Fast;
    }

    @Override
    public @Nullable ISegmentStore<ISegment> getSegmentStore() {
        return fSegmentStore;
    }

    @Override
    public void dispose() {
        super.dispose();
        ISegmentStore<ISegment> store = fSegmentStore;
        if (store != null) {
            store.dispose();
        }
    }

    @Override
    protected boolean executeAnalysis(IProgressMonitor monitor) throws TmfAnalysisException {
        SegmentStoreType type = getSegmentStoreType();
        ISegmentStore<ISegment> store = null;
        switch (type) {
        case Distinct:
            // Fall-through
        case Fast:
            // Fall-through
        case Stable:
            store = buildInMemorySegmentStore(type, monitor);
            break;
        case OnDisk:
            final @Nullable String dataFileName = getDataFileName();
            store = buildOnDiskSegmentStore(dataFileName, monitor);
            break;
        default:
            Activator.getInstance().logError("Unknown segment store type: " + type); //$NON-NLS-1$
            break;
        }

        if (store == null) {
            return false;
        }

        fSegmentStore = store;
        sendUpdate(store);
        return true;
    }

    private @Nullable ISegmentStore<@NonNull ISegment> buildOnDiskSegmentStore(@Nullable String dataFileName, IProgressMonitor monitor) throws TmfAnalysisException {
        ITmfTrace trace = checkNotNull(getTrace());

        String fileName = dataFileName;
        if (fileName == null) {
            fileName = getId() + ".ss"; //$NON-NLS-1$
        }
        /* See if the data file already exists on disk */
        String dir = TmfTraceManager.getSupplementaryFileDir(trace);
        final Path file = Paths.get(dir, fileName);

        boolean built = false;
        ISegmentStore<ISegment> segmentStore;
        try {
            // Compare the file creation time to determine if this analysis is
            // built from scratch or not
            FileTime origCreationTime = (Files.exists(file) ? NonNullUtils.checkNotNull(Files.readAttributes(file, BasicFileAttributes.class)).creationTime() : FileTime.fromMillis(0));
            segmentStore = SegmentStoreFactory.createOnDiskSegmentStore(file, getSegmentReader());
            FileTime creationTime = NonNullUtils.checkNotNull(Files.readAttributes(file, BasicFileAttributes.class)).creationTime();
            built = origCreationTime.equals(creationTime);
        } catch (IOException e) {
            try {
                Files.deleteIfExists(file);
            } catch (IOException e1) {
                // Ignore
            }
            Activator.getInstance().logError("Error creating segment store", e); //$NON-NLS-1$
            return null;
        }

        if (built) {
            return segmentStore;
        }
        boolean completed = buildAnalysisSegments(segmentStore, monitor);
        if (!completed) {
            return null;
        }

        return segmentStore;
    }

    private @Nullable ISegmentStore<@NonNull ISegment> buildInMemorySegmentStore(SegmentStoreType type, IProgressMonitor monitor) throws TmfAnalysisException {
        ISegmentStore<ISegment> segmentStore = SegmentStoreFactory.createSegmentStore(type);
        boolean completed = buildAnalysisSegments(segmentStore, monitor);
        if (!completed) {
            return null;
        }

        return segmentStore;
    }

    /**
     * Send the segment store to all its listener
     *
     * @param store
     *            The segment store to broadcast
     */
    protected void sendUpdate(final ISegmentStore<ISegment> store) {
        for (IAnalysisProgressListener listener : getListeners()) {
            listener.onComplete(this, store);
        }
    }
}