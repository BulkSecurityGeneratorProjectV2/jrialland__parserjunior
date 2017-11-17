package net.jr.lexer.impl;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import static net.jr.lexer.impl.CharConstraint.Builder.eq;

public class NewLine extends LexemeImpl {

    public NewLine() {
        DefaultAutomaton.Builder builder = DefaultAutomaton.Builder.forTokenType(this);
        DefaultAutomaton.Builder.BuilderState init = builder.initialState();
        DefaultAutomaton.Builder.BuilderState gotCR = builder.newNonFinalState();
        DefaultAutomaton.Builder.BuilderState finalState = builder.newFinalState();
        init.when(eq('\r')).goTo(gotCR);
        gotCR.when(eq('\n')).goTo(finalState);
        init.when(eq('\n')).goTo(finalState);
        setAutomaton(builder.build());
    }

    @Override
    public String toString() {
        return "NewLine";
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (!obj.getClass().equals(NewLine.class)) {
            return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        return -48141;
    }

    @Override
    public void marshall(DataOutputStream dataOutputStream) throws IOException {

    }

    @SuppressWarnings("unused")
    public static NewLine unMarshall(DataInputStream in) throws IOException {
        return new NewLine();
    }
}
