#!/bin/bash

antlr3 CTFLexer.g -o ../src/org/eclipse/linuxtools/ctf/parser/
antlr3 CTFParser.g -o ../src/org/eclipse/linuxtools/ctf/parser/
rm ../src/org/eclipse/linuxtools/ctf/parser/*.tokens
