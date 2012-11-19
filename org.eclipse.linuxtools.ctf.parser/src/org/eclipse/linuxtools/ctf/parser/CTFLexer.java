// $ANTLR !Unknown version! CTFLexer.g 2012-10-22 14:14:33

package org.eclipse.linuxtools.ctf.parser;

import org.antlr.runtime.BaseRecognizer;
import org.antlr.runtime.CharStream;
import org.antlr.runtime.DFA;
import org.antlr.runtime.EarlyExitException;
import org.antlr.runtime.Lexer;
import org.antlr.runtime.MismatchedSetException;
import org.antlr.runtime.NoViableAltException;
import org.antlr.runtime.RecognitionException;
import org.antlr.runtime.RecognizerSharedState;

@SuppressWarnings({ "javadoc", "nls", "incomplete-switch" })
public class CTFLexer extends Lexer {
    public static final int SIGN = 50;
    public static final int TERM = 48;
    public static final int BOOLTOK = 26;
    public static final int LT = 40;
    public static final int TYPEDEFTOK = 22;
    public static final int STRING_CONTENT = 71;
    public static final int INTEGER_TYPES_SUFFIX = 54;
    public static final int POINTER = 49;
    public static final int HEX_PREFIX = 58;
    public static final int INTTOK = 13;
    public static final int SEPARATOR = 35;
    public static final int TYPE_ASSIGNMENT = 39;
    public static final int ENUMTOK = 8;
    public static final int COMPLEXTOK = 27;
    public static final int IMAGINARYTOK = 28;
    public static final int DOUBLEQUOTE = 70;
    public static final int STREAMTOK = 17;
    public static final int EOF = -1;
    public static final int LPAREN = 44;
    public static final int INFINITYTOK = 33;
    public static final int STRINGPREFIX = 66;
    public static final int UNSIGNEDTOK = 23;
    public static final int ESCAPE_SEQUENCE = 65;
    public static final int CHAR_CONTENT = 68;
    public static final int RPAREN = 45;
    public static final int UNICODE_ESCAPE = 63;
    public static final int STRING_LITERAL = 72;
    public static final int CALLSITETOK = 31;
    public static final int SINGLEQUOTE = 67;
    public static final int IDENTIFIER = 79;
    public static final int ALIGNTOK = 4;
    public static final int FLOATTOK = 11;
    public static final int COMMENT_CLOSE = 75;
    public static final int STRINGTOK = 18;
    public static final int HEX_LITERAL = 60;
    public static final int DIGIT = 56;
    public static final int COMMENT = 76;
    public static final int DOT = 52;
    public static final int STRUCTTOK = 19;
    public static final int ENVTOK = 29;
    public static final int TYPEALIASTOK = 21;
    public static final int OPENBRAC = 42;
    public static final int FLOATINGPOINTTOK = 10;
    public static final int EVENTTOK = 9;
    public static final int LINE_COMMENT = 77;
    public static final int NINFINITYTOK = 34;
    public static final int VOIDTOK = 25;
    public static final int DOUBLETOK = 7;
    public static final int CHARACTER_LITERAL = 69;
    public static final int OCTAL_LITERAL = 55;
    public static final int COMMENT_OPEN = 74;
    public static final int HEX_DIGIT = 59;
    public static final int OCTAL_ESCAPE = 62;
    public static final int NANNUMBERTOK = 32;
    public static final int LONGTOK = 14;
    public static final int CLOCKTOK = 30;
    public static final int SIGNEDTOK = 16;
    public static final int TRACETOK = 20;
    public static final int COLON = 36;
    public static final int HEXADECIMAL_ESCAPE = 64;
    public static final int CHARTOK = 6;
    public static final int LCURL = 46;
    public static final int WS = 73;
    public static final int INTEGERTOK = 12;
    public static final int VARIANTTOK = 24;
    public static final int ELIPSES = 37;
    public static final int NONDIGIT = 78;
    public static final int RCURL = 47;
    public static final int ARROW = 51;
    public static final int GT = 41;
    public static final int ASSIGNMENT = 38;
    public static final int SHORTTOK = 15;
    public static final int NONZERO_DIGIT = 61;
    public static final int DECIMAL_LITERAL = 57;
    public static final int CONSTTOK = 5;
    public static final int BACKSLASH = 53;
    public static final int CLOSEBRAC = 43;

    // delegates
    // delegators

    public CTFLexer() {
    }

    public CTFLexer(CharStream input) {
        this(input, new RecognizerSharedState());
    }

    public CTFLexer(CharStream input, RecognizerSharedState state) {
        super(input, state);

    }

    @Override
    public String getGrammarFileName() {
        return "CTFLexer.g";
    }

    // $ANTLR start "ALIGNTOK"
    public final void mALIGNTOK() throws RecognitionException {
        try {
            int _type = ALIGNTOK;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // CTFLexer.g:18:18: ( 'align' )
            // CTFLexer.g:18:20: 'align'
            {
                match("align");

            }

            state.type = _type;
            state.channel = _channel;
        } finally {
        }
    }

    // $ANTLR end "ALIGNTOK"

    // $ANTLR start "CONSTTOK"
    public final void mCONSTTOK() throws RecognitionException {
        try {
            int _type = CONSTTOK;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // CTFLexer.g:19:18: ( 'const' )
            // CTFLexer.g:19:20: 'const'
            {
                match("const");

            }

            state.type = _type;
            state.channel = _channel;
        } finally {
        }
    }

    // $ANTLR end "CONSTTOK"

    // $ANTLR start "CHARTOK"
    public final void mCHARTOK() throws RecognitionException {
        try {
            int _type = CHARTOK;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // CTFLexer.g:20:18: ( 'char' )
            // CTFLexer.g:20:20: 'char'
            {
                match("char");

            }

            state.type = _type;
            state.channel = _channel;
        } finally {
        }
    }

    // $ANTLR end "CHARTOK"

    // $ANTLR start "DOUBLETOK"
    public final void mDOUBLETOK() throws RecognitionException {
        try {
            int _type = DOUBLETOK;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // CTFLexer.g:21:18: ( 'double' )
            // CTFLexer.g:21:20: 'double'
            {
                match("double");

            }

            state.type = _type;
            state.channel = _channel;
        } finally {
        }
    }

    // $ANTLR end "DOUBLETOK"

    // $ANTLR start "ENUMTOK"
    public final void mENUMTOK() throws RecognitionException {
        try {
            int _type = ENUMTOK;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // CTFLexer.g:22:18: ( 'enum' )
            // CTFLexer.g:22:20: 'enum'
            {
                match("enum");

            }

            state.type = _type;
            state.channel = _channel;
        } finally {
        }
    }

    // $ANTLR end "ENUMTOK"

    // $ANTLR start "EVENTTOK"
    public final void mEVENTTOK() throws RecognitionException {
        try {
            int _type = EVENTTOK;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // CTFLexer.g:23:18: ( 'event' )
            // CTFLexer.g:23:20: 'event'
            {
                match("event");

            }

            state.type = _type;
            state.channel = _channel;
        } finally {
        }
    }

    // $ANTLR end "EVENTTOK"

    // $ANTLR start "FLOATINGPOINTTOK"
    public final void mFLOATINGPOINTTOK() throws RecognitionException {
        try {
            int _type = FLOATINGPOINTTOK;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // CTFLexer.g:24:18: ( 'floating_point' )
            // CTFLexer.g:24:20: 'floating_point'
            {
                match("floating_point");

            }

            state.type = _type;
            state.channel = _channel;
        } finally {
        }
    }

    // $ANTLR end "FLOATINGPOINTTOK"

    // $ANTLR start "FLOATTOK"
    public final void mFLOATTOK() throws RecognitionException {
        try {
            int _type = FLOATTOK;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // CTFLexer.g:25:18: ( 'float' )
            // CTFLexer.g:25:20: 'float'
            {
                match("float");

            }

            state.type = _type;
            state.channel = _channel;
        } finally {
        }
    }

    // $ANTLR end "FLOATTOK"

    // $ANTLR start "INTEGERTOK"
    public final void mINTEGERTOK() throws RecognitionException {
        try {
            int _type = INTEGERTOK;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // CTFLexer.g:26:18: ( 'integer' )
            // CTFLexer.g:26:20: 'integer'
            {
                match("integer");

            }

            state.type = _type;
            state.channel = _channel;
        } finally {
        }
    }

    // $ANTLR end "INTEGERTOK"

    // $ANTLR start "INTTOK"
    public final void mINTTOK() throws RecognitionException {
        try {
            int _type = INTTOK;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // CTFLexer.g:27:18: ( 'int' )
            // CTFLexer.g:27:20: 'int'
            {
                match("int");

            }

            state.type = _type;
            state.channel = _channel;
        } finally {
        }
    }

    // $ANTLR end "INTTOK"

    // $ANTLR start "LONGTOK"
    public final void mLONGTOK() throws RecognitionException {
        try {
            int _type = LONGTOK;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // CTFLexer.g:28:18: ( 'long' )
            // CTFLexer.g:28:20: 'long'
            {
                match("long");

            }

            state.type = _type;
            state.channel = _channel;
        } finally {
        }
    }

    // $ANTLR end "LONGTOK"

    // $ANTLR start "SHORTTOK"
    public final void mSHORTTOK() throws RecognitionException {
        try {
            int _type = SHORTTOK;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // CTFLexer.g:29:18: ( 'short' )
            // CTFLexer.g:29:20: 'short'
            {
                match("short");

            }

            state.type = _type;
            state.channel = _channel;
        } finally {
        }
    }

    // $ANTLR end "SHORTTOK"

    // $ANTLR start "SIGNEDTOK"
    public final void mSIGNEDTOK() throws RecognitionException {
        try {
            int _type = SIGNEDTOK;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // CTFLexer.g:30:18: ( 'signed' )
            // CTFLexer.g:30:20: 'signed'
            {
                match("signed");

            }

            state.type = _type;
            state.channel = _channel;
        } finally {
        }
    }

    // $ANTLR end "SIGNEDTOK"

    // $ANTLR start "STREAMTOK"
    public final void mSTREAMTOK() throws RecognitionException {
        try {
            int _type = STREAMTOK;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // CTFLexer.g:31:18: ( 'stream' )
            // CTFLexer.g:31:20: 'stream'
            {
                match("stream");

            }

            state.type = _type;
            state.channel = _channel;
        } finally {
        }
    }

    // $ANTLR end "STREAMTOK"

    // $ANTLR start "STRINGTOK"
    public final void mSTRINGTOK() throws RecognitionException {
        try {
            int _type = STRINGTOK;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // CTFLexer.g:32:18: ( 'string' )
            // CTFLexer.g:32:20: 'string'
            {
                match("string");

            }

            state.type = _type;
            state.channel = _channel;
        } finally {
        }
    }

    // $ANTLR end "STRINGTOK"

    // $ANTLR start "STRUCTTOK"
    public final void mSTRUCTTOK() throws RecognitionException {
        try {
            int _type = STRUCTTOK;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // CTFLexer.g:33:18: ( 'struct' )
            // CTFLexer.g:33:20: 'struct'
            {
                match("struct");

            }

            state.type = _type;
            state.channel = _channel;
        } finally {
        }
    }

    // $ANTLR end "STRUCTTOK"

    // $ANTLR start "TRACETOK"
    public final void mTRACETOK() throws RecognitionException {
        try {
            int _type = TRACETOK;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // CTFLexer.g:34:18: ( 'trace' )
            // CTFLexer.g:34:20: 'trace'
            {
                match("trace");

            }

            state.type = _type;
            state.channel = _channel;
        } finally {
        }
    }

