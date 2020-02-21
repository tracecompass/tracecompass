// $ANTLR 3.5.2 org/eclipse/tracecompass/tmf/filter/parser/FilterParser.g 2019-07-05 11:56:36

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


import org.antlr.runtime.*;
import java.util.Stack;
import java.util.List;
import java.util.ArrayList;

@SuppressWarnings("all")
public class FilterParserLexer extends Lexer {
	public static final int EOF=-1;
	public static final int T__21=21;
	public static final int T__22=22;
	public static final int T__23=23;
	public static final int CONSTANT=4;
	public static final int LOGICAL=5;
	public static final int OP=6;
	public static final int OPERATION=7;
	public static final int OPERATION1=8;
	public static final int OPERATION2=9;
	public static final int OPERATION3=10;
	public static final int OPERATION4=11;
	public static final int OPERATION5=12;
	public static final int OP_NEGATE=13;
	public static final int OP_PRESENT=14;
	public static final int PAR_CONSTANT=15;
	public static final int ROOT1=16;
	public static final int ROOT2=17;
	public static final int SEPARATOR=18;
	public static final int TEXT=19;
	public static final int WS=20;

	private IErrorListener errListener;

	public void setErrorListener(IErrorListener listener) {
	    errListener = listener;
	}

	@Override
	public void reportError(RecognitionException e) {
	    errListener.error(e);
	}


	// delegates
	// delegators
	public Lexer[] getDelegates() {
		return new Lexer[] {};
	}

	public FilterParserLexer() {} 
	public FilterParserLexer(CharStream input) {
		this(input, new RecognizerSharedState());
	}
	public FilterParserLexer(CharStream input, RecognizerSharedState state) {
		super(input,state);
	}
	@Override public String getGrammarFileName() { return "org/eclipse/tracecompass/tmf/filter/parser/FilterParser.g"; }

	// $ANTLR start "T__21"
	public final void mT__21() throws RecognitionException {
		try {
			int _type = T__21;
			int _channel = DEFAULT_TOKEN_CHANNEL;
			// org/eclipse/tracecompass/tmf/filter/parser/FilterParser.g:37:7: ( '\"' )
			// org/eclipse/tracecompass/tmf/filter/parser/FilterParser.g:37:9: '\"'
			{
			match('\"'); 
			}

			state.type = _type;
			state.channel = _channel;
		}
		finally {
			// do for sure before leaving
		}
	}
	// $ANTLR end "T__21"

