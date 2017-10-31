/*******************************************************************************
 * Copyright (c) 2016 École Polytechnique de Montréal
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/

package org.eclipse.tracecompass.statesystem.core.tests.perf.historytree;

import static org.junit.Assert.fail;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.PriorityQueue;
import java.util.Queue;
import java.util.Random;

import org.apache.commons.io.FileUtils;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.test.performance.Dimension;
import org.eclipse.test.performance.Performance;
import org.eclipse.test.performance.PerformanceMeter;
import org.eclipse.tracecompass.common.core.NonNullUtils;
import org.eclipse.tracecompass.internal.statesystem.core.backend.historytree.HistoryTreeBackend;
import org.eclipse.tracecompass.statesystem.core.ITmfStateSystemBuilder;
import org.eclipse.tracecompass.statesystem.core.StateSystemFactory;
import org.eclipse.tracecompass.statesystem.core.StateSystemUtils;
import org.eclipse.tracecompass.statesystem.core.backend.IStateHistoryBackend;
import org.eclipse.tracecompass.statesystem.core.backend.StateHistoryBackendFactory;
import org.eclipse.tracecompass.statesystem.core.exceptions.AttributeNotFoundException;
import org.eclipse.tracecompass.statesystem.core.exceptions.StateSystemDisposedException;
import org.eclipse.tracecompass.statesystem.core.exceptions.StateValueTypeException;
import org.eclipse.tracecompass.statesystem.core.interval.ITmfStateInterval;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

import com.google.common.collect.ImmutableList;

/**
 * This class benchmarks writing intervals to the state system and querying them
 * using a history tree backend
 *
 * @author Geneviève Bastien
 */
@RunWith(Parameterized.class)
public class HistoryTreeBackendBenchmark {

    private static final @NonNull String TEST_PREFIX = "org.eclipse.tracecompass#History Tree Backend#";
    private static final @NonNull String TEST_BUILDING_ID = "Build: ";
    private static final @NonNull String TEST_SINGLE_QUERY_ID = "Single Queries: ";
    private static final @NonNull String TEST_FULL_QUERY_ID = "Full Queries: ";
    private static final @NonNull String TEST_QUERY_RANGE_ID = "Query History Range: ";
    private static final @NonNull String TEST_2D_QUERY_ID = "2D Queries: ";
    private static final @NonNull String ROOT_NODE = "root";
    private static final int QUEUE_SIZE = 10000;
    private static final long SEED = 5575784704147L;
    private static final int QUERY_COUNT = 100;
    private static final int INTERVAL_AVG_TIME = 1000;

    /* Values for the average case */
    private static final int DEFAULT_NB_ATTRIB = 1500;
    private static final int DEFAULT_NB_INTERVALS = 500;
    private static final int DEFAULT_LOOP_COUNT = 40;

    private File fTempFile;
    private final String fName;
    private final String fShortName;
    private final int fNbAttrib;
    private final int fNbAvgIntervals;
    private final int fNbLoops;
    private final HTBValues fValues;
    private final IIntervalDistribution fDistributionMethod;

    /**
     * Constructor
     *
     * @param name
     *            The name of the test
     * @param shortName
     *            A short name for this scenario (at most 40 characters,
     *            otherwise it will be truncated in the DB)
     * @param nbAttrib
     *            The number of attributes
     * @param nbAvgIntervals
     *            An idea on the number of intervals per attributes. The exact
     *            number will depend on the interval time distribution
     * @param nbLoops
     *            The number of times to execute the benchmark
     * @param values
     *            The values to put in the history tree
     * @param distributionMethod
     *            A distribution method that will return the next interval
     *            duration according to an algorithm
     */
    public HistoryTreeBackendBenchmark(String name, String shortName, int nbAttrib, int nbAvgIntervals, int nbLoops, HTBValues values, IIntervalDistribution distributionMethod) {
        fName = name;
        fShortName = shortName;
        fNbAttrib = nbAttrib;
        fNbAvgIntervals = nbAvgIntervals;
        fNbLoops = nbLoops;
        fValues = values;
        fDistributionMethod = distributionMethod;
    }

