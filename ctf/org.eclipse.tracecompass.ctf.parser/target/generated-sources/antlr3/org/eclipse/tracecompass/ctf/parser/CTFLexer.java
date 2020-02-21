// $ANTLR 3.5.2 org/eclipse/tracecompass/ctf/parser/CTFLexer.g 2015-07-09 14:10:51

/*******************************************************************************
 * Copyright (c) 2010, 2015 Ericsson, Ecole Polytechnique de Montréal and others
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License 2.0 which
 * accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Matthew Khouzam - Initial API and implementation
 *   Simon Marchi - Initial API and implementation
 *   Etienne Bergeron - Update to Antlr 3.5 syntax
 *******************************************************************************/

package org.eclipse.tracecompass.ctf.parser;


import org.antlr.runtime.BaseRecognizer;
import org.antlr.runtime.CharStream;
import org.antlr.runtime.DFA;
import org.antlr.runtime.EarlyExitException;
import org.antlr.runtime.Lexer;
import org.antlr.runtime.MismatchedSetException;
import org.antlr.runtime.NoViableAltException;
import org.antlr.runtime.RecognitionException;
import org.antlr.runtime.RecognizerSharedState;

@SuppressWarnings("all")
public class CTFLexer extends Lexer {
	public static final int EOF=-1;
	public static final int ALIGNTOK=4;
	public static final int ARROW=5;
	public static final int ASSIGNMENT=6;
	public static final int BACKSLASH=7;
	public static final int BOOLTOK=8;
	public static final int CALLSITETOK=9;
	public static final int CHARACTER_LITERAL=10;
	public static final int CHARTOK=11;
	public static final int CHAR_CONTENT=12;
	public static final int CLOCKTOK=13;
	public static final int CLOSEBRAC=14;
	public static final int COLON=15;
	public static final int COMMENT=16;
	public static final int COMMENT_CLOSE=17;
	public static final int COMMENT_OPEN=18;
	public static final int COMPLEXTOK=19;
	public static final int CONSTTOK=20;
	public static final int DECIMAL_LITERAL=21;
	public static final int DIGIT=22;
	public static final int DOT=23;
	public static final int DOUBLEQUOTE=24;
	public static final int DOUBLETOK=25;
	public static final int ELIPSES=26;
	public static final int ENUMTOK=27;
	public static final int ENVTOK=28;
	public static final int ESCAPE_SEQUENCE=29;
	public static final int EVENTTOK=30;
	public static final int FLOATINGPOINTTOK=31;
	public static final int FLOATTOK=32;
	public static final int GT=33;
	public static final int HEXADECIMAL_ESCAPE=34;
	public static final int HEX_DIGIT=35;
	public static final int HEX_LITERAL=36;
	public static final int HEX_PREFIX=37;
	public static final int IDENTIFIER=38;
	public static final int IMAGINARYTOK=39;
	public static final int INFINITYTOK=40;
	public static final int INTEGERTOK=41;
	public static final int INTEGER_TYPES_SUFFIX=42;
	public static final int INTTOK=43;
	public static final int LCURL=44;
	public static final int LINE_COMMENT=45;
	public static final int LONGTOK=46;
	public static final int LPAREN=47;
	public static final int LT=48;
	public static final int NANNUMBERTOK=49;
	public static final int NINFINITYTOK=50;
	public static final int NONDIGIT=51;
	public static final int NONZERO_DIGIT=52;
	public static final int OCTAL_ESCAPE=53;
	public static final int OCTAL_LITERAL=54;
	public static final int OCT_DIGIT=55;
	public static final int OCT_PREFIX=56;
	public static final int OPENBRAC=57;
	public static final int POINTER=58;
	public static final int RCURL=59;
	public static final int RPAREN=60;
	public static final int SEPARATOR=61;
	public static final int SHORTTOK=62;
	public static final int SIGN=63;
	public static final int SIGNEDTOK=64;
	public static final int SINGLEQUOTE=65;
	public static final int STREAMTOK=66;
	public static final int STRINGPREFIX=67;
	public static final int STRINGTOK=68;
	public static final int STRING_CONTENT=69;
	public static final int STRING_LITERAL=70;
	public static final int STRUCTTOK=71;
	public static final int TERM=72;
	public static final int TRACETOK=73;
	public static final int TYPEALIASTOK=74;
	public static final int TYPEDEFTOK=75;
	public static final int TYPE_ASSIGNMENT=76;
	public static final int UNICODE_ESCAPE=77;
	public static final int UNSIGNEDTOK=78;
	public static final int VARIANTTOK=79;
	public static final int VOIDTOK=80;
	public static final int WS=81;

	// delegates
	// delegators
	public Lexer[] getDelegates() {
		return new Lexer[] {};
	}

	public CTFLexer() {} 
	public CTFLexer(CharStream input) {
		this(input, new RecognizerSharedState());
	}
	public CTFLexer(CharStream input, RecognizerSharedState state) {
		super(input,state);
	}
	@Override public String getGrammarFileName() { return "org/eclipse/tracecompass/ctf/parser/CTFLexer.g"; }

	// $ANTLR start "ALIGNTOK"
	public final void mALIGNTOK() throws RecognitionException {
		try {
			int _type = ALIGNTOK;
			int _channel = DEFAULT_TOKEN_CHANNEL;
			// org/eclipse/tracecompass/ctf/parser/CTFLexer.g:32:18: ( 'align' )
			// org/eclipse/tracecompass/ctf/parser/CTFLexer.g:32:20: 'align'
			{
			match("align"); 

			}

			state.type = _type;
			state.channel = _channel;
		}
		finally {
			// do for sure before leaving
		}
	}
	// $ANTLR end "ALIGNTOK"

	// $ANTLR start "CONSTTOK"
	public final void mCONSTTOK() throws RecognitionException {
		try {
			int _type = CONSTTOK;
			int _channel = DEFAULT_TOKEN_CHANNEL;
			// org/eclipse/tracecompass/ctf/parser/CTFLexer.g:33:18: ( 'const' )
			// org/eclipse/tracecompass/ctf/parser/CTFLexer.g:33:20: 'const'
			{
			match("const"); 

			}

			state.type = _type;
			state.channel = _channel;
		}
		finally {
			// do for sure before leaving
		}
	}
	// $ANTLR end "CONSTTOK"

	// $ANTLR start "CHARTOK"
	public final void mCHARTOK() throws RecognitionException {
		try {
			int _type = CHARTOK;
			int _channel = DEFAULT_TOKEN_CHANNEL;
			// org/eclipse/tracecompass/ctf/parser/CTFLexer.g:34:18: ( 'char' )
			// org/eclipse/tracecompass/ctf/parser/CTFLexer.g:34:20: 'char'
			{
			match("char"); 

			}

			state.type = _type;
			state.channel = _channel;
		}
		finally {
			// do for sure before leaving
		}
	}
	// $ANTLR end "CHARTOK"

	// $ANTLR start "DOUBLETOK"
	public final void mDOUBLETOK() throws RecognitionException {
		try {
			int _type = DOUBLETOK;
			int _channel = DEFAULT_TOKEN_CHANNEL;
			// org/eclipse/tracecompass/ctf/parser/CTFLexer.g:35:18: ( 'double' )
			// org/eclipse/tracecompass/ctf/parser/CTFLexer.g:35:20: 'double'
			{
			match("double"); 

			}

			state.type = _type;
			state.channel = _channel;
		}
		finally {
			// do for sure before leaving
		}
	}
	// $ANTLR end "DOUBLETOK"

	// $ANTLR start "ENUMTOK"
	public final void mENUMTOK() throws RecognitionException {
		try {
			int _type = ENUMTOK;
			int _channel = DEFAULT_TOKEN_CHANNEL;
			// org/eclipse/tracecompass/ctf/parser/CTFLexer.g:36:18: ( 'enum' )
			// org/eclipse/tracecompass/ctf/parser/CTFLexer.g:36:20: 'enum'
			{
			match("enum"); 

			}

			state.type = _type;
			state.channel = _channel;
		}
		finally {
			// do for sure before leaving
		}
	}
	// $ANTLR end "ENUMTOK"

	// $ANTLR start "EVENTTOK"
	public final void mEVENTTOK() throws RecognitionException {
		try {
			int _type = EVENTTOK;
			int _channel = DEFAULT_TOKEN_CHANNEL;
			// org/eclipse/tracecompass/ctf/parser/CTFLexer.g:37:18: ( 'event' )
			// org/eclipse/tracecompass/ctf/parser/CTFLexer.g:37:20: 'event'
			{
			match("event"); 

			}

			state.type = _type;
			state.channel = _channel;
		}
		finally {
			// do for sure before leaving
		}
	}
	// $ANTLR end "EVENTTOK"

	// $ANTLR start "FLOATINGPOINTTOK"
	public final void mFLOATINGPOINTTOK() throws RecognitionException {
		try {
			int _type = FLOATINGPOINTTOK;
			int _channel = DEFAULT_TOKEN_CHANNEL;
			// org/eclipse/tracecompass/ctf/parser/CTFLexer.g:38:18: ( 'floating_point' )
			// org/eclipse/tracecompass/ctf/parser/CTFLexer.g:38:20: 'floating_point'
			{
			match("floating_point"); 

			}

			state.type = _type;
			state.channel = _channel;
		}
		finally {
			// do for sure before leaving
		}
	}
	// $ANTLR end "FLOATINGPOINTTOK"

	// $ANTLR start "FLOATTOK"
	public final void mFLOATTOK() throws RecognitionException {
		try {
			int _type = FLOATTOK;
			int _channel = DEFAULT_TOKEN_CHANNEL;
			// org/eclipse/tracecompass/ctf/parser/CTFLexer.g:39:18: ( 'float' )
			// org/eclipse/tracecompass/ctf/parser/CTFLexer.g:39:20: 'float'
			{
			match("float"); 

			}

			state.type = _type;
			state.channel = _channel;
		}
		finally {
			// do for sure before leaving
		}
	}
	// $ANTLR end "FLOATTOK"

	// $ANTLR start "INTEGERTOK"
	public final void mINTEGERTOK() throws RecognitionException {
		try {
			int _type = INTEGERTOK;
			int _channel = DEFAULT_TOKEN_CHANNEL;
			// org/eclipse/tracecompass/ctf/parser/CTFLexer.g:40:18: ( 'integer' )
			// org/eclipse/tracecompass/ctf/parser/CTFLexer.g:40:20: 'integer'
			{
			match("integer"); 

			}

			state.type = _type;
			state.channel = _channel;
		}
		finally {
			// do for sure before leaving
		}
	}
	// $ANTLR end "INTEGERTOK"

	// $ANTLR start "INTTOK"
	public final void mINTTOK() throws RecognitionException {
		try {
			int _type = INTTOK;
			int _channel = DEFAULT_TOKEN_CHANNEL;
			// org/eclipse/tracecompass/ctf/parser/CTFLexer.g:41:18: ( 'int' )
			// org/eclipse/tracecompass/ctf/parser/CTFLexer.g:41:20: 'int'
			{
			match("int"); 

			}

			state.type = _type;
			state.channel = _channel;
		}
		finally {
			// do for sure before leaving
		}
	}
	// $ANTLR end "INTTOK"

	// $ANTLR start "LONGTOK"
	public final void mLONGTOK() throws RecognitionException {
		try {
			int _type = LONGTOK;
			int _channel = DEFAULT_TOKEN_CHANNEL;
			// org/eclipse/tracecompass/ctf/parser/CTFLexer.g:42:18: ( 'long' )
			// org/eclipse/tracecompass/ctf/parser/CTFLexer.g:42:20: 'long'
			{
			match("long"); 

			}

			state.type = _type;
			state.channel = _channel;
		}
		finally {
			// do for sure before leaving
		}
	}
	// $ANTLR end "LONGTOK"

	// $ANTLR start "SHORTTOK"
	public final void mSHORTTOK() throws RecognitionException {
		try {
			int _type = SHORTTOK;
			int _channel = DEFAULT_TOKEN_CHANNEL;
			// org/eclipse/tracecompass/ctf/parser/CTFLexer.g:43:18: ( 'short' )
			// org/eclipse/tracecompass/ctf/parser/CTFLexer.g:43:20: 'short'
			{
			match("short"); 

			}

			state.type = _type;
			state.channel = _channel;
		}
		finally {
			// do for sure before leaving
		}
	}
	// $ANTLR end "SHORTTOK"

	// $ANTLR start "SIGNEDTOK"
	public final void mSIGNEDTOK() throws RecognitionException {
		try {
			int _type = SIGNEDTOK;
			int _channel = DEFAULT_TOKEN_CHANNEL;
			// org/eclipse/tracecompass/ctf/parser/CTFLexer.g:44:18: ( 'signed' )
			// org/eclipse/tracecompass/ctf/parser/CTFLexer.g:44:20: 'signed'
			{
			match("signed"); 

			}

			state.type = _type;
			state.channel = _channel;
		}
		finally {
			// do for sure before leaving
		}
	}
	// $ANTLR end "SIGNEDTOK"

	// $ANTLR start "STREAMTOK"
	public final void mSTREAMTOK() throws RecognitionException {
		try {
			int _type = STREAMTOK;
			int _channel = DEFAULT_TOKEN_CHANNEL;
			// org/eclipse/tracecompass/ctf/parser/CTFLexer.g:45:18: ( 'stream' )
			// org/eclipse/tracecompass/ctf/parser/CTFLexer.g:45:20: 'stream'
			{
			match("stream"); 

			}

			state.type = _type;
			state.channel = _channel;
		}
		finally {
			// do for sure before leaving
		}
	}
	// $ANTLR end "STREAMTOK"

	// $ANTLR start "STRINGTOK"
	public final void mSTRINGTOK() throws RecognitionException {
		try {
			int _type = STRINGTOK;
			int _channel = DEFAULT_TOKEN_CHANNEL;
			// org/eclipse/tracecompass/ctf/parser/CTFLexer.g:46:18: ( 'string' )
			// org/eclipse/tracecompass/ctf/parser/CTFLexer.g:46:20: 'string'
			{
			match("string"); 

			}

			state.type = _type;
			state.channel = _channel;
		}
		finally {
			// do for sure before leaving
		}
	}
	// $ANTLR end "STRINGTOK"

