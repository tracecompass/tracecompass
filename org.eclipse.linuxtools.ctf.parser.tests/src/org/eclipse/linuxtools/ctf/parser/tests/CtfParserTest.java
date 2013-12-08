/*******************************************************************************
 * Copyright (c) 2013 Etienne Bergeron
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Etienne Bergeron - Initial API and implementation
 *******************************************************************************/

package org.eclipse.linuxtools.ctf.parser.tests;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import org.antlr.runtime.ANTLRStringStream;
import org.antlr.runtime.CharStream;
import org.antlr.runtime.CommonTokenStream;
import org.antlr.runtime.RecognitionException;
import org.antlr.runtime.tree.CommonTree;
import org.eclipse.linuxtools.ctf.parser.CTFLexer;
import org.eclipse.linuxtools.ctf.parser.CTFParser;
import org.junit.Ignore;
import org.junit.Test;

/**
 * This test validates the CTF-Parser implementation.
 *
 * The goal of these tests is to validate syntactic rules and not the
 * CTF semantic. Each test parses a string with a given rule of the
 * compiled parser and validates the resulting tree by using match rules.
 *
 * @author Etienne Bergeron
 */
public class CtfParserTest {

    // ------------------------------------------------------------------------
    // Attributes
    // ------------------------------------------------------------------------

    private CTFParser parser;

    // ------------------------------------------------------------------------
    // Matches - Helper class and functions to match a parsed tree.
    // ------------------------------------------------------------------------

    private class TreeMatcher {
        int fType;
        String fText;
        TreeMatcher[] fChild;

        TreeMatcher(int type, String text, TreeMatcher child[]) {
            fType = type;
            fText = text;
            fChild = child;
        }

        void matches(CommonTree tree) {
            if (fType == -1) {
                return;
            }
            if (tree.getType() != fType) {
                fail("Type mismatch!" +
                     " expected:" + fType +
                     " actual:" + tree.getType());
            }

            if (fText != null) {
                if (tree.getText().compareTo(fText) != 0) {
                    fail("Text mismatch!" +
                            " expected:" + fText +
                            " actual:" + tree.getText());
                }
            }

            if (fChild != null) {
                int size = fChild.length;
                if (tree.getChildren() == null) {
                    if (size != 0) {
                        fail("Invalid children!"
                                + "Expect: " + size + "child");
                    }
                } else {
                    if (tree.getChildren().size() != size) {
                        fail("Invalid number of childs!"
                             + " expected:" + size
                             + " actual:" + tree.getChildren().size());
                    }

                    for (int i = 0; i < size; ++i) {
                        fChild[i].matches((CommonTree) tree.getChild(i));
                    }
                }
            }
        }
    }

    void Matches(TreeMatcher matcher, CommonTree tree) {
        if (tree == null) {
            fail("Parsing failed!");
        }
        matcher.matches(tree);
    }

    TreeMatcher All() {
        return new TreeMatcher(-1, null, null);
    }

    TreeMatcher Node(int type, TreeMatcher... child) {
        return new TreeMatcher(type, null, child);
    }

    TreeMatcher Node(int type, String text, TreeMatcher... child) {
        return new TreeMatcher(type, text, child);
    }

    TreeMatcher List(TreeMatcher... child) {
        return new TreeMatcher(0, null, child);
    }

    // ------------------------------------------------------------------------
    // Operations
    // ------------------------------------------------------------------------

    private void setInput(String content) {
        CharStream cs = new ANTLRStringStream(content);
        CTFLexer lexer = new CTFLexer(cs);
        CommonTokenStream tokens = new CommonTokenStream(lexer);
        parser = new CTFParser(tokens, false);
    }

    private CommonTree primaryExpression(String content) {
        try {
            setInput(content);
            return (CommonTree) parser.primaryExpression().getTree();
        } catch (RecognitionException e) {
            return null;
        }
    }

    private CommonTree unaryExpression(String content) {
        try {
            setInput(content);
            return (CommonTree) parser.unaryExpression().getTree();
        } catch (RecognitionException e) {
            return null;
        }
    }

    private CommonTree declaration(String content) {
        try {
            setInput(content);
            return (CommonTree) parser.declaration().getTree();
        } catch (RecognitionException e) {
            return null;
        }
    }

