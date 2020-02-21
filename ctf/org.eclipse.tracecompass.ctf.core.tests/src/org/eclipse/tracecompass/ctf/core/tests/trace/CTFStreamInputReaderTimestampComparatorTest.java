/*******************************************************************************
 * Copyright (c) 2013, 2014 Ericsson
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Matthew Khouzam - Initial API and implementation
 *******************************************************************************/

package org.eclipse.tracecompass.ctf.core.tests.trace;

import static org.junit.Assert.assertNotNull;

import org.eclipse.tracecompass.internal.ctf.core.trace.StreamInputReaderTimestampComparator;
import org.junit.Before;
import org.junit.Test;

/**
 * The class <code>StreamInputReaderTimestampComparatorTest</code> contains
 * tests for the class <code>{@link StreamInputReaderTimestampComparator}</code>
 *
 * @author ematkho
 * @version $Revision: 1.0 $
 */
public class CTFStreamInputReaderTimestampComparatorTest {

    private StreamInputReaderTimestampComparator fixture;

    /**
     * Perform pre-test initialization.
     */
    @Before
    public void setUp() {
        fixture = new StreamInputReaderTimestampComparator();
    }

    /**
     * Run the StreamInputReaderTimestampComparator() constructor test.
     */
    @Test
    public void testStreamInputReaderTimestampComparator_1() {
        assertNotNull(fixture);
    }

}
