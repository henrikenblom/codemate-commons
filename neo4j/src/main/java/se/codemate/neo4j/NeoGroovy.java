package se.codemate.neo4j;

import groovy.lang.Binding;
import groovy.lang.GroovyShell;
import org.codehaus.groovy.control.CompilationFailedException;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.PropertyContainer;

import java.io.File;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

public class NeoGroovy {

    private GraphDatabaseService neo;
    private NeoSearch neoSearch;

    public NeoGroovy(GraphDatabaseService neo) {
        this(neo, null);
    }

    public NeoGroovy(GraphDatabaseService neo, NeoSearch neoSearch) {
        this.neo = neo;
        this.neoSearch = neoSearch;
    }

    private GroovyShell initShell() {

        Binding binding = new Binding();

        if (neo != null) {
            binding.setVariable("neo", neo);
        }

        if (neoSearch != null) {
            binding.setVariable("neoSearch", neoSearch);
        }

        return new GroovyShell(binding);

    }

    @SuppressWarnings("unchecked")
    private List<PropertyContainer> processValue(Object value) {
        if (PropertyContainer.class.isInstance(value)) {
            List<PropertyContainer> containers = new LinkedList<PropertyContainer>();
            containers.add((PropertyContainer) value);
            return containers;
        } else {
            return (List<PropertyContainer>) value;
        }
    }

    public List<PropertyContainer> evaluate(String groovy) throws CompilationFailedException {
        GroovyShell groovyShell = initShell();
        return processValue(groovyShell.evaluate(groovy));
    }

    public List<PropertyContainer> evaluate(File groovy) throws CompilationFailedException, IOException {
        GroovyShell groovyShell = initShell();
        return processValue(groovyShell.evaluate(groovy));
    }

}
