package net.jr.grammar.c;


import net.jr.lexer.Lexer;
import net.jr.lexer.LexicalError;
import net.jr.lexer.Token;
import net.jr.parser.ParseError;
import net.jr.parser.Parser;
import net.jr.parser.ast.AstNode;
import net.jr.parser.ast.VisitorHelper;
import net.jr.parser.ast.annotations.After;
import net.jr.parser.impl.ActionTable;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.StringReader;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;


public class CGrammarTest {

    private boolean useCache = true;

    @BeforeClass
    public static void setupClass() {
        System.setProperty("org.slf4j.simpleLogger.defaultLogLevel", "debug");
    }

    @Test
    public void testLex() {
        Lexer l = new CGrammar().createParser().getLexer();
        Iterator<Token> it = l.iterator(new StringReader("for (int i=0;str[i]!='\\0';i+=1) {}"));
        while(it.hasNext()) {
            it.next();
        }
    }

    @Test
    public void testEqAssociativity() {
        System.setProperty("org.slf4j.simpleLogger.defaultLogLevel", "trace");
        new CGrammar().createParser(CGrammar.Statement, false).parse("res = res * 10 ;");
    }


    @Test
    public void testSimple() {
        CGrammar grammar = new CGrammar();
        Parser parser = grammar.createParser(useCache);
        AstNode ast = parser.parse("int main(int argc, char **argv) { return EXIT_SUCCESS;}");
        System.out.println(ast);
    }

    @Test
    public void testIfElse() {
        CGrammar grammar = new CGrammar();
        Parser parser = grammar.createParser(useCache);
        AstNode ast = parser.parse("int main(int argc, char **argv) { if(argc==0) { return false;} else {return true;}}");
        Assert.assertEquals(grammar.getTargetSymbol(), ast.getSymbol());
    }

    @Test
    public void testTypedef() {
        CGrammar grammar = new CGrammar();
        Parser parser = grammar.createParser(useCache);
        parser.parse("typedef unsigned int size_t, *ptr_size_t; ptr_size_t pointer = 0;");
    }

    @Test
    public void testFunc() {
        AstNode root = new CGrammar().createParser(useCache).parse("int recursivefactorial(int n) { return n==0||n==1?1: n * fibo(n-1); }");
        AtomicBoolean called = new AtomicBoolean(false);
        VisitorHelper.visit(root, new Object() {

            @After("FunctionDefinition")
            public void visitFunctionDef(AstNode node) {
                String methodName = node.getChildOfType(CGrammar.Declarator).getChildOfType(CGrammar.DirectDeclarator).getChildren().get(0).asToken().getText();
                Assert.assertEquals("recursivefactorial", methodName);
                called.set(true);
            }

        });
        Assert.assertTrue(called.get());
    }

    @Test(expected = LexicalError.class)
    public void testLexicalError() {
        new CGrammar().createParser(useCache).parse("int aç(void);");
    }

    @Test(expected = ParseError.class)
    public void testParseError() {
        new CGrammar().createParser(useCache).parse("int main(void) pefkpezofk;");
    }
}


