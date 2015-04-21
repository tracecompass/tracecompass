/*******************************************************************************
 * Copyright (c) 2015 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Matthew Khouzam - Initial API and implementation
 *******************************************************************************/

package org.eclipse.tracecompass.common.core.tests.collect;

import static org.eclipse.tracecompass.common.core.NonNullUtils.nullToEmptyString;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.LinkedList;
import java.util.Random;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import org.eclipse.tracecompass.common.core.NonNullUtils;
import org.eclipse.tracecompass.common.core.collect.BufferedBlockingQueue;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestRule;
import org.junit.rules.Timeout;

/**
 * Test suite for the {@link BufferedBlockingQueue}
 */
public class BufferedBlockingQueueTest {

    /** Timeout the tests after 2 minutes */
    @Rule
    public TestRule timeoutRule = new Timeout(120000);

    private BufferedBlockingQueue<Character> charQueue;

    /**
     * Test setup
     */
    @Before
    public void init() {
        charQueue = new BufferedBlockingQueue<>(15, 15);
    }

    /**
     * Test inserting one element and removing it.
     */
    @Test
    public void testSingleInsertion() {
        Character element = 'x';
        charQueue.put(element);
        charQueue.flushInputBuffer();

        Character out = charQueue.take();
        assertEquals(element, out);
    }

    /**
     * Test insertion of elements that fit into the input buffer.
     */
    @Test
    public void testSimpleInsertion() {
        String string = "Hello world!";
        for (char elem : string.toCharArray()) {
            charQueue.put(elem);
        }
        charQueue.flushInputBuffer();

        StringBuilder sb = new StringBuilder();
        while (!charQueue.isEmpty()) {
            sb.append(charQueue.take());
        }
        assertEquals(string, sb.toString());
    }

    /**
     * Test insertion of elements that will require more than one input buffer.
     */
    @Test
    public void testLargeInsertion() {
        String string = testString.substring(0, 222);
        for (char elem : string.toCharArray()) {
            charQueue.put(elem);
        }
        charQueue.flushInputBuffer();

        StringBuilder sb = new StringBuilder();
        while (!charQueue.isEmpty()) {
            sb.append(charQueue.take());
        }
        assertEquals(string, sb.toString());
    }

    /**
     * Test the state of the {@link BufferedBlockingQueue#isEmpty()} method at
     * various moments.
     */
    @Test
    public void testIsEmpty() {
        BufferedBlockingQueue<String> stringQueue = new BufferedBlockingQueue<>(15, 15);
        assertTrue(stringQueue.isEmpty());

        stringQueue.put("Hello");
        assertFalse(stringQueue.isEmpty());

        stringQueue.flushInputBuffer();
        assertFalse(stringQueue.isEmpty());

        stringQueue.flushInputBuffer();
        assertFalse(stringQueue.isEmpty());

        stringQueue.flushInputBuffer();
        stringQueue.take();
        assertTrue(stringQueue.isEmpty());

        stringQueue.flushInputBuffer();
        assertTrue(stringQueue.isEmpty());
    }

    /**
     * Write random data in and read it, several times.
     */
    @Test
    public void testOddInsertions() {
        BufferedBlockingQueue<Object> objectQueue = new BufferedBlockingQueue<>(15, 15);
        LinkedList<Object> expectedValues = new LinkedList<>();
        Random rnd = new Random();
        rnd.setSeed(123);

        for (int i = 0; i < 10; i++) {
            /*
             * The queue's total size is 225 (15x15). We must make sure to not
             * fill it up here!
             */
            for (int j = 0; j < 50; j++) {
                Integer testInt = NonNullUtils.checkNotNull(rnd.nextInt());
                Long testLong = NonNullUtils.checkNotNull(rnd.nextLong());
                Double testDouble = NonNullUtils.checkNotNull(rnd.nextDouble());
                Double testGaussian = NonNullUtils.checkNotNull(rnd.nextGaussian());

                expectedValues.add(testInt);
                expectedValues.add(testLong);
                expectedValues.add(testDouble);
                expectedValues.add(testGaussian);
                objectQueue.put(testInt);
                objectQueue.put(testLong);
                objectQueue.put(testDouble);
                objectQueue.put(testGaussian);
            }
            objectQueue.flushInputBuffer();

            while (!expectedValues.isEmpty()) {
                Object expected = expectedValues.removeFirst();
                Object actual = objectQueue.take();
                assertEquals(expected, actual);
            }
        }
    }

