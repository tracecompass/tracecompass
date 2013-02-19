#!/bin/bash

# If you do not have (or do not feel like setting up) Maven, but you have Antlr3
# installed on your system, you can use this script to generate the Java CTF
# parser files.

SRC=src/main/antlr3/org/eclipse/linuxtools/ctf/parser
DEST=target/generated-sources/antlr3/org/eclipse/linuxtools/ctf/parser

antlr3 $SRC/CTFLexer.g -fo $DEST
antlr3 $SRC/CTFParser.g -fo $DEST