    // $ANTLR end "TRACETOK"

    // $ANTLR start "TYPEALIASTOK"
    public final void mTYPEALIASTOK() throws RecognitionException {
        try {
            int _type = TYPEALIASTOK;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // CTFLexer.g:35:18: ( 'typealias' )
            // CTFLexer.g:35:20: 'typealias'
            {
                match("typealias");

            }

            state.type = _type;
            state.channel = _channel;
        } finally {
        }
    }

    // $ANTLR end "TYPEALIASTOK"

    // $ANTLR start "TYPEDEFTOK"
    public final void mTYPEDEFTOK() throws RecognitionException {
        try {
            int _type = TYPEDEFTOK;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // CTFLexer.g:36:18: ( 'typedef' )
            // CTFLexer.g:36:20: 'typedef'
            {
                match("typedef");

            }

            state.type = _type;
            state.channel = _channel;
        } finally {
        }
    }

    // $ANTLR end "TYPEDEFTOK"

    // $ANTLR start "UNSIGNEDTOK"
    public final void mUNSIGNEDTOK() throws RecognitionException {
        try {
            int _type = UNSIGNEDTOK;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // CTFLexer.g:37:18: ( 'unsigned' )
            // CTFLexer.g:37:20: 'unsigned'
            {
                match("unsigned");

            }

            state.type = _type;
            state.channel = _channel;
        } finally {
        }
    }

    // $ANTLR end "UNSIGNEDTOK"

    // $ANTLR start "VARIANTTOK"
    public final void mVARIANTTOK() throws RecognitionException {
        try {
            int _type = VARIANTTOK;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // CTFLexer.g:38:18: ( 'variant' )
            // CTFLexer.g:38:20: 'variant'
            {
                match("variant");

            }

            state.type = _type;
            state.channel = _channel;
        } finally {
        }
    }

    // $ANTLR end "VARIANTTOK"

    // $ANTLR start "VOIDTOK"
    public final void mVOIDTOK() throws RecognitionException {
        try {
            int _type = VOIDTOK;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // CTFLexer.g:39:18: ( 'void' )
            // CTFLexer.g:39:20: 'void'
            {
                match("void");

            }

            state.type = _type;
            state.channel = _channel;
        } finally {
        }
    }

    // $ANTLR end "VOIDTOK"

    // $ANTLR start "BOOLTOK"
    public final void mBOOLTOK() throws RecognitionException {
        try {
            int _type = BOOLTOK;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // CTFLexer.g:40:18: ( '_Bool' )
            // CTFLexer.g:40:20: '_Bool'
            {
                match("_Bool");

            }

            state.type = _type;
            state.channel = _channel;
        } finally {
        }
    }

    // $ANTLR end "BOOLTOK"

    // $ANTLR start "COMPLEXTOK"
    public final void mCOMPLEXTOK() throws RecognitionException {
        try {
            int _type = COMPLEXTOK;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // CTFLexer.g:41:18: ( '_Complex' )
            // CTFLexer.g:41:20: '_Complex'
            {
                match("_Complex");

            }

            state.type = _type;
            state.channel = _channel;
        } finally {
        }
    }

    // $ANTLR end "COMPLEXTOK"

    // $ANTLR start "IMAGINARYTOK"
    public final void mIMAGINARYTOK() throws RecognitionException {
        try {
            int _type = IMAGINARYTOK;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // CTFLexer.g:42:18: ( '_Imaginary' )
            // CTFLexer.g:42:20: '_Imaginary'
            {
                match("_Imaginary");

            }

            state.type = _type;
            state.channel = _channel;
        } finally {
        }
    }

    // $ANTLR end "IMAGINARYTOK"

    // $ANTLR start "ENVTOK"
    public final void mENVTOK() throws RecognitionException {
        try {
            int _type = ENVTOK;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // CTFLexer.g:43:18: ( 'env' )
            // CTFLexer.g:43:20: 'env'
            {
                match("env");

            }

            state.type = _type;
            state.channel = _channel;
        } finally {
        }
    }

    // $ANTLR end "ENVTOK"

    // $ANTLR start "CLOCKTOK"
    public final void mCLOCKTOK() throws RecognitionException {
        try {
            int _type = CLOCKTOK;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // CTFLexer.g:44:18: ( 'clock' )
            // CTFLexer.g:44:20: 'clock'
            {
                match("clock");

            }

            state.type = _type;
            state.channel = _channel;
        } finally {
        }
    }

    // $ANTLR end "CLOCKTOK"

    // $ANTLR start "CALLSITETOK"
    public final void mCALLSITETOK() throws RecognitionException {
        try {
            int _type = CALLSITETOK;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // CTFLexer.g:48:18: ( 'callsite' )
            // CTFLexer.g:48:20: 'callsite'
            {
                match("callsite");

            }

            state.type = _type;
            state.channel = _channel;
        } finally {
        }
    }

    // $ANTLR end "CALLSITETOK"

    // $ANTLR start "NANNUMBERTOK"
    public final void mNANNUMBERTOK() throws RecognitionException {
        try {
            int _type = NANNUMBERTOK;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // CTFLexer.g:54:15: ( 'NaN' )
            // CTFLexer.g:54:17: 'NaN'
            {
                match("NaN");

            }

            state.type = _type;
            state.channel = _channel;
        } finally {
        }
    }

    // $ANTLR end "NANNUMBERTOK"

    // $ANTLR start "INFINITYTOK"
    public final void mINFINITYTOK() throws RecognitionException {
        try {
            int _type = INFINITYTOK;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // CTFLexer.g:55:15: ( '+inf' )
            // CTFLexer.g:55:17: '+inf'
            {
                match("+inf");

            }

            state.type = _type;
            state.channel = _channel;
        } finally {
        }
    }

    // $ANTLR end "INFINITYTOK"

    // $ANTLR start "NINFINITYTOK"
    public final void mNINFINITYTOK() throws RecognitionException {
        try {
            int _type = NINFINITYTOK;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // CTFLexer.g:56:15: ( '-inf' )
            // CTFLexer.g:56:17: '-inf'
            {
                match("-inf");

            }

            state.type = _type;
            state.channel = _channel;
        } finally {
        }
    }

    // $ANTLR end "NINFINITYTOK"

    // $ANTLR start "SEPARATOR"
    public final void mSEPARATOR() throws RecognitionException {
        try {
            int _type = SEPARATOR;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // CTFLexer.g:61:20: ( ',' )
            // CTFLexer.g:61:22: ','
            {
                match(',');

            }

            state.type = _type;
            state.channel = _channel;
        } finally {
        }
    }

    // $ANTLR end "SEPARATOR"

    // $ANTLR start "COLON"
    public final void mCOLON() throws RecognitionException {
        try {
            int _type = COLON;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // CTFLexer.g:62:20: ( ':' )
            // CTFLexer.g:62:22: ':'
            {
                match(':');

            }

            state.type = _type;
            state.channel = _channel;
        } finally {
        }
    }

    // $ANTLR end "COLON"

    // $ANTLR start "ELIPSES"
    public final void mELIPSES() throws RecognitionException {
        try {
            int _type = ELIPSES;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // CTFLexer.g:63:20: ( '...' )
            // CTFLexer.g:63:22: '...'
            {
                match("...");

            }

            state.type = _type;
            state.channel = _channel;
        } finally {
        }
    }

    // $ANTLR end "ELIPSES"

    // $ANTLR start "ASSIGNMENT"
    public final void mASSIGNMENT() throws RecognitionException {
        try {
            int _type = ASSIGNMENT;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // CTFLexer.g:64:20: ( '=' )
            // CTFLexer.g:64:22: '='
            {
                match('=');

            }

            state.type = _type;
            state.channel = _channel;
        } finally {
        }
    }

    // $ANTLR end "ASSIGNMENT"

    // $ANTLR start "TYPE_ASSIGNMENT"
    public final void mTYPE_ASSIGNMENT() throws RecognitionException {
        try {
            int _type = TYPE_ASSIGNMENT;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // CTFLexer.g:65:20: ( ':=' )
            // CTFLexer.g:65:22: ':='
            {
                match(":=");

            }

            state.type = _type;
            state.channel = _channel;
        } finally {
        }
    }

    // $ANTLR end "TYPE_ASSIGNMENT"

    // $ANTLR start "LT"
    public final void mLT() throws RecognitionException {
        try {
            int _type = LT;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // CTFLexer.g:66:20: ( '<' )
            // CTFLexer.g:66:22: '<'
            {
                match('<');

            }

            state.type = _type;
            state.channel = _channel;
        } finally {
        }
    }

    // $ANTLR end "LT"

    // $ANTLR start "GT"
    public final void mGT() throws RecognitionException {
        try {
            int _type = GT;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // CTFLexer.g:67:20: ( '>' )
            // CTFLexer.g:67:22: '>'
            {
                match('>');

            }

            state.type = _type;
            state.channel = _channel;
        } finally {
        }
    }

    // $ANTLR end "GT"

    // $ANTLR start "OPENBRAC"
    public final void mOPENBRAC() throws RecognitionException {
        try {
            int _type = OPENBRAC;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // CTFLexer.g:68:20: ( '[' )
            // CTFLexer.g:68:22: '['
            {
                match('[');

            }

            state.type = _type;
            state.channel = _channel;
        } finally {
        }
    }

    // $ANTLR end "OPENBRAC"

    // $ANTLR start "CLOSEBRAC"
    public final void mCLOSEBRAC() throws RecognitionException {
        try {
            int _type = CLOSEBRAC;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // CTFLexer.g:69:20: ( ']' )
            // CTFLexer.g:69:22: ']'
            {
                match(']');

            }

            state.type = _type;
            state.channel = _channel;
        } finally {
        }
    }

    // $ANTLR end "CLOSEBRAC"

    // $ANTLR start "LPAREN"
    public final void mLPAREN() throws RecognitionException {
        try {
            int _type = LPAREN;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // CTFLexer.g:70:20: ( '(' )
            // CTFLexer.g:70:22: '('
            {
                match('(');

            }

            state.type = _type;
            state.channel = _channel;
        } finally {
        }
    }

    // $ANTLR end "LPAREN"

    // $ANTLR start "RPAREN"
    public final void mRPAREN() throws RecognitionException {
        try {
            int _type = RPAREN;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // CTFLexer.g:71:20: ( ')' )
            // CTFLexer.g:71:22: ')'
            {
                match(')');

            }

            state.type = _type;
            state.channel = _channel;
        } finally {
        }
    }

    // $ANTLR end "RPAREN"

    // $ANTLR start "LCURL"
    public final void mLCURL() throws RecognitionException {
        try {
            int _type = LCURL;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // CTFLexer.g:72:20: ( '{' )
            // CTFLexer.g:72:22: '{'
            {
                match('{');

            }

            state.type = _type;
            state.channel = _channel;
        } finally {
        }
    }

    // $ANTLR end "LCURL"

    // $ANTLR start "RCURL"
    public final void mRCURL() throws RecognitionException {
        try {
            int _type = RCURL;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // CTFLexer.g:73:20: ( '}' )
            // CTFLexer.g:73:22: '}'
            {
                match('}');

            }

            state.type = _type;
            state.channel = _channel;
        } finally {
        }
    }

    // $ANTLR end "RCURL"