	// $ANTLR start "STRUCTTOK"
	public final void mSTRUCTTOK() throws RecognitionException {
		try {
			int _type = STRUCTTOK;
			int _channel = DEFAULT_TOKEN_CHANNEL;
			// org/eclipse/tracecompass/ctf/parser/CTFLexer.g:47:18: ( 'struct' )
			// org/eclipse/tracecompass/ctf/parser/CTFLexer.g:47:20: 'struct'
			{
			match("struct"); 

			}

			state.type = _type;
			state.channel = _channel;
		}
		finally {
			// do for sure before leaving
		}
	}
	// $ANTLR end "STRUCTTOK"

	// $ANTLR start "TRACETOK"
	public final void mTRACETOK() throws RecognitionException {
		try {
			int _type = TRACETOK;
			int _channel = DEFAULT_TOKEN_CHANNEL;
			// org/eclipse/tracecompass/ctf/parser/CTFLexer.g:48:18: ( 'trace' )
			// org/eclipse/tracecompass/ctf/parser/CTFLexer.g:48:20: 'trace'
			{
			match("trace"); 

			}

			state.type = _type;
			state.channel = _channel;
		}
		finally {
			// do for sure before leaving
		}
	}
	// $ANTLR end "TRACETOK"

	// $ANTLR start "TYPEALIASTOK"
	public final void mTYPEALIASTOK() throws RecognitionException {
		try {
			int _type = TYPEALIASTOK;
			int _channel = DEFAULT_TOKEN_CHANNEL;
			// org/eclipse/tracecompass/ctf/parser/CTFLexer.g:49:18: ( 'typealias' )
			// org/eclipse/tracecompass/ctf/parser/CTFLexer.g:49:20: 'typealias'
			{
			match("typealias"); 

			}

			state.type = _type;
			state.channel = _channel;
		}
		finally {
			// do for sure before leaving
		}
	}
	// $ANTLR end "TYPEALIASTOK"

	// $ANTLR start "TYPEDEFTOK"
	public final void mTYPEDEFTOK() throws RecognitionException {
		try {
			int _type = TYPEDEFTOK;
			int _channel = DEFAULT_TOKEN_CHANNEL;
			// org/eclipse/tracecompass/ctf/parser/CTFLexer.g:50:18: ( 'typedef' )
			// org/eclipse/tracecompass/ctf/parser/CTFLexer.g:50:20: 'typedef'
			{
			match("typedef"); 

			}

			state.type = _type;
			state.channel = _channel;
		}
		finally {
			// do for sure before leaving
		}
	}
	// $ANTLR end "TYPEDEFTOK"

	// $ANTLR start "UNSIGNEDTOK"
	public final void mUNSIGNEDTOK() throws RecognitionException {
		try {
			int _type = UNSIGNEDTOK;
			int _channel = DEFAULT_TOKEN_CHANNEL;
			// org/eclipse/tracecompass/ctf/parser/CTFLexer.g:51:18: ( 'unsigned' )
			// org/eclipse/tracecompass/ctf/parser/CTFLexer.g:51:20: 'unsigned'
			{
			match("unsigned"); 

			}

			state.type = _type;
			state.channel = _channel;
		}
		finally {
			// do for sure before leaving
		}
	}
	// $ANTLR end "UNSIGNEDTOK"

	// $ANTLR start "VARIANTTOK"
	public final void mVARIANTTOK() throws RecognitionException {
		try {
			int _type = VARIANTTOK;
			int _channel = DEFAULT_TOKEN_CHANNEL;
			// org/eclipse/tracecompass/ctf/parser/CTFLexer.g:52:18: ( 'variant' )
			// org/eclipse/tracecompass/ctf/parser/CTFLexer.g:52:20: 'variant'
			{
			match("variant"); 

			}

			state.type = _type;
			state.channel = _channel;
		}
		finally {
			// do for sure before leaving
		}
	}
	// $ANTLR end "VARIANTTOK"

	// $ANTLR start "VOIDTOK"
	public final void mVOIDTOK() throws RecognitionException {
		try {
			int _type = VOIDTOK;
			int _channel = DEFAULT_TOKEN_CHANNEL;
			// org/eclipse/tracecompass/ctf/parser/CTFLexer.g:53:18: ( 'void' )
			// org/eclipse/tracecompass/ctf/parser/CTFLexer.g:53:20: 'void'
			{
			match("void"); 

			}

			state.type = _type;
			state.channel = _channel;
		}
		finally {
			// do for sure before leaving
		}
	}
	// $ANTLR end "VOIDTOK"

	// $ANTLR start "BOOLTOK"
	public final void mBOOLTOK() throws RecognitionException {
		try {
			int _type = BOOLTOK;
			int _channel = DEFAULT_TOKEN_CHANNEL;
			// org/eclipse/tracecompass/ctf/parser/CTFLexer.g:54:18: ( '_Bool' )
			// org/eclipse/tracecompass/ctf/parser/CTFLexer.g:54:20: '_Bool'
			{
			match("_Bool"); 

			}

			state.type = _type;
			state.channel = _channel;
		}
		finally {
			// do for sure before leaving
		}
	}
	// $ANTLR end "BOOLTOK"

	// $ANTLR start "COMPLEXTOK"
	public final void mCOMPLEXTOK() throws RecognitionException {
		try {
			int _type = COMPLEXTOK;
			int _channel = DEFAULT_TOKEN_CHANNEL;
			// org/eclipse/tracecompass/ctf/parser/CTFLexer.g:55:18: ( '_Complex' )
			// org/eclipse/tracecompass/ctf/parser/CTFLexer.g:55:20: '_Complex'
			{
			match("_Complex"); 

			}

			state.type = _type;
			state.channel = _channel;
		}
		finally {
			// do for sure before leaving
		}
	}
	// $ANTLR end "COMPLEXTOK"

	// $ANTLR start "IMAGINARYTOK"
	public final void mIMAGINARYTOK() throws RecognitionException {
		try {
			int _type = IMAGINARYTOK;
			int _channel = DEFAULT_TOKEN_CHANNEL;
			// org/eclipse/tracecompass/ctf/parser/CTFLexer.g:56:18: ( '_Imaginary' )
			// org/eclipse/tracecompass/ctf/parser/CTFLexer.g:56:20: '_Imaginary'
			{
			match("_Imaginary"); 

			}

			state.type = _type;
			state.channel = _channel;
		}
		finally {
			// do for sure before leaving
		}
	}
	// $ANTLR end "IMAGINARYTOK"

	// $ANTLR start "ENVTOK"
	public final void mENVTOK() throws RecognitionException {
		try {
			int _type = ENVTOK;
			int _channel = DEFAULT_TOKEN_CHANNEL;
			// org/eclipse/tracecompass/ctf/parser/CTFLexer.g:57:18: ( 'env' )
			// org/eclipse/tracecompass/ctf/parser/CTFLexer.g:57:20: 'env'
			{
			match("env"); 

			}

			state.type = _type;
			state.channel = _channel;
		}
		finally {
			// do for sure before leaving
		}
	}
	// $ANTLR end "ENVTOK"

	// $ANTLR start "CLOCKTOK"
	public final void mCLOCKTOK() throws RecognitionException {
		try {
			int _type = CLOCKTOK;
			int _channel = DEFAULT_TOKEN_CHANNEL;
			// org/eclipse/tracecompass/ctf/parser/CTFLexer.g:58:18: ( 'clock' )
			// org/eclipse/tracecompass/ctf/parser/CTFLexer.g:58:20: 'clock'
			{
			match("clock"); 

			}

			state.type = _type;
			state.channel = _channel;
		}
		finally {
			// do for sure before leaving
		}
	}
	// $ANTLR end "CLOCKTOK"

	// $ANTLR start "CALLSITETOK"
	public final void mCALLSITETOK() throws RecognitionException {
		try {
			int _type = CALLSITETOK;
			int _channel = DEFAULT_TOKEN_CHANNEL;
			// org/eclipse/tracecompass/ctf/parser/CTFLexer.g:62:18: ( 'callsite' )
			// org/eclipse/tracecompass/ctf/parser/CTFLexer.g:62:20: 'callsite'
			{
			match("callsite"); 

			}

			state.type = _type;
			state.channel = _channel;
		}
		finally {
			// do for sure before leaving
		}
	}
	// $ANTLR end "CALLSITETOK"

	// $ANTLR start "NANNUMBERTOK"
	public final void mNANNUMBERTOK() throws RecognitionException {
		try {
			int _type = NANNUMBERTOK;
			int _channel = DEFAULT_TOKEN_CHANNEL;
			// org/eclipse/tracecompass/ctf/parser/CTFLexer.g:69:15: ( 'NaN' )
			// org/eclipse/tracecompass/ctf/parser/CTFLexer.g:69:17: 'NaN'
			{
			match("NaN"); 

			}

			state.type = _type;
			state.channel = _channel;
		}
		finally {
			// do for sure before leaving
		}
	}
	// $ANTLR end "NANNUMBERTOK"

	// $ANTLR start "INFINITYTOK"
	public final void mINFINITYTOK() throws RecognitionException {
		try {
			int _type = INFINITYTOK;
			int _channel = DEFAULT_TOKEN_CHANNEL;
			// org/eclipse/tracecompass/ctf/parser/CTFLexer.g:70:15: ( '+inf' )
			// org/eclipse/tracecompass/ctf/parser/CTFLexer.g:70:17: '+inf'
			{
			match("+inf"); 

			}

			state.type = _type;
			state.channel = _channel;
		}
		finally {
			// do for sure before leaving
		}
	}
	// $ANTLR end "INFINITYTOK"

	// $ANTLR start "NINFINITYTOK"
	public final void mNINFINITYTOK() throws RecognitionException {
		try {
			int _type = NINFINITYTOK;
			int _channel = DEFAULT_TOKEN_CHANNEL;
			// org/eclipse/tracecompass/ctf/parser/CTFLexer.g:71:15: ( '-inf' )
			// org/eclipse/tracecompass/ctf/parser/CTFLexer.g:71:17: '-inf'
			{
			match("-inf"); 

			}

			state.type = _type;
			state.channel = _channel;
		}
		finally {
			// do for sure before leaving
		}
	}
	// $ANTLR end "NINFINITYTOK"

	// $ANTLR start "SEPARATOR"
	public final void mSEPARATOR() throws RecognitionException {
		try {
			int _type = SEPARATOR;
			int _channel = DEFAULT_TOKEN_CHANNEL;
			// org/eclipse/tracecompass/ctf/parser/CTFLexer.g:76:20: ( ',' )
			// org/eclipse/tracecompass/ctf/parser/CTFLexer.g:76:22: ','
			{
			match(','); 
			}

			state.type = _type;
			state.channel = _channel;
		}
		finally {
			// do for sure before leaving
		}
	}
	// $ANTLR end "SEPARATOR"

	// $ANTLR start "COLON"
	public final void mCOLON() throws RecognitionException {
		try {
			int _type = COLON;
			int _channel = DEFAULT_TOKEN_CHANNEL;
			// org/eclipse/tracecompass/ctf/parser/CTFLexer.g:77:20: ( ':' )
			// org/eclipse/tracecompass/ctf/parser/CTFLexer.g:77:22: ':'
			{
			match(':'); 
			}

			state.type = _type;
			state.channel = _channel;
		}
		finally {
			// do for sure before leaving
		}
	}
	// $ANTLR end "COLON"

	// $ANTLR start "ELIPSES"
	public final void mELIPSES() throws RecognitionException {
		try {
			int _type = ELIPSES;
			int _channel = DEFAULT_TOKEN_CHANNEL;
			// org/eclipse/tracecompass/ctf/parser/CTFLexer.g:78:20: ( '...' )
			// org/eclipse/tracecompass/ctf/parser/CTFLexer.g:78:22: '...'
			{
			match("..."); 

			}

			state.type = _type;
			state.channel = _channel;
		}
		finally {
			// do for sure before leaving
		}
	}
	// $ANTLR end "ELIPSES"

	// $ANTLR start "ASSIGNMENT"
	public final void mASSIGNMENT() throws RecognitionException {
		try {
			int _type = ASSIGNMENT;
			int _channel = DEFAULT_TOKEN_CHANNEL;
			// org/eclipse/tracecompass/ctf/parser/CTFLexer.g:79:20: ( '=' )
			// org/eclipse/tracecompass/ctf/parser/CTFLexer.g:79:22: '='
			{
			match('='); 
			}

			state.type = _type;
			state.channel = _channel;
		}
		finally {
			// do for sure before leaving
		}
	}
	// $ANTLR end "ASSIGNMENT"

	// $ANTLR start "TYPE_ASSIGNMENT"
	public final void mTYPE_ASSIGNMENT() throws RecognitionException {
		try {
			int _type = TYPE_ASSIGNMENT;
			int _channel = DEFAULT_TOKEN_CHANNEL;
			// org/eclipse/tracecompass/ctf/parser/CTFLexer.g:80:20: ( ':=' )
			// org/eclipse/tracecompass/ctf/parser/CTFLexer.g:80:22: ':='
			{
			match(":="); 

			}

			state.type = _type;
			state.channel = _channel;
		}
		finally {
			// do for sure before leaving
		}
	}
	// $ANTLR end "TYPE_ASSIGNMENT"

	// $ANTLR start "LT"
	public final void mLT() throws RecognitionException {
		try {
			int _type = LT;
			int _channel = DEFAULT_TOKEN_CHANNEL;
			// org/eclipse/tracecompass/ctf/parser/CTFLexer.g:81:20: ( '<' )
			// org/eclipse/tracecompass/ctf/parser/CTFLexer.g:81:22: '<'
			{
			match('<'); 
			}

			state.type = _type;
			state.channel = _channel;
		}
		finally {
			// do for sure before leaving
		}
	}
	// $ANTLR end "LT"

	// $ANTLR start "GT"
	public final void mGT() throws RecognitionException {
		try {
			int _type = GT;
			int _channel = DEFAULT_TOKEN_CHANNEL;
			// org/eclipse/tracecompass/ctf/parser/CTFLexer.g:82:20: ( '>' )
			// org/eclipse/tracecompass/ctf/parser/CTFLexer.g:82:22: '>'
			{
			match('>'); 
			}

			state.type = _type;
			state.channel = _channel;
		}
		finally {
			// do for sure before leaving
		}
	}
	// $ANTLR end "GT"

