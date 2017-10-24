package net.jr.parser.impl;

import net.jr.common.Symbol;
import net.jr.parser.Derivation;
import net.jr.parser.Rule;

import java.io.StringWriter;
import java.util.Arrays;
import java.util.function.Consumer;
import java.util.stream.Collectors;

/**
 * Base implementation of a grammar {@link Rule}.
 */
public class BaseRule extends Rule {

    private Derivation derivation = Derivation.None;

    private Consumer<Rule> action;

    private String name;

    private Symbol target;

    private Symbol[] clause;

    public BaseRule(int id, String name, Symbol target, Symbol... clause) {
        setId(id);
        this.name = name;
        this.target = target;
        this.clause = clause;
    }

    public void setAction(Consumer<Rule> action) {
        this.action = action;
    }

    public Consumer<Rule> getAction() {
        return action;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public Symbol[] getClause() {
        return clause;
    }

    public Symbol getTarget() {
        return target;
    }

    public Derivation getDerivation() {
        return derivation;
    }

}
