#!/bin/sh
#This small script will download the traces needed to run some tests.

# Trace used by the CTF plugin unit tests
wget http://lttng.org/files/samples/sample-ctf-trace-20120412.tar.bz2 -O-  | tar xvjf - &&

# Trace used by the lttng2 kernel state provider tests
wget http://www.dorsal.polymtl.ca/~alexmont/data/trace2.tar.bz2 -O- | tar xvjf -