	// $ANTLR start "OPENBRAC"
	public final void mOPENBRAC() throws RecognitionException {
		try {
			int _type = OPENBRAC;
			int _channel = DEFAULT_TOKEN_CHANNEL;
			// org/eclipse/tracecompass/ctf/parser/CTFLexer.g:83:20: ( '[' )
			// org/eclipse/tracecompass/ctf/parser/CTFLexer.g:83:22: '['
			{
			match('['); 
			}

			state.type = _type;
			state.channel = _channel;
		}
		finally {
			// do for sure before leaving
		}
	}
	// $ANTLR end "OPENBRAC"

	// $ANTLR start "CLOSEBRAC"
	public final void mCLOSEBRAC() throws RecognitionException {
		try {
			int _type = CLOSEBRAC;
			int _channel = DEFAULT_TOKEN_CHANNEL;
			// org/eclipse/tracecompass/ctf/parser/CTFLexer.g:84:20: ( ']' )
			// org/eclipse/tracecompass/ctf/parser/CTFLexer.g:84:22: ']'
			{
			match(']'); 
			}

			state.type = _type;
			state.channel = _channel;
		}
		finally {
			// do for sure before leaving
		}
	}
	// $ANTLR end "CLOSEBRAC"

	// $ANTLR start "LPAREN"
	public final void mLPAREN() throws RecognitionException {
		try {
			int _type = LPAREN;
			int _channel = DEFAULT_TOKEN_CHANNEL;
			// org/eclipse/tracecompass/ctf/parser/CTFLexer.g:85:20: ( '(' )
			// org/eclipse/tracecompass/ctf/parser/CTFLexer.g:85:22: '('
			{
			match('('); 
			}

			state.type = _type;
			state.channel = _channel;
		}
		finally {
			// do for sure before leaving
		}
	}
	// $ANTLR end "LPAREN"

	// $ANTLR start "RPAREN"
	public final void mRPAREN() throws RecognitionException {
		try {
			int _type = RPAREN;
			int _channel = DEFAULT_TOKEN_CHANNEL;
			// org/eclipse/tracecompass/ctf/parser/CTFLexer.g:86:20: ( ')' )
			// org/eclipse/tracecompass/ctf/parser/CTFLexer.g:86:22: ')'
			{
			match(')'); 
			}

			state.type = _type;
			state.channel = _channel;
		}
		finally {
			// do for sure before leaving
		}
	}
	// $ANTLR end "RPAREN"

	// $ANTLR start "LCURL"
	public final void mLCURL() throws RecognitionException {
		try {
			int _type = LCURL;
			int _channel = DEFAULT_TOKEN_CHANNEL;
			// org/eclipse/tracecompass/ctf/parser/CTFLexer.g:87:20: ( '{' )
			// org/eclipse/tracecompass/ctf/parser/CTFLexer.g:87:22: '{'
			{
			match('{'); 
			}

			state.type = _type;
			state.channel = _channel;
		}
		finally {
			// do for sure before leaving
		}
	}
	// $ANTLR end "LCURL"

	// $ANTLR start "RCURL"
	public final void mRCURL() throws RecognitionException {
		try {
			int _type = RCURL;
			int _channel = DEFAULT_TOKEN_CHANNEL;
			// org/eclipse/tracecompass/ctf/parser/CTFLexer.g:88:20: ( '}' )
			// org/eclipse/tracecompass/ctf/parser/CTFLexer.g:88:22: '}'
			{
			match('}'); 
			}

			state.type = _type;
			state.channel = _channel;
		}
		finally {
			// do for sure before leaving
		}
	}
	// $ANTLR end "RCURL"

	// $ANTLR start "TERM"
	public final void mTERM() throws RecognitionException {
		try {
			int _type = TERM;
			int _channel = DEFAULT_TOKEN_CHANNEL;
			// org/eclipse/tracecompass/ctf/parser/CTFLexer.g:89:20: ( ';' )
			// org/eclipse/tracecompass/ctf/parser/CTFLexer.g:89:22: ';'
			{
			match(';'); 
			}

			state.type = _type;
			state.channel = _channel;
		}
		finally {
			// do for sure before leaving
		}
	}
	// $ANTLR end "TERM"

	// $ANTLR start "POINTER"
	public final void mPOINTER() throws RecognitionException {
		try {
			int _type = POINTER;
			int _channel = DEFAULT_TOKEN_CHANNEL;
			// org/eclipse/tracecompass/ctf/parser/CTFLexer.g:90:20: ( '*' )
			// org/eclipse/tracecompass/ctf/parser/CTFLexer.g:90:22: '*'
			{
			match('*'); 
			}

			state.type = _type;
			state.channel = _channel;
		}
		finally {
			// do for sure before leaving
		}
	}
	// $ANTLR end "POINTER"

	// $ANTLR start "SIGN"
	public final void mSIGN() throws RecognitionException {
		try {
			int _type = SIGN;
			int _channel = DEFAULT_TOKEN_CHANNEL;
			// org/eclipse/tracecompass/ctf/parser/CTFLexer.g:91:20: ( '+' | '-' )
			// org/eclipse/tracecompass/ctf/parser/CTFLexer.g:
			{
			if ( input.LA(1)=='+'||input.LA(1)=='-' ) {
				input.consume();
			}
			else {
				MismatchedSetException mse = new MismatchedSetException(null,input);
				recover(mse);
				throw mse;
			}
			}

			state.type = _type;
			state.channel = _channel;
		}
		finally {
			// do for sure before leaving
		}
	}
	// $ANTLR end "SIGN"

	// $ANTLR start "ARROW"
	public final void mARROW() throws RecognitionException {
		try {
			int _type = ARROW;
			int _channel = DEFAULT_TOKEN_CHANNEL;
			// org/eclipse/tracecompass/ctf/parser/CTFLexer.g:92:20: ( '->' )
			// org/eclipse/tracecompass/ctf/parser/CTFLexer.g:92:22: '->'
			{
			match("->"); 

			}

			state.type = _type;
			state.channel = _channel;
		}
		finally {
			// do for sure before leaving
		}
	}
	// $ANTLR end "ARROW"

	// $ANTLR start "DOT"
	public final void mDOT() throws RecognitionException {
		try {
			int _type = DOT;
			int _channel = DEFAULT_TOKEN_CHANNEL;
			// org/eclipse/tracecompass/ctf/parser/CTFLexer.g:93:20: ( '.' )
			// org/eclipse/tracecompass/ctf/parser/CTFLexer.g:93:22: '.'
			{
			match('.'); 
			}

			state.type = _type;
			state.channel = _channel;
		}
		finally {
			// do for sure before leaving
		}
	}
	// $ANTLR end "DOT"

	// $ANTLR start "BACKSLASH"
	public final void mBACKSLASH() throws RecognitionException {
		try {
			// org/eclipse/tracecompass/ctf/parser/CTFLexer.g:94:20: ( '\\\\' )
			// org/eclipse/tracecompass/ctf/parser/CTFLexer.g:94:22: '\\\\'
			{
			match('\\'); 
			}

		}
		finally {
			// do for sure before leaving
		}
	}
	// $ANTLR end "BACKSLASH"

	// $ANTLR start "DIGIT"
	public final void mDIGIT() throws RecognitionException {
		try {
			// org/eclipse/tracecompass/ctf/parser/CTFLexer.g:97:16: ( '0' .. '9' )
			// org/eclipse/tracecompass/ctf/parser/CTFLexer.g:
			{
			if ( (input.LA(1) >= '0' && input.LA(1) <= '9') ) {
				input.consume();
			}
			else {
				MismatchedSetException mse = new MismatchedSetException(null,input);
				recover(mse);
				throw mse;
			}
			}

		}
		finally {
			// do for sure before leaving
		}
	}
	// $ANTLR end "DIGIT"

	// $ANTLR start "OCT_DIGIT"
	public final void mOCT_DIGIT() throws RecognitionException {
		try {
			// org/eclipse/tracecompass/ctf/parser/CTFLexer.g:98:20: ( '0' .. '7' )
			// org/eclipse/tracecompass/ctf/parser/CTFLexer.g:
			{
			if ( (input.LA(1) >= '0' && input.LA(1) <= '7') ) {
				input.consume();
			}
			else {
				MismatchedSetException mse = new MismatchedSetException(null,input);
				recover(mse);
				throw mse;
			}
			}

		}
		finally {
			// do for sure before leaving
		}
	}
	// $ANTLR end "OCT_DIGIT"

	// $ANTLR start "OCT_PREFIX"
	public final void mOCT_PREFIX() throws RecognitionException {
		try {
			// org/eclipse/tracecompass/ctf/parser/CTFLexer.g:99:21: ( '0' )
			// org/eclipse/tracecompass/ctf/parser/CTFLexer.g:99:23: '0'
			{
			match('0'); 
			}

		}
		finally {
			// do for sure before leaving
		}
	}
	// $ANTLR end "OCT_PREFIX"

	// $ANTLR start "NONZERO_DIGIT"
	public final void mNONZERO_DIGIT() throws RecognitionException {
		try {
			// org/eclipse/tracecompass/ctf/parser/CTFLexer.g:100:24: ( '1' .. '9' )
			// org/eclipse/tracecompass/ctf/parser/CTFLexer.g:
			{
			if ( (input.LA(1) >= '1' && input.LA(1) <= '9') ) {
				input.consume();
			}
			else {
				MismatchedSetException mse = new MismatchedSetException(null,input);
				recover(mse);
				throw mse;
			}
			}

		}
		finally {
			// do for sure before leaving
		}
	}
	// $ANTLR end "NONZERO_DIGIT"

	// $ANTLR start "HEX_DIGIT"
	public final void mHEX_DIGIT() throws RecognitionException {
		try {
			// org/eclipse/tracecompass/ctf/parser/CTFLexer.g:101:20: ( DIGIT | ( 'a' .. 'f' ) | ( 'A' .. 'F' ) )
			// org/eclipse/tracecompass/ctf/parser/CTFLexer.g:
			{
			if ( (input.LA(1) >= '0' && input.LA(1) <= '9')||(input.LA(1) >= 'A' && input.LA(1) <= 'F')||(input.LA(1) >= 'a' && input.LA(1) <= 'f') ) {
				input.consume();
			}
			else {
				MismatchedSetException mse = new MismatchedSetException(null,input);
				recover(mse);
				throw mse;
			}
			}

		}
		finally {
			// do for sure before leaving
		}
	}
	// $ANTLR end "HEX_DIGIT"

	// $ANTLR start "HEX_PREFIX"
	public final void mHEX_PREFIX() throws RecognitionException {
		try {
			// org/eclipse/tracecompass/ctf/parser/CTFLexer.g:102:21: ( '0' ( 'x' | 'X' ) )
			// org/eclipse/tracecompass/ctf/parser/CTFLexer.g:102:23: '0' ( 'x' | 'X' )
			{
			match('0'); 
			if ( input.LA(1)=='X'||input.LA(1)=='x' ) {
				input.consume();
			}
			else {
				MismatchedSetException mse = new MismatchedSetException(null,input);
				recover(mse);
				throw mse;
			}
			}

		}
		finally {
			// do for sure before leaving
		}
	}
	// $ANTLR end "HEX_PREFIX"

	// $ANTLR start "OCTAL_LITERAL"
	public final void mOCTAL_LITERAL() throws RecognitionException {
		try {
			int _type = OCTAL_LITERAL;
			int _channel = DEFAULT_TOKEN_CHANNEL;
			// org/eclipse/tracecompass/ctf/parser/CTFLexer.g:107:15: ( OCT_PREFIX ( OCT_DIGIT )+ ( INTEGER_TYPES_SUFFIX )? )
			// org/eclipse/tracecompass/ctf/parser/CTFLexer.g:107:17: OCT_PREFIX ( OCT_DIGIT )+ ( INTEGER_TYPES_SUFFIX )?
			{
			mOCT_PREFIX(); 

			// org/eclipse/tracecompass/ctf/parser/CTFLexer.g:107:28: ( OCT_DIGIT )+
			int cnt1=0;
			loop1:
			while (true) {
				int alt1=2;
				int LA1_0 = input.LA(1);
				if ( ((LA1_0 >= '0' && LA1_0 <= '7')) ) {
					alt1=1;
				}

				switch (alt1) {
				case 1 :
					// org/eclipse/tracecompass/ctf/parser/CTFLexer.g:
					{
					if ( (input.LA(1) >= '0' && input.LA(1) <= '7') ) {
						input.consume();
					}
					else {
						MismatchedSetException mse = new MismatchedSetException(null,input);
						recover(mse);
						throw mse;
					}
					}
					break;

				default :
					if ( cnt1 >= 1 ) break loop1;
					EarlyExitException eee = new EarlyExitException(1, input);
					throw eee;
				}
				cnt1++;
			}

			// org/eclipse/tracecompass/ctf/parser/CTFLexer.g:107:41: ( INTEGER_TYPES_SUFFIX )?
			int alt2=2;
			int LA2_0 = input.LA(1);
			if ( (LA2_0=='L'||LA2_0=='U'||LA2_0=='l'||LA2_0=='u') ) {
				alt2=1;
			}
			switch (alt2) {
				case 1 :
					// org/eclipse/tracecompass/ctf/parser/CTFLexer.g:107:41: INTEGER_TYPES_SUFFIX
					{
					mINTEGER_TYPES_SUFFIX(); 

					}
					break;

			}

			}

			state.type = _type;
			state.channel = _channel;
		}
		finally {
			// do for sure before leaving
		}
	}
	// $ANTLR end "OCTAL_LITERAL"