    // $ANTLR start "TERM"
    public final void mTERM() throws RecognitionException {
        try {
            int _type = TERM;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // CTFLexer.g:74:20: ( ';' )
            // CTFLexer.g:74:22: ';'
            {
                match(';');

            }

            state.type = _type;
            state.channel = _channel;
        } finally {
        }
    }

    // $ANTLR end "TERM"

    // $ANTLR start "POINTER"
    public final void mPOINTER() throws RecognitionException {
        try {
            int _type = POINTER;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // CTFLexer.g:75:20: ( '*' )
            // CTFLexer.g:75:22: '*'
            {
                match('*');

            }

            state.type = _type;
            state.channel = _channel;
        } finally {
        }
    }

    // $ANTLR end "POINTER"

    // $ANTLR start "SIGN"
    public final void mSIGN() throws RecognitionException {
        try {
            int _type = SIGN;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // CTFLexer.g:76:20: ( '+' | '-' )
            // CTFLexer.g:
            {
                if (input.LA(1) == '+' || input.LA(1) == '-') {
                    input.consume();

                } else {
                    MismatchedSetException mse = new MismatchedSetException(
                            null, input);
                    recover(mse);
                    throw mse;
                }

            }

            state.type = _type;
            state.channel = _channel;
        } finally {
        }
    }

    // $ANTLR end "SIGN"

    // $ANTLR start "ARROW"
    public final void mARROW() throws RecognitionException {
        try {
            int _type = ARROW;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // CTFLexer.g:77:20: ( '->' )
            // CTFLexer.g:77:22: '->'
            {
                match("->");

            }

            state.type = _type;
            state.channel = _channel;
        } finally {
        }
    }

    // $ANTLR end "ARROW"

    // $ANTLR start "DOT"
    public final void mDOT() throws RecognitionException {
        try {
            int _type = DOT;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // CTFLexer.g:78:20: ( '.' )
            // CTFLexer.g:78:22: '.'
            {
                match('.');

            }

            state.type = _type;
            state.channel = _channel;
        } finally {
        }
    }

    // $ANTLR end "DOT"

    // $ANTLR start "BACKSLASH"
    public final void mBACKSLASH() throws RecognitionException {
        try {
            // CTFLexer.g:79:20: ( '\\\\' )
            // CTFLexer.g:79:22: '\\\\'
            {
                match('\\');

            }

        } finally {
        }
    }

    // $ANTLR end "BACKSLASH"

    // $ANTLR start "OCTAL_LITERAL"
    public final void mOCTAL_LITERAL() throws RecognitionException {
        try {
            int _type = OCTAL_LITERAL;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // CTFLexer.g:94:15: ( '0' ( '0' .. '7' )+ ( INTEGER_TYPES_SUFFIX )?
            // )
            // CTFLexer.g:94:17: '0' ( '0' .. '7' )+ ( INTEGER_TYPES_SUFFIX )?
            {
                match('0');
                // CTFLexer.g:94:21: ( '0' .. '7' )+
                int cnt1 = 0;
                loop1: do {
                    int alt1 = 2;
                    int LA1_0 = input.LA(1);

                    if (((LA1_0 >= '0' && LA1_0 <= '7'))) {
                        alt1 = 1;
                    }

                    switch (alt1) {
                    case 1:
                    // CTFLexer.g:94:22: '0' .. '7'
                    {
                        matchRange('0', '7');

                    }
                        break;

                    default:
                        if (cnt1 >= 1) {
                            break loop1;
                        }
                        EarlyExitException eee = new EarlyExitException(1,
                                input);
                        throw eee;
                    }
                    cnt1++;
                } while (true);

                // CTFLexer.g:94:33: ( INTEGER_TYPES_SUFFIX )?
                int alt2 = 2;
                int LA2_0 = input.LA(1);

                if ((LA2_0 == 'L' || LA2_0 == 'U' || LA2_0 == 'l' || LA2_0 == 'u')) {
                    alt2 = 1;
                }
                switch (alt2) {
                case 1:
                // CTFLexer.g:94:33: INTEGER_TYPES_SUFFIX
                {
                    mINTEGER_TYPES_SUFFIX();

                }
                    break;

                }

            }

            state.type = _type;
            state.channel = _channel;
        } finally {
        }
    }

    // $ANTLR end "OCTAL_LITERAL"

    // $ANTLR start "DECIMAL_LITERAL"
    public final void mDECIMAL_LITERAL() throws RecognitionException {
        try {
            int _type = DECIMAL_LITERAL;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // CTFLexer.g:96:17: ( ( DIGIT )+ ( INTEGER_TYPES_SUFFIX )? )
            // CTFLexer.g:96:19: ( DIGIT )+ ( INTEGER_TYPES_SUFFIX )?
            {
                // CTFLexer.g:96:19: ( DIGIT )+
                int cnt3 = 0;
                loop3: do {
                    int alt3 = 2;
                    int LA3_0 = input.LA(1);

                    if (((LA3_0 >= '0' && LA3_0 <= '9'))) {
                        alt3 = 1;
                    }

                    switch (alt3) {
                    case 1:
                    // CTFLexer.g:96:19: DIGIT
                    {
                        mDIGIT();

                    }
                        break;

                    default:
                        if (cnt3 >= 1) {
                            break loop3;
                        }
                        EarlyExitException eee = new EarlyExitException(3,
                                input);
                        throw eee;
                    }
                    cnt3++;
                } while (true);

                // CTFLexer.g:96:26: ( INTEGER_TYPES_SUFFIX )?
                int alt4 = 2;
                int LA4_0 = input.LA(1);

                if ((LA4_0 == 'L' || LA4_0 == 'U' || LA4_0 == 'l' || LA4_0 == 'u')) {
                    alt4 = 1;
                }
                switch (alt4) {
                case 1:
                // CTFLexer.g:96:26: INTEGER_TYPES_SUFFIX
                {
                    mINTEGER_TYPES_SUFFIX();

                }
                    break;

                }

            }

            state.type = _type;
            state.channel = _channel;
        } finally {
        }
    }

    // $ANTLR end "DECIMAL_LITERAL"

    // $ANTLR start "HEX_LITERAL"
    public final void mHEX_LITERAL() throws RecognitionException {
        try {
            int _type = HEX_LITERAL;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // CTFLexer.g:98:13: ( HEX_PREFIX ( HEX_DIGIT )+ (
            // INTEGER_TYPES_SUFFIX )? )
            // CTFLexer.g:98:15: HEX_PREFIX ( HEX_DIGIT )+ (
            // INTEGER_TYPES_SUFFIX )?
            {
                mHEX_PREFIX();
                // CTFLexer.g:98:26: ( HEX_DIGIT )+
                int cnt5 = 0;
                loop5: do {
                    int alt5 = 2;
                    int LA5_0 = input.LA(1);

                    if (((LA5_0 >= '0' && LA5_0 <= '9')
                            || (LA5_0 >= 'A' && LA5_0 <= 'F') || (LA5_0 >= 'a' && LA5_0 <= 'f'))) {
                        alt5 = 1;
                    }

                    switch (alt5) {
                    case 1:
                    // CTFLexer.g:98:26: HEX_DIGIT
                    {
                        mHEX_DIGIT();

                    }
                        break;

                    default:
                        if (cnt5 >= 1) {
                            break loop5;
                        }
                        EarlyExitException eee = new EarlyExitException(5,
                                input);
                        throw eee;
                    }
                    cnt5++;
                } while (true);

                // CTFLexer.g:98:37: ( INTEGER_TYPES_SUFFIX )?
                int alt6 = 2;
                int LA6_0 = input.LA(1);

                if ((LA6_0 == 'L' || LA6_0 == 'U' || LA6_0 == 'l' || LA6_0 == 'u')) {
                    alt6 = 1;
                }
                switch (alt6) {
                case 1:
                // CTFLexer.g:98:37: INTEGER_TYPES_SUFFIX
                {
                    mINTEGER_TYPES_SUFFIX();

                }
                    break;

                }

            }

            state.type = _type;
            state.channel = _channel;
        } finally {
        }
    }

    // $ANTLR end "HEX_LITERAL"

    // $ANTLR start "HEX_DIGIT"
    public final void mHEX_DIGIT() throws RecognitionException {
        try {
            // CTFLexer.g:99:20: ( DIGIT | ( 'a' .. 'f' ) | ( 'A' .. 'F' ) )
            int alt7 = 3;
            switch (input.LA(1)) {
            case '0':
            case '1':
            case '2':
            case '3':
            case '4':
            case '5':
            case '6':
            case '7':
            case '8':
            case '9': {
                alt7 = 1;
            }
                break;
            case 'a':
            case 'b':
            case 'c':
            case 'd':
            case 'e':
            case 'f': {
                alt7 = 2;
            }
                break;
            case 'A':
            case 'B':
            case 'C':
            case 'D':
            case 'E':
            case 'F': {
                alt7 = 3;
            }
                break;
            default:
                NoViableAltException nvae = new NoViableAltException("", 7, 0,
                        input);

                throw nvae;
            }

            switch (alt7) {
            case 1:
            // CTFLexer.g:99:22: DIGIT
            {
                mDIGIT();

            }
                break;
            case 2:
            // CTFLexer.g:99:30: ( 'a' .. 'f' )
            {
                // CTFLexer.g:99:30: ( 'a' .. 'f' )
                // CTFLexer.g:99:31: 'a' .. 'f'
                {
                    matchRange('a', 'f');

                }

            }
                break;
            case 3:
            // CTFLexer.g:99:43: ( 'A' .. 'F' )
            {
                // CTFLexer.g:99:43: ( 'A' .. 'F' )
                // CTFLexer.g:99:44: 'A' .. 'F'
                {
                    matchRange('A', 'F');

                }

            }
                break;

            }
        } finally {
        }
    }

    // $ANTLR end "HEX_DIGIT"

    // $ANTLR start "HEX_PREFIX"
    public final void mHEX_PREFIX() throws RecognitionException {
        try {
            // CTFLexer.g:100:21: ( '0' ( 'x' | 'X' ) )
            // CTFLexer.g:100:23: '0' ( 'x' | 'X' )
            {
                match('0');
                if (input.LA(1) == 'X' || input.LA(1) == 'x') {
                    input.consume();

                } else {
                    MismatchedSetException mse = new MismatchedSetException(
                            null, input);
                    recover(mse);
                    throw mse;
                }

            }

        } finally {
        }
    }

    // $ANTLR end "HEX_PREFIX"

    // $ANTLR start "DIGIT"
    public final void mDIGIT() throws RecognitionException {
        try {
            // CTFLexer.g:103:16: ( '0' .. '9' )
            // CTFLexer.g:103:18: '0' .. '9'
            {
                matchRange('0', '9');

            }

        } finally {
        }
    }

    // $ANTLR end "DIGIT"

    // $ANTLR start "NONZERO_DIGIT"
    public final void mNONZERO_DIGIT() throws RecognitionException {
        try {
            // CTFLexer.g:104:24: ( '1' .. '9' )
            // CTFLexer.g:104:26: '1' .. '9'
            {
                matchRange('1', '9');

            }

        } finally {
        }
    }

    // $ANTLR end "NONZERO_DIGIT"

