/*******************************************************************************
 * Copyright (c) 2013 École Polytechnique de Montréal
 *
 * All rights reserved. This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Geneviève Bastien - Initial implementation
 *******************************************************************************/

package org.eclipse.linuxtools.tmf.core.tests.synchronization;

import static org.junit.Assert.assertEquals;

import java.util.Collection;
import java.util.LinkedList;

import org.eclipse.linuxtools.tmf.core.event.matching.TmfEventDependency;
import org.eclipse.linuxtools.tmf.core.synchronization.ITmfTimestampTransform;
import org.eclipse.linuxtools.tmf.core.synchronization.SynchronizationAlgorithm;
import org.eclipse.linuxtools.tmf.core.synchronization.SynchronizationAlgorithm.SyncQuality;
import org.eclipse.linuxtools.tmf.core.synchronization.SynchronizationAlgorithmFactory;
import org.eclipse.linuxtools.tmf.core.synchronization.TimestampTransformFactory;
import org.eclipse.linuxtools.tmf.core.timestamp.TmfTimestamp;
import org.eclipse.linuxtools.tmf.core.trace.ITmfTrace;
import org.eclipse.linuxtools.tmf.tests.stubs.event.TmfSyncEventStub;
import org.eclipse.linuxtools.tmf.tests.stubs.trace.TmfTraceStub;
import org.junit.Before;
import org.junit.Test;

/**
 * Tests for {@link SynchronizationAlgorithm} and its descendants
 *
 * @author Geneviève Bastien
 */
@SuppressWarnings("nls")
public class SyncTest {

    private TmfTraceStub t1, t2;
    private Collection<ITmfTrace> fTraces;

    /**
     * Initializing the traces
     */
    @Before
    public void init() {
        t1 = new TmfTraceStub();
        t1.init("t1");
        t2 = new TmfTraceStub();
        t2.init("t2");

        Collection<ITmfTrace> traces = new LinkedList<>();
        traces.add(t1);
        traces.add(t2);
        fTraces = traces;
    }

