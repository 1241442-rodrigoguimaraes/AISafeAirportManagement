package eapli.alsafe.antlr;

import eapli.alsafe.antlr.domain.FlightPlanDSL;
import eapli.alsafe.antlr.semantic.FlightSemanticAnalyzerVisitor;
import eapli.alsafe.dsl.FlightLexer;
import eapli.alsafe.dsl.FlightParser;
import org.antlr.v4.runtime.*;
import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.tree.ParseTreeWalker;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/**
 * Orchestrates the full processing pipeline for a Flight DSL file:
 * <ol>
 *   <li>Lexical + syntactic analysis (ANTLR)</li>
 *   <li>Semantic validation — fills the symbol table and computes parse-tree attributes</li>
 *   <li>Listener walk — prints a human-readable summary</li>
 *   <li>Visitor — builds domain objects</li>
 *   <li>JSON export — writes a .json file only if the input is fully valid</li>
 * </ol>
 *
 * <p>Usage — automatic JSON output next to the input file:
 * <pre>
 *   FlightDSLProcessor.Result result = new FlightDSLProcessor().process(Path.of("myplan.fp"));
 *   // produces myplan.json if valid
 * </pre>
 *
 * <p>Usage — custom JSON output path:
 * <pre>
 *   FlightDSLProcessor.Result result = new FlightDSLProcessor()
 *       .process(Path.of("myplan.fp"), Path.of("output/myplan.json"));
 * </pre>
 */
public class FlightDSLProcessor {

    private final static String FP_OUTPUT_DIR = "validFPFiles";

    public static class Result {

        private final boolean syntaxOk;
        private final List<FlightPlanDSL> flightPlanDSLS;
        private final List<String> syntaxErrors;
        private final List<SemanticError> semanticErrors;
        private Path jsonOutputPath;

        Result(boolean syntaxOk, List<FlightPlanDSL> flightPlanDSLS, List<SemanticError> semanticErrors) {
            this(syntaxOk, flightPlanDSLS, List.of(), semanticErrors);
        }

        Result(boolean syntaxOk, List<FlightPlanDSL> flightPlanDSLS, List<String> syntaxErrors,
               List<SemanticError> semanticErrors) {
            this.syntaxOk       = syntaxOk;
            this.flightPlanDSLS = flightPlanDSLS;
            this.syntaxErrors   = syntaxErrors;
            this.semanticErrors = semanticErrors;
        }

        /** true only if syntax was correct AND there are no semantic errors. */
        public boolean isValid()                  { return syntaxOk && semanticErrors.isEmpty(); }
        public boolean isSyntaxOk()               { return syntaxOk; }
        public List<FlightPlanDSL> getFlightPlans()  { return flightPlanDSLS; }
        public List<String> getSyntaxErrors()     { return syntaxErrors; }
        public List<SemanticError> getErrors()    { return semanticErrors; }
        public List<String> allErrors() {
            List<String> all = new ArrayList<>(syntaxErrors);
            semanticErrors.forEach(e -> all.add(e.toString()));
            return all;
        }

        /** Path of the generated JSON file, or null if the input was invalid. */
        public Path getJsonOutputPath()           { return jsonOutputPath; }
        void setJsonOutputPath(Path p)            { this.jsonOutputPath = p; }
    }



    /**
     * Processes a .fp file. If valid, exports JSON to the same directory
     * with the same base name and a .json extension.
     * Example: flights/myplan.fp -> flights/myplan.json
     */
    public Result process(Path filePath) throws IOException {
        return process(filePath, deriveJsonPath(filePath));
    }

