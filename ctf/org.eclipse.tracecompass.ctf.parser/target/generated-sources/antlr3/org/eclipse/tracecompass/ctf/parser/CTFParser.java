// $ANTLR 3.5.2 org/eclipse/tracecompass/ctf/parser/CTFParser.g 2015-07-09 14:10:52

/*******************************************************************************
 * Copyright (c) 2010, 2015 Ericsson, Ecole Polytechnique de Montréal and others
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

import java.util.HashSet;
import java.util.Set;
import java.util.Stack;

import org.antlr.runtime.BaseRecognizer;
import org.antlr.runtime.BitSet;
import org.antlr.runtime.DFA;
import org.antlr.runtime.EarlyExitException;
import org.antlr.runtime.FailedPredicateException;
import org.antlr.runtime.IntStream;
import org.antlr.runtime.MismatchedSetException;
import org.antlr.runtime.MismatchedTokenException;
import org.antlr.runtime.NoViableAltException;
import org.antlr.runtime.Parser;
import org.antlr.runtime.ParserRuleReturnScope;
import org.antlr.runtime.RecognitionException;
import org.antlr.runtime.RecognizerSharedState;
import org.antlr.runtime.Token;
import org.antlr.runtime.TokenStream;
import org.antlr.runtime.tree.CommonTree;
import org.antlr.runtime.tree.CommonTreeAdaptor;
import org.antlr.runtime.tree.RewriteEarlyExitException;
import org.antlr.runtime.tree.RewriteRuleSubtreeStream;
import org.antlr.runtime.tree.RewriteRuleTokenStream;
import org.antlr.runtime.tree.TreeAdaptor;


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
	            debug_print("New type: " + name +  " " + declaration_stack);
	        }
	    }

	    void typedefOn() {
	        debug_print("typedefOn" + declaration_stack);
	        declaration_stack.peek().isTypedef =true;
	    }

	    void typedefOff() {
	        debug_print("typedefOff" + declaration_stack);
	        declaration_stack.peek().isTypedef =false;
	    }

	    boolean inTypedef() {
	        return declaration_stack.peek().isTypedef;
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
	// org/eclipse/tracecompass/ctf/parser/CTFParser.g:190:1: parse : ( declaration )+ EOF -> ^( ROOT ( declaration )+ ) ;
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
			// org/eclipse/tracecompass/ctf/parser/CTFParser.g:195:3: ( ( declaration )+ EOF -> ^( ROOT ( declaration )+ ) )
			// org/eclipse/tracecompass/ctf/parser/CTFParser.g:195:5: ( declaration )+ EOF
			{
			// org/eclipse/tracecompass/ctf/parser/CTFParser.g:195:5: ( declaration )+
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
					// org/eclipse/tracecompass/ctf/parser/CTFParser.g:195:5: declaration
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
			// 195:22: -> ^( ROOT ( declaration )+ )
			{
				// org/eclipse/tracecompass/ctf/parser/CTFParser.g:195:25: ^( ROOT ( declaration )+ )
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
	// org/eclipse/tracecompass/ctf/parser/CTFParser.g:198:1: numberLiteral : ( SIGN )* ( HEX_LITERAL -> ^( UNARY_EXPRESSION_HEX HEX_LITERAL ( SIGN )* ) | DECIMAL_LITERAL -> ^( UNARY_EXPRESSION_DEC DECIMAL_LITERAL ( SIGN )* ) | OCTAL_LITERAL -> ^( UNARY_EXPRESSION_OCT OCTAL_LITERAL ( SIGN )* ) ) ;
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
			// org/eclipse/tracecompass/ctf/parser/CTFParser.g:199:3: ( ( SIGN )* ( HEX_LITERAL -> ^( UNARY_EXPRESSION_HEX HEX_LITERAL ( SIGN )* ) | DECIMAL_LITERAL -> ^( UNARY_EXPRESSION_DEC DECIMAL_LITERAL ( SIGN )* ) | OCTAL_LITERAL -> ^( UNARY_EXPRESSION_OCT OCTAL_LITERAL ( SIGN )* ) ) )
			// org/eclipse/tracecompass/ctf/parser/CTFParser.g:199:5: ( SIGN )* ( HEX_LITERAL -> ^( UNARY_EXPRESSION_HEX HEX_LITERAL ( SIGN )* ) | DECIMAL_LITERAL -> ^( UNARY_EXPRESSION_DEC DECIMAL_LITERAL ( SIGN )* ) | OCTAL_LITERAL -> ^( UNARY_EXPRESSION_OCT OCTAL_LITERAL ( SIGN )* ) )
			{
			// org/eclipse/tracecompass/ctf/parser/CTFParser.g:199:5: ( SIGN )*
			loop2:
			while (true) {
				int alt2=2;
				int LA2_0 = input.LA(1);
				if ( (LA2_0==SIGN) ) {
					alt2=1;
				}

				switch (alt2) {
				case 1 :
					// org/eclipse/tracecompass/ctf/parser/CTFParser.g:199:5: SIGN
					{
					SIGN3=(Token)match(input,SIGN,FOLLOW_SIGN_in_numberLiteral474); if (state.failed) return retval; 
					if ( state.backtracking==0 ) stream_SIGN.add(SIGN3);

					}
					break;

				default :
					break loop2;
				}
			}

			// org/eclipse/tracecompass/ctf/parser/CTFParser.g:200:7: ( HEX_LITERAL -> ^( UNARY_EXPRESSION_HEX HEX_LITERAL ( SIGN )* ) | DECIMAL_LITERAL -> ^( UNARY_EXPRESSION_DEC DECIMAL_LITERAL ( SIGN )* ) | OCTAL_LITERAL -> ^( UNARY_EXPRESSION_OCT OCTAL_LITERAL ( SIGN )* ) )
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
					// org/eclipse/tracecompass/ctf/parser/CTFParser.g:200:9: HEX_LITERAL
					{
					HEX_LITERAL4=(Token)match(input,HEX_LITERAL,FOLLOW_HEX_LITERAL_in_numberLiteral485); if (state.failed) return retval; 
					if ( state.backtracking==0 ) stream_HEX_LITERAL.add(HEX_LITERAL4);

					// AST REWRITE
					// elements: SIGN, HEX_LITERAL
					// token labels: 
					// rule labels: retval
					// token list labels: 
					// rule list labels: 
					// wildcard labels: 
					if ( state.backtracking==0 ) {
					retval.tree = root_0;
					RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.getTree():null);

					root_0 = (CommonTree)adaptor.nil();
					// 200:21: -> ^( UNARY_EXPRESSION_HEX HEX_LITERAL ( SIGN )* )
					{
						// org/eclipse/tracecompass/ctf/parser/CTFParser.g:200:24: ^( UNARY_EXPRESSION_HEX HEX_LITERAL ( SIGN )* )
						{
						CommonTree root_1 = (CommonTree)adaptor.nil();
						root_1 = (CommonTree)adaptor.becomeRoot((CommonTree)adaptor.create(UNARY_EXPRESSION_HEX, "UNARY_EXPRESSION_HEX"), root_1);
						adaptor.addChild(root_1, stream_HEX_LITERAL.nextNode());
						// org/eclipse/tracecompass/ctf/parser/CTFParser.g:200:59: ( SIGN )*
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
					// org/eclipse/tracecompass/ctf/parser/CTFParser.g:201:9: DECIMAL_LITERAL
					{
					DECIMAL_LITERAL5=(Token)match(input,DECIMAL_LITERAL,FOLLOW_DECIMAL_LITERAL_in_numberLiteral506); if (state.failed) return retval; 
					if ( state.backtracking==0 ) stream_DECIMAL_LITERAL.add(DECIMAL_LITERAL5);

					// AST REWRITE
					// elements: DECIMAL_LITERAL, SIGN
					// token labels: 
					// rule labels: retval
					// token list labels: 
					// rule list labels: 
					// wildcard labels: 
					if ( state.backtracking==0 ) {
					retval.tree = root_0;
					RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.getTree():null);

					root_0 = (CommonTree)adaptor.nil();
					// 201:25: -> ^( UNARY_EXPRESSION_DEC DECIMAL_LITERAL ( SIGN )* )
					{
						// org/eclipse/tracecompass/ctf/parser/CTFParser.g:201:28: ^( UNARY_EXPRESSION_DEC DECIMAL_LITERAL ( SIGN )* )
						{
						CommonTree root_1 = (CommonTree)adaptor.nil();
						root_1 = (CommonTree)adaptor.becomeRoot((CommonTree)adaptor.create(UNARY_EXPRESSION_DEC, "UNARY_EXPRESSION_DEC"), root_1);
						adaptor.addChild(root_1, stream_DECIMAL_LITERAL.nextNode());
						// org/eclipse/tracecompass/ctf/parser/CTFParser.g:201:67: ( SIGN )*
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
					// org/eclipse/tracecompass/ctf/parser/CTFParser.g:202:9: OCTAL_LITERAL
					{
					OCTAL_LITERAL6=(Token)match(input,OCTAL_LITERAL,FOLLOW_OCTAL_LITERAL_in_numberLiteral527); if (state.failed) return retval; 
					if ( state.backtracking==0 ) stream_OCTAL_LITERAL.add(OCTAL_LITERAL6);

					// AST REWRITE
					// elements: OCTAL_LITERAL, SIGN
					// token labels: 
					// rule labels: retval
					// token list labels: 
					// rule list labels: 
					// wildcard labels: 
					if ( state.backtracking==0 ) {
					retval.tree = root_0;
					RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.getTree():null);

					root_0 = (CommonTree)adaptor.nil();
					// 202:23: -> ^( UNARY_EXPRESSION_OCT OCTAL_LITERAL ( SIGN )* )
					{
						// org/eclipse/tracecompass/ctf/parser/CTFParser.g:202:26: ^( UNARY_EXPRESSION_OCT OCTAL_LITERAL ( SIGN )* )
						{
						CommonTree root_1 = (CommonTree)adaptor.nil();
						root_1 = (CommonTree)adaptor.becomeRoot((CommonTree)adaptor.create(UNARY_EXPRESSION_OCT, "UNARY_EXPRESSION_OCT"), root_1);
						adaptor.addChild(root_1, stream_OCTAL_LITERAL.nextNode());
						// org/eclipse/tracecompass/ctf/parser/CTFParser.g:202:63: ( SIGN )*
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
	// org/eclipse/tracecompass/ctf/parser/CTFParser.g:206:1: primaryExpression : ( ( IDENTIFIER )=> IDENTIFIER -> ^( UNARY_EXPRESSION_STRING IDENTIFIER ) | ( ctfKeyword )=> ctfKeyword -> ^( UNARY_EXPRESSION_STRING ctfKeyword ) | ( STRING_LITERAL )=> STRING_LITERAL -> ^( UNARY_EXPRESSION_STRING_QUOTES STRING_LITERAL ) | numberLiteral | enumConstant | CHARACTER_LITERAL );
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
			// org/eclipse/tracecompass/ctf/parser/CTFParser.g:207:3: ( ( IDENTIFIER )=> IDENTIFIER -> ^( UNARY_EXPRESSION_STRING IDENTIFIER ) | ( ctfKeyword )=> ctfKeyword -> ^( UNARY_EXPRESSION_STRING ctfKeyword ) | ( STRING_LITERAL )=> STRING_LITERAL -> ^( UNARY_EXPRESSION_STRING_QUOTES STRING_LITERAL ) | numberLiteral | enumConstant | CHARACTER_LITERAL )
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
					// org/eclipse/tracecompass/ctf/parser/CTFParser.g:207:5: ( IDENTIFIER )=> IDENTIFIER
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
					// 208:7: -> ^( UNARY_EXPRESSION_STRING IDENTIFIER )
					{
						// org/eclipse/tracecompass/ctf/parser/CTFParser.g:208:10: ^( UNARY_EXPRESSION_STRING IDENTIFIER )
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
					// org/eclipse/tracecompass/ctf/parser/CTFParser.g:209:5: ( ctfKeyword )=> ctfKeyword
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
					// 209:32: -> ^( UNARY_EXPRESSION_STRING ctfKeyword )
					{
						// org/eclipse/tracecompass/ctf/parser/CTFParser.g:209:35: ^( UNARY_EXPRESSION_STRING ctfKeyword )
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
					// org/eclipse/tracecompass/ctf/parser/CTFParser.g:210:5: ( STRING_LITERAL )=> STRING_LITERAL
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
					// 211:7: -> ^( UNARY_EXPRESSION_STRING_QUOTES STRING_LITERAL )
					{
						// org/eclipse/tracecompass/ctf/parser/CTFParser.g:211:10: ^( UNARY_EXPRESSION_STRING_QUOTES STRING_LITERAL )
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
					// org/eclipse/tracecompass/ctf/parser/CTFParser.g:213:5: numberLiteral
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
					// org/eclipse/tracecompass/ctf/parser/CTFParser.g:214:5: enumConstant
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
					// org/eclipse/tracecompass/ctf/parser/CTFParser.g:215:5: CHARACTER_LITERAL
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
	// org/eclipse/tracecompass/ctf/parser/CTFParser.g:218:1: postfixExpressionSuffix : ( OPENBRAC unaryExpression CLOSEBRAC !| (ref= DOT |ref= ARROW ) IDENTIFIER -> ^( $ref ^( UNARY_EXPRESSION_STRING IDENTIFIER ) ) );
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
			// org/eclipse/tracecompass/ctf/parser/CTFParser.g:219:3: ( OPENBRAC unaryExpression CLOSEBRAC !| (ref= DOT |ref= ARROW ) IDENTIFIER -> ^( $ref ^( UNARY_EXPRESSION_STRING IDENTIFIER ) ) )
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
					// org/eclipse/tracecompass/ctf/parser/CTFParser.g:219:5: OPENBRAC unaryExpression CLOSEBRAC !
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
					// org/eclipse/tracecompass/ctf/parser/CTFParser.g:220:5: (ref= DOT |ref= ARROW ) IDENTIFIER
					{
					// org/eclipse/tracecompass/ctf/parser/CTFParser.g:220:5: (ref= DOT |ref= ARROW )
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
							// org/eclipse/tracecompass/ctf/parser/CTFParser.g:220:6: ref= DOT
							{
							ref=(Token)match(input,DOT,FOLLOW_DOT_in_postfixExpressionSuffix675); if (state.failed) return retval; 
							if ( state.backtracking==0 ) stream_DOT.add(ref);

							}
							break;
						case 2 :
							// org/eclipse/tracecompass/ctf/parser/CTFParser.g:220:16: ref= ARROW
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
					// 221:7: -> ^( $ref ^( UNARY_EXPRESSION_STRING IDENTIFIER ) )
					{
						// org/eclipse/tracecompass/ctf/parser/CTFParser.g:221:10: ^( $ref ^( UNARY_EXPRESSION_STRING IDENTIFIER ) )
						{
						CommonTree root_1 = (CommonTree)adaptor.nil();
						root_1 = (CommonTree)adaptor.becomeRoot(stream_ref.nextNode(), root_1);
						// org/eclipse/tracecompass/ctf/parser/CTFParser.g:221:17: ^( UNARY_EXPRESSION_STRING IDENTIFIER )
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


	public static class postfixCtfExpression_return extends ParserRuleReturnScope {
		CommonTree tree;
		@Override
		public CommonTree getTree() { return tree; }
	};


	// $ANTLR start "postfixCtfExpression"
	// org/eclipse/tracecompass/ctf/parser/CTFParser.g:224:1: postfixCtfExpression : (ref= DOT ) ctfSpecifierHead -> ^( $ref ^( UNARY_EXPRESSION_STRING ctfSpecifierHead ) ) ;
	public final CTFParser.postfixCtfExpression_return postfixCtfExpression() throws RecognitionException {
		CTFParser.postfixCtfExpression_return retval = new CTFParser.postfixCtfExpression_return();
		retval.start = input.LT(1);

		CommonTree root_0 = null;

		Token ref=null;
		ParserRuleReturnScope ctfSpecifierHead17 =null;

		CommonTree ref_tree=null;
		RewriteRuleTokenStream stream_DOT=new RewriteRuleTokenStream(adaptor,"token DOT");
		RewriteRuleSubtreeStream stream_ctfSpecifierHead=new RewriteRuleSubtreeStream(adaptor,"rule ctfSpecifierHead");

		try {
			// org/eclipse/tracecompass/ctf/parser/CTFParser.g:225:3: ( (ref= DOT ) ctfSpecifierHead -> ^( $ref ^( UNARY_EXPRESSION_STRING ctfSpecifierHead ) ) )
			// org/eclipse/tracecompass/ctf/parser/CTFParser.g:225:5: (ref= DOT ) ctfSpecifierHead
			{
			// org/eclipse/tracecompass/ctf/parser/CTFParser.g:225:5: (ref= DOT )
			// org/eclipse/tracecompass/ctf/parser/CTFParser.g:225:6: ref= DOT
			{
			ref=(Token)match(input,DOT,FOLLOW_DOT_in_postfixCtfExpression719); if (state.failed) return retval; 
			if ( state.backtracking==0 ) stream_DOT.add(ref);

			}

			pushFollow(FOLLOW_ctfSpecifierHead_in_postfixCtfExpression722);
			ctfSpecifierHead17=ctfSpecifierHead();
			state._fsp--;
			if (state.failed) return retval;
			if ( state.backtracking==0 ) stream_ctfSpecifierHead.add(ctfSpecifierHead17.getTree());
			// AST REWRITE
			// elements: ref, ctfSpecifierHead
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
			// 226:7: -> ^( $ref ^( UNARY_EXPRESSION_STRING ctfSpecifierHead ) )
			{
				// org/eclipse/tracecompass/ctf/parser/CTFParser.g:226:10: ^( $ref ^( UNARY_EXPRESSION_STRING ctfSpecifierHead ) )
				{
				CommonTree root_1 = (CommonTree)adaptor.nil();
				root_1 = (CommonTree)adaptor.becomeRoot(stream_ref.nextNode(), root_1);
				// org/eclipse/tracecompass/ctf/parser/CTFParser.g:226:17: ^( UNARY_EXPRESSION_STRING ctfSpecifierHead )
				{
				CommonTree root_2 = (CommonTree)adaptor.nil();
				root_2 = (CommonTree)adaptor.becomeRoot((CommonTree)adaptor.create(UNARY_EXPRESSION_STRING, "UNARY_EXPRESSION_STRING"), root_2);
				adaptor.addChild(root_2, stream_ctfSpecifierHead.nextTree());
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
	// $ANTLR end "postfixCtfExpression"


	public static class postfixExpression_return extends ParserRuleReturnScope {
		CommonTree tree;
		@Override
		public CommonTree getTree() { return tree; }
	};


	// $ANTLR start "postfixExpression"
	// org/eclipse/tracecompass/ctf/parser/CTFParser.g:229:1: postfixExpression : ( ( primaryExpression ( postfixExpressionSuffix )* ) | ( ctfSpecifierHead ( postfixCtfExpression )* ( postfixExpressionSuffix )+ ) );
	public final CTFParser.postfixExpression_return postfixExpression() throws RecognitionException {
		CTFParser.postfixExpression_return retval = new CTFParser.postfixExpression_return();
		retval.start = input.LT(1);

		CommonTree root_0 = null;

		ParserRuleReturnScope primaryExpression18 =null;
		ParserRuleReturnScope postfixExpressionSuffix19 =null;
		ParserRuleReturnScope ctfSpecifierHead20 =null;
		ParserRuleReturnScope postfixCtfExpression21 =null;
		ParserRuleReturnScope postfixExpressionSuffix22 =null;


		try {
			// org/eclipse/tracecompass/ctf/parser/CTFParser.g:230:3: ( ( primaryExpression ( postfixExpressionSuffix )* ) | ( ctfSpecifierHead ( postfixCtfExpression )* ( postfixExpressionSuffix )+ ) )
			int alt10=2;
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
				alt10=1;
				}
				break;
			case EVENTTOK:
				{
				alt10=1;
				}
				break;
			case CALLSITETOK:
			case CLOCKTOK:
			case ENVTOK:
			case STREAMTOK:
			case TRACETOK:
				{
				alt10=2;
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
					// org/eclipse/tracecompass/ctf/parser/CTFParser.g:230:5: ( primaryExpression ( postfixExpressionSuffix )* )
					{
					root_0 = (CommonTree)adaptor.nil();


					// org/eclipse/tracecompass/ctf/parser/CTFParser.g:230:5: ( primaryExpression ( postfixExpressionSuffix )* )
					// org/eclipse/tracecompass/ctf/parser/CTFParser.g:230:6: primaryExpression ( postfixExpressionSuffix )*
					{
					pushFollow(FOLLOW_primaryExpression_in_postfixExpression755);
					primaryExpression18=primaryExpression();
					state._fsp--;
					if (state.failed) return retval;
					if ( state.backtracking==0 ) adaptor.addChild(root_0, primaryExpression18.getTree());

					// org/eclipse/tracecompass/ctf/parser/CTFParser.g:230:24: ( postfixExpressionSuffix )*
					loop7:
					while (true) {
						int alt7=2;
						int LA7_0 = input.LA(1);
						if ( (LA7_0==ARROW||LA7_0==DOT||LA7_0==OPENBRAC) ) {
							alt7=1;
						}

						switch (alt7) {
						case 1 :
							// org/eclipse/tracecompass/ctf/parser/CTFParser.g:230:24: postfixExpressionSuffix
							{
							pushFollow(FOLLOW_postfixExpressionSuffix_in_postfixExpression757);
							postfixExpressionSuffix19=postfixExpressionSuffix();
							state._fsp--;
							if (state.failed) return retval;
							if ( state.backtracking==0 ) adaptor.addChild(root_0, postfixExpressionSuffix19.getTree());

							}
							break;

						default :
							break loop7;
						}
					}

					}

					}
					break;
				case 2 :
					// org/eclipse/tracecompass/ctf/parser/CTFParser.g:231:5: ( ctfSpecifierHead ( postfixCtfExpression )* ( postfixExpressionSuffix )+ )
					{
					root_0 = (CommonTree)adaptor.nil();


					// org/eclipse/tracecompass/ctf/parser/CTFParser.g:231:5: ( ctfSpecifierHead ( postfixCtfExpression )* ( postfixExpressionSuffix )+ )
					// org/eclipse/tracecompass/ctf/parser/CTFParser.g:231:6: ctfSpecifierHead ( postfixCtfExpression )* ( postfixExpressionSuffix )+
					{
					pushFollow(FOLLOW_ctfSpecifierHead_in_postfixExpression766);
					ctfSpecifierHead20=ctfSpecifierHead();
					state._fsp--;
					if (state.failed) return retval;
					if ( state.backtracking==0 ) adaptor.addChild(root_0, ctfSpecifierHead20.getTree());

					// org/eclipse/tracecompass/ctf/parser/CTFParser.g:231:23: ( postfixCtfExpression )*
					loop8:
					while (true) {
						int alt8=2;
						int LA8_0 = input.LA(1);
						if ( (LA8_0==DOT) ) {
							int LA8_2 = input.LA(2);
							if ( (LA8_2==CALLSITETOK||LA8_2==CLOCKTOK||LA8_2==ENVTOK||LA8_2==EVENTTOK||LA8_2==STREAMTOK||LA8_2==TRACETOK) ) {
								alt8=1;
							}

						}

						switch (alt8) {
						case 1 :
							// org/eclipse/tracecompass/ctf/parser/CTFParser.g:231:23: postfixCtfExpression
							{
							pushFollow(FOLLOW_postfixCtfExpression_in_postfixExpression768);
							postfixCtfExpression21=postfixCtfExpression();
							state._fsp--;
							if (state.failed) return retval;
							if ( state.backtracking==0 ) adaptor.addChild(root_0, postfixCtfExpression21.getTree());

							}
							break;

						default :
							break loop8;
						}
					}

					// org/eclipse/tracecompass/ctf/parser/CTFParser.g:231:45: ( postfixExpressionSuffix )+
					int cnt9=0;
					loop9:
					while (true) {
						int alt9=2;
						int LA9_0 = input.LA(1);
						if ( (LA9_0==ARROW||LA9_0==DOT||LA9_0==OPENBRAC) ) {
							alt9=1;
						}

						switch (alt9) {
						case 1 :
							// org/eclipse/tracecompass/ctf/parser/CTFParser.g:231:45: postfixExpressionSuffix
							{
							pushFollow(FOLLOW_postfixExpressionSuffix_in_postfixExpression771);
							postfixExpressionSuffix22=postfixExpressionSuffix();
							state._fsp--;
							if (state.failed) return retval;
							if ( state.backtracking==0 ) adaptor.addChild(root_0, postfixExpressionSuffix22.getTree());

							}
							break;

						default :
							if ( cnt9 >= 1 ) break loop9;
							if (state.backtracking>0) {state.failed=true; return retval;}
							EarlyExitException eee = new EarlyExitException(9, input);
							throw eee;
						}
						cnt9++;
					}

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
	// org/eclipse/tracecompass/ctf/parser/CTFParser.g:234:1: unaryExpression : postfixExpression ;
	public final CTFParser.unaryExpression_return unaryExpression() throws RecognitionException {
		CTFParser.unaryExpression_return retval = new CTFParser.unaryExpression_return();
		retval.start = input.LT(1);

		CommonTree root_0 = null;

		ParserRuleReturnScope postfixExpression23 =null;


		try {
			// org/eclipse/tracecompass/ctf/parser/CTFParser.g:235:3: ( postfixExpression )
			// org/eclipse/tracecompass/ctf/parser/CTFParser.g:235:5: postfixExpression
			{
			root_0 = (CommonTree)adaptor.nil();


			pushFollow(FOLLOW_postfixExpression_in_unaryExpression787);
			postfixExpression23=postfixExpression();
			state._fsp--;
			if (state.failed) return retval;
			if ( state.backtracking==0 ) adaptor.addChild(root_0, postfixExpression23.getTree());

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
	// org/eclipse/tracecompass/ctf/parser/CTFParser.g:239:1: enumConstant : ( STRING_LITERAL -> ^( UNARY_EXPRESSION_STRING_QUOTES STRING_LITERAL ) | IDENTIFIER -> ^( UNARY_EXPRESSION_STRING IDENTIFIER ) | ctfKeyword -> ^( UNARY_EXPRESSION_STRING ctfKeyword ) );
	public final CTFParser.enumConstant_return enumConstant() throws RecognitionException {
		CTFParser.enumConstant_return retval = new CTFParser.enumConstant_return();
		retval.start = input.LT(1);

		CommonTree root_0 = null;

		Token STRING_LITERAL24=null;
		Token IDENTIFIER25=null;
		ParserRuleReturnScope ctfKeyword26 =null;

		CommonTree STRING_LITERAL24_tree=null;
		CommonTree IDENTIFIER25_tree=null;
		RewriteRuleTokenStream stream_STRING_LITERAL=new RewriteRuleTokenStream(adaptor,"token STRING_LITERAL");
		RewriteRuleTokenStream stream_IDENTIFIER=new RewriteRuleTokenStream(adaptor,"token IDENTIFIER");
		RewriteRuleSubtreeStream stream_ctfKeyword=new RewriteRuleSubtreeStream(adaptor,"rule ctfKeyword");

		try {
			// org/eclipse/tracecompass/ctf/parser/CTFParser.g:240:3: ( STRING_LITERAL -> ^( UNARY_EXPRESSION_STRING_QUOTES STRING_LITERAL ) | IDENTIFIER -> ^( UNARY_EXPRESSION_STRING IDENTIFIER ) | ctfKeyword -> ^( UNARY_EXPRESSION_STRING ctfKeyword ) )
			int alt11=3;
			switch ( input.LA(1) ) {
			case STRING_LITERAL:
				{
				alt11=1;
				}
				break;
			case IDENTIFIER:
				{
				alt11=2;
				}
				break;
			case ALIGNTOK:
			case EVENTTOK:
			case SIGNEDTOK:
			case STRINGTOK:
				{
				alt11=3;
				}
				break;
			default:
				if (state.backtracking>0) {state.failed=true; return retval;}
				NoViableAltException nvae =
					new NoViableAltException("", 11, 0, input);
				throw nvae;
			}
			switch (alt11) {
				case 1 :
					// org/eclipse/tracecompass/ctf/parser/CTFParser.g:240:5: STRING_LITERAL
					{
					STRING_LITERAL24=(Token)match(input,STRING_LITERAL,FOLLOW_STRING_LITERAL_in_enumConstant804); if (state.failed) return retval; 
					if ( state.backtracking==0 ) stream_STRING_LITERAL.add(STRING_LITERAL24);

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
					// 240:20: -> ^( UNARY_EXPRESSION_STRING_QUOTES STRING_LITERAL )
					{
						// org/eclipse/tracecompass/ctf/parser/CTFParser.g:240:23: ^( UNARY_EXPRESSION_STRING_QUOTES STRING_LITERAL )
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
					// org/eclipse/tracecompass/ctf/parser/CTFParser.g:241:5: IDENTIFIER
					{
					IDENTIFIER25=(Token)match(input,IDENTIFIER,FOLLOW_IDENTIFIER_in_enumConstant818); if (state.failed) return retval; 
					if ( state.backtracking==0 ) stream_IDENTIFIER.add(IDENTIFIER25);

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
					// 241:16: -> ^( UNARY_EXPRESSION_STRING IDENTIFIER )
					{
						// org/eclipse/tracecompass/ctf/parser/CTFParser.g:241:19: ^( UNARY_EXPRESSION_STRING IDENTIFIER )
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
					// org/eclipse/tracecompass/ctf/parser/CTFParser.g:242:5: ctfKeyword
					{
					pushFollow(FOLLOW_ctfKeyword_in_enumConstant832);
					ctfKeyword26=ctfKeyword();
					state._fsp--;
					if (state.failed) return retval;
					if ( state.backtracking==0 ) stream_ctfKeyword.add(ctfKeyword26.getTree());
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
					// 242:16: -> ^( UNARY_EXPRESSION_STRING ctfKeyword )
					{
						// org/eclipse/tracecompass/ctf/parser/CTFParser.g:242:19: ^( UNARY_EXPRESSION_STRING ctfKeyword )
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


	protected static class declaration_scope {
		boolean isTypedef;
	}
	protected Stack<declaration_scope> declaration_stack = new Stack<declaration_scope>();

	public static class declaration_return extends ParserRuleReturnScope {
		CommonTree tree;
		@Override
		public CommonTree getTree() { return tree; }
	};


	// $ANTLR start "declaration"
	// org/eclipse/tracecompass/ctf/parser/CTFParser.g:247:1: declaration : ( declarationSpecifiers ( declaratorList )? TERM -> {inTypedef()}? ^( DECLARATION ^( TYPEDEF declaratorList declarationSpecifiers ) ) -> ^( DECLARATION declarationSpecifiers ( declaratorList )? ) | ctfSpecifier TERM !);
	public final CTFParser.declaration_return declaration() throws RecognitionException {
		declaration_stack.push(new declaration_scope());
		CTFParser.declaration_return retval = new CTFParser.declaration_return();
		retval.start = input.LT(1);

		CommonTree root_0 = null;

		Token TERM29=null;
		Token TERM31=null;
		ParserRuleReturnScope declarationSpecifiers27 =null;
		ParserRuleReturnScope declaratorList28 =null;
		ParserRuleReturnScope ctfSpecifier30 =null;

		CommonTree TERM29_tree=null;
		CommonTree TERM31_tree=null;
		RewriteRuleTokenStream stream_TERM=new RewriteRuleTokenStream(adaptor,"token TERM");
		RewriteRuleSubtreeStream stream_declaratorList=new RewriteRuleSubtreeStream(adaptor,"rule declaratorList");
		RewriteRuleSubtreeStream stream_declarationSpecifiers=new RewriteRuleSubtreeStream(adaptor,"rule declarationSpecifiers");


		  typedefOff();

		try {
			// org/eclipse/tracecompass/ctf/parser/CTFParser.g:254:3: ( declarationSpecifiers ( declaratorList )? TERM -> {inTypedef()}? ^( DECLARATION ^( TYPEDEF declaratorList declarationSpecifiers ) ) -> ^( DECLARATION declarationSpecifiers ( declaratorList )? ) | ctfSpecifier TERM !)
			int alt13=2;
			int LA13_0 = input.LA(1);
			if ( (LA13_0==BOOLTOK||LA13_0==CHARTOK||(LA13_0 >= COMPLEXTOK && LA13_0 <= CONSTTOK)||LA13_0==DOUBLETOK||LA13_0==ENUMTOK||(LA13_0 >= FLOATINGPOINTTOK && LA13_0 <= FLOATTOK)||LA13_0==IMAGINARYTOK||LA13_0==INTEGERTOK||LA13_0==INTTOK||LA13_0==LONGTOK||LA13_0==SHORTTOK||LA13_0==SIGNEDTOK||LA13_0==STRINGTOK||LA13_0==STRUCTTOK||LA13_0==TYPEDEFTOK||(LA13_0 >= UNSIGNEDTOK && LA13_0 <= VOIDTOK)) ) {
				alt13=1;
			}
			else if ( (LA13_0==IDENTIFIER) && (( inTypealiasAlias() || isTypeName(input.LT(1).getText()) ))) {
				alt13=1;
			}
			else if ( (LA13_0==CALLSITETOK||LA13_0==CLOCKTOK||LA13_0==ENVTOK||LA13_0==EVENTTOK||LA13_0==STREAMTOK||(LA13_0 >= TRACETOK && LA13_0 <= TYPEALIASTOK)) ) {
				alt13=2;
			}

			else {
				if (state.backtracking>0) {state.failed=true; return retval;}
				NoViableAltException nvae =
					new NoViableAltException("", 13, 0, input);
				throw nvae;
			}

			switch (alt13) {
				case 1 :
					// org/eclipse/tracecompass/ctf/parser/CTFParser.g:254:5: declarationSpecifiers ( declaratorList )? TERM
					{
					pushFollow(FOLLOW_declarationSpecifiers_in_declaration863);
					declarationSpecifiers27=declarationSpecifiers();
					state._fsp--;
					if (state.failed) return retval;
					if ( state.backtracking==0 ) stream_declarationSpecifiers.add(declarationSpecifiers27.getTree());
					// org/eclipse/tracecompass/ctf/parser/CTFParser.g:254:27: ( declaratorList )?
					int alt12=2;
					int LA12_0 = input.LA(1);
					if ( (LA12_0==IDENTIFIER||LA12_0==POINTER) ) {
						alt12=1;
					}
					switch (alt12) {
						case 1 :
							// org/eclipse/tracecompass/ctf/parser/CTFParser.g:254:27: declaratorList
							{
							pushFollow(FOLLOW_declaratorList_in_declaration865);
							declaratorList28=declaratorList();
							state._fsp--;
							if (state.failed) return retval;
							if ( state.backtracking==0 ) stream_declaratorList.add(declaratorList28.getTree());
							}
							break;

					}

					TERM29=(Token)match(input,TERM,FOLLOW_TERM_in_declaration868); if (state.failed) return retval; 
					if ( state.backtracking==0 ) stream_TERM.add(TERM29);

					// AST REWRITE
					// elements: declarationSpecifiers, declaratorList, declarationSpecifiers, declaratorList
					// token labels: 
					// rule labels: retval
					// token list labels: 
					// rule list labels: 
					// wildcard labels: 
					if ( state.backtracking==0 ) {
					retval.tree = root_0;
					RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.getTree():null);

					root_0 = (CommonTree)adaptor.nil();
					// 257:7: -> {inTypedef()}? ^( DECLARATION ^( TYPEDEF declaratorList declarationSpecifiers ) )
					if (inTypedef()) {
						// org/eclipse/tracecompass/ctf/parser/CTFParser.g:258:10: ^( DECLARATION ^( TYPEDEF declaratorList declarationSpecifiers ) )
						{
						CommonTree root_1 = (CommonTree)adaptor.nil();
						root_1 = (CommonTree)adaptor.becomeRoot((CommonTree)adaptor.create(DECLARATION, "DECLARATION"), root_1);
						// org/eclipse/tracecompass/ctf/parser/CTFParser.g:258:24: ^( TYPEDEF declaratorList declarationSpecifiers )
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

					else // 259:7: -> ^( DECLARATION declarationSpecifiers ( declaratorList )? )
					{
						// org/eclipse/tracecompass/ctf/parser/CTFParser.g:259:10: ^( DECLARATION declarationSpecifiers ( declaratorList )? )
						{
						CommonTree root_1 = (CommonTree)adaptor.nil();
						root_1 = (CommonTree)adaptor.becomeRoot((CommonTree)adaptor.create(DECLARATION, "DECLARATION"), root_1);
						adaptor.addChild(root_1, stream_declarationSpecifiers.nextTree());
						// org/eclipse/tracecompass/ctf/parser/CTFParser.g:259:46: ( declaratorList )?
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
					// org/eclipse/tracecompass/ctf/parser/CTFParser.g:260:5: ctfSpecifier TERM !
					{
					root_0 = (CommonTree)adaptor.nil();


					pushFollow(FOLLOW_ctfSpecifier_in_declaration936);
					ctfSpecifier30=ctfSpecifier();
					state._fsp--;
					if (state.failed) return retval;
					if ( state.backtracking==0 ) adaptor.addChild(root_0, ctfSpecifier30.getTree());

					TERM31=(Token)match(input,TERM,FOLLOW_TERM_in_declaration938); if (state.failed) return retval;
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
			declaration_stack.pop();
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
	// org/eclipse/tracecompass/ctf/parser/CTFParser.g:263:1: declarationSpecifiers : ( storageClassSpecifier | typeQualifier | typeSpecifier )+ -> ^( TYPE_SPECIFIER_LIST ( typeQualifier )* ( typeSpecifier )* ) ;
	public final CTFParser.declarationSpecifiers_return declarationSpecifiers() throws RecognitionException {
		CTFParser.declarationSpecifiers_return retval = new CTFParser.declarationSpecifiers_return();
		retval.start = input.LT(1);

		CommonTree root_0 = null;

		ParserRuleReturnScope storageClassSpecifier32 =null;
		ParserRuleReturnScope typeQualifier33 =null;
		ParserRuleReturnScope typeSpecifier34 =null;

		RewriteRuleSubtreeStream stream_typeSpecifier=new RewriteRuleSubtreeStream(adaptor,"rule typeSpecifier");
		RewriteRuleSubtreeStream stream_typeQualifier=new RewriteRuleSubtreeStream(adaptor,"rule typeQualifier");
		RewriteRuleSubtreeStream stream_storageClassSpecifier=new RewriteRuleSubtreeStream(adaptor,"rule storageClassSpecifier");

		try {
			// org/eclipse/tracecompass/ctf/parser/CTFParser.g:264:3: ( ( storageClassSpecifier | typeQualifier | typeSpecifier )+ -> ^( TYPE_SPECIFIER_LIST ( typeQualifier )* ( typeSpecifier )* ) )
			// org/eclipse/tracecompass/ctf/parser/CTFParser.g:264:5: ( storageClassSpecifier | typeQualifier | typeSpecifier )+
			{
			// org/eclipse/tracecompass/ctf/parser/CTFParser.g:264:5: ( storageClassSpecifier | typeQualifier | typeSpecifier )+
			int cnt14=0;
			loop14:
			while (true) {
				int alt14=4;
				switch ( input.LA(1) ) {
				case IDENTIFIER:
					{
					int LA14_2 = input.LA(2);
					if ( ((( inTypealiasAlias() || isTypeName(input.LT(1).getText()) )&&(inTypealiasAlias() || isTypeName(input.LT(1).getText())))) ) {
						alt14=3;
					}

					}
					break;
				case TYPEDEFTOK:
					{
					alt14=1;
					}
					break;
				case CONSTTOK:
					{
					alt14=2;
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
					alt14=3;
					}
					break;
				}
				switch (alt14) {
				case 1 :
					// org/eclipse/tracecompass/ctf/parser/CTFParser.g:267:9: storageClassSpecifier
					{
					pushFollow(FOLLOW_storageClassSpecifier_in_declarationSpecifiers976);
					storageClassSpecifier32=storageClassSpecifier();
					state._fsp--;
					if (state.failed) return retval;
					if ( state.backtracking==0 ) stream_storageClassSpecifier.add(storageClassSpecifier32.getTree());
					}
					break;
				case 2 :
					// org/eclipse/tracecompass/ctf/parser/CTFParser.g:268:9: typeQualifier
					{
					pushFollow(FOLLOW_typeQualifier_in_declarationSpecifiers986);
					typeQualifier33=typeQualifier();
					state._fsp--;
					if (state.failed) return retval;
					if ( state.backtracking==0 ) stream_typeQualifier.add(typeQualifier33.getTree());
					}
					break;
				case 3 :
					// org/eclipse/tracecompass/ctf/parser/CTFParser.g:269:9: typeSpecifier
					{
					pushFollow(FOLLOW_typeSpecifier_in_declarationSpecifiers996);
					typeSpecifier34=typeSpecifier();
					state._fsp--;
					if (state.failed) return retval;
					if ( state.backtracking==0 ) stream_typeSpecifier.add(typeSpecifier34.getTree());
					}
					break;

				default :
					if ( cnt14 >= 1 ) break loop14;
					if (state.backtracking>0) {state.failed=true; return retval;}
					EarlyExitException eee = new EarlyExitException(14, input);
					throw eee;
				}
				cnt14++;
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
			// 270:6: -> ^( TYPE_SPECIFIER_LIST ( typeQualifier )* ( typeSpecifier )* )
			{
				// org/eclipse/tracecompass/ctf/parser/CTFParser.g:270:9: ^( TYPE_SPECIFIER_LIST ( typeQualifier )* ( typeSpecifier )* )
				{
				CommonTree root_1 = (CommonTree)adaptor.nil();
				root_1 = (CommonTree)adaptor.becomeRoot((CommonTree)adaptor.create(TYPE_SPECIFIER_LIST, "TYPE_SPECIFIER_LIST"), root_1);
				// org/eclipse/tracecompass/ctf/parser/CTFParser.g:270:31: ( typeQualifier )*
				while ( stream_typeQualifier.hasNext() ) {
					adaptor.addChild(root_1, stream_typeQualifier.nextTree());
				}
				stream_typeQualifier.reset();

				// org/eclipse/tracecompass/ctf/parser/CTFParser.g:270:46: ( typeSpecifier )*
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
	// org/eclipse/tracecompass/ctf/parser/CTFParser.g:273:1: declaratorList : declarator ( SEPARATOR declarator )* -> ^( TYPE_DECLARATOR_LIST ( declarator )+ ) ;
	public final CTFParser.declaratorList_return declaratorList() throws RecognitionException {
		CTFParser.declaratorList_return retval = new CTFParser.declaratorList_return();
		retval.start = input.LT(1);

		CommonTree root_0 = null;

		Token SEPARATOR36=null;
		ParserRuleReturnScope declarator35 =null;
		ParserRuleReturnScope declarator37 =null;

		CommonTree SEPARATOR36_tree=null;
		RewriteRuleTokenStream stream_SEPARATOR=new RewriteRuleTokenStream(adaptor,"token SEPARATOR");
		RewriteRuleSubtreeStream stream_declarator=new RewriteRuleSubtreeStream(adaptor,"rule declarator");

		try {
			// org/eclipse/tracecompass/ctf/parser/CTFParser.g:274:3: ( declarator ( SEPARATOR declarator )* -> ^( TYPE_DECLARATOR_LIST ( declarator )+ ) )
			// org/eclipse/tracecompass/ctf/parser/CTFParser.g:274:5: declarator ( SEPARATOR declarator )*
			{
			pushFollow(FOLLOW_declarator_in_declaratorList1026);
			declarator35=declarator();
			state._fsp--;
			if (state.failed) return retval;
			if ( state.backtracking==0 ) stream_declarator.add(declarator35.getTree());
			// org/eclipse/tracecompass/ctf/parser/CTFParser.g:274:16: ( SEPARATOR declarator )*
			loop15:
			while (true) {
				int alt15=2;
				int LA15_0 = input.LA(1);
				if ( (LA15_0==SEPARATOR) ) {
					alt15=1;
				}

				switch (alt15) {
				case 1 :
					// org/eclipse/tracecompass/ctf/parser/CTFParser.g:274:17: SEPARATOR declarator
					{
					SEPARATOR36=(Token)match(input,SEPARATOR,FOLLOW_SEPARATOR_in_declaratorList1029); if (state.failed) return retval; 
					if ( state.backtracking==0 ) stream_SEPARATOR.add(SEPARATOR36);

					pushFollow(FOLLOW_declarator_in_declaratorList1031);
					declarator37=declarator();
					state._fsp--;
					if (state.failed) return retval;
					if ( state.backtracking==0 ) stream_declarator.add(declarator37.getTree());
					}
					break;

				default :
					break loop15;
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
			// 275:7: -> ^( TYPE_DECLARATOR_LIST ( declarator )+ )
			{
				// org/eclipse/tracecompass/ctf/parser/CTFParser.g:275:10: ^( TYPE_DECLARATOR_LIST ( declarator )+ )
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
	// org/eclipse/tracecompass/ctf/parser/CTFParser.g:278:1: abstractDeclaratorList : abstractDeclarator ( SEPARATOR abstractDeclarator )* -> ^( TYPE_DECLARATOR_LIST ( abstractDeclarator )+ ) ;
	public final CTFParser.abstractDeclaratorList_return abstractDeclaratorList() throws RecognitionException {
		CTFParser.abstractDeclaratorList_return retval = new CTFParser.abstractDeclaratorList_return();
		retval.start = input.LT(1);

		CommonTree root_0 = null;

		Token SEPARATOR39=null;
		ParserRuleReturnScope abstractDeclarator38 =null;
		ParserRuleReturnScope abstractDeclarator40 =null;

		CommonTree SEPARATOR39_tree=null;
		RewriteRuleTokenStream stream_SEPARATOR=new RewriteRuleTokenStream(adaptor,"token SEPARATOR");
		RewriteRuleSubtreeStream stream_abstractDeclarator=new RewriteRuleSubtreeStream(adaptor,"rule abstractDeclarator");

		try {
			// org/eclipse/tracecompass/ctf/parser/CTFParser.g:279:3: ( abstractDeclarator ( SEPARATOR abstractDeclarator )* -> ^( TYPE_DECLARATOR_LIST ( abstractDeclarator )+ ) )
			// org/eclipse/tracecompass/ctf/parser/CTFParser.g:279:5: abstractDeclarator ( SEPARATOR abstractDeclarator )*
			{
			pushFollow(FOLLOW_abstractDeclarator_in_abstractDeclaratorList1061);
			abstractDeclarator38=abstractDeclarator();
			state._fsp--;
			if (state.failed) return retval;
			if ( state.backtracking==0 ) stream_abstractDeclarator.add(abstractDeclarator38.getTree());
			// org/eclipse/tracecompass/ctf/parser/CTFParser.g:279:24: ( SEPARATOR abstractDeclarator )*
			loop16:
			while (true) {
				int alt16=2;
				int LA16_0 = input.LA(1);
				if ( (LA16_0==SEPARATOR) ) {
					alt16=1;
				}

				switch (alt16) {
				case 1 :
					// org/eclipse/tracecompass/ctf/parser/CTFParser.g:279:25: SEPARATOR abstractDeclarator
					{
					SEPARATOR39=(Token)match(input,SEPARATOR,FOLLOW_SEPARATOR_in_abstractDeclaratorList1064); if (state.failed) return retval; 
					if ( state.backtracking==0 ) stream_SEPARATOR.add(SEPARATOR39);

					pushFollow(FOLLOW_abstractDeclarator_in_abstractDeclaratorList1066);
					abstractDeclarator40=abstractDeclarator();
					state._fsp--;
					if (state.failed) return retval;
					if ( state.backtracking==0 ) stream_abstractDeclarator.add(abstractDeclarator40.getTree());
					}
					break;

				default :
					break loop16;
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
			// 280:7: -> ^( TYPE_DECLARATOR_LIST ( abstractDeclarator )+ )
			{
				// org/eclipse/tracecompass/ctf/parser/CTFParser.g:280:10: ^( TYPE_DECLARATOR_LIST ( abstractDeclarator )+ )
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
	// org/eclipse/tracecompass/ctf/parser/CTFParser.g:283:1: storageClassSpecifier : TYPEDEFTOK ;
	public final CTFParser.storageClassSpecifier_return storageClassSpecifier() throws RecognitionException {
		CTFParser.storageClassSpecifier_return retval = new CTFParser.storageClassSpecifier_return();
		retval.start = input.LT(1);

		CommonTree root_0 = null;

		Token TYPEDEFTOK41=null;

		CommonTree TYPEDEFTOK41_tree=null;

		try {
			// org/eclipse/tracecompass/ctf/parser/CTFParser.g:284:3: ( TYPEDEFTOK )
			// org/eclipse/tracecompass/ctf/parser/CTFParser.g:284:5: TYPEDEFTOK
			{
			root_0 = (CommonTree)adaptor.nil();


			TYPEDEFTOK41=(Token)match(input,TYPEDEFTOK,FOLLOW_TYPEDEFTOK_in_storageClassSpecifier1096); if (state.failed) return retval;
			if ( state.backtracking==0 ) {
			TYPEDEFTOK41_tree = (CommonTree)adaptor.create(TYPEDEFTOK41);
			adaptor.addChild(root_0, TYPEDEFTOK41_tree);
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
	// org/eclipse/tracecompass/ctf/parser/CTFParser.g:287:1: typeSpecifier : ( FLOATTOK | INTTOK | LONGTOK | SHORTTOK | SIGNEDTOK | UNSIGNEDTOK | CHARTOK | DOUBLETOK | VOIDTOK | BOOLTOK | COMPLEXTOK | IMAGINARYTOK | structSpecifier | variantSpecifier | enumSpecifier | ctfTypeSpecifier |{...}? => typedefName );
	public final CTFParser.typeSpecifier_return typeSpecifier() throws RecognitionException {
		CTFParser.typeSpecifier_return retval = new CTFParser.typeSpecifier_return();
		retval.start = input.LT(1);

		CommonTree root_0 = null;

		Token FLOATTOK42=null;
		Token INTTOK43=null;
		Token LONGTOK44=null;
		Token SHORTTOK45=null;
		Token SIGNEDTOK46=null;
		Token UNSIGNEDTOK47=null;
		Token CHARTOK48=null;
		Token DOUBLETOK49=null;
		Token VOIDTOK50=null;
		Token BOOLTOK51=null;
		Token COMPLEXTOK52=null;
		Token IMAGINARYTOK53=null;
		ParserRuleReturnScope structSpecifier54 =null;
		ParserRuleReturnScope variantSpecifier55 =null;
		ParserRuleReturnScope enumSpecifier56 =null;
		ParserRuleReturnScope ctfTypeSpecifier57 =null;
		ParserRuleReturnScope typedefName58 =null;

		CommonTree FLOATTOK42_tree=null;
		CommonTree INTTOK43_tree=null;
		CommonTree LONGTOK44_tree=null;
		CommonTree SHORTTOK45_tree=null;
		CommonTree SIGNEDTOK46_tree=null;
		CommonTree UNSIGNEDTOK47_tree=null;
		CommonTree CHARTOK48_tree=null;
		CommonTree DOUBLETOK49_tree=null;
		CommonTree VOIDTOK50_tree=null;
		CommonTree BOOLTOK51_tree=null;
		CommonTree COMPLEXTOK52_tree=null;
		CommonTree IMAGINARYTOK53_tree=null;

		try {
			// org/eclipse/tracecompass/ctf/parser/CTFParser.g:288:3: ( FLOATTOK | INTTOK | LONGTOK | SHORTTOK | SIGNEDTOK | UNSIGNEDTOK | CHARTOK | DOUBLETOK | VOIDTOK | BOOLTOK | COMPLEXTOK | IMAGINARYTOK | structSpecifier | variantSpecifier | enumSpecifier | ctfTypeSpecifier |{...}? => typedefName )
			int alt17=17;
			int LA17_0 = input.LA(1);
			if ( (LA17_0==FLOATTOK) ) {
				alt17=1;
			}
			else if ( (LA17_0==INTTOK) ) {
				alt17=2;
			}
			else if ( (LA17_0==LONGTOK) ) {
				alt17=3;
			}
			else if ( (LA17_0==SHORTTOK) ) {
				alt17=4;
			}
			else if ( (LA17_0==SIGNEDTOK) ) {
				alt17=5;
			}
			else if ( (LA17_0==UNSIGNEDTOK) ) {
				alt17=6;
			}
			else if ( (LA17_0==CHARTOK) ) {
				alt17=7;
			}
			else if ( (LA17_0==DOUBLETOK) ) {
				alt17=8;
			}
			else if ( (LA17_0==VOIDTOK) ) {
				alt17=9;
			}
			else if ( (LA17_0==BOOLTOK) ) {
				alt17=10;
			}
			else if ( (LA17_0==COMPLEXTOK) ) {
				alt17=11;
			}
			else if ( (LA17_0==IMAGINARYTOK) ) {
				alt17=12;
			}
			else if ( (LA17_0==STRUCTTOK) ) {
				alt17=13;
			}
			else if ( (LA17_0==VARIANTTOK) ) {
				alt17=14;
			}
			else if ( (LA17_0==ENUMTOK) ) {
				alt17=15;
			}
			else if ( (LA17_0==FLOATINGPOINTTOK||LA17_0==INTEGERTOK||LA17_0==STRINGTOK) ) {
				alt17=16;
			}
			else if ( (LA17_0==IDENTIFIER) && (( inTypealiasAlias() || isTypeName(input.LT(1).getText()) ))) {
				alt17=17;
			}

			switch (alt17) {
				case 1 :
					// org/eclipse/tracecompass/ctf/parser/CTFParser.g:288:5: FLOATTOK
					{
					root_0 = (CommonTree)adaptor.nil();


					FLOATTOK42=(Token)match(input,FLOATTOK,FOLLOW_FLOATTOK_in_typeSpecifier1112); if (state.failed) return retval;
					if ( state.backtracking==0 ) {
					FLOATTOK42_tree = (CommonTree)adaptor.create(FLOATTOK42);
					adaptor.addChild(root_0, FLOATTOK42_tree);
					}

					}
					break;
				case 2 :
					// org/eclipse/tracecompass/ctf/parser/CTFParser.g:289:5: INTTOK
					{
					root_0 = (CommonTree)adaptor.nil();


					INTTOK43=(Token)match(input,INTTOK,FOLLOW_INTTOK_in_typeSpecifier1118); if (state.failed) return retval;
					if ( state.backtracking==0 ) {
					INTTOK43_tree = (CommonTree)adaptor.create(INTTOK43);
					adaptor.addChild(root_0, INTTOK43_tree);
					}

					}
					break;
				case 3 :
					// org/eclipse/tracecompass/ctf/parser/CTFParser.g:290:5: LONGTOK
					{
					root_0 = (CommonTree)adaptor.nil();


					LONGTOK44=(Token)match(input,LONGTOK,FOLLOW_LONGTOK_in_typeSpecifier1124); if (state.failed) return retval;
					if ( state.backtracking==0 ) {
					LONGTOK44_tree = (CommonTree)adaptor.create(LONGTOK44);
					adaptor.addChild(root_0, LONGTOK44_tree);
					}

					}
					break;
				case 4 :
					// org/eclipse/tracecompass/ctf/parser/CTFParser.g:291:5: SHORTTOK
					{
					root_0 = (CommonTree)adaptor.nil();


					SHORTTOK45=(Token)match(input,SHORTTOK,FOLLOW_SHORTTOK_in_typeSpecifier1130); if (state.failed) return retval;
					if ( state.backtracking==0 ) {
					SHORTTOK45_tree = (CommonTree)adaptor.create(SHORTTOK45);
					adaptor.addChild(root_0, SHORTTOK45_tree);
					}

					}
					break;
				case 5 :
					// org/eclipse/tracecompass/ctf/parser/CTFParser.g:292:5: SIGNEDTOK
					{
					root_0 = (CommonTree)adaptor.nil();


					SIGNEDTOK46=(Token)match(input,SIGNEDTOK,FOLLOW_SIGNEDTOK_in_typeSpecifier1136); if (state.failed) return retval;
					if ( state.backtracking==0 ) {
					SIGNEDTOK46_tree = (CommonTree)adaptor.create(SIGNEDTOK46);
					adaptor.addChild(root_0, SIGNEDTOK46_tree);
					}

					}
					break;
				case 6 :
					// org/eclipse/tracecompass/ctf/parser/CTFParser.g:293:5: UNSIGNEDTOK
					{
					root_0 = (CommonTree)adaptor.nil();


					UNSIGNEDTOK47=(Token)match(input,UNSIGNEDTOK,FOLLOW_UNSIGNEDTOK_in_typeSpecifier1142); if (state.failed) return retval;
					if ( state.backtracking==0 ) {
					UNSIGNEDTOK47_tree = (CommonTree)adaptor.create(UNSIGNEDTOK47);
					adaptor.addChild(root_0, UNSIGNEDTOK47_tree);
					}

					}
					break;
				case 7 :
					// org/eclipse/tracecompass/ctf/parser/CTFParser.g:294:5: CHARTOK
					{
					root_0 = (CommonTree)adaptor.nil();


					CHARTOK48=(Token)match(input,CHARTOK,FOLLOW_CHARTOK_in_typeSpecifier1148); if (state.failed) return retval;
					if ( state.backtracking==0 ) {
					CHARTOK48_tree = (CommonTree)adaptor.create(CHARTOK48);
					adaptor.addChild(root_0, CHARTOK48_tree);
					}

					}
					break;
				case 8 :
					// org/eclipse/tracecompass/ctf/parser/CTFParser.g:295:5: DOUBLETOK
					{
					root_0 = (CommonTree)adaptor.nil();


					DOUBLETOK49=(Token)match(input,DOUBLETOK,FOLLOW_DOUBLETOK_in_typeSpecifier1154); if (state.failed) return retval;
					if ( state.backtracking==0 ) {
					DOUBLETOK49_tree = (CommonTree)adaptor.create(DOUBLETOK49);
					adaptor.addChild(root_0, DOUBLETOK49_tree);
					}

					}
					break;
				case 9 :
					// org/eclipse/tracecompass/ctf/parser/CTFParser.g:296:5: VOIDTOK
					{
					root_0 = (CommonTree)adaptor.nil();


					VOIDTOK50=(Token)match(input,VOIDTOK,FOLLOW_VOIDTOK_in_typeSpecifier1160); if (state.failed) return retval;
					if ( state.backtracking==0 ) {
					VOIDTOK50_tree = (CommonTree)adaptor.create(VOIDTOK50);
					adaptor.addChild(root_0, VOIDTOK50_tree);
					}

					}
					break;
				case 10 :
					// org/eclipse/tracecompass/ctf/parser/CTFParser.g:297:5: BOOLTOK
					{
					root_0 = (CommonTree)adaptor.nil();


					BOOLTOK51=(Token)match(input,BOOLTOK,FOLLOW_BOOLTOK_in_typeSpecifier1166); if (state.failed) return retval;
					if ( state.backtracking==0 ) {
					BOOLTOK51_tree = (CommonTree)adaptor.create(BOOLTOK51);
					adaptor.addChild(root_0, BOOLTOK51_tree);
					}

					}
					break;
				case 11 :
					// org/eclipse/tracecompass/ctf/parser/CTFParser.g:298:5: COMPLEXTOK
					{
					root_0 = (CommonTree)adaptor.nil();


					COMPLEXTOK52=(Token)match(input,COMPLEXTOK,FOLLOW_COMPLEXTOK_in_typeSpecifier1172); if (state.failed) return retval;
					if ( state.backtracking==0 ) {
					COMPLEXTOK52_tree = (CommonTree)adaptor.create(COMPLEXTOK52);
					adaptor.addChild(root_0, COMPLEXTOK52_tree);
					}

					}
					break;
				case 12 :
					// org/eclipse/tracecompass/ctf/parser/CTFParser.g:299:5: IMAGINARYTOK
					{
					root_0 = (CommonTree)adaptor.nil();


					IMAGINARYTOK53=(Token)match(input,IMAGINARYTOK,FOLLOW_IMAGINARYTOK_in_typeSpecifier1178); if (state.failed) return retval;
					if ( state.backtracking==0 ) {
					IMAGINARYTOK53_tree = (CommonTree)adaptor.create(IMAGINARYTOK53);
					adaptor.addChild(root_0, IMAGINARYTOK53_tree);
					}

					}
					break;
				case 13 :
					// org/eclipse/tracecompass/ctf/parser/CTFParser.g:300:5: structSpecifier
					{
					root_0 = (CommonTree)adaptor.nil();


					pushFollow(FOLLOW_structSpecifier_in_typeSpecifier1184);
					structSpecifier54=structSpecifier();
					state._fsp--;
					if (state.failed) return retval;
					if ( state.backtracking==0 ) adaptor.addChild(root_0, structSpecifier54.getTree());

					}
					break;
				case 14 :
					// org/eclipse/tracecompass/ctf/parser/CTFParser.g:301:5: variantSpecifier
					{
					root_0 = (CommonTree)adaptor.nil();


					pushFollow(FOLLOW_variantSpecifier_in_typeSpecifier1190);
					variantSpecifier55=variantSpecifier();
					state._fsp--;
					if (state.failed) return retval;
					if ( state.backtracking==0 ) adaptor.addChild(root_0, variantSpecifier55.getTree());

					}
					break;
				case 15 :
					// org/eclipse/tracecompass/ctf/parser/CTFParser.g:302:5: enumSpecifier
					{
					root_0 = (CommonTree)adaptor.nil();


					pushFollow(FOLLOW_enumSpecifier_in_typeSpecifier1196);
					enumSpecifier56=enumSpecifier();
					state._fsp--;
					if (state.failed) return retval;
					if ( state.backtracking==0 ) adaptor.addChild(root_0, enumSpecifier56.getTree());

					}
					break;
				case 16 :
					// org/eclipse/tracecompass/ctf/parser/CTFParser.g:303:5: ctfTypeSpecifier
					{
					root_0 = (CommonTree)adaptor.nil();


					pushFollow(FOLLOW_ctfTypeSpecifier_in_typeSpecifier1202);
					ctfTypeSpecifier57=ctfTypeSpecifier();
					state._fsp--;
					if (state.failed) return retval;
					if ( state.backtracking==0 ) adaptor.addChild(root_0, ctfTypeSpecifier57.getTree());

					}
					break;
				case 17 :
					// org/eclipse/tracecompass/ctf/parser/CTFParser.g:304:5: {...}? => typedefName
					{
					root_0 = (CommonTree)adaptor.nil();


					if ( !(( inTypealiasAlias() || isTypeName(input.LT(1).getText()) )) ) {
						if (state.backtracking>0) {state.failed=true; return retval;}
						throw new FailedPredicateException(input, "typeSpecifier", " inTypealiasAlias() || isTypeName(input.LT(1).getText()) ");
					}
					pushFollow(FOLLOW_typedefName_in_typeSpecifier1212);
					typedefName58=typedefName();
					state._fsp--;
					if (state.failed) return retval;
					if ( state.backtracking==0 ) adaptor.addChild(root_0, typedefName58.getTree());

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
	// org/eclipse/tracecompass/ctf/parser/CTFParser.g:307:1: typeQualifier : CONSTTOK ;
	public final CTFParser.typeQualifier_return typeQualifier() throws RecognitionException {
		CTFParser.typeQualifier_return retval = new CTFParser.typeQualifier_return();
		retval.start = input.LT(1);

		CommonTree root_0 = null;

		Token CONSTTOK59=null;

		CommonTree CONSTTOK59_tree=null;

		try {
			// org/eclipse/tracecompass/ctf/parser/CTFParser.g:308:3: ( CONSTTOK )
			// org/eclipse/tracecompass/ctf/parser/CTFParser.g:308:5: CONSTTOK
			{
			root_0 = (CommonTree)adaptor.nil();


			CONSTTOK59=(Token)match(input,CONSTTOK,FOLLOW_CONSTTOK_in_typeQualifier1225); if (state.failed) return retval;
			if ( state.backtracking==0 ) {
			CONSTTOK59_tree = (CommonTree)adaptor.create(CONSTTOK59);
			adaptor.addChild(root_0, CONSTTOK59_tree);
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
	// org/eclipse/tracecompass/ctf/parser/CTFParser.g:311:1: alignAttribute : ALIGNTOK LPAREN unaryExpression RPAREN -> ^( ALIGN unaryExpression ) ;
	public final CTFParser.alignAttribute_return alignAttribute() throws RecognitionException {
		CTFParser.alignAttribute_return retval = new CTFParser.alignAttribute_return();
		retval.start = input.LT(1);

		CommonTree root_0 = null;

		Token ALIGNTOK60=null;
		Token LPAREN61=null;
		Token RPAREN63=null;
		ParserRuleReturnScope unaryExpression62 =null;

		CommonTree ALIGNTOK60_tree=null;
		CommonTree LPAREN61_tree=null;
		CommonTree RPAREN63_tree=null;
		RewriteRuleTokenStream stream_RPAREN=new RewriteRuleTokenStream(adaptor,"token RPAREN");
		RewriteRuleTokenStream stream_ALIGNTOK=new RewriteRuleTokenStream(adaptor,"token ALIGNTOK");
		RewriteRuleTokenStream stream_LPAREN=new RewriteRuleTokenStream(adaptor,"token LPAREN");
		RewriteRuleSubtreeStream stream_unaryExpression=new RewriteRuleSubtreeStream(adaptor,"rule unaryExpression");

		try {
			// org/eclipse/tracecompass/ctf/parser/CTFParser.g:312:3: ( ALIGNTOK LPAREN unaryExpression RPAREN -> ^( ALIGN unaryExpression ) )
			// org/eclipse/tracecompass/ctf/parser/CTFParser.g:312:5: ALIGNTOK LPAREN unaryExpression RPAREN
			{
			ALIGNTOK60=(Token)match(input,ALIGNTOK,FOLLOW_ALIGNTOK_in_alignAttribute1238); if (state.failed) return retval; 
			if ( state.backtracking==0 ) stream_ALIGNTOK.add(ALIGNTOK60);

			LPAREN61=(Token)match(input,LPAREN,FOLLOW_LPAREN_in_alignAttribute1240); if (state.failed) return retval; 
			if ( state.backtracking==0 ) stream_LPAREN.add(LPAREN61);

			pushFollow(FOLLOW_unaryExpression_in_alignAttribute1242);
			unaryExpression62=unaryExpression();
			state._fsp--;
			if (state.failed) return retval;
			if ( state.backtracking==0 ) stream_unaryExpression.add(unaryExpression62.getTree());
			RPAREN63=(Token)match(input,RPAREN,FOLLOW_RPAREN_in_alignAttribute1244); if (state.failed) return retval; 
			if ( state.backtracking==0 ) stream_RPAREN.add(RPAREN63);

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
			// 312:44: -> ^( ALIGN unaryExpression )
			{
				// org/eclipse/tracecompass/ctf/parser/CTFParser.g:312:47: ^( ALIGN unaryExpression )
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
	// org/eclipse/tracecompass/ctf/parser/CTFParser.g:316:1: structBody : LCURL ( structOrVariantDeclarationList )? RCURL -> ^( STRUCT_BODY ( structOrVariantDeclarationList )? ) ;
	public final CTFParser.structBody_return structBody() throws RecognitionException {
		Symbols_stack.push(new Symbols_scope());

		CTFParser.structBody_return retval = new CTFParser.structBody_return();
		retval.start = input.LT(1);

		CommonTree root_0 = null;

		Token LCURL64=null;
		Token RCURL66=null;
		ParserRuleReturnScope structOrVariantDeclarationList65 =null;

		CommonTree LCURL64_tree=null;
		CommonTree RCURL66_tree=null;
		RewriteRuleTokenStream stream_LCURL=new RewriteRuleTokenStream(adaptor,"token LCURL");
		RewriteRuleTokenStream stream_RCURL=new RewriteRuleTokenStream(adaptor,"token RCURL");
		RewriteRuleSubtreeStream stream_structOrVariantDeclarationList=new RewriteRuleSubtreeStream(adaptor,"rule structOrVariantDeclarationList");


		    Symbols_stack.peek().types = new HashSet<String>();

		try {
			// org/eclipse/tracecompass/ctf/parser/CTFParser.g:321:3: ( LCURL ( structOrVariantDeclarationList )? RCURL -> ^( STRUCT_BODY ( structOrVariantDeclarationList )? ) )
			// org/eclipse/tracecompass/ctf/parser/CTFParser.g:321:5: LCURL ( structOrVariantDeclarationList )? RCURL
			{
			LCURL64=(Token)match(input,LCURL,FOLLOW_LCURL_in_structBody1278); if (state.failed) return retval; 
			if ( state.backtracking==0 ) stream_LCURL.add(LCURL64);

			// org/eclipse/tracecompass/ctf/parser/CTFParser.g:321:11: ( structOrVariantDeclarationList )?
			int alt18=2;
			int LA18_0 = input.LA(1);
			if ( (LA18_0==BOOLTOK||LA18_0==CHARTOK||(LA18_0 >= COMPLEXTOK && LA18_0 <= CONSTTOK)||LA18_0==DOUBLETOK||LA18_0==ENUMTOK||(LA18_0 >= FLOATINGPOINTTOK && LA18_0 <= FLOATTOK)||LA18_0==IMAGINARYTOK||LA18_0==INTEGERTOK||LA18_0==INTTOK||LA18_0==LONGTOK||LA18_0==SHORTTOK||LA18_0==SIGNEDTOK||LA18_0==STRINGTOK||LA18_0==STRUCTTOK||LA18_0==TYPEDEFTOK||(LA18_0 >= UNSIGNEDTOK && LA18_0 <= VOIDTOK)) ) {
				alt18=1;
			}
			else if ( (LA18_0==IDENTIFIER) && (( inTypealiasAlias() || isTypeName(input.LT(1).getText()) ))) {
				alt18=1;
			}
			else if ( (LA18_0==TYPEALIASTOK) ) {
				alt18=1;
			}
			switch (alt18) {
				case 1 :
					// org/eclipse/tracecompass/ctf/parser/CTFParser.g:321:11: structOrVariantDeclarationList
					{
					pushFollow(FOLLOW_structOrVariantDeclarationList_in_structBody1280);
					structOrVariantDeclarationList65=structOrVariantDeclarationList();
					state._fsp--;
					if (state.failed) return retval;
					if ( state.backtracking==0 ) stream_structOrVariantDeclarationList.add(structOrVariantDeclarationList65.getTree());
					}
					break;

			}

			RCURL66=(Token)match(input,RCURL,FOLLOW_RCURL_in_structBody1283); if (state.failed) return retval; 
			if ( state.backtracking==0 ) stream_RCURL.add(RCURL66);

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
			// 322:7: -> ^( STRUCT_BODY ( structOrVariantDeclarationList )? )
			{
				// org/eclipse/tracecompass/ctf/parser/CTFParser.g:322:10: ^( STRUCT_BODY ( structOrVariantDeclarationList )? )
				{
				CommonTree root_1 = (CommonTree)adaptor.nil();
				root_1 = (CommonTree)adaptor.becomeRoot((CommonTree)adaptor.create(STRUCT_BODY, "STRUCT_BODY"), root_1);
				// org/eclipse/tracecompass/ctf/parser/CTFParser.g:322:24: ( structOrVariantDeclarationList )?
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
	// org/eclipse/tracecompass/ctf/parser/CTFParser.g:325:1: structSpecifier : STRUCTTOK ( ( structName ( alignAttribute | ( structBody ( alignAttribute |) ) |) ) | ( structBody ( alignAttribute |) ) ) -> ^( STRUCT ( structName )? ( structBody )? ( alignAttribute )? ) ;
	public final CTFParser.structSpecifier_return structSpecifier() throws RecognitionException {
		CTFParser.structSpecifier_return retval = new CTFParser.structSpecifier_return();
		retval.start = input.LT(1);

		CommonTree root_0 = null;

		Token STRUCTTOK67=null;
		ParserRuleReturnScope structName68 =null;
		ParserRuleReturnScope alignAttribute69 =null;
		ParserRuleReturnScope structBody70 =null;
		ParserRuleReturnScope alignAttribute71 =null;
		ParserRuleReturnScope structBody72 =null;
		ParserRuleReturnScope alignAttribute73 =null;

		CommonTree STRUCTTOK67_tree=null;
		RewriteRuleTokenStream stream_STRUCTTOK=new RewriteRuleTokenStream(adaptor,"token STRUCTTOK");
		RewriteRuleSubtreeStream stream_structName=new RewriteRuleSubtreeStream(adaptor,"rule structName");
		RewriteRuleSubtreeStream stream_structBody=new RewriteRuleSubtreeStream(adaptor,"rule structBody");
		RewriteRuleSubtreeStream stream_alignAttribute=new RewriteRuleSubtreeStream(adaptor,"rule alignAttribute");

		try {
			// org/eclipse/tracecompass/ctf/parser/CTFParser.g:326:3: ( STRUCTTOK ( ( structName ( alignAttribute | ( structBody ( alignAttribute |) ) |) ) | ( structBody ( alignAttribute |) ) ) -> ^( STRUCT ( structName )? ( structBody )? ( alignAttribute )? ) )
			// org/eclipse/tracecompass/ctf/parser/CTFParser.g:326:5: STRUCTTOK ( ( structName ( alignAttribute | ( structBody ( alignAttribute |) ) |) ) | ( structBody ( alignAttribute |) ) )
			{
			STRUCTTOK67=(Token)match(input,STRUCTTOK,FOLLOW_STRUCTTOK_in_structSpecifier1311); if (state.failed) return retval; 
			if ( state.backtracking==0 ) stream_STRUCTTOK.add(STRUCTTOK67);

			// org/eclipse/tracecompass/ctf/parser/CTFParser.g:327:3: ( ( structName ( alignAttribute | ( structBody ( alignAttribute |) ) |) ) | ( structBody ( alignAttribute |) ) )
			int alt22=2;
			int LA22_0 = input.LA(1);
			if ( (LA22_0==IDENTIFIER) ) {
				alt22=1;
			}
			else if ( (LA22_0==LCURL) ) {
				alt22=2;
			}

			else {
				if (state.backtracking>0) {state.failed=true; return retval;}
				NoViableAltException nvae =
					new NoViableAltException("", 22, 0, input);
				throw nvae;
			}

			switch (alt22) {
				case 1 :
					// org/eclipse/tracecompass/ctf/parser/CTFParser.g:329:5: ( structName ( alignAttribute | ( structBody ( alignAttribute |) ) |) )
					{
					// org/eclipse/tracecompass/ctf/parser/CTFParser.g:329:5: ( structName ( alignAttribute | ( structBody ( alignAttribute |) ) |) )
					// org/eclipse/tracecompass/ctf/parser/CTFParser.g:330:9: structName ( alignAttribute | ( structBody ( alignAttribute |) ) |)
					{
					pushFollow(FOLLOW_structName_in_structSpecifier1336);
					structName68=structName();
					state._fsp--;
					if (state.failed) return retval;
					if ( state.backtracking==0 ) stream_structName.add(structName68.getTree());
					// org/eclipse/tracecompass/ctf/parser/CTFParser.g:331:9: ( alignAttribute | ( structBody ( alignAttribute |) ) |)
					int alt20=3;
					switch ( input.LA(1) ) {
					case ALIGNTOK:
						{
						alt20=1;
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
							alt20=2;
							}
							break;
						case SIGNEDTOK:
							{
							int LA20_5 = input.LA(3);
							if ( (LA20_5==BOOLTOK||LA20_5==CHARTOK||(LA20_5 >= COMPLEXTOK && LA20_5 <= CONSTTOK)||LA20_5==DOUBLETOK||LA20_5==ENUMTOK||(LA20_5 >= FLOATINGPOINTTOK && LA20_5 <= FLOATTOK)||(LA20_5 >= IDENTIFIER && LA20_5 <= IMAGINARYTOK)||LA20_5==INTEGERTOK||LA20_5==INTTOK||LA20_5==LONGTOK||LA20_5==POINTER||LA20_5==SHORTTOK||LA20_5==SIGNEDTOK||LA20_5==STRINGTOK||LA20_5==STRUCTTOK||LA20_5==TYPEDEFTOK||(LA20_5 >= UNSIGNEDTOK && LA20_5 <= VOIDTOK)) ) {
								alt20=2;
							}
							else if ( (LA20_5==ASSIGNMENT||LA20_5==RCURL||LA20_5==SEPARATOR) ) {
								alt20=3;
							}

							else {
								if (state.backtracking>0) {state.failed=true; return retval;}
								int nvaeMark = input.mark();
								try {
									for (int nvaeConsume = 0; nvaeConsume < 3 - 1; nvaeConsume++) {
										input.consume();
									}
									NoViableAltException nvae =
										new NoViableAltException("", 20, 5, input);
									throw nvae;
								} finally {
									input.rewind(nvaeMark);
								}
							}

							}
							break;
						case STRINGTOK:
							{
							int LA20_6 = input.LA(3);
							if ( (LA20_6==BOOLTOK||LA20_6==CHARTOK||(LA20_6 >= COMPLEXTOK && LA20_6 <= CONSTTOK)||LA20_6==DOUBLETOK||LA20_6==ENUMTOK||(LA20_6 >= FLOATINGPOINTTOK && LA20_6 <= FLOATTOK)||(LA20_6 >= IDENTIFIER && LA20_6 <= IMAGINARYTOK)||LA20_6==INTEGERTOK||(LA20_6 >= INTTOK && LA20_6 <= LCURL)||LA20_6==LONGTOK||LA20_6==POINTER||LA20_6==SHORTTOK||LA20_6==SIGNEDTOK||LA20_6==STRINGTOK||LA20_6==STRUCTTOK||LA20_6==TYPEDEFTOK||(LA20_6 >= UNSIGNEDTOK && LA20_6 <= VOIDTOK)) ) {
								alt20=2;
							}
							else if ( (LA20_6==ASSIGNMENT||LA20_6==RCURL||LA20_6==SEPARATOR) ) {
								alt20=3;
							}

							else {
								if (state.backtracking>0) {state.failed=true; return retval;}
								int nvaeMark = input.mark();
								try {
									for (int nvaeConsume = 0; nvaeConsume < 3 - 1; nvaeConsume++) {
										input.consume();
									}
									NoViableAltException nvae =
										new NoViableAltException("", 20, 6, input);
									throw nvae;
								} finally {
									input.rewind(nvaeMark);
								}
							}

							}
							break;
						case IDENTIFIER:
							{
							int LA20_7 = input.LA(3);
							if ( (LA20_7==BOOLTOK||LA20_7==CHARTOK||(LA20_7 >= COMPLEXTOK && LA20_7 <= CONSTTOK)||LA20_7==DOUBLETOK||LA20_7==ENUMTOK||(LA20_7 >= FLOATINGPOINTTOK && LA20_7 <= FLOATTOK)||(LA20_7 >= IDENTIFIER && LA20_7 <= IMAGINARYTOK)||LA20_7==INTEGERTOK||LA20_7==INTTOK||LA20_7==LONGTOK||LA20_7==POINTER||LA20_7==SHORTTOK||LA20_7==SIGNEDTOK||LA20_7==STRINGTOK||LA20_7==STRUCTTOK||LA20_7==TYPEDEFTOK||(LA20_7 >= UNSIGNEDTOK && LA20_7 <= VOIDTOK)) ) {
								alt20=2;
							}
							else if ( (LA20_7==ASSIGNMENT||LA20_7==RCURL||LA20_7==SEPARATOR) ) {
								alt20=3;
							}

							else {
								if (state.backtracking>0) {state.failed=true; return retval;}
								int nvaeMark = input.mark();
								try {
									for (int nvaeConsume = 0; nvaeConsume < 3 - 1; nvaeConsume++) {
										input.consume();
									}
									NoViableAltException nvae =
										new NoViableAltException("", 20, 7, input);
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
							alt20=3;
							}
							break;
						default:
							if (state.backtracking>0) {state.failed=true; return retval;}
							int nvaeMark = input.mark();
							try {
								input.consume();
								NoViableAltException nvae =
									new NoViableAltException("", 20, 2, input);
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
						alt20=3;
						}
						break;
					default:
						if (state.backtracking>0) {state.failed=true; return retval;}
						NoViableAltException nvae =
							new NoViableAltException("", 20, 0, input);
						throw nvae;
					}
					switch (alt20) {
						case 1 :
							// org/eclipse/tracecompass/ctf/parser/CTFParser.g:332:11: alignAttribute
							{
							pushFollow(FOLLOW_alignAttribute_in_structSpecifier1358);
							alignAttribute69=alignAttribute();
							state._fsp--;
							if (state.failed) return retval;
							if ( state.backtracking==0 ) stream_alignAttribute.add(alignAttribute69.getTree());
							}
							break;
						case 2 :
							// org/eclipse/tracecompass/ctf/parser/CTFParser.g:334:11: ( structBody ( alignAttribute |) )
							{
							// org/eclipse/tracecompass/ctf/parser/CTFParser.g:334:11: ( structBody ( alignAttribute |) )
							// org/eclipse/tracecompass/ctf/parser/CTFParser.g:335:13: structBody ( alignAttribute |)
							{
							pushFollow(FOLLOW_structBody_in_structSpecifier1394);
							structBody70=structBody();
							state._fsp--;
							if (state.failed) return retval;
							if ( state.backtracking==0 ) stream_structBody.add(structBody70.getTree());
							// org/eclipse/tracecompass/ctf/parser/CTFParser.g:336:13: ( alignAttribute |)
							int alt19=2;
							int LA19_0 = input.LA(1);
							if ( (LA19_0==ALIGNTOK) ) {
								alt19=1;
							}
							else if ( (LA19_0==EOF||LA19_0==BOOLTOK||LA19_0==CHARTOK||(LA19_0 >= COMPLEXTOK && LA19_0 <= CONSTTOK)||LA19_0==DOUBLETOK||LA19_0==ENUMTOK||(LA19_0 >= FLOATINGPOINTTOK && LA19_0 <= FLOATTOK)||(LA19_0 >= IDENTIFIER && LA19_0 <= IMAGINARYTOK)||LA19_0==INTEGERTOK||(LA19_0 >= INTTOK && LA19_0 <= LCURL)||(LA19_0 >= LONGTOK && LA19_0 <= LPAREN)||LA19_0==POINTER||LA19_0==SHORTTOK||LA19_0==SIGNEDTOK||LA19_0==STRINGTOK||(LA19_0 >= STRUCTTOK && LA19_0 <= TERM)||(LA19_0 >= TYPEDEFTOK && LA19_0 <= TYPE_ASSIGNMENT)||(LA19_0 >= UNSIGNEDTOK && LA19_0 <= VOIDTOK)) ) {
								alt19=2;
							}

							else {
								if (state.backtracking>0) {state.failed=true; return retval;}
								NoViableAltException nvae =
									new NoViableAltException("", 19, 0, input);
								throw nvae;
							}

							switch (alt19) {
								case 1 :
									// org/eclipse/tracecompass/ctf/parser/CTFParser.g:337:14: alignAttribute
									{
									pushFollow(FOLLOW_alignAttribute_in_structSpecifier1425);
									alignAttribute71=alignAttribute();
									state._fsp--;
									if (state.failed) return retval;
									if ( state.backtracking==0 ) stream_alignAttribute.add(alignAttribute71.getTree());
									}
									break;
								case 2 :
									// org/eclipse/tracecompass/ctf/parser/CTFParser.g:340:13: 
									{
									}
									break;

							}

							}

							}
							break;
						case 3 :
							// org/eclipse/tracecompass/ctf/parser/CTFParser.g:344:9: 
							{
							}
							break;

					}

					}

					}
					break;
				case 2 :
					// org/eclipse/tracecompass/ctf/parser/CTFParser.g:348:5: ( structBody ( alignAttribute |) )
					{
					// org/eclipse/tracecompass/ctf/parser/CTFParser.g:348:5: ( structBody ( alignAttribute |) )
					// org/eclipse/tracecompass/ctf/parser/CTFParser.g:349:7: structBody ( alignAttribute |)
					{
					pushFollow(FOLLOW_structBody_in_structSpecifier1541);
					structBody72=structBody();
					state._fsp--;
					if (state.failed) return retval;
					if ( state.backtracking==0 ) stream_structBody.add(structBody72.getTree());
					// org/eclipse/tracecompass/ctf/parser/CTFParser.g:350:7: ( alignAttribute |)
					int alt21=2;
					int LA21_0 = input.LA(1);
					if ( (LA21_0==ALIGNTOK) ) {
						alt21=1;
					}
					else if ( (LA21_0==EOF||LA21_0==BOOLTOK||LA21_0==CHARTOK||(LA21_0 >= COMPLEXTOK && LA21_0 <= CONSTTOK)||LA21_0==DOUBLETOK||LA21_0==ENUMTOK||(LA21_0 >= FLOATINGPOINTTOK && LA21_0 <= FLOATTOK)||(LA21_0 >= IDENTIFIER && LA21_0 <= IMAGINARYTOK)||LA21_0==INTEGERTOK||(LA21_0 >= INTTOK && LA21_0 <= LCURL)||(LA21_0 >= LONGTOK && LA21_0 <= LPAREN)||LA21_0==POINTER||LA21_0==SHORTTOK||LA21_0==SIGNEDTOK||LA21_0==STRINGTOK||(LA21_0 >= STRUCTTOK && LA21_0 <= TERM)||(LA21_0 >= TYPEDEFTOK && LA21_0 <= TYPE_ASSIGNMENT)||(LA21_0 >= UNSIGNEDTOK && LA21_0 <= VOIDTOK)) ) {
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
							// org/eclipse/tracecompass/ctf/parser/CTFParser.g:351:9: alignAttribute
							{
							pushFollow(FOLLOW_alignAttribute_in_structSpecifier1559);
							alignAttribute73=alignAttribute();
							state._fsp--;
							if (state.failed) return retval;
							if ( state.backtracking==0 ) stream_alignAttribute.add(alignAttribute73.getTree());
							}
							break;
						case 2 :
							// org/eclipse/tracecompass/ctf/parser/CTFParser.g:354:7: 
							{
							}
							break;

					}

					}

					}
					break;

			}

			// AST REWRITE
			// elements: structBody, alignAttribute, structName
			// token labels: 
			// rule labels: retval
			// token list labels: 
			// rule list labels: 
			// wildcard labels: 
			if ( state.backtracking==0 ) {
			retval.tree = root_0;
			RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.getTree():null);

			root_0 = (CommonTree)adaptor.nil();
			// 356:5: -> ^( STRUCT ( structName )? ( structBody )? ( alignAttribute )? )
			{
				// org/eclipse/tracecompass/ctf/parser/CTFParser.g:356:8: ^( STRUCT ( structName )? ( structBody )? ( alignAttribute )? )
				{
				CommonTree root_1 = (CommonTree)adaptor.nil();
				root_1 = (CommonTree)adaptor.becomeRoot((CommonTree)adaptor.create(STRUCT, "STRUCT"), root_1);
				// org/eclipse/tracecompass/ctf/parser/CTFParser.g:356:17: ( structName )?
				if ( stream_structName.hasNext() ) {
					adaptor.addChild(root_1, stream_structName.nextTree());
				}
				stream_structName.reset();

				// org/eclipse/tracecompass/ctf/parser/CTFParser.g:356:29: ( structBody )?
				if ( stream_structBody.hasNext() ) {
					adaptor.addChild(root_1, stream_structBody.nextTree());
				}
				stream_structBody.reset();

				// org/eclipse/tracecompass/ctf/parser/CTFParser.g:356:41: ( alignAttribute )?
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
	// org/eclipse/tracecompass/ctf/parser/CTFParser.g:359:1: structName : IDENTIFIER -> ^( STRUCT_NAME IDENTIFIER ) ;
	public final CTFParser.structName_return structName() throws RecognitionException {
		CTFParser.structName_return retval = new CTFParser.structName_return();
		retval.start = input.LT(1);

		CommonTree root_0 = null;

		Token IDENTIFIER74=null;

		CommonTree IDENTIFIER74_tree=null;
		RewriteRuleTokenStream stream_IDENTIFIER=new RewriteRuleTokenStream(adaptor,"token IDENTIFIER");

		try {
			// org/eclipse/tracecompass/ctf/parser/CTFParser.g:360:3: ( IDENTIFIER -> ^( STRUCT_NAME IDENTIFIER ) )
			// org/eclipse/tracecompass/ctf/parser/CTFParser.g:360:5: IDENTIFIER
			{
			IDENTIFIER74=(Token)match(input,IDENTIFIER,FOLLOW_IDENTIFIER_in_structName1625); if (state.failed) return retval; 
			if ( state.backtracking==0 ) stream_IDENTIFIER.add(IDENTIFIER74);

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
			// 360:16: -> ^( STRUCT_NAME IDENTIFIER )
			{
				// org/eclipse/tracecompass/ctf/parser/CTFParser.g:360:19: ^( STRUCT_NAME IDENTIFIER )
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
	// org/eclipse/tracecompass/ctf/parser/CTFParser.g:363:1: structOrVariantDeclarationList : ( structOrVariantDeclaration )+ ;
	public final CTFParser.structOrVariantDeclarationList_return structOrVariantDeclarationList() throws RecognitionException {
		CTFParser.structOrVariantDeclarationList_return retval = new CTFParser.structOrVariantDeclarationList_return();
		retval.start = input.LT(1);

		CommonTree root_0 = null;

		ParserRuleReturnScope structOrVariantDeclaration75 =null;


		try {
			// org/eclipse/tracecompass/ctf/parser/CTFParser.g:364:3: ( ( structOrVariantDeclaration )+ )
			// org/eclipse/tracecompass/ctf/parser/CTFParser.g:364:5: ( structOrVariantDeclaration )+
			{
			root_0 = (CommonTree)adaptor.nil();


			// org/eclipse/tracecompass/ctf/parser/CTFParser.g:364:5: ( structOrVariantDeclaration )+
			int cnt23=0;
			loop23:
			while (true) {
				int alt23=2;
				int LA23_0 = input.LA(1);
				if ( (LA23_0==BOOLTOK||LA23_0==CHARTOK||(LA23_0 >= COMPLEXTOK && LA23_0 <= CONSTTOK)||LA23_0==DOUBLETOK||LA23_0==ENUMTOK||(LA23_0 >= FLOATINGPOINTTOK && LA23_0 <= FLOATTOK)||LA23_0==IMAGINARYTOK||LA23_0==INTEGERTOK||LA23_0==INTTOK||LA23_0==LONGTOK||LA23_0==SHORTTOK||LA23_0==SIGNEDTOK||LA23_0==STRINGTOK||LA23_0==STRUCTTOK||LA23_0==TYPEDEFTOK||(LA23_0 >= UNSIGNEDTOK && LA23_0 <= VOIDTOK)) ) {
					alt23=1;
				}
				else if ( (LA23_0==IDENTIFIER) && (( inTypealiasAlias() || isTypeName(input.LT(1).getText()) ))) {
					alt23=1;
				}
				else if ( (LA23_0==TYPEALIASTOK) ) {
					alt23=1;
				}

				switch (alt23) {
				case 1 :
					// org/eclipse/tracecompass/ctf/parser/CTFParser.g:364:5: structOrVariantDeclaration
					{
					pushFollow(FOLLOW_structOrVariantDeclaration_in_structOrVariantDeclarationList1646);
					structOrVariantDeclaration75=structOrVariantDeclaration();
					state._fsp--;
					if (state.failed) return retval;
					if ( state.backtracking==0 ) adaptor.addChild(root_0, structOrVariantDeclaration75.getTree());

					}
					break;

				default :
					if ( cnt23 >= 1 ) break loop23;
					if (state.backtracking>0) {state.failed=true; return retval;}
					EarlyExitException eee = new EarlyExitException(23, input);
					throw eee;
				}
				cnt23++;
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
	// org/eclipse/tracecompass/ctf/parser/CTFParser.g:367:1: structOrVariantDeclaration : ( ( declarationSpecifiers ({...}? => declaratorList -> ^( TYPEDEF declaratorList declarationSpecifiers ) | structOrVariantDeclaratorList -> ^( SV_DECLARATION declarationSpecifiers structOrVariantDeclaratorList ) ) ) | typealiasDecl -> typealiasDecl ) TERM ;
	public final CTFParser.structOrVariantDeclaration_return structOrVariantDeclaration() throws RecognitionException {
		CTFParser.structOrVariantDeclaration_return retval = new CTFParser.structOrVariantDeclaration_return();
		retval.start = input.LT(1);

		CommonTree root_0 = null;

		Token TERM80=null;
		ParserRuleReturnScope declarationSpecifiers76 =null;
		ParserRuleReturnScope declaratorList77 =null;
		ParserRuleReturnScope structOrVariantDeclaratorList78 =null;
		ParserRuleReturnScope typealiasDecl79 =null;

		CommonTree TERM80_tree=null;
		RewriteRuleTokenStream stream_TERM=new RewriteRuleTokenStream(adaptor,"token TERM");
		RewriteRuleSubtreeStream stream_declaratorList=new RewriteRuleSubtreeStream(adaptor,"rule declaratorList");
		RewriteRuleSubtreeStream stream_typealiasDecl=new RewriteRuleSubtreeStream(adaptor,"rule typealiasDecl");
		RewriteRuleSubtreeStream stream_declarationSpecifiers=new RewriteRuleSubtreeStream(adaptor,"rule declarationSpecifiers");
		RewriteRuleSubtreeStream stream_structOrVariantDeclaratorList=new RewriteRuleSubtreeStream(adaptor,"rule structOrVariantDeclaratorList");

		try {
			// org/eclipse/tracecompass/ctf/parser/CTFParser.g:368:3: ( ( ( declarationSpecifiers ({...}? => declaratorList -> ^( TYPEDEF declaratorList declarationSpecifiers ) | structOrVariantDeclaratorList -> ^( SV_DECLARATION declarationSpecifiers structOrVariantDeclaratorList ) ) ) | typealiasDecl -> typealiasDecl ) TERM )
			// org/eclipse/tracecompass/ctf/parser/CTFParser.g:369:3: ( ( declarationSpecifiers ({...}? => declaratorList -> ^( TYPEDEF declaratorList declarationSpecifiers ) | structOrVariantDeclaratorList -> ^( SV_DECLARATION declarationSpecifiers structOrVariantDeclaratorList ) ) ) | typealiasDecl -> typealiasDecl ) TERM
			{
			// org/eclipse/tracecompass/ctf/parser/CTFParser.g:369:3: ( ( declarationSpecifiers ({...}? => declaratorList -> ^( TYPEDEF declaratorList declarationSpecifiers ) | structOrVariantDeclaratorList -> ^( SV_DECLARATION declarationSpecifiers structOrVariantDeclaratorList ) ) ) | typealiasDecl -> typealiasDecl )
			int alt25=2;
			int LA25_0 = input.LA(1);
			if ( (LA25_0==BOOLTOK||LA25_0==CHARTOK||(LA25_0 >= COMPLEXTOK && LA25_0 <= CONSTTOK)||LA25_0==DOUBLETOK||LA25_0==ENUMTOK||(LA25_0 >= FLOATINGPOINTTOK && LA25_0 <= FLOATTOK)||LA25_0==IMAGINARYTOK||LA25_0==INTEGERTOK||LA25_0==INTTOK||LA25_0==LONGTOK||LA25_0==SHORTTOK||LA25_0==SIGNEDTOK||LA25_0==STRINGTOK||LA25_0==STRUCTTOK||LA25_0==TYPEDEFTOK||(LA25_0 >= UNSIGNEDTOK && LA25_0 <= VOIDTOK)) ) {
				alt25=1;
			}
			else if ( (LA25_0==IDENTIFIER) && (( inTypealiasAlias() || isTypeName(input.LT(1).getText()) ))) {
				alt25=1;
			}
			else if ( (LA25_0==TYPEALIASTOK) ) {
				alt25=2;
			}

			else {
				if (state.backtracking>0) {state.failed=true; return retval;}
				NoViableAltException nvae =
					new NoViableAltException("", 25, 0, input);
				throw nvae;
			}

			switch (alt25) {
				case 1 :
					// org/eclipse/tracecompass/ctf/parser/CTFParser.g:370:7: ( declarationSpecifiers ({...}? => declaratorList -> ^( TYPEDEF declaratorList declarationSpecifiers ) | structOrVariantDeclaratorList -> ^( SV_DECLARATION declarationSpecifiers structOrVariantDeclaratorList ) ) )
					{
					// org/eclipse/tracecompass/ctf/parser/CTFParser.g:370:7: ( declarationSpecifiers ({...}? => declaratorList -> ^( TYPEDEF declaratorList declarationSpecifiers ) | structOrVariantDeclaratorList -> ^( SV_DECLARATION declarationSpecifiers structOrVariantDeclaratorList ) ) )
					// org/eclipse/tracecompass/ctf/parser/CTFParser.g:371:8: declarationSpecifiers ({...}? => declaratorList -> ^( TYPEDEF declaratorList declarationSpecifiers ) | structOrVariantDeclaratorList -> ^( SV_DECLARATION declarationSpecifiers structOrVariantDeclaratorList ) )
					{
					pushFollow(FOLLOW_declarationSpecifiers_in_structOrVariantDeclaration1679);
					declarationSpecifiers76=declarationSpecifiers();
					state._fsp--;
					if (state.failed) return retval;
					if ( state.backtracking==0 ) stream_declarationSpecifiers.add(declarationSpecifiers76.getTree());
					// org/eclipse/tracecompass/ctf/parser/CTFParser.g:372:10: ({...}? => declaratorList -> ^( TYPEDEF declaratorList declarationSpecifiers ) | structOrVariantDeclaratorList -> ^( SV_DECLARATION declarationSpecifiers structOrVariantDeclaratorList ) )
					int alt24=2;
					alt24 = dfa24.predict(input);
					switch (alt24) {
						case 1 :
							// org/eclipse/tracecompass/ctf/parser/CTFParser.g:374:12: {...}? => declaratorList
							{
							if ( !((inTypedef())) ) {
								if (state.backtracking>0) {state.failed=true; return retval;}
								throw new FailedPredicateException(input, "structOrVariantDeclaration", "inTypedef()");
							}
							pushFollow(FOLLOW_declaratorList_in_structOrVariantDeclaration1720);
							declaratorList77=declaratorList();
							state._fsp--;
							if (state.failed) return retval;
							if ( state.backtracking==0 ) stream_declaratorList.add(declaratorList77.getTree());
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
							// 375:14: -> ^( TYPEDEF declaratorList declarationSpecifiers )
							{
								// org/eclipse/tracecompass/ctf/parser/CTFParser.g:375:17: ^( TYPEDEF declaratorList declarationSpecifiers )
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
							// org/eclipse/tracecompass/ctf/parser/CTFParser.g:376:14: structOrVariantDeclaratorList
							{
							pushFollow(FOLLOW_structOrVariantDeclaratorList_in_structOrVariantDeclaration1758);
							structOrVariantDeclaratorList78=structOrVariantDeclaratorList();
							state._fsp--;
							if (state.failed) return retval;
							if ( state.backtracking==0 ) stream_structOrVariantDeclaratorList.add(structOrVariantDeclaratorList78.getTree());
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
							// 377:14: -> ^( SV_DECLARATION declarationSpecifiers structOrVariantDeclaratorList )
							{
								// org/eclipse/tracecompass/ctf/parser/CTFParser.g:377:17: ^( SV_DECLARATION declarationSpecifiers structOrVariantDeclaratorList )
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
					// org/eclipse/tracecompass/ctf/parser/CTFParser.g:382:5: typealiasDecl
					{
					pushFollow(FOLLOW_typealiasDecl_in_structOrVariantDeclaration1817);
					typealiasDecl79=typealiasDecl();
					state._fsp--;
					if (state.failed) return retval;
					if ( state.backtracking==0 ) stream_typealiasDecl.add(typealiasDecl79.getTree());
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
					// 382:19: -> typealiasDecl
					{
						adaptor.addChild(root_0, stream_typealiasDecl.nextTree());
					}


					retval.tree = root_0;
					}

					}
					break;

			}

			TERM80=(Token)match(input,TERM,FOLLOW_TERM_in_structOrVariantDeclaration1829); if (state.failed) return retval; 
			if ( state.backtracking==0 ) stream_TERM.add(TERM80);

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
	// org/eclipse/tracecompass/ctf/parser/CTFParser.g:387:1: specifierQualifierList : ( typeQualifier | typeSpecifier )+ -> ^( TYPE_SPECIFIER_LIST ( typeQualifier )* ( typeSpecifier )* ) ;
	public final CTFParser.specifierQualifierList_return specifierQualifierList() throws RecognitionException {
		CTFParser.specifierQualifierList_return retval = new CTFParser.specifierQualifierList_return();
		retval.start = input.LT(1);

		CommonTree root_0 = null;

		ParserRuleReturnScope typeQualifier81 =null;
		ParserRuleReturnScope typeSpecifier82 =null;

		RewriteRuleSubtreeStream stream_typeSpecifier=new RewriteRuleSubtreeStream(adaptor,"rule typeSpecifier");
		RewriteRuleSubtreeStream stream_typeQualifier=new RewriteRuleSubtreeStream(adaptor,"rule typeQualifier");

		try {
			// org/eclipse/tracecompass/ctf/parser/CTFParser.g:388:3: ( ( typeQualifier | typeSpecifier )+ -> ^( TYPE_SPECIFIER_LIST ( typeQualifier )* ( typeSpecifier )* ) )
			// org/eclipse/tracecompass/ctf/parser/CTFParser.g:388:5: ( typeQualifier | typeSpecifier )+
			{
			// org/eclipse/tracecompass/ctf/parser/CTFParser.g:388:5: ( typeQualifier | typeSpecifier )+
			int cnt26=0;
			loop26:
			while (true) {
				int alt26=3;
				int LA26_0 = input.LA(1);
				if ( (LA26_0==CONSTTOK) ) {
					alt26=1;
				}
				else if ( (LA26_0==BOOLTOK||LA26_0==CHARTOK||LA26_0==COMPLEXTOK||LA26_0==DOUBLETOK||LA26_0==ENUMTOK||(LA26_0 >= FLOATINGPOINTTOK && LA26_0 <= FLOATTOK)||LA26_0==IMAGINARYTOK||LA26_0==INTEGERTOK||LA26_0==INTTOK||LA26_0==LONGTOK||LA26_0==SHORTTOK||LA26_0==SIGNEDTOK||LA26_0==STRINGTOK||LA26_0==STRUCTTOK||(LA26_0 >= UNSIGNEDTOK && LA26_0 <= VOIDTOK)) ) {
					alt26=2;
				}
				else if ( (LA26_0==IDENTIFIER) && (( inTypealiasAlias() || isTypeName(input.LT(1).getText()) ))) {
					alt26=2;
				}

				switch (alt26) {
				case 1 :
					// org/eclipse/tracecompass/ctf/parser/CTFParser.g:388:6: typeQualifier
					{
					pushFollow(FOLLOW_typeQualifier_in_specifierQualifierList1843);
					typeQualifier81=typeQualifier();
					state._fsp--;
					if (state.failed) return retval;
					if ( state.backtracking==0 ) stream_typeQualifier.add(typeQualifier81.getTree());
					}
					break;
				case 2 :
					// org/eclipse/tracecompass/ctf/parser/CTFParser.g:388:22: typeSpecifier
					{
					pushFollow(FOLLOW_typeSpecifier_in_specifierQualifierList1847);
					typeSpecifier82=typeSpecifier();
					state._fsp--;
					if (state.failed) return retval;
					if ( state.backtracking==0 ) stream_typeSpecifier.add(typeSpecifier82.getTree());
					}
					break;

				default :
					if ( cnt26 >= 1 ) break loop26;
					if (state.backtracking>0) {state.failed=true; return retval;}
					EarlyExitException eee = new EarlyExitException(26, input);
					throw eee;
				}
				cnt26++;
			}

			// AST REWRITE
			// elements: typeSpecifier, typeQualifier
			// token labels: 
			// rule labels: retval
			// token list labels: 
			// rule list labels: 
			// wildcard labels: 
			if ( state.backtracking==0 ) {
			retval.tree = root_0;
			RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.getTree():null);

			root_0 = (CommonTree)adaptor.nil();
			// 389:7: -> ^( TYPE_SPECIFIER_LIST ( typeQualifier )* ( typeSpecifier )* )
			{
				// org/eclipse/tracecompass/ctf/parser/CTFParser.g:389:10: ^( TYPE_SPECIFIER_LIST ( typeQualifier )* ( typeSpecifier )* )
				{
				CommonTree root_1 = (CommonTree)adaptor.nil();
				root_1 = (CommonTree)adaptor.becomeRoot((CommonTree)adaptor.create(TYPE_SPECIFIER_LIST, "TYPE_SPECIFIER_LIST"), root_1);
				// org/eclipse/tracecompass/ctf/parser/CTFParser.g:389:32: ( typeQualifier )*
				while ( stream_typeQualifier.hasNext() ) {
					adaptor.addChild(root_1, stream_typeQualifier.nextTree());
				}
				stream_typeQualifier.reset();

				// org/eclipse/tracecompass/ctf/parser/CTFParser.g:389:47: ( typeSpecifier )*
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
	// org/eclipse/tracecompass/ctf/parser/CTFParser.g:392:1: structOrVariantDeclaratorList : structOrVariantDeclarator ( SEPARATOR structOrVariantDeclarator )* -> ^( TYPE_DECLARATOR_LIST ( structOrVariantDeclarator )+ ) ;
	public final CTFParser.structOrVariantDeclaratorList_return structOrVariantDeclaratorList() throws RecognitionException {
		CTFParser.structOrVariantDeclaratorList_return retval = new CTFParser.structOrVariantDeclaratorList_return();
		retval.start = input.LT(1);

		CommonTree root_0 = null;

		Token SEPARATOR84=null;
		ParserRuleReturnScope structOrVariantDeclarator83 =null;
		ParserRuleReturnScope structOrVariantDeclarator85 =null;

		CommonTree SEPARATOR84_tree=null;
		RewriteRuleTokenStream stream_SEPARATOR=new RewriteRuleTokenStream(adaptor,"token SEPARATOR");
		RewriteRuleSubtreeStream stream_structOrVariantDeclarator=new RewriteRuleSubtreeStream(adaptor,"rule structOrVariantDeclarator");

		try {
			// org/eclipse/tracecompass/ctf/parser/CTFParser.g:393:3: ( structOrVariantDeclarator ( SEPARATOR structOrVariantDeclarator )* -> ^( TYPE_DECLARATOR_LIST ( structOrVariantDeclarator )+ ) )
			// org/eclipse/tracecompass/ctf/parser/CTFParser.g:393:5: structOrVariantDeclarator ( SEPARATOR structOrVariantDeclarator )*
			{
			pushFollow(FOLLOW_structOrVariantDeclarator_in_structOrVariantDeclaratorList1880);
			structOrVariantDeclarator83=structOrVariantDeclarator();
			state._fsp--;
			if (state.failed) return retval;
			if ( state.backtracking==0 ) stream_structOrVariantDeclarator.add(structOrVariantDeclarator83.getTree());
			// org/eclipse/tracecompass/ctf/parser/CTFParser.g:393:31: ( SEPARATOR structOrVariantDeclarator )*
			loop27:
			while (true) {
				int alt27=2;
				int LA27_0 = input.LA(1);
				if ( (LA27_0==SEPARATOR) ) {
					alt27=1;
				}

				switch (alt27) {
				case 1 :
					// org/eclipse/tracecompass/ctf/parser/CTFParser.g:393:32: SEPARATOR structOrVariantDeclarator
					{
					SEPARATOR84=(Token)match(input,SEPARATOR,FOLLOW_SEPARATOR_in_structOrVariantDeclaratorList1883); if (state.failed) return retval; 
					if ( state.backtracking==0 ) stream_SEPARATOR.add(SEPARATOR84);

					pushFollow(FOLLOW_structOrVariantDeclarator_in_structOrVariantDeclaratorList1885);
					structOrVariantDeclarator85=structOrVariantDeclarator();
					state._fsp--;
					if (state.failed) return retval;
					if ( state.backtracking==0 ) stream_structOrVariantDeclarator.add(structOrVariantDeclarator85.getTree());
					}
					break;

				default :
					break loop27;
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
			// 394:7: -> ^( TYPE_DECLARATOR_LIST ( structOrVariantDeclarator )+ )
			{
				// org/eclipse/tracecompass/ctf/parser/CTFParser.g:394:10: ^( TYPE_DECLARATOR_LIST ( structOrVariantDeclarator )+ )
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
	// org/eclipse/tracecompass/ctf/parser/CTFParser.g:397:1: structOrVariantDeclarator : ( declarator ( COLON numberLiteral )? ) -> declarator ;
	public final CTFParser.structOrVariantDeclarator_return structOrVariantDeclarator() throws RecognitionException {
		CTFParser.structOrVariantDeclarator_return retval = new CTFParser.structOrVariantDeclarator_return();
		retval.start = input.LT(1);

		CommonTree root_0 = null;

		Token COLON87=null;
		ParserRuleReturnScope declarator86 =null;
		ParserRuleReturnScope numberLiteral88 =null;

		CommonTree COLON87_tree=null;
		RewriteRuleTokenStream stream_COLON=new RewriteRuleTokenStream(adaptor,"token COLON");
		RewriteRuleSubtreeStream stream_declarator=new RewriteRuleSubtreeStream(adaptor,"rule declarator");
		RewriteRuleSubtreeStream stream_numberLiteral=new RewriteRuleSubtreeStream(adaptor,"rule numberLiteral");

		try {
			// org/eclipse/tracecompass/ctf/parser/CTFParser.g:398:3: ( ( declarator ( COLON numberLiteral )? ) -> declarator )
			// org/eclipse/tracecompass/ctf/parser/CTFParser.g:400:5: ( declarator ( COLON numberLiteral )? )
			{
			// org/eclipse/tracecompass/ctf/parser/CTFParser.g:400:5: ( declarator ( COLON numberLiteral )? )
			// org/eclipse/tracecompass/ctf/parser/CTFParser.g:400:6: declarator ( COLON numberLiteral )?
			{
			pushFollow(FOLLOW_declarator_in_structOrVariantDeclarator1924);
			declarator86=declarator();
			state._fsp--;
			if (state.failed) return retval;
			if ( state.backtracking==0 ) stream_declarator.add(declarator86.getTree());
			// org/eclipse/tracecompass/ctf/parser/CTFParser.g:400:17: ( COLON numberLiteral )?
			int alt28=2;
			int LA28_0 = input.LA(1);
			if ( (LA28_0==COLON) ) {
				alt28=1;
			}
			switch (alt28) {
				case 1 :
					// org/eclipse/tracecompass/ctf/parser/CTFParser.g:400:18: COLON numberLiteral
					{
					COLON87=(Token)match(input,COLON,FOLLOW_COLON_in_structOrVariantDeclarator1927); if (state.failed) return retval; 
					if ( state.backtracking==0 ) stream_COLON.add(COLON87);

					pushFollow(FOLLOW_numberLiteral_in_structOrVariantDeclarator1929);
					numberLiteral88=numberLiteral();
					state._fsp--;
					if (state.failed) return retval;
					if ( state.backtracking==0 ) stream_numberLiteral.add(numberLiteral88.getTree());
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
			// 400:41: -> declarator
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
	// org/eclipse/tracecompass/ctf/parser/CTFParser.g:404:1: variantSpecifier : VARIANTTOK ( ( variantName ( ( variantTag ( variantBody |) ) | variantBody ) ) | ( variantTag variantBody ) | variantBody ) -> ^( VARIANT ( variantName )? ( variantTag )? ( variantBody )? ) ;
	public final CTFParser.variantSpecifier_return variantSpecifier() throws RecognitionException {
		CTFParser.variantSpecifier_return retval = new CTFParser.variantSpecifier_return();
		retval.start = input.LT(1);

		CommonTree root_0 = null;

		Token VARIANTTOK89=null;
		ParserRuleReturnScope variantName90 =null;
		ParserRuleReturnScope variantTag91 =null;
		ParserRuleReturnScope variantBody92 =null;
		ParserRuleReturnScope variantBody93 =null;
		ParserRuleReturnScope variantTag94 =null;
		ParserRuleReturnScope variantBody95 =null;
		ParserRuleReturnScope variantBody96 =null;

		CommonTree VARIANTTOK89_tree=null;
		RewriteRuleTokenStream stream_VARIANTTOK=new RewriteRuleTokenStream(adaptor,"token VARIANTTOK");
		RewriteRuleSubtreeStream stream_variantName=new RewriteRuleSubtreeStream(adaptor,"rule variantName");
		RewriteRuleSubtreeStream stream_variantTag=new RewriteRuleSubtreeStream(adaptor,"rule variantTag");
		RewriteRuleSubtreeStream stream_variantBody=new RewriteRuleSubtreeStream(adaptor,"rule variantBody");

		try {
			// org/eclipse/tracecompass/ctf/parser/CTFParser.g:405:3: ( VARIANTTOK ( ( variantName ( ( variantTag ( variantBody |) ) | variantBody ) ) | ( variantTag variantBody ) | variantBody ) -> ^( VARIANT ( variantName )? ( variantTag )? ( variantBody )? ) )
			// org/eclipse/tracecompass/ctf/parser/CTFParser.g:405:5: VARIANTTOK ( ( variantName ( ( variantTag ( variantBody |) ) | variantBody ) ) | ( variantTag variantBody ) | variantBody )
			{
			VARIANTTOK89=(Token)match(input,VARIANTTOK,FOLLOW_VARIANTTOK_in_variantSpecifier1953); if (state.failed) return retval; 
			if ( state.backtracking==0 ) stream_VARIANTTOK.add(VARIANTTOK89);

			// org/eclipse/tracecompass/ctf/parser/CTFParser.g:406:3: ( ( variantName ( ( variantTag ( variantBody |) ) | variantBody ) ) | ( variantTag variantBody ) | variantBody )
			int alt31=3;
			switch ( input.LA(1) ) {
			case IDENTIFIER:
				{
				alt31=1;
				}
				break;
			case LT:
				{
				alt31=2;
				}
				break;
			case LCURL:
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
					// org/eclipse/tracecompass/ctf/parser/CTFParser.g:407:5: ( variantName ( ( variantTag ( variantBody |) ) | variantBody ) )
					{
					// org/eclipse/tracecompass/ctf/parser/CTFParser.g:407:5: ( variantName ( ( variantTag ( variantBody |) ) | variantBody ) )
					// org/eclipse/tracecompass/ctf/parser/CTFParser.g:408:7: variantName ( ( variantTag ( variantBody |) ) | variantBody )
					{
					pushFollow(FOLLOW_variantName_in_variantSpecifier1971);
					variantName90=variantName();
					state._fsp--;
					if (state.failed) return retval;
					if ( state.backtracking==0 ) stream_variantName.add(variantName90.getTree());
					// org/eclipse/tracecompass/ctf/parser/CTFParser.g:409:7: ( ( variantTag ( variantBody |) ) | variantBody )
					int alt30=2;
					int LA30_0 = input.LA(1);
					if ( (LA30_0==LT) ) {
						alt30=1;
					}
					else if ( (LA30_0==LCURL) ) {
						alt30=2;
					}

					else {
						if (state.backtracking>0) {state.failed=true; return retval;}
						NoViableAltException nvae =
							new NoViableAltException("", 30, 0, input);
						throw nvae;
					}

					switch (alt30) {
						case 1 :
							// org/eclipse/tracecompass/ctf/parser/CTFParser.g:410:9: ( variantTag ( variantBody |) )
							{
							// org/eclipse/tracecompass/ctf/parser/CTFParser.g:410:9: ( variantTag ( variantBody |) )
							// org/eclipse/tracecompass/ctf/parser/CTFParser.g:411:11: variantTag ( variantBody |)
							{
							pushFollow(FOLLOW_variantTag_in_variantSpecifier2001);
							variantTag91=variantTag();
							state._fsp--;
							if (state.failed) return retval;
							if ( state.backtracking==0 ) stream_variantTag.add(variantTag91.getTree());
							// org/eclipse/tracecompass/ctf/parser/CTFParser.g:412:11: ( variantBody |)
							int alt29=2;
							int LA29_0 = input.LA(1);
							if ( (LA29_0==LCURL) ) {
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
									alt29=1;
									}
									break;
								case SIGNEDTOK:
									{
									int LA29_4 = input.LA(3);
									if ( (LA29_4==BOOLTOK||LA29_4==CHARTOK||(LA29_4 >= COMPLEXTOK && LA29_4 <= CONSTTOK)||LA29_4==DOUBLETOK||LA29_4==ENUMTOK||(LA29_4 >= FLOATINGPOINTTOK && LA29_4 <= FLOATTOK)||(LA29_4 >= IDENTIFIER && LA29_4 <= IMAGINARYTOK)||LA29_4==INTEGERTOK||LA29_4==INTTOK||LA29_4==LONGTOK||LA29_4==POINTER||LA29_4==SHORTTOK||LA29_4==SIGNEDTOK||LA29_4==STRINGTOK||LA29_4==STRUCTTOK||LA29_4==TYPEDEFTOK||(LA29_4 >= UNSIGNEDTOK && LA29_4 <= VOIDTOK)) ) {
										alt29=1;
									}
									else if ( (LA29_4==ASSIGNMENT||LA29_4==RCURL||LA29_4==SEPARATOR) ) {
										alt29=2;
									}

									else {
										if (state.backtracking>0) {state.failed=true; return retval;}
										int nvaeMark = input.mark();
										try {
											for (int nvaeConsume = 0; nvaeConsume < 3 - 1; nvaeConsume++) {
												input.consume();
											}
											NoViableAltException nvae =
												new NoViableAltException("", 29, 4, input);
											throw nvae;
										} finally {
											input.rewind(nvaeMark);
										}
									}

									}
									break;
								case STRINGTOK:
									{
									int LA29_5 = input.LA(3);
									if ( (LA29_5==BOOLTOK||LA29_5==CHARTOK||(LA29_5 >= COMPLEXTOK && LA29_5 <= CONSTTOK)||LA29_5==DOUBLETOK||LA29_5==ENUMTOK||(LA29_5 >= FLOATINGPOINTTOK && LA29_5 <= FLOATTOK)||(LA29_5 >= IDENTIFIER && LA29_5 <= IMAGINARYTOK)||LA29_5==INTEGERTOK||(LA29_5 >= INTTOK && LA29_5 <= LCURL)||LA29_5==LONGTOK||LA29_5==POINTER||LA29_5==SHORTTOK||LA29_5==SIGNEDTOK||LA29_5==STRINGTOK||LA29_5==STRUCTTOK||LA29_5==TYPEDEFTOK||(LA29_5 >= UNSIGNEDTOK && LA29_5 <= VOIDTOK)) ) {
										alt29=1;
									}
									else if ( (LA29_5==ASSIGNMENT||LA29_5==RCURL||LA29_5==SEPARATOR) ) {
										alt29=2;
									}

									else {
										if (state.backtracking>0) {state.failed=true; return retval;}
										int nvaeMark = input.mark();
										try {
											for (int nvaeConsume = 0; nvaeConsume < 3 - 1; nvaeConsume++) {
												input.consume();
											}
											NoViableAltException nvae =
												new NoViableAltException("", 29, 5, input);
											throw nvae;
										} finally {
											input.rewind(nvaeMark);
										}
									}

									}
									break;
								case IDENTIFIER:
									{
									int LA29_6 = input.LA(3);
									if ( (LA29_6==BOOLTOK||LA29_6==CHARTOK||(LA29_6 >= COMPLEXTOK && LA29_6 <= CONSTTOK)||LA29_6==DOUBLETOK||LA29_6==ENUMTOK||(LA29_6 >= FLOATINGPOINTTOK && LA29_6 <= FLOATTOK)||(LA29_6 >= IDENTIFIER && LA29_6 <= IMAGINARYTOK)||LA29_6==INTEGERTOK||LA29_6==INTTOK||LA29_6==LONGTOK||LA29_6==POINTER||LA29_6==SHORTTOK||LA29_6==SIGNEDTOK||LA29_6==STRINGTOK||LA29_6==STRUCTTOK||LA29_6==TYPEDEFTOK||(LA29_6 >= UNSIGNEDTOK && LA29_6 <= VOIDTOK)) ) {
										alt29=1;
									}
									else if ( (LA29_6==ASSIGNMENT||LA29_6==RCURL||LA29_6==SEPARATOR) ) {
										alt29=2;
									}

									else {
										if (state.backtracking>0) {state.failed=true; return retval;}
										int nvaeMark = input.mark();
										try {
											for (int nvaeConsume = 0; nvaeConsume < 3 - 1; nvaeConsume++) {
												input.consume();
											}
											NoViableAltException nvae =
												new NoViableAltException("", 29, 6, input);
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
									alt29=2;
									}
									break;
								default:
									if (state.backtracking>0) {state.failed=true; return retval;}
									int nvaeMark = input.mark();
									try {
										input.consume();
										NoViableAltException nvae =
											new NoViableAltException("", 29, 1, input);
										throw nvae;
									} finally {
										input.rewind(nvaeMark);
									}
								}
							}
							else if ( (LA29_0==EOF||LA29_0==BOOLTOK||LA29_0==CHARTOK||(LA29_0 >= COMPLEXTOK && LA29_0 <= CONSTTOK)||LA29_0==DOUBLETOK||LA29_0==ENUMTOK||(LA29_0 >= FLOATINGPOINTTOK && LA29_0 <= FLOATTOK)||(LA29_0 >= IDENTIFIER && LA29_0 <= IMAGINARYTOK)||LA29_0==INTEGERTOK||LA29_0==INTTOK||(LA29_0 >= LONGTOK && LA29_0 <= LPAREN)||LA29_0==POINTER||LA29_0==SHORTTOK||LA29_0==SIGNEDTOK||LA29_0==STRINGTOK||(LA29_0 >= STRUCTTOK && LA29_0 <= TERM)||(LA29_0 >= TYPEDEFTOK && LA29_0 <= TYPE_ASSIGNMENT)||(LA29_0 >= UNSIGNEDTOK && LA29_0 <= VOIDTOK)) ) {
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
									// org/eclipse/tracecompass/ctf/parser/CTFParser.g:413:13: variantBody
									{
									pushFollow(FOLLOW_variantBody_in_variantSpecifier2027);
									variantBody92=variantBody();
									state._fsp--;
									if (state.failed) return retval;
									if ( state.backtracking==0 ) stream_variantBody.add(variantBody92.getTree());
									}
									break;
								case 2 :
									// org/eclipse/tracecompass/ctf/parser/CTFParser.g:416:11: 
									{
									}
									break;

							}

							}

							}
							break;
						case 2 :
							// org/eclipse/tracecompass/ctf/parser/CTFParser.g:419:9: variantBody
							{
							pushFollow(FOLLOW_variantBody_in_variantSpecifier2095);
							variantBody93=variantBody();
							state._fsp--;
							if (state.failed) return retval;
							if ( state.backtracking==0 ) stream_variantBody.add(variantBody93.getTree());
							}
							break;

					}

					}

					}
					break;
				case 2 :
					// org/eclipse/tracecompass/ctf/parser/CTFParser.g:422:5: ( variantTag variantBody )
					{
					// org/eclipse/tracecompass/ctf/parser/CTFParser.g:422:5: ( variantTag variantBody )
					// org/eclipse/tracecompass/ctf/parser/CTFParser.g:422:6: variantTag variantBody
					{
					pushFollow(FOLLOW_variantTag_in_variantSpecifier2116);
					variantTag94=variantTag();
					state._fsp--;
					if (state.failed) return retval;
					if ( state.backtracking==0 ) stream_variantTag.add(variantTag94.getTree());
					pushFollow(FOLLOW_variantBody_in_variantSpecifier2118);
					variantBody95=variantBody();
					state._fsp--;
					if (state.failed) return retval;
					if ( state.backtracking==0 ) stream_variantBody.add(variantBody95.getTree());
					}

					}
					break;
				case 3 :
					// org/eclipse/tracecompass/ctf/parser/CTFParser.g:423:5: variantBody
					{
					pushFollow(FOLLOW_variantBody_in_variantSpecifier2125);
					variantBody96=variantBody();
					state._fsp--;
					if (state.failed) return retval;
					if ( state.backtracking==0 ) stream_variantBody.add(variantBody96.getTree());
					}
					break;

			}

			// AST REWRITE
			// elements: variantName, variantTag, variantBody
			// token labels: 
			// rule labels: retval
			// token list labels: 
			// rule list labels: 
			// wildcard labels: 
			if ( state.backtracking==0 ) {
			retval.tree = root_0;
			RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.getTree():null);

			root_0 = (CommonTree)adaptor.nil();
			// 424:5: -> ^( VARIANT ( variantName )? ( variantTag )? ( variantBody )? )
			{
				// org/eclipse/tracecompass/ctf/parser/CTFParser.g:424:8: ^( VARIANT ( variantName )? ( variantTag )? ( variantBody )? )
				{
				CommonTree root_1 = (CommonTree)adaptor.nil();
				root_1 = (CommonTree)adaptor.becomeRoot((CommonTree)adaptor.create(VARIANT, "VARIANT"), root_1);
				// org/eclipse/tracecompass/ctf/parser/CTFParser.g:424:18: ( variantName )?
				if ( stream_variantName.hasNext() ) {
					adaptor.addChild(root_1, stream_variantName.nextTree());
				}
				stream_variantName.reset();

				// org/eclipse/tracecompass/ctf/parser/CTFParser.g:424:31: ( variantTag )?
				if ( stream_variantTag.hasNext() ) {
					adaptor.addChild(root_1, stream_variantTag.nextTree());
				}
				stream_variantTag.reset();

				// org/eclipse/tracecompass/ctf/parser/CTFParser.g:424:43: ( variantBody )?
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
	// org/eclipse/tracecompass/ctf/parser/CTFParser.g:427:1: variantName : IDENTIFIER -> ^( VARIANT_NAME IDENTIFIER ) ;
	public final CTFParser.variantName_return variantName() throws RecognitionException {
		CTFParser.variantName_return retval = new CTFParser.variantName_return();
		retval.start = input.LT(1);

		CommonTree root_0 = null;

		Token IDENTIFIER97=null;

		CommonTree IDENTIFIER97_tree=null;
		RewriteRuleTokenStream stream_IDENTIFIER=new RewriteRuleTokenStream(adaptor,"token IDENTIFIER");

		try {
			// org/eclipse/tracecompass/ctf/parser/CTFParser.g:428:3: ( IDENTIFIER -> ^( VARIANT_NAME IDENTIFIER ) )
			// org/eclipse/tracecompass/ctf/parser/CTFParser.g:428:5: IDENTIFIER
			{
			IDENTIFIER97=(Token)match(input,IDENTIFIER,FOLLOW_IDENTIFIER_in_variantName2157); if (state.failed) return retval; 
			if ( state.backtracking==0 ) stream_IDENTIFIER.add(IDENTIFIER97);

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
			// 428:16: -> ^( VARIANT_NAME IDENTIFIER )
			{
				// org/eclipse/tracecompass/ctf/parser/CTFParser.g:428:19: ^( VARIANT_NAME IDENTIFIER )
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
	// org/eclipse/tracecompass/ctf/parser/CTFParser.g:431:1: variantBody : LCURL structOrVariantDeclarationList RCURL -> ^( VARIANT_BODY structOrVariantDeclarationList ) ;
	public final CTFParser.variantBody_return variantBody() throws RecognitionException {
		Symbols_stack.push(new Symbols_scope());

		CTFParser.variantBody_return retval = new CTFParser.variantBody_return();
		retval.start = input.LT(1);

		CommonTree root_0 = null;

		Token LCURL98=null;
		Token RCURL100=null;
		ParserRuleReturnScope structOrVariantDeclarationList99 =null;

		CommonTree LCURL98_tree=null;
		CommonTree RCURL100_tree=null;
		RewriteRuleTokenStream stream_LCURL=new RewriteRuleTokenStream(adaptor,"token LCURL");
		RewriteRuleTokenStream stream_RCURL=new RewriteRuleTokenStream(adaptor,"token RCURL");
		RewriteRuleSubtreeStream stream_structOrVariantDeclarationList=new RewriteRuleSubtreeStream(adaptor,"rule structOrVariantDeclarationList");


		    Symbols_stack.peek().types = new HashSet<String>();

		try {
			// org/eclipse/tracecompass/ctf/parser/CTFParser.g:436:3: ( LCURL structOrVariantDeclarationList RCURL -> ^( VARIANT_BODY structOrVariantDeclarationList ) )
			// org/eclipse/tracecompass/ctf/parser/CTFParser.g:436:5: LCURL structOrVariantDeclarationList RCURL
			{
			LCURL98=(Token)match(input,LCURL,FOLLOW_LCURL_in_variantBody2188); if (state.failed) return retval; 
			if ( state.backtracking==0 ) stream_LCURL.add(LCURL98);

			pushFollow(FOLLOW_structOrVariantDeclarationList_in_variantBody2190);
			structOrVariantDeclarationList99=structOrVariantDeclarationList();
			state._fsp--;
			if (state.failed) return retval;
			if ( state.backtracking==0 ) stream_structOrVariantDeclarationList.add(structOrVariantDeclarationList99.getTree());
			RCURL100=(Token)match(input,RCURL,FOLLOW_RCURL_in_variantBody2192); if (state.failed) return retval; 
			if ( state.backtracking==0 ) stream_RCURL.add(RCURL100);

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
			// 437:7: -> ^( VARIANT_BODY structOrVariantDeclarationList )
			{
				// org/eclipse/tracecompass/ctf/parser/CTFParser.g:437:10: ^( VARIANT_BODY structOrVariantDeclarationList )
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
	// org/eclipse/tracecompass/ctf/parser/CTFParser.g:440:1: variantTag : LT IDENTIFIER GT -> ^( VARIANT_TAG IDENTIFIER ) ;
	public final CTFParser.variantTag_return variantTag() throws RecognitionException {
		CTFParser.variantTag_return retval = new CTFParser.variantTag_return();
		retval.start = input.LT(1);

		CommonTree root_0 = null;

		Token LT101=null;
		Token IDENTIFIER102=null;
		Token GT103=null;

		CommonTree LT101_tree=null;
		CommonTree IDENTIFIER102_tree=null;
		CommonTree GT103_tree=null;
		RewriteRuleTokenStream stream_GT=new RewriteRuleTokenStream(adaptor,"token GT");
		RewriteRuleTokenStream stream_LT=new RewriteRuleTokenStream(adaptor,"token LT");
		RewriteRuleTokenStream stream_IDENTIFIER=new RewriteRuleTokenStream(adaptor,"token IDENTIFIER");

		try {
			// org/eclipse/tracecompass/ctf/parser/CTFParser.g:441:3: ( LT IDENTIFIER GT -> ^( VARIANT_TAG IDENTIFIER ) )
			// org/eclipse/tracecompass/ctf/parser/CTFParser.g:441:5: LT IDENTIFIER GT
			{
			LT101=(Token)match(input,LT,FOLLOW_LT_in_variantTag2219); if (state.failed) return retval; 
			if ( state.backtracking==0 ) stream_LT.add(LT101);

			IDENTIFIER102=(Token)match(input,IDENTIFIER,FOLLOW_IDENTIFIER_in_variantTag2221); if (state.failed) return retval; 
			if ( state.backtracking==0 ) stream_IDENTIFIER.add(IDENTIFIER102);

			GT103=(Token)match(input,GT,FOLLOW_GT_in_variantTag2223); if (state.failed) return retval; 
			if ( state.backtracking==0 ) stream_GT.add(GT103);

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
			// 441:22: -> ^( VARIANT_TAG IDENTIFIER )
			{
				// org/eclipse/tracecompass/ctf/parser/CTFParser.g:441:25: ^( VARIANT_TAG IDENTIFIER )
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
	// org/eclipse/tracecompass/ctf/parser/CTFParser.g:444:1: enumSpecifier : ENUMTOK ( ( enumName ( enumContainerType enumBody | enumBody |) ) | ( enumContainerType enumBody | enumBody ) ) -> ^( ENUM ( enumName )? ( enumContainerType )? ( enumBody )? ) ;
	public final CTFParser.enumSpecifier_return enumSpecifier() throws RecognitionException {
		CTFParser.enumSpecifier_return retval = new CTFParser.enumSpecifier_return();
		retval.start = input.LT(1);

		CommonTree root_0 = null;

		Token ENUMTOK104=null;
		ParserRuleReturnScope enumName105 =null;
		ParserRuleReturnScope enumContainerType106 =null;
		ParserRuleReturnScope enumBody107 =null;
		ParserRuleReturnScope enumBody108 =null;
		ParserRuleReturnScope enumContainerType109 =null;
		ParserRuleReturnScope enumBody110 =null;
		ParserRuleReturnScope enumBody111 =null;

		CommonTree ENUMTOK104_tree=null;
		RewriteRuleTokenStream stream_ENUMTOK=new RewriteRuleTokenStream(adaptor,"token ENUMTOK");
		RewriteRuleSubtreeStream stream_enumName=new RewriteRuleSubtreeStream(adaptor,"rule enumName");
		RewriteRuleSubtreeStream stream_enumContainerType=new RewriteRuleSubtreeStream(adaptor,"rule enumContainerType");
		RewriteRuleSubtreeStream stream_enumBody=new RewriteRuleSubtreeStream(adaptor,"rule enumBody");

		try {
			// org/eclipse/tracecompass/ctf/parser/CTFParser.g:445:3: ( ENUMTOK ( ( enumName ( enumContainerType enumBody | enumBody |) ) | ( enumContainerType enumBody | enumBody ) ) -> ^( ENUM ( enumName )? ( enumContainerType )? ( enumBody )? ) )
			// org/eclipse/tracecompass/ctf/parser/CTFParser.g:445:5: ENUMTOK ( ( enumName ( enumContainerType enumBody | enumBody |) ) | ( enumContainerType enumBody | enumBody ) )
			{
			ENUMTOK104=(Token)match(input,ENUMTOK,FOLLOW_ENUMTOK_in_enumSpecifier2244); if (state.failed) return retval; 
			if ( state.backtracking==0 ) stream_ENUMTOK.add(ENUMTOK104);

			// org/eclipse/tracecompass/ctf/parser/CTFParser.g:446:5: ( ( enumName ( enumContainerType enumBody | enumBody |) ) | ( enumContainerType enumBody | enumBody ) )
			int alt34=2;
			int LA34_0 = input.LA(1);
			if ( (LA34_0==IDENTIFIER) ) {
				alt34=1;
			}
			else if ( (LA34_0==COLON||LA34_0==LCURL) ) {
				alt34=2;
			}

			else {
				if (state.backtracking>0) {state.failed=true; return retval;}
				NoViableAltException nvae =
					new NoViableAltException("", 34, 0, input);
				throw nvae;
			}

			switch (alt34) {
				case 1 :
					// org/eclipse/tracecompass/ctf/parser/CTFParser.g:448:9: ( enumName ( enumContainerType enumBody | enumBody |) )
					{
					// org/eclipse/tracecompass/ctf/parser/CTFParser.g:448:9: ( enumName ( enumContainerType enumBody | enumBody |) )
					// org/eclipse/tracecompass/ctf/parser/CTFParser.g:449:13: enumName ( enumContainerType enumBody | enumBody |)
					{
					pushFollow(FOLLOW_enumName_in_enumSpecifier2283);
					enumName105=enumName();
					state._fsp--;
					if (state.failed) return retval;
					if ( state.backtracking==0 ) stream_enumName.add(enumName105.getTree());
					// org/eclipse/tracecompass/ctf/parser/CTFParser.g:450:13: ( enumContainerType enumBody | enumBody |)
					int alt32=3;
					switch ( input.LA(1) ) {
					case COLON:
						{
						alt32=1;
						}
						break;
					case LCURL:
						{
						alt32=2;
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
						alt32=3;
						}
						break;
					default:
						if (state.backtracking>0) {state.failed=true; return retval;}
						NoViableAltException nvae =
							new NoViableAltException("", 32, 0, input);
						throw nvae;
					}
					switch (alt32) {
						case 1 :
							// org/eclipse/tracecompass/ctf/parser/CTFParser.g:451:17: enumContainerType enumBody
							{
							pushFollow(FOLLOW_enumContainerType_in_enumSpecifier2315);
							enumContainerType106=enumContainerType();
							state._fsp--;
							if (state.failed) return retval;
							if ( state.backtracking==0 ) stream_enumContainerType.add(enumContainerType106.getTree());
							pushFollow(FOLLOW_enumBody_in_enumSpecifier2317);
							enumBody107=enumBody();
							state._fsp--;
							if (state.failed) return retval;
							if ( state.backtracking==0 ) stream_enumBody.add(enumBody107.getTree());
							}
							break;
						case 2 :
							// org/eclipse/tracecompass/ctf/parser/CTFParser.g:453:17: enumBody
							{
							pushFollow(FOLLOW_enumBody_in_enumSpecifier2347);
							enumBody108=enumBody();
							state._fsp--;
							if (state.failed) return retval;
							if ( state.backtracking==0 ) stream_enumBody.add(enumBody108.getTree());
							}
							break;
						case 3 :
							// org/eclipse/tracecompass/ctf/parser/CTFParser.g:456:13: 
							{
							}
							break;

					}

					}

					}
					break;
				case 2 :
					// org/eclipse/tracecompass/ctf/parser/CTFParser.g:460:9: ( enumContainerType enumBody | enumBody )
					{
					// org/eclipse/tracecompass/ctf/parser/CTFParser.g:460:9: ( enumContainerType enumBody | enumBody )
					int alt33=2;
					int LA33_0 = input.LA(1);
					if ( (LA33_0==COLON) ) {
						alt33=1;
					}
					else if ( (LA33_0==LCURL) ) {
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
							// org/eclipse/tracecompass/ctf/parser/CTFParser.g:461:13: enumContainerType enumBody
							{
							pushFollow(FOLLOW_enumContainerType_in_enumSpecifier2439);
							enumContainerType109=enumContainerType();
							state._fsp--;
							if (state.failed) return retval;
							if ( state.backtracking==0 ) stream_enumContainerType.add(enumContainerType109.getTree());
							pushFollow(FOLLOW_enumBody_in_enumSpecifier2441);
							enumBody110=enumBody();
							state._fsp--;
							if (state.failed) return retval;
							if ( state.backtracking==0 ) stream_enumBody.add(enumBody110.getTree());
							}
							break;
						case 2 :
							// org/eclipse/tracecompass/ctf/parser/CTFParser.g:463:13: enumBody
							{
							pushFollow(FOLLOW_enumBody_in_enumSpecifier2465);
							enumBody111=enumBody();
							state._fsp--;
							if (state.failed) return retval;
							if ( state.backtracking==0 ) stream_enumBody.add(enumBody111.getTree());
							}
							break;

					}

					}
					break;

			}

			// AST REWRITE
			// elements: enumName, enumContainerType, enumBody
			// token labels: 
			// rule labels: retval
			// token list labels: 
			// rule list labels: 
			// wildcard labels: 
			if ( state.backtracking==0 ) {
			retval.tree = root_0;
			RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.getTree():null);

			root_0 = (CommonTree)adaptor.nil();
			// 465:7: -> ^( ENUM ( enumName )? ( enumContainerType )? ( enumBody )? )
			{
				// org/eclipse/tracecompass/ctf/parser/CTFParser.g:465:10: ^( ENUM ( enumName )? ( enumContainerType )? ( enumBody )? )
				{
				CommonTree root_1 = (CommonTree)adaptor.nil();
				root_1 = (CommonTree)adaptor.becomeRoot((CommonTree)adaptor.create(ENUM, "ENUM"), root_1);
				// org/eclipse/tracecompass/ctf/parser/CTFParser.g:465:17: ( enumName )?
				if ( stream_enumName.hasNext() ) {
					adaptor.addChild(root_1, stream_enumName.nextTree());
				}
				stream_enumName.reset();

				// org/eclipse/tracecompass/ctf/parser/CTFParser.g:465:27: ( enumContainerType )?
				if ( stream_enumContainerType.hasNext() ) {
					adaptor.addChild(root_1, stream_enumContainerType.nextTree());
				}
				stream_enumContainerType.reset();

				// org/eclipse/tracecompass/ctf/parser/CTFParser.g:465:46: ( enumBody )?
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
	// org/eclipse/tracecompass/ctf/parser/CTFParser.g:468:1: enumName : IDENTIFIER -> ^( ENUM_NAME IDENTIFIER ) ;
	public final CTFParser.enumName_return enumName() throws RecognitionException {
		CTFParser.enumName_return retval = new CTFParser.enumName_return();
		retval.start = input.LT(1);

		CommonTree root_0 = null;

		Token IDENTIFIER112=null;

		CommonTree IDENTIFIER112_tree=null;
		RewriteRuleTokenStream stream_IDENTIFIER=new RewriteRuleTokenStream(adaptor,"token IDENTIFIER");

		try {
			// org/eclipse/tracecompass/ctf/parser/CTFParser.g:469:3: ( IDENTIFIER -> ^( ENUM_NAME IDENTIFIER ) )
			// org/eclipse/tracecompass/ctf/parser/CTFParser.g:469:5: IDENTIFIER
			{
			IDENTIFIER112=(Token)match(input,IDENTIFIER,FOLLOW_IDENTIFIER_in_enumName2509); if (state.failed) return retval; 
			if ( state.backtracking==0 ) stream_IDENTIFIER.add(IDENTIFIER112);

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
			// 469:16: -> ^( ENUM_NAME IDENTIFIER )
			{
				// org/eclipse/tracecompass/ctf/parser/CTFParser.g:469:19: ^( ENUM_NAME IDENTIFIER )
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
	// org/eclipse/tracecompass/ctf/parser/CTFParser.g:472:1: enumBody : LCURL enumeratorList ( SEPARATOR )? RCURL -> ^( ENUM_BODY enumeratorList ) ;
	public final CTFParser.enumBody_return enumBody() throws RecognitionException {
		CTFParser.enumBody_return retval = new CTFParser.enumBody_return();
		retval.start = input.LT(1);

		CommonTree root_0 = null;

		Token LCURL113=null;
		Token SEPARATOR115=null;
		Token RCURL116=null;
		ParserRuleReturnScope enumeratorList114 =null;

		CommonTree LCURL113_tree=null;
		CommonTree SEPARATOR115_tree=null;
		CommonTree RCURL116_tree=null;
		RewriteRuleTokenStream stream_LCURL=new RewriteRuleTokenStream(adaptor,"token LCURL");
		RewriteRuleTokenStream stream_SEPARATOR=new RewriteRuleTokenStream(adaptor,"token SEPARATOR");
		RewriteRuleTokenStream stream_RCURL=new RewriteRuleTokenStream(adaptor,"token RCURL");
		RewriteRuleSubtreeStream stream_enumeratorList=new RewriteRuleSubtreeStream(adaptor,"rule enumeratorList");

		try {
			// org/eclipse/tracecompass/ctf/parser/CTFParser.g:473:3: ( LCURL enumeratorList ( SEPARATOR )? RCURL -> ^( ENUM_BODY enumeratorList ) )
			// org/eclipse/tracecompass/ctf/parser/CTFParser.g:473:5: LCURL enumeratorList ( SEPARATOR )? RCURL
			{
			LCURL113=(Token)match(input,LCURL,FOLLOW_LCURL_in_enumBody2530); if (state.failed) return retval; 
			if ( state.backtracking==0 ) stream_LCURL.add(LCURL113);

			pushFollow(FOLLOW_enumeratorList_in_enumBody2532);
			enumeratorList114=enumeratorList();
			state._fsp--;
			if (state.failed) return retval;
			if ( state.backtracking==0 ) stream_enumeratorList.add(enumeratorList114.getTree());
			// org/eclipse/tracecompass/ctf/parser/CTFParser.g:473:26: ( SEPARATOR )?
			int alt35=2;
			int LA35_0 = input.LA(1);
			if ( (LA35_0==SEPARATOR) ) {
				alt35=1;
			}
			switch (alt35) {
				case 1 :
					// org/eclipse/tracecompass/ctf/parser/CTFParser.g:473:26: SEPARATOR
					{
					SEPARATOR115=(Token)match(input,SEPARATOR,FOLLOW_SEPARATOR_in_enumBody2534); if (state.failed) return retval; 
					if ( state.backtracking==0 ) stream_SEPARATOR.add(SEPARATOR115);

					}
					break;

			}

			RCURL116=(Token)match(input,RCURL,FOLLOW_RCURL_in_enumBody2537); if (state.failed) return retval; 
			if ( state.backtracking==0 ) stream_RCURL.add(RCURL116);

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
			// 473:43: -> ^( ENUM_BODY enumeratorList )
			{
				// org/eclipse/tracecompass/ctf/parser/CTFParser.g:473:46: ^( ENUM_BODY enumeratorList )
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
	// org/eclipse/tracecompass/ctf/parser/CTFParser.g:476:1: enumContainerType : COLON declarationSpecifiers -> ^( ENUM_CONTAINER_TYPE declarationSpecifiers ) ;
	public final CTFParser.enumContainerType_return enumContainerType() throws RecognitionException {
		CTFParser.enumContainerType_return retval = new CTFParser.enumContainerType_return();
		retval.start = input.LT(1);

		CommonTree root_0 = null;

		Token COLON117=null;
		ParserRuleReturnScope declarationSpecifiers118 =null;

		CommonTree COLON117_tree=null;
		RewriteRuleTokenStream stream_COLON=new RewriteRuleTokenStream(adaptor,"token COLON");
		RewriteRuleSubtreeStream stream_declarationSpecifiers=new RewriteRuleSubtreeStream(adaptor,"rule declarationSpecifiers");

		try {
			// org/eclipse/tracecompass/ctf/parser/CTFParser.g:477:3: ( COLON declarationSpecifiers -> ^( ENUM_CONTAINER_TYPE declarationSpecifiers ) )
			// org/eclipse/tracecompass/ctf/parser/CTFParser.g:477:5: COLON declarationSpecifiers
			{
			COLON117=(Token)match(input,COLON,FOLLOW_COLON_in_enumContainerType2558); if (state.failed) return retval; 
			if ( state.backtracking==0 ) stream_COLON.add(COLON117);

			pushFollow(FOLLOW_declarationSpecifiers_in_enumContainerType2560);
			declarationSpecifiers118=declarationSpecifiers();
			state._fsp--;
			if (state.failed) return retval;
			if ( state.backtracking==0 ) stream_declarationSpecifiers.add(declarationSpecifiers118.getTree());
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
			// 477:33: -> ^( ENUM_CONTAINER_TYPE declarationSpecifiers )
			{
				// org/eclipse/tracecompass/ctf/parser/CTFParser.g:477:36: ^( ENUM_CONTAINER_TYPE declarationSpecifiers )
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
	// org/eclipse/tracecompass/ctf/parser/CTFParser.g:480:1: enumeratorList : enumerator ( SEPARATOR enumerator )* -> ( ^( ENUM_ENUMERATOR enumerator ) )+ ;
	public final CTFParser.enumeratorList_return enumeratorList() throws RecognitionException {
		CTFParser.enumeratorList_return retval = new CTFParser.enumeratorList_return();
		retval.start = input.LT(1);

		CommonTree root_0 = null;

		Token SEPARATOR120=null;
		ParserRuleReturnScope enumerator119 =null;
		ParserRuleReturnScope enumerator121 =null;

		CommonTree SEPARATOR120_tree=null;
		RewriteRuleTokenStream stream_SEPARATOR=new RewriteRuleTokenStream(adaptor,"token SEPARATOR");
		RewriteRuleSubtreeStream stream_enumerator=new RewriteRuleSubtreeStream(adaptor,"rule enumerator");

		try {
			// org/eclipse/tracecompass/ctf/parser/CTFParser.g:481:3: ( enumerator ( SEPARATOR enumerator )* -> ( ^( ENUM_ENUMERATOR enumerator ) )+ )
			// org/eclipse/tracecompass/ctf/parser/CTFParser.g:481:5: enumerator ( SEPARATOR enumerator )*
			{
			pushFollow(FOLLOW_enumerator_in_enumeratorList2581);
			enumerator119=enumerator();
			state._fsp--;
			if (state.failed) return retval;
			if ( state.backtracking==0 ) stream_enumerator.add(enumerator119.getTree());
			// org/eclipse/tracecompass/ctf/parser/CTFParser.g:481:16: ( SEPARATOR enumerator )*
			loop36:
			while (true) {
				int alt36=2;
				int LA36_0 = input.LA(1);
				if ( (LA36_0==SEPARATOR) ) {
					int LA36_1 = input.LA(2);
					if ( (LA36_1==ALIGNTOK||LA36_1==EVENTTOK||LA36_1==IDENTIFIER||LA36_1==SIGNEDTOK||LA36_1==STRINGTOK||LA36_1==STRING_LITERAL) ) {
						alt36=1;
					}

				}

				switch (alt36) {
				case 1 :
					// org/eclipse/tracecompass/ctf/parser/CTFParser.g:481:17: SEPARATOR enumerator
					{
					SEPARATOR120=(Token)match(input,SEPARATOR,FOLLOW_SEPARATOR_in_enumeratorList2584); if (state.failed) return retval; 
					if ( state.backtracking==0 ) stream_SEPARATOR.add(SEPARATOR120);

					pushFollow(FOLLOW_enumerator_in_enumeratorList2586);
					enumerator121=enumerator();
					state._fsp--;
					if (state.failed) return retval;
					if ( state.backtracking==0 ) stream_enumerator.add(enumerator121.getTree());
					}
					break;

				default :
					break loop36;
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
			// 481:40: -> ( ^( ENUM_ENUMERATOR enumerator ) )+
			{
				if ( !(stream_enumerator.hasNext()) ) {
					throw new RewriteEarlyExitException();
				}
				while ( stream_enumerator.hasNext() ) {
					// org/eclipse/tracecompass/ctf/parser/CTFParser.g:481:44: ^( ENUM_ENUMERATOR enumerator )
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
	// org/eclipse/tracecompass/ctf/parser/CTFParser.g:484:1: enumerator : enumConstant ( enumeratorValue )? ;
	public final CTFParser.enumerator_return enumerator() throws RecognitionException {
		CTFParser.enumerator_return retval = new CTFParser.enumerator_return();
		retval.start = input.LT(1);

		CommonTree root_0 = null;

		ParserRuleReturnScope enumConstant122 =null;
		ParserRuleReturnScope enumeratorValue123 =null;


		try {
			// org/eclipse/tracecompass/ctf/parser/CTFParser.g:485:3: ( enumConstant ( enumeratorValue )? )
			// org/eclipse/tracecompass/ctf/parser/CTFParser.g:485:5: enumConstant ( enumeratorValue )?
			{
			root_0 = (CommonTree)adaptor.nil();


			pushFollow(FOLLOW_enumConstant_in_enumerator2612);
			enumConstant122=enumConstant();
			state._fsp--;
			if (state.failed) return retval;
			if ( state.backtracking==0 ) adaptor.addChild(root_0, enumConstant122.getTree());

			// org/eclipse/tracecompass/ctf/parser/CTFParser.g:485:18: ( enumeratorValue )?
			int alt37=2;
			int LA37_0 = input.LA(1);
			if ( (LA37_0==ASSIGNMENT) ) {
				alt37=1;
			}
			switch (alt37) {
				case 1 :
					// org/eclipse/tracecompass/ctf/parser/CTFParser.g:485:18: enumeratorValue
					{
					pushFollow(FOLLOW_enumeratorValue_in_enumerator2614);
					enumeratorValue123=enumeratorValue();
					state._fsp--;
					if (state.failed) return retval;
					if ( state.backtracking==0 ) adaptor.addChild(root_0, enumeratorValue123.getTree());

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
	// org/eclipse/tracecompass/ctf/parser/CTFParser.g:488:1: enumeratorValue : ASSIGNMENT e1= unaryExpression ( -> ^( ENUM_VALUE $e1) | ELIPSES e2= unaryExpression -> ^( ENUM_VALUE_RANGE $e1 $e2) ) ;
	public final CTFParser.enumeratorValue_return enumeratorValue() throws RecognitionException {
		CTFParser.enumeratorValue_return retval = new CTFParser.enumeratorValue_return();
		retval.start = input.LT(1);

		CommonTree root_0 = null;

		Token ASSIGNMENT124=null;
		Token ELIPSES125=null;
		ParserRuleReturnScope e1 =null;
		ParserRuleReturnScope e2 =null;

		CommonTree ASSIGNMENT124_tree=null;
		CommonTree ELIPSES125_tree=null;
		RewriteRuleTokenStream stream_ASSIGNMENT=new RewriteRuleTokenStream(adaptor,"token ASSIGNMENT");
		RewriteRuleTokenStream stream_ELIPSES=new RewriteRuleTokenStream(adaptor,"token ELIPSES");
		RewriteRuleSubtreeStream stream_unaryExpression=new RewriteRuleSubtreeStream(adaptor,"rule unaryExpression");

		try {
			// org/eclipse/tracecompass/ctf/parser/CTFParser.g:489:3: ( ASSIGNMENT e1= unaryExpression ( -> ^( ENUM_VALUE $e1) | ELIPSES e2= unaryExpression -> ^( ENUM_VALUE_RANGE $e1 $e2) ) )
			// org/eclipse/tracecompass/ctf/parser/CTFParser.g:489:5: ASSIGNMENT e1= unaryExpression ( -> ^( ENUM_VALUE $e1) | ELIPSES e2= unaryExpression -> ^( ENUM_VALUE_RANGE $e1 $e2) )
			{
			ASSIGNMENT124=(Token)match(input,ASSIGNMENT,FOLLOW_ASSIGNMENT_in_enumeratorValue2628); if (state.failed) return retval; 
			if ( state.backtracking==0 ) stream_ASSIGNMENT.add(ASSIGNMENT124);

			pushFollow(FOLLOW_unaryExpression_in_enumeratorValue2632);
			e1=unaryExpression();
			state._fsp--;
			if (state.failed) return retval;
			if ( state.backtracking==0 ) stream_unaryExpression.add(e1.getTree());
			// org/eclipse/tracecompass/ctf/parser/CTFParser.g:490:7: ( -> ^( ENUM_VALUE $e1) | ELIPSES e2= unaryExpression -> ^( ENUM_VALUE_RANGE $e1 $e2) )
			int alt38=2;
			int LA38_0 = input.LA(1);
			if ( (LA38_0==RCURL||LA38_0==SEPARATOR) ) {
				alt38=1;
			}
			else if ( (LA38_0==ELIPSES) ) {
				alt38=2;
			}

			else {
				if (state.backtracking>0) {state.failed=true; return retval;}
				NoViableAltException nvae =
					new NoViableAltException("", 38, 0, input);
				throw nvae;
			}

			switch (alt38) {
				case 1 :
					// org/eclipse/tracecompass/ctf/parser/CTFParser.g:491:11: 
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
					// 491:11: -> ^( ENUM_VALUE $e1)
					{
						// org/eclipse/tracecompass/ctf/parser/CTFParser.g:491:14: ^( ENUM_VALUE $e1)
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
					// org/eclipse/tracecompass/ctf/parser/CTFParser.g:492:9: ELIPSES e2= unaryExpression
					{
					ELIPSES125=(Token)match(input,ELIPSES,FOLLOW_ELIPSES_in_enumeratorValue2671); if (state.failed) return retval; 
					if ( state.backtracking==0 ) stream_ELIPSES.add(ELIPSES125);

					pushFollow(FOLLOW_unaryExpression_in_enumeratorValue2675);
					e2=unaryExpression();
					state._fsp--;
					if (state.failed) return retval;
					if ( state.backtracking==0 ) stream_unaryExpression.add(e2.getTree());
					// AST REWRITE
					// elements: e2, e1
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
					// 493:11: -> ^( ENUM_VALUE_RANGE $e1 $e2)
					{
						// org/eclipse/tracecompass/ctf/parser/CTFParser.g:493:14: ^( ENUM_VALUE_RANGE $e1 $e2)
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
	// org/eclipse/tracecompass/ctf/parser/CTFParser.g:497:1: declarator : ( pointer )* directDeclarator -> ^( TYPE_DECLARATOR ( pointer )* directDeclarator ) ;
	public final CTFParser.declarator_return declarator() throws RecognitionException {
		CTFParser.declarator_return retval = new CTFParser.declarator_return();
		retval.start = input.LT(1);

		CommonTree root_0 = null;

		ParserRuleReturnScope pointer126 =null;
		ParserRuleReturnScope directDeclarator127 =null;

		RewriteRuleSubtreeStream stream_directDeclarator=new RewriteRuleSubtreeStream(adaptor,"rule directDeclarator");
		RewriteRuleSubtreeStream stream_pointer=new RewriteRuleSubtreeStream(adaptor,"rule pointer");

		try {
			// org/eclipse/tracecompass/ctf/parser/CTFParser.g:498:3: ( ( pointer )* directDeclarator -> ^( TYPE_DECLARATOR ( pointer )* directDeclarator ) )
			// org/eclipse/tracecompass/ctf/parser/CTFParser.g:498:5: ( pointer )* directDeclarator
			{
			// org/eclipse/tracecompass/ctf/parser/CTFParser.g:498:5: ( pointer )*
			loop39:
			while (true) {
				int alt39=2;
				int LA39_0 = input.LA(1);
				if ( (LA39_0==POINTER) ) {
					alt39=1;
				}

				switch (alt39) {
				case 1 :
					// org/eclipse/tracecompass/ctf/parser/CTFParser.g:498:5: pointer
					{
					pushFollow(FOLLOW_pointer_in_declarator2718);
					pointer126=pointer();
					state._fsp--;
					if (state.failed) return retval;
					if ( state.backtracking==0 ) stream_pointer.add(pointer126.getTree());
					}
					break;

				default :
					break loop39;
				}
			}

			pushFollow(FOLLOW_directDeclarator_in_declarator2721);
			directDeclarator127=directDeclarator();
			state._fsp--;
			if (state.failed) return retval;
			if ( state.backtracking==0 ) stream_directDeclarator.add(directDeclarator127.getTree());
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
			// 499:7: -> ^( TYPE_DECLARATOR ( pointer )* directDeclarator )
			{
				// org/eclipse/tracecompass/ctf/parser/CTFParser.g:499:10: ^( TYPE_DECLARATOR ( pointer )* directDeclarator )
				{
				CommonTree root_1 = (CommonTree)adaptor.nil();
				root_1 = (CommonTree)adaptor.becomeRoot((CommonTree)adaptor.create(TYPE_DECLARATOR, "TYPE_DECLARATOR"), root_1);
				// org/eclipse/tracecompass/ctf/parser/CTFParser.g:499:28: ( pointer )*
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
	// org/eclipse/tracecompass/ctf/parser/CTFParser.g:502:1: directDeclarator : ( IDENTIFIER ) ( directDeclaratorSuffix )* ;
	public final CTFParser.directDeclarator_return directDeclarator() throws RecognitionException {
		CTFParser.directDeclarator_return retval = new CTFParser.directDeclarator_return();
		retval.start = input.LT(1);

		CommonTree root_0 = null;

		Token IDENTIFIER128=null;
		ParserRuleReturnScope directDeclaratorSuffix129 =null;

		CommonTree IDENTIFIER128_tree=null;

		try {
			// org/eclipse/tracecompass/ctf/parser/CTFParser.g:503:3: ( ( IDENTIFIER ) ( directDeclaratorSuffix )* )
			// org/eclipse/tracecompass/ctf/parser/CTFParser.g:503:5: ( IDENTIFIER ) ( directDeclaratorSuffix )*
			{
			root_0 = (CommonTree)adaptor.nil();


			// org/eclipse/tracecompass/ctf/parser/CTFParser.g:503:5: ( IDENTIFIER )
			// org/eclipse/tracecompass/ctf/parser/CTFParser.g:504:7: IDENTIFIER
			{
			IDENTIFIER128=(Token)match(input,IDENTIFIER,FOLLOW_IDENTIFIER_in_directDeclarator2759); if (state.failed) return retval;
			if ( state.backtracking==0 ) {
			IDENTIFIER128_tree = (CommonTree)adaptor.create(IDENTIFIER128);
			adaptor.addChild(root_0, IDENTIFIER128_tree);
			}

			if ( state.backtracking==0 ) { if (inTypedef()) addTypeName((IDENTIFIER128!=null?IDENTIFIER128.getText():null)); }
			if ( state.backtracking==0 ) { debug_print((IDENTIFIER128!=null?IDENTIFIER128.getText():null)); }
			}

			// org/eclipse/tracecompass/ctf/parser/CTFParser.g:509:5: ( directDeclaratorSuffix )*
			loop40:
			while (true) {
				int alt40=2;
				int LA40_0 = input.LA(1);
				if ( (LA40_0==OPENBRAC) ) {
					alt40=1;
				}

				switch (alt40) {
				case 1 :
					// org/eclipse/tracecompass/ctf/parser/CTFParser.g:509:5: directDeclaratorSuffix
					{
					pushFollow(FOLLOW_directDeclaratorSuffix_in_directDeclarator2799);
					directDeclaratorSuffix129=directDeclaratorSuffix();
					state._fsp--;
					if (state.failed) return retval;
					if ( state.backtracking==0 ) adaptor.addChild(root_0, directDeclaratorSuffix129.getTree());

					}
					break;

				default :
					break loop40;
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
	// org/eclipse/tracecompass/ctf/parser/CTFParser.g:512:1: directDeclaratorSuffix : OPENBRAC directDeclaratorLength CLOSEBRAC -> ^( LENGTH directDeclaratorLength ) ;
	public final CTFParser.directDeclaratorSuffix_return directDeclaratorSuffix() throws RecognitionException {
		CTFParser.directDeclaratorSuffix_return retval = new CTFParser.directDeclaratorSuffix_return();
		retval.start = input.LT(1);

		CommonTree root_0 = null;

		Token OPENBRAC130=null;
		Token CLOSEBRAC132=null;
		ParserRuleReturnScope directDeclaratorLength131 =null;

		CommonTree OPENBRAC130_tree=null;
		CommonTree CLOSEBRAC132_tree=null;
		RewriteRuleTokenStream stream_OPENBRAC=new RewriteRuleTokenStream(adaptor,"token OPENBRAC");
		RewriteRuleTokenStream stream_CLOSEBRAC=new RewriteRuleTokenStream(adaptor,"token CLOSEBRAC");
		RewriteRuleSubtreeStream stream_directDeclaratorLength=new RewriteRuleSubtreeStream(adaptor,"rule directDeclaratorLength");

		try {
			// org/eclipse/tracecompass/ctf/parser/CTFParser.g:513:3: ( OPENBRAC directDeclaratorLength CLOSEBRAC -> ^( LENGTH directDeclaratorLength ) )
			// org/eclipse/tracecompass/ctf/parser/CTFParser.g:513:5: OPENBRAC directDeclaratorLength CLOSEBRAC
			{
			OPENBRAC130=(Token)match(input,OPENBRAC,FOLLOW_OPENBRAC_in_directDeclaratorSuffix2813); if (state.failed) return retval; 
			if ( state.backtracking==0 ) stream_OPENBRAC.add(OPENBRAC130);

			pushFollow(FOLLOW_directDeclaratorLength_in_directDeclaratorSuffix2815);
			directDeclaratorLength131=directDeclaratorLength();
			state._fsp--;
			if (state.failed) return retval;
			if ( state.backtracking==0 ) stream_directDeclaratorLength.add(directDeclaratorLength131.getTree());
			CLOSEBRAC132=(Token)match(input,CLOSEBRAC,FOLLOW_CLOSEBRAC_in_directDeclaratorSuffix2817); if (state.failed) return retval; 
			if ( state.backtracking==0 ) stream_CLOSEBRAC.add(CLOSEBRAC132);

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
			// 514:7: -> ^( LENGTH directDeclaratorLength )
			{
				// org/eclipse/tracecompass/ctf/parser/CTFParser.g:514:10: ^( LENGTH directDeclaratorLength )
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
	// org/eclipse/tracecompass/ctf/parser/CTFParser.g:517:1: directDeclaratorLength : unaryExpression ;
	public final CTFParser.directDeclaratorLength_return directDeclaratorLength() throws RecognitionException {
		CTFParser.directDeclaratorLength_return retval = new CTFParser.directDeclaratorLength_return();
		retval.start = input.LT(1);

		CommonTree root_0 = null;

		ParserRuleReturnScope unaryExpression133 =null;


		try {
			// org/eclipse/tracecompass/ctf/parser/CTFParser.g:518:3: ( unaryExpression )
			// org/eclipse/tracecompass/ctf/parser/CTFParser.g:518:5: unaryExpression
			{
			root_0 = (CommonTree)adaptor.nil();


			pushFollow(FOLLOW_unaryExpression_in_directDeclaratorLength2845);
			unaryExpression133=unaryExpression();
			state._fsp--;
			if (state.failed) return retval;
			if ( state.backtracking==0 ) adaptor.addChild(root_0, unaryExpression133.getTree());

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
	// org/eclipse/tracecompass/ctf/parser/CTFParser.g:521:1: abstractDeclarator : ( ( pointer )+ ( directAbstractDeclarator )? -> ^( TYPE_DECLARATOR ( pointer )+ ( directAbstractDeclarator )? ) | directAbstractDeclarator -> ^( TYPE_DECLARATOR directAbstractDeclarator ) );
	public final CTFParser.abstractDeclarator_return abstractDeclarator() throws RecognitionException {
		CTFParser.abstractDeclarator_return retval = new CTFParser.abstractDeclarator_return();
		retval.start = input.LT(1);

		CommonTree root_0 = null;

		ParserRuleReturnScope pointer134 =null;
		ParserRuleReturnScope directAbstractDeclarator135 =null;
		ParserRuleReturnScope directAbstractDeclarator136 =null;

		RewriteRuleSubtreeStream stream_pointer=new RewriteRuleSubtreeStream(adaptor,"rule pointer");
		RewriteRuleSubtreeStream stream_directAbstractDeclarator=new RewriteRuleSubtreeStream(adaptor,"rule directAbstractDeclarator");

		try {
			// org/eclipse/tracecompass/ctf/parser/CTFParser.g:522:3: ( ( pointer )+ ( directAbstractDeclarator )? -> ^( TYPE_DECLARATOR ( pointer )+ ( directAbstractDeclarator )? ) | directAbstractDeclarator -> ^( TYPE_DECLARATOR directAbstractDeclarator ) )
			int alt43=2;
			int LA43_0 = input.LA(1);
			if ( (LA43_0==POINTER) ) {
				alt43=1;
			}
			else if ( (LA43_0==IDENTIFIER||LA43_0==LPAREN) ) {
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
					// org/eclipse/tracecompass/ctf/parser/CTFParser.g:522:5: ( pointer )+ ( directAbstractDeclarator )?
					{
					// org/eclipse/tracecompass/ctf/parser/CTFParser.g:522:5: ( pointer )+
					int cnt41=0;
					loop41:
					while (true) {
						int alt41=2;
						int LA41_0 = input.LA(1);
						if ( (LA41_0==POINTER) ) {
							alt41=1;
						}

						switch (alt41) {
						case 1 :
							// org/eclipse/tracecompass/ctf/parser/CTFParser.g:522:5: pointer
							{
							pushFollow(FOLLOW_pointer_in_abstractDeclarator2858);
							pointer134=pointer();
							state._fsp--;
							if (state.failed) return retval;
							if ( state.backtracking==0 ) stream_pointer.add(pointer134.getTree());
							}
							break;

						default :
							if ( cnt41 >= 1 ) break loop41;
							if (state.backtracking>0) {state.failed=true; return retval;}
							EarlyExitException eee = new EarlyExitException(41, input);
							throw eee;
						}
						cnt41++;
					}

					// org/eclipse/tracecompass/ctf/parser/CTFParser.g:522:14: ( directAbstractDeclarator )?
					int alt42=2;
					int LA42_0 = input.LA(1);
					if ( (LA42_0==IDENTIFIER||LA42_0==LPAREN) ) {
						alt42=1;
					}
					switch (alt42) {
						case 1 :
							// org/eclipse/tracecompass/ctf/parser/CTFParser.g:522:14: directAbstractDeclarator
							{
							pushFollow(FOLLOW_directAbstractDeclarator_in_abstractDeclarator2861);
							directAbstractDeclarator135=directAbstractDeclarator();
							state._fsp--;
							if (state.failed) return retval;
							if ( state.backtracking==0 ) stream_directAbstractDeclarator.add(directAbstractDeclarator135.getTree());
							}
							break;

					}

					// AST REWRITE
					// elements: directAbstractDeclarator, pointer
					// token labels: 
					// rule labels: retval
					// token list labels: 
					// rule list labels: 
					// wildcard labels: 
					if ( state.backtracking==0 ) {
					retval.tree = root_0;
					RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.getTree():null);

					root_0 = (CommonTree)adaptor.nil();
					// 523:7: -> ^( TYPE_DECLARATOR ( pointer )+ ( directAbstractDeclarator )? )
					{
						// org/eclipse/tracecompass/ctf/parser/CTFParser.g:523:10: ^( TYPE_DECLARATOR ( pointer )+ ( directAbstractDeclarator )? )
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

						// org/eclipse/tracecompass/ctf/parser/CTFParser.g:523:37: ( directAbstractDeclarator )?
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
					// org/eclipse/tracecompass/ctf/parser/CTFParser.g:524:5: directAbstractDeclarator
					{
					pushFollow(FOLLOW_directAbstractDeclarator_in_abstractDeclarator2886);
					directAbstractDeclarator136=directAbstractDeclarator();
					state._fsp--;
					if (state.failed) return retval;
					if ( state.backtracking==0 ) stream_directAbstractDeclarator.add(directAbstractDeclarator136.getTree());
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
					// 525:7: -> ^( TYPE_DECLARATOR directAbstractDeclarator )
					{
						// org/eclipse/tracecompass/ctf/parser/CTFParser.g:525:10: ^( TYPE_DECLARATOR directAbstractDeclarator )
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
	// org/eclipse/tracecompass/ctf/parser/CTFParser.g:533:1: directAbstractDeclarator : ( IDENTIFIER | ( LPAREN abstractDeclarator RPAREN ) ) ( OPENBRAC ( unaryExpression )? CLOSEBRAC )? ;
	public final CTFParser.directAbstractDeclarator_return directAbstractDeclarator() throws RecognitionException {
		CTFParser.directAbstractDeclarator_return retval = new CTFParser.directAbstractDeclarator_return();
		retval.start = input.LT(1);

		CommonTree root_0 = null;

		Token IDENTIFIER137=null;
		Token LPAREN138=null;
		Token RPAREN140=null;
		Token OPENBRAC141=null;
		Token CLOSEBRAC143=null;
		ParserRuleReturnScope abstractDeclarator139 =null;
		ParserRuleReturnScope unaryExpression142 =null;

		CommonTree IDENTIFIER137_tree=null;
		CommonTree LPAREN138_tree=null;
		CommonTree RPAREN140_tree=null;
		CommonTree OPENBRAC141_tree=null;
		CommonTree CLOSEBRAC143_tree=null;

		try {
			// org/eclipse/tracecompass/ctf/parser/CTFParser.g:534:3: ( ( IDENTIFIER | ( LPAREN abstractDeclarator RPAREN ) ) ( OPENBRAC ( unaryExpression )? CLOSEBRAC )? )
			// org/eclipse/tracecompass/ctf/parser/CTFParser.g:534:5: ( IDENTIFIER | ( LPAREN abstractDeclarator RPAREN ) ) ( OPENBRAC ( unaryExpression )? CLOSEBRAC )?
			{
			root_0 = (CommonTree)adaptor.nil();


			// org/eclipse/tracecompass/ctf/parser/CTFParser.g:534:5: ( IDENTIFIER | ( LPAREN abstractDeclarator RPAREN ) )
			int alt44=2;
			int LA44_0 = input.LA(1);
			if ( (LA44_0==IDENTIFIER) ) {
				alt44=1;
			}
			else if ( (LA44_0==LPAREN) ) {
				alt44=2;
			}

			else {
				if (state.backtracking>0) {state.failed=true; return retval;}
				NoViableAltException nvae =
					new NoViableAltException("", 44, 0, input);
				throw nvae;
			}

			switch (alt44) {
				case 1 :
					// org/eclipse/tracecompass/ctf/parser/CTFParser.g:535:7: IDENTIFIER
					{
					IDENTIFIER137=(Token)match(input,IDENTIFIER,FOLLOW_IDENTIFIER_in_directAbstractDeclarator2923); if (state.failed) return retval;
					if ( state.backtracking==0 ) {
					IDENTIFIER137_tree = (CommonTree)adaptor.create(IDENTIFIER137);
					adaptor.addChild(root_0, IDENTIFIER137_tree);
					}

					}
					break;
				case 2 :
					// org/eclipse/tracecompass/ctf/parser/CTFParser.g:536:9: ( LPAREN abstractDeclarator RPAREN )
					{
					// org/eclipse/tracecompass/ctf/parser/CTFParser.g:536:9: ( LPAREN abstractDeclarator RPAREN )
					// org/eclipse/tracecompass/ctf/parser/CTFParser.g:536:10: LPAREN abstractDeclarator RPAREN
					{
					LPAREN138=(Token)match(input,LPAREN,FOLLOW_LPAREN_in_directAbstractDeclarator2934); if (state.failed) return retval;
					if ( state.backtracking==0 ) {
					LPAREN138_tree = (CommonTree)adaptor.create(LPAREN138);
					adaptor.addChild(root_0, LPAREN138_tree);
					}

					pushFollow(FOLLOW_abstractDeclarator_in_directAbstractDeclarator2936);
					abstractDeclarator139=abstractDeclarator();
					state._fsp--;
					if (state.failed) return retval;
					if ( state.backtracking==0 ) adaptor.addChild(root_0, abstractDeclarator139.getTree());

					RPAREN140=(Token)match(input,RPAREN,FOLLOW_RPAREN_in_directAbstractDeclarator2938); if (state.failed) return retval;
					if ( state.backtracking==0 ) {
					RPAREN140_tree = (CommonTree)adaptor.create(RPAREN140);
					adaptor.addChild(root_0, RPAREN140_tree);
					}

					}

					}
					break;

			}

			// org/eclipse/tracecompass/ctf/parser/CTFParser.g:537:5: ( OPENBRAC ( unaryExpression )? CLOSEBRAC )?
			int alt46=2;
			int LA46_0 = input.LA(1);
			if ( (LA46_0==OPENBRAC) ) {
				alt46=1;
			}
			switch (alt46) {
				case 1 :
					// org/eclipse/tracecompass/ctf/parser/CTFParser.g:538:7: OPENBRAC ( unaryExpression )? CLOSEBRAC
					{
					OPENBRAC141=(Token)match(input,OPENBRAC,FOLLOW_OPENBRAC_in_directAbstractDeclarator2953); if (state.failed) return retval;
					if ( state.backtracking==0 ) {
					OPENBRAC141_tree = (CommonTree)adaptor.create(OPENBRAC141);
					adaptor.addChild(root_0, OPENBRAC141_tree);
					}

					// org/eclipse/tracecompass/ctf/parser/CTFParser.g:538:16: ( unaryExpression )?
					int alt45=2;
					int LA45_0 = input.LA(1);
					if ( (LA45_0==ALIGNTOK||(LA45_0 >= CALLSITETOK && LA45_0 <= CHARACTER_LITERAL)||LA45_0==CLOCKTOK||LA45_0==DECIMAL_LITERAL||LA45_0==ENVTOK||LA45_0==EVENTTOK||LA45_0==HEX_LITERAL||LA45_0==IDENTIFIER||LA45_0==OCTAL_LITERAL||(LA45_0 >= SIGN && LA45_0 <= SIGNEDTOK)||LA45_0==STREAMTOK||LA45_0==STRINGTOK||LA45_0==STRING_LITERAL||LA45_0==TRACETOK) ) {
						alt45=1;
					}
					switch (alt45) {
						case 1 :
							// org/eclipse/tracecompass/ctf/parser/CTFParser.g:538:16: unaryExpression
							{
							pushFollow(FOLLOW_unaryExpression_in_directAbstractDeclarator2955);
							unaryExpression142=unaryExpression();
							state._fsp--;
							if (state.failed) return retval;
							if ( state.backtracking==0 ) adaptor.addChild(root_0, unaryExpression142.getTree());

							}
							break;

					}

					CLOSEBRAC143=(Token)match(input,CLOSEBRAC,FOLLOW_CLOSEBRAC_in_directAbstractDeclarator2958); if (state.failed) return retval;
					if ( state.backtracking==0 ) {
					CLOSEBRAC143_tree = (CommonTree)adaptor.create(CLOSEBRAC143);
					adaptor.addChild(root_0, CLOSEBRAC143_tree);
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
	// org/eclipse/tracecompass/ctf/parser/CTFParser.g:542:1: pointer : POINTER ( typeQualifierList )? -> ^( POINTER ( typeQualifierList )? ) ;
	public final CTFParser.pointer_return pointer() throws RecognitionException {
		CTFParser.pointer_return retval = new CTFParser.pointer_return();
		retval.start = input.LT(1);

		CommonTree root_0 = null;

		Token POINTER144=null;
		ParserRuleReturnScope typeQualifierList145 =null;

		CommonTree POINTER144_tree=null;
		RewriteRuleTokenStream stream_POINTER=new RewriteRuleTokenStream(adaptor,"token POINTER");
		RewriteRuleSubtreeStream stream_typeQualifierList=new RewriteRuleSubtreeStream(adaptor,"rule typeQualifierList");

		try {
			// org/eclipse/tracecompass/ctf/parser/CTFParser.g:543:3: ( POINTER ( typeQualifierList )? -> ^( POINTER ( typeQualifierList )? ) )
			// org/eclipse/tracecompass/ctf/parser/CTFParser.g:543:5: POINTER ( typeQualifierList )?
			{
			POINTER144=(Token)match(input,POINTER,FOLLOW_POINTER_in_pointer2976); if (state.failed) return retval; 
			if ( state.backtracking==0 ) stream_POINTER.add(POINTER144);

			// org/eclipse/tracecompass/ctf/parser/CTFParser.g:543:13: ( typeQualifierList )?
			int alt47=2;
			int LA47_0 = input.LA(1);
			if ( (LA47_0==CONSTTOK) ) {
				alt47=1;
			}
			switch (alt47) {
				case 1 :
					// org/eclipse/tracecompass/ctf/parser/CTFParser.g:543:13: typeQualifierList
					{
					pushFollow(FOLLOW_typeQualifierList_in_pointer2978);
					typeQualifierList145=typeQualifierList();
					state._fsp--;
					if (state.failed) return retval;
					if ( state.backtracking==0 ) stream_typeQualifierList.add(typeQualifierList145.getTree());
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
			// 543:32: -> ^( POINTER ( typeQualifierList )? )
			{
				// org/eclipse/tracecompass/ctf/parser/CTFParser.g:543:35: ^( POINTER ( typeQualifierList )? )
				{
				CommonTree root_1 = (CommonTree)adaptor.nil();
				root_1 = (CommonTree)adaptor.becomeRoot(stream_POINTER.nextNode(), root_1);
				// org/eclipse/tracecompass/ctf/parser/CTFParser.g:543:45: ( typeQualifierList )?
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
	// org/eclipse/tracecompass/ctf/parser/CTFParser.g:546:1: typeQualifierList : ( typeQualifier )+ ;
	public final CTFParser.typeQualifierList_return typeQualifierList() throws RecognitionException {
		CTFParser.typeQualifierList_return retval = new CTFParser.typeQualifierList_return();
		retval.start = input.LT(1);

		CommonTree root_0 = null;

		ParserRuleReturnScope typeQualifier146 =null;


		try {
			// org/eclipse/tracecompass/ctf/parser/CTFParser.g:547:3: ( ( typeQualifier )+ )
			// org/eclipse/tracecompass/ctf/parser/CTFParser.g:547:5: ( typeQualifier )+
			{
			root_0 = (CommonTree)adaptor.nil();


			// org/eclipse/tracecompass/ctf/parser/CTFParser.g:547:5: ( typeQualifier )+
			int cnt48=0;
			loop48:
			while (true) {
				int alt48=2;
				int LA48_0 = input.LA(1);
				if ( (LA48_0==CONSTTOK) ) {
					alt48=1;
				}

				switch (alt48) {
				case 1 :
					// org/eclipse/tracecompass/ctf/parser/CTFParser.g:547:5: typeQualifier
					{
					pushFollow(FOLLOW_typeQualifier_in_typeQualifierList3001);
					typeQualifier146=typeQualifier();
					state._fsp--;
					if (state.failed) return retval;
					if ( state.backtracking==0 ) adaptor.addChild(root_0, typeQualifier146.getTree());

					}
					break;

				default :
					if ( cnt48 >= 1 ) break loop48;
					if (state.backtracking>0) {state.failed=true; return retval;}
					EarlyExitException eee = new EarlyExitException(48, input);
					throw eee;
				}
				cnt48++;
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
	// org/eclipse/tracecompass/ctf/parser/CTFParser.g:550:1: typedefName :{...}? IDENTIFIER ;
	public final CTFParser.typedefName_return typedefName() throws RecognitionException {
		CTFParser.typedefName_return retval = new CTFParser.typedefName_return();
		retval.start = input.LT(1);

		CommonTree root_0 = null;

		Token IDENTIFIER147=null;

		CommonTree IDENTIFIER147_tree=null;

		try {
			// org/eclipse/tracecompass/ctf/parser/CTFParser.g:551:3: ({...}? IDENTIFIER )
			// org/eclipse/tracecompass/ctf/parser/CTFParser.g:551:5: {...}? IDENTIFIER
			{
			root_0 = (CommonTree)adaptor.nil();


			if ( !((inTypealiasAlias() || isTypeName(input.LT(1).getText()))) ) {
				if (state.backtracking>0) {state.failed=true; return retval;}
				throw new FailedPredicateException(input, "typedefName", "inTypealiasAlias() || isTypeName(input.LT(1).getText())");
			}
			IDENTIFIER147=(Token)match(input,IDENTIFIER,FOLLOW_IDENTIFIER_in_typedefName3017); if (state.failed) return retval;
			if ( state.backtracking==0 ) {
			IDENTIFIER147_tree = (CommonTree)adaptor.create(IDENTIFIER147);
			adaptor.addChild(root_0, IDENTIFIER147_tree);
			}

			if ( state.backtracking==0 ) { if ((inTypedef() || inTypealiasAlias()) && !isTypeName((IDENTIFIER147!=null?IDENTIFIER147.getText():null))) { addTypeName((IDENTIFIER147!=null?IDENTIFIER147.getText():null)); } }
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
	// org/eclipse/tracecompass/ctf/parser/CTFParser.g:560:1: typealiasTarget : declarationSpecifiers ( abstractDeclaratorList )? ;
	public final CTFParser.typealiasTarget_return typealiasTarget() throws RecognitionException {
		CTFParser.typealiasTarget_return retval = new CTFParser.typealiasTarget_return();
		retval.start = input.LT(1);

		CommonTree root_0 = null;

		ParserRuleReturnScope declarationSpecifiers148 =null;
		ParserRuleReturnScope abstractDeclaratorList149 =null;


		try {
			// org/eclipse/tracecompass/ctf/parser/CTFParser.g:561:3: ( declarationSpecifiers ( abstractDeclaratorList )? )
			// org/eclipse/tracecompass/ctf/parser/CTFParser.g:561:5: declarationSpecifiers ( abstractDeclaratorList )?
			{
			root_0 = (CommonTree)adaptor.nil();


			pushFollow(FOLLOW_declarationSpecifiers_in_typealiasTarget3034);
			declarationSpecifiers148=declarationSpecifiers();
			state._fsp--;
			if (state.failed) return retval;
			if ( state.backtracking==0 ) adaptor.addChild(root_0, declarationSpecifiers148.getTree());

			// org/eclipse/tracecompass/ctf/parser/CTFParser.g:561:27: ( abstractDeclaratorList )?
			int alt49=2;
			int LA49_0 = input.LA(1);
			if ( (LA49_0==IDENTIFIER||LA49_0==LPAREN||LA49_0==POINTER) ) {
				alt49=1;
			}
			switch (alt49) {
				case 1 :
					// org/eclipse/tracecompass/ctf/parser/CTFParser.g:561:27: abstractDeclaratorList
					{
					pushFollow(FOLLOW_abstractDeclaratorList_in_typealiasTarget3036);
					abstractDeclaratorList149=abstractDeclaratorList();
					state._fsp--;
					if (state.failed) return retval;
					if ( state.backtracking==0 ) adaptor.addChild(root_0, abstractDeclaratorList149.getTree());

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
	// org/eclipse/tracecompass/ctf/parser/CTFParser.g:570:1: typealiasAlias : ( abstractDeclaratorList | declarationSpecifiers ( abstractDeclaratorList )? );
	public final CTFParser.typealiasAlias_return typealiasAlias() throws RecognitionException {
		CTFParser.typealiasAlias_return retval = new CTFParser.typealiasAlias_return();
		retval.start = input.LT(1);

		CommonTree root_0 = null;

		ParserRuleReturnScope abstractDeclaratorList150 =null;
		ParserRuleReturnScope declarationSpecifiers151 =null;
		ParserRuleReturnScope abstractDeclaratorList152 =null;



		    typealiasAliasOn();

		try {
			// org/eclipse/tracecompass/ctf/parser/CTFParser.g:577:3: ( abstractDeclaratorList | declarationSpecifiers ( abstractDeclaratorList )? )
			int alt51=2;
			switch ( input.LA(1) ) {
			case LPAREN:
			case POINTER:
				{
				alt51=1;
				}
				break;
			case IDENTIFIER:
				{
				int LA51_2 = input.LA(2);
				if ( (!(((( inTypealiasAlias() || isTypeName(input.LT(1).getText()) )&&(inTypealiasAlias() || isTypeName(input.LT(1).getText())))))) ) {
					alt51=1;
				}
				else if ( ((( inTypealiasAlias() || isTypeName(input.LT(1).getText()) )&&(inTypealiasAlias() || isTypeName(input.LT(1).getText())))) ) {
					alt51=2;
				}

				else {
					if (state.backtracking>0) {state.failed=true; return retval;}
					int nvaeMark = input.mark();
					try {
						input.consume();
						NoViableAltException nvae =
							new NoViableAltException("", 51, 2, input);
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
				alt51=2;
				}
				break;
			default:
				if (state.backtracking>0) {state.failed=true; return retval;}
				NoViableAltException nvae =
					new NoViableAltException("", 51, 0, input);
				throw nvae;
			}
			switch (alt51) {
				case 1 :
					// org/eclipse/tracecompass/ctf/parser/CTFParser.g:577:5: abstractDeclaratorList
					{
					root_0 = (CommonTree)adaptor.nil();


					pushFollow(FOLLOW_abstractDeclaratorList_in_typealiasAlias3062);
					abstractDeclaratorList150=abstractDeclaratorList();
					state._fsp--;
					if (state.failed) return retval;
					if ( state.backtracking==0 ) adaptor.addChild(root_0, abstractDeclaratorList150.getTree());

					}
					break;
				case 2 :
					// org/eclipse/tracecompass/ctf/parser/CTFParser.g:578:5: declarationSpecifiers ( abstractDeclaratorList )?
					{
					root_0 = (CommonTree)adaptor.nil();


					pushFollow(FOLLOW_declarationSpecifiers_in_typealiasAlias3068);
					declarationSpecifiers151=declarationSpecifiers();
					state._fsp--;
					if (state.failed) return retval;
					if ( state.backtracking==0 ) adaptor.addChild(root_0, declarationSpecifiers151.getTree());

					// org/eclipse/tracecompass/ctf/parser/CTFParser.g:578:27: ( abstractDeclaratorList )?
					int alt50=2;
					int LA50_0 = input.LA(1);
					if ( (LA50_0==IDENTIFIER||LA50_0==LPAREN||LA50_0==POINTER) ) {
						alt50=1;
					}
					switch (alt50) {
						case 1 :
							// org/eclipse/tracecompass/ctf/parser/CTFParser.g:578:27: abstractDeclaratorList
							{
							pushFollow(FOLLOW_abstractDeclaratorList_in_typealiasAlias3070);
							abstractDeclaratorList152=abstractDeclaratorList();
							state._fsp--;
							if (state.failed) return retval;
							if ( state.backtracking==0 ) adaptor.addChild(root_0, abstractDeclaratorList152.getTree());

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
	// org/eclipse/tracecompass/ctf/parser/CTFParser.g:581:1: typealiasDecl : TYPEALIASTOK typealiasTarget TYPE_ASSIGNMENT typealiasAlias -> ^( TYPEALIAS ^( TYPEALIAS_TARGET typealiasTarget ) ^( TYPEALIAS_ALIAS typealiasAlias ) ) ;
	public final CTFParser.typealiasDecl_return typealiasDecl() throws RecognitionException {
		CTFParser.typealiasDecl_return retval = new CTFParser.typealiasDecl_return();
		retval.start = input.LT(1);

		CommonTree root_0 = null;

		Token TYPEALIASTOK153=null;
		Token TYPE_ASSIGNMENT155=null;
		ParserRuleReturnScope typealiasTarget154 =null;
		ParserRuleReturnScope typealiasAlias156 =null;

		CommonTree TYPEALIASTOK153_tree=null;
		CommonTree TYPE_ASSIGNMENT155_tree=null;
		RewriteRuleTokenStream stream_TYPE_ASSIGNMENT=new RewriteRuleTokenStream(adaptor,"token TYPE_ASSIGNMENT");
		RewriteRuleTokenStream stream_TYPEALIASTOK=new RewriteRuleTokenStream(adaptor,"token TYPEALIASTOK");
		RewriteRuleSubtreeStream stream_typealiasAlias=new RewriteRuleSubtreeStream(adaptor,"rule typealiasAlias");
		RewriteRuleSubtreeStream stream_typealiasTarget=new RewriteRuleSubtreeStream(adaptor,"rule typealiasTarget");

		try {
			// org/eclipse/tracecompass/ctf/parser/CTFParser.g:582:3: ( TYPEALIASTOK typealiasTarget TYPE_ASSIGNMENT typealiasAlias -> ^( TYPEALIAS ^( TYPEALIAS_TARGET typealiasTarget ) ^( TYPEALIAS_ALIAS typealiasAlias ) ) )
			// org/eclipse/tracecompass/ctf/parser/CTFParser.g:582:5: TYPEALIASTOK typealiasTarget TYPE_ASSIGNMENT typealiasAlias
			{
			TYPEALIASTOK153=(Token)match(input,TYPEALIASTOK,FOLLOW_TYPEALIASTOK_in_typealiasDecl3084); if (state.failed) return retval; 
			if ( state.backtracking==0 ) stream_TYPEALIASTOK.add(TYPEALIASTOK153);

			pushFollow(FOLLOW_typealiasTarget_in_typealiasDecl3086);
			typealiasTarget154=typealiasTarget();
			state._fsp--;
			if (state.failed) return retval;
			if ( state.backtracking==0 ) stream_typealiasTarget.add(typealiasTarget154.getTree());
			TYPE_ASSIGNMENT155=(Token)match(input,TYPE_ASSIGNMENT,FOLLOW_TYPE_ASSIGNMENT_in_typealiasDecl3088); if (state.failed) return retval; 
			if ( state.backtracking==0 ) stream_TYPE_ASSIGNMENT.add(TYPE_ASSIGNMENT155);

			pushFollow(FOLLOW_typealiasAlias_in_typealiasDecl3090);
			typealiasAlias156=typealiasAlias();
			state._fsp--;
			if (state.failed) return retval;
			if ( state.backtracking==0 ) stream_typealiasAlias.add(typealiasAlias156.getTree());
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
			// 583:7: -> ^( TYPEALIAS ^( TYPEALIAS_TARGET typealiasTarget ) ^( TYPEALIAS_ALIAS typealiasAlias ) )
			{
				// org/eclipse/tracecompass/ctf/parser/CTFParser.g:583:10: ^( TYPEALIAS ^( TYPEALIAS_TARGET typealiasTarget ) ^( TYPEALIAS_ALIAS typealiasAlias ) )
				{
				CommonTree root_1 = (CommonTree)adaptor.nil();
				root_1 = (CommonTree)adaptor.becomeRoot((CommonTree)adaptor.create(TYPEALIAS, "TYPEALIAS"), root_1);
				// org/eclipse/tracecompass/ctf/parser/CTFParser.g:584:14: ^( TYPEALIAS_TARGET typealiasTarget )
				{
				CommonTree root_2 = (CommonTree)adaptor.nil();
				root_2 = (CommonTree)adaptor.becomeRoot((CommonTree)adaptor.create(TYPEALIAS_TARGET, "TYPEALIAS_TARGET"), root_2);
				adaptor.addChild(root_2, stream_typealiasTarget.nextTree());
				adaptor.addChild(root_1, root_2);
				}

				// org/eclipse/tracecompass/ctf/parser/CTFParser.g:585:14: ^( TYPEALIAS_ALIAS typealiasAlias )
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
	// org/eclipse/tracecompass/ctf/parser/CTFParser.g:591:1: ctfKeyword : ( ALIGNTOK | EVENTTOK | SIGNEDTOK | STRINGTOK );
	public final CTFParser.ctfKeyword_return ctfKeyword() throws RecognitionException {
		CTFParser.ctfKeyword_return retval = new CTFParser.ctfKeyword_return();
		retval.start = input.LT(1);

		CommonTree root_0 = null;

		Token set157=null;

		CommonTree set157_tree=null;

		try {
			// org/eclipse/tracecompass/ctf/parser/CTFParser.g:592:3: ( ALIGNTOK | EVENTTOK | SIGNEDTOK | STRINGTOK )
			// org/eclipse/tracecompass/ctf/parser/CTFParser.g:
			{
			root_0 = (CommonTree)adaptor.nil();


			set157=input.LT(1);
			if ( input.LA(1)==ALIGNTOK||input.LA(1)==EVENTTOK||input.LA(1)==SIGNEDTOK||input.LA(1)==STRINGTOK ) {
				input.consume();
				if ( state.backtracking==0 ) adaptor.addChild(root_0, (CommonTree)adaptor.create(set157));
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
	// org/eclipse/tracecompass/ctf/parser/CTFParser.g:598:1: ctfSpecifier : ( ctfSpecifierHead ctfBody -> ^( ctfSpecifierHead ctfBody ) | typealiasDecl -> ^( DECLARATION typealiasDecl ) );
	public final CTFParser.ctfSpecifier_return ctfSpecifier() throws RecognitionException {
		CTFParser.ctfSpecifier_return retval = new CTFParser.ctfSpecifier_return();
		retval.start = input.LT(1);

		CommonTree root_0 = null;

		ParserRuleReturnScope ctfSpecifierHead158 =null;
		ParserRuleReturnScope ctfBody159 =null;
		ParserRuleReturnScope typealiasDecl160 =null;

		RewriteRuleSubtreeStream stream_ctfSpecifierHead=new RewriteRuleSubtreeStream(adaptor,"rule ctfSpecifierHead");
		RewriteRuleSubtreeStream stream_typealiasDecl=new RewriteRuleSubtreeStream(adaptor,"rule typealiasDecl");
		RewriteRuleSubtreeStream stream_ctfBody=new RewriteRuleSubtreeStream(adaptor,"rule ctfBody");

		try {
			// org/eclipse/tracecompass/ctf/parser/CTFParser.g:600:3: ( ctfSpecifierHead ctfBody -> ^( ctfSpecifierHead ctfBody ) | typealiasDecl -> ^( DECLARATION typealiasDecl ) )
			int alt52=2;
			int LA52_0 = input.LA(1);
			if ( (LA52_0==CALLSITETOK||LA52_0==CLOCKTOK||LA52_0==ENVTOK||LA52_0==EVENTTOK||LA52_0==STREAMTOK||LA52_0==TRACETOK) ) {
				alt52=1;
			}
			else if ( (LA52_0==TYPEALIASTOK) ) {
				alt52=2;
			}

			else {
				if (state.backtracking>0) {state.failed=true; return retval;}
				NoViableAltException nvae =
					new NoViableAltException("", 52, 0, input);
				throw nvae;
			}

			switch (alt52) {
				case 1 :
					// org/eclipse/tracecompass/ctf/parser/CTFParser.g:600:5: ctfSpecifierHead ctfBody
					{
					pushFollow(FOLLOW_ctfSpecifierHead_in_ctfSpecifier3190);
					ctfSpecifierHead158=ctfSpecifierHead();
					state._fsp--;
					if (state.failed) return retval;
					if ( state.backtracking==0 ) stream_ctfSpecifierHead.add(ctfSpecifierHead158.getTree());
					pushFollow(FOLLOW_ctfBody_in_ctfSpecifier3192);
					ctfBody159=ctfBody();
					state._fsp--;
					if (state.failed) return retval;
					if ( state.backtracking==0 ) stream_ctfBody.add(ctfBody159.getTree());
					// AST REWRITE
					// elements: ctfBody, ctfSpecifierHead
					// token labels: 
					// rule labels: retval
					// token list labels: 
					// rule list labels: 
					// wildcard labels: 
					if ( state.backtracking==0 ) {
					retval.tree = root_0;
					RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.getTree():null);

					root_0 = (CommonTree)adaptor.nil();
					// 600:30: -> ^( ctfSpecifierHead ctfBody )
					{
						// org/eclipse/tracecompass/ctf/parser/CTFParser.g:600:33: ^( ctfSpecifierHead ctfBody )
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
					// org/eclipse/tracecompass/ctf/parser/CTFParser.g:602:5: typealiasDecl
					{
					pushFollow(FOLLOW_typealiasDecl_in_ctfSpecifier3209);
					typealiasDecl160=typealiasDecl();
					state._fsp--;
					if (state.failed) return retval;
					if ( state.backtracking==0 ) stream_typealiasDecl.add(typealiasDecl160.getTree());
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
					// 602:19: -> ^( DECLARATION typealiasDecl )
					{
						// org/eclipse/tracecompass/ctf/parser/CTFParser.g:602:22: ^( DECLARATION typealiasDecl )
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
	// org/eclipse/tracecompass/ctf/parser/CTFParser.g:605:1: ctfSpecifierHead : ( EVENTTOK -> EVENT | STREAMTOK -> STREAM | TRACETOK -> TRACE | ENVTOK -> ENV | CLOCKTOK -> CLOCK | CALLSITETOK -> CALLSITE );
	public final CTFParser.ctfSpecifierHead_return ctfSpecifierHead() throws RecognitionException {
		CTFParser.ctfSpecifierHead_return retval = new CTFParser.ctfSpecifierHead_return();
		retval.start = input.LT(1);

		CommonTree root_0 = null;

		Token EVENTTOK161=null;
		Token STREAMTOK162=null;
		Token TRACETOK163=null;
		Token ENVTOK164=null;
		Token CLOCKTOK165=null;
		Token CALLSITETOK166=null;

		CommonTree EVENTTOK161_tree=null;
		CommonTree STREAMTOK162_tree=null;
		CommonTree TRACETOK163_tree=null;
		CommonTree ENVTOK164_tree=null;
		CommonTree CLOCKTOK165_tree=null;
		CommonTree CALLSITETOK166_tree=null;
		RewriteRuleTokenStream stream_EVENTTOK=new RewriteRuleTokenStream(adaptor,"token EVENTTOK");
		RewriteRuleTokenStream stream_CALLSITETOK=new RewriteRuleTokenStream(adaptor,"token CALLSITETOK");
		RewriteRuleTokenStream stream_STREAMTOK=new RewriteRuleTokenStream(adaptor,"token STREAMTOK");
		RewriteRuleTokenStream stream_ENVTOK=new RewriteRuleTokenStream(adaptor,"token ENVTOK");
		RewriteRuleTokenStream stream_CLOCKTOK=new RewriteRuleTokenStream(adaptor,"token CLOCKTOK");
		RewriteRuleTokenStream stream_TRACETOK=new RewriteRuleTokenStream(adaptor,"token TRACETOK");

		try {
			// org/eclipse/tracecompass/ctf/parser/CTFParser.g:606:3: ( EVENTTOK -> EVENT | STREAMTOK -> STREAM | TRACETOK -> TRACE | ENVTOK -> ENV | CLOCKTOK -> CLOCK | CALLSITETOK -> CALLSITE )
			int alt53=6;
			switch ( input.LA(1) ) {
			case EVENTTOK:
				{
				alt53=1;
				}
				break;
			case STREAMTOK:
				{
				alt53=2;
				}
				break;
			case TRACETOK:
				{
				alt53=3;
				}
				break;
			case ENVTOK:
				{
				alt53=4;
				}
				break;
			case CLOCKTOK:
				{
				alt53=5;
				}
				break;
			case CALLSITETOK:
				{
				alt53=6;
				}
				break;
			default:
				if (state.backtracking>0) {state.failed=true; return retval;}
				NoViableAltException nvae =
					new NoViableAltException("", 53, 0, input);
				throw nvae;
			}
			switch (alt53) {
				case 1 :
					// org/eclipse/tracecompass/ctf/parser/CTFParser.g:606:5: EVENTTOK
					{
					EVENTTOK161=(Token)match(input,EVENTTOK,FOLLOW_EVENTTOK_in_ctfSpecifierHead3230); if (state.failed) return retval; 
					if ( state.backtracking==0 ) stream_EVENTTOK.add(EVENTTOK161);

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
					// 606:14: -> EVENT
					{
						adaptor.addChild(root_0, (CommonTree)adaptor.create(EVENT, "EVENT"));
					}


					retval.tree = root_0;
					}

					}
					break;
				case 2 :
					// org/eclipse/tracecompass/ctf/parser/CTFParser.g:607:5: STREAMTOK
					{
					STREAMTOK162=(Token)match(input,STREAMTOK,FOLLOW_STREAMTOK_in_ctfSpecifierHead3240); if (state.failed) return retval; 
					if ( state.backtracking==0 ) stream_STREAMTOK.add(STREAMTOK162);

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
					// 607:15: -> STREAM
					{
						adaptor.addChild(root_0, (CommonTree)adaptor.create(STREAM, "STREAM"));
					}


					retval.tree = root_0;
					}

					}
					break;
				case 3 :
					// org/eclipse/tracecompass/ctf/parser/CTFParser.g:608:5: TRACETOK
					{
					TRACETOK163=(Token)match(input,TRACETOK,FOLLOW_TRACETOK_in_ctfSpecifierHead3250); if (state.failed) return retval; 
					if ( state.backtracking==0 ) stream_TRACETOK.add(TRACETOK163);

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
					// 608:14: -> TRACE
					{
						adaptor.addChild(root_0, (CommonTree)adaptor.create(TRACE, "TRACE"));
					}


					retval.tree = root_0;
					}

					}
					break;
				case 4 :
					// org/eclipse/tracecompass/ctf/parser/CTFParser.g:609:5: ENVTOK
					{
					ENVTOK164=(Token)match(input,ENVTOK,FOLLOW_ENVTOK_in_ctfSpecifierHead3260); if (state.failed) return retval; 
					if ( state.backtracking==0 ) stream_ENVTOK.add(ENVTOK164);

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
					// 609:12: -> ENV
					{
						adaptor.addChild(root_0, (CommonTree)adaptor.create(ENV, "ENV"));
					}


					retval.tree = root_0;
					}

					}
					break;
				case 5 :
					// org/eclipse/tracecompass/ctf/parser/CTFParser.g:610:5: CLOCKTOK
					{
					CLOCKTOK165=(Token)match(input,CLOCKTOK,FOLLOW_CLOCKTOK_in_ctfSpecifierHead3270); if (state.failed) return retval; 
					if ( state.backtracking==0 ) stream_CLOCKTOK.add(CLOCKTOK165);

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
					// 610:14: -> CLOCK
					{
						adaptor.addChild(root_0, (CommonTree)adaptor.create(CLOCK, "CLOCK"));
					}


					retval.tree = root_0;
					}

					}
					break;
				case 6 :
					// org/eclipse/tracecompass/ctf/parser/CTFParser.g:611:5: CALLSITETOK
					{
					CALLSITETOK166=(Token)match(input,CALLSITETOK,FOLLOW_CALLSITETOK_in_ctfSpecifierHead3280); if (state.failed) return retval; 
					if ( state.backtracking==0 ) stream_CALLSITETOK.add(CALLSITETOK166);

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
					// 611:17: -> CALLSITE
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
	// org/eclipse/tracecompass/ctf/parser/CTFParser.g:614:1: ctfTypeSpecifier : ( FLOATINGPOINTTOK ctfBody -> ^( FLOATING_POINT ( ctfBody )? ) | INTEGERTOK ctfBody -> ^( INTEGER ( ctfBody )? ) | STRINGTOK ( ctfBody )? -> ^( STRING ( ctfBody )? ) );
	public final CTFParser.ctfTypeSpecifier_return ctfTypeSpecifier() throws RecognitionException {
		CTFParser.ctfTypeSpecifier_return retval = new CTFParser.ctfTypeSpecifier_return();
		retval.start = input.LT(1);

		CommonTree root_0 = null;

		Token FLOATINGPOINTTOK167=null;
		Token INTEGERTOK169=null;
		Token STRINGTOK171=null;
		ParserRuleReturnScope ctfBody168 =null;
		ParserRuleReturnScope ctfBody170 =null;
		ParserRuleReturnScope ctfBody172 =null;

		CommonTree FLOATINGPOINTTOK167_tree=null;
		CommonTree INTEGERTOK169_tree=null;
		CommonTree STRINGTOK171_tree=null;
		RewriteRuleTokenStream stream_FLOATINGPOINTTOK=new RewriteRuleTokenStream(adaptor,"token FLOATINGPOINTTOK");
		RewriteRuleTokenStream stream_STRINGTOK=new RewriteRuleTokenStream(adaptor,"token STRINGTOK");
		RewriteRuleTokenStream stream_INTEGERTOK=new RewriteRuleTokenStream(adaptor,"token INTEGERTOK");
		RewriteRuleSubtreeStream stream_ctfBody=new RewriteRuleSubtreeStream(adaptor,"rule ctfBody");

		try {
			// org/eclipse/tracecompass/ctf/parser/CTFParser.g:616:3: ( FLOATINGPOINTTOK ctfBody -> ^( FLOATING_POINT ( ctfBody )? ) | INTEGERTOK ctfBody -> ^( INTEGER ( ctfBody )? ) | STRINGTOK ( ctfBody )? -> ^( STRING ( ctfBody )? ) )
			int alt55=3;
			switch ( input.LA(1) ) {
			case FLOATINGPOINTTOK:
				{
				alt55=1;
				}
				break;
			case INTEGERTOK:
				{
				alt55=2;
				}
				break;
			case STRINGTOK:
				{
				alt55=3;
				}
				break;
			default:
				if (state.backtracking>0) {state.failed=true; return retval;}
				NoViableAltException nvae =
					new NoViableAltException("", 55, 0, input);
				throw nvae;
			}
			switch (alt55) {
				case 1 :
					// org/eclipse/tracecompass/ctf/parser/CTFParser.g:616:5: FLOATINGPOINTTOK ctfBody
					{
					FLOATINGPOINTTOK167=(Token)match(input,FLOATINGPOINTTOK,FOLLOW_FLOATINGPOINTTOK_in_ctfTypeSpecifier3303); if (state.failed) return retval; 
					if ( state.backtracking==0 ) stream_FLOATINGPOINTTOK.add(FLOATINGPOINTTOK167);

					pushFollow(FOLLOW_ctfBody_in_ctfTypeSpecifier3305);
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
					// 616:30: -> ^( FLOATING_POINT ( ctfBody )? )
					{
						// org/eclipse/tracecompass/ctf/parser/CTFParser.g:616:33: ^( FLOATING_POINT ( ctfBody )? )
						{
						CommonTree root_1 = (CommonTree)adaptor.nil();
						root_1 = (CommonTree)adaptor.becomeRoot((CommonTree)adaptor.create(FLOATING_POINT, "FLOATING_POINT"), root_1);
						// org/eclipse/tracecompass/ctf/parser/CTFParser.g:616:50: ( ctfBody )?
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
					// org/eclipse/tracecompass/ctf/parser/CTFParser.g:617:5: INTEGERTOK ctfBody
					{
					INTEGERTOK169=(Token)match(input,INTEGERTOK,FOLLOW_INTEGERTOK_in_ctfTypeSpecifier3320); if (state.failed) return retval; 
					if ( state.backtracking==0 ) stream_INTEGERTOK.add(INTEGERTOK169);

					pushFollow(FOLLOW_ctfBody_in_ctfTypeSpecifier3322);
					ctfBody170=ctfBody();
					state._fsp--;
					if (state.failed) return retval;
					if ( state.backtracking==0 ) stream_ctfBody.add(ctfBody170.getTree());
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
					// 617:24: -> ^( INTEGER ( ctfBody )? )
					{
						// org/eclipse/tracecompass/ctf/parser/CTFParser.g:617:27: ^( INTEGER ( ctfBody )? )
						{
						CommonTree root_1 = (CommonTree)adaptor.nil();
						root_1 = (CommonTree)adaptor.becomeRoot((CommonTree)adaptor.create(INTEGER, "INTEGER"), root_1);
						// org/eclipse/tracecompass/ctf/parser/CTFParser.g:617:37: ( ctfBody )?
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
					// org/eclipse/tracecompass/ctf/parser/CTFParser.g:618:5: STRINGTOK ( ctfBody )?
					{
					STRINGTOK171=(Token)match(input,STRINGTOK,FOLLOW_STRINGTOK_in_ctfTypeSpecifier3337); if (state.failed) return retval; 
					if ( state.backtracking==0 ) stream_STRINGTOK.add(STRINGTOK171);

					// org/eclipse/tracecompass/ctf/parser/CTFParser.g:618:15: ( ctfBody )?
					int alt54=2;
					int LA54_0 = input.LA(1);
					if ( (LA54_0==LCURL) ) {
						alt54=1;
					}
					switch (alt54) {
						case 1 :
							// org/eclipse/tracecompass/ctf/parser/CTFParser.g:618:15: ctfBody
							{
							pushFollow(FOLLOW_ctfBody_in_ctfTypeSpecifier3339);
							ctfBody172=ctfBody();
							state._fsp--;
							if (state.failed) return retval;
							if ( state.backtracking==0 ) stream_ctfBody.add(ctfBody172.getTree());
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
					// 618:24: -> ^( STRING ( ctfBody )? )
					{
						// org/eclipse/tracecompass/ctf/parser/CTFParser.g:618:27: ^( STRING ( ctfBody )? )
						{
						CommonTree root_1 = (CommonTree)adaptor.nil();
						root_1 = (CommonTree)adaptor.becomeRoot((CommonTree)adaptor.create(STRING, "STRING"), root_1);
						// org/eclipse/tracecompass/ctf/parser/CTFParser.g:618:36: ( ctfBody )?
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
	// org/eclipse/tracecompass/ctf/parser/CTFParser.g:621:1: ctfBody : LCURL ( ctfAssignmentExpressionList )? RCURL -> ( ctfAssignmentExpressionList )? ;
	public final CTFParser.ctfBody_return ctfBody() throws RecognitionException {
		Symbols_stack.push(new Symbols_scope());

		CTFParser.ctfBody_return retval = new CTFParser.ctfBody_return();
		retval.start = input.LT(1);

		CommonTree root_0 = null;

		Token LCURL173=null;
		Token RCURL175=null;
		ParserRuleReturnScope ctfAssignmentExpressionList174 =null;

		CommonTree LCURL173_tree=null;
		CommonTree RCURL175_tree=null;
		RewriteRuleTokenStream stream_LCURL=new RewriteRuleTokenStream(adaptor,"token LCURL");
		RewriteRuleTokenStream stream_RCURL=new RewriteRuleTokenStream(adaptor,"token RCURL");
		RewriteRuleSubtreeStream stream_ctfAssignmentExpressionList=new RewriteRuleSubtreeStream(adaptor,"rule ctfAssignmentExpressionList");


		    Symbols_stack.peek().types = new HashSet<String>();

		try {
			// org/eclipse/tracecompass/ctf/parser/CTFParser.g:626:3: ( LCURL ( ctfAssignmentExpressionList )? RCURL -> ( ctfAssignmentExpressionList )? )
			// org/eclipse/tracecompass/ctf/parser/CTFParser.g:626:5: LCURL ( ctfAssignmentExpressionList )? RCURL
			{
			LCURL173=(Token)match(input,LCURL,FOLLOW_LCURL_in_ctfBody3372); if (state.failed) return retval; 
			if ( state.backtracking==0 ) stream_LCURL.add(LCURL173);

			// org/eclipse/tracecompass/ctf/parser/CTFParser.g:626:11: ( ctfAssignmentExpressionList )?
			int alt56=2;
			int LA56_0 = input.LA(1);
			if ( (LA56_0==ALIGNTOK||(LA56_0 >= BOOLTOK && LA56_0 <= CHARTOK)||LA56_0==CLOCKTOK||(LA56_0 >= COMPLEXTOK && LA56_0 <= DECIMAL_LITERAL)||LA56_0==DOUBLETOK||(LA56_0 >= ENUMTOK && LA56_0 <= ENVTOK)||(LA56_0 >= EVENTTOK && LA56_0 <= FLOATTOK)||LA56_0==HEX_LITERAL||(LA56_0 >= IDENTIFIER && LA56_0 <= IMAGINARYTOK)||LA56_0==INTEGERTOK||LA56_0==INTTOK||LA56_0==LONGTOK||LA56_0==OCTAL_LITERAL||(LA56_0 >= SHORTTOK && LA56_0 <= SIGNEDTOK)||LA56_0==STREAMTOK||LA56_0==STRINGTOK||(LA56_0 >= STRING_LITERAL && LA56_0 <= STRUCTTOK)||(LA56_0 >= TRACETOK && LA56_0 <= TYPEDEFTOK)||(LA56_0 >= UNSIGNEDTOK && LA56_0 <= VOIDTOK)) ) {
				alt56=1;
			}
			switch (alt56) {
				case 1 :
					// org/eclipse/tracecompass/ctf/parser/CTFParser.g:626:11: ctfAssignmentExpressionList
					{
					pushFollow(FOLLOW_ctfAssignmentExpressionList_in_ctfBody3374);
					ctfAssignmentExpressionList174=ctfAssignmentExpressionList();
					state._fsp--;
					if (state.failed) return retval;
					if ( state.backtracking==0 ) stream_ctfAssignmentExpressionList.add(ctfAssignmentExpressionList174.getTree());
					}
					break;

			}

			RCURL175=(Token)match(input,RCURL,FOLLOW_RCURL_in_ctfBody3377); if (state.failed) return retval; 
			if ( state.backtracking==0 ) stream_RCURL.add(RCURL175);

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
			// 626:46: -> ( ctfAssignmentExpressionList )?
			{
				// org/eclipse/tracecompass/ctf/parser/CTFParser.g:626:49: ( ctfAssignmentExpressionList )?
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
	// org/eclipse/tracecompass/ctf/parser/CTFParser.g:629:1: ctfAssignmentExpressionList : ( ctfAssignmentExpression TERM !)+ ;
	public final CTFParser.ctfAssignmentExpressionList_return ctfAssignmentExpressionList() throws RecognitionException {
		CTFParser.ctfAssignmentExpressionList_return retval = new CTFParser.ctfAssignmentExpressionList_return();
		retval.start = input.LT(1);

		CommonTree root_0 = null;

		Token TERM177=null;
		ParserRuleReturnScope ctfAssignmentExpression176 =null;

		CommonTree TERM177_tree=null;

		try {
			// org/eclipse/tracecompass/ctf/parser/CTFParser.g:630:3: ( ( ctfAssignmentExpression TERM !)+ )
			// org/eclipse/tracecompass/ctf/parser/CTFParser.g:630:5: ( ctfAssignmentExpression TERM !)+
			{
			root_0 = (CommonTree)adaptor.nil();


			// org/eclipse/tracecompass/ctf/parser/CTFParser.g:630:5: ( ctfAssignmentExpression TERM !)+
			int cnt57=0;
			loop57:
			while (true) {
				int alt57=2;
				int LA57_0 = input.LA(1);
				if ( (LA57_0==ALIGNTOK||(LA57_0 >= BOOLTOK && LA57_0 <= CHARTOK)||LA57_0==CLOCKTOK||(LA57_0 >= COMPLEXTOK && LA57_0 <= DECIMAL_LITERAL)||LA57_0==DOUBLETOK||(LA57_0 >= ENUMTOK && LA57_0 <= ENVTOK)||(LA57_0 >= EVENTTOK && LA57_0 <= FLOATTOK)||LA57_0==HEX_LITERAL||(LA57_0 >= IDENTIFIER && LA57_0 <= IMAGINARYTOK)||LA57_0==INTEGERTOK||LA57_0==INTTOK||LA57_0==LONGTOK||LA57_0==OCTAL_LITERAL||(LA57_0 >= SHORTTOK && LA57_0 <= SIGNEDTOK)||LA57_0==STREAMTOK||LA57_0==STRINGTOK||(LA57_0 >= STRING_LITERAL && LA57_0 <= STRUCTTOK)||(LA57_0 >= TRACETOK && LA57_0 <= TYPEDEFTOK)||(LA57_0 >= UNSIGNEDTOK && LA57_0 <= VOIDTOK)) ) {
					alt57=1;
				}

				switch (alt57) {
				case 1 :
					// org/eclipse/tracecompass/ctf/parser/CTFParser.g:630:6: ctfAssignmentExpression TERM !
					{
					pushFollow(FOLLOW_ctfAssignmentExpression_in_ctfAssignmentExpressionList3396);
					ctfAssignmentExpression176=ctfAssignmentExpression();
					state._fsp--;
					if (state.failed) return retval;
					if ( state.backtracking==0 ) adaptor.addChild(root_0, ctfAssignmentExpression176.getTree());

					TERM177=(Token)match(input,TERM,FOLLOW_TERM_in_ctfAssignmentExpressionList3398); if (state.failed) return retval;
					}
					break;

				default :
					if ( cnt57 >= 1 ) break loop57;
					if (state.backtracking>0) {state.failed=true; return retval;}
					EarlyExitException eee = new EarlyExitException(57, input);
					throw eee;
				}
				cnt57++;
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
	// org/eclipse/tracecompass/ctf/parser/CTFParser.g:633:1: ctfAssignmentExpression : (left= unaryExpression (assignment= ASSIGNMENT right1= unaryExpression -> ^( CTF_EXPRESSION_VAL ^( CTF_LEFT $left) ^( CTF_RIGHT $right1) ) |type_assignment= TYPE_ASSIGNMENT right2= typeSpecifier -> ^( CTF_EXPRESSION_TYPE ^( CTF_LEFT $left) ^( CTF_RIGHT ^( TYPE_SPECIFIER_LIST $right2) ) ) ) | ( declarationSpecifiers {...}? declaratorList ) -> ^( TYPEDEF declaratorList declarationSpecifiers ) | typealiasDecl );
	public final CTFParser.ctfAssignmentExpression_return ctfAssignmentExpression() throws RecognitionException {
		CTFParser.ctfAssignmentExpression_return retval = new CTFParser.ctfAssignmentExpression_return();
		retval.start = input.LT(1);

		CommonTree root_0 = null;

		Token assignment=null;
		Token type_assignment=null;
		ParserRuleReturnScope left =null;
		ParserRuleReturnScope right1 =null;
		ParserRuleReturnScope right2 =null;
		ParserRuleReturnScope declarationSpecifiers178 =null;
		ParserRuleReturnScope declaratorList179 =null;
		ParserRuleReturnScope typealiasDecl180 =null;

		CommonTree assignment_tree=null;
		CommonTree type_assignment_tree=null;
		RewriteRuleTokenStream stream_ASSIGNMENT=new RewriteRuleTokenStream(adaptor,"token ASSIGNMENT");
		RewriteRuleTokenStream stream_TYPE_ASSIGNMENT=new RewriteRuleTokenStream(adaptor,"token TYPE_ASSIGNMENT");
		RewriteRuleSubtreeStream stream_declaratorList=new RewriteRuleSubtreeStream(adaptor,"rule declaratorList");
		RewriteRuleSubtreeStream stream_unaryExpression=new RewriteRuleSubtreeStream(adaptor,"rule unaryExpression");
		RewriteRuleSubtreeStream stream_declarationSpecifiers=new RewriteRuleSubtreeStream(adaptor,"rule declarationSpecifiers");
		RewriteRuleSubtreeStream stream_typeSpecifier=new RewriteRuleSubtreeStream(adaptor,"rule typeSpecifier");

		try {
			// org/eclipse/tracecompass/ctf/parser/CTFParser.g:639:3: (left= unaryExpression (assignment= ASSIGNMENT right1= unaryExpression -> ^( CTF_EXPRESSION_VAL ^( CTF_LEFT $left) ^( CTF_RIGHT $right1) ) |type_assignment= TYPE_ASSIGNMENT right2= typeSpecifier -> ^( CTF_EXPRESSION_TYPE ^( CTF_LEFT $left) ^( CTF_RIGHT ^( TYPE_SPECIFIER_LIST $right2) ) ) ) | ( declarationSpecifiers {...}? declaratorList ) -> ^( TYPEDEF declaratorList declarationSpecifiers ) | typealiasDecl )
			int alt59=3;
			switch ( input.LA(1) ) {
			case IDENTIFIER:
				{
				int LA59_1 = input.LA(2);
				if ( ((LA59_1 >= ARROW && LA59_1 <= ASSIGNMENT)||LA59_1==DOT||LA59_1==OPENBRAC||LA59_1==TYPE_ASSIGNMENT) ) {
					alt59=1;
				}
				else if ( (LA59_1==BOOLTOK||LA59_1==CHARTOK||(LA59_1 >= COMPLEXTOK && LA59_1 <= CONSTTOK)||LA59_1==DOUBLETOK||LA59_1==ENUMTOK||(LA59_1 >= FLOATINGPOINTTOK && LA59_1 <= FLOATTOK)||(LA59_1 >= IDENTIFIER && LA59_1 <= IMAGINARYTOK)||LA59_1==INTEGERTOK||LA59_1==INTTOK||LA59_1==LONGTOK||LA59_1==POINTER||LA59_1==SHORTTOK||LA59_1==SIGNEDTOK||LA59_1==STRINGTOK||LA59_1==STRUCTTOK||LA59_1==TYPEDEFTOK||(LA59_1 >= UNSIGNEDTOK && LA59_1 <= VOIDTOK)) && (( inTypealiasAlias() || isTypeName(input.LT(1).getText()) ))) {
					alt59=2;
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
				alt59=1;
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
					alt59=1;
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
					alt59=2;
					}
					break;
				case TYPEDEFTOK:
					{
					alt59=2;
					}
					break;
				default:
					if (state.backtracking>0) {state.failed=true; return retval;}
					int nvaeMark = input.mark();
					try {
						input.consume();
						NoViableAltException nvae =
							new NoViableAltException("", 59, 3, input);
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
				alt59=2;
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
					alt59=1;
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
					alt59=2;
					}
					break;
				case TYPEDEFTOK:
					{
					alt59=2;
					}
					break;
				default:
					if (state.backtracking>0) {state.failed=true; return retval;}
					int nvaeMark = input.mark();
					try {
						input.consume();
						NoViableAltException nvae =
							new NoViableAltException("", 59, 5, input);
						throw nvae;
					} finally {
						input.rewind(nvaeMark);
					}
				}
				}
				break;
			case TYPEALIASTOK:
				{
				alt59=3;
				}
				break;
			default:
				if (state.backtracking>0) {state.failed=true; return retval;}
				NoViableAltException nvae =
					new NoViableAltException("", 59, 0, input);
				throw nvae;
			}
			switch (alt59) {
				case 1 :
					// org/eclipse/tracecompass/ctf/parser/CTFParser.g:639:5: left= unaryExpression (assignment= ASSIGNMENT right1= unaryExpression -> ^( CTF_EXPRESSION_VAL ^( CTF_LEFT $left) ^( CTF_RIGHT $right1) ) |type_assignment= TYPE_ASSIGNMENT right2= typeSpecifier -> ^( CTF_EXPRESSION_TYPE ^( CTF_LEFT $left) ^( CTF_RIGHT ^( TYPE_SPECIFIER_LIST $right2) ) ) )
					{
					pushFollow(FOLLOW_unaryExpression_in_ctfAssignmentExpression3421);
					left=unaryExpression();
					state._fsp--;
					if (state.failed) return retval;
					if ( state.backtracking==0 ) stream_unaryExpression.add(left.getTree());
					// org/eclipse/tracecompass/ctf/parser/CTFParser.g:640:7: (assignment= ASSIGNMENT right1= unaryExpression -> ^( CTF_EXPRESSION_VAL ^( CTF_LEFT $left) ^( CTF_RIGHT $right1) ) |type_assignment= TYPE_ASSIGNMENT right2= typeSpecifier -> ^( CTF_EXPRESSION_TYPE ^( CTF_LEFT $left) ^( CTF_RIGHT ^( TYPE_SPECIFIER_LIST $right2) ) ) )
					int alt58=2;
					int LA58_0 = input.LA(1);
					if ( (LA58_0==ASSIGNMENT) ) {
						alt58=1;
					}
					else if ( (LA58_0==TYPE_ASSIGNMENT) ) {
						alt58=2;
					}

					else {
						if (state.backtracking>0) {state.failed=true; return retval;}
						NoViableAltException nvae =
							new NoViableAltException("", 58, 0, input);
						throw nvae;
					}

					switch (alt58) {
						case 1 :
							// org/eclipse/tracecompass/ctf/parser/CTFParser.g:640:9: assignment= ASSIGNMENT right1= unaryExpression
							{
							assignment=(Token)match(input,ASSIGNMENT,FOLLOW_ASSIGNMENT_in_ctfAssignmentExpression3433); if (state.failed) return retval; 
							if ( state.backtracking==0 ) stream_ASSIGNMENT.add(assignment);

							pushFollow(FOLLOW_unaryExpression_in_ctfAssignmentExpression3437);
							right1=unaryExpression();
							state._fsp--;
							if (state.failed) return retval;
							if ( state.backtracking==0 ) stream_unaryExpression.add(right1.getTree());
							// AST REWRITE
							// elements: left, right1
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
							// 641:11: -> ^( CTF_EXPRESSION_VAL ^( CTF_LEFT $left) ^( CTF_RIGHT $right1) )
							{
								// org/eclipse/tracecompass/ctf/parser/CTFParser.g:641:14: ^( CTF_EXPRESSION_VAL ^( CTF_LEFT $left) ^( CTF_RIGHT $right1) )
								{
								CommonTree root_1 = (CommonTree)adaptor.nil();
								root_1 = (CommonTree)adaptor.becomeRoot((CommonTree)adaptor.create(CTF_EXPRESSION_VAL, "CTF_EXPRESSION_VAL"), root_1);
								// org/eclipse/tracecompass/ctf/parser/CTFParser.g:642:18: ^( CTF_LEFT $left)
								{
								CommonTree root_2 = (CommonTree)adaptor.nil();
								root_2 = (CommonTree)adaptor.becomeRoot((CommonTree)adaptor.create(CTF_LEFT, "CTF_LEFT"), root_2);
								adaptor.addChild(root_2, stream_left.nextTree());
								adaptor.addChild(root_1, root_2);
								}

								// org/eclipse/tracecompass/ctf/parser/CTFParser.g:643:18: ^( CTF_RIGHT $right1)
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
							// org/eclipse/tracecompass/ctf/parser/CTFParser.g:644:9: type_assignment= TYPE_ASSIGNMENT right2= typeSpecifier
							{
							type_assignment=(Token)match(input,TYPE_ASSIGNMENT,FOLLOW_TYPE_ASSIGNMENT_in_ctfAssignmentExpression3513); if (state.failed) return retval; 
							if ( state.backtracking==0 ) stream_TYPE_ASSIGNMENT.add(type_assignment);

							pushFollow(FOLLOW_typeSpecifier_in_ctfAssignmentExpression3517);
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
							// 645:11: -> ^( CTF_EXPRESSION_TYPE ^( CTF_LEFT $left) ^( CTF_RIGHT ^( TYPE_SPECIFIER_LIST $right2) ) )
							{
								// org/eclipse/tracecompass/ctf/parser/CTFParser.g:645:14: ^( CTF_EXPRESSION_TYPE ^( CTF_LEFT $left) ^( CTF_RIGHT ^( TYPE_SPECIFIER_LIST $right2) ) )
								{
								CommonTree root_1 = (CommonTree)adaptor.nil();
								root_1 = (CommonTree)adaptor.becomeRoot((CommonTree)adaptor.create(CTF_EXPRESSION_TYPE, "CTF_EXPRESSION_TYPE"), root_1);
								// org/eclipse/tracecompass/ctf/parser/CTFParser.g:646:18: ^( CTF_LEFT $left)
								{
								CommonTree root_2 = (CommonTree)adaptor.nil();
								root_2 = (CommonTree)adaptor.becomeRoot((CommonTree)adaptor.create(CTF_LEFT, "CTF_LEFT"), root_2);
								adaptor.addChild(root_2, stream_left.nextTree());
								adaptor.addChild(root_1, root_2);
								}

								// org/eclipse/tracecompass/ctf/parser/CTFParser.g:647:18: ^( CTF_RIGHT ^( TYPE_SPECIFIER_LIST $right2) )
								{
								CommonTree root_2 = (CommonTree)adaptor.nil();
								root_2 = (CommonTree)adaptor.becomeRoot((CommonTree)adaptor.create(CTF_RIGHT, "CTF_RIGHT"), root_2);
								// org/eclipse/tracecompass/ctf/parser/CTFParser.g:647:30: ^( TYPE_SPECIFIER_LIST $right2)
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
					// org/eclipse/tracecompass/ctf/parser/CTFParser.g:649:5: ( declarationSpecifiers {...}? declaratorList )
					{
					// org/eclipse/tracecompass/ctf/parser/CTFParser.g:649:5: ( declarationSpecifiers {...}? declaratorList )
					// org/eclipse/tracecompass/ctf/parser/CTFParser.g:649:6: declarationSpecifiers {...}? declaratorList
					{
					pushFollow(FOLLOW_declarationSpecifiers_in_ctfAssignmentExpression3600);
					declarationSpecifiers178=declarationSpecifiers();
					state._fsp--;
					if (state.failed) return retval;
					if ( state.backtracking==0 ) stream_declarationSpecifiers.add(declarationSpecifiers178.getTree());
					if ( !((inTypedef())) ) {
						if (state.backtracking>0) {state.failed=true; return retval;}
						throw new FailedPredicateException(input, "ctfAssignmentExpression", "inTypedef()");
					}
					pushFollow(FOLLOW_declaratorList_in_ctfAssignmentExpression3604);
					declaratorList179=declaratorList();
					state._fsp--;
					if (state.failed) return retval;
					if ( state.backtracking==0 ) stream_declaratorList.add(declaratorList179.getTree());
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
					// 650:7: -> ^( TYPEDEF declaratorList declarationSpecifiers )
					{
						// org/eclipse/tracecompass/ctf/parser/CTFParser.g:650:10: ^( TYPEDEF declaratorList declarationSpecifiers )
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
					// org/eclipse/tracecompass/ctf/parser/CTFParser.g:651:5: typealiasDecl
					{
					root_0 = (CommonTree)adaptor.nil();


					pushFollow(FOLLOW_typealiasDecl_in_ctfAssignmentExpression3627);
					typealiasDecl180=typealiasDecl();
					state._fsp--;
					if (state.failed) return retval;
					if ( state.backtracking==0 ) adaptor.addChild(root_0, typealiasDecl180.getTree());

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
		// org/eclipse/tracecompass/ctf/parser/CTFParser.g:207:5: ( IDENTIFIER )
		// org/eclipse/tracecompass/ctf/parser/CTFParser.g:207:6: IDENTIFIER
		{
		match(input,IDENTIFIER,FOLLOW_IDENTIFIER_in_synpred1_CTFParser560); if (state.failed) return;

		}

	}
	// $ANTLR end synpred1_CTFParser

	// $ANTLR start synpred2_CTFParser
	public final void synpred2_CTFParser_fragment() throws RecognitionException {
		// org/eclipse/tracecompass/ctf/parser/CTFParser.g:209:5: ( ctfKeyword )
		// org/eclipse/tracecompass/ctf/parser/CTFParser.g:209:6: ctfKeyword
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
		// org/eclipse/tracecompass/ctf/parser/CTFParser.g:210:5: ( STRING_LITERAL )
		// org/eclipse/tracecompass/ctf/parser/CTFParser.g:210:6: STRING_LITERAL
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


	protected DFA24 dfa24 = new DFA24(this);
	static final String DFA24_eotS =
		"\10\uffff";
	static final String DFA24_eofS =
		"\10\uffff";
	static final String DFA24_minS =
		"\1\46\1\24\1\0\1\24\1\0\2\uffff\1\0";
	static final String DFA24_maxS =
		"\2\72\1\0\1\72\1\0\2\uffff\1\0";
	static final String DFA24_acceptS =
		"\5\uffff\1\1\1\2\1\uffff";
	static final String DFA24_specialS =
		"\2\uffff\1\2\1\uffff\1\1\2\uffff\1\0}>";
	static final String[] DFA24_transitionS = {
			"\1\2\23\uffff\1\1",
			"\1\3\21\uffff\1\4\23\uffff\1\1",
			"\1\uffff",
			"\1\3\21\uffff\1\7\23\uffff\1\1",
			"\1\uffff",
			"",
			"",
			"\1\uffff"
	};

	static final short[] DFA24_eot = DFA.unpackEncodedString(DFA24_eotS);
	static final short[] DFA24_eof = DFA.unpackEncodedString(DFA24_eofS);
	static final char[] DFA24_min = DFA.unpackEncodedStringToUnsignedChars(DFA24_minS);
	static final char[] DFA24_max = DFA.unpackEncodedStringToUnsignedChars(DFA24_maxS);
	static final short[] DFA24_accept = DFA.unpackEncodedString(DFA24_acceptS);
	static final short[] DFA24_special = DFA.unpackEncodedString(DFA24_specialS);
	static final short[][] DFA24_transition;

	static {
		int numStates = DFA24_transitionS.length;
		DFA24_transition = new short[numStates][];
		for (int i=0; i<numStates; i++) {
			DFA24_transition[i] = DFA.unpackEncodedString(DFA24_transitionS[i]);
		}
	}

	protected class DFA24 extends DFA {

		public DFA24(BaseRecognizer recognizer) {
			this.recognizer = recognizer;
			this.decisionNumber = 24;
			this.eot = DFA24_eot;
			this.eof = DFA24_eof;
			this.min = DFA24_min;
			this.max = DFA24_max;
			this.accept = DFA24_accept;
			this.special = DFA24_special;
			this.transition = DFA24_transition;
		}
		@Override
		public String getDescription() {
			return "372:10: ({...}? => declaratorList -> ^( TYPEDEF declaratorList declarationSpecifiers ) | structOrVariantDeclaratorList -> ^( SV_DECLARATION declarationSpecifiers structOrVariantDeclaratorList ) )";
		}
		@Override
		public int specialStateTransition(int s, IntStream _input) throws NoViableAltException {
			TokenStream input = (TokenStream)_input;
			int _s = s;
			switch ( s ) {
					case 0 : 
						int LA24_7 = input.LA(1);
						 
						int index24_7 = input.index();
						input.rewind();
						s = -1;
						if ( ((inTypedef())) ) {s = 5;}
						else if ( (true) ) {s = 6;}
						 
						input.seek(index24_7);
						if ( s>=0 ) return s;
						break;

					case 1 : 
						int LA24_4 = input.LA(1);
						 
						int index24_4 = input.index();
						input.rewind();
						s = -1;
						if ( ((inTypedef())) ) {s = 5;}
						else if ( (true) ) {s = 6;}
						 
						input.seek(index24_4);
						if ( s>=0 ) return s;
						break;

					case 2 : 
						int LA24_2 = input.LA(1);
						 
						int index24_2 = input.index();
						input.rewind();
						s = -1;
						if ( ((inTypedef())) ) {s = 5;}
						else if ( (true) ) {s = 6;}
						 
						input.seek(index24_2);
						if ( s>=0 ) return s;
						break;
			}
			if (state.backtracking>0) {state.failed=true; return -1;}
			NoViableAltException nvae =
				new NoViableAltException(getDescription(), 24, _s, input);
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
	public static final BitSet FOLLOW_DOT_in_postfixCtfExpression719 = new BitSet(new long[]{0x0000000050002200L,0x0000000000000204L});
	public static final BitSet FOLLOW_ctfSpecifierHead_in_postfixCtfExpression722 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_primaryExpression_in_postfixExpression755 = new BitSet(new long[]{0x0200000000800022L});
	public static final BitSet FOLLOW_postfixExpressionSuffix_in_postfixExpression757 = new BitSet(new long[]{0x0200000000800022L});
	public static final BitSet FOLLOW_ctfSpecifierHead_in_postfixExpression766 = new BitSet(new long[]{0x0200000000800020L});
	public static final BitSet FOLLOW_postfixCtfExpression_in_postfixExpression768 = new BitSet(new long[]{0x0200000000800020L});
	public static final BitSet FOLLOW_postfixExpressionSuffix_in_postfixExpression771 = new BitSet(new long[]{0x0200000000800022L});
	public static final BitSet FOLLOW_postfixExpression_in_unaryExpression787 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_STRING_LITERAL_in_enumConstant804 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_IDENTIFIER_in_enumConstant818 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_ctfKeyword_in_enumConstant832 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_declarationSpecifiers_in_declaration863 = new BitSet(new long[]{0x0400004000000000L,0x0000000000000100L});
	public static final BitSet FOLLOW_declaratorList_in_declaration865 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000100L});
	public static final BitSet FOLLOW_TERM_in_declaration868 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_ctfSpecifier_in_declaration936 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000100L});
	public static final BitSet FOLLOW_TERM_in_declaration938 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_storageClassSpecifier_in_declarationSpecifiers976 = new BitSet(new long[]{0x40004AC18A180902L,0x000000000001C891L});
	public static final BitSet FOLLOW_typeQualifier_in_declarationSpecifiers986 = new BitSet(new long[]{0x40004AC18A180902L,0x000000000001C891L});
	public static final BitSet FOLLOW_typeSpecifier_in_declarationSpecifiers996 = new BitSet(new long[]{0x40004AC18A180902L,0x000000000001C891L});
	public static final BitSet FOLLOW_declarator_in_declaratorList1026 = new BitSet(new long[]{0x2000000000000002L});
	public static final BitSet FOLLOW_SEPARATOR_in_declaratorList1029 = new BitSet(new long[]{0x0400004000000000L});
	public static final BitSet FOLLOW_declarator_in_declaratorList1031 = new BitSet(new long[]{0x2000000000000002L});
	public static final BitSet FOLLOW_abstractDeclarator_in_abstractDeclaratorList1061 = new BitSet(new long[]{0x2000000000000002L});
	public static final BitSet FOLLOW_SEPARATOR_in_abstractDeclaratorList1064 = new BitSet(new long[]{0x0400804000000000L});
	public static final BitSet FOLLOW_abstractDeclarator_in_abstractDeclaratorList1066 = new BitSet(new long[]{0x2000000000000002L});
	public static final BitSet FOLLOW_TYPEDEFTOK_in_storageClassSpecifier1096 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_FLOATTOK_in_typeSpecifier1112 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_INTTOK_in_typeSpecifier1118 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_LONGTOK_in_typeSpecifier1124 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_SHORTTOK_in_typeSpecifier1130 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_SIGNEDTOK_in_typeSpecifier1136 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_UNSIGNEDTOK_in_typeSpecifier1142 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_CHARTOK_in_typeSpecifier1148 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_DOUBLETOK_in_typeSpecifier1154 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_VOIDTOK_in_typeSpecifier1160 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_BOOLTOK_in_typeSpecifier1166 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_COMPLEXTOK_in_typeSpecifier1172 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_IMAGINARYTOK_in_typeSpecifier1178 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_structSpecifier_in_typeSpecifier1184 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_variantSpecifier_in_typeSpecifier1190 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_enumSpecifier_in_typeSpecifier1196 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_ctfTypeSpecifier_in_typeSpecifier1202 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_typedefName_in_typeSpecifier1212 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_CONSTTOK_in_typeQualifier1225 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_ALIGNTOK_in_alignAttribute1238 = new BitSet(new long[]{0x0000800000000000L});
	public static final BitSet FOLLOW_LPAREN_in_alignAttribute1240 = new BitSet(new long[]{0x8040005050202610L,0x0000000000000255L});
	public static final BitSet FOLLOW_unaryExpression_in_alignAttribute1242 = new BitSet(new long[]{0x1000000000000000L});
	public static final BitSet FOLLOW_RPAREN_in_alignAttribute1244 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_LCURL_in_structBody1278 = new BitSet(new long[]{0x48004AC18A180900L,0x000000000001CC91L});
	public static final BitSet FOLLOW_structOrVariantDeclarationList_in_structBody1280 = new BitSet(new long[]{0x0800000000000000L});
	public static final BitSet FOLLOW_RCURL_in_structBody1283 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_STRUCTTOK_in_structSpecifier1311 = new BitSet(new long[]{0x0000104000000000L});
	public static final BitSet FOLLOW_structName_in_structSpecifier1336 = new BitSet(new long[]{0x0000100000000012L});
	public static final BitSet FOLLOW_alignAttribute_in_structSpecifier1358 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_structBody_in_structSpecifier1394 = new BitSet(new long[]{0x0000000000000012L});
	public static final BitSet FOLLOW_alignAttribute_in_structSpecifier1425 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_structBody_in_structSpecifier1541 = new BitSet(new long[]{0x0000000000000012L});
	public static final BitSet FOLLOW_alignAttribute_in_structSpecifier1559 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_IDENTIFIER_in_structName1625 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_structOrVariantDeclaration_in_structOrVariantDeclarationList1646 = new BitSet(new long[]{0x40004AC18A180902L,0x000000000001CC91L});
	public static final BitSet FOLLOW_declarationSpecifiers_in_structOrVariantDeclaration1679 = new BitSet(new long[]{0x0400004000000000L});
	public static final BitSet FOLLOW_declaratorList_in_structOrVariantDeclaration1720 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000100L});
	public static final BitSet FOLLOW_structOrVariantDeclaratorList_in_structOrVariantDeclaration1758 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000100L});
	public static final BitSet FOLLOW_typealiasDecl_in_structOrVariantDeclaration1817 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000100L});
	public static final BitSet FOLLOW_TERM_in_structOrVariantDeclaration1829 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_typeQualifier_in_specifierQualifierList1843 = new BitSet(new long[]{0x40004AC18A180902L,0x000000000001C091L});
	public static final BitSet FOLLOW_typeSpecifier_in_specifierQualifierList1847 = new BitSet(new long[]{0x40004AC18A180902L,0x000000000001C091L});
	public static final BitSet FOLLOW_structOrVariantDeclarator_in_structOrVariantDeclaratorList1880 = new BitSet(new long[]{0x2000000000000002L});
	public static final BitSet FOLLOW_SEPARATOR_in_structOrVariantDeclaratorList1883 = new BitSet(new long[]{0x0400004000000000L});
	public static final BitSet FOLLOW_structOrVariantDeclarator_in_structOrVariantDeclaratorList1885 = new BitSet(new long[]{0x2000000000000002L});
	public static final BitSet FOLLOW_declarator_in_structOrVariantDeclarator1924 = new BitSet(new long[]{0x0000000000008002L});
	public static final BitSet FOLLOW_COLON_in_structOrVariantDeclarator1927 = new BitSet(new long[]{0x8040001000200000L});
	public static final BitSet FOLLOW_numberLiteral_in_structOrVariantDeclarator1929 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_VARIANTTOK_in_variantSpecifier1953 = new BitSet(new long[]{0x0001104000000000L});
	public static final BitSet FOLLOW_variantName_in_variantSpecifier1971 = new BitSet(new long[]{0x0001100000000000L});
	public static final BitSet FOLLOW_variantTag_in_variantSpecifier2001 = new BitSet(new long[]{0x0000100000000002L});
	public static final BitSet FOLLOW_variantBody_in_variantSpecifier2027 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_variantBody_in_variantSpecifier2095 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_variantTag_in_variantSpecifier2116 = new BitSet(new long[]{0x0000100000000000L});
	public static final BitSet FOLLOW_variantBody_in_variantSpecifier2118 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_variantBody_in_variantSpecifier2125 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_IDENTIFIER_in_variantName2157 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_LCURL_in_variantBody2188 = new BitSet(new long[]{0x40004AC18A180900L,0x000000000001CC91L});
	public static final BitSet FOLLOW_structOrVariantDeclarationList_in_variantBody2190 = new BitSet(new long[]{0x0800000000000000L});
	public static final BitSet FOLLOW_RCURL_in_variantBody2192 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_LT_in_variantTag2219 = new BitSet(new long[]{0x0000004000000000L});
	public static final BitSet FOLLOW_IDENTIFIER_in_variantTag2221 = new BitSet(new long[]{0x0000000200000000L});
	public static final BitSet FOLLOW_GT_in_variantTag2223 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_ENUMTOK_in_enumSpecifier2244 = new BitSet(new long[]{0x0000104000008000L});
	public static final BitSet FOLLOW_enumName_in_enumSpecifier2283 = new BitSet(new long[]{0x0000100000008002L});
	public static final BitSet FOLLOW_enumContainerType_in_enumSpecifier2315 = new BitSet(new long[]{0x0000100000000000L});
	public static final BitSet FOLLOW_enumBody_in_enumSpecifier2317 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_enumBody_in_enumSpecifier2347 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_enumContainerType_in_enumSpecifier2439 = new BitSet(new long[]{0x0000100000000000L});
	public static final BitSet FOLLOW_enumBody_in_enumSpecifier2441 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_enumBody_in_enumSpecifier2465 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_IDENTIFIER_in_enumName2509 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_LCURL_in_enumBody2530 = new BitSet(new long[]{0x0000004040000010L,0x0000000000000051L});
	public static final BitSet FOLLOW_enumeratorList_in_enumBody2532 = new BitSet(new long[]{0x2800000000000000L});
	public static final BitSet FOLLOW_SEPARATOR_in_enumBody2534 = new BitSet(new long[]{0x0800000000000000L});
	public static final BitSet FOLLOW_RCURL_in_enumBody2537 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_COLON_in_enumContainerType2558 = new BitSet(new long[]{0x40004AC18A180900L,0x000000000001C891L});
	public static final BitSet FOLLOW_declarationSpecifiers_in_enumContainerType2560 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_enumerator_in_enumeratorList2581 = new BitSet(new long[]{0x2000000000000002L});
	public static final BitSet FOLLOW_SEPARATOR_in_enumeratorList2584 = new BitSet(new long[]{0x0000004040000010L,0x0000000000000051L});
	public static final BitSet FOLLOW_enumerator_in_enumeratorList2586 = new BitSet(new long[]{0x2000000000000002L});
	public static final BitSet FOLLOW_enumConstant_in_enumerator2612 = new BitSet(new long[]{0x0000000000000042L});
	public static final BitSet FOLLOW_enumeratorValue_in_enumerator2614 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_ASSIGNMENT_in_enumeratorValue2628 = new BitSet(new long[]{0x8040005050202610L,0x0000000000000255L});
	public static final BitSet FOLLOW_unaryExpression_in_enumeratorValue2632 = new BitSet(new long[]{0x0000000004000002L});
	public static final BitSet FOLLOW_ELIPSES_in_enumeratorValue2671 = new BitSet(new long[]{0x8040005050202610L,0x0000000000000255L});
	public static final BitSet FOLLOW_unaryExpression_in_enumeratorValue2675 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_pointer_in_declarator2718 = new BitSet(new long[]{0x0400004000000000L});
	public static final BitSet FOLLOW_directDeclarator_in_declarator2721 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_IDENTIFIER_in_directDeclarator2759 = new BitSet(new long[]{0x0200000000000002L});
	public static final BitSet FOLLOW_directDeclaratorSuffix_in_directDeclarator2799 = new BitSet(new long[]{0x0200000000000002L});
	public static final BitSet FOLLOW_OPENBRAC_in_directDeclaratorSuffix2813 = new BitSet(new long[]{0x8040005050202610L,0x0000000000000255L});
	public static final BitSet FOLLOW_directDeclaratorLength_in_directDeclaratorSuffix2815 = new BitSet(new long[]{0x0000000000004000L});
	public static final BitSet FOLLOW_CLOSEBRAC_in_directDeclaratorSuffix2817 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_unaryExpression_in_directDeclaratorLength2845 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_pointer_in_abstractDeclarator2858 = new BitSet(new long[]{0x0400804000000002L});
	public static final BitSet FOLLOW_directAbstractDeclarator_in_abstractDeclarator2861 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_directAbstractDeclarator_in_abstractDeclarator2886 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_IDENTIFIER_in_directAbstractDeclarator2923 = new BitSet(new long[]{0x0200000000000002L});
	public static final BitSet FOLLOW_LPAREN_in_directAbstractDeclarator2934 = new BitSet(new long[]{0x0400804000000000L});
	public static final BitSet FOLLOW_abstractDeclarator_in_directAbstractDeclarator2936 = new BitSet(new long[]{0x1000000000000000L});
	public static final BitSet FOLLOW_RPAREN_in_directAbstractDeclarator2938 = new BitSet(new long[]{0x0200000000000002L});
	public static final BitSet FOLLOW_OPENBRAC_in_directAbstractDeclarator2953 = new BitSet(new long[]{0x8040005050206610L,0x0000000000000255L});
	public static final BitSet FOLLOW_unaryExpression_in_directAbstractDeclarator2955 = new BitSet(new long[]{0x0000000000004000L});
	public static final BitSet FOLLOW_CLOSEBRAC_in_directAbstractDeclarator2958 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_POINTER_in_pointer2976 = new BitSet(new long[]{0x0000000000100002L});
	public static final BitSet FOLLOW_typeQualifierList_in_pointer2978 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_typeQualifier_in_typeQualifierList3001 = new BitSet(new long[]{0x0000000000100002L});
	public static final BitSet FOLLOW_IDENTIFIER_in_typedefName3017 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_declarationSpecifiers_in_typealiasTarget3034 = new BitSet(new long[]{0x0400804000000002L});
	public static final BitSet FOLLOW_abstractDeclaratorList_in_typealiasTarget3036 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_abstractDeclaratorList_in_typealiasAlias3062 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_declarationSpecifiers_in_typealiasAlias3068 = new BitSet(new long[]{0x0400804000000002L});
	public static final BitSet FOLLOW_abstractDeclaratorList_in_typealiasAlias3070 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_TYPEALIASTOK_in_typealiasDecl3084 = new BitSet(new long[]{0x40004AC18A180900L,0x000000000001C891L});
	public static final BitSet FOLLOW_typealiasTarget_in_typealiasDecl3086 = new BitSet(new long[]{0x0000000000000000L,0x0000000000001000L});
	public static final BitSet FOLLOW_TYPE_ASSIGNMENT_in_typealiasDecl3088 = new BitSet(new long[]{0x4400CAC18A180900L,0x000000000001C891L});
	public static final BitSet FOLLOW_typealiasAlias_in_typealiasDecl3090 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_ctfSpecifierHead_in_ctfSpecifier3190 = new BitSet(new long[]{0x0000100000000000L});
	public static final BitSet FOLLOW_ctfBody_in_ctfSpecifier3192 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_typealiasDecl_in_ctfSpecifier3209 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_EVENTTOK_in_ctfSpecifierHead3230 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_STREAMTOK_in_ctfSpecifierHead3240 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_TRACETOK_in_ctfSpecifierHead3250 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_ENVTOK_in_ctfSpecifierHead3260 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_CLOCKTOK_in_ctfSpecifierHead3270 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_CALLSITETOK_in_ctfSpecifierHead3280 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_FLOATINGPOINTTOK_in_ctfTypeSpecifier3303 = new BitSet(new long[]{0x0000100000000000L});
	public static final BitSet FOLLOW_ctfBody_in_ctfTypeSpecifier3305 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_INTEGERTOK_in_ctfTypeSpecifier3320 = new BitSet(new long[]{0x0000100000000000L});
	public static final BitSet FOLLOW_ctfBody_in_ctfTypeSpecifier3322 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_STRINGTOK_in_ctfTypeSpecifier3337 = new BitSet(new long[]{0x0000100000000002L});
	public static final BitSet FOLLOW_ctfBody_in_ctfTypeSpecifier3339 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_LCURL_in_ctfBody3372 = new BitSet(new long[]{0xC8404AD1DA382F10L,0x000000000001CED5L});
	public static final BitSet FOLLOW_ctfAssignmentExpressionList_in_ctfBody3374 = new BitSet(new long[]{0x0800000000000000L});
	public static final BitSet FOLLOW_RCURL_in_ctfBody3377 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_ctfAssignmentExpression_in_ctfAssignmentExpressionList3396 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000100L});
	public static final BitSet FOLLOW_TERM_in_ctfAssignmentExpressionList3398 = new BitSet(new long[]{0xC0404AD1DA382F12L,0x000000000001CED5L});
	public static final BitSet FOLLOW_unaryExpression_in_ctfAssignmentExpression3421 = new BitSet(new long[]{0x0000000000000040L,0x0000000000001000L});
	public static final BitSet FOLLOW_ASSIGNMENT_in_ctfAssignmentExpression3433 = new BitSet(new long[]{0x8040005050202610L,0x0000000000000255L});
	public static final BitSet FOLLOW_unaryExpression_in_ctfAssignmentExpression3437 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_TYPE_ASSIGNMENT_in_ctfAssignmentExpression3513 = new BitSet(new long[]{0x40004AC18A080900L,0x000000000001C091L});
	public static final BitSet FOLLOW_typeSpecifier_in_ctfAssignmentExpression3517 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_declarationSpecifiers_in_ctfAssignmentExpression3600 = new BitSet(new long[]{0x0400004000000000L});
	public static final BitSet FOLLOW_declaratorList_in_ctfAssignmentExpression3604 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_typealiasDecl_in_ctfAssignmentExpression3627 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_IDENTIFIER_in_synpred1_CTFParser560 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_ctfKeyword_in_synpred2_CTFParser586 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_STRING_LITERAL_in_synpred3_CTFParser606 = new BitSet(new long[]{0x0000000000000002L});
}
