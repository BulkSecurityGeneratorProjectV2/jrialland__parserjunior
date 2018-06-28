package net.jr.cpreproc.procs;

import net.jr.cpreproc.macrodefs.MacroDefinition;

import javax.script.ScriptContext;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import java.util.Map;
import java.util.function.Function;

public class ExpressionEval {


    private static boolean isLegalConstant(String val) {
        return !val.isEmpty() && val.matches("^[_a-zA-Z][_a-zA-Z0-9]*$");
    }

    private static final ScriptEngine scriptEngine = new ScriptEngineManager().getEngineByName("JavaScript");

    public static boolean eval(String expression, Map<String, MacroDefinition> macroDefs) {

        //'defined' may appear as keyword an not a function (i.e 'defined FOO' instead of 'defined(FOO)')
        //so we shamelessly use a regex to force the function-style syntax
        expression = expression.replaceAll("defined\\p{Blank}+([_a-zA-Z][_a-zA-Z0-9]*)", "defined(\"$1\")");


        //FIXME we should definitely use a custom C-style interpreter for that
        //now we just use the built-in javascript interpreter
        //Parser parser = new CGrammar().createParser(CGrammar.AssignmentExpression, true);
        //AstNode root = parser.parse(expression);

        try {

            scriptEngine.getContext().setAttribute("defined", new Function<String, Boolean>() {
                @Override
                public Boolean apply(String s) {
                    return macroDefs.containsKey(s);
                }
            }, ScriptContext.ENGINE_SCOPE);


            Object obj = scriptEngine.eval("!!function(){return(" + expression + ");}();");
            return obj != null && ((Boolean) obj).booleanValue();
        } catch (Exception e) {
            throw new IllegalArgumentException(e);
        }
    }


}
