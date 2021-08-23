package cs.extras;


import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;
import org.neo4j.driver.*;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Relationship;

import java.net.URI;
import java.net.URISyntaxException;
import javax.ws.rs.core.MediaType;

import static org.neo4j.driver.Values.parameters;

public class Neo4jGraph {
    private static final String SERVER_ROOT_URI = "bolt://localhost:7687";
    private Driver driver;
    Node firstNode;
    Node secondNode;
    Relationship relationship;
    
    public Neo4jGraph() {
        this.driver = GraphDatabase.driver(SERVER_ROOT_URI, AuthTokens.basic("neo4j", "12345"));
    }
    
    public void addNode(String value) {
        try (Session session = driver.session()) {
            session.run("CREATE (:Node {value: $value})", parameters("value", value));
        }
    }
    
    public void connectNodes(String source, String target) {
        try (Session session = driver.session()) {
            session.run("MATCH\n" +
                    "  (a:Node),\n" +
                    "  (b:Node)\n" +
                    "WHERE a.value = $A AND b.value = $B\n" +
                    "CREATE (a)-[r:RELTYPE {name: a.value + '<->' + b.value}]->(b)", parameters("A", source, "B", target));
        }
    }
    
    public void addPerson(final String name) {
        try (Session session = driver.session()) {
            session.writeTransaction(new TransactionWork<Integer>() {
                @Override
                public Integer execute(Transaction tx) {
                    
                    return createPersonNode(tx, name);
                }
            });
        }
    }
    
    private static int createPersonNode(Transaction tx, String name) {
        tx.run("CREATE (a:Person {name: $name})", parameters("name", name));
        return 1;
    }
    
    public static void main(String[] args) throws URISyntaxException {
        Neo4jGraph neo = new Neo4jGraph();
        neo.addNode(String.valueOf(1));
        neo.addNode(String.valueOf(2));
        neo.connectNodes(String.valueOf(1), String.valueOf(2));
    }
    
}