#!/bin/bash
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

# If you do not have (or do not feel like setting up) Maven, but you have Antlr3
# installed on your system, you can use this script to generate the Java CTF
# parser files.

SRC=src/main/antlr3/org/eclipse/linuxtools/ctf/parser
DEST=target/generated-sources/antlr3/org/eclipse/linuxtools/ctf/parser

antlr3 $SRC/CTFLexer.g -fo $DEST
antlr3 $SRC/CTFParser.g -fo $DEST