	// $ANTLR start "DECIMAL_LITERAL"
	public final void mDECIMAL_LITERAL() throws RecognitionException {
		try {
			int _type = DECIMAL_LITERAL;
			int _channel = DEFAULT_TOKEN_CHANNEL;
			// org/eclipse/tracecompass/ctf/parser/CTFLexer.g:108:17: ( ( DIGIT )+ ( INTEGER_TYPES_SUFFIX )? )
			// org/eclipse/tracecompass/ctf/parser/CTFLexer.g:108:19: ( DIGIT )+ ( INTEGER_TYPES_SUFFIX )?
			{
			// org/eclipse/tracecompass/ctf/parser/CTFLexer.g:108:19: ( DIGIT )+
			int cnt3=0;
			loop3:
			while (true) {
				int alt3=2;
				int LA3_0 = input.LA(1);
				if ( ((LA3_0 >= '0' && LA3_0 <= '9')) ) {
					alt3=1;
				}

				switch (alt3) {
				case 1 :
					// org/eclipse/tracecompass/ctf/parser/CTFLexer.g:
					{
					if ( (input.LA(1) >= '0' && input.LA(1) <= '9') ) {
						input.consume();
					}
					else {
						MismatchedSetException mse = new MismatchedSetException(null,input);
						recover(mse);
						throw mse;
					}
					}
					break;

				default :
					if ( cnt3 >= 1 ) break loop3;
					EarlyExitException eee = new EarlyExitException(3, input);
					throw eee;
				}
				cnt3++;
			}

			// org/eclipse/tracecompass/ctf/parser/CTFLexer.g:108:26: ( INTEGER_TYPES_SUFFIX )?
			int alt4=2;
			int LA4_0 = input.LA(1);
			if ( (LA4_0=='L'||LA4_0=='U'||LA4_0=='l'||LA4_0=='u') ) {
				alt4=1;
			}
			switch (alt4) {
				case 1 :
					// org/eclipse/tracecompass/ctf/parser/CTFLexer.g:108:26: INTEGER_TYPES_SUFFIX
					{
					mINTEGER_TYPES_SUFFIX(); 

					}
					break;

			}

			}

			state.type = _type;
			state.channel = _channel;
		}
		finally {
			// do for sure before leaving
		}
	}
	// $ANTLR end "DECIMAL_LITERAL"

	// $ANTLR start "HEX_LITERAL"
	public final void mHEX_LITERAL() throws RecognitionException {
		try {
			int _type = HEX_LITERAL;
			int _channel = DEFAULT_TOKEN_CHANNEL;
			// org/eclipse/tracecompass/ctf/parser/CTFLexer.g:109:13: ( HEX_PREFIX ( HEX_DIGIT )+ ( INTEGER_TYPES_SUFFIX )? )
			// org/eclipse/tracecompass/ctf/parser/CTFLexer.g:109:15: HEX_PREFIX ( HEX_DIGIT )+ ( INTEGER_TYPES_SUFFIX )?
			{
			mHEX_PREFIX(); 

			// org/eclipse/tracecompass/ctf/parser/CTFLexer.g:109:26: ( HEX_DIGIT )+
			int cnt5=0;
			loop5:
			while (true) {
				int alt5=2;
				int LA5_0 = input.LA(1);
				if ( ((LA5_0 >= '0' && LA5_0 <= '9')||(LA5_0 >= 'A' && LA5_0 <= 'F')||(LA5_0 >= 'a' && LA5_0 <= 'f')) ) {
					alt5=1;
				}

				switch (alt5) {
				case 1 :
					// org/eclipse/tracecompass/ctf/parser/CTFLexer.g:
					{
					if ( (input.LA(1) >= '0' && input.LA(1) <= '9')||(input.LA(1) >= 'A' && input.LA(1) <= 'F')||(input.LA(1) >= 'a' && input.LA(1) <= 'f') ) {
						input.consume();
					}
					else {
						MismatchedSetException mse = new MismatchedSetException(null,input);
						recover(mse);
						throw mse;
					}
					}
					break;

				default :
					if ( cnt5 >= 1 ) break loop5;
					EarlyExitException eee = new EarlyExitException(5, input);
					throw eee;
				}
				cnt5++;
			}

			// org/eclipse/tracecompass/ctf/parser/CTFLexer.g:109:37: ( INTEGER_TYPES_SUFFIX )?
			int alt6=2;
			int LA6_0 = input.LA(1);
			if ( (LA6_0=='L'||LA6_0=='U'||LA6_0=='l'||LA6_0=='u') ) {
				alt6=1;
			}
			switch (alt6) {
				case 1 :
					// org/eclipse/tracecompass/ctf/parser/CTFLexer.g:109:37: INTEGER_TYPES_SUFFIX
					{
					mINTEGER_TYPES_SUFFIX(); 

					}
					break;

			}

			}

			state.type = _type;
			state.channel = _channel;
		}
		finally {
			// do for sure before leaving
		}
	}
	// $ANTLR end "HEX_LITERAL"

	// $ANTLR start "INTEGER_TYPES_SUFFIX"
	public final void mINTEGER_TYPES_SUFFIX() throws RecognitionException {
		try {
			// org/eclipse/tracecompass/ctf/parser/CTFLexer.g:117:3: ( ( 'l' ( 'l' )? | 'L' ( 'L' )? ) | ( 'u' | 'U' ) | ( 'u' | 'U' ) ( 'l' ( 'l' )? | 'L' ( 'L' )? ) | ( 'l' ( 'l' )? | 'L' ( 'L' )? ) ( 'u' | 'U' ) )
			int alt16=4;
			switch ( input.LA(1) ) {
			case 'l':
				{
				switch ( input.LA(2) ) {
				case 'l':
					{
					int LA16_4 = input.LA(3);
					if ( (LA16_4=='U'||LA16_4=='u') ) {
						alt16=4;
					}

					else {
						alt16=1;
					}

					}
					break;
				case 'U':
				case 'u':
					{
					alt16=4;
					}
					break;
				default:
					alt16=1;
				}
				}
				break;
			case 'L':
				{
				switch ( input.LA(2) ) {
				case 'L':
					{
					int LA16_7 = input.LA(3);
					if ( (LA16_7=='U'||LA16_7=='u') ) {
						alt16=4;
					}

					else {
						alt16=1;
					}

					}
					break;
				case 'U':
				case 'u':
					{
					alt16=4;
					}
					break;
				default:
					alt16=1;
				}
				}
				break;
			case 'U':
			case 'u':
				{
				int LA16_3 = input.LA(2);
				if ( (LA16_3=='L'||LA16_3=='l') ) {
					alt16=3;
				}

				else {
					alt16=2;
				}

				}
				break;
			default:
				NoViableAltException nvae =
					new NoViableAltException("", 16, 0, input);
				throw nvae;
			}
			switch (alt16) {
				case 1 :
					// org/eclipse/tracecompass/ctf/parser/CTFLexer.g:117:5: ( 'l' ( 'l' )? | 'L' ( 'L' )? )
					{
					// org/eclipse/tracecompass/ctf/parser/CTFLexer.g:117:5: ( 'l' ( 'l' )? | 'L' ( 'L' )? )
					int alt9=2;
					int LA9_0 = input.LA(1);
					if ( (LA9_0=='l') ) {
						alt9=1;
					}
					else if ( (LA9_0=='L') ) {
						alt9=2;
					}

					else {
						NoViableAltException nvae =
							new NoViableAltException("", 9, 0, input);
						throw nvae;
					}

					switch (alt9) {
						case 1 :
							// org/eclipse/tracecompass/ctf/parser/CTFLexer.g:117:6: 'l' ( 'l' )?
							{
							match('l'); 
							// org/eclipse/tracecompass/ctf/parser/CTFLexer.g:117:10: ( 'l' )?
							int alt7=2;
							int LA7_0 = input.LA(1);
							if ( (LA7_0=='l') ) {
								alt7=1;
							}
							switch (alt7) {
								case 1 :
									// org/eclipse/tracecompass/ctf/parser/CTFLexer.g:117:11: 'l'
									{
									match('l'); 
									}
									break;

							}

							}
							break;
						case 2 :
							// org/eclipse/tracecompass/ctf/parser/CTFLexer.g:117:19: 'L' ( 'L' )?
							{
							match('L'); 
							// org/eclipse/tracecompass/ctf/parser/CTFLexer.g:117:23: ( 'L' )?
							int alt8=2;
							int LA8_0 = input.LA(1);
							if ( (LA8_0=='L') ) {
								alt8=1;
							}
							switch (alt8) {
								case 1 :
									// org/eclipse/tracecompass/ctf/parser/CTFLexer.g:117:24: 'L'
									{
									match('L'); 
									}
									break;

							}

							}
							break;

					}

					}
					break;
				case 2 :
					// org/eclipse/tracecompass/ctf/parser/CTFLexer.g:118:5: ( 'u' | 'U' )
					{
					if ( input.LA(1)=='U'||input.LA(1)=='u' ) {
						input.consume();
					}
					else {
						MismatchedSetException mse = new MismatchedSetException(null,input);
						recover(mse);
						throw mse;
					}
					}
					break;
				case 3 :
					// org/eclipse/tracecompass/ctf/parser/CTFLexer.g:119:5: ( 'u' | 'U' ) ( 'l' ( 'l' )? | 'L' ( 'L' )? )
					{
					if ( input.LA(1)=='U'||input.LA(1)=='u' ) {
						input.consume();
					}
					else {
						MismatchedSetException mse = new MismatchedSetException(null,input);
						recover(mse);
						throw mse;
					}
					// org/eclipse/tracecompass/ctf/parser/CTFLexer.g:119:17: ( 'l' ( 'l' )? | 'L' ( 'L' )? )
					int alt12=2;
					int LA12_0 = input.LA(1);
					if ( (LA12_0=='l') ) {
						alt12=1;
					}
					else if ( (LA12_0=='L') ) {
						alt12=2;
					}

					else {
						NoViableAltException nvae =
							new NoViableAltException("", 12, 0, input);
						throw nvae;
					}

					switch (alt12) {
						case 1 :
							// org/eclipse/tracecompass/ctf/parser/CTFLexer.g:119:18: 'l' ( 'l' )?
							{
							match('l'); 
							// org/eclipse/tracecompass/ctf/parser/CTFLexer.g:119:22: ( 'l' )?
							int alt10=2;
							int LA10_0 = input.LA(1);
							if ( (LA10_0=='l') ) {
								alt10=1;
							}
							switch (alt10) {
								case 1 :
									// org/eclipse/tracecompass/ctf/parser/CTFLexer.g:119:23: 'l'
									{
									match('l'); 
									}
									break;

							}

							}
							break;
						case 2 :
							// org/eclipse/tracecompass/ctf/parser/CTFLexer.g:119:31: 'L' ( 'L' )?
							{
							match('L'); 
							// org/eclipse/tracecompass/ctf/parser/CTFLexer.g:119:35: ( 'L' )?
							int alt11=2;
							int LA11_0 = input.LA(1);
							if ( (LA11_0=='L') ) {
								alt11=1;
							}
							switch (alt11) {
								case 1 :
									// org/eclipse/tracecompass/ctf/parser/CTFLexer.g:119:36: 'L'
									{
									match('L'); 
									}
									break;

							}

							}
							break;

					}

					}
					break;
				case 4 :
					// org/eclipse/tracecompass/ctf/parser/CTFLexer.g:120:5: ( 'l' ( 'l' )? | 'L' ( 'L' )? ) ( 'u' | 'U' )
					{
					// org/eclipse/tracecompass/ctf/parser/CTFLexer.g:120:5: ( 'l' ( 'l' )? | 'L' ( 'L' )? )
					int alt15=2;
					int LA15_0 = input.LA(1);
					if ( (LA15_0=='l') ) {
						alt15=1;
					}
					else if ( (LA15_0=='L') ) {
						alt15=2;
					}

					else {
						NoViableAltException nvae =
							new NoViableAltException("", 15, 0, input);
						throw nvae;
					}

					switch (alt15) {
						case 1 :
							// org/eclipse/tracecompass/ctf/parser/CTFLexer.g:120:6: 'l' ( 'l' )?
							{
							match('l'); 
							// org/eclipse/tracecompass/ctf/parser/CTFLexer.g:120:10: ( 'l' )?
							int alt13=2;
							int LA13_0 = input.LA(1);
							if ( (LA13_0=='l') ) {
								alt13=1;
							}
							switch (alt13) {
								case 1 :
									// org/eclipse/tracecompass/ctf/parser/CTFLexer.g:120:11: 'l'
									{
									match('l'); 
									}
									break;

							}

							}
							break;
						case 2 :
							// org/eclipse/tracecompass/ctf/parser/CTFLexer.g:120:19: 'L' ( 'L' )?
							{
							match('L'); 
							// org/eclipse/tracecompass/ctf/parser/CTFLexer.g:120:23: ( 'L' )?
							int alt14=2;
							int LA14_0 = input.LA(1);
							if ( (LA14_0=='L') ) {
								alt14=1;
							}
							switch (alt14) {
								case 1 :
									// org/eclipse/tracecompass/ctf/parser/CTFLexer.g:120:24: 'L'
									{
									match('L'); 
									}
									break;

							}

							}
							break;

					}

					if ( input.LA(1)=='U'||input.LA(1)=='u' ) {
						input.consume();
					}
					else {
						MismatchedSetException mse = new MismatchedSetException(null,input);
						recover(mse);
						throw mse;
					}
					}
					break;

			}
		}
		finally {
			// do for sure before leaving
		}
	}
	// $ANTLR end "INTEGER_TYPES_SUFFIX"

	// $ANTLR start "ESCAPE_SEQUENCE"
	public final void mESCAPE_SEQUENCE() throws RecognitionException {
		try {
			// org/eclipse/tracecompass/ctf/parser/CTFLexer.g:127:3: ( BACKSLASH ( '\\'' | '\"' | '?' | BACKSLASH | 'a' | 'b' | 'f' | 'n' | 'r' | 't' | 'v' ) | OCTAL_ESCAPE | UNICODE_ESCAPE | HEXADECIMAL_ESCAPE )
			int alt17=4;
			int LA17_0 = input.LA(1);
			if ( (LA17_0=='\\') ) {
				switch ( input.LA(2) ) {
				case '\"':
				case '\'':
				case '?':
				case '\\':
				case 'a':
				case 'b':
				case 'f':
				case 'n':
				case 'r':
				case 't':
				case 'v':
					{
					alt17=1;
					}
					break;
				case '0':
				case '1':
				case '2':
				case '3':
				case '4':
				case '5':
				case '6':
				case '7':
					{
					alt17=2;
					}
					break;
				case 'U':
				case 'u':
					{
					alt17=3;
					}
					break;
				case 'x':
					{
					alt17=4;
					}
					break;
				default:
					int nvaeMark = input.mark();
					try {
						input.consume();
						NoViableAltException nvae =
							new NoViableAltException("", 17, 1, input);
						throw nvae;
					} finally {
						input.rewind(nvaeMark);
					}
				}
			}

			else {
				NoViableAltException nvae =
					new NoViableAltException("", 17, 0, input);
				throw nvae;
			}

			switch (alt17) {
				case 1 :
					// org/eclipse/tracecompass/ctf/parser/CTFLexer.g:127:5: BACKSLASH ( '\\'' | '\"' | '?' | BACKSLASH | 'a' | 'b' | 'f' | 'n' | 'r' | 't' | 'v' )
					{
					mBACKSLASH(); 

					if ( input.LA(1)=='\"'||input.LA(1)=='\''||input.LA(1)=='?'||input.LA(1)=='\\'||(input.LA(1) >= 'a' && input.LA(1) <= 'b')||input.LA(1)=='f'||input.LA(1)=='n'||input.LA(1)=='r'||input.LA(1)=='t'||input.LA(1)=='v' ) {
						input.consume();
					}
					else {
						MismatchedSetException mse = new MismatchedSetException(null,input);
						recover(mse);
						throw mse;
					}
					}
					break;
				case 2 :
					// org/eclipse/tracecompass/ctf/parser/CTFLexer.g:128:5: OCTAL_ESCAPE
					{
					mOCTAL_ESCAPE(); 

					}
					break;
				case 3 :
					// org/eclipse/tracecompass/ctf/parser/CTFLexer.g:129:5: UNICODE_ESCAPE
					{
					mUNICODE_ESCAPE(); 

					}
					break;
				case 4 :
					// org/eclipse/tracecompass/ctf/parser/CTFLexer.g:130:5: HEXADECIMAL_ESCAPE
					{
					mHEXADECIMAL_ESCAPE(); 

					}
					break;

			}
		}
		finally {
			// do for sure before leaving
		}
	}
	// $ANTLR end "ESCAPE_SEQUENCE"

