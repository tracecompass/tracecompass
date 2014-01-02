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

import static org.junit.Assert.fail;

import java.util.LinkedList;
import java.util.List;

import org.antlr.runtime.ANTLRStringStream;
import org.antlr.runtime.CharStream;
import org.antlr.runtime.Token;
import org.eclipse.linuxtools.ctf.parser.CTFLexer;
import org.junit.Ignore;
import org.junit.Test;

/**
 * This test validates the CTF-Lexer implementation.
 *
 * The test splits a string into tokens with the compiled lexer and
 * validates the sequences of tokens produced by comparing their type
 * and content.
 *
 * @author Etienne Bergeron
 */
public class CtfLexerTest {

    // ------------------------------------------------------------------------
    // Attributes
    // ------------------------------------------------------------------------

    private final List<Token> tokens = new LinkedList<>();

    // ------------------------------------------------------------------------
    // Operations
    // ------------------------------------------------------------------------

    private void tokenize(String content) {
        CharStream cs = new ANTLRStringStream(content);
        CTFLexer lexer = new CTFLexer(cs);

        tokens.clear();
        for (;;) {
          Token token = lexer.nextToken();
          if (token == Token.EOF_TOKEN) {
            return;
          }
          tokens.add(token);
        }
    }

    private void checkToken(int type, String content) {
        Token token = tokens.remove(0);
        if (token.getType() != type) {
            fail("Invalid type [value " + token.getType()
                    + " but expect " + type + "]."
                    + " Fail to tokenize:" + content);
        } else if (token.getText().compareTo(content) != 0) {
            fail("Invalid content [value " + token.getText()
                    + " but expect " + content + "].");
        }
    }

    private void checkSingle(int type, String content) {
       tokenize(content);
       checkToken(type, content);
    }

    // ------------------------------------------------------------------------
    // Test cases
    // ------------------------------------------------------------------------

    /**
     *  Validate the parsing of keywords
     */
    @Test
    public void testKeywords() {
        checkSingle(CTFLexer.ALIGNTOK, "align");
        checkSingle(CTFLexer.CONSTTOK, "const");
        checkSingle(CTFLexer.CHARTOK, "char");
        checkSingle(CTFLexer.DOUBLETOK, "double");
        checkSingle(CTFLexer.ENUMTOK, "enum");
        checkSingle(CTFLexer.EVENTTOK, "event");
        checkSingle(CTFLexer.FLOATINGPOINTTOK, "floating_point");
        checkSingle(CTFLexer.FLOATTOK, "float");
        checkSingle(CTFLexer.INTEGERTOK, "integer");
        checkSingle(CTFLexer.INTTOK, "int");
        checkSingle(CTFLexer.LONGTOK, "long");
        checkSingle(CTFLexer.SHORTTOK, "short");
        checkSingle(CTFLexer.SIGNEDTOK, "signed");
        checkSingle(CTFLexer.STREAMTOK, "stream");
        checkSingle(CTFLexer.STRINGTOK, "string");
        checkSingle(CTFLexer.STRUCTTOK, "struct");
        checkSingle(CTFLexer.TRACETOK, "trace");
        checkSingle(CTFLexer.TYPEALIASTOK, "typealias");
        checkSingle(CTFLexer.TYPEDEFTOK, "typedef");
        checkSingle(CTFLexer.UNSIGNEDTOK, "unsigned");
        checkSingle(CTFLexer.VARIANTTOK, "variant");
        checkSingle(CTFLexer.VOIDTOK, "void");
        checkSingle(CTFLexer.BOOLTOK, "_Bool");
        checkSingle(CTFLexer.COMPLEXTOK, "_Complex");
        checkSingle(CTFLexer.IMAGINARYTOK, "_Imaginary");
        checkSingle(CTFLexer.ENVTOK, "env");
        checkSingle(CTFLexer.CLOCKTOK, "clock");
        checkSingle(CTFLexer.CALLSITETOK, "callsite");
        checkSingle(CTFLexer.NANNUMBERTOK, "NaN");
        checkSingle(CTFLexer.INFINITYTOK,  "+inf");
        checkSingle(CTFLexer.NINFINITYTOK, "-inf");
    }

