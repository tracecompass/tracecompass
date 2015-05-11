// $ANTLR 3.5.2 org/eclipse/tracecompass/ctf/parser/CTFParser.g 2014-10-20 18:17:49

/*******************************************************************************
 * Copyright (c) 2010, 2013 Ericsson, Ecole Polytechnique de Montréal and others
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Matthew Khouzam - Initial API and implementation
 *   Simon Marchi - Initial API and implementation
 *   Etienne Bergeron - Update to Antlr 3.5 syntax
 *******************************************************************************/

package org.eclipse.tracecompass.ctf.parser;

import java.util.Set;
import java.util.HashSet;


import org.antlr.runtime.*;
import java.util.Stack;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;

import org.antlr.runtime.tree.*;


@SuppressWarnings("all")
public class CTFParser extends Parser {
	public static final String[] tokenNames = new String[] {
		"<invalid>", "<EOR>", "<DOWN>", "<UP>", "ALIGNTOK", "ARROW", "ASSIGNMENT", 
		"BACKSLASH", "BOOLTOK", "CALLSITETOK", "CHARACTER_LITERAL", "CHARTOK", 
		"CHAR_CONTENT", "CLOCKTOK", "CLOSEBRAC", "COLON", "COMMENT", "COMMENT_CLOSE", 
		"COMMENT_OPEN", "COMPLEXTOK", "CONSTTOK", "DECIMAL_LITERAL", "DIGIT", 
		"DOT", "DOUBLEQUOTE", "DOUBLETOK", "ELIPSES", "ENUMTOK", "ENVTOK", "ESCAPE_SEQUENCE", 
		"EVENTTOK", "FLOATINGPOINTTOK", "FLOATTOK", "GT", "HEXADECIMAL_ESCAPE", 
		"HEX_DIGIT", "HEX_LITERAL", "HEX_PREFIX", "IDENTIFIER", "IMAGINARYTOK", 
		"INFINITYTOK", "INTEGERTOK", "INTEGER_TYPES_SUFFIX", "INTTOK", "LCURL", 
		"LINE_COMMENT", "LONGTOK", "LPAREN", "LT", "NANNUMBERTOK", "NINFINITYTOK", 
		"NONDIGIT", "NONZERO_DIGIT", "OCTAL_ESCAPE", "OCTAL_LITERAL", "OCT_DIGIT", 
		"OCT_PREFIX", "OPENBRAC", "POINTER", "RCURL", "RPAREN", "SEPARATOR", "SHORTTOK", 
		"SIGN", "SIGNEDTOK", "SINGLEQUOTE", "STREAMTOK", "STRINGPREFIX", "STRINGTOK", 
		"STRING_CONTENT", "STRING_LITERAL", "STRUCTTOK", "TERM", "TRACETOK", "TYPEALIASTOK", 
		"TYPEDEFTOK", "TYPE_ASSIGNMENT", "UNICODE_ESCAPE", "UNSIGNEDTOK", "VARIANTTOK", 
		"VOIDTOK", "WS", "ALIGN", "CALLSITE", "CLOCK", "CTF_EXPRESSION_TYPE", 
		"CTF_EXPRESSION_VAL", "CTF_LEFT", "CTF_RIGHT", "DECLARATION", "DECLARATOR", 
		"ENUM", "ENUM_BODY", "ENUM_CONTAINER_TYPE", "ENUM_ENUMERATOR", "ENUM_NAME", 
		"ENUM_VALUE", "ENUM_VALUE_RANGE", "ENV", "EVENT", "FLOATING_POINT", "INTEGER", 
		"LENGTH", "ROOT", "STREAM", "STRING", "STRUCT", "STRUCT_BODY", "STRUCT_NAME", 
		"SV_DECLARATION", "TRACE", "TYPEALIAS", "TYPEALIAS_ALIAS", "TYPEALIAS_TARGET", 
		"TYPEDEF", "TYPE_DECLARATOR", "TYPE_DECLARATOR_LIST", "TYPE_SPECIFIER_LIST", 
		"UNARY_EXPRESSION_DEC", "UNARY_EXPRESSION_HEX", "UNARY_EXPRESSION_OCT", 
		"UNARY_EXPRESSION_STRING", "UNARY_EXPRESSION_STRING_QUOTES", "VARIANT", 
		"VARIANT_BODY", "VARIANT_NAME", "VARIANT_TAG"
	};
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
	public static final int ALIGN=82;
	public static final int CALLSITE=83;
	public static final int CLOCK=84;
	public static final int CTF_EXPRESSION_TYPE=85;
	public static final int CTF_EXPRESSION_VAL=86;
	public static final int CTF_LEFT=87;
	public static final int CTF_RIGHT=88;
	public static final int DECLARATION=89;
	public static final int DECLARATOR=90;
	public static final int ENUM=91;
	public static final int ENUM_BODY=92;
	public static final int ENUM_CONTAINER_TYPE=93;
	public static final int ENUM_ENUMERATOR=94;
	public static final int ENUM_NAME=95;
	public static final int ENUM_VALUE=96;
	public static final int ENUM_VALUE_RANGE=97;
	public static final int ENV=98;
	public static final int EVENT=99;
	public static final int FLOATING_POINT=100;
	public static final int INTEGER=101;
	public static final int LENGTH=102;
	public static final int ROOT=103;
	public static final int STREAM=104;
	public static final int STRING=105;
	public static final int STRUCT=106;
	public static final int STRUCT_BODY=107;
	public static final int STRUCT_NAME=108;
	public static final int SV_DECLARATION=109;
	public static final int TRACE=110;
	public static final int TYPEALIAS=111;
	public static final int TYPEALIAS_ALIAS=112;
	public static final int TYPEALIAS_TARGET=113;
	public static final int TYPEDEF=114;
	public static final int TYPE_DECLARATOR=115;
	public static final int TYPE_DECLARATOR_LIST=116;
	public static final int TYPE_SPECIFIER_LIST=117;
	public static final int UNARY_EXPRESSION_DEC=118;
	public static final int UNARY_EXPRESSION_HEX=119;
	public static final int UNARY_EXPRESSION_OCT=120;
	public static final int UNARY_EXPRESSION_STRING=121;
	public static final int UNARY_EXPRESSION_STRING_QUOTES=122;
	public static final int VARIANT=123;
	public static final int VARIANT_BODY=124;
	public static final int VARIANT_NAME=125;
	public static final int VARIANT_TAG=126;

	// delegates
	public Parser[] getDelegates() {
		return new Parser[] {};
	}

	// delegators

	protected static class Symbols_scope {
		Set<String> types;
	}
	protected Stack<Symbols_scope> Symbols_stack = new Stack<Symbols_scope>();


	public CTFParser(TokenStream input) {
		this(input, new RecognizerSharedState());
	}
	public CTFParser(TokenStream input, RecognizerSharedState state) {
		super(input, state);
	}

	protected TreeAdaptor adaptor = new CommonTreeAdaptor();

	public void setTreeAdaptor(TreeAdaptor adaptor) {
		this.adaptor = adaptor;
	}
	public TreeAdaptor getTreeAdaptor() {
		return adaptor;
	}
	@Override public String[] getTokenNames() { return CTFParser.tokenNames; }
	@Override public String getGrammarFileName() { return "org/eclipse/tracecompass/ctf/parser/CTFParser.g"; }


	    public CTFParser(TokenStream input, boolean verbose) {
	        this(input);
	        this.verbose = verbose;
	    }

	    /**
	      * This method is overriden to disable automatic error recovery.
	      * On a mismatched token, it simply re-throw an exception.
	      */
	    @Override
	    protected Object recoverFromMismatchedToken(IntStream input, int ttype, BitSet follow) throws RecognitionException {
	        throw new MismatchedTokenException(ttype, input);
	    }

	    /**
	     * Checks if a given name has been defined has a type.
	     * From: http://www.antlr.org/grammar/1153358328744/C.g
	     *
	     * @param name The name to check.
	     * @return True if is is a type, false otherwise.
	     */
	    boolean isTypeName(String name) {
	        for (int i = Symbols_stack.size() - 1; i >= 0; i--) {
	            Symbols_scope scope = (Symbols_scope) Symbols_stack.get(i);
	            if (scope.types.contains(name)) {
	                return true;
	            }
	        }
	        return false;
	    }

	    void addTypeName(String name) {
	        Symbols_stack.peek().types.add(name);
	        if (verbose) {
	            debug_print("New type: " + name);
	        }
	    }

	    boolean _inTypedef = false;

	    void typedefOn() {
	        debug_print("typedefOn");
	        _inTypedef = true;
	    }

	    void typedefOff() {
	        debug_print("typedefOff");
	        _inTypedef = false;
	    }

	    boolean inTypedef() {
	        return _inTypedef;
	    }

	    boolean _inTypealiasAlias = false;

	    void typealiasAliasOn() {
	        debug_print("typealiasAliasOn");
	        _inTypealiasAlias = true;
	    }

	    void typealiasAliasOff() {
	         debug_print("typealiasAliasOff");
	        _inTypealiasAlias = false;
	    }

	    boolean inTypealiasAlias() {
	        return _inTypealiasAlias;
	    }

	    void debug_print(String str) {
	        if (verbose) {
	            System.out.println(str);
	        }
	    }

	    /* Prints rule entry and exit while parsing */
	    boolean verbose = false;


	public static class parse_return extends ParserRuleReturnScope {
		CommonTree tree;
		@Override
		public CommonTree getTree() { return tree; }
	};


	// $ANTLR start "parse"
	// org/eclipse/tracecompass/ctf/parser/CTFParser.g:192:1: parse : ( declaration )+ EOF -> ^( ROOT ( declaration )+ ) ;
	public final CTFParser.parse_return parse() throws RecognitionException {
		Symbols_stack.push(new Symbols_scope());

		CTFParser.parse_return retval = new CTFParser.parse_return();
		retval.start = input.LT(1);

		CommonTree root_0 = null;

		Token EOF2=null;
		ParserRuleReturnScope declaration1 =null;

		CommonTree EOF2_tree=null;
		RewriteRuleTokenStream stream_EOF=new RewriteRuleTokenStream(adaptor,"token EOF");
		RewriteRuleSubtreeStream stream_declaration=new RewriteRuleSubtreeStream(adaptor,"rule declaration");


		    Symbols_stack.peek().types = new HashSet<String>();

		try {
			// org/eclipse/tracecompass/ctf/parser/CTFParser.g:197:3: ( ( declaration )+ EOF -> ^( ROOT ( declaration )+ ) )
			// org/eclipse/tracecompass/ctf/parser/CTFParser.g:197:5: ( declaration )+ EOF
			{
			// org/eclipse/tracecompass/ctf/parser/CTFParser.g:197:5: ( declaration )+
			int cnt1=0;
			loop1:
			while (true) {
				int alt1=2;
				int LA1_0 = input.LA(1);
				if ( (LA1_0==BOOLTOK||LA1_0==CHARTOK||(LA1_0 >= COMPLEXTOK && LA1_0 <= CONSTTOK)||LA1_0==DOUBLETOK||LA1_0==ENUMTOK||(LA1_0 >= FLOATINGPOINTTOK && LA1_0 <= FLOATTOK)||LA1_0==IMAGINARYTOK||LA1_0==INTEGERTOK||LA1_0==INTTOK||LA1_0==LONGTOK||LA1_0==SHORTTOK||LA1_0==SIGNEDTOK||LA1_0==STRINGTOK||LA1_0==STRUCTTOK||LA1_0==TYPEDEFTOK||(LA1_0 >= UNSIGNEDTOK && LA1_0 <= VOIDTOK)) ) {
					alt1=1;
				}
				else if ( (LA1_0==IDENTIFIER) && (( inTypealiasAlias() || isTypeName(input.LT(1).getText()) ))) {
					alt1=1;
				}
				else if ( (LA1_0==CALLSITETOK||LA1_0==CLOCKTOK||LA1_0==ENVTOK||LA1_0==EVENTTOK||LA1_0==STREAMTOK||(LA1_0 >= TRACETOK && LA1_0 <= TYPEALIASTOK)) ) {
					alt1=1;
				}

				switch (alt1) {
				case 1 :
					// org/eclipse/tracecompass/ctf/parser/CTFParser.g:197:5: declaration
					{
					pushFollow(FOLLOW_declaration_in_parse449);
					declaration1=declaration();
					state._fsp--;
					if (state.failed) return retval;
					if ( state.backtracking==0 ) stream_declaration.add(declaration1.getTree());
					}
					break;

				default :
					if ( cnt1 >= 1 ) break loop1;
					if (state.backtracking>0) {state.failed=true; return retval;}
					EarlyExitException eee = new EarlyExitException(1, input);
					throw eee;
				}
				cnt1++;
			}

			EOF2=(Token)match(input,EOF,FOLLOW_EOF_in_parse452); if (state.failed) return retval; 
			if ( state.backtracking==0 ) stream_EOF.add(EOF2);

			// AST REWRITE
			// elements: declaration
			// token labels: 
			// rule labels: retval
			// token list labels: 
			// rule list labels: 
			// wildcard labels: 
			if ( state.backtracking==0 ) {
			retval.tree = root_0;
			RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.getTree():null);

			root_0 = (CommonTree)adaptor.nil();
			// 197:22: -> ^( ROOT ( declaration )+ )
			{
				// org/eclipse/tracecompass/ctf/parser/CTFParser.g:197:25: ^( ROOT ( declaration )+ )
				{
				CommonTree root_1 = (CommonTree)adaptor.nil();
				root_1 = (CommonTree)adaptor.becomeRoot((CommonTree)adaptor.create(ROOT, "ROOT"), root_1);
				if ( !(stream_declaration.hasNext()) ) {
					throw new RewriteEarlyExitException();
				}
				while ( stream_declaration.hasNext() ) {
					adaptor.addChild(root_1, stream_declaration.nextTree());
				}
				stream_declaration.reset();

				adaptor.addChild(root_0, root_1);
				}

			}


			retval.tree = root_0;
			}

			}

			retval.stop = input.LT(-1);

			if ( state.backtracking==0 ) {
			retval.tree = (CommonTree)adaptor.rulePostProcessing(root_0);
			adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
			}
		}

		    catch (RecognitionException e) {
		        throw e;
		    }

		finally {
			// do for sure before leaving
			Symbols_stack.pop();

		}
		return retval;
	}
	// $ANTLR end "parse"


	public static class numberLiteral_return extends ParserRuleReturnScope {
		CommonTree tree;
		@Override
		public CommonTree getTree() { return tree; }
	};


	// $ANTLR start "numberLiteral"
	// org/eclipse/tracecompass/ctf/parser/CTFParser.g:200:1: numberLiteral : ( SIGN )* ( HEX_LITERAL -> ^( UNARY_EXPRESSION_HEX HEX_LITERAL ( SIGN )* ) | DECIMAL_LITERAL -> ^( UNARY_EXPRESSION_DEC DECIMAL_LITERAL ( SIGN )* ) | OCTAL_LITERAL -> ^( UNARY_EXPRESSION_OCT OCTAL_LITERAL ( SIGN )* ) ) ;
	public final CTFParser.numberLiteral_return numberLiteral() throws RecognitionException {
		CTFParser.numberLiteral_return retval = new CTFParser.numberLiteral_return();
		retval.start = input.LT(1);

		CommonTree root_0 = null;

		Token SIGN3=null;
		Token HEX_LITERAL4=null;
		Token DECIMAL_LITERAL5=null;
		Token OCTAL_LITERAL6=null;

		CommonTree SIGN3_tree=null;
		CommonTree HEX_LITERAL4_tree=null;
		CommonTree DECIMAL_LITERAL5_tree=null;
		CommonTree OCTAL_LITERAL6_tree=null;
		RewriteRuleTokenStream stream_SIGN=new RewriteRuleTokenStream(adaptor,"token SIGN");
		RewriteRuleTokenStream stream_OCTAL_LITERAL=new RewriteRuleTokenStream(adaptor,"token OCTAL_LITERAL");
		RewriteRuleTokenStream stream_HEX_LITERAL=new RewriteRuleTokenStream(adaptor,"token HEX_LITERAL");
		RewriteRuleTokenStream stream_DECIMAL_LITERAL=new RewriteRuleTokenStream(adaptor,"token DECIMAL_LITERAL");

		try {
			// org/eclipse/tracecompass/ctf/parser/CTFParser.g:201:3: ( ( SIGN )* ( HEX_LITERAL -> ^( UNARY_EXPRESSION_HEX HEX_LITERAL ( SIGN )* ) | DECIMAL_LITERAL -> ^( UNARY_EXPRESSION_DEC DECIMAL_LITERAL ( SIGN )* ) | OCTAL_LITERAL -> ^( UNARY_EXPRESSION_OCT OCTAL_LITERAL ( SIGN )* ) ) )
			// org/eclipse/tracecompass/ctf/parser/CTFParser.g:201:5: ( SIGN )* ( HEX_LITERAL -> ^( UNARY_EXPRESSION_HEX HEX_LITERAL ( SIGN )* ) | DECIMAL_LITERAL -> ^( UNARY_EXPRESSION_DEC DECIMAL_LITERAL ( SIGN )* ) | OCTAL_LITERAL -> ^( UNARY_EXPRESSION_OCT OCTAL_LITERAL ( SIGN )* ) )
			{
			// org/eclipse/tracecompass/ctf/parser/CTFParser.g:201:5: ( SIGN )*
			loop2:
			while (true) {
				int alt2=2;
				int LA2_0 = input.LA(1);
				if ( (LA2_0==SIGN) ) {
					alt2=1;
				}

				switch (alt2) {
				case 1 :
					// org/eclipse/tracecompass/ctf/parser/CTFParser.g:201:5: SIGN
					{
					SIGN3=(Token)match(input,SIGN,FOLLOW_SIGN_in_numberLiteral474); if (state.failed) return retval; 
					if ( state.backtracking==0 ) stream_SIGN.add(SIGN3);

					}
					break;

				default :
					break loop2;
				}
			}

			// org/eclipse/tracecompass/ctf/parser/CTFParser.g:202:7: ( HEX_LITERAL -> ^( UNARY_EXPRESSION_HEX HEX_LITERAL ( SIGN )* ) | DECIMAL_LITERAL -> ^( UNARY_EXPRESSION_DEC DECIMAL_LITERAL ( SIGN )* ) | OCTAL_LITERAL -> ^( UNARY_EXPRESSION_OCT OCTAL_LITERAL ( SIGN )* ) )
			int alt3=3;
			switch ( input.LA(1) ) {
			case HEX_LITERAL:
				{
				alt3=1;
				}
				break;
			case DECIMAL_LITERAL:
				{
				alt3=2;
				}
				break;
			case OCTAL_LITERAL:
				{
				alt3=3;
				}
				break;
			default:
				if (state.backtracking>0) {state.failed=true; return retval;}
				NoViableAltException nvae =
					new NoViableAltException("", 3, 0, input);
				throw nvae;
			}
			switch (alt3) {
				case 1 :
					// org/eclipse/tracecompass/ctf/parser/CTFParser.g:202:9: HEX_LITERAL
					{
					HEX_LITERAL4=(Token)match(input,HEX_LITERAL,FOLLOW_HEX_LITERAL_in_numberLiteral485); if (state.failed) return retval; 
					if ( state.backtracking==0 ) stream_HEX_LITERAL.add(HEX_LITERAL4);

					// AST REWRITE
					// elements: HEX_LITERAL, SIGN
					// token labels: 
					// rule labels: retval
					// token list labels: 
					// rule list labels: 
					// wildcard labels: 
					if ( state.backtracking==0 ) {
					retval.tree = root_0;
					RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.getTree():null);

					root_0 = (CommonTree)adaptor.nil();
					// 202:21: -> ^( UNARY_EXPRESSION_HEX HEX_LITERAL ( SIGN )* )
					{
						// org/eclipse/tracecompass/ctf/parser/CTFParser.g:202:24: ^( UNARY_EXPRESSION_HEX HEX_LITERAL ( SIGN )* )
						{
						CommonTree root_1 = (CommonTree)adaptor.nil();
						root_1 = (CommonTree)adaptor.becomeRoot((CommonTree)adaptor.create(UNARY_EXPRESSION_HEX, "UNARY_EXPRESSION_HEX"), root_1);
						adaptor.addChild(root_1, stream_HEX_LITERAL.nextNode());
						// org/eclipse/tracecompass/ctf/parser/CTFParser.g:202:59: ( SIGN )*
						while ( stream_SIGN.hasNext() ) {
							adaptor.addChild(root_1, stream_SIGN.nextNode());
						}
						stream_SIGN.reset();

						adaptor.addChild(root_0, root_1);
						}

					}


					retval.tree = root_0;
					}

					}
					break;
				case 2 :
					// org/eclipse/tracecompass/ctf/parser/CTFParser.g:203:9: DECIMAL_LITERAL
					{
					DECIMAL_LITERAL5=(Token)match(input,DECIMAL_LITERAL,FOLLOW_DECIMAL_LITERAL_in_numberLiteral506); if (state.failed) return retval; 
					if ( state.backtracking==0 ) stream_DECIMAL_LITERAL.add(DECIMAL_LITERAL5);

					// AST REWRITE
					// elements: SIGN, DECIMAL_LITERAL
					// token labels: 
					// rule labels: retval
					// token list labels: 
					// rule list labels: 
					// wildcard labels: 
					if ( state.backtracking==0 ) {
					retval.tree = root_0;
					RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.getTree():null);

					root_0 = (CommonTree)adaptor.nil();
					// 203:25: -> ^( UNARY_EXPRESSION_DEC DECIMAL_LITERAL ( SIGN )* )
					{
						// org/eclipse/tracecompass/ctf/parser/CTFParser.g:203:28: ^( UNARY_EXPRESSION_DEC DECIMAL_LITERAL ( SIGN )* )
						{
						CommonTree root_1 = (CommonTree)adaptor.nil();
						root_1 = (CommonTree)adaptor.becomeRoot((CommonTree)adaptor.create(UNARY_EXPRESSION_DEC, "UNARY_EXPRESSION_DEC"), root_1);
						adaptor.addChild(root_1, stream_DECIMAL_LITERAL.nextNode());
						// org/eclipse/tracecompass/ctf/parser/CTFParser.g:203:67: ( SIGN )*
						while ( stream_SIGN.hasNext() ) {
							adaptor.addChild(root_1, stream_SIGN.nextNode());
						}
						stream_SIGN.reset();

						adaptor.addChild(root_0, root_1);
						}

					}


					retval.tree = root_0;
					}

					}
					break;
				case 3 :
					// org/eclipse/tracecompass/ctf/parser/CTFParser.g:204:9: OCTAL_LITERAL
					{
					OCTAL_LITERAL6=(Token)match(input,OCTAL_LITERAL,FOLLOW_OCTAL_LITERAL_in_numberLiteral527); if (state.failed) return retval; 
					if ( state.backtracking==0 ) stream_OCTAL_LITERAL.add(OCTAL_LITERAL6);

					// AST REWRITE
					// elements: SIGN, OCTAL_LITERAL
					// token labels: 
					// rule labels: retval
					// token list labels: 
					// rule list labels: 
					// wildcard labels: 
					if ( state.backtracking==0 ) {
					retval.tree = root_0;
					RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.getTree():null);

					root_0 = (CommonTree)adaptor.nil();
					// 204:23: -> ^( UNARY_EXPRESSION_OCT OCTAL_LITERAL ( SIGN )* )
					{
						// org/eclipse/tracecompass/ctf/parser/CTFParser.g:204:26: ^( UNARY_EXPRESSION_OCT OCTAL_LITERAL ( SIGN )* )
						{
						CommonTree root_1 = (CommonTree)adaptor.nil();
						root_1 = (CommonTree)adaptor.becomeRoot((CommonTree)adaptor.create(UNARY_EXPRESSION_OCT, "UNARY_EXPRESSION_OCT"), root_1);
						adaptor.addChild(root_1, stream_OCTAL_LITERAL.nextNode());
						// org/eclipse/tracecompass/ctf/parser/CTFParser.g:204:63: ( SIGN )*
						while ( stream_SIGN.hasNext() ) {
							adaptor.addChild(root_1, stream_SIGN.nextNode());
						}
						stream_SIGN.reset();

						adaptor.addChild(root_0, root_1);
						}

					}


					retval.tree = root_0;
					}

					}
					break;

			}

			}

			retval.stop = input.LT(-1);

			if ( state.backtracking==0 ) {
			retval.tree = (CommonTree)adaptor.rulePostProcessing(root_0);
			adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
			}
		}

		    catch (RecognitionException e) {
		        throw e;
		    }

		finally {
			// do for sure before leaving
		}
		return retval;
	}
	// $ANTLR end "numberLiteral"


	public static class primaryExpression_return extends ParserRuleReturnScope {
		CommonTree tree;
		@Override
		public CommonTree getTree() { return tree; }
	};


	// $ANTLR start "primaryExpression"
	// org/eclipse/tracecompass/ctf/parser/CTFParser.g:208:1: primaryExpression : ( ( IDENTIFIER )=> IDENTIFIER -> ^( UNARY_EXPRESSION_STRING IDENTIFIER ) | ( ctfKeyword )=> ctfKeyword -> ^( UNARY_EXPRESSION_STRING ctfKeyword ) | ( STRING_LITERAL )=> STRING_LITERAL -> ^( UNARY_EXPRESSION_STRING_QUOTES STRING_LITERAL ) | numberLiteral | enumConstant | CHARACTER_LITERAL );
	public final CTFParser.primaryExpression_return primaryExpression() throws RecognitionException {
		CTFParser.primaryExpression_return retval = new CTFParser.primaryExpression_return();
		retval.start = input.LT(1);

		CommonTree root_0 = null;

		Token IDENTIFIER7=null;
		Token STRING_LITERAL9=null;
		Token CHARACTER_LITERAL12=null;
		ParserRuleReturnScope ctfKeyword8 =null;
		ParserRuleReturnScope numberLiteral10 =null;
		ParserRuleReturnScope enumConstant11 =null;

		CommonTree IDENTIFIER7_tree=null;
		CommonTree STRING_LITERAL9_tree=null;
		CommonTree CHARACTER_LITERAL12_tree=null;
		RewriteRuleTokenStream stream_STRING_LITERAL=new RewriteRuleTokenStream(adaptor,"token STRING_LITERAL");
		RewriteRuleTokenStream stream_IDENTIFIER=new RewriteRuleTokenStream(adaptor,"token IDENTIFIER");
		RewriteRuleSubtreeStream stream_ctfKeyword=new RewriteRuleSubtreeStream(adaptor,"rule ctfKeyword");

		try {
			// org/eclipse/tracecompass/ctf/parser/CTFParser.g:209:3: ( ( IDENTIFIER )=> IDENTIFIER -> ^( UNARY_EXPRESSION_STRING IDENTIFIER ) | ( ctfKeyword )=> ctfKeyword -> ^( UNARY_EXPRESSION_STRING ctfKeyword ) | ( STRING_LITERAL )=> STRING_LITERAL -> ^( UNARY_EXPRESSION_STRING_QUOTES STRING_LITERAL ) | numberLiteral | enumConstant | CHARACTER_LITERAL )
			int alt4=6;
			switch ( input.LA(1) ) {
			case IDENTIFIER:
				{
				int LA4_1 = input.LA(2);
				if ( (synpred1_CTFParser()) ) {
					alt4=1;
				}
				else if ( (true) ) {
					alt4=5;
				}

				}
				break;
			case ALIGNTOK:
			case EVENTTOK:
			case SIGNEDTOK:
			case STRINGTOK:
				{
				int LA4_2 = input.LA(2);
				if ( (synpred2_CTFParser()) ) {
					alt4=2;
				}
				else if ( (true) ) {
					alt4=5;
				}

				}
				break;
			case STRING_LITERAL:
				{
				int LA4_3 = input.LA(2);
				if ( (synpred3_CTFParser()) ) {
					alt4=3;
				}
				else if ( (true) ) {
					alt4=5;
				}

				}
				break;
			case DECIMAL_LITERAL:
			case HEX_LITERAL:
			case OCTAL_LITERAL:
			case SIGN:
				{
				alt4=4;
				}
				break;
			case CHARACTER_LITERAL:
				{
				alt4=6;
				}
				break;
			default:
				if (state.backtracking>0) {state.failed=true; return retval;}
				NoViableAltException nvae =
					new NoViableAltException("", 4, 0, input);
				throw nvae;
			}
			switch (alt4) {
				case 1 :
					// org/eclipse/tracecompass/ctf/parser/CTFParser.g:209:5: ( IDENTIFIER )=> IDENTIFIER
					{
					IDENTIFIER7=(Token)match(input,IDENTIFIER,FOLLOW_IDENTIFIER_in_primaryExpression565); if (state.failed) return retval; 
					if ( state.backtracking==0 ) stream_IDENTIFIER.add(IDENTIFIER7);

					// AST REWRITE
					// elements: IDENTIFIER
					// token labels: 
					// rule labels: retval
					// token list labels: 
					// rule list labels: 
					// wildcard labels: 
					if ( state.backtracking==0 ) {
					retval.tree = root_0;
					RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.getTree():null);

					root_0 = (CommonTree)adaptor.nil();
					// 210:7: -> ^( UNARY_EXPRESSION_STRING IDENTIFIER )
					{
						// org/eclipse/tracecompass/ctf/parser/CTFParser.g:210:10: ^( UNARY_EXPRESSION_STRING IDENTIFIER )
						{
						CommonTree root_1 = (CommonTree)adaptor.nil();
						root_1 = (CommonTree)adaptor.becomeRoot((CommonTree)adaptor.create(UNARY_EXPRESSION_STRING, "UNARY_EXPRESSION_STRING"), root_1);
						adaptor.addChild(root_1, stream_IDENTIFIER.nextNode());
						adaptor.addChild(root_0, root_1);
						}

					}


					retval.tree = root_0;
					}

					}
					break;
				case 2 :
					// org/eclipse/tracecompass/ctf/parser/CTFParser.g:211:5: ( ctfKeyword )=> ctfKeyword
					{
					pushFollow(FOLLOW_ctfKeyword_in_primaryExpression591);
					ctfKeyword8=ctfKeyword();
					state._fsp--;
					if (state.failed) return retval;
					if ( state.backtracking==0 ) stream_ctfKeyword.add(ctfKeyword8.getTree());
					// AST REWRITE
					// elements: ctfKeyword
					// token labels: 
					// rule labels: retval
					// token list labels: 
					// rule list labels: 
					// wildcard labels: 
					if ( state.backtracking==0 ) {
					retval.tree = root_0;
					RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.getTree():null);

					root_0 = (CommonTree)adaptor.nil();
					// 211:32: -> ^( UNARY_EXPRESSION_STRING ctfKeyword )
					{
						// org/eclipse/tracecompass/ctf/parser/CTFParser.g:211:35: ^( UNARY_EXPRESSION_STRING ctfKeyword )
						{
						CommonTree root_1 = (CommonTree)adaptor.nil();
						root_1 = (CommonTree)adaptor.becomeRoot((CommonTree)adaptor.create(UNARY_EXPRESSION_STRING, "UNARY_EXPRESSION_STRING"), root_1);
						adaptor.addChild(root_1, stream_ctfKeyword.nextTree());
						adaptor.addChild(root_0, root_1);
						}

					}


					retval.tree = root_0;
					}

					}
					break;
				case 3 :
					// org/eclipse/tracecompass/ctf/parser/CTFParser.g:212:5: ( STRING_LITERAL )=> STRING_LITERAL
					{
					STRING_LITERAL9=(Token)match(input,STRING_LITERAL,FOLLOW_STRING_LITERAL_in_primaryExpression611); if (state.failed) return retval; 
					if ( state.backtracking==0 ) stream_STRING_LITERAL.add(STRING_LITERAL9);

					// AST REWRITE
					// elements: STRING_LITERAL
					// token labels: 
					// rule labels: retval
					// token list labels: 
					// rule list labels: 
					// wildcard labels: 
					if ( state.backtracking==0 ) {
					retval.tree = root_0;
					RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.getTree():null);

					root_0 = (CommonTree)adaptor.nil();
					// 213:7: -> ^( UNARY_EXPRESSION_STRING_QUOTES STRING_LITERAL )
					{
						// org/eclipse/tracecompass/ctf/parser/CTFParser.g:213:10: ^( UNARY_EXPRESSION_STRING_QUOTES STRING_LITERAL )
						{
						CommonTree root_1 = (CommonTree)adaptor.nil();
						root_1 = (CommonTree)adaptor.becomeRoot((CommonTree)adaptor.create(UNARY_EXPRESSION_STRING_QUOTES, "UNARY_EXPRESSION_STRING_QUOTES"), root_1);
						adaptor.addChild(root_1, stream_STRING_LITERAL.nextNode());
						adaptor.addChild(root_0, root_1);
						}

					}


					retval.tree = root_0;
					}

					}
					break;
				case 4 :
					// org/eclipse/tracecompass/ctf/parser/CTFParser.g:215:5: numberLiteral
					{
					root_0 = (CommonTree)adaptor.nil();


					pushFollow(FOLLOW_numberLiteral_in_primaryExpression636);
					numberLiteral10=numberLiteral();
					state._fsp--;
					if (state.failed) return retval;
					if ( state.backtracking==0 ) adaptor.addChild(root_0, numberLiteral10.getTree());

					}
					break;
				case 5 :
					// org/eclipse/tracecompass/ctf/parser/CTFParser.g:216:5: enumConstant
					{
					root_0 = (CommonTree)adaptor.nil();


					pushFollow(FOLLOW_enumConstant_in_primaryExpression642);
					enumConstant11=enumConstant();
					state._fsp--;
					if (state.failed) return retval;
					if ( state.backtracking==0 ) adaptor.addChild(root_0, enumConstant11.getTree());

					}
					break;
				case 6 :
					// org/eclipse/tracecompass/ctf/parser/CTFParser.g:217:5: CHARACTER_LITERAL
					{
					root_0 = (CommonTree)adaptor.nil();


					CHARACTER_LITERAL12=(Token)match(input,CHARACTER_LITERAL,FOLLOW_CHARACTER_LITERAL_in_primaryExpression648); if (state.failed) return retval;
					if ( state.backtracking==0 ) {
					CHARACTER_LITERAL12_tree = (CommonTree)adaptor.create(CHARACTER_LITERAL12);
					adaptor.addChild(root_0, CHARACTER_LITERAL12_tree);
					}

					}
					break;

			}
			retval.stop = input.LT(-1);

			if ( state.backtracking==0 ) {
			retval.tree = (CommonTree)adaptor.rulePostProcessing(root_0);
			adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
			}
		}

		    catch (RecognitionException e) {
		        throw e;
		    }

		finally {
			// do for sure before leaving
		}
		return retval;
	}
	// $ANTLR end "primaryExpression"


	public static class postfixExpressionSuffix_return extends ParserRuleReturnScope {
		CommonTree tree;
		@Override
		public CommonTree getTree() { return tree; }
	};


	// $ANTLR start "postfixExpressionSuffix"
	// org/eclipse/tracecompass/ctf/parser/CTFParser.g:220:1: postfixExpressionSuffix : ( OPENBRAC unaryExpression CLOSEBRAC !| (ref= DOT |ref= ARROW ) IDENTIFIER -> ^( $ref ^( UNARY_EXPRESSION_STRING IDENTIFIER ) ) );
	public final CTFParser.postfixExpressionSuffix_return postfixExpressionSuffix() throws RecognitionException {
		CTFParser.postfixExpressionSuffix_return retval = new CTFParser.postfixExpressionSuffix_return();
		retval.start = input.LT(1);

		CommonTree root_0 = null;

		Token ref=null;
		Token OPENBRAC13=null;
		Token CLOSEBRAC15=null;
		Token IDENTIFIER16=null;
		ParserRuleReturnScope unaryExpression14 =null;

		CommonTree ref_tree=null;
		CommonTree OPENBRAC13_tree=null;
		CommonTree CLOSEBRAC15_tree=null;
		CommonTree IDENTIFIER16_tree=null;
		RewriteRuleTokenStream stream_ARROW=new RewriteRuleTokenStream(adaptor,"token ARROW");
		RewriteRuleTokenStream stream_DOT=new RewriteRuleTokenStream(adaptor,"token DOT");
		RewriteRuleTokenStream stream_IDENTIFIER=new RewriteRuleTokenStream(adaptor,"token IDENTIFIER");

		try {
			// org/eclipse/tracecompass/ctf/parser/CTFParser.g:221:3: ( OPENBRAC unaryExpression CLOSEBRAC !| (ref= DOT |ref= ARROW ) IDENTIFIER -> ^( $ref ^( UNARY_EXPRESSION_STRING IDENTIFIER ) ) )
			int alt6=2;
			int LA6_0 = input.LA(1);
			if ( (LA6_0==OPENBRAC) ) {
				alt6=1;
			}
			else if ( (LA6_0==ARROW||LA6_0==DOT) ) {
				alt6=2;
			}

			else {
				if (state.backtracking>0) {state.failed=true; return retval;}
				NoViableAltException nvae =
					new NoViableAltException("", 6, 0, input);
				throw nvae;
			}

			switch (alt6) {
				case 1 :
					// org/eclipse/tracecompass/ctf/parser/CTFParser.g:221:5: OPENBRAC unaryExpression CLOSEBRAC !
					{
					root_0 = (CommonTree)adaptor.nil();


					OPENBRAC13=(Token)match(input,OPENBRAC,FOLLOW_OPENBRAC_in_postfixExpressionSuffix661); if (state.failed) return retval;
					if ( state.backtracking==0 ) {
					OPENBRAC13_tree = (CommonTree)adaptor.create(OPENBRAC13);
					adaptor.addChild(root_0, OPENBRAC13_tree);
					}

					pushFollow(FOLLOW_unaryExpression_in_postfixExpressionSuffix663);
					unaryExpression14=unaryExpression();
					state._fsp--;
					if (state.failed) return retval;
					if ( state.backtracking==0 ) adaptor.addChild(root_0, unaryExpression14.getTree());

					CLOSEBRAC15=(Token)match(input,CLOSEBRAC,FOLLOW_CLOSEBRAC_in_postfixExpressionSuffix665); if (state.failed) return retval;
					}
					break;
				case 2 :
					// org/eclipse/tracecompass/ctf/parser/CTFParser.g:222:5: (ref= DOT |ref= ARROW ) IDENTIFIER
					{
					// org/eclipse/tracecompass/ctf/parser/CTFParser.g:222:5: (ref= DOT |ref= ARROW )
					int alt5=2;
					int LA5_0 = input.LA(1);
					if ( (LA5_0==DOT) ) {
						alt5=1;
					}
					else if ( (LA5_0==ARROW) ) {
						alt5=2;
					}

					else {
						if (state.backtracking>0) {state.failed=true; return retval;}
						NoViableAltException nvae =
							new NoViableAltException("", 5, 0, input);
						throw nvae;
					}

					switch (alt5) {
						case 1 :
							// org/eclipse/tracecompass/ctf/parser/CTFParser.g:222:6: ref= DOT
							{
							ref=(Token)match(input,DOT,FOLLOW_DOT_in_postfixExpressionSuffix675); if (state.failed) return retval; 
							if ( state.backtracking==0 ) stream_DOT.add(ref);

							}
							break;
						case 2 :
							// org/eclipse/tracecompass/ctf/parser/CTFParser.g:222:16: ref= ARROW
							{
							ref=(Token)match(input,ARROW,FOLLOW_ARROW_in_postfixExpressionSuffix681); if (state.failed) return retval; 
							if ( state.backtracking==0 ) stream_ARROW.add(ref);

							}
							break;

					}

					IDENTIFIER16=(Token)match(input,IDENTIFIER,FOLLOW_IDENTIFIER_in_postfixExpressionSuffix684); if (state.failed) return retval; 
					if ( state.backtracking==0 ) stream_IDENTIFIER.add(IDENTIFIER16);

					// AST REWRITE
					// elements: IDENTIFIER, ref
					// token labels: ref
					// rule labels: retval
					// token list labels: 
					// rule list labels: 
					// wildcard labels: 
					if ( state.backtracking==0 ) {
					retval.tree = root_0;
					RewriteRuleTokenStream stream_ref=new RewriteRuleTokenStream(adaptor,"token ref",ref);
					RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.getTree():null);

					root_0 = (CommonTree)adaptor.nil();
					// 223:7: -> ^( $ref ^( UNARY_EXPRESSION_STRING IDENTIFIER ) )
					{
						// org/eclipse/tracecompass/ctf/parser/CTFParser.g:223:10: ^( $ref ^( UNARY_EXPRESSION_STRING IDENTIFIER ) )
						{
						CommonTree root_1 = (CommonTree)adaptor.nil();
						root_1 = (CommonTree)adaptor.becomeRoot(stream_ref.nextNode(), root_1);
						// org/eclipse/tracecompass/ctf/parser/CTFParser.g:223:17: ^( UNARY_EXPRESSION_STRING IDENTIFIER )
						{
						CommonTree root_2 = (CommonTree)adaptor.nil();
						root_2 = (CommonTree)adaptor.becomeRoot((CommonTree)adaptor.create(UNARY_EXPRESSION_STRING, "UNARY_EXPRESSION_STRING"), root_2);
						adaptor.addChild(root_2, stream_IDENTIFIER.nextNode());
						adaptor.addChild(root_1, root_2);
						}

						adaptor.addChild(root_0, root_1);
						}

					}


					retval.tree = root_0;
					}

					}
					break;

			}
			retval.stop = input.LT(-1);

			if ( state.backtracking==0 ) {
			retval.tree = (CommonTree)adaptor.rulePostProcessing(root_0);
			adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
			}
		}

		    catch (RecognitionException e) {
		        throw e;
		    }

		finally {
			// do for sure before leaving
		}
		return retval;
	}
	// $ANTLR end "postfixExpressionSuffix"


	public static class postfixExpression_return extends ParserRuleReturnScope {
		CommonTree tree;
		@Override
		public CommonTree getTree() { return tree; }
	};


	// $ANTLR start "postfixExpression"
	// org/eclipse/tracecompass/ctf/parser/CTFParser.g:226:1: postfixExpression : ( primaryExpression ( postfixExpressionSuffix )* | ctfSpecifierHead ( postfixExpressionSuffix )+ );
	public final CTFParser.postfixExpression_return postfixExpression() throws RecognitionException {
		CTFParser.postfixExpression_return retval = new CTFParser.postfixExpression_return();
		retval.start = input.LT(1);

		CommonTree root_0 = null;

		ParserRuleReturnScope primaryExpression17 =null;
		ParserRuleReturnScope postfixExpressionSuffix18 =null;
		ParserRuleReturnScope ctfSpecifierHead19 =null;
		ParserRuleReturnScope postfixExpressionSuffix20 =null;


		try {
			// org/eclipse/tracecompass/ctf/parser/CTFParser.g:227:3: ( primaryExpression ( postfixExpressionSuffix )* | ctfSpecifierHead ( postfixExpressionSuffix )+ )
			int alt9=2;
			switch ( input.LA(1) ) {
			case ALIGNTOK:
			case CHARACTER_LITERAL:
			case DECIMAL_LITERAL:
			case HEX_LITERAL:
			case IDENTIFIER:
			case OCTAL_LITERAL:
			case SIGN:
			case SIGNEDTOK:
			case STRINGTOK:
			case STRING_LITERAL:
				{
				alt9=1;
				}
				break;
			case EVENTTOK:
				{
				alt9=1;
				}
				break;
			case CALLSITETOK:
			case CLOCKTOK:
			case ENVTOK:
			case STREAMTOK:
			case TRACETOK:
				{
				alt9=2;
				}
				break;
			default:
				if (state.backtracking>0) {state.failed=true; return retval;}
				NoViableAltException nvae =
					new NoViableAltException("", 9, 0, input);
				throw nvae;
			}
			switch (alt9) {
				case 1 :
					// org/eclipse/tracecompass/ctf/parser/CTFParser.g:227:5: primaryExpression ( postfixExpressionSuffix )*
					{
					root_0 = (CommonTree)adaptor.nil();


					pushFollow(FOLLOW_primaryExpression_in_postfixExpression716);
					primaryExpression17=primaryExpression();
					state._fsp--;
					if (state.failed) return retval;
					if ( state.backtracking==0 ) adaptor.addChild(root_0, primaryExpression17.getTree());

					// org/eclipse/tracecompass/ctf/parser/CTFParser.g:227:23: ( postfixExpressionSuffix )*
					loop7:
					while (true) {
						int alt7=2;
						int LA7_0 = input.LA(1);
						if ( (LA7_0==ARROW||LA7_0==DOT||LA7_0==OPENBRAC) ) {
							alt7=1;
						}

						switch (alt7) {
						case 1 :
							// org/eclipse/tracecompass/ctf/parser/CTFParser.g:227:23: postfixExpressionSuffix
							{
							pushFollow(FOLLOW_postfixExpressionSuffix_in_postfixExpression718);
							postfixExpressionSuffix18=postfixExpressionSuffix();
							state._fsp--;
							if (state.failed) return retval;
							if ( state.backtracking==0 ) adaptor.addChild(root_0, postfixExpressionSuffix18.getTree());

							}
							break;

						default :
							break loop7;
						}
					}

					}
					break;
				case 2 :
					// org/eclipse/tracecompass/ctf/parser/CTFParser.g:228:5: ctfSpecifierHead ( postfixExpressionSuffix )+
					{
					root_0 = (CommonTree)adaptor.nil();


					pushFollow(FOLLOW_ctfSpecifierHead_in_postfixExpression725);
					ctfSpecifierHead19=ctfSpecifierHead();
					state._fsp--;
					if (state.failed) return retval;
					if ( state.backtracking==0 ) adaptor.addChild(root_0, ctfSpecifierHead19.getTree());

					// org/eclipse/tracecompass/ctf/parser/CTFParser.g:228:22: ( postfixExpressionSuffix )+
					int cnt8=0;
					loop8:
					while (true) {
						int alt8=2;
						int LA8_0 = input.LA(1);
						if ( (LA8_0==ARROW||LA8_0==DOT||LA8_0==OPENBRAC) ) {
							alt8=1;
						}

						switch (alt8) {
						case 1 :
							// org/eclipse/tracecompass/ctf/parser/CTFParser.g:228:22: postfixExpressionSuffix
							{
							pushFollow(FOLLOW_postfixExpressionSuffix_in_postfixExpression727);
							postfixExpressionSuffix20=postfixExpressionSuffix();
							state._fsp--;
							if (state.failed) return retval;
							if ( state.backtracking==0 ) adaptor.addChild(root_0, postfixExpressionSuffix20.getTree());

							}
							break;

						default :
							if ( cnt8 >= 1 ) break loop8;
							if (state.backtracking>0) {state.failed=true; return retval;}
							EarlyExitException eee = new EarlyExitException(8, input);
							throw eee;
						}
						cnt8++;
					}

					}
					break;

			}
			retval.stop = input.LT(-1);

			if ( state.backtracking==0 ) {
			retval.tree = (CommonTree)adaptor.rulePostProcessing(root_0);
			adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
			}
		}

		    catch (RecognitionException e) {
		        throw e;
		    }

		finally {
			// do for sure before leaving
		}
		return retval;
	}
	// $ANTLR end "postfixExpression"


	public static class unaryExpression_return extends ParserRuleReturnScope {
		CommonTree tree;
		@Override
		public CommonTree getTree() { return tree; }
	};


	// $ANTLR start "unaryExpression"
	// org/eclipse/tracecompass/ctf/parser/CTFParser.g:231:1: unaryExpression : postfixExpression ;
	public final CTFParser.unaryExpression_return unaryExpression() throws RecognitionException {
		CTFParser.unaryExpression_return retval = new CTFParser.unaryExpression_return();
		retval.start = input.LT(1);

		CommonTree root_0 = null;

		ParserRuleReturnScope postfixExpression21 =null;


		try {
			// org/eclipse/tracecompass/ctf/parser/CTFParser.g:232:3: ( postfixExpression )
			// org/eclipse/tracecompass/ctf/parser/CTFParser.g:232:5: postfixExpression
			{
			root_0 = (CommonTree)adaptor.nil();


			pushFollow(FOLLOW_postfixExpression_in_unaryExpression743);
			postfixExpression21=postfixExpression();
			state._fsp--;
			if (state.failed) return retval;
			if ( state.backtracking==0 ) adaptor.addChild(root_0, postfixExpression21.getTree());

			}

			retval.stop = input.LT(-1);

			if ( state.backtracking==0 ) {
			retval.tree = (CommonTree)adaptor.rulePostProcessing(root_0);
			adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
			}
		}

		    catch (RecognitionException e) {
		        throw e;
		    }

		finally {
			// do for sure before leaving
		}
		return retval;
	}
	// $ANTLR end "unaryExpression"


	public static class enumConstant_return extends ParserRuleReturnScope {
		CommonTree tree;
		@Override
		public CommonTree getTree() { return tree; }
	};


	// $ANTLR start "enumConstant"
	// org/eclipse/tracecompass/ctf/parser/CTFParser.g:236:1: enumConstant : ( STRING_LITERAL -> ^( UNARY_EXPRESSION_STRING_QUOTES STRING_LITERAL ) | IDENTIFIER -> ^( UNARY_EXPRESSION_STRING IDENTIFIER ) | ctfKeyword -> ^( UNARY_EXPRESSION_STRING ctfKeyword ) );
	public final CTFParser.enumConstant_return enumConstant() throws RecognitionException {
		CTFParser.enumConstant_return retval = new CTFParser.enumConstant_return();
		retval.start = input.LT(1);

		CommonTree root_0 = null;

		Token STRING_LITERAL22=null;
		Token IDENTIFIER23=null;
		ParserRuleReturnScope ctfKeyword24 =null;

		CommonTree STRING_LITERAL22_tree=null;
		CommonTree IDENTIFIER23_tree=null;
		RewriteRuleTokenStream stream_STRING_LITERAL=new RewriteRuleTokenStream(adaptor,"token STRING_LITERAL");
		RewriteRuleTokenStream stream_IDENTIFIER=new RewriteRuleTokenStream(adaptor,"token IDENTIFIER");
		RewriteRuleSubtreeStream stream_ctfKeyword=new RewriteRuleSubtreeStream(adaptor,"rule ctfKeyword");

		try {
			// org/eclipse/tracecompass/ctf/parser/CTFParser.g:237:3: ( STRING_LITERAL -> ^( UNARY_EXPRESSION_STRING_QUOTES STRING_LITERAL ) | IDENTIFIER -> ^( UNARY_EXPRESSION_STRING IDENTIFIER ) | ctfKeyword -> ^( UNARY_EXPRESSION_STRING ctfKeyword ) )
			int alt10=3;
			switch ( input.LA(1) ) {
			case STRING_LITERAL:
				{
				alt10=1;
				}
				break;
			case IDENTIFIER:
				{
				alt10=2;
				}
				break;
			case ALIGNTOK:
			case EVENTTOK:
			case SIGNEDTOK:
			case STRINGTOK:
				{
				alt10=3;
				}
				break;
			default:
				if (state.backtracking>0) {state.failed=true; return retval;}
				NoViableAltException nvae =
					new NoViableAltException("", 10, 0, input);
				throw nvae;
			}
			switch (alt10) {
				case 1 :
					// org/eclipse/tracecompass/ctf/parser/CTFParser.g:237:5: STRING_LITERAL
					{
					STRING_LITERAL22=(Token)match(input,STRING_LITERAL,FOLLOW_STRING_LITERAL_in_enumConstant760); if (state.failed) return retval; 
					if ( state.backtracking==0 ) stream_STRING_LITERAL.add(STRING_LITERAL22);

					// AST REWRITE
					// elements: STRING_LITERAL
					// token labels: 
					// rule labels: retval
					// token list labels: 
					// rule list labels: 
					// wildcard labels: 
					if ( state.backtracking==0 ) {
					retval.tree = root_0;
					RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.getTree():null);

					root_0 = (CommonTree)adaptor.nil();
					// 237:20: -> ^( UNARY_EXPRESSION_STRING_QUOTES STRING_LITERAL )
					{
						// org/eclipse/tracecompass/ctf/parser/CTFParser.g:237:23: ^( UNARY_EXPRESSION_STRING_QUOTES STRING_LITERAL )
						{
						CommonTree root_1 = (CommonTree)adaptor.nil();
						root_1 = (CommonTree)adaptor.becomeRoot((CommonTree)adaptor.create(UNARY_EXPRESSION_STRING_QUOTES, "UNARY_EXPRESSION_STRING_QUOTES"), root_1);
						adaptor.addChild(root_1, stream_STRING_LITERAL.nextNode());
						adaptor.addChild(root_0, root_1);
						}

					}


					retval.tree = root_0;
					}

					}
					break;
				case 2 :
					// org/eclipse/tracecompass/ctf/parser/CTFParser.g:238:5: IDENTIFIER
					{
					IDENTIFIER23=(Token)match(input,IDENTIFIER,FOLLOW_IDENTIFIER_in_enumConstant774); if (state.failed) return retval; 
					if ( state.backtracking==0 ) stream_IDENTIFIER.add(IDENTIFIER23);

					// AST REWRITE
					// elements: IDENTIFIER
					// token labels: 
					// rule labels: retval
					// token list labels: 
					// rule list labels: 
					// wildcard labels: 
					if ( state.backtracking==0 ) {
					retval.tree = root_0;
					RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.getTree():null);

					root_0 = (CommonTree)adaptor.nil();
					// 238:16: -> ^( UNARY_EXPRESSION_STRING IDENTIFIER )
					{
						// org/eclipse/tracecompass/ctf/parser/CTFParser.g:238:19: ^( UNARY_EXPRESSION_STRING IDENTIFIER )
						{
						CommonTree root_1 = (CommonTree)adaptor.nil();
						root_1 = (CommonTree)adaptor.becomeRoot((CommonTree)adaptor.create(UNARY_EXPRESSION_STRING, "UNARY_EXPRESSION_STRING"), root_1);
						adaptor.addChild(root_1, stream_IDENTIFIER.nextNode());
						adaptor.addChild(root_0, root_1);
						}

					}


					retval.tree = root_0;
					}

					}
					break;
				case 3 :
					// org/eclipse/tracecompass/ctf/parser/CTFParser.g:239:5: ctfKeyword
					{
					pushFollow(FOLLOW_ctfKeyword_in_enumConstant788);
					ctfKeyword24=ctfKeyword();
					state._fsp--;
					if (state.failed) return retval;
					if ( state.backtracking==0 ) stream_ctfKeyword.add(ctfKeyword24.getTree());
					// AST REWRITE
					// elements: ctfKeyword
					// token labels: 
					// rule labels: retval
					// token list labels: 
					// rule list labels: 
					// wildcard labels: 
					if ( state.backtracking==0 ) {
					retval.tree = root_0;
					RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.getTree():null);

					root_0 = (CommonTree)adaptor.nil();
					// 239:16: -> ^( UNARY_EXPRESSION_STRING ctfKeyword )
					{
						// org/eclipse/tracecompass/ctf/parser/CTFParser.g:239:19: ^( UNARY_EXPRESSION_STRING ctfKeyword )
						{
						CommonTree root_1 = (CommonTree)adaptor.nil();
						root_1 = (CommonTree)adaptor.becomeRoot((CommonTree)adaptor.create(UNARY_EXPRESSION_STRING, "UNARY_EXPRESSION_STRING"), root_1);
						adaptor.addChild(root_1, stream_ctfKeyword.nextTree());
						adaptor.addChild(root_0, root_1);
						}

					}


					retval.tree = root_0;
					}

					}
					break;

			}
			retval.stop = input.LT(-1);

			if ( state.backtracking==0 ) {
			retval.tree = (CommonTree)adaptor.rulePostProcessing(root_0);
			adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
			}
		}

		    catch (RecognitionException e) {
		        throw e;
		    }

		finally {
			// do for sure before leaving
		}
		return retval;
	}
	// $ANTLR end "enumConstant"


	public static class declaration_return extends ParserRuleReturnScope {
		CommonTree tree;
		@Override
		public CommonTree getTree() { return tree; }
	};


	// $ANTLR start "declaration"
	// org/eclipse/tracecompass/ctf/parser/CTFParser.g:244:1: declaration : ( declarationSpecifiers ( declaratorList )? TERM -> {inTypedef()}? ^( DECLARATION ^( TYPEDEF declaratorList declarationSpecifiers ) ) -> ^( DECLARATION declarationSpecifiers ( declaratorList )? ) | ctfSpecifier TERM !);
	public final CTFParser.declaration_return declaration() throws RecognitionException {
		CTFParser.declaration_return retval = new CTFParser.declaration_return();
		retval.start = input.LT(1);

		CommonTree root_0 = null;

		Token TERM27=null;
		Token TERM29=null;
		ParserRuleReturnScope declarationSpecifiers25 =null;
		ParserRuleReturnScope declaratorList26 =null;
		ParserRuleReturnScope ctfSpecifier28 =null;

		CommonTree TERM27_tree=null;
		CommonTree TERM29_tree=null;
		RewriteRuleTokenStream stream_TERM=new RewriteRuleTokenStream(adaptor,"token TERM");
		RewriteRuleSubtreeStream stream_declaratorList=new RewriteRuleSubtreeStream(adaptor,"rule declaratorList");
		RewriteRuleSubtreeStream stream_declarationSpecifiers=new RewriteRuleSubtreeStream(adaptor,"rule declarationSpecifiers");

		try {
			// org/eclipse/tracecompass/ctf/parser/CTFParser.g:250:3: ( declarationSpecifiers ( declaratorList )? TERM -> {inTypedef()}? ^( DECLARATION ^( TYPEDEF declaratorList declarationSpecifiers ) ) -> ^( DECLARATION declarationSpecifiers ( declaratorList )? ) | ctfSpecifier TERM !)
			int alt12=2;
			int LA12_0 = input.LA(1);
			if ( (LA12_0==BOOLTOK||LA12_0==CHARTOK||(LA12_0 >= COMPLEXTOK && LA12_0 <= CONSTTOK)||LA12_0==DOUBLETOK||LA12_0==ENUMTOK||(LA12_0 >= FLOATINGPOINTTOK && LA12_0 <= FLOATTOK)||LA12_0==IMAGINARYTOK||LA12_0==INTEGERTOK||LA12_0==INTTOK||LA12_0==LONGTOK||LA12_0==SHORTTOK||LA12_0==SIGNEDTOK||LA12_0==STRINGTOK||LA12_0==STRUCTTOK||LA12_0==TYPEDEFTOK||(LA12_0 >= UNSIGNEDTOK && LA12_0 <= VOIDTOK)) ) {
				alt12=1;
			}
			else if ( (LA12_0==IDENTIFIER) && (( inTypealiasAlias() || isTypeName(input.LT(1).getText()) ))) {
				alt12=1;
			}
			else if ( (LA12_0==CALLSITETOK||LA12_0==CLOCKTOK||LA12_0==ENVTOK||LA12_0==EVENTTOK||LA12_0==STREAMTOK||(LA12_0 >= TRACETOK && LA12_0 <= TYPEALIASTOK)) ) {
				alt12=2;
			}

			else {
				if (state.backtracking>0) {state.failed=true; return retval;}
				NoViableAltException nvae =
					new NoViableAltException("", 12, 0, input);
				throw nvae;
			}

			switch (alt12) {
				case 1 :
					// org/eclipse/tracecompass/ctf/parser/CTFParser.g:250:5: declarationSpecifiers ( declaratorList )? TERM
					{
					pushFollow(FOLLOW_declarationSpecifiers_in_declaration816);
					declarationSpecifiers25=declarationSpecifiers();
					state._fsp--;
					if (state.failed) return retval;
					if ( state.backtracking==0 ) stream_declarationSpecifiers.add(declarationSpecifiers25.getTree());
					// org/eclipse/tracecompass/ctf/parser/CTFParser.g:250:27: ( declaratorList )?
					int alt11=2;
					int LA11_0 = input.LA(1);
					if ( (LA11_0==IDENTIFIER||LA11_0==POINTER) ) {
						alt11=1;
					}
					switch (alt11) {
						case 1 :
							// org/eclipse/tracecompass/ctf/parser/CTFParser.g:250:27: declaratorList
							{
							pushFollow(FOLLOW_declaratorList_in_declaration818);
							declaratorList26=declaratorList();
							state._fsp--;
							if (state.failed) return retval;
							if ( state.backtracking==0 ) stream_declaratorList.add(declaratorList26.getTree());
							}
							break;

					}

					TERM27=(Token)match(input,TERM,FOLLOW_TERM_in_declaration821); if (state.failed) return retval; 
					if ( state.backtracking==0 ) stream_TERM.add(TERM27);

					// AST REWRITE
					// elements: declaratorList, declaratorList, declarationSpecifiers, declarationSpecifiers
					// token labels: 
					// rule labels: retval
					// token list labels: 
					// rule list labels: 
					// wildcard labels: 
					if ( state.backtracking==0 ) {
					retval.tree = root_0;
					RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.getTree():null);

					root_0 = (CommonTree)adaptor.nil();
					// 253:7: -> {inTypedef()}? ^( DECLARATION ^( TYPEDEF declaratorList declarationSpecifiers ) )
					if (inTypedef()) {
						// org/eclipse/tracecompass/ctf/parser/CTFParser.g:254:10: ^( DECLARATION ^( TYPEDEF declaratorList declarationSpecifiers ) )
						{
						CommonTree root_1 = (CommonTree)adaptor.nil();
						root_1 = (CommonTree)adaptor.becomeRoot((CommonTree)adaptor.create(DECLARATION, "DECLARATION"), root_1);
						// org/eclipse/tracecompass/ctf/parser/CTFParser.g:254:24: ^( TYPEDEF declaratorList declarationSpecifiers )
						{
						CommonTree root_2 = (CommonTree)adaptor.nil();
						root_2 = (CommonTree)adaptor.becomeRoot((CommonTree)adaptor.create(TYPEDEF, "TYPEDEF"), root_2);
						adaptor.addChild(root_2, stream_declaratorList.nextTree());
						adaptor.addChild(root_2, stream_declarationSpecifiers.nextTree());
						adaptor.addChild(root_1, root_2);
						}

						adaptor.addChild(root_0, root_1);
						}

					}

					else // 255:7: -> ^( DECLARATION declarationSpecifiers ( declaratorList )? )
					{
						// org/eclipse/tracecompass/ctf/parser/CTFParser.g:255:10: ^( DECLARATION declarationSpecifiers ( declaratorList )? )
						{
						CommonTree root_1 = (CommonTree)adaptor.nil();
						root_1 = (CommonTree)adaptor.becomeRoot((CommonTree)adaptor.create(DECLARATION, "DECLARATION"), root_1);
						adaptor.addChild(root_1, stream_declarationSpecifiers.nextTree());
						// org/eclipse/tracecompass/ctf/parser/CTFParser.g:255:46: ( declaratorList )?
						if ( stream_declaratorList.hasNext() ) {
							adaptor.addChild(root_1, stream_declaratorList.nextTree());
						}
						stream_declaratorList.reset();

						adaptor.addChild(root_0, root_1);
						}

					}


					retval.tree = root_0;
					}

					}
					break;
				case 2 :
					// org/eclipse/tracecompass/ctf/parser/CTFParser.g:256:5: ctfSpecifier TERM !
					{
					root_0 = (CommonTree)adaptor.nil();


					pushFollow(FOLLOW_ctfSpecifier_in_declaration889);
					ctfSpecifier28=ctfSpecifier();
					state._fsp--;
					if (state.failed) return retval;
					if ( state.backtracking==0 ) adaptor.addChild(root_0, ctfSpecifier28.getTree());

					TERM29=(Token)match(input,TERM,FOLLOW_TERM_in_declaration891); if (state.failed) return retval;
					}
					break;

			}
			retval.stop = input.LT(-1);

			if ( state.backtracking==0 ) {
			retval.tree = (CommonTree)adaptor.rulePostProcessing(root_0);
			adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
			}
			if ( state.backtracking==0 ) {
			    if (inTypedef()) {
			        typedefOff();
			    }
			}
		}

		    catch (RecognitionException e) {
		        throw e;
		    }

		finally {
			// do for sure before leaving
		}
		return retval;
	}
	// $ANTLR end "declaration"


	public static class declarationSpecifiers_return extends ParserRuleReturnScope {
		CommonTree tree;
		@Override
		public CommonTree getTree() { return tree; }
	};


	// $ANTLR start "declarationSpecifiers"
	// org/eclipse/tracecompass/ctf/parser/CTFParser.g:259:1: declarationSpecifiers : ( storageClassSpecifier | typeQualifier | typeSpecifier )+ -> ^( TYPE_SPECIFIER_LIST ( typeQualifier )* ( typeSpecifier )* ) ;
	public final CTFParser.declarationSpecifiers_return declarationSpecifiers() throws RecognitionException {
		CTFParser.declarationSpecifiers_return retval = new CTFParser.declarationSpecifiers_return();
		retval.start = input.LT(1);

		CommonTree root_0 = null;

		ParserRuleReturnScope storageClassSpecifier30 =null;
		ParserRuleReturnScope typeQualifier31 =null;
		ParserRuleReturnScope typeSpecifier32 =null;

		RewriteRuleSubtreeStream stream_typeSpecifier=new RewriteRuleSubtreeStream(adaptor,"rule typeSpecifier");
		RewriteRuleSubtreeStream stream_typeQualifier=new RewriteRuleSubtreeStream(adaptor,"rule typeQualifier");
		RewriteRuleSubtreeStream stream_storageClassSpecifier=new RewriteRuleSubtreeStream(adaptor,"rule storageClassSpecifier");

		try {
			// org/eclipse/tracecompass/ctf/parser/CTFParser.g:260:3: ( ( storageClassSpecifier | typeQualifier | typeSpecifier )+ -> ^( TYPE_SPECIFIER_LIST ( typeQualifier )* ( typeSpecifier )* ) )
			// org/eclipse/tracecompass/ctf/parser/CTFParser.g:260:5: ( storageClassSpecifier | typeQualifier | typeSpecifier )+
			{
			// org/eclipse/tracecompass/ctf/parser/CTFParser.g:260:5: ( storageClassSpecifier | typeQualifier | typeSpecifier )+
			int cnt13=0;
			loop13:
			while (true) {
				int alt13=4;
				switch ( input.LA(1) ) {
				case IDENTIFIER:
					{
					int LA13_2 = input.LA(2);
					if ( ((( inTypealiasAlias() || isTypeName(input.LT(1).getText()) )&&(inTypealiasAlias() || isTypeName(input.LT(1).getText())))) ) {
						alt13=3;
					}

					}
					break;
				case TYPEDEFTOK:
					{
					alt13=1;
					}
					break;
				case CONSTTOK:
					{
					alt13=2;
					}
					break;
				case BOOLTOK:
				case CHARTOK:
				case COMPLEXTOK:
				case DOUBLETOK:
				case ENUMTOK:
				case FLOATINGPOINTTOK:
				case FLOATTOK:
				case IMAGINARYTOK:
				case INTEGERTOK:
				case INTTOK:
				case LONGTOK:
				case SHORTTOK:
				case SIGNEDTOK:
				case STRINGTOK:
				case STRUCTTOK:
				case UNSIGNEDTOK:
				case VARIANTTOK:
				case VOIDTOK:
					{
					alt13=3;
					}
					break;
				}
				switch (alt13) {
				case 1 :
					// org/eclipse/tracecompass/ctf/parser/CTFParser.g:263:9: storageClassSpecifier
					{
					pushFollow(FOLLOW_storageClassSpecifier_in_declarationSpecifiers929);
					storageClassSpecifier30=storageClassSpecifier();
					state._fsp--;
					if (state.failed) return retval;
					if ( state.backtracking==0 ) stream_storageClassSpecifier.add(storageClassSpecifier30.getTree());
					}
					break;
				case 2 :
					// org/eclipse/tracecompass/ctf/parser/CTFParser.g:264:9: typeQualifier
					{
					pushFollow(FOLLOW_typeQualifier_in_declarationSpecifiers939);
					typeQualifier31=typeQualifier();
					state._fsp--;
					if (state.failed) return retval;
					if ( state.backtracking==0 ) stream_typeQualifier.add(typeQualifier31.getTree());
					}
					break;
				case 3 :
					// org/eclipse/tracecompass/ctf/parser/CTFParser.g:265:9: typeSpecifier
					{
					pushFollow(FOLLOW_typeSpecifier_in_declarationSpecifiers949);
					typeSpecifier32=typeSpecifier();
					state._fsp--;
					if (state.failed) return retval;
					if ( state.backtracking==0 ) stream_typeSpecifier.add(typeSpecifier32.getTree());
					}
					break;

				default :
					if ( cnt13 >= 1 ) break loop13;
					if (state.backtracking>0) {state.failed=true; return retval;}
					EarlyExitException eee = new EarlyExitException(13, input);
					throw eee;
				}
				cnt13++;
			}

			// AST REWRITE
			// elements: typeQualifier, typeSpecifier
			// token labels: 
			// rule labels: retval
			// token list labels: 
			// rule list labels: 
			// wildcard labels: 
			if ( state.backtracking==0 ) {
			retval.tree = root_0;
			RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.getTree():null);

			root_0 = (CommonTree)adaptor.nil();
			// 266:6: -> ^( TYPE_SPECIFIER_LIST ( typeQualifier )* ( typeSpecifier )* )
			{
				// org/eclipse/tracecompass/ctf/parser/CTFParser.g:266:9: ^( TYPE_SPECIFIER_LIST ( typeQualifier )* ( typeSpecifier )* )
				{
				CommonTree root_1 = (CommonTree)adaptor.nil();
				root_1 = (CommonTree)adaptor.becomeRoot((CommonTree)adaptor.create(TYPE_SPECIFIER_LIST, "TYPE_SPECIFIER_LIST"), root_1);
				// org/eclipse/tracecompass/ctf/parser/CTFParser.g:266:31: ( typeQualifier )*
				while ( stream_typeQualifier.hasNext() ) {
					adaptor.addChild(root_1, stream_typeQualifier.nextTree());
				}
				stream_typeQualifier.reset();

				// org/eclipse/tracecompass/ctf/parser/CTFParser.g:266:46: ( typeSpecifier )*
				while ( stream_typeSpecifier.hasNext() ) {
					adaptor.addChild(root_1, stream_typeSpecifier.nextTree());
				}
				stream_typeSpecifier.reset();

				adaptor.addChild(root_0, root_1);
				}

			}


			retval.tree = root_0;
			}

			}

			retval.stop = input.LT(-1);

			if ( state.backtracking==0 ) {
			retval.tree = (CommonTree)adaptor.rulePostProcessing(root_0);
			adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
			}
		}

		    catch (RecognitionException e) {
		        throw e;
		    }

		finally {
			// do for sure before leaving
		}
		return retval;
	}
	// $ANTLR end "declarationSpecifiers"


	public static class declaratorList_return extends ParserRuleReturnScope {
		CommonTree tree;
		@Override
		public CommonTree getTree() { return tree; }
	};


	// $ANTLR start "declaratorList"
	// org/eclipse/tracecompass/ctf/parser/CTFParser.g:269:1: declaratorList : declarator ( SEPARATOR declarator )* -> ^( TYPE_DECLARATOR_LIST ( declarator )+ ) ;
	public final CTFParser.declaratorList_return declaratorList() throws RecognitionException {
		CTFParser.declaratorList_return retval = new CTFParser.declaratorList_return();
		retval.start = input.LT(1);

		CommonTree root_0 = null;

		Token SEPARATOR34=null;
		ParserRuleReturnScope declarator33 =null;
		ParserRuleReturnScope declarator35 =null;

		CommonTree SEPARATOR34_tree=null;
		RewriteRuleTokenStream stream_SEPARATOR=new RewriteRuleTokenStream(adaptor,"token SEPARATOR");
		RewriteRuleSubtreeStream stream_declarator=new RewriteRuleSubtreeStream(adaptor,"rule declarator");

		try {
			// org/eclipse/tracecompass/ctf/parser/CTFParser.g:270:3: ( declarator ( SEPARATOR declarator )* -> ^( TYPE_DECLARATOR_LIST ( declarator )+ ) )
			// org/eclipse/tracecompass/ctf/parser/CTFParser.g:270:5: declarator ( SEPARATOR declarator )*
			{
			pushFollow(FOLLOW_declarator_in_declaratorList979);
			declarator33=declarator();
			state._fsp--;
			if (state.failed) return retval;
			if ( state.backtracking==0 ) stream_declarator.add(declarator33.getTree());
			// org/eclipse/tracecompass/ctf/parser/CTFParser.g:270:16: ( SEPARATOR declarator )*
			loop14:
			while (true) {
				int alt14=2;
				int LA14_0 = input.LA(1);
				if ( (LA14_0==SEPARATOR) ) {
					alt14=1;
				}

				switch (alt14) {
				case 1 :
					// org/eclipse/tracecompass/ctf/parser/CTFParser.g:270:17: SEPARATOR declarator
					{
					SEPARATOR34=(Token)match(input,SEPARATOR,FOLLOW_SEPARATOR_in_declaratorList982); if (state.failed) return retval; 
					if ( state.backtracking==0 ) stream_SEPARATOR.add(SEPARATOR34);

					pushFollow(FOLLOW_declarator_in_declaratorList984);
					declarator35=declarator();
					state._fsp--;
					if (state.failed) return retval;
					if ( state.backtracking==0 ) stream_declarator.add(declarator35.getTree());
					}
					break;

				default :
					break loop14;
				}
			}

			// AST REWRITE
			// elements: declarator
			// token labels: 
			// rule labels: retval
			// token list labels: 
			// rule list labels: 
			// wildcard labels: 
			if ( state.backtracking==0 ) {
			retval.tree = root_0;
			RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.getTree():null);

			root_0 = (CommonTree)adaptor.nil();
			// 271:7: -> ^( TYPE_DECLARATOR_LIST ( declarator )+ )
			{
				// org/eclipse/tracecompass/ctf/parser/CTFParser.g:271:10: ^( TYPE_DECLARATOR_LIST ( declarator )+ )
				{
				CommonTree root_1 = (CommonTree)adaptor.nil();
				root_1 = (CommonTree)adaptor.becomeRoot((CommonTree)adaptor.create(TYPE_DECLARATOR_LIST, "TYPE_DECLARATOR_LIST"), root_1);
				if ( !(stream_declarator.hasNext()) ) {
					throw new RewriteEarlyExitException();
				}
				while ( stream_declarator.hasNext() ) {
					adaptor.addChild(root_1, stream_declarator.nextTree());
				}
				stream_declarator.reset();

				adaptor.addChild(root_0, root_1);
				}

			}


			retval.tree = root_0;
			}

			}

			retval.stop = input.LT(-1);

			if ( state.backtracking==0 ) {
			retval.tree = (CommonTree)adaptor.rulePostProcessing(root_0);
			adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
			}
		}

		    catch (RecognitionException e) {
		        throw e;
		    }

		finally {
			// do for sure before leaving
		}
		return retval;
	}
	// $ANTLR end "declaratorList"


	public static class abstractDeclaratorList_return extends ParserRuleReturnScope {
		CommonTree tree;
		@Override
		public CommonTree getTree() { return tree; }
	};


	// $ANTLR start "abstractDeclaratorList"
	// org/eclipse/tracecompass/ctf/parser/CTFParser.g:274:1: abstractDeclaratorList : abstractDeclarator ( SEPARATOR abstractDeclarator )* -> ^( TYPE_DECLARATOR_LIST ( abstractDeclarator )+ ) ;
	public final CTFParser.abstractDeclaratorList_return abstractDeclaratorList() throws RecognitionException {
		CTFParser.abstractDeclaratorList_return retval = new CTFParser.abstractDeclaratorList_return();
		retval.start = input.LT(1);

		CommonTree root_0 = null;

		Token SEPARATOR37=null;
		ParserRuleReturnScope abstractDeclarator36 =null;
		ParserRuleReturnScope abstractDeclarator38 =null;

		CommonTree SEPARATOR37_tree=null;
		RewriteRuleTokenStream stream_SEPARATOR=new RewriteRuleTokenStream(adaptor,"token SEPARATOR");
		RewriteRuleSubtreeStream stream_abstractDeclarator=new RewriteRuleSubtreeStream(adaptor,"rule abstractDeclarator");

		try {
			// org/eclipse/tracecompass/ctf/parser/CTFParser.g:275:3: ( abstractDeclarator ( SEPARATOR abstractDeclarator )* -> ^( TYPE_DECLARATOR_LIST ( abstractDeclarator )+ ) )
			// org/eclipse/tracecompass/ctf/parser/CTFParser.g:275:5: abstractDeclarator ( SEPARATOR abstractDeclarator )*
			{
			pushFollow(FOLLOW_abstractDeclarator_in_abstractDeclaratorList1014);
			abstractDeclarator36=abstractDeclarator();
			state._fsp--;
			if (state.failed) return retval;
			if ( state.backtracking==0 ) stream_abstractDeclarator.add(abstractDeclarator36.getTree());
			// org/eclipse/tracecompass/ctf/parser/CTFParser.g:275:24: ( SEPARATOR abstractDeclarator )*
			loop15:
			while (true) {
				int alt15=2;
				int LA15_0 = input.LA(1);
				if ( (LA15_0==SEPARATOR) ) {
					alt15=1;
				}

				switch (alt15) {
				case 1 :
					// org/eclipse/tracecompass/ctf/parser/CTFParser.g:275:25: SEPARATOR abstractDeclarator
					{
					SEPARATOR37=(Token)match(input,SEPARATOR,FOLLOW_SEPARATOR_in_abstractDeclaratorList1017); if (state.failed) return retval; 
					if ( state.backtracking==0 ) stream_SEPARATOR.add(SEPARATOR37);

					pushFollow(FOLLOW_abstractDeclarator_in_abstractDeclaratorList1019);
					abstractDeclarator38=abstractDeclarator();
					state._fsp--;
					if (state.failed) return retval;
					if ( state.backtracking==0 ) stream_abstractDeclarator.add(abstractDeclarator38.getTree());
					}
					break;

				default :
					break loop15;
				}
			}

			// AST REWRITE
			// elements: abstractDeclarator
			// token labels: 
			// rule labels: retval
			// token list labels: 
			// rule list labels: 
			// wildcard labels: 
			if ( state.backtracking==0 ) {
			retval.tree = root_0;
			RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.getTree():null);

			root_0 = (CommonTree)adaptor.nil();
			// 276:7: -> ^( TYPE_DECLARATOR_LIST ( abstractDeclarator )+ )
			{
				// org/eclipse/tracecompass/ctf/parser/CTFParser.g:276:10: ^( TYPE_DECLARATOR_LIST ( abstractDeclarator )+ )
				{
				CommonTree root_1 = (CommonTree)adaptor.nil();
				root_1 = (CommonTree)adaptor.becomeRoot((CommonTree)adaptor.create(TYPE_DECLARATOR_LIST, "TYPE_DECLARATOR_LIST"), root_1);
				if ( !(stream_abstractDeclarator.hasNext()) ) {
					throw new RewriteEarlyExitException();
				}
				while ( stream_abstractDeclarator.hasNext() ) {
					adaptor.addChild(root_1, stream_abstractDeclarator.nextTree());
				}
				stream_abstractDeclarator.reset();

				adaptor.addChild(root_0, root_1);
				}

			}


			retval.tree = root_0;
			}

			}

			retval.stop = input.LT(-1);

			if ( state.backtracking==0 ) {
			retval.tree = (CommonTree)adaptor.rulePostProcessing(root_0);
			adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
			}
		}

		    catch (RecognitionException e) {
		        throw e;
		    }

		finally {
			// do for sure before leaving
		}
		return retval;
	}
	// $ANTLR end "abstractDeclaratorList"


	public static class storageClassSpecifier_return extends ParserRuleReturnScope {
		CommonTree tree;
		@Override
		public CommonTree getTree() { return tree; }
	};


	// $ANTLR start "storageClassSpecifier"
	// org/eclipse/tracecompass/ctf/parser/CTFParser.g:279:1: storageClassSpecifier : TYPEDEFTOK ;
	public final CTFParser.storageClassSpecifier_return storageClassSpecifier() throws RecognitionException {
		CTFParser.storageClassSpecifier_return retval = new CTFParser.storageClassSpecifier_return();
		retval.start = input.LT(1);

		CommonTree root_0 = null;

		Token TYPEDEFTOK39=null;

		CommonTree TYPEDEFTOK39_tree=null;

		try {
			// org/eclipse/tracecompass/ctf/parser/CTFParser.g:280:3: ( TYPEDEFTOK )
			// org/eclipse/tracecompass/ctf/parser/CTFParser.g:280:5: TYPEDEFTOK
			{
			root_0 = (CommonTree)adaptor.nil();


			TYPEDEFTOK39=(Token)match(input,TYPEDEFTOK,FOLLOW_TYPEDEFTOK_in_storageClassSpecifier1049); if (state.failed) return retval;
			if ( state.backtracking==0 ) {
			TYPEDEFTOK39_tree = (CommonTree)adaptor.create(TYPEDEFTOK39);
			adaptor.addChild(root_0, TYPEDEFTOK39_tree);
			}

			if ( state.backtracking==0 ) { typedefOn(); }
			}

			retval.stop = input.LT(-1);

			if ( state.backtracking==0 ) {
			retval.tree = (CommonTree)adaptor.rulePostProcessing(root_0);
			adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
			}
		}

		    catch (RecognitionException e) {
		        throw e;
		    }

		finally {
			// do for sure before leaving
		}
		return retval;
	}
	// $ANTLR end "storageClassSpecifier"


	public static class typeSpecifier_return extends ParserRuleReturnScope {
		CommonTree tree;
		@Override
		public CommonTree getTree() { return tree; }
	};


	// $ANTLR start "typeSpecifier"
	// org/eclipse/tracecompass/ctf/parser/CTFParser.g:283:1: typeSpecifier : ( FLOATTOK | INTTOK | LONGTOK | SHORTTOK | SIGNEDTOK | UNSIGNEDTOK | CHARTOK | DOUBLETOK | VOIDTOK | BOOLTOK | COMPLEXTOK | IMAGINARYTOK | structSpecifier | variantSpecifier | enumSpecifier | ctfTypeSpecifier |{...}? => typedefName );
	public final CTFParser.typeSpecifier_return typeSpecifier() throws RecognitionException {
		CTFParser.typeSpecifier_return retval = new CTFParser.typeSpecifier_return();
		retval.start = input.LT(1);

		CommonTree root_0 = null;

		Token FLOATTOK40=null;
		Token INTTOK41=null;
		Token LONGTOK42=null;
		Token SHORTTOK43=null;
		Token SIGNEDTOK44=null;
		Token UNSIGNEDTOK45=null;
		Token CHARTOK46=null;
		Token DOUBLETOK47=null;
		Token VOIDTOK48=null;
		Token BOOLTOK49=null;
		Token COMPLEXTOK50=null;
		Token IMAGINARYTOK51=null;
		ParserRuleReturnScope structSpecifier52 =null;
		ParserRuleReturnScope variantSpecifier53 =null;
		ParserRuleReturnScope enumSpecifier54 =null;
		ParserRuleReturnScope ctfTypeSpecifier55 =null;
		ParserRuleReturnScope typedefName56 =null;

		CommonTree FLOATTOK40_tree=null;
		CommonTree INTTOK41_tree=null;
		CommonTree LONGTOK42_tree=null;
		CommonTree SHORTTOK43_tree=null;
		CommonTree SIGNEDTOK44_tree=null;
		CommonTree UNSIGNEDTOK45_tree=null;
		CommonTree CHARTOK46_tree=null;
		CommonTree DOUBLETOK47_tree=null;
		CommonTree VOIDTOK48_tree=null;
		CommonTree BOOLTOK49_tree=null;
		CommonTree COMPLEXTOK50_tree=null;
		CommonTree IMAGINARYTOK51_tree=null;

		try {
			// org/eclipse/tracecompass/ctf/parser/CTFParser.g:284:3: ( FLOATTOK | INTTOK | LONGTOK | SHORTTOK | SIGNEDTOK | UNSIGNEDTOK | CHARTOK | DOUBLETOK | VOIDTOK | BOOLTOK | COMPLEXTOK | IMAGINARYTOK | structSpecifier | variantSpecifier | enumSpecifier | ctfTypeSpecifier |{...}? => typedefName )
			int alt16=17;
			int LA16_0 = input.LA(1);
			if ( (LA16_0==FLOATTOK) ) {
				alt16=1;
			}
			else if ( (LA16_0==INTTOK) ) {
				alt16=2;
			}
			else if ( (LA16_0==LONGTOK) ) {
				alt16=3;
			}
			else if ( (LA16_0==SHORTTOK) ) {
				alt16=4;
			}
			else if ( (LA16_0==SIGNEDTOK) ) {
				alt16=5;
			}
			else if ( (LA16_0==UNSIGNEDTOK) ) {
				alt16=6;
			}
			else if ( (LA16_0==CHARTOK) ) {
				alt16=7;
			}
			else if ( (LA16_0==DOUBLETOK) ) {
				alt16=8;
			}
			else if ( (LA16_0==VOIDTOK) ) {
				alt16=9;
			}
			else if ( (LA16_0==BOOLTOK) ) {
				alt16=10;
			}
			else if ( (LA16_0==COMPLEXTOK) ) {
				alt16=11;
			}
			else if ( (LA16_0==IMAGINARYTOK) ) {
				alt16=12;
			}
			else if ( (LA16_0==STRUCTTOK) ) {
				alt16=13;
			}
			else if ( (LA16_0==VARIANTTOK) ) {
				alt16=14;
			}
			else if ( (LA16_0==ENUMTOK) ) {
				alt16=15;
			}
			else if ( (LA16_0==FLOATINGPOINTTOK||LA16_0==INTEGERTOK||LA16_0==STRINGTOK) ) {
				alt16=16;
			}
			else if ( (LA16_0==IDENTIFIER) && (( inTypealiasAlias() || isTypeName(input.LT(1).getText()) ))) {
				alt16=17;
			}

			switch (alt16) {
				case 1 :
					// org/eclipse/tracecompass/ctf/parser/CTFParser.g:284:5: FLOATTOK
					{
					root_0 = (CommonTree)adaptor.nil();


					FLOATTOK40=(Token)match(input,FLOATTOK,FOLLOW_FLOATTOK_in_typeSpecifier1065); if (state.failed) return retval;
					if ( state.backtracking==0 ) {
					FLOATTOK40_tree = (CommonTree)adaptor.create(FLOATTOK40);
					adaptor.addChild(root_0, FLOATTOK40_tree);
					}

					}
					break;
				case 2 :
					// org/eclipse/tracecompass/ctf/parser/CTFParser.g:285:5: INTTOK
					{
					root_0 = (CommonTree)adaptor.nil();


					INTTOK41=(Token)match(input,INTTOK,FOLLOW_INTTOK_in_typeSpecifier1071); if (state.failed) return retval;
					if ( state.backtracking==0 ) {
					INTTOK41_tree = (CommonTree)adaptor.create(INTTOK41);
					adaptor.addChild(root_0, INTTOK41_tree);
					}

					}
					break;
				case 3 :
					// org/eclipse/tracecompass/ctf/parser/CTFParser.g:286:5: LONGTOK
					{
					root_0 = (CommonTree)adaptor.nil();


					LONGTOK42=(Token)match(input,LONGTOK,FOLLOW_LONGTOK_in_typeSpecifier1077); if (state.failed) return retval;
					if ( state.backtracking==0 ) {
					LONGTOK42_tree = (CommonTree)adaptor.create(LONGTOK42);
					adaptor.addChild(root_0, LONGTOK42_tree);
					}

					}
					break;
				case 4 :
					// org/eclipse/tracecompass/ctf/parser/CTFParser.g:287:5: SHORTTOK
					{
					root_0 = (CommonTree)adaptor.nil();


					SHORTTOK43=(Token)match(input,SHORTTOK,FOLLOW_SHORTTOK_in_typeSpecifier1083); if (state.failed) return retval;
					if ( state.backtracking==0 ) {
					SHORTTOK43_tree = (CommonTree)adaptor.create(SHORTTOK43);
					adaptor.addChild(root_0, SHORTTOK43_tree);
					}

					}
					break;
				case 5 :
					// org/eclipse/tracecompass/ctf/parser/CTFParser.g:288:5: SIGNEDTOK
					{
					root_0 = (CommonTree)adaptor.nil();


					SIGNEDTOK44=(Token)match(input,SIGNEDTOK,FOLLOW_SIGNEDTOK_in_typeSpecifier1089); if (state.failed) return retval;
					if ( state.backtracking==0 ) {
					SIGNEDTOK44_tree = (CommonTree)adaptor.create(SIGNEDTOK44);
					adaptor.addChild(root_0, SIGNEDTOK44_tree);
					}

					}
					break;
				case 6 :
					// org/eclipse/tracecompass/ctf/parser/CTFParser.g:289:5: UNSIGNEDTOK
					{
					root_0 = (CommonTree)adaptor.nil();


					UNSIGNEDTOK45=(Token)match(input,UNSIGNEDTOK,FOLLOW_UNSIGNEDTOK_in_typeSpecifier1095); if (state.failed) return retval;
					if ( state.backtracking==0 ) {
					UNSIGNEDTOK45_tree = (CommonTree)adaptor.create(UNSIGNEDTOK45);
					adaptor.addChild(root_0, UNSIGNEDTOK45_tree);
					}

					}
					break;
				case 7 :
					// org/eclipse/tracecompass/ctf/parser/CTFParser.g:290:5: CHARTOK
					{
					root_0 = (CommonTree)adaptor.nil();


					CHARTOK46=(Token)match(input,CHARTOK,FOLLOW_CHARTOK_in_typeSpecifier1101); if (state.failed) return retval;
					if ( state.backtracking==0 ) {
					CHARTOK46_tree = (CommonTree)adaptor.create(CHARTOK46);
					adaptor.addChild(root_0, CHARTOK46_tree);
					}

					}
					break;
				case 8 :
					// org/eclipse/tracecompass/ctf/parser/CTFParser.g:291:5: DOUBLETOK
					{
					root_0 = (CommonTree)adaptor.nil();


					DOUBLETOK47=(Token)match(input,DOUBLETOK,FOLLOW_DOUBLETOK_in_typeSpecifier1107); if (state.failed) return retval;
					if ( state.backtracking==0 ) {
					DOUBLETOK47_tree = (CommonTree)adaptor.create(DOUBLETOK47);
					adaptor.addChild(root_0, DOUBLETOK47_tree);
					}

					}
					break;
				case 9 :
					// org/eclipse/tracecompass/ctf/parser/CTFParser.g:292:5: VOIDTOK
					{
					root_0 = (CommonTree)adaptor.nil();


					VOIDTOK48=(Token)match(input,VOIDTOK,FOLLOW_VOIDTOK_in_typeSpecifier1113); if (state.failed) return retval;
					if ( state.backtracking==0 ) {
					VOIDTOK48_tree = (CommonTree)adaptor.create(VOIDTOK48);
					adaptor.addChild(root_0, VOIDTOK48_tree);
					}

					}
					break;
				case 10 :
					// org/eclipse/tracecompass/ctf/parser/CTFParser.g:293:5: BOOLTOK
					{
					root_0 = (CommonTree)adaptor.nil();


					BOOLTOK49=(Token)match(input,BOOLTOK,FOLLOW_BOOLTOK_in_typeSpecifier1119); if (state.failed) return retval;
					if ( state.backtracking==0 ) {
					BOOLTOK49_tree = (CommonTree)adaptor.create(BOOLTOK49);
					adaptor.addChild(root_0, BOOLTOK49_tree);
					}

					}
					break;
				case 11 :
					// org/eclipse/tracecompass/ctf/parser/CTFParser.g:294:5: COMPLEXTOK
					{
					root_0 = (CommonTree)adaptor.nil();


					COMPLEXTOK50=(Token)match(input,COMPLEXTOK,FOLLOW_COMPLEXTOK_in_typeSpecifier1125); if (state.failed) return retval;
					if ( state.backtracking==0 ) {
					COMPLEXTOK50_tree = (CommonTree)adaptor.create(COMPLEXTOK50);
					adaptor.addChild(root_0, COMPLEXTOK50_tree);
					}

					}
					break;
				case 12 :
					// org/eclipse/tracecompass/ctf/parser/CTFParser.g:295:5: IMAGINARYTOK
					{
					root_0 = (CommonTree)adaptor.nil();


					IMAGINARYTOK51=(Token)match(input,IMAGINARYTOK,FOLLOW_IMAGINARYTOK_in_typeSpecifier1131); if (state.failed) return retval;
					if ( state.backtracking==0 ) {
					IMAGINARYTOK51_tree = (CommonTree)adaptor.create(IMAGINARYTOK51);
					adaptor.addChild(root_0, IMAGINARYTOK51_tree);
					}

					}
					break;
				case 13 :
					// org/eclipse/tracecompass/ctf/parser/CTFParser.g:296:5: structSpecifier
					{
					root_0 = (CommonTree)adaptor.nil();


					pushFollow(FOLLOW_structSpecifier_in_typeSpecifier1137);
					structSpecifier52=structSpecifier();
					state._fsp--;
					if (state.failed) return retval;
					if ( state.backtracking==0 ) adaptor.addChild(root_0, structSpecifier52.getTree());

					}
					break;
				case 14 :
					// org/eclipse/tracecompass/ctf/parser/CTFParser.g:297:5: variantSpecifier
					{
					root_0 = (CommonTree)adaptor.nil();


					pushFollow(FOLLOW_variantSpecifier_in_typeSpecifier1143);
					variantSpecifier53=variantSpecifier();
					state._fsp--;
					if (state.failed) return retval;
					if ( state.backtracking==0 ) adaptor.addChild(root_0, variantSpecifier53.getTree());

					}
					break;
				case 15 :
					// org/eclipse/tracecompass/ctf/parser/CTFParser.g:298:5: enumSpecifier
					{
					root_0 = (CommonTree)adaptor.nil();


					pushFollow(FOLLOW_enumSpecifier_in_typeSpecifier1149);
					enumSpecifier54=enumSpecifier();
					state._fsp--;
					if (state.failed) return retval;
					if ( state.backtracking==0 ) adaptor.addChild(root_0, enumSpecifier54.getTree());

					}
					break;
				case 16 :
					// org/eclipse/tracecompass/ctf/parser/CTFParser.g:299:5: ctfTypeSpecifier
					{
					root_0 = (CommonTree)adaptor.nil();


					pushFollow(FOLLOW_ctfTypeSpecifier_in_typeSpecifier1155);
					ctfTypeSpecifier55=ctfTypeSpecifier();
					state._fsp--;
					if (state.failed) return retval;
					if ( state.backtracking==0 ) adaptor.addChild(root_0, ctfTypeSpecifier55.getTree());

					}
					break;
				case 17 :
					// org/eclipse/tracecompass/ctf/parser/CTFParser.g:300:5: {...}? => typedefName
					{
					root_0 = (CommonTree)adaptor.nil();


					if ( !(( inTypealiasAlias() || isTypeName(input.LT(1).getText()) )) ) {
						if (state.backtracking>0) {state.failed=true; return retval;}
						throw new FailedPredicateException(input, "typeSpecifier", " inTypealiasAlias() || isTypeName(input.LT(1).getText()) ");
					}
					pushFollow(FOLLOW_typedefName_in_typeSpecifier1165);
					typedefName56=typedefName();
					state._fsp--;
					if (state.failed) return retval;
					if ( state.backtracking==0 ) adaptor.addChild(root_0, typedefName56.getTree());

					}
					break;

			}
			retval.stop = input.LT(-1);

			if ( state.backtracking==0 ) {
			retval.tree = (CommonTree)adaptor.rulePostProcessing(root_0);
			adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
			}
		}

		    catch (RecognitionException e) {
		        throw e;
		    }

		finally {
			// do for sure before leaving
		}
		return retval;
	}
	// $ANTLR end "typeSpecifier"


	public static class typeQualifier_return extends ParserRuleReturnScope {
		CommonTree tree;
		@Override
		public CommonTree getTree() { return tree; }
	};


	// $ANTLR start "typeQualifier"
	// org/eclipse/tracecompass/ctf/parser/CTFParser.g:303:1: typeQualifier : CONSTTOK ;
	public final CTFParser.typeQualifier_return typeQualifier() throws RecognitionException {
		CTFParser.typeQualifier_return retval = new CTFParser.typeQualifier_return();
		retval.start = input.LT(1);

		CommonTree root_0 = null;

		Token CONSTTOK57=null;

		CommonTree CONSTTOK57_tree=null;

		try {
			// org/eclipse/tracecompass/ctf/parser/CTFParser.g:304:3: ( CONSTTOK )
			// org/eclipse/tracecompass/ctf/parser/CTFParser.g:304:5: CONSTTOK
			{
			root_0 = (CommonTree)adaptor.nil();


			CONSTTOK57=(Token)match(input,CONSTTOK,FOLLOW_CONSTTOK_in_typeQualifier1178); if (state.failed) return retval;
			if ( state.backtracking==0 ) {
			CONSTTOK57_tree = (CommonTree)adaptor.create(CONSTTOK57);
			adaptor.addChild(root_0, CONSTTOK57_tree);
			}

			}

			retval.stop = input.LT(-1);

			if ( state.backtracking==0 ) {
			retval.tree = (CommonTree)adaptor.rulePostProcessing(root_0);
			adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
			}
		}

		    catch (RecognitionException e) {
		        throw e;
		    }

		finally {
			// do for sure before leaving
		}
		return retval;
	}
	// $ANTLR end "typeQualifier"


	public static class alignAttribute_return extends ParserRuleReturnScope {
		CommonTree tree;
		@Override
		public CommonTree getTree() { return tree; }
	};


	// $ANTLR start "alignAttribute"
	// org/eclipse/tracecompass/ctf/parser/CTFParser.g:307:1: alignAttribute : ALIGNTOK LPAREN unaryExpression RPAREN -> ^( ALIGN unaryExpression ) ;
	public final CTFParser.alignAttribute_return alignAttribute() throws RecognitionException {
		CTFParser.alignAttribute_return retval = new CTFParser.alignAttribute_return();
		retval.start = input.LT(1);

		CommonTree root_0 = null;

		Token ALIGNTOK58=null;
		Token LPAREN59=null;
		Token RPAREN61=null;
		ParserRuleReturnScope unaryExpression60 =null;

		CommonTree ALIGNTOK58_tree=null;
		CommonTree LPAREN59_tree=null;
		CommonTree RPAREN61_tree=null;
		RewriteRuleTokenStream stream_RPAREN=new RewriteRuleTokenStream(adaptor,"token RPAREN");
		RewriteRuleTokenStream stream_ALIGNTOK=new RewriteRuleTokenStream(adaptor,"token ALIGNTOK");
		RewriteRuleTokenStream stream_LPAREN=new RewriteRuleTokenStream(adaptor,"token LPAREN");
		RewriteRuleSubtreeStream stream_unaryExpression=new RewriteRuleSubtreeStream(adaptor,"rule unaryExpression");

		try {
			// org/eclipse/tracecompass/ctf/parser/CTFParser.g:308:3: ( ALIGNTOK LPAREN unaryExpression RPAREN -> ^( ALIGN unaryExpression ) )
			// org/eclipse/tracecompass/ctf/parser/CTFParser.g:308:5: ALIGNTOK LPAREN unaryExpression RPAREN
			{
			ALIGNTOK58=(Token)match(input,ALIGNTOK,FOLLOW_ALIGNTOK_in_alignAttribute1191); if (state.failed) return retval; 
			if ( state.backtracking==0 ) stream_ALIGNTOK.add(ALIGNTOK58);

			LPAREN59=(Token)match(input,LPAREN,FOLLOW_LPAREN_in_alignAttribute1193); if (state.failed) return retval; 
			if ( state.backtracking==0 ) stream_LPAREN.add(LPAREN59);

			pushFollow(FOLLOW_unaryExpression_in_alignAttribute1195);
			unaryExpression60=unaryExpression();
			state._fsp--;
			if (state.failed) return retval;
			if ( state.backtracking==0 ) stream_unaryExpression.add(unaryExpression60.getTree());
			RPAREN61=(Token)match(input,RPAREN,FOLLOW_RPAREN_in_alignAttribute1197); if (state.failed) return retval; 
			if ( state.backtracking==0 ) stream_RPAREN.add(RPAREN61);

			// AST REWRITE
			// elements: unaryExpression
			// token labels: 
			// rule labels: retval
			// token list labels: 
			// rule list labels: 
			// wildcard labels: 
			if ( state.backtracking==0 ) {
			retval.tree = root_0;
			RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.getTree():null);

			root_0 = (CommonTree)adaptor.nil();
			// 308:44: -> ^( ALIGN unaryExpression )
			{
				// org/eclipse/tracecompass/ctf/parser/CTFParser.g:308:47: ^( ALIGN unaryExpression )
				{
				CommonTree root_1 = (CommonTree)adaptor.nil();
				root_1 = (CommonTree)adaptor.becomeRoot((CommonTree)adaptor.create(ALIGN, "ALIGN"), root_1);
				adaptor.addChild(root_1, stream_unaryExpression.nextTree());
				adaptor.addChild(root_0, root_1);
				}

			}


			retval.tree = root_0;
			}

			}

			retval.stop = input.LT(-1);

			if ( state.backtracking==0 ) {
			retval.tree = (CommonTree)adaptor.rulePostProcessing(root_0);
			adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
			}
		}

		    catch (RecognitionException e) {
		        throw e;
		    }

		finally {
			// do for sure before leaving
		}
		return retval;
	}
	// $ANTLR end "alignAttribute"


	public static class structBody_return extends ParserRuleReturnScope {
		CommonTree tree;
		@Override
		public CommonTree getTree() { return tree; }
	};


	// $ANTLR start "structBody"
	// org/eclipse/tracecompass/ctf/parser/CTFParser.g:312:1: structBody : LCURL ( structOrVariantDeclarationList )? RCURL -> ^( STRUCT_BODY ( structOrVariantDeclarationList )? ) ;
	public final CTFParser.structBody_return structBody() throws RecognitionException {
		Symbols_stack.push(new Symbols_scope());

		CTFParser.structBody_return retval = new CTFParser.structBody_return();
		retval.start = input.LT(1);

		CommonTree root_0 = null;

		Token LCURL62=null;
		Token RCURL64=null;
		ParserRuleReturnScope structOrVariantDeclarationList63 =null;

		CommonTree LCURL62_tree=null;
		CommonTree RCURL64_tree=null;
		RewriteRuleTokenStream stream_LCURL=new RewriteRuleTokenStream(adaptor,"token LCURL");
		RewriteRuleTokenStream stream_RCURL=new RewriteRuleTokenStream(adaptor,"token RCURL");
		RewriteRuleSubtreeStream stream_structOrVariantDeclarationList=new RewriteRuleSubtreeStream(adaptor,"rule structOrVariantDeclarationList");


		    Symbols_stack.peek().types = new HashSet<String>();

		try {
			// org/eclipse/tracecompass/ctf/parser/CTFParser.g:317:3: ( LCURL ( structOrVariantDeclarationList )? RCURL -> ^( STRUCT_BODY ( structOrVariantDeclarationList )? ) )
			// org/eclipse/tracecompass/ctf/parser/CTFParser.g:317:5: LCURL ( structOrVariantDeclarationList )? RCURL
			{
			LCURL62=(Token)match(input,LCURL,FOLLOW_LCURL_in_structBody1231); if (state.failed) return retval; 
			if ( state.backtracking==0 ) stream_LCURL.add(LCURL62);

			// org/eclipse/tracecompass/ctf/parser/CTFParser.g:317:11: ( structOrVariantDeclarationList )?
			int alt17=2;
			int LA17_0 = input.LA(1);
			if ( (LA17_0==BOOLTOK||LA17_0==CHARTOK||(LA17_0 >= COMPLEXTOK && LA17_0 <= CONSTTOK)||LA17_0==DOUBLETOK||LA17_0==ENUMTOK||(LA17_0 >= FLOATINGPOINTTOK && LA17_0 <= FLOATTOK)||LA17_0==IMAGINARYTOK||LA17_0==INTEGERTOK||LA17_0==INTTOK||LA17_0==LONGTOK||LA17_0==SHORTTOK||LA17_0==SIGNEDTOK||LA17_0==STRINGTOK||LA17_0==STRUCTTOK||LA17_0==TYPEDEFTOK||(LA17_0 >= UNSIGNEDTOK && LA17_0 <= VOIDTOK)) ) {
				alt17=1;
			}
			else if ( (LA17_0==IDENTIFIER) && (( inTypealiasAlias() || isTypeName(input.LT(1).getText()) ))) {
				alt17=1;
			}
			else if ( (LA17_0==TYPEALIASTOK) ) {
				alt17=1;
			}
			switch (alt17) {
				case 1 :
					// org/eclipse/tracecompass/ctf/parser/CTFParser.g:317:11: structOrVariantDeclarationList
					{
					pushFollow(FOLLOW_structOrVariantDeclarationList_in_structBody1233);
					structOrVariantDeclarationList63=structOrVariantDeclarationList();
					state._fsp--;
					if (state.failed) return retval;
					if ( state.backtracking==0 ) stream_structOrVariantDeclarationList.add(structOrVariantDeclarationList63.getTree());
					}
					break;

			}

			RCURL64=(Token)match(input,RCURL,FOLLOW_RCURL_in_structBody1236); if (state.failed) return retval; 
			if ( state.backtracking==0 ) stream_RCURL.add(RCURL64);

			// AST REWRITE
			// elements: structOrVariantDeclarationList
			// token labels: 
			// rule labels: retval
			// token list labels: 
			// rule list labels: 
			// wildcard labels: 
			if ( state.backtracking==0 ) {
			retval.tree = root_0;
			RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.getTree():null);

			root_0 = (CommonTree)adaptor.nil();
			// 318:7: -> ^( STRUCT_BODY ( structOrVariantDeclarationList )? )
			{
				// org/eclipse/tracecompass/ctf/parser/CTFParser.g:318:10: ^( STRUCT_BODY ( structOrVariantDeclarationList )? )
				{
				CommonTree root_1 = (CommonTree)adaptor.nil();
				root_1 = (CommonTree)adaptor.becomeRoot((CommonTree)adaptor.create(STRUCT_BODY, "STRUCT_BODY"), root_1);
				// org/eclipse/tracecompass/ctf/parser/CTFParser.g:318:24: ( structOrVariantDeclarationList )?
				if ( stream_structOrVariantDeclarationList.hasNext() ) {
					adaptor.addChild(root_1, stream_structOrVariantDeclarationList.nextTree());
				}
				stream_structOrVariantDeclarationList.reset();

				adaptor.addChild(root_0, root_1);
				}

			}


			retval.tree = root_0;
			}

			}

			retval.stop = input.LT(-1);

			if ( state.backtracking==0 ) {
			retval.tree = (CommonTree)adaptor.rulePostProcessing(root_0);
			adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
			}
		}

		    catch (RecognitionException e) {
		        throw e;
		    }

		finally {
			// do for sure before leaving
			Symbols_stack.pop();

		}
		return retval;
	}
	// $ANTLR end "structBody"


	public static class structSpecifier_return extends ParserRuleReturnScope {
		CommonTree tree;
		@Override
		public CommonTree getTree() { return tree; }
	};


	// $ANTLR start "structSpecifier"
	// org/eclipse/tracecompass/ctf/parser/CTFParser.g:321:1: structSpecifier : STRUCTTOK ( ( structName ( alignAttribute | ( structBody ( alignAttribute |) ) |) ) | ( structBody ( alignAttribute |) ) ) -> ^( STRUCT ( structName )? ( structBody )? ( alignAttribute )? ) ;
	public final CTFParser.structSpecifier_return structSpecifier() throws RecognitionException {
		CTFParser.structSpecifier_return retval = new CTFParser.structSpecifier_return();
		retval.start = input.LT(1);

		CommonTree root_0 = null;

		Token STRUCTTOK65=null;
		ParserRuleReturnScope structName66 =null;
		ParserRuleReturnScope alignAttribute67 =null;
		ParserRuleReturnScope structBody68 =null;
		ParserRuleReturnScope alignAttribute69 =null;
		ParserRuleReturnScope structBody70 =null;
		ParserRuleReturnScope alignAttribute71 =null;

		CommonTree STRUCTTOK65_tree=null;
		RewriteRuleTokenStream stream_STRUCTTOK=new RewriteRuleTokenStream(adaptor,"token STRUCTTOK");
		RewriteRuleSubtreeStream stream_structName=new RewriteRuleSubtreeStream(adaptor,"rule structName");
		RewriteRuleSubtreeStream stream_structBody=new RewriteRuleSubtreeStream(adaptor,"rule structBody");
		RewriteRuleSubtreeStream stream_alignAttribute=new RewriteRuleSubtreeStream(adaptor,"rule alignAttribute");

		try {
			// org/eclipse/tracecompass/ctf/parser/CTFParser.g:322:3: ( STRUCTTOK ( ( structName ( alignAttribute | ( structBody ( alignAttribute |) ) |) ) | ( structBody ( alignAttribute |) ) ) -> ^( STRUCT ( structName )? ( structBody )? ( alignAttribute )? ) )
			// org/eclipse/tracecompass/ctf/parser/CTFParser.g:322:5: STRUCTTOK ( ( structName ( alignAttribute | ( structBody ( alignAttribute |) ) |) ) | ( structBody ( alignAttribute |) ) )
			{
			STRUCTTOK65=(Token)match(input,STRUCTTOK,FOLLOW_STRUCTTOK_in_structSpecifier1264); if (state.failed) return retval; 
			if ( state.backtracking==0 ) stream_STRUCTTOK.add(STRUCTTOK65);

			// org/eclipse/tracecompass/ctf/parser/CTFParser.g:323:3: ( ( structName ( alignAttribute | ( structBody ( alignAttribute |) ) |) ) | ( structBody ( alignAttribute |) ) )
			int alt21=2;
			int LA21_0 = input.LA(1);
			if ( (LA21_0==IDENTIFIER) ) {
				alt21=1;
			}
			else if ( (LA21_0==LCURL) ) {
				alt21=2;
			}

			else {
				if (state.backtracking>0) {state.failed=true; return retval;}
				NoViableAltException nvae =
					new NoViableAltException("", 21, 0, input);
				throw nvae;
			}

			switch (alt21) {
				case 1 :
					// org/eclipse/tracecompass/ctf/parser/CTFParser.g:325:5: ( structName ( alignAttribute | ( structBody ( alignAttribute |) ) |) )
					{
					// org/eclipse/tracecompass/ctf/parser/CTFParser.g:325:5: ( structName ( alignAttribute | ( structBody ( alignAttribute |) ) |) )
					// org/eclipse/tracecompass/ctf/parser/CTFParser.g:326:9: structName ( alignAttribute | ( structBody ( alignAttribute |) ) |)
					{
					pushFollow(FOLLOW_structName_in_structSpecifier1289);
					structName66=structName();
					state._fsp--;
					if (state.failed) return retval;
					if ( state.backtracking==0 ) stream_structName.add(structName66.getTree());
					// org/eclipse/tracecompass/ctf/parser/CTFParser.g:327:9: ( alignAttribute | ( structBody ( alignAttribute |) ) |)
					int alt19=3;
					switch ( input.LA(1) ) {
					case ALIGNTOK:
						{
						alt19=1;
						}
						break;
					case LCURL:
						{
						switch ( input.LA(2) ) {
						case BOOLTOK:
						case CHARTOK:
						case COMPLEXTOK:
						case CONSTTOK:
						case DOUBLETOK:
						case ENUMTOK:
						case FLOATINGPOINTTOK:
						case FLOATTOK:
						case IMAGINARYTOK:
						case INTEGERTOK:
						case INTTOK:
						case LONGTOK:
						case RCURL:
						case SHORTTOK:
						case STRUCTTOK:
						case TYPEALIASTOK:
						case TYPEDEFTOK:
						case UNSIGNEDTOK:
						case VARIANTTOK:
						case VOIDTOK:
							{
							alt19=2;
							}
							break;
						case SIGNEDTOK:
							{
							int LA19_5 = input.LA(3);
							if ( (LA19_5==BOOLTOK||LA19_5==CHARTOK||(LA19_5 >= COMPLEXTOK && LA19_5 <= CONSTTOK)||LA19_5==DOUBLETOK||LA19_5==ENUMTOK||(LA19_5 >= FLOATINGPOINTTOK && LA19_5 <= FLOATTOK)||(LA19_5 >= IDENTIFIER && LA19_5 <= IMAGINARYTOK)||LA19_5==INTEGERTOK||LA19_5==INTTOK||LA19_5==LONGTOK||LA19_5==POINTER||LA19_5==SHORTTOK||LA19_5==SIGNEDTOK||LA19_5==STRINGTOK||LA19_5==STRUCTTOK||LA19_5==TYPEDEFTOK||(LA19_5 >= UNSIGNEDTOK && LA19_5 <= VOIDTOK)) ) {
								alt19=2;
							}
							else if ( (LA19_5==ASSIGNMENT||LA19_5==RCURL||LA19_5==SEPARATOR) ) {
								alt19=3;
							}

							else {
								if (state.backtracking>0) {state.failed=true; return retval;}
								int nvaeMark = input.mark();
								try {
									for (int nvaeConsume = 0; nvaeConsume < 3 - 1; nvaeConsume++) {
										input.consume();
									}
									NoViableAltException nvae =
										new NoViableAltException("", 19, 5, input);
									throw nvae;
								} finally {
									input.rewind(nvaeMark);
								}
							}

							}
							break;
						case STRINGTOK:
							{
							int LA19_6 = input.LA(3);
							if ( (LA19_6==BOOLTOK||LA19_6==CHARTOK||(LA19_6 >= COMPLEXTOK && LA19_6 <= CONSTTOK)||LA19_6==DOUBLETOK||LA19_6==ENUMTOK||(LA19_6 >= FLOATINGPOINTTOK && LA19_6 <= FLOATTOK)||(LA19_6 >= IDENTIFIER && LA19_6 <= IMAGINARYTOK)||LA19_6==INTEGERTOK||(LA19_6 >= INTTOK && LA19_6 <= LCURL)||LA19_6==LONGTOK||LA19_6==POINTER||LA19_6==SHORTTOK||LA19_6==SIGNEDTOK||LA19_6==STRINGTOK||LA19_6==STRUCTTOK||LA19_6==TYPEDEFTOK||(LA19_6 >= UNSIGNEDTOK && LA19_6 <= VOIDTOK)) ) {
								alt19=2;
							}
							else if ( (LA19_6==ASSIGNMENT||LA19_6==RCURL||LA19_6==SEPARATOR) ) {
								alt19=3;
							}

							else {
								if (state.backtracking>0) {state.failed=true; return retval;}
								int nvaeMark = input.mark();
								try {
									for (int nvaeConsume = 0; nvaeConsume < 3 - 1; nvaeConsume++) {
										input.consume();
									}
									NoViableAltException nvae =
										new NoViableAltException("", 19, 6, input);
									throw nvae;
								} finally {
									input.rewind(nvaeMark);
								}
							}

							}
							break;
						case IDENTIFIER:
							{
							int LA19_7 = input.LA(3);
							if ( (LA19_7==BOOLTOK||LA19_7==CHARTOK||(LA19_7 >= COMPLEXTOK && LA19_7 <= CONSTTOK)||LA19_7==DOUBLETOK||LA19_7==ENUMTOK||(LA19_7 >= FLOATINGPOINTTOK && LA19_7 <= FLOATTOK)||(LA19_7 >= IDENTIFIER && LA19_7 <= IMAGINARYTOK)||LA19_7==INTEGERTOK||LA19_7==INTTOK||LA19_7==LONGTOK||LA19_7==POINTER||LA19_7==SHORTTOK||LA19_7==SIGNEDTOK||LA19_7==STRINGTOK||LA19_7==STRUCTTOK||LA19_7==TYPEDEFTOK||(LA19_7 >= UNSIGNEDTOK && LA19_7 <= VOIDTOK)) ) {
								alt19=2;
							}
							else if ( (LA19_7==ASSIGNMENT||LA19_7==RCURL||LA19_7==SEPARATOR) ) {
								alt19=3;
							}

							else {
								if (state.backtracking>0) {state.failed=true; return retval;}
								int nvaeMark = input.mark();
								try {
									for (int nvaeConsume = 0; nvaeConsume < 3 - 1; nvaeConsume++) {
										input.consume();
									}
									NoViableAltException nvae =
										new NoViableAltException("", 19, 7, input);
									throw nvae;
								} finally {
									input.rewind(nvaeMark);
								}
							}

							}
							break;
						case ALIGNTOK:
						case EVENTTOK:
						case STRING_LITERAL:
							{
							alt19=3;
							}
							break;
						default:
							if (state.backtracking>0) {state.failed=true; return retval;}
							int nvaeMark = input.mark();
							try {
								input.consume();
								NoViableAltException nvae =
									new NoViableAltException("", 19, 2, input);
								throw nvae;
							} finally {
								input.rewind(nvaeMark);
							}
						}
						}
						break;
					case EOF:
					case BOOLTOK:
					case CHARTOK:
					case COMPLEXTOK:
					case CONSTTOK:
					case DOUBLETOK:
					case ENUMTOK:
					case FLOATINGPOINTTOK:
					case FLOATTOK:
					case IDENTIFIER:
					case IMAGINARYTOK:
					case INTEGERTOK:
					case INTTOK:
					case LONGTOK:
					case LPAREN:
					case POINTER:
					case SHORTTOK:
					case SIGNEDTOK:
					case STRINGTOK:
					case STRUCTTOK:
					case TERM:
					case TYPEDEFTOK:
					case TYPE_ASSIGNMENT:
					case UNSIGNEDTOK:
					case VARIANTTOK:
					case VOIDTOK:
						{
						alt19=3;
						}
						break;
					default:
						if (state.backtracking>0) {state.failed=true; return retval;}
						NoViableAltException nvae =
							new NoViableAltException("", 19, 0, input);
						throw nvae;
					}
					switch (alt19) {
						case 1 :
							// org/eclipse/tracecompass/ctf/parser/CTFParser.g:328:11: alignAttribute
							{
							pushFollow(FOLLOW_alignAttribute_in_structSpecifier1311);
							alignAttribute67=alignAttribute();
							state._fsp--;
							if (state.failed) return retval;
							if ( state.backtracking==0 ) stream_alignAttribute.add(alignAttribute67.getTree());
							}
							break;
						case 2 :
							// org/eclipse/tracecompass/ctf/parser/CTFParser.g:330:11: ( structBody ( alignAttribute |) )
							{
							// org/eclipse/tracecompass/ctf/parser/CTFParser.g:330:11: ( structBody ( alignAttribute |) )
							// org/eclipse/tracecompass/ctf/parser/CTFParser.g:331:13: structBody ( alignAttribute |)
							{
							pushFollow(FOLLOW_structBody_in_structSpecifier1347);
							structBody68=structBody();
							state._fsp--;
							if (state.failed) return retval;
							if ( state.backtracking==0 ) stream_structBody.add(structBody68.getTree());
							// org/eclipse/tracecompass/ctf/parser/CTFParser.g:332:13: ( alignAttribute |)
							int alt18=2;
							int LA18_0 = input.LA(1);
							if ( (LA18_0==ALIGNTOK) ) {
								alt18=1;
							}
							else if ( (LA18_0==EOF||LA18_0==BOOLTOK||LA18_0==CHARTOK||(LA18_0 >= COMPLEXTOK && LA18_0 <= CONSTTOK)||LA18_0==DOUBLETOK||LA18_0==ENUMTOK||(LA18_0 >= FLOATINGPOINTTOK && LA18_0 <= FLOATTOK)||(LA18_0 >= IDENTIFIER && LA18_0 <= IMAGINARYTOK)||LA18_0==INTEGERTOK||(LA18_0 >= INTTOK && LA18_0 <= LCURL)||(LA18_0 >= LONGTOK && LA18_0 <= LPAREN)||LA18_0==POINTER||LA18_0==SHORTTOK||LA18_0==SIGNEDTOK||LA18_0==STRINGTOK||(LA18_0 >= STRUCTTOK && LA18_0 <= TERM)||(LA18_0 >= TYPEDEFTOK && LA18_0 <= TYPE_ASSIGNMENT)||(LA18_0 >= UNSIGNEDTOK && LA18_0 <= VOIDTOK)) ) {
								alt18=2;
							}

							else {
								if (state.backtracking>0) {state.failed=true; return retval;}
								NoViableAltException nvae =
									new NoViableAltException("", 18, 0, input);
								throw nvae;
							}

							switch (alt18) {
								case 1 :
									// org/eclipse/tracecompass/ctf/parser/CTFParser.g:333:14: alignAttribute
									{
									pushFollow(FOLLOW_alignAttribute_in_structSpecifier1378);
									alignAttribute69=alignAttribute();
									state._fsp--;
									if (state.failed) return retval;
									if ( state.backtracking==0 ) stream_alignAttribute.add(alignAttribute69.getTree());
									}
									break;
								case 2 :
									// org/eclipse/tracecompass/ctf/parser/CTFParser.g:336:13: 
									{
									}
									break;

							}

							}

							}
							break;
						case 3 :
							// org/eclipse/tracecompass/ctf/parser/CTFParser.g:340:9: 
							{
							}
							break;

					}

					}

					}
					break;
				case 2 :
					// org/eclipse/tracecompass/ctf/parser/CTFParser.g:344:5: ( structBody ( alignAttribute |) )
					{
					// org/eclipse/tracecompass/ctf/parser/CTFParser.g:344:5: ( structBody ( alignAttribute |) )
					// org/eclipse/tracecompass/ctf/parser/CTFParser.g:345:7: structBody ( alignAttribute |)
					{
					pushFollow(FOLLOW_structBody_in_structSpecifier1494);
					structBody70=structBody();
					state._fsp--;
					if (state.failed) return retval;
					if ( state.backtracking==0 ) stream_structBody.add(structBody70.getTree());
					// org/eclipse/tracecompass/ctf/parser/CTFParser.g:346:7: ( alignAttribute |)
					int alt20=2;
					int LA20_0 = input.LA(1);
					if ( (LA20_0==ALIGNTOK) ) {
						alt20=1;
					}
					else if ( (LA20_0==EOF||LA20_0==BOOLTOK||LA20_0==CHARTOK||(LA20_0 >= COMPLEXTOK && LA20_0 <= CONSTTOK)||LA20_0==DOUBLETOK||LA20_0==ENUMTOK||(LA20_0 >= FLOATINGPOINTTOK && LA20_0 <= FLOATTOK)||(LA20_0 >= IDENTIFIER && LA20_0 <= IMAGINARYTOK)||LA20_0==INTEGERTOK||(LA20_0 >= INTTOK && LA20_0 <= LCURL)||(LA20_0 >= LONGTOK && LA20_0 <= LPAREN)||LA20_0==POINTER||LA20_0==SHORTTOK||LA20_0==SIGNEDTOK||LA20_0==STRINGTOK||(LA20_0 >= STRUCTTOK && LA20_0 <= TERM)||(LA20_0 >= TYPEDEFTOK && LA20_0 <= TYPE_ASSIGNMENT)||(LA20_0 >= UNSIGNEDTOK && LA20_0 <= VOIDTOK)) ) {
						alt20=2;
					}

					else {
						if (state.backtracking>0) {state.failed=true; return retval;}
						NoViableAltException nvae =
							new NoViableAltException("", 20, 0, input);
						throw nvae;
					}

					switch (alt20) {
						case 1 :
							// org/eclipse/tracecompass/ctf/parser/CTFParser.g:347:9: alignAttribute
							{
							pushFollow(FOLLOW_alignAttribute_in_structSpecifier1512);
							alignAttribute71=alignAttribute();
							state._fsp--;
							if (state.failed) return retval;
							if ( state.backtracking==0 ) stream_alignAttribute.add(alignAttribute71.getTree());
							}
							break;
						case 2 :
							// org/eclipse/tracecompass/ctf/parser/CTFParser.g:350:7: 
							{
							}
							break;

					}

					}

					}
					break;

			}

			// AST REWRITE
			// elements: alignAttribute, structBody, structName
			// token labels: 
			// rule labels: retval
			// token list labels: 
			// rule list labels: 
			// wildcard labels: 
			if ( state.backtracking==0 ) {
			retval.tree = root_0;
			RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.getTree():null);

			root_0 = (CommonTree)adaptor.nil();
			// 352:5: -> ^( STRUCT ( structName )? ( structBody )? ( alignAttribute )? )
			{
				// org/eclipse/tracecompass/ctf/parser/CTFParser.g:352:8: ^( STRUCT ( structName )? ( structBody )? ( alignAttribute )? )
				{
				CommonTree root_1 = (CommonTree)adaptor.nil();
				root_1 = (CommonTree)adaptor.becomeRoot((CommonTree)adaptor.create(STRUCT, "STRUCT"), root_1);
				// org/eclipse/tracecompass/ctf/parser/CTFParser.g:352:17: ( structName )?
				if ( stream_structName.hasNext() ) {
					adaptor.addChild(root_1, stream_structName.nextTree());
				}
				stream_structName.reset();

				// org/eclipse/tracecompass/ctf/parser/CTFParser.g:352:29: ( structBody )?
				if ( stream_structBody.hasNext() ) {
					adaptor.addChild(root_1, stream_structBody.nextTree());
				}
				stream_structBody.reset();

				// org/eclipse/tracecompass/ctf/parser/CTFParser.g:352:41: ( alignAttribute )?
				if ( stream_alignAttribute.hasNext() ) {
					adaptor.addChild(root_1, stream_alignAttribute.nextTree());
				}
				stream_alignAttribute.reset();

				adaptor.addChild(root_0, root_1);
				}

			}


			retval.tree = root_0;
			}

			}

			retval.stop = input.LT(-1);

			if ( state.backtracking==0 ) {
			retval.tree = (CommonTree)adaptor.rulePostProcessing(root_0);
			adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
			}
		}

		    catch (RecognitionException e) {
		        throw e;
		    }

		finally {
			// do for sure before leaving
		}
		return retval;
	}
	// $ANTLR end "structSpecifier"


	public static class structName_return extends ParserRuleReturnScope {
		CommonTree tree;
		@Override
		public CommonTree getTree() { return tree; }
	};


	// $ANTLR start "structName"
	// org/eclipse/tracecompass/ctf/parser/CTFParser.g:355:1: structName : IDENTIFIER -> ^( STRUCT_NAME IDENTIFIER ) ;
	public final CTFParser.structName_return structName() throws RecognitionException {
		CTFParser.structName_return retval = new CTFParser.structName_return();
		retval.start = input.LT(1);

		CommonTree root_0 = null;

		Token IDENTIFIER72=null;

		CommonTree IDENTIFIER72_tree=null;
		RewriteRuleTokenStream stream_IDENTIFIER=new RewriteRuleTokenStream(adaptor,"token IDENTIFIER");

		try {
			// org/eclipse/tracecompass/ctf/parser/CTFParser.g:356:3: ( IDENTIFIER -> ^( STRUCT_NAME IDENTIFIER ) )
			// org/eclipse/tracecompass/ctf/parser/CTFParser.g:356:5: IDENTIFIER
			{
			IDENTIFIER72=(Token)match(input,IDENTIFIER,FOLLOW_IDENTIFIER_in_structName1578); if (state.failed) return retval; 
			if ( state.backtracking==0 ) stream_IDENTIFIER.add(IDENTIFIER72);

			// AST REWRITE
			// elements: IDENTIFIER
			// token labels: 
			// rule labels: retval
			// token list labels: 
			// rule list labels: 
			// wildcard labels: 
			if ( state.backtracking==0 ) {
			retval.tree = root_0;
			RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.getTree():null);

			root_0 = (CommonTree)adaptor.nil();
			// 356:16: -> ^( STRUCT_NAME IDENTIFIER )
			{
				// org/eclipse/tracecompass/ctf/parser/CTFParser.g:356:19: ^( STRUCT_NAME IDENTIFIER )
				{
				CommonTree root_1 = (CommonTree)adaptor.nil();
				root_1 = (CommonTree)adaptor.becomeRoot((CommonTree)adaptor.create(STRUCT_NAME, "STRUCT_NAME"), root_1);
				adaptor.addChild(root_1, stream_IDENTIFIER.nextNode());
				adaptor.addChild(root_0, root_1);
				}

			}


			retval.tree = root_0;
			}

			}

			retval.stop = input.LT(-1);

			if ( state.backtracking==0 ) {
			retval.tree = (CommonTree)adaptor.rulePostProcessing(root_0);
			adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
			}
		}

		    catch (RecognitionException e) {
		        throw e;
		    }

		finally {
			// do for sure before leaving
		}
		return retval;
	}
	// $ANTLR end "structName"


	public static class structOrVariantDeclarationList_return extends ParserRuleReturnScope {
		CommonTree tree;
		@Override
		public CommonTree getTree() { return tree; }
	};


	// $ANTLR start "structOrVariantDeclarationList"
	// org/eclipse/tracecompass/ctf/parser/CTFParser.g:359:1: structOrVariantDeclarationList : ( structOrVariantDeclaration )+ ;
	public final CTFParser.structOrVariantDeclarationList_return structOrVariantDeclarationList() throws RecognitionException {
		CTFParser.structOrVariantDeclarationList_return retval = new CTFParser.structOrVariantDeclarationList_return();
		retval.start = input.LT(1);

		CommonTree root_0 = null;

		ParserRuleReturnScope structOrVariantDeclaration73 =null;


		try {
			// org/eclipse/tracecompass/ctf/parser/CTFParser.g:360:3: ( ( structOrVariantDeclaration )+ )
			// org/eclipse/tracecompass/ctf/parser/CTFParser.g:360:5: ( structOrVariantDeclaration )+
			{
			root_0 = (CommonTree)adaptor.nil();


			// org/eclipse/tracecompass/ctf/parser/CTFParser.g:360:5: ( structOrVariantDeclaration )+
			int cnt22=0;
			loop22:
			while (true) {
				int alt22=2;
				int LA22_0 = input.LA(1);
				if ( (LA22_0==BOOLTOK||LA22_0==CHARTOK||(LA22_0 >= COMPLEXTOK && LA22_0 <= CONSTTOK)||LA22_0==DOUBLETOK||LA22_0==ENUMTOK||(LA22_0 >= FLOATINGPOINTTOK && LA22_0 <= FLOATTOK)||LA22_0==IMAGINARYTOK||LA22_0==INTEGERTOK||LA22_0==INTTOK||LA22_0==LONGTOK||LA22_0==SHORTTOK||LA22_0==SIGNEDTOK||LA22_0==STRINGTOK||LA22_0==STRUCTTOK||LA22_0==TYPEDEFTOK||(LA22_0 >= UNSIGNEDTOK && LA22_0 <= VOIDTOK)) ) {
					alt22=1;
				}
				else if ( (LA22_0==IDENTIFIER) && (( inTypealiasAlias() || isTypeName(input.LT(1).getText()) ))) {
					alt22=1;
				}
				else if ( (LA22_0==TYPEALIASTOK) ) {
					alt22=1;
				}

				switch (alt22) {
				case 1 :
					// org/eclipse/tracecompass/ctf/parser/CTFParser.g:360:5: structOrVariantDeclaration
					{
					pushFollow(FOLLOW_structOrVariantDeclaration_in_structOrVariantDeclarationList1599);
					structOrVariantDeclaration73=structOrVariantDeclaration();
					state._fsp--;
					if (state.failed) return retval;
					if ( state.backtracking==0 ) adaptor.addChild(root_0, structOrVariantDeclaration73.getTree());

					}
					break;

				default :
					if ( cnt22 >= 1 ) break loop22;
					if (state.backtracking>0) {state.failed=true; return retval;}
					EarlyExitException eee = new EarlyExitException(22, input);
					throw eee;
				}
				cnt22++;
			}

			}

			retval.stop = input.LT(-1);

			if ( state.backtracking==0 ) {
			retval.tree = (CommonTree)adaptor.rulePostProcessing(root_0);
			adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
			}
		}

		    catch (RecognitionException e) {
		        throw e;
		    }

		finally {
			// do for sure before leaving
		}
		return retval;
	}
	// $ANTLR end "structOrVariantDeclarationList"


	public static class structOrVariantDeclaration_return extends ParserRuleReturnScope {
		CommonTree tree;
		@Override
		public CommonTree getTree() { return tree; }
	};


	// $ANTLR start "structOrVariantDeclaration"
	// org/eclipse/tracecompass/ctf/parser/CTFParser.g:363:1: structOrVariantDeclaration : ( ( declarationSpecifiers ({...}? => declaratorList -> ^( TYPEDEF declaratorList declarationSpecifiers ) | structOrVariantDeclaratorList -> ^( SV_DECLARATION declarationSpecifiers structOrVariantDeclaratorList ) ) ) | typealiasDecl -> typealiasDecl ) TERM ;
	public final CTFParser.structOrVariantDeclaration_return structOrVariantDeclaration() throws RecognitionException {
		CTFParser.structOrVariantDeclaration_return retval = new CTFParser.structOrVariantDeclaration_return();
		retval.start = input.LT(1);

		CommonTree root_0 = null;

		Token TERM78=null;
		ParserRuleReturnScope declarationSpecifiers74 =null;
		ParserRuleReturnScope declaratorList75 =null;
		ParserRuleReturnScope structOrVariantDeclaratorList76 =null;
		ParserRuleReturnScope typealiasDecl77 =null;

		CommonTree TERM78_tree=null;
		RewriteRuleTokenStream stream_TERM=new RewriteRuleTokenStream(adaptor,"token TERM");
		RewriteRuleSubtreeStream stream_declaratorList=new RewriteRuleSubtreeStream(adaptor,"rule declaratorList");
		RewriteRuleSubtreeStream stream_typealiasDecl=new RewriteRuleSubtreeStream(adaptor,"rule typealiasDecl");
		RewriteRuleSubtreeStream stream_declarationSpecifiers=new RewriteRuleSubtreeStream(adaptor,"rule declarationSpecifiers");
		RewriteRuleSubtreeStream stream_structOrVariantDeclaratorList=new RewriteRuleSubtreeStream(adaptor,"rule structOrVariantDeclaratorList");

		try {
			// org/eclipse/tracecompass/ctf/parser/CTFParser.g:364:3: ( ( ( declarationSpecifiers ({...}? => declaratorList -> ^( TYPEDEF declaratorList declarationSpecifiers ) | structOrVariantDeclaratorList -> ^( SV_DECLARATION declarationSpecifiers structOrVariantDeclaratorList ) ) ) | typealiasDecl -> typealiasDecl ) TERM )
			// org/eclipse/tracecompass/ctf/parser/CTFParser.g:365:3: ( ( declarationSpecifiers ({...}? => declaratorList -> ^( TYPEDEF declaratorList declarationSpecifiers ) | structOrVariantDeclaratorList -> ^( SV_DECLARATION declarationSpecifiers structOrVariantDeclaratorList ) ) ) | typealiasDecl -> typealiasDecl ) TERM
			{
			// org/eclipse/tracecompass/ctf/parser/CTFParser.g:365:3: ( ( declarationSpecifiers ({...}? => declaratorList -> ^( TYPEDEF declaratorList declarationSpecifiers ) | structOrVariantDeclaratorList -> ^( SV_DECLARATION declarationSpecifiers structOrVariantDeclaratorList ) ) ) | typealiasDecl -> typealiasDecl )
			int alt24=2;
			int LA24_0 = input.LA(1);
			if ( (LA24_0==BOOLTOK||LA24_0==CHARTOK||(LA24_0 >= COMPLEXTOK && LA24_0 <= CONSTTOK)||LA24_0==DOUBLETOK||LA24_0==ENUMTOK||(LA24_0 >= FLOATINGPOINTTOK && LA24_0 <= FLOATTOK)||LA24_0==IMAGINARYTOK||LA24_0==INTEGERTOK||LA24_0==INTTOK||LA24_0==LONGTOK||LA24_0==SHORTTOK||LA24_0==SIGNEDTOK||LA24_0==STRINGTOK||LA24_0==STRUCTTOK||LA24_0==TYPEDEFTOK||(LA24_0 >= UNSIGNEDTOK && LA24_0 <= VOIDTOK)) ) {
				alt24=1;
			}
			else if ( (LA24_0==IDENTIFIER) && (( inTypealiasAlias() || isTypeName(input.LT(1).getText()) ))) {
				alt24=1;
			}
			else if ( (LA24_0==TYPEALIASTOK) ) {
				alt24=2;
			}

			else {
				if (state.backtracking>0) {state.failed=true; return retval;}
				NoViableAltException nvae =
					new NoViableAltException("", 24, 0, input);
				throw nvae;
			}

			switch (alt24) {
				case 1 :
					// org/eclipse/tracecompass/ctf/parser/CTFParser.g:366:7: ( declarationSpecifiers ({...}? => declaratorList -> ^( TYPEDEF declaratorList declarationSpecifiers ) | structOrVariantDeclaratorList -> ^( SV_DECLARATION declarationSpecifiers structOrVariantDeclaratorList ) ) )
					{
					// org/eclipse/tracecompass/ctf/parser/CTFParser.g:366:7: ( declarationSpecifiers ({...}? => declaratorList -> ^( TYPEDEF declaratorList declarationSpecifiers ) | structOrVariantDeclaratorList -> ^( SV_DECLARATION declarationSpecifiers structOrVariantDeclaratorList ) ) )
					// org/eclipse/tracecompass/ctf/parser/CTFParser.g:367:8: declarationSpecifiers ({...}? => declaratorList -> ^( TYPEDEF declaratorList declarationSpecifiers ) | structOrVariantDeclaratorList -> ^( SV_DECLARATION declarationSpecifiers structOrVariantDeclaratorList ) )
					{
					pushFollow(FOLLOW_declarationSpecifiers_in_structOrVariantDeclaration1632);
					declarationSpecifiers74=declarationSpecifiers();
					state._fsp--;
					if (state.failed) return retval;
					if ( state.backtracking==0 ) stream_declarationSpecifiers.add(declarationSpecifiers74.getTree());
					// org/eclipse/tracecompass/ctf/parser/CTFParser.g:368:10: ({...}? => declaratorList -> ^( TYPEDEF declaratorList declarationSpecifiers ) | structOrVariantDeclaratorList -> ^( SV_DECLARATION declarationSpecifiers structOrVariantDeclaratorList ) )
					int alt23=2;
					alt23 = dfa23.predict(input);
					switch (alt23) {
						case 1 :
							// org/eclipse/tracecompass/ctf/parser/CTFParser.g:370:12: {...}? => declaratorList
							{
							if ( !((inTypedef())) ) {
								if (state.backtracking>0) {state.failed=true; return retval;}
								throw new FailedPredicateException(input, "structOrVariantDeclaration", "inTypedef()");
							}
							pushFollow(FOLLOW_declaratorList_in_structOrVariantDeclaration1673);
							declaratorList75=declaratorList();
							state._fsp--;
							if (state.failed) return retval;
							if ( state.backtracking==0 ) stream_declaratorList.add(declaratorList75.getTree());
							if ( state.backtracking==0 ) {typedefOff();}
							// AST REWRITE
							// elements: declaratorList, declarationSpecifiers
							// token labels: 
							// rule labels: retval
							// token list labels: 
							// rule list labels: 
							// wildcard labels: 
							if ( state.backtracking==0 ) {
							retval.tree = root_0;
							RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.getTree():null);

							root_0 = (CommonTree)adaptor.nil();
							// 371:14: -> ^( TYPEDEF declaratorList declarationSpecifiers )
							{
								// org/eclipse/tracecompass/ctf/parser/CTFParser.g:371:17: ^( TYPEDEF declaratorList declarationSpecifiers )
								{
								CommonTree root_1 = (CommonTree)adaptor.nil();
								root_1 = (CommonTree)adaptor.becomeRoot((CommonTree)adaptor.create(TYPEDEF, "TYPEDEF"), root_1);
								adaptor.addChild(root_1, stream_declaratorList.nextTree());
								adaptor.addChild(root_1, stream_declarationSpecifiers.nextTree());
								adaptor.addChild(root_0, root_1);
								}

							}


							retval.tree = root_0;
							}

							}
							break;
						case 2 :
							// org/eclipse/tracecompass/ctf/parser/CTFParser.g:372:14: structOrVariantDeclaratorList
							{
							pushFollow(FOLLOW_structOrVariantDeclaratorList_in_structOrVariantDeclaration1713);
							structOrVariantDeclaratorList76=structOrVariantDeclaratorList();
							state._fsp--;
							if (state.failed) return retval;
							if ( state.backtracking==0 ) stream_structOrVariantDeclaratorList.add(structOrVariantDeclaratorList76.getTree());
							// AST REWRITE
							// elements: structOrVariantDeclaratorList, declarationSpecifiers
							// token labels: 
							// rule labels: retval
							// token list labels: 
							// rule list labels: 
							// wildcard labels: 
							if ( state.backtracking==0 ) {
							retval.tree = root_0;
							RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.getTree():null);

							root_0 = (CommonTree)adaptor.nil();
							// 373:14: -> ^( SV_DECLARATION declarationSpecifiers structOrVariantDeclaratorList )
							{
								// org/eclipse/tracecompass/ctf/parser/CTFParser.g:373:17: ^( SV_DECLARATION declarationSpecifiers structOrVariantDeclaratorList )
								{
								CommonTree root_1 = (CommonTree)adaptor.nil();
								root_1 = (CommonTree)adaptor.becomeRoot((CommonTree)adaptor.create(SV_DECLARATION, "SV_DECLARATION"), root_1);
								adaptor.addChild(root_1, stream_declarationSpecifiers.nextTree());
								adaptor.addChild(root_1, stream_structOrVariantDeclaratorList.nextTree());
								adaptor.addChild(root_0, root_1);
								}

							}


							retval.tree = root_0;
							}

							}
							break;

					}

					}

					}
					break;
				case 2 :
					// org/eclipse/tracecompass/ctf/parser/CTFParser.g:378:5: typealiasDecl
					{
					pushFollow(FOLLOW_typealiasDecl_in_structOrVariantDeclaration1772);
					typealiasDecl77=typealiasDecl();
					state._fsp--;
					if (state.failed) return retval;
					if ( state.backtracking==0 ) stream_typealiasDecl.add(typealiasDecl77.getTree());
					// AST REWRITE
					// elements: typealiasDecl
					// token labels: 
					// rule labels: retval
					// token list labels: 
					// rule list labels: 
					// wildcard labels: 
					if ( state.backtracking==0 ) {
					retval.tree = root_0;
					RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.getTree():null);

					root_0 = (CommonTree)adaptor.nil();
					// 378:19: -> typealiasDecl
					{
						adaptor.addChild(root_0, stream_typealiasDecl.nextTree());
					}


					retval.tree = root_0;
					}

					}
					break;

			}

			TERM78=(Token)match(input,TERM,FOLLOW_TERM_in_structOrVariantDeclaration1784); if (state.failed) return retval; 
			if ( state.backtracking==0 ) stream_TERM.add(TERM78);

			}

			retval.stop = input.LT(-1);

			if ( state.backtracking==0 ) {
			retval.tree = (CommonTree)adaptor.rulePostProcessing(root_0);
			adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
			}
		}

		    catch (RecognitionException e) {
		        throw e;
		    }

		finally {
			// do for sure before leaving
		}
		return retval;
	}
	// $ANTLR end "structOrVariantDeclaration"


	public static class specifierQualifierList_return extends ParserRuleReturnScope {
		CommonTree tree;
		@Override
		public CommonTree getTree() { return tree; }
	};


	// $ANTLR start "specifierQualifierList"
	// org/eclipse/tracecompass/ctf/parser/CTFParser.g:383:1: specifierQualifierList : ( typeQualifier | typeSpecifier )+ -> ^( TYPE_SPECIFIER_LIST ( typeQualifier )* ( typeSpecifier )* ) ;
	public final CTFParser.specifierQualifierList_return specifierQualifierList() throws RecognitionException {
		CTFParser.specifierQualifierList_return retval = new CTFParser.specifierQualifierList_return();
		retval.start = input.LT(1);

		CommonTree root_0 = null;

		ParserRuleReturnScope typeQualifier79 =null;
		ParserRuleReturnScope typeSpecifier80 =null;

		RewriteRuleSubtreeStream stream_typeSpecifier=new RewriteRuleSubtreeStream(adaptor,"rule typeSpecifier");
		RewriteRuleSubtreeStream stream_typeQualifier=new RewriteRuleSubtreeStream(adaptor,"rule typeQualifier");

		try {
			// org/eclipse/tracecompass/ctf/parser/CTFParser.g:384:3: ( ( typeQualifier | typeSpecifier )+ -> ^( TYPE_SPECIFIER_LIST ( typeQualifier )* ( typeSpecifier )* ) )
			// org/eclipse/tracecompass/ctf/parser/CTFParser.g:384:5: ( typeQualifier | typeSpecifier )+
			{
			// org/eclipse/tracecompass/ctf/parser/CTFParser.g:384:5: ( typeQualifier | typeSpecifier )+
			int cnt25=0;
			loop25:
			while (true) {
				int alt25=3;
				int LA25_0 = input.LA(1);
				if ( (LA25_0==CONSTTOK) ) {
					alt25=1;
				}
				else if ( (LA25_0==BOOLTOK||LA25_0==CHARTOK||LA25_0==COMPLEXTOK||LA25_0==DOUBLETOK||LA25_0==ENUMTOK||(LA25_0 >= FLOATINGPOINTTOK && LA25_0 <= FLOATTOK)||LA25_0==IMAGINARYTOK||LA25_0==INTEGERTOK||LA25_0==INTTOK||LA25_0==LONGTOK||LA25_0==SHORTTOK||LA25_0==SIGNEDTOK||LA25_0==STRINGTOK||LA25_0==STRUCTTOK||(LA25_0 >= UNSIGNEDTOK && LA25_0 <= VOIDTOK)) ) {
					alt25=2;
				}
				else if ( (LA25_0==IDENTIFIER) && (( inTypealiasAlias() || isTypeName(input.LT(1).getText()) ))) {
					alt25=2;
				}

				switch (alt25) {
				case 1 :
					// org/eclipse/tracecompass/ctf/parser/CTFParser.g:384:6: typeQualifier
					{
					pushFollow(FOLLOW_typeQualifier_in_specifierQualifierList1798);
					typeQualifier79=typeQualifier();
					state._fsp--;
					if (state.failed) return retval;
					if ( state.backtracking==0 ) stream_typeQualifier.add(typeQualifier79.getTree());
					}
					break;
				case 2 :
					// org/eclipse/tracecompass/ctf/parser/CTFParser.g:384:22: typeSpecifier
					{
					pushFollow(FOLLOW_typeSpecifier_in_specifierQualifierList1802);
					typeSpecifier80=typeSpecifier();
					state._fsp--;
					if (state.failed) return retval;
					if ( state.backtracking==0 ) stream_typeSpecifier.add(typeSpecifier80.getTree());
					}
					break;

				default :
					if ( cnt25 >= 1 ) break loop25;
					if (state.backtracking>0) {state.failed=true; return retval;}
					EarlyExitException eee = new EarlyExitException(25, input);
					throw eee;
				}
				cnt25++;
			}

			// AST REWRITE
			// elements: typeQualifier, typeSpecifier
			// token labels: 
			// rule labels: retval
			// token list labels: 
			// rule list labels: 
			// wildcard labels: 
			if ( state.backtracking==0 ) {
			retval.tree = root_0;
			RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.getTree():null);

			root_0 = (CommonTree)adaptor.nil();
			// 385:7: -> ^( TYPE_SPECIFIER_LIST ( typeQualifier )* ( typeSpecifier )* )
			{
				// org/eclipse/tracecompass/ctf/parser/CTFParser.g:385:10: ^( TYPE_SPECIFIER_LIST ( typeQualifier )* ( typeSpecifier )* )
				{
				CommonTree root_1 = (CommonTree)adaptor.nil();
				root_1 = (CommonTree)adaptor.becomeRoot((CommonTree)adaptor.create(TYPE_SPECIFIER_LIST, "TYPE_SPECIFIER_LIST"), root_1);
				// org/eclipse/tracecompass/ctf/parser/CTFParser.g:385:32: ( typeQualifier )*
				while ( stream_typeQualifier.hasNext() ) {
					adaptor.addChild(root_1, stream_typeQualifier.nextTree());
				}
				stream_typeQualifier.reset();

				// org/eclipse/tracecompass/ctf/parser/CTFParser.g:385:47: ( typeSpecifier )*
				while ( stream_typeSpecifier.hasNext() ) {
					adaptor.addChild(root_1, stream_typeSpecifier.nextTree());
				}
				stream_typeSpecifier.reset();

				adaptor.addChild(root_0, root_1);
				}

			}


			retval.tree = root_0;
			}

			}

			retval.stop = input.LT(-1);

			if ( state.backtracking==0 ) {
			retval.tree = (CommonTree)adaptor.rulePostProcessing(root_0);
			adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
			}
		}

		    catch (RecognitionException e) {
		        throw e;
		    }

		finally {
			// do for sure before leaving
		}
		return retval;
	}
	// $ANTLR end "specifierQualifierList"


	public static class structOrVariantDeclaratorList_return extends ParserRuleReturnScope {
		CommonTree tree;
		@Override
		public CommonTree getTree() { return tree; }
	};


	// $ANTLR start "structOrVariantDeclaratorList"
	// org/eclipse/tracecompass/ctf/parser/CTFParser.g:388:1: structOrVariantDeclaratorList : structOrVariantDeclarator ( SEPARATOR structOrVariantDeclarator )* -> ^( TYPE_DECLARATOR_LIST ( structOrVariantDeclarator )+ ) ;
	public final CTFParser.structOrVariantDeclaratorList_return structOrVariantDeclaratorList() throws RecognitionException {
		CTFParser.structOrVariantDeclaratorList_return retval = new CTFParser.structOrVariantDeclaratorList_return();
		retval.start = input.LT(1);

		CommonTree root_0 = null;

		Token SEPARATOR82=null;
		ParserRuleReturnScope structOrVariantDeclarator81 =null;
		ParserRuleReturnScope structOrVariantDeclarator83 =null;

		CommonTree SEPARATOR82_tree=null;
		RewriteRuleTokenStream stream_SEPARATOR=new RewriteRuleTokenStream(adaptor,"token SEPARATOR");
		RewriteRuleSubtreeStream stream_structOrVariantDeclarator=new RewriteRuleSubtreeStream(adaptor,"rule structOrVariantDeclarator");

		try {
			// org/eclipse/tracecompass/ctf/parser/CTFParser.g:389:3: ( structOrVariantDeclarator ( SEPARATOR structOrVariantDeclarator )* -> ^( TYPE_DECLARATOR_LIST ( structOrVariantDeclarator )+ ) )
			// org/eclipse/tracecompass/ctf/parser/CTFParser.g:389:5: structOrVariantDeclarator ( SEPARATOR structOrVariantDeclarator )*
			{
			pushFollow(FOLLOW_structOrVariantDeclarator_in_structOrVariantDeclaratorList1835);
			structOrVariantDeclarator81=structOrVariantDeclarator();
			state._fsp--;
			if (state.failed) return retval;
			if ( state.backtracking==0 ) stream_structOrVariantDeclarator.add(structOrVariantDeclarator81.getTree());
			// org/eclipse/tracecompass/ctf/parser/CTFParser.g:389:31: ( SEPARATOR structOrVariantDeclarator )*
			loop26:
			while (true) {
				int alt26=2;
				int LA26_0 = input.LA(1);
				if ( (LA26_0==SEPARATOR) ) {
					alt26=1;
				}

				switch (alt26) {
				case 1 :
					// org/eclipse/tracecompass/ctf/parser/CTFParser.g:389:32: SEPARATOR structOrVariantDeclarator
					{
					SEPARATOR82=(Token)match(input,SEPARATOR,FOLLOW_SEPARATOR_in_structOrVariantDeclaratorList1838); if (state.failed) return retval; 
					if ( state.backtracking==0 ) stream_SEPARATOR.add(SEPARATOR82);

					pushFollow(FOLLOW_structOrVariantDeclarator_in_structOrVariantDeclaratorList1840);
					structOrVariantDeclarator83=structOrVariantDeclarator();
					state._fsp--;
					if (state.failed) return retval;
					if ( state.backtracking==0 ) stream_structOrVariantDeclarator.add(structOrVariantDeclarator83.getTree());
					}
					break;

				default :
					break loop26;
				}
			}

			// AST REWRITE
			// elements: structOrVariantDeclarator
			// token labels: 
			// rule labels: retval
			// token list labels: 
			// rule list labels: 
			// wildcard labels: 
			if ( state.backtracking==0 ) {
			retval.tree = root_0;
			RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.getTree():null);

			root_0 = (CommonTree)adaptor.nil();
			// 390:7: -> ^( TYPE_DECLARATOR_LIST ( structOrVariantDeclarator )+ )
			{
				// org/eclipse/tracecompass/ctf/parser/CTFParser.g:390:10: ^( TYPE_DECLARATOR_LIST ( structOrVariantDeclarator )+ )
				{
				CommonTree root_1 = (CommonTree)adaptor.nil();
				root_1 = (CommonTree)adaptor.becomeRoot((CommonTree)adaptor.create(TYPE_DECLARATOR_LIST, "TYPE_DECLARATOR_LIST"), root_1);
				if ( !(stream_structOrVariantDeclarator.hasNext()) ) {
					throw new RewriteEarlyExitException();
				}
				while ( stream_structOrVariantDeclarator.hasNext() ) {
					adaptor.addChild(root_1, stream_structOrVariantDeclarator.nextTree());
				}
				stream_structOrVariantDeclarator.reset();

				adaptor.addChild(root_0, root_1);
				}

			}


			retval.tree = root_0;
			}

			}

			retval.stop = input.LT(-1);

			if ( state.backtracking==0 ) {
			retval.tree = (CommonTree)adaptor.rulePostProcessing(root_0);
			adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
			}
		}

		    catch (RecognitionException e) {
		        throw e;
		    }

		finally {
			// do for sure before leaving
		}
		return retval;
	}
	// $ANTLR end "structOrVariantDeclaratorList"


	public static class structOrVariantDeclarator_return extends ParserRuleReturnScope {
		CommonTree tree;
		@Override
		public CommonTree getTree() { return tree; }
	};


	// $ANTLR start "structOrVariantDeclarator"
	// org/eclipse/tracecompass/ctf/parser/CTFParser.g:393:1: structOrVariantDeclarator : ( declarator ( COLON numberLiteral )? ) -> declarator ;
	public final CTFParser.structOrVariantDeclarator_return structOrVariantDeclarator() throws RecognitionException {
		CTFParser.structOrVariantDeclarator_return retval = new CTFParser.structOrVariantDeclarator_return();
		retval.start = input.LT(1);

		CommonTree root_0 = null;

		Token COLON85=null;
		ParserRuleReturnScope declarator84 =null;
		ParserRuleReturnScope numberLiteral86 =null;

		CommonTree COLON85_tree=null;
		RewriteRuleTokenStream stream_COLON=new RewriteRuleTokenStream(adaptor,"token COLON");
		RewriteRuleSubtreeStream stream_declarator=new RewriteRuleSubtreeStream(adaptor,"rule declarator");
		RewriteRuleSubtreeStream stream_numberLiteral=new RewriteRuleSubtreeStream(adaptor,"rule numberLiteral");

		try {
			// org/eclipse/tracecompass/ctf/parser/CTFParser.g:394:3: ( ( declarator ( COLON numberLiteral )? ) -> declarator )
			// org/eclipse/tracecompass/ctf/parser/CTFParser.g:396:5: ( declarator ( COLON numberLiteral )? )
			{
			// org/eclipse/tracecompass/ctf/parser/CTFParser.g:396:5: ( declarator ( COLON numberLiteral )? )
			// org/eclipse/tracecompass/ctf/parser/CTFParser.g:396:6: declarator ( COLON numberLiteral )?
			{
			pushFollow(FOLLOW_declarator_in_structOrVariantDeclarator1879);
			declarator84=declarator();
			state._fsp--;
			if (state.failed) return retval;
			if ( state.backtracking==0 ) stream_declarator.add(declarator84.getTree());
			// org/eclipse/tracecompass/ctf/parser/CTFParser.g:396:17: ( COLON numberLiteral )?
			int alt27=2;
			int LA27_0 = input.LA(1);
			if ( (LA27_0==COLON) ) {
				alt27=1;
			}
			switch (alt27) {
				case 1 :
					// org/eclipse/tracecompass/ctf/parser/CTFParser.g:396:18: COLON numberLiteral
					{
					COLON85=(Token)match(input,COLON,FOLLOW_COLON_in_structOrVariantDeclarator1882); if (state.failed) return retval; 
					if ( state.backtracking==0 ) stream_COLON.add(COLON85);

					pushFollow(FOLLOW_numberLiteral_in_structOrVariantDeclarator1884);
					numberLiteral86=numberLiteral();
					state._fsp--;
					if (state.failed) return retval;
					if ( state.backtracking==0 ) stream_numberLiteral.add(numberLiteral86.getTree());
					}
					break;

			}

			}

			// AST REWRITE
			// elements: declarator
			// token labels: 
			// rule labels: retval
			// token list labels: 
			// rule list labels: 
			// wildcard labels: 
			if ( state.backtracking==0 ) {
			retval.tree = root_0;
			RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.getTree():null);

			root_0 = (CommonTree)adaptor.nil();
			// 396:41: -> declarator
			{
				adaptor.addChild(root_0, stream_declarator.nextTree());
			}


			retval.tree = root_0;
			}

			}

			retval.stop = input.LT(-1);

			if ( state.backtracking==0 ) {
			retval.tree = (CommonTree)adaptor.rulePostProcessing(root_0);
			adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
			}
		}

		    catch (RecognitionException e) {
		        throw e;
		    }

		finally {
			// do for sure before leaving
		}
		return retval;
	}
	// $ANTLR end "structOrVariantDeclarator"


	public static class variantSpecifier_return extends ParserRuleReturnScope {
		CommonTree tree;
		@Override
		public CommonTree getTree() { return tree; }
	};


	// $ANTLR start "variantSpecifier"
	// org/eclipse/tracecompass/ctf/parser/CTFParser.g:400:1: variantSpecifier : VARIANTTOK ( ( variantName ( ( variantTag ( variantBody |) ) | variantBody ) ) | ( variantTag variantBody ) | variantBody ) -> ^( VARIANT ( variantName )? ( variantTag )? ( variantBody )? ) ;
	public final CTFParser.variantSpecifier_return variantSpecifier() throws RecognitionException {
		CTFParser.variantSpecifier_return retval = new CTFParser.variantSpecifier_return();
		retval.start = input.LT(1);

		CommonTree root_0 = null;

		Token VARIANTTOK87=null;
		ParserRuleReturnScope variantName88 =null;
		ParserRuleReturnScope variantTag89 =null;
		ParserRuleReturnScope variantBody90 =null;
		ParserRuleReturnScope variantBody91 =null;
		ParserRuleReturnScope variantTag92 =null;
		ParserRuleReturnScope variantBody93 =null;
		ParserRuleReturnScope variantBody94 =null;

		CommonTree VARIANTTOK87_tree=null;
		RewriteRuleTokenStream stream_VARIANTTOK=new RewriteRuleTokenStream(adaptor,"token VARIANTTOK");
		RewriteRuleSubtreeStream stream_variantName=new RewriteRuleSubtreeStream(adaptor,"rule variantName");
		RewriteRuleSubtreeStream stream_variantTag=new RewriteRuleSubtreeStream(adaptor,"rule variantTag");
		RewriteRuleSubtreeStream stream_variantBody=new RewriteRuleSubtreeStream(adaptor,"rule variantBody");

		try {
			// org/eclipse/tracecompass/ctf/parser/CTFParser.g:401:3: ( VARIANTTOK ( ( variantName ( ( variantTag ( variantBody |) ) | variantBody ) ) | ( variantTag variantBody ) | variantBody ) -> ^( VARIANT ( variantName )? ( variantTag )? ( variantBody )? ) )
			// org/eclipse/tracecompass/ctf/parser/CTFParser.g:401:5: VARIANTTOK ( ( variantName ( ( variantTag ( variantBody |) ) | variantBody ) ) | ( variantTag variantBody ) | variantBody )
			{
			VARIANTTOK87=(Token)match(input,VARIANTTOK,FOLLOW_VARIANTTOK_in_variantSpecifier1908); if (state.failed) return retval; 
			if ( state.backtracking==0 ) stream_VARIANTTOK.add(VARIANTTOK87);

			// org/eclipse/tracecompass/ctf/parser/CTFParser.g:402:3: ( ( variantName ( ( variantTag ( variantBody |) ) | variantBody ) ) | ( variantTag variantBody ) | variantBody )
			int alt30=3;
			switch ( input.LA(1) ) {
			case IDENTIFIER:
				{
				alt30=1;
				}
				break;
			case LT:
				{
				alt30=2;
				}
				break;
			case LCURL:
				{
				alt30=3;
				}
				break;
			default:
				if (state.backtracking>0) {state.failed=true; return retval;}
				NoViableAltException nvae =
					new NoViableAltException("", 30, 0, input);
				throw nvae;
			}
			switch (alt30) {
				case 1 :
					// org/eclipse/tracecompass/ctf/parser/CTFParser.g:403:5: ( variantName ( ( variantTag ( variantBody |) ) | variantBody ) )
					{
					// org/eclipse/tracecompass/ctf/parser/CTFParser.g:403:5: ( variantName ( ( variantTag ( variantBody |) ) | variantBody ) )
					// org/eclipse/tracecompass/ctf/parser/CTFParser.g:404:7: variantName ( ( variantTag ( variantBody |) ) | variantBody )
					{
					pushFollow(FOLLOW_variantName_in_variantSpecifier1926);
					variantName88=variantName();
					state._fsp--;
					if (state.failed) return retval;
					if ( state.backtracking==0 ) stream_variantName.add(variantName88.getTree());
					// org/eclipse/tracecompass/ctf/parser/CTFParser.g:405:7: ( ( variantTag ( variantBody |) ) | variantBody )
					int alt29=2;
					int LA29_0 = input.LA(1);
					if ( (LA29_0==LT) ) {
						alt29=1;
					}
					else if ( (LA29_0==LCURL) ) {
						alt29=2;
					}

					else {
						if (state.backtracking>0) {state.failed=true; return retval;}
						NoViableAltException nvae =
							new NoViableAltException("", 29, 0, input);
						throw nvae;
					}

					switch (alt29) {
						case 1 :
							// org/eclipse/tracecompass/ctf/parser/CTFParser.g:406:9: ( variantTag ( variantBody |) )
							{
							// org/eclipse/tracecompass/ctf/parser/CTFParser.g:406:9: ( variantTag ( variantBody |) )
							// org/eclipse/tracecompass/ctf/parser/CTFParser.g:407:11: variantTag ( variantBody |)
							{
							pushFollow(FOLLOW_variantTag_in_variantSpecifier1956);
							variantTag89=variantTag();
							state._fsp--;
							if (state.failed) return retval;
							if ( state.backtracking==0 ) stream_variantTag.add(variantTag89.getTree());
							// org/eclipse/tracecompass/ctf/parser/CTFParser.g:408:11: ( variantBody |)
							int alt28=2;
							int LA28_0 = input.LA(1);
							if ( (LA28_0==LCURL) ) {
								switch ( input.LA(2) ) {
								case BOOLTOK:
								case CHARTOK:
								case COMPLEXTOK:
								case CONSTTOK:
								case DOUBLETOK:
								case ENUMTOK:
								case FLOATINGPOINTTOK:
								case FLOATTOK:
								case IMAGINARYTOK:
								case INTEGERTOK:
								case INTTOK:
								case LONGTOK:
								case SHORTTOK:
								case STRUCTTOK:
								case TYPEALIASTOK:
								case TYPEDEFTOK:
								case UNSIGNEDTOK:
								case VARIANTTOK:
								case VOIDTOK:
									{
									alt28=1;
									}
									break;
								case SIGNEDTOK:
									{
									int LA28_4 = input.LA(3);
									if ( (LA28_4==BOOLTOK||LA28_4==CHARTOK||(LA28_4 >= COMPLEXTOK && LA28_4 <= CONSTTOK)||LA28_4==DOUBLETOK||LA28_4==ENUMTOK||(LA28_4 >= FLOATINGPOINTTOK && LA28_4 <= FLOATTOK)||(LA28_4 >= IDENTIFIER && LA28_4 <= IMAGINARYTOK)||LA28_4==INTEGERTOK||LA28_4==INTTOK||LA28_4==LONGTOK||LA28_4==POINTER||LA28_4==SHORTTOK||LA28_4==SIGNEDTOK||LA28_4==STRINGTOK||LA28_4==STRUCTTOK||LA28_4==TYPEDEFTOK||(LA28_4 >= UNSIGNEDTOK && LA28_4 <= VOIDTOK)) ) {
										alt28=1;
									}
									else if ( (LA28_4==ASSIGNMENT||LA28_4==RCURL||LA28_4==SEPARATOR) ) {
										alt28=2;
									}

									else {
										if (state.backtracking>0) {state.failed=true; return retval;}
										int nvaeMark = input.mark();
										try {
											for (int nvaeConsume = 0; nvaeConsume < 3 - 1; nvaeConsume++) {
												input.consume();
											}
											NoViableAltException nvae =
												new NoViableAltException("", 28, 4, input);
											throw nvae;
										} finally {
											input.rewind(nvaeMark);
										}
									}

									}
									break;
								case STRINGTOK:
									{
									int LA28_5 = input.LA(3);
									if ( (LA28_5==BOOLTOK||LA28_5==CHARTOK||(LA28_5 >= COMPLEXTOK && LA28_5 <= CONSTTOK)||LA28_5==DOUBLETOK||LA28_5==ENUMTOK||(LA28_5 >= FLOATINGPOINTTOK && LA28_5 <= FLOATTOK)||(LA28_5 >= IDENTIFIER && LA28_5 <= IMAGINARYTOK)||LA28_5==INTEGERTOK||(LA28_5 >= INTTOK && LA28_5 <= LCURL)||LA28_5==LONGTOK||LA28_5==POINTER||LA28_5==SHORTTOK||LA28_5==SIGNEDTOK||LA28_5==STRINGTOK||LA28_5==STRUCTTOK||LA28_5==TYPEDEFTOK||(LA28_5 >= UNSIGNEDTOK && LA28_5 <= VOIDTOK)) ) {
										alt28=1;
									}
									else if ( (LA28_5==ASSIGNMENT||LA28_5==RCURL||LA28_5==SEPARATOR) ) {
										alt28=2;
									}

									else {
										if (state.backtracking>0) {state.failed=true; return retval;}
										int nvaeMark = input.mark();
										try {
											for (int nvaeConsume = 0; nvaeConsume < 3 - 1; nvaeConsume++) {
												input.consume();
											}
											NoViableAltException nvae =
												new NoViableAltException("", 28, 5, input);
											throw nvae;
										} finally {
											input.rewind(nvaeMark);
										}
									}

									}
									break;
								case IDENTIFIER:
									{
									int LA28_6 = input.LA(3);
									if ( (LA28_6==BOOLTOK||LA28_6==CHARTOK||(LA28_6 >= COMPLEXTOK && LA28_6 <= CONSTTOK)||LA28_6==DOUBLETOK||LA28_6==ENUMTOK||(LA28_6 >= FLOATINGPOINTTOK && LA28_6 <= FLOATTOK)||(LA28_6 >= IDENTIFIER && LA28_6 <= IMAGINARYTOK)||LA28_6==INTEGERTOK||LA28_6==INTTOK||LA28_6==LONGTOK||LA28_6==POINTER||LA28_6==SHORTTOK||LA28_6==SIGNEDTOK||LA28_6==STRINGTOK||LA28_6==STRUCTTOK||LA28_6==TYPEDEFTOK||(LA28_6 >= UNSIGNEDTOK && LA28_6 <= VOIDTOK)) ) {
										alt28=1;
									}
									else if ( (LA28_6==ASSIGNMENT||LA28_6==RCURL||LA28_6==SEPARATOR) ) {
										alt28=2;
									}

									else {
										if (state.backtracking>0) {state.failed=true; return retval;}
										int nvaeMark = input.mark();
										try {
											for (int nvaeConsume = 0; nvaeConsume < 3 - 1; nvaeConsume++) {
												input.consume();
											}
											NoViableAltException nvae =
												new NoViableAltException("", 28, 6, input);
											throw nvae;
										} finally {
											input.rewind(nvaeMark);
										}
									}

									}
									break;
								case ALIGNTOK:
								case EVENTTOK:
								case STRING_LITERAL:
									{
									alt28=2;
									}
									break;
								default:
									if (state.backtracking>0) {state.failed=true; return retval;}
									int nvaeMark = input.mark();
									try {
										input.consume();
										NoViableAltException nvae =
											new NoViableAltException("", 28, 1, input);
										throw nvae;
									} finally {
										input.rewind(nvaeMark);
									}
								}
							}
							else if ( (LA28_0==EOF||LA28_0==BOOLTOK||LA28_0==CHARTOK||(LA28_0 >= COMPLEXTOK && LA28_0 <= CONSTTOK)||LA28_0==DOUBLETOK||LA28_0==ENUMTOK||(LA28_0 >= FLOATINGPOINTTOK && LA28_0 <= FLOATTOK)||(LA28_0 >= IDENTIFIER && LA28_0 <= IMAGINARYTOK)||LA28_0==INTEGERTOK||LA28_0==INTTOK||(LA28_0 >= LONGTOK && LA28_0 <= LPAREN)||LA28_0==POINTER||LA28_0==SHORTTOK||LA28_0==SIGNEDTOK||LA28_0==STRINGTOK||(LA28_0 >= STRUCTTOK && LA28_0 <= TERM)||(LA28_0 >= TYPEDEFTOK && LA28_0 <= TYPE_ASSIGNMENT)||(LA28_0 >= UNSIGNEDTOK && LA28_0 <= VOIDTOK)) ) {
								alt28=2;
							}

							else {
								if (state.backtracking>0) {state.failed=true; return retval;}
								NoViableAltException nvae =
									new NoViableAltException("", 28, 0, input);
								throw nvae;
							}

							switch (alt28) {
								case 1 :
									// org/eclipse/tracecompass/ctf/parser/CTFParser.g:409:13: variantBody
									{
									pushFollow(FOLLOW_variantBody_in_variantSpecifier1982);
									variantBody90=variantBody();
									state._fsp--;
									if (state.failed) return retval;
									if ( state.backtracking==0 ) stream_variantBody.add(variantBody90.getTree());
									}
									break;
								case 2 :
									// org/eclipse/tracecompass/ctf/parser/CTFParser.g:412:11: 
									{
									}
									break;

							}

							}

							}
							break;
						case 2 :
							// org/eclipse/tracecompass/ctf/parser/CTFParser.g:415:9: variantBody
							{
							pushFollow(FOLLOW_variantBody_in_variantSpecifier2050);
							variantBody91=variantBody();
							state._fsp--;
							if (state.failed) return retval;
							if ( state.backtracking==0 ) stream_variantBody.add(variantBody91.getTree());
							}
							break;

					}

					}

					}
					break;
				case 2 :
					// org/eclipse/tracecompass/ctf/parser/CTFParser.g:418:5: ( variantTag variantBody )
					{
					// org/eclipse/tracecompass/ctf/parser/CTFParser.g:418:5: ( variantTag variantBody )
					// org/eclipse/tracecompass/ctf/parser/CTFParser.g:418:6: variantTag variantBody
					{
					pushFollow(FOLLOW_variantTag_in_variantSpecifier2071);
					variantTag92=variantTag();
					state._fsp--;
					if (state.failed) return retval;
					if ( state.backtracking==0 ) stream_variantTag.add(variantTag92.getTree());
					pushFollow(FOLLOW_variantBody_in_variantSpecifier2073);
					variantBody93=variantBody();
					state._fsp--;
					if (state.failed) return retval;
					if ( state.backtracking==0 ) stream_variantBody.add(variantBody93.getTree());
					}

					}
					break;
				case 3 :
					// org/eclipse/tracecompass/ctf/parser/CTFParser.g:419:5: variantBody
					{
					pushFollow(FOLLOW_variantBody_in_variantSpecifier2080);
					variantBody94=variantBody();
					state._fsp--;
					if (state.failed) return retval;
					if ( state.backtracking==0 ) stream_variantBody.add(variantBody94.getTree());
					}
					break;

			}

			// AST REWRITE
			// elements: variantBody, variantName, variantTag
			// token labels: 
			// rule labels: retval
			// token list labels: 
			// rule list labels: 
			// wildcard labels: 
			if ( state.backtracking==0 ) {
			retval.tree = root_0;
			RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.getTree():null);

			root_0 = (CommonTree)adaptor.nil();
			// 420:5: -> ^( VARIANT ( variantName )? ( variantTag )? ( variantBody )? )
			{
				// org/eclipse/tracecompass/ctf/parser/CTFParser.g:420:8: ^( VARIANT ( variantName )? ( variantTag )? ( variantBody )? )
				{
				CommonTree root_1 = (CommonTree)adaptor.nil();
				root_1 = (CommonTree)adaptor.becomeRoot((CommonTree)adaptor.create(VARIANT, "VARIANT"), root_1);
				// org/eclipse/tracecompass/ctf/parser/CTFParser.g:420:18: ( variantName )?
				if ( stream_variantName.hasNext() ) {
					adaptor.addChild(root_1, stream_variantName.nextTree());
				}
				stream_variantName.reset();

				// org/eclipse/tracecompass/ctf/parser/CTFParser.g:420:31: ( variantTag )?
				if ( stream_variantTag.hasNext() ) {
					adaptor.addChild(root_1, stream_variantTag.nextTree());
				}
				stream_variantTag.reset();

				// org/eclipse/tracecompass/ctf/parser/CTFParser.g:420:43: ( variantBody )?
				if ( stream_variantBody.hasNext() ) {
					adaptor.addChild(root_1, stream_variantBody.nextTree());
				}
				stream_variantBody.reset();

				adaptor.addChild(root_0, root_1);
				}

			}


			retval.tree = root_0;
			}

			}

			retval.stop = input.LT(-1);

			if ( state.backtracking==0 ) {
			retval.tree = (CommonTree)adaptor.rulePostProcessing(root_0);
			adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
			}
		}

		    catch (RecognitionException e) {
		        throw e;
		    }

		finally {
			// do for sure before leaving
		}
		return retval;
	}
	// $ANTLR end "variantSpecifier"


	public static class variantName_return extends ParserRuleReturnScope {
		CommonTree tree;
		@Override
		public CommonTree getTree() { return tree; }
	};


	// $ANTLR start "variantName"
	// org/eclipse/tracecompass/ctf/parser/CTFParser.g:423:1: variantName : IDENTIFIER -> ^( VARIANT_NAME IDENTIFIER ) ;
	public final CTFParser.variantName_return variantName() throws RecognitionException {
		CTFParser.variantName_return retval = new CTFParser.variantName_return();
		retval.start = input.LT(1);

		CommonTree root_0 = null;

		Token IDENTIFIER95=null;

		CommonTree IDENTIFIER95_tree=null;
		RewriteRuleTokenStream stream_IDENTIFIER=new RewriteRuleTokenStream(adaptor,"token IDENTIFIER");

		try {
			// org/eclipse/tracecompass/ctf/parser/CTFParser.g:424:3: ( IDENTIFIER -> ^( VARIANT_NAME IDENTIFIER ) )
			// org/eclipse/tracecompass/ctf/parser/CTFParser.g:424:5: IDENTIFIER
			{
			IDENTIFIER95=(Token)match(input,IDENTIFIER,FOLLOW_IDENTIFIER_in_variantName2112); if (state.failed) return retval; 
			if ( state.backtracking==0 ) stream_IDENTIFIER.add(IDENTIFIER95);

			// AST REWRITE
			// elements: IDENTIFIER
			// token labels: 
			// rule labels: retval
			// token list labels: 
			// rule list labels: 
			// wildcard labels: 
			if ( state.backtracking==0 ) {
			retval.tree = root_0;
			RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.getTree():null);

			root_0 = (CommonTree)adaptor.nil();
			// 424:16: -> ^( VARIANT_NAME IDENTIFIER )
			{
				// org/eclipse/tracecompass/ctf/parser/CTFParser.g:424:19: ^( VARIANT_NAME IDENTIFIER )
				{
				CommonTree root_1 = (CommonTree)adaptor.nil();
				root_1 = (CommonTree)adaptor.becomeRoot((CommonTree)adaptor.create(VARIANT_NAME, "VARIANT_NAME"), root_1);
				adaptor.addChild(root_1, stream_IDENTIFIER.nextNode());
				adaptor.addChild(root_0, root_1);
				}

			}


			retval.tree = root_0;
			}

			}

			retval.stop = input.LT(-1);

			if ( state.backtracking==0 ) {
			retval.tree = (CommonTree)adaptor.rulePostProcessing(root_0);
			adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
			}
		}

		    catch (RecognitionException e) {
		        throw e;
		    }

		finally {
			// do for sure before leaving
		}
		return retval;
	}
	// $ANTLR end "variantName"


	public static class variantBody_return extends ParserRuleReturnScope {
		CommonTree tree;
		@Override
		public CommonTree getTree() { return tree; }
	};


	// $ANTLR start "variantBody"
	// org/eclipse/tracecompass/ctf/parser/CTFParser.g:427:1: variantBody : LCURL structOrVariantDeclarationList RCURL -> ^( VARIANT_BODY structOrVariantDeclarationList ) ;
	public final CTFParser.variantBody_return variantBody() throws RecognitionException {
		Symbols_stack.push(new Symbols_scope());

		CTFParser.variantBody_return retval = new CTFParser.variantBody_return();
		retval.start = input.LT(1);

		CommonTree root_0 = null;

		Token LCURL96=null;
		Token RCURL98=null;
		ParserRuleReturnScope structOrVariantDeclarationList97 =null;

		CommonTree LCURL96_tree=null;
		CommonTree RCURL98_tree=null;
		RewriteRuleTokenStream stream_LCURL=new RewriteRuleTokenStream(adaptor,"token LCURL");
		RewriteRuleTokenStream stream_RCURL=new RewriteRuleTokenStream(adaptor,"token RCURL");
		RewriteRuleSubtreeStream stream_structOrVariantDeclarationList=new RewriteRuleSubtreeStream(adaptor,"rule structOrVariantDeclarationList");


		    Symbols_stack.peek().types = new HashSet<String>();

		try {
			// org/eclipse/tracecompass/ctf/parser/CTFParser.g:432:3: ( LCURL structOrVariantDeclarationList RCURL -> ^( VARIANT_BODY structOrVariantDeclarationList ) )
			// org/eclipse/tracecompass/ctf/parser/CTFParser.g:432:5: LCURL structOrVariantDeclarationList RCURL
			{
			LCURL96=(Token)match(input,LCURL,FOLLOW_LCURL_in_variantBody2143); if (state.failed) return retval; 
			if ( state.backtracking==0 ) stream_LCURL.add(LCURL96);

			pushFollow(FOLLOW_structOrVariantDeclarationList_in_variantBody2145);
			structOrVariantDeclarationList97=structOrVariantDeclarationList();
			state._fsp--;
			if (state.failed) return retval;
			if ( state.backtracking==0 ) stream_structOrVariantDeclarationList.add(structOrVariantDeclarationList97.getTree());
			RCURL98=(Token)match(input,RCURL,FOLLOW_RCURL_in_variantBody2147); if (state.failed) return retval; 
			if ( state.backtracking==0 ) stream_RCURL.add(RCURL98);

			// AST REWRITE
			// elements: structOrVariantDeclarationList
			// token labels: 
			// rule labels: retval
			// token list labels: 
			// rule list labels: 
			// wildcard labels: 
			if ( state.backtracking==0 ) {
			retval.tree = root_0;
			RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.getTree():null);

			root_0 = (CommonTree)adaptor.nil();
			// 433:7: -> ^( VARIANT_BODY structOrVariantDeclarationList )
			{
				// org/eclipse/tracecompass/ctf/parser/CTFParser.g:433:10: ^( VARIANT_BODY structOrVariantDeclarationList )
				{
				CommonTree root_1 = (CommonTree)adaptor.nil();
				root_1 = (CommonTree)adaptor.becomeRoot((CommonTree)adaptor.create(VARIANT_BODY, "VARIANT_BODY"), root_1);
				adaptor.addChild(root_1, stream_structOrVariantDeclarationList.nextTree());
				adaptor.addChild(root_0, root_1);
				}

			}


			retval.tree = root_0;
			}

			}

			retval.stop = input.LT(-1);

			if ( state.backtracking==0 ) {
			retval.tree = (CommonTree)adaptor.rulePostProcessing(root_0);
			adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
			}
		}

		    catch (RecognitionException e) {
		        throw e;
		    }

		finally {
			// do for sure before leaving
			Symbols_stack.pop();

		}
		return retval;
	}
	// $ANTLR end "variantBody"


	public static class variantTag_return extends ParserRuleReturnScope {
		CommonTree tree;
		@Override
		public CommonTree getTree() { return tree; }
	};


	// $ANTLR start "variantTag"
	// org/eclipse/tracecompass/ctf/parser/CTFParser.g:436:1: variantTag : LT IDENTIFIER GT -> ^( VARIANT_TAG IDENTIFIER ) ;
	public final CTFParser.variantTag_return variantTag() throws RecognitionException {
		CTFParser.variantTag_return retval = new CTFParser.variantTag_return();
		retval.start = input.LT(1);

		CommonTree root_0 = null;

		Token LT99=null;
		Token IDENTIFIER100=null;
		Token GT101=null;

		CommonTree LT99_tree=null;
		CommonTree IDENTIFIER100_tree=null;
		CommonTree GT101_tree=null;
		RewriteRuleTokenStream stream_GT=new RewriteRuleTokenStream(adaptor,"token GT");
		RewriteRuleTokenStream stream_LT=new RewriteRuleTokenStream(adaptor,"token LT");
		RewriteRuleTokenStream stream_IDENTIFIER=new RewriteRuleTokenStream(adaptor,"token IDENTIFIER");

		try {
			// org/eclipse/tracecompass/ctf/parser/CTFParser.g:437:3: ( LT IDENTIFIER GT -> ^( VARIANT_TAG IDENTIFIER ) )
			// org/eclipse/tracecompass/ctf/parser/CTFParser.g:437:5: LT IDENTIFIER GT
			{
			LT99=(Token)match(input,LT,FOLLOW_LT_in_variantTag2174); if (state.failed) return retval; 
			if ( state.backtracking==0 ) stream_LT.add(LT99);

			IDENTIFIER100=(Token)match(input,IDENTIFIER,FOLLOW_IDENTIFIER_in_variantTag2176); if (state.failed) return retval; 
			if ( state.backtracking==0 ) stream_IDENTIFIER.add(IDENTIFIER100);

			GT101=(Token)match(input,GT,FOLLOW_GT_in_variantTag2178); if (state.failed) return retval; 
			if ( state.backtracking==0 ) stream_GT.add(GT101);

			// AST REWRITE
			// elements: IDENTIFIER
			// token labels: 
			// rule labels: retval
			// token list labels: 
			// rule list labels: 
			// wildcard labels: 
			if ( state.backtracking==0 ) {
			retval.tree = root_0;
			RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.getTree():null);

			root_0 = (CommonTree)adaptor.nil();
			// 437:22: -> ^( VARIANT_TAG IDENTIFIER )
			{
				// org/eclipse/tracecompass/ctf/parser/CTFParser.g:437:25: ^( VARIANT_TAG IDENTIFIER )
				{
				CommonTree root_1 = (CommonTree)adaptor.nil();
				root_1 = (CommonTree)adaptor.becomeRoot((CommonTree)adaptor.create(VARIANT_TAG, "VARIANT_TAG"), root_1);
				adaptor.addChild(root_1, stream_IDENTIFIER.nextNode());
				adaptor.addChild(root_0, root_1);
				}

			}


			retval.tree = root_0;
			}

			}

			retval.stop = input.LT(-1);

			if ( state.backtracking==0 ) {
			retval.tree = (CommonTree)adaptor.rulePostProcessing(root_0);
			adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
			}
		}

		    catch (RecognitionException e) {
		        throw e;
		    }

		finally {
			// do for sure before leaving
		}
		return retval;
	}
	// $ANTLR end "variantTag"


	public static class enumSpecifier_return extends ParserRuleReturnScope {
		CommonTree tree;
		@Override
		public CommonTree getTree() { return tree; }
	};


	// $ANTLR start "enumSpecifier"
	// org/eclipse/tracecompass/ctf/parser/CTFParser.g:440:1: enumSpecifier : ENUMTOK ( ( enumName ( enumContainerType enumBody | enumBody |) ) | ( enumContainerType enumBody | enumBody ) ) -> ^( ENUM ( enumName )? ( enumContainerType )? ( enumBody )? ) ;
	public final CTFParser.enumSpecifier_return enumSpecifier() throws RecognitionException {
		CTFParser.enumSpecifier_return retval = new CTFParser.enumSpecifier_return();
		retval.start = input.LT(1);

		CommonTree root_0 = null;

		Token ENUMTOK102=null;
		ParserRuleReturnScope enumName103 =null;
		ParserRuleReturnScope enumContainerType104 =null;
		ParserRuleReturnScope enumBody105 =null;
		ParserRuleReturnScope enumBody106 =null;
		ParserRuleReturnScope enumContainerType107 =null;
		ParserRuleReturnScope enumBody108 =null;
		ParserRuleReturnScope enumBody109 =null;

		CommonTree ENUMTOK102_tree=null;
		RewriteRuleTokenStream stream_ENUMTOK=new RewriteRuleTokenStream(adaptor,"token ENUMTOK");
		RewriteRuleSubtreeStream stream_enumName=new RewriteRuleSubtreeStream(adaptor,"rule enumName");
		RewriteRuleSubtreeStream stream_enumContainerType=new RewriteRuleSubtreeStream(adaptor,"rule enumContainerType");
		RewriteRuleSubtreeStream stream_enumBody=new RewriteRuleSubtreeStream(adaptor,"rule enumBody");

		try {
			// org/eclipse/tracecompass/ctf/parser/CTFParser.g:441:3: ( ENUMTOK ( ( enumName ( enumContainerType enumBody | enumBody |) ) | ( enumContainerType enumBody | enumBody ) ) -> ^( ENUM ( enumName )? ( enumContainerType )? ( enumBody )? ) )
			// org/eclipse/tracecompass/ctf/parser/CTFParser.g:441:5: ENUMTOK ( ( enumName ( enumContainerType enumBody | enumBody |) ) | ( enumContainerType enumBody | enumBody ) )
			{
			ENUMTOK102=(Token)match(input,ENUMTOK,FOLLOW_ENUMTOK_in_enumSpecifier2199); if (state.failed) return retval; 
			if ( state.backtracking==0 ) stream_ENUMTOK.add(ENUMTOK102);

			// org/eclipse/tracecompass/ctf/parser/CTFParser.g:442:5: ( ( enumName ( enumContainerType enumBody | enumBody |) ) | ( enumContainerType enumBody | enumBody ) )
			int alt33=2;
			int LA33_0 = input.LA(1);
			if ( (LA33_0==IDENTIFIER) ) {
				alt33=1;
			}
			else if ( (LA33_0==COLON||LA33_0==LCURL) ) {
				alt33=2;
			}

			else {
				if (state.backtracking>0) {state.failed=true; return retval;}
				NoViableAltException nvae =
					new NoViableAltException("", 33, 0, input);
				throw nvae;
			}

			switch (alt33) {
				case 1 :
					// org/eclipse/tracecompass/ctf/parser/CTFParser.g:444:9: ( enumName ( enumContainerType enumBody | enumBody |) )
					{
					// org/eclipse/tracecompass/ctf/parser/CTFParser.g:444:9: ( enumName ( enumContainerType enumBody | enumBody |) )
					// org/eclipse/tracecompass/ctf/parser/CTFParser.g:445:13: enumName ( enumContainerType enumBody | enumBody |)
					{
					pushFollow(FOLLOW_enumName_in_enumSpecifier2238);
					enumName103=enumName();
					state._fsp--;
					if (state.failed) return retval;
					if ( state.backtracking==0 ) stream_enumName.add(enumName103.getTree());
					// org/eclipse/tracecompass/ctf/parser/CTFParser.g:446:13: ( enumContainerType enumBody | enumBody |)
					int alt31=3;
					switch ( input.LA(1) ) {
					case COLON:
						{
						alt31=1;
						}
						break;
					case LCURL:
						{
						alt31=2;
						}
						break;
					case EOF:
					case BOOLTOK:
					case CHARTOK:
					case COMPLEXTOK:
					case CONSTTOK:
					case DOUBLETOK:
					case ENUMTOK:
					case FLOATINGPOINTTOK:
					case FLOATTOK:
					case IDENTIFIER:
					case IMAGINARYTOK:
					case INTEGERTOK:
					case INTTOK:
					case LONGTOK:
					case LPAREN:
					case POINTER:
					case SHORTTOK:
					case SIGNEDTOK:
					case STRINGTOK:
					case STRUCTTOK:
					case TERM:
					case TYPEDEFTOK:
					case TYPE_ASSIGNMENT:
					case UNSIGNEDTOK:
					case VARIANTTOK:
					case VOIDTOK:
						{
						alt31=3;
						}
						break;
					default:
						if (state.backtracking>0) {state.failed=true; return retval;}
						NoViableAltException nvae =
							new NoViableAltException("", 31, 0, input);
						throw nvae;
					}
					switch (alt31) {
						case 1 :
							// org/eclipse/tracecompass/ctf/parser/CTFParser.g:447:17: enumContainerType enumBody
							{
							pushFollow(FOLLOW_enumContainerType_in_enumSpecifier2270);
							enumContainerType104=enumContainerType();
							state._fsp--;
							if (state.failed) return retval;
							if ( state.backtracking==0 ) stream_enumContainerType.add(enumContainerType104.getTree());
							pushFollow(FOLLOW_enumBody_in_enumSpecifier2272);
							enumBody105=enumBody();
							state._fsp--;
							if (state.failed) return retval;
							if ( state.backtracking==0 ) stream_enumBody.add(enumBody105.getTree());
							}
							break;
						case 2 :
							// org/eclipse/tracecompass/ctf/parser/CTFParser.g:449:17: enumBody
							{
							pushFollow(FOLLOW_enumBody_in_enumSpecifier2302);
							enumBody106=enumBody();
							state._fsp--;
							if (state.failed) return retval;
							if ( state.backtracking==0 ) stream_enumBody.add(enumBody106.getTree());
							}
							break;
						case 3 :
							// org/eclipse/tracecompass/ctf/parser/CTFParser.g:452:13: 
							{
							}
							break;

					}

					}

					}
					break;
				case 2 :
					// org/eclipse/tracecompass/ctf/parser/CTFParser.g:456:9: ( enumContainerType enumBody | enumBody )
					{
					// org/eclipse/tracecompass/ctf/parser/CTFParser.g:456:9: ( enumContainerType enumBody | enumBody )
					int alt32=2;
					int LA32_0 = input.LA(1);
					if ( (LA32_0==COLON) ) {
						alt32=1;
					}
					else if ( (LA32_0==LCURL) ) {
						alt32=2;
					}

					else {
						if (state.backtracking>0) {state.failed=true; return retval;}
						NoViableAltException nvae =
							new NoViableAltException("", 32, 0, input);
						throw nvae;
					}

					switch (alt32) {
						case 1 :
							// org/eclipse/tracecompass/ctf/parser/CTFParser.g:457:13: enumContainerType enumBody
							{
							pushFollow(FOLLOW_enumContainerType_in_enumSpecifier2394);
							enumContainerType107=enumContainerType();
							state._fsp--;
							if (state.failed) return retval;
							if ( state.backtracking==0 ) stream_enumContainerType.add(enumContainerType107.getTree());
							pushFollow(FOLLOW_enumBody_in_enumSpecifier2396);
							enumBody108=enumBody();
							state._fsp--;
							if (state.failed) return retval;
							if ( state.backtracking==0 ) stream_enumBody.add(enumBody108.getTree());
							}
							break;
						case 2 :
							// org/eclipse/tracecompass/ctf/parser/CTFParser.g:459:13: enumBody
							{
							pushFollow(FOLLOW_enumBody_in_enumSpecifier2420);
							enumBody109=enumBody();
							state._fsp--;
							if (state.failed) return retval;
							if ( state.backtracking==0 ) stream_enumBody.add(enumBody109.getTree());
							}
							break;

					}

					}
					break;

			}

			// AST REWRITE
			// elements: enumContainerType, enumName, enumBody
			// token labels: 
			// rule labels: retval
			// token list labels: 
			// rule list labels: 
			// wildcard labels: 
			if ( state.backtracking==0 ) {
			retval.tree = root_0;
			RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.getTree():null);

			root_0 = (CommonTree)adaptor.nil();
			// 461:7: -> ^( ENUM ( enumName )? ( enumContainerType )? ( enumBody )? )
			{
				// org/eclipse/tracecompass/ctf/parser/CTFParser.g:461:10: ^( ENUM ( enumName )? ( enumContainerType )? ( enumBody )? )
				{
				CommonTree root_1 = (CommonTree)adaptor.nil();
				root_1 = (CommonTree)adaptor.becomeRoot((CommonTree)adaptor.create(ENUM, "ENUM"), root_1);
				// org/eclipse/tracecompass/ctf/parser/CTFParser.g:461:17: ( enumName )?
				if ( stream_enumName.hasNext() ) {
					adaptor.addChild(root_1, stream_enumName.nextTree());
				}
				stream_enumName.reset();

				// org/eclipse/tracecompass/ctf/parser/CTFParser.g:461:27: ( enumContainerType )?
				if ( stream_enumContainerType.hasNext() ) {
					adaptor.addChild(root_1, stream_enumContainerType.nextTree());
				}
				stream_enumContainerType.reset();

				// org/eclipse/tracecompass/ctf/parser/CTFParser.g:461:46: ( enumBody )?
				if ( stream_enumBody.hasNext() ) {
					adaptor.addChild(root_1, stream_enumBody.nextTree());
				}
				stream_enumBody.reset();

				adaptor.addChild(root_0, root_1);
				}

			}


			retval.tree = root_0;
			}

			}

			retval.stop = input.LT(-1);

			if ( state.backtracking==0 ) {
			retval.tree = (CommonTree)adaptor.rulePostProcessing(root_0);
			adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
			}
		}

		    catch (RecognitionException e) {
		        throw e;
		    }

		finally {
			// do for sure before leaving
		}
		return retval;
	}
	// $ANTLR end "enumSpecifier"


	public static class enumName_return extends ParserRuleReturnScope {
		CommonTree tree;
		@Override
		public CommonTree getTree() { return tree; }
	};


	// $ANTLR start "enumName"
	// org/eclipse/tracecompass/ctf/parser/CTFParser.g:464:1: enumName : IDENTIFIER -> ^( ENUM_NAME IDENTIFIER ) ;
	public final CTFParser.enumName_return enumName() throws RecognitionException {
		CTFParser.enumName_return retval = new CTFParser.enumName_return();
		retval.start = input.LT(1);

		CommonTree root_0 = null;

		Token IDENTIFIER110=null;

		CommonTree IDENTIFIER110_tree=null;
		RewriteRuleTokenStream stream_IDENTIFIER=new RewriteRuleTokenStream(adaptor,"token IDENTIFIER");

		try {
			// org/eclipse/tracecompass/ctf/parser/CTFParser.g:465:3: ( IDENTIFIER -> ^( ENUM_NAME IDENTIFIER ) )
			// org/eclipse/tracecompass/ctf/parser/CTFParser.g:465:5: IDENTIFIER
			{
			IDENTIFIER110=(Token)match(input,IDENTIFIER,FOLLOW_IDENTIFIER_in_enumName2464); if (state.failed) return retval; 
			if ( state.backtracking==0 ) stream_IDENTIFIER.add(IDENTIFIER110);

			// AST REWRITE
			// elements: IDENTIFIER
			// token labels: 
			// rule labels: retval
			// token list labels: 
			// rule list labels: 
			// wildcard labels: 
			if ( state.backtracking==0 ) {
			retval.tree = root_0;
			RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.getTree():null);

			root_0 = (CommonTree)adaptor.nil();
			// 465:16: -> ^( ENUM_NAME IDENTIFIER )
			{
				// org/eclipse/tracecompass/ctf/parser/CTFParser.g:465:19: ^( ENUM_NAME IDENTIFIER )
				{
				CommonTree root_1 = (CommonTree)adaptor.nil();
				root_1 = (CommonTree)adaptor.becomeRoot((CommonTree)adaptor.create(ENUM_NAME, "ENUM_NAME"), root_1);
				adaptor.addChild(root_1, stream_IDENTIFIER.nextNode());
				adaptor.addChild(root_0, root_1);
				}

			}


			retval.tree = root_0;
			}

			}

			retval.stop = input.LT(-1);

			if ( state.backtracking==0 ) {
			retval.tree = (CommonTree)adaptor.rulePostProcessing(root_0);
			adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
			}
		}

		    catch (RecognitionException e) {
		        throw e;
		    }

		finally {
			// do for sure before leaving
		}
		return retval;
	}
	// $ANTLR end "enumName"


	public static class enumBody_return extends ParserRuleReturnScope {
		CommonTree tree;
		@Override
		public CommonTree getTree() { return tree; }
	};


	// $ANTLR start "enumBody"
	// org/eclipse/tracecompass/ctf/parser/CTFParser.g:468:1: enumBody : LCURL enumeratorList ( SEPARATOR )? RCURL -> ^( ENUM_BODY enumeratorList ) ;
	public final CTFParser.enumBody_return enumBody() throws RecognitionException {
		CTFParser.enumBody_return retval = new CTFParser.enumBody_return();
		retval.start = input.LT(1);

		CommonTree root_0 = null;

		Token LCURL111=null;
		Token SEPARATOR113=null;
		Token RCURL114=null;
		ParserRuleReturnScope enumeratorList112 =null;

		CommonTree LCURL111_tree=null;
		CommonTree SEPARATOR113_tree=null;
		CommonTree RCURL114_tree=null;
		RewriteRuleTokenStream stream_LCURL=new RewriteRuleTokenStream(adaptor,"token LCURL");
		RewriteRuleTokenStream stream_SEPARATOR=new RewriteRuleTokenStream(adaptor,"token SEPARATOR");
		RewriteRuleTokenStream stream_RCURL=new RewriteRuleTokenStream(adaptor,"token RCURL");
		RewriteRuleSubtreeStream stream_enumeratorList=new RewriteRuleSubtreeStream(adaptor,"rule enumeratorList");

		try {
			// org/eclipse/tracecompass/ctf/parser/CTFParser.g:469:3: ( LCURL enumeratorList ( SEPARATOR )? RCURL -> ^( ENUM_BODY enumeratorList ) )
			// org/eclipse/tracecompass/ctf/parser/CTFParser.g:469:5: LCURL enumeratorList ( SEPARATOR )? RCURL
			{
			LCURL111=(Token)match(input,LCURL,FOLLOW_LCURL_in_enumBody2485); if (state.failed) return retval; 
			if ( state.backtracking==0 ) stream_LCURL.add(LCURL111);

			pushFollow(FOLLOW_enumeratorList_in_enumBody2487);
			enumeratorList112=enumeratorList();
			state._fsp--;
			if (state.failed) return retval;
			if ( state.backtracking==0 ) stream_enumeratorList.add(enumeratorList112.getTree());
			// org/eclipse/tracecompass/ctf/parser/CTFParser.g:469:26: ( SEPARATOR )?
			int alt34=2;
			int LA34_0 = input.LA(1);
			if ( (LA34_0==SEPARATOR) ) {
				alt34=1;
			}
			switch (alt34) {
				case 1 :
					// org/eclipse/tracecompass/ctf/parser/CTFParser.g:469:26: SEPARATOR
					{
					SEPARATOR113=(Token)match(input,SEPARATOR,FOLLOW_SEPARATOR_in_enumBody2489); if (state.failed) return retval; 
					if ( state.backtracking==0 ) stream_SEPARATOR.add(SEPARATOR113);

					}
					break;

			}

			RCURL114=(Token)match(input,RCURL,FOLLOW_RCURL_in_enumBody2492); if (state.failed) return retval; 
			if ( state.backtracking==0 ) stream_RCURL.add(RCURL114);

			// AST REWRITE
			// elements: enumeratorList
			// token labels: 
			// rule labels: retval
			// token list labels: 
			// rule list labels: 
			// wildcard labels: 
			if ( state.backtracking==0 ) {
			retval.tree = root_0;
			RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.getTree():null);

			root_0 = (CommonTree)adaptor.nil();
			// 469:43: -> ^( ENUM_BODY enumeratorList )
			{
				// org/eclipse/tracecompass/ctf/parser/CTFParser.g:469:46: ^( ENUM_BODY enumeratorList )
				{
				CommonTree root_1 = (CommonTree)adaptor.nil();
				root_1 = (CommonTree)adaptor.becomeRoot((CommonTree)adaptor.create(ENUM_BODY, "ENUM_BODY"), root_1);
				adaptor.addChild(root_1, stream_enumeratorList.nextTree());
				adaptor.addChild(root_0, root_1);
				}

			}


			retval.tree = root_0;
			}

			}

			retval.stop = input.LT(-1);

			if ( state.backtracking==0 ) {
			retval.tree = (CommonTree)adaptor.rulePostProcessing(root_0);
			adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
			}
		}

		    catch (RecognitionException e) {
		        throw e;
		    }

		finally {
			// do for sure before leaving
		}
		return retval;
	}
	// $ANTLR end "enumBody"


	public static class enumContainerType_return extends ParserRuleReturnScope {
		CommonTree tree;
		@Override
		public CommonTree getTree() { return tree; }
	};


	// $ANTLR start "enumContainerType"
	// org/eclipse/tracecompass/ctf/parser/CTFParser.g:472:1: enumContainerType : COLON declarationSpecifiers -> ^( ENUM_CONTAINER_TYPE declarationSpecifiers ) ;
	public final CTFParser.enumContainerType_return enumContainerType() throws RecognitionException {
		CTFParser.enumContainerType_return retval = new CTFParser.enumContainerType_return();
		retval.start = input.LT(1);

		CommonTree root_0 = null;

		Token COLON115=null;
		ParserRuleReturnScope declarationSpecifiers116 =null;

		CommonTree COLON115_tree=null;
		RewriteRuleTokenStream stream_COLON=new RewriteRuleTokenStream(adaptor,"token COLON");
		RewriteRuleSubtreeStream stream_declarationSpecifiers=new RewriteRuleSubtreeStream(adaptor,"rule declarationSpecifiers");

		try {
			// org/eclipse/tracecompass/ctf/parser/CTFParser.g:473:3: ( COLON declarationSpecifiers -> ^( ENUM_CONTAINER_TYPE declarationSpecifiers ) )
			// org/eclipse/tracecompass/ctf/parser/CTFParser.g:473:5: COLON declarationSpecifiers
			{
			COLON115=(Token)match(input,COLON,FOLLOW_COLON_in_enumContainerType2513); if (state.failed) return retval; 
			if ( state.backtracking==0 ) stream_COLON.add(COLON115);

			pushFollow(FOLLOW_declarationSpecifiers_in_enumContainerType2515);
			declarationSpecifiers116=declarationSpecifiers();
			state._fsp--;
			if (state.failed) return retval;
			if ( state.backtracking==0 ) stream_declarationSpecifiers.add(declarationSpecifiers116.getTree());
			// AST REWRITE
			// elements: declarationSpecifiers
			// token labels: 
			// rule labels: retval
			// token list labels: 
			// rule list labels: 
			// wildcard labels: 
			if ( state.backtracking==0 ) {
			retval.tree = root_0;
			RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.getTree():null);

			root_0 = (CommonTree)adaptor.nil();
			// 473:33: -> ^( ENUM_CONTAINER_TYPE declarationSpecifiers )
			{
				// org/eclipse/tracecompass/ctf/parser/CTFParser.g:473:36: ^( ENUM_CONTAINER_TYPE declarationSpecifiers )
				{
				CommonTree root_1 = (CommonTree)adaptor.nil();
				root_1 = (CommonTree)adaptor.becomeRoot((CommonTree)adaptor.create(ENUM_CONTAINER_TYPE, "ENUM_CONTAINER_TYPE"), root_1);
				adaptor.addChild(root_1, stream_declarationSpecifiers.nextTree());
				adaptor.addChild(root_0, root_1);
				}

			}


			retval.tree = root_0;
			}

			}

			retval.stop = input.LT(-1);

			if ( state.backtracking==0 ) {
			retval.tree = (CommonTree)adaptor.rulePostProcessing(root_0);
			adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
			}
		}

		    catch (RecognitionException e) {
		        throw e;
		    }

		finally {
			// do for sure before leaving
		}
		return retval;
	}
	// $ANTLR end "enumContainerType"


	public static class enumeratorList_return extends ParserRuleReturnScope {
		CommonTree tree;
		@Override
		public CommonTree getTree() { return tree; }
	};


	// $ANTLR start "enumeratorList"
	// org/eclipse/tracecompass/ctf/parser/CTFParser.g:476:1: enumeratorList : enumerator ( SEPARATOR enumerator )* -> ( ^( ENUM_ENUMERATOR enumerator ) )+ ;
	public final CTFParser.enumeratorList_return enumeratorList() throws RecognitionException {
		CTFParser.enumeratorList_return retval = new CTFParser.enumeratorList_return();
		retval.start = input.LT(1);

		CommonTree root_0 = null;

		Token SEPARATOR118=null;
		ParserRuleReturnScope enumerator117 =null;
		ParserRuleReturnScope enumerator119 =null;

		CommonTree SEPARATOR118_tree=null;
		RewriteRuleTokenStream stream_SEPARATOR=new RewriteRuleTokenStream(adaptor,"token SEPARATOR");
		RewriteRuleSubtreeStream stream_enumerator=new RewriteRuleSubtreeStream(adaptor,"rule enumerator");

		try {
			// org/eclipse/tracecompass/ctf/parser/CTFParser.g:477:3: ( enumerator ( SEPARATOR enumerator )* -> ( ^( ENUM_ENUMERATOR enumerator ) )+ )
			// org/eclipse/tracecompass/ctf/parser/CTFParser.g:477:5: enumerator ( SEPARATOR enumerator )*
			{
			pushFollow(FOLLOW_enumerator_in_enumeratorList2536);
			enumerator117=enumerator();
			state._fsp--;
			if (state.failed) return retval;
			if ( state.backtracking==0 ) stream_enumerator.add(enumerator117.getTree());
			// org/eclipse/tracecompass/ctf/parser/CTFParser.g:477:16: ( SEPARATOR enumerator )*
			loop35:
			while (true) {
				int alt35=2;
				int LA35_0 = input.LA(1);
				if ( (LA35_0==SEPARATOR) ) {
					int LA35_1 = input.LA(2);
					if ( (LA35_1==ALIGNTOK||LA35_1==EVENTTOK||LA35_1==IDENTIFIER||LA35_1==SIGNEDTOK||LA35_1==STRINGTOK||LA35_1==STRING_LITERAL) ) {
						alt35=1;
					}

				}

				switch (alt35) {
				case 1 :
					// org/eclipse/tracecompass/ctf/parser/CTFParser.g:477:17: SEPARATOR enumerator
					{
					SEPARATOR118=(Token)match(input,SEPARATOR,FOLLOW_SEPARATOR_in_enumeratorList2539); if (state.failed) return retval; 
					if ( state.backtracking==0 ) stream_SEPARATOR.add(SEPARATOR118);

					pushFollow(FOLLOW_enumerator_in_enumeratorList2541);
					enumerator119=enumerator();
					state._fsp--;
					if (state.failed) return retval;
					if ( state.backtracking==0 ) stream_enumerator.add(enumerator119.getTree());
					}
					break;

				default :
					break loop35;
				}
			}

			// AST REWRITE
			// elements: enumerator
			// token labels: 
			// rule labels: retval
			// token list labels: 
			// rule list labels: 
			// wildcard labels: 
			if ( state.backtracking==0 ) {
			retval.tree = root_0;
			RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.getTree():null);

			root_0 = (CommonTree)adaptor.nil();
			// 477:40: -> ( ^( ENUM_ENUMERATOR enumerator ) )+
			{
				if ( !(stream_enumerator.hasNext()) ) {
					throw new RewriteEarlyExitException();
				}
				while ( stream_enumerator.hasNext() ) {
					// org/eclipse/tracecompass/ctf/parser/CTFParser.g:477:44: ^( ENUM_ENUMERATOR enumerator )
					{
					CommonTree root_1 = (CommonTree)adaptor.nil();
					root_1 = (CommonTree)adaptor.becomeRoot((CommonTree)adaptor.create(ENUM_ENUMERATOR, "ENUM_ENUMERATOR"), root_1);
					adaptor.addChild(root_1, stream_enumerator.nextTree());
					adaptor.addChild(root_0, root_1);
					}

				}
				stream_enumerator.reset();

			}


			retval.tree = root_0;
			}

			}

			retval.stop = input.LT(-1);

			if ( state.backtracking==0 ) {
			retval.tree = (CommonTree)adaptor.rulePostProcessing(root_0);
			adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
			}
		}

		    catch (RecognitionException e) {
		        throw e;
		    }

		finally {
			// do for sure before leaving
		}
		return retval;
	}
	// $ANTLR end "enumeratorList"


	public static class enumerator_return extends ParserRuleReturnScope {
		CommonTree tree;
		@Override
		public CommonTree getTree() { return tree; }
	};


	// $ANTLR start "enumerator"
	// org/eclipse/tracecompass/ctf/parser/CTFParser.g:480:1: enumerator : enumConstant ( enumeratorValue )? ;
	public final CTFParser.enumerator_return enumerator() throws RecognitionException {
		CTFParser.enumerator_return retval = new CTFParser.enumerator_return();
		retval.start = input.LT(1);

		CommonTree root_0 = null;

		ParserRuleReturnScope enumConstant120 =null;
		ParserRuleReturnScope enumeratorValue121 =null;


		try {
			// org/eclipse/tracecompass/ctf/parser/CTFParser.g:481:3: ( enumConstant ( enumeratorValue )? )
			// org/eclipse/tracecompass/ctf/parser/CTFParser.g:481:5: enumConstant ( enumeratorValue )?
			{
			root_0 = (CommonTree)adaptor.nil();


			pushFollow(FOLLOW_enumConstant_in_enumerator2567);
			enumConstant120=enumConstant();
			state._fsp--;
			if (state.failed) return retval;
			if ( state.backtracking==0 ) adaptor.addChild(root_0, enumConstant120.getTree());

			// org/eclipse/tracecompass/ctf/parser/CTFParser.g:481:18: ( enumeratorValue )?
			int alt36=2;
			int LA36_0 = input.LA(1);
			if ( (LA36_0==ASSIGNMENT) ) {
				alt36=1;
			}
			switch (alt36) {
				case 1 :
					// org/eclipse/tracecompass/ctf/parser/CTFParser.g:481:18: enumeratorValue
					{
					pushFollow(FOLLOW_enumeratorValue_in_enumerator2569);
					enumeratorValue121=enumeratorValue();
					state._fsp--;
					if (state.failed) return retval;
					if ( state.backtracking==0 ) adaptor.addChild(root_0, enumeratorValue121.getTree());

					}
					break;

			}

			}

			retval.stop = input.LT(-1);

			if ( state.backtracking==0 ) {
			retval.tree = (CommonTree)adaptor.rulePostProcessing(root_0);
			adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
			}
		}

		    catch (RecognitionException e) {
		        throw e;
		    }

		finally {
			// do for sure before leaving
		}
		return retval;
	}
	// $ANTLR end "enumerator"


	public static class enumeratorValue_return extends ParserRuleReturnScope {
		CommonTree tree;
		@Override
		public CommonTree getTree() { return tree; }
	};


	// $ANTLR start "enumeratorValue"
	// org/eclipse/tracecompass/ctf/parser/CTFParser.g:484:1: enumeratorValue : ASSIGNMENT e1= unaryExpression ( -> ^( ENUM_VALUE $e1) | ELIPSES e2= unaryExpression -> ^( ENUM_VALUE_RANGE $e1 $e2) ) ;
	public final CTFParser.enumeratorValue_return enumeratorValue() throws RecognitionException {
		CTFParser.enumeratorValue_return retval = new CTFParser.enumeratorValue_return();
		retval.start = input.LT(1);

		CommonTree root_0 = null;

		Token ASSIGNMENT122=null;
		Token ELIPSES123=null;
		ParserRuleReturnScope e1 =null;
		ParserRuleReturnScope e2 =null;

		CommonTree ASSIGNMENT122_tree=null;
		CommonTree ELIPSES123_tree=null;
		RewriteRuleTokenStream stream_ASSIGNMENT=new RewriteRuleTokenStream(adaptor,"token ASSIGNMENT");
		RewriteRuleTokenStream stream_ELIPSES=new RewriteRuleTokenStream(adaptor,"token ELIPSES");
		RewriteRuleSubtreeStream stream_unaryExpression=new RewriteRuleSubtreeStream(adaptor,"rule unaryExpression");

		try {
			// org/eclipse/tracecompass/ctf/parser/CTFParser.g:485:3: ( ASSIGNMENT e1= unaryExpression ( -> ^( ENUM_VALUE $e1) | ELIPSES e2= unaryExpression -> ^( ENUM_VALUE_RANGE $e1 $e2) ) )
			// org/eclipse/tracecompass/ctf/parser/CTFParser.g:485:5: ASSIGNMENT e1= unaryExpression ( -> ^( ENUM_VALUE $e1) | ELIPSES e2= unaryExpression -> ^( ENUM_VALUE_RANGE $e1 $e2) )
			{
			ASSIGNMENT122=(Token)match(input,ASSIGNMENT,FOLLOW_ASSIGNMENT_in_enumeratorValue2583); if (state.failed) return retval; 
			if ( state.backtracking==0 ) stream_ASSIGNMENT.add(ASSIGNMENT122);

			pushFollow(FOLLOW_unaryExpression_in_enumeratorValue2587);
			e1=unaryExpression();
			state._fsp--;
			if (state.failed) return retval;
			if ( state.backtracking==0 ) stream_unaryExpression.add(e1.getTree());
			// org/eclipse/tracecompass/ctf/parser/CTFParser.g:486:7: ( -> ^( ENUM_VALUE $e1) | ELIPSES e2= unaryExpression -> ^( ENUM_VALUE_RANGE $e1 $e2) )
			int alt37=2;
			int LA37_0 = input.LA(1);
			if ( (LA37_0==RCURL||LA37_0==SEPARATOR) ) {
				alt37=1;
			}
			else if ( (LA37_0==ELIPSES) ) {
				alt37=2;
			}

			else {
				if (state.backtracking>0) {state.failed=true; return retval;}
				NoViableAltException nvae =
					new NoViableAltException("", 37, 0, input);
				throw nvae;
			}

			switch (alt37) {
				case 1 :
					// org/eclipse/tracecompass/ctf/parser/CTFParser.g:487:11: 
					{
					// AST REWRITE
					// elements: e1
					// token labels: 
					// rule labels: retval, e1
					// token list labels: 
					// rule list labels: 
					// wildcard labels: 
					if ( state.backtracking==0 ) {
					retval.tree = root_0;
					RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.getTree():null);
					RewriteRuleSubtreeStream stream_e1=new RewriteRuleSubtreeStream(adaptor,"rule e1",e1!=null?e1.getTree():null);

					root_0 = (CommonTree)adaptor.nil();
					// 487:11: -> ^( ENUM_VALUE $e1)
					{
						// org/eclipse/tracecompass/ctf/parser/CTFParser.g:487:14: ^( ENUM_VALUE $e1)
						{
						CommonTree root_1 = (CommonTree)adaptor.nil();
						root_1 = (CommonTree)adaptor.becomeRoot((CommonTree)adaptor.create(ENUM_VALUE, "ENUM_VALUE"), root_1);
						adaptor.addChild(root_1, stream_e1.nextTree());
						adaptor.addChild(root_0, root_1);
						}

					}


					retval.tree = root_0;
					}

					}
					break;
				case 2 :
					// org/eclipse/tracecompass/ctf/parser/CTFParser.g:488:9: ELIPSES e2= unaryExpression
					{
					ELIPSES123=(Token)match(input,ELIPSES,FOLLOW_ELIPSES_in_enumeratorValue2626); if (state.failed) return retval; 
					if ( state.backtracking==0 ) stream_ELIPSES.add(ELIPSES123);

					pushFollow(FOLLOW_unaryExpression_in_enumeratorValue2630);
					e2=unaryExpression();
					state._fsp--;
					if (state.failed) return retval;
					if ( state.backtracking==0 ) stream_unaryExpression.add(e2.getTree());
					// AST REWRITE
					// elements: e1, e2
					// token labels: 
					// rule labels: retval, e1, e2
					// token list labels: 
					// rule list labels: 
					// wildcard labels: 
					if ( state.backtracking==0 ) {
					retval.tree = root_0;
					RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.getTree():null);
					RewriteRuleSubtreeStream stream_e1=new RewriteRuleSubtreeStream(adaptor,"rule e1",e1!=null?e1.getTree():null);
					RewriteRuleSubtreeStream stream_e2=new RewriteRuleSubtreeStream(adaptor,"rule e2",e2!=null?e2.getTree():null);

					root_0 = (CommonTree)adaptor.nil();
					// 489:11: -> ^( ENUM_VALUE_RANGE $e1 $e2)
					{
						// org/eclipse/tracecompass/ctf/parser/CTFParser.g:489:14: ^( ENUM_VALUE_RANGE $e1 $e2)
						{
						CommonTree root_1 = (CommonTree)adaptor.nil();
						root_1 = (CommonTree)adaptor.becomeRoot((CommonTree)adaptor.create(ENUM_VALUE_RANGE, "ENUM_VALUE_RANGE"), root_1);
						adaptor.addChild(root_1, stream_e1.nextTree());
						adaptor.addChild(root_1, stream_e2.nextTree());
						adaptor.addChild(root_0, root_1);
						}

					}


					retval.tree = root_0;
					}

					}
					break;

			}

			}

			retval.stop = input.LT(-1);

			if ( state.backtracking==0 ) {
			retval.tree = (CommonTree)adaptor.rulePostProcessing(root_0);
			adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
			}
		}

		    catch (RecognitionException e) {
		        throw e;
		    }

		finally {
			// do for sure before leaving
		}
		return retval;
	}
	// $ANTLR end "enumeratorValue"


	public static class declarator_return extends ParserRuleReturnScope {
		CommonTree tree;
		@Override
		public CommonTree getTree() { return tree; }
	};


	// $ANTLR start "declarator"
	// org/eclipse/tracecompass/ctf/parser/CTFParser.g:493:1: declarator : ( pointer )* directDeclarator -> ^( TYPE_DECLARATOR ( pointer )* directDeclarator ) ;
	public final CTFParser.declarator_return declarator() throws RecognitionException {
		CTFParser.declarator_return retval = new CTFParser.declarator_return();
		retval.start = input.LT(1);

		CommonTree root_0 = null;

		ParserRuleReturnScope pointer124 =null;
		ParserRuleReturnScope directDeclarator125 =null;

		RewriteRuleSubtreeStream stream_directDeclarator=new RewriteRuleSubtreeStream(adaptor,"rule directDeclarator");
		RewriteRuleSubtreeStream stream_pointer=new RewriteRuleSubtreeStream(adaptor,"rule pointer");

		try {
			// org/eclipse/tracecompass/ctf/parser/CTFParser.g:494:3: ( ( pointer )* directDeclarator -> ^( TYPE_DECLARATOR ( pointer )* directDeclarator ) )
			// org/eclipse/tracecompass/ctf/parser/CTFParser.g:494:5: ( pointer )* directDeclarator
			{
			// org/eclipse/tracecompass/ctf/parser/CTFParser.g:494:5: ( pointer )*
			loop38:
			while (true) {
				int alt38=2;
				int LA38_0 = input.LA(1);
				if ( (LA38_0==POINTER) ) {
					alt38=1;
				}

				switch (alt38) {
				case 1 :
					// org/eclipse/tracecompass/ctf/parser/CTFParser.g:494:5: pointer
					{
					pushFollow(FOLLOW_pointer_in_declarator2673);
					pointer124=pointer();
					state._fsp--;
					if (state.failed) return retval;
					if ( state.backtracking==0 ) stream_pointer.add(pointer124.getTree());
					}
					break;

				default :
					break loop38;
				}
			}

			pushFollow(FOLLOW_directDeclarator_in_declarator2676);
			directDeclarator125=directDeclarator();
			state._fsp--;
			if (state.failed) return retval;
			if ( state.backtracking==0 ) stream_directDeclarator.add(directDeclarator125.getTree());
			// AST REWRITE
			// elements: directDeclarator, pointer
			// token labels: 
			// rule labels: retval
			// token list labels: 
			// rule list labels: 
			// wildcard labels: 
			if ( state.backtracking==0 ) {
			retval.tree = root_0;
			RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.getTree():null);

			root_0 = (CommonTree)adaptor.nil();
			// 495:7: -> ^( TYPE_DECLARATOR ( pointer )* directDeclarator )
			{
				// org/eclipse/tracecompass/ctf/parser/CTFParser.g:495:10: ^( TYPE_DECLARATOR ( pointer )* directDeclarator )
				{
				CommonTree root_1 = (CommonTree)adaptor.nil();
				root_1 = (CommonTree)adaptor.becomeRoot((CommonTree)adaptor.create(TYPE_DECLARATOR, "TYPE_DECLARATOR"), root_1);
				// org/eclipse/tracecompass/ctf/parser/CTFParser.g:495:28: ( pointer )*
				while ( stream_pointer.hasNext() ) {
					adaptor.addChild(root_1, stream_pointer.nextTree());
				}
				stream_pointer.reset();

				adaptor.addChild(root_1, stream_directDeclarator.nextTree());
				adaptor.addChild(root_0, root_1);
				}

			}


			retval.tree = root_0;
			}

			}

			retval.stop = input.LT(-1);

			if ( state.backtracking==0 ) {
			retval.tree = (CommonTree)adaptor.rulePostProcessing(root_0);
			adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
			}
		}

		    catch (RecognitionException e) {
		        throw e;
		    }

		finally {
			// do for sure before leaving
		}
		return retval;
	}
	// $ANTLR end "declarator"


	public static class directDeclarator_return extends ParserRuleReturnScope {
		CommonTree tree;
		@Override
		public CommonTree getTree() { return tree; }
	};


	// $ANTLR start "directDeclarator"
	// org/eclipse/tracecompass/ctf/parser/CTFParser.g:498:1: directDeclarator : ( IDENTIFIER ) ( directDeclaratorSuffix )* ;
	public final CTFParser.directDeclarator_return directDeclarator() throws RecognitionException {
		CTFParser.directDeclarator_return retval = new CTFParser.directDeclarator_return();
		retval.start = input.LT(1);

		CommonTree root_0 = null;

		Token IDENTIFIER126=null;
		ParserRuleReturnScope directDeclaratorSuffix127 =null;

		CommonTree IDENTIFIER126_tree=null;

		try {
			// org/eclipse/tracecompass/ctf/parser/CTFParser.g:499:3: ( ( IDENTIFIER ) ( directDeclaratorSuffix )* )
			// org/eclipse/tracecompass/ctf/parser/CTFParser.g:499:5: ( IDENTIFIER ) ( directDeclaratorSuffix )*
			{
			root_0 = (CommonTree)adaptor.nil();


			// org/eclipse/tracecompass/ctf/parser/CTFParser.g:499:5: ( IDENTIFIER )
			// org/eclipse/tracecompass/ctf/parser/CTFParser.g:500:7: IDENTIFIER
			{
			IDENTIFIER126=(Token)match(input,IDENTIFIER,FOLLOW_IDENTIFIER_in_directDeclarator2714); if (state.failed) return retval;
			if ( state.backtracking==0 ) {
			IDENTIFIER126_tree = (CommonTree)adaptor.create(IDENTIFIER126);
			adaptor.addChild(root_0, IDENTIFIER126_tree);
			}

			if ( state.backtracking==0 ) { if (inTypedef()) addTypeName((IDENTIFIER126!=null?IDENTIFIER126.getText():null)); }
			if ( state.backtracking==0 ) { debug_print((IDENTIFIER126!=null?IDENTIFIER126.getText():null)); }
			}

			// org/eclipse/tracecompass/ctf/parser/CTFParser.g:505:5: ( directDeclaratorSuffix )*
			loop39:
			while (true) {
				int alt39=2;
				int LA39_0 = input.LA(1);
				if ( (LA39_0==OPENBRAC) ) {
					alt39=1;
				}

				switch (alt39) {
				case 1 :
					// org/eclipse/tracecompass/ctf/parser/CTFParser.g:505:5: directDeclaratorSuffix
					{
					pushFollow(FOLLOW_directDeclaratorSuffix_in_directDeclarator2754);
					directDeclaratorSuffix127=directDeclaratorSuffix();
					state._fsp--;
					if (state.failed) return retval;
					if ( state.backtracking==0 ) adaptor.addChild(root_0, directDeclaratorSuffix127.getTree());

					}
					break;

				default :
					break loop39;
				}
			}

			}

			retval.stop = input.LT(-1);

			if ( state.backtracking==0 ) {
			retval.tree = (CommonTree)adaptor.rulePostProcessing(root_0);
			adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
			}
		}

		    catch (RecognitionException e) {
		        throw e;
		    }

		finally {
			// do for sure before leaving
		}
		return retval;
	}
	// $ANTLR end "directDeclarator"


	public static class directDeclaratorSuffix_return extends ParserRuleReturnScope {
		CommonTree tree;
		@Override
		public CommonTree getTree() { return tree; }
	};


	// $ANTLR start "directDeclaratorSuffix"
	// org/eclipse/tracecompass/ctf/parser/CTFParser.g:508:1: directDeclaratorSuffix : OPENBRAC directDeclaratorLength CLOSEBRAC -> ^( LENGTH directDeclaratorLength ) ;
	public final CTFParser.directDeclaratorSuffix_return directDeclaratorSuffix() throws RecognitionException {
		CTFParser.directDeclaratorSuffix_return retval = new CTFParser.directDeclaratorSuffix_return();
		retval.start = input.LT(1);

		CommonTree root_0 = null;

		Token OPENBRAC128=null;
		Token CLOSEBRAC130=null;
		ParserRuleReturnScope directDeclaratorLength129 =null;

		CommonTree OPENBRAC128_tree=null;
		CommonTree CLOSEBRAC130_tree=null;
		RewriteRuleTokenStream stream_OPENBRAC=new RewriteRuleTokenStream(adaptor,"token OPENBRAC");
		RewriteRuleTokenStream stream_CLOSEBRAC=new RewriteRuleTokenStream(adaptor,"token CLOSEBRAC");
		RewriteRuleSubtreeStream stream_directDeclaratorLength=new RewriteRuleSubtreeStream(adaptor,"rule directDeclaratorLength");

		try {
			// org/eclipse/tracecompass/ctf/parser/CTFParser.g:509:3: ( OPENBRAC directDeclaratorLength CLOSEBRAC -> ^( LENGTH directDeclaratorLength ) )
			// org/eclipse/tracecompass/ctf/parser/CTFParser.g:509:5: OPENBRAC directDeclaratorLength CLOSEBRAC
			{
			OPENBRAC128=(Token)match(input,OPENBRAC,FOLLOW_OPENBRAC_in_directDeclaratorSuffix2768); if (state.failed) return retval; 
			if ( state.backtracking==0 ) stream_OPENBRAC.add(OPENBRAC128);

			pushFollow(FOLLOW_directDeclaratorLength_in_directDeclaratorSuffix2770);
			directDeclaratorLength129=directDeclaratorLength();
			state._fsp--;
			if (state.failed) return retval;
			if ( state.backtracking==0 ) stream_directDeclaratorLength.add(directDeclaratorLength129.getTree());
			CLOSEBRAC130=(Token)match(input,CLOSEBRAC,FOLLOW_CLOSEBRAC_in_directDeclaratorSuffix2772); if (state.failed) return retval; 
			if ( state.backtracking==0 ) stream_CLOSEBRAC.add(CLOSEBRAC130);

			// AST REWRITE
			// elements: directDeclaratorLength
			// token labels: 
			// rule labels: retval
			// token list labels: 
			// rule list labels: 
			// wildcard labels: 
			if ( state.backtracking==0 ) {
			retval.tree = root_0;
			RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.getTree():null);

			root_0 = (CommonTree)adaptor.nil();
			// 510:7: -> ^( LENGTH directDeclaratorLength )
			{
				// org/eclipse/tracecompass/ctf/parser/CTFParser.g:510:10: ^( LENGTH directDeclaratorLength )
				{
				CommonTree root_1 = (CommonTree)adaptor.nil();
				root_1 = (CommonTree)adaptor.becomeRoot((CommonTree)adaptor.create(LENGTH, "LENGTH"), root_1);
				adaptor.addChild(root_1, stream_directDeclaratorLength.nextTree());
				adaptor.addChild(root_0, root_1);
				}

			}


			retval.tree = root_0;
			}

			}

			retval.stop = input.LT(-1);

			if ( state.backtracking==0 ) {
			retval.tree = (CommonTree)adaptor.rulePostProcessing(root_0);
			adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
			}
		}

		    catch (RecognitionException e) {
		        throw e;
		    }

		finally {
			// do for sure before leaving
		}
		return retval;
	}
	// $ANTLR end "directDeclaratorSuffix"


	public static class directDeclaratorLength_return extends ParserRuleReturnScope {
		CommonTree tree;
		@Override
		public CommonTree getTree() { return tree; }
	};


	// $ANTLR start "directDeclaratorLength"
	// org/eclipse/tracecompass/ctf/parser/CTFParser.g:513:1: directDeclaratorLength : unaryExpression ;
	public final CTFParser.directDeclaratorLength_return directDeclaratorLength() throws RecognitionException {
		CTFParser.directDeclaratorLength_return retval = new CTFParser.directDeclaratorLength_return();
		retval.start = input.LT(1);

		CommonTree root_0 = null;

		ParserRuleReturnScope unaryExpression131 =null;


		try {
			// org/eclipse/tracecompass/ctf/parser/CTFParser.g:514:3: ( unaryExpression )
			// org/eclipse/tracecompass/ctf/parser/CTFParser.g:514:5: unaryExpression
			{
			root_0 = (CommonTree)adaptor.nil();


			pushFollow(FOLLOW_unaryExpression_in_directDeclaratorLength2800);
			unaryExpression131=unaryExpression();
			state._fsp--;
			if (state.failed) return retval;
			if ( state.backtracking==0 ) adaptor.addChild(root_0, unaryExpression131.getTree());

			}

			retval.stop = input.LT(-1);

			if ( state.backtracking==0 ) {
			retval.tree = (CommonTree)adaptor.rulePostProcessing(root_0);
			adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
			}
		}

		    catch (RecognitionException e) {
		        throw e;
		    }

		finally {
			// do for sure before leaving
		}
		return retval;
	}
	// $ANTLR end "directDeclaratorLength"


	public static class abstractDeclarator_return extends ParserRuleReturnScope {
		CommonTree tree;
		@Override
		public CommonTree getTree() { return tree; }
	};


	// $ANTLR start "abstractDeclarator"
	// org/eclipse/tracecompass/ctf/parser/CTFParser.g:517:1: abstractDeclarator : ( ( pointer )+ ( directAbstractDeclarator )? -> ^( TYPE_DECLARATOR ( pointer )+ ( directAbstractDeclarator )? ) | directAbstractDeclarator -> ^( TYPE_DECLARATOR directAbstractDeclarator ) );
	public final CTFParser.abstractDeclarator_return abstractDeclarator() throws RecognitionException {
		CTFParser.abstractDeclarator_return retval = new CTFParser.abstractDeclarator_return();
		retval.start = input.LT(1);

		CommonTree root_0 = null;

		ParserRuleReturnScope pointer132 =null;
		ParserRuleReturnScope directAbstractDeclarator133 =null;
		ParserRuleReturnScope directAbstractDeclarator134 =null;

		RewriteRuleSubtreeStream stream_pointer=new RewriteRuleSubtreeStream(adaptor,"rule pointer");
		RewriteRuleSubtreeStream stream_directAbstractDeclarator=new RewriteRuleSubtreeStream(adaptor,"rule directAbstractDeclarator");

		try {
			// org/eclipse/tracecompass/ctf/parser/CTFParser.g:518:3: ( ( pointer )+ ( directAbstractDeclarator )? -> ^( TYPE_DECLARATOR ( pointer )+ ( directAbstractDeclarator )? ) | directAbstractDeclarator -> ^( TYPE_DECLARATOR directAbstractDeclarator ) )
			int alt42=2;
			int LA42_0 = input.LA(1);
			if ( (LA42_0==POINTER) ) {
				alt42=1;
			}
			else if ( (LA42_0==IDENTIFIER||LA42_0==LPAREN) ) {
				alt42=2;
			}

			else {
				if (state.backtracking>0) {state.failed=true; return retval;}
				NoViableAltException nvae =
					new NoViableAltException("", 42, 0, input);
				throw nvae;
			}

			switch (alt42) {
				case 1 :
					// org/eclipse/tracecompass/ctf/parser/CTFParser.g:518:5: ( pointer )+ ( directAbstractDeclarator )?
					{
					// org/eclipse/tracecompass/ctf/parser/CTFParser.g:518:5: ( pointer )+
					int cnt40=0;
					loop40:
					while (true) {
						int alt40=2;
						int LA40_0 = input.LA(1);
						if ( (LA40_0==POINTER) ) {
							alt40=1;
						}

						switch (alt40) {
						case 1 :
							// org/eclipse/tracecompass/ctf/parser/CTFParser.g:518:5: pointer
							{
							pushFollow(FOLLOW_pointer_in_abstractDeclarator2813);
							pointer132=pointer();
							state._fsp--;
							if (state.failed) return retval;
							if ( state.backtracking==0 ) stream_pointer.add(pointer132.getTree());
							}
							break;

						default :
							if ( cnt40 >= 1 ) break loop40;
							if (state.backtracking>0) {state.failed=true; return retval;}
							EarlyExitException eee = new EarlyExitException(40, input);
							throw eee;
						}
						cnt40++;
					}

					// org/eclipse/tracecompass/ctf/parser/CTFParser.g:518:14: ( directAbstractDeclarator )?
					int alt41=2;
					int LA41_0 = input.LA(1);
					if ( (LA41_0==IDENTIFIER||LA41_0==LPAREN) ) {
						alt41=1;
					}
					switch (alt41) {
						case 1 :
							// org/eclipse/tracecompass/ctf/parser/CTFParser.g:518:14: directAbstractDeclarator
							{
							pushFollow(FOLLOW_directAbstractDeclarator_in_abstractDeclarator2816);
							directAbstractDeclarator133=directAbstractDeclarator();
							state._fsp--;
							if (state.failed) return retval;
							if ( state.backtracking==0 ) stream_directAbstractDeclarator.add(directAbstractDeclarator133.getTree());
							}
							break;

					}

					// AST REWRITE
					// elements: pointer, directAbstractDeclarator
					// token labels: 
					// rule labels: retval
					// token list labels: 
					// rule list labels: 
					// wildcard labels: 
					if ( state.backtracking==0 ) {
					retval.tree = root_0;
					RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.getTree():null);

					root_0 = (CommonTree)adaptor.nil();
					// 519:7: -> ^( TYPE_DECLARATOR ( pointer )+ ( directAbstractDeclarator )? )
					{
						// org/eclipse/tracecompass/ctf/parser/CTFParser.g:519:10: ^( TYPE_DECLARATOR ( pointer )+ ( directAbstractDeclarator )? )
						{
						CommonTree root_1 = (CommonTree)adaptor.nil();
						root_1 = (CommonTree)adaptor.becomeRoot((CommonTree)adaptor.create(TYPE_DECLARATOR, "TYPE_DECLARATOR"), root_1);
						if ( !(stream_pointer.hasNext()) ) {
							throw new RewriteEarlyExitException();
						}
						while ( stream_pointer.hasNext() ) {
							adaptor.addChild(root_1, stream_pointer.nextTree());
						}
						stream_pointer.reset();

						// org/eclipse/tracecompass/ctf/parser/CTFParser.g:519:37: ( directAbstractDeclarator )?
						if ( stream_directAbstractDeclarator.hasNext() ) {
							adaptor.addChild(root_1, stream_directAbstractDeclarator.nextTree());
						}
						stream_directAbstractDeclarator.reset();

						adaptor.addChild(root_0, root_1);
						}

					}


					retval.tree = root_0;
					}

					}
					break;
				case 2 :
					// org/eclipse/tracecompass/ctf/parser/CTFParser.g:520:5: directAbstractDeclarator
					{
					pushFollow(FOLLOW_directAbstractDeclarator_in_abstractDeclarator2841);
					directAbstractDeclarator134=directAbstractDeclarator();
					state._fsp--;
					if (state.failed) return retval;
					if ( state.backtracking==0 ) stream_directAbstractDeclarator.add(directAbstractDeclarator134.getTree());
					// AST REWRITE
					// elements: directAbstractDeclarator
					// token labels: 
					// rule labels: retval
					// token list labels: 
					// rule list labels: 
					// wildcard labels: 
					if ( state.backtracking==0 ) {
					retval.tree = root_0;
					RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.getTree():null);

					root_0 = (CommonTree)adaptor.nil();
					// 521:7: -> ^( TYPE_DECLARATOR directAbstractDeclarator )
					{
						// org/eclipse/tracecompass/ctf/parser/CTFParser.g:521:10: ^( TYPE_DECLARATOR directAbstractDeclarator )
						{
						CommonTree root_1 = (CommonTree)adaptor.nil();
						root_1 = (CommonTree)adaptor.becomeRoot((CommonTree)adaptor.create(TYPE_DECLARATOR, "TYPE_DECLARATOR"), root_1);
						adaptor.addChild(root_1, stream_directAbstractDeclarator.nextTree());
						adaptor.addChild(root_0, root_1);
						}

					}


					retval.tree = root_0;
					}

					}
					break;

			}
			retval.stop = input.LT(-1);

			if ( state.backtracking==0 ) {
			retval.tree = (CommonTree)adaptor.rulePostProcessing(root_0);
			adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
			}
		}

		    catch (RecognitionException e) {
		        throw e;
		    }

		finally {
			// do for sure before leaving
		}
		return retval;
	}
	// $ANTLR end "abstractDeclarator"


	public static class directAbstractDeclarator_return extends ParserRuleReturnScope {
		CommonTree tree;
		@Override
		public CommonTree getTree() { return tree; }
	};


	// $ANTLR start "directAbstractDeclarator"
	// org/eclipse/tracecompass/ctf/parser/CTFParser.g:529:1: directAbstractDeclarator : ( IDENTIFIER | ( LPAREN abstractDeclarator RPAREN ) ) ( OPENBRAC ( unaryExpression )? CLOSEBRAC )? ;
	public final CTFParser.directAbstractDeclarator_return directAbstractDeclarator() throws RecognitionException {
		CTFParser.directAbstractDeclarator_return retval = new CTFParser.directAbstractDeclarator_return();
		retval.start = input.LT(1);

		CommonTree root_0 = null;

		Token IDENTIFIER135=null;
		Token LPAREN136=null;
		Token RPAREN138=null;
		Token OPENBRAC139=null;
		Token CLOSEBRAC141=null;
		ParserRuleReturnScope abstractDeclarator137 =null;
		ParserRuleReturnScope unaryExpression140 =null;

		CommonTree IDENTIFIER135_tree=null;
		CommonTree LPAREN136_tree=null;
		CommonTree RPAREN138_tree=null;
		CommonTree OPENBRAC139_tree=null;
		CommonTree CLOSEBRAC141_tree=null;

		try {
			// org/eclipse/tracecompass/ctf/parser/CTFParser.g:530:3: ( ( IDENTIFIER | ( LPAREN abstractDeclarator RPAREN ) ) ( OPENBRAC ( unaryExpression )? CLOSEBRAC )? )
			// org/eclipse/tracecompass/ctf/parser/CTFParser.g:530:5: ( IDENTIFIER | ( LPAREN abstractDeclarator RPAREN ) ) ( OPENBRAC ( unaryExpression )? CLOSEBRAC )?
			{
			root_0 = (CommonTree)adaptor.nil();


			// org/eclipse/tracecompass/ctf/parser/CTFParser.g:530:5: ( IDENTIFIER | ( LPAREN abstractDeclarator RPAREN ) )
			int alt43=2;
			int LA43_0 = input.LA(1);
			if ( (LA43_0==IDENTIFIER) ) {
				alt43=1;
			}
			else if ( (LA43_0==LPAREN) ) {
				alt43=2;
			}

			else {
				if (state.backtracking>0) {state.failed=true; return retval;}
				NoViableAltException nvae =
					new NoViableAltException("", 43, 0, input);
				throw nvae;
			}

			switch (alt43) {
				case 1 :
					// org/eclipse/tracecompass/ctf/parser/CTFParser.g:531:7: IDENTIFIER
					{
					IDENTIFIER135=(Token)match(input,IDENTIFIER,FOLLOW_IDENTIFIER_in_directAbstractDeclarator2878); if (state.failed) return retval;
					if ( state.backtracking==0 ) {
					IDENTIFIER135_tree = (CommonTree)adaptor.create(IDENTIFIER135);
					adaptor.addChild(root_0, IDENTIFIER135_tree);
					}

					}
					break;
				case 2 :
					// org/eclipse/tracecompass/ctf/parser/CTFParser.g:532:9: ( LPAREN abstractDeclarator RPAREN )
					{
					// org/eclipse/tracecompass/ctf/parser/CTFParser.g:532:9: ( LPAREN abstractDeclarator RPAREN )
					// org/eclipse/tracecompass/ctf/parser/CTFParser.g:532:10: LPAREN abstractDeclarator RPAREN
					{
					LPAREN136=(Token)match(input,LPAREN,FOLLOW_LPAREN_in_directAbstractDeclarator2889); if (state.failed) return retval;
					if ( state.backtracking==0 ) {
					LPAREN136_tree = (CommonTree)adaptor.create(LPAREN136);
					adaptor.addChild(root_0, LPAREN136_tree);
					}

					pushFollow(FOLLOW_abstractDeclarator_in_directAbstractDeclarator2891);
					abstractDeclarator137=abstractDeclarator();
					state._fsp--;
					if (state.failed) return retval;
					if ( state.backtracking==0 ) adaptor.addChild(root_0, abstractDeclarator137.getTree());

					RPAREN138=(Token)match(input,RPAREN,FOLLOW_RPAREN_in_directAbstractDeclarator2893); if (state.failed) return retval;
					if ( state.backtracking==0 ) {
					RPAREN138_tree = (CommonTree)adaptor.create(RPAREN138);
					adaptor.addChild(root_0, RPAREN138_tree);
					}

					}

					}
					break;

			}

			// org/eclipse/tracecompass/ctf/parser/CTFParser.g:533:5: ( OPENBRAC ( unaryExpression )? CLOSEBRAC )?
			int alt45=2;
			int LA45_0 = input.LA(1);
			if ( (LA45_0==OPENBRAC) ) {
				alt45=1;
			}
			switch (alt45) {
				case 1 :
					// org/eclipse/tracecompass/ctf/parser/CTFParser.g:534:7: OPENBRAC ( unaryExpression )? CLOSEBRAC
					{
					OPENBRAC139=(Token)match(input,OPENBRAC,FOLLOW_OPENBRAC_in_directAbstractDeclarator2908); if (state.failed) return retval;
					if ( state.backtracking==0 ) {
					OPENBRAC139_tree = (CommonTree)adaptor.create(OPENBRAC139);
					adaptor.addChild(root_0, OPENBRAC139_tree);
					}

					// org/eclipse/tracecompass/ctf/parser/CTFParser.g:534:16: ( unaryExpression )?
					int alt44=2;
					int LA44_0 = input.LA(1);
					if ( (LA44_0==ALIGNTOK||(LA44_0 >= CALLSITETOK && LA44_0 <= CHARACTER_LITERAL)||LA44_0==CLOCKTOK||LA44_0==DECIMAL_LITERAL||LA44_0==ENVTOK||LA44_0==EVENTTOK||LA44_0==HEX_LITERAL||LA44_0==IDENTIFIER||LA44_0==OCTAL_LITERAL||(LA44_0 >= SIGN && LA44_0 <= SIGNEDTOK)||LA44_0==STREAMTOK||LA44_0==STRINGTOK||LA44_0==STRING_LITERAL||LA44_0==TRACETOK) ) {
						alt44=1;
					}
					switch (alt44) {
						case 1 :
							// org/eclipse/tracecompass/ctf/parser/CTFParser.g:534:16: unaryExpression
							{
							pushFollow(FOLLOW_unaryExpression_in_directAbstractDeclarator2910);
							unaryExpression140=unaryExpression();
							state._fsp--;
							if (state.failed) return retval;
							if ( state.backtracking==0 ) adaptor.addChild(root_0, unaryExpression140.getTree());

							}
							break;

					}

					CLOSEBRAC141=(Token)match(input,CLOSEBRAC,FOLLOW_CLOSEBRAC_in_directAbstractDeclarator2913); if (state.failed) return retval;
					if ( state.backtracking==0 ) {
					CLOSEBRAC141_tree = (CommonTree)adaptor.create(CLOSEBRAC141);
					adaptor.addChild(root_0, CLOSEBRAC141_tree);
					}

					}
					break;

			}

			}

			retval.stop = input.LT(-1);

			if ( state.backtracking==0 ) {
			retval.tree = (CommonTree)adaptor.rulePostProcessing(root_0);
			adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
			}
		}

		    catch (RecognitionException e) {
		        throw e;
		    }

		finally {
			// do for sure before leaving
		}
		return retval;
	}
	// $ANTLR end "directAbstractDeclarator"


	public static class pointer_return extends ParserRuleReturnScope {
		CommonTree tree;
		@Override
		public CommonTree getTree() { return tree; }
	};


	// $ANTLR start "pointer"
	// org/eclipse/tracecompass/ctf/parser/CTFParser.g:538:1: pointer : POINTER ( typeQualifierList )? -> ^( POINTER ( typeQualifierList )? ) ;
	public final CTFParser.pointer_return pointer() throws RecognitionException {
		CTFParser.pointer_return retval = new CTFParser.pointer_return();
		retval.start = input.LT(1);

		CommonTree root_0 = null;

		Token POINTER142=null;
		ParserRuleReturnScope typeQualifierList143 =null;

		CommonTree POINTER142_tree=null;
		RewriteRuleTokenStream stream_POINTER=new RewriteRuleTokenStream(adaptor,"token POINTER");
		RewriteRuleSubtreeStream stream_typeQualifierList=new RewriteRuleSubtreeStream(adaptor,"rule typeQualifierList");

		try {
			// org/eclipse/tracecompass/ctf/parser/CTFParser.g:539:3: ( POINTER ( typeQualifierList )? -> ^( POINTER ( typeQualifierList )? ) )
			// org/eclipse/tracecompass/ctf/parser/CTFParser.g:539:5: POINTER ( typeQualifierList )?
			{
			POINTER142=(Token)match(input,POINTER,FOLLOW_POINTER_in_pointer2931); if (state.failed) return retval; 
			if ( state.backtracking==0 ) stream_POINTER.add(POINTER142);

			// org/eclipse/tracecompass/ctf/parser/CTFParser.g:539:13: ( typeQualifierList )?
			int alt46=2;
			int LA46_0 = input.LA(1);
			if ( (LA46_0==CONSTTOK) ) {
				alt46=1;
			}
			switch (alt46) {
				case 1 :
					// org/eclipse/tracecompass/ctf/parser/CTFParser.g:539:13: typeQualifierList
					{
					pushFollow(FOLLOW_typeQualifierList_in_pointer2933);
					typeQualifierList143=typeQualifierList();
					state._fsp--;
					if (state.failed) return retval;
					if ( state.backtracking==0 ) stream_typeQualifierList.add(typeQualifierList143.getTree());
					}
					break;

			}

			// AST REWRITE
			// elements: POINTER, typeQualifierList
			// token labels: 
			// rule labels: retval
			// token list labels: 
			// rule list labels: 
			// wildcard labels: 
			if ( state.backtracking==0 ) {
			retval.tree = root_0;
			RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.getTree():null);

			root_0 = (CommonTree)adaptor.nil();
			// 539:32: -> ^( POINTER ( typeQualifierList )? )
			{
				// org/eclipse/tracecompass/ctf/parser/CTFParser.g:539:35: ^( POINTER ( typeQualifierList )? )
				{
				CommonTree root_1 = (CommonTree)adaptor.nil();
				root_1 = (CommonTree)adaptor.becomeRoot(stream_POINTER.nextNode(), root_1);
				// org/eclipse/tracecompass/ctf/parser/CTFParser.g:539:45: ( typeQualifierList )?
				if ( stream_typeQualifierList.hasNext() ) {
					adaptor.addChild(root_1, stream_typeQualifierList.nextTree());
				}
				stream_typeQualifierList.reset();

				adaptor.addChild(root_0, root_1);
				}

			}


			retval.tree = root_0;
			}

			}

			retval.stop = input.LT(-1);

			if ( state.backtracking==0 ) {
			retval.tree = (CommonTree)adaptor.rulePostProcessing(root_0);
			adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
			}
		}

		    catch (RecognitionException e) {
		        throw e;
		    }

		finally {
			// do for sure before leaving
		}
		return retval;
	}
	// $ANTLR end "pointer"


	public static class typeQualifierList_return extends ParserRuleReturnScope {
		CommonTree tree;
		@Override
		public CommonTree getTree() { return tree; }
	};


	// $ANTLR start "typeQualifierList"
	// org/eclipse/tracecompass/ctf/parser/CTFParser.g:542:1: typeQualifierList : ( typeQualifier )+ ;
	public final CTFParser.typeQualifierList_return typeQualifierList() throws RecognitionException {
		CTFParser.typeQualifierList_return retval = new CTFParser.typeQualifierList_return();
		retval.start = input.LT(1);

		CommonTree root_0 = null;

		ParserRuleReturnScope typeQualifier144 =null;


		try {
			// org/eclipse/tracecompass/ctf/parser/CTFParser.g:543:3: ( ( typeQualifier )+ )
			// org/eclipse/tracecompass/ctf/parser/CTFParser.g:543:5: ( typeQualifier )+
			{
			root_0 = (CommonTree)adaptor.nil();


			// org/eclipse/tracecompass/ctf/parser/CTFParser.g:543:5: ( typeQualifier )+
			int cnt47=0;
			loop47:
			while (true) {
				int alt47=2;
				int LA47_0 = input.LA(1);
				if ( (LA47_0==CONSTTOK) ) {
					alt47=1;
				}

				switch (alt47) {
				case 1 :
					// org/eclipse/tracecompass/ctf/parser/CTFParser.g:543:5: typeQualifier
					{
					pushFollow(FOLLOW_typeQualifier_in_typeQualifierList2956);
					typeQualifier144=typeQualifier();
					state._fsp--;
					if (state.failed) return retval;
					if ( state.backtracking==0 ) adaptor.addChild(root_0, typeQualifier144.getTree());

					}
					break;

				default :
					if ( cnt47 >= 1 ) break loop47;
					if (state.backtracking>0) {state.failed=true; return retval;}
					EarlyExitException eee = new EarlyExitException(47, input);
					throw eee;
				}
				cnt47++;
			}

			}

			retval.stop = input.LT(-1);

			if ( state.backtracking==0 ) {
			retval.tree = (CommonTree)adaptor.rulePostProcessing(root_0);
			adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
			}
		}

		    catch (RecognitionException e) {
		        throw e;
		    }

		finally {
			// do for sure before leaving
		}
		return retval;
	}
	// $ANTLR end "typeQualifierList"


	public static class typedefName_return extends ParserRuleReturnScope {
		CommonTree tree;
		@Override
		public CommonTree getTree() { return tree; }
	};


	// $ANTLR start "typedefName"
	// org/eclipse/tracecompass/ctf/parser/CTFParser.g:546:1: typedefName :{...}? IDENTIFIER ;
	public final CTFParser.typedefName_return typedefName() throws RecognitionException {
		CTFParser.typedefName_return retval = new CTFParser.typedefName_return();
		retval.start = input.LT(1);

		CommonTree root_0 = null;

		Token IDENTIFIER145=null;

		CommonTree IDENTIFIER145_tree=null;

		try {
			// org/eclipse/tracecompass/ctf/parser/CTFParser.g:547:3: ({...}? IDENTIFIER )
			// org/eclipse/tracecompass/ctf/parser/CTFParser.g:547:5: {...}? IDENTIFIER
			{
			root_0 = (CommonTree)adaptor.nil();


			if ( !((inTypealiasAlias() || isTypeName(input.LT(1).getText()))) ) {
				if (state.backtracking>0) {state.failed=true; return retval;}
				throw new FailedPredicateException(input, "typedefName", "inTypealiasAlias() || isTypeName(input.LT(1).getText())");
			}
			IDENTIFIER145=(Token)match(input,IDENTIFIER,FOLLOW_IDENTIFIER_in_typedefName2972); if (state.failed) return retval;
			if ( state.backtracking==0 ) {
			IDENTIFIER145_tree = (CommonTree)adaptor.create(IDENTIFIER145);
			adaptor.addChild(root_0, IDENTIFIER145_tree);
			}

			if ( state.backtracking==0 ) { if ((inTypedef() || inTypealiasAlias()) && !isTypeName((IDENTIFIER145!=null?IDENTIFIER145.getText():null))) { addTypeName((IDENTIFIER145!=null?IDENTIFIER145.getText():null)); } }
			}

			retval.stop = input.LT(-1);

			if ( state.backtracking==0 ) {
			retval.tree = (CommonTree)adaptor.rulePostProcessing(root_0);
			adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
			}
		}

		    catch (RecognitionException e) {
		        throw e;
		    }

		finally {
			// do for sure before leaving
		}
		return retval;
	}
	// $ANTLR end "typedefName"


	public static class typealiasTarget_return extends ParserRuleReturnScope {
		CommonTree tree;
		@Override
		public CommonTree getTree() { return tree; }
	};


	// $ANTLR start "typealiasTarget"
	// org/eclipse/tracecompass/ctf/parser/CTFParser.g:556:1: typealiasTarget : declarationSpecifiers ( abstractDeclaratorList )? ;
	public final CTFParser.typealiasTarget_return typealiasTarget() throws RecognitionException {
		CTFParser.typealiasTarget_return retval = new CTFParser.typealiasTarget_return();
		retval.start = input.LT(1);

		CommonTree root_0 = null;

		ParserRuleReturnScope declarationSpecifiers146 =null;
		ParserRuleReturnScope abstractDeclaratorList147 =null;


		try {
			// org/eclipse/tracecompass/ctf/parser/CTFParser.g:557:3: ( declarationSpecifiers ( abstractDeclaratorList )? )
			// org/eclipse/tracecompass/ctf/parser/CTFParser.g:557:5: declarationSpecifiers ( abstractDeclaratorList )?
			{
			root_0 = (CommonTree)adaptor.nil();


			pushFollow(FOLLOW_declarationSpecifiers_in_typealiasTarget2989);
			declarationSpecifiers146=declarationSpecifiers();
			state._fsp--;
			if (state.failed) return retval;
			if ( state.backtracking==0 ) adaptor.addChild(root_0, declarationSpecifiers146.getTree());

			// org/eclipse/tracecompass/ctf/parser/CTFParser.g:557:27: ( abstractDeclaratorList )?
			int alt48=2;
			int LA48_0 = input.LA(1);
			if ( (LA48_0==IDENTIFIER||LA48_0==LPAREN||LA48_0==POINTER) ) {
				alt48=1;
			}
			switch (alt48) {
				case 1 :
					// org/eclipse/tracecompass/ctf/parser/CTFParser.g:557:27: abstractDeclaratorList
					{
					pushFollow(FOLLOW_abstractDeclaratorList_in_typealiasTarget2991);
					abstractDeclaratorList147=abstractDeclaratorList();
					state._fsp--;
					if (state.failed) return retval;
					if ( state.backtracking==0 ) adaptor.addChild(root_0, abstractDeclaratorList147.getTree());

					}
					break;

			}

			}

			retval.stop = input.LT(-1);

			if ( state.backtracking==0 ) {
			retval.tree = (CommonTree)adaptor.rulePostProcessing(root_0);
			adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
			}
		}

		    catch (RecognitionException e) {
		        throw e;
		    }

		finally {
			// do for sure before leaving
		}
		return retval;
	}
	// $ANTLR end "typealiasTarget"


	public static class typealiasAlias_return extends ParserRuleReturnScope {
		CommonTree tree;
		@Override
		public CommonTree getTree() { return tree; }
	};


	// $ANTLR start "typealiasAlias"
	// org/eclipse/tracecompass/ctf/parser/CTFParser.g:566:1: typealiasAlias : ( abstractDeclaratorList | declarationSpecifiers ( abstractDeclaratorList )? );
	public final CTFParser.typealiasAlias_return typealiasAlias() throws RecognitionException {
		CTFParser.typealiasAlias_return retval = new CTFParser.typealiasAlias_return();
		retval.start = input.LT(1);

		CommonTree root_0 = null;

		ParserRuleReturnScope abstractDeclaratorList148 =null;
		ParserRuleReturnScope declarationSpecifiers149 =null;
		ParserRuleReturnScope abstractDeclaratorList150 =null;



		    typealiasAliasOn();

		try {
			// org/eclipse/tracecompass/ctf/parser/CTFParser.g:573:3: ( abstractDeclaratorList | declarationSpecifiers ( abstractDeclaratorList )? )
			int alt50=2;
			switch ( input.LA(1) ) {
			case LPAREN:
			case POINTER:
				{
				alt50=1;
				}
				break;
			case IDENTIFIER:
				{
				int LA50_2 = input.LA(2);
				if ( (!(((( inTypealiasAlias() || isTypeName(input.LT(1).getText()) )&&(inTypealiasAlias() || isTypeName(input.LT(1).getText())))))) ) {
					alt50=1;
				}
				else if ( ((( inTypealiasAlias() || isTypeName(input.LT(1).getText()) )&&(inTypealiasAlias() || isTypeName(input.LT(1).getText())))) ) {
					alt50=2;
				}

				else {
					if (state.backtracking>0) {state.failed=true; return retval;}
					int nvaeMark = input.mark();
					try {
						input.consume();
						NoViableAltException nvae =
							new NoViableAltException("", 50, 2, input);
						throw nvae;
					} finally {
						input.rewind(nvaeMark);
					}
				}

				}
				break;
			case BOOLTOK:
			case CHARTOK:
			case COMPLEXTOK:
			case CONSTTOK:
			case DOUBLETOK:
			case ENUMTOK:
			case FLOATINGPOINTTOK:
			case FLOATTOK:
			case IMAGINARYTOK:
			case INTEGERTOK:
			case INTTOK:
			case LONGTOK:
			case SHORTTOK:
			case SIGNEDTOK:
			case STRINGTOK:
			case STRUCTTOK:
			case TYPEDEFTOK:
			case UNSIGNEDTOK:
			case VARIANTTOK:
			case VOIDTOK:
				{
				alt50=2;
				}
				break;
			default:
				if (state.backtracking>0) {state.failed=true; return retval;}
				NoViableAltException nvae =
					new NoViableAltException("", 50, 0, input);
				throw nvae;
			}
			switch (alt50) {
				case 1 :
					// org/eclipse/tracecompass/ctf/parser/CTFParser.g:573:5: abstractDeclaratorList
					{
					root_0 = (CommonTree)adaptor.nil();


					pushFollow(FOLLOW_abstractDeclaratorList_in_typealiasAlias3017);
					abstractDeclaratorList148=abstractDeclaratorList();
					state._fsp--;
					if (state.failed) return retval;
					if ( state.backtracking==0 ) adaptor.addChild(root_0, abstractDeclaratorList148.getTree());

					}
					break;
				case 2 :
					// org/eclipse/tracecompass/ctf/parser/CTFParser.g:574:5: declarationSpecifiers ( abstractDeclaratorList )?
					{
					root_0 = (CommonTree)adaptor.nil();


					pushFollow(FOLLOW_declarationSpecifiers_in_typealiasAlias3023);
					declarationSpecifiers149=declarationSpecifiers();
					state._fsp--;
					if (state.failed) return retval;
					if ( state.backtracking==0 ) adaptor.addChild(root_0, declarationSpecifiers149.getTree());

					// org/eclipse/tracecompass/ctf/parser/CTFParser.g:574:27: ( abstractDeclaratorList )?
					int alt49=2;
					int LA49_0 = input.LA(1);
					if ( (LA49_0==IDENTIFIER||LA49_0==LPAREN||LA49_0==POINTER) ) {
						alt49=1;
					}
					switch (alt49) {
						case 1 :
							// org/eclipse/tracecompass/ctf/parser/CTFParser.g:574:27: abstractDeclaratorList
							{
							pushFollow(FOLLOW_abstractDeclaratorList_in_typealiasAlias3025);
							abstractDeclaratorList150=abstractDeclaratorList();
							state._fsp--;
							if (state.failed) return retval;
							if ( state.backtracking==0 ) adaptor.addChild(root_0, abstractDeclaratorList150.getTree());

							}
							break;

					}

					}
					break;

			}
			retval.stop = input.LT(-1);

			if ( state.backtracking==0 ) {
			retval.tree = (CommonTree)adaptor.rulePostProcessing(root_0);
			adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
			}
			if ( state.backtracking==0 ) {
			    typealiasAliasOff();
			}
		}

		    catch (RecognitionException e) {
		        throw e;
		    }

		finally {
			// do for sure before leaving
		}
		return retval;
	}
	// $ANTLR end "typealiasAlias"


	public static class typealiasDecl_return extends ParserRuleReturnScope {
		CommonTree tree;
		@Override
		public CommonTree getTree() { return tree; }
	};


	// $ANTLR start "typealiasDecl"
	// org/eclipse/tracecompass/ctf/parser/CTFParser.g:577:1: typealiasDecl : TYPEALIASTOK typealiasTarget TYPE_ASSIGNMENT typealiasAlias -> ^( TYPEALIAS ^( TYPEALIAS_TARGET typealiasTarget ) ^( TYPEALIAS_ALIAS typealiasAlias ) ) ;
	public final CTFParser.typealiasDecl_return typealiasDecl() throws RecognitionException {
		CTFParser.typealiasDecl_return retval = new CTFParser.typealiasDecl_return();
		retval.start = input.LT(1);

		CommonTree root_0 = null;

		Token TYPEALIASTOK151=null;
		Token TYPE_ASSIGNMENT153=null;
		ParserRuleReturnScope typealiasTarget152 =null;
		ParserRuleReturnScope typealiasAlias154 =null;

		CommonTree TYPEALIASTOK151_tree=null;
		CommonTree TYPE_ASSIGNMENT153_tree=null;
		RewriteRuleTokenStream stream_TYPE_ASSIGNMENT=new RewriteRuleTokenStream(adaptor,"token TYPE_ASSIGNMENT");
		RewriteRuleTokenStream stream_TYPEALIASTOK=new RewriteRuleTokenStream(adaptor,"token TYPEALIASTOK");
		RewriteRuleSubtreeStream stream_typealiasAlias=new RewriteRuleSubtreeStream(adaptor,"rule typealiasAlias");
		RewriteRuleSubtreeStream stream_typealiasTarget=new RewriteRuleSubtreeStream(adaptor,"rule typealiasTarget");

		try {
			// org/eclipse/tracecompass/ctf/parser/CTFParser.g:578:3: ( TYPEALIASTOK typealiasTarget TYPE_ASSIGNMENT typealiasAlias -> ^( TYPEALIAS ^( TYPEALIAS_TARGET typealiasTarget ) ^( TYPEALIAS_ALIAS typealiasAlias ) ) )
			// org/eclipse/tracecompass/ctf/parser/CTFParser.g:578:5: TYPEALIASTOK typealiasTarget TYPE_ASSIGNMENT typealiasAlias
			{
			TYPEALIASTOK151=(Token)match(input,TYPEALIASTOK,FOLLOW_TYPEALIASTOK_in_typealiasDecl3039); if (state.failed) return retval; 
			if ( state.backtracking==0 ) stream_TYPEALIASTOK.add(TYPEALIASTOK151);

			pushFollow(FOLLOW_typealiasTarget_in_typealiasDecl3041);
			typealiasTarget152=typealiasTarget();
			state._fsp--;
			if (state.failed) return retval;
			if ( state.backtracking==0 ) stream_typealiasTarget.add(typealiasTarget152.getTree());
			TYPE_ASSIGNMENT153=(Token)match(input,TYPE_ASSIGNMENT,FOLLOW_TYPE_ASSIGNMENT_in_typealiasDecl3043); if (state.failed) return retval; 
			if ( state.backtracking==0 ) stream_TYPE_ASSIGNMENT.add(TYPE_ASSIGNMENT153);

			pushFollow(FOLLOW_typealiasAlias_in_typealiasDecl3045);
			typealiasAlias154=typealiasAlias();
			state._fsp--;
			if (state.failed) return retval;
			if ( state.backtracking==0 ) stream_typealiasAlias.add(typealiasAlias154.getTree());
			// AST REWRITE
			// elements: typealiasTarget, typealiasAlias
			// token labels: 
			// rule labels: retval
			// token list labels: 
			// rule list labels: 
			// wildcard labels: 
			if ( state.backtracking==0 ) {
			retval.tree = root_0;
			RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.getTree():null);

			root_0 = (CommonTree)adaptor.nil();
			// 579:7: -> ^( TYPEALIAS ^( TYPEALIAS_TARGET typealiasTarget ) ^( TYPEALIAS_ALIAS typealiasAlias ) )
			{
				// org/eclipse/tracecompass/ctf/parser/CTFParser.g:579:10: ^( TYPEALIAS ^( TYPEALIAS_TARGET typealiasTarget ) ^( TYPEALIAS_ALIAS typealiasAlias ) )
				{
				CommonTree root_1 = (CommonTree)adaptor.nil();
				root_1 = (CommonTree)adaptor.becomeRoot((CommonTree)adaptor.create(TYPEALIAS, "TYPEALIAS"), root_1);
				// org/eclipse/tracecompass/ctf/parser/CTFParser.g:580:14: ^( TYPEALIAS_TARGET typealiasTarget )
				{
				CommonTree root_2 = (CommonTree)adaptor.nil();
				root_2 = (CommonTree)adaptor.becomeRoot((CommonTree)adaptor.create(TYPEALIAS_TARGET, "TYPEALIAS_TARGET"), root_2);
				adaptor.addChild(root_2, stream_typealiasTarget.nextTree());
				adaptor.addChild(root_1, root_2);
				}

				// org/eclipse/tracecompass/ctf/parser/CTFParser.g:581:14: ^( TYPEALIAS_ALIAS typealiasAlias )
				{
				CommonTree root_2 = (CommonTree)adaptor.nil();
				root_2 = (CommonTree)adaptor.becomeRoot((CommonTree)adaptor.create(TYPEALIAS_ALIAS, "TYPEALIAS_ALIAS"), root_2);
				adaptor.addChild(root_2, stream_typealiasAlias.nextTree());
				adaptor.addChild(root_1, root_2);
				}

				adaptor.addChild(root_0, root_1);
				}

			}


			retval.tree = root_0;
			}

			}

			retval.stop = input.LT(-1);

			if ( state.backtracking==0 ) {
			retval.tree = (CommonTree)adaptor.rulePostProcessing(root_0);
			adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
			}
		}

		    catch (RecognitionException e) {
		        throw e;
		    }

		finally {
			// do for sure before leaving
		}
		return retval;
	}
	// $ANTLR end "typealiasDecl"


	public static class ctfKeyword_return extends ParserRuleReturnScope {
		CommonTree tree;
		@Override
		public CommonTree getTree() { return tree; }
	};


	// $ANTLR start "ctfKeyword"
	// org/eclipse/tracecompass/ctf/parser/CTFParser.g:587:1: ctfKeyword : ( ALIGNTOK | EVENTTOK | SIGNEDTOK | STRINGTOK );
	public final CTFParser.ctfKeyword_return ctfKeyword() throws RecognitionException {
		CTFParser.ctfKeyword_return retval = new CTFParser.ctfKeyword_return();
		retval.start = input.LT(1);

		CommonTree root_0 = null;

		Token set155=null;

		CommonTree set155_tree=null;

		try {
			// org/eclipse/tracecompass/ctf/parser/CTFParser.g:588:3: ( ALIGNTOK | EVENTTOK | SIGNEDTOK | STRINGTOK )
			// org/eclipse/tracecompass/ctf/parser/CTFParser.g:
			{
			root_0 = (CommonTree)adaptor.nil();


			set155=input.LT(1);
			if ( input.LA(1)==ALIGNTOK||input.LA(1)==EVENTTOK||input.LA(1)==SIGNEDTOK||input.LA(1)==STRINGTOK ) {
				input.consume();
				if ( state.backtracking==0 ) adaptor.addChild(root_0, (CommonTree)adaptor.create(set155));
				state.errorRecovery=false;
				state.failed=false;
			}
			else {
				if (state.backtracking>0) {state.failed=true; return retval;}
				MismatchedSetException mse = new MismatchedSetException(null,input);
				throw mse;
			}
			}

			retval.stop = input.LT(-1);

			if ( state.backtracking==0 ) {
			retval.tree = (CommonTree)adaptor.rulePostProcessing(root_0);
			adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
			}
		}

		    catch (RecognitionException e) {
		        throw e;
		    }

		finally {
			// do for sure before leaving
		}
		return retval;
	}
	// $ANTLR end "ctfKeyword"


	public static class ctfSpecifier_return extends ParserRuleReturnScope {
		CommonTree tree;
		@Override
		public CommonTree getTree() { return tree; }
	};


	// $ANTLR start "ctfSpecifier"
	// org/eclipse/tracecompass/ctf/parser/CTFParser.g:594:1: ctfSpecifier : ( ctfSpecifierHead ctfBody -> ^( ctfSpecifierHead ctfBody ) | typealiasDecl -> ^( DECLARATION typealiasDecl ) );
	public final CTFParser.ctfSpecifier_return ctfSpecifier() throws RecognitionException {
		CTFParser.ctfSpecifier_return retval = new CTFParser.ctfSpecifier_return();
		retval.start = input.LT(1);

		CommonTree root_0 = null;

		ParserRuleReturnScope ctfSpecifierHead156 =null;
		ParserRuleReturnScope ctfBody157 =null;
		ParserRuleReturnScope typealiasDecl158 =null;

		RewriteRuleSubtreeStream stream_ctfSpecifierHead=new RewriteRuleSubtreeStream(adaptor,"rule ctfSpecifierHead");
		RewriteRuleSubtreeStream stream_typealiasDecl=new RewriteRuleSubtreeStream(adaptor,"rule typealiasDecl");
		RewriteRuleSubtreeStream stream_ctfBody=new RewriteRuleSubtreeStream(adaptor,"rule ctfBody");

		try {
			// org/eclipse/tracecompass/ctf/parser/CTFParser.g:596:3: ( ctfSpecifierHead ctfBody -> ^( ctfSpecifierHead ctfBody ) | typealiasDecl -> ^( DECLARATION typealiasDecl ) )
			int alt51=2;
			int LA51_0 = input.LA(1);
			if ( (LA51_0==CALLSITETOK||LA51_0==CLOCKTOK||LA51_0==ENVTOK||LA51_0==EVENTTOK||LA51_0==STREAMTOK||LA51_0==TRACETOK) ) {
				alt51=1;
			}
			else if ( (LA51_0==TYPEALIASTOK) ) {
				alt51=2;
			}

			else {
				if (state.backtracking>0) {state.failed=true; return retval;}
				NoViableAltException nvae =
					new NoViableAltException("", 51, 0, input);
				throw nvae;
			}

			switch (alt51) {
				case 1 :
					// org/eclipse/tracecompass/ctf/parser/CTFParser.g:596:5: ctfSpecifierHead ctfBody
					{
					pushFollow(FOLLOW_ctfSpecifierHead_in_ctfSpecifier3145);
					ctfSpecifierHead156=ctfSpecifierHead();
					state._fsp--;
					if (state.failed) return retval;
					if ( state.backtracking==0 ) stream_ctfSpecifierHead.add(ctfSpecifierHead156.getTree());
					pushFollow(FOLLOW_ctfBody_in_ctfSpecifier3147);
					ctfBody157=ctfBody();
					state._fsp--;
					if (state.failed) return retval;
					if ( state.backtracking==0 ) stream_ctfBody.add(ctfBody157.getTree());
					// AST REWRITE
					// elements: ctfSpecifierHead, ctfBody
					// token labels: 
					// rule labels: retval
					// token list labels: 
					// rule list labels: 
					// wildcard labels: 
					if ( state.backtracking==0 ) {
					retval.tree = root_0;
					RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.getTree():null);

					root_0 = (CommonTree)adaptor.nil();
					// 596:30: -> ^( ctfSpecifierHead ctfBody )
					{
						// org/eclipse/tracecompass/ctf/parser/CTFParser.g:596:33: ^( ctfSpecifierHead ctfBody )
						{
						CommonTree root_1 = (CommonTree)adaptor.nil();
						root_1 = (CommonTree)adaptor.becomeRoot(stream_ctfSpecifierHead.nextNode(), root_1);
						adaptor.addChild(root_1, stream_ctfBody.nextTree());
						adaptor.addChild(root_0, root_1);
						}

					}


					retval.tree = root_0;
					}

					}
					break;
				case 2 :
					// org/eclipse/tracecompass/ctf/parser/CTFParser.g:598:5: typealiasDecl
					{
					pushFollow(FOLLOW_typealiasDecl_in_ctfSpecifier3164);
					typealiasDecl158=typealiasDecl();
					state._fsp--;
					if (state.failed) return retval;
					if ( state.backtracking==0 ) stream_typealiasDecl.add(typealiasDecl158.getTree());
					// AST REWRITE
					// elements: typealiasDecl
					// token labels: 
					// rule labels: retval
					// token list labels: 
					// rule list labels: 
					// wildcard labels: 
					if ( state.backtracking==0 ) {
					retval.tree = root_0;
					RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.getTree():null);

					root_0 = (CommonTree)adaptor.nil();
					// 598:19: -> ^( DECLARATION typealiasDecl )
					{
						// org/eclipse/tracecompass/ctf/parser/CTFParser.g:598:22: ^( DECLARATION typealiasDecl )
						{
						CommonTree root_1 = (CommonTree)adaptor.nil();
						root_1 = (CommonTree)adaptor.becomeRoot((CommonTree)adaptor.create(DECLARATION, "DECLARATION"), root_1);
						adaptor.addChild(root_1, stream_typealiasDecl.nextTree());
						adaptor.addChild(root_0, root_1);
						}

					}


					retval.tree = root_0;
					}

					}
					break;

			}
			retval.stop = input.LT(-1);

			if ( state.backtracking==0 ) {
			retval.tree = (CommonTree)adaptor.rulePostProcessing(root_0);
			adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
			}
		}

		    catch (RecognitionException e) {
		        throw e;
		    }

		finally {
			// do for sure before leaving
		}
		return retval;
	}
	// $ANTLR end "ctfSpecifier"


	public static class ctfSpecifierHead_return extends ParserRuleReturnScope {
		CommonTree tree;
		@Override
		public CommonTree getTree() { return tree; }
	};


	// $ANTLR start "ctfSpecifierHead"
	// org/eclipse/tracecompass/ctf/parser/CTFParser.g:601:1: ctfSpecifierHead : ( EVENTTOK -> EVENT | STREAMTOK -> STREAM | TRACETOK -> TRACE | ENVTOK -> ENV | CLOCKTOK -> CLOCK | CALLSITETOK -> CALLSITE );
	public final CTFParser.ctfSpecifierHead_return ctfSpecifierHead() throws RecognitionException {
		CTFParser.ctfSpecifierHead_return retval = new CTFParser.ctfSpecifierHead_return();
		retval.start = input.LT(1);

		CommonTree root_0 = null;

		Token EVENTTOK159=null;
		Token STREAMTOK160=null;
		Token TRACETOK161=null;
		Token ENVTOK162=null;
		Token CLOCKTOK163=null;
		Token CALLSITETOK164=null;

		CommonTree EVENTTOK159_tree=null;
		CommonTree STREAMTOK160_tree=null;
		CommonTree TRACETOK161_tree=null;
		CommonTree ENVTOK162_tree=null;
		CommonTree CLOCKTOK163_tree=null;
		CommonTree CALLSITETOK164_tree=null;
		RewriteRuleTokenStream stream_EVENTTOK=new RewriteRuleTokenStream(adaptor,"token EVENTTOK");
		RewriteRuleTokenStream stream_CALLSITETOK=new RewriteRuleTokenStream(adaptor,"token CALLSITETOK");
		RewriteRuleTokenStream stream_STREAMTOK=new RewriteRuleTokenStream(adaptor,"token STREAMTOK");
		RewriteRuleTokenStream stream_ENVTOK=new RewriteRuleTokenStream(adaptor,"token ENVTOK");
		RewriteRuleTokenStream stream_CLOCKTOK=new RewriteRuleTokenStream(adaptor,"token CLOCKTOK");
		RewriteRuleTokenStream stream_TRACETOK=new RewriteRuleTokenStream(adaptor,"token TRACETOK");

		try {
			// org/eclipse/tracecompass/ctf/parser/CTFParser.g:602:3: ( EVENTTOK -> EVENT | STREAMTOK -> STREAM | TRACETOK -> TRACE | ENVTOK -> ENV | CLOCKTOK -> CLOCK | CALLSITETOK -> CALLSITE )
			int alt52=6;
			switch ( input.LA(1) ) {
			case EVENTTOK:
				{
				alt52=1;
				}
				break;
			case STREAMTOK:
				{
				alt52=2;
				}
				break;
			case TRACETOK:
				{
				alt52=3;
				}
				break;
			case ENVTOK:
				{
				alt52=4;
				}
				break;
			case CLOCKTOK:
				{
				alt52=5;
				}
				break;
			case CALLSITETOK:
				{
				alt52=6;
				}
				break;
			default:
				if (state.backtracking>0) {state.failed=true; return retval;}
				NoViableAltException nvae =
					new NoViableAltException("", 52, 0, input);
				throw nvae;
			}
			switch (alt52) {
				case 1 :
					// org/eclipse/tracecompass/ctf/parser/CTFParser.g:602:5: EVENTTOK
					{
					EVENTTOK159=(Token)match(input,EVENTTOK,FOLLOW_EVENTTOK_in_ctfSpecifierHead3185); if (state.failed) return retval; 
					if ( state.backtracking==0 ) stream_EVENTTOK.add(EVENTTOK159);

					// AST REWRITE
					// elements: 
					// token labels: 
					// rule labels: retval
					// token list labels: 
					// rule list labels: 
					// wildcard labels: 
					if ( state.backtracking==0 ) {
					retval.tree = root_0;
					RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.getTree():null);

					root_0 = (CommonTree)adaptor.nil();
					// 602:14: -> EVENT
					{
						adaptor.addChild(root_0, (CommonTree)adaptor.create(EVENT, "EVENT"));
					}


					retval.tree = root_0;
					}

					}
					break;
				case 2 :
					// org/eclipse/tracecompass/ctf/parser/CTFParser.g:603:5: STREAMTOK
					{
					STREAMTOK160=(Token)match(input,STREAMTOK,FOLLOW_STREAMTOK_in_ctfSpecifierHead3195); if (state.failed) return retval; 
					if ( state.backtracking==0 ) stream_STREAMTOK.add(STREAMTOK160);

					// AST REWRITE
					// elements: 
					// token labels: 
					// rule labels: retval
					// token list labels: 
					// rule list labels: 
					// wildcard labels: 
					if ( state.backtracking==0 ) {
					retval.tree = root_0;
					RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.getTree():null);

					root_0 = (CommonTree)adaptor.nil();
					// 603:15: -> STREAM
					{
						adaptor.addChild(root_0, (CommonTree)adaptor.create(STREAM, "STREAM"));
					}


					retval.tree = root_0;
					}

					}
					break;
				case 3 :
					// org/eclipse/tracecompass/ctf/parser/CTFParser.g:604:5: TRACETOK
					{
					TRACETOK161=(Token)match(input,TRACETOK,FOLLOW_TRACETOK_in_ctfSpecifierHead3205); if (state.failed) return retval; 
					if ( state.backtracking==0 ) stream_TRACETOK.add(TRACETOK161);

					// AST REWRITE
					// elements: 
					// token labels: 
					// rule labels: retval
					// token list labels: 
					// rule list labels: 
					// wildcard labels: 
					if ( state.backtracking==0 ) {
					retval.tree = root_0;
					RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.getTree():null);

					root_0 = (CommonTree)adaptor.nil();
					// 604:14: -> TRACE
					{
						adaptor.addChild(root_0, (CommonTree)adaptor.create(TRACE, "TRACE"));
					}


					retval.tree = root_0;
					}

					}
					break;
				case 4 :
					// org/eclipse/tracecompass/ctf/parser/CTFParser.g:605:5: ENVTOK
					{
					ENVTOK162=(Token)match(input,ENVTOK,FOLLOW_ENVTOK_in_ctfSpecifierHead3215); if (state.failed) return retval; 
					if ( state.backtracking==0 ) stream_ENVTOK.add(ENVTOK162);

					// AST REWRITE
					// elements: 
					// token labels: 
					// rule labels: retval
					// token list labels: 
					// rule list labels: 
					// wildcard labels: 
					if ( state.backtracking==0 ) {
					retval.tree = root_0;
					RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.getTree():null);

					root_0 = (CommonTree)adaptor.nil();
					// 605:12: -> ENV
					{
						adaptor.addChild(root_0, (CommonTree)adaptor.create(ENV, "ENV"));
					}


					retval.tree = root_0;
					}

					}
					break;
				case 5 :
					// org/eclipse/tracecompass/ctf/parser/CTFParser.g:606:5: CLOCKTOK
					{
					CLOCKTOK163=(Token)match(input,CLOCKTOK,FOLLOW_CLOCKTOK_in_ctfSpecifierHead3225); if (state.failed) return retval; 
					if ( state.backtracking==0 ) stream_CLOCKTOK.add(CLOCKTOK163);

					// AST REWRITE
					// elements: 
					// token labels: 
					// rule labels: retval
					// token list labels: 
					// rule list labels: 
					// wildcard labels: 
					if ( state.backtracking==0 ) {
					retval.tree = root_0;
					RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.getTree():null);

					root_0 = (CommonTree)adaptor.nil();
					// 606:14: -> CLOCK
					{
						adaptor.addChild(root_0, (CommonTree)adaptor.create(CLOCK, "CLOCK"));
					}


					retval.tree = root_0;
					}

					}
					break;
				case 6 :
					// org/eclipse/tracecompass/ctf/parser/CTFParser.g:607:5: CALLSITETOK
					{
					CALLSITETOK164=(Token)match(input,CALLSITETOK,FOLLOW_CALLSITETOK_in_ctfSpecifierHead3235); if (state.failed) return retval; 
					if ( state.backtracking==0 ) stream_CALLSITETOK.add(CALLSITETOK164);

					// AST REWRITE
					// elements: 
					// token labels: 
					// rule labels: retval
					// token list labels: 
					// rule list labels: 
					// wildcard labels: 
					if ( state.backtracking==0 ) {
					retval.tree = root_0;
					RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.getTree():null);

					root_0 = (CommonTree)adaptor.nil();
					// 607:17: -> CALLSITE
					{
						adaptor.addChild(root_0, (CommonTree)adaptor.create(CALLSITE, "CALLSITE"));
					}


					retval.tree = root_0;
					}

					}
					break;

			}
			retval.stop = input.LT(-1);

			if ( state.backtracking==0 ) {
			retval.tree = (CommonTree)adaptor.rulePostProcessing(root_0);
			adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
			}
		}

		    catch (RecognitionException e) {
		        throw e;
		    }

		finally {
			// do for sure before leaving
		}
		return retval;
	}
	// $ANTLR end "ctfSpecifierHead"


	public static class ctfTypeSpecifier_return extends ParserRuleReturnScope {
		CommonTree tree;
		@Override
		public CommonTree getTree() { return tree; }
	};


	// $ANTLR start "ctfTypeSpecifier"
	// org/eclipse/tracecompass/ctf/parser/CTFParser.g:610:1: ctfTypeSpecifier : ( FLOATINGPOINTTOK ctfBody -> ^( FLOATING_POINT ( ctfBody )? ) | INTEGERTOK ctfBody -> ^( INTEGER ( ctfBody )? ) | STRINGTOK ( ctfBody )? -> ^( STRING ( ctfBody )? ) );
	public final CTFParser.ctfTypeSpecifier_return ctfTypeSpecifier() throws RecognitionException {
		CTFParser.ctfTypeSpecifier_return retval = new CTFParser.ctfTypeSpecifier_return();
		retval.start = input.LT(1);

		CommonTree root_0 = null;

		Token FLOATINGPOINTTOK165=null;
		Token INTEGERTOK167=null;
		Token STRINGTOK169=null;
		ParserRuleReturnScope ctfBody166 =null;
		ParserRuleReturnScope ctfBody168 =null;
		ParserRuleReturnScope ctfBody170 =null;

		CommonTree FLOATINGPOINTTOK165_tree=null;
		CommonTree INTEGERTOK167_tree=null;
		CommonTree STRINGTOK169_tree=null;
		RewriteRuleTokenStream stream_FLOATINGPOINTTOK=new RewriteRuleTokenStream(adaptor,"token FLOATINGPOINTTOK");
		RewriteRuleTokenStream stream_STRINGTOK=new RewriteRuleTokenStream(adaptor,"token STRINGTOK");
		RewriteRuleTokenStream stream_INTEGERTOK=new RewriteRuleTokenStream(adaptor,"token INTEGERTOK");
		RewriteRuleSubtreeStream stream_ctfBody=new RewriteRuleSubtreeStream(adaptor,"rule ctfBody");

		try {
			// org/eclipse/tracecompass/ctf/parser/CTFParser.g:612:3: ( FLOATINGPOINTTOK ctfBody -> ^( FLOATING_POINT ( ctfBody )? ) | INTEGERTOK ctfBody -> ^( INTEGER ( ctfBody )? ) | STRINGTOK ( ctfBody )? -> ^( STRING ( ctfBody )? ) )
			int alt54=3;
			switch ( input.LA(1) ) {
			case FLOATINGPOINTTOK:
				{
				alt54=1;
				}
				break;
			case INTEGERTOK:
				{
				alt54=2;
				}
				break;
			case STRINGTOK:
				{
				alt54=3;
				}
				break;
			default:
				if (state.backtracking>0) {state.failed=true; return retval;}
				NoViableAltException nvae =
					new NoViableAltException("", 54, 0, input);
				throw nvae;
			}
			switch (alt54) {
				case 1 :
					// org/eclipse/tracecompass/ctf/parser/CTFParser.g:612:5: FLOATINGPOINTTOK ctfBody
					{
					FLOATINGPOINTTOK165=(Token)match(input,FLOATINGPOINTTOK,FOLLOW_FLOATINGPOINTTOK_in_ctfTypeSpecifier3258); if (state.failed) return retval; 
					if ( state.backtracking==0 ) stream_FLOATINGPOINTTOK.add(FLOATINGPOINTTOK165);

					pushFollow(FOLLOW_ctfBody_in_ctfTypeSpecifier3260);
					ctfBody166=ctfBody();
					state._fsp--;
					if (state.failed) return retval;
					if ( state.backtracking==0 ) stream_ctfBody.add(ctfBody166.getTree());
					// AST REWRITE
					// elements: ctfBody
					// token labels: 
					// rule labels: retval
					// token list labels: 
					// rule list labels: 
					// wildcard labels: 
					if ( state.backtracking==0 ) {
					retval.tree = root_0;
					RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.getTree():null);

					root_0 = (CommonTree)adaptor.nil();
					// 612:30: -> ^( FLOATING_POINT ( ctfBody )? )
					{
						// org/eclipse/tracecompass/ctf/parser/CTFParser.g:612:33: ^( FLOATING_POINT ( ctfBody )? )
						{
						CommonTree root_1 = (CommonTree)adaptor.nil();
						root_1 = (CommonTree)adaptor.becomeRoot((CommonTree)adaptor.create(FLOATING_POINT, "FLOATING_POINT"), root_1);
						// org/eclipse/tracecompass/ctf/parser/CTFParser.g:612:50: ( ctfBody )?
						if ( stream_ctfBody.hasNext() ) {
							adaptor.addChild(root_1, stream_ctfBody.nextTree());
						}
						stream_ctfBody.reset();

						adaptor.addChild(root_0, root_1);
						}

					}


					retval.tree = root_0;
					}

					}
					break;
				case 2 :
					// org/eclipse/tracecompass/ctf/parser/CTFParser.g:613:5: INTEGERTOK ctfBody
					{
					INTEGERTOK167=(Token)match(input,INTEGERTOK,FOLLOW_INTEGERTOK_in_ctfTypeSpecifier3275); if (state.failed) return retval; 
					if ( state.backtracking==0 ) stream_INTEGERTOK.add(INTEGERTOK167);

					pushFollow(FOLLOW_ctfBody_in_ctfTypeSpecifier3277);
					ctfBody168=ctfBody();
					state._fsp--;
					if (state.failed) return retval;
					if ( state.backtracking==0 ) stream_ctfBody.add(ctfBody168.getTree());
					// AST REWRITE
					// elements: ctfBody
					// token labels: 
					// rule labels: retval
					// token list labels: 
					// rule list labels: 
					// wildcard labels: 
					if ( state.backtracking==0 ) {
					retval.tree = root_0;
					RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.getTree():null);

					root_0 = (CommonTree)adaptor.nil();
					// 613:24: -> ^( INTEGER ( ctfBody )? )
					{
						// org/eclipse/tracecompass/ctf/parser/CTFParser.g:613:27: ^( INTEGER ( ctfBody )? )
						{
						CommonTree root_1 = (CommonTree)adaptor.nil();
						root_1 = (CommonTree)adaptor.becomeRoot((CommonTree)adaptor.create(INTEGER, "INTEGER"), root_1);
						// org/eclipse/tracecompass/ctf/parser/CTFParser.g:613:37: ( ctfBody )?
						if ( stream_ctfBody.hasNext() ) {
							adaptor.addChild(root_1, stream_ctfBody.nextTree());
						}
						stream_ctfBody.reset();

						adaptor.addChild(root_0, root_1);
						}

					}


					retval.tree = root_0;
					}

					}
					break;
				case 3 :
					// org/eclipse/tracecompass/ctf/parser/CTFParser.g:614:5: STRINGTOK ( ctfBody )?
					{
					STRINGTOK169=(Token)match(input,STRINGTOK,FOLLOW_STRINGTOK_in_ctfTypeSpecifier3292); if (state.failed) return retval; 
					if ( state.backtracking==0 ) stream_STRINGTOK.add(STRINGTOK169);

					// org/eclipse/tracecompass/ctf/parser/CTFParser.g:614:15: ( ctfBody )?
					int alt53=2;
					int LA53_0 = input.LA(1);
					if ( (LA53_0==LCURL) ) {
						alt53=1;
					}
					switch (alt53) {
						case 1 :
							// org/eclipse/tracecompass/ctf/parser/CTFParser.g:614:15: ctfBody
							{
							pushFollow(FOLLOW_ctfBody_in_ctfTypeSpecifier3294);
							ctfBody170=ctfBody();
							state._fsp--;
							if (state.failed) return retval;
							if ( state.backtracking==0 ) stream_ctfBody.add(ctfBody170.getTree());
							}
							break;

					}

					// AST REWRITE
					// elements: ctfBody
					// token labels: 
					// rule labels: retval
					// token list labels: 
					// rule list labels: 
					// wildcard labels: 
					if ( state.backtracking==0 ) {
					retval.tree = root_0;
					RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.getTree():null);

					root_0 = (CommonTree)adaptor.nil();
					// 614:24: -> ^( STRING ( ctfBody )? )
					{
						// org/eclipse/tracecompass/ctf/parser/CTFParser.g:614:27: ^( STRING ( ctfBody )? )
						{
						CommonTree root_1 = (CommonTree)adaptor.nil();
						root_1 = (CommonTree)adaptor.becomeRoot((CommonTree)adaptor.create(STRING, "STRING"), root_1);
						// org/eclipse/tracecompass/ctf/parser/CTFParser.g:614:36: ( ctfBody )?
						if ( stream_ctfBody.hasNext() ) {
							adaptor.addChild(root_1, stream_ctfBody.nextTree());
						}
						stream_ctfBody.reset();

						adaptor.addChild(root_0, root_1);
						}

					}


					retval.tree = root_0;
					}

					}
					break;

			}
			retval.stop = input.LT(-1);

			if ( state.backtracking==0 ) {
			retval.tree = (CommonTree)adaptor.rulePostProcessing(root_0);
			adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
			}
		}

		    catch (RecognitionException e) {
		        throw e;
		    }

		finally {
			// do for sure before leaving
		}
		return retval;
	}
	// $ANTLR end "ctfTypeSpecifier"


	public static class ctfBody_return extends ParserRuleReturnScope {
		CommonTree tree;
		@Override
		public CommonTree getTree() { return tree; }
	};


	// $ANTLR start "ctfBody"
	// org/eclipse/tracecompass/ctf/parser/CTFParser.g:617:1: ctfBody : LCURL ( ctfAssignmentExpressionList )? RCURL -> ( ctfAssignmentExpressionList )? ;
	public final CTFParser.ctfBody_return ctfBody() throws RecognitionException {
		Symbols_stack.push(new Symbols_scope());

		CTFParser.ctfBody_return retval = new CTFParser.ctfBody_return();
		retval.start = input.LT(1);

		CommonTree root_0 = null;

		Token LCURL171=null;
		Token RCURL173=null;
		ParserRuleReturnScope ctfAssignmentExpressionList172 =null;

		CommonTree LCURL171_tree=null;
		CommonTree RCURL173_tree=null;
		RewriteRuleTokenStream stream_LCURL=new RewriteRuleTokenStream(adaptor,"token LCURL");
		RewriteRuleTokenStream stream_RCURL=new RewriteRuleTokenStream(adaptor,"token RCURL");
		RewriteRuleSubtreeStream stream_ctfAssignmentExpressionList=new RewriteRuleSubtreeStream(adaptor,"rule ctfAssignmentExpressionList");


		    Symbols_stack.peek().types = new HashSet<String>();

		try {
			// org/eclipse/tracecompass/ctf/parser/CTFParser.g:622:3: ( LCURL ( ctfAssignmentExpressionList )? RCURL -> ( ctfAssignmentExpressionList )? )
			// org/eclipse/tracecompass/ctf/parser/CTFParser.g:622:5: LCURL ( ctfAssignmentExpressionList )? RCURL
			{
			LCURL171=(Token)match(input,LCURL,FOLLOW_LCURL_in_ctfBody3327); if (state.failed) return retval; 
			if ( state.backtracking==0 ) stream_LCURL.add(LCURL171);

			// org/eclipse/tracecompass/ctf/parser/CTFParser.g:622:11: ( ctfAssignmentExpressionList )?
			int alt55=2;
			int LA55_0 = input.LA(1);
			if ( (LA55_0==ALIGNTOK||(LA55_0 >= BOOLTOK && LA55_0 <= CHARTOK)||LA55_0==CLOCKTOK||(LA55_0 >= COMPLEXTOK && LA55_0 <= DECIMAL_LITERAL)||LA55_0==DOUBLETOK||(LA55_0 >= ENUMTOK && LA55_0 <= ENVTOK)||(LA55_0 >= EVENTTOK && LA55_0 <= FLOATTOK)||LA55_0==HEX_LITERAL||(LA55_0 >= IDENTIFIER && LA55_0 <= IMAGINARYTOK)||LA55_0==INTEGERTOK||LA55_0==INTTOK||LA55_0==LONGTOK||LA55_0==OCTAL_LITERAL||(LA55_0 >= SHORTTOK && LA55_0 <= SIGNEDTOK)||LA55_0==STREAMTOK||LA55_0==STRINGTOK||(LA55_0 >= STRING_LITERAL && LA55_0 <= STRUCTTOK)||(LA55_0 >= TRACETOK && LA55_0 <= TYPEDEFTOK)||(LA55_0 >= UNSIGNEDTOK && LA55_0 <= VOIDTOK)) ) {
				alt55=1;
			}
			switch (alt55) {
				case 1 :
					// org/eclipse/tracecompass/ctf/parser/CTFParser.g:622:11: ctfAssignmentExpressionList
					{
					pushFollow(FOLLOW_ctfAssignmentExpressionList_in_ctfBody3329);
					ctfAssignmentExpressionList172=ctfAssignmentExpressionList();
					state._fsp--;
					if (state.failed) return retval;
					if ( state.backtracking==0 ) stream_ctfAssignmentExpressionList.add(ctfAssignmentExpressionList172.getTree());
					}
					break;

			}

			RCURL173=(Token)match(input,RCURL,FOLLOW_RCURL_in_ctfBody3332); if (state.failed) return retval; 
			if ( state.backtracking==0 ) stream_RCURL.add(RCURL173);

			// AST REWRITE
			// elements: ctfAssignmentExpressionList
			// token labels: 
			// rule labels: retval
			// token list labels: 
			// rule list labels: 
			// wildcard labels: 
			if ( state.backtracking==0 ) {
			retval.tree = root_0;
			RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.getTree():null);

			root_0 = (CommonTree)adaptor.nil();
			// 622:46: -> ( ctfAssignmentExpressionList )?
			{
				// org/eclipse/tracecompass/ctf/parser/CTFParser.g:622:49: ( ctfAssignmentExpressionList )?
				if ( stream_ctfAssignmentExpressionList.hasNext() ) {
					adaptor.addChild(root_0, stream_ctfAssignmentExpressionList.nextTree());
				}
				stream_ctfAssignmentExpressionList.reset();

			}


			retval.tree = root_0;
			}

			}

			retval.stop = input.LT(-1);

			if ( state.backtracking==0 ) {
			retval.tree = (CommonTree)adaptor.rulePostProcessing(root_0);
			adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
			}
		}

		    catch (RecognitionException e) {
		        throw e;
		    }

		finally {
			// do for sure before leaving
			Symbols_stack.pop();

		}
		return retval;
	}
	// $ANTLR end "ctfBody"


	public static class ctfAssignmentExpressionList_return extends ParserRuleReturnScope {
		CommonTree tree;
		@Override
		public CommonTree getTree() { return tree; }
	};


	// $ANTLR start "ctfAssignmentExpressionList"
	// org/eclipse/tracecompass/ctf/parser/CTFParser.g:625:1: ctfAssignmentExpressionList : ( ctfAssignmentExpression TERM !)+ ;
	public final CTFParser.ctfAssignmentExpressionList_return ctfAssignmentExpressionList() throws RecognitionException {
		CTFParser.ctfAssignmentExpressionList_return retval = new CTFParser.ctfAssignmentExpressionList_return();
		retval.start = input.LT(1);

		CommonTree root_0 = null;

		Token TERM175=null;
		ParserRuleReturnScope ctfAssignmentExpression174 =null;

		CommonTree TERM175_tree=null;

		try {
			// org/eclipse/tracecompass/ctf/parser/CTFParser.g:626:3: ( ( ctfAssignmentExpression TERM !)+ )
			// org/eclipse/tracecompass/ctf/parser/CTFParser.g:626:5: ( ctfAssignmentExpression TERM !)+
			{
			root_0 = (CommonTree)adaptor.nil();


			// org/eclipse/tracecompass/ctf/parser/CTFParser.g:626:5: ( ctfAssignmentExpression TERM !)+
			int cnt56=0;
			loop56:
			while (true) {
				int alt56=2;
				int LA56_0 = input.LA(1);
				if ( (LA56_0==ALIGNTOK||(LA56_0 >= BOOLTOK && LA56_0 <= CHARTOK)||LA56_0==CLOCKTOK||(LA56_0 >= COMPLEXTOK && LA56_0 <= DECIMAL_LITERAL)||LA56_0==DOUBLETOK||(LA56_0 >= ENUMTOK && LA56_0 <= ENVTOK)||(LA56_0 >= EVENTTOK && LA56_0 <= FLOATTOK)||LA56_0==HEX_LITERAL||(LA56_0 >= IDENTIFIER && LA56_0 <= IMAGINARYTOK)||LA56_0==INTEGERTOK||LA56_0==INTTOK||LA56_0==LONGTOK||LA56_0==OCTAL_LITERAL||(LA56_0 >= SHORTTOK && LA56_0 <= SIGNEDTOK)||LA56_0==STREAMTOK||LA56_0==STRINGTOK||(LA56_0 >= STRING_LITERAL && LA56_0 <= STRUCTTOK)||(LA56_0 >= TRACETOK && LA56_0 <= TYPEDEFTOK)||(LA56_0 >= UNSIGNEDTOK && LA56_0 <= VOIDTOK)) ) {
					alt56=1;
				}

				switch (alt56) {
				case 1 :
					// org/eclipse/tracecompass/ctf/parser/CTFParser.g:626:6: ctfAssignmentExpression TERM !
					{
					pushFollow(FOLLOW_ctfAssignmentExpression_in_ctfAssignmentExpressionList3351);
					ctfAssignmentExpression174=ctfAssignmentExpression();
					state._fsp--;
					if (state.failed) return retval;
					if ( state.backtracking==0 ) adaptor.addChild(root_0, ctfAssignmentExpression174.getTree());

					TERM175=(Token)match(input,TERM,FOLLOW_TERM_in_ctfAssignmentExpressionList3353); if (state.failed) return retval;
					}
					break;

				default :
					if ( cnt56 >= 1 ) break loop56;
					if (state.backtracking>0) {state.failed=true; return retval;}
					EarlyExitException eee = new EarlyExitException(56, input);
					throw eee;
				}
				cnt56++;
			}

			}

			retval.stop = input.LT(-1);

			if ( state.backtracking==0 ) {
			retval.tree = (CommonTree)adaptor.rulePostProcessing(root_0);
			adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
			}
		}

		    catch (RecognitionException e) {
		        throw e;
		    }

		finally {
			// do for sure before leaving
		}
		return retval;
	}
	// $ANTLR end "ctfAssignmentExpressionList"


	public static class ctfAssignmentExpression_return extends ParserRuleReturnScope {
		CommonTree tree;
		@Override
		public CommonTree getTree() { return tree; }
	};


	// $ANTLR start "ctfAssignmentExpression"
	// org/eclipse/tracecompass/ctf/parser/CTFParser.g:629:1: ctfAssignmentExpression : (left= unaryExpression (assignment= ASSIGNMENT right1= unaryExpression -> ^( CTF_EXPRESSION_VAL ^( CTF_LEFT $left) ^( CTF_RIGHT $right1) ) |type_assignment= TYPE_ASSIGNMENT right2= typeSpecifier -> ^( CTF_EXPRESSION_TYPE ^( CTF_LEFT $left) ^( CTF_RIGHT ^( TYPE_SPECIFIER_LIST $right2) ) ) ) | ( declarationSpecifiers {...}? declaratorList ) -> ^( TYPEDEF declaratorList declarationSpecifiers ) | typealiasDecl );
	public final CTFParser.ctfAssignmentExpression_return ctfAssignmentExpression() throws RecognitionException {
		CTFParser.ctfAssignmentExpression_return retval = new CTFParser.ctfAssignmentExpression_return();
		retval.start = input.LT(1);

		CommonTree root_0 = null;

		Token assignment=null;
		Token type_assignment=null;
		ParserRuleReturnScope left =null;
		ParserRuleReturnScope right1 =null;
		ParserRuleReturnScope right2 =null;
		ParserRuleReturnScope declarationSpecifiers176 =null;
		ParserRuleReturnScope declaratorList177 =null;
		ParserRuleReturnScope typealiasDecl178 =null;

		CommonTree assignment_tree=null;
		CommonTree type_assignment_tree=null;
		RewriteRuleTokenStream stream_ASSIGNMENT=new RewriteRuleTokenStream(adaptor,"token ASSIGNMENT");
		RewriteRuleTokenStream stream_TYPE_ASSIGNMENT=new RewriteRuleTokenStream(adaptor,"token TYPE_ASSIGNMENT");
		RewriteRuleSubtreeStream stream_declaratorList=new RewriteRuleSubtreeStream(adaptor,"rule declaratorList");
		RewriteRuleSubtreeStream stream_unaryExpression=new RewriteRuleSubtreeStream(adaptor,"rule unaryExpression");
		RewriteRuleSubtreeStream stream_declarationSpecifiers=new RewriteRuleSubtreeStream(adaptor,"rule declarationSpecifiers");
		RewriteRuleSubtreeStream stream_typeSpecifier=new RewriteRuleSubtreeStream(adaptor,"rule typeSpecifier");

		try {
			// org/eclipse/tracecompass/ctf/parser/CTFParser.g:635:3: (left= unaryExpression (assignment= ASSIGNMENT right1= unaryExpression -> ^( CTF_EXPRESSION_VAL ^( CTF_LEFT $left) ^( CTF_RIGHT $right1) ) |type_assignment= TYPE_ASSIGNMENT right2= typeSpecifier -> ^( CTF_EXPRESSION_TYPE ^( CTF_LEFT $left) ^( CTF_RIGHT ^( TYPE_SPECIFIER_LIST $right2) ) ) ) | ( declarationSpecifiers {...}? declaratorList ) -> ^( TYPEDEF declaratorList declarationSpecifiers ) | typealiasDecl )
			int alt58=3;
			switch ( input.LA(1) ) {
			case IDENTIFIER:
				{
				int LA58_1 = input.LA(2);
				if ( ((LA58_1 >= ARROW && LA58_1 <= ASSIGNMENT)||LA58_1==DOT||LA58_1==OPENBRAC||LA58_1==TYPE_ASSIGNMENT) ) {
					alt58=1;
				}
				else if ( (LA58_1==BOOLTOK||LA58_1==CHARTOK||(LA58_1 >= COMPLEXTOK && LA58_1 <= CONSTTOK)||LA58_1==DOUBLETOK||LA58_1==ENUMTOK||(LA58_1 >= FLOATINGPOINTTOK && LA58_1 <= FLOATTOK)||(LA58_1 >= IDENTIFIER && LA58_1 <= IMAGINARYTOK)||LA58_1==INTEGERTOK||LA58_1==INTTOK||LA58_1==LONGTOK||LA58_1==POINTER||LA58_1==SHORTTOK||LA58_1==SIGNEDTOK||LA58_1==STRINGTOK||LA58_1==STRUCTTOK||LA58_1==TYPEDEFTOK||(LA58_1 >= UNSIGNEDTOK && LA58_1 <= VOIDTOK)) && (( inTypealiasAlias() || isTypeName(input.LT(1).getText()) ))) {
					alt58=2;
				}

				}
				break;
			case ALIGNTOK:
			case CALLSITETOK:
			case CHARACTER_LITERAL:
			case CLOCKTOK:
			case DECIMAL_LITERAL:
			case ENVTOK:
			case EVENTTOK:
			case HEX_LITERAL:
			case OCTAL_LITERAL:
			case SIGN:
			case STREAMTOK:
			case STRING_LITERAL:
			case TRACETOK:
				{
				alt58=1;
				}
				break;
			case SIGNEDTOK:
				{
				switch ( input.LA(2) ) {
				case ARROW:
				case ASSIGNMENT:
				case DOT:
				case OPENBRAC:
				case TYPE_ASSIGNMENT:
					{
					alt58=1;
					}
					break;
				case BOOLTOK:
				case CHARTOK:
				case COMPLEXTOK:
				case CONSTTOK:
				case DOUBLETOK:
				case ENUMTOK:
				case FLOATINGPOINTTOK:
				case FLOATTOK:
				case IDENTIFIER:
				case IMAGINARYTOK:
				case INTEGERTOK:
				case INTTOK:
				case LONGTOK:
				case POINTER:
				case SHORTTOK:
				case SIGNEDTOK:
				case STRINGTOK:
				case STRUCTTOK:
				case UNSIGNEDTOK:
				case VARIANTTOK:
				case VOIDTOK:
					{
					alt58=2;
					}
					break;
				case TYPEDEFTOK:
					{
					alt58=2;
					}
					break;
				default:
					if (state.backtracking>0) {state.failed=true; return retval;}
					int nvaeMark = input.mark();
					try {
						input.consume();
						NoViableAltException nvae =
							new NoViableAltException("", 58, 3, input);
						throw nvae;
					} finally {
						input.rewind(nvaeMark);
					}
				}
				}
				break;
			case BOOLTOK:
			case CHARTOK:
			case COMPLEXTOK:
			case CONSTTOK:
			case DOUBLETOK:
			case ENUMTOK:
			case FLOATINGPOINTTOK:
			case FLOATTOK:
			case IMAGINARYTOK:
			case INTEGERTOK:
			case INTTOK:
			case LONGTOK:
			case SHORTTOK:
			case STRUCTTOK:
			case TYPEDEFTOK:
			case UNSIGNEDTOK:
			case VARIANTTOK:
			case VOIDTOK:
				{
				alt58=2;
				}
				break;
			case STRINGTOK:
				{
				switch ( input.LA(2) ) {
				case ARROW:
				case ASSIGNMENT:
				case DOT:
				case OPENBRAC:
				case TYPE_ASSIGNMENT:
					{
					alt58=1;
					}
					break;
				case BOOLTOK:
				case CHARTOK:
				case COMPLEXTOK:
				case CONSTTOK:
				case DOUBLETOK:
				case ENUMTOK:
				case FLOATINGPOINTTOK:
				case FLOATTOK:
				case IDENTIFIER:
				case IMAGINARYTOK:
				case INTEGERTOK:
				case INTTOK:
				case LCURL:
				case LONGTOK:
				case POINTER:
				case SHORTTOK:
				case SIGNEDTOK:
				case STRINGTOK:
				case STRUCTTOK:
				case UNSIGNEDTOK:
				case VARIANTTOK:
				case VOIDTOK:
					{
					alt58=2;
					}
					break;
				case TYPEDEFTOK:
					{
					alt58=2;
					}
					break;
				default:
					if (state.backtracking>0) {state.failed=true; return retval;}
					int nvaeMark = input.mark();
					try {
						input.consume();
						NoViableAltException nvae =
							new NoViableAltException("", 58, 5, input);
						throw nvae;
					} finally {
						input.rewind(nvaeMark);
					}
				}
				}
				break;
			case TYPEALIASTOK:
				{
				alt58=3;
				}
				break;
			default:
				if (state.backtracking>0) {state.failed=true; return retval;}
				NoViableAltException nvae =
					new NoViableAltException("", 58, 0, input);
				throw nvae;
			}
			switch (alt58) {
				case 1 :
					// org/eclipse/tracecompass/ctf/parser/CTFParser.g:635:5: left= unaryExpression (assignment= ASSIGNMENT right1= unaryExpression -> ^( CTF_EXPRESSION_VAL ^( CTF_LEFT $left) ^( CTF_RIGHT $right1) ) |type_assignment= TYPE_ASSIGNMENT right2= typeSpecifier -> ^( CTF_EXPRESSION_TYPE ^( CTF_LEFT $left) ^( CTF_RIGHT ^( TYPE_SPECIFIER_LIST $right2) ) ) )
					{
					pushFollow(FOLLOW_unaryExpression_in_ctfAssignmentExpression3376);
					left=unaryExpression();
					state._fsp--;
					if (state.failed) return retval;
					if ( state.backtracking==0 ) stream_unaryExpression.add(left.getTree());
					// org/eclipse/tracecompass/ctf/parser/CTFParser.g:636:7: (assignment= ASSIGNMENT right1= unaryExpression -> ^( CTF_EXPRESSION_VAL ^( CTF_LEFT $left) ^( CTF_RIGHT $right1) ) |type_assignment= TYPE_ASSIGNMENT right2= typeSpecifier -> ^( CTF_EXPRESSION_TYPE ^( CTF_LEFT $left) ^( CTF_RIGHT ^( TYPE_SPECIFIER_LIST $right2) ) ) )
					int alt57=2;
					int LA57_0 = input.LA(1);
					if ( (LA57_0==ASSIGNMENT) ) {
						alt57=1;
					}
					else if ( (LA57_0==TYPE_ASSIGNMENT) ) {
						alt57=2;
					}

					else {
						if (state.backtracking>0) {state.failed=true; return retval;}
						NoViableAltException nvae =
							new NoViableAltException("", 57, 0, input);
						throw nvae;
					}

					switch (alt57) {
						case 1 :
							// org/eclipse/tracecompass/ctf/parser/CTFParser.g:636:9: assignment= ASSIGNMENT right1= unaryExpression
							{
							assignment=(Token)match(input,ASSIGNMENT,FOLLOW_ASSIGNMENT_in_ctfAssignmentExpression3388); if (state.failed) return retval; 
							if ( state.backtracking==0 ) stream_ASSIGNMENT.add(assignment);

							pushFollow(FOLLOW_unaryExpression_in_ctfAssignmentExpression3392);
							right1=unaryExpression();
							state._fsp--;
							if (state.failed) return retval;
							if ( state.backtracking==0 ) stream_unaryExpression.add(right1.getTree());
							// AST REWRITE
							// elements: right1, left
							// token labels: 
							// rule labels: retval, left, right1
							// token list labels: 
							// rule list labels: 
							// wildcard labels: 
							if ( state.backtracking==0 ) {
							retval.tree = root_0;
							RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.getTree():null);
							RewriteRuleSubtreeStream stream_left=new RewriteRuleSubtreeStream(adaptor,"rule left",left!=null?left.getTree():null);
							RewriteRuleSubtreeStream stream_right1=new RewriteRuleSubtreeStream(adaptor,"rule right1",right1!=null?right1.getTree():null);

							root_0 = (CommonTree)adaptor.nil();
							// 637:11: -> ^( CTF_EXPRESSION_VAL ^( CTF_LEFT $left) ^( CTF_RIGHT $right1) )
							{
								// org/eclipse/tracecompass/ctf/parser/CTFParser.g:637:14: ^( CTF_EXPRESSION_VAL ^( CTF_LEFT $left) ^( CTF_RIGHT $right1) )
								{
								CommonTree root_1 = (CommonTree)adaptor.nil();
								root_1 = (CommonTree)adaptor.becomeRoot((CommonTree)adaptor.create(CTF_EXPRESSION_VAL, "CTF_EXPRESSION_VAL"), root_1);
								// org/eclipse/tracecompass/ctf/parser/CTFParser.g:638:18: ^( CTF_LEFT $left)
								{
								CommonTree root_2 = (CommonTree)adaptor.nil();
								root_2 = (CommonTree)adaptor.becomeRoot((CommonTree)adaptor.create(CTF_LEFT, "CTF_LEFT"), root_2);
								adaptor.addChild(root_2, stream_left.nextTree());
								adaptor.addChild(root_1, root_2);
								}

								// org/eclipse/tracecompass/ctf/parser/CTFParser.g:639:18: ^( CTF_RIGHT $right1)
								{
								CommonTree root_2 = (CommonTree)adaptor.nil();
								root_2 = (CommonTree)adaptor.becomeRoot((CommonTree)adaptor.create(CTF_RIGHT, "CTF_RIGHT"), root_2);
								adaptor.addChild(root_2, stream_right1.nextTree());
								adaptor.addChild(root_1, root_2);
								}

								adaptor.addChild(root_0, root_1);
								}

							}


							retval.tree = root_0;
							}

							}
							break;
						case 2 :
							// org/eclipse/tracecompass/ctf/parser/CTFParser.g:640:9: type_assignment= TYPE_ASSIGNMENT right2= typeSpecifier
							{
							type_assignment=(Token)match(input,TYPE_ASSIGNMENT,FOLLOW_TYPE_ASSIGNMENT_in_ctfAssignmentExpression3468); if (state.failed) return retval; 
							if ( state.backtracking==0 ) stream_TYPE_ASSIGNMENT.add(type_assignment);

							pushFollow(FOLLOW_typeSpecifier_in_ctfAssignmentExpression3472);
							right2=typeSpecifier();
							state._fsp--;
							if (state.failed) return retval;
							if ( state.backtracking==0 ) stream_typeSpecifier.add(right2.getTree());
							// AST REWRITE
							// elements: right2, left
							// token labels: 
							// rule labels: retval, left, right2
							// token list labels: 
							// rule list labels: 
							// wildcard labels: 
							if ( state.backtracking==0 ) {
							retval.tree = root_0;
							RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.getTree():null);
							RewriteRuleSubtreeStream stream_left=new RewriteRuleSubtreeStream(adaptor,"rule left",left!=null?left.getTree():null);
							RewriteRuleSubtreeStream stream_right2=new RewriteRuleSubtreeStream(adaptor,"rule right2",right2!=null?right2.getTree():null);

							root_0 = (CommonTree)adaptor.nil();
							// 641:11: -> ^( CTF_EXPRESSION_TYPE ^( CTF_LEFT $left) ^( CTF_RIGHT ^( TYPE_SPECIFIER_LIST $right2) ) )
							{
								// org/eclipse/tracecompass/ctf/parser/CTFParser.g:641:14: ^( CTF_EXPRESSION_TYPE ^( CTF_LEFT $left) ^( CTF_RIGHT ^( TYPE_SPECIFIER_LIST $right2) ) )
								{
								CommonTree root_1 = (CommonTree)adaptor.nil();
								root_1 = (CommonTree)adaptor.becomeRoot((CommonTree)adaptor.create(CTF_EXPRESSION_TYPE, "CTF_EXPRESSION_TYPE"), root_1);
								// org/eclipse/tracecompass/ctf/parser/CTFParser.g:642:18: ^( CTF_LEFT $left)
								{
								CommonTree root_2 = (CommonTree)adaptor.nil();
								root_2 = (CommonTree)adaptor.becomeRoot((CommonTree)adaptor.create(CTF_LEFT, "CTF_LEFT"), root_2);
								adaptor.addChild(root_2, stream_left.nextTree());
								adaptor.addChild(root_1, root_2);
								}

								// org/eclipse/tracecompass/ctf/parser/CTFParser.g:643:18: ^( CTF_RIGHT ^( TYPE_SPECIFIER_LIST $right2) )
								{
								CommonTree root_2 = (CommonTree)adaptor.nil();
								root_2 = (CommonTree)adaptor.becomeRoot((CommonTree)adaptor.create(CTF_RIGHT, "CTF_RIGHT"), root_2);
								// org/eclipse/tracecompass/ctf/parser/CTFParser.g:643:30: ^( TYPE_SPECIFIER_LIST $right2)
								{
								CommonTree root_3 = (CommonTree)adaptor.nil();
								root_3 = (CommonTree)adaptor.becomeRoot((CommonTree)adaptor.create(TYPE_SPECIFIER_LIST, "TYPE_SPECIFIER_LIST"), root_3);
								adaptor.addChild(root_3, stream_right2.nextTree());
								adaptor.addChild(root_2, root_3);
								}

								adaptor.addChild(root_1, root_2);
								}

								adaptor.addChild(root_0, root_1);
								}

							}


							retval.tree = root_0;
							}

							}
							break;

					}

					}
					break;
				case 2 :
					// org/eclipse/tracecompass/ctf/parser/CTFParser.g:645:5: ( declarationSpecifiers {...}? declaratorList )
					{
					// org/eclipse/tracecompass/ctf/parser/CTFParser.g:645:5: ( declarationSpecifiers {...}? declaratorList )
					// org/eclipse/tracecompass/ctf/parser/CTFParser.g:645:6: declarationSpecifiers {...}? declaratorList
					{
					pushFollow(FOLLOW_declarationSpecifiers_in_ctfAssignmentExpression3555);
					declarationSpecifiers176=declarationSpecifiers();
					state._fsp--;
					if (state.failed) return retval;
					if ( state.backtracking==0 ) stream_declarationSpecifiers.add(declarationSpecifiers176.getTree());
					if ( !((inTypedef())) ) {
						if (state.backtracking>0) {state.failed=true; return retval;}
						throw new FailedPredicateException(input, "ctfAssignmentExpression", "inTypedef()");
					}
					pushFollow(FOLLOW_declaratorList_in_ctfAssignmentExpression3559);
					declaratorList177=declaratorList();
					state._fsp--;
					if (state.failed) return retval;
					if ( state.backtracking==0 ) stream_declaratorList.add(declaratorList177.getTree());
					}

					// AST REWRITE
					// elements: declarationSpecifiers, declaratorList
					// token labels: 
					// rule labels: retval
					// token list labels: 
					// rule list labels: 
					// wildcard labels: 
					if ( state.backtracking==0 ) {
					retval.tree = root_0;
					RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.getTree():null);

					root_0 = (CommonTree)adaptor.nil();
					// 646:7: -> ^( TYPEDEF declaratorList declarationSpecifiers )
					{
						// org/eclipse/tracecompass/ctf/parser/CTFParser.g:646:10: ^( TYPEDEF declaratorList declarationSpecifiers )
						{
						CommonTree root_1 = (CommonTree)adaptor.nil();
						root_1 = (CommonTree)adaptor.becomeRoot((CommonTree)adaptor.create(TYPEDEF, "TYPEDEF"), root_1);
						adaptor.addChild(root_1, stream_declaratorList.nextTree());
						adaptor.addChild(root_1, stream_declarationSpecifiers.nextTree());
						adaptor.addChild(root_0, root_1);
						}

					}


					retval.tree = root_0;
					}

					}
					break;
				case 3 :
					// org/eclipse/tracecompass/ctf/parser/CTFParser.g:647:5: typealiasDecl
					{
					root_0 = (CommonTree)adaptor.nil();


					pushFollow(FOLLOW_typealiasDecl_in_ctfAssignmentExpression3582);
					typealiasDecl178=typealiasDecl();
					state._fsp--;
					if (state.failed) return retval;
					if ( state.backtracking==0 ) adaptor.addChild(root_0, typealiasDecl178.getTree());

					}
					break;

			}
			retval.stop = input.LT(-1);

			if ( state.backtracking==0 ) {
			retval.tree = (CommonTree)adaptor.rulePostProcessing(root_0);
			adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
			}
			if ( state.backtracking==0 ) {
			    if (inTypedef()) {
			        typedefOff();
			    }
			}
		}

		    catch (RecognitionException e) {
		        throw e;
		    }

		finally {
			// do for sure before leaving
		}
		return retval;
	}
	// $ANTLR end "ctfAssignmentExpression"

	// $ANTLR start synpred1_CTFParser
	public final void synpred1_CTFParser_fragment() throws RecognitionException {
		// org/eclipse/tracecompass/ctf/parser/CTFParser.g:209:5: ( IDENTIFIER )
		// org/eclipse/tracecompass/ctf/parser/CTFParser.g:209:6: IDENTIFIER
		{
		match(input,IDENTIFIER,FOLLOW_IDENTIFIER_in_synpred1_CTFParser560); if (state.failed) return;

		}

	}
	// $ANTLR end synpred1_CTFParser

	// $ANTLR start synpred2_CTFParser
	public final void synpred2_CTFParser_fragment() throws RecognitionException {
		// org/eclipse/tracecompass/ctf/parser/CTFParser.g:211:5: ( ctfKeyword )
		// org/eclipse/tracecompass/ctf/parser/CTFParser.g:211:6: ctfKeyword
		{
		pushFollow(FOLLOW_ctfKeyword_in_synpred2_CTFParser586);
		ctfKeyword();
		state._fsp--;
		if (state.failed) return;

		}

	}
	// $ANTLR end synpred2_CTFParser

	// $ANTLR start synpred3_CTFParser
	public final void synpred3_CTFParser_fragment() throws RecognitionException {
		// org/eclipse/tracecompass/ctf/parser/CTFParser.g:212:5: ( STRING_LITERAL )
		// org/eclipse/tracecompass/ctf/parser/CTFParser.g:212:6: STRING_LITERAL
		{
		match(input,STRING_LITERAL,FOLLOW_STRING_LITERAL_in_synpred3_CTFParser606); if (state.failed) return;

		}

	}
	// $ANTLR end synpred3_CTFParser

	// Delegated rules

	public final boolean synpred2_CTFParser() {
		state.backtracking++;
		int start = input.mark();
		try {
			synpred2_CTFParser_fragment(); // can never throw exception
		} catch (RecognitionException re) {
			System.err.println("impossible: "+re);
		}
		boolean success = !state.failed;
		input.rewind(start);
		state.backtracking--;
		state.failed=false;
		return success;
	}
	public final boolean synpred1_CTFParser() {
		state.backtracking++;
		int start = input.mark();
		try {
			synpred1_CTFParser_fragment(); // can never throw exception
		} catch (RecognitionException re) {
			System.err.println("impossible: "+re);
		}
		boolean success = !state.failed;
		input.rewind(start);
		state.backtracking--;
		state.failed=false;
		return success;
	}
	public final boolean synpred3_CTFParser() {
		state.backtracking++;
		int start = input.mark();
		try {
			synpred3_CTFParser_fragment(); // can never throw exception
		} catch (RecognitionException re) {
			System.err.println("impossible: "+re);
		}
		boolean success = !state.failed;
		input.rewind(start);
		state.backtracking--;
		state.failed=false;
		return success;
	}


	protected DFA23 dfa23 = new DFA23(this);
	static final String DFA23_eotS =
		"\10\uffff";
	static final String DFA23_eofS =
		"\10\uffff";
	static final String DFA23_minS =
		"\1\46\1\24\1\0\1\24\1\0\2\uffff\1\0";
	static final String DFA23_maxS =
		"\2\72\1\0\1\72\1\0\2\uffff\1\0";
	static final String DFA23_acceptS =
		"\5\uffff\1\1\1\2\1\uffff";
	static final String DFA23_specialS =
		"\2\uffff\1\2\1\uffff\1\1\2\uffff\1\0}>";
	static final String[] DFA23_transitionS = {
			"\1\2\23\uffff\1\1",
			"\1\3\21\uffff\1\4\23\uffff\1\1",
			"\1\uffff",
			"\1\3\21\uffff\1\7\23\uffff\1\1",
			"\1\uffff",
			"",
			"",
			"\1\uffff"
	};

	static final short[] DFA23_eot = DFA.unpackEncodedString(DFA23_eotS);
	static final short[] DFA23_eof = DFA.unpackEncodedString(DFA23_eofS);
	static final char[] DFA23_min = DFA.unpackEncodedStringToUnsignedChars(DFA23_minS);
	static final char[] DFA23_max = DFA.unpackEncodedStringToUnsignedChars(DFA23_maxS);
	static final short[] DFA23_accept = DFA.unpackEncodedString(DFA23_acceptS);
	static final short[] DFA23_special = DFA.unpackEncodedString(DFA23_specialS);
	static final short[][] DFA23_transition;

	static {
		int numStates = DFA23_transitionS.length;
		DFA23_transition = new short[numStates][];
		for (int i=0; i<numStates; i++) {
			DFA23_transition[i] = DFA.unpackEncodedString(DFA23_transitionS[i]);
		}
	}

	protected class DFA23 extends DFA {

		public DFA23(BaseRecognizer recognizer) {
			this.recognizer = recognizer;
			this.decisionNumber = 23;
			this.eot = DFA23_eot;
			this.eof = DFA23_eof;
			this.min = DFA23_min;
			this.max = DFA23_max;
			this.accept = DFA23_accept;
			this.special = DFA23_special;
			this.transition = DFA23_transition;
		}
		@Override
		public String getDescription() {
			return "368:10: ({...}? => declaratorList -> ^( TYPEDEF declaratorList declarationSpecifiers ) | structOrVariantDeclaratorList -> ^( SV_DECLARATION declarationSpecifiers structOrVariantDeclaratorList ) )";
		}
		@Override
		public int specialStateTransition(int s, IntStream _input) throws NoViableAltException {
			TokenStream input = (TokenStream)_input;
			int _s = s;
			switch ( s ) {
					case 0 : 
						int LA23_7 = input.LA(1);
						 
						int index23_7 = input.index();
						input.rewind();
						s = -1;
						if ( ((inTypedef())) ) {s = 5;}
						else if ( (true) ) {s = 6;}
						 
						input.seek(index23_7);
						if ( s>=0 ) return s;
						break;

					case 1 : 
						int LA23_4 = input.LA(1);
						 
						int index23_4 = input.index();
						input.rewind();
						s = -1;
						if ( ((inTypedef())) ) {s = 5;}
						else if ( (true) ) {s = 6;}
						 
						input.seek(index23_4);
						if ( s>=0 ) return s;
						break;

					case 2 : 
						int LA23_2 = input.LA(1);
						 
						int index23_2 = input.index();
						input.rewind();
						s = -1;
						if ( ((inTypedef())) ) {s = 5;}
						else if ( (true) ) {s = 6;}
						 
						input.seek(index23_2);
						if ( s>=0 ) return s;
						break;
			}
			if (state.backtracking>0) {state.failed=true; return -1;}
			NoViableAltException nvae =
				new NoViableAltException(getDescription(), 23, _s, input);
			error(nvae);
			throw nvae;
		}
	}

	public static final BitSet FOLLOW_declaration_in_parse449 = new BitSet(new long[]{0x40004AC1DA182B00L,0x000000000001CE95L});
	public static final BitSet FOLLOW_EOF_in_parse452 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_SIGN_in_numberLiteral474 = new BitSet(new long[]{0x8040001000200000L});
	public static final BitSet FOLLOW_HEX_LITERAL_in_numberLiteral485 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_DECIMAL_LITERAL_in_numberLiteral506 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_OCTAL_LITERAL_in_numberLiteral527 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_IDENTIFIER_in_primaryExpression565 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_ctfKeyword_in_primaryExpression591 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_STRING_LITERAL_in_primaryExpression611 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_numberLiteral_in_primaryExpression636 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_enumConstant_in_primaryExpression642 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_CHARACTER_LITERAL_in_primaryExpression648 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_OPENBRAC_in_postfixExpressionSuffix661 = new BitSet(new long[]{0x8040005050202610L,0x0000000000000255L});
	public static final BitSet FOLLOW_unaryExpression_in_postfixExpressionSuffix663 = new BitSet(new long[]{0x0000000000004000L});
	public static final BitSet FOLLOW_CLOSEBRAC_in_postfixExpressionSuffix665 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_DOT_in_postfixExpressionSuffix675 = new BitSet(new long[]{0x0000004000000000L});
	public static final BitSet FOLLOW_ARROW_in_postfixExpressionSuffix681 = new BitSet(new long[]{0x0000004000000000L});
	public static final BitSet FOLLOW_IDENTIFIER_in_postfixExpressionSuffix684 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_primaryExpression_in_postfixExpression716 = new BitSet(new long[]{0x0200000000800022L});
	public static final BitSet FOLLOW_postfixExpressionSuffix_in_postfixExpression718 = new BitSet(new long[]{0x0200000000800022L});
	public static final BitSet FOLLOW_ctfSpecifierHead_in_postfixExpression725 = new BitSet(new long[]{0x0200000000800020L});
	public static final BitSet FOLLOW_postfixExpressionSuffix_in_postfixExpression727 = new BitSet(new long[]{0x0200000000800022L});
	public static final BitSet FOLLOW_postfixExpression_in_unaryExpression743 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_STRING_LITERAL_in_enumConstant760 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_IDENTIFIER_in_enumConstant774 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_ctfKeyword_in_enumConstant788 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_declarationSpecifiers_in_declaration816 = new BitSet(new long[]{0x0400004000000000L,0x0000000000000100L});
	public static final BitSet FOLLOW_declaratorList_in_declaration818 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000100L});
	public static final BitSet FOLLOW_TERM_in_declaration821 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_ctfSpecifier_in_declaration889 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000100L});
	public static final BitSet FOLLOW_TERM_in_declaration891 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_storageClassSpecifier_in_declarationSpecifiers929 = new BitSet(new long[]{0x40004AC18A180902L,0x000000000001C891L});
	public static final BitSet FOLLOW_typeQualifier_in_declarationSpecifiers939 = new BitSet(new long[]{0x40004AC18A180902L,0x000000000001C891L});
	public static final BitSet FOLLOW_typeSpecifier_in_declarationSpecifiers949 = new BitSet(new long[]{0x40004AC18A180902L,0x000000000001C891L});
	public static final BitSet FOLLOW_declarator_in_declaratorList979 = new BitSet(new long[]{0x2000000000000002L});
	public static final BitSet FOLLOW_SEPARATOR_in_declaratorList982 = new BitSet(new long[]{0x0400004000000000L});
	public static final BitSet FOLLOW_declarator_in_declaratorList984 = new BitSet(new long[]{0x2000000000000002L});
	public static final BitSet FOLLOW_abstractDeclarator_in_abstractDeclaratorList1014 = new BitSet(new long[]{0x2000000000000002L});
	public static final BitSet FOLLOW_SEPARATOR_in_abstractDeclaratorList1017 = new BitSet(new long[]{0x0400804000000000L});
	public static final BitSet FOLLOW_abstractDeclarator_in_abstractDeclaratorList1019 = new BitSet(new long[]{0x2000000000000002L});
	public static final BitSet FOLLOW_TYPEDEFTOK_in_storageClassSpecifier1049 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_FLOATTOK_in_typeSpecifier1065 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_INTTOK_in_typeSpecifier1071 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_LONGTOK_in_typeSpecifier1077 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_SHORTTOK_in_typeSpecifier1083 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_SIGNEDTOK_in_typeSpecifier1089 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_UNSIGNEDTOK_in_typeSpecifier1095 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_CHARTOK_in_typeSpecifier1101 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_DOUBLETOK_in_typeSpecifier1107 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_VOIDTOK_in_typeSpecifier1113 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_BOOLTOK_in_typeSpecifier1119 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_COMPLEXTOK_in_typeSpecifier1125 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_IMAGINARYTOK_in_typeSpecifier1131 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_structSpecifier_in_typeSpecifier1137 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_variantSpecifier_in_typeSpecifier1143 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_enumSpecifier_in_typeSpecifier1149 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_ctfTypeSpecifier_in_typeSpecifier1155 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_typedefName_in_typeSpecifier1165 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_CONSTTOK_in_typeQualifier1178 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_ALIGNTOK_in_alignAttribute1191 = new BitSet(new long[]{0x0000800000000000L});
	public static final BitSet FOLLOW_LPAREN_in_alignAttribute1193 = new BitSet(new long[]{0x8040005050202610L,0x0000000000000255L});
	public static final BitSet FOLLOW_unaryExpression_in_alignAttribute1195 = new BitSet(new long[]{0x1000000000000000L});
	public static final BitSet FOLLOW_RPAREN_in_alignAttribute1197 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_LCURL_in_structBody1231 = new BitSet(new long[]{0x48004AC18A180900L,0x000000000001CC91L});
	public static final BitSet FOLLOW_structOrVariantDeclarationList_in_structBody1233 = new BitSet(new long[]{0x0800000000000000L});
	public static final BitSet FOLLOW_RCURL_in_structBody1236 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_STRUCTTOK_in_structSpecifier1264 = new BitSet(new long[]{0x0000104000000000L});
	public static final BitSet FOLLOW_structName_in_structSpecifier1289 = new BitSet(new long[]{0x0000100000000012L});
	public static final BitSet FOLLOW_alignAttribute_in_structSpecifier1311 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_structBody_in_structSpecifier1347 = new BitSet(new long[]{0x0000000000000012L});
	public static final BitSet FOLLOW_alignAttribute_in_structSpecifier1378 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_structBody_in_structSpecifier1494 = new BitSet(new long[]{0x0000000000000012L});
	public static final BitSet FOLLOW_alignAttribute_in_structSpecifier1512 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_IDENTIFIER_in_structName1578 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_structOrVariantDeclaration_in_structOrVariantDeclarationList1599 = new BitSet(new long[]{0x40004AC18A180902L,0x000000000001CC91L});
	public static final BitSet FOLLOW_declarationSpecifiers_in_structOrVariantDeclaration1632 = new BitSet(new long[]{0x0400004000000000L});
	public static final BitSet FOLLOW_declaratorList_in_structOrVariantDeclaration1673 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000100L});
	public static final BitSet FOLLOW_structOrVariantDeclaratorList_in_structOrVariantDeclaration1713 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000100L});
	public static final BitSet FOLLOW_typealiasDecl_in_structOrVariantDeclaration1772 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000100L});
	public static final BitSet FOLLOW_TERM_in_structOrVariantDeclaration1784 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_typeQualifier_in_specifierQualifierList1798 = new BitSet(new long[]{0x40004AC18A180902L,0x000000000001C091L});
	public static final BitSet FOLLOW_typeSpecifier_in_specifierQualifierList1802 = new BitSet(new long[]{0x40004AC18A180902L,0x000000000001C091L});
	public static final BitSet FOLLOW_structOrVariantDeclarator_in_structOrVariantDeclaratorList1835 = new BitSet(new long[]{0x2000000000000002L});
	public static final BitSet FOLLOW_SEPARATOR_in_structOrVariantDeclaratorList1838 = new BitSet(new long[]{0x0400004000000000L});
	public static final BitSet FOLLOW_structOrVariantDeclarator_in_structOrVariantDeclaratorList1840 = new BitSet(new long[]{0x2000000000000002L});
	public static final BitSet FOLLOW_declarator_in_structOrVariantDeclarator1879 = new BitSet(new long[]{0x0000000000008002L});
	public static final BitSet FOLLOW_COLON_in_structOrVariantDeclarator1882 = new BitSet(new long[]{0x8040001000200000L});
	public static final BitSet FOLLOW_numberLiteral_in_structOrVariantDeclarator1884 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_VARIANTTOK_in_variantSpecifier1908 = new BitSet(new long[]{0x0001104000000000L});
	public static final BitSet FOLLOW_variantName_in_variantSpecifier1926 = new BitSet(new long[]{0x0001100000000000L});
	public static final BitSet FOLLOW_variantTag_in_variantSpecifier1956 = new BitSet(new long[]{0x0000100000000002L});
	public static final BitSet FOLLOW_variantBody_in_variantSpecifier1982 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_variantBody_in_variantSpecifier2050 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_variantTag_in_variantSpecifier2071 = new BitSet(new long[]{0x0000100000000000L});
	public static final BitSet FOLLOW_variantBody_in_variantSpecifier2073 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_variantBody_in_variantSpecifier2080 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_IDENTIFIER_in_variantName2112 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_LCURL_in_variantBody2143 = new BitSet(new long[]{0x40004AC18A180900L,0x000000000001CC91L});
	public static final BitSet FOLLOW_structOrVariantDeclarationList_in_variantBody2145 = new BitSet(new long[]{0x0800000000000000L});
	public static final BitSet FOLLOW_RCURL_in_variantBody2147 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_LT_in_variantTag2174 = new BitSet(new long[]{0x0000004000000000L});
	public static final BitSet FOLLOW_IDENTIFIER_in_variantTag2176 = new BitSet(new long[]{0x0000000200000000L});
	public static final BitSet FOLLOW_GT_in_variantTag2178 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_ENUMTOK_in_enumSpecifier2199 = new BitSet(new long[]{0x0000104000008000L});
	public static final BitSet FOLLOW_enumName_in_enumSpecifier2238 = new BitSet(new long[]{0x0000100000008002L});
	public static final BitSet FOLLOW_enumContainerType_in_enumSpecifier2270 = new BitSet(new long[]{0x0000100000000000L});
	public static final BitSet FOLLOW_enumBody_in_enumSpecifier2272 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_enumBody_in_enumSpecifier2302 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_enumContainerType_in_enumSpecifier2394 = new BitSet(new long[]{0x0000100000000000L});
	public static final BitSet FOLLOW_enumBody_in_enumSpecifier2396 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_enumBody_in_enumSpecifier2420 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_IDENTIFIER_in_enumName2464 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_LCURL_in_enumBody2485 = new BitSet(new long[]{0x0000004040000010L,0x0000000000000051L});
	public static final BitSet FOLLOW_enumeratorList_in_enumBody2487 = new BitSet(new long[]{0x2800000000000000L});
	public static final BitSet FOLLOW_SEPARATOR_in_enumBody2489 = new BitSet(new long[]{0x0800000000000000L});
	public static final BitSet FOLLOW_RCURL_in_enumBody2492 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_COLON_in_enumContainerType2513 = new BitSet(new long[]{0x40004AC18A180900L,0x000000000001C891L});
	public static final BitSet FOLLOW_declarationSpecifiers_in_enumContainerType2515 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_enumerator_in_enumeratorList2536 = new BitSet(new long[]{0x2000000000000002L});
	public static final BitSet FOLLOW_SEPARATOR_in_enumeratorList2539 = new BitSet(new long[]{0x0000004040000010L,0x0000000000000051L});
	public static final BitSet FOLLOW_enumerator_in_enumeratorList2541 = new BitSet(new long[]{0x2000000000000002L});
	public static final BitSet FOLLOW_enumConstant_in_enumerator2567 = new BitSet(new long[]{0x0000000000000042L});
	public static final BitSet FOLLOW_enumeratorValue_in_enumerator2569 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_ASSIGNMENT_in_enumeratorValue2583 = new BitSet(new long[]{0x8040005050202610L,0x0000000000000255L});
	public static final BitSet FOLLOW_unaryExpression_in_enumeratorValue2587 = new BitSet(new long[]{0x0000000004000002L});
	public static final BitSet FOLLOW_ELIPSES_in_enumeratorValue2626 = new BitSet(new long[]{0x8040005050202610L,0x0000000000000255L});
	public static final BitSet FOLLOW_unaryExpression_in_enumeratorValue2630 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_pointer_in_declarator2673 = new BitSet(new long[]{0x0400004000000000L});
	public static final BitSet FOLLOW_directDeclarator_in_declarator2676 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_IDENTIFIER_in_directDeclarator2714 = new BitSet(new long[]{0x0200000000000002L});
	public static final BitSet FOLLOW_directDeclaratorSuffix_in_directDeclarator2754 = new BitSet(new long[]{0x0200000000000002L});
	public static final BitSet FOLLOW_OPENBRAC_in_directDeclaratorSuffix2768 = new BitSet(new long[]{0x8040005050202610L,0x0000000000000255L});
	public static final BitSet FOLLOW_directDeclaratorLength_in_directDeclaratorSuffix2770 = new BitSet(new long[]{0x0000000000004000L});
	public static final BitSet FOLLOW_CLOSEBRAC_in_directDeclaratorSuffix2772 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_unaryExpression_in_directDeclaratorLength2800 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_pointer_in_abstractDeclarator2813 = new BitSet(new long[]{0x0400804000000002L});
	public static final BitSet FOLLOW_directAbstractDeclarator_in_abstractDeclarator2816 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_directAbstractDeclarator_in_abstractDeclarator2841 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_IDENTIFIER_in_directAbstractDeclarator2878 = new BitSet(new long[]{0x0200000000000002L});
	public static final BitSet FOLLOW_LPAREN_in_directAbstractDeclarator2889 = new BitSet(new long[]{0x0400804000000000L});
	public static final BitSet FOLLOW_abstractDeclarator_in_directAbstractDeclarator2891 = new BitSet(new long[]{0x1000000000000000L});
	public static final BitSet FOLLOW_RPAREN_in_directAbstractDeclarator2893 = new BitSet(new long[]{0x0200000000000002L});
	public static final BitSet FOLLOW_OPENBRAC_in_directAbstractDeclarator2908 = new BitSet(new long[]{0x8040005050206610L,0x0000000000000255L});
	public static final BitSet FOLLOW_unaryExpression_in_directAbstractDeclarator2910 = new BitSet(new long[]{0x0000000000004000L});
	public static final BitSet FOLLOW_CLOSEBRAC_in_directAbstractDeclarator2913 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_POINTER_in_pointer2931 = new BitSet(new long[]{0x0000000000100002L});
	public static final BitSet FOLLOW_typeQualifierList_in_pointer2933 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_typeQualifier_in_typeQualifierList2956 = new BitSet(new long[]{0x0000000000100002L});
	public static final BitSet FOLLOW_IDENTIFIER_in_typedefName2972 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_declarationSpecifiers_in_typealiasTarget2989 = new BitSet(new long[]{0x0400804000000002L});
	public static final BitSet FOLLOW_abstractDeclaratorList_in_typealiasTarget2991 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_abstractDeclaratorList_in_typealiasAlias3017 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_declarationSpecifiers_in_typealiasAlias3023 = new BitSet(new long[]{0x0400804000000002L});
	public static final BitSet FOLLOW_abstractDeclaratorList_in_typealiasAlias3025 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_TYPEALIASTOK_in_typealiasDecl3039 = new BitSet(new long[]{0x40004AC18A180900L,0x000000000001C891L});
	public static final BitSet FOLLOW_typealiasTarget_in_typealiasDecl3041 = new BitSet(new long[]{0x0000000000000000L,0x0000000000001000L});
	public static final BitSet FOLLOW_TYPE_ASSIGNMENT_in_typealiasDecl3043 = new BitSet(new long[]{0x4400CAC18A180900L,0x000000000001C891L});
	public static final BitSet FOLLOW_typealiasAlias_in_typealiasDecl3045 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_ctfSpecifierHead_in_ctfSpecifier3145 = new BitSet(new long[]{0x0000100000000000L});
	public static final BitSet FOLLOW_ctfBody_in_ctfSpecifier3147 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_typealiasDecl_in_ctfSpecifier3164 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_EVENTTOK_in_ctfSpecifierHead3185 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_STREAMTOK_in_ctfSpecifierHead3195 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_TRACETOK_in_ctfSpecifierHead3205 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_ENVTOK_in_ctfSpecifierHead3215 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_CLOCKTOK_in_ctfSpecifierHead3225 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_CALLSITETOK_in_ctfSpecifierHead3235 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_FLOATINGPOINTTOK_in_ctfTypeSpecifier3258 = new BitSet(new long[]{0x0000100000000000L});
	public static final BitSet FOLLOW_ctfBody_in_ctfTypeSpecifier3260 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_INTEGERTOK_in_ctfTypeSpecifier3275 = new BitSet(new long[]{0x0000100000000000L});
	public static final BitSet FOLLOW_ctfBody_in_ctfTypeSpecifier3277 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_STRINGTOK_in_ctfTypeSpecifier3292 = new BitSet(new long[]{0x0000100000000002L});
	public static final BitSet FOLLOW_ctfBody_in_ctfTypeSpecifier3294 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_LCURL_in_ctfBody3327 = new BitSet(new long[]{0xC8404AD1DA382F10L,0x000000000001CED5L});
	public static final BitSet FOLLOW_ctfAssignmentExpressionList_in_ctfBody3329 = new BitSet(new long[]{0x0800000000000000L});
	public static final BitSet FOLLOW_RCURL_in_ctfBody3332 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_ctfAssignmentExpression_in_ctfAssignmentExpressionList3351 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000100L});
	public static final BitSet FOLLOW_TERM_in_ctfAssignmentExpressionList3353 = new BitSet(new long[]{0xC0404AD1DA382F12L,0x000000000001CED5L});
	public static final BitSet FOLLOW_unaryExpression_in_ctfAssignmentExpression3376 = new BitSet(new long[]{0x0000000000000040L,0x0000000000001000L});
	public static final BitSet FOLLOW_ASSIGNMENT_in_ctfAssignmentExpression3388 = new BitSet(new long[]{0x8040005050202610L,0x0000000000000255L});
	public static final BitSet FOLLOW_unaryExpression_in_ctfAssignmentExpression3392 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_TYPE_ASSIGNMENT_in_ctfAssignmentExpression3468 = new BitSet(new long[]{0x40004AC18A080900L,0x000000000001C091L});
	public static final BitSet FOLLOW_typeSpecifier_in_ctfAssignmentExpression3472 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_declarationSpecifiers_in_ctfAssignmentExpression3555 = new BitSet(new long[]{0x0400004000000000L});
	public static final BitSet FOLLOW_declaratorList_in_ctfAssignmentExpression3559 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_typealiasDecl_in_ctfAssignmentExpression3582 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_IDENTIFIER_in_synpred1_CTFParser560 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_ctfKeyword_in_synpred2_CTFParser586 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_STRING_LITERAL_in_synpred3_CTFParser606 = new BitSet(new long[]{0x0000000000000002L});
}
