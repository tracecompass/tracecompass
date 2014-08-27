/*******************************************************************************
 * Copyright (c) 2013 École Polytechnique de Montréal
 *
 * All rights reserved. This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Geneviève Bastien - Initial implementation and API
 *   Francis Giraldeau - Transform computation using synchronization graph
 *******************************************************************************/

package org.eclipse.linuxtools.internal.tmf.core.synchronization;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.math.BigDecimal;
import java.math.MathContext;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.eclipse.linuxtools.internal.tmf.core.synchronization.graph.SyncSpanningTree;
import org.eclipse.linuxtools.tmf.core.event.ITmfEvent;
import org.eclipse.linuxtools.tmf.core.event.matching.TmfEventDependency;
import org.eclipse.linuxtools.tmf.core.synchronization.ITmfTimestampTransform;
import org.eclipse.linuxtools.tmf.core.synchronization.Messages;
import org.eclipse.linuxtools.tmf.core.synchronization.SynchronizationAlgorithm;
import org.eclipse.linuxtools.tmf.core.synchronization.TimestampTransformFactory;
import org.eclipse.linuxtools.tmf.core.timestamp.ITmfTimestamp;
import org.eclipse.linuxtools.tmf.core.trace.ITmfTrace;

/**
 * Class implementing fully incremental trace synchronization approach as
 * described in
 *
 * Masoume Jabbarifar, Michel Dagenais and Alireza Shameli-Sendi,
 * "Streaming Mode Incremental Clock Synchronization"
 *
 * Since the algorithm itself applies to two traces, it is implemented in a
 * private class, while this public class manages the synchronization between
 * all traces.
 *
 * @author Geneviève Bastien
 */
public class SyncAlgorithmFullyIncremental extends SynchronizationAlgorithm {

    /**
     * Auto-generated serial UID
     */
    private static final long serialVersionUID = -1782788842774838830L;

    private static final MathContext fMc = MathContext.DECIMAL128;

    /** @Serial */
    private final List<ConvexHull> fSyncs;

    private SyncSpanningTree fTree = null;

    /**
     * Initialization of the attributes
     */
    public SyncAlgorithmFullyIncremental() {
        fSyncs = new LinkedList<>();
    }

    /**
     * Function called after all matching has been done, to do any post-match
     * treatment. For this class, it calculates stats, while the data is
     * available
     */
    @Override
    public void matchingEnded() {
        getStats();
    }

    @Override
    public void init(Collection<ITmfTrace> traces) {
        ITmfTrace[] traceArr = traces.toArray(new ITmfTrace[traces.size()]);
        fSyncs.clear();
        /* Create a convex hull for all trace pairs */
        // FIXME: is it necessary to make ConvexHull for every pairs up-front?
        // The ConvexHull seems to be created on the fly in processMatch().
        for (int i = 0; i < traceArr.length; i++) {
            for (int j = i + 1; j < traceArr.length; j++) {
                ConvexHull algo = new ConvexHull(traceArr[i].getHostId(), traceArr[j].getHostId());
                fSyncs.add(algo);
            }
        }
    }

    @Override
    protected void processMatch(TmfEventDependency match) {
        String host1 = match.getSourceEvent().getTrace().getHostId();
        String host2 = match.getDestinationEvent().getTrace().getHostId();

        /* Process only if source and destination are different */
        if (host1.equals(host2)) {
            return;
        }

        /* Check if a convex hull algorithm already exists for these 2 hosts */
        ConvexHull algo = null;
        for (ConvexHull traceSync : fSyncs) {
            if (traceSync.isForHosts(host1, host2)) {
                algo = traceSync;
            }
        }
        if (algo == null) {
            algo = new ConvexHull(host1, host2);
            fSyncs.add(algo);
        }
        algo.processMatch(match);
        invalidateSyncGraph();
    }

    private void invalidateSyncGraph() {
        fTree = null;
    }

    @Override
    public ITmfTimestampTransform getTimestampTransform(ITmfTrace trace) {
        return getTimestampTransform(trace.getHostId());
    }

    @Override
    public ITmfTimestampTransform getTimestampTransform(String hostId) {
        SyncSpanningTree tree = getSyncTree();
        return tree.getTimestampTransform(hostId);
    }

