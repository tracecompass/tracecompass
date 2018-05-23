// $ANTLR 3.5.2 org/eclipse/tracecompass/tmf/filter/parser/FilterParser.g 2018-05-24 14:09:42

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


import org.antlr.runtime.*;
import java.util.Stack;
import java.util.List;
import java.util.ArrayList;

import org.antlr.runtime.tree.*;


@SuppressWarnings("all")
public class FilterParserParser extends Parser {
	public static final String[] tokenNames = new String[] {
		"<invalid>", "<EOR>", "<DOWN>", "<UP>", "CONSTANT", "EXP_NEG", "EXP_NODE", 
		"EXP_PAR", "OP", "OPERATION", "OPERATION1", "OPERATION2", "OPERATION3", 
		"OP_NEGATE", "OP_PRESENT", "ROOT", "SEPARATOR", "TEXT", "WS", "'\"'", 
		"'('", "')'"
	};
	public static final int EOF=-1;
	public static final int T__19=19;
	public static final int T__20=20;
	public static final int T__21=21;
	public static final int CONSTANT=4;
	public static final int EXP_NEG=5;
	public static final int EXP_NODE=6;
	public static final int EXP_PAR=7;
	public static final int OP=8;
	public static final int OPERATION=9;
	public static final int OPERATION1=10;
	public static final int OPERATION2=11;
	public static final int OPERATION3=12;
	public static final int OP_NEGATE=13;
	public static final int OP_PRESENT=14;
	public static final int ROOT=15;
	public static final int SEPARATOR=16;
	public static final int TEXT=17;
	public static final int WS=18;

	// delegates
	public Parser[] getDelegates() {
		return new Parser[] {};
	}

	// delegators


	public FilterParserParser(TokenStream input) {
		this(input, new RecognizerSharedState());
	}
	public FilterParserParser(TokenStream input, RecognizerSharedState state) {
		super(input, state);
	}

	protected TreeAdaptor adaptor = new CommonTreeAdaptor();

	public void setTreeAdaptor(TreeAdaptor adaptor) {
		this.adaptor = adaptor;
	}
	public TreeAdaptor getTreeAdaptor() {
		return adaptor;
	}
	@Override public String[] getTokenNames() { return FilterParserParser.tokenNames; }
	@Override public String getGrammarFileName() { return "org/eclipse/tracecompass/tmf/filter/parser/FilterParser.g"; }


	private IErrorListener errListener;

	public void setErrorListener(IErrorListener listener) {
	    errListener = listener;
	}

	@Override
	public void reportError(RecognitionException e) {
	    super.reportError(e);
	    errListener.error(e);
	}


	public static class parse_return extends ParserRuleReturnScope {
		CommonTree tree;
		@Override
		public CommonTree getTree() { return tree; }
	};