	// $ANTLR start "OCTAL_ESCAPE"
	public final void mOCTAL_ESCAPE() throws RecognitionException {
		try {
			// org/eclipse/tracecompass/ctf/parser/CTFLexer.g:137:3: ( BACKSLASH ( '0' .. '3' ) ( '0' .. '7' ) ( '0' .. '7' ) | BACKSLASH ( '0' .. '7' ) ( '0' .. '7' ) | BACKSLASH ( '0' .. '7' ) )
			int alt18=3;
			int LA18_0 = input.LA(1);
			if ( (LA18_0=='\\') ) {
				int LA18_1 = input.LA(2);
				if ( ((LA18_1 >= '0' && LA18_1 <= '3')) ) {
					int LA18_2 = input.LA(3);
					if ( ((LA18_2 >= '0' && LA18_2 <= '7')) ) {
						int LA18_4 = input.LA(4);
						if ( ((LA18_4 >= '0' && LA18_4 <= '7')) ) {
							alt18=1;
						}

						else {
							alt18=2;
						}

					}

					else {
						alt18=3;
					}

				}
				else if ( ((LA18_1 >= '4' && LA18_1 <= '7')) ) {
					int LA18_3 = input.LA(3);
					if ( ((LA18_3 >= '0' && LA18_3 <= '7')) ) {
						alt18=2;
					}

					else {
						alt18=3;
					}

				}

				else {
					int nvaeMark = input.mark();
					try {
						input.consume();
						NoViableAltException nvae =
							new NoViableAltException("", 18, 1, input);
						throw nvae;
					} finally {
						input.rewind(nvaeMark);
					}
				}

			}

			else {
				NoViableAltException nvae =
					new NoViableAltException("", 18, 0, input);
				throw nvae;
			}

			switch (alt18) {
				case 1 :
					// org/eclipse/tracecompass/ctf/parser/CTFLexer.g:137:5: BACKSLASH ( '0' .. '3' ) ( '0' .. '7' ) ( '0' .. '7' )
					{
					mBACKSLASH(); 

					if ( (input.LA(1) >= '0' && input.LA(1) <= '3') ) {
						input.consume();
					}
					else {
						MismatchedSetException mse = new MismatchedSetException(null,input);
						recover(mse);
						throw mse;
					}
					if ( (input.LA(1) >= '0' && input.LA(1) <= '7') ) {
						input.consume();
					}
					else {
						MismatchedSetException mse = new MismatchedSetException(null,input);
						recover(mse);
						throw mse;
					}
					if ( (input.LA(1) >= '0' && input.LA(1) <= '7') ) {
						input.consume();
					}
					else {
						MismatchedSetException mse = new MismatchedSetException(null,input);
						recover(mse);
						throw mse;
					}
					}
					break;
				case 2 :
					// org/eclipse/tracecompass/ctf/parser/CTFLexer.g:138:5: BACKSLASH ( '0' .. '7' ) ( '0' .. '7' )
					{
					mBACKSLASH(); 

					if ( (input.LA(1) >= '0' && input.LA(1) <= '7') ) {
						input.consume();
					}
					else {
						MismatchedSetException mse = new MismatchedSetException(null,input);
						recover(mse);
						throw mse;
					}
					if ( (input.LA(1) >= '0' && input.LA(1) <= '7') ) {
						input.consume();
					}
					else {
						MismatchedSetException mse = new MismatchedSetException(null,input);
						recover(mse);
						throw mse;
					}
					}
					break;
				case 3 :
					// org/eclipse/tracecompass/ctf/parser/CTFLexer.g:139:5: BACKSLASH ( '0' .. '7' )
					{
					mBACKSLASH(); 

					if ( (input.LA(1) >= '0' && input.LA(1) <= '7') ) {
						input.consume();
					}
					else {
						MismatchedSetException mse = new MismatchedSetException(null,input);
						recover(mse);
						throw mse;
					}
					}
					break;

			}
		}
		finally {
			// do for sure before leaving
		}
	}
	// $ANTLR end "OCTAL_ESCAPE"

	// $ANTLR start "HEXADECIMAL_ESCAPE"
	public final void mHEXADECIMAL_ESCAPE() throws RecognitionException {
		try {
			// org/eclipse/tracecompass/ctf/parser/CTFLexer.g:145:29: ( BACKSLASH 'x' ( HEX_DIGIT )+ )
			// org/eclipse/tracecompass/ctf/parser/CTFLexer.g:145:31: BACKSLASH 'x' ( HEX_DIGIT )+
			{
			mBACKSLASH(); 

			match('x'); 
			// org/eclipse/tracecompass/ctf/parser/CTFLexer.g:145:45: ( HEX_DIGIT )+
			int cnt19=0;
			loop19:
			while (true) {
				int alt19=2;
				int LA19_0 = input.LA(1);
				if ( ((LA19_0 >= '0' && LA19_0 <= '9')||(LA19_0 >= 'A' && LA19_0 <= 'F')||(LA19_0 >= 'a' && LA19_0 <= 'f')) ) {
					alt19=1;
				}

				switch (alt19) {
				case 1 :
					// org/eclipse/tracecompass/ctf/parser/CTFLexer.g:
					{
					if ( (input.LA(1) >= '0' && input.LA(1) <= '9')||(input.LA(1) >= 'A' && input.LA(1) <= 'F')||(input.LA(1) >= 'a' && input.LA(1) <= 'f') ) {
						input.consume();
					}
					else {
						MismatchedSetException mse = new MismatchedSetException(null,input);
						recover(mse);
						throw mse;
					}
					}
					break;

				default :
					if ( cnt19 >= 1 ) break loop19;
					EarlyExitException eee = new EarlyExitException(19, input);
					throw eee;
				}
				cnt19++;
			}

			}

		}
		finally {
			// do for sure before leaving
		}
	}
	// $ANTLR end "HEXADECIMAL_ESCAPE"

	// $ANTLR start "UNICODE_ESCAPE"
	public final void mUNICODE_ESCAPE() throws RecognitionException {
		try {
			// org/eclipse/tracecompass/ctf/parser/CTFLexer.g:151:3: ( BACKSLASH 'u' HEX_DIGIT HEX_DIGIT HEX_DIGIT HEX_DIGIT | BACKSLASH 'U' HEX_DIGIT HEX_DIGIT HEX_DIGIT HEX_DIGIT HEX_DIGIT HEX_DIGIT HEX_DIGIT HEX_DIGIT )
			int alt20=2;
			int LA20_0 = input.LA(1);
			if ( (LA20_0=='\\') ) {
				int LA20_1 = input.LA(2);
				if ( (LA20_1=='u') ) {
					alt20=1;
				}
				else if ( (LA20_1=='U') ) {
					alt20=2;
				}

				else {
					int nvaeMark = input.mark();
					try {
						input.consume();
						NoViableAltException nvae =
							new NoViableAltException("", 20, 1, input);
						throw nvae;
					} finally {
						input.rewind(nvaeMark);
					}
				}

			}

			else {
				NoViableAltException nvae =
					new NoViableAltException("", 20, 0, input);
				throw nvae;
			}

			switch (alt20) {
				case 1 :
					// org/eclipse/tracecompass/ctf/parser/CTFLexer.g:151:5: BACKSLASH 'u' HEX_DIGIT HEX_DIGIT HEX_DIGIT HEX_DIGIT
					{
					mBACKSLASH(); 

					match('u'); 
					mHEX_DIGIT(); 

					mHEX_DIGIT(); 

					mHEX_DIGIT(); 

					mHEX_DIGIT(); 

					}
					break;
				case 2 :
					// org/eclipse/tracecompass/ctf/parser/CTFLexer.g:152:5: BACKSLASH 'U' HEX_DIGIT HEX_DIGIT HEX_DIGIT HEX_DIGIT HEX_DIGIT HEX_DIGIT HEX_DIGIT HEX_DIGIT
					{
					mBACKSLASH(); 

					match('U'); 
					mHEX_DIGIT(); 

					mHEX_DIGIT(); 

					mHEX_DIGIT(); 

					mHEX_DIGIT(); 

					mHEX_DIGIT(); 

					mHEX_DIGIT(); 

					mHEX_DIGIT(); 

					mHEX_DIGIT(); 

					}
					break;

			}
		}
		finally {
			// do for sure before leaving
		}
	}
	// $ANTLR end "UNICODE_ESCAPE"

	// $ANTLR start "STRINGPREFIX"
	public final void mSTRINGPREFIX() throws RecognitionException {
		try {
			// org/eclipse/tracecompass/ctf/parser/CTFLexer.g:157:23: ( 'L' )
			// org/eclipse/tracecompass/ctf/parser/CTFLexer.g:157:25: 'L'
			{
			match('L'); 
			}

		}
		finally {
			// do for sure before leaving
		}
	}
	// $ANTLR end "STRINGPREFIX"

	// $ANTLR start "CHARACTER_LITERAL"
	public final void mCHARACTER_LITERAL() throws RecognitionException {
		try {
			int _type = CHARACTER_LITERAL;
			int _channel = DEFAULT_TOKEN_CHANNEL;
			// org/eclipse/tracecompass/ctf/parser/CTFLexer.g:162:19: ( ( STRINGPREFIX )? SINGLEQUOTE ( CHAR_CONTENT )+ SINGLEQUOTE )
			// org/eclipse/tracecompass/ctf/parser/CTFLexer.g:162:21: ( STRINGPREFIX )? SINGLEQUOTE ( CHAR_CONTENT )+ SINGLEQUOTE
			{
			// org/eclipse/tracecompass/ctf/parser/CTFLexer.g:162:21: ( STRINGPREFIX )?
			int alt21=2;
			int LA21_0 = input.LA(1);
			if ( (LA21_0=='L') ) {
				alt21=1;
			}
			switch (alt21) {
				case 1 :
					// org/eclipse/tracecompass/ctf/parser/CTFLexer.g:
					{
					if ( input.LA(1)=='L' ) {
						input.consume();
					}
					else {
						MismatchedSetException mse = new MismatchedSetException(null,input);
						recover(mse);
						throw mse;
					}
					}
					break;

			}

			mSINGLEQUOTE(); 

			// org/eclipse/tracecompass/ctf/parser/CTFLexer.g:162:47: ( CHAR_CONTENT )+
			int cnt22=0;
			loop22:
			while (true) {
				int alt22=2;
				int LA22_0 = input.LA(1);
				if ( ((LA22_0 >= '\u0000' && LA22_0 <= '&')||(LA22_0 >= '(' && LA22_0 <= '\uFFFF')) ) {
					alt22=1;
				}

				switch (alt22) {
				case 1 :
					// org/eclipse/tracecompass/ctf/parser/CTFLexer.g:162:47: CHAR_CONTENT
					{
					mCHAR_CONTENT(); 

					}
					break;

				default :
					if ( cnt22 >= 1 ) break loop22;
					EarlyExitException eee = new EarlyExitException(22, input);
					throw eee;
				}
				cnt22++;
			}

			mSINGLEQUOTE(); 

			}

			state.type = _type;
			state.channel = _channel;
		}
		finally {
			// do for sure before leaving
		}
	}
	// $ANTLR end "CHARACTER_LITERAL"

	// $ANTLR start "CHAR_CONTENT"
	public final void mCHAR_CONTENT() throws RecognitionException {
		try {
			// org/eclipse/tracecompass/ctf/parser/CTFLexer.g:163:23: ( ( ESCAPE_SEQUENCE |~ ( BACKSLASH | SINGLEQUOTE ) ) )
			// org/eclipse/tracecompass/ctf/parser/CTFLexer.g:163:25: ( ESCAPE_SEQUENCE |~ ( BACKSLASH | SINGLEQUOTE ) )
			{
			// org/eclipse/tracecompass/ctf/parser/CTFLexer.g:163:25: ( ESCAPE_SEQUENCE |~ ( BACKSLASH | SINGLEQUOTE ) )
			int alt23=2;
			int LA23_0 = input.LA(1);
			if ( (LA23_0=='\\') ) {
				alt23=1;
			}
			else if ( ((LA23_0 >= '\u0000' && LA23_0 <= '&')||(LA23_0 >= '(' && LA23_0 <= '[')||(LA23_0 >= ']' && LA23_0 <= '\uFFFF')) ) {
				alt23=2;
			}

			else {
				NoViableAltException nvae =
					new NoViableAltException("", 23, 0, input);
				throw nvae;
			}

			switch (alt23) {
				case 1 :
					// org/eclipse/tracecompass/ctf/parser/CTFLexer.g:163:26: ESCAPE_SEQUENCE
					{
					mESCAPE_SEQUENCE(); 

					}
					break;
				case 2 :
					// org/eclipse/tracecompass/ctf/parser/CTFLexer.g:163:44: ~ ( BACKSLASH | SINGLEQUOTE )
					{
					if ( (input.LA(1) >= '\u0000' && input.LA(1) <= '&')||(input.LA(1) >= '(' && input.LA(1) <= '[')||(input.LA(1) >= ']' && input.LA(1) <= '\uFFFF') ) {
						input.consume();
					}
					else {
						MismatchedSetException mse = new MismatchedSetException(null,input);
						recover(mse);
						throw mse;
					}
					}
					break;

			}

			}

		}
		finally {
			// do for sure before leaving
		}
	}
	// $ANTLR end "CHAR_CONTENT"

