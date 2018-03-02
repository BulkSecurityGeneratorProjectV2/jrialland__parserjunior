package net.jr.lexer.basiclexemes;


import net.jr.lexer.automaton.Automaton;
import net.jr.lexer.automaton.DefaultAutomaton;
import net.jr.lexer.impl.CharConstraint;
import net.jr.lexer.impl.LexemeImpl;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public class SingleChar extends LexemeImpl {

    private char character;

    public SingleChar(char character) {
        this.character = character;
    }

    @Override
    public Automaton getAutomaton() {
        DefaultAutomaton.Builder builder = DefaultAutomaton.Builder.forTokenType(this);
        builder.initialState().when(CharConstraint.Builder.eq(character)).goTo(builder.newFinalState());
        return builder.build();
    }

    @Override
    public String toString() {
        String name = getName();
        return name==null?"'" + Character.toString(character) + "'":name;
    }

    @Override
    public int hashCode() {
        return character + 1844;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null || !obj.getClass().equals(SingleChar.class)) {
            return false;
        }
        return ((SingleChar) obj).character == character;
    }

    @Override
    public int getPriority() {
        return 0;
    }

    @Override
    public void marshall(DataOutputStream dataOutputStream) throws IOException {
        dataOutputStream.writeChar(character);
    }

    @SuppressWarnings("unchecked")
    public static SingleChar unMarshall(DataInputStream dataInputStream) throws IOException {
        return new SingleChar(dataInputStream.readChar());
    }

}
