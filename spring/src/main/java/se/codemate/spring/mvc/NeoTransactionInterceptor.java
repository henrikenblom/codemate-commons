package se.codemate.spring.mvc;

import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Transaction;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;
import se.codemate.neo4j.NeoSearch;
import se.codemate.neo4j.NeoUtils;
import se.codemate.spring.freemarker.NeoSortMethod;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

public class NeoTransactionInterceptor extends HandlerInterceptorAdapter {

    public static final String TRANSACTION_ATTRIBUTE_NAME = "_neo_transaction";

    @Resource
    private GraphDatabaseService neo;

    private boolean doTransactions = true;

    private boolean ignoreExceptions = false;

    @Resource
    private NeoSearch neoSearch;

    private NeoUtils neoUtils;

    public GraphDatabaseService getNeoService() {
        return neo;
    }

    public void setNoTransactions(boolean flag) {
        doTransactions = !flag;
    }

    public void setIgnoreExceptions(boolean ignoreExceptions) {
        this.ignoreExceptions = ignoreExceptions;
    }

    @PostConstruct
    private void init() {
        neoUtils = new NeoUtils(neo);
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        HttpSession session = request.getSession(true);
        if (doTransactions) {
            Transaction transaction = neo.beginTx();
            session.setAttribute(TRANSACTION_ATTRIBUTE_NAME, transaction);
        }
        request.setAttribute("neo", neo);
        request.setAttribute("neoSearch", neoSearch);
        request.setAttribute("neoUtils", neoUtils);
        request.setAttribute("neoSort", new NeoSortMethod());
        if (request.getUserPrincipal() != null) {
            request.setAttribute("userPrincipal", request.getUserPrincipal());
        }
        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
        if (doTransactions) {
            HttpSession session = request.getSession(false);
            if (session != null) {
                Transaction transaction = (Transaction) session.getAttribute(TRANSACTION_ATTRIBUTE_NAME);
                if (ignoreExceptions || ex == null) {
                    transaction.success();
                } else {
                    transaction.failure();
                }
                transaction.finish();
            }
        }
    }

}
