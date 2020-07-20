/*******************************************************************************
 * Copyright (c) 2016 École Polytechnique de Montréal
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License 2.0 which
 * accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/

package org.eclipse.tracecompass.internal.analysis.os.linux.core.inputoutput;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.tracecompass.internal.analysis.os.linux.core.Activator;
import org.eclipse.tracecompass.statesystem.core.ITmfStateSystem;
import org.eclipse.tracecompass.statesystem.core.ITmfStateSystemBuilder;
import org.eclipse.tracecompass.statesystem.core.StateSystemBuilderUtils;
import org.eclipse.tracecompass.statesystem.core.exceptions.StateValueTypeException;
import org.eclipse.tracecompass.tmf.core.statesystem.TmfAttributePool;
import org.eclipse.tracecompass.tmf.core.statesystem.TmfAttributePool.QueueType;
import org.eclipse.tracecompass.tmf.core.util.Pair;

/**
 * Class that represents a disk on a system. This class provides operation to
 * save the analysis data in a state system.
 *
 * @author Houssem Daoud
 * @since 2.0
 */
public class DiskWriteModel extends Disk {
    private final List<Pair<Request, Integer>> fDriverQueue = new ArrayList<>();
    private final List<Pair<Request, Integer>> fWaitingQueue = new ArrayList<>();
    private final ITmfStateSystemBuilder fSs;
    private final TmfAttributePool fWaitingQueueAttrib;
    private final TmfAttributePool fDriverQueueAttrib;

    /**
     * Constructor
     *
     * @param dev
     *            The device number of the disk
     * @param ss
     *            The state system this disk will be saved to
     */
   public DiskWriteModel(Integer dev, ITmfStateSystemBuilder ss) {
        super(dev, ss, ss.getQuarkAbsoluteAndAdd(Attributes.DISKS, String.valueOf(dev)));
        fSs = ss;
        int diskQuark = getQuark();
        /* Initialize the state system for this disk */
        int wqQuark = fSs.getQuarkRelativeAndAdd(diskQuark, Attributes.WAITING_QUEUE);
        fWaitingQueueAttrib = new TmfAttributePool(fSs, wqQuark, QueueType.PRIORITY);
        fSs.getQuarkRelativeAndAdd(diskQuark, Attributes.WAITING_QUEUE_LENGTH);
        int dqQuark = fSs.getQuarkRelativeAndAdd(diskQuark, Attributes.DRIVER_QUEUE);
        fDriverQueueAttrib = new TmfAttributePool(fSs, dqQuark, QueueType.PRIORITY);
        fSs.getQuarkRelativeAndAdd(diskQuark, Attributes.DRIVER_QUEUE_LENGTH);
    }

    @Override
    public void setDiskName(String diskname) {
        super.setDiskName(diskname);
        try {
            fSs.modifyAttribute(fSs.getCurrentEndTime(), diskname, getQuark());
        } catch (StateValueTypeException e) {
            Activator.getDefault().logError("Cannot set the diskname for disk " + diskname, e); //$NON-NLS-1$
        }
    }

    /**
     * Return a request from the waiting queue starting at requested base sector
     *
     * @param sector
     *            The sector where the requests starts
     * @return The request corresponding to this sector, or null if no request
     *         available
     */
    public @Nullable Request getWaitingRequest(Long sector) {
        for (Pair<Request, Integer> reqQuark : fWaitingQueue) {
            if (reqQuark.getFirst().getSector().equals(sector)) {
                return reqQuark.getFirst();
            }
        }
        return null;
    }

