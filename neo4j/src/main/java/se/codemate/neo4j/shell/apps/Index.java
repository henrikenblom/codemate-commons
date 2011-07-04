package se.codemate.neo4j.shell.apps;

import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.NotFoundException;
import org.neo4j.graphdb.Relationship;
import org.neo4j.shell.kernel.apps.GraphDatabaseApp;
import org.neo4j.shell.*;
import se.codemate.neo4j.NeoSearch;

import java.io.IOException;
import java.rmi.RemoteException;

public class Index extends GraphDatabaseApp implements NeoSearchApp {

    private static NeoSearch neoSearch;

    public Index() {
        this.addOptionDefinition("r", new OptionDefinition(OptionValueType.MAY, "Index the entire connected graph of a node"));
    }

    public void setLuceneIndexer(NeoSearch indexer) {
        neoSearch = indexer;
    }

    @Override
    public String getDescription() {
        return "Indexes a node and its relationship. In no id is provided the current node is indexed. Usage: index [id]";
    }

    protected String exec(AppCommandParser parser, Session session, Output output) throws ShellException, RemoteException {

        Node node = (Node) getCurrent(getServer(),session);
        if (!parser.arguments().isEmpty()) {
            String arg = parser.arguments().get(0);
            try {
                long id = Long.parseLong(arg);
                node = this.getNodeById(id);
            } catch (NotFoundException exception) {
                output.println("Node "+arg+" does not exist!");
            } catch (NumberFormatException e) {
                output.println("You must enter a valid number!");
                return null;
            }
        }

        if (neoSearch != null) {
            try {
                if (parser.options().containsKey("r")) {
                    neoSearch.indexGraph(node);
                    output.println("Indexed node (" + node.getId() + ") recursively");
                } else {
                    neoSearch.indexNode(node);
                    output.println("Indexed node (" + node.getId() + ")");
                    for (Relationship relationship : node.getRelationships()) {
                        neoSearch.indexRelationship(relationship);
                        output.println("Indexed relationship [" + relationship.getId() + "]");
                    }
                }
            } catch (IOException exception) {
                output.println("Error occurred while trying to index node (" + node.getId() + "): " + exception.getMessage());
            }
        } else {
            output.println("Indexer is missing");
        }

        return null;

    }

}
