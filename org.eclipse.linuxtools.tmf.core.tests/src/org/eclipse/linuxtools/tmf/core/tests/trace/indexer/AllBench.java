/*******************************************************************************
 * Copyright (c) 2013 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Marc-Andre Laperle - Initial API and implementation
 *******************************************************************************/

package org.eclipse.linuxtools.tmf.core.tests.trace.indexer;

import static org.junit.Assert.assertEquals;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.Random;

import org.eclipse.linuxtools.internal.tmf.core.trace.indexer.BTree;
import org.eclipse.linuxtools.internal.tmf.core.trace.indexer.BTreeCheckpointVisitor;
import org.eclipse.linuxtools.internal.tmf.core.trace.indexer.FlatArray;
import org.eclipse.linuxtools.tmf.core.timestamp.TmfTimestamp;
import org.eclipse.linuxtools.tmf.core.trace.indexer.checkpoint.TmfCheckpoint;
import org.eclipse.linuxtools.tmf.core.trace.location.TmfLongLocation;
import org.eclipse.linuxtools.tmf.tests.stubs.trace.TmfTraceStub;

/**
 * A class to benchmark different algoritms for storing the
 * checkpoint index on disk
 *
 * @author Marc-Andre Laperle
 */
public class AllBench {

    private static final boolean reportProgress = true;
    private static ArrayList<ArrayList<Integer>> nums;
    private TmfTraceStub fTrace;
    private File file = new File("index.idx");

    static int BTREE_DEGREE = 10;

    private void setUp() {
        fTrace = new TmfTraceStub();
        if (file.exists()) {
            file.delete();
        }
    }

    private void tearDown() {
        fTrace.dispose();
        fTrace = null;
        if (file.exists()) {
            file.delete();
        }
    }

    private static void generateDataFile(ArrayList<Integer> list, int checkpointsNums) throws IOException {
        File randomDataFile = new File("data" + checkpointsNums);
        try (RandomAccessFile f = new RandomAccessFile(randomDataFile, "rw");) {
            if (randomDataFile.exists()) {
                for (int i = 0; i < checkpointsNums; i++) {
                    Random rand = new Random();
                    int nextInt = rand.nextInt(checkpointsNums);
                    list.add(nextInt);
                    f.writeInt(nextInt);
                }
            } else {
                for (int i = 0; i < checkpointsNums; i++) {
                    list.add(f.readInt());
                }
            }
        }
    }

    @SuppressWarnings("javadoc")
    public static void main(String[] args) throws IOException {
        int checkpointsNums [] = new int [] { 5000, 50000, 500000, 1000000 };
        nums = new ArrayList<>(checkpointsNums.length);

        System.out.println("DEGREE: " + BTREE_DEGREE);

        AllBench b = new AllBench();
        b.setUp();
        for (int i = 0; i < checkpointsNums.length; i++) {
            ArrayList<Integer> list = new ArrayList<>();
            generateDataFile(list, checkpointsNums[i]);
            nums.add(list);

            System.out.println("*** " + checkpointsNums[i] + " checkpoints ***\n");

            b.benchIt(list);
        }
        b.tearDown();
    }

    private void benchIt(ArrayList<Integer> list) {

        System.out.println("Testing BTree\n");

        testInsertAlot(list);

        System.out.println("Testing Array\n");

        testInsertAlotArray(list);
    }

    private void testInsertAlot(ArrayList<Integer> list2) {
        int checkpointsNum = list2.size();

        writeCheckpoints(checkpointsNum);

        ArrayList<Integer> list = new ArrayList<>();
        for (int i = 0; i < checkpointsNum; i++) {
            list.add(i);
        }

        readCheckpoints(checkpointsNum, list, false);
        readCheckpoints(checkpointsNum, list2, true);

        file.delete();

        System.out.println();
    }

    private void testInsertAlotArray(ArrayList<Integer> list2) {
        int checkpointsNum = list2.size();

        writeCheckpointsArray(checkpointsNum);

        ArrayList<Integer> list = new ArrayList<>();
        for (int i = 0; i < checkpointsNum; i++) {
            list.add(i);
        }

        readCheckpointsArray(checkpointsNum, list, false);
        readCheckpointsArray(checkpointsNum, list2, true);

        file.delete();

        System.out.println();
    }