    // $ANTLR start "INTEGER_TYPES_SUFFIX"
    public final void mINTEGER_TYPES_SUFFIX() throws RecognitionException {
        try {
            // CTFLexer.g:112:31: ( ( 'l' ( 'l' )? | 'L' ( 'L' )? ) | ( 'u' |
            // 'U' ) | ( 'u' | 'U' ) ( 'l' ( 'l' )? | 'L' ( 'L' )? ) | ( 'l' (
            // 'l' )? | 'L' ( 'L' )? ) ( 'u' | 'U' ) )
            int alt17 = 4;
            alt17 = dfa17.predict(input);
            switch (alt17) {
            case 1:
            // CTFLexer.g:113:4: ( 'l' ( 'l' )? | 'L' ( 'L' )? )
            {
                // CTFLexer.g:113:4: ( 'l' ( 'l' )? | 'L' ( 'L' )? )
                int alt10 = 2;
                int LA10_0 = input.LA(1);

                if ((LA10_0 == 'l')) {
                    alt10 = 1;
                } else if ((LA10_0 == 'L')) {
                    alt10 = 2;
                } else {
                    NoViableAltException nvae = new NoViableAltException("",
                            10, 0, input);

                    throw nvae;
                }
                switch (alt10) {
                case 1:
                // CTFLexer.g:113:5: 'l' ( 'l' )?
                {
                    match('l');
                    // CTFLexer.g:113:9: ( 'l' )?
                    int alt8 = 2;
                    int LA8_0 = input.LA(1);

                    if ((LA8_0 == 'l')) {
                        alt8 = 1;
                    }
                    switch (alt8) {
                    case 1:
                    // CTFLexer.g:113:10: 'l'
                    {
                        match('l');

                    }
                        break;

                    }

                }
                    break;
                case 2:
                // CTFLexer.g:113:18: 'L' ( 'L' )?
                {
                    match('L');
                    // CTFLexer.g:113:22: ( 'L' )?
                    int alt9 = 2;
                    int LA9_0 = input.LA(1);

                    if ((LA9_0 == 'L')) {
                        alt9 = 1;
                    }
                    switch (alt9) {
                    case 1:
                    // CTFLexer.g:113:23: 'L'
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
            case 2:
            // CTFLexer.g:114:4: ( 'u' | 'U' )
            {
                if (input.LA(1) == 'U' || input.LA(1) == 'u') {
                    input.consume();

                } else {
                    MismatchedSetException mse = new MismatchedSetException(
                            null, input);
                    recover(mse);
                    throw mse;
                }

            }
                break;
            case 3:
            // CTFLexer.g:115:4: ( 'u' | 'U' ) ( 'l' ( 'l' )? | 'L' ( 'L' )? )
            {
                if (input.LA(1) == 'U' || input.LA(1) == 'u') {
                    input.consume();

                } else {
                    MismatchedSetException mse = new MismatchedSetException(
                            null, input);
                    recover(mse);
                    throw mse;
                }

                // CTFLexer.g:115:16: ( 'l' ( 'l' )? | 'L' ( 'L' )? )
                int alt13 = 2;
                int LA13_0 = input.LA(1);

                if ((LA13_0 == 'l')) {
                    alt13 = 1;
                } else if ((LA13_0 == 'L')) {
                    alt13 = 2;
                } else {
                    NoViableAltException nvae = new NoViableAltException("",
                            13, 0, input);

                    throw nvae;
                }
                switch (alt13) {
                case 1:
                // CTFLexer.g:115:17: 'l' ( 'l' )?
                {
                    match('l');
                    // CTFLexer.g:115:21: ( 'l' )?
                    int alt11 = 2;
                    int LA11_0 = input.LA(1);

                    if ((LA11_0 == 'l')) {
                        alt11 = 1;
                    }
                    switch (alt11) {
                    case 1:
                    // CTFLexer.g:115:22: 'l'
                    {
                        match('l');

                    }
                        break;

                    }

                }
                    break;
                case 2:
                // CTFLexer.g:115:30: 'L' ( 'L' )?
                {
                    match('L');
                    // CTFLexer.g:115:34: ( 'L' )?
                    int alt12 = 2;
                    int LA12_0 = input.LA(1);

                    if ((LA12_0 == 'L')) {
                        alt12 = 1;
                    }
                    switch (alt12) {
                    case 1:
                    // CTFLexer.g:115:35: 'L'
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
            case 4:
            // CTFLexer.g:116:4: ( 'l' ( 'l' )? | 'L' ( 'L' )? ) ( 'u' | 'U' )
            {
                // CTFLexer.g:116:4: ( 'l' ( 'l' )? | 'L' ( 'L' )? )
                int alt16 = 2;
                int LA16_0 = input.LA(1);

                if ((LA16_0 == 'l')) {
                    alt16 = 1;
                } else if ((LA16_0 == 'L')) {
                    alt16 = 2;
                } else {
                    NoViableAltException nvae = new NoViableAltException("",
                            16, 0, input);

                    throw nvae;
                }
                switch (alt16) {
                case 1:
                // CTFLexer.g:116:5: 'l' ( 'l' )?
                {
                    match('l');
                    // CTFLexer.g:116:9: ( 'l' )?
                    int alt14 = 2;
                    int LA14_0 = input.LA(1);

                    if ((LA14_0 == 'l')) {
                        alt14 = 1;
                    }
                    switch (alt14) {
                    case 1:
                    // CTFLexer.g:116:10: 'l'
                    {
                        match('l');

                    }
                        break;

                    }

                }
                    break;
                case 2:
                // CTFLexer.g:116:18: 'L' ( 'L' )?
                {
                    match('L');
                    // CTFLexer.g:116:22: ( 'L' )?
                    int alt15 = 2;
                    int LA15_0 = input.LA(1);

                    if ((LA15_0 == 'L')) {
                        alt15 = 1;
                    }
                    switch (alt15) {
                    case 1:
                    // CTFLexer.g:116:23: 'L'
                    {
                        match('L');

                    }
                        break;

                    }

                }
                    break;

                }

                if (input.LA(1) == 'U' || input.LA(1) == 'u') {
                    input.consume();

                } else {
                    MismatchedSetException mse = new MismatchedSetException(
                            null, input);
                    recover(mse);
                    throw mse;
                }

            }
                break;

            }
        } finally {
        }
    }

    // $ANTLR end "INTEGER_TYPES_SUFFIX"

    // $ANTLR start "ESCAPE_SEQUENCE"
    public final void mESCAPE_SEQUENCE() throws RecognitionException {
        try {
            // CTFLexer.g:122:26: ( BACKSLASH ( '\\'' | '\"' | '?' | BACKSLASH |
            // 'a' | 'b' | 'f' | 'n' | 'r' | 't' | 'v' ) | OCTAL_ESCAPE |
            // UNICODE_ESCAPE | HEXADECIMAL_ESCAPE )
            int alt18 = 4;
            int LA18_0 = input.LA(1);

            if ((LA18_0 == '\\')) {
                switch (input.LA(2)) {
                case 'x': {
                    alt18 = 4;
                }
                    break;
                case '0':
                case '1':
                case '2':
                case '3':
                case '4':
                case '5':
                case '6':
                case '7': {
                    alt18 = 2;
                }
                    break;
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
                case 'v': {
                    alt18 = 1;
                }
                    break;
                case 'U':
                case 'u': {
                    alt18 = 3;
                }
                    break;
                default:
                    NoViableAltException nvae = new NoViableAltException("",
                            18, 1, input);

                    throw nvae;
                }

            } else {
                NoViableAltException nvae = new NoViableAltException("", 18, 0,
                        input);

                throw nvae;
            }
            switch (alt18) {
            case 1:
            // CTFLexer.g:123:4: BACKSLASH ( '\\'' | '\"' | '?' | BACKSLASH |
            // 'a' | 'b' | 'f' | 'n' | 'r' | 't' | 'v' )
            {
                mBACKSLASH();
                if (input.LA(1) == '\"' || input.LA(1) == '\''
                        || input.LA(1) == '?' || input.LA(1) == '\\'
                        || (input.LA(1) >= 'a' && input.LA(1) <= 'b')
                        || input.LA(1) == 'f' || input.LA(1) == 'n'
                        || input.LA(1) == 'r' || input.LA(1) == 't'
                        || input.LA(1) == 'v') {
                    input.consume();

                } else {
                    MismatchedSetException mse = new MismatchedSetException(
                            null, input);
                    recover(mse);
                    throw mse;
                }

            }
                break;
            case 2:
            // CTFLexer.g:124:4: OCTAL_ESCAPE
            {
                mOCTAL_ESCAPE();

            }
                break;
            case 3:
            // CTFLexer.g:125:4: UNICODE_ESCAPE
            {
                mUNICODE_ESCAPE();

            }
                break;
            case 4:
            // CTFLexer.g:126:4: HEXADECIMAL_ESCAPE
            {
                mHEXADECIMAL_ESCAPE();

            }
                break;

            }
        } finally {
        }
    }

    // $ANTLR end "ESCAPE_SEQUENCE"

    // $ANTLR start "OCTAL_ESCAPE"
    public final void mOCTAL_ESCAPE() throws RecognitionException {
        try {
            // CTFLexer.g:132:23: ( BACKSLASH ( '0' .. '3' ) ( '0' .. '7' ) (
            // '0' .. '7' ) | BACKSLASH ( '0' .. '7' ) ( '0' .. '7' ) |
            // BACKSLASH ( '0' .. '7' ) )
            int alt19 = 3;
            int LA19_0 = input.LA(1);

            if ((LA19_0 == '\\')) {
                int LA19_1 = input.LA(2);

                if (((LA19_1 >= '0' && LA19_1 <= '3'))) {
                    int LA19_2 = input.LA(3);

                    if (((LA19_2 >= '0' && LA19_2 <= '7'))) {
                        int LA19_4 = input.LA(4);

                        if (((LA19_4 >= '0' && LA19_4 <= '7'))) {
                            alt19 = 1;
                        } else {
                            alt19 = 2;
                        }
                    } else {
                        alt19 = 3;
                    }
                } else if (((LA19_1 >= '4' && LA19_1 <= '7'))) {
                    int LA19_3 = input.LA(3);

                    if (((LA19_3 >= '0' && LA19_3 <= '7'))) {
                        alt19 = 2;
                    } else {
                        alt19 = 3;
                    }
                } else {
                    NoViableAltException nvae = new NoViableAltException("",
                            19, 1, input);

                    throw nvae;
                }
            } else {
                NoViableAltException nvae = new NoViableAltException("", 19, 0,
                        input);

                throw nvae;
            }
            switch (alt19) {
            case 1:
            // CTFLexer.g:133:5: BACKSLASH ( '0' .. '3' ) ( '0' .. '7' ) ( '0'
            // .. '7' )
            {
                mBACKSLASH();
                // CTFLexer.g:133:15: ( '0' .. '3' )
                // CTFLexer.g:133:16: '0' .. '3'
                {
                    matchRange('0', '3');

                }

                // CTFLexer.g:133:26: ( '0' .. '7' )
                // CTFLexer.g:133:27: '0' .. '7'
                {
                    matchRange('0', '7');

                }

                // CTFLexer.g:133:37: ( '0' .. '7' )
                // CTFLexer.g:133:38: '0' .. '7'
                {
                    matchRange('0', '7');

                }

            }
                break;
            case 2:
            // CTFLexer.g:134:5: BACKSLASH ( '0' .. '7' ) ( '0' .. '7' )
            {
                mBACKSLASH();
                // CTFLexer.g:134:15: ( '0' .. '7' )
                // CTFLexer.g:134:16: '0' .. '7'
                {
                    matchRange('0', '7');

                }

                // CTFLexer.g:134:26: ( '0' .. '7' )
                // CTFLexer.g:134:27: '0' .. '7'
                {
                    matchRange('0', '7');

                }

            }
                break;
            case 3:
            // CTFLexer.g:135:5: BACKSLASH ( '0' .. '7' )
            {
                mBACKSLASH();
                // CTFLexer.g:135:15: ( '0' .. '7' )
                // CTFLexer.g:135:16: '0' .. '7'
                {
                    matchRange('0', '7');

                }

            }
                break;

            }
        } finally {
        }
    }

    // $ANTLR end "OCTAL_ESCAPE"

    // $ANTLR start "HEXADECIMAL_ESCAPE"
    public final void mHEXADECIMAL_ESCAPE() throws RecognitionException {
        try {
            // CTFLexer.g:141:29: ( BACKSLASH 'x' ( HEX_DIGIT )+ )
            // CTFLexer.g:141:31: BACKSLASH 'x' ( HEX_DIGIT )+
            {
                mBACKSLASH();
                match('x');
                // CTFLexer.g:141:45: ( HEX_DIGIT )+
                int cnt20 = 0;
                loop20: do {
                    int alt20 = 2;
                    int LA20_0 = input.LA(1);

                    if (((LA20_0 >= '0' && LA20_0 <= '9')
                            || (LA20_0 >= 'A' && LA20_0 <= 'F') || (LA20_0 >= 'a' && LA20_0 <= 'f'))) {
                        alt20 = 1;
                    }

                    switch (alt20) {
                    case 1:
                    // CTFLexer.g:141:45: HEX_DIGIT
                    {
                        mHEX_DIGIT();

                    }
                        break;

                    default:
                        if (cnt20 >= 1) {
                            break loop20;
                        }
                        EarlyExitException eee = new EarlyExitException(20,
                                input);
                        throw eee;
                    }
                    cnt20++;
                } while (true);

            }

        } finally {
        }
    }

    // $ANTLR end "HEXADECIMAL_ESCAPE"

    // $ANTLR start "UNICODE_ESCAPE"
    public final void mUNICODE_ESCAPE() throws RecognitionException {
        try {
            // CTFLexer.g:146:25: ( BACKSLASH 'u' HEX_DIGIT HEX_DIGIT HEX_DIGIT
            // HEX_DIGIT | BACKSLASH 'U' HEX_DIGIT HEX_DIGIT HEX_DIGIT HEX_DIGIT
            // HEX_DIGIT HEX_DIGIT HEX_DIGIT HEX_DIGIT )
            int alt21 = 2;
            int LA21_0 = input.LA(1);

            if ((LA21_0 == '\\')) {
                int LA21_1 = input.LA(2);

                if ((LA21_1 == 'U')) {
                    alt21 = 2;
                } else if ((LA21_1 == 'u')) {
                    alt21 = 1;
                } else {
                    NoViableAltException nvae = new NoViableAltException("",
                            21, 1, input);

                    throw nvae;
                }
            } else {
                NoViableAltException nvae = new NoViableAltException("", 21, 0,
                        input);

                throw nvae;
            }
            switch (alt21) {
            case 1:
            // CTFLexer.g:147:5: BACKSLASH 'u' HEX_DIGIT HEX_DIGIT HEX_DIGIT
            // HEX_DIGIT
            {
                mBACKSLASH();
                match('u');
                mHEX_DIGIT();
                mHEX_DIGIT();
                mHEX_DIGIT();
                mHEX_DIGIT();

            }
                break;
            case 2:
            // CTFLexer.g:148:5: BACKSLASH 'U' HEX_DIGIT HEX_DIGIT HEX_DIGIT
            // HEX_DIGIT HEX_DIGIT HEX_DIGIT HEX_DIGIT HEX_DIGIT
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
        } finally {
        }
    }

    // $ANTLR end "UNICODE_ESCAPE"

    // $ANTLR start "STRINGPREFIX"
    public final void mSTRINGPREFIX() throws RecognitionException {
        try {
            // CTFLexer.g:153:23: ( 'L' )
            // CTFLexer.g:153:25: 'L'
            {
                match('L');

            }

        } finally {
        }
    }

    // $ANTLR end "STRINGPREFIX"

    // $ANTLR start "CHARACTER_LITERAL"
    public final void mCHARACTER_LITERAL() throws RecognitionException {
        try {
            int _type = CHARACTER_LITERAL;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // CTFLexer.g:158:19: ( ( STRINGPREFIX )? SINGLEQUOTE ( CHAR_CONTENT
            // )+ SINGLEQUOTE )
            // CTFLexer.g:158:21: ( STRINGPREFIX )? SINGLEQUOTE ( CHAR_CONTENT
            // )+ SINGLEQUOTE
            {
                // CTFLexer.g:158:21: ( STRINGPREFIX )?
                int alt22 = 2;
                int LA22_0 = input.LA(1);

                if ((LA22_0 == 'L')) {
                    alt22 = 1;
                }
                switch (alt22) {
                case 1:
                // CTFLexer.g:158:21: STRINGPREFIX
                {
                    mSTRINGPREFIX();

                }
                    break;

                }

                mSINGLEQUOTE();
                // CTFLexer.g:158:47: ( CHAR_CONTENT )+
                int cnt23 = 0;
                loop23: do {
                    int alt23 = 2;
                    int LA23_0 = input.LA(1);

                    if (((LA23_0 >= '\u0000' && LA23_0 <= '&') || (LA23_0 >= '(' && LA23_0 <= '\uFFFF'))) {
                        alt23 = 1;
                    }

                    switch (alt23) {
                    case 1:
                    // CTFLexer.g:158:47: CHAR_CONTENT
                    {
                        mCHAR_CONTENT();

                    }
                        break;

                    default:
                        if (cnt23 >= 1) {
                            break loop23;
                        }
                        EarlyExitException eee = new EarlyExitException(23,
                                input);
                        throw eee;
                    }
                    cnt23++;
                } while (true);

                mSINGLEQUOTE();

            }

            state.type = _type;
            state.channel = _channel;
        } finally {
        }
    }

    // $ANTLR end "CHARACTER_LITERAL"

    // $ANTLR start "CHAR_CONTENT"
    public final void mCHAR_CONTENT() throws RecognitionException {
        try {
            // CTFLexer.g:159:23: ( ( ESCAPE_SEQUENCE | ~ ( BACKSLASH |
            // SINGLEQUOTE ) ) )
            // CTFLexer.g:159:25: ( ESCAPE_SEQUENCE | ~ ( BACKSLASH |
            // SINGLEQUOTE ) )
            {
                // CTFLexer.g:159:25: ( ESCAPE_SEQUENCE | ~ ( BACKSLASH |
                // SINGLEQUOTE ) )
                int alt24 = 2;
                int LA24_0 = input.LA(1);

                if ((LA24_0 == '\\')) {
                    alt24 = 1;
                } else if (((LA24_0 >= '\u0000' && LA24_0 <= '&')
                        || (LA24_0 >= '(' && LA24_0 <= '[') || (LA24_0 >= ']' && LA24_0 <= '\uFFFF'))) {
                    alt24 = 2;
                } else {
                    NoViableAltException nvae = new NoViableAltException("",
                            24, 0, input);

                    throw nvae;
                }
                switch (alt24) {
                case 1:
                // CTFLexer.g:159:26: ESCAPE_SEQUENCE
                {
                    mESCAPE_SEQUENCE();

                }
                    break;
                case 2:
                // CTFLexer.g:159:44: ~ ( BACKSLASH | SINGLEQUOTE )
                {
                    if ((input.LA(1) >= '\u0000' && input.LA(1) <= '&')
                            || (input.LA(1) >= '(' && input.LA(1) <= '[')
                            || (input.LA(1) >= ']' && input.LA(1) <= '\uFFFF')) {
                        input.consume();

                    } else {
                        MismatchedSetException mse = new MismatchedSetException(
                                null, input);
                        recover(mse);
                        throw mse;
                    }

                }
                    break;

                }

            }

        } finally {
        }
    }

    // $ANTLR end "CHAR_CONTENT"

    // $ANTLR start "SINGLEQUOTE"
    public final void mSINGLEQUOTE() throws RecognitionException {
        try {
            // CTFLexer.g:160:22: ( '\\'' )
            // CTFLexer.g:160:24: '\\''
            {
                match('\'');

            }

        } finally {
        }
    }

    // $ANTLR end "SINGLEQUOTE"

    // $ANTLR start "STRING_LITERAL"
    public final void mSTRING_LITERAL() throws RecognitionException {
        try {
            int _type = STRING_LITERAL;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // CTFLexer.g:165:16: ( ( STRINGPREFIX )? DOUBLEQUOTE (
            // STRING_CONTENT )* DOUBLEQUOTE )
            // CTFLexer.g:165:18: ( STRINGPREFIX )? DOUBLEQUOTE ( STRING_CONTENT
            // )* DOUBLEQUOTE
            {
                // CTFLexer.g:165:18: ( STRINGPREFIX )?
                int alt25 = 2;
                int LA25_0 = input.LA(1);

                if ((LA25_0 == 'L')) {
                    alt25 = 1;
                }
                switch (alt25) {
                case 1:
                // CTFLexer.g:165:18: STRINGPREFIX
                {
                    mSTRINGPREFIX();

                }
                    break;

                }

                mDOUBLEQUOTE();
                // CTFLexer.g:165:44: ( STRING_CONTENT )*
                loop26: do {
                    int alt26 = 2;
                    int LA26_0 = input.LA(1);

                    if (((LA26_0 >= '\u0000' && LA26_0 <= '!') || (LA26_0 >= '#' && LA26_0 <= '\uFFFF'))) {
                        alt26 = 1;
                    }

                    switch (alt26) {
                    case 1:
                    // CTFLexer.g:165:44: STRING_CONTENT
                    {
                        mSTRING_CONTENT();

                    }
                        break;

                    default:
                        break loop26;
                    }
                } while (true);

                mDOUBLEQUOTE();

            }

            state.type = _type;
            state.channel = _channel;
        } finally {
        }
    }

    // $ANTLR end "STRING_LITERAL"

    // $ANTLR start "STRING_CONTENT"
    public final void mSTRING_CONTENT() throws RecognitionException {
        try {
            // CTFLexer.g:166:25: ( ( ESCAPE_SEQUENCE | ~ ( BACKSLASH |
            // DOUBLEQUOTE ) ) )
            // CTFLexer.g:166:27: ( ESCAPE_SEQUENCE | ~ ( BACKSLASH |
            // DOUBLEQUOTE ) )
            {
                // CTFLexer.g:166:27: ( ESCAPE_SEQUENCE | ~ ( BACKSLASH |
                // DOUBLEQUOTE ) )
                int alt27 = 2;
                int LA27_0 = input.LA(1);

                if ((LA27_0 == '\\')) {
                    alt27 = 1;
                } else if (((LA27_0 >= '\u0000' && LA27_0 <= '!')
                        || (LA27_0 >= '#' && LA27_0 <= '[') || (LA27_0 >= ']' && LA27_0 <= '\uFFFF'))) {
                    alt27 = 2;
                } else {
                    NoViableAltException nvae = new NoViableAltException("",
                            27, 0, input);

                    throw nvae;
                }
                switch (alt27) {
                case 1:
                // CTFLexer.g:166:28: ESCAPE_SEQUENCE
                {
                    mESCAPE_SEQUENCE();

                }
                    break;
                case 2:
                // CTFLexer.g:166:46: ~ ( BACKSLASH | DOUBLEQUOTE )
                {
                    if ((input.LA(1) >= '\u0000' && input.LA(1) <= '!')
                            || (input.LA(1) >= '#' && input.LA(1) <= '[')
                            || (input.LA(1) >= ']' && input.LA(1) <= '\uFFFF')) {
                        input.consume();

                    } else {
                        MismatchedSetException mse = new MismatchedSetException(
                                null, input);
                        recover(mse);
                        throw mse;
                    }

                }
                    break;

                }

            }

        } finally {
        }
    }

    // $ANTLR end "STRING_CONTENT"

    // $ANTLR start "DOUBLEQUOTE"
    public final void mDOUBLEQUOTE() throws RecognitionException {
        try {
            // CTFLexer.g:167:22: ( '\"' )
            // CTFLexer.g:167:24: '\"'
            {
                match('\"');

            }

        } finally {
        }
    }

    // $ANTLR end "DOUBLEQUOTE"

    // $ANTLR start "WS"
    public final void mWS() throws RecognitionException {
        try {
            int _type = WS;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // CTFLexer.g:172:4: ( ( ' ' | '\\r' | '\\t' | '\ ' | '\\n' ) )
            // CTFLexer.g:172:6: ( ' ' | '\\r' | '\\t' | '\ ' | '\\n' )
            {
                if ((input.LA(1) >= '\t' && input.LA(1) <= '\n')
                        || (input.LA(1) >= '\f' && input.LA(1) <= '\r')
                        || input.LA(1) == ' ') {
                    input.consume();

                } else {
                    MismatchedSetException mse = new MismatchedSetException(
                            null, input);
                    recover(mse);
                    throw mse;
                }

                _channel = HIDDEN;

            }

            state.type = _type;
            state.channel = _channel;
        } finally {
        }
    }

    // $ANTLR end "WS"

    // $ANTLR start "COMMENT"
    public final void mCOMMENT() throws RecognitionException {
        try {
            int _type = COMMENT;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // CTFLexer.g:179:9: ( COMMENT_OPEN ( . )* COMMENT_CLOSE )
            // CTFLexer.g:179:11: COMMENT_OPEN ( . )* COMMENT_CLOSE
            {
                mCOMMENT_OPEN();
                // CTFLexer.g:179:24: ( . )*
                loop28: do {
                    int alt28 = 2;
                    int LA28_0 = input.LA(1);

                    if ((LA28_0 == '*')) {
                        int LA28_1 = input.LA(2);

                        if ((LA28_1 == '/')) {
                            alt28 = 2;
                        } else if (((LA28_1 >= '\u0000' && LA28_1 <= '.') || (LA28_1 >= '0' && LA28_1 <= '\uFFFF'))) {
                            alt28 = 1;
                        }

                    } else if (((LA28_0 >= '\u0000' && LA28_0 <= ')') || (LA28_0 >= '+' && LA28_0 <= '\uFFFF'))) {
                        alt28 = 1;
                    }

                    switch (alt28) {
                    case 1:
                    // CTFLexer.g:179:24: .
                    {
                        matchAny();

                    }
                        break;

                    default:
                        break loop28;
                    }
                } while (true);

                mCOMMENT_CLOSE();
                _channel = HIDDEN;

            }

            state.type = _type;
            state.channel = _channel;
        } finally {
        }
    }

    // $ANTLR end "COMMENT"

    // $ANTLR start "COMMENT_OPEN"
    public final void mCOMMENT_OPEN() throws RecognitionException {
        try {
            // CTFLexer.g:180:23: ( '/*' )
            // CTFLexer.g:180:25: '/*'
            {
                match("/*");

            }

        } finally {
        }
    }

    // $ANTLR end "COMMENT_OPEN"

    // $ANTLR start "COMMENT_CLOSE"
    public final void mCOMMENT_CLOSE() throws RecognitionException {
        try {
            // CTFLexer.g:181:24: ( '*/' )
            // CTFLexer.g:181:26: '*/'
            {
                match("*/");

            }

        } finally {
        }
    }

    // $ANTLR end "COMMENT_CLOSE"

    // $ANTLR start "LINE_COMMENT"
    public final void mLINE_COMMENT() throws RecognitionException {
        try {
            int _type = LINE_COMMENT;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // CTFLexer.g:186:14: ( '//' (~ ( '\\n' | '\\r' ) )* ( '\\r' )?
            // '\\n' )
            // CTFLexer.g:186:16: '//' (~ ( '\\n' | '\\r' ) )* ( '\\r' )? '\\n'
            {
                match("//");

                // CTFLexer.g:186:21: (~ ( '\\n' | '\\r' ) )*
                loop29: do {
                    int alt29 = 2;
                    int LA29_0 = input.LA(1);

                    if (((LA29_0 >= '\u0000' && LA29_0 <= '\t')
                            || (LA29_0 >= '\u000B' && LA29_0 <= '\f') || (LA29_0 >= '\u000E' && LA29_0 <= '\uFFFF'))) {
                        alt29 = 1;
                    }

                    switch (alt29) {
                    case 1:
                    // CTFLexer.g:186:21: ~ ( '\\n' | '\\r' )
                    {
                        if ((input.LA(1) >= '\u0000' && input.LA(1) <= '\t')
                                || (input.LA(1) >= '\u000B' && input.LA(1) <= '\f')
                                || (input.LA(1) >= '\u000E' && input.LA(1) <= '\uFFFF')) {
                            input.consume();

                        } else {
                            MismatchedSetException mse = new MismatchedSetException(
                                    null, input);
                            recover(mse);
                            throw mse;
                        }

                    }
                        break;

                    default:
                        break loop29;
                    }
                } while (true);

                // CTFLexer.g:186:35: ( '\\r' )?
                int alt30 = 2;
                int LA30_0 = input.LA(1);

                if ((LA30_0 == '\r')) {
                    alt30 = 1;
                }
                switch (alt30) {
                case 1:
                // CTFLexer.g:186:35: '\\r'
                {
                    match('\r');

                }
                    break;

                }

                match('\n');
                _channel = HIDDEN;

            }

            state.type = _type;
            state.channel = _channel;
        } finally {
        }
    }

    // $ANTLR end "LINE_COMMENT"

    // $ANTLR start "IDENTIFIER"
    public final void mIDENTIFIER() throws RecognitionException {
        try {
            int _type = IDENTIFIER;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // CTFLexer.g:191:12: ( NONDIGIT ( NONDIGIT | DIGIT )* )
            // CTFLexer.g:191:14: NONDIGIT ( NONDIGIT | DIGIT )*
            {
                mNONDIGIT();
                // CTFLexer.g:191:23: ( NONDIGIT | DIGIT )*
                loop31: do {
                    int alt31 = 3;
                    int LA31_0 = input.LA(1);

                    if (((LA31_0 >= 'A' && LA31_0 <= 'Z') || LA31_0 == '_' || (LA31_0 >= 'a' && LA31_0 <= 'z'))) {
                        alt31 = 1;
                    } else if (((LA31_0 >= '0' && LA31_0 <= '9'))) {
                        alt31 = 2;
                    }

                    switch (alt31) {
                    case 1:
                    // CTFLexer.g:191:24: NONDIGIT
                    {
                        mNONDIGIT();

                    }
                        break;
                    case 2:
                    // CTFLexer.g:191:35: DIGIT
                    {
                        mDIGIT();

                    }
                        break;

                    default:
                        break loop31;
                    }
                } while (true);

            }

            state.type = _type;
            state.channel = _channel;
        } finally {
        }
    }

    // $ANTLR end "IDENTIFIER"

    // $ANTLR start "NONDIGIT"
    public final void mNONDIGIT() throws RecognitionException {
        try {
            // CTFLexer.g:192:19: ( ( '_' ) | ( 'A' .. 'Z' ) | ( 'a' .. 'z' ) )
            int alt32 = 3;
            switch (input.LA(1)) {
            case '_': {
                alt32 = 1;
            }
                break;
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
            case 'Z': {
                alt32 = 2;
            }
                break;
            case 'a':
            case 'b':
            case 'c':
            case 'd':
            case 'e':
            case 'f':
            case 'g':
            case 'h':
            case 'i':
            case 'j':
            case 'k':
            case 'l':
            case 'm':
            case 'n':
            case 'o':
            case 'p':
            case 'q':
            case 'r':
            case 's':
            case 't':
            case 'u':
            case 'v':
            case 'w':
            case 'x':
            case 'y':
            case 'z': {
                alt32 = 3;
            }
                break;
            default:
                NoViableAltException nvae = new NoViableAltException("", 32, 0,
                        input);

                throw nvae;
            }

            switch (alt32) {
            case 1:
            // CTFLexer.g:192:21: ( '_' )
            {
                // CTFLexer.g:192:21: ( '_' )
                // CTFLexer.g:192:22: '_'
                {
                    match('_');

                }

            }
                break;
            case 2:
            // CTFLexer.g:192:29: ( 'A' .. 'Z' )
            {
                // CTFLexer.g:192:29: ( 'A' .. 'Z' )
                // CTFLexer.g:192:30: 'A' .. 'Z'
                {
                    matchRange('A', 'Z');

                }

            }
                break;
            case 3:
            // CTFLexer.g:192:42: ( 'a' .. 'z' )
            {
                // CTFLexer.g:192:42: ( 'a' .. 'z' )
                // CTFLexer.g:192:43: 'a' .. 'z'
                {
                    matchRange('a', 'z');

                }

            }
                break;

            }
        } finally {
        }
    }

    // $ANTLR end "NONDIGIT"

    @Override
    public void mTokens() throws RecognitionException {
        // CTFLexer.g:1:8: ( ALIGNTOK | CONSTTOK | CHARTOK | DOUBLETOK | ENUMTOK
        // | EVENTTOK | FLOATINGPOINTTOK | FLOATTOK | INTEGERTOK | INTTOK |
        // LONGTOK | SHORTTOK | SIGNEDTOK | STREAMTOK | STRINGTOK | STRUCTTOK |
        // TRACETOK | TYPEALIASTOK | TYPEDEFTOK | UNSIGNEDTOK | VARIANTTOK |
        // VOIDTOK | BOOLTOK | COMPLEXTOK | IMAGINARYTOK | ENVTOK | CLOCKTOK |
        // CALLSITETOK | NANNUMBERTOK | INFINITYTOK | NINFINITYTOK | SEPARATOR |
        // COLON | ELIPSES | ASSIGNMENT | TYPE_ASSIGNMENT | LT | GT | OPENBRAC |
        // CLOSEBRAC | LPAREN | RPAREN | LCURL | RCURL | TERM | POINTER | SIGN |
        // ARROW | DOT | OCTAL_LITERAL | DECIMAL_LITERAL | HEX_LITERAL |
        // CHARACTER_LITERAL | STRING_LITERAL | WS | COMMENT | LINE_COMMENT |
        // IDENTIFIER )
        int alt33 = 58;
        alt33 = dfa33.predict(input);
        switch (alt33) {
        case 1:
        // CTFLexer.g:1:10: ALIGNTOK
        {
            mALIGNTOK();

        }
            break;
        case 2:
        // CTFLexer.g:1:19: CONSTTOK
        {
            mCONSTTOK();

        }
            break;
        case 3:
        // CTFLexer.g:1:28: CHARTOK
        {
            mCHARTOK();

        }
            break;
        case 4:
        // CTFLexer.g:1:36: DOUBLETOK
        {
            mDOUBLETOK();

        }
            break;
        case 5:
        // CTFLexer.g:1:46: ENUMTOK
        {
            mENUMTOK();

        }
            break;
        case 6:
        // CTFLexer.g:1:54: EVENTTOK
        {
            mEVENTTOK();

        }
            break;
        case 7:
        // CTFLexer.g:1:63: FLOATINGPOINTTOK
        {
            mFLOATINGPOINTTOK();

        }
            break;
        case 8:
        // CTFLexer.g:1:80: FLOATTOK
        {
            mFLOATTOK();

        }
            break;
        case 9:
        // CTFLexer.g:1:89: INTEGERTOK
        {
            mINTEGERTOK();

        }
            break;
        case 10:
        // CTFLexer.g:1:100: INTTOK
        {
            mINTTOK();

        }
            break;
        case 11:
        // CTFLexer.g:1:107: LONGTOK
        {
            mLONGTOK();

        }
            break;
        case 12:
        // CTFLexer.g:1:115: SHORTTOK
        {
            mSHORTTOK();

        }
            break;
        case 13:
        // CTFLexer.g:1:124: SIGNEDTOK
        {
            mSIGNEDTOK();

        }
            break;
        case 14:
        // CTFLexer.g:1:134: STREAMTOK
        {
            mSTREAMTOK();

        }
            break;
        case 15:
        // CTFLexer.g:1:144: STRINGTOK
        {
            mSTRINGTOK();

        }
            break;
        case 16:
        // CTFLexer.g:1:154: STRUCTTOK
        {
            mSTRUCTTOK();

        }
            break;
        case 17:
        // CTFLexer.g:1:164: TRACETOK
        {
            mTRACETOK();

        }
            break;
        case 18:
        // CTFLexer.g:1:173: TYPEALIASTOK
        {
            mTYPEALIASTOK();

        }
            break;
        case 19:
        // CTFLexer.g:1:186: TYPEDEFTOK
        {
            mTYPEDEFTOK();

        }
            break;
        case 20:
        // CTFLexer.g:1:197: UNSIGNEDTOK
        {
            mUNSIGNEDTOK();

        }
            break;
        case 21:
        // CTFLexer.g:1:209: VARIANTTOK
        {
            mVARIANTTOK();

        }
            break;
        case 22:
        // CTFLexer.g:1:220: VOIDTOK
        {
            mVOIDTOK();

        }
            break;
        case 23:
        // CTFLexer.g:1:228: BOOLTOK
        {
            mBOOLTOK();

        }
            break;
        case 24:
        // CTFLexer.g:1:236: COMPLEXTOK
        {
            mCOMPLEXTOK();

        }
            break;
        case 25:
        // CTFLexer.g:1:247: IMAGINARYTOK
        {
            mIMAGINARYTOK();

        }
            break;
        case 26:
        // CTFLexer.g:1:260: ENVTOK
        {
            mENVTOK();

        }
            break;
        case 27:
        // CTFLexer.g:1:267: CLOCKTOK
        {
            mCLOCKTOK();

        }
            break;
        case 28:
        // CTFLexer.g:1:276: CALLSITETOK
        {
            mCALLSITETOK();

        }
            break;
        case 29:
        // CTFLexer.g:1:288: NANNUMBERTOK
        {
            mNANNUMBERTOK();

        }
            break;
        case 30:
        // CTFLexer.g:1:301: INFINITYTOK
        {
            mINFINITYTOK();

        }
            break;
        case 31:
        // CTFLexer.g:1:313: NINFINITYTOK
        {
            mNINFINITYTOK();

        }
            break;
        case 32:
        // CTFLexer.g:1:326: SEPARATOR
        {
            mSEPARATOR();

        }
            break;
        case 33:
        // CTFLexer.g:1:336: COLON
        {
            mCOLON();

        }
            break;
        case 34:
        // CTFLexer.g:1:342: ELIPSES
        {
            mELIPSES();

        }
            break;
        case 35:
        // CTFLexer.g:1:350: ASSIGNMENT
        {
            mASSIGNMENT();

        }
            break;
        case 36:
        // CTFLexer.g:1:361: TYPE_ASSIGNMENT
        {
            mTYPE_ASSIGNMENT();

        }
            break;
        case 37:
        // CTFLexer.g:1:377: LT
        {
            mLT();

        }
            break;
        case 38:
        // CTFLexer.g:1:380: GT
        {
            mGT();

        }
            break;
        case 39:
        // CTFLexer.g:1:383: OPENBRAC
        {
            mOPENBRAC();

        }
            break;
        case 40:
        // CTFLexer.g:1:392: CLOSEBRAC
        {
            mCLOSEBRAC();

        }
            break;
        case 41:
        // CTFLexer.g:1:402: LPAREN
        {
            mLPAREN();

        }
            break;
        case 42:
        // CTFLexer.g:1:409: RPAREN
        {
            mRPAREN();

        }
            break;
        case 43:
        // CTFLexer.g:1:416: LCURL
        {
            mLCURL();

        }
            break;
        case 44:
        // CTFLexer.g:1:422: RCURL
        {
            mRCURL();

        }
            break;
        case 45:
        // CTFLexer.g:1:428: TERM
        {
            mTERM();

        }
            break;
        case 46:
        // CTFLexer.g:1:433: POINTER
        {
            mPOINTER();

        }
            break;
        case 47:
        // CTFLexer.g:1:441: SIGN
        {
            mSIGN();

        }
            break;
        case 48:
        // CTFLexer.g:1:446: ARROW
        {
            mARROW();

        }
            break;
        case 49:
        // CTFLexer.g:1:452: DOT
        {
            mDOT();

        }
            break;
        case 50:
        // CTFLexer.g:1:456: OCTAL_LITERAL
        {
            mOCTAL_LITERAL();

        }
            break;
        case 51:
        // CTFLexer.g:1:470: DECIMAL_LITERAL
        {
            mDECIMAL_LITERAL();

        }
            break;
        case 52:
        // CTFLexer.g:1:486: HEX_LITERAL
        {
            mHEX_LITERAL();

        }
            break;
        case 53:
        // CTFLexer.g:1:498: CHARACTER_LITERAL
        {
            mCHARACTER_LITERAL();

        }
            break;
        case 54:
        // CTFLexer.g:1:516: STRING_LITERAL
        {
            mSTRING_LITERAL();

        }
            break;
        case 55:
        // CTFLexer.g:1:531: WS
        {
            mWS();

        }
            break;
        case 56:
        // CTFLexer.g:1:534: COMMENT
        {
            mCOMMENT();

        }
            break;
        case 57:
        // CTFLexer.g:1:542: LINE_COMMENT
        {
            mLINE_COMMENT();

        }
            break;
        case 58:
        // CTFLexer.g:1:555: IDENTIFIER
        {
            mIDENTIFIER();

        }
            break;

        }

    }

    protected DFA17 dfa17 = new DFA17(this);
    protected DFA33 dfa33 = new DFA33(this);
    static final String DFA17_eotS = "\1\uffff\2\6\1\11\1\6\2\uffff\1\6\2\uffff";
    static final String DFA17_eofS = "\12\uffff";
    static final String DFA17_minS = "\1\114\1\125\2\114\1\125\2\uffff\1\125\2\uffff";
    static final String DFA17_maxS = "\3\165\1\154\1\165\2\uffff\1\165\2\uffff";
    static final String DFA17_acceptS = "\5\uffff\1\4\1\1\1\uffff\1\3\1\2";
    static final String DFA17_specialS = "\12\uffff}>";
    static final String[] DFA17_transitionS = {
            "\1\2\10\uffff\1\3\26\uffff\1\1\10\uffff\1\3",
            "\1\5\26\uffff\1\4\10\uffff\1\5", "\1\7\10\uffff\1\5\37\uffff\1\5",
            "\1\10\37\uffff\1\10", "\1\5\37\uffff\1\5", "", "",
            "\1\5\37\uffff\1\5", "", "" };

    static final short[] DFA17_eot = DFA.unpackEncodedString(DFA17_eotS);
    static final short[] DFA17_eof = DFA.unpackEncodedString(DFA17_eofS);
    static final char[] DFA17_min = DFA.unpackEncodedStringToUnsignedChars(DFA17_minS);
    static final char[] DFA17_max = DFA.unpackEncodedStringToUnsignedChars(DFA17_maxS);
    static final short[] DFA17_accept = DFA.unpackEncodedString(DFA17_acceptS);
    static final short[] DFA17_special = DFA.unpackEncodedString(DFA17_specialS);
    static final short[][] DFA17_transition;

    static {
        int numStates = DFA17_transitionS.length;
        DFA17_transition = new short[numStates][];
        for (int i = 0; i < numStates; i++) {
            DFA17_transition[i] = DFA.unpackEncodedString(DFA17_transitionS[i]);
        }
    }

    class DFA17 extends DFA {

        public DFA17(BaseRecognizer recognizer) {
            this.recognizer = recognizer;
            this.decisionNumber = 17;
            this.eot = DFA17_eot;
            this.eof = DFA17_eof;
            this.min = DFA17_min;
            this.max = DFA17_max;
            this.accept = DFA17_accept;
            this.special = DFA17_special;
            this.transition = DFA17_transition;
        }

        @Override
        public String getDescription() {
            return "107:10: fragment INTEGER_TYPES_SUFFIX : ( ( 'l' ( 'l' )? | 'L' ( 'L' )? ) | ( 'u' | 'U' ) | ( 'u' | 'U' ) ( 'l' ( 'l' )? | 'L' ( 'L' )? ) | ( 'l' ( 'l' )? | 'L' ( 'L' )? ) ( 'u' | 'U' ) );";
        }
    }

    static final String DFA33_eotS = "\1\uffff\15\45\2\76\1\uffff\1\102\1\104\13\uffff\1\37\1\uffff\1"
            + "\45\5\uffff\27\45\11\uffff\1\144\2\uffff\7\45\1\154\2\45\1\160\14"
            + "\45\1\177\3\144\1\uffff\2\45\1\u0087\3\45\1\u008b\1\uffff\3\45\1"
            + "\uffff\1\u008f\11\45\1\u009a\3\45\1\uffff\1\144\1\uffff\3\144\1"
            + "\u00a0\1\u00a1\1\uffff\1\u00a2\2\45\1\uffff\1\u00a5\1\u00a7\1\45"
            + "\1\uffff\1\u00a9\4\45\1\u00ae\4\45\1\uffff\1\u00b3\2\45\5\uffff"
            + "\1\45\1\u00b7\1\uffff\1\45\1\uffff\1\45\1\uffff\1\u00ba\1\u00bb"
            + "\1\u00bc\1\u00bd\1\uffff\4\45\1\uffff\3\45\1\uffff\1\45\1\u00c6"
            + "\4\uffff\1\45\1\u00c8\1\45\1\u00ca\2\45\1\u00cd\1\45\1\uffff\1\45"
            + "\1\uffff\1\u00d0\1\uffff\1\u00d1\1\45\1\uffff\1\45\1\u00d4\2\uffff"
            + "\2\45\1\uffff\1\u00d7\1\45\1\uffff\2\45\1\u00db\1\uffff";
    static final String DFA33_eofS = "\u00dc\uffff";
    static final String DFA33_minS = "\1\11\1\154\1\141\1\157\1\156\1\154\1\156\1\157\1\150\1\162\1\156"
            + "\1\141\1\102\1\141\1\151\1\76\1\uffff\1\75\1\56\13\uffff\1\60\1"
            + "\uffff\1\42\3\uffff\1\52\1\uffff\1\151\1\156\1\141\1\157\1\154\2"
            + "\165\1\145\1\157\1\164\1\156\1\157\1\147\1\162\1\141\1\160\1\163"
            + "\1\162\1\151\2\157\1\155\1\116\11\uffff\1\60\2\uffff\1\147\1\163"
            + "\1\162\1\143\1\154\1\142\1\155\1\60\1\156\1\141\1\60\1\147\1\162"
            + "\1\156\1\145\1\143\1\145\2\151\1\144\1\157\1\155\1\141\1\60\1\125"
            + "\2\114\1\uffff\1\156\1\164\1\60\1\153\1\163\1\154\1\60\1\uffff\2"
            + "\164\1\147\1\uffff\1\60\1\164\1\145\1\141\1\156\1\143\1\145\1\141"
            + "\1\147\1\141\1\60\1\154\1\160\1\147\1\uffff\1\125\1\uffff\1\125"
            + "\1\154\1\114\2\60\1\uffff\1\60\1\151\1\145\1\uffff\2\60\1\145\1"
            + "\uffff\1\60\1\144\1\155\1\147\1\164\1\60\1\154\1\145\2\156\1\uffff"
            + "\1\60\1\154\1\151\5\uffff\1\164\1\60\1\uffff\1\156\1\uffff\1\162"
            + "\1\uffff\4\60\1\uffff\1\151\1\146\1\145\1\164\1\uffff\1\145\1\156"
            + "\1\145\1\uffff\1\147\1\60\4\uffff\1\141\1\60\1\144\1\60\1\170\1"
            + "\141\1\60\1\137\1\uffff\1\163\1\uffff\1\60\1\uffff\1\60\1\162\1"
            + "\uffff\1\160\1\60\2\uffff\1\171\1\157\1\uffff\1\60\1\151\1\uffff"
            + "\1\156\1\164\1\60\1\uffff";
    static final String DFA33_maxS = "\1\175\1\154\2\157\1\166\1\154\1\156\1\157\1\164\1\171\1\156\1\157"
            + "\1\111\1\141\2\151\1\uffff\1\75\1\56\13\uffff\1\170\1\uffff\1\47"
            + "\3\uffff\1\57\1\uffff\1\151\1\156\1\141\1\157\1\154\1\165\1\166"
            + "\1\145\1\157\1\164\1\156\1\157\1\147\1\162\1\141\1\160\1\163\1\162"
            + "\1\151\2\157\1\155\1\116\11\uffff\1\165\2\uffff\1\147\1\163\1\162"
            + "\1\143\1\154\1\142\1\155\1\172\1\156\1\141\1\172\1\147\1\162\1\156"
            + "\1\165\1\143\1\145\2\151\1\144\1\157\1\155\1\141\1\172\2\165\1\154"
            + "\1\uffff\1\156\1\164\1\172\1\153\1\163\1\154\1\172\1\uffff\2\164"
            + "\1\147\1\uffff\1\172\1\164\1\145\1\141\1\156\1\143\1\145\1\144\1"
            + "\147\1\141\1\172\1\154\1\160\1\147\1\uffff\1\165\1\uffff\1\165\1"
            + "\154\1\114\2\172\1\uffff\1\172\1\151\1\145\1\uffff\2\172\1\145\1"
            + "\uffff\1\172\1\144\1\155\1\147\1\164\1\172\1\154\1\145\2\156\1\uffff"
            + "\1\172\1\154\1\151\5\uffff\1\164\1\172\1\uffff\1\156\1\uffff\1\162"
            + "\1\uffff\4\172\1\uffff\1\151\1\146\1\145\1\164\1\uffff\1\145\1\156"
            + "\1\145\1\uffff\1\147\1\172\4\uffff\1\141\1\172\1\144\1\172\1\170"
            + "\1\141\1\172\1\137\1\uffff\1\163\1\uffff\1\172\1\uffff\1\172\1\162"
            + "\1\uffff\1\160\1\172\2\uffff\1\171\1\157\1\uffff\1\172\1\151\1\uffff"
            + "\1\156\1\164\1\172\1\uffff";
    static final String DFA33_acceptS = "\20\uffff\1\40\2\uffff\1\43\1\45\1\46\1\47\1\50\1\51\1\52\1\53\1"
            + "\54\1\55\1\56\1\uffff\1\63\1\uffff\1\65\1\66\1\67\1\uffff\1\72\27"
            + "\uffff\1\36\1\57\1\37\1\60\1\44\1\41\1\42\1\61\1\64\1\uffff\1\70"
            + "\1\71\33\uffff\1\62\7\uffff\1\32\3\uffff\1\12\16\uffff\1\35\1\uffff"
            + "\1\62\5\uffff\1\3\3\uffff\1\5\3\uffff\1\13\12\uffff\1\26\3\uffff"
            + "\2\62\1\1\1\2\1\33\2\uffff\1\6\1\uffff\1\10\1\uffff\1\14\4\uffff"
            + "\1\21\4\uffff\1\27\3\uffff\1\4\2\uffff\1\15\1\16\1\17\1\20\10\uffff"
            + "\1\11\1\uffff\1\23\1\uffff\1\25\2\uffff\1\34\2\uffff\1\24\1\30\2"
            + "\uffff\1\22\2\uffff\1\31\3\uffff\1\7";
    static final String DFA33_specialS = "\u00dc\uffff}>";
    static final String[] DFA33_transitionS = {
            "\2\43\1\uffff\2\43\22\uffff\1\43\1\uffff\1\42\4\uffff\1\41\1"
                    + "\30\1\31\1\35\1\16\1\20\1\17\1\22\1\44\1\36\11\37\1\21\1\34"
                    + "\1\24\1\23\1\25\2\uffff\13\45\1\40\1\45\1\15\14\45\1\26\1\uffff"
                    + "\1\27\1\uffff\1\14\1\uffff\1\1\1\45\1\2\1\3\1\4\1\5\2\45\1\6"
                    + "\2\45\1\7\6\45\1\10\1\11\1\12\1\13\4\45\1\32\1\uffff\1\33",
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
            "\10\106\2\37\22\uffff\1\142\10\uffff\1\143\26\uffff\1\141\10"
                    + "\uffff\1\143",
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
            "\12\45\7\uffff\32\45\4\uffff\1\45\1\uffff\10\45\1\u00a6\21"
                    + "\45", "\1\u00a8", "",
            "\12\45\7\uffff\32\45\4\uffff\1\45\1\uffff\32\45", "\1\u00aa",
            "\1\u00ab", "\1\u00ac", "\1\u00ad",
            "\12\45\7\uffff\32\45\4\uffff\1\45\1\uffff\32\45", "\1\u00af",
            "\1\u00b0", "\1\u00b1", "\1\u00b2", "",
            "\12\45\7\uffff\32\45\4\uffff\1\45\1\uffff\32\45", "\1\u00b4",
            "\1\u00b5", "", "", "", "", "", "\1\u00b6",
            "\12\45\7\uffff\32\45\4\uffff\1\45\1\uffff\32\45", "", "\1\u00b8",
            "", "\1\u00b9", "",
            "\12\45\7\uffff\32\45\4\uffff\1\45\1\uffff\32\45",
            "\12\45\7\uffff\32\45\4\uffff\1\45\1\uffff\32\45",
            "\12\45\7\uffff\32\45\4\uffff\1\45\1\uffff\32\45",
            "\12\45\7\uffff\32\45\4\uffff\1\45\1\uffff\32\45", "", "\1\u00be",
            "\1\u00bf", "\1\u00c0", "\1\u00c1", "", "\1\u00c2", "\1\u00c3",
            "\1\u00c4", "", "\1\u00c5",
            "\12\45\7\uffff\32\45\4\uffff\1\45\1\uffff\32\45", "", "", "", "",
            "\1\u00c7", "\12\45\7\uffff\32\45\4\uffff\1\45\1\uffff\32\45",
            "\1\u00c9", "\12\45\7\uffff\32\45\4\uffff\1\45\1\uffff\32\45",
            "\1\u00cb", "\1\u00cc",
            "\12\45\7\uffff\32\45\4\uffff\1\45\1\uffff\32\45", "\1\u00ce", "",
            "\1\u00cf", "", "\12\45\7\uffff\32\45\4\uffff\1\45\1\uffff\32\45",
            "", "\12\45\7\uffff\32\45\4\uffff\1\45\1\uffff\32\45", "\1\u00d2",
            "", "\1\u00d3", "\12\45\7\uffff\32\45\4\uffff\1\45\1\uffff\32\45",
            "", "", "\1\u00d5", "\1\u00d6", "",
            "\12\45\7\uffff\32\45\4\uffff\1\45\1\uffff\32\45", "\1\u00d8", "",
            "\1\u00d9", "\1\u00da",
            "\12\45\7\uffff\32\45\4\uffff\1\45\1\uffff\32\45", "" };

    static final short[] DFA33_eot = DFA.unpackEncodedString(DFA33_eotS);
    static final short[] DFA33_eof = DFA.unpackEncodedString(DFA33_eofS);
    static final char[] DFA33_min = DFA.unpackEncodedStringToUnsignedChars(DFA33_minS);
    static final char[] DFA33_max = DFA.unpackEncodedStringToUnsignedChars(DFA33_maxS);
    static final short[] DFA33_accept = DFA.unpackEncodedString(DFA33_acceptS);
    static final short[] DFA33_special = DFA.unpackEncodedString(DFA33_specialS);
    static final short[][] DFA33_transition;

    static {
        int numStates = DFA33_transitionS.length;
        DFA33_transition = new short[numStates][];
        for (int i = 0; i < numStates; i++) {
            DFA33_transition[i] = DFA.unpackEncodedString(DFA33_transitionS[i]);
        }
    }

    class DFA33 extends DFA {

        public DFA33(BaseRecognizer recognizer) {
            this.recognizer = recognizer;
            this.decisionNumber = 33;
            this.eot = DFA33_eot;
            this.eof = DFA33_eof;
            this.min = DFA33_min;
            this.max = DFA33_max;
            this.accept = DFA33_accept;
            this.special = DFA33_special;
            this.transition = DFA33_transition;
        }

        @Override
        public String getDescription() {
            return "1:1: Tokens : ( ALIGNTOK | CONSTTOK | CHARTOK | DOUBLETOK | ENUMTOK | EVENTTOK | FLOATINGPOINTTOK | FLOATTOK | INTEGERTOK | INTTOK | LONGTOK | SHORTTOK | SIGNEDTOK | STREAMTOK | STRINGTOK | STRUCTTOK | TRACETOK | TYPEALIASTOK | TYPEDEFTOK | UNSIGNEDTOK | VARIANTTOK | VOIDTOK | BOOLTOK | COMPLEXTOK | IMAGINARYTOK | ENVTOK | CLOCKTOK | CALLSITETOK | NANNUMBERTOK | INFINITYTOK | NINFINITYTOK | SEPARATOR | COLON | ELIPSES | ASSIGNMENT | TYPE_ASSIGNMENT | LT | GT | OPENBRAC | CLOSEBRAC | LPAREN | RPAREN | LCURL | RCURL | TERM | POINTER | SIGN | ARROW | DOT | OCTAL_LITERAL | DECIMAL_LITERAL | HEX_LITERAL | CHARACTER_LITERAL | STRING_LITERAL | WS | COMMENT | LINE_COMMENT | IDENTIFIER );";
        }
    }

}