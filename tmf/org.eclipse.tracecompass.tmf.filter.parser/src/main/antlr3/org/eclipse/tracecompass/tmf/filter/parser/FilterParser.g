grammar FilterParser;

options {
    language   = Java;
    output     = AST;
    ASTLabelType = CommonTree;
}

tokens {
  LOGICAL;
  ROOT1;
  ROOT2;
  OPERATION;
  OPERATION1;
  OPERATION2;
  OPERATION3;
  OPERATION4;
  OPERATION5;
  CONSTANT;
  PAR_CONSTANT;
}
@header {
/*******************************************************************************
 * Copyright (c) 2018 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License 2.0 which
 * accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Jean-Christian Kouame - Initial API and implementation
 *******************************************************************************/

package org.eclipse.tracecompass.tmf.filter.parser;

import org.eclipse.tracecompass.tmf.filter.parser.error.IErrorListener;
}

@members {
private IErrorListener errListener;

public void setErrorListener(IErrorListener listener) {
    errListener = listener;
}

@Override
public void reportError(RecognitionException e) {
    errListener.error(e);
}
}

@lexer::header {
/*******************************************************************************
 * Copyright (c) 2018 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License 2.0 which
 * accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Jean-Christian Kouame - Initial API and implementation
 *******************************************************************************/

package org.eclipse.tracecompass.tmf.filter.parser;

import org.eclipse.tracecompass.tmf.filter.parser.error.IErrorListener;
}

@lexer::members {
private IErrorListener errListener;

public void setErrorListener(IErrorListener listener) {
    errListener = listener;
}

@Override
public void reportError(RecognitionException e) {
    errListener.error(e);
}
}

parse: (parseRoot)+;

parseRoot: root (SEPARATOR root)* -> ^(LOGICAL root (SEPARATOR root)*);

root : (OP_NEGATE)? start = '(' parseRoot close = ')' -> ^(ROOT1 (OP_NEGATE)? $start parseRoot $close)
      | (OP_NEGATE)? (expression) -> ^(ROOT2 (OP_NEGATE)? expression);

SEPARATOR  :'||' | '&&';

paragraph : TEXT+;

expression :TEXT OP TEXT -> ^(OPERATION TEXT OP TEXT)
           | TEXT OP_PRESENT -> ^(OPERATION1 TEXT OP_PRESENT)
           | '"' key0 = paragraph '"' op = OP text = TEXT -> ^(OPERATION2 $key0 $op $text)
           | '"' key1 = paragraph '"' op_present = OP_PRESENT -> ^(OPERATION3 $key1 $op_present)
           | TEXT OP '"' paragraph '"' -> ^(OPERATION4 TEXT OP paragraph)
           | '"' key3 = paragraph '"' OP '"' key4 = paragraph '"' -> ^(OPERATION5 $key3 OP $key4)
           | TEXT  -> ^(CONSTANT TEXT)
           | '"' paragraph '"' -> ^(PAR_CONSTANT paragraph);

OP_PRESENT : 'present';
OP_NEGATE  : '!';
OP         : '==' | '!=' | 'contains' | 'matches' | '>' | '<';
TEXT   : (('a'..'z')|('A'..'Z')|('0'..'9')|'-'|'_'|'['|']'|'.'|'*'|'$'|'^'|'|'|'\\'|'{'|'}'|'?'|'+'|':'|';')+;

WS         : (' '|'\t'|'\r'|'\n')+ { skip(); } ;
