/**
 * Created by Stephanya CASANOVA on 03/04/2018.
 */


import org.apache.http.HttpHost;
import org.apache.http.client.HttpClient;
import org.apache.http.conn.routing.HttpRoute;
import org.apache.http.impl.client.LaxRedirectStrategy;
import org.apache.jena.query.*;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.riot.web.HttpOp;
import org.apache.jena.sparql.engine.http.QueryEngineHTTP;
import org.apache.http.HttpConnection;
import org.apache.http.conn.HttpClientConnectionManager;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.AbstractHttpEntity;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;


public class Search {

    static public final String NL = System.getProperty("line.separator");
    PrintStream out = null;

    private static List<RDFNode> getRoots(String model) {
        String prolog = "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>";
        String skos = "PREFIX skos: <http://www.w3.org/2004/02/skos/core#>";
        String getRootsQuery = skos + NL +
                "SELECT ?pL WHERE {" +
                "<"+model+">" + " skos:prefLabel ?prefLabel ;" +
                "skos:narrower ?pL ." +
                "?pL skos:prefLabel ?label" +
                "}order by asc (?label)";

        Query query = QueryFactory.create(getRootsQuery) ;
        QueryExecution qexec = QueryExecutionFactory.sparqlService("http://data.bnf.fr/sparql", query);
        ResultSet results = qexec.execSelect();
        //ResultSetFormatter.outputAsJSON(results);
        List<RDFNode> result = new ArrayList<RDFNode>();
        int i=0;
        for ( ; results.hasNext() ; ){
            QuerySolution soln = results.nextSolution() ;
            RDFNode x = soln.get("pL") ;       // Get a result variable by name.
            result.add(x);
            i++;        }
        return result;
    }

    public static void traverseStart(String model, String entity, PrintStream out) {
        // if starting class available



        if (entity != null) {
            traverse(entity, "k","cM",new ArrayList<String>(), 0, out);
        }
        // get roots and traverse each root
        else {
            List<RDFNode> roots = getRoots(model);

            for (int i = 0; i < roots.size(); i++) {
                traverse(roots.get(i).toString(),"k","cM", new ArrayList<String>(), 0, out);
            }
        }
    }

    public static void traverse(String entity, String k, String cM, List<String> occurs, int depth, PrintStream out) {
        if (entity == null) return;
        String prolog = "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>" ;
        String skos = "PREFIX skos: <http://www.w3.org/2004/02/skos/core#>" ;
        String foaf = "PREFIX foaf: <http://xmlns.com/foaf/0.1/> ";
        String bnf = "PREFIX bnf-onto: <http://data.bnf.fr/ontology/bnf-onto/>";
        //System.out.println("entity "+entity+ " size occu "+occurs.size()+" depth "+ depth);
        String nodeActuel = entity;
        String queryString = skos + foaf+ bnf+NL +
                "SELECT * WHERE {" +
                "<"+nodeActuel+">"+" " +
                "skos:narrower ?pL ." +
                "}" ;

        Query query = QueryFactory.create(queryString);

        if (!occurs.contains(entity)) {
            // print depth times "\t" to retrieve an explorer tree like output
            for (int i = 0; i < depth; i++) {
                System.out.print("\t");
            }
            System.out.println(entity); //node x
            out.println(entity);


            try ( QueryExecution qexec = QueryExecutionFactory.sparqlService("http://data.bnf.fr/sparql", query); ) {
                PoolingHttpClientConnectionManager cm = new PoolingHttpClientConnectionManager();
                cm.setMaxTotal(2000);
                cm.setDefaultMaxPerRoute(200);
                HttpHost localhost = new HttpHost("locahost", 80);
                cm.setMaxPerRoute(new HttpRoute(localhost), 100);

                CloseableHttpClient httpClient = HttpClients.custom()
                        .setConnectionManager(cm)
                        .build();

                HttpOp.setDefaultHttpClient(httpClient);

                QueryEngineHTTP objectToExec= (QueryEngineHTTP) QueryExecutionFactory.sparqlService("http://data.bnf.fr/sparql",query, httpClient);

                ResultSet results = qexec.execSelect();

                while (results.hasNext()) {
                    QuerySolution soln = results.nextSolution();
                    RDFNode sub = soln.get("pL");
                    RDFNode frnd =soln.get("pL");
                    RDFNode cMatch=soln.get("pL");
                    // System.out.println("sub "+sub.toString());
                    if (!sub.isURIResource()) continue;
                    // push this expression on the occurs list before we recurse to avoid loops
                    occurs.add(entity);
                    // traverse down and increase depth (used for logging tabs)
                    traverse(sub.toString(), frnd.toString(),cMatch.toString(), occurs, depth + 1, out);
                    // after traversing the path, remove from occurs list
                    occurs.remove(entity);

                }
            }
        }

    }

    public static void main(String[] args) {
        PrintStream out = null;
        try {
            out = new PrintStream(new FileOutputStream("./src/output.txt"));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        String model = "http://data.bnf.fr/ark:/12148/cb119759109";
        System.out.println("model "+model);
        traverseStart(model, null, out);
    }

}
