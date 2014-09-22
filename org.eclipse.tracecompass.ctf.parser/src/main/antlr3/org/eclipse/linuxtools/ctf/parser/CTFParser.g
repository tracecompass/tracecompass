parser grammar CTFParser;

options {
    language   = Java;
    output     = AST;
    ASTLabelType = CommonTree;
    tokenVocab = CTFLexer;
}

tokens {
    ROOT;

    EVENT;
    STREAM;
    TRACE;
    ENV;
    CLOCK;
    CALLSITE;

    DECLARATION;
    SV_DECLARATION;
    TYPE_SPECIFIER_LIST;
    TYPE_DECLARATOR_LIST;
    TYPE_DECLARATOR;

    STRUCT;
    STRUCT_NAME;
    STRUCT_BODY;
    ALIGN;

    CTF_EXPRESSION_TYPE;
    CTF_EXPRESSION_VAL;
    CTF_LEFT;
    CTF_RIGHT;

    UNARY_EXPRESSION_STRING;
    UNARY_EXPRESSION_STRING_QUOTES;
    UNARY_EXPRESSION_DEC;
    UNARY_EXPRESSION_HEX;
    UNARY_EXPRESSION_OCT;
    LENGTH;

    TYPEDEF;

    TYPEALIAS;
    TYPEALIAS_TARGET;
    TYPEALIAS_ALIAS;

    INTEGER;
    STRING;
    FLOATING_POINT;

    ENUM;
    ENUM_CONTAINER_TYPE;
    ENUM_ENUMERATOR;
    ENUM_NAME;
    ENUM_VALUE;
    ENUM_VALUE_RANGE;
    ENUM_BODY;

    VARIANT;
    VARIANT_NAME;
    VARIANT_TAG;
    VARIANT_BODY;

    DECLARATOR;
    LENGTH;
}

/*
 * Scope for the tracking of types.
 * For now we just track the names (it's a simple Set), but
 * later we will have to track the info about the target type.
 */
scope Symbols {
    Set<String> types;
}

@header {
    package org.eclipse.linuxtools.ctf.parser;
    import java.util.Set;
    import java.util.HashSet;
}

@members {
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
        $Symbols::types.add(name);
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
}

 /*
  * Override the catch clause to disable automatic error recovery.
  * By default, the catch block of every rule simple rethrows the error.
  */
@rulecatch {
    catch (RecognitionException e) {
        throw e;
    }
}

/* The top-level rule. */
parse
scope Symbols;
@init {
    $Symbols::types = new HashSet<String>();
}
  : declaration+ EOF -> ^(ROOT declaration+)
  ;

numberLiteral
  : SIGN*
      ( HEX_LITERAL -> ^(UNARY_EXPRESSION_HEX HEX_LITERAL SIGN*)
      | DECIMAL_LITERAL -> ^(UNARY_EXPRESSION_DEC DECIMAL_LITERAL SIGN*)
      | OCTAL_LITERAL -> ^(UNARY_EXPRESSION_OCT OCTAL_LITERAL SIGN*)
      )
  ;

primaryExpression
  : (IDENTIFIER) => IDENTIFIER
      -> ^(UNARY_EXPRESSION_STRING IDENTIFIER)
  | (ctfKeyword) => ctfKeyword -> ^(UNARY_EXPRESSION_STRING ctfKeyword)
  | (STRING_LITERAL) => STRING_LITERAL
      -> ^(UNARY_EXPRESSION_STRING_QUOTES STRING_LITERAL)
  /*| (LPAREN unaryExpression RPAREN)*/ // Not supported yet
  | numberLiteral
  | enumConstant
  | CHARACTER_LITERAL
  ;

postfixExpressionSuffix
  : OPENBRAC unaryExpression CLOSEBRAC!
  | (ref=DOT | ref=ARROW) IDENTIFIER
      -> ^($ref ^(UNARY_EXPRESSION_STRING IDENTIFIER))
  ;

