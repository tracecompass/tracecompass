/*******************************************************************************
 * Copyright (c) 2013 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Alexandre Montplaisir - Initial API and implementation
 *******************************************************************************/

package org.eclipse.linuxtools.tmf.core.tests.signal;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.eclipse.linuxtools.tmf.core.component.TmfComponent;
import org.eclipse.linuxtools.tmf.core.signal.TmfSignal;
import org.eclipse.linuxtools.tmf.core.signal.TmfSignalHandler;
import org.eclipse.linuxtools.tmf.core.signal.TmfSignalThrottler;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * Test suite for {@link TmfSignalThrottler}
 *
 * @author Alexandre Montplaisir
 */
public class TmfSignalThrottlerTest {

    private MySender sender;
    private MyListener listener;

    /**
     * Pre-test setup
     */
    @Before
    public void setUp() {
        sender = new MySender();
        listener = new MyListener();
    }

    /**
     * After-test cleanup
     */
    @After
    public void tearDown() {
        sender.dispose();
        listener.dispose();
    }

    // ------------------------------------------------------------------------
    // Test cases
    // ------------------------------------------------------------------------

    /**
     * Test using only one throttler. Only one signal should go through.
     */
    @Test
    public void testOneChannel() {
        final MySignal sig1 = new MySignal(sender, 0);
        final MySignal sig2 = new MySignal(sender, 0);
        final MySignal sig3 = new MySignal(sender, 0);

        synchronized(this) {
            sender.sendSignal(sig1);
            sender.sendSignal(sig2);
            sender.sendSignal(sig3);
        }

        sleep(1000);

        assertEquals(1, listener.nbReceived[0]);
        assertEquals(0, listener.nbReceived[1]);
        assertEquals(0, listener.nbReceived[2]);
    }

    /**
     * Test using multiple throttlers in parrallel. Only one signal per
     * throttler should go through.
     */
    @Test
    public void testMultipleChannels() {
        List<MySignal> signals = new ArrayList<>();
        signals.add(new MySignal(sender, 0));
        signals.add(new MySignal(sender, 0));
        signals.add(new MySignal(sender, 0));

        signals.add(new MySignal(sender, 1));
        signals.add(new MySignal(sender, 1));
        signals.add(new MySignal(sender, 1));

        signals.add(new MySignal(sender, 2));
        signals.add(new MySignal(sender, 2));
        signals.add(new MySignal(sender, 2));

        Collections.shuffle(signals); /* Every day */

        synchronized(this) {
            for (MySignal sig : signals) {
                sender.sendSignal(sig);
            }
        }

        sleep(2000);

        for (int nb : listener.nbReceived) {
            assertEquals(1, nb);
        }
    }

    /**
     * Test with one throttler, sending signals slowly. All three signals should
     * go through.
     */
    @Test
    public void testDelay() {
        final MySignal sig1 = new MySignal(sender, 0);
        final MySignal sig2 = new MySignal(sender, 0);
        final MySignal sig3 = new MySignal(sender, 0);

        sender.sendSignal(sig1);
        sleep(1000);
        sender.sendSignal(sig2);
        sleep(1000);
        sender.sendSignal(sig3);
        sleep(1000);

        assertEquals(3, listener.nbReceived[0]);
    }

    // ------------------------------------------------------------------------
    // Helper methods
    // ------------------------------------------------------------------------

    private static void sleep(long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    // ------------------------------------------------------------------------
    // Helper classes
    // ------------------------------------------------------------------------

    /**
     * Signal sender
     */
    private class MySender extends TmfComponent {

        private final TmfSignalThrottler[] throttlers;

        MySender() {
            super("MySender");
            throttlers = new TmfSignalThrottler[] {
                    new TmfSignalThrottler(this,  200),
                    new TmfSignalThrottler(this,  500),
                    new TmfSignalThrottler(this, 1000),
            };
        }

        void sendSignal(MySignal signal) {
            throttlers[signal.getChannel()].queue(signal);
        }

        @Override
        public void dispose() {
            super.dispose();
            for (TmfSignalThrottler elem : throttlers) {
                elem.dispose();
            }
        }
    }

    /**
     * Signal listener
     */
    public class MyListener extends TmfComponent {

        int[] nbReceived = { 0, 0, 0 };

        /**
         * Constructor. Needs to be public so TmfSignalHandler can see it.
         */
        public MyListener() {
            super("MyListener");
        }

        /**
         * Receive a signal.
         *
         * @param sig
         *            Signal received
         */
        @TmfSignalHandler
        public void receiveSignal(final MySignal sig) {
            nbReceived[sig.getChannel()]++;
        }
    }

    /**
     * Signal object
     */
    private class MySignal extends TmfSignal {

        private final int channel;

        public MySignal(MySender source, int channel) {
            super(source);
            this.channel = channel;
        }

        public int getChannel() {
            return channel;
        }
    }
}