	// $ANTLR start "SINGLEQUOTE"
	public final void mSINGLEQUOTE() throws RecognitionException {
		try {
			// org/eclipse/tracecompass/ctf/parser/CTFLexer.g:164:22: ( '\\'' )
			// org/eclipse/tracecompass/ctf/parser/CTFLexer.g:164:24: '\\''
			{
			match('\''); 
			}

		}
		finally {
			// do for sure before leaving
		}
	}
	// $ANTLR end "SINGLEQUOTE"

	// $ANTLR start "STRING_LITERAL"
	public final void mSTRING_LITERAL() throws RecognitionException {
		try {
			int _type = STRING_LITERAL;
			int _channel = DEFAULT_TOKEN_CHANNEL;
			// org/eclipse/tracecompass/ctf/parser/CTFLexer.g:169:16: ( ( STRINGPREFIX )? DOUBLEQUOTE ( STRING_CONTENT )* DOUBLEQUOTE )
			// org/eclipse/tracecompass/ctf/parser/CTFLexer.g:169:18: ( STRINGPREFIX )? DOUBLEQUOTE ( STRING_CONTENT )* DOUBLEQUOTE
			{
			// org/eclipse/tracecompass/ctf/parser/CTFLexer.g:169:18: ( STRINGPREFIX )?
			int alt24=2;
			int LA24_0 = input.LA(1);
			if ( (LA24_0=='L') ) {
				alt24=1;
			}
			switch (alt24) {
				case 1 :
					// org/eclipse/tracecompass/ctf/parser/CTFLexer.g:
					{
					if ( input.LA(1)=='L' ) {
						input.consume();
					}
					else {
						MismatchedSetException mse = new MismatchedSetException(null,input);
						recover(mse);
						throw mse;
					}
					}
					break;

			}

			mDOUBLEQUOTE(); 

			// org/eclipse/tracecompass/ctf/parser/CTFLexer.g:169:44: ( STRING_CONTENT )*
			loop25:
			while (true) {
				int alt25=2;
				int LA25_0 = input.LA(1);
				if ( ((LA25_0 >= '\u0000' && LA25_0 <= '!')||(LA25_0 >= '#' && LA25_0 <= '\uFFFF')) ) {
					alt25=1;
				}

				switch (alt25) {
				case 1 :
					// org/eclipse/tracecompass/ctf/parser/CTFLexer.g:169:44: STRING_CONTENT
					{
					mSTRING_CONTENT(); 

					}
					break;

				default :
					break loop25;
				}
			}

			mDOUBLEQUOTE(); 

			}

			state.type = _type;
			state.channel = _channel;
		}
		finally {
			// do for sure before leaving
		}
	}
	// $ANTLR end "STRING_LITERAL"

	// $ANTLR start "STRING_CONTENT"
	public final void mSTRING_CONTENT() throws RecognitionException {
		try {
			// org/eclipse/tracecompass/ctf/parser/CTFLexer.g:170:25: ( ( ESCAPE_SEQUENCE |~ ( BACKSLASH | DOUBLEQUOTE ) ) )
			// org/eclipse/tracecompass/ctf/parser/CTFLexer.g:170:27: ( ESCAPE_SEQUENCE |~ ( BACKSLASH | DOUBLEQUOTE ) )
			{
			// org/eclipse/tracecompass/ctf/parser/CTFLexer.g:170:27: ( ESCAPE_SEQUENCE |~ ( BACKSLASH | DOUBLEQUOTE ) )
			int alt26=2;
			int LA26_0 = input.LA(1);
			if ( (LA26_0=='\\') ) {
				alt26=1;
			}
			else if ( ((LA26_0 >= '\u0000' && LA26_0 <= '!')||(LA26_0 >= '#' && LA26_0 <= '[')||(LA26_0 >= ']' && LA26_0 <= '\uFFFF')) ) {
				alt26=2;
			}

			else {
				NoViableAltException nvae =
					new NoViableAltException("", 26, 0, input);
				throw nvae;
			}

			switch (alt26) {
				case 1 :
					// org/eclipse/tracecompass/ctf/parser/CTFLexer.g:170:28: ESCAPE_SEQUENCE
					{
					mESCAPE_SEQUENCE(); 

					}
					break;
				case 2 :
					// org/eclipse/tracecompass/ctf/parser/CTFLexer.g:170:46: ~ ( BACKSLASH | DOUBLEQUOTE )
					{
					if ( (input.LA(1) >= '\u0000' && input.LA(1) <= '!')||(input.LA(1) >= '#' && input.LA(1) <= '[')||(input.LA(1) >= ']' && input.LA(1) <= '\uFFFF') ) {
						input.consume();
					}
					else {
						MismatchedSetException mse = new MismatchedSetException(null,input);
						recover(mse);
						throw mse;
					}
					}
					break;

			}

			}

		}
		finally {
			// do for sure before leaving
		}
	}
	// $ANTLR end "STRING_CONTENT"

	// $ANTLR start "DOUBLEQUOTE"
	public final void mDOUBLEQUOTE() throws RecognitionException {
		try {
			// org/eclipse/tracecompass/ctf/parser/CTFLexer.g:171:22: ( '\"' )
			// org/eclipse/tracecompass/ctf/parser/CTFLexer.g:171:24: '\"'
			{
			match('\"'); 
			}

		}
		finally {
			// do for sure before leaving
		}
	}
	// $ANTLR end "DOUBLEQUOTE"

	// $ANTLR start "WS"
	public final void mWS() throws RecognitionException {
		try {
			int _type = WS;
			int _channel = DEFAULT_TOKEN_CHANNEL;
			// org/eclipse/tracecompass/ctf/parser/CTFLexer.g:176:4: ( ( ' ' | '\\r' | '\\t' | '\\u000C' | '\\n' ) )
			// org/eclipse/tracecompass/ctf/parser/CTFLexer.g:176:6: ( ' ' | '\\r' | '\\t' | '\\u000C' | '\\n' )
			{
			if ( (input.LA(1) >= '\t' && input.LA(1) <= '\n')||(input.LA(1) >= '\f' && input.LA(1) <= '\r')||input.LA(1)==' ' ) {
				input.consume();
			}
			else {
				MismatchedSetException mse = new MismatchedSetException(null,input);
				recover(mse);
				throw mse;
			}
			 _channel = HIDDEN; 
			}

			state.type = _type;
			state.channel = _channel;
		}
		finally {
			// do for sure before leaving
		}
	}
	// $ANTLR end "WS"

	// $ANTLR start "COMMENT"
	public final void mCOMMENT() throws RecognitionException {
		try {
			int _type = COMMENT;
			int _channel = DEFAULT_TOKEN_CHANNEL;
			// org/eclipse/tracecompass/ctf/parser/CTFLexer.g:183:9: ( COMMENT_OPEN ( . )* COMMENT_CLOSE )
			// org/eclipse/tracecompass/ctf/parser/CTFLexer.g:183:11: COMMENT_OPEN ( . )* COMMENT_CLOSE
			{
			mCOMMENT_OPEN(); 

			// org/eclipse/tracecompass/ctf/parser/CTFLexer.g:183:24: ( . )*
			loop27:
			while (true) {
				int alt27=2;
				int LA27_0 = input.LA(1);
				if ( (LA27_0=='*') ) {
					int LA27_1 = input.LA(2);
					if ( (LA27_1=='/') ) {
						alt27=2;
					}
					else if ( ((LA27_1 >= '\u0000' && LA27_1 <= '.')||(LA27_1 >= '0' && LA27_1 <= '\uFFFF')) ) {
						alt27=1;
					}

				}
				else if ( ((LA27_0 >= '\u0000' && LA27_0 <= ')')||(LA27_0 >= '+' && LA27_0 <= '\uFFFF')) ) {
					alt27=1;
				}

				switch (alt27) {
				case 1 :
					// org/eclipse/tracecompass/ctf/parser/CTFLexer.g:183:24: .
					{
					matchAny(); 
					}
					break;

				default :
					break loop27;
				}
			}

			mCOMMENT_CLOSE(); 

			 _channel = HIDDEN; 
			}

			state.type = _type;
			state.channel = _channel;
		}
		finally {
			// do for sure before leaving
		}
	}
	// $ANTLR end "COMMENT"

	// $ANTLR start "COMMENT_OPEN"
	public final void mCOMMENT_OPEN() throws RecognitionException {
		try {
			// org/eclipse/tracecompass/ctf/parser/CTFLexer.g:184:23: ( '/*' )
			// org/eclipse/tracecompass/ctf/parser/CTFLexer.g:184:25: '/*'
			{
			match("/*"); 

			}

		}
		finally {
			// do for sure before leaving
		}
	}
	// $ANTLR end "COMMENT_OPEN"

	// $ANTLR start "COMMENT_CLOSE"
	public final void mCOMMENT_CLOSE() throws RecognitionException {
		try {
			// org/eclipse/tracecompass/ctf/parser/CTFLexer.g:185:24: ( '*/' )
			// org/eclipse/tracecompass/ctf/parser/CTFLexer.g:185:26: '*/'
			{
			match("*/"); 

			}

		}
		finally {
			// do for sure before leaving
		}
	}
	// $ANTLR end "COMMENT_CLOSE"

	// $ANTLR start "LINE_COMMENT"
	public final void mLINE_COMMENT() throws RecognitionException {
		try {
			int _type = LINE_COMMENT;
			int _channel = DEFAULT_TOKEN_CHANNEL;
			// org/eclipse/tracecompass/ctf/parser/CTFLexer.g:190:14: ( '//' (~ ( '\\n' ) )* '\\n' )
			// org/eclipse/tracecompass/ctf/parser/CTFLexer.g:190:16: '//' (~ ( '\\n' ) )* '\\n'
			{
			match("//"); 

			// org/eclipse/tracecompass/ctf/parser/CTFLexer.g:190:21: (~ ( '\\n' ) )*
			loop28:
			while (true) {
				int alt28=2;
				int LA28_0 = input.LA(1);
				if ( ((LA28_0 >= '\u0000' && LA28_0 <= '\t')||(LA28_0 >= '\u000B' && LA28_0 <= '\uFFFF')) ) {
					alt28=1;
				}

				switch (alt28) {
				case 1 :
					// org/eclipse/tracecompass/ctf/parser/CTFLexer.g:
					{
					if ( (input.LA(1) >= '\u0000' && input.LA(1) <= '\t')||(input.LA(1) >= '\u000B' && input.LA(1) <= '\uFFFF') ) {
						input.consume();
					}
					else {
						MismatchedSetException mse = new MismatchedSetException(null,input);
						recover(mse);
						throw mse;
					}
					}
					break;

				default :
					break loop28;
				}
			}

			match('\n'); 
			 _channel = HIDDEN; 
			}

			state.type = _type;
			state.channel = _channel;
		}
		finally {
			// do for sure before leaving
		}
	}
	// $ANTLR end "LINE_COMMENT"

	// $ANTLR start "IDENTIFIER"
	public final void mIDENTIFIER() throws RecognitionException {
		try {
			int _type = IDENTIFIER;
			int _channel = DEFAULT_TOKEN_CHANNEL;
			// org/eclipse/tracecompass/ctf/parser/CTFLexer.g:195:12: ( NONDIGIT ( NONDIGIT | DIGIT )* )
			// org/eclipse/tracecompass/ctf/parser/CTFLexer.g:195:14: NONDIGIT ( NONDIGIT | DIGIT )*
			{
			mNONDIGIT(); 

			// org/eclipse/tracecompass/ctf/parser/CTFLexer.g:195:23: ( NONDIGIT | DIGIT )*
			loop29:
			while (true) {
				int alt29=2;
				int LA29_0 = input.LA(1);
				if ( ((LA29_0 >= '0' && LA29_0 <= '9')||(LA29_0 >= 'A' && LA29_0 <= 'Z')||LA29_0=='_'||(LA29_0 >= 'a' && LA29_0 <= 'z')) ) {
					alt29=1;
				}

				switch (alt29) {
				case 1 :
					// org/eclipse/tracecompass/ctf/parser/CTFLexer.g:
					{
					if ( (input.LA(1) >= '0' && input.LA(1) <= '9')||(input.LA(1) >= 'A' && input.LA(1) <= 'Z')||input.LA(1)=='_'||(input.LA(1) >= 'a' && input.LA(1) <= 'z') ) {
						input.consume();
					}
					else {
						MismatchedSetException mse = new MismatchedSetException(null,input);
						recover(mse);
						throw mse;
					}
					}
					break;

				default :
					break loop29;
				}
			}

			}

			state.type = _type;
			state.channel = _channel;
		}
		finally {
			// do for sure before leaving
		}
	}
	// $ANTLR end "IDENTIFIER"

	// $ANTLR start "NONDIGIT"
	public final void mNONDIGIT() throws RecognitionException {
		try {
			// org/eclipse/tracecompass/ctf/parser/CTFLexer.g:196:19: ( ( '_' ) | ( 'A' .. 'Z' ) | ( 'a' .. 'z' ) )
			// org/eclipse/tracecompass/ctf/parser/CTFLexer.g:
			{
			if ( (input.LA(1) >= 'A' && input.LA(1) <= 'Z')||input.LA(1)=='_'||(input.LA(1) >= 'a' && input.LA(1) <= 'z') ) {
				input.consume();
			}
			else {
				MismatchedSetException mse = new MismatchedSetException(null,input);
				recover(mse);
				throw mse;
			}
			}

		}
		finally {
			// do for sure before leaving
		}
	}
	// $ANTLR end "NONDIGIT"

