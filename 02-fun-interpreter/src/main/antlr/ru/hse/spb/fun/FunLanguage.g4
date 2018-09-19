grammar FunLanguage;

@header {
package ru.hse.spb.fun;
}

// FILE = BLOCK
// BLOCK = (STATEMENT)*
// BLOCK_WITH_BRACES = "{" BLOCK "}"
// STATEMENT = FUNCTION | VARIABLE | EXPRESSION | WHILE | IF | ASSIGNMENT | RETURN
// FUNCTION = "fun" IDENTIFIER "(" PARAMETER_NAMES ")" BLOCK_WITH_BRACES
// VARIABLE = "var" IDENTIFIER ("=" EXPRESSION)?
// PARAMETER_NAMES = IDENTIFIER{,}
// WHILE = "while" "(" EXPRESSION ")" BLOCK_WITH_BRACES
// IF = "if" "(" EXPRESSION ")" BLOCK_WITH_BRACES ("else" BLOCK_WITH_BRACES)?
// ASSIGNMENT = IDENTIFIER "=" EXPRESSION
// RETURN = "return" EXPRESSION
// EXPRESSION = FUNCTION_CALL | BINARY_EXPRESSION | IDENTIFIER | LITERAL | "(" EXPRESSION ")"
// FUNCTION_CALL = IDENTIFIER "(" ARGUMENTS ")"
// ARGUMENTS = EXPRESSION{","}
//
//
// BINARY_EXPRESSION = <define-yourself>
// IDENTIFIER = <define-yourself>
// LITERAL = <define-yourself>

file
    : block
    ;

blockWithBracers
    : '{' NEWLINE* SPACES* block SPACES* '}' NEWLINE*
    ;

block
    : (statements+=statement NEWLINE*)*
    ;

// -------------------------- statements  --------------------------

statement
    : SPACES* functionDef NEWLINE
    | SPACES* variable NEWLINE
    | SPACES* intExpr NEWLINE
    | SPACES* logicExpr NEWLINE
    | SPACES* whileLoop NEWLINE
    | SPACES* ifOperator NEWLINE
    | SPACES* assignment NEWLINE
    | SPACES* returnStatement NEWLINE
    ;

functionDef
    : 'fun' SPACES Identifier SPACES* parameterNames SPACES* blockWithBracers
    ;

parameterNames
    : '(' params+=Identifier SPACES* (',' SPACES* params+=Identifier)* SPACES* ')'
    ;

variable
    : 'var' SPACES Identifier SPACES* ('=' SPACES* intExpr)?
    ;

whileLoop
    : 'while' SPACES* '(' SPACES* logicExpr SPACES* ')' SPACES* blockWithBracers
    ;

ifOperator
    : 'if' SPACES* '(' SPACES* logicExpr SPACES* ')' SPACES* thenBlock=blockWithBracers (SPACES* 'else' SPACES* elseBlock=blockWithBracers)?
    ;

assignment
    : Identifier SPACES* '=' SPACES* intExpr
    ;

returnStatement
    : 'return' SPACES intExpr
    ;

// -------------------------- expressions --------------------------

logicExpr
    : logicOrExpr
    | '(' SPACES* logicExpr SPACES* ')'
    ;

intExpr
    : additionExp
    | functionCall
    | Identifier
    | Literal
    | '(' SPACES* intExpr SPACES* ')'
    ;

functionCall
    : Identifier arguments
    ;

arguments
    : '(' SPACES* args+=intExpr (SPACES* ',' SPACES* args+=intExpr)* SPACES* ')'
    ;

// -------------------------- logic --------------------------

logicOrExpr
    : var=logicAndExpr (SPACES* '||' SPACES* vars+=logicAndExpr)*
    ;

logicAndExpr
    : var=atomLogicExpr (SPACES* '&&' SPACES* vars+=atomLogicExpr)*
    ;

atomLogicExpr
    : value=equalityExpr
    | '(' SPACES* exp=logicOrExpr SPACES* ')'
    ;

equalityExpr
    : var1=intExpr SPACES* op=(EQUAL | NOT_EQUAL | LT | LE | GT | GE) SPACES* var2=intExpr
    ;

// ------------------------ arithmetic ------------------------

additionExp
    : var=multiplyExp (SPACES* ops+=(PLUS | MINUS) SPACES* vars+=multiplyExp)*
    ;

multiplyExp
    : var=atomExp (SPACES* ops+=(MULT | DIV | MOD) SPACES* vars+=atomExp)*
    ;

atomExp
    : n=Literal
    | id=Identifier
    | '(' SPACES* exp=intExpr SPACES* ')'
;

// ---------------------------------------------------------------------------------

SINGLE_COMMENT
    : SPACES* '//' ~[\r\n]* [\r\n] -> skip
    ;

MULTI_COMMENT
    : SPACES* '/*' .*? '*/' [\r\n]? -> skip
    ;

fragment Digit
    : [0-9]
    ;

Literal
    : '-'? '0'
    | '-'? [1-9] Digit*
    ;

Identifier
    : ('_'|[a-z])('_'|[a-z0-9])*
    ;

PLUS
    : '+'
    ;

MINUS
    : '-'
    ;

MULT
    : '*'
    ;

DIV
    : '/'
    ;

MOD
    : '%'
    ;

EQUAL
    : '=='
    ;

NOT_EQUAL
    : '!='
    ;

GT
    : '>'
    ;

GE
    : '>='
    ;

LT
    : '<'
    ;

LE
    : '<='
    ;

AND
    : '&&'
    ;

OR
    : '||'
    ;

NEWLINE
    : [\r\n]+
    ;

SPACES
    : ' '+
    ;