    /**
     *  Validate the parsing of symbols
     */
    @Test
    public void testSymbols() {
        tokenize(" , : ... ");
        checkToken(CTFLexer.WS, " ");
        checkToken(CTFLexer.SEPARATOR, ",");
        checkToken(CTFLexer.WS, " ");
        checkToken(CTFLexer.COLON, ":");
        checkToken(CTFLexer.WS, " ");
        checkToken(CTFLexer.ELIPSES, "...");
        checkToken(CTFLexer.WS, " ");

        tokenize(" = := = ");
        checkToken(CTFLexer.WS, " ");
        checkToken(CTFLexer.ASSIGNMENT, "=");
        checkToken(CTFLexer.WS, " ");
        checkToken(CTFLexer.TYPE_ASSIGNMENT, ":=");
        checkToken(CTFLexer.WS, " ");

        tokenize(" <<>> ");
        checkToken(CTFLexer.WS, " ");
        checkToken(CTFLexer.LT, "<");
        checkToken(CTFLexer.LT, "<");
        checkToken(CTFLexer.GT, ">");
        checkToken(CTFLexer.GT, ">");
        checkToken(CTFLexer.WS, " ");

        tokenize(" ({[]}) ");
        checkToken(CTFLexer.WS, " ");
        checkToken(CTFLexer.LPAREN, "(");
        checkToken(CTFLexer.LCURL, "{");
        checkToken(CTFLexer.OPENBRAC, "[");
        checkToken(CTFLexer.CLOSEBRAC, "]");
        checkToken(CTFLexer.RCURL, "}");
        checkToken(CTFLexer.RPAREN, ")");
        checkToken(CTFLexer.WS, " ");

        tokenize(";;");
        checkToken(CTFLexer.TERM, ";");
        checkToken(CTFLexer.TERM, ";");

        tokenize(" ++ -- ");
        checkToken(CTFLexer.WS, " ");
        checkToken(CTFLexer.SIGN, "+");
        checkToken(CTFLexer.SIGN, "+");
        checkToken(CTFLexer.WS, " ");
        checkToken(CTFLexer.SIGN, "-");
        checkToken(CTFLexer.SIGN, "-");
        checkToken(CTFLexer.WS, " ");

        tokenize("-> .*.");
        checkToken(CTFLexer.ARROW, "->");
        checkToken(CTFLexer.WS, " ");
        checkToken(CTFLexer.DOT, ".");
        checkToken(CTFLexer.POINTER, "*");
        checkToken(CTFLexer.DOT, ".");
    }

    /**
     *  Validate the parsing of literals
     */
    @Test
    public void testLiterals() {
        tokenize("01 02 010");
        checkToken(CTFLexer.OCTAL_LITERAL, "01");
        checkToken(CTFLexer.WS, " ");
        checkToken(CTFLexer.OCTAL_LITERAL, "02");
        checkToken(CTFLexer.WS, " ");
        checkToken(CTFLexer.OCTAL_LITERAL, "010");

        tokenize("1 2 10 1024 ");
        checkToken(CTFLexer.DECIMAL_LITERAL, "1");
        checkToken(CTFLexer.WS, " ");
        checkToken(CTFLexer.DECIMAL_LITERAL, "2");
        checkToken(CTFLexer.WS, " ");
        checkToken(CTFLexer.DECIMAL_LITERAL, "10");
        checkToken(CTFLexer.WS, " ");
        checkToken(CTFLexer.DECIMAL_LITERAL, "1024");
        checkToken(CTFLexer.WS, " ");

        tokenize("0x01 0x02 0x0F0");
        checkToken(CTFLexer.HEX_LITERAL, "0x01");
        checkToken(CTFLexer.WS, " ");
        checkToken(CTFLexer.HEX_LITERAL, "0x02");
        checkToken(CTFLexer.WS, " ");
        checkToken(CTFLexer.HEX_LITERAL, "0x0F0");
    }