    /**
     * Removes the request starting at sector from the waiting queue
     *
     * @param ts
     *            The timestamp at which to add this request
     * @param sector
     *            The sector where the requests starts
     * @param type The type of request
     * @return The quark of the request that was removed or
     *         {@link ITmfStateSystem.INVALID_ATTRIBUTE} if the request was not
     *         present
     */
    private int removeWaitingRequest(long ts, Long sector, IoOperationType type) {
        Pair<Request, Integer> reqQuark = null;
        for (Pair<Request, Integer> waitingRequest : fWaitingQueue) {
            Request request = waitingRequest.getFirst();
            if (request.getSector().equals(sector) && request.getType() == type) {
                reqQuark = waitingRequest;
                break;
            }
        }
        if (reqQuark == null) {
            return ITmfStateSystem.INVALID_ATTRIBUTE;
        }
        fWaitingQueue.remove(reqQuark);
        fWaitingQueueAttrib.recycle(reqQuark.getSecond(), ts);
        return reqQuark.getSecond();
    }

    /**
     * Add a request to the waiting queue. Also saves this request to the state
     * system
     *
     * @param ts
     *            The timestamp at which to add this request
     * @param request
     *            The requests to put
     * @return The quark of the request that has been added
     */
    public int addWaitingRequest(long ts, Request request) {
        int slotQuark = insertInWaitingQueue(ts, request);
        updateQueuesLength(ts);
        return slotQuark;
    }

    private int insertInWaitingQueue(long ts, Request request) {
        int slotQuark = fWaitingQueueAttrib.getAvailable();

        /* Insertion in waiting queue */
        try {
            fSs.modifyAttribute(ts, request.getType().ordinal(), slotQuark);

            int currentRequestQuark = fSs.getQuarkRelativeAndAdd(slotQuark, Attributes.CURRENT_REQUEST);
            fSs.modifyAttribute(ts, request.getSector(), currentRequestQuark);

            int requestSizeQuark = fSs.getQuarkRelativeAndAdd(slotQuark, Attributes.REQUEST_SIZE);
            fSs.modifyAttribute(ts, request.getNrSector(), requestSizeQuark);

            int mergedInQuark = fSs.getQuarkRelativeAndAdd(slotQuark, Attributes.MERGED_IN);
            fSs.modifyAttribute(ts, (Object) null, mergedInQuark);
        } catch (StateValueTypeException e) {
            Activator.getDefault().logError("Error inserting request", e); //$NON-NLS-1$
        }
        fWaitingQueue.add(new Pair<>(request, slotQuark));

        return slotQuark;
    }

    /**
     * Update a request in the waiting queue. Also saves this request to the
     * state system. If the request did not exist previously, it will be added
     * to the queue. Since the sector may have been updated, the initialSector
     * parameters allows to say which was the original sector this request was
     * known for.
     *
     * @param ts
     *            The timestamp at which to add this request
     * @param request
     *            The requests to put
     * @return The quark of the request that has been updated
     */
    public int updateWaitingRequest(long ts, Request request) {
        Pair<Request, Integer> reqQuark = null;
        for (Pair<Request, Integer> waiting : fWaitingQueue) {
            if (waiting.getFirst().getSector().equals(request.getSector())) {
                reqQuark = waiting;
            }
        }
        if (reqQuark == null) {
            return addWaitingRequest(ts, request);
        }

        int slotQuark = reqQuark.getSecond();

        /*
         * Update the sector, number of sectors and merged in request in waiting
         * queue
         */
        try {
            int currentRequestQuark = fSs.getQuarkRelativeAndAdd(slotQuark, Attributes.CURRENT_REQUEST);
            fSs.modifyAttribute(ts, request.getSector(), currentRequestQuark);

            int requestSizeQuark = fSs.getQuarkRelativeAndAdd(slotQuark, Attributes.REQUEST_SIZE);
            fSs.modifyAttribute(ts, request.getNrSector(), requestSizeQuark);

            int mergedInQuark = fSs.getQuarkRelativeAndAdd(slotQuark, Attributes.MERGED_IN);
            fSs.modifyAttribute(ts, (Object) null, mergedInQuark);
        } catch (StateValueTypeException e) {
            Activator.getDefault().logError("Error inserting request", e); //$NON-NLS-1$
        }

        updateQueuesLength(ts);
        return slotQuark;
    }

