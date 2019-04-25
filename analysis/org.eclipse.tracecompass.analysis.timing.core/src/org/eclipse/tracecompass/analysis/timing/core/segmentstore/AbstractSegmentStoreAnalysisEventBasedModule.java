/*******************************************************************************
 * Copyright (c) 2016 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.eclipse.tracecompass.analysis.timing.core.segmentstore;

import static org.eclipse.tracecompass.common.core.NonNullUtils.checkNotNull;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.tracecompass.segmentstore.core.ISegment;
import org.eclipse.tracecompass.segmentstore.core.ISegmentStore;
import org.eclipse.tracecompass.tmf.core.event.ITmfEvent;
import org.eclipse.tracecompass.tmf.core.exceptions.TmfAnalysisException;
import org.eclipse.tracecompass.tmf.core.request.ITmfEventRequest;
import org.eclipse.tracecompass.tmf.core.request.TmfEventRequest;
import org.eclipse.tracecompass.tmf.core.timestamp.TmfTimeRange;
import org.eclipse.tracecompass.tmf.core.trace.ITmfTrace;

/**
 * Abstract class to create an event base segment store analysis. It uses an
 * event request to define how events will generate segments.
 *
 * @author Jean-Christian Kouame
 *
 */
public abstract class AbstractSegmentStoreAnalysisEventBasedModule extends AbstractSegmentStoreAnalysisModule {

    private @Nullable ITmfEventRequest fOngoingRequest = null;

    /**
     * Returns the analysis request for creating the segment store
     *
     * @param segmentStore
     *            a segment store to fill
     * @param monitor
     *            The progress monitor to use for the request
     * @return the segment store analysis request implementation
     * @since 4.0
     */
    protected abstract AbstractSegmentStoreAnalysisRequest createAnalysisRequest(ISegmentStore<ISegment> segmentStore, IProgressMonitor monitor);

    @Override
    protected void canceling() {
        ITmfEventRequest req = fOngoingRequest;
        if ((req != null) && (!req.isCompleted())) {
            req.cancel();
        }
    }

    @Override
    protected boolean buildAnalysisSegments(ISegmentStore<ISegment> segmentStore, IProgressMonitor monitor) throws TmfAnalysisException {
        ITmfTrace trace = checkNotNull(getTrace());
        /* Cancel an ongoing request */
        ITmfEventRequest req = fOngoingRequest;
        if ((req != null) && (!req.isCompleted())) {
            req.cancel();
        }

        /* Create a new request */
        req = createAnalysisRequest(segmentStore, monitor);
        fOngoingRequest = req;
        trace.sendRequest(req);

        try {
            req.waitForCompletion();
        } catch (InterruptedException e) {
        }

        /* Do not process the results if the request was cancelled */
        return !(req.isCancelled() || req.isFailed());
    }

    /**
     * Abstract event request to fill a a segment store
     */
    protected abstract class AbstractSegmentStoreAnalysisRequest extends TmfEventRequest {

        private final ISegmentStore<ISegment> fSegmentStore;

        /**
         * Constructor
         *
         * @param segmentStore
         *            a segment store to fill
         */
        public AbstractSegmentStoreAnalysisRequest(ISegmentStore<ISegment> segmentStore) {
            super(ITmfEvent.class, TmfTimeRange.ETERNITY, 0, ITmfEventRequest.ALL_DATA, ExecutionType.BACKGROUND, AbstractSegmentStoreAnalysisEventBasedModule.this.getDependencyLevel());
            /*
             * We do NOT make a copy here! We want to modify the list that was
             * passed in parameter.
             */
            fSegmentStore = segmentStore;
        }

        /**
         * Returns the segment store
         *
         * @return the segment store
         */
        public ISegmentStore<ISegment> getSegmentStore() {
            return fSegmentStore;
        }

        @Override
        public void handleSuccess() {
            super.handleSuccess();
            closeSegmentStore(false);
        }

        @Override
        public void handleFailure() {
            super.handleFailure();
            closeSegmentStore(true);
        }

        @Override
        public void handleCancel() {
            super.handleCancel();
            closeSegmentStore(true);
        }

        private void closeSegmentStore(boolean deleteFiles) {
            fSegmentStore.close(deleteFiles);
        }

    }

}