    /**
     *  Validate the parsing of literals with hexa prefix
     */
    @Test
    public void testLiteralPrefixes() {
        checkSingle(CTFLexer.HEX_LITERAL, "0x1");
        checkSingle(CTFLexer.HEX_LITERAL, "0X1");
    }

    /**
     *  Validate the parsing of literals with type suffix
     */
    @Test
    public void testLiteralSuffixes() {
        checkSingle(CTFLexer.DECIMAL_LITERAL, "0l");
        checkSingle(CTFLexer.DECIMAL_LITERAL, "0L");
        checkSingle(CTFLexer.DECIMAL_LITERAL, "0ll");
        checkSingle(CTFLexer.DECIMAL_LITERAL, "0LL");
        checkSingle(CTFLexer.DECIMAL_LITERAL, "0ul");
        checkSingle(CTFLexer.DECIMAL_LITERAL, "0uL");
        checkSingle(CTFLexer.DECIMAL_LITERAL, "0ull");
        checkSingle(CTFLexer.DECIMAL_LITERAL, "0uLL");
        checkSingle(CTFLexer.DECIMAL_LITERAL, "0Ul");
        checkSingle(CTFLexer.DECIMAL_LITERAL, "0UL");
        checkSingle(CTFLexer.DECIMAL_LITERAL, "0Ull");
        checkSingle(CTFLexer.DECIMAL_LITERAL, "0ULL");
    }

    /**
     *  Validate the accepted characters in literals.
     */
    @Test
    public void testLiteralDigits() {
        checkSingle(CTFLexer.OCTAL_LITERAL, "001234567");

        checkSingle(CTFLexer.DECIMAL_LITERAL, "123456");
        checkSingle(CTFLexer.DECIMAL_LITERAL, "987654");

        checkSingle(CTFLexer.HEX_LITERAL, "0x012345");
        checkSingle(CTFLexer.HEX_LITERAL, "0x678990");
        checkSingle(CTFLexer.HEX_LITERAL, "0xABCDEF");
        checkSingle(CTFLexer.HEX_LITERAL, "0xabcdef");
    }

    /**
     *  Validate zero literal to be the right token.
     */
    @Test
    public void testLiteralZero() {
        checkSingle(CTFLexer.OCTAL_LITERAL, "00");
        checkSingle(CTFLexer.DECIMAL_LITERAL, "0");
        checkSingle(CTFLexer.HEX_LITERAL, "0x0");
    }

    /**
     *  Validate character literals
     */
    @Test
    public void testCharLiteral() {
        checkSingle(CTFLexer.CHARACTER_LITERAL, "'x'");
        checkSingle(CTFLexer.CHARACTER_LITERAL, "'\\''");
        checkSingle(CTFLexer.CHARACTER_LITERAL, "' '");
        checkSingle(CTFLexer.CHARACTER_LITERAL, "L'1'");
    }