    /**
     * Get the size of the waiting queue
     *
     * @return The waiting queue size
     */
    public int getWaitingQueueSize() {
        return fWaitingQueue.size();
    }

    /**
     * Return a request from the driver queue starting at requested base sector
     *
     * @param sector
     *            The sector where the requests starts
     * @return The request corresponding to this sector, or null if no request
     *         available
     */
    public @Nullable Request getDriverRequest(Long sector) {
        for (Pair<Request, Integer> request : fDriverQueue) {
            if (request.getFirst().getSector().equals(sector)) {
                return request.getFirst();
            }
        }
        return null;
    }

    /**
     * Removes the request starting at sector from the driver queue
     *
     * @param ts
     *            The timestamp at which to add this request
     * @param sector
     *            The sector where the requests starts
     * @param type
     *            The type of this request
     */
    private void removeDriverRequest(long ts, Long sector, IoOperationType type) {
        Pair<Request, Integer> reqQuark = null;
        for (Pair<Request, Integer> driverRequest : fDriverQueue) {
            Request request = driverRequest.getFirst();
            if (request.getSector().equals(sector) && request.getType() == type) {
                reqQuark = driverRequest;
                break;
            }
        }
        if (reqQuark != null) {
            fDriverQueue.remove(reqQuark);
            fDriverQueueAttrib.recycle(reqQuark.getSecond(), ts);
        }
    }

    /**
     * Issues a request to the disk. This method removes the request from the
     * waiting queue if necessary and adds it to the driver queue.
     *
     * @param ts
     *            The timestamp of this operation
     * @param request
     *            The requests to put
     * @return The quark of the request that was just issued
     */
    public int issueRequest(long ts, Request request) {
        /* Remove from waiting queue */
        Object issuedFromValue = null;
        int fromQuark = removeWaitingRequest(ts, request.getSector(), request.getType());
        if (fromQuark != ITmfStateSystem.INVALID_ATTRIBUTE) {
            issuedFromValue = Integer.parseInt(fSs.getAttributeName(fromQuark));
        } else {
            // Maybe this request is already being processed, so this one is not added
            for (Pair<Request, Integer> issued : fDriverQueue) {
                Request first = issued.getFirst();
                if (first.includes(request)) {
                    return ITmfStateSystem.INVALID_ATTRIBUTE;
                }
            }
        }
        removeDanglingRequests(ts, request);

        int slotQuark = fDriverQueueAttrib.getAvailable();

        /* Insertion in driver queue */
        try {
            fSs.modifyAttribute(ts, request.getType().ordinal(), slotQuark);

            int currentRequestQuark = fSs.getQuarkRelativeAndAdd(slotQuark, Attributes.CURRENT_REQUEST);
            fSs.modifyAttribute(ts, request.getSector(), currentRequestQuark);

            int requestSizeQuark = fSs.getQuarkRelativeAndAdd(slotQuark, Attributes.REQUEST_SIZE);
            fSs.modifyAttribute(ts, request.getNrSector(), requestSizeQuark);

            int issuedFromQuark = fSs.getQuarkRelativeAndAdd(slotQuark, Attributes.ISSUED_FROM);
            fSs.modifyAttribute(ts, issuedFromValue, issuedFromQuark);
        } catch (StateValueTypeException e) {
            Activator.getDefault().logError("Error issuing request", e); //$NON-NLS-1$
        }

        fDriverQueue.add(new Pair<>(request, slotQuark));
        updateQueuesLength(ts);
        return slotQuark;
    }

