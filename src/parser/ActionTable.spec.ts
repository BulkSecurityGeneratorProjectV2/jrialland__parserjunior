import {Grammar} from './Grammar';
import { NonTerminal } from '../common/NonTerminal';
import {SingleChar} from '../lexer/SingleChar';
import {getFirstItemSet, getAllItemSets, getTranslationTable, initializeShiftsAndGotos, initializeReductions, initializeAccept, makeExtendedGrammar, getFollowSets, ActionTable} from './ActionTable';
import { ParseSymbol } from '../common/ParseSymbol';
import { Eof } from '../common/SpecialTerminal';

let symbols:Map<string, ParseSymbol> = new Map;
symbols.set('S', new NonTerminal('S'));
symbols.set('N', new NonTerminal('N'));
symbols.set('V', new NonTerminal('V'));
symbols.set('E', new NonTerminal('E'));
symbols.set('x', new SingleChar('x'));
symbols.set('=', new SingleChar('='));
symbols.set('*', new SingleChar('*'));
symbols.set('eof', Eof);

function makeGrammar() {

    let S = symbols.get('S');
    let N = symbols.get('N');
    let V = symbols.get('V');
    let E = symbols.get('E');

    let x = symbols.get('x');
    let eq = symbols.get('=');
    let star = symbols.get('*');
    
    let g = new Grammar();
    
    // S → N
    g.defineRule(S, [N]);
    
    // N → V = E
    g.defineRule(N, [V, eq, E]);
    
    // N → E
    g.defineRule(N, [E]);
    
    // E → V
    g.defineRule(E, [V]);
    
    // V → x
    g.defineRule(V, [x]);
    
    // V → * E
    g.defineRule(V, [star, E]);

    return g;
}

