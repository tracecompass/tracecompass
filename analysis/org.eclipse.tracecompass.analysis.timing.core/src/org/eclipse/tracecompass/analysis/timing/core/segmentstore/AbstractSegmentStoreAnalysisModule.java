/*******************************************************************************
 * Copyright (c) 2015, 2019 Ericsson
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/
package org.eclipse.tracecompass.analysis.timing.core.segmentstore;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.attribute.FileTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.ListenerList;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.tracecompass.common.core.format.DataSizeWithUnitFormat;
import org.eclipse.tracecompass.datastore.core.interval.IHTIntervalReader;
import org.eclipse.tracecompass.internal.analysis.timing.core.Activator;
import org.eclipse.tracecompass.internal.analysis.timing.core.segmentstore.Messages;
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
    private final ListenerList<IAnalysisProgressListener> fListeners = new ListenerList<>(ListenerList.IDENTITY);

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
     * Segment store analyses should provide a version number. This is relevant
     * for on disk segment store. If the segment structure changes, ie the
     * segments previously saved are not readable anymore by the reader method,
     * then this version number should be incremented. If the version of the
     * segment store on disk does not match that of the reader, then the segment
     * store will be rebuilt.
     *
     * @return The version number of the segment store
     * @since 4.1
     */
    protected int getVersion() {
        return 1;
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
        ITmfTrace trace = Objects.requireNonNull((getTrace()));

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
            FileTime origCreationTime = (Files.exists(file) ? Objects.requireNonNull(Files.readAttributes(file, BasicFileAttributes.class)).creationTime() : FileTime.fromMillis(0));
            segmentStore = SegmentStoreFactory.createOnDiskSegmentStore(file, getSegmentReader(), getVersion());
            FileTime creationTime = Objects.requireNonNull(Files.readAttributes(file, BasicFileAttributes.class)).creationTime();
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

    // ------------------------------------------------------------------------
    // ITmfPropertiesProvider
    // ------------------------------------------------------------------------

    /**
     * @since 2.0
     */
    @Override
    public @NonNull Map<@NonNull String, @NonNull String> getProperties() {
        Map<@NonNull String, @NonNull String> properties = super.getProperties();

        // Add the file size if available
        ITmfTrace trace = getTrace();
        if (trace == null) {
            return properties;
        }

        String fileName = getDataFileName();
        /* See if the data file already exists on disk */
        String dir = TmfTraceManager.getSupplementaryFileDir(trace);
        final Path file = Paths.get(dir, fileName);

        if (Files.exists(file)) {
            try {
                properties.put(Objects.requireNonNull(Messages.SegmentStoreAnalysis_PropertiesFileSize), Objects.requireNonNull(DataSizeWithUnitFormat.getInstance().format(Files.size(file))));
            } catch (IOException e) {
                properties.put(Objects.requireNonNull(Messages.SegmentStoreAnalysis_PropertiesFileSize), Objects.requireNonNull(Messages.SegmentStoreAnalysis_ErrorGettingFileSize));
            }
        } else {
            properties.put(Objects.requireNonNull(Messages.SegmentStoreAnalysis_PropertiesFileSize), Objects.requireNonNull(Messages.SegmentStoreAnalysis_PropertiesAnalysisNotExecuted));
        }

        return properties;
    }

}