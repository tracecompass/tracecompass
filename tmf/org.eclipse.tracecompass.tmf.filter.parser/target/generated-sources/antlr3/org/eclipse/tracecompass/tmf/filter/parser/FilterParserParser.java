// $ANTLR 3.5.2 org/eclipse/tracecompass/tmf/filter/parser/FilterParser.g 2018-05-23 13:02:29

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
		"<invalid>", "<EOR>", "<DOWN>", "<UP>", "CONSTANT", "EXP_NODE", "EXP_PAR", 
		"OP", "OPERATION", "OPERATION1", "OPERATION2", "OP_NEGATE", "OP_PRESENT", 
		"ROOT", "SEPARATOR", "TEXT", "WS", "'('", "')'"
	};
	public static final int EOF=-1;
	public static final int T__17=17;
	public static final int T__18=18;
	public static final int CONSTANT=4;
	public static final int EXP_NODE=5;
	public static final int EXP_PAR=6;
	public static final int OP=7;
	public static final int OPERATION=8;
	public static final int OPERATION1=9;
	public static final int OPERATION2=10;
	public static final int OP_NEGATE=11;
	public static final int OP_PRESENT=12;
	public static final int ROOT=13;
	public static final int SEPARATOR=14;
	public static final int TEXT=15;
	public static final int WS=16;

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
	// org/eclipse/tracecompass/tmf/filter/parser/FilterParser.g:82:1: parse : ( expression )+ -> ^( ROOT ( expression )+ ) ;
	public final FilterParserParser.parse_return parse() throws RecognitionException {
		FilterParserParser.parse_return retval = new FilterParserParser.parse_return();
		retval.start = input.LT(1);

		CommonTree root_0 = null;

		ParserRuleReturnScope expression1 =null;

		RewriteRuleSubtreeStream stream_expression=new RewriteRuleSubtreeStream(adaptor,"rule expression");

		try {
			// org/eclipse/tracecompass/tmf/filter/parser/FilterParser.g:82:7: ( ( expression )+ -> ^( ROOT ( expression )+ ) )
			// org/eclipse/tracecompass/tmf/filter/parser/FilterParser.g:82:9: ( expression )+
			{
			// org/eclipse/tracecompass/tmf/filter/parser/FilterParser.g:82:9: ( expression )+
			int cnt1=0;
			loop1:
			while (true) {
				int alt1=2;
				int LA1_0 = input.LA(1);
				if ( (LA1_0==OP_NEGATE||LA1_0==TEXT||LA1_0==17) ) {
					alt1=1;
				}

				switch (alt1) {
				case 1 :
					// org/eclipse/tracecompass/tmf/filter/parser/FilterParser.g:82:10: expression
					{
					pushFollow(FOLLOW_expression_in_parse124);
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
			// 82:23: -> ^( ROOT ( expression )+ )
			{
				// org/eclipse/tracecompass/tmf/filter/parser/FilterParser.g:82:26: ^( ROOT ( expression )+ )
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
	// org/eclipse/tracecompass/tmf/filter/parser/FilterParser.g:83:1: expression : (left= expr (sep= SEPARATOR right= expr )? -> ^( EXP_NODE $left ( $sep $right)? ) | '(' expression ')' -> ^( EXP_PAR expression ) | ( OP_NEGATE ) ( expression ) -> ^( OPERATION2 OP_NEGATE expression ) );
	public final FilterParserParser.expression_return expression() throws RecognitionException {
		FilterParserParser.expression_return retval = new FilterParserParser.expression_return();
		retval.start = input.LT(1);

		CommonTree root_0 = null;

		Token sep=null;
		Token char_literal2=null;
		Token char_literal4=null;
		Token OP_NEGATE5=null;
		ParserRuleReturnScope left =null;
		ParserRuleReturnScope right =null;
		ParserRuleReturnScope expression3 =null;
		ParserRuleReturnScope expression6 =null;

		CommonTree sep_tree=null;
		CommonTree char_literal2_tree=null;
		CommonTree char_literal4_tree=null;
		CommonTree OP_NEGATE5_tree=null;
		RewriteRuleTokenStream stream_OP_NEGATE=new RewriteRuleTokenStream(adaptor,"token OP_NEGATE");
		RewriteRuleTokenStream stream_SEPARATOR=new RewriteRuleTokenStream(adaptor,"token SEPARATOR");
		RewriteRuleTokenStream stream_17=new RewriteRuleTokenStream(adaptor,"token 17");
		RewriteRuleTokenStream stream_18=new RewriteRuleTokenStream(adaptor,"token 18");
		RewriteRuleSubtreeStream stream_expression=new RewriteRuleSubtreeStream(adaptor,"rule expression");
		RewriteRuleSubtreeStream stream_expr=new RewriteRuleSubtreeStream(adaptor,"rule expr");

		try {
			// org/eclipse/tracecompass/tmf/filter/parser/FilterParser.g:83:12: (left= expr (sep= SEPARATOR right= expr )? -> ^( EXP_NODE $left ( $sep $right)? ) | '(' expression ')' -> ^( EXP_PAR expression ) | ( OP_NEGATE ) ( expression ) -> ^( OPERATION2 OP_NEGATE expression ) )
			int alt3=3;
			switch ( input.LA(1) ) {
			case TEXT:
				{
				alt3=1;
				}
				break;
			case 17:
				{
				alt3=2;
				}
				break;
			case OP_NEGATE:
				{
				alt3=3;
				}
				break;
			default:
				NoViableAltException nvae =
					new NoViableAltException("", 3, 0, input);
				throw nvae;
			}
			switch (alt3) {
				case 1 :
					// org/eclipse/tracecompass/tmf/filter/parser/FilterParser.g:83:14: left= expr (sep= SEPARATOR right= expr )?
					{
					pushFollow(FOLLOW_expr_in_expression148);
					left=expr();
					state._fsp--;

					stream_expr.add(left.getTree());
					// org/eclipse/tracecompass/tmf/filter/parser/FilterParser.g:83:26: (sep= SEPARATOR right= expr )?
					int alt2=2;
					int LA2_0 = input.LA(1);
					if ( (LA2_0==SEPARATOR) ) {
						alt2=1;
					}
					switch (alt2) {
						case 1 :
							// org/eclipse/tracecompass/tmf/filter/parser/FilterParser.g:83:27: sep= SEPARATOR right= expr
							{
							sep=(Token)match(input,SEPARATOR,FOLLOW_SEPARATOR_in_expression155);  
							stream_SEPARATOR.add(sep);

							pushFollow(FOLLOW_expr_in_expression161);
							right=expr();
							state._fsp--;

							stream_expr.add(right.getTree());
							}
							break;

					}

					// AST REWRITE
					// elements: left, sep, right
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
					// 83:58: -> ^( EXP_NODE $left ( $sep $right)? )
					{
						// org/eclipse/tracecompass/tmf/filter/parser/FilterParser.g:83:61: ^( EXP_NODE $left ( $sep $right)? )
						{
						CommonTree root_1 = (CommonTree)adaptor.nil();
						root_1 = (CommonTree)adaptor.becomeRoot((CommonTree)adaptor.create(EXP_NODE, "EXP_NODE"), root_1);
						adaptor.addChild(root_1, stream_left.nextTree());
						// org/eclipse/tracecompass/tmf/filter/parser/FilterParser.g:83:78: ( $sep $right)?
						if ( stream_sep.hasNext()||stream_right.hasNext() ) {
							adaptor.addChild(root_1, stream_sep.nextNode());
							adaptor.addChild(root_1, stream_right.nextTree());
						}
						stream_sep.reset();
						stream_right.reset();

						adaptor.addChild(root_0, root_1);
						}

					}


					retval.tree = root_0;

					}
					break;
				case 2 :
					// org/eclipse/tracecompass/tmf/filter/parser/FilterParser.g:84:14: '(' expression ')'
					{
					char_literal2=(Token)match(input,17,FOLLOW_17_in_expression196);  
					stream_17.add(char_literal2);

					pushFollow(FOLLOW_expression_in_expression198);
					expression3=expression();
					state._fsp--;

					stream_expression.add(expression3.getTree());
					char_literal4=(Token)match(input,18,FOLLOW_18_in_expression200);  
					stream_18.add(char_literal4);

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
					// 84:33: -> ^( EXP_PAR expression )
					{
						// org/eclipse/tracecompass/tmf/filter/parser/FilterParser.g:84:36: ^( EXP_PAR expression )
						{
						CommonTree root_1 = (CommonTree)adaptor.nil();
						root_1 = (CommonTree)adaptor.becomeRoot((CommonTree)adaptor.create(EXP_PAR, "EXP_PAR"), root_1);
						adaptor.addChild(root_1, stream_expression.nextTree());
						adaptor.addChild(root_0, root_1);
						}

					}


					retval.tree = root_0;

					}
					break;
				case 3 :
					// org/eclipse/tracecompass/tmf/filter/parser/FilterParser.g:85:14: ( OP_NEGATE ) ( expression )
					{
					// org/eclipse/tracecompass/tmf/filter/parser/FilterParser.g:85:14: ( OP_NEGATE )
					// org/eclipse/tracecompass/tmf/filter/parser/FilterParser.g:85:15: OP_NEGATE
					{
					OP_NEGATE5=(Token)match(input,OP_NEGATE,FOLLOW_OP_NEGATE_in_expression224);  
					stream_OP_NEGATE.add(OP_NEGATE5);

					}

					// org/eclipse/tracecompass/tmf/filter/parser/FilterParser.g:85:25: ( expression )
					// org/eclipse/tracecompass/tmf/filter/parser/FilterParser.g:85:26: expression
					{
					pushFollow(FOLLOW_expression_in_expression227);
					expression6=expression();
					state._fsp--;

					stream_expression.add(expression6.getTree());
					}

					// AST REWRITE
					// elements: expression, OP_NEGATE
					// token labels: 
					// rule labels: retval
					// token list labels: 
					// rule list labels: 
					// wildcard labels: 
					retval.tree = root_0;
					RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.getTree():null);

					root_0 = (CommonTree)adaptor.nil();
					// 85:38: -> ^( OPERATION2 OP_NEGATE expression )
					{
						// org/eclipse/tracecompass/tmf/filter/parser/FilterParser.g:85:41: ^( OPERATION2 OP_NEGATE expression )
						{
						CommonTree root_1 = (CommonTree)adaptor.nil();
						root_1 = (CommonTree)adaptor.becomeRoot((CommonTree)adaptor.create(OPERATION2, "OPERATION2"), root_1);
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


	public static class expr_return extends ParserRuleReturnScope {
		CommonTree tree;
		@Override
		public CommonTree getTree() { return tree; }
	};


	// $ANTLR start "expr"
	// org/eclipse/tracecompass/tmf/filter/parser/FilterParser.g:89:1: expr : ( TEXT OP TEXT -> ^( OPERATION TEXT OP TEXT ) | TEXT OP_PRESENT -> ^( OPERATION1 TEXT OP_PRESENT ) | TEXT -> ^( CONSTANT TEXT ) );
	public final FilterParserParser.expr_return expr() throws RecognitionException {
		FilterParserParser.expr_return retval = new FilterParserParser.expr_return();
		retval.start = input.LT(1);

		CommonTree root_0 = null;

		Token TEXT7=null;
		Token OP8=null;
		Token TEXT9=null;
		Token TEXT10=null;
		Token OP_PRESENT11=null;
		Token TEXT12=null;

		CommonTree TEXT7_tree=null;
		CommonTree OP8_tree=null;
		CommonTree TEXT9_tree=null;
		CommonTree TEXT10_tree=null;
		CommonTree OP_PRESENT11_tree=null;
		CommonTree TEXT12_tree=null;
		RewriteRuleTokenStream stream_OP=new RewriteRuleTokenStream(adaptor,"token OP");
		RewriteRuleTokenStream stream_OP_PRESENT=new RewriteRuleTokenStream(adaptor,"token OP_PRESENT");
		RewriteRuleTokenStream stream_TEXT=new RewriteRuleTokenStream(adaptor,"token TEXT");

		try {
			// org/eclipse/tracecompass/tmf/filter/parser/FilterParser.g:89:12: ( TEXT OP TEXT -> ^( OPERATION TEXT OP TEXT ) | TEXT OP_PRESENT -> ^( OPERATION1 TEXT OP_PRESENT ) | TEXT -> ^( CONSTANT TEXT ) )
			int alt4=3;
			int LA4_0 = input.LA(1);
			if ( (LA4_0==TEXT) ) {
				switch ( input.LA(2) ) {
				case OP:
					{
					alt4=1;
					}
					break;
				case OP_PRESENT:
					{
					alt4=2;
					}
					break;
				case EOF:
				case OP_NEGATE:
				case SEPARATOR:
				case TEXT:
				case 17:
				case 18:
					{
					alt4=3;
					}
					break;
				default:
					int nvaeMark = input.mark();
					try {
						input.consume();
						NoViableAltException nvae =
							new NoViableAltException("", 4, 1, input);
						throw nvae;
					} finally {
						input.rewind(nvaeMark);
					}
				}
			}

			else {
				NoViableAltException nvae =
					new NoViableAltException("", 4, 0, input);
				throw nvae;
			}

			switch (alt4) {
				case 1 :
					// org/eclipse/tracecompass/tmf/filter/parser/FilterParser.g:89:14: TEXT OP TEXT
					{
					TEXT7=(Token)match(input,TEXT,FOLLOW_TEXT_in_expr264);  
					stream_TEXT.add(TEXT7);

					OP8=(Token)match(input,OP,FOLLOW_OP_in_expr266);  
					stream_OP.add(OP8);

					TEXT9=(Token)match(input,TEXT,FOLLOW_TEXT_in_expr268);  
					stream_TEXT.add(TEXT9);

					// AST REWRITE
					// elements: TEXT, TEXT, OP
					// token labels: 
					// rule labels: retval
					// token list labels: 
					// rule list labels: 
					// wildcard labels: 
					retval.tree = root_0;
					RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.getTree():null);

					root_0 = (CommonTree)adaptor.nil();
					// 89:27: -> ^( OPERATION TEXT OP TEXT )
					{
						// org/eclipse/tracecompass/tmf/filter/parser/FilterParser.g:89:30: ^( OPERATION TEXT OP TEXT )
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
					// org/eclipse/tracecompass/tmf/filter/parser/FilterParser.g:90:14: TEXT OP_PRESENT
					{
					TEXT10=(Token)match(input,TEXT,FOLLOW_TEXT_in_expr295);  
					stream_TEXT.add(TEXT10);

					OP_PRESENT11=(Token)match(input,OP_PRESENT,FOLLOW_OP_PRESENT_in_expr297);  
					stream_OP_PRESENT.add(OP_PRESENT11);

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
					// 90:30: -> ^( OPERATION1 TEXT OP_PRESENT )
					{
						// org/eclipse/tracecompass/tmf/filter/parser/FilterParser.g:90:33: ^( OPERATION1 TEXT OP_PRESENT )
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
					// org/eclipse/tracecompass/tmf/filter/parser/FilterParser.g:91:14: TEXT
					{
					TEXT12=(Token)match(input,TEXT,FOLLOW_TEXT_in_expr322);  
					stream_TEXT.add(TEXT12);

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
					// 91:20: -> ^( CONSTANT TEXT )
					{
						// org/eclipse/tracecompass/tmf/filter/parser/FilterParser.g:91:23: ^( CONSTANT TEXT )
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



	public static final BitSet FOLLOW_expression_in_parse124 = new BitSet(new long[]{0x0000000000028802L});
	public static final BitSet FOLLOW_expr_in_expression148 = new BitSet(new long[]{0x0000000000004002L});
	public static final BitSet FOLLOW_SEPARATOR_in_expression155 = new BitSet(new long[]{0x0000000000008000L});
	public static final BitSet FOLLOW_expr_in_expression161 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_17_in_expression196 = new BitSet(new long[]{0x0000000000028800L});
	public static final BitSet FOLLOW_expression_in_expression198 = new BitSet(new long[]{0x0000000000040000L});
	public static final BitSet FOLLOW_18_in_expression200 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_OP_NEGATE_in_expression224 = new BitSet(new long[]{0x0000000000028800L});
	public static final BitSet FOLLOW_expression_in_expression227 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_TEXT_in_expr264 = new BitSet(new long[]{0x0000000000000080L});
	public static final BitSet FOLLOW_OP_in_expr266 = new BitSet(new long[]{0x0000000000008000L});
	public static final BitSet FOLLOW_TEXT_in_expr268 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_TEXT_in_expr295 = new BitSet(new long[]{0x0000000000001000L});
	public static final BitSet FOLLOW_OP_PRESENT_in_expr297 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_TEXT_in_expr322 = new BitSet(new long[]{0x0000000000000002L});
}
