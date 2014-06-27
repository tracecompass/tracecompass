/*******************************************************************************
 * Copyright (c) 2014 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Vincent Perot - Initial API and implementation
 *******************************************************************************/

package org.eclipse.linuxtools.pcap.core.tests.protocol.tcp;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;

/**
 * TCP test suite
 */
@RunWith(Suite.class)
@Suite.SuiteClasses({
        TCPPacketTest.class
})
public class AllTests {

}
