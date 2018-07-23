package net.jr.codegen.lexer;

import java.io.Reader;
import java.io.PushbackReader;
import java.io.IOException;

import java.util.function.Consumer;

import java.util.Set;
import java.util.TreeSet;
import javax.annotation.Generated;

@Generated("parserjunior-codegenerator:1.0-SNAPSHOT")
public class Lexer {

    public enum TokenType {
        tok_eof(false),
        tok_unsigned(false),
        tok_cString(false),
        tok_typedef(false),
        tok_divAssign(false),
        tok_threePoints(false),
        tok_signed(false),
        tok_int(false),
        tok_cFloatingPoint(false),
        tok_plusAssign(false),
        tok_return(false),
        tok_break(false),
        tok_cInteger(false),
        tok_leftSquareBracket(false),
        tok_continue(false),
        tok_rightSquareBracket(false),
        tok_sizeof(false),
        tok_bitwiseNot(false),
        tok_cOctal(false),
        tok_rightShiftEq(false),
        tok_short(false),
        tok_shiftLeft(false),
        tok_logicalOr(false),
        tok_lte(false),
        tok_void(false),
        tok_extern(false),
        tok_double(false),
        tok_long(false),
        tok_do(false),
        tok_float(false),
        tok_cIdentifier(false),
        tok_leftCurlyBrace(false),
        tok_bitwiseOr(false),
        tok_switch(false),
        tok_rightCurlyBrace(false),
        tok_tilde(false),
        tok_if(false),
        tok_bitwiseNotAssign(false),
        tok_minusMinus(false),
        tok_eqEq(false),
        tok_enum(false),
        tok_struct(false),
        tok_union(false),
        tok_char(false),
        tok_volatile(false),
        tok_minusAssign(false),
        tok_arrow(false),
        tok_static(false),
        tok_goto(false),
        tok_default(false),
        tok_moduloEq(false),
        tok_notEq(false),
        tok_leftShiftAssign(false),
        tok_exclamationMark(false),
        tok_typeName(false),
        tok_gte(false),
        tok_shiftRight(false),
        tok_logicalAnd(false),
        tok_bitwiseOrEq(false),
        tok_modulo(false),
        tok_cHexNumber(false),
        tok_bitwiseAnd(false),
        tok_leftParen(false),
        tok_rightParen(false),
        tok_const(false),
        tok_mult(false),
        tok_plus(false),
        tok_comma(false),
        tok_minus(false),
        tok_for(false),
        tok_dot(false),
        tok_slash(false),
        tok_register(false),
        tok_cCharacter(false),
        tok_case(false),
        tok_mulAssign(false),
        tok_auto(false),
        tok_twoPoints(false),
        tok_andEq(false),
        tok_dotComma(false),
        tok_lt(false),
        tok_eq(false),
        tok_gt(false),
        tok_questionMark(false),
        tok_while(false),
        tok_plusPlus(false),
        tok_cBinary(false),
        tok_else(false),
        tok_multilineComment(true),
        tok_lineComment(true),
        tok_Whitespace(true),
        tok_newLine(true)
        ;

        private boolean filtered;

        TokenType(boolean filtered) {
            this.filtered = filtered;
        }

        public boolean isFiltered() {
            return filtered;
        }

    }

    public static class Token {

        private TokenType tokenType;

        private String matchedText;

        private int line;

        private int column;

        public Token(TokenType tokenType, String matchedText, int line, int column) {
            this.tokenType = tokenType;
            this.matchedText = matchedText;
            this.line = line;
            this.column = column;
        }

        TokenType getTokenType() {
            return tokenType;
        }

        String getMatchedText() {
            return matchedText;
        }

        public int getLine() {
            return line;
        }

        public int getColumn() {
            return column;
        }

        public String toString() {
            return tokenType.name()+"@"+line+":"+column;
        }
    }

    private static class LexerState {
        Set<Integer> currentStates = new TreeSet<Integer>();
        Set<Integer> nextStates = new TreeSet<Integer>();
        int startLine = 1, line = 1, startColumn = 1, column = 1;
        TokenType candidate = null;
        String matchedText = "";

        public LexerState() {
            currentStates.add(0);
        }

        public void prepareNextStep() {
            Set<Integer> tmp = currentStates;
            currentStates = nextStates;
            tmp.clear();
            nextStates = tmp;
        }
    }

    public void lex(Reader reader, Consumer<Token> consumer) throws IOException {
        final PushbackReader pbReader = reader instanceof PushbackReader ? (PushbackReader) reader : new PushbackReader(reader);
        final LexerState lexerState = new LexerState();
        while(step(pbReader, lexerState, consumer)) {
            lexerState.prepareNextStep();
        }
    }

