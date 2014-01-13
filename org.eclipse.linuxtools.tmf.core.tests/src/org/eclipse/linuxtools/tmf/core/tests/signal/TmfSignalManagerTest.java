/*******************************************************************************
 * Copyright (c) 2014 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Bernd Hufmann - Initial API and implementation
 *******************************************************************************/

package org.eclipse.linuxtools.tmf.core.tests.signal;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;

import org.eclipse.linuxtools.tmf.core.component.TmfComponent;
import org.eclipse.linuxtools.tmf.core.signal.TmfEndSynchSignal;
import org.eclipse.linuxtools.tmf.core.signal.TmfSignal;
import org.eclipse.linuxtools.tmf.core.signal.TmfSignalHandler;
import org.eclipse.linuxtools.tmf.core.signal.TmfSignalManager;
import org.eclipse.linuxtools.tmf.core.signal.TmfStartSynchSignal;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * Test suite for {@link TmfSignalManager}
 *
 * @author Bernd Hufmann
 */
public class TmfSignalManagerTest {

    private TestSignalSender signalSender;

    /**
     * Pre-test setup
     */
    @Before
    public void setUp() {
        signalSender = new TestSignalSender();
    }

    /**
     * After-test cleanup
     */
    @After
    public void tearDown() {
        signalSender.dispose();
    }

    // ------------------------------------------------------------------------
    // Test cases
    // ------------------------------------------------------------------------

    /**
     * Test send and receive including synch signals.
     */
    @Test
    public void testSendReceive() {
        final int NB_HANDLERS = 10;
        TestSignalHandler[] signalReceivers = new TestSignalHandler[NB_HANDLERS];
        for (int i = 0; i < NB_HANDLERS; i++) {
            signalReceivers[i] = new TestSignalHandler();
        }
        final TestSignal1 firstSignal = new TestSignal1(signalSender);
        final TestSignal2 secondSignal = new TestSignal2(signalSender);

        final Class<?>[] expectedOrder = new Class[] {
                TmfStartSynchSignal.class, TestSignal1.class, TmfEndSynchSignal.class,
                TmfStartSynchSignal.class, TestSignal2.class, TmfEndSynchSignal.class };

        try {
            signalSender.sendSignal(firstSignal);
            signalSender.sendSignal(secondSignal);

            for (int i = 0; i < NB_HANDLERS; i++) {
                assertEquals(expectedOrder.length, signalReceivers[i].receivedSignals.size());

                for (int k = 0; k < expectedOrder.length; k++) {
                    assertEquals(signalReceivers[i].receivedSignals.get(k).getClass(), expectedOrder[k]);
                }

                for (int k = 0; k < expectedOrder.length; k += 3) {
                    // Verify signal IDs
                    int startSyncId = signalReceivers[i].receivedSignals.get(k).getReference();
                    int signalId = signalReceivers[i].receivedSignals.get(k + 1).getReference();
                    int endSyncId = signalReceivers[i].receivedSignals.get(k + 2).getReference();

                    assertEquals(startSyncId, signalId);
                    assertEquals(startSyncId, endSyncId);
                }
            }
        } finally {
            // Make sure that handlers are disposed in any case (success or not success)
            for (int i = 0; i < NB_HANDLERS; i++) {
                signalReceivers[i].dispose();
            }
        }
    }

    /**
     * Test nesting signals. Verify that they are handled in the same thread.
     */
    @Test
    public void testNestedSignals() {
        TestSignalHandlerNested signalResender = new TestSignalHandlerNested();
        TestSignalHandler signalReceiver = new TestSignalHandler();

        final TestSignal1 mainSignal = new TestSignal1(signalSender);

        final Class<?>[] expectedOrder = new Class[] {
                TmfStartSynchSignal.class,
                    TmfStartSynchSignal.class,
                    TestSignal2.class,
                    TmfEndSynchSignal.class,
                    TmfStartSynchSignal.class,
                        TmfStartSynchSignal.class,
                        TestSignal4.class,
                        TmfEndSynchSignal.class,
                    TestSignal3.class,
                    TmfEndSynchSignal.class,
                TestSignal1.class,
                TmfEndSynchSignal.class
        };

        /*
         *  Index of signals in array signalReceiver.receivedSignals which have
         *  to have the same signal ID.
         */

        final int[] sameSigNoIndex = new int[] {
                0, 10, 11, 1, 2, 3, 4, 8, 9, 5, 6, 7
        };

        try {
            signalSender.sendSignal(mainSignal);

            assertEquals(expectedOrder.length, signalReceiver.receivedSignals.size());

            for (int i = 0; i < expectedOrder.length; i++) {
                assertEquals(signalReceiver.receivedSignals.get(i).getClass(), expectedOrder[i]);
            }

            for (int i = 0; i < sameSigNoIndex.length; i+=3) {
                // Verify signal IDs
                int startSyncId = signalReceiver.receivedSignals.get(sameSigNoIndex[i]).getReference();
                int signalId = signalReceiver.receivedSignals.get(sameSigNoIndex[i + 1]).getReference();
                int endSyncId = signalReceiver.receivedSignals.get(sameSigNoIndex[i + 2]).getReference();
                assertEquals(startSyncId, signalId);
                assertEquals(startSyncId, endSyncId);
            }
        } finally {
            // Make sure that handlers are disposed in any case (success or not success)
            signalResender.dispose();
            signalReceiver.dispose();
        }
    }