    /**
     * Each convex hull computes the synchronization between 2 given hosts. A
     * synchronization can be done on multiple hosts that may not all
     * communicate with each other. We must use another algorithm to determine
     * which host will be the reference node and what synchronization formula
     * will be used between each host and this reference node.
     *
     * For example, take traces a, b and c where a and c talk to b but do not
     * know each other ({@literal a <-> b <-> c}). The convex hulls will contain
     * the formulae between their 2 traces, but if a is the reference node, then
     * the resulting formula of c would be the composition of {@literal a <-> b}
     * and {@literal b <-> c}
     *
     * @return The synchronization spanning tree for this synchronization
     */
    private SyncSpanningTree getSyncTree() {
        if (fTree == null) {
            fTree = new SyncSpanningTree();
            for (ConvexHull traceSync : fSyncs) {
                SyncQuality q = traceSync.getQuality();
                if (q == SyncQuality.ACCURATE || q == SyncQuality.APPROXIMATE) {
                    String from = traceSync.getReferenceHost();
                    String to = traceSync.getOtherHost();
                    fTree.addSynchronization(from, to, traceSync.getTimestampTransform(to), traceSync.getAccuracy());
                }
            }
        }
        return fTree;
    }

    @Override
    public SyncQuality getSynchronizationQuality(ITmfTrace trace1, ITmfTrace trace2) {
        for (ConvexHull traceSync : fSyncs) {
            if (traceSync.isForHosts(trace1.getHostId(), trace2.getHostId())) {
                return traceSync.getQuality();
            }
        }
        return SyncQuality.ABSENT;
    }

    @Override
    public boolean isTraceSynced(String hostId) {
        ITmfTimestampTransform t = getTimestampTransform(hostId);
        return !t.equals(TimestampTransformFactory.getDefaultTransform());
    }

    @Override
    public Map<String, Map<String, Object>> getStats() {
        /*
         * TODO: Stats, while still accurate, may be misleading now that the
         * sync tree changes synchronization formula. The stats should use the
         * tree instead
         */
        Map<String, Map<String, Object>> statmap = new LinkedHashMap<>();
        for (ConvexHull traceSync : fSyncs) {
            statmap.put(traceSync.getReferenceHost() + " <==> " + traceSync.getOtherHost(), traceSync.getStats()); //$NON-NLS-1$
        }
        return statmap;
    }

    @Override
    public String toString() {
        StringBuilder b = new StringBuilder();
        b.append(getClass().getSimpleName() + " "); //$NON-NLS-1$
        b.append(fSyncs);
        return b.toString();
    }

    /**
     * This is the actual synchronization algorithm between two traces using
     * convex hull
     */
    private class ConvexHull implements Serializable {

        private static final long serialVersionUID = 8309351175030935291L;

        /**
         * The list of meaningful points on the upper hull (received by the
         * reference trace, below in a graph)
         */
        private final LinkedList<SyncPoint> fUpperBoundList = new LinkedList<>();

        /**
         * The list of meaninful points on the lower hull (sent by the reference
         * trace, above in a graph)
         */
        private final LinkedList<SyncPoint> fLowerBoundList = new LinkedList<>();

        /** Points forming the line with maximum slope */
        private final SyncPoint[] fLmax;

        /** Points forming the line with minimum slope */
        private final SyncPoint[] fLmin;

        /**
         * Slopes and ordinate at origin of respectively fLmin, fLmax and the
         * bisector
         */
        private BigDecimal fAlphamin, fBetamax, fAlphamax, fBetamin, fAlpha, fBeta;

        private int fNbMatches, fNbAccurateMatches;
        private String fReferenceHost = "", fOtherHost = ""; //$NON-NLS-1$//$NON-NLS-2$
        private SyncQuality fQuality;

        private Map<String, Object> fStats = new LinkedHashMap<>();

        /**
         * Initialization of the attributes
         *
         * @param host1
         *            ID of the first host
         * @param host2
         *            ID of the second host
         */
        public ConvexHull(String host1, String host2) {
            if (host1.compareTo(host2) > 0) {
                fReferenceHost = host2;
                fOtherHost = host1;
            } else {
                fReferenceHost = host1;
                fOtherHost = host2;
            }
            fLmax = new SyncPoint[2];
            fLmin = new SyncPoint[2];
            fAlpha = BigDecimal.ONE;
            fAlphamax = BigDecimal.ONE;
            fAlphamin = BigDecimal.ONE;
            fBeta = BigDecimal.ZERO;
            fBetamax = BigDecimal.ZERO;
            fBetamin = BigDecimal.ZERO;
            fNbMatches = 0;
            fNbAccurateMatches = 0;
            fQuality = SyncQuality.ABSENT; // default quality
        }