    // ------------------------------------------------------------------------
    // Test cases
    // ------------------------------------------------------------------------


    /**
     * Validate that parsing of an empty expression is invalid.
     */
    @Test
    public void testPrimaryExpression() {
        CommonTree tree_empty = primaryExpression("");
        assertEquals(null, tree_empty);
    }

    /**
     * Validate parsing of literals through a primary expression
     */
    @Test
    public void testIntegerLiteralPrimaryExpression() {
        Matches(Node(CTFParser.UNARY_EXPRESSION_DEC,
                     Node(CTFParser.DECIMAL_LITERAL, "123")),
                primaryExpression("123"));

        Matches(Node(CTFParser.UNARY_EXPRESSION_HEX,
                     Node(CTFParser.HEX_LITERAL, "0x123")),
                primaryExpression("0x123"));

        Matches(Node(CTFParser.UNARY_EXPRESSION_OCT,
                     Node(CTFParser.OCTAL_LITERAL, "0123")),
                primaryExpression("0123"));

        Matches(Node(CTFParser.UNARY_EXPRESSION_DEC,
                     Node(CTFParser.DECIMAL_LITERAL, "123"),
                     Node(CTFParser.SIGN, "-")),
                primaryExpression("-123"));

        Matches(Node(CTFParser.UNARY_EXPRESSION_DEC,
                     Node(CTFParser.DECIMAL_LITERAL, "123"),
                     Node(CTFParser.SIGN, "-")),
                primaryExpression("  -  123"));

        Matches(Node(CTFParser.UNARY_EXPRESSION_DEC,
                     Node(CTFParser.DECIMAL_LITERAL, "123"),
                     Node(CTFParser.SIGN, "-"),
                     Node(CTFParser.SIGN, "-"),
                     Node(CTFParser.SIGN, "+")),
                primaryExpression(" - -  + 123"));

        Matches(Node(CTFParser.UNARY_EXPRESSION_HEX,
                     Node(CTFParser.HEX_LITERAL, "0x123"),
                     Node(CTFParser.SIGN, "+"),
                     Node(CTFParser.SIGN, "-")),
                primaryExpression("+ - 0x123"));

        Matches(Node(CTFParser.UNARY_EXPRESSION_OCT,
                     Node(CTFParser.OCTAL_LITERAL, "0123"),
                     Node(CTFParser.SIGN, "+"),
                     Node(CTFParser.SIGN, "-")),
                primaryExpression("+ - 0123"));
    }

    /**
     * Validate parsing of a character literals through a primary expression
     */
    @Test
    public void testCharacterLiteralPrimaryExpression() {
        Matches(Node(CTFParser.CHARACTER_LITERAL, "'a'"),
                primaryExpression("'a'"));

        Matches(Node(CTFParser.CHARACTER_LITERAL, "'\\n'"),
                primaryExpression("'\\n'"));
    }

    /**
     * Validate parsing of a string literals through a primary expression
     */
    @Test
    public void testStringLiteralPrimaryExpression() {
        Matches(Node(CTFParser.UNARY_EXPRESSION_STRING_QUOTES,
                     Node(CTFParser.STRING_LITERAL, "\"aaa\"")),
                primaryExpression("\"aaa\""));

        Matches(Node(CTFParser.UNARY_EXPRESSION_STRING_QUOTES,
                     Node(CTFParser.STRING_LITERAL, "L\"aaa\"")),
                primaryExpression("L\"aaa\""));

        Matches(Node(CTFParser.UNARY_EXPRESSION_STRING_QUOTES,
                     Node(CTFParser.STRING_LITERAL, "\"aaa\\n\"")),
                primaryExpression("\"aaa\\n\""));
    }

    /**
     * Validate parsing of keywords through a primary expression
     */
    @Test
    public void testKeywordPrimaryExpression() {
        Matches(Node(CTFParser.UNARY_EXPRESSION_STRING,
                     Node(CTFParser.SIGNEDTOK, "signed")),
                primaryExpression("signed"));
        Matches(Node(CTFParser.UNARY_EXPRESSION_STRING,
                     Node(CTFParser.ALIGNTOK, "align")),
                primaryExpression("align"));
    }

