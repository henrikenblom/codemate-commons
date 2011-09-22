package se.codemate.spring.controllers;

import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.shell.App;
import org.neo4j.shell.Output;
import org.neo4j.shell.Session;
import org.neo4j.shell.ShellException;
import org.neo4j.shell.impl.RemoteOutput;
import org.neo4j.shell.impl.SameJvmSession;
import org.neo4j.shell.kernel.GraphDatabaseShellServer;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import se.codemate.neo4j.NeoSearch;
import se.codemate.neo4j.shell.apps.NeoSearchApp;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.Serializable;
import java.rmi.RemoteException;

@Controller
public class NeoShellController {

    private class PrintWriterOutput implements Output {

        private PrintWriter writer;

        public PrintWriterOutput(PrintWriter writer) {
            this.writer = writer;
        }

        public void print(Serializable object) {
            writer.print(object);
        }

        public void println() {
            writer.println();
        }

        public void println(Serializable object) {
            writer.println(object);
        }

        public Appendable append(char ch) {
            this.print(ch);
            return this;
        }

        public Appendable append(CharSequence sequence) {
            this.println(RemoteOutput.asString(sequence));
            return this;
        }

        public Appendable append(CharSequence sequence, int start, int end) {
            this.print(RemoteOutput.asString(sequence).substring(start, end));
            return this;
        }

    }

    @Resource
    private NeoSearch neoSearch;

    @Resource
    private GraphDatabaseService neo;

    private GraphDatabaseShellServer neoShellServer;

    private String welcomeMessage;

    @PostConstruct
    public void initialize() throws RemoteException {

        neoShellServer = new GraphDatabaseShellServer(neo);
        neoShellServer.addApp(se.codemate.neo4j.shell.apps.Index.class);
        neoShellServer.addApp(se.codemate.neo4j.shell.apps.Search.class);

        StringBuilder stringBuilder = new StringBuilder("<strong>Welcome to NeoShell</strong>\n");
        stringBuilder.append("Available commands: <em>");

        for (String command : neoShellServer.getAllAvailableCommands()) {
            App app = neoShellServer.findApp(command);
            if (NeoSearchApp.class.isAssignableFrom(app.getClass())) {
                NeoSearchApp indexerApp = (NeoSearchApp) app;
                indexerApp.setLuceneIndexer(neoSearch);
            }
            stringBuilder.append(command);
            stringBuilder.append(" ");
        }
        stringBuilder.append("</em>\n");
        stringBuilder.append("Use <em>man &lt;command&gt;</em> for info about each command.");

        welcomeMessage = stringBuilder.toString();

    }

    @PreDestroy
    public void shutdown() throws RemoteException {
        neoShellServer.shutdown();
    }

    @RequestMapping(value = {"/neo/shell-current-node.do"})
    public void getCurrentNode(HttpServletResponse response, HttpSession httpSession) throws IOException {
        response.setContentType("text/plain; charset=UTF-8");
        PrintWriter writer = response.getWriter();
        Session session = (Session) httpSession.getAttribute("neoSession");
        if (session == null || session.get("CURRENT_NODE") == null) {
            writer.print("?");
        } else {
            writer.print(session.get("CURRENT_NODE"));
        }
    }

    @RequestMapping(value = {"/neo/shell-interpret.do"})
    public void interpretLine(HttpServletResponse response, HttpSession httpSession, @RequestParam("line") String line) throws IOException, ShellException {
        response.setContentType("text/plain; charset=UTF-8");
        PrintWriter writer = response.getWriter();
        Output output = new PrintWriterOutput(writer);
        Session session = (Session) httpSession.getAttribute("neoSession");
        if (session == null) {
            session = new SameJvmSession();
            httpSession.setAttribute("neoSession", session);
        }
        neoShellServer.interpretLine(line, session, output);
    }

    @RequestMapping(value = {"/neo/shell.do"})
    public ModelMap displayShell() {
        ModelMap model = new ModelMap();
        model.addAttribute("welcome", welcomeMessage);
        return model;
    }

}
