package se.codemate.spring.security;

import org.apache.log4j.Logger;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.TermQuery;
import org.neo4j.graphdb.*;
import org.springframework.dao.DataAccessException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.GrantedAuthorityImpl;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import se.codemate.neo4j.NeoSearch;
import se.codemate.neo4j.SimpleRelationshipType;

import javax.annotation.Resource;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class NeoUserDetailsService implements UserDetailsService {

    public static String HAS_ROLE_RELATIONSHIP = "HAS_ROLE";
    public static String EXTENDS_ROLE_RELATIONSHIP = "EXTENDS_ROLE";

    private static RelationshipType hasRoleType = new SimpleRelationshipType(HAS_ROLE_RELATIONSHIP);
    private static RelationshipType extendsRoleType = new SimpleRelationshipType(EXTENDS_ROLE_RELATIONSHIP);

    private static Logger log = Logger.getLogger(NeoUserDetailsService.class);

    @Resource
    private NeoSearch neoSearch;

    @Resource
    private GraphDatabaseService neoService;

    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException, DataAccessException {

        Transaction transaction = neoService.beginTx();

        try {

            List<Node> nodes = neoSearch.getNodes(new TermQuery(new Term("username", username)));

            if (nodes.size() == 1) {

                Node userNode = nodes.get(0);

                if (log.isDebugEnabled()) {
                    log.debug("Found user '" + username + "'");
                }

                Traverser traverser = userNode.traverse(
                        Traverser.Order.BREADTH_FIRST,
                        StopEvaluator.END_OF_GRAPH,
                        ReturnableEvaluator.ALL_BUT_START_NODE,
                        hasRoleType, Direction.OUTGOING,
                        extendsRoleType, Direction.OUTGOING
                );

                Collection<GrantedAuthority> authorities = new ArrayList<GrantedAuthority>();
                for (Node relationshipNode : traverser.getAllNodes()) {
                    if (relationshipNode.hasProperty("authority")) {
                        if (log.isDebugEnabled()) {
                            log.debug("Adding authority '" + relationshipNode.getProperty("authority") + "' to user '" + username + "'");
                        }
                        authorities.add(new GrantedAuthorityImpl(relationshipNode.getProperty("authority").toString()));
                    }
                }

                return new User(
                        username,
                        (String) userNode.getProperty("password"),
                        (Boolean) userNode.getProperty("enabled", true),
                        (Boolean) userNode.getProperty("accountNonExpired", true),
                        (Boolean) userNode.getProperty("credentialsNonExpired", true),
                        (Boolean) userNode.getProperty("accountNonLocked", true),
                        authorities
                );

            } else if (nodes.size() > 1) {
                throw new UsernameNotFoundException("Multiple nodes match '" + username + "'");
            } else {
                throw new UsernameNotFoundException(username);
            }

        } catch (IOException exception) {
            log.warn("IOException while trying to lookup user", exception);
            throw new UsernameNotFoundException(username);
        } catch (NotFoundException exception) {
            throw new UsernameNotFoundException(username);
        } finally {
            transaction.finish();
        }

    }

}