postfixExpression
  : primaryExpression postfixExpressionSuffix*
  | ctfSpecifierHead postfixExpressionSuffix+  // added for ctf-v1.8
  ;

unaryExpression
  : postfixExpression
  /* | ((SIGN postfixExpression[true]) | postfixExpression[false]) */
  ;

enumConstant
  : STRING_LITERAL -> ^(UNARY_EXPRESSION_STRING_QUOTES STRING_LITERAL)
  | IDENTIFIER -> ^(UNARY_EXPRESSION_STRING IDENTIFIER)
  | ctfKeyword -> ^(UNARY_EXPRESSION_STRING ctfKeyword)
  ;

// 2.2

declaration
@after {
    if (inTypedef()) {
        typedefOff();
    }
}
  : declarationSpecifiers declaratorList? TERM
      // When the declaration is completely parsed and was a typedef,
      // we add the declarators to the symbol table.
      -> {inTypedef()}?
         ^(DECLARATION ^(TYPEDEF declaratorList declarationSpecifiers))
      -> ^(DECLARATION declarationSpecifiers declaratorList?)
  | ctfSpecifier TERM!
  ;

declarationSpecifiers
  : (
      // We don't want to keep the typedef keyword in the specifier list.
      // Instead, we keep track that we encountered a typedef in the declaration.
        storageClassSpecifier
      | typeQualifier
      | typeSpecifier
  )+ -> ^(TYPE_SPECIFIER_LIST typeQualifier* typeSpecifier*)
  ;

declaratorList
  : declarator (SEPARATOR declarator)*
      -> ^(TYPE_DECLARATOR_LIST declarator+)
  ;

abstractDeclaratorList
  : abstractDeclarator (SEPARATOR abstractDeclarator)*
      -> ^(TYPE_DECLARATOR_LIST abstractDeclarator+)
  ;

storageClassSpecifier
  : TYPEDEFTOK  { typedefOn(); }
  ;

typeSpecifier
  : FLOATTOK
  | INTTOK
  | LONGTOK
  | SHORTTOK
  | SIGNEDTOK
  | UNSIGNEDTOK
  | CHARTOK
  | DOUBLETOK
  | VOIDTOK
  | BOOLTOK
  | COMPLEXTOK
  | IMAGINARYTOK
  | structSpecifier
  | variantSpecifier
  | enumSpecifier
  | ctfTypeSpecifier
  | { inTypealiasAlias() || isTypeName(input.LT(1).getText()) }? => typedefName
  ;

typeQualifier
  : CONSTTOK
  ;

alignAttribute
  : ALIGNTOK LPAREN unaryExpression RPAREN -> ^(ALIGN unaryExpression)
  ;

  // you can have an empty struct but not an empty variant
structBody
scope Symbols;
@init {
    $Symbols::types = new HashSet<String>();
}
  : LCURL structOrVariantDeclarationList? RCURL
      -> ^(STRUCT_BODY structOrVariantDeclarationList?)
  ;

structSpecifier
  : STRUCTTOK
  (
    // We have an IDENTIFIER after 'struct'
    (
        structName
        (
          alignAttribute
        |
          (
            structBody
            ( /* structBody can return an empty tree, so we need those ? */
             alignAttribute
            |
             /* empty */
            )
          )
        |
          /* empty */
        )
    )
  |
    // We have a body after 'struct'
    (
      structBody
      (
        alignAttribute
        |
        /* empty */
      )
    )
  ) -> ^(STRUCT structName? structBody? alignAttribute?)
  ;

structName
  : IDENTIFIER -> ^(STRUCT_NAME IDENTIFIER)
  ;

structOrVariantDeclarationList
  : structOrVariantDeclaration+
  ;

structOrVariantDeclaration
  :
  (
      (
       declarationSpecifiers
         (
           /* If we met a "typedef" */
           {inTypedef()}? => declaratorList {typedefOff();}
             -> ^(TYPEDEF declaratorList declarationSpecifiers)
           | structOrVariantDeclaratorList
             -> ^(SV_DECLARATION declarationSpecifiers structOrVariantDeclaratorList)
         )
      )
    |
    // Lines 3 and 4
    typealiasDecl -> typealiasDecl
  )
  TERM
  ;