        protected void processMatch(TmfEventDependency match) {

            LinkedList<SyncPoint> boundList, otherBoundList;

            SyncPoint[] line, otherLine;
            SyncPoint p;
            int inversionFactor = 1;
            boolean qualify = false;
            fNbMatches++;

            /* Initialize data depending on the which hull the match is part of */
            if (match.getSourceEvent().getTrace().getHostId().compareTo(match.getDestinationEvent().getTrace().getHostId()) > 0) {
                boundList = fUpperBoundList;
                otherBoundList = fLowerBoundList;
                line = fLmin;
                otherLine = fLmax;
                p = new SyncPoint(match.getDestinationEvent(), match.getSourceEvent());
                inversionFactor = 1;
            } else {
                boundList = fLowerBoundList;
                otherBoundList = fUpperBoundList;
                line = fLmax;
                otherLine = fLmin;
                p = new SyncPoint(match.getSourceEvent(), match.getDestinationEvent());
                inversionFactor = -1;
            }

            /*
             * Does the message qualify for the hull, or is in on the wrong side
             * of the reference line
             */
            if ((line[0] == null) || (line[1] == null) || (p.crossProduct(line[0], line[1]) * inversionFactor > 0)) {
                /*
                 * If message qualifies, verify if points need to be removed
                 * from the hull and add the new point as the maximum reference
                 * point for the line. Also clear the stats that are not good
                 * anymore
                 */
                fNbAccurateMatches++;
                qualify = true;
                removeUselessPoints(p, boundList, inversionFactor);
                line[1] = p;
                fStats.clear();
            }

            /*
             * Adjust the boundary of the reference line and if one of the
             * reference point of the other line was removed from the hull, also
             * adjust the other line
             */
            adjustBound(line, otherBoundList, inversionFactor);
            if ((otherLine[1] != null) && !boundList.contains(otherLine[0])) {
                adjustBound(otherLine, boundList, inversionFactor * -1);
            }

            if (qualify) {
                approximateSync();
            }

        }

        /**
         * Calculates slopes and ordinate at origin of fLmax and fLmin to obtain
         * and approximation of the synchronization at this time
         */
        private void approximateSync() {

            /**
             * Line slopes functions
             *
             * Lmax = alpha_max T + beta_min
             *
             * Lmin = alpha_min T + beta_max
             */
            if ((fLmax[0] != null) || (fLmin[0] != null)) {
                /**
                 * Do not recalculate synchronization after it is failed. We
                 * keep the last not failed result.
                 */
                if (getQuality() != SyncQuality.FAIL) {
                    BigDecimal alphamax = fLmax[1].getAlpha(fLmax[0]);
                    BigDecimal alphamin = fLmin[1].getAlpha(fLmin[0]);
                    SyncQuality quality = null;

                    if ((fLmax[0] == null) || (fLmin[0] == null)) {
                        quality = SyncQuality.APPROXIMATE;
                    }
                    else if (alphamax.compareTo(alphamin) > 0) {
                        quality = SyncQuality.ACCURATE;
                    } else {
                        /* Lines intersect, not good */
                        quality = SyncQuality.FAIL;
                    }
                    /*
                     * Only calculate sync if this match does not cause failure
                     * of synchronization
                     */
                    if (quality != SyncQuality.FAIL) {
                        fAlphamax = alphamax;
                        fBetamin = fLmax[1].getBeta(fAlphamax);
                        fAlphamin = alphamin;
                        fBetamax = fLmin[1].getBeta(fAlphamin);
                        fAlpha = fAlphamax.add(fAlphamin).divide(BigDecimal.valueOf(2), fMc);
                        fBeta = fBetamin.add(fBetamax).divide(BigDecimal.valueOf(2), fMc);
                    }
                    setQuality(quality);
                }
            } else if (((fLmax[0] == null) && (fLmin[1] == null))
                    || ((fLmax[1] == null) && (fLmin[0] == null))) {
                /* Either there is no upper hull point or no lower hull */
                setQuality(SyncQuality.INCOMPLETE);
            }
        }