    /**
     * Test concurrent signals. Verify that they are handled one after each
     * other.
     */
    @Test
    public void testConcurrentSignals() {

        TestSignalHandlerNested signalResender = new TestSignalHandlerNested();
        TestSignalHandler signalReceiver = new TestSignalHandler(false, null);

        /*
         * Test of synchronization of signal manager.
         *
         * The order of received signals is either set of signals triggered by
         * thread1 before set of signals triggered by thread2 or the other way
         * around.
         *
         * If both received sets were interleaved then the synchronization of
         * the signal manager would be not working.
         */

        final Class<?>[] expectedOrder1 = new Class[] {
                TestSignal2.class, TestSignal4.class, TestSignal3.class, TestSignal1.class, // signals triggered by thread 1
                TestSignal4.class // signal triggered by thread2
        };

        final Class<?>[] expectedOrder2 = new Class[] {
                TestSignal4.class, // signal triggered by thread2
                TestSignal2.class, TestSignal4.class, TestSignal3.class, TestSignal1.class // signals triggered by thread 1
        };

        /*
         *  Run it multiple times so that both expected order are triggered
         */
        try {
            for (int k = 0; k < 10; k++) {
                // Latch to ensure that both threads are started
                final CountDownLatch startLatch = new CountDownLatch(2);
                // Latch to ensure that signals are send roughly at the same time
                final CountDownLatch sendLatch = new CountDownLatch(1);
                // Latch to ensure that both treads are finished
                final CountDownLatch endLatch = new CountDownLatch(2);

                signalReceiver.receivedSignals.clear();

                Thread senderThread1 = new Thread() {
                    @Override
                    public void run() {
                        startLatch.countDown();
                        try {
                            sendLatch.await();
                        } catch (InterruptedException e) {
                        }
                        signalSender.sendSignal(new TestSignal1(signalSender));
                        endLatch.countDown();
                    }
                };

                Thread senderThread2 = new Thread() {
                    @Override
                    public void run() {
                        startLatch.countDown();
                        try {
                            sendLatch.await();
                        } catch (InterruptedException e) {
                        }
                        signalSender.sendSignal(new TestSignal4(signalSender));
                        endLatch.countDown();
                    }
                };

                senderThread1.start();
                senderThread2.start();
                try {
                    startLatch.await();
                } catch (InterruptedException e) {
                }
                sendLatch.countDown();

                try {
                    endLatch.await();
                } catch (InterruptedException e) {
                }

                assertEquals(expectedOrder1.length, signalReceiver.receivedSignals.size());
                boolean pass = true;
                for (int i = 0; i < expectedOrder1.length; i++) {
                    if (!signalReceiver.receivedSignals.get(i).getClass().equals(expectedOrder1[i])) {
                        pass = false;
                        break;
                    }
                }

                if (!pass) {
                    for (int i = 0; i < expectedOrder2.length; i++) {
                        if (!signalReceiver.receivedSignals.get(i).getClass().equals(expectedOrder2[i])) {
                            fail("Concurrent signal test failure!");
                        }
                    }
                }
            }
        } finally {
            // Make sure that handlers are disposed in any case (success or not success)
            signalResender.dispose();
            signalReceiver.dispose();
        }
    }

    /**
     * Test broadcastAsync()
     */
    @Test
    public void testBroadcastAsync() {
        TestSignalHandlerNested signalResender = new TestSignalHandlerNested(false);

        final int NB_HANDLERS = 10;
        final CountDownLatch latch = new CountDownLatch(NB_HANDLERS);
        TestSignalHandler[] signalReceivers = new TestSignalHandler[NB_HANDLERS];
        for (int i = 0; i < NB_HANDLERS; i++) {
            signalReceivers[i] = new TestSignalHandler(false, latch);
        }

        final Class<?>[] expectedOrder = new Class[] {
                TestSignal1.class, TestSignal2.class, TestSignal3.class, TestSignal4.class
        };

        try {
            final TestSignal1 mainSignal = new TestSignal1(signalSender);
            signalSender.sendSignal(mainSignal);

            try {
                latch.await();
            } catch (InterruptedException e) {
            }

            for (int i = 0; i < NB_HANDLERS; i++) {
                assertEquals(expectedOrder.length, signalReceivers[i].receivedSignals.size());
                for (int k = 0; k < expectedOrder.length; k++) {
                    assertEquals(signalReceivers[i].receivedSignals.get(k).getClass(), expectedOrder[k]);
                }
            }
        } finally {
            // Make sure that handlers are disposed in any case (success or not success)
            for (int i = 0; i < NB_HANDLERS; i++) {
                signalReceivers[i].dispose();
            }
            signalResender.dispose();
        }
    }

    // ------------------------------------------------------------------------
    // Helper classes
    // ------------------------------------------------------------------------