    /**
     * Validate parsing of identifiers through a primary expression
     */
    @Test
    public void testIdentifierPrimaryExpression() {
        Matches(Node(CTFParser.UNARY_EXPRESSION_STRING,
                     Node(CTFParser.IDENTIFIER, "x")),
                primaryExpression("x"));
        Matches(Node(CTFParser.UNARY_EXPRESSION_STRING,
                     Node(CTFParser.IDENTIFIER, "_123")),
                primaryExpression("_123"));
    }

    /**
     * Validate that parsing of an empty unary expression is invalid.
     */
    @Test
    public void testUnaryExpression() {
        CommonTree tree_empty = unaryExpression("");
        assertEquals(null, tree_empty);
    }

    /**
     * Validate parsing primary expression through an unary expression
     */
    @Test
    public void testSimpleUnaryExpression() {
        Matches(Node(CTFParser.UNARY_EXPRESSION_DEC,
                     Node(CTFParser.DECIMAL_LITERAL, "123")),
                unaryExpression("123"));

        Matches(Node(CTFParser.UNARY_EXPRESSION_STRING,
                     Node(CTFParser.IDENTIFIER, "x")),
                unaryExpression("x"));
    }

    /**
     * Validate parsing array through an unary expression
     */
    @Test
    public void testArrayUnaryExpression() {
        Matches(List(Node(CTFParser.UNARY_EXPRESSION_STRING,
                          Node(CTFParser.IDENTIFIER, "x")),
                     Node(CTFParser.OPENBRAC),
                     Node(CTFParser.UNARY_EXPRESSION_DEC,
                          Node(CTFParser.DECIMAL_LITERAL, "1"))),
                unaryExpression("x[1]"));

        Matches(List(Node(CTFParser.UNARY_EXPRESSION_STRING,
                          Node(CTFParser.IDENTIFIER, "x")),
                     Node(CTFParser.OPENBRAC),
                     Node(CTFParser.UNARY_EXPRESSION_STRING,
                          Node(CTFParser.IDENTIFIER, "n"))),
                unaryExpression("x[n]"));

        Matches(List(Node(CTFParser.UNARY_EXPRESSION_STRING,
                          Node(CTFParser.IDENTIFIER, "x")),
                     Node(CTFParser.OPENBRAC),
                     Node(CTFParser.UNARY_EXPRESSION_STRING,
                          Node(CTFParser.IDENTIFIER, "n")),
                     Node(CTFParser.OPENBRAC),
                     Node(CTFParser.UNARY_EXPRESSION_DEC,
                          Node(CTFParser.DECIMAL_LITERAL, "1"))),
                unaryExpression("x[n][1]"));

        Matches(List(Node(CTFParser.UNARY_EXPRESSION_STRING,
                          Node(CTFParser.IDENTIFIER, "x")),
                     Node(CTFParser.OPENBRAC),
                     Node(CTFParser.UNARY_EXPRESSION_STRING,
                          Node(CTFParser.IDENTIFIER, "n")),
                     Node(CTFParser.OPENBRAC),
                     Node(CTFParser.UNARY_EXPRESSION_DEC,
                          Node(CTFParser.DECIMAL_LITERAL, "1"),
                          Node(CTFParser.SIGN, "+"))),
                unaryExpression("x[n][+1]"));
    }

    /**
     * Validate parsing array with keywords through an unary expression
     */
    @Test
    public void testSpecialArrayUnaryExpression() {
        // Added for CTF-v1.8
        Matches(List(Node(CTFParser.TRACE),
                     Node(CTFParser.OPENBRAC),
                     Node(CTFParser.UNARY_EXPRESSION_STRING,
                          Node(CTFParser.IDENTIFIER, "n"))),
                unaryExpression("trace[n]"));

        Matches(List(Node(CTFParser.CLOCK),
                     Node(CTFParser.OPENBRAC),
                     Node(CTFParser.UNARY_EXPRESSION_STRING,
                          Node(CTFParser.IDENTIFIER, "n")),
                     Node(CTFParser.OPENBRAC),
                     Node(CTFParser.UNARY_EXPRESSION_DEC,
                          Node(CTFParser.DECIMAL_LITERAL, "1"))),
                unaryExpression("clock[n][1]"));
    }

