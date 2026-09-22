package eapli.alsafe.antlr.semantic;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * One entry in the symbol table, with the attributes discovered for that symbol.
 */
public class Symbol {

    private final String name;
    private final SymbolKind kind;
    private final int line;
    private final int column;
    private final Map<String, Object> attributes = new LinkedHashMap<>();

    public Symbol(String name, SymbolKind kind, int line, int column) {
        this.name = name;
        this.kind = kind;
        this.line = line;
        this.column = column;
    }

    public String name() {
        return name;
    }

    public SymbolKind kind() {
        return kind;
    }

    public int line() {
        return line;
    }

    public int column() {
        return column;
    }

    public Symbol withAttribute(String key, Object value) {
        attributes.put(key, value);
        return this;
    }

    public Object attribute(String key) {
        return attributes.get(key);
    }

    public Map<String, Object> attributes() {
        return Collections.unmodifiableMap(attributes);
    }
}