    /**
     * Testing fully incremental algorithm with communication between the two
     * traces
     */
    @Test
    public void testFullyIncremental() {

        SynchronizationAlgorithm syncAlgo = SynchronizationAlgorithmFactory.getFullyIncrementalAlgorithm();

        syncAlgo.init(fTraces);

        assertEquals(SyncQuality.ABSENT, syncAlgo.getSynchronizationQuality(t1, t2));
        syncAlgo.addMatch(
                new TmfEventDependency(new TmfSyncEventStub(t2, new TmfTimestamp(1)),
                        new TmfSyncEventStub(t1, new TmfTimestamp(1))
                ));
        assertEquals("SyncAlgorithmFullyIncremental [Between t1 and t2 [ alpha 1 beta 0 ]]", syncAlgo.toString());
        assertEquals(SyncQuality.INCOMPLETE, syncAlgo.getSynchronizationQuality(t1, t2));

        syncAlgo.addMatch(
                new TmfEventDependency(new TmfSyncEventStub(t1, new TmfTimestamp(1)),
                        new TmfSyncEventStub(t2, new TmfTimestamp(3))
                ));
        assertEquals("SyncAlgorithmFullyIncremental [Between t1 and t2 [ alpha 1 beta 0 ]]", syncAlgo.toString());
        assertEquals(SyncQuality.INCOMPLETE, syncAlgo.getSynchronizationQuality(t1, t2));

        syncAlgo.addMatch(
                new TmfEventDependency(new TmfSyncEventStub(t2, new TmfTimestamp(2)),
                        new TmfSyncEventStub(t1, new TmfTimestamp(3))
                ));
        assertEquals("SyncAlgorithmFullyIncremental [Between t1 and t2 [ alpha 1 beta 0.5 ]]", syncAlgo.toString());
        assertEquals(SyncQuality.APPROXIMATE, syncAlgo.getSynchronizationQuality(t1, t2));

        syncAlgo.addMatch(
                new TmfEventDependency(new TmfSyncEventStub(t1, new TmfTimestamp(3)),
                        new TmfSyncEventStub(t2, new TmfTimestamp(5))
                ));
        assertEquals("SyncAlgorithmFullyIncremental [Between t1 and t2 [ alpha 0.75 beta 1.25 ]]", syncAlgo.toString());
        assertEquals(SyncQuality.ACCURATE, syncAlgo.getSynchronizationQuality(t1, t2));

        syncAlgo.addMatch(
                new TmfEventDependency(new TmfSyncEventStub(t1, new TmfTimestamp(4)),
                        new TmfSyncEventStub(t2, new TmfTimestamp(8))
                ));
        assertEquals("SyncAlgorithmFullyIncremental [Between t1 and t2 [ alpha 0.75 beta 1.25 ]]", syncAlgo.toString());
        assertEquals(SyncQuality.ACCURATE, syncAlgo.getSynchronizationQuality(t1, t2));

        syncAlgo.addMatch(
                new TmfEventDependency(new TmfSyncEventStub(t2, new TmfTimestamp(4)),
                        new TmfSyncEventStub(t1, new TmfTimestamp(5))
                ));
        assertEquals("SyncAlgorithmFullyIncremental [Between t1 and t2 [ alpha 1.125 beta 0.875 ]]", syncAlgo.toString());
        assertEquals(SyncQuality.ACCURATE, syncAlgo.getSynchronizationQuality(t1, t2));

        syncAlgo.addMatch(
                new TmfEventDependency(new TmfSyncEventStub(t2, new TmfTimestamp(4)),
                        new TmfSyncEventStub(t1, new TmfTimestamp(6))
                ));
        assertEquals("SyncAlgorithmFullyIncremental [Between t1 and t2 [ alpha 1.125 beta 0.875 ]]", syncAlgo.toString());
        assertEquals(SyncQuality.ACCURATE, syncAlgo.getSynchronizationQuality(t1, t2));

        syncAlgo.addMatch(
                new TmfEventDependency(new TmfSyncEventStub(t1, new TmfTimestamp(6)),
                        new TmfSyncEventStub(t2, new TmfTimestamp(7))
                ));
        assertEquals("SyncAlgorithmFullyIncremental [Between t1 and t2 [ alpha 0.725 beta 1.275 ]]", syncAlgo.toString());
        assertEquals(SyncQuality.ACCURATE, syncAlgo.getSynchronizationQuality(t1, t2));

        ITmfTimestampTransform tt2 = syncAlgo.getTimestampTransform(t2);
        ITmfTimestampTransform tt1 = syncAlgo.getTimestampTransform(t1);

        assertEquals(syncAlgo.getTimestampTransform(t1.getHostId()), tt1);
        assertEquals(TimestampTransformFactory.getDefaultTransform(), tt1);
        assertEquals(syncAlgo.getTimestampTransform(t2.getHostId()), tt2);

        /* Make the two hulls intersect */
        syncAlgo.addMatch(
                new TmfEventDependency(new TmfSyncEventStub(t1, new TmfTimestamp(7)),
                        new TmfSyncEventStub(t2, new TmfTimestamp(4))
                ));
        syncAlgo.addMatch(
                new TmfEventDependency(new TmfSyncEventStub(t2, new TmfTimestamp(7)),
                        new TmfSyncEventStub(t1, new TmfTimestamp(3))
                ));
        assertEquals(SyncQuality.FAIL, syncAlgo.getSynchronizationQuality(t1, t2));
    }

