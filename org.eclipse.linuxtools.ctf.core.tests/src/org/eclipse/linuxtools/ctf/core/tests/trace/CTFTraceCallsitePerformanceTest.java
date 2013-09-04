/*******************************************************************************
 * Copyright (c) 2013 Ericsson
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Matthew Khouzam - Initial API and implementation
 *******************************************************************************/

package org.eclipse.linuxtools.ctf.core.tests.trace;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assume.assumeTrue;

import java.util.List;
import java.util.Random;

import org.eclipse.linuxtools.ctf.core.event.CTFCallsite;
import org.eclipse.linuxtools.ctf.core.tests.shared.CtfTestTrace;
import org.eclipse.linuxtools.ctf.core.trace.CTFReaderException;
import org.eclipse.linuxtools.ctf.core.trace.CTFTrace;
import org.junit.Before;
import org.junit.Test;

/**
 * Test the performance of the callsite storage in the CTF trace.
 *
 * @author Matthew Khouzam
 */
public class CTFTraceCallsitePerformanceTest {

    private static final CtfTestTrace testTrace = CtfTestTrace.KERNEL;

    private static final int NUMBER_OF_SEEKS = 100000;

    private final String[] callsites = { "Alligator", "Bunny", "Cat",
            "Dolphin", "Echidna", "Gazelle", "Heron", "Ibex", "Jackalope",
            "Koala", "Lynx", "Meerkat", "Narwhal", "Ocelot", "Pangolin",
            "Quetzal", "Ringtail", "Sandpiper", "Tiger", "Urchin", "Vulture",
            "Walrus", "X-Ray Tetra", "Zonkey" };

    private final String[] functions = { "sentence", "together", "children",
            "mountain", "chipmunk", "crashing", "drinking", "insisted",
            "insulted", "invented", "squinted", "standing", "swishing",
            "talented", "whiplash", "complain", "granddad", "sprinkle",
            "surprise", "umbrella", "anything", "anywhere", "baseball",
            "birthday", "bluebird", "cheerful", "colorful", "daylight",
            "doghouse", "driveway", "everyone" };

    private final String[] files = { "Adult.java", "Aeroplane.java",
            "Air.java", "Airforce.java", "Airport.java", "Album.java",
            "Alphabet.java", "Apple.java", "Arm.java", "Army.java", "Babby.java" };

    Random rnd = new Random();
    CTFTrace fTrace = null;

    /**
     * main, launches the tests.
     * @param args not read
     */
    public static void main(String[] args) {
        new org.junit.runner.JUnitCore().run(CTFTraceCallsitePerformanceTest.class);
    }


    /**
     * sets up the test by making a new trace.
     * @throws CTFReaderException an exception from the reader
     * @throws SecurityException an exception from accessing files illegally
     * @throws IllegalArgumentException an exception for passing bad values
     */
    @Before
    public void setup() throws CTFReaderException, SecurityException,
            IllegalArgumentException {
        assumeTrue(testTrace.exists());
        fTrace = new CTFTrace(testTrace.getPath());
    }

    private void addCallsites(int numCallsites) {
        long stepSize = (Long.MAX_VALUE / (numCallsites + 1));
        int jitter = (int) Math.min(stepSize, Integer.MAX_VALUE);
        for (int i = 0; i < numCallsites; i++) {
            final long ip = ((i)) * stepSize + rnd.nextInt(jitter);
            fTrace.addCallsite(getRandomElement(callsites),
                    getRandomElement(functions), ip, getRandomElement(files),
                    (ip / 1000000) * 100);
        }
    }

    private String getRandomElement(String[] array) {
        return array[rnd.nextInt(array.length)];
    }

    private long testMain() {
        List<CTFCallsite> l = fTrace.getCallsiteCandidates(callsites[0]);
        CTFCallsite cs = fTrace.getCallsite(1);
        CTFCallsite cs1 = fTrace.getCallsite(callsites[0]);
        CTFCallsite cs2 = fTrace.getCallsite(callsites[0], 1);
        assertNotNull(l);
        assertNotNull(cs);
        assertNotNull(cs1);
        assertNotNull(cs2);
        /* performance test */
        long start = System.nanoTime();
        perfTest();
        long end = System.nanoTime();
        long diff = end - start;
        assertTrue(diff > 0);
        return diff;
    }

    /**
     * @param callsiteSize
     */
    private void test(int callsiteSize) {
        addCallsites(callsiteSize);
        long ns = testMain();
        System.out.println( "perf ( " + callsiteSize + ", " + ns + ")");
    }

    private void perfTest() {
        for (int i = 0; i < NUMBER_OF_SEEKS; i++) {
            fTrace.getCallsite((((long) rnd.nextInt()) << 16L));
        }
    }

    /**
     * Test seeks with 1000 callsites
     */
    @Test
    public void test1KCallsites() {
        test(1000);
    }

    /**
     * Test seeks with 2000 callsites
     */
    @Test
    public void test2KCallsites() {
        test(2000);
    }

    /**
     * Test seeks with 5000 callsites
     */
    @Test
    public void test5KCallsites() {
        test(5000);
    }

    /**
     * Test seeks with 10000 callsites
     */
    @Test
    public void test10KCallsites() {
        test(10000);
    }

    /**
     * Test seeks with 20000 callsites
     */
    @Test
    public void test20KCallsites() {
        test(20000);
    }

    /**
     * Test seeks with 50000 callsites
     */
    @Test
    public void test50KCallsites() {
        test(50000);
    }

    /**
     * Test seeks with 100000 callsites
     */
    @Test
    public void test100KCallsites() {
        test(100000);
    }

    /**
     * Test seeks with 1000000 callsites
     */
    @Test
    public void test1MCallsites() {
        test(1000000);
    }

    /**
     * Test seeks with 2000000 callsites
     */
    @Test
    public void test2MCallsites() {
        test(2000000);
    }
}