specifierQualifierList
  : (typeQualifier | typeSpecifier)+
      -> ^(TYPE_SPECIFIER_LIST typeQualifier* typeSpecifier*)
  ;

structOrVariantDeclaratorList
  : structOrVariantDeclarator (SEPARATOR structOrVariantDeclarator)*
      -> ^(TYPE_DECLARATOR_LIST structOrVariantDeclarator+)
  ;

structOrVariantDeclarator
  :
  /* Bitfields not supported yet */
    (declarator (COLON numberLiteral)?) -> declarator
  /*| (COLON numberLiteral)*/
  ;

variantSpecifier
  : VARIANTTOK
  (
    (
      variantName
      (
        (
          variantTag
          (
            variantBody
            |
            /* empty */
          )
        )
      |
        variantBody
      )
    )
  | (variantTag variantBody)
  | variantBody
  ) -> ^(VARIANT variantName? variantTag? variantBody?)
  ;

variantName
  : IDENTIFIER -> ^(VARIANT_NAME IDENTIFIER)
  ;

variantBody
scope Symbols;
@init {
    $Symbols::types = new HashSet<String>();
}
  : LCURL structOrVariantDeclarationList RCURL
      -> ^(VARIANT_BODY structOrVariantDeclarationList)
  ;

variantTag
  : LT IDENTIFIER GT -> ^(VARIANT_TAG IDENTIFIER)
  ;

enumSpecifier
  : ENUMTOK
    (
        // Lines 1 to 5, when we have "ENUMTOK IDENTIFIER".
        (
            enumName
            (
                enumContainerType enumBody
          |
                enumBody
            |
                // no enumDeclarator or enumBodym
            )
        )
    |
      // Lines 1, 2, 4, 5, when we have no IDENTIFIER.
        (
            enumContainerType enumBody
        |
            enumBody
        )
    ) -> ^(ENUM enumName? enumContainerType? enumBody?)
  ;

enumName
  : IDENTIFIER -> ^(ENUM_NAME IDENTIFIER)
  ;

enumBody
  : LCURL enumeratorList SEPARATOR? RCURL -> ^(ENUM_BODY enumeratorList)
  ;

enumContainerType
  : COLON declarationSpecifiers -> ^(ENUM_CONTAINER_TYPE declarationSpecifiers)
  ;

enumeratorList
  : enumerator (SEPARATOR enumerator)* -> (^(ENUM_ENUMERATOR enumerator))+
  ;

enumerator
  : enumConstant enumeratorValue?
  ;

enumeratorValue
  : ASSIGNMENT e1=unaryExpression
      ( /* empty */
          -> ^(ENUM_VALUE $e1)
      | ELIPSES e2=unaryExpression
          -> ^(ENUM_VALUE_RANGE $e1 $e2)
      )
  ;

declarator
  : pointer* directDeclarator
      -> ^(TYPE_DECLARATOR pointer* directDeclarator)
  ;

directDeclarator
  : (
      IDENTIFIER
       { if (inTypedef()) addTypeName($IDENTIFIER.text); }
       { debug_print($IDENTIFIER.text); }
      /*| LPAREN declarator RPAREN*/ /* Not supported yet */
    )
    directDeclaratorSuffix*
  ;

directDeclaratorSuffix
  : OPENBRAC directDeclaratorLength CLOSEBRAC 
      -> ^(LENGTH directDeclaratorLength)
  ;

directDeclaratorLength
  : unaryExpression
  ;

abstractDeclarator
  : pointer+ directAbstractDeclarator?
      -> ^(TYPE_DECLARATOR pointer+ directAbstractDeclarator?)
  | directAbstractDeclarator
      -> ^(TYPE_DECLARATOR directAbstractDeclarator)
  ;