    /**
     * Read with a producer and a consumer
     *
     * @throws InterruptedException
     *             The test was interrupted
     */
    @Test
    public void testMultiThread() throws InterruptedException {
        /* A character not found in the test string */
        final Character lastElement = '%';

        Thread producer = new Thread() {
            @Override
            public void run() {
                for (char c : testString.toCharArray()) {
                    charQueue.put(c);
                }
                charQueue.put(lastElement);
                charQueue.flushInputBuffer();
            }
        };
        producer.start();

        Thread consumer = new Thread() {
            @Override
            public void run() {
                Character s = charQueue.take();
                while (!s.equals(lastElement)) {
                    s = charQueue.take();
                }
            }
        };
        consumer.start();

        consumer.join();
        producer.join();
    }

    /**
     * Test like multi-threaded with a producer and consumer but now with an
     * inquisitor checking up on the queue. A buffered blocking queue smoke
     * test.
     *
     * @throws InterruptedException
     *             The test was interrupted
     * @throws ExecutionException
     *             If one of the sub-threads throws an exception, which should
     *             not happen
     */
    @Test
    public void testMultiThreadWithInterruptions() throws InterruptedException, ExecutionException {
        final BufferedBlockingQueue<String> isq = new BufferedBlockingQueue<>(15, 15);
        final BufferedBlockingQueue<String> queryQueue = new BufferedBlockingQueue<>(15, 15);

        ExecutorService pool = Executors.newFixedThreadPool(4);

        final String poisonPill = "That's all folks!";
        final String lastElement = "END";

        Runnable producer = new Runnable() {
            @Override
            public void run() {
                for (int i = 0; i < testString.length(); i++) {
                    isq.put(nullToEmptyString(String.valueOf(testString.charAt(i))));
                }
                isq.put(poisonPill);
                isq.flushInputBuffer();
            }
        };

        Callable<String> consumer = new Callable<String>() {
            @Override
            public String call() {
                StringBuilder sb = new StringBuilder();
                String s = null;
                s = isq.take();
                while (!s.equals(poisonPill)) {
                    sb.append(s);
                    s = isq.take();
                }
                return sb.toString();
            }
        };

        Runnable inquisitor = new Runnable() {
            @Override
            public void run() {
                while (!isq.isEmpty()) {
                    /*
                     * The interest of this test is here: we are iterating on
                     * the queue while it is being used.
                     */
                    for (String input : isq) {
                        queryQueue.put(nullToEmptyString(input));
                    }
                }
                queryQueue.put(lastElement);
                queryQueue.flushInputBuffer();
            }
        };

        Callable<Boolean> auditor = new Callable<Boolean>() {
            @Override
            public Boolean call() {
                String val = queryQueue.take();
                while (!val.equals(lastElement)) {
                    if (testString.indexOf(val) == -1) {
                        return true;
                    }
                    val = queryQueue.take();
                }
                return false;
            }
        };

        pool.submit(producer);
        pool.submit(inquisitor);
        Future<String> message = pool.submit(consumer);
        Future<Boolean> fail = pool.submit(auditor);

        pool.shutdown();
        pool.awaitTermination(2, TimeUnit.MINUTES);

        assertEquals(testString, message.get());
        assertFalse(fail.get());
    }

