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

import org.antlr.runtime.tree.*;


@SuppressWarnings("all")
public class FilterParserParser extends Parser {
	public static final String[] tokenNames = new String[] {
		"<invalid>", "<EOR>", "<DOWN>", "<UP>", "CONSTANT", "LOGICAL", "OP", "OPERATION", 
		"OPERATION1", "OPERATION2", "OPERATION3", "OPERATION4", "OPERATION5", 
		"OP_NEGATE", "OP_PRESENT", "PAR_CONSTANT", "ROOT1", "ROOT2", "SEPARATOR", 
		"TEXT", "WS", "'\"'", "'('", "')'"
	};
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
	    errListener.error(e);
	}


	public static class parse_return extends ParserRuleReturnScope {
		CommonTree tree;
		@Override
		public CommonTree getTree() { return tree; }
	};


	// $ANTLR start "parse"
	// org/eclipse/tracecompass/tmf/filter/parser/FilterParser.g:84:1: parse : ( parseRoot )+ ;
	public final FilterParserParser.parse_return parse() throws RecognitionException {
		FilterParserParser.parse_return retval = new FilterParserParser.parse_return();
		retval.start = input.LT(1);

		CommonTree root_0 = null;

		ParserRuleReturnScope parseRoot1 =null;


		try {
			// org/eclipse/tracecompass/tmf/filter/parser/FilterParser.g:84:6: ( ( parseRoot )+ )
			// org/eclipse/tracecompass/tmf/filter/parser/FilterParser.g:84:8: ( parseRoot )+
			{
			root_0 = (CommonTree)adaptor.nil();


			// org/eclipse/tracecompass/tmf/filter/parser/FilterParser.g:84:8: ( parseRoot )+
			int cnt1=0;
			loop1:
			while (true) {
				int alt1=2;
				int LA1_0 = input.LA(1);
				if ( (LA1_0==OP_NEGATE||LA1_0==TEXT||(LA1_0 >= 21 && LA1_0 <= 22)) ) {
					alt1=1;
				}

				switch (alt1) {
				case 1 :
					// org/eclipse/tracecompass/tmf/filter/parser/FilterParser.g:84:9: parseRoot
					{
					pushFollow(FOLLOW_parseRoot_in_parse143);
					parseRoot1=parseRoot();
					state._fsp--;

					adaptor.addChild(root_0, parseRoot1.getTree());

					}
					break;

				default :
					if ( cnt1 >= 1 ) break loop1;
					EarlyExitException eee = new EarlyExitException(1, input);
					throw eee;
				}
				cnt1++;
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
	// $ANTLR end "parse"


	public static class parseRoot_return extends ParserRuleReturnScope {
		CommonTree tree;
		@Override
		public CommonTree getTree() { return tree; }
	};


	// $ANTLR start "parseRoot"
	// org/eclipse/tracecompass/tmf/filter/parser/FilterParser.g:86:1: parseRoot : root ( SEPARATOR root )* -> ^( LOGICAL root ( SEPARATOR root )* ) ;
	public final FilterParserParser.parseRoot_return parseRoot() throws RecognitionException {
		FilterParserParser.parseRoot_return retval = new FilterParserParser.parseRoot_return();
		retval.start = input.LT(1);

		CommonTree root_0 = null;

		Token SEPARATOR3=null;
		ParserRuleReturnScope root2 =null;
		ParserRuleReturnScope root4 =null;

		CommonTree SEPARATOR3_tree=null;
		RewriteRuleTokenStream stream_SEPARATOR=new RewriteRuleTokenStream(adaptor,"token SEPARATOR");
		RewriteRuleSubtreeStream stream_root=new RewriteRuleSubtreeStream(adaptor,"rule root");

		try {
			// org/eclipse/tracecompass/tmf/filter/parser/FilterParser.g:86:10: ( root ( SEPARATOR root )* -> ^( LOGICAL root ( SEPARATOR root )* ) )
			// org/eclipse/tracecompass/tmf/filter/parser/FilterParser.g:86:12: root ( SEPARATOR root )*
			{
			pushFollow(FOLLOW_root_in_parseRoot152);
			root2=root();
			state._fsp--;

			stream_root.add(root2.getTree());
			// org/eclipse/tracecompass/tmf/filter/parser/FilterParser.g:86:17: ( SEPARATOR root )*
			loop2:
			while (true) {
				int alt2=2;
				int LA2_0 = input.LA(1);
				if ( (LA2_0==SEPARATOR) ) {
					alt2=1;
				}

				switch (alt2) {
				case 1 :
					// org/eclipse/tracecompass/tmf/filter/parser/FilterParser.g:86:18: SEPARATOR root
					{
					SEPARATOR3=(Token)match(input,SEPARATOR,FOLLOW_SEPARATOR_in_parseRoot155);  
					stream_SEPARATOR.add(SEPARATOR3);

					pushFollow(FOLLOW_root_in_parseRoot157);
					root4=root();
					state._fsp--;

					stream_root.add(root4.getTree());
					}
					break;

				default :
					break loop2;
				}
			}

			// AST REWRITE
			// elements: SEPARATOR, root, root
			// token labels: 
			// rule labels: retval
			// token list labels: 
			// rule list labels: 
			// wildcard labels: 
			retval.tree = root_0;
			RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.getTree():null);

			root_0 = (CommonTree)adaptor.nil();
			// 86:35: -> ^( LOGICAL root ( SEPARATOR root )* )
			{
				// org/eclipse/tracecompass/tmf/filter/parser/FilterParser.g:86:38: ^( LOGICAL root ( SEPARATOR root )* )
				{
				CommonTree root_1 = (CommonTree)adaptor.nil();
				root_1 = (CommonTree)adaptor.becomeRoot((CommonTree)adaptor.create(LOGICAL, "LOGICAL"), root_1);
				adaptor.addChild(root_1, stream_root.nextTree());
				// org/eclipse/tracecompass/tmf/filter/parser/FilterParser.g:86:53: ( SEPARATOR root )*
				while ( stream_SEPARATOR.hasNext()||stream_root.hasNext() ) {
					adaptor.addChild(root_1, stream_SEPARATOR.nextNode());
					adaptor.addChild(root_1, stream_root.nextTree());
				}
				stream_SEPARATOR.reset();
				stream_root.reset();

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
	// $ANTLR end "parseRoot"


	public static class root_return extends ParserRuleReturnScope {
		CommonTree tree;
		@Override
		public CommonTree getTree() { return tree; }
	};


	// $ANTLR start "root"
	// org/eclipse/tracecompass/tmf/filter/parser/FilterParser.g:88:1: root : ( ( OP_NEGATE )? start= '(' parseRoot close= ')' -> ^( ROOT1 ( OP_NEGATE )? $start parseRoot $close) | ( OP_NEGATE )? ( expression ) -> ^( ROOT2 ( OP_NEGATE )? expression ) );
	public final FilterParserParser.root_return root() throws RecognitionException {
		FilterParserParser.root_return retval = new FilterParserParser.root_return();
		retval.start = input.LT(1);

		CommonTree root_0 = null;

		Token start=null;
		Token close=null;
		Token OP_NEGATE5=null;
		Token OP_NEGATE7=null;
		ParserRuleReturnScope parseRoot6 =null;
		ParserRuleReturnScope expression8 =null;

		CommonTree start_tree=null;
		CommonTree close_tree=null;
		CommonTree OP_NEGATE5_tree=null;
		CommonTree OP_NEGATE7_tree=null;
		RewriteRuleTokenStream stream_22=new RewriteRuleTokenStream(adaptor,"token 22");
		RewriteRuleTokenStream stream_23=new RewriteRuleTokenStream(adaptor,"token 23");
		RewriteRuleTokenStream stream_OP_NEGATE=new RewriteRuleTokenStream(adaptor,"token OP_NEGATE");
		RewriteRuleSubtreeStream stream_expression=new RewriteRuleSubtreeStream(adaptor,"rule expression");
		RewriteRuleSubtreeStream stream_parseRoot=new RewriteRuleSubtreeStream(adaptor,"rule parseRoot");

		try {
			// org/eclipse/tracecompass/tmf/filter/parser/FilterParser.g:88:6: ( ( OP_NEGATE )? start= '(' parseRoot close= ')' -> ^( ROOT1 ( OP_NEGATE )? $start parseRoot $close) | ( OP_NEGATE )? ( expression ) -> ^( ROOT2 ( OP_NEGATE )? expression ) )
			int alt5=2;
			switch ( input.LA(1) ) {
			case OP_NEGATE:
				{
				int LA5_1 = input.LA(2);
				if ( (LA5_1==22) ) {
					alt5=1;
				}
				else if ( (LA5_1==TEXT||LA5_1==21) ) {
					alt5=2;
				}

				else {
					int nvaeMark = input.mark();
					try {
						input.consume();
						NoViableAltException nvae =
							new NoViableAltException("", 5, 1, input);
						throw nvae;
					} finally {
						input.rewind(nvaeMark);
					}
				}

				}
				break;
			case 22:
				{
				alt5=1;
				}
				break;
			case TEXT:
			case 21:
				{
				alt5=2;
				}
				break;
			default:
				NoViableAltException nvae =
					new NoViableAltException("", 5, 0, input);
				throw nvae;
			}
			switch (alt5) {
				case 1 :
					// org/eclipse/tracecompass/tmf/filter/parser/FilterParser.g:88:8: ( OP_NEGATE )? start= '(' parseRoot close= ')'
					{
					// org/eclipse/tracecompass/tmf/filter/parser/FilterParser.g:88:8: ( OP_NEGATE )?
					int alt3=2;
					int LA3_0 = input.LA(1);
					if ( (LA3_0==OP_NEGATE) ) {
						alt3=1;
					}
					switch (alt3) {
						case 1 :
							// org/eclipse/tracecompass/tmf/filter/parser/FilterParser.g:88:9: OP_NEGATE
							{
							OP_NEGATE5=(Token)match(input,OP_NEGATE,FOLLOW_OP_NEGATE_in_root183);  
							stream_OP_NEGATE.add(OP_NEGATE5);

							}
							break;

					}

					start=(Token)match(input,22,FOLLOW_22_in_root191);  
					stream_22.add(start);

					pushFollow(FOLLOW_parseRoot_in_root193);
					parseRoot6=parseRoot();
					state._fsp--;

					stream_parseRoot.add(parseRoot6.getTree());
					close=(Token)match(input,23,FOLLOW_23_in_root199);  
					stream_23.add(close);

					// AST REWRITE
					// elements: OP_NEGATE, parseRoot, start, close
					// token labels: start, close
					// rule labels: retval
					// token list labels: 
					// rule list labels: 
					// wildcard labels: 
					retval.tree = root_0;
					RewriteRuleTokenStream stream_start=new RewriteRuleTokenStream(adaptor,"token start",start);
					RewriteRuleTokenStream stream_close=new RewriteRuleTokenStream(adaptor,"token close",close);
					RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.getTree():null);

					root_0 = (CommonTree)adaptor.nil();
					// 88:55: -> ^( ROOT1 ( OP_NEGATE )? $start parseRoot $close)
					{
						// org/eclipse/tracecompass/tmf/filter/parser/FilterParser.g:88:58: ^( ROOT1 ( OP_NEGATE )? $start parseRoot $close)
						{
						CommonTree root_1 = (CommonTree)adaptor.nil();
						root_1 = (CommonTree)adaptor.becomeRoot((CommonTree)adaptor.create(ROOT1, "ROOT1"), root_1);
						// org/eclipse/tracecompass/tmf/filter/parser/FilterParser.g:88:66: ( OP_NEGATE )?
						if ( stream_OP_NEGATE.hasNext() ) {
							adaptor.addChild(root_1, stream_OP_NEGATE.nextNode());
						}
						stream_OP_NEGATE.reset();

						adaptor.addChild(root_1, stream_start.nextNode());
						adaptor.addChild(root_1, stream_parseRoot.nextTree());
						adaptor.addChild(root_1, stream_close.nextNode());
						adaptor.addChild(root_0, root_1);
						}

					}


					retval.tree = root_0;

					}
					break;
				case 2 :
					// org/eclipse/tracecompass/tmf/filter/parser/FilterParser.g:89:9: ( OP_NEGATE )? ( expression )
					{
					// org/eclipse/tracecompass/tmf/filter/parser/FilterParser.g:89:9: ( OP_NEGATE )?
					int alt4=2;
					int LA4_0 = input.LA(1);
					if ( (LA4_0==OP_NEGATE) ) {
						alt4=1;
					}
					switch (alt4) {
						case 1 :
							// org/eclipse/tracecompass/tmf/filter/parser/FilterParser.g:89:10: OP_NEGATE
							{
							OP_NEGATE7=(Token)match(input,OP_NEGATE,FOLLOW_OP_NEGATE_in_root229);  
							stream_OP_NEGATE.add(OP_NEGATE7);

							}
							break;

					}

					// org/eclipse/tracecompass/tmf/filter/parser/FilterParser.g:89:22: ( expression )
					// org/eclipse/tracecompass/tmf/filter/parser/FilterParser.g:89:23: expression
					{
					pushFollow(FOLLOW_expression_in_root234);
					expression8=expression();
					state._fsp--;

					stream_expression.add(expression8.getTree());
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
					// 89:35: -> ^( ROOT2 ( OP_NEGATE )? expression )
					{
						// org/eclipse/tracecompass/tmf/filter/parser/FilterParser.g:89:38: ^( ROOT2 ( OP_NEGATE )? expression )
						{
						CommonTree root_1 = (CommonTree)adaptor.nil();
						root_1 = (CommonTree)adaptor.becomeRoot((CommonTree)adaptor.create(ROOT2, "ROOT2"), root_1);
						// org/eclipse/tracecompass/tmf/filter/parser/FilterParser.g:89:46: ( OP_NEGATE )?
						if ( stream_OP_NEGATE.hasNext() ) {
							adaptor.addChild(root_1, stream_OP_NEGATE.nextNode());
						}
						stream_OP_NEGATE.reset();

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
	// $ANTLR end "root"


	public static class paragraph_return extends ParserRuleReturnScope {
		CommonTree tree;
		@Override
		public CommonTree getTree() { return tree; }
	};


	// $ANTLR start "paragraph"
	// org/eclipse/tracecompass/tmf/filter/parser/FilterParser.g:93:1: paragraph : ( TEXT )+ ;
	public final FilterParserParser.paragraph_return paragraph() throws RecognitionException {
		FilterParserParser.paragraph_return retval = new FilterParserParser.paragraph_return();
		retval.start = input.LT(1);

		CommonTree root_0 = null;

		Token TEXT9=null;

		CommonTree TEXT9_tree=null;

		try {
			// org/eclipse/tracecompass/tmf/filter/parser/FilterParser.g:93:11: ( ( TEXT )+ )
			// org/eclipse/tracecompass/tmf/filter/parser/FilterParser.g:93:13: ( TEXT )+
			{
			root_0 = (CommonTree)adaptor.nil();


			// org/eclipse/tracecompass/tmf/filter/parser/FilterParser.g:93:13: ( TEXT )+
			int cnt6=0;
			loop6:
			while (true) {
				int alt6=2;
				int LA6_0 = input.LA(1);
				if ( (LA6_0==TEXT) ) {
					alt6=1;
				}

				switch (alt6) {
				case 1 :
					// org/eclipse/tracecompass/tmf/filter/parser/FilterParser.g:93:13: TEXT
					{
					TEXT9=(Token)match(input,TEXT,FOLLOW_TEXT_in_paragraph268); 
					TEXT9_tree = (CommonTree)adaptor.create(TEXT9);
					adaptor.addChild(root_0, TEXT9_tree);

					}
					break;

				default :
					if ( cnt6 >= 1 ) break loop6;
					EarlyExitException eee = new EarlyExitException(6, input);
					throw eee;
				}
				cnt6++;
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


	public static class expression_return extends ParserRuleReturnScope {
		CommonTree tree;
		@Override
		public CommonTree getTree() { return tree; }
	};


	// $ANTLR start "expression"
	// org/eclipse/tracecompass/tmf/filter/parser/FilterParser.g:95:1: expression : ( TEXT OP TEXT -> ^( OPERATION TEXT OP TEXT ) | TEXT OP_PRESENT -> ^( OPERATION1 TEXT OP_PRESENT ) | '\"' key0= paragraph '\"' op= OP text= TEXT -> ^( OPERATION2 $key0 $op $text) | '\"' key1= paragraph '\"' op_present= OP_PRESENT -> ^( OPERATION3 $key1 $op_present) | TEXT OP '\"' paragraph '\"' -> ^( OPERATION4 TEXT OP paragraph ) | '\"' key3= paragraph '\"' OP '\"' key4= paragraph '\"' -> ^( OPERATION5 $key3 OP $key4) | TEXT -> ^( CONSTANT TEXT ) | '\"' paragraph '\"' -> ^( PAR_CONSTANT paragraph ) );
	public final FilterParserParser.expression_return expression() throws RecognitionException {
		FilterParserParser.expression_return retval = new FilterParserParser.expression_return();
		retval.start = input.LT(1);

		CommonTree root_0 = null;

		Token op=null;
		Token text=null;
		Token op_present=null;
		Token TEXT10=null;
		Token OP11=null;
		Token TEXT12=null;
		Token TEXT13=null;
		Token OP_PRESENT14=null;
		Token char_literal15=null;
		Token char_literal16=null;
		Token char_literal17=null;
		Token char_literal18=null;
		Token TEXT19=null;
		Token OP20=null;
		Token char_literal21=null;
		Token char_literal23=null;
		Token char_literal24=null;
		Token char_literal25=null;
		Token OP26=null;
		Token char_literal27=null;
		Token char_literal28=null;
		Token TEXT29=null;
		Token char_literal30=null;
		Token char_literal32=null;
		ParserRuleReturnScope key0 =null;
		ParserRuleReturnScope key1 =null;
		ParserRuleReturnScope key3 =null;
		ParserRuleReturnScope key4 =null;
		ParserRuleReturnScope paragraph22 =null;
		ParserRuleReturnScope paragraph31 =null;

		CommonTree op_tree=null;
		CommonTree text_tree=null;
		CommonTree op_present_tree=null;
		CommonTree TEXT10_tree=null;
		CommonTree OP11_tree=null;
		CommonTree TEXT12_tree=null;
		CommonTree TEXT13_tree=null;
		CommonTree OP_PRESENT14_tree=null;
		CommonTree char_literal15_tree=null;
		CommonTree char_literal16_tree=null;
		CommonTree char_literal17_tree=null;
		CommonTree char_literal18_tree=null;
		CommonTree TEXT19_tree=null;
		CommonTree OP20_tree=null;
		CommonTree char_literal21_tree=null;
		CommonTree char_literal23_tree=null;
		CommonTree char_literal24_tree=null;
		CommonTree char_literal25_tree=null;
		CommonTree OP26_tree=null;
		CommonTree char_literal27_tree=null;
		CommonTree char_literal28_tree=null;
		CommonTree TEXT29_tree=null;
		CommonTree char_literal30_tree=null;
		CommonTree char_literal32_tree=null;
		RewriteRuleTokenStream stream_OP=new RewriteRuleTokenStream(adaptor,"token OP");
		RewriteRuleTokenStream stream_OP_PRESENT=new RewriteRuleTokenStream(adaptor,"token OP_PRESENT");
		RewriteRuleTokenStream stream_TEXT=new RewriteRuleTokenStream(adaptor,"token TEXT");
		RewriteRuleTokenStream stream_21=new RewriteRuleTokenStream(adaptor,"token 21");
		RewriteRuleSubtreeStream stream_paragraph=new RewriteRuleSubtreeStream(adaptor,"rule paragraph");

		try {
			// org/eclipse/tracecompass/tmf/filter/parser/FilterParser.g:95:12: ( TEXT OP TEXT -> ^( OPERATION TEXT OP TEXT ) | TEXT OP_PRESENT -> ^( OPERATION1 TEXT OP_PRESENT ) | '\"' key0= paragraph '\"' op= OP text= TEXT -> ^( OPERATION2 $key0 $op $text) | '\"' key1= paragraph '\"' op_present= OP_PRESENT -> ^( OPERATION3 $key1 $op_present) | TEXT OP '\"' paragraph '\"' -> ^( OPERATION4 TEXT OP paragraph ) | '\"' key3= paragraph '\"' OP '\"' key4= paragraph '\"' -> ^( OPERATION5 $key3 OP $key4) | TEXT -> ^( CONSTANT TEXT ) | '\"' paragraph '\"' -> ^( PAR_CONSTANT paragraph ) )
			int alt7=8;
			alt7 = dfa7.predict(input);
			switch (alt7) {
				case 1 :
					// org/eclipse/tracecompass/tmf/filter/parser/FilterParser.g:95:13: TEXT OP TEXT
					{
					TEXT10=(Token)match(input,TEXT,FOLLOW_TEXT_in_expression276);  
					stream_TEXT.add(TEXT10);

					OP11=(Token)match(input,OP,FOLLOW_OP_in_expression278);  
					stream_OP.add(OP11);

					TEXT12=(Token)match(input,TEXT,FOLLOW_TEXT_in_expression280);  
					stream_TEXT.add(TEXT12);

					// AST REWRITE
					// elements: TEXT, OP, TEXT
					// token labels: 
					// rule labels: retval
					// token list labels: 
					// rule list labels: 
					// wildcard labels: 
					retval.tree = root_0;
					RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.getTree():null);

					root_0 = (CommonTree)adaptor.nil();
					// 95:26: -> ^( OPERATION TEXT OP TEXT )
					{
						// org/eclipse/tracecompass/tmf/filter/parser/FilterParser.g:95:29: ^( OPERATION TEXT OP TEXT )
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
					// org/eclipse/tracecompass/tmf/filter/parser/FilterParser.g:96:14: TEXT OP_PRESENT
					{
					TEXT13=(Token)match(input,TEXT,FOLLOW_TEXT_in_expression307);  
					stream_TEXT.add(TEXT13);

					OP_PRESENT14=(Token)match(input,OP_PRESENT,FOLLOW_OP_PRESENT_in_expression309);  
					stream_OP_PRESENT.add(OP_PRESENT14);

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
					// 96:30: -> ^( OPERATION1 TEXT OP_PRESENT )
					{
						// org/eclipse/tracecompass/tmf/filter/parser/FilterParser.g:96:33: ^( OPERATION1 TEXT OP_PRESENT )
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
					// org/eclipse/tracecompass/tmf/filter/parser/FilterParser.g:97:14: '\"' key0= paragraph '\"' op= OP text= TEXT
					{
					char_literal15=(Token)match(input,21,FOLLOW_21_in_expression334);  
					stream_21.add(char_literal15);

					pushFollow(FOLLOW_paragraph_in_expression340);
					key0=paragraph();
					state._fsp--;

					stream_paragraph.add(key0.getTree());
					char_literal16=(Token)match(input,21,FOLLOW_21_in_expression342);  
					stream_21.add(char_literal16);

					op=(Token)match(input,OP,FOLLOW_OP_in_expression348);  
					stream_OP.add(op);

					text=(Token)match(input,TEXT,FOLLOW_TEXT_in_expression354);  
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
					// 97:59: -> ^( OPERATION2 $key0 $op $text)
					{
						// org/eclipse/tracecompass/tmf/filter/parser/FilterParser.g:97:62: ^( OPERATION2 $key0 $op $text)
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
					// org/eclipse/tracecompass/tmf/filter/parser/FilterParser.g:98:14: '\"' key1= paragraph '\"' op_present= OP_PRESENT
					{
					char_literal17=(Token)match(input,21,FOLLOW_21_in_expression384);  
					stream_21.add(char_literal17);

					pushFollow(FOLLOW_paragraph_in_expression390);
					key1=paragraph();
					state._fsp--;

					stream_paragraph.add(key1.getTree());
					char_literal18=(Token)match(input,21,FOLLOW_21_in_expression392);  
					stream_21.add(char_literal18);

					op_present=(Token)match(input,OP_PRESENT,FOLLOW_OP_PRESENT_in_expression398);  
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
					// 98:63: -> ^( OPERATION3 $key1 $op_present)
					{
						// org/eclipse/tracecompass/tmf/filter/parser/FilterParser.g:98:66: ^( OPERATION3 $key1 $op_present)
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
					// org/eclipse/tracecompass/tmf/filter/parser/FilterParser.g:99:14: TEXT OP '\"' paragraph '\"'
					{
					TEXT19=(Token)match(input,TEXT,FOLLOW_TEXT_in_expression425);  
					stream_TEXT.add(TEXT19);

					OP20=(Token)match(input,OP,FOLLOW_OP_in_expression427);  
					stream_OP.add(OP20);

					char_literal21=(Token)match(input,21,FOLLOW_21_in_expression429);  
					stream_21.add(char_literal21);

					pushFollow(FOLLOW_paragraph_in_expression431);
					paragraph22=paragraph();
					state._fsp--;

					stream_paragraph.add(paragraph22.getTree());
					char_literal23=(Token)match(input,21,FOLLOW_21_in_expression433);  
					stream_21.add(char_literal23);

					// AST REWRITE
					// elements: TEXT, OP, paragraph
					// token labels: 
					// rule labels: retval
					// token list labels: 
					// rule list labels: 
					// wildcard labels: 
					retval.tree = root_0;
					RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.getTree():null);

					root_0 = (CommonTree)adaptor.nil();
					// 99:40: -> ^( OPERATION4 TEXT OP paragraph )
					{
						// org/eclipse/tracecompass/tmf/filter/parser/FilterParser.g:99:43: ^( OPERATION4 TEXT OP paragraph )
						{
						CommonTree root_1 = (CommonTree)adaptor.nil();
						root_1 = (CommonTree)adaptor.becomeRoot((CommonTree)adaptor.create(OPERATION4, "OPERATION4"), root_1);
						adaptor.addChild(root_1, stream_TEXT.nextNode());
						adaptor.addChild(root_1, stream_OP.nextNode());
						adaptor.addChild(root_1, stream_paragraph.nextTree());
						adaptor.addChild(root_0, root_1);
						}

					}


					retval.tree = root_0;

					}
					break;
				case 6 :
					// org/eclipse/tracecompass/tmf/filter/parser/FilterParser.g:100:14: '\"' key3= paragraph '\"' OP '\"' key4= paragraph '\"'
					{
					char_literal24=(Token)match(input,21,FOLLOW_21_in_expression460);  
					stream_21.add(char_literal24);

					pushFollow(FOLLOW_paragraph_in_expression466);
					key3=paragraph();
					state._fsp--;

					stream_paragraph.add(key3.getTree());
					char_literal25=(Token)match(input,21,FOLLOW_21_in_expression468);  
					stream_21.add(char_literal25);

					OP26=(Token)match(input,OP,FOLLOW_OP_in_expression470);  
					stream_OP.add(OP26);

					char_literal27=(Token)match(input,21,FOLLOW_21_in_expression472);  
					stream_21.add(char_literal27);

					pushFollow(FOLLOW_paragraph_in_expression478);
					key4=paragraph();
					state._fsp--;

					stream_paragraph.add(key4.getTree());
					char_literal28=(Token)match(input,21,FOLLOW_21_in_expression480);  
					stream_21.add(char_literal28);

					// AST REWRITE
					// elements: OP, key4, key3
					// token labels: 
					// rule labels: key3, key4, retval
					// token list labels: 
					// rule list labels: 
					// wildcard labels: 
					retval.tree = root_0;
					RewriteRuleSubtreeStream stream_key3=new RewriteRuleSubtreeStream(adaptor,"rule key3",key3!=null?key3.getTree():null);
					RewriteRuleSubtreeStream stream_key4=new RewriteRuleSubtreeStream(adaptor,"rule key4",key4!=null?key4.getTree():null);
					RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.getTree():null);

					root_0 = (CommonTree)adaptor.nil();
					// 100:67: -> ^( OPERATION5 $key3 OP $key4)
					{
						// org/eclipse/tracecompass/tmf/filter/parser/FilterParser.g:100:70: ^( OPERATION5 $key3 OP $key4)
						{
						CommonTree root_1 = (CommonTree)adaptor.nil();
						root_1 = (CommonTree)adaptor.becomeRoot((CommonTree)adaptor.create(OPERATION5, "OPERATION5"), root_1);
						adaptor.addChild(root_1, stream_key3.nextTree());
						adaptor.addChild(root_1, stream_OP.nextNode());
						adaptor.addChild(root_1, stream_key4.nextTree());
						adaptor.addChild(root_0, root_1);
						}

					}


					retval.tree = root_0;

					}
					break;
				case 7 :
					// org/eclipse/tracecompass/tmf/filter/parser/FilterParser.g:101:14: TEXT
					{
					TEXT29=(Token)match(input,TEXT,FOLLOW_TEXT_in_expression509);  
					stream_TEXT.add(TEXT29);

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
					// 101:20: -> ^( CONSTANT TEXT )
					{
						// org/eclipse/tracecompass/tmf/filter/parser/FilterParser.g:101:23: ^( CONSTANT TEXT )
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
				case 8 :
					// org/eclipse/tracecompass/tmf/filter/parser/FilterParser.g:102:14: '\"' paragraph '\"'
					{
					char_literal30=(Token)match(input,21,FOLLOW_21_in_expression533);  
					stream_21.add(char_literal30);

					pushFollow(FOLLOW_paragraph_in_expression535);
					paragraph31=paragraph();
					state._fsp--;

					stream_paragraph.add(paragraph31.getTree());
					char_literal32=(Token)match(input,21,FOLLOW_21_in_expression537);  
					stream_21.add(char_literal32);

					// AST REWRITE
					// elements: paragraph
					// token labels: 
					// rule labels: retval
					// token list labels: 
					// rule list labels: 
					// wildcard labels: 
					retval.tree = root_0;
					RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.getTree():null);

					root_0 = (CommonTree)adaptor.nil();
					// 102:32: -> ^( PAR_CONSTANT paragraph )
					{
						// org/eclipse/tracecompass/tmf/filter/parser/FilterParser.g:102:35: ^( PAR_CONSTANT paragraph )
						{
						CommonTree root_1 = (CommonTree)adaptor.nil();
						root_1 = (CommonTree)adaptor.becomeRoot((CommonTree)adaptor.create(PAR_CONSTANT, "PAR_CONSTANT"), root_1);
						adaptor.addChild(root_1, stream_paragraph.nextTree());
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

	// Delegated rules


	protected DFA7 dfa7 = new DFA7(this);
	static final String DFA7_eotS =
		"\17\uffff";
	static final String DFA7_eofS =
		"\1\uffff\1\5\7\uffff\1\14\5\uffff";
	static final String DFA7_minS =
		"\1\23\1\6\2\23\2\uffff\1\23\2\uffff\1\6\1\23\4\uffff";
	static final String DFA7_maxS =
		"\1\25\1\27\1\23\1\25\2\uffff\1\25\2\uffff\1\27\1\25\4\uffff";
	static final String DFA7_acceptS =
		"\4\uffff\1\2\1\7\1\uffff\1\1\1\5\2\uffff\1\4\1\10\1\3\1\6";
	static final String DFA7_specialS =
		"\17\uffff}>";
	static final String[] DFA7_transitionS = {
			"\1\1\1\uffff\1\2",
			"\1\3\6\uffff\1\5\1\4\3\uffff\2\5\1\uffff\3\5",
			"\1\6",
			"\1\7\1\uffff\1\10",
			"",
			"",
			"\1\6\1\uffff\1\11",
			"",
			"",
			"\1\12\6\uffff\1\14\1\13\3\uffff\2\14\1\uffff\3\14",
			"\1\15\1\uffff\1\16",
			"",
			"",
			"",
			""
	};

	static final short[] DFA7_eot = DFA.unpackEncodedString(DFA7_eotS);
	static final short[] DFA7_eof = DFA.unpackEncodedString(DFA7_eofS);
	static final char[] DFA7_min = DFA.unpackEncodedStringToUnsignedChars(DFA7_minS);
	static final char[] DFA7_max = DFA.unpackEncodedStringToUnsignedChars(DFA7_maxS);
	static final short[] DFA7_accept = DFA.unpackEncodedString(DFA7_acceptS);
	static final short[] DFA7_special = DFA.unpackEncodedString(DFA7_specialS);
	static final short[][] DFA7_transition;

	static {
		int numStates = DFA7_transitionS.length;
		DFA7_transition = new short[numStates][];
		for (int i=0; i<numStates; i++) {
			DFA7_transition[i] = DFA.unpackEncodedString(DFA7_transitionS[i]);
		}
	}

	protected class DFA7 extends DFA {

		public DFA7(BaseRecognizer recognizer) {
			this.recognizer = recognizer;
			this.decisionNumber = 7;
			this.eot = DFA7_eot;
			this.eof = DFA7_eof;
			this.min = DFA7_min;
			this.max = DFA7_max;
			this.accept = DFA7_accept;
			this.special = DFA7_special;
			this.transition = DFA7_transition;
		}
		@Override
		public String getDescription() {
			return "95:1: expression : ( TEXT OP TEXT -> ^( OPERATION TEXT OP TEXT ) | TEXT OP_PRESENT -> ^( OPERATION1 TEXT OP_PRESENT ) | '\"' key0= paragraph '\"' op= OP text= TEXT -> ^( OPERATION2 $key0 $op $text) | '\"' key1= paragraph '\"' op_present= OP_PRESENT -> ^( OPERATION3 $key1 $op_present) | TEXT OP '\"' paragraph '\"' -> ^( OPERATION4 TEXT OP paragraph ) | '\"' key3= paragraph '\"' OP '\"' key4= paragraph '\"' -> ^( OPERATION5 $key3 OP $key4) | TEXT -> ^( CONSTANT TEXT ) | '\"' paragraph '\"' -> ^( PAR_CONSTANT paragraph ) );";
		}
	}

	public static final BitSet FOLLOW_parseRoot_in_parse143 = new BitSet(new long[]{0x0000000000682002L});
	public static final BitSet FOLLOW_root_in_parseRoot152 = new BitSet(new long[]{0x0000000000040002L});
	public static final BitSet FOLLOW_SEPARATOR_in_parseRoot155 = new BitSet(new long[]{0x0000000000682000L});
	public static final BitSet FOLLOW_root_in_parseRoot157 = new BitSet(new long[]{0x0000000000040002L});
	public static final BitSet FOLLOW_OP_NEGATE_in_root183 = new BitSet(new long[]{0x0000000000400000L});
	public static final BitSet FOLLOW_22_in_root191 = new BitSet(new long[]{0x0000000000682000L});
	public static final BitSet FOLLOW_parseRoot_in_root193 = new BitSet(new long[]{0x0000000000800000L});
	public static final BitSet FOLLOW_23_in_root199 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_OP_NEGATE_in_root229 = new BitSet(new long[]{0x0000000000280000L});
	public static final BitSet FOLLOW_expression_in_root234 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_TEXT_in_paragraph268 = new BitSet(new long[]{0x0000000000080002L});
	public static final BitSet FOLLOW_TEXT_in_expression276 = new BitSet(new long[]{0x0000000000000040L});
	public static final BitSet FOLLOW_OP_in_expression278 = new BitSet(new long[]{0x0000000000080000L});
	public static final BitSet FOLLOW_TEXT_in_expression280 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_TEXT_in_expression307 = new BitSet(new long[]{0x0000000000004000L});
	public static final BitSet FOLLOW_OP_PRESENT_in_expression309 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_21_in_expression334 = new BitSet(new long[]{0x0000000000080000L});
	public static final BitSet FOLLOW_paragraph_in_expression340 = new BitSet(new long[]{0x0000000000200000L});
	public static final BitSet FOLLOW_21_in_expression342 = new BitSet(new long[]{0x0000000000000040L});
	public static final BitSet FOLLOW_OP_in_expression348 = new BitSet(new long[]{0x0000000000080000L});
	public static final BitSet FOLLOW_TEXT_in_expression354 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_21_in_expression384 = new BitSet(new long[]{0x0000000000080000L});
	public static final BitSet FOLLOW_paragraph_in_expression390 = new BitSet(new long[]{0x0000000000200000L});
	public static final BitSet FOLLOW_21_in_expression392 = new BitSet(new long[]{0x0000000000004000L});
	public static final BitSet FOLLOW_OP_PRESENT_in_expression398 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_TEXT_in_expression425 = new BitSet(new long[]{0x0000000000000040L});
	public static final BitSet FOLLOW_OP_in_expression427 = new BitSet(new long[]{0x0000000000200000L});
	public static final BitSet FOLLOW_21_in_expression429 = new BitSet(new long[]{0x0000000000080000L});
	public static final BitSet FOLLOW_paragraph_in_expression431 = new BitSet(new long[]{0x0000000000200000L});
	public static final BitSet FOLLOW_21_in_expression433 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_21_in_expression460 = new BitSet(new long[]{0x0000000000080000L});
	public static final BitSet FOLLOW_paragraph_in_expression466 = new BitSet(new long[]{0x0000000000200000L});
	public static final BitSet FOLLOW_21_in_expression468 = new BitSet(new long[]{0x0000000000000040L});
	public static final BitSet FOLLOW_OP_in_expression470 = new BitSet(new long[]{0x0000000000200000L});
	public static final BitSet FOLLOW_21_in_expression472 = new BitSet(new long[]{0x0000000000080000L});
	public static final BitSet FOLLOW_paragraph_in_expression478 = new BitSet(new long[]{0x0000000000200000L});
	public static final BitSet FOLLOW_21_in_expression480 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_TEXT_in_expression509 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_21_in_expression533 = new BitSet(new long[]{0x0000000000080000L});
	public static final BitSet FOLLOW_paragraph_in_expression535 = new BitSet(new long[]{0x0000000000200000L});
	public static final BitSet FOLLOW_21_in_expression537 = new BitSet(new long[]{0x0000000000000002L});
}
