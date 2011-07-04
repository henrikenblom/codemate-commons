package se.codemate.reporting.jasperreports;

import net.sf.jasperreports.engine.JRDataset;
import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.query.JRQueryExecuter;
import net.sf.jasperreports.engine.query.JRQueryExecuterFactory;
import org.neo4j.graphdb.GraphDatabaseService;
import se.codemate.neo4j.NeoSearch;

import java.util.Map;

public class JRNeoQueryExecuterFactory implements JRQueryExecuterFactory {

    public static final String QUERY_LANGUAGE_SQL = "neo";

    public final static String PARAMETER_NEO = "NEO";
    public final static String PARAMETER_NEO_SEARCH = "NEO_SEARCH";

    private final static Object[] BUILTIN_PARAMETERS = {
            PARAMETER_NEO, GraphDatabaseService.class,
            PARAMETER_NEO_SEARCH, NeoSearch.class
    };

    public Object[] getBuiltinParameters() {
        return BUILTIN_PARAMETERS;
    }

    public JRQueryExecuter createQueryExecuter(JRDataset dataset, Map parameters) throws JRException {
        return new JRNeoQueryExecuter(dataset, parameters);
    }

    public boolean supportsQueryParameterType(String className) {
        return true;
    }

}
