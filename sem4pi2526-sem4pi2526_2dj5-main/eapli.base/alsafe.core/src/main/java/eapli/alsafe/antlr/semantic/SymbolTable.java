package eapli.alsafe.antlr.semantic;

import java.util.ArrayDeque;
import java.util.Collection;
import java.util.Collections;
import java.util.Deque;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

/**
 * Multi-level symbol table. Lookup starts in the nearest scope and walks outwards.
 */
public class SymbolTable {

    private final Deque<Map<String, Symbol>> scopes = new ArrayDeque<>();

    public SymbolTable() {
        enterScope();
    }

    public void enterScope() {
        scopes.push(new LinkedHashMap<>());
    }

    public void exitScope() {
        if (scopes.size() == 1) {
            throw new IllegalStateException("Cannot remove the global scope.");
        }
        scopes.pop();
    }

    public boolean insertCurrentScope(Symbol symbol) {
        Map<String, Symbol> currentScope = scopes.peek();
        if (currentScope.containsKey(symbol.name())) {
            return false;
        }
        currentScope.put(symbol.name(), symbol);
        return true;
    }

    public Optional<Symbol> lookup(String name) {
        for (Map<String, Symbol> scope : scopes) {
            Symbol symbol = scope.get(name);
            if (symbol != null) {
                return Optional.of(symbol);
            }
        }
        return Optional.empty();
    }

    public Optional<Symbol> lookupCurrentScope(String name) {
        return Optional.ofNullable(scopes.peek().get(name));
    }

    public Collection<Symbol> currentScopeSymbols() {
        return Collections.unmodifiableCollection(scopes.peek().values());
    }
}
