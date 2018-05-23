grammar FilterParser;

options {
    language   = Java;
    output     = AST;
    ASTLabelType = CommonTree;
}

tokens {
  ROOT;
  EXP_NODE;
  EXP_NEG;
  EXP_PAR;
  OPERATION;
  OPERATION1;
  OPERATION2;
  OPERATION3;
  CONSTANT;
}
@header {
/*******************************************************************************
 * Copyright (c) 2018 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
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
    super.reportError(e);
    errListener.error(e);
}
}

@lexer::header {
/*******************************************************************************
 * Copyright (c) 2018 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
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
    super.reportError(e);
    errListener.error(e);
}
}

parse : (expression)+ -> ^(ROOT (expression)+);
expression : left = expr (sep = SEPARATOR right = expr)? -> ^(EXP_NODE $left ($sep $right)?)
           | (OP_NEGATE)(expression) -> ^(EXP_NEG OP_NEGATE expression);

SEPARATOR  :'||' | '&&';

paragraph : TEXT+;

expr       : TEXT OP TEXT -> ^(OPERATION TEXT OP TEXT)
           | TEXT OP_PRESENT -> ^(OPERATION1 TEXT OP_PRESENT)
           | '"' key0 = paragraph '"' op = OP text = TEXT -> ^(OPERATION2 $key0 $op $text)
           | '"' key1 = paragraph '"' op_present = OP_PRESENT -> ^(OPERATION3 $key1 $op_present)
           | '(' expr ')' -> ^(EXP_PAR expr)
           | TEXT  -> ^(CONSTANT TEXT);

OP_PRESENT : 'present';
OP_NEGATE  : '!';
OP         : '==' | '!=' | 'contains' | 'matches' | '>' | '<';
TEXT   : (('a'..'z')|('A'..'Z')|('0'..'9')|'-'|'_'|'['|']'|'.'|'*'|'$'|'^'|'|'|'\\'|'{'|'}')+;

WS         : (' '|'\t'|'\r'|'\n')+ { skip(); } ;