    /**
     *  Validate escaped character literals
     */
    @Test
    public void testEscapeCharLiteral() {
        checkSingle(CTFLexer.CHARACTER_LITERAL, "'\\a'");
        checkSingle(CTFLexer.CHARACTER_LITERAL, "'\\b'");
        checkSingle(CTFLexer.CHARACTER_LITERAL, "'\\f'");
        checkSingle(CTFLexer.CHARACTER_LITERAL, "'\\n'");
        checkSingle(CTFLexer.CHARACTER_LITERAL, "'\\r'");
        checkSingle(CTFLexer.CHARACTER_LITERAL, "'\\t'");
        checkSingle(CTFLexer.CHARACTER_LITERAL, "'\\v'");
        checkSingle(CTFLexer.CHARACTER_LITERAL, "'\\''");
        checkSingle(CTFLexer.CHARACTER_LITERAL, "'\\\"'");
        checkSingle(CTFLexer.CHARACTER_LITERAL, "'\\\\'");

        checkSingle(CTFLexer.CHARACTER_LITERAL, "'\001'");
        checkSingle(CTFLexer.CHARACTER_LITERAL, "'\01'");
        checkSingle(CTFLexer.CHARACTER_LITERAL, "'\1'");

        checkSingle(CTFLexer.CHARACTER_LITERAL, "'\\x1A'");
        checkSingle(CTFLexer.CHARACTER_LITERAL, "'\\x1a'");
        checkSingle(CTFLexer.CHARACTER_LITERAL, "'\\xa'");
        checkSingle(CTFLexer.CHARACTER_LITERAL, "'\\x0'");

        checkSingle(CTFLexer.CHARACTER_LITERAL, "'\uABCD'");
        checkSingle(CTFLexer.CHARACTER_LITERAL, "'\u0123'");
        checkSingle(CTFLexer.CHARACTER_LITERAL, "'\u012345678'");
        checkSingle(CTFLexer.CHARACTER_LITERAL, "'\uFEDCBA987'");
    }

    /**
     *  Validate string literals
     */
    @Test
    public void testStringLiteral() {
        checkSingle(CTFLexer.STRING_LITERAL, "\"\"");
        checkSingle(CTFLexer.STRING_LITERAL, "\"x\"");
        checkSingle(CTFLexer.STRING_LITERAL, "\"\\\"\"");
        checkSingle(CTFLexer.STRING_LITERAL, "\" \"");
        checkSingle(CTFLexer.STRING_LITERAL, "L\"1\"");

        checkSingle(CTFLexer.STRING_LITERAL, "\"This is \\n a multiline\\r\\n\"");
        checkSingle(CTFLexer.STRING_LITERAL, "L\"This is \\n a multiline\\r\\n\"");
    }

    /**
     *  Validate string literals with escape sequence
     */
    @Test
    public void testEscapeStringLiteral() {
        checkSingle(CTFLexer.STRING_LITERAL, "\"\\a\"");
        checkSingle(CTFLexer.STRING_LITERAL, "\"\\b\"");
        checkSingle(CTFLexer.STRING_LITERAL, "\"\\f\"");
        checkSingle(CTFLexer.STRING_LITERAL, "\"\\n\"");
        checkSingle(CTFLexer.STRING_LITERAL, "\"\\r\"");
        checkSingle(CTFLexer.STRING_LITERAL, "\"\\t\"");
        checkSingle(CTFLexer.STRING_LITERAL, "\"\\v\"");
        checkSingle(CTFLexer.STRING_LITERAL, "\"\\'\"");
        checkSingle(CTFLexer.STRING_LITERAL, "\"\\\"\"");
        checkSingle(CTFLexer.STRING_LITERAL, "\"\\\\\"");

        checkSingle(CTFLexer.STRING_LITERAL, "\"\001\"");
        checkSingle(CTFLexer.STRING_LITERAL, "\"\01\"");
        checkSingle(CTFLexer.STRING_LITERAL, "\"\1\"");

        checkSingle(CTFLexer.STRING_LITERAL, "\"\\x1A\"");
        checkSingle(CTFLexer.STRING_LITERAL, "\"\\x1a\"");
        checkSingle(CTFLexer.STRING_LITERAL, "\"\\xa\"");
        checkSingle(CTFLexer.STRING_LITERAL, "\"\\x0\"");

        checkSingle(CTFLexer.STRING_LITERAL, "\"\uABCD\"");
        checkSingle(CTFLexer.STRING_LITERAL, "\"\u0123\"");
        checkSingle(CTFLexer.STRING_LITERAL, "\"\u012345678\"");
        checkSingle(CTFLexer.STRING_LITERAL, "\"\uFEDCBA987\"");
    }

