#!/bin/sh
#*******************************************************************************
# Copyright (c) 2012, 2013 Ericsson
#
# All rights reserved. This program and the accompanying materials
# are made available under the terms of the Eclipse Public License v1.0
# which accompanies this distribution, and is available at
# http://www.eclipse.org/legal/epl-v10.html
#
# Contributors:
#     Alexandre Montplaisir - Initial version
#*******************************************************************************

#This small script will download the traces needed to run some tests.

# Trace used by the CTF plugin unit tests
wget http://lttng.org/files/samples/sample-ctf-trace-20120412.tar.bz2 -O-  | tar xvjf - &&

# Trace used by the lttng2 kernel state provider tests
wget http://www.dorsal.polymtl.ca/~alexmont/data/trace2.tar.bz2 -O- | tar xvjf - &&

# Trace using event contexts
wget http://www.dorsal.polymtl.ca/~alexmont/data/kernel_vm.tar.bz2 -O- | tar xvjf - &&

# Trace with lost events
wget http://www.dorsal.polymtl.ca/~alexmont/data/hello-lost.tar.bz2 -O- | tar xvjf - &&

# CTF test suite, used for testing CTF parser compliance
git clone https://github.com/efficios/ctf-testsuite.git &&

# Trace used by the lttng2 kernel to match packets and synchronize
wget http://www.dorsal.polymtl.ca/~gbastien/traces/synctraces.tar.gz -O- | tar xvzf - &&

# Traces with lttng-ust-cyg-profile (-finstrument-functions) events
wget http://www.dorsal.polymtl.ca/~alexmont/data/cyg-profile.tar.bz2 -O- | tar xvjf -