    /**
     * Validate parsing member expression through an unary expression
     */
    @Test
    public void testMemberUnaryExpression() {
        Matches(List(Node(CTFParser.UNARY_EXPRESSION_STRING,
                          Node(CTFParser.IDENTIFIER, "x")),
                     Node(CTFParser.DOT,
                          Node(CTFParser.UNARY_EXPRESSION_STRING,
                               Node(CTFParser.IDENTIFIER, "y")))),
                unaryExpression("x.y"));

        Matches(List(Node(CTFParser.UNARY_EXPRESSION_STRING,
                         Node(CTFParser.IDENTIFIER, "x")),
                     Node(CTFParser.DOT,
                          Node(CTFParser.UNARY_EXPRESSION_STRING,
                               Node(CTFParser.IDENTIFIER, "y"))),
                     Node(CTFParser.DOT,
                          Node(CTFParser.UNARY_EXPRESSION_STRING,
                               Node(CTFParser.IDENTIFIER, "z")))),
                unaryExpression("x.y.z"));
    }

    /**
     * Validate parsing pointer expression through an unary expression
     */
    @Test
    public void testPointerUnaryExpression() {
        Matches(List(Node(CTFParser.UNARY_EXPRESSION_STRING,
                          Node(CTFParser.IDENTIFIER, "x")),
                     Node(CTFParser.ARROW,
                          Node(CTFParser.UNARY_EXPRESSION_STRING,
                               Node(CTFParser.IDENTIFIER, "y")))),
                unaryExpression("x->y"));

        Matches(List(Node(CTFParser.UNARY_EXPRESSION_STRING,
                          Node(CTFParser.IDENTIFIER, "x")),
                     Node(CTFParser.ARROW,
                          Node(CTFParser.UNARY_EXPRESSION_STRING,
                               Node(CTFParser.IDENTIFIER, "y"))),
                     Node(CTFParser.ARROW,
                          Node(CTFParser.UNARY_EXPRESSION_STRING,
                               Node(CTFParser.IDENTIFIER, "z")))),
                unaryExpression("x->y->z"));
    }

    /**
     * Validate complex expressions through an unary expression
     */
    @Test
    public void testMixedUnaryExpression() {
        Matches(List(Node(CTFParser.UNARY_EXPRESSION_STRING,
                          Node(CTFParser.IDENTIFIER, "x")),
                     Node(CTFParser.OPENBRAC),
                          Node(CTFParser.UNARY_EXPRESSION_DEC,
                               Node(CTFParser.DECIMAL_LITERAL, "2")),
                     Node(CTFParser.ARROW,
                          Node(CTFParser.UNARY_EXPRESSION_STRING,
                               Node(CTFParser.IDENTIFIER, "y"))),
                     Node(CTFParser.DOT,
                          Node(CTFParser.UNARY_EXPRESSION_STRING,
                               Node(CTFParser.IDENTIFIER, "z"))),
                     Node(CTFParser.OPENBRAC),
                     Node(CTFParser.UNARY_EXPRESSION_DEC,
                          Node(CTFParser.DECIMAL_LITERAL, "1"))),
                unaryExpression("x[2]->y.z[1]"));
    }

    /**
     * Validate that parsing of an empty declaration is invalid.
     */
    @Test
    public void testDeclaration() {
        CommonTree tree_empty = declaration("");
        assertEquals(null, tree_empty);
    }

    /**
     * Validate parsing of integer declaration
     */
    @Test
    public void testIntegerTypeAliasDeclaration() {
        // TODO: replace the "all" match with a better tree matcher.
        Matches(All(),
                declaration("typealias integer { } := int;"));
        Matches(All(),
                declaration("typealias integer { signed=true; } := int;"));
    }

    /**
     * Validate parsing of floating declaration
     */
    @Test
    public void testFloatingTypeAliasDeclaration() {
        // TODO: replace the "all" match with a better tree matcher.
        Matches(All(),
                declaration("typealias floating_point { } := float;"));
        Matches(All(),
                declaration("typealias floating_point { align = 32; } := float;"));
    }

    /**
     * Validate parsing of typedef declaration
     */
    @Ignore("This need a fix to the grammar to support a dummy initial scope. ")
    @Test
    public void testTypedefDeclaration() {
        // TODO: replace the "all" match with a better tree matcher.
        Matches(All(),
                declaration("typedef dummy int;"));
        Matches(All(),
                declaration("typedef integer { } int;"));
    }

