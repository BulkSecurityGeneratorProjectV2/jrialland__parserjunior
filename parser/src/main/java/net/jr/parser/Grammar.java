package net.jr.parser;


import net.jr.common.Symbol;
import net.jr.lexer.Lexemes;
import net.jr.parser.ast.AstNode;
import net.jr.parser.impl.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.StringWriter;
import java.util.*;
import java.util.function.Consumer;
import java.util.stream.Collectors;

public class Grammar {

    private static final Logger LOGGER = LoggerFactory.getLogger(Grammar.class);

    private static final Logger getLog() {
        return LOGGER;
    }

    private String name;

    private Set<Rule> rules = new HashSet<>();

    private Map<Symbol, Integer> precedenceLevels = new HashMap<>();

    private Symbol targetSymbol;

    public Grammar() {
        this(null);
    }

    public Grammar(String name) {
        this.name = name;
    }

    public Set<Rule> getRules() {
        return rules;
    }

    public interface ComponentsSpecifier {

        /**
         * Ends specifying a rule by giving its left side
         */
        RuleSpecifier def(Symbol... symbols);

    }

    /**
     * Starts specifying a rule by giving its goal
     *
     * @param symbol
     * @return
     */
    public ComponentsSpecifier target(Symbol symbol) {
        return symbols -> addRule(symbol, symbols);
    }

    /**
     * Ends by specifying extrans infos about the rule
     */
    public interface RuleSpecifier {

        /**
         * Code to be executed each time the rule is matched during parsing
         *
         * @param consumer
         * @return
         */
        RuleSpecifier withAction(Consumer<ParsingContext> consumer);

        /**
         * Give a non-default (i.e non-zero) precedence level to the rule.
         *
         * @param level precedence level
         * @return this
         * @see Grammar#getConflictResolutionHint(Rule, Symbol)
         */
        RuleSpecifier withPrecedenceLevel(int level);

        /**
         * Sets a custom name for this rule
         *
         * @param name
         * @return this
         */
        RuleSpecifier withName(String name);

        /**
         * Sets the conflict arbitration strategy
         *
         * @return this
         */
        RuleSpecifier preferShiftOverReduce();

        /**
         * Sets the conflict arbitration strategy
         *
         * @return this
         */
        RuleSpecifier preferReduceOverShift();

        /**
         * sets the associativity of the rule
         *
         * @param associativity
         * @return this
         */
        RuleSpecifier withAssociativity(Associativity associativity);

        /**
         * gets the rule itself
         *
         * @return
         */
        Rule get();
    }

    /**
     * add a rule to the set of the rules for this grammar
     *
     * @param rule
     */
    public void addRule(Rule rule) {
        if (rules.isEmpty()) {
            targetSymbol = rule.getTarget();
        }
        rules.add(rule);
    }

    /**
     * add an empty rule (sugar for addRule(target, Collections.emptyList());
     *
     * @param target
     * @return
     */
    public RuleSpecifier addEmptyRule(Symbol target) {
        return addRule(target);
    }

    /**
     * Add a rule.
     *
     * @param target
     * @param clause
     * @return
     */
    public RuleSpecifier addRule(Symbol target, Collection<Symbol> clause) {
        return addRule(target, clause.toArray(new Symbol[]{}));
    }

    /**
     * Add a rule.
     *
     * @param target
     * @param clause
     * @return
     */
    public RuleSpecifier addRule(Symbol target, Symbol... clause) {

        //replace empty clause with the 'Empty' pseudo-terminal
        if (clause.length == 0) {
            clause = new Symbol[]{Lexemes.empty()};
        }

        if (target.isTerminal()) {
            throw new IllegalArgumentException("The target symbol cannot be a terminal !");
        }

        final BaseRule rule = new BaseRule(rules.size(), null, target, clause);
        addRule(rule);
        return new RuleSpecifier() {

            @Override
            public RuleSpecifier withAction(Consumer<ParsingContext> consumer) {
                rule.setAction(consumer);
                return this;
            }

            @Override
            public RuleSpecifier withName(String name) {
                rule.setName(name);
                return this;
            }

            @Override
            public RuleSpecifier withPrecedenceLevel(int level) {
                return this;
            }

            @Override
            public Rule get() {
                return rule;
            }

            @Override
            public RuleSpecifier preferReduceOverShift() {
                rule.setConflictArbitration(ActionType.Reduce);
                return this;
            }

            @Override
            public RuleSpecifier preferShiftOverReduce() {
                rule.setConflictArbitration(ActionType.Shift);
                return this;
            }

            @Override
            public RuleSpecifier withAssociativity(Associativity associativity) {
                assert associativity != null;
                switch (associativity) {
                    case NonAssoc:
                        rule.setConflictArbitration(ActionType.Fail);
                        break;
                    case Right:
                        rule.setConflictArbitration(ActionType.Shift);
                        break;
                    case Left:
                        rule.setConflictArbitration(ActionType.Reduce);
                        break;
                }
                return this;
            }
        };
    }