	// $ANTLR start "T__22"
	public final void mT__22() throws RecognitionException {
		try {
			int _type = T__22;
			int _channel = DEFAULT_TOKEN_CHANNEL;
			// org/eclipse/tracecompass/tmf/filter/parser/FilterParser.g:38:7: ( '(' )
			// org/eclipse/tracecompass/tmf/filter/parser/FilterParser.g:38:9: '('
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
	// $ANTLR end "T__22"

	// $ANTLR start "T__23"
	public final void mT__23() throws RecognitionException {
		try {
			int _type = T__23;
			int _channel = DEFAULT_TOKEN_CHANNEL;
			// org/eclipse/tracecompass/tmf/filter/parser/FilterParser.g:39:7: ( ')' )
			// org/eclipse/tracecompass/tmf/filter/parser/FilterParser.g:39:9: ')'
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
	// $ANTLR end "T__23"

	// $ANTLR start "SEPARATOR"
	public final void mSEPARATOR() throws RecognitionException {
		try {
			int _type = SEPARATOR;
			int _channel = DEFAULT_TOKEN_CHANNEL;
			// org/eclipse/tracecompass/tmf/filter/parser/FilterParser.g:91:12: ( '||' | '&&' )
			int alt1=2;
			int LA1_0 = input.LA(1);
			if ( (LA1_0=='|') ) {
				alt1=1;
			}
			else if ( (LA1_0=='&') ) {
				alt1=2;
			}

			else {
				NoViableAltException nvae =
					new NoViableAltException("", 1, 0, input);
				throw nvae;
			}

			switch (alt1) {
				case 1 :
					// org/eclipse/tracecompass/tmf/filter/parser/FilterParser.g:91:13: '||'
					{
					match("||"); 

					}
					break;
				case 2 :
					// org/eclipse/tracecompass/tmf/filter/parser/FilterParser.g:91:20: '&&'
					{
					match("&&"); 

					}
					break;

			}
			state.type = _type;
			state.channel = _channel;
		}
		finally {
			// do for sure before leaving
		}
	}
	// $ANTLR end "SEPARATOR"

	// $ANTLR start "OP_PRESENT"
	public final void mOP_PRESENT() throws RecognitionException {
		try {
			int _type = OP_PRESENT;
			int _channel = DEFAULT_TOKEN_CHANNEL;
			// org/eclipse/tracecompass/tmf/filter/parser/FilterParser.g:104:12: ( 'present' )
			// org/eclipse/tracecompass/tmf/filter/parser/FilterParser.g:104:14: 'present'
			{
			match("present"); 

			}

			state.type = _type;
			state.channel = _channel;
		}
		finally {
			// do for sure before leaving
		}
	}
	// $ANTLR end "OP_PRESENT"

	// $ANTLR start "OP_NEGATE"
	public final void mOP_NEGATE() throws RecognitionException {
		try {
			int _type = OP_NEGATE;
			int _channel = DEFAULT_TOKEN_CHANNEL;
			// org/eclipse/tracecompass/tmf/filter/parser/FilterParser.g:105:12: ( '!' )
			// org/eclipse/tracecompass/tmf/filter/parser/FilterParser.g:105:14: '!'
			{
			match('!'); 
			}

			state.type = _type;
			state.channel = _channel;
		}
		finally {
			// do for sure before leaving
		}
	}
	// $ANTLR end "OP_NEGATE"

	// $ANTLR start "OP"
	public final void mOP() throws RecognitionException {
		try {
			int _type = OP;
			int _channel = DEFAULT_TOKEN_CHANNEL;
			// org/eclipse/tracecompass/tmf/filter/parser/FilterParser.g:106:12: ( '==' | '!=' | 'contains' | 'matches' | '>' | '<' )
			int alt2=6;
			switch ( input.LA(1) ) {
			case '=':
				{
				alt2=1;
				}
				break;
			case '!':
				{
				alt2=2;
				}
				break;
			case 'c':
				{
				alt2=3;
				}
				break;
			case 'm':
				{
				alt2=4;
				}
				break;
			case '>':
				{
				alt2=5;
				}
				break;
			case '<':
				{
				alt2=6;
				}
				break;
			default:
				NoViableAltException nvae =
					new NoViableAltException("", 2, 0, input);
				throw nvae;
			}
			switch (alt2) {
				case 1 :
					// org/eclipse/tracecompass/tmf/filter/parser/FilterParser.g:106:14: '=='
					{
					match("=="); 

					}
					break;
				case 2 :
					// org/eclipse/tracecompass/tmf/filter/parser/FilterParser.g:106:21: '!='
					{
					match("!="); 

					}
					break;
				case 3 :
					// org/eclipse/tracecompass/tmf/filter/parser/FilterParser.g:106:28: 'contains'
					{
					match("contains"); 

					}
					break;
				case 4 :
					// org/eclipse/tracecompass/tmf/filter/parser/FilterParser.g:106:41: 'matches'
					{
					match("matches"); 

					}
					break;
				case 5 :
					// org/eclipse/tracecompass/tmf/filter/parser/FilterParser.g:106:53: '>'
					{
					match('>'); 
					}
					break;
				case 6 :
					// org/eclipse/tracecompass/tmf/filter/parser/FilterParser.g:106:59: '<'
					{
					match('<'); 
					}
					break;

			}
			state.type = _type;
			state.channel = _channel;
		}
		finally {
			// do for sure before leaving
		}
	}
	// $ANTLR end "OP"

	// $ANTLR start "TEXT"
	public final void mTEXT() throws RecognitionException {
		try {
			int _type = TEXT;
			int _channel = DEFAULT_TOKEN_CHANNEL;
			// org/eclipse/tracecompass/tmf/filter/parser/FilterParser.g:107:8: ( ( ( 'a' .. 'z' ) | ( 'A' .. 'Z' ) | ( '0' .. '9' ) | '-' | '_' | '[' | ']' | '.' | '*' | '$' | '^' | '|' | '\\\\' | '{' | '}' | '?' | '+' | ':' | ';' )+ )
			// org/eclipse/tracecompass/tmf/filter/parser/FilterParser.g:107:10: ( ( 'a' .. 'z' ) | ( 'A' .. 'Z' ) | ( '0' .. '9' ) | '-' | '_' | '[' | ']' | '.' | '*' | '$' | '^' | '|' | '\\\\' | '{' | '}' | '?' | '+' | ':' | ';' )+
			{
			// org/eclipse/tracecompass/tmf/filter/parser/FilterParser.g:107:10: ( ( 'a' .. 'z' ) | ( 'A' .. 'Z' ) | ( '0' .. '9' ) | '-' | '_' | '[' | ']' | '.' | '*' | '$' | '^' | '|' | '\\\\' | '{' | '}' | '?' | '+' | ':' | ';' )+
			int cnt3=0;
			loop3:
			while (true) {
				int alt3=2;
				int LA3_0 = input.LA(1);
				if ( (LA3_0=='$'||(LA3_0 >= '*' && LA3_0 <= '+')||(LA3_0 >= '-' && LA3_0 <= '.')||(LA3_0 >= '0' && LA3_0 <= ';')||LA3_0=='?'||(LA3_0 >= 'A' && LA3_0 <= '_')||(LA3_0 >= 'a' && LA3_0 <= '}')) ) {
					alt3=1;
				}

				switch (alt3) {
				case 1 :
					// org/eclipse/tracecompass/tmf/filter/parser/FilterParser.g:
					{
					if ( input.LA(1)=='$'||(input.LA(1) >= '*' && input.LA(1) <= '+')||(input.LA(1) >= '-' && input.LA(1) <= '.')||(input.LA(1) >= '0' && input.LA(1) <= ';')||input.LA(1)=='?'||(input.LA(1) >= 'A' && input.LA(1) <= '_')||(input.LA(1) >= 'a' && input.LA(1) <= '}') ) {
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

			}

			state.type = _type;
			state.channel = _channel;
		}
		finally {
			// do for sure before leaving
		}
	}
	// $ANTLR end "TEXT"

	// $ANTLR start "WS"
	public final void mWS() throws RecognitionException {
		try {
			int _type = WS;
			int _channel = DEFAULT_TOKEN_CHANNEL;
			// org/eclipse/tracecompass/tmf/filter/parser/FilterParser.g:109:12: ( ( ' ' | '\\t' | '\\r' | '\\n' )+ )
			// org/eclipse/tracecompass/tmf/filter/parser/FilterParser.g:109:14: ( ' ' | '\\t' | '\\r' | '\\n' )+
			{
			// org/eclipse/tracecompass/tmf/filter/parser/FilterParser.g:109:14: ( ' ' | '\\t' | '\\r' | '\\n' )+
			int cnt4=0;
			loop4:
			while (true) {
				int alt4=2;
				int LA4_0 = input.LA(1);
				if ( ((LA4_0 >= '\t' && LA4_0 <= '\n')||LA4_0=='\r'||LA4_0==' ') ) {
					alt4=1;
				}

				switch (alt4) {
				case 1 :
					// org/eclipse/tracecompass/tmf/filter/parser/FilterParser.g:
					{
					if ( (input.LA(1) >= '\t' && input.LA(1) <= '\n')||input.LA(1)=='\r'||input.LA(1)==' ' ) {
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
					if ( cnt4 >= 1 ) break loop4;
					EarlyExitException eee = new EarlyExitException(4, input);
					throw eee;
				}
				cnt4++;
			}

			 skip(); 
			}

			state.type = _type;
			state.channel = _channel;
		}
		finally {
			// do for sure before leaving
		}
	}
	// $ANTLR end "WS"

	@Override
	public void mTokens() throws RecognitionException {
		// org/eclipse/tracecompass/tmf/filter/parser/FilterParser.g:1:8: ( T__21 | T__22 | T__23 | SEPARATOR | OP_PRESENT | OP_NEGATE | OP | TEXT | WS )
		int alt5=9;
		switch ( input.LA(1) ) {
		case '\"':
			{
			alt5=1;
			}
			break;
		case '(':
			{
			alt5=2;
			}
			break;
		case ')':
			{
			alt5=3;
			}
			break;
		case '|':
			{
			int LA5_4 = input.LA(2);
			if ( (LA5_4=='|') ) {
				int LA5_13 = input.LA(3);
				if ( (LA5_13=='$'||(LA5_13 >= '*' && LA5_13 <= '+')||(LA5_13 >= '-' && LA5_13 <= '.')||(LA5_13 >= '0' && LA5_13 <= ';')||LA5_13=='?'||(LA5_13 >= 'A' && LA5_13 <= '_')||(LA5_13 >= 'a' && LA5_13 <= '}')) ) {
					alt5=8;
				}

				else {
					alt5=4;
				}

			}

			else {
				alt5=8;
			}

			}
			break;
		case '&':
			{
			alt5=4;
			}
			break;
		case 'p':
			{
			int LA5_6 = input.LA(2);
			if ( (LA5_6=='r') ) {
				int LA5_14 = input.LA(3);
				if ( (LA5_14=='e') ) {
					int LA5_18 = input.LA(4);
					if ( (LA5_18=='s') ) {
						int LA5_21 = input.LA(5);
						if ( (LA5_21=='e') ) {
							int LA5_24 = input.LA(6);
							if ( (LA5_24=='n') ) {
								int LA5_27 = input.LA(7);
								if ( (LA5_27=='t') ) {
									int LA5_30 = input.LA(8);
									if ( (LA5_30=='$'||(LA5_30 >= '*' && LA5_30 <= '+')||(LA5_30 >= '-' && LA5_30 <= '.')||(LA5_30 >= '0' && LA5_30 <= ';')||LA5_30=='?'||(LA5_30 >= 'A' && LA5_30 <= '_')||(LA5_30 >= 'a' && LA5_30 <= '}')) ) {
										alt5=8;
									}

									else {
										alt5=5;
									}

								}

								else {
									alt5=8;
								}

							}

							else {
								alt5=8;
							}

						}

						else {
							alt5=8;
						}

					}

					else {
						alt5=8;
					}

				}

				else {
					alt5=8;
				}

			}

			else {
				alt5=8;
			}

			}
			break;
		case '!':
			{
			int LA5_7 = input.LA(2);
			if ( (LA5_7=='=') ) {
				alt5=7;
			}

			else {
				alt5=6;
			}

			}
			break;
		case '<':
		case '=':
		case '>':
			{
			alt5=7;
			}
			break;
		case 'c':
			{
			int LA5_9 = input.LA(2);
			if ( (LA5_9=='o') ) {
				int LA5_16 = input.LA(3);
				if ( (LA5_16=='n') ) {
					int LA5_19 = input.LA(4);
					if ( (LA5_19=='t') ) {
						int LA5_22 = input.LA(5);
						if ( (LA5_22=='a') ) {
							int LA5_25 = input.LA(6);
							if ( (LA5_25=='i') ) {
								int LA5_28 = input.LA(7);
								if ( (LA5_28=='n') ) {
									int LA5_31 = input.LA(8);
									if ( (LA5_31=='s') ) {
										int LA5_34 = input.LA(9);
										if ( (LA5_34=='$'||(LA5_34 >= '*' && LA5_34 <= '+')||(LA5_34 >= '-' && LA5_34 <= '.')||(LA5_34 >= '0' && LA5_34 <= ';')||LA5_34=='?'||(LA5_34 >= 'A' && LA5_34 <= '_')||(LA5_34 >= 'a' && LA5_34 <= '}')) ) {
											alt5=8;
										}

										else {
											alt5=7;
										}

									}

									else {
										alt5=8;
									}

								}

								else {
									alt5=8;
								}

							}

							else {
								alt5=8;
							}

						}

						else {
							alt5=8;
						}

					}

					else {
						alt5=8;
					}

				}

				else {
					alt5=8;
				}

			}

			else {
				alt5=8;
			}

			}
			break;
		case 'm':
			{
			int LA5_10 = input.LA(2);
			if ( (LA5_10=='a') ) {
				int LA5_17 = input.LA(3);
				if ( (LA5_17=='t') ) {
					int LA5_20 = input.LA(4);
					if ( (LA5_20=='c') ) {
						int LA5_23 = input.LA(5);
						if ( (LA5_23=='h') ) {
							int LA5_26 = input.LA(6);
							if ( (LA5_26=='e') ) {
								int LA5_29 = input.LA(7);
								if ( (LA5_29=='s') ) {
									int LA5_32 = input.LA(8);
									if ( (LA5_32=='$'||(LA5_32 >= '*' && LA5_32 <= '+')||(LA5_32 >= '-' && LA5_32 <= '.')||(LA5_32 >= '0' && LA5_32 <= ';')||LA5_32=='?'||(LA5_32 >= 'A' && LA5_32 <= '_')||(LA5_32 >= 'a' && LA5_32 <= '}')) ) {
										alt5=8;
									}

									else {
										alt5=7;
									}

								}

								else {
									alt5=8;
								}

							}

							else {
								alt5=8;
							}

						}

						else {
							alt5=8;
						}

					}

					else {
						alt5=8;
					}

				}

				else {
					alt5=8;
				}

			}

			else {
				alt5=8;
			}

			}
			break;
		case '$':
		case '*':
		case '+':
		case '-':
		case '.':
		case '0':
		case '1':
		case '2':
		case '3':
		case '4':
		case '5':
		case '6':
		case '7':
		case '8':
		case '9':
		case ':':
		case ';':
		case '?':
		case 'A':
		case 'B':
		case 'C':
		case 'D':
		case 'E':
		case 'F':
		case 'G':
		case 'H':
		case 'I':
		case 'J':
		case 'K':
		case 'L':
		case 'M':
		case 'N':
		case 'O':
		case 'P':
		case 'Q':
		case 'R':
		case 'S':
		case 'T':
		case 'U':
		case 'V':
		case 'W':
		case 'X':
		case 'Y':
		case 'Z':
		case '[':
		case '\\':
		case ']':
		case '^':
		case '_':
		case 'a':
		case 'b':
		case 'd':
		case 'e':
		case 'f':
		case 'g':
		case 'h':
		case 'i':
		case 'j':
		case 'k':
		case 'l':
		case 'n':
		case 'o':
		case 'q':
		case 'r':
		case 's':
		case 't':
		case 'u':
		case 'v':
		case 'w':
		case 'x':
		case 'y':
		case 'z':
		case '{':
		case '}':
			{
			alt5=8;
			}
			break;
		case '\t':
		case '\n':
		case '\r':
		case ' ':
			{
			alt5=9;
			}
			break;
		default:
			NoViableAltException nvae =
				new NoViableAltException("", 5, 0, input);
			throw nvae;
		}
		switch (alt5) {
			case 1 :
				// org/eclipse/tracecompass/tmf/filter/parser/FilterParser.g:1:10: T__21
				{
				mT__21(); 

				}
				break;
			case 2 :
				// org/eclipse/tracecompass/tmf/filter/parser/FilterParser.g:1:16: T__22
				{
				mT__22(); 

				}
				break;
			case 3 :
				// org/eclipse/tracecompass/tmf/filter/parser/FilterParser.g:1:22: T__23
				{
				mT__23(); 

				}
				break;
			case 4 :
				// org/eclipse/tracecompass/tmf/filter/parser/FilterParser.g:1:28: SEPARATOR
				{
				mSEPARATOR(); 

				}
				break;
			case 5 :
				// org/eclipse/tracecompass/tmf/filter/parser/FilterParser.g:1:38: OP_PRESENT
				{
				mOP_PRESENT(); 

				}
				break;
			case 6 :
				// org/eclipse/tracecompass/tmf/filter/parser/FilterParser.g:1:49: OP_NEGATE
				{
				mOP_NEGATE(); 

				}
				break;
			case 7 :
				// org/eclipse/tracecompass/tmf/filter/parser/FilterParser.g:1:59: OP
				{
				mOP(); 

				}
				break;
			case 8 :
				// org/eclipse/tracecompass/tmf/filter/parser/FilterParser.g:1:62: TEXT
				{
				mTEXT(); 

				}
				break;
			case 9 :
				// org/eclipse/tracecompass/tmf/filter/parser/FilterParser.g:1:67: WS
				{
				mWS(); 

				}
				break;

		}
	}



}
