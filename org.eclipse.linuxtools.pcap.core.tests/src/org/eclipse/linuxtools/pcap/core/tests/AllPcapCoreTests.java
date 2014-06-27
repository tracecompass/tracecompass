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

package org.eclipse.linuxtools.pcap.core.tests;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;

/**
 * Master test suite
 */
@RunWith(Suite.class)
@Suite.SuiteClasses({
        org.eclipse.linuxtools.pcap.core.tests.file.AllTests.class,
        org.eclipse.linuxtools.pcap.core.tests.packet.AllTests.class,
        org.eclipse.linuxtools.pcap.core.tests.protocol.AllTests.class,
        org.eclipse.linuxtools.pcap.core.tests.protocol.ethernet2.AllTests.class,
        org.eclipse.linuxtools.pcap.core.tests.protocol.ipv4.AllTests.class,
        org.eclipse.linuxtools.pcap.core.tests.protocol.pcap.AllTests.class,
        org.eclipse.linuxtools.pcap.core.tests.protocol.tcp.AllTests.class,
        org.eclipse.linuxtools.pcap.core.tests.protocol.udp.AllTests.class,
        org.eclipse.linuxtools.pcap.core.tests.protocol.unknown.AllTests.class,
        org.eclipse.linuxtools.pcap.core.tests.stream.AllTests.class
})
public class AllPcapCoreTests {

}