    /**
     * Validate parsing of an enum declaration
     */
    @Test
    public void testEnumDeclaration() {
        Matches(Node(CTFParser.DECLARATION,
                     Node(CTFParser.TYPE_SPECIFIER_LIST,
                          Node(CTFParser.ENUM,
                               Node(CTFParser.ENUM_NAME,
                                    Node(CTFParser.IDENTIFIER, "name")),
                               Node(CTFParser.ENUM_BODY,
                                    Node(CTFParser.ENUM_ENUMERATOR,
                                         Node(CTFParser.UNARY_EXPRESSION_STRING,
                                              Node(CTFParser.IDENTIFIER, "A"))))))),
                declaration("enum name { A };"));

        Matches(Node(CTFParser.DECLARATION,
                     Node(CTFParser.TYPE_SPECIFIER_LIST,
                          Node(CTFParser.ENUM,
                               Node(CTFParser.ENUM_NAME, All()),
                               Node(CTFParser.ENUM_CONTAINER_TYPE,
                                    Node(CTFParser.TYPE_SPECIFIER_LIST,
                                         Node(CTFParser.INTTOK))),
                               Node(CTFParser.ENUM_BODY, All())))),
                declaration("enum name : int { A };"));

        Matches(Node(CTFParser.DECLARATION,
                Node(CTFParser.TYPE_SPECIFIER_LIST,
                        Node(CTFParser.ENUM,
                             Node(CTFParser.ENUM_BODY, All())))),
                declaration("enum { A };"));

        Matches(Node(CTFParser.DECLARATION,
                Node(CTFParser.TYPE_SPECIFIER_LIST,
                     Node(CTFParser.ENUM,
                          Node(CTFParser.ENUM_CONTAINER_TYPE,
                               Node(CTFParser.TYPE_SPECIFIER_LIST,
                                    Node(CTFParser.INTTOK))),
                          Node(CTFParser.ENUM_BODY, All())))),
                declaration("enum : int { A };"));
    }

    /**
     * Validate parsing of an enumerator
     */
    @Ignore("The grammar needs to be fixed.")
    @Test
    public void testDeclaratorOfEnumDeclaration() {
        /* TODO: This test crash the parser. */
        Matches(All(),
                declaration("enum { };"));

        Matches(Node(CTFParser.DECLARATION,
                     Node(CTFParser.TYPE_SPECIFIER_LIST,
                          Node(CTFParser.ENUM,
                               Node(CTFParser.ENUM_BODY,
                                    Node(CTFParser.ENUM_ENUMERATOR,
                                         Node(CTFParser.UNARY_EXPRESSION_STRING,
                                              Node(CTFParser.IDENTIFIER, "A"))),
                                    Node(CTFParser.ENUM_ENUMERATOR,
                                         Node(CTFParser.UNARY_EXPRESSION_STRING,
                                              Node(CTFParser.IDENTIFIER, "B")),
                                         Node(CTFParser.ENUM_VALUE,
                                              Node(CTFParser.UNARY_EXPRESSION_DEC,
                                                   Node(CTFParser.DECIMAL_LITERAL, "2")))),
                                    Node(CTFParser.ENUM_ENUMERATOR,
                                         Node(CTFParser.UNARY_EXPRESSION_STRING,
                                              Node(CTFParser.IDENTIFIER, "C")),
                                         Node(CTFParser.ENUM_VALUE_RANGE,
                                              Node(CTFParser.UNARY_EXPRESSION_DEC,
                                                   Node(CTFParser.DECIMAL_LITERAL, "3")),
                                              Node(CTFParser.UNARY_EXPRESSION_DEC,
                                                   Node(CTFParser.DECIMAL_LITERAL, "5")))))))),
                declaration("enum { A, B=2, C=3...5 };"));

        Matches(Node(CTFParser.DECLARATION,
                     Node(CTFParser.TYPE_SPECIFIER_LIST,
                          Node(CTFParser.ENUM,
                               Node(CTFParser.ENUM_BODY,
                                    Node(CTFParser.ENUM_ENUMERATOR,
                                         Node(CTFParser.UNARY_EXPRESSION_STRING_QUOTES,
                                              Node(CTFParser.STRING_LITERAL, "\"A\""))),
                                    Node(CTFParser.ENUM_ENUMERATOR,
                                         Node(CTFParser.UNARY_EXPRESSION_STRING_QUOTES,
                                              Node(CTFParser.STRING_LITERAL, "\"B\"")),
                                         All()))))),
                declaration("enum { \"A\", \"B\"=2 };"));
    }