    private void removeDanglingRequests(long ts, Request request) {
        // There are no events sometimes when merging requests, that should be
        // fixed in kernel instrumentation, but in the meantime, verify if any
        // other request is totally covered by this one, it means it was merged
        // somehow
        List<Pair<Request, Integer>> toRemove = new ArrayList<>();
        for (Pair<Request, Integer> entry : fWaitingQueue) {
            Request otherRequest = entry.getFirst();
            if (request.includes(otherRequest)) {

                toRemove.add(entry);
            }
        }
        for (Pair<Request, Integer> sectorToRemove : toRemove) {
            if (fWaitingQueue.remove(sectorToRemove)) {
                int slotQuark = sectorToRemove.getSecond();
                fWaitingQueueAttrib.recycle(slotQuark, ts);
            }
        }

    }

    /**
     * Completes a request on the disk. It adds to the total of sectors read and
     * written on this disk. It also removes the request from the driver queue
     * if necessary.
     *
     * @param ts
     *            The timestamp of this operation
     * @param request
     *            The requests to put
     */
    public void completeRequest(long ts, Request request) {
        /* Add the total number of sectors read or written */
        try {
            switch (request.getType()) {
            case READ:
                int readQuark = fSs.getQuarkRelativeAndAdd(getQuark(), Attributes.SECTORS_READ);
                StateSystemBuilderUtils.incrementAttributeInt(fSs, ts, readQuark, request.getNrSector());
                break;
            case WRITE:
                int writtenQuark = fSs.getQuarkRelativeAndAdd(getQuark(), Attributes.SECTORS_WRITTEN);
                StateSystemBuilderUtils.incrementAttributeInt(fSs, ts, writtenQuark, request.getNrSector());
                break;
            case FLUSH:
            case OTHER:
                // No read/write to increment
                break;
            default:
                throw new IllegalStateException("Complete request: the request cannot be other than READ or WRITE:" + request.getType()); //$NON-NLS-1$
            }
        } catch (StateValueTypeException e) {
            Activator.getDefault().logError("Error completing request", e); //$NON-NLS-1$
        }

        /* Remove the request from driver queue */
        removeDriverRequest(ts, request.getSector(), request.getType());
        updateQueuesLength(ts);
    }

    /**
     * Merges 2 requests from the waiting queue. The second request will be
     * removed from the queue while the first one will be udpated
     *
     * @param ts
     *            The timestamp of this operation
     * @param baseRequest
     *            The base request that will be kept
     * @param mergedRequest
     *            The merged request to be removed from the queue
     */
    public void mergeRequests(long ts, Request baseRequest, Request mergedRequest) {
        int mergedQuark = removeWaitingRequest(ts, mergedRequest.getSector(), mergedRequest.getType());
        baseRequest.mergeRequest(mergedRequest);
        int baseQuark = updateWaitingRequest(ts, baseRequest);
        if (mergedQuark != ITmfStateSystem.INVALID_ATTRIBUTE) {
            /* Add the merge information */
            try {
                int issuedFromQuark = fSs.getQuarkRelativeAndAdd(mergedQuark, Attributes.MERGED_IN);
                fSs.modifyAttribute(ts, Integer.parseInt(fSs.getAttributeName(baseQuark)), issuedFromQuark);
            } catch (StateValueTypeException e) {
                Activator.getDefault().logError("Error adding the merged request information", e); //$NON-NLS-1$
            }
        }
        updateQueuesLength(ts);
    }

    /**
     * Get the size of the driver queue
     *
     * @return The driver queue size
     */
    public int getDriverQueueSize() {
        return fDriverQueue.size();
    }

    private void updateQueuesLength(long ts) {
        try {
            int fDriverQueueLength = fSs.getQuarkRelativeAndAdd(getQuark(), Attributes.DRIVER_QUEUE_LENGTH);
            fSs.modifyAttribute(ts, getDriverQueueSize(), fDriverQueueLength);
            int fWaitinQueueLength = fSs.getQuarkRelativeAndAdd(getQuark(), Attributes.WAITING_QUEUE_LENGTH);
            fSs.modifyAttribute(ts, getWaitingQueueSize(), fWaitinQueueLength);
        } catch (StateValueTypeException e) {
            Activator.getDefault().logError("Error updating queues lengths", e); //$NON-NLS-1$
        }
    }

}