    /**
     * Get the terminals for this grammar, according to the recorded rules.
     *
     * @return
     */
    public Set<? extends Symbol> getTerminals() {
        Set<Symbol> terminals = new HashSet<>();
        for (Rule r : rules) {
            for (Symbol s : r.getClause()) {
                if (s != Lexemes.empty() && s.isTerminal()) {
                    terminals.add(s);
                }
            }
        }
        return terminals;
    }

    /**
     * Get all the different symbols known to the grammar
     *
     * @return
     */
    public Set<? extends Symbol> getSymbols() {
        Set<Symbol> symbols = new HashSet<>();
        for (Rule r : rules) {
            symbols.add(r.getTarget());
            for (Symbol s : r.getClause()) {
                if (s.isTerminal()) {
                    symbols.add(s);
                }
            }
        }
        return symbols;
    }

    /**
     * Get the list of non terminals known to the grammar
     *
     * @return
     */
    public Set<Symbol> getNonTerminals() {
        return rules.stream().map(r -> r.getTarget()).collect(Collectors.toSet());
    }

    /**
     * @return the name for this grammar
     */
    public String getName() {
        return name;
    }

    /**
     * Set the target rule, i.e. the rule that has to be accepted by the parser.
     *
     * @param rule
     */
    public void setTargetRule(Rule rule) {

        Symbol t = rule.getTarget();
        int rulesCount = getRulesTargeting(t).size();
        if (rulesCount != 1) {
            throw new IllegalStateException("The rule %s cannot be used as target rule");
        } else {
            targetSymbol = t;
        }

        if (rule.equals(getRuleById(0))) {
            return;
        }

        if (!rules.contains(rule)) {
            throw new IllegalArgumentException("Unknown rule :" + rule);
        }

        List<Rule> lRules = new ArrayList<>(rules);
        lRules.remove(rule);
        lRules.add(0, rule);
        int i = 0;
        for (Rule r : lRules) {
            r.setId(i++);
        }
        rules = new HashSet<>(lRules);
    }

    /**
     * sets the name of the grammar
     *
     * @param name
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Gets the target symbol for this grammar, I.e the target symbol for the target rule.
     * This symbol should appear only once on the left side of a rule
     *
     * @return
     */
    public Symbol getTargetSymbol() {
        if (targetSymbol == null) {
            targetSymbol = getRuleById(0).getTarget();
        }
        return targetSymbol;
    }

    /**
     * Dumps the rules.
     *
     * @return
     */
    @Override
    public String toString() {

        List<Rule> lRules = new ArrayList(rules);
        Collections.sort(lRules, Comparator.comparing(Rule::getId));

        StringWriter sw = new StringWriter();
        if (name != null) {
            sw.append(name);
            sw.append(" : ");
        }
        sw.append("{\n");
        for (Rule r : lRules) {
            sw.append("    " + r.toString());
            sw.append("\n");
        }
        sw.append("}\n");
        return sw.toString();
    }

    /**
     * each rule is assigned an id, the target rule of the grammar has always the id 0.
     *
     * @param id
     * @return
     */
    public Rule getRuleById(int id) {
        Optional<Rule> opt = rules.stream().filter(r -> r.getId() == id).findAny();
        return opt.isPresent() ? opt.get() : null;
    }

    /**
     * Generates a parser that can parse target symbol (the one returned by {@link Grammar#getTargetSymbol()}  for this grammar.
     *
     * @return a parser
     */
    public Parser createParser() {
        return createParser(getTargetSymbol());
    }

    private void fixPrecedenceLevels() {
        Map<Rule, Integer> mRules = new HashMap<>();
        for (Rule rule : rules) {
            List<Symbol> clause = new ArrayList<>(Arrays.asList(rule.getClause()));
            Collections.reverse(clause);
            for (Symbol s : clause) {
                if (s.isTerminal() && precedenceLevels.containsKey(s)) {
                    int level = precedenceLevels.get(s);
                    mRules.put(rule, level);
                    rules.stream().filter(r -> Arrays.asList(r.getClause()).contains(rule.getTarget())).forEach(r -> {
                        mRules.put(r, level);
                    });
                    break;
                }
            }
        }
        for (Map.Entry<Rule, Integer> entry : mRules.entrySet()) {
            getLog().debug(String.format("Precedence Level %d : %s", entry.getValue(), entry.getKey()));
            ((BaseRule) entry.getKey()).setPrecedenceLevel(entry.getValue());
        }
    }

