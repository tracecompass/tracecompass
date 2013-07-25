/*******************************************************************************
 * Copyright (c) 2013 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Francois Chouinard - Initial API and implementation
 *   Alexandre Montplaisir - Port to JUnit4
 *******************************************************************************/

package org.eclipse.linuxtools.tmf.core.tests.request;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;

/**
 * Requests tests
 */
@RunWith(Suite.class)
@Suite.SuiteClasses({
    TmfCoalescedDataRequestTest.class,
    TmfCoalescedEventRequestTest.class,
    TmfDataRequestTest.class,
    TmfEventRequestTest.class,
    TmfSchedulerTest.class
})
public class AllTests {

}