    /**
     * Testing the fully incremental synchronization algorithm when
     * communication goes in only one direction
     */
    @Test
    public void testOneHull() {

        SynchronizationAlgorithm syncAlgo = SynchronizationAlgorithmFactory.getFullyIncrementalAlgorithm();

        syncAlgo.init(fTraces);

        assertEquals(SyncQuality.ABSENT, syncAlgo.getSynchronizationQuality(t1, t2));

        syncAlgo.addMatch(
                new TmfEventDependency(new TmfSyncEventStub(t1, new TmfTimestamp(1)),
                        new TmfSyncEventStub(t2, new TmfTimestamp(3)))
                );
        assertEquals(SyncQuality.INCOMPLETE, syncAlgo.getSynchronizationQuality(t1, t2));

        syncAlgo.addMatch(
                new TmfEventDependency(new TmfSyncEventStub(t1, new TmfTimestamp(2)),
                        new TmfSyncEventStub(t2, new TmfTimestamp(5)))
                );

        assertEquals(SyncQuality.INCOMPLETE, syncAlgo.getSynchronizationQuality(t1, t2));

        syncAlgo.addMatch(
                new TmfEventDependency(new TmfSyncEventStub(t1, new TmfTimestamp(3)),
                        new TmfSyncEventStub(t2, new TmfTimestamp(5)))
                );
        assertEquals(SyncQuality.INCOMPLETE, syncAlgo.getSynchronizationQuality(t1, t2));

        syncAlgo.addMatch(
                new TmfEventDependency(new TmfSyncEventStub(t1, new TmfTimestamp(4)),
                        new TmfSyncEventStub(t2, new TmfTimestamp(7)))
                );
        assertEquals(SyncQuality.INCOMPLETE, syncAlgo.getSynchronizationQuality(t1, t2));
        assertEquals("SyncAlgorithmFullyIncremental [Between t1 and t2 [ alpha 1 beta 0 ]]", syncAlgo.toString());

    }

    /**
     * Testing the fully incremental synchronization algorithm when all
     * communication from trace1 to trace2 happens before all communication from
     * trace2 to trace1
     */
    @Test
    public void testDisjoint() {

        SynchronizationAlgorithm syncAlgo = SynchronizationAlgorithmFactory.getFullyIncrementalAlgorithm();

        syncAlgo.init(fTraces);

        assertEquals(SyncQuality.ABSENT, syncAlgo.getSynchronizationQuality(t1, t2));

        syncAlgo.addMatch(
                new TmfEventDependency(new TmfSyncEventStub(t1, new TmfTimestamp(1)),
                        new TmfSyncEventStub(t2, new TmfTimestamp(3)))
                );
        assertEquals(SyncQuality.INCOMPLETE, syncAlgo.getSynchronizationQuality(t1, t2));

        syncAlgo.addMatch(
                new TmfEventDependency(new TmfSyncEventStub(t1, new TmfTimestamp(2)),
                        new TmfSyncEventStub(t2, new TmfTimestamp(5)))
                );

        assertEquals(SyncQuality.INCOMPLETE, syncAlgo.getSynchronizationQuality(t1, t2));

        syncAlgo.addMatch(
                new TmfEventDependency(new TmfSyncEventStub(t1, new TmfTimestamp(3)),
                        new TmfSyncEventStub(t2, new TmfTimestamp(5)))
                );
        assertEquals(SyncQuality.INCOMPLETE, syncAlgo.getSynchronizationQuality(t1, t2));

        syncAlgo.addMatch(
                new TmfEventDependency(new TmfSyncEventStub(t1, new TmfTimestamp(4)),
                        new TmfSyncEventStub(t2, new TmfTimestamp(7)))
                );
        assertEquals(SyncQuality.INCOMPLETE, syncAlgo.getSynchronizationQuality(t1, t2));
        assertEquals("SyncAlgorithmFullyIncremental [Between t1 and t2 [ alpha 1 beta 0 ]]", syncAlgo.toString());

        syncAlgo.addMatch(
                new TmfEventDependency(new TmfSyncEventStub(t2, new TmfTimestamp(7)),
                        new TmfSyncEventStub(t1, new TmfTimestamp(6)))
                );
        assertEquals(SyncQuality.APPROXIMATE, syncAlgo.getSynchronizationQuality(t1, t2));

        syncAlgo.addMatch(
                new TmfEventDependency(new TmfSyncEventStub(t2, new TmfTimestamp(8)),
                        new TmfSyncEventStub(t1, new TmfTimestamp(6)))
                );
        assertEquals(SyncQuality.APPROXIMATE, syncAlgo.getSynchronizationQuality(t1, t2));

        syncAlgo.addMatch(
                new TmfEventDependency(new TmfSyncEventStub(t2, new TmfTimestamp(10)),
                        new TmfSyncEventStub(t1, new TmfTimestamp(8)))
                );
        assertEquals(SyncQuality.APPROXIMATE, syncAlgo.getSynchronizationQuality(t1, t2));
        assertEquals("SyncAlgorithmFullyIncremental [Between t1 and t2 [ alpha 1 beta 2.5 ]]", syncAlgo.toString());
    }
}
