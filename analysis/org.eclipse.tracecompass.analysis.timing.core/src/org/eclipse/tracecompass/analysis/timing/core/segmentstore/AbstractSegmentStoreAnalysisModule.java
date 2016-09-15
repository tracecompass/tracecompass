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
import java.io.ObjectOutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.ListenerList;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.tracecompass.common.core.NonNullUtils;
import org.eclipse.tracecompass.segmentstore.core.ISegment;
import org.eclipse.tracecompass.segmentstore.core.ISegmentStore;
import org.eclipse.tracecompass.segmentstore.core.SegmentStoreFactory;
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
     * @return segment store fine name, or null if you don't want a file
     */
    protected @Nullable String getDataFileName() {
        return null;
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
     */
    protected abstract Object[] readObject(ObjectInputStream ois) throws ClassNotFoundException, IOException;

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
        ITmfTrace trace = checkNotNull(getTrace());

        final @Nullable String dataFileName = getDataFileName();
        if (dataFileName != null) {
            /* See if the data file already exists on disk */
            String dir = TmfTraceManager.getSupplementaryFileDir(trace);
            final Path file = Paths.get(dir, dataFileName);

            if (Files.exists(file)) {
                /* Attempt to read the existing file */
                try (ObjectInputStream ois = new ObjectInputStream(Files.newInputStream(file))) {
                    Object[] segmentArray = readObject(ois);
                    ISegmentStore<ISegment> store = SegmentStoreFactory.createSegmentStore(NonNullUtils.checkNotNullContents(segmentArray));
                    fSegmentStore = store;
                    sendUpdate(store);
                    return true;
                } catch (IOException | ClassNotFoundException | ClassCastException e) {
                    /*
                     * We did not manage to read the file successfully, we will
                     * just fall-through to rebuild a new one.
                     */
                    try {
                        Files.delete(file);
                    } catch (IOException e1) {
                    }
                }
            }
        }

        ISegmentStore<ISegment> segmentStore = SegmentStoreFactory.createSegmentStore();
        boolean completed = buildAnalysisSegments(segmentStore, monitor);
        if (!completed) {
            return false;
        }
        fSegmentStore = segmentStore;

        if (dataFileName != null) {
            String dir = TmfTraceManager.getSupplementaryFileDir(trace);
            final Path file = Paths.get(dir, dataFileName);

            /* Serialize the collections to disk for future usage */
            try (ObjectOutputStream oos = new ObjectOutputStream(Files.newOutputStream(file))) {
                oos.writeObject(segmentStore.toArray());
            } catch (IOException e) {
                /*
                 * Didn't work, oh well. We will just re-read the trace next
                 * time
                 */
            }
        }

        sendUpdate(segmentStore);

        return true;
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