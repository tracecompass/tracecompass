/*******************************************************************************
 * Copyright (c) 2013, 2014 École Polytechnique de Montréal
 *
 * All rights reserved. This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Geneviève Bastien - Initial implementation
 *******************************************************************************/

package org.eclipse.tracecompass.tmf.core.tests.synchronization;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.tracecompass.internal.tmf.core.synchronization.SyncAlgorithmFullyIncremental;
import org.eclipse.tracecompass.internal.tmf.core.synchronization.TmfTimestampTransformLinearFast;
import org.eclipse.tracecompass.tmf.core.event.matching.TmfEventDependency;
import org.eclipse.tracecompass.tmf.core.synchronization.ITmfTimestampTransform;
import org.eclipse.tracecompass.tmf.core.synchronization.SynchronizationAlgorithm;
import org.eclipse.tracecompass.tmf.core.synchronization.SynchronizationAlgorithm.SyncQuality;
import org.eclipse.tracecompass.tmf.core.synchronization.SynchronizationAlgorithmFactory;
import org.eclipse.tracecompass.tmf.core.synchronization.TimestampTransformFactory;
import org.eclipse.tracecompass.tmf.core.timestamp.TmfTimestamp;
import org.eclipse.tracecompass.tmf.core.trace.ITmfTrace;
import org.eclipse.tracecompass.tmf.tests.stubs.event.TmfSyncEventStub;
import org.eclipse.tracecompass.tmf.tests.stubs.trace.TmfTraceStub;
import org.junit.After;
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
    private @NonNull Collection<ITmfTrace> fTraces = Collections.EMPTY_LIST;

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
     * Clean up
     */
    @After
    public void cleanup() {
        if (t1 != null) {
            t1.dispose();
        }
        if (t2 != null) {
            t2.dispose();
        }
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
        addSyncMatch(syncAlgo, t2, 1, t1, 1);
        assertEquals("SyncAlgorithmFullyIncremental [Between t1 and t2 [ alpha 1 beta 0 ]]", syncAlgo.toString());
        assertEquals(SyncQuality.INCOMPLETE, syncAlgo.getSynchronizationQuality(t1, t2));

        addSyncMatch(syncAlgo, t1, 1, t2, 3);
        assertEquals("SyncAlgorithmFullyIncremental [Between t1 and t2 [ alpha 1 beta 0 ]]", syncAlgo.toString());
        assertEquals(SyncQuality.INCOMPLETE, syncAlgo.getSynchronizationQuality(t1, t2));

        addSyncMatch(syncAlgo, t2, 2, t1, 3);
        assertEquals("SyncAlgorithmFullyIncremental [Between t1 and t2 [ alpha 1 beta 0.5 ]]", syncAlgo.toString());
        assertEquals(SyncQuality.APPROXIMATE, syncAlgo.getSynchronizationQuality(t1, t2));

        addSyncMatch(syncAlgo, t1, 3, t2, 5);
        assertEquals("SyncAlgorithmFullyIncremental [Between t1 and t2 [ alpha 0.75 beta 1.25 ]]", syncAlgo.toString());
        assertEquals(SyncQuality.ACCURATE, syncAlgo.getSynchronizationQuality(t1, t2));

        addSyncMatch(syncAlgo, t1, 4, t2, 8);
        assertEquals("SyncAlgorithmFullyIncremental [Between t1 and t2 [ alpha 0.75 beta 1.25 ]]", syncAlgo.toString());
        assertEquals(SyncQuality.ACCURATE, syncAlgo.getSynchronizationQuality(t1, t2));

        addSyncMatch(syncAlgo, t2, 4, t1, 5);
        assertEquals("SyncAlgorithmFullyIncremental [Between t1 and t2 [ alpha 1.125 beta 0.875 ]]", syncAlgo.toString());
        assertEquals(SyncQuality.ACCURATE, syncAlgo.getSynchronizationQuality(t1, t2));

        addSyncMatch(syncAlgo, t2, 4, t1, 6);
        assertEquals("SyncAlgorithmFullyIncremental [Between t1 and t2 [ alpha 1.125 beta 0.875 ]]", syncAlgo.toString());
        assertEquals(SyncQuality.ACCURATE, syncAlgo.getSynchronizationQuality(t1, t2));

        addSyncMatch(syncAlgo, t1, 6, t2, 7);
        assertEquals("SyncAlgorithmFullyIncremental [Between t1 and t2 [ alpha 0.725 beta 1.275 ]]", syncAlgo.toString());
        assertEquals(SyncQuality.ACCURATE, syncAlgo.getSynchronizationQuality(t1, t2));

        ITmfTimestampTransform tt2 = syncAlgo.getTimestampTransform(t2);
        ITmfTimestampTransform tt1 = syncAlgo.getTimestampTransform(t1);

        assertEquals(syncAlgo.getTimestampTransform(t1.getHostId()), tt1);
        assertEquals(TimestampTransformFactory.getDefaultTransform(), tt1);
        assertEquals(syncAlgo.getTimestampTransform(t2.getHostId()), tt2);
        assertTrue(tt2 instanceof TmfTimestampTransformLinearFast);

        /*
         * Make the two hulls intersect, and make sure the last good formula is
         * kept after failure
         */
        addSyncMatch(syncAlgo, t1, 7, t2, 4);
        assertEquals(SyncQuality.ACCURATE, syncAlgo.getSynchronizationQuality(t1, t2));
        assertEquals("SyncAlgorithmFullyIncremental [Between t1 and t2 [ alpha 0.375 beta 1.625 ]]", syncAlgo.toString());
        // Last good synchronization
        tt2 = syncAlgo.getTimestampTransform(t2);
        tt1 = syncAlgo.getTimestampTransform(t1);
        assertTrue(tt2 instanceof TmfTimestampTransformLinearFast);

        addSyncMatch(syncAlgo, t2, 7, t1, 3);
        assertEquals(SyncQuality.FAIL, syncAlgo.getSynchronizationQuality(t1, t2));
        assertEquals("SyncAlgorithmFullyIncremental [Between t1 and t2 [ alpha 0.375 beta 1.625 ]]", syncAlgo.toString());

        assertEquals(tt2, syncAlgo.getTimestampTransform(t2.getHostId()));
        assertEquals(tt1, syncAlgo.getTimestampTransform(t1.getHostId()));
        assertEquals(TimestampTransformFactory.getDefaultTransform(), tt1);
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

        addSyncMatch(syncAlgo, t1, 1, t2, 3);
        assertEquals(SyncQuality.INCOMPLETE, syncAlgo.getSynchronizationQuality(t1, t2));

        addSyncMatch(syncAlgo, t1, 2, t2, 5);
        assertEquals(SyncQuality.INCOMPLETE, syncAlgo.getSynchronizationQuality(t1, t2));

        addSyncMatch(syncAlgo, t1, 3, t2, 5);
        assertEquals(SyncQuality.INCOMPLETE, syncAlgo.getSynchronizationQuality(t1, t2));

        addSyncMatch(syncAlgo, t1, 4, t2, 7);
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

        addSyncMatch(syncAlgo, t1, 1, t2, 3);
        assertEquals(SyncQuality.INCOMPLETE, syncAlgo.getSynchronizationQuality(t1, t2));

        addSyncMatch(syncAlgo, t1, 2, t2, 5);
        assertEquals(SyncQuality.INCOMPLETE, syncAlgo.getSynchronizationQuality(t1, t2));

        addSyncMatch(syncAlgo, t1, 3, t2, 5);
        assertEquals(SyncQuality.INCOMPLETE, syncAlgo.getSynchronizationQuality(t1, t2));

        addSyncMatch(syncAlgo, t1, 4, t2, 7);
        assertEquals(SyncQuality.INCOMPLETE, syncAlgo.getSynchronizationQuality(t1, t2));
        assertEquals("SyncAlgorithmFullyIncremental [Between t1 and t2 [ alpha 1 beta 0 ]]", syncAlgo.toString());

        addSyncMatch(syncAlgo, t2, 7, t1, 6);
        assertEquals(SyncQuality.APPROXIMATE, syncAlgo.getSynchronizationQuality(t1, t2));

        addSyncMatch(syncAlgo, t2, 8, t1, 6);
        assertEquals(SyncQuality.APPROXIMATE, syncAlgo.getSynchronizationQuality(t1, t2));

        addSyncMatch(syncAlgo, t2, 10, t1, 8);
        assertEquals(SyncQuality.APPROXIMATE, syncAlgo.getSynchronizationQuality(t1, t2));
        assertEquals("SyncAlgorithmFullyIncremental [Between t1 and t2 [ alpha 1 beta 2.5 ]]", syncAlgo.toString());
    }

    private static void addSyncMatch(SynchronizationAlgorithm algo, ITmfTrace sender, long sendTs, ITmfTrace receiver, long receiveTs) {
        algo.addMatch(
                new TmfEventDependency(
                        new TmfSyncEventStub(sender, TmfTimestamp.fromSeconds(sendTs)),
                        new TmfSyncEventStub(receiver, TmfTimestamp.fromSeconds(receiveTs))
                ));
    }

    /**
     * Testing the serialization of the fully incremental synchronization
     * algorithm
     */
    @Test
    public void testFullyIncrementalSerialization() {

        /* Do a run of synchronization and check the results */
        SynchronizationAlgorithm syncAlgo = SynchronizationAlgorithmFactory.getFullyIncrementalAlgorithm();

        syncAlgo.init(fTraces);

        addSyncMatch(syncAlgo, t2, 1, t1, 1);
        addSyncMatch(syncAlgo, t1, 1, t2, 3);
        addSyncMatch(syncAlgo, t2, 2, t1, 3);
        addSyncMatch(syncAlgo, t1, 3, t2, 5);
        addSyncMatch(syncAlgo, t1, 4, t2, 8);
        addSyncMatch(syncAlgo, t2, 4, t1, 5);
        addSyncMatch(syncAlgo, t2, 4, t1, 6);
        addSyncMatch(syncAlgo, t1, 6, t2, 7);

        ITmfTimestampTransform tt2 = syncAlgo.getTimestampTransform(t2);
        ITmfTimestampTransform tt1 = syncAlgo.getTimestampTransform(t1);

        assertEquals(SyncQuality.ACCURATE, syncAlgo.getSynchronizationQuality(t1, t2));
        assertEquals(syncAlgo.getTimestampTransform(t1.getHostId()), tt1);
        assertEquals(TimestampTransformFactory.getDefaultTransform(), tt1);
        assertEquals(syncAlgo.getTimestampTransform(t2.getHostId()), tt2);

        /* Serialize the object */
        String filePath = null;
        try {
            File temp = File.createTempFile("serialSyncAlgo", ".tmp");
            filePath = temp.getAbsolutePath();
        } catch (IOException e) {
            fail("Could not create temporary file for serialization");
        }
        assertNotNull(filePath);

        try (FileOutputStream fileOut = new FileOutputStream(filePath);
                ObjectOutputStream out = new ObjectOutputStream(fileOut);) {
            out.writeObject(syncAlgo);

        } catch (IOException e) {
            fail("Error serializing the synchronization algorithm " + e.getMessage());
        }

        SynchronizationAlgorithm deserialAlgo = null;
        /* De-Serialize the object */
        try (FileInputStream fileIn = new FileInputStream(filePath);
                ObjectInputStream in = new ObjectInputStream(fileIn);) {
            deserialAlgo = (SynchronizationAlgorithm) in.readObject();
        } catch (IOException | ClassNotFoundException e) {
            fail("Error de-serializing the synchronization algorithm " + e.getMessage());
        }

        /* Check that the deserialize algorithm is equivalent to original */
        assertNotNull(deserialAlgo);
        assertTrue(deserialAlgo instanceof SyncAlgorithmFullyIncremental);
        assertEquals(SyncQuality.ACCURATE, deserialAlgo.getSynchronizationQuality(t1, t2));
        assertEquals(tt1, deserialAlgo.getTimestampTransform(t1));
        assertEquals(tt2, deserialAlgo.getTimestampTransform(t2));

    }

}
