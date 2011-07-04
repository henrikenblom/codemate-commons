package se.codemate.neo4j.shell.apps;

import org.apache.lucene.queryParser.ParseException;
import org.neo4j.shell.*;
import org.neo4j.shell.kernel.apps.GraphDatabaseApp;
import se.codemate.neo4j.NeoSearch;

import java.io.IOException;
import java.rmi.RemoteException;

public class Search extends GraphDatabaseApp implements NeoSearchApp {

    private static NeoSearch neoSearch;

    public Search() {
        this.addOptionDefinition("r", new OptionDefinition(OptionValueType.MAY, "Search for a relationship"));
        this.addOptionDefinition("p", new OptionDefinition(OptionValueType.MAY, "Search for a property container"));
    }

    public void setLuceneIndexer(NeoSearch indexer) {
        neoSearch = indexer;
    }

    @Override
    public String getDescription() {
        return "Searches for nodes, relationships, or property containers in the index. Usage: search [-rp] \"[query]\"";
    }

    protected String exec(AppCommandParser parser, Session session, Output output) throws ShellException, RemoteException {

        String query = parser.getLineWithoutApp();

        if (neoSearch != null) {
            try {
                if (parser.options().containsKey("p")) {
                    query = query.substring(query.indexOf(' ')).trim();
                    output.println(neoSearch.getPropertyContainers(query).toString());
                } else if (parser.options().containsKey("r")) {
                    query = query.substring(query.indexOf(' ')).trim();
                    output.println(neoSearch.getRelationships(query).toString());
                } else {
                    output.println(neoSearch.getNodes(query).toString());
                }
            } catch (ParseException exception) {
                output.println("Error occurred while trying to search the index" + exception.getMessage());
            } catch (IOException exception) {
                output.println("Error occurred while trying to search the index" + exception.getMessage());
            }
        } else {
            output.println("Indexer is missing");
        }

        return null;

    }

}