    /* A list of values to use for the intervals */
    private enum HTBValues {
        INTEGERS(ImmutableList.of(1, 2, 3)),
        STRINGS(ImmutableList.of("abc", "def", "wihi!")),
        LONGS(ImmutableList.of(Long.MAX_VALUE, 1L, 1234567L)),
        DOUBLES(ImmutableList.of(Double.MAX_VALUE, 1.0, 123.456));

        private final List<Object> fValues;

        private HTBValues(List<Object> values) {
            fValues = values;
        }

        public List<Object> getValues() {
            return fValues;
        }
    }

    @FunctionalInterface
    private static interface IIntervalDistribution {
        long getNextEndTime(Random randomGenerator, long limit);
    }

    /* Generate interval duration uniformly distributed in the range [1, 2l] */
    private static IIntervalDistribution UNIFORM = (r, l) -> {
        long nextLong = r.nextLong();
        long nextDelta = l + (nextLong % l);
        return nextDelta;
    };

    /*
     * Generates interval duration in the range [1, 2l] with 75% between [0.5l,
     * 1.5l]
     */
    private static IIntervalDistribution CLOSER_TO_LIMIT = (r, l) -> {
        /*
         * This method will return interval time closer to the specified limit
         */
        double nextDouble = r.nextDouble();
        int sign = (nextDouble < 0) ? -1 : 1;
        long nextDelta = l + (long) (Math.sqrt(nextDouble) * l * sign);
        return nextDelta;
    };

    /*
     * Generates interval duration in the range [1, 2l] with 75% between [0.5l,
     * 1.5l], but with 10% outliers between [2l, 5l]
     */
    private static IIntervalDistribution CLOSER_TO_LIMIT_10_PERCENT_OUTLIERS = (r, l) -> {
        long nextLong = Math.abs(r.nextLong());
        /* Is this an outlier */
        if ((nextLong % 100) < 10) {
            /* Create an outlier that can go up to 5 times the limit */
            return l + (((nextLong % 3) + 1) * l) + (nextLong % l);
        }
        /* Otherwise follow a distribution with more values around the limit */
        double nextDouble = r.nextDouble();
        int sign = (nextDouble < 0) ? -1 : 1;
        long nextDelta = l + (long) (Math.sqrt(nextDouble) * l * sign);
        return nextDelta;
    };

    /**
     * @return The arrays of parameters
     */
    @Parameters(name = "{index}: {0}")
    public static Iterable<Object[]> getParameters() {
        return Arrays.asList(new Object[][] {
                { "Average case: 1500 attributes, integers, interval duration random around limit l with 75 percent within [0.5l, 1.5l]", "Average case", DEFAULT_NB_ATTRIB, DEFAULT_NB_INTERVALS, DEFAULT_LOOP_COUNT, HTBValues.INTEGERS, CLOSER_TO_LIMIT },
                { "Vertical scaling (more attributes)", "Vertical scaling", 3500, DEFAULT_NB_INTERVALS, 5, HTBValues.INTEGERS, CLOSER_TO_LIMIT },
                { "Horizontal scaling (more intervals/attribute)", "Horizontal scaling", DEFAULT_NB_ATTRIB, 20000, 10, HTBValues.INTEGERS, CLOSER_TO_LIMIT },
                { "Interval durations uniformly distributed within [1, 2l]", "Uniform distribution of intervals", DEFAULT_NB_ATTRIB, DEFAULT_NB_INTERVALS, DEFAULT_LOOP_COUNT, HTBValues.INTEGERS, UNIFORM },
                { "Interval durations with 10 percent outliers > 2l", "Distribution with outliers", DEFAULT_NB_ATTRIB, DEFAULT_NB_INTERVALS, DEFAULT_LOOP_COUNT, HTBValues.INTEGERS, CLOSER_TO_LIMIT_10_PERCENT_OUTLIERS },
                { "Data type: strings", "Data type: strings", DEFAULT_NB_ATTRIB, DEFAULT_NB_INTERVALS, DEFAULT_LOOP_COUNT, HTBValues.STRINGS, CLOSER_TO_LIMIT },
                { "Data type: longs", "Data type: longs", DEFAULT_NB_ATTRIB, DEFAULT_NB_INTERVALS, DEFAULT_LOOP_COUNT, HTBValues.LONGS, CLOSER_TO_LIMIT },
                { "Data type: doubles", "Data type: doubles", DEFAULT_NB_ATTRIB, DEFAULT_NB_INTERVALS, DEFAULT_LOOP_COUNT, HTBValues.DOUBLES, CLOSER_TO_LIMIT },
        });
    }