    private boolean step(PushbackReader reader, LexerState lexerState, Consumer<Token> consumer) throws IOException {

        int c = reader.read();
        if (c == -1) {
            if(lexerState.candidate == null) {
                if(lexerState.currentStates.size() != 1 || lexerState.currentStates.iterator().next() != 0) {
                    throw new IllegalStateException(String.format("lexical error at line %d, column %d", lexerState.line, lexerState.column));
                }
            } else {
                if(!lexerState.candidate.isFiltered()) {
                    consumer.accept(new Token(lexerState.candidate, lexerState.matchedText, lexerState.startLine, lexerState.startColumn));
                }
            }
            consumer.accept(new Token(TokenType.tok_eof, "", lexerState.line, lexerState.column+1));
            return false;
        }
        int priority = -1;
        TokenType newCandidate = null;
        lexerState.matchedText += (char)c;

        for(int state : lexerState.currentStates) {
            switch(state) {
                case 0 :
                    if(c=='|') {
                        lexerState.nextStates.add(300);
                    }
                    if(c=='!') {
                        lexerState.nextStates.add(-299);
                        if(priority < 1) {
                            priority = 1;
                            newCandidate = TokenType.tok_exclamationMark;
                        }
                    }
                    if(c=='/') {
                        lexerState.nextStates.add(295);
                    }
                    if(c==':') {
                        lexerState.nextStates.add(-294);
                        if(priority < 1) {
                            priority = 1;
                            newCandidate = TokenType.tok_twoPoints;
                        }
                    }
                    if("123456789".indexOf((char)c)>-1) {
                        lexerState.nextStates.add(-290);
                        if(priority < 1) {
                            priority = 1;
                            newCandidate = TokenType.tok_cInteger;
                        }
                    }
                    if(c=='\'') {
                        lexerState.nextStates.add(274);
                    }
                    if(c=='u') {
                        lexerState.nextStates.add(269);
                    }
                    if(c=='-') {
                        lexerState.nextStates.add(267);
                    }
                    if(c=='.') {
                        lexerState.nextStates.add(-266);
                        if(priority < 1) {
                            priority = 1;
                            newCandidate = TokenType.tok_dot;
                        }
                    }
                    if(c=='>') {
                        lexerState.nextStates.add(-265);
                        if(priority < 1) {
                            priority = 1;
                            newCandidate = TokenType.tok_gt;
                        }
                    }
                    if(c=='{') {
                        lexerState.nextStates.add(-264);
                        if(priority < 1) {
                            priority = 1;
                            newCandidate = TokenType.tok_leftCurlyBrace;
                        }
                    }
                    if(c=='s') {
                        lexerState.nextStates.add(258);
                    }
                    if(c=='+') {
                        lexerState.nextStates.add(-257);
                        if(priority < 1) {
                            priority = 1;
                            newCandidate = TokenType.tok_plus;
                        }
                    }
                    if(c=='=') {
                        lexerState.nextStates.add(255);
                    }
                    if(c=='>') {
                        lexerState.nextStates.add(252);
                    }
                    if(c=='d') {
                        lexerState.nextStates.add(250);
                    }
                    if(c=='d') {
                        lexerState.nextStates.add(243);
                    }
                    if(c=='d') {
                        lexerState.nextStates.add(237);
                    }
                    if(c=='v') {
                        lexerState.nextStates.add(229);
                    }
                    if("_abcdefghiklmnopqrstuvwxyzABCDEFGHIKLMNOPQRSTUVWXYZ".indexOf((char)c)>-1) {
                        lexerState.nextStates.add(-228);
                        if(priority < 0) {
                            priority = 0;
                            newCandidate = TokenType.tok_cIdentifier;
                        }
                    }
                    if(c=='g') {
                        lexerState.nextStates.add(224);
                    }
                    if(c=='/') {
                        lexerState.nextStates.add(221);
                    }
                    if(c=='/') {
                        lexerState.nextStates.add(219);
                    }
                    if(c=='.') {
                        lexerState.nextStates.add(218);
                    }
                    if(c==',') {
                        lexerState.nextStates.add(-217);
                        if(priority < 1) {
                            priority = 1;
                            newCandidate = TokenType.tok_comma;
                        }
                    }
                    if(c=='e') {
                        lexerState.nextStates.add(213);
                    }
                    if(c=='0') {
                        lexerState.nextStates.add(212);
                    }
                    if(c=='[') {
                        lexerState.nextStates.add(-211);
                        if(priority < 1) {
                            priority = 1;
                            newCandidate = TokenType.tok_leftSquareBracket;
                        }
                    }
                    if(c=='s') {
                        lexerState.nextStates.add(205);
                    }
                    if(c=='t') {
                        lexerState.nextStates.add(198);
                    }
                    if(c=='<') {
                        lexerState.nextStates.add(196);
                    }
                    if(c=='*') {
                        lexerState.nextStates.add(-195);
                        if(priority < 1) {
                            priority = 1;
                            newCandidate = TokenType.tok_mult;
                        }
                    }
                    if(c=='?') {
                        lexerState.nextStates.add(-194);
                        if(priority < 1) {
                            priority = 1;
                            newCandidate = TokenType.tok_questionMark;
                        }
                    }
                    if(c=='s') {
                        lexerState.nextStates.add(188);
                    }
                    if(c=='\r') {
                        lexerState.nextStates.add(187);
                    }
                    if(c=='!') {
                        lexerState.nextStates.add(185);
                    }
                    if(c=='|') {
                        lexerState.nextStates.add(-184);
                        if(priority < 1) {
                            priority = 1;
                            newCandidate = TokenType.tok_bitwiseOr;
                        }
                    }
                    if(c=='c') {
                        lexerState.nextStates.add(179);
                    }
                    if(c=='f') {
                        lexerState.nextStates.add(176);
                    }
                    if(c=='\"') {
                        lexerState.nextStates.add(172);
                    }
                    if(c=='\n') {
                        lexerState.nextStates.add(-171);
                        if(priority < 1) {
                            priority = 1;
                            newCandidate = TokenType.tok_newLine;
                        }
                    }
                    if(c=='s') {
                        lexerState.nextStates.add(165);
                    }
                    if(c==';') {
                        lexerState.nextStates.add(-164);
                        if(priority < 1) {
                            priority = 1;
                            newCandidate = TokenType.tok_dotComma;
                        }
                    }
                    if(c=='/') {
                        lexerState.nextStates.add(-163);
                        if(priority < 1) {
                            priority = 1;
                            newCandidate = TokenType.tok_slash;
                        }
                    }
                    if(c=='&') {
                        lexerState.nextStates.add(161);
                    }
                    if(c=='r') {
                        lexerState.nextStates.add(153);
                    }
                    if(c=='~') {
                        lexerState.nextStates.add(-152);
                        if(priority < 1) {
                            priority = 1;
                            newCandidate = TokenType.tok_tilde;
                        }
                    }
                    if(c=='0') {
                        lexerState.nextStates.add(149);
                    }
                    if(c=='+') {
                        lexerState.nextStates.add(147);
                    }
                    if(c=='.') {
                        lexerState.nextStates.add(144);
                    }
                    if(c=='e') {
                        lexerState.nextStates.add(140);
                    }
                    if(c=='&') {
                        lexerState.nextStates.add(-139);
                        if(priority < 1) {
                            priority = 1;
                            newCandidate = TokenType.tok_bitwiseAnd;
                        }
                    }
                    if(c=='0') {
                        lexerState.nextStates.add(-135);
                        if(priority < 1) {
                            priority = 1;
                            newCandidate = TokenType.tok_cInteger;
                        }
                    }
                    if(c=='-') {
                        lexerState.nextStates.add(-134);
                        if(priority < 1) {
                            priority = 1;
                            newCandidate = TokenType.tok_minus;
                        }
                    }
                    if(c=='%') {
                        lexerState.nextStates.add(132);
                    }
                    if(c=='}') {
                        lexerState.nextStates.add(-131);
                        if(priority < 1) {
                            priority = 1;
                            newCandidate = TokenType.tok_rightCurlyBrace;
                        }
                    }
                    if(c=='c') {
                        lexerState.nextStates.add(127);
                    }
                    if(c=='0') {
                        lexerState.nextStates.add(121);
                    }
                    if(c=='f') {
                        lexerState.nextStates.add(116);
                    }
                    if(c=='=') {
                        lexerState.nextStates.add(-115);
                        if(priority < 1) {
                            priority = 1;
                            newCandidate = TokenType.tok_eq;
                        }
                    }
                    if(c=='>') {
                        lexerState.nextStates.add(113);
                    }
                    if(c=='c') {
                        lexerState.nextStates.add(105);
                    }
                    if("01234567".indexOf((char)c)>-1) {
                        lexerState.nextStates.add(-101);
                        if(priority < 1) {
                            priority = 1;
                            newCandidate = TokenType.tok_cOctal;
                        }
                    }
                    if(c==']') {
                        lexerState.nextStates.add(-100);
                        if(priority < 1) {
                            priority = 1;
                            newCandidate = TokenType.tok_rightSquareBracket;
                        }
                    }
                    if(c=='-') {
                        lexerState.nextStates.add(98);
                    }
                    if(c=='a') {
                        lexerState.nextStates.add(94);
                    }
                    if(c=='>') {
                        lexerState.nextStates.add(92);
                    }
                    if(c==')') {
                        lexerState.nextStates.add(-91);
                        if(priority < 1) {
                            priority = 1;
                            newCandidate = TokenType.tok_rightParen;
                        }
                    }
                    if(c=='<') {
                        lexerState.nextStates.add(-90);
                        if(priority < 1) {
                            priority = 1;
                            newCandidate = TokenType.tok_lt;
                        }
                    }
                    if(c=='v') {
                        lexerState.nextStates.add(86);
                    }
                    if(c=='%') {
                        lexerState.nextStates.add(-85);
                        if(priority < 1) {
                            priority = 1;
                            newCandidate = TokenType.tok_modulo;
                        }
                    }
                    if(c=='(') {
                        lexerState.nextStates.add(-84);
                        if(priority < 1) {
                            priority = 1;
                            newCandidate = TokenType.tok_leftParen;
                        }
                    }
                    if(c=='^') {
                        lexerState.nextStates.add(82);
                    }
                    if(c=='s') {
                        lexerState.nextStates.add(76);
                    }
                    if(c=='l') {
                        lexerState.nextStates.add(72);
                    }
                    if(c=='<') {
                        lexerState.nextStates.add(69);
                    }
                    if(c=='i') {
                        lexerState.nextStates.add(66);
                    }
                    if(c=='^') {
                        lexerState.nextStates.add(-65);
                        if(priority < 1) {
                            priority = 1;
                            newCandidate = TokenType.tok_bitwiseNot;
                        }
                    }
                    if(c=='s') {
                        lexerState.nextStates.add(60);
                    }
                    if(c=='-') {
                        lexerState.nextStates.add(58);
                    }
                    if(c=='w') {
                        lexerState.nextStates.add(53);
                    }
                    if(c=='r') {
                        lexerState.nextStates.add(47);
                    }
                    if(c=='c') {
                        lexerState.nextStates.add(43);
                    }
                    if(" \u00A0\u2007\u202F\u000B\u001C\u001D\u001E\u001F\t\f\r".indexOf((char)c)>-1) {
                        lexerState.nextStates.add(-42);
                        if(priority < 1) {
                            priority = 1;
                            newCandidate = TokenType.tok_Whitespace;
                        }
                    }
                    if(c=='e') {
                        lexerState.nextStates.add(36);
                    }
                    if(c=='&') {
                        lexerState.nextStates.add(34);
                    }
                    if(c=='u') {
                        lexerState.nextStates.add(26);
                    }
                    if("0123456789".indexOf((char)c)>-1) {
                        lexerState.nextStates.add(16);
                    }
                    if(c=='*') {
                        lexerState.nextStates.add(14);
                    }
                    if(c=='+') {
                        lexerState.nextStates.add(12);
                    }
                    if(c=='b') {
                        lexerState.nextStates.add(7);
                    }
                    if(c=='<') {
                        lexerState.nextStates.add(5);
                    }
                    if(c=='i') {
                        lexerState.nextStates.add(3);
                    }
                    if(c=='|') {
                        lexerState.nextStates.add(1);
                    }
                    break;
                case 1 :
                    if(c=='|') {
                        lexerState.nextStates.add(-2);
                        if(priority < 2) {
                            priority = 2;
                            newCandidate = TokenType.tok_logicalOr;
                        }
                    }
                    break;
                case 3 :
                    if(c=='f') {
                        lexerState.nextStates.add(-4);
                        if(priority < 2) {
                            priority = 2;
                            newCandidate = TokenType.tok_if;
                        }
                    }
                    break;
                case 5 :
                    if(c=='<') {
                        lexerState.nextStates.add(-6);
                        if(priority < 2) {
                            priority = 2;
                            newCandidate = TokenType.tok_shiftLeft;
                        }
                    }
                    break;
                case 7 :
                    if(c=='r') {
                        lexerState.nextStates.add(8);
                    }
                    break;
                case 8 :
                    if(c=='e') {
                        lexerState.nextStates.add(9);
                    }
                    break;
                case 9 :
                    if(c=='a') {
                        lexerState.nextStates.add(10);
                    }
                    break;
                case 10 :
                    if(c=='k') {
                        lexerState.nextStates.add(-11);
                        if(priority < 2) {
                            priority = 2;
                            newCandidate = TokenType.tok_break;
                        }
                    }
                    break;
                case 12 :
                    if(c=='+') {
                        lexerState.nextStates.add(-13);
                        if(priority < 2) {
                            priority = 2;
                            newCandidate = TokenType.tok_plusPlus;
                        }
                    }
                    break;
                case 14 :
                    if(c=='=') {
                        lexerState.nextStates.add(-15);
                        if(priority < 2) {
                            priority = 2;
                            newCandidate = TokenType.tok_mulAssign;
                        }
                    }
                    break;
                case 16 :
                    if(c=='.') {
                        lexerState.nextStates.add(-17);
                        if(priority < 1) {
                            priority = 1;
                            newCandidate = TokenType.tok_cFloatingPoint;
                        }
                    }
                    if("0123456789".indexOf((char)c)>-1) {
                        lexerState.nextStates.add(16);
                    }
                    break;
                case -17 :
                    if((c=='E')||(c=='e')) {
                        lexerState.nextStates.add(22);
                    }
                    if("lfLF".indexOf((char)c)>-1) {
                        lexerState.nextStates.add(-18);
                        if(priority < 1) {
                            priority = 1;
                            newCandidate = TokenType.tok_cFloatingPoint;
                        }
                    }
                    if("0123456789".indexOf((char)c)>-1) {
                        lexerState.nextStates.add(-17);
                        if(priority < 1) {
                            priority = 1;
                            newCandidate = TokenType.tok_cFloatingPoint;
                        }
                    }
                    break;
                case -18 :
                    if((c=='E')||(c=='e')) {
                        lexerState.nextStates.add(19);
                    }
                    break;
                case 19 :
                    if("123456789".indexOf((char)c)>-1) {
                        lexerState.nextStates.add(-21);
                        if(priority < 1) {
                            priority = 1;
                            newCandidate = TokenType.tok_cFloatingPoint;
                        }
                    }
                    if((c=='+')||(c=='-')) {
                        lexerState.nextStates.add(20);
                    }
                    break;
                case 20 :
                    if("123456789".indexOf((char)c)>-1) {
                        lexerState.nextStates.add(-21);
                        if(priority < 1) {
                            priority = 1;
                            newCandidate = TokenType.tok_cFloatingPoint;
                        }
                    }
                    break;
                case -21 :
                    if("0123456789".indexOf((char)c)>-1) {
                        lexerState.nextStates.add(-21);
                        if(priority < 1) {
                            priority = 1;
                            newCandidate = TokenType.tok_cFloatingPoint;
                        }
                    }
                    break;
                case 22 :
                    if((c=='+')||(c=='-')) {
                        lexerState.nextStates.add(25);
                    }
                    if("123456789".indexOf((char)c)>-1) {
                        lexerState.nextStates.add(-23);
                        if(priority < 1) {
                            priority = 1;
                            newCandidate = TokenType.tok_cFloatingPoint;
                        }
                    }
                    break;
                case -23 :
                    if("lfLF".indexOf((char)c)>-1) {
                        lexerState.nextStates.add(-24);
                        if(priority < 1) {
                            priority = 1;
                            newCandidate = TokenType.tok_cFloatingPoint;
                        }
                    }
                    if("0123456789".indexOf((char)c)>-1) {
                        lexerState.nextStates.add(-23);
                        if(priority < 1) {
                            priority = 1;
                            newCandidate = TokenType.tok_cFloatingPoint;
                        }
                    }
                    break;
                case 25 :
                    if("123456789".indexOf((char)c)>-1) {
                        lexerState.nextStates.add(-23);
                        if(priority < 1) {
                            priority = 1;
                            newCandidate = TokenType.tok_cFloatingPoint;
                        }
                    }
                    break;
                case 26 :
                    if(c=='n') {
                        lexerState.nextStates.add(27);
                    }
                    break;
                case 27 :
                    if(c=='s') {
                        lexerState.nextStates.add(28);
                    }
                    break;
                case 28 :
                    if(c=='i') {
                        lexerState.nextStates.add(29);
                    }
                    break;
                case 29 :
                    if(c=='g') {
                        lexerState.nextStates.add(30);
                    }
                    break;
                case 30 :
                    if(c=='n') {
                        lexerState.nextStates.add(31);
                    }
                    break;
                case 31 :
                    if(c=='e') {
                        lexerState.nextStates.add(32);
                    }
                    break;
                case 32 :
                    if(c=='d') {
                        lexerState.nextStates.add(-33);
                        if(priority < 2) {
                            priority = 2;
                            newCandidate = TokenType.tok_unsigned;
                        }
                    }
                    break;
                case 34 :
                    if(c=='=') {
                        lexerState.nextStates.add(-35);
                        if(priority < 2) {
                            priority = 2;
                            newCandidate = TokenType.tok_andEq;
                        }
                    }
                    break;
                case 36 :
                    if(c=='x') {
                        lexerState.nextStates.add(37);
                    }
                    break;
                case 37 :
                    if(c=='t') {
                        lexerState.nextStates.add(38);
                    }
                    break;
                case 38 :
                    if(c=='e') {
                        lexerState.nextStates.add(39);
                    }
                    break;
                case 39 :
                    if(c=='r') {
                        lexerState.nextStates.add(40);
                    }
                    break;
                case 40 :
                    if(c=='n') {
                        lexerState.nextStates.add(-41);
                        if(priority < 2) {
                            priority = 2;
                            newCandidate = TokenType.tok_extern;
                        }
                    }
                    break;
                case -42 :
                    if(" \u00A0\u2007\u202F\u000B\u001C\u001D\u001E\u001F\t\f\r".indexOf((char)c)>-1) {
                        lexerState.nextStates.add(-42);
                        if(priority < 1) {
                            priority = 1;
                            newCandidate = TokenType.tok_Whitespace;
                        }
                    }
                    break;
                case 43 :
                    if(c=='a') {
                        lexerState.nextStates.add(44);
                    }
                    break;
                case 44 :
                    if(c=='s') {
                        lexerState.nextStates.add(45);
                    }
                    break;
                case 45 :
                    if(c=='e') {
                        lexerState.nextStates.add(-46);
                        if(priority < 2) {
                            priority = 2;
                            newCandidate = TokenType.tok_case;
                        }
                    }
                    break;
                case 47 :
                    if(c=='e') {
                        lexerState.nextStates.add(48);
                    }
                    break;
                case 48 :
                    if(c=='t') {
                        lexerState.nextStates.add(49);
                    }
                    break;
                case 49 :
                    if(c=='u') {
                        lexerState.nextStates.add(50);
                    }
                    break;
                case 50 :
                    if(c=='r') {
                        lexerState.nextStates.add(51);
                    }
                    break;
                case 51 :
                    if(c=='n') {
                        lexerState.nextStates.add(-52);
                        if(priority < 2) {
                            priority = 2;
                            newCandidate = TokenType.tok_return;
                        }
                    }
                    break;
                case 53 :
                    if(c=='h') {
                        lexerState.nextStates.add(54);
                    }
                    break;
                case 54 :
                    if(c=='i') {
                        lexerState.nextStates.add(55);
                    }
                    break;
                case 55 :
                    if(c=='l') {
                        lexerState.nextStates.add(56);
                    }
                    break;
                case 56 :
                    if(c=='e') {
                        lexerState.nextStates.add(-57);
                        if(priority < 2) {
                            priority = 2;
                            newCandidate = TokenType.tok_while;
                        }
                    }
                    break;
                case 58 :
                    if(c=='-') {
                        lexerState.nextStates.add(-59);
                        if(priority < 2) {
                            priority = 2;
                            newCandidate = TokenType.tok_minusMinus;
                        }
                    }
                    break;
                case 60 :
                    if(c=='h') {
                        lexerState.nextStates.add(61);
                    }
                    break;
                case 61 :
                    if(c=='o') {
                        lexerState.nextStates.add(62);
                    }
                    break;
                case 62 :
                    if(c=='r') {
                        lexerState.nextStates.add(63);
                    }
                    break;
                case 63 :
                    if(c=='t') {
                        lexerState.nextStates.add(-64);
                        if(priority < 2) {
                            priority = 2;
                            newCandidate = TokenType.tok_short;
                        }
                    }
                    break;
                case 66 :
                    if(c=='n') {
                        lexerState.nextStates.add(67);
                    }
                    break;
                case 67 :
                    if(c=='t') {
                        lexerState.nextStates.add(-68);
                        if(priority < 2) {
                            priority = 2;
                            newCandidate = TokenType.tok_int;
                        }
                    }
                    break;
                case 69 :
                    if(c=='<') {
                        lexerState.nextStates.add(70);
                    }
                    break;
                case 70 :
                    if(c=='=') {
                        lexerState.nextStates.add(-71);
                        if(priority < 2) {
                            priority = 2;
                            newCandidate = TokenType.tok_leftShiftAssign;
                        }
                    }
                    break;
                case 72 :
                    if(c=='o') {
                        lexerState.nextStates.add(73);
                    }
                    break;
                case 73 :
                    if(c=='n') {
                        lexerState.nextStates.add(74);
                    }
                    break;
                case 74 :
                    if(c=='g') {
                        lexerState.nextStates.add(-75);
                        if(priority < 2) {
                            priority = 2;
                            newCandidate = TokenType.tok_long;
                        }
                    }
                    break;
                case 76 :
                    if(c=='i') {
                        lexerState.nextStates.add(77);
                    }
                    break;
                case 77 :
                    if(c=='z') {
                        lexerState.nextStates.add(78);
                    }
                    break;
                case 78 :
                    if(c=='e') {
                        lexerState.nextStates.add(79);
                    }
                    break;
                case 79 :
                    if(c=='o') {
                        lexerState.nextStates.add(80);
                    }
                    break;
                case 80 :
                    if(c=='f') {
                        lexerState.nextStates.add(-81);
                        if(priority < 2) {
                            priority = 2;
                            newCandidate = TokenType.tok_sizeof;
                        }
                    }
                    break;
                case 82 :
                    if(c=='=') {
                        lexerState.nextStates.add(-83);
                        if(priority < 2) {
                            priority = 2;
                            newCandidate = TokenType.tok_bitwiseNotAssign;
                        }
                    }
                    break;
                case 86 :
                    if(c=='o') {
                        lexerState.nextStates.add(87);
                    }
                    break;
                case 87 :
                    if(c=='i') {
                        lexerState.nextStates.add(88);
                    }
                    break;
                case 88 :
                    if(c=='d') {
                        lexerState.nextStates.add(-89);
                        if(priority < 2) {
                            priority = 2;
                            newCandidate = TokenType.tok_void;
                        }
                    }
                    break;
                case 92 :
                    if(c=='=') {
                        lexerState.nextStates.add(-93);
                        if(priority < 2) {
                            priority = 2;
                            newCandidate = TokenType.tok_gte;
                        }
                    }
                    break;
                case 94 :
                    if(c=='u') {
                        lexerState.nextStates.add(95);
                    }
                    break;
                case 95 :
                    if(c=='t') {
                        lexerState.nextStates.add(96);
                    }
                    break;
                case 96 :
                    if(c=='o') {
                        lexerState.nextStates.add(-97);
                        if(priority < 2) {
                            priority = 2;
                            newCandidate = TokenType.tok_auto;
                        }
                    }
                    break;
                case 98 :
                    if(c=='=') {
                        lexerState.nextStates.add(-99);
                        if(priority < 2) {
                            priority = 2;
                            newCandidate = TokenType.tok_minusAssign;
                        }
                    }
                    break;
                case -101 :
                    if((c=='L')||(c=='l')) {
                        lexerState.nextStates.add(-104);
                        if(priority < 1) {
                            priority = 1;
                            newCandidate = TokenType.tok_cOctal;
                        }
                    }
                    if("01234567".indexOf((char)c)>-1) {
                        lexerState.nextStates.add(-101);
                        if(priority < 1) {
                            priority = 1;
                            newCandidate = TokenType.tok_cOctal;
                        }
                    }
                    if((c=='U')||(c=='u')) {
                        lexerState.nextStates.add(-102);
                        if(priority < 1) {
                            priority = 1;
                            newCandidate = TokenType.tok_cOctal;
                        }
                    }
                    break;
                case -102 :
                    if((c=='L')||(c=='l')) {
                        lexerState.nextStates.add(-103);
                        if(priority < 1) {
                            priority = 1;
                            newCandidate = TokenType.tok_cOctal;
                        }
                    }
                    break;
                case -104 :
                    if((c=='U')||(c=='u')) {
                        lexerState.nextStates.add(-103);
                        if(priority < 1) {
                            priority = 1;
                            newCandidate = TokenType.tok_cOctal;
                        }
                    }
                    break;
                case 105 :
                    if(c=='o') {
                        lexerState.nextStates.add(106);
                    }
                    break;
                case 106 :
                    if(c=='n') {
                        lexerState.nextStates.add(107);
                    }
                    break;
                case 107 :
                    if(c=='t') {
                        lexerState.nextStates.add(108);
                    }
                    break;
                case 108 :
                    if(c=='i') {
                        lexerState.nextStates.add(109);
                    }
                    break;
                case 109 :
                    if(c=='n') {
                        lexerState.nextStates.add(110);
                    }
                    break;
                case 110 :
                    if(c=='u') {
                        lexerState.nextStates.add(111);
                    }
                    break;
                case 111 :
                    if(c=='e') {
                        lexerState.nextStates.add(-112);
                        if(priority < 2) {
                            priority = 2;
                            newCandidate = TokenType.tok_continue;
                        }
                    }
                    break;
                case 113 :
                    if(c=='>') {
                        lexerState.nextStates.add(-114);
                        if(priority < 2) {
                            priority = 2;
                            newCandidate = TokenType.tok_shiftRight;
                        }
                    }
                    break;
                case 116 :
                    if(c=='l') {
                        lexerState.nextStates.add(117);
                    }
                    break;
                case 117 :
                    if(c=='o') {
                        lexerState.nextStates.add(118);
                    }
                    break;
                case 118 :
                    if(c=='a') {
                        lexerState.nextStates.add(119);
                    }
                    break;
                case 119 :
                    if(c=='t') {
                        lexerState.nextStates.add(-120);
                        if(priority < 2) {
                            priority = 2;
                            newCandidate = TokenType.tok_float;
                        }
                    }
                    break;
                case 121 :
                    if(c=='x') {
                        lexerState.nextStates.add(122);
                    }
                    break;
                case 122 :
                    if("0123456789abcdefABCDEF".indexOf((char)c)>-1) {
                        lexerState.nextStates.add(-123);
                        if(priority < 1) {
                            priority = 1;
                            newCandidate = TokenType.tok_cHexNumber;
                        }
                    }
                    break;
                case -123 :
                    if((c=='U')||(c=='u')) {
                        lexerState.nextStates.add(-126);
                        if(priority < 1) {
                            priority = 1;
                            newCandidate = TokenType.tok_cHexNumber;
                        }
                    }
                    if("0123456789abcdefABCDEF".indexOf((char)c)>-1) {
                        lexerState.nextStates.add(-123);
                        if(priority < 1) {
                            priority = 1;
                            newCandidate = TokenType.tok_cHexNumber;
                        }
                    }
                    if((c=='L')||(c=='l')) {
                        lexerState.nextStates.add(-124);
                        if(priority < 1) {
                            priority = 1;
                            newCandidate = TokenType.tok_cHexNumber;
                        }
                    }
                    break;
                case -124 :
                    if((c=='U')||(c=='u')) {
                        lexerState.nextStates.add(-125);
                        if(priority < 1) {
                            priority = 1;
                            newCandidate = TokenType.tok_cHexNumber;
                        }
                    }
                    break;
                case -126 :
                    if((c=='L')||(c=='l')) {
                        lexerState.nextStates.add(-125);
                        if(priority < 1) {
                            priority = 1;
                            newCandidate = TokenType.tok_cHexNumber;
                        }
                    }
                    break;
                case 127 :
                    if(c=='h') {
                        lexerState.nextStates.add(128);
                    }
                    break;
                case 128 :
                    if(c=='a') {
                        lexerState.nextStates.add(129);
                    }
                    break;
                case 129 :
                    if(c=='r') {
                        lexerState.nextStates.add(-130);
                        if(priority < 2) {
                            priority = 2;
                            newCandidate = TokenType.tok_char;
                        }
                    }
                    break;
                case 132 :
                    if(c=='=') {
                        lexerState.nextStates.add(-133);
                        if(priority < 2) {
                            priority = 2;
                            newCandidate = TokenType.tok_moduloEq;
                        }
                    }
                    break;
                case -135 :
                    if((c=='U')||(c=='u')) {
                        lexerState.nextStates.add(-138);
                        if(priority < 1) {
                            priority = 1;
                            newCandidate = TokenType.tok_cInteger;
                        }
                    }
                    if((c=='L')||(c=='l')) {
                        lexerState.nextStates.add(-136);
                        if(priority < 1) {
                            priority = 1;
                            newCandidate = TokenType.tok_cInteger;
                        }
                    }
                    break;
                case -136 :
                    if((c=='U')||(c=='u')) {
                        lexerState.nextStates.add(-137);
                        if(priority < 1) {
                            priority = 1;
                            newCandidate = TokenType.tok_cInteger;
                        }
                    }
                    break;
                case -138 :
                    if((c=='L')||(c=='l')) {
                        lexerState.nextStates.add(-137);
                        if(priority < 1) {
                            priority = 1;
                            newCandidate = TokenType.tok_cInteger;
                        }
                    }
                    break;
                case 140 :
                    if(c=='n') {
                        lexerState.nextStates.add(141);
                    }
                    break;
                case 141 :
                    if(c=='u') {
                        lexerState.nextStates.add(142);
                    }
                    break;
                case 142 :
                    if(c=='m') {
                        lexerState.nextStates.add(-143);
                        if(priority < 2) {
                            priority = 2;
                            newCandidate = TokenType.tok_enum;
                        }
                    }
                    break;
                case 144 :
                    if(c=='.') {
                        lexerState.nextStates.add(145);
                    }
                    break;
                case 145 :
                    if(c=='.') {
                        lexerState.nextStates.add(-146);
                        if(priority < 2) {
                            priority = 2;
                            newCandidate = TokenType.tok_threePoints;
                        }
                    }
                    break;
                case 147 :
                    if(c=='=') {
                        lexerState.nextStates.add(-148);
                        if(priority < 2) {
                            priority = 2;
                            newCandidate = TokenType.tok_plusAssign;
                        }
                    }
                    break;
                case 149 :
                    if((c=='B')||(c=='b')) {
                        lexerState.nextStates.add(150);
                    }
                    break;
                case 150 :
                    if((c=='0')||(c=='1')) {
                        lexerState.nextStates.add(-151);
                        if(priority < 1) {
                            priority = 1;
                            newCandidate = TokenType.tok_cBinary;
                        }
                    }
                    break;
                case -151 :
                    if((c=='0')||(c=='1')) {
                        lexerState.nextStates.add(-151);
                        if(priority < 1) {
                            priority = 1;
                            newCandidate = TokenType.tok_cBinary;
                        }
                    }
                    break;
                case 153 :
                    if(c=='e') {
                        lexerState.nextStates.add(154);
                    }
                    break;
                case 154 :
                    if(c=='g') {
                        lexerState.nextStates.add(155);
                    }
                    break;
                case 155 :
                    if(c=='i') {
                        lexerState.nextStates.add(156);
                    }
                    break;
                case 156 :
                    if(c=='s') {
                        lexerState.nextStates.add(157);
                    }
                    break;
                case 157 :
                    if(c=='t') {
                        lexerState.nextStates.add(158);
                    }
                    break;
                case 158 :
                    if(c=='e') {
                        lexerState.nextStates.add(159);
                    }
                    break;
                case 159 :
                    if(c=='r') {
                        lexerState.nextStates.add(-160);
                        if(priority < 2) {
                            priority = 2;
                            newCandidate = TokenType.tok_register;
                        }
                    }
                    break;
                case 161 :
                    if(c=='&') {
                        lexerState.nextStates.add(-162);
                        if(priority < 2) {
                            priority = 2;
                            newCandidate = TokenType.tok_logicalAnd;
                        }
                    }
                    break;
                case 165 :
                    if(c=='t') {
                        lexerState.nextStates.add(166);
                    }
                    break;
                case 166 :
                    if(c=='r') {
                        lexerState.nextStates.add(167);
                    }
                    break;
                case 167 :
                    if(c=='u') {
                        lexerState.nextStates.add(168);
                    }
                    break;
                case 168 :
                    if(c=='c') {
                        lexerState.nextStates.add(169);
                    }
                    break;
                case 169 :
                    if(c=='t') {
                        lexerState.nextStates.add(-170);
                        if(priority < 2) {
                            priority = 2;
                            newCandidate = TokenType.tok_struct;
                        }
                    }
                    break;
                case 172 :
                    if(c=='\\') {
                        lexerState.nextStates.add(175);
                    }
                    if("\r\n".indexOf((char)c)>-1) {
                        lexerState.nextStates.add(174);
                    }
                    if(!(c=='\"')) {
                        lexerState.nextStates.add(172);
                    }
                    if(c=='\"') {
                        lexerState.nextStates.add(-173);
                        if(priority < 1) {
                            priority = 1;
                            newCandidate = TokenType.tok_cString;
                        }
                    }
                    break;
                case 175 :
                    if(true) {
                        lexerState.nextStates.add(172);
                    }
                    break;
                case 176 :
                    if(c=='o') {
                        lexerState.nextStates.add(177);
                    }
                    break;
                case 177 :
                    if(c=='r') {
                        lexerState.nextStates.add(-178);
                        if(priority < 2) {
                            priority = 2;
                            newCandidate = TokenType.tok_for;
                        }
                    }
                    break;
                case 179 :
                    if(c=='o') {
                        lexerState.nextStates.add(180);
                    }
                    break;
                case 180 :
                    if(c=='n') {
                        lexerState.nextStates.add(181);
                    }
                    break;
                case 181 :
                    if(c=='s') {
                        lexerState.nextStates.add(182);
                    }
                    break;
                case 182 :
                    if(c=='t') {
                        lexerState.nextStates.add(-183);
                        if(priority < 2) {
                            priority = 2;
                            newCandidate = TokenType.tok_const;
                        }
                    }
                    break;
                case 185 :
                    if(c=='=') {
                        lexerState.nextStates.add(-186);
                        if(priority < 2) {
                            priority = 2;
                            newCandidate = TokenType.tok_notEq;
                        }
                    }
                    break;
                case 187 :
                    if(c=='\n') {
                        lexerState.nextStates.add(-171);
                        if(priority < 1) {
                            priority = 1;
                            newCandidate = TokenType.tok_newLine;
                        }
                    }
                    break;
                case 188 :
                    if(c=='i') {
                        lexerState.nextStates.add(189);
                    }
                    break;
                case 189 :
                    if(c=='g') {
                        lexerState.nextStates.add(190);
                    }
                    break;
                case 190 :
                    if(c=='n') {
                        lexerState.nextStates.add(191);
                    }
                    break;
                case 191 :
                    if(c=='e') {
                        lexerState.nextStates.add(192);
                    }
                    break;
                case 192 :
                    if(c=='d') {
                        lexerState.nextStates.add(-193);
                        if(priority < 2) {
                            priority = 2;
                            newCandidate = TokenType.tok_signed;
                        }
                    }
                    break;
                case 196 :
                    if(c=='=') {
                        lexerState.nextStates.add(-197);
                        if(priority < 2) {
                            priority = 2;
                            newCandidate = TokenType.tok_lte;
                        }
                    }
                    break;
                case 198 :
                    if(c=='y') {
                        lexerState.nextStates.add(199);
                    }
                    break;
                case 199 :
                    if(c=='p') {
                        lexerState.nextStates.add(200);
                    }
                    break;
                case 200 :
                    if(c=='e') {
                        lexerState.nextStates.add(201);
                    }
                    break;
                case 201 :
                    if(c=='d') {
                        lexerState.nextStates.add(202);
                    }
                    break;
                case 202 :
                    if(c=='e') {
                        lexerState.nextStates.add(203);
                    }
                    break;
                case 203 :
                    if(c=='f') {
                        lexerState.nextStates.add(-204);
                        if(priority < 2) {
                            priority = 2;
                            newCandidate = TokenType.tok_typedef;
                        }
                    }
                    break;
                case 205 :
                    if(c=='t') {
                        lexerState.nextStates.add(206);
                    }
                    break;
                case 206 :
                    if(c=='a') {
                        lexerState.nextStates.add(207);
                    }
                    break;
                case 207 :
                    if(c=='t') {
                        lexerState.nextStates.add(208);
                    }
                    break;
                case 208 :
                    if(c=='i') {
                        lexerState.nextStates.add(209);
                    }
                    break;
                case 209 :
                    if(c=='c') {
                        lexerState.nextStates.add(-210);
                        if(priority < 2) {
                            priority = 2;
                            newCandidate = TokenType.tok_static;
                        }
                    }
                    break;
                case 212 :
                    if(c=='0') {
                        lexerState.nextStates.add(212);
                    }
                    if("01234567".indexOf((char)c)>-1) {
                        lexerState.nextStates.add(-101);
                        if(priority < 1) {
                            priority = 1;
                            newCandidate = TokenType.tok_cOctal;
                        }
                    }
                    break;
                case 213 :
                    if(c=='l') {
                        lexerState.nextStates.add(214);
                    }
                    break;
                case 214 :
                    if(c=='s') {
                        lexerState.nextStates.add(215);
                    }
                    break;
                case 215 :
                    if(c=='e') {
                        lexerState.nextStates.add(-216);
                        if(priority < 2) {
                            priority = 2;
                            newCandidate = TokenType.tok_else;
                        }
                    }
                    break;
                case 218 :
                    if("0123456789".indexOf((char)c)>-1) {
                        lexerState.nextStates.add(-17);
                        if(priority < 1) {
                            priority = 1;
                            newCandidate = TokenType.tok_cFloatingPoint;
                        }
                    }
                    break;
                case 219 :
                    if(c=='=') {
                        lexerState.nextStates.add(-220);
                        if(priority < 2) {
                            priority = 2;
                            newCandidate = TokenType.tok_divAssign;
                        }
                    }
                    break;
                case 221 :
                    if(c=='/') {
                        lexerState.nextStates.add(222);
                    }
                    break;
                case 222 :
                    if(!(c=='\n')) {
                        lexerState.nextStates.add(222);
                    }
                    if(c=='\n') {
                        lexerState.nextStates.add(-223);
                        if(priority < 1) {
                            priority = 1;
                            newCandidate = TokenType.tok_lineComment;
                        }
                    }
                    break;
                case 224 :
                    if(c=='o') {
                        lexerState.nextStates.add(225);
                    }
                    break;
                case 225 :
                    if(c=='t') {
                        lexerState.nextStates.add(226);
                    }
                    break;
                case 226 :
                    if(c=='o') {
                        lexerState.nextStates.add(-227);
                        if(priority < 2) {
                            priority = 2;
                            newCandidate = TokenType.tok_goto;
                        }
                    }
                    break;
                case -228 :
                    if("_abcdefghiklmnopqrstuvwxyzABCDEFGHIKLMNOPQRSTUVWXYZ0123456789".indexOf((char)c)>-1) {
                        lexerState.nextStates.add(-228);
                        if(priority < 0) {
                            priority = 0;
                            newCandidate = TokenType.tok_cIdentifier;
                        }
                    }
                    break;
                case 229 :
                    if(c=='o') {
                        lexerState.nextStates.add(230);
                    }
                    break;
                case 230 :
                    if(c=='l') {
                        lexerState.nextStates.add(231);
                    }
                    break;
                case 231 :
                    if(c=='a') {
                        lexerState.nextStates.add(232);
                    }
                    break;
                case 232 :
                    if(c=='t') {
                        lexerState.nextStates.add(233);
                    }
                    break;
                case 233 :
                    if(c=='i') {
                        lexerState.nextStates.add(234);
                    }
                    break;
                case 234 :
                    if(c=='l') {
                        lexerState.nextStates.add(235);
                    }
                    break;
                case 235 :
                    if(c=='e') {
                        lexerState.nextStates.add(-236);
                        if(priority < 2) {
                            priority = 2;
                            newCandidate = TokenType.tok_volatile;
                        }
                    }
                    break;
                case 237 :
                    if(c=='o') {
                        lexerState.nextStates.add(238);
                    }
                    break;
                case 238 :
                    if(c=='u') {
                        lexerState.nextStates.add(239);
                    }
                    break;
                case 239 :
                    if(c=='b') {
                        lexerState.nextStates.add(240);
                    }
                    break;
                case 240 :
                    if(c=='l') {
                        lexerState.nextStates.add(241);
                    }
                    break;
                case 241 :
                    if(c=='e') {
                        lexerState.nextStates.add(-242);
                        if(priority < 2) {
                            priority = 2;
                            newCandidate = TokenType.tok_double;
                        }
                    }
                    break;
                case 243 :
                    if(c=='e') {
                        lexerState.nextStates.add(244);
                    }
                    break;
                case 244 :
                    if(c=='f') {
                        lexerState.nextStates.add(245);
                    }
                    break;
                case 245 :
                    if(c=='a') {
                        lexerState.nextStates.add(246);
                    }
                    break;
                case 246 :
                    if(c=='u') {
                        lexerState.nextStates.add(247);
                    }
                    break;
                case 247 :
                    if(c=='l') {
                        lexerState.nextStates.add(248);
                    }
                    break;
                case 248 :
                    if(c=='t') {
                        lexerState.nextStates.add(-249);
                        if(priority < 2) {
                            priority = 2;
                            newCandidate = TokenType.tok_default;
                        }
                    }
                    break;
                case 250 :
                    if(c=='o') {
                        lexerState.nextStates.add(-251);
                        if(priority < 2) {
                            priority = 2;
                            newCandidate = TokenType.tok_do;
                        }
                    }
                    break;
                case 252 :
                    if(c=='>') {
                        lexerState.nextStates.add(253);
                    }
                    break;
                case 253 :
                    if(c=='=') {
                        lexerState.nextStates.add(-254);
                        if(priority < 2) {
                            priority = 2;
                            newCandidate = TokenType.tok_rightShiftEq;
                        }
                    }
                    break;
                case 255 :
                    if(c=='=') {
                        lexerState.nextStates.add(-256);
                        if(priority < 2) {
                            priority = 2;
                            newCandidate = TokenType.tok_eqEq;
                        }
                    }
                    break;
                case 258 :
                    if(c=='w') {
                        lexerState.nextStates.add(259);
                    }
                    break;
                case 259 :
                    if(c=='i') {
                        lexerState.nextStates.add(260);
                    }
                    break;
                case 260 :
                    if(c=='t') {
                        lexerState.nextStates.add(261);
                    }
                    break;
                case 261 :
                    if(c=='c') {
                        lexerState.nextStates.add(262);
                    }
                    break;
                case 262 :
                    if(c=='h') {
                        lexerState.nextStates.add(-263);
                        if(priority < 2) {
                            priority = 2;
                            newCandidate = TokenType.tok_switch;
                        }
                    }
                    break;
                case 267 :
                    if(c=='>') {
                        lexerState.nextStates.add(-268);
                        if(priority < 2) {
                            priority = 2;
                            newCandidate = TokenType.tok_arrow;
                        }
                    }
                    break;
                case 269 :
                    if(c=='n') {
                        lexerState.nextStates.add(270);
                    }
                    break;
                case 270 :
                    if(c=='i') {
                        lexerState.nextStates.add(271);
                    }
                    break;
                case 271 :
                    if(c=='o') {
                        lexerState.nextStates.add(272);
                    }
                    break;
                case 272 :
                    if(c=='n') {
                        lexerState.nextStates.add(-273);
                        if(priority < 2) {
                            priority = 2;
                            newCandidate = TokenType.tok_union;
                        }
                    }
                    break;
                case 274 :
                    if(c=='\\') {
                        lexerState.nextStates.add(277);
                    }
                    if((c>=32 && c<=128)&&(!(c=='\\'))) {
                        lexerState.nextStates.add(275);
                    }
                    break;
                case 275 :
                    if(c=='\'') {
                        lexerState.nextStates.add(-276);
                        if(priority < 1) {
                            priority = 1;
                            newCandidate = TokenType.tok_cCharacter;
                        }
                    }
                    break;
                case 277 :
                    if(c=='x') {
                        lexerState.nextStates.add(289);
                    }
                    if("\"?abfnrtv\\".indexOf((char)c)>-1) {
                        lexerState.nextStates.add(275);
                    }
                    if("01234567".indexOf((char)c)>-1) {
                        lexerState.nextStates.add(287);
                    }
                    if("0123456789abcdefABCDEF".indexOf((char)c)>-1) {
                        lexerState.nextStates.add(289);
                    }
                    if((c=='u')||(c=='U')) {
                        lexerState.nextStates.add(278);
                    }
                    break;
                case 278 :
                    if("0123456789abcdefABCDEF".indexOf((char)c)>-1) {
                        lexerState.nextStates.add(279);
                    }
                    break;
                case 279 :
                    if("0123456789abcdefABCDEF".indexOf((char)c)>-1) {
                        lexerState.nextStates.add(280);
                    }
                    break;
                case 280 :
                    if("0123456789abcdefABCDEF".indexOf((char)c)>-1) {
                        lexerState.nextStates.add(281);
                    }
                    break;
                case 281 :
                    if("0123456789abcdefABCDEF".indexOf((char)c)>-1) {
                        lexerState.nextStates.add(282);
                    }
                    break;
                case 282 :
                    if("0123456789abcdefABCDEF".indexOf((char)c)>-1) {
                        lexerState.nextStates.add(283);
                    }
                    if(c=='\'') {
                        lexerState.nextStates.add(-276);
                        if(priority < 1) {
                            priority = 1;
                            newCandidate = TokenType.tok_cCharacter;
                        }
                    }
                    break;
                case 283 :
                    if("0123456789abcdefABCDEF".indexOf((char)c)>-1) {
                        lexerState.nextStates.add(284);
                    }
                    break;
                case 284 :
                    if("0123456789abcdefABCDEF".indexOf((char)c)>-1) {
                        lexerState.nextStates.add(285);
                    }
                    break;
                case 285 :
                    if("0123456789abcdefABCDEF".indexOf((char)c)>-1) {
                        lexerState.nextStates.add(286);
                    }
                    break;
                case 286 :
                    if(c=='\'') {
                        lexerState.nextStates.add(-276);
                        if(priority < 1) {
                            priority = 1;
                            newCandidate = TokenType.tok_cCharacter;
                        }
                    }
                    break;
                case 287 :
                    if(c=='\'') {
                        lexerState.nextStates.add(-276);
                        if(priority < 1) {
                            priority = 1;
                            newCandidate = TokenType.tok_cCharacter;
                        }
                    }
                    if("01234567".indexOf((char)c)>-1) {
                        lexerState.nextStates.add(288);
                    }
                    break;
                case 288 :
                    if("01234567".indexOf((char)c)>-1) {
                        lexerState.nextStates.add(275);
                    }
                    if(c=='\'') {
                        lexerState.nextStates.add(-276);
                        if(priority < 1) {
                            priority = 1;
                            newCandidate = TokenType.tok_cCharacter;
                        }
                    }
                    break;
                case 289 :
                    if(c=='\'') {
                        lexerState.nextStates.add(-276);
                        if(priority < 1) {
                            priority = 1;
                            newCandidate = TokenType.tok_cCharacter;
                        }
                    }
                    break;
                case -290 :
                    if((c=='L')||(c=='l')) {
                        lexerState.nextStates.add(-293);
                        if(priority < 1) {
                            priority = 1;
                            newCandidate = TokenType.tok_cInteger;
                        }
                    }
                    if("0123456789".indexOf((char)c)>-1) {
                        lexerState.nextStates.add(-290);
                        if(priority < 1) {
                            priority = 1;
                            newCandidate = TokenType.tok_cInteger;
                        }
                    }
                    if((c=='U')||(c=='u')) {
                        lexerState.nextStates.add(-291);
                        if(priority < 1) {
                            priority = 1;
                            newCandidate = TokenType.tok_cInteger;
                        }
                    }
                    break;
                case -291 :
                    if((c=='L')||(c=='l')) {
                        lexerState.nextStates.add(-292);
                        if(priority < 1) {
                            priority = 1;
                            newCandidate = TokenType.tok_cInteger;
                        }
                    }
                    break;
                case -293 :
                    if((c=='U')||(c=='u')) {
                        lexerState.nextStates.add(-292);
                        if(priority < 1) {
                            priority = 1;
                            newCandidate = TokenType.tok_cInteger;
                        }
                    }
                    break;
                case 295 :
                    if(c=='*') {
                        lexerState.nextStates.add(296);
                    }
                    break;
                case 296 :
                    if(c=='*') {
                        lexerState.nextStates.add(297);
                    }
                    if(!(c=='*')) {
                        lexerState.nextStates.add(296);
                    }
                    break;
                case 297 :
                    if(!(c=='/')) {
                        lexerState.nextStates.add(296);
                    }
                    if(c=='/') {
                        lexerState.nextStates.add(-298);
                        if(priority < 1) {
                            priority = 1;
                            newCandidate = TokenType.tok_multilineComment;
                        }
                    }
                    break;
                case 300 :
                    if(c=='=') {
                        lexerState.nextStates.add(-301);
                        if(priority < 2) {
                            priority = 2;
                            newCandidate = TokenType.tok_bitwiseOrEq;
                        }
                    }
                    break;
            }
        }

        if(lexerState.nextStates.isEmpty()) {
            if(lexerState.candidate == null) {
                throw new IllegalStateException(String.format("lexical error at line %d, column %d", lexerState.line, lexerState.column));
            } else {

                if(!lexerState.candidate.isFiltered()) {
                    consumer.accept(new Token(lexerState.candidate, lexerState.matchedText, lexerState.startLine, lexerState.startColumn));
                }

                lexerState.candidate = null;
                lexerState.matchedText = "";
                lexerState.nextStates.add(0);
                reader.unread(c);
                lexerState.startLine = lexerState.line;
                lexerState.startColumn = lexerState.column;
                return true;
            }
        }

        lexerState.candidate = newCandidate;

        if(c=='\n') {
            lexerState.line += 1;
            lexerState.column = 1;
        } else {
            lexerState.column +=1;
        }

        return true;

    }
}