    /**
     * Create a parser for a subset of this grammar's rules, that can parse a particular symbol.
     * useful for tests.
     *
     * @param symbol
     * @return
     */
    public Parser createParser(Symbol symbol) {
        Grammar grammar = getSubGrammar(symbol);
        ActionTable actionTable = ActionTableCaching.get(grammar);

        return new LRParser(grammar, actionTable);
    }

    protected Grammar getSubGrammar(Symbol symbol) {
        fixPrecedenceLevels();
        Set<Rule> targetRules = getRulesTargeting(symbol);
        if (targetRules.isEmpty()) {
            throw new IllegalArgumentException(String.format("Symbol '%s' is not a target for this grammar", symbol));
        } else if (targetRules.size() == 1) {
            Rule targetRule = targetRules.iterator().next();
            setTargetRule(targetRule);
            return this;
        } else {
            //ensure that we have a target rule that appear only once
            Symbol start = new Forward("(all)");
            Grammar cleanGrammar = new Grammar();
            cleanGrammar.precedenceLevels = precedenceLevels;
            if (name != null) {
                cleanGrammar.name = String.format("Subgrammar of '%s' targeting '%s'", name, symbol.toString());
            }
            //find all the rules that depend on the target rules
            HashSet<Symbol> seen = new HashSet<>();
            seen.add(symbol);

            Stack<Rule> stack = new Stack<>();
            stack.addAll(targetRules);

            //add all the rules with a definition that depend on the target symbol, recursively
            while (!stack.isEmpty()) {
                Rule rule = stack.pop();
                cleanGrammar.addRule(rule);
                seen.add(rule.getTarget());
                for (Symbol s : rule.getClause()) {
                    if (!s.isTerminal() && !seen.contains(s)) {
                        rules.stream().filter(r -> r.getTarget().equals(s)).forEach(r -> stack.push(r));
                    }
                }
            }

            Rule startRule = cleanGrammar.addRule(start, symbol).get();
            cleanGrammar.setTargetRule(startRule);
            return cleanGrammar;
        }
    }

    /**
     * get all the rules kwnown by this grammar that have the given symbol on the left side.
     *
     * @param symbol
     * @return all the rules where symbol appears as the target
     */
    public Set<Rule> getRulesTargeting(Symbol symbol) {
        return rules.stream().filter(r -> r.getTarget().equals(symbol)).collect(Collectors.toSet());
    }

    /**
     * <p>
     * Shift/Reduce conflict resolution
     * </p>
     * <p>
     * <p>
     * The resolution of conflicts works by comparing the precedence of the rule being considered with that of the look-ahead token.
     * If the token's precedence is higher, the choice is to shift.
     * If the rule's precedence is higher, the choice is to reduce.
     * If they have equal precedence, the choice is made based on the associativity of that precedence level
     * </p>
     *
     * @param rule
     * @param symbol
     * @return
     */
    public ActionType getConflictResolutionHint(Rule rule, Symbol symbol) {
        ActionType decision = null;

        int rulePrecedence = ((BaseRule) rule).getPrecedenceLevel();
        int tokenPrecedence = getPrecedenceLevel(symbol);

        if (tokenPrecedence > rulePrecedence) {
            getLog().debug(String.format("{rule=%s, symbol=%s} : Shift ((tokenPrecedence=%d) > (rulePrecedence=%d))", rule, symbol, tokenPrecedence, rulePrecedence));
            decision = ActionType.Shift;
        } else if (rulePrecedence > tokenPrecedence) {
            getLog().debug(String.format("{rule=%s, symbol=%s} : Reduce ((tokenPrecedence=%d) < (rulePrecedence=%d))", rule, symbol, tokenPrecedence, rulePrecedence));
            decision = ActionType.Reduce;
        } else {
            decision = ((BaseRule) rule).getConflictArbitration();
        }
        return decision;
    }

    private int getPrecedenceLevel(Symbol symbol) {
        Integer val = precedenceLevels.get(symbol);
        return val == null ? 0 : val;
    }

    public void setPrecedenceLevel(int level, Symbol... symbols) {
        //set precedence for the symbols
        for (Symbol symbol : symbols) {
            precedenceLevels.put(symbol, level);
        }
    }