/**
  * In the CTF grammar, direct-abstract-declarator can be empty (because of
  * identifier-opt). We take care of that by appending a '?' to each use of
  * "abstractDeclaratorList".
  */
directAbstractDeclarator
  : (
      IDENTIFIER
      | (LPAREN abstractDeclarator RPAREN)
  ) (
      OPENBRAC unaryExpression? CLOSEBRAC
  )?
  ;

pointer
  : POINTER typeQualifierList? -> ^(POINTER typeQualifierList?)
  ;

typeQualifierList
  : typeQualifier+
  ;

typedefName
  : {inTypealiasAlias() || isTypeName(input.LT(1).getText())}? IDENTIFIER { if ((inTypedef() || inTypealiasAlias()) && !isTypeName($IDENTIFIER.text)) { addTypeName($IDENTIFIER.text); } }
  ;

/**
 * What goes in the target part of a typealias.
 *
 * For example, the integer part in:
 * typealias integer {...} := my_new_integer;
 */
typealiasTarget
  : declarationSpecifiers abstractDeclaratorList?
  ;

/**
 * What goes in the alias part of a typealias.
 *
 * For example, the my_new_integer part in:
 * typealias integer {...} := my_new_integer;
 */
typealiasAlias
@init {
    typealiasAliasOn();
}
@after {
    typealiasAliasOff();
}
  : abstractDeclaratorList
  | declarationSpecifiers abstractDeclaratorList?
  ;

typealiasDecl
  : TYPEALIASTOK typealiasTarget TYPE_ASSIGNMENT typealiasAlias
      -> ^(TYPEALIAS
             ^(TYPEALIAS_TARGET typealiasTarget)
             ^(TYPEALIAS_ALIAS typealiasAlias))
  ;

// 2.3 CTF stuff

// TODO: Ajouter ceux qui manquent
ctfKeyword
  : ALIGNTOK
  | EVENTTOK
  | SIGNEDTOK
  | STRINGTOK
  ;

ctfSpecifier
  // event {...}, stream {...}, trace {...}
  : ctfSpecifierHead ctfBody -> ^(ctfSpecifierHead ctfBody)
  // typealias
  | typealiasDecl -> ^(DECLARATION typealiasDecl)
  ;

ctfSpecifierHead
  : EVENTTOK -> EVENT
  | STREAMTOK -> STREAM
  | TRACETOK -> TRACE
  | ENVTOK -> ENV
  | CLOCKTOK -> CLOCK
  | CALLSITETOK -> CALLSITE
  ;

ctfTypeSpecifier
    /* ctfBody can return an empty tree if the body is empty */
  : FLOATINGPOINTTOK ctfBody -> ^(FLOATING_POINT ctfBody?)
  | INTEGERTOK ctfBody -> ^(INTEGER ctfBody?)
  | STRINGTOK ctfBody? -> ^(STRING ctfBody?)
  ;

ctfBody
scope Symbols;
@init {
    $Symbols::types = new HashSet<String>();
}
  : LCURL ctfAssignmentExpressionList? RCURL -> ctfAssignmentExpressionList?
  ;

ctfAssignmentExpressionList
  : (ctfAssignmentExpression TERM!)+
  ;

ctfAssignmentExpression
@after {
    if (inTypedef()) {
        typedefOff();
    }
}
  : left=unaryExpression
      ( assignment=ASSIGNMENT right1=unaryExpression
          -> ^(CTF_EXPRESSION_VAL
                 ^(CTF_LEFT $left)
                 ^(CTF_RIGHT $right1))
      | type_assignment=TYPE_ASSIGNMENT right2=typeSpecifier
          -> ^(CTF_EXPRESSION_TYPE
                 ^(CTF_LEFT $left)
                 ^(CTF_RIGHT ^(TYPE_SPECIFIER_LIST $right2)))
      )
  | (declarationSpecifiers {inTypedef()}? declaratorList)
      -> ^(TYPEDEF declaratorList declarationSpecifiers)
  | typealiasDecl
  ;