        /*
         * Verify if the line should be adjusted to be more accurate give the
         * hull
         */
        private void adjustBound(SyncPoint[] line, LinkedList<SyncPoint> otherBoundList, int inversionFactor) {
            SyncPoint minPoint = null, nextPoint;
            boolean finishedSearch = false;

            /*
             * Find in the other bound, the origin point of the line, start from
             * the beginning if the point was lost
             */
            int i = Math.max(0, otherBoundList.indexOf(line[0]));

            while ((i < otherBoundList.size() - 1) && !finishedSearch) {
                minPoint = otherBoundList.get(i);
                nextPoint = otherBoundList.get(i + 1);

                /*
                 * If the rotation (cross-product) is not optimal, move to next
                 * point as reference for the line (if available)
                 *
                 * Otherwise, the current minPoint is the minPoint of the line
                 */
                if (minPoint.crossProduct(nextPoint, line[1]) * inversionFactor > 0) {
                    if (nextPoint.getTimeX() < line[1].getTimeX()) {
                        i++;
                    } else {
                        line[0] = null;
                        finishedSearch = true;
                    }
                } else {
                    line[0] = minPoint;
                    finishedSearch = true;
                }
            }

            if (line[0] == null) {
                line[0] = minPoint;
            }

            /* Make sure point 0 is before point 1 */
            if ((line[0] != null) && (line[0].getTimeX() > line[1].getTimeX())) {
                line[0] = null;
            }
        }

        /*
         * When a point qualifies to be in a hull, we verify if any of the
         * existing points need to be removed from the hull
         */
        private void removeUselessPoints(final SyncPoint p, final LinkedList<SyncPoint> boundList, final int inversionFactor) {

            boolean checkRemove = true;

            while (checkRemove && boundList.size() >= 2) {
                if (p.crossProduct(boundList.get(boundList.size() - 2), boundList.getLast()) * inversionFactor > 0) {
                    boundList.removeLast();
                } else {
                    checkRemove = false;
                }
            }
            boundList.addLast(p);
        }

        public ITmfTimestampTransform getTimestampTransform(String hostId) {
            if (hostId.equals(fOtherHost) && (getQuality() == SyncQuality.ACCURATE || getQuality() == SyncQuality.APPROXIMATE || getQuality() == SyncQuality.FAIL)) {
                /* alpha: beta => 1 / fAlpha, -1 * fBeta / fAlpha); */
                return TimestampTransformFactory.createLinear(BigDecimal.ONE.divide(fAlpha, fMc), BigDecimal.valueOf(-1).multiply(fBeta).divide(fAlpha, fMc));
            }
            return TimestampTransformFactory.getDefaultTransform();
        }

        public SyncQuality getQuality() {
            return fQuality;
        }

        public BigDecimal getAccuracy() {
            return fAlphamax.subtract(fAlphamin);
        }

        public Map<String, Object> getStats() {
            if (fStats.size() == 0) {
                String syncQuality;
                switch (getQuality()) {
                case ABSENT:
                    syncQuality = Messages.SyncAlgorithmFullyIncremental_absent;
                    break;
                case ACCURATE:
                    syncQuality = Messages.SyncAlgorithmFullyIncremental_accurate;
                    break;
                case APPROXIMATE:
                    syncQuality = Messages.SyncAlgorithmFullyIncremental_approx;
                    break;
                case INCOMPLETE:
                    syncQuality = Messages.SyncAlgorithmFullyIncremental_incomplete;
                    break;
                case FAIL:
                default:
                    syncQuality = Messages.SyncAlgorithmFullyIncremental_fail;
                    break;
                }

                fStats.put(Messages.SyncAlgorithmFullyIncremental_refhost, fReferenceHost);
                fStats.put(Messages.SyncAlgorithmFullyIncremental_otherhost, fOtherHost);
                fStats.put(Messages.SyncAlgorithmFullyIncremental_quality, syncQuality);
                fStats.put(Messages.SyncAlgorithmFullyIncremental_alpha, fAlpha);
                fStats.put(Messages.SyncAlgorithmFullyIncremental_beta, fBeta);
                fStats.put(Messages.SyncAlgorithmFullyIncremental_ub, (fUpperBoundList.size() == 0) ? Messages.SyncAlgorithmFullyIncremental_NA : fUpperBoundList.size());
                fStats.put(Messages.SyncAlgorithmFullyIncremental_lb, (fLowerBoundList.size() == 0) ? Messages.SyncAlgorithmFullyIncremental_NA : fLowerBoundList.size());
                fStats.put(Messages.SyncAlgorithmFullyIncremental_accuracy, getAccuracy().doubleValue());
                fStats.put(Messages.SyncAlgorithmFullyIncremental_nbmatch, (fNbMatches == 0) ? Messages.SyncAlgorithmFullyIncremental_NA : fNbMatches);
                fStats.put(Messages.SyncAlgorithmFullyIncremental_nbacc, (fNbAccurateMatches == 0) ? Messages.SyncAlgorithmFullyIncremental_NA : fNbAccurateMatches);
                fStats.put(Messages.SyncAlgorithmFullyIncremental_refformula, Messages.SyncAlgorithmFullyIncremental_T_ + fReferenceHost);
                fStats.put(Messages.SyncAlgorithmFullyIncremental_otherformula, fAlpha + Messages.SyncAlgorithmFullyIncremental_mult + Messages.SyncAlgorithmFullyIncremental_T_ + fReferenceHost + Messages.SyncAlgorithmFullyIncremental_add + fBeta);
            }
            return fStats;

        }