    /**
     * Create the temporary file for this history tree
     */
    private void createFile() {
        try {
            fTempFile = File.createTempFile("tmpStateSystem", null);
        } catch (IOException e) {
            fail(e.getMessage());
        }
    }

    /**
     * Delete the temporary history tree file after the test
     */
    private void deleteFile() {
        if (fTempFile != null) {
            fTempFile.delete();
            fTempFile = null;
        }
    }

    private static class QuarkEvent implements Comparable<QuarkEvent> {
        private final int fQuark;
        private long fNextEventTime;
        private final List<Object> fPossibleValues;
        private int fNextValue = 0;

        public QuarkEvent(int quark, long nextEventTime, List<Object> valuesList) {
            fQuark = quark;
            fNextEventTime = nextEventTime;
            fPossibleValues = valuesList;
        }

        public int getQuark() {
            return fQuark;
        }

        public long getNextEventTime() {
            return fNextEventTime;
        }

        public void setNextEventTime(long t) {
            fNextEventTime = t;
        }

        public Object getNextValue() {
            Object value = fPossibleValues.get(fNextValue);
            fNextValue++;
            if (fNextValue >= fPossibleValues.size()) {
                fNextValue = 0;
            }
            return value;
        }

        @Override
        public int compareTo(QuarkEvent other) {
            return Long.compare(fNextEventTime, other.getNextEventTime());
        }
    }