    /**
     * Signal sender
     */
    private class TestSignalSender extends TmfComponent {

        TestSignalSender() {
            super("TestSignalSender");
        }

        /**
         * Send a signal
         *
         * @param signal
         *            main signal to send
         */
        public void sendSignal(TmfSignal signal) {
            broadcast(signal);
        }
    }

    /**
     * Signal handler implementation for testing nested signals.
     * Needs to be public so TmfSignalManager can see it.
     */
    public class TestSignalHandlerNested extends AbstractBaseSignalHandler {

        private boolean sync;

        /**
         * Constructor
         */
        private TestSignalHandlerNested() {
            this(true);
        }

        /**
         * Constructor
         *
         * @param sync
         *            log sync signals
         *
         */
        private TestSignalHandlerNested(boolean sync) {
            super("TestSignalHandlerNested", false);
            this.sync = sync;
            TmfSignalManager.deregister(this);
            TmfSignalManager.registerVIP(this);
        }

        /**
         * Receive a signal of type TestSignal1.
         *
         * @param signal
         *            Signal received
         */
        @TmfSignalHandler
        public void receiveSignal1(final TestSignal1 signal) {
            if (sync) {
                broadcast(new TestSignal2(signal.getSource()));
                broadcast(new TestSignal3(signal.getSource()));
            } else {
                broadcastAsync(new TestSignal2(signal.getSource()));
                broadcastAsync(new TestSignal3(signal.getSource()));
            }
        }

        /**
         * Receive a signal of type TestSignal3.
         *
         * @param signal
         *            Signal received
         */
        @TmfSignalHandler
        public void receiveSignal3(final TestSignal3 signal) {
            if (sync) {
                broadcast(new TestSignal4(signal.getSource()));
            } else {
                broadcastAsync(new TestSignal4(signal.getSource()));
            }
        }
    }

    /**
     * Signal handler implementation for testing of sending and receiving
     * signals.
     */
    public class TestSignalHandler extends AbstractBaseSignalHandler {

        private CountDownLatch latch;

        /**
         * Constructor
         *
         */
        private TestSignalHandler() {
            this(true, null);
        }

        /**
         * Constructor
         *
         * @param logSyncSigs
         *            log sync signals
         * @param latch
         *            latch to count down when receiving last signal
         *            (TmfSingal4)
         */
        private TestSignalHandler(boolean logSyncSigs, CountDownLatch latch) {
            super("TestSignalHandler", logSyncSigs);
            this.latch = latch;
        }

        /**
         * Receive a signal of type TestSignal1.
         *
         * @param signal
         *            Signal received
         */
        @TmfSignalHandler
        public void receiveSignal1(final TestSignal1 signal) {
            receivedSignals.add(signal);
        }

        /**
         * Receive a signal of type TestSignal2.
         *
         * @param signal
         *            Signal received
         */
        @TmfSignalHandler
        public void receiveSignal2(final TestSignal2 signal) {
            receivedSignals.add(signal);
        }

        /**
         * Receive a signal of type TestSignal3.
         *
         * @param signal
         *            Signal received
         */
        @TmfSignalHandler
        public void receiveSignal3(final TestSignal3 signal) {
            receivedSignals.add(signal);
        }

        /**
         * Receive a signal of type TestSignal4.
         *
         * @param signal
         *            Signal received
         */
        @TmfSignalHandler
        public void receiveSignal4(final TestSignal4 signal) {
            receivedSignals.add(signal);
            if (latch != null) {
                latch.countDown();
            }
        }
    }

    /**
     * Base signal handler for start and end sync signals.
     */
    public abstract class AbstractBaseSignalHandler extends TmfComponent {
        List<TmfSignal> receivedSignals = new ArrayList<>();
        private boolean logSyncSigs;

        private AbstractBaseSignalHandler(String name, boolean logSyncSignal) {
            super(name);
            this.logSyncSigs = logSyncSignal;
        }

        /**
         * Receive a signal of type TmfStartSynchSignal.
         *
         * @param signal
         *            Signal received
         */
        @TmfSignalHandler
        public void receiveStartSynch(final TmfStartSynchSignal signal) {
            if (logSyncSigs) {
                receivedSignals.add(signal);
            }
        }

        /**
         * Receive a signal of type TmfEndSynchSignal.
         *
         * @param signal
         *            Signal received
         */
        @TmfSignalHandler
        public void receiveEndSynch(final TmfEndSynchSignal signal) {
            if (logSyncSigs) {
                receivedSignals.add(signal);
            }
        }
    }


    /**
     * Test Signal object
     */
    private class TestSignal1 extends TmfSignal {

        public TestSignal1(Object source) {
            super(source);
        }
    }

    /**
     * Test Signal object
     */
    private class TestSignal2 extends TmfSignal {

        public TestSignal2(Object source) {
            super(source);
        }
    }

    /**
     * Test Signal object
     */
    private class TestSignal3 extends TmfSignal {

        public TestSignal3(Object source) {
            super(source);
        }
    }

    /**
     * Test Signal object
     */
    private class TestSignal4 extends TmfSignal {

        public TestSignal4(Object source) {
            super(source);
        }
    }
}