    /**
     *  Validate spaces parsing
     */
    @Test
    public void testWhitespaces() {
        tokenize("  \r\t\n\u000C ");
        checkToken(CTFLexer.WS, " ");
        checkToken(CTFLexer.WS, " ");
        checkToken(CTFLexer.WS, "\r");
        checkToken(CTFLexer.WS, "\t");
        checkToken(CTFLexer.WS, "\n");
        checkToken(CTFLexer.WS, "\u000C");
        checkToken(CTFLexer.WS, " ");
    }

    /**
     *  Validate comments parsing
     */
    @Test
    public void testComment() {
        tokenize(" /* test */ ");
        checkToken(CTFLexer.WS, " ");
        checkToken(CTFLexer.COMMENT, "/* test */");
        checkToken(CTFLexer.WS, " ");
    }

    /**
     *  Validate complex nested comments parsing
     */
    @Test
    public void testNestedComment() {
        tokenize(" /* /* */ ");
        checkToken(CTFLexer.WS, " ");
        checkToken(CTFLexer.COMMENT, "/* /* */");
        checkToken(CTFLexer.WS, " ");

        tokenize(" /* /* * ** / */ ");
        checkToken(CTFLexer.WS, " ");
        checkToken(CTFLexer.COMMENT, "/* /* * ** / */");
        checkToken(CTFLexer.WS, " ");
    }

    /**
     *  Validate multi-lines comments
     */
    @Test
    public void testMultiLineComment() {
        tokenize(" /*\ntest\n*/ ");
        checkToken(CTFLexer.WS, " ");
        checkToken(CTFLexer.COMMENT, "/*\ntest\n*/");
        checkToken(CTFLexer.WS, " ");
    }

    /**
     *  Validate single line comments
     */
    @Test
    public void testLineComment() {
        tokenize(" // asdad\r\n ");
        checkToken(CTFLexer.WS, " ");
        checkToken(CTFLexer.LINE_COMMENT, "// asdad\r\n");
        checkToken(CTFLexer.WS, " ");
    }

    /**
     *  Validate incomplete comments parsing
     */
    @Ignore("Lexer must be fixed first")
    @Test
    public void testLineCommentWithEOF() {
        tokenize("//");
        checkToken(CTFLexer.LINE_COMMENT, "//");
    }

    /**
     *  Validate parsing of mixed kind of comments
     */
    @Test
    public void testMixedComment() {
        tokenize(" // /*\n");
        checkToken(CTFLexer.WS, " ");
        checkToken(CTFLexer.LINE_COMMENT, "// /*\n");

        tokenize(" /*\n//\n*/ ");
        checkToken(CTFLexer.WS, " ");
        checkToken(CTFLexer.COMMENT, "/*\n//\n*/");
        checkToken(CTFLexer.WS, " ");
    }

    /**
     *  Validate parsing identifiers
     */
    @Test
    public void testIdentifier() {
        tokenize("_ a a1 B ");
        checkToken(CTFLexer.IDENTIFIER, "_");
        checkToken(CTFLexer.WS, " ");
        checkToken(CTFLexer.IDENTIFIER, "a");
        checkToken(CTFLexer.WS, " ");
        checkToken(CTFLexer.IDENTIFIER, "a1");
        checkToken(CTFLexer.WS, " ");
        checkToken(CTFLexer.IDENTIFIER, "B");
        checkToken(CTFLexer.WS, " ");
    }

    /**
     *  Validate accepted characters within an identifier
     */
    @Test
    public void testIdentifierLetters() {
        checkSingle(CTFLexer.IDENTIFIER, "ABCDEFGHI");
        checkSingle(CTFLexer.IDENTIFIER, "JKLMNOPQR");
        checkSingle(CTFLexer.IDENTIFIER, "STUVWXYZ");
        checkSingle(CTFLexer.IDENTIFIER, "abcdefghi");
        checkSingle(CTFLexer.IDENTIFIER, "jklmnopqr");
        checkSingle(CTFLexer.IDENTIFIER, "stuvwxyz");
        checkSingle(CTFLexer.IDENTIFIER, "_0123456789");
    }
}