        public String getReferenceHost() {
            return fReferenceHost;
        }

        public String getOtherHost() {
            return fOtherHost;
        }

        public boolean isForHosts(String hostId1, String hostId2) {
            return ((fReferenceHost.equals(hostId1) && fOtherHost.equals(hostId2)) || (fReferenceHost.equals(hostId2) && fOtherHost.equals(hostId1)));
        }

        private void writeObject(ObjectOutputStream s)
                throws IOException {
            /*
             * Remove calculation data because most of it is not serializable.
             * We have the statistics anyway
             */
            fUpperBoundList.clear();
            fLowerBoundList.clear();
            fLmin[0] = null;
            fLmin[1] = null;
            fLmax[0] = null;
            fLmax[1] = null;
            s.defaultWriteObject();
        }

        @SuppressWarnings("nls")
        @Override
        public String toString() {
            StringBuilder b = new StringBuilder();
            b.append("Between " + fReferenceHost + " and " + fOtherHost + " [");
            b.append(" alpha " + fAlpha + " beta " + fBeta + " ]");
            return b.toString();
        }

        private void setQuality(SyncQuality fQuality) {
            this.fQuality = fQuality;
        }

    }

    /**
     * Private class representing a point to synchronize on a graph. The x axis
     * is the timestamp of the event from the reference trace while the y axis
     * is the timestamp of the event on the other trace
     */
    private class SyncPoint {
        private final ITmfTimestamp x, y;

        public SyncPoint(ITmfEvent ex, ITmfEvent ey) {
            x = ex.getTimestamp();
            y = ey.getTimestamp();
        }

        public long getTimeX() {
            return x.getValue();
        }

        /**
         * Calculate a cross product of 3 points:
         *
         * If the cross-product < 0, then p, pa, pb are clockwise
         *
         * If the cross-product > 0, then p, pa, pb are counter-clockwise
         *
         * If cross-product == 0, then they are in a line
         *
         * @param pa
         *            First point
         * @param pb
         *            Second point
         * @return The cross product
         */
        public long crossProduct(SyncPoint pa, SyncPoint pb) {
            long cp = ((pa.x.getValue() - x.getValue()) * (pb.y.getValue() - y.getValue()) - (pa.y.getValue() - y.getValue()) * (pb.x.getValue() - x.getValue()));
            return cp;
        }

        /*
         * Gets the alpha (slope) between two points
         */
        public BigDecimal getAlpha(SyncPoint p1) {
            if (p1 == null) {
                return BigDecimal.ONE;
            }
            BigDecimal deltay = BigDecimal.valueOf(y.getValue() - p1.y.getValue());
            BigDecimal deltax = BigDecimal.valueOf(x.getValue() - p1.x.getValue());
            if (deltax.equals(BigDecimal.ZERO)) {
                return BigDecimal.ONE;
            }
            return deltay.divide(deltax, fMc);
        }

        /*
         * Get the beta value (when x = 0) of the line given alpha
         */
        public BigDecimal getBeta(BigDecimal alpha) {
            return BigDecimal.valueOf(y.getValue()).subtract(alpha.multiply(BigDecimal.valueOf(x.getValue()), fMc));
        }

        @Override
        public String toString() {
            return String.format("%s (%s,  %s)", this.getClass().getCanonicalName(), x, y); //$NON-NLS-1$
        }
    }

    private void writeObject(ObjectOutputStream s)
            throws IOException {
        /*
         * Remove the tree because it is not serializable
         */
        fTree = null;
        s.defaultWriteObject();
    }

}