	@Override
	public void mTokens() throws RecognitionException {
		// org/eclipse/tracecompass/ctf/parser/CTFLexer.g:1:8: ( ALIGNTOK | CONSTTOK | CHARTOK | DOUBLETOK | ENUMTOK | EVENTTOK | FLOATINGPOINTTOK | FLOATTOK | INTEGERTOK | INTTOK | LONGTOK | SHORTTOK | SIGNEDTOK | STREAMTOK | STRINGTOK | STRUCTTOK | TRACETOK | TYPEALIASTOK | TYPEDEFTOK | UNSIGNEDTOK | VARIANTTOK | VOIDTOK | BOOLTOK | COMPLEXTOK | IMAGINARYTOK | ENVTOK | CLOCKTOK | CALLSITETOK | NANNUMBERTOK | INFINITYTOK | NINFINITYTOK | SEPARATOR | COLON | ELIPSES | ASSIGNMENT | TYPE_ASSIGNMENT | LT | GT | OPENBRAC | CLOSEBRAC | LPAREN | RPAREN | LCURL | RCURL | TERM | POINTER | SIGN | ARROW | DOT | OCTAL_LITERAL | DECIMAL_LITERAL | HEX_LITERAL | CHARACTER_LITERAL | STRING_LITERAL | WS | COMMENT | LINE_COMMENT | IDENTIFIER )
		int alt30=58;
		alt30 = dfa30.predict(input);
		switch (alt30) {
			case 1 :
				// org/eclipse/tracecompass/ctf/parser/CTFLexer.g:1:10: ALIGNTOK
				{
				mALIGNTOK(); 

				}
				break;
			case 2 :
				// org/eclipse/tracecompass/ctf/parser/CTFLexer.g:1:19: CONSTTOK
				{
				mCONSTTOK(); 

				}
				break;
			case 3 :
				// org/eclipse/tracecompass/ctf/parser/CTFLexer.g:1:28: CHARTOK
				{
				mCHARTOK(); 

				}
				break;
			case 4 :
				// org/eclipse/tracecompass/ctf/parser/CTFLexer.g:1:36: DOUBLETOK
				{
				mDOUBLETOK(); 

				}
				break;
			case 5 :
				// org/eclipse/tracecompass/ctf/parser/CTFLexer.g:1:46: ENUMTOK
				{
				mENUMTOK(); 

				}
				break;
			case 6 :
				// org/eclipse/tracecompass/ctf/parser/CTFLexer.g:1:54: EVENTTOK
				{
				mEVENTTOK(); 

				}
				break;
			case 7 :
				// org/eclipse/tracecompass/ctf/parser/CTFLexer.g:1:63: FLOATINGPOINTTOK
				{
				mFLOATINGPOINTTOK(); 

				}
				break;
			case 8 :
				// org/eclipse/tracecompass/ctf/parser/CTFLexer.g:1:80: FLOATTOK
				{
				mFLOATTOK(); 

				}
				break;
			case 9 :
				// org/eclipse/tracecompass/ctf/parser/CTFLexer.g:1:89: INTEGERTOK
				{
				mINTEGERTOK(); 

				}
				break;
			case 10 :
				// org/eclipse/tracecompass/ctf/parser/CTFLexer.g:1:100: INTTOK
				{
				mINTTOK(); 

				}
				break;
			case 11 :
				// org/eclipse/tracecompass/ctf/parser/CTFLexer.g:1:107: LONGTOK
				{
				mLONGTOK(); 

				}
				break;
			case 12 :
				// org/eclipse/tracecompass/ctf/parser/CTFLexer.g:1:115: SHORTTOK
				{
				mSHORTTOK(); 

				}
				break;
			case 13 :
				// org/eclipse/tracecompass/ctf/parser/CTFLexer.g:1:124: SIGNEDTOK
				{
				mSIGNEDTOK(); 

				}
				break;
			case 14 :
				// org/eclipse/tracecompass/ctf/parser/CTFLexer.g:1:134: STREAMTOK
				{
				mSTREAMTOK(); 

				}
				break;
			case 15 :
				// org/eclipse/tracecompass/ctf/parser/CTFLexer.g:1:144: STRINGTOK
				{
				mSTRINGTOK(); 

				}
				break;
			case 16 :
				// org/eclipse/tracecompass/ctf/parser/CTFLexer.g:1:154: STRUCTTOK
				{
				mSTRUCTTOK(); 

				}
				break;
			case 17 :
				// org/eclipse/tracecompass/ctf/parser/CTFLexer.g:1:164: TRACETOK
				{
				mTRACETOK(); 

				}
				break;
			case 18 :
				// org/eclipse/tracecompass/ctf/parser/CTFLexer.g:1:173: TYPEALIASTOK
				{
				mTYPEALIASTOK(); 

				}
				break;
			case 19 :
				// org/eclipse/tracecompass/ctf/parser/CTFLexer.g:1:186: TYPEDEFTOK
				{
				mTYPEDEFTOK(); 

				}
				break;
			case 20 :
				// org/eclipse/tracecompass/ctf/parser/CTFLexer.g:1:197: UNSIGNEDTOK
				{
				mUNSIGNEDTOK(); 

				}
				break;
			case 21 :
				// org/eclipse/tracecompass/ctf/parser/CTFLexer.g:1:209: VARIANTTOK
				{
				mVARIANTTOK(); 

				}
				break;
			case 22 :
				// org/eclipse/tracecompass/ctf/parser/CTFLexer.g:1:220: VOIDTOK
				{
				mVOIDTOK(); 

				}
				break;
			case 23 :
				// org/eclipse/tracecompass/ctf/parser/CTFLexer.g:1:228: BOOLTOK
				{
				mBOOLTOK(); 

				}
				break;
			case 24 :
				// org/eclipse/tracecompass/ctf/parser/CTFLexer.g:1:236: COMPLEXTOK
				{
				mCOMPLEXTOK(); 

				}
				break;
			case 25 :
				// org/eclipse/tracecompass/ctf/parser/CTFLexer.g:1:247: IMAGINARYTOK
				{
				mIMAGINARYTOK(); 

				}
				break;
			case 26 :
				// org/eclipse/tracecompass/ctf/parser/CTFLexer.g:1:260: ENVTOK
				{
				mENVTOK(); 

				}
				break;
			case 27 :
				// org/eclipse/tracecompass/ctf/parser/CTFLexer.g:1:267: CLOCKTOK
				{
				mCLOCKTOK(); 

				}
				break;
			case 28 :
				// org/eclipse/tracecompass/ctf/parser/CTFLexer.g:1:276: CALLSITETOK
				{
				mCALLSITETOK(); 

				}
				break;
			case 29 :
				// org/eclipse/tracecompass/ctf/parser/CTFLexer.g:1:288: NANNUMBERTOK
				{
				mNANNUMBERTOK(); 

				}
				break;
			case 30 :
				// org/eclipse/tracecompass/ctf/parser/CTFLexer.g:1:301: INFINITYTOK
				{
				mINFINITYTOK(); 

				}
				break;
			case 31 :
				// org/eclipse/tracecompass/ctf/parser/CTFLexer.g:1:313: NINFINITYTOK
				{
				mNINFINITYTOK(); 

				}
				break;
			case 32 :
				// org/eclipse/tracecompass/ctf/parser/CTFLexer.g:1:326: SEPARATOR
				{
				mSEPARATOR(); 

				}
				break;
			case 33 :
				// org/eclipse/tracecompass/ctf/parser/CTFLexer.g:1:336: COLON
				{
				mCOLON(); 

				}
				break;
			case 34 :
				// org/eclipse/tracecompass/ctf/parser/CTFLexer.g:1:342: ELIPSES
				{
				mELIPSES(); 

				}
				break;
			case 35 :
				// org/eclipse/tracecompass/ctf/parser/CTFLexer.g:1:350: ASSIGNMENT
				{
				mASSIGNMENT(); 

				}
				break;
			case 36 :
				// org/eclipse/tracecompass/ctf/parser/CTFLexer.g:1:361: TYPE_ASSIGNMENT
				{
				mTYPE_ASSIGNMENT(); 

				}
				break;
			case 37 :
				// org/eclipse/tracecompass/ctf/parser/CTFLexer.g:1:377: LT
				{
				mLT(); 

				}
				break;
			case 38 :
				// org/eclipse/tracecompass/ctf/parser/CTFLexer.g:1:380: GT
				{
				mGT(); 

				}
				break;
			case 39 :
				// org/eclipse/tracecompass/ctf/parser/CTFLexer.g:1:383: OPENBRAC
				{
				mOPENBRAC(); 

				}
				break;
			case 40 :
				// org/eclipse/tracecompass/ctf/parser/CTFLexer.g:1:392: CLOSEBRAC
				{
				mCLOSEBRAC(); 

				}
				break;
			case 41 :
				// org/eclipse/tracecompass/ctf/parser/CTFLexer.g:1:402: LPAREN
				{
				mLPAREN(); 

				}
				break;
			case 42 :
				// org/eclipse/tracecompass/ctf/parser/CTFLexer.g:1:409: RPAREN
				{
				mRPAREN(); 

				}
				break;
			case 43 :
				// org/eclipse/tracecompass/ctf/parser/CTFLexer.g:1:416: LCURL
				{
				mLCURL(); 

				}
				break;
			case 44 :
				// org/eclipse/tracecompass/ctf/parser/CTFLexer.g:1:422: RCURL
				{
				mRCURL(); 

				}
				break;
			case 45 :
				// org/eclipse/tracecompass/ctf/parser/CTFLexer.g:1:428: TERM
				{
				mTERM(); 

				}
				break;
			case 46 :
				// org/eclipse/tracecompass/ctf/parser/CTFLexer.g:1:433: POINTER
				{
				mPOINTER(); 

				}
				break;
			case 47 :
				// org/eclipse/tracecompass/ctf/parser/CTFLexer.g:1:441: SIGN
				{
				mSIGN(); 

				}
				break;
			case 48 :
				// org/eclipse/tracecompass/ctf/parser/CTFLexer.g:1:446: ARROW
				{
				mARROW(); 

				}
				break;
			case 49 :
				// org/eclipse/tracecompass/ctf/parser/CTFLexer.g:1:452: DOT
				{
				mDOT(); 

				}
				break;
			case 50 :
				// org/eclipse/tracecompass/ctf/parser/CTFLexer.g:1:456: OCTAL_LITERAL
				{
				mOCTAL_LITERAL(); 

				}
				break;
			case 51 :
				// org/eclipse/tracecompass/ctf/parser/CTFLexer.g:1:470: DECIMAL_LITERAL
				{
				mDECIMAL_LITERAL(); 

				}
				break;
			case 52 :
				// org/eclipse/tracecompass/ctf/parser/CTFLexer.g:1:486: HEX_LITERAL
				{
				mHEX_LITERAL(); 

				}
				break;
			case 53 :
				// org/eclipse/tracecompass/ctf/parser/CTFLexer.g:1:498: CHARACTER_LITERAL
				{
				mCHARACTER_LITERAL(); 

				}
				break;
			case 54 :
				// org/eclipse/tracecompass/ctf/parser/CTFLexer.g:1:516: STRING_LITERAL
				{
				mSTRING_LITERAL(); 

				}
				break;
			case 55 :
				// org/eclipse/tracecompass/ctf/parser/CTFLexer.g:1:531: WS
				{
				mWS(); 

				}
				break;
			case 56 :
				// org/eclipse/tracecompass/ctf/parser/CTFLexer.g:1:534: COMMENT
				{
				mCOMMENT(); 

				}
				break;
			case 57 :
				// org/eclipse/tracecompass/ctf/parser/CTFLexer.g:1:542: LINE_COMMENT
				{
				mLINE_COMMENT(); 

				}
				break;
			case 58 :
				// org/eclipse/tracecompass/ctf/parser/CTFLexer.g:1:555: IDENTIFIER
				{
				mIDENTIFIER(); 

				}
				break;

		}
	}