	// $ANTLR start "parse"
	// org/eclipse/tracecompass/tmf/filter/parser/FilterParser.g:84:1: parse : ( expression )+ -> ^( ROOT ( expression )+ ) ;
	public final FilterParserParser.parse_return parse() throws RecognitionException {
		FilterParserParser.parse_return retval = new FilterParserParser.parse_return();
		retval.start = input.LT(1);

		CommonTree root_0 = null;

		ParserRuleReturnScope expression1 =null;

		RewriteRuleSubtreeStream stream_expression=new RewriteRuleSubtreeStream(adaptor,"rule expression");

		try {
			// org/eclipse/tracecompass/tmf/filter/parser/FilterParser.g:84:7: ( ( expression )+ -> ^( ROOT ( expression )+ ) )
			// org/eclipse/tracecompass/tmf/filter/parser/FilterParser.g:84:9: ( expression )+
			{
			// org/eclipse/tracecompass/tmf/filter/parser/FilterParser.g:84:9: ( expression )+
			int cnt1=0;
			loop1:
			while (true) {
				int alt1=2;
				int LA1_0 = input.LA(1);
				if ( (LA1_0==OP_NEGATE||LA1_0==TEXT||(LA1_0 >= 19 && LA1_0 <= 20)) ) {
					alt1=1;
				}

				switch (alt1) {
				case 1 :
					// org/eclipse/tracecompass/tmf/filter/parser/FilterParser.g:84:10: expression
					{
					pushFollow(FOLLOW_expression_in_parse134);
					expression1=expression();
					state._fsp--;

					stream_expression.add(expression1.getTree());
					}
					break;

				default :
					if ( cnt1 >= 1 ) break loop1;
					EarlyExitException eee = new EarlyExitException(1, input);
					throw eee;
				}
				cnt1++;
			}

			// AST REWRITE
			// elements: expression
			// token labels: 
			// rule labels: retval
			// token list labels: 
			// rule list labels: 
			// wildcard labels: 
			retval.tree = root_0;
			RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.getTree():null);

			root_0 = (CommonTree)adaptor.nil();
			// 84:23: -> ^( ROOT ( expression )+ )
			{
				// org/eclipse/tracecompass/tmf/filter/parser/FilterParser.g:84:26: ^( ROOT ( expression )+ )
				{
				CommonTree root_1 = (CommonTree)adaptor.nil();
				root_1 = (CommonTree)adaptor.becomeRoot((CommonTree)adaptor.create(ROOT, "ROOT"), root_1);
				if ( !(stream_expression.hasNext()) ) {
					throw new RewriteEarlyExitException();
				}
				while ( stream_expression.hasNext() ) {
					adaptor.addChild(root_1, stream_expression.nextTree());
				}
				stream_expression.reset();

				adaptor.addChild(root_0, root_1);
				}

			}


			retval.tree = root_0;

			}

			retval.stop = input.LT(-1);

			retval.tree = (CommonTree)adaptor.rulePostProcessing(root_0);
			adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);

		}
		catch (RecognitionException re) {
			reportError(re);
			recover(input,re);
			retval.tree = (CommonTree)adaptor.errorNode(input, retval.start, input.LT(-1), re);
		}
		finally {
			// do for sure before leaving
		}
		return retval;
	}
	// $ANTLR end "parse"


	public static class expression_return extends ParserRuleReturnScope {
		CommonTree tree;
		@Override
		public CommonTree getTree() { return tree; }
	};


	// $ANTLR start "expression"
	// org/eclipse/tracecompass/tmf/filter/parser/FilterParser.g:85:1: expression : (left= expr (sep= SEPARATOR right= expr )? -> ^( EXP_NODE $left ( $sep $right)? ) | ( OP_NEGATE ) ( expression ) -> ^( EXP_NEG OP_NEGATE expression ) );
	public final FilterParserParser.expression_return expression() throws RecognitionException {
		FilterParserParser.expression_return retval = new FilterParserParser.expression_return();
		retval.start = input.LT(1);

		CommonTree root_0 = null;

		Token sep=null;
		Token OP_NEGATE2=null;
		ParserRuleReturnScope left =null;
		ParserRuleReturnScope right =null;
		ParserRuleReturnScope expression3 =null;

		CommonTree sep_tree=null;
		CommonTree OP_NEGATE2_tree=null;
		RewriteRuleTokenStream stream_OP_NEGATE=new RewriteRuleTokenStream(adaptor,"token OP_NEGATE");
		RewriteRuleTokenStream stream_SEPARATOR=new RewriteRuleTokenStream(adaptor,"token SEPARATOR");
		RewriteRuleSubtreeStream stream_expression=new RewriteRuleSubtreeStream(adaptor,"rule expression");
		RewriteRuleSubtreeStream stream_expr=new RewriteRuleSubtreeStream(adaptor,"rule expr");

		try {
			// org/eclipse/tracecompass/tmf/filter/parser/FilterParser.g:85:12: (left= expr (sep= SEPARATOR right= expr )? -> ^( EXP_NODE $left ( $sep $right)? ) | ( OP_NEGATE ) ( expression ) -> ^( EXP_NEG OP_NEGATE expression ) )
			int alt3=2;
			int LA3_0 = input.LA(1);
			if ( (LA3_0==TEXT||(LA3_0 >= 19 && LA3_0 <= 20)) ) {
				alt3=1;
			}
			else if ( (LA3_0==OP_NEGATE) ) {
				alt3=2;
			}

			else {
				NoViableAltException nvae =
					new NoViableAltException("", 3, 0, input);
				throw nvae;
			}

			switch (alt3) {
				case 1 :
					// org/eclipse/tracecompass/tmf/filter/parser/FilterParser.g:85:14: left= expr (sep= SEPARATOR right= expr )?
					{
					pushFollow(FOLLOW_expr_in_expression158);
					left=expr();
					state._fsp--;

					stream_expr.add(left.getTree());
					// org/eclipse/tracecompass/tmf/filter/parser/FilterParser.g:85:26: (sep= SEPARATOR right= expr )?
					int alt2=2;
					int LA2_0 = input.LA(1);
					if ( (LA2_0==SEPARATOR) ) {
						alt2=1;
					}
					switch (alt2) {
						case 1 :
							// org/eclipse/tracecompass/tmf/filter/parser/FilterParser.g:85:27: sep= SEPARATOR right= expr
							{
							sep=(Token)match(input,SEPARATOR,FOLLOW_SEPARATOR_in_expression165);  
							stream_SEPARATOR.add(sep);

							pushFollow(FOLLOW_expr_in_expression171);
							right=expr();
							state._fsp--;

							stream_expr.add(right.getTree());
							}
							break;

					}

					// AST REWRITE
					// elements: right, sep, left
					// token labels: sep
					// rule labels: left, right, retval
					// token list labels: 
					// rule list labels: 
					// wildcard labels: 
					retval.tree = root_0;
					RewriteRuleTokenStream stream_sep=new RewriteRuleTokenStream(adaptor,"token sep",sep);
					RewriteRuleSubtreeStream stream_left=new RewriteRuleSubtreeStream(adaptor,"rule left",left!=null?left.getTree():null);
					RewriteRuleSubtreeStream stream_right=new RewriteRuleSubtreeStream(adaptor,"rule right",right!=null?right.getTree():null);
					RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.getTree():null);

					root_0 = (CommonTree)adaptor.nil();
					// 85:58: -> ^( EXP_NODE $left ( $sep $right)? )
					{
						// org/eclipse/tracecompass/tmf/filter/parser/FilterParser.g:85:61: ^( EXP_NODE $left ( $sep $right)? )
						{
						CommonTree root_1 = (CommonTree)adaptor.nil();
						root_1 = (CommonTree)adaptor.becomeRoot((CommonTree)adaptor.create(EXP_NODE, "EXP_NODE"), root_1);
						adaptor.addChild(root_1, stream_left.nextTree());
						// org/eclipse/tracecompass/tmf/filter/parser/FilterParser.g:85:78: ( $sep $right)?
						if ( stream_right.hasNext()||stream_sep.hasNext() ) {
							adaptor.addChild(root_1, stream_sep.nextNode());
							adaptor.addChild(root_1, stream_right.nextTree());
						}
						stream_right.reset();
						stream_sep.reset();

						adaptor.addChild(root_0, root_1);
						}

					}


					retval.tree = root_0;

					}
					break;
				case 2 :
					// org/eclipse/tracecompass/tmf/filter/parser/FilterParser.g:86:14: ( OP_NEGATE ) ( expression )
					{
					// org/eclipse/tracecompass/tmf/filter/parser/FilterParser.g:86:14: ( OP_NEGATE )
					// org/eclipse/tracecompass/tmf/filter/parser/FilterParser.g:86:15: OP_NEGATE
					{
					OP_NEGATE2=(Token)match(input,OP_NEGATE,FOLLOW_OP_NEGATE_in_expression207);  
					stream_OP_NEGATE.add(OP_NEGATE2);

					}

					// org/eclipse/tracecompass/tmf/filter/parser/FilterParser.g:86:25: ( expression )
					// org/eclipse/tracecompass/tmf/filter/parser/FilterParser.g:86:26: expression
					{
					pushFollow(FOLLOW_expression_in_expression210);
					expression3=expression();
					state._fsp--;

					stream_expression.add(expression3.getTree());
					}

					// AST REWRITE
					// elements: OP_NEGATE, expression
					// token labels: 
					// rule labels: retval
					// token list labels: 
					// rule list labels: 
					// wildcard labels: 
					retval.tree = root_0;
					RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.getTree():null);

					root_0 = (CommonTree)adaptor.nil();
					// 86:38: -> ^( EXP_NEG OP_NEGATE expression )
					{
						// org/eclipse/tracecompass/tmf/filter/parser/FilterParser.g:86:41: ^( EXP_NEG OP_NEGATE expression )
						{
						CommonTree root_1 = (CommonTree)adaptor.nil();
						root_1 = (CommonTree)adaptor.becomeRoot((CommonTree)adaptor.create(EXP_NEG, "EXP_NEG"), root_1);
						adaptor.addChild(root_1, stream_OP_NEGATE.nextNode());
						adaptor.addChild(root_1, stream_expression.nextTree());
						adaptor.addChild(root_0, root_1);
						}

					}


					retval.tree = root_0;

					}
					break;

			}
			retval.stop = input.LT(-1);

			retval.tree = (CommonTree)adaptor.rulePostProcessing(root_0);
			adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);

		}
		catch (RecognitionException re) {
			reportError(re);
			recover(input,re);
			retval.tree = (CommonTree)adaptor.errorNode(input, retval.start, input.LT(-1), re);
		}
		finally {
			// do for sure before leaving
		}
		return retval;
	}
	// $ANTLR end "expression"


	public static class paragraph_return extends ParserRuleReturnScope {
		CommonTree tree;
		@Override
		public CommonTree getTree() { return tree; }
	};


	// $ANTLR start "paragraph"
	// org/eclipse/tracecompass/tmf/filter/parser/FilterParser.g:90:1: paragraph : ( TEXT )+ ;
	public final FilterParserParser.paragraph_return paragraph() throws RecognitionException {
		FilterParserParser.paragraph_return retval = new FilterParserParser.paragraph_return();
		retval.start = input.LT(1);

		CommonTree root_0 = null;

		Token TEXT4=null;

		CommonTree TEXT4_tree=null;

		try {
			// org/eclipse/tracecompass/tmf/filter/parser/FilterParser.g:90:11: ( ( TEXT )+ )
			// org/eclipse/tracecompass/tmf/filter/parser/FilterParser.g:90:13: ( TEXT )+
			{
			root_0 = (CommonTree)adaptor.nil();


			// org/eclipse/tracecompass/tmf/filter/parser/FilterParser.g:90:13: ( TEXT )+
			int cnt4=0;
			loop4:
			while (true) {
				int alt4=2;
				int LA4_0 = input.LA(1);
				if ( (LA4_0==TEXT) ) {
					alt4=1;
				}

				switch (alt4) {
				case 1 :
					// org/eclipse/tracecompass/tmf/filter/parser/FilterParser.g:90:13: TEXT
					{
					TEXT4=(Token)match(input,TEXT,FOLLOW_TEXT_in_paragraph241); 
					TEXT4_tree = (CommonTree)adaptor.create(TEXT4);
					adaptor.addChild(root_0, TEXT4_tree);

					}
					break;

				default :
					if ( cnt4 >= 1 ) break loop4;
					EarlyExitException eee = new EarlyExitException(4, input);
					throw eee;
				}
				cnt4++;
			}

			}

			retval.stop = input.LT(-1);

			retval.tree = (CommonTree)adaptor.rulePostProcessing(root_0);
			adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);

		}
		catch (RecognitionException re) {
			reportError(re);
			recover(input,re);
			retval.tree = (CommonTree)adaptor.errorNode(input, retval.start, input.LT(-1), re);
		}
		finally {
			// do for sure before leaving
		}
		return retval;
	}
	// $ANTLR end "paragraph"


	public static class expr_return extends ParserRuleReturnScope {
		CommonTree tree;
		@Override
		public CommonTree getTree() { return tree; }
	};


	// $ANTLR start "expr"
	// org/eclipse/tracecompass/tmf/filter/parser/FilterParser.g:92:1: expr : ( TEXT OP TEXT -> ^( OPERATION TEXT OP TEXT ) | TEXT OP_PRESENT -> ^( OPERATION1 TEXT OP_PRESENT ) | '\"' key0= paragraph '\"' op= OP text= TEXT -> ^( OPERATION2 $key0 $op $text) | '\"' key1= paragraph '\"' op_present= OP_PRESENT -> ^( OPERATION3 $key1 $op_present) | '(' expr ')' -> ^( EXP_PAR expr ) | TEXT -> ^( CONSTANT TEXT ) );
	public final FilterParserParser.expr_return expr() throws RecognitionException {
		FilterParserParser.expr_return retval = new FilterParserParser.expr_return();
		retval.start = input.LT(1);

		CommonTree root_0 = null;

		Token op=null;
		Token text=null;
		Token op_present=null;
		Token TEXT5=null;
		Token OP6=null;
		Token TEXT7=null;
		Token TEXT8=null;
		Token OP_PRESENT9=null;
		Token char_literal10=null;
		Token char_literal11=null;
		Token char_literal12=null;
		Token char_literal13=null;
		Token char_literal14=null;
		Token char_literal16=null;
		Token TEXT17=null;
		ParserRuleReturnScope key0 =null;
		ParserRuleReturnScope key1 =null;
		ParserRuleReturnScope expr15 =null;

		CommonTree op_tree=null;
		CommonTree text_tree=null;
		CommonTree op_present_tree=null;
		CommonTree TEXT5_tree=null;
		CommonTree OP6_tree=null;
		CommonTree TEXT7_tree=null;
		CommonTree TEXT8_tree=null;
		CommonTree OP_PRESENT9_tree=null;
		CommonTree char_literal10_tree=null;
		CommonTree char_literal11_tree=null;
		CommonTree char_literal12_tree=null;
		CommonTree char_literal13_tree=null;
		CommonTree char_literal14_tree=null;
		CommonTree char_literal16_tree=null;
		CommonTree TEXT17_tree=null;
		RewriteRuleTokenStream stream_OP=new RewriteRuleTokenStream(adaptor,"token OP");
		RewriteRuleTokenStream stream_OP_PRESENT=new RewriteRuleTokenStream(adaptor,"token OP_PRESENT");
		RewriteRuleTokenStream stream_19=new RewriteRuleTokenStream(adaptor,"token 19");
		RewriteRuleTokenStream stream_TEXT=new RewriteRuleTokenStream(adaptor,"token TEXT");
		RewriteRuleTokenStream stream_20=new RewriteRuleTokenStream(adaptor,"token 20");
		RewriteRuleTokenStream stream_21=new RewriteRuleTokenStream(adaptor,"token 21");
		RewriteRuleSubtreeStream stream_paragraph=new RewriteRuleSubtreeStream(adaptor,"rule paragraph");
		RewriteRuleSubtreeStream stream_expr=new RewriteRuleSubtreeStream(adaptor,"rule expr");

		try {
			// org/eclipse/tracecompass/tmf/filter/parser/FilterParser.g:92:12: ( TEXT OP TEXT -> ^( OPERATION TEXT OP TEXT ) | TEXT OP_PRESENT -> ^( OPERATION1 TEXT OP_PRESENT ) | '\"' key0= paragraph '\"' op= OP text= TEXT -> ^( OPERATION2 $key0 $op $text) | '\"' key1= paragraph '\"' op_present= OP_PRESENT -> ^( OPERATION3 $key1 $op_present) | '(' expr ')' -> ^( EXP_PAR expr ) | TEXT -> ^( CONSTANT TEXT ) )
			int alt5=6;
			alt5 = dfa5.predict(input);
			switch (alt5) {
				case 1 :
					// org/eclipse/tracecompass/tmf/filter/parser/FilterParser.g:92:14: TEXT OP TEXT
					{
					TEXT5=(Token)match(input,TEXT,FOLLOW_TEXT_in_expr256);  
					stream_TEXT.add(TEXT5);

					OP6=(Token)match(input,OP,FOLLOW_OP_in_expr258);  
					stream_OP.add(OP6);

					TEXT7=(Token)match(input,TEXT,FOLLOW_TEXT_in_expr260);  
					stream_TEXT.add(TEXT7);

					// AST REWRITE
					// elements: OP, TEXT, TEXT
					// token labels: 
					// rule labels: retval
					// token list labels: 
					// rule list labels: 
					// wildcard labels: 
					retval.tree = root_0;
					RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.getTree():null);

					root_0 = (CommonTree)adaptor.nil();
					// 92:27: -> ^( OPERATION TEXT OP TEXT )
					{
						// org/eclipse/tracecompass/tmf/filter/parser/FilterParser.g:92:30: ^( OPERATION TEXT OP TEXT )
						{
						CommonTree root_1 = (CommonTree)adaptor.nil();
						root_1 = (CommonTree)adaptor.becomeRoot((CommonTree)adaptor.create(OPERATION, "OPERATION"), root_1);
						adaptor.addChild(root_1, stream_TEXT.nextNode());
						adaptor.addChild(root_1, stream_OP.nextNode());
						adaptor.addChild(root_1, stream_TEXT.nextNode());
						adaptor.addChild(root_0, root_1);
						}

					}


					retval.tree = root_0;

					}
					break;
				case 2 :
					// org/eclipse/tracecompass/tmf/filter/parser/FilterParser.g:93:14: TEXT OP_PRESENT
					{
					TEXT8=(Token)match(input,TEXT,FOLLOW_TEXT_in_expr287);  
					stream_TEXT.add(TEXT8);

					OP_PRESENT9=(Token)match(input,OP_PRESENT,FOLLOW_OP_PRESENT_in_expr289);  
					stream_OP_PRESENT.add(OP_PRESENT9);

					// AST REWRITE
					// elements: OP_PRESENT, TEXT
					// token labels: 
					// rule labels: retval
					// token list labels: 
					// rule list labels: 
					// wildcard labels: 
					retval.tree = root_0;
					RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.getTree():null);

					root_0 = (CommonTree)adaptor.nil();
					// 93:30: -> ^( OPERATION1 TEXT OP_PRESENT )
					{
						// org/eclipse/tracecompass/tmf/filter/parser/FilterParser.g:93:33: ^( OPERATION1 TEXT OP_PRESENT )
						{
						CommonTree root_1 = (CommonTree)adaptor.nil();
						root_1 = (CommonTree)adaptor.becomeRoot((CommonTree)adaptor.create(OPERATION1, "OPERATION1"), root_1);
						adaptor.addChild(root_1, stream_TEXT.nextNode());
						adaptor.addChild(root_1, stream_OP_PRESENT.nextNode());
						adaptor.addChild(root_0, root_1);
						}

					}


					retval.tree = root_0;

					}
					break;
				case 3 :
					// org/eclipse/tracecompass/tmf/filter/parser/FilterParser.g:94:14: '\"' key0= paragraph '\"' op= OP text= TEXT
					{
					char_literal10=(Token)match(input,19,FOLLOW_19_in_expr314);  
					stream_19.add(char_literal10);

					pushFollow(FOLLOW_paragraph_in_expr320);
					key0=paragraph();
					state._fsp--;

					stream_paragraph.add(key0.getTree());
					char_literal11=(Token)match(input,19,FOLLOW_19_in_expr322);  
					stream_19.add(char_literal11);

					op=(Token)match(input,OP,FOLLOW_OP_in_expr328);  
					stream_OP.add(op);

					text=(Token)match(input,TEXT,FOLLOW_TEXT_in_expr334);  
					stream_TEXT.add(text);

					// AST REWRITE
					// elements: key0, op, text
					// token labels: op, text
					// rule labels: key0, retval
					// token list labels: 
					// rule list labels: 
					// wildcard labels: 
					retval.tree = root_0;
					RewriteRuleTokenStream stream_op=new RewriteRuleTokenStream(adaptor,"token op",op);
					RewriteRuleTokenStream stream_text=new RewriteRuleTokenStream(adaptor,"token text",text);
					RewriteRuleSubtreeStream stream_key0=new RewriteRuleSubtreeStream(adaptor,"rule key0",key0!=null?key0.getTree():null);
					RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.getTree():null);

					root_0 = (CommonTree)adaptor.nil();
					// 94:59: -> ^( OPERATION2 $key0 $op $text)
					{
						// org/eclipse/tracecompass/tmf/filter/parser/FilterParser.g:94:62: ^( OPERATION2 $key0 $op $text)
						{
						CommonTree root_1 = (CommonTree)adaptor.nil();
						root_1 = (CommonTree)adaptor.becomeRoot((CommonTree)adaptor.create(OPERATION2, "OPERATION2"), root_1);
						adaptor.addChild(root_1, stream_key0.nextTree());
						adaptor.addChild(root_1, stream_op.nextNode());
						adaptor.addChild(root_1, stream_text.nextNode());
						adaptor.addChild(root_0, root_1);
						}

					}


					retval.tree = root_0;

					}
					break;
				case 4 :
					// org/eclipse/tracecompass/tmf/filter/parser/FilterParser.g:95:14: '\"' key1= paragraph '\"' op_present= OP_PRESENT
					{
					char_literal12=(Token)match(input,19,FOLLOW_19_in_expr364);  
					stream_19.add(char_literal12);

					pushFollow(FOLLOW_paragraph_in_expr370);
					key1=paragraph();
					state._fsp--;

					stream_paragraph.add(key1.getTree());
					char_literal13=(Token)match(input,19,FOLLOW_19_in_expr372);  
					stream_19.add(char_literal13);

					op_present=(Token)match(input,OP_PRESENT,FOLLOW_OP_PRESENT_in_expr378);  
					stream_OP_PRESENT.add(op_present);

					// AST REWRITE
					// elements: key1, op_present
					// token labels: op_present
					// rule labels: key1, retval
					// token list labels: 
					// rule list labels: 
					// wildcard labels: 
					retval.tree = root_0;
					RewriteRuleTokenStream stream_op_present=new RewriteRuleTokenStream(adaptor,"token op_present",op_present);
					RewriteRuleSubtreeStream stream_key1=new RewriteRuleSubtreeStream(adaptor,"rule key1",key1!=null?key1.getTree():null);
					RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.getTree():null);

					root_0 = (CommonTree)adaptor.nil();
					// 95:63: -> ^( OPERATION3 $key1 $op_present)
					{
						// org/eclipse/tracecompass/tmf/filter/parser/FilterParser.g:95:66: ^( OPERATION3 $key1 $op_present)
						{
						CommonTree root_1 = (CommonTree)adaptor.nil();
						root_1 = (CommonTree)adaptor.becomeRoot((CommonTree)adaptor.create(OPERATION3, "OPERATION3"), root_1);
						adaptor.addChild(root_1, stream_key1.nextTree());
						adaptor.addChild(root_1, stream_op_present.nextNode());
						adaptor.addChild(root_0, root_1);
						}

					}


					retval.tree = root_0;

					}
					break;
				case 5 :
					// org/eclipse/tracecompass/tmf/filter/parser/FilterParser.g:96:14: '(' expr ')'
					{
					char_literal14=(Token)match(input,20,FOLLOW_20_in_expr405);  
					stream_20.add(char_literal14);

					pushFollow(FOLLOW_expr_in_expr407);
					expr15=expr();
					state._fsp--;

					stream_expr.add(expr15.getTree());
					char_literal16=(Token)match(input,21,FOLLOW_21_in_expr409);  
					stream_21.add(char_literal16);

					// AST REWRITE
					// elements: expr
					// token labels: 
					// rule labels: retval
					// token list labels: 
					// rule list labels: 
					// wildcard labels: 
					retval.tree = root_0;
					RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.getTree():null);

					root_0 = (CommonTree)adaptor.nil();
					// 96:27: -> ^( EXP_PAR expr )
					{
						// org/eclipse/tracecompass/tmf/filter/parser/FilterParser.g:96:30: ^( EXP_PAR expr )
						{
						CommonTree root_1 = (CommonTree)adaptor.nil();
						root_1 = (CommonTree)adaptor.becomeRoot((CommonTree)adaptor.create(EXP_PAR, "EXP_PAR"), root_1);
						adaptor.addChild(root_1, stream_expr.nextTree());
						adaptor.addChild(root_0, root_1);
						}

					}


					retval.tree = root_0;

					}
					break;
				case 6 :
					// org/eclipse/tracecompass/tmf/filter/parser/FilterParser.g:97:14: TEXT
					{
					TEXT17=(Token)match(input,TEXT,FOLLOW_TEXT_in_expr432);  
					stream_TEXT.add(TEXT17);

					// AST REWRITE
					// elements: TEXT
					// token labels: 
					// rule labels: retval
					// token list labels: 
					// rule list labels: 
					// wildcard labels: 
					retval.tree = root_0;
					RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.getTree():null);

					root_0 = (CommonTree)adaptor.nil();
					// 97:20: -> ^( CONSTANT TEXT )
					{
						// org/eclipse/tracecompass/tmf/filter/parser/FilterParser.g:97:23: ^( CONSTANT TEXT )
						{
						CommonTree root_1 = (CommonTree)adaptor.nil();
						root_1 = (CommonTree)adaptor.becomeRoot((CommonTree)adaptor.create(CONSTANT, "CONSTANT"), root_1);
						adaptor.addChild(root_1, stream_TEXT.nextNode());
						adaptor.addChild(root_0, root_1);
						}

					}


					retval.tree = root_0;

					}
					break;

			}
			retval.stop = input.LT(-1);

			retval.tree = (CommonTree)adaptor.rulePostProcessing(root_0);
			adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);

		}
		catch (RecognitionException re) {
			reportError(re);
			recover(input,re);
			retval.tree = (CommonTree)adaptor.errorNode(input, retval.start, input.LT(-1), re);
		}
		finally {
			// do for sure before leaving
		}
		return retval;
	}
	// $ANTLR end "expr"

	// Delegated rules


	protected DFA5 dfa5 = new DFA5(this);
	static final String DFA5_eotS =
		"\13\uffff";
	static final String DFA5_eofS =
		"\1\uffff\1\6\11\uffff";
	static final String DFA5_minS =
		"\1\21\1\10\1\21\4\uffff\1\21\1\10\2\uffff";
	static final String DFA5_maxS =
		"\1\24\1\25\1\21\4\uffff\1\23\1\16\2\uffff";
	static final String DFA5_acceptS =
		"\3\uffff\1\5\1\1\1\2\1\6\2\uffff\1\3\1\4";
	static final String DFA5_specialS =
		"\13\uffff}>";
	static final String[] DFA5_transitionS = {
			"\1\1\1\uffff\1\2\1\3",
			"\1\4\4\uffff\1\6\1\5\1\uffff\2\6\1\uffff\3\6",
			"\1\7",
			"",
			"",
			"",
			"",
			"\1\7\1\uffff\1\10",
			"\1\11\5\uffff\1\12",
			"",
			""
	};

	static final short[] DFA5_eot = DFA.unpackEncodedString(DFA5_eotS);
	static final short[] DFA5_eof = DFA.unpackEncodedString(DFA5_eofS);
	static final char[] DFA5_min = DFA.unpackEncodedStringToUnsignedChars(DFA5_minS);
	static final char[] DFA5_max = DFA.unpackEncodedStringToUnsignedChars(DFA5_maxS);
	static final short[] DFA5_accept = DFA.unpackEncodedString(DFA5_acceptS);
	static final short[] DFA5_special = DFA.unpackEncodedString(DFA5_specialS);
	static final short[][] DFA5_transition;

	static {
		int numStates = DFA5_transitionS.length;
		DFA5_transition = new short[numStates][];
		for (int i=0; i<numStates; i++) {
			DFA5_transition[i] = DFA.unpackEncodedString(DFA5_transitionS[i]);
		}
	}

	protected class DFA5 extends DFA {

		public DFA5(BaseRecognizer recognizer) {
			this.recognizer = recognizer;
			this.decisionNumber = 5;
			this.eot = DFA5_eot;
			this.eof = DFA5_eof;
			this.min = DFA5_min;
			this.max = DFA5_max;
			this.accept = DFA5_accept;
			this.special = DFA5_special;
			this.transition = DFA5_transition;
		}
		@Override
		public String getDescription() {
			return "92:1: expr : ( TEXT OP TEXT -> ^( OPERATION TEXT OP TEXT ) | TEXT OP_PRESENT -> ^( OPERATION1 TEXT OP_PRESENT ) | '\"' key0= paragraph '\"' op= OP text= TEXT -> ^( OPERATION2 $key0 $op $text) | '\"' key1= paragraph '\"' op_present= OP_PRESENT -> ^( OPERATION3 $key1 $op_present) | '(' expr ')' -> ^( EXP_PAR expr ) | TEXT -> ^( CONSTANT TEXT ) );";
		}
	}

	public static final BitSet FOLLOW_expression_in_parse134 = new BitSet(new long[]{0x00000000001A2002L});
	public static final BitSet FOLLOW_expr_in_expression158 = new BitSet(new long[]{0x0000000000010002L});
	public static final BitSet FOLLOW_SEPARATOR_in_expression165 = new BitSet(new long[]{0x00000000001A0000L});
	public static final BitSet FOLLOW_expr_in_expression171 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_OP_NEGATE_in_expression207 = new BitSet(new long[]{0x00000000001A2000L});
	public static final BitSet FOLLOW_expression_in_expression210 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_TEXT_in_paragraph241 = new BitSet(new long[]{0x0000000000020002L});
	public static final BitSet FOLLOW_TEXT_in_expr256 = new BitSet(new long[]{0x0000000000000100L});
	public static final BitSet FOLLOW_OP_in_expr258 = new BitSet(new long[]{0x0000000000020000L});
	public static final BitSet FOLLOW_TEXT_in_expr260 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_TEXT_in_expr287 = new BitSet(new long[]{0x0000000000004000L});
	public static final BitSet FOLLOW_OP_PRESENT_in_expr289 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_19_in_expr314 = new BitSet(new long[]{0x0000000000020000L});
	public static final BitSet FOLLOW_paragraph_in_expr320 = new BitSet(new long[]{0x0000000000080000L});
	public static final BitSet FOLLOW_19_in_expr322 = new BitSet(new long[]{0x0000000000000100L});
	public static final BitSet FOLLOW_OP_in_expr328 = new BitSet(new long[]{0x0000000000020000L});
	public static final BitSet FOLLOW_TEXT_in_expr334 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_19_in_expr364 = new BitSet(new long[]{0x0000000000020000L});
	public static final BitSet FOLLOW_paragraph_in_expr370 = new BitSet(new long[]{0x0000000000080000L});
	public static final BitSet FOLLOW_19_in_expr372 = new BitSet(new long[]{0x0000000000004000L});
	public static final BitSet FOLLOW_OP_PRESENT_in_expr378 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_20_in_expr405 = new BitSet(new long[]{0x00000000001A0000L});
	public static final BitSet FOLLOW_expr_in_expr407 = new BitSet(new long[]{0x0000000000200000L});
	public static final BitSet FOLLOW_21_in_expr409 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_TEXT_in_expr432 = new BitSet(new long[]{0x0000000000000002L});
}