    private void writeCheckpoints(int checkpointsNum) {
        BTree bTree;
        int REPEAT = 10;
        long time = 0;
        for (int j = 0; j < REPEAT; j++) {
            long old = System.currentTimeMillis();
            bTree = new BTree(BTREE_DEGREE, file, fTrace);
            for (int i = 0; i < checkpointsNum; i++) {
                TmfCheckpoint checkpoint = new TmfCheckpoint(new TmfTimestamp(12345 + i), new TmfLongLocation(123456L + i), i);
                bTree.insert(checkpoint);
            }

            time += (System.currentTimeMillis() - old);
            bTree.setIndexComplete();
            bTree.dispose();
            if (j != REPEAT - 1) {
                file.delete();
            }
            if (reportProgress) {
                System.out.print(".");
            }
        }

        System.out.println("Write time average: " + (float) time / REPEAT);
    }

    private void writeCheckpointsArray(int checkpointsNum) {
        FlatArray array;
        int REPEAT = 10;
        long time = 0;
        for (int j = 0; j < REPEAT; j++) {
            long old = System.currentTimeMillis();
            array = new FlatArray(file, fTrace);
            for (int i = 0; i < checkpointsNum; i++) {
                TmfCheckpoint checkpoint = new TmfCheckpoint(new TmfTimestamp(12345 + i), new TmfLongLocation(123456L + i), i);
                array.insert(checkpoint);
            }

            time += (System.currentTimeMillis() - old);
            array.setIndexComplete();
            array.dispose();
            if (j != REPEAT - 1) {
                file.delete();
            }
            if (reportProgress) {
                System.out.print(".");
            }
        }

        System.out.println("Write time average: " + (float) time / REPEAT);
    }


    private void readCheckpoints(int checkpointsNum, ArrayList<Integer> list, boolean random) {
        BTree bTree;
        int REPEAT = 10;
        long time = 0;
        long cacheMisses = 0;
        for (int j = 0; j < REPEAT; j++) {
            long old = System.currentTimeMillis();
            bTree = new BTree(BTREE_DEGREE, file, fTrace);
            for (int i = 0; i < checkpointsNum; i++) {
                Integer randomCheckpoint = list.get(i);
                TmfCheckpoint checkpoint = new TmfCheckpoint(new TmfTimestamp(12345 + randomCheckpoint), new TmfLongLocation(123456L + randomCheckpoint), 0);
                BTreeCheckpointVisitor treeVisitor = new BTreeCheckpointVisitor(checkpoint);
                bTree.accept(treeVisitor);
                assertEquals(randomCheckpoint.intValue(), treeVisitor.getCheckpoint().getCheckpointRank());
            }
            time += (System.currentTimeMillis() - old);
            cacheMisses = bTree.getCacheMisses();
            bTree.dispose();
            if (reportProgress) {
                System.out.print(".");
            }
        }

        System.out.println("Read " + (random ? "(random)" : "(linear)") + "time average: " + (float) time / REPEAT + "            (cache miss: " + cacheMisses + ")");
    }

    private void readCheckpointsArray(int checkpointsNum, ArrayList<Integer> list, boolean random) {
        FlatArray array;
        int REPEAT = 10;
        long time = 0;
        long cacheMisses = 0;
        for (int j = 0; j < REPEAT; j++) {
            long old = System.currentTimeMillis();
            array = new FlatArray(file, fTrace);
            for (int i = 0; i < checkpointsNum; i++) {
                Integer randomCheckpoint = list.get(i);
                TmfCheckpoint checkpoint = new TmfCheckpoint(new TmfTimestamp(12345 + randomCheckpoint), new TmfLongLocation(123456L + randomCheckpoint), 0);
                long found = array.binarySearch(checkpoint);
                assertEquals(randomCheckpoint.intValue(), found);
            }
            time += (System.currentTimeMillis() - old);
            cacheMisses = array.getCacheMisses();
            array.dispose();
            if (reportProgress) {
                System.out.print(".");
            }
        }

        System.out.println("Read " + (random ? "(random)" : "(linear)") + "time average: " + (float) time / REPEAT + "            (cache miss: " + cacheMisses + ")");
    }

}
