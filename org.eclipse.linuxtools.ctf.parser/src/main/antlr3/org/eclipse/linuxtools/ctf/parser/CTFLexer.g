lexer grammar CTFLexer;

options {
    language = Java;
}

@lexer::header {
    package org.eclipse.linuxtools.ctf.parser;
}

/*
 * Lexer grammers
 */

/*
 * Keywords
 */
ALIGNTOK         : 'align' ;
CONSTTOK         : 'const' ;
CHARTOK          : 'char' ;
DOUBLETOK        : 'double' ;
ENUMTOK          : 'enum' ;
EVENTTOK         : 'event' ;
FLOATINGPOINTTOK : 'floating_point' ;
FLOATTOK         : 'float' ;
INTEGERTOK       : 'integer' ;
INTTOK           : 'int' ;
LONGTOK          : 'long' ;
SHORTTOK         : 'short' ;
SIGNEDTOK        : 'signed' ;
STREAMTOK        : 'stream' ;
STRINGTOK        : 'string' ;
STRUCTTOK        : 'struct' ;
TRACETOK         : 'trace' ;
TYPEALIASTOK     : 'typealias' ;
TYPEDEFTOK       : 'typedef' ;
UNSIGNEDTOK      : 'unsigned' ;
VARIANTTOK       : 'variant' ;
VOIDTOK          : 'void' ;
BOOLTOK          : '_Bool' ;
COMPLEXTOK       : '_Complex' ;
IMAGINARYTOK     : '_Imaginary' ;
ENVTOK           : 'env' ;
CLOCKTOK         : 'clock' ;
/*
 * Callsite tokens (v1.9)
 */
CALLSITETOK      : 'callsite' ;


/*
 * Spec still to come.
 */
NANNUMBERTOK  : 'NaN' ;
INFINITYTOK   : '+inf' ;
NINFINITYTOK  : '-inf' ;

/*
 * Symbols
 */
SEPARATOR          : ',' ;
COLON              : ':' ;
ELIPSES            : '...' ;
ASSIGNMENT         : '=' ;
TYPE_ASSIGNMENT    : ':=' ;
LT                 : '<' ;
GT                 : '>' ;
OPENBRAC           : '[' ;
CLOSEBRAC          : ']' ;
LPAREN             : '(' ;
RPAREN             : ')' ;
LCURL              : '{' ;
RCURL              : '}' ;
TERM               : ';' ;
POINTER            : '*' ;
SIGN               : '+' | '-' ;
ARROW              : '->' ;
DOT                : '.' ;
fragment BACKSLASH : '\\' ;


/*
 * Integer literals
 */
OCTAL_LITERAL : '0' ('0'..'7')+ INTEGER_TYPES_SUFFIX? ;

DECIMAL_LITERAL : DIGIT+ INTEGER_TYPES_SUFFIX? ;

HEX_LITERAL : HEX_PREFIX HEX_DIGIT+ INTEGER_TYPES_SUFFIX? ;
fragment HEX_DIGIT : DIGIT | ('a'..'f') | ('A'..'F') ;
fragment HEX_PREFIX : '0' ('x' | 'X') ;

/* Helpers for integer literals */
fragment DIGIT : '0'..'9' ;
fragment NONZERO_DIGIT : '1'..'9' ;


/**
 * Integer suffix for long, long long and unsigned.
 *
 * Matches all possible combination of L, LL and U.
 */
fragment INTEGER_TYPES_SUFFIX :
    ('l' ('l')? | 'L' ('L')?)             // l, ll
  | ('u' | 'U')                           // u
  | ('u' | 'U') ('l' ('l')? | 'L' ('L')?) // ul, ull
  | ('l' ('l')? | 'L' ('L')?) ('u'| 'U')  // lu, llu
  ;

/**
 * Escape sequences
 */
fragment ESCAPE_SEQUENCE :
    BACKSLASH ('\'' | '"' | '?' | BACKSLASH | 'a' | 'b' | 'f' | 'n' | 'r' | 't' | 'v' )
  | OCTAL_ESCAPE
  | UNICODE_ESCAPE
  | HEXADECIMAL_ESCAPE
    ;

/**
 * Octal escape sequence
 */
fragment OCTAL_ESCAPE :
    BACKSLASH ('0'..'3') ('0'..'7') ('0'..'7')
  | BACKSLASH ('0'..'7') ('0'..'7')
  | BACKSLASH ('0'..'7')
  ;

/**
 * Hexadecimal escape sequence
 */
fragment HEXADECIMAL_ESCAPE : BACKSLASH 'x' HEX_DIGIT+ ;

/**
 * Unicode escape sequence
 */
fragment UNICODE_ESCAPE :
    BACKSLASH 'u' HEX_DIGIT HEX_DIGIT HEX_DIGIT HEX_DIGIT
  | BACKSLASH 'U' HEX_DIGIT HEX_DIGIT HEX_DIGIT HEX_DIGIT HEX_DIGIT HEX_DIGIT HEX_DIGIT HEX_DIGIT
  ;


/* Used in both character and string literal */
fragment STRINGPREFIX : 'L';

/*
 * Character literal
 */
CHARACTER_LITERAL : STRINGPREFIX? SINGLEQUOTE CHAR_CONTENT+ SINGLEQUOTE ;
fragment CHAR_CONTENT : (ESCAPE_SEQUENCE | ~(BACKSLASH | SINGLEQUOTE)) ;
fragment SINGLEQUOTE : '\'';

/*
 * String literal
 */
STRING_LITERAL : STRINGPREFIX? DOUBLEQUOTE STRING_CONTENT* DOUBLEQUOTE ;
fragment STRING_CONTENT : (ESCAPE_SEQUENCE | ~(BACKSLASH | DOUBLEQUOTE)) ;
fragment DOUBLEQUOTE : '"' ;

/**
 * Whitespaces
 */
WS : (' ' | '\r' | '\t' | '\u000C' | '\n') { $channel=HIDDEN; } ;

/**
 * Multiline comment
 */
// About the greedy option: see page 100-101 of The Definitive ANTLR reference
// COMMENT : '/*' ( options {greedy=false;} : . )* '*/' {$channel=HIDDEN;} ;
COMMENT : COMMENT_OPEN .* COMMENT_CLOSE { $channel = HIDDEN; } ;
fragment COMMENT_OPEN : '/*';
fragment COMMENT_CLOSE : '*/';

/**
 * Single line comment
 */
LINE_COMMENT : '//' ~('\n'|'\r')* '\r'? '\n' {$channel=HIDDEN;} ;

/**
 * Identifiers
 */
IDENTIFIER : NONDIGIT (NONDIGIT | DIGIT)* ;
fragment NONDIGIT : ('_') | ('A'..'Z') | ('a'..'z') ;