    /**
     * Quickly define a set of rules that match one of the give symbols.
     * <pre>
     * oneOf(a, b) → a.
     * oneOf(a, b) → b.
     * </pre>
     *
     * @param symbols
     * @return
     */
    public Forward oneOf(Symbol... symbols) {
        assert symbols.length > 1;
        Forward tmp = new Forward("oneOf(" + String.join(", ", Arrays.asList(symbols).stream().map(Symbol::toString).collect(Collectors.toList())) + ")");
        for (Symbol s : symbols) {
            addRule(tmp, s);
        }
        return tmp;
    }

    /**
     * Quickly define a rule for a symbol that may or not appear.
     * <pre>
     *     optional(a) → a.
     *     optional(a) → ε.
     * </pre>
     *
     * @param symbols
     * @return
     */
    public Forward optional(Symbol... symbols) {
        Forward opt = new Forward("optional(" + String.join(", ", Arrays.asList(symbols).stream().map(Symbol::toString).collect(Collectors.toList())) + ")");
        addRule(opt, symbols);
        addEmptyRule(opt);
        return opt;
    }

    /**
     * Quickly define a rule for a sequence that may appear at least one time
     * <pre>
     *     oneOrMore(a) → a.
     *     oneOrMore(a) → oneOrMore(a) a.
     * </pre>
     *
     * @param symbols
     * @return
     */
    public Forward oneOrMore(Symbol... symbols) {
        Forward tmp = new Forward();
        tmp.setName("oneOrMore" + tmp.hashCode());
        addRule(tmp, symbols);
        List<Symbol> list = new ArrayList<>();
        list.add(tmp);
        list.addAll(Arrays.asList(symbols));
        addRule(tmp, list);
        return tmp;
    }

    /**
     * Quickly define a rule for a sequence that may appear several times
     * <pre>
     *     zeroOrMore(a) → oneOrMore(a).
     *     zeroOrMore(a) → ε.
     * </pre>
     *
     * @param symbols
     * @return
     */
    public Forward zeroOrMore(Symbol... symbols) {
        Forward tmp = new Forward();
        tmp.setName("zeroOrMore" + tmp.hashCode());
        addRule(tmp, symbols);
        List<Symbol> list = new ArrayList<>();
        list.add(tmp);
        list.addAll(Arrays.asList(symbols));
        addRule(tmp, list);
        addRule(tmp, Lexemes.empty());
        return tmp;
    }

    /**
     * Useful method for defining a rule that is a list of items separated by symbols (typically a terminal)
     *
     *  It differs from {@link Grammar#zeroOrMore(Symbol...)} and {@link Grammar#oneOrMore(Symbol...)} by the fact
     *  this it generates a rule that strips the symbol used as a separator from the generated node's children, and that it
     *  will also "flatten" the list of children, making it more easy to handle.
     *  <p>
     *  The following rule :
     *  <pre>
     *      listOfInts → list(Lexemes.singleChar(','), Lexemes.cInteger())
     *  </pre>
     *  Applied to :
     *  <pre>
     *      "521, 17, 514, -5"
     *  </pre>
     *
     *  </p>
     *
     *  <p>
     *      Would generate the following node (json-like representation) :
     *      <pre>
     *      {
     *          "symbol" : "listOfInts",
     *          "children" : [
     *              "symbol": "listOf(',', cInteger)",
     *              "children": [
     *                  {
     *                      "symbol":"cInteger", children:[]
     *                  },
     *                  {
     *                      "symbol":"cInteger", children:[]
     *                  },
     *                  {
     *                      "symbol":"cInteger", children:[]
     *                  },
     *                  {
     *                      "symbol":"cInteger", children:[]
     *                  },
     *              ]
     *          ]
     *      }
     *      </pre>
     *  </p>
     *
     *
     * @param allowEmptyList is it ok to have an empty list ?
     * @param separator      the separator, (a good one may be "Lexemes.singleChar(',')")
     * @param typeOfItems    the symbol for each element
     * @return
     */
    public Forward list(boolean allowEmptyList, Symbol separator, Symbol typeOfItems) {
        Forward tmp = new Forward("listOf(" + typeOfItems + ")");

        //a list may contain only one item
        addRule(tmp, typeOfItems);

        addRule(tmp, tmp, separator, typeOfItems).withAction(context -> {
           AstNode targetNode = context.getAstNode();
           List<AstNode> list = targetNode.getFirstChild().getChildren();
           AstNode extraElement = targetNode.getLastChild();
           targetNode.getChildren().clear();
           targetNode.getChildren().addAll(list);
           targetNode.getChildren().add(extraElement);
        });

        //may optionally may empty
        if (allowEmptyList) {
            addEmptyRule(tmp);
        }

        return tmp;
    }
}