	protected DFA30 dfa30 = new DFA30(this);
	static final String DFA30_eotS =
		"\1\uffff\15\45\2\76\1\uffff\1\102\1\104\13\uffff\1\37\1\uffff\1\45\5\uffff"+
		"\27\45\11\uffff\1\144\2\uffff\7\45\1\154\2\45\1\160\14\45\1\177\3\144"+
		"\1\uffff\2\45\1\u0087\3\45\1\u008b\1\uffff\3\45\1\uffff\1\u008f\11\45"+
		"\1\u009a\3\45\1\uffff\1\144\1\uffff\3\144\1\u00a0\1\u00a1\1\uffff\1\u00a2"+
		"\2\45\1\uffff\1\u00a5\1\u00a7\1\45\1\uffff\1\u00a9\4\45\1\u00ae\4\45\1"+
		"\uffff\1\u00b3\2\45\5\uffff\1\45\1\u00b7\1\uffff\1\45\1\uffff\1\45\1\uffff"+
		"\1\u00ba\1\u00bb\1\u00bc\1\u00bd\1\uffff\4\45\1\uffff\3\45\1\uffff\1\45"+
		"\1\u00c6\4\uffff\1\45\1\u00c8\1\45\1\u00ca\2\45\1\u00cd\1\45\1\uffff\1"+
		"\45\1\uffff\1\u00d0\1\uffff\1\u00d1\1\45\1\uffff\1\45\1\u00d4\2\uffff"+
		"\2\45\1\uffff\1\u00d7\1\45\1\uffff\2\45\1\u00db\1\uffff";
	static final String DFA30_eofS =
		"\u00dc\uffff";
	static final String DFA30_minS =
		"\1\11\1\154\1\141\1\157\1\156\1\154\1\156\1\157\1\150\1\162\1\156\1\141"+
		"\1\102\1\141\1\151\1\76\1\uffff\1\75\1\56\13\uffff\1\60\1\uffff\1\42\3"+
		"\uffff\1\52\1\uffff\1\151\1\156\1\141\1\157\1\154\2\165\1\145\1\157\1"+
		"\164\1\156\1\157\1\147\1\162\1\141\1\160\1\163\1\162\1\151\2\157\1\155"+
		"\1\116\11\uffff\1\60\2\uffff\1\147\1\163\1\162\1\143\1\154\1\142\1\155"+
		"\1\60\1\156\1\141\1\60\1\147\1\162\1\156\1\145\1\143\1\145\2\151\1\144"+
		"\1\157\1\155\1\141\1\60\1\125\2\114\1\uffff\1\156\1\164\1\60\1\153\1\163"+
		"\1\154\1\60\1\uffff\2\164\1\147\1\uffff\1\60\1\164\1\145\1\141\1\156\1"+
		"\143\1\145\1\141\1\147\1\141\1\60\1\154\1\160\1\147\1\uffff\1\125\1\uffff"+
		"\1\125\1\154\1\114\2\60\1\uffff\1\60\1\151\1\145\1\uffff\2\60\1\145\1"+
		"\uffff\1\60\1\144\1\155\1\147\1\164\1\60\1\154\1\145\2\156\1\uffff\1\60"+
		"\1\154\1\151\5\uffff\1\164\1\60\1\uffff\1\156\1\uffff\1\162\1\uffff\4"+
		"\60\1\uffff\1\151\1\146\1\145\1\164\1\uffff\1\145\1\156\1\145\1\uffff"+
		"\1\147\1\60\4\uffff\1\141\1\60\1\144\1\60\1\170\1\141\1\60\1\137\1\uffff"+
		"\1\163\1\uffff\1\60\1\uffff\1\60\1\162\1\uffff\1\160\1\60\2\uffff\1\171"+
		"\1\157\1\uffff\1\60\1\151\1\uffff\1\156\1\164\1\60\1\uffff";
	static final String DFA30_maxS =
		"\1\175\1\154\2\157\1\166\1\154\1\156\1\157\1\164\1\171\1\156\1\157\1\111"+
		"\1\141\2\151\1\uffff\1\75\1\56\13\uffff\1\170\1\uffff\1\47\3\uffff\1\57"+
		"\1\uffff\1\151\1\156\1\141\1\157\1\154\1\165\1\166\1\145\1\157\1\164\1"+
		"\156\1\157\1\147\1\162\1\141\1\160\1\163\1\162\1\151\2\157\1\155\1\116"+
		"\11\uffff\1\165\2\uffff\1\147\1\163\1\162\1\143\1\154\1\142\1\155\1\172"+
		"\1\156\1\141\1\172\1\147\1\162\1\156\1\165\1\143\1\145\2\151\1\144\1\157"+
		"\1\155\1\141\1\172\2\165\1\154\1\uffff\1\156\1\164\1\172\1\153\1\163\1"+
		"\154\1\172\1\uffff\2\164\1\147\1\uffff\1\172\1\164\1\145\1\141\1\156\1"+
		"\143\1\145\1\144\1\147\1\141\1\172\1\154\1\160\1\147\1\uffff\1\165\1\uffff"+
		"\1\165\1\154\1\114\2\172\1\uffff\1\172\1\151\1\145\1\uffff\2\172\1\145"+
		"\1\uffff\1\172\1\144\1\155\1\147\1\164\1\172\1\154\1\145\2\156\1\uffff"+
		"\1\172\1\154\1\151\5\uffff\1\164\1\172\1\uffff\1\156\1\uffff\1\162\1\uffff"+
		"\4\172\1\uffff\1\151\1\146\1\145\1\164\1\uffff\1\145\1\156\1\145\1\uffff"+
		"\1\147\1\172\4\uffff\1\141\1\172\1\144\1\172\1\170\1\141\1\172\1\137\1"+
		"\uffff\1\163\1\uffff\1\172\1\uffff\1\172\1\162\1\uffff\1\160\1\172\2\uffff"+
		"\1\171\1\157\1\uffff\1\172\1\151\1\uffff\1\156\1\164\1\172\1\uffff";
	static final String DFA30_acceptS =
		"\20\uffff\1\40\2\uffff\1\43\1\45\1\46\1\47\1\50\1\51\1\52\1\53\1\54\1"+
		"\55\1\56\1\uffff\1\63\1\uffff\1\65\1\66\1\67\1\uffff\1\72\27\uffff\1\36"+
		"\1\57\1\37\1\60\1\44\1\41\1\42\1\61\1\64\1\uffff\1\70\1\71\33\uffff\1"+
		"\62\7\uffff\1\32\3\uffff\1\12\16\uffff\1\35\1\uffff\1\62\5\uffff\1\3\3"+
		"\uffff\1\5\3\uffff\1\13\12\uffff\1\26\3\uffff\2\62\1\1\1\2\1\33\2\uffff"+
		"\1\6\1\uffff\1\10\1\uffff\1\14\4\uffff\1\21\4\uffff\1\27\3\uffff\1\4\2"+
		"\uffff\1\15\1\16\1\17\1\20\10\uffff\1\11\1\uffff\1\23\1\uffff\1\25\2\uffff"+
		"\1\34\2\uffff\1\24\1\30\2\uffff\1\22\2\uffff\1\31\3\uffff\1\7";
	static final String DFA30_specialS =
		"\u00dc\uffff}>";
	static final String[] DFA30_transitionS = {
			"\2\43\1\uffff\2\43\22\uffff\1\43\1\uffff\1\42\4\uffff\1\41\1\30\1\31"+
			"\1\35\1\16\1\20\1\17\1\22\1\44\1\36\11\37\1\21\1\34\1\24\1\23\1\25\2"+
			"\uffff\13\45\1\40\1\45\1\15\14\45\1\26\1\uffff\1\27\1\uffff\1\14\1\uffff"+
			"\1\1\1\45\1\2\1\3\1\4\1\5\2\45\1\6\2\45\1\7\6\45\1\10\1\11\1\12\1\13"+
			"\4\45\1\32\1\uffff\1\33",
			"\1\46",
			"\1\52\6\uffff\1\50\3\uffff\1\51\2\uffff\1\47",
			"\1\53",
			"\1\54\7\uffff\1\55",
			"\1\56",
			"\1\57",
			"\1\60",
			"\1\61\1\62\12\uffff\1\63",
			"\1\64\6\uffff\1\65",
			"\1\66",
			"\1\67\15\uffff\1\70",
			"\1\71\1\72\5\uffff\1\73",
			"\1\74",
			"\1\75",
			"\1\100\52\uffff\1\77",
			"",
			"\1\101",
			"\1\103",
			"",
			"",
			"",
			"",
			"",
			"",
			"",
			"",
			"",
			"",
			"",
			"\10\106\40\uffff\1\105\37\uffff\1\105",
			"",
			"\1\42\4\uffff\1\41",
			"",
			"",
			"",
			"\1\107\4\uffff\1\110",
			"",
			"\1\111",
			"\1\112",
			"\1\113",
			"\1\114",
			"\1\115",
			"\1\116",
			"\1\117\1\120",
			"\1\121",
			"\1\122",
			"\1\123",
			"\1\124",
			"\1\125",
			"\1\126",
			"\1\127",
			"\1\130",
			"\1\131",
			"\1\132",
			"\1\133",
			"\1\134",
			"\1\135",
			"\1\136",
			"\1\137",
			"\1\140",
			"",
			"",
			"",
			"",
			"",
			"",
			"",
			"",
			"",
			"\10\106\2\37\22\uffff\1\142\10\uffff\1\143\26\uffff\1\141\10\uffff\1"+
			"\143",
			"",
			"",
			"\1\145",
			"\1\146",
			"\1\147",
			"\1\150",
			"\1\151",
			"\1\152",
			"\1\153",
			"\12\45\7\uffff\32\45\4\uffff\1\45\1\uffff\32\45",
			"\1\155",
			"\1\156",
			"\12\45\7\uffff\32\45\4\uffff\1\45\1\uffff\4\45\1\157\25\45",
			"\1\161",
			"\1\162",
			"\1\163",
			"\1\164\3\uffff\1\165\13\uffff\1\166",
			"\1\167",
			"\1\170",
			"\1\171",
			"\1\172",
			"\1\173",
			"\1\174",
			"\1\175",
			"\1\176",
			"\12\45\7\uffff\32\45\4\uffff\1\45\1\uffff\32\45",
			"\1\u0081\26\uffff\1\u0080\10\uffff\1\u0081",
			"\1\u0082\10\uffff\1\u0081\37\uffff\1\u0081",
			"\1\u0084\37\uffff\1\u0083",
			"",
			"\1\u0085",
			"\1\u0086",
			"\12\45\7\uffff\32\45\4\uffff\1\45\1\uffff\32\45",
			"\1\u0088",
			"\1\u0089",
			"\1\u008a",
			"\12\45\7\uffff\32\45\4\uffff\1\45\1\uffff\32\45",
			"",
			"\1\u008c",
			"\1\u008d",
			"\1\u008e",
			"",
			"\12\45\7\uffff\32\45\4\uffff\1\45\1\uffff\32\45",
			"\1\u0090",
			"\1\u0091",
			"\1\u0092",
			"\1\u0093",
			"\1\u0094",
			"\1\u0095",
			"\1\u0096\2\uffff\1\u0097",
			"\1\u0098",
			"\1\u0099",
			"\12\45\7\uffff\32\45\4\uffff\1\45\1\uffff\32\45",
			"\1\u009b",
			"\1\u009c",
			"\1\u009d",
			"",
			"\1\u0081\37\uffff\1\u0081",
			"",
			"\1\u0081\37\uffff\1\u0081",
			"\1\u009e",
			"\1\u009f",
			"\12\45\7\uffff\32\45\4\uffff\1\45\1\uffff\32\45",
			"\12\45\7\uffff\32\45\4\uffff\1\45\1\uffff\32\45",
			"",
			"\12\45\7\uffff\32\45\4\uffff\1\45\1\uffff\32\45",
			"\1\u00a3",
			"\1\u00a4",
			"",
			"\12\45\7\uffff\32\45\4\uffff\1\45\1\uffff\32\45",
			"\12\45\7\uffff\32\45\4\uffff\1\45\1\uffff\10\45\1\u00a6\21\45",
			"\1\u00a8",
			"",
			"\12\45\7\uffff\32\45\4\uffff\1\45\1\uffff\32\45",
			"\1\u00aa",
			"\1\u00ab",
			"\1\u00ac",
			"\1\u00ad",
			"\12\45\7\uffff\32\45\4\uffff\1\45\1\uffff\32\45",
			"\1\u00af",
			"\1\u00b0",
			"\1\u00b1",
			"\1\u00b2",
			"",
			"\12\45\7\uffff\32\45\4\uffff\1\45\1\uffff\32\45",
			"\1\u00b4",
			"\1\u00b5",
			"",
			"",
			"",
			"",
			"",
			"\1\u00b6",
			"\12\45\7\uffff\32\45\4\uffff\1\45\1\uffff\32\45",
			"",
			"\1\u00b8",
			"",
			"\1\u00b9",
			"",
			"\12\45\7\uffff\32\45\4\uffff\1\45\1\uffff\32\45",
			"\12\45\7\uffff\32\45\4\uffff\1\45\1\uffff\32\45",
			"\12\45\7\uffff\32\45\4\uffff\1\45\1\uffff\32\45",
			"\12\45\7\uffff\32\45\4\uffff\1\45\1\uffff\32\45",
			"",
			"\1\u00be",
			"\1\u00bf",
			"\1\u00c0",
			"\1\u00c1",
			"",
			"\1\u00c2",
			"\1\u00c3",
			"\1\u00c4",
			"",
			"\1\u00c5",
			"\12\45\7\uffff\32\45\4\uffff\1\45\1\uffff\32\45",
			"",
			"",
			"",
			"",
			"\1\u00c7",
			"\12\45\7\uffff\32\45\4\uffff\1\45\1\uffff\32\45",
			"\1\u00c9",
			"\12\45\7\uffff\32\45\4\uffff\1\45\1\uffff\32\45",
			"\1\u00cb",
			"\1\u00cc",
			"\12\45\7\uffff\32\45\4\uffff\1\45\1\uffff\32\45",
			"\1\u00ce",
			"",
			"\1\u00cf",
			"",
			"\12\45\7\uffff\32\45\4\uffff\1\45\1\uffff\32\45",
			"",
			"\12\45\7\uffff\32\45\4\uffff\1\45\1\uffff\32\45",
			"\1\u00d2",
			"",
			"\1\u00d3",
			"\12\45\7\uffff\32\45\4\uffff\1\45\1\uffff\32\45",
			"",
			"",
			"\1\u00d5",
			"\1\u00d6",
			"",
			"\12\45\7\uffff\32\45\4\uffff\1\45\1\uffff\32\45",
			"\1\u00d8",
			"",
			"\1\u00d9",
			"\1\u00da",
			"\12\45\7\uffff\32\45\4\uffff\1\45\1\uffff\32\45",
			""
	};

	static final short[] DFA30_eot = DFA.unpackEncodedString(DFA30_eotS);
	static final short[] DFA30_eof = DFA.unpackEncodedString(DFA30_eofS);
	static final char[] DFA30_min = DFA.unpackEncodedStringToUnsignedChars(DFA30_minS);
	static final char[] DFA30_max = DFA.unpackEncodedStringToUnsignedChars(DFA30_maxS);
	static final short[] DFA30_accept = DFA.unpackEncodedString(DFA30_acceptS);
	static final short[] DFA30_special = DFA.unpackEncodedString(DFA30_specialS);
	static final short[][] DFA30_transition;

	static {
		int numStates = DFA30_transitionS.length;
		DFA30_transition = new short[numStates][];
		for (int i=0; i<numStates; i++) {
			DFA30_transition[i] = DFA.unpackEncodedString(DFA30_transitionS[i]);
		}
	}

	protected class DFA30 extends DFA {

		public DFA30(BaseRecognizer recognizer) {
			this.recognizer = recognizer;
			this.decisionNumber = 30;
			this.eot = DFA30_eot;
			this.eof = DFA30_eof;
			this.min = DFA30_min;
			this.max = DFA30_max;
			this.accept = DFA30_accept;
			this.special = DFA30_special;
			this.transition = DFA30_transition;
		}
		@Override
		public String getDescription() {
			return "1:1: Tokens : ( ALIGNTOK | CONSTTOK | CHARTOK | DOUBLETOK | ENUMTOK | EVENTTOK | FLOATINGPOINTTOK | FLOATTOK | INTEGERTOK | INTTOK | LONGTOK | SHORTTOK | SIGNEDTOK | STREAMTOK | STRINGTOK | STRUCTTOK | TRACETOK | TYPEALIASTOK | TYPEDEFTOK | UNSIGNEDTOK | VARIANTTOK | VOIDTOK | BOOLTOK | COMPLEXTOK | IMAGINARYTOK | ENVTOK | CLOCKTOK | CALLSITETOK | NANNUMBERTOK | INFINITYTOK | NINFINITYTOK | SEPARATOR | COLON | ELIPSES | ASSIGNMENT | TYPE_ASSIGNMENT | LT | GT | OPENBRAC | CLOSEBRAC | LPAREN | RPAREN | LCURL | RCURL | TERM | POINTER | SIGN | ARROW | DOT | OCTAL_LITERAL | DECIMAL_LITERAL | HEX_LITERAL | CHARACTER_LITERAL | STRING_LITERAL | WS | COMMENT | LINE_COMMENT | IDENTIFIER );";
		}
	}

}