    /**
     * The EPL text is long and I think covered by epl
     */
    private static final String testString = "Eclipse Public License - v 1.0\n"
            +
            "\n"
            +
            "THE ACCOMPANYING PROGRAM IS PROVIDED UNDER THE TERMS OF THIS ECLIPSE PUBLIC LICENSE (\"AGREEMENT\"). ANY USE, REPRODUCTION OR DISTRIBUTION OF THE PROGRAM CONSTITUTES RECIPIENT\'S ACCEPTANCE OF THIS AGREEMENT.\n"
            +
            "\n"
            +
            "1. DEFINITIONS\n"
            +
            "\n"
            +
            "\"Contribution\" means:\n"
            +
            "\n"
            +
            "a) in the case of the initial Contributor, the initial code and documentation distributed under this Agreement, and\n"
            +
            "\n"
            +
            "b) in the case of each subsequent Contributor:\n"
            +
            "\n"
            +
            "i) changes to the Program, and\n"
            +
            "\n"
            +
            "ii) additions to the Program;\n"
            +
            "\n"
            +
            "where such changes and/or additions to the Program originate from and are distributed by that particular Contributor. A Contribution \'originates\' from a Contributor if it was added to the Program by such Contributor itself or anyone acting on such Contributor\'s behalf. Contributions do not include additions to the Program which: (i) are separate modules of software distributed in conjunction with the Program under their own license agreement, and (ii) are not derivative works of the Program.\n"
            +
            "\n"
            +
            "\"Contributor\" means any person or entity that distributes the Program.\n"
            +
            "\n"
            +
            "\"Licensed Patents\" mean patent claims licensable by a Contributor which are necessarily infringed by the use or sale of its Contribution alone or when combined with the Program.\n"
            +
            "\n"
            +
            "\"Program\" means the Contributions distributed in accordance with this Agreement.\n"
            +
            "\n"
            +
            "\"Recipient\" means anyone who receives the Program under this Agreement, including all Contributors.\n"
            +
            "\n"
            +
            "2. GRANT OF RIGHTS\n"
            +
            "\n"
            +
            "a) Subject to the terms of this Agreement, each Contributor hereby grants Recipient a non-exclusive, worldwide, royalty-free copyright license to reproduce, prepare derivative works of, publicly display, publicly perform, distribute and sublicense the Contribution of such Contributor, if any, and such derivative works, in source code and object code form.\n"
            +
            "\n"
            +
            "b) Subject to the terms of this Agreement, each Contributor hereby grants Recipient a non-exclusive, worldwide, royalty-free patent license under Licensed Patents to make, use, sell, offer to sell, import and otherwise transfer the Contribution of such Contributor, if any, in source code and object code form. This patent license shall apply to the combination of the Contribution and the Program if, at the time the Contribution is added by the Contributor, such addition of the Contribution causes such combination to be covered by the Licensed Patents. The patent license shall not apply to any other combinations which include the Contribution. No hardware per se is licensed hereunder.\n"
            +
            "\n"
            +
            "c) Recipient understands that although each Contributor grants the licenses to its Contributions set forth herein, no assurances are provided by any Contributor that the Program does not infringe the patent or other intellectual property rights of any other entity. Each Contributor disclaims any liability to Recipient for claims brought by any other entity based on infringement of intellectual property rights or otherwise. As a condition to exercising the rights and licenses granted hereunder, each Recipient hereby assumes sole responsibility to secure any other intellectual property rights needed, if any. For example, if a third party patent license is required to allow Recipient to distribute the Program, it is Recipient\'s responsibility to acquire that license before distributing the Program.\n"
            +
            "\n"
            +
            "d) Each Contributor represents that to its knowledge it has sufficient copyright rights in its Contribution, if any, to grant the copyright license set forth in this Agreement.\n"
            +
            "\n"
            +
            "3. REQUIREMENTS\n"
            +
            "\n"
            +
            "A Contributor may choose to distribute the Program in object code form under its own license agreement, provided that:\n"
            +
            "\n"
            +
            "a) it complies with the terms and conditions of this Agreement; and\n"
            +
            "\n"
            +
            "b) its license agreement:\n"
            +
            "\n"
            +
            "i) effectively disclaims on behalf of all Contributors all warranties and conditions, express and implied, including warranties or conditions of title and non-infringement, and implied warranties or conditions of merchantability and fitness for a particular purpose;\n"
            +
            "\n"
            +
            "ii) effectively excludes on behalf of all Contributors all liability for damages, including direct, indirect, special, incidental and consequential damages, such as lost profits;\n"
            +
            "\n"
            +
            "iii) states that any provisions which differ from this Agreement are offered by that Contributor alone and not by any other party; and\n"
            +
            "\n"
            +
            "iv) states that source code for the Program is available from such Contributor, and informs licensees how to obtain it in a reasonable manner on or through a medium customarily used for software exchange.\n"
            +
            "\n"
            +
            "When the Program is made available in source code form:\n"
            +
            "\n"
            +
            "a) it must be made available under this Agreement; and\n"
            +
            "\n"
            +
            "b) a copy of this Agreement must be included with each copy of the Program.\n"
            +
            "\n"
            +
            "Contributors may not remove or alter any copyright notices contained within the Program.\n"
            +
            "\n"
            +
            "Each Contributor must identify itself as the originator of its Contribution, if any, in a manner that reasonably allows subsequent Recipients to identify the originator of the Contribution.\n"
            +
            "\n"
            +
            "4. COMMERCIAL DISTRIBUTION\n"
            +
            "\n"
            +
            "Commercial distributors of software may accept certain responsibilities with respect to end users, business partners and the like. While this license is intended to facilitate the commercial use of the Program, the Contributor who includes the Program in a commercial product offering should do so in a manner which does not create potential liability for other Contributors. Therefore, if a Contributor includes the Program in a commercial product offering, such Contributor (\"Commercial Contributor\") hereby agrees to defend and indemnify every other Contributor (\"Indemnified Contributor\") against any losses, damages and costs (collectively \"Losses\") arising from claims, lawsuits and other legal actions brought by a third party against the Indemnified Contributor to the extent caused by the acts or omissions of such Commercial Contributor in connection with its distribution of the Program in a commercial product offering. The obligations in this section do not apply to any claims or Losses relating to any actual or alleged intellectual property infringement. In order to qualify, an Indemnified Contributor must: a) promptly notify the Commercial Contributor in writing of such claim, and b) allow the Commercial Contributor to control, and cooperate with the Commercial Contributor in, the defense and any related settlement negotiations. The Indemnified Contributor may participate in any such claim at its own expense.\n"
            +
            "\n"
            +
            "For example, a Contributor might include the Program in a commercial product offering, Product X. That Contributor is then a Commercial Contributor. If that Commercial Contributor then makes performance claims, or offers warranties related to Product X, those performance claims and warranties are such Commercial Contributor\'s responsibility alone. Under this section, the Commercial Contributor would have to defend claims against the other Contributors related to those performance claims and warranties, and if a court requires any other Contributor to pay any damages as a result, the Commercial Contributor must pay those damages.\n"
            +
            "\n"
            +
            "5. NO WARRANTY\n"
            +
            "\n"
            +
            "EXCEPT AS EXPRESSLY SET FORTH IN THIS AGREEMENT, THE PROGRAM IS PROVIDED ON AN \"AS IS\" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, EITHER EXPRESS OR IMPLIED INCLUDING, WITHOUT LIMITATION, ANY WARRANTIES OR CONDITIONS OF TITLE, NON-INFRINGEMENT, MERCHANTABILITY OR FITNESS FOR A PARTICULAR PURPOSE. Each Recipient is solely responsible for determining the appropriateness of using and distributing the Program and assumes all risks associated with its exercise of rights under this Agreement , including but not limited to the risks and costs of program errors, compliance with applicable laws, damage to or loss of data, programs or equipment, and unavailability or interruption of operations.\n"
            +
            "\n"
            +
            "6. DISCLAIMER OF LIABILITY\n"
            +
            "\n"
            +
            "EXCEPT AS EXPRESSLY SET FORTH IN THIS AGREEMENT, NEITHER RECIPIENT NOR ANY CONTRIBUTORS SHALL HAVE ANY LIABILITY FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING WITHOUT LIMITATION LOST PROFITS), HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OR DISTRIBUTION OF THE PROGRAM OR THE EXERCISE OF ANY RIGHTS GRANTED HEREUNDER, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGES.\n"
            +
            "\n"
            +
            "7. GENERAL\n"
            +
            "\n"
            +
            "If any provision of this Agreement is invalid or unenforceable under applicable law, it shall not affect the validity or enforceability of the remainder of the terms of this Agreement, and without further action by the parties hereto, such provision shall be reformed to the minimum extent necessary to make such provision valid and enforceable.\n"
            +
            "\n"
            +
            "If Recipient institutes patent litigation against any entity (including a cross-claim or counterclaim in a lawsuit) alleging that the Program itself (excluding combinations of the Program with other software or hardware) infringes such Recipient\'s patent(s), then such Recipient\'s rights granted under Section 2(b) shall terminate as of the date such litigation is filed.\n"
            +
            "\n"
            +
            "All Recipient\'s rights under this Agreement shall terminate if it fails to comply with any of the material terms or conditions of this Agreement and does not cure such failure in a reasonable period of time after becoming aware of such noncompliance. If all Recipient\'s rights under this Agreement terminate, Recipient agrees to cease use and distribution of the Program as soon as reasonably practicable. However, Recipient\'s obligations under this Agreement and any licenses granted by Recipient relating to the Program shall continue and survive.\n"
            +
            "\n"
            +
            "Everyone is permitted to copy and distribute copies of this Agreement, but in order to avoid inconsistency the Agreement is copyrighted and may only be modified in the following manner. The Agreement Steward reserves the right to publish new versions (including revisions) of this Agreement from time to time. No one other than the Agreement Steward has the right to modify this Agreement. The Eclipse Foundation is the initial Agreement Steward. The Eclipse Foundation may assign the responsibility to serve as the Agreement Steward to a suitable separate entity. Each new version of the Agreement will be given a distinguishing version number. The Program (including Contributions) may always be distributed subject to the version of the Agreement under which it was received. In addition, after a new version of the Agreement is published, Contributor may elect to distribute the Program (including its Contributions) under the new version. Except as expressly stated in Sections 2(a) and 2(b) above, Recipient receives no rights or licenses to the intellectual property of any Contributor under this Agreement, whether expressly, by implication, estoppel or otherwise. All rights in the Program not expressly granted under this Agreement are reserved.\n"
            +
            "\n"
            +
            "This Agreement is governed by the laws of the State of New York and the intellectual property laws of the United States of America. No party to this Agreement will bring a legal action under this Agreement more than one year after the cause of action arose. Each party waives its rights to a jury trial in any resulting litigation.";

}