    /**
     * Validate parsing of empty declaration
     */
    @Ignore("The grammar need to be fixed to support empty ctf-body.")
    @Test
    public void testEmptyDeclaration() {
        /* TODO: An exception is throw when building an common tree without
         *       assignments in the ctf-body.
         */
        Matches(All(),
                declaration("env { };"));
        Matches(All(),
                declaration("trace { };"));
        Matches(All(),
                declaration("stream { };"));
        Matches(All(),
                declaration("event { };"));
    }

    /**
     * Validate parsing of an environment declaration
     */
    @Test
    public void testEnvDeclaration() {
        Matches(Node(CTFParser.ENV,
                     Node(CTFParser.CTF_EXPRESSION_VAL,
                          Node(CTFParser.CTF_LEFT,
                               Node(CTFParser.UNARY_EXPRESSION_STRING,
                                    Node(CTFParser.IDENTIFIER, "pid"))),
                          Node(CTFParser.CTF_RIGHT,
                               Node(CTFParser.UNARY_EXPRESSION_STRING,
                                    Node(CTFParser.IDENTIFIER, "value"))))),
                declaration("env { pid = value; };"));

        Matches(Node(CTFParser.ENV,
                     Node(CTFParser.CTF_EXPRESSION_VAL, All(), All()),
                     Node(CTFParser.CTF_EXPRESSION_VAL, All(), All()),
                     Node(CTFParser.CTF_EXPRESSION_VAL, All(), All())),
                declaration("env { pid = value; proc_name = \"name\"; x = y;};"));
    }

    /**
     * Validate parsing of a trace declaration
     */
    @Ignore("The grammar need to be fixed.")
    @Test
    public void testTraceDeclaration() {
        Matches(Node(CTFParser.TRACE,
                     Node(CTFParser.CTF_EXPRESSION_VAL,
                          Node(CTFParser.CTF_LEFT,
                               Node(CTFParser.UNARY_EXPRESSION_STRING,
                                    Node(CTFParser.IDENTIFIER, "major"))),
                          Node(CTFParser.CTF_RIGHT,
                               Node(CTFParser.UNARY_EXPRESSION_DEC,
                                    Node(CTFParser.DECIMAL_LITERAL, "1"))))),
                declaration("trace { major = 1; };"));

        Matches(Node(CTFParser.TRACE,
                     Node(CTFParser.CTF_EXPRESSION_TYPE,
                          Node(CTFParser.CTF_LEFT,
                               Node(CTFParser.UNARY_EXPRESSION_STRING,
                                    Node(CTFParser.IDENTIFIER, "packet")),
                               Node(CTFParser.DOT,
                                    Node(CTFParser.UNARY_EXPRESSION_STRING,
                                         Node(CTFParser.IDENTIFIER, "header")))),
                          Node(CTFParser.CTF_RIGHT,
                               Node(CTFParser.TYPE_SPECIFIER_LIST,
                                    Node(CTFParser.STRUCT,
                                         Node(CTFParser.STRUCT_NAME,
                                              Node(CTFParser.IDENTIFIER, "dummy"))))))),
                declaration("trace { packet.header := struct dummy; };"));

        /* TODO: This test crash the parser. */
        Matches(Node(CTFParser.TRACE,
                     All()),
                declaration("trace { typedef x y; };"));

        Matches(Node(CTFParser.TRACE,
                     Node(CTFParser.CTF_EXPRESSION_VAL, All(), All()),
                     Node(CTFParser.CTF_EXPRESSION_VAL, All(), All()),
                     Node(CTFParser.CTF_EXPRESSION_TYPE, All(), All())),
                declaration("trace { major = 1; minor = 1;"
                            + "packet.header := struct dummy; };"));
    }

}