    /**
     * Processes a .fp file and, if fully valid, writes JSON to jsonOutputPath.
     * Invalid files (syntax or semantic errors) do NOT produce a JSON file.
     */
    public Result process(Path filePath, Path jsonOutputPath) throws IOException {

        CharStream input = CharStreams.fromPath(filePath);
        FlightLexer lexer = new FlightLexer(input);
        CommonTokenStream tokens = new CommonTokenStream(lexer);
        FlightParser parser = new FlightParser(tokens);

        SyntaxErrorCollector errorListener = new SyntaxErrorCollector();
        lexer.removeErrorListeners();
        lexer.addErrorListener(errorListener);
        parser.removeErrorListeners();
        parser.addErrorListener(errorListener);

        ParseTree tree = parser.start();

        if (errorListener.hasErrors()) {
            System.err.println("=== Syntax / Lexical Errors ===");
            errorListener.getErrors().forEach(System.err::println);
            return new Result(false, List.of(), errorListener.getErrors(), List.of());
        }

        FlightSemanticAnalyzerVisitor semanticAnalyzer = new FlightSemanticAnalyzerVisitor();
        semanticAnalyzer.visit(tree);
        List<SemanticError> semanticErrors = semanticAnalyzer.getErrors();

        if (!semanticErrors.isEmpty()) {
            System.err.println("=== Semantic Errors ===");
            semanticErrors.forEach(System.err::println);
            return new Result(true, List.of(), semanticErrors);
        }

        System.out.println("=== Flight Plan Summary ===");
        FlightPlanSummaryListener listener = new FlightPlanSummaryListener();
        ParseTreeWalker.DEFAULT.walk(listener, tree);

        FlightPlanBuilderVisitor visitor = new FlightPlanBuilderVisitor();
        visitor.visit(tree);
        List<FlightPlanDSL> plans = visitor.getFlightPlans();

        Result result = new Result(true, plans, semanticErrors);

        if (result.isValid()) {
            new FlightPlanJsonExporter().export(plans, jsonOutputPath);
            result.setJsonOutputPath(jsonOutputPath);
            System.out.println("=== JSON exported to: " + jsonOutputPath + " ===");
        }

        return result;
    }

    public static Result process(String content) throws IOException {
        CharStream input = CharStreams.fromString(content);
        FlightLexer lexer = new FlightLexer(input);
        CommonTokenStream tokens = new CommonTokenStream(lexer);
        FlightParser parser = new FlightParser(tokens);

        SyntaxErrorCollector errorListener = new SyntaxErrorCollector();
        lexer.removeErrorListeners();
        lexer.addErrorListener(errorListener);
        parser.removeErrorListeners();
        parser.addErrorListener(errorListener);

        ParseTree tree = parser.start();

        if (errorListener.hasErrors()) {
            System.err.println("=== Syntax / Lexical Errors ===");
            errorListener.getErrors().forEach(System.err::println);
            return new Result(false, List.of(), errorListener.getErrors(), List.of());
        }

        FlightSemanticAnalyzerVisitor semanticAnalyzer = new FlightSemanticAnalyzerVisitor();
        semanticAnalyzer.visit(tree);
        List<SemanticError> semanticErrors = semanticAnalyzer.getErrors();

        if (!semanticErrors.isEmpty()) {
            System.err.println("=== Semantic Errors ===");
            semanticErrors.forEach(System.err::println);
            return new Result(true, List.of(), semanticErrors);
        }

        System.out.println("=== Flight Plan Summary ===");
        FlightPlanSummaryListener listener = new FlightPlanSummaryListener();
        ParseTreeWalker.DEFAULT.walk(listener, tree);

        FlightPlanBuilderVisitor visitor = new FlightPlanBuilderVisitor();
        visitor.visit(tree);
        List<FlightPlanDSL> plans = visitor.getFlightPlans();

        Result result = new Result(true, plans, semanticErrors);

        return result;
    }



    private static Path deriveJsonPath(Path filePath) {
        String fileName = filePath.getFileName().toString();
        String base = fileName.contains(".")
                ? fileName.substring(0, fileName.lastIndexOf('.'))
                : fileName;
        return filePath.resolveSibling(base + ".json");
    }

    // =========================================================================
    // Inner helper: collects ANTLR syntax errors
    // =========================================================================

    private static class SyntaxErrorCollector extends BaseErrorListener {

        private final List<String> errors = new ArrayList<>();

        @Override
        public void syntaxError(Recognizer<?, ?> recognizer,
                                Object offendingSymbol,
                                int line,
                                int charPositionInLine,
                                String msg,
                                RecognitionException e) {
            errors.add(String.format("[SYNTAX ERROR] line %d:%d — %s", line, charPositionInLine, msg));
        }

        public boolean hasErrors()      { return !errors.isEmpty(); }
        public List<String> getErrors() { return errors; }
    }
}