    /**
     * Benchmarks creating, single querying and full querying the state system
     */
    @Test
    public void testBenchmark() {
        /* Check arguments */
        long totalTime = this.fNbAvgIntervals * INTERVAL_AVG_TIME;

        Performance perf = Performance.getDefault();
        PerformanceMeter pmBuild = perf.createPerformanceMeter(TEST_PREFIX + TEST_BUILDING_ID + fName);
        perf.tagAsSummary(pmBuild, TEST_BUILDING_ID + fShortName, Dimension.CPU_TIME);

        PerformanceMeter pmSingleQuery = perf.createPerformanceMeter(TEST_PREFIX + TEST_SINGLE_QUERY_ID + fName);
        perf.tagAsSummary(pmSingleQuery, TEST_SINGLE_QUERY_ID + fShortName, Dimension.CPU_TIME);

        PerformanceMeter pmFullQuery = perf.createPerformanceMeter(TEST_PREFIX + TEST_FULL_QUERY_ID + fName);
        perf.tagAsSummary(pmFullQuery, TEST_FULL_QUERY_ID + fShortName, Dimension.CPU_TIME);

        PerformanceMeter pmRangeQuery = perf.createPerformanceMeter(TEST_PREFIX + TEST_QUERY_RANGE_ID + fName);
        perf.tagAsSummary(pmRangeQuery, TEST_QUERY_RANGE_ID + fShortName, Dimension.CPU_TIME);

        PerformanceMeter pm2DQuery = perf.createPerformanceMeter(TEST_PREFIX + TEST_2D_QUERY_ID + fName);
        perf.tagAsSummary(pm2DQuery, TEST_2D_QUERY_ID + fShortName, Dimension.CPU_TIME);

        for (int i = 0; i < fNbLoops; i++) {
            try {
                /* Create the state system */
                createFile();
                IStateHistoryBackend backend = StateHistoryBackendFactory.createHistoryTreeBackendNewFile(TEST_BUILDING_ID, NonNullUtils.checkNotNull(fTempFile), 1, 1, QUEUE_SIZE);
                ITmfStateSystemBuilder ss = StateSystemFactory.newStateSystem(backend);

                /* Initialize the attributes */
                Queue<QuarkEvent> quarkEvents = new PriorityQueue<>(fNbAttrib);
                Random randomGenerator = new Random(SEED);
                int rootQuark = ss.getQuarkAbsoluteAndAdd(ROOT_NODE);

                /* Create all attributes before testing */
                for (int j = 0; j < fNbAttrib; j++) {
                    int quark = ss.getQuarkRelativeAndAdd(rootQuark, String.valueOf(j));
                    quarkEvents.add(new QuarkEvent(quark, (Math.abs(randomGenerator.nextLong()) % INTERVAL_AVG_TIME) + 1, fValues.getValues()));
                }

                /* Adds random intervals to the state system */
                pmBuild.start();
                while (true) {
                    QuarkEvent quarkEvent = quarkEvents.poll();
                    if (quarkEvent == null) {
                        break;
                    }
                    long eventTime = quarkEvent.getNextEventTime();
                    ss.modifyAttribute(eventTime, quarkEvent.getNextValue(), quarkEvent.getQuark());
                    long nextDelta = fDistributionMethod.getNextEndTime(randomGenerator, INTERVAL_AVG_TIME);
                    long nextEndTime = eventTime + nextDelta;
                    if (nextEndTime <= totalTime) {
                        quarkEvent.setNextEventTime(nextEndTime);
                        quarkEvents.add(quarkEvent);
                    }
                }
                ss.closeHistory(totalTime);
                pmBuild.stop();

                /*
                 * Benchmark the single queries: for each random timestamp,
                 * query a random attribute
                 */
                List<Integer> subAttributes = ss.getSubAttributes(rootQuark, false);
                pmSingleQuery.start();
                for (int j = 0; j < QUERY_COUNT; j++) {
                    long ts = getNextRandomValue(randomGenerator, totalTime);
                    int attrib = (int) getNextRandomValue(randomGenerator, subAttributes.size());
                    ss.querySingleState(ts, attrib);
                }
                pmSingleQuery.stop();

                /* Benchmark the history range query of 10 attributes */
                pmRangeQuery.start();
                List<Integer> queryAttributes = new ArrayList<>();
                for (int j = 0; j < 10; j++) {
                    int attrib = (int) getNextRandomValue(randomGenerator, subAttributes.size());
                    queryAttributes.add(attrib);
                    StateSystemUtils.queryHistoryRange(ss, attrib, ss.getStartTime(), ss.getCurrentEndTime());
                }
                pmRangeQuery.stop();

                /* Benchmark 2D query of the same 10 attributes */
                pm2DQuery.start();
                for (int j = 0; j < 10; j++) {
                    Iterable<@NonNull ITmfStateInterval> query2d = ss.query2D(queryAttributes, ss.getStartTime(), ss.getCurrentEndTime());
                    Iterator<@NonNull ITmfStateInterval> iterator = query2d.iterator();
                    while (iterator.hasNext()) {
                        iterator.next();
                    }
                }
                pm2DQuery.stop();

                /* Benchmark the full queries */
                pmFullQuery.start();
                for (int j = 0; j < QUERY_COUNT; j++) {
                    long ts = getNextRandomValue(randomGenerator, totalTime);
                    ss.queryFullState(ts);
                }
                pmFullQuery.stop();

                /* Output some data on the file */
                if (i == 0) {
                    if (backend instanceof HistoryTreeBackend) {
                        HistoryTreeBackend htBackend = (HistoryTreeBackend) backend;
                        System.out.println("History tree file size: " + FileUtils.byteCountToDisplaySize(htBackend.getFileSize()));
                        System.out.println("Average node usage: " + htBackend.getAverageNodeUsage());
                    }
                }
                deleteFile();
            } catch (IOException | StateValueTypeException | AttributeNotFoundException | StateSystemDisposedException e) {
                fail(e.getMessage());
            } finally {
                deleteFile();
            }
        }
        pmBuild.commit();
        pmSingleQuery.commit();
        pmFullQuery.commit();
        pmRangeQuery.commit();
        pm2DQuery.commit();
    }

    /**
     * Get a next random value between 1 and a boundary.
     */
    private static long getNextRandomValue(Random randomGenerator, long limit) {
        long nextLong = Math.abs(randomGenerator.nextLong());
        long nextDelta = (nextLong % limit) + 1;
        return nextDelta;
    }
}