let testGrammar = makeGrammar();
/*
test('First ItemSet', () => {
    let i0 = getFirstItemSet(testGrammar, testGrammar.getTargetRule());
    expect(i0.kernel.size).toBe(1);
    expect(i0.toString()).toBe(
       "S → • N\n"+
       " + N → • V '=' E\n"+
       " + N → • E\n"+
       " + E → • V\n"+
       " + V → • 'x'\n"+
       " + V → • '*' E"
    );
});

test('All ItemSets', () => {
    let itemSets = getAllItemSets(testGrammar, testGrammar.getTargetRule());
    expect(itemSets.size).toBe(10);
    let counter=0;
    itemSets.forEach((itemSet) => {
        switch(itemSet.id) {
            case 0:
                    expect(itemSet.toString()).toBe(
                    "S → • N\n"+
                    " + N → • V '=' E\n"+
                    " + N → • E\n"+
                    " + E → • V\n"+
                    " + V → • 'x'\n"+
                    " + V → • '*' E"
                );
                counter+=1;
                break;
            case 1:
                expect(itemSet.toString()).toBe("S → N •");
                counter+=1;
                break;
            case 2:
                expect(itemSet.toString()).toBe(
                    "N → V • '=' E\n"+
                    "E → V •");
                counter+=1;
                break;
            case 3:
                expect(itemSet.toString()).toBe("N → E •");
                counter+=1;
                break;
            case 4:
                expect(itemSet.toString()).toBe("V → 'x' •");
                counter+=1;
                break;
            case 5:
                expect(itemSet.toString()).toBe(
                    "V → '*' • E\n"+
                    " + E → • V\n"+
                    " + V → • 'x'\n"+
                    " + V → • '*' E"
                );
                counter+=1;
                break;
            case 6:
                expect(itemSet.toString()).toBe("V → '*' E •");
                counter+=1;
                break;
            case 7:
                expect(itemSet.toString()).toBe("E → V •");
                counter+=1;
                break;
            case 8:
                expect(itemSet.toString()).toBe(
                    "N → V '=' • E\n"+
                    " + E → • V\n"+
                    " + V → • 'x'\n"+
                    " + V → • '*' E"
                );
                counter+=1;
                break;
            case 9:
                expect(itemSet.toString()).toBe("N → V '=' E •");
                counter+=1;
                break;
            default:
                fail('Unexpected ItemSet id ' + itemSet.id);
                break;                               
        }
    });
    expect(counter).toBe(10);
});

test('Translation Table', () => {
    let itemSets = getAllItemSets(testGrammar, testGrammar.getTargetRule());
    let table = getTranslationTable(testGrammar, itemSets);
    expect(table.size).toBe(10);
    let str = '';
    for(let i=0; i<10; i++) {
        str += i + ':';
        let line = table.get(i);
        for(let t of 'x=*SNEV') {
            if(line.has(t)) {
                str+=line.get(t);
            } else {
                str += ' ';
            }
        }
        if(i<9) {
            str += '\n';
        }
    }
    expect(str).toBe(
        "0:4 5 132\n"+
        "1:       \n"+
        "2: 8     \n"+
        "3:       \n"+
        "4:       \n"+
        "5:4 5  67\n"+
        "6:       \n"+
        "7:       \n"+
        "8:4 5  97\n"+
        "9:       "
    );
});

function checkAction(a:ActionTable, state:number, s:string, expectedAction:string, expectedTarget:number) {
    let action = a.getAction(state, symbols.get(s));
    expect(action.typeStr.toUpperCase()).toBe(expectedAction);
    expect(action.target).toBe(expectedTarget);
};

test('Initialize Shifts and Gotos', () => {
    let a = new ActionTable(testGrammar, false);
    let itemSets = getAllItemSets(testGrammar, testGrammar.getTargetRule());
    let translationTable = getTranslationTable(testGrammar, itemSets);
    initializeShiftsAndGotos(a, translationTable);
    checkAction(a, 0, 'N', 'GOTO', 1);
    checkAction(a, 0, 'V', 'GOTO', 2);
    checkAction(a, 0, 'E', 'GOTO', 3);
    checkAction(a, 0, 'x', 'SHIFT', 4);
    checkAction(a, 0, '*', 'SHIFT', 5);
    checkAction(a, 2, '=', 'SHIFT', 8);
    checkAction(a, 5, 'V', 'GOTO', 7);
    checkAction(a, 5, 'E', 'GOTO', 6);
    checkAction(a, 5, 'x', 'SHIFT', 4);
    checkAction(a, 5, '*', 'SHIFT', 5);
    checkAction(a, 8, 'V', 'GOTO', 7);
    checkAction(a, 8, 'E', 'GOTO', 9);
    checkAction(a, 8, 'x', 'SHIFT', 4);
    checkAction(a, 8, '*', 'SHIFT', 5);
});

test('Initialize Accept', ()=> {
    let a = new ActionTable(testGrammar, false);
    let itemSets = getAllItemSets(testGrammar, testGrammar.getTargetRule());
    initializeAccept(testGrammar, a, itemSets);
    checkAction(a, 1, 'eof', 'ACCEPT', 0);
});
*/
/*
test('Initialize Reductions', () => {
    let a = new ActionTable(testGrammar, false);
    let itemSets = getAllItemSets(testGrammar, testGrammar.getTargetRule());
    initializeReductions(testGrammar, a, itemSets);
});
*/
/*
test('Make extended grammar', ()=> {
    let itemSets = getAllItemSets(testGrammar, testGrammar.getTargetRule());
    let eGrammar:Grammar = makeExtendedGrammar(testGrammar.getTargetRule(), itemSets);
    expect(eGrammar.getRules().length).toBe(12);
});
*/
test('Build the FOLLOW sets', ()=> {
    let itemSets = getAllItemSets(testGrammar, testGrammar.getTargetRule());
    let eGrammar:Grammar = makeExtendedGrammar(testGrammar.getTargetRule(), itemSets);
    let followSets = getFollowSets(eGrammar);
    console.log(followSets);
});

/*
test('Complete ActionTable', () => {

    let a:ActionTable = new ActionTable(testGrammar);



});
*/
