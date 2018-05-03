/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */


// The ARQ application API.

import org.apache.jena.query.*;
import org.apache.jena.rdf.model.RDFNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

/**
 * Example 1 : Execute a simple SELECT query on a model
 * to find the DC titles contained in a model.
 */

public class main {
    private static final Logger log = LoggerFactory.getLogger(main.class);
    static public final String NL = System.getProperty("line.separator");

       public void getBranchs(String nodeA){
             String prolog = "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>" ;
             String skos = "PREFIX skos: <http://www.w3.org/2004/02/skos/core#>" ;
             System.out.println(nodeA);
             String nodeActuel = nodeA;
             String queryString = skos + NL +
                    "SELECT ?pL  WHERE {" +
                    "<"+nodeActuel+">"+" skos:prefLabel ?prefLabel ;"+
                    "skos:narrower ?pL ." +
                    "?pL skos:prefLabel ?label"+
                    "}order by asc (?pL)" ;

        Query query = QueryFactory.create(queryString) ;
        QueryExecution qexec = QueryExecutionFactory.sparqlService("http://data.bnf.fr/sparql", query);
        ResultSet results = qexec.execSelect();
        //ResultSetFormatter.outputAsJSON(results);
           List<RDFNode> result = new ArrayList<RDFNode>();
        int i=0;
            for ( ; results.hasNext() ; ){
            QuerySolution soln = results.nextSolution() ;
            RDFNode x = soln.get("pL") ;       // Get a result variable by name.
                if( !x.isURIResource() ) continue;
            result.add(x);
           // System.out.print("size "+result.size());
             //  Resource r = soln.getResource("VarR") ; // Get a result variable - must be a resource
            //   Literal l = soln.getLiteral("VarL") ;   // Get a result variable - must be a literal
            System.out.println("ind " + i +" "+x.toString()+ " "+ x.asResource().getLocalName());
            i++;
           // getBranchs(x.toString());//recursividad
       }}


    public static void main(String[] args) {
       // RDFNode nodeA=("<http://data.bnf.fr/ark:/12148/cb119759109>").;
        main miMain=new main();
        miMain.getBranchs("http://data.bnf.fr/ark:/12148/cb119759109");

       /* String prolog = "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>";
        String skos = "PREFIX skos: <http://www.w3.org/2004/02/skos/core#>";
        String nodeActuel = "<http://data.bnf.fr/ark:/12148/cb119759109>";
        String queryString = skos + NL +
                "SELECT ?pL  WHERE {" +
                nodeActuel + " skos:prefLabel ?prefLabel ;" +
                "skos:narrower ?pL ." +
                "?pL skos:prefLabel ?label" +
                "}";

        Query query = QueryFactory.create(queryString);
        QueryExecution qexec = QueryExecutionFactory.sparqlService("http://data.bnf.fr/sparql", query);
        ResultSet results = qexec.execSelect();
        System.out.println("test " + results);
        //ResultSetFormatter.outputAsJSON(results);}}
        int i = 0;
        for (; results.hasNext(); ) {
            QuerySolution soln = results.nextSolution();
            RDFNode x = soln.get("pL");       // Get a result variable by name.
            //  Resource r = soln.getResource("VarR") ; // Get a result variable - must be a resource
            //   Literal l = soln.getLiteral("VarL") ;   // Get a result variable - must be a literal
            System.out.println("ind " + i + " " + x);
            i++;
           // doTree(x);
        }*/
    }
}
/*
import java.io.FileInputStream;
        import java.io.IOException;
        import java.io.InputStream;
        import java.util.ArrayList;
        import java.util.List;

        import org.apache.jena.ontology.OntModel;
        import org.apache.jena.query.Query;
        import org.apache.jena.query.QueryExecution;
        import org.apache.jena.query.QueryExecutionFactory;
        import org.apache.jena.query.QueryFactory;
        import org.apache.jena.query.QuerySolution;
        import org.apache.jena.query.ResultSet;
        import org.apache.jena.rdf.model.ModelFactory;
        import org.apache.jena.rdf.model.RDFNode;


public class OntologyTraverserSPARQL
{
    public static void readOntology( String file, OntModel model )
    {
        InputStream in = null;
        try
        {
            in = new FileInputStream( file );
            model.read( in, "RDF/XML" );
            in.close();
        } catch ( IOException e )
        {
            e.printStackTrace();
        }
    }

    private static List<String> getRoots( OntModel model )
    {
        List<String> roots = new ArrayList<String>();

        // find all owl:Class entities and filter these which do not have a parent
        String getRootsQuery =
                "SELECT DISTINCT ?s WHERE "
                        + "{"
                        + "  ?s <http://www.w3.org/2000/01/rdf-schema#subClassOf> <http://www.w3.org/2002/07/owl#Thing> . "
                        + "  FILTER ( ?s != <http://www.w3.org/2002/07/owl#Thing> && ?s != <http://www.w3.org/2002/07/owl#Nothing> ) . "
                        + "  OPTIONAL { ?s <http://www.w3.org/2000/01/rdf-schema#subClassOf> ?super . "
                        + "  FILTER ( ?super != <http://www.w3.org/2002/07/owl#Thing> && ?super != ?s ) } . "
                        + "}";

        Query query = QueryFactory.create( getRootsQuery );

        try ( QueryExecution qexec = QueryExecutionFactory.create( query, model ) )
        {
            ResultSet results = qexec.execSelect();
            while( results.hasNext() )
            {
                QuerySolution soln = results.nextSolution();
                RDFNode sub = soln.get("s");

                if( !sub.isURIResource() ) continue;

                roots.add( sub.toString() );
            }
        }

        return roots;
    }

    public static void traverseStart( OntModel model, String entity )
    {
        // if starting class available
        if( entity != null )
        {
            traverse( model, entity,  new ArrayList<String>(), 0  );
        }
        // get roots and traverse each root
        else
        {
            List<String> roots = getRoots( model );

            for( int i = 0; i < roots.size(); i++ )
            {
                traverse( model, roots.get( i ), new ArrayList<String>(), 0 );
            }
        }
    }

    public static void traverse( OntModel model, String entity, List<String> occurs, int depth )
    {
        if( entity == null ) return;

        String queryString 	= "SELECT ?s WHERE { "
                + "?s <http://www.w3.org/2000/01/rdf-schema#subClassOf> <" + entity + "> . }" ;

        Query query = QueryFactory.create( queryString  );

        if ( !occurs.contains( entity ) )
        {
            // print depth times "\t" to retrieve an explorer tree like output
            for( int i = 0; i < depth; i++ ) { System.out.print("\t"); }
            // print out the URI
            System.out.println( entity );

            try ( QueryExecution qexec = QueryExecutionFactory.create( query, model ) )
            {
                ResultSet results = qexec.execSelect();
                while( results.hasNext() )
                {
                    QuerySolution soln = results.nextSolution();
                    RDFNode sub = soln.get("s");

                    if( !sub.isURIResource() ) continue;

                    String str = sub.toString();

                    // push this expression on the occurs list before we recurse to avoid loops
                    occurs.add( entity );
                    // traverse down and increase depth (used for logging tabs)
                    traverse( model, str, occurs, depth + 1 );
                    // after traversing the path, remove from occurs list
                    occurs.remove( entity );
                }
            }
        }

    }

    public static void main(String[] args)
    {
        // create OntModel
        OntModel model = ModelFactory.createOntologyModel();
        // read camera ontology
        readOntology( "./ontology/camera.owl", model );
        // start traverse
        traverseStart( model, null );
    }

}


/*
        +
                    "filter (?l =\""+tokens.get(i)+"\"@fr )" +
        try {
        out = new PrintWriter("list.txt");
    } catch (FileNotFoundException e) {
        e.printStackTrace();
    }
    for (RDFNode line : result) {
        out.println(result);
    }
        String queryString = prolog + NL +
                "select ?s where {" +
                "?s rdfs:label ?l . " +
                "filter (?l =\""+tokens.get(i)+"\"@fr || ?l= \"" + tokens.get(i) + "\"@en )" +
                "}" ;
        List<RDFNode> result = new ArrayList<RDFNode>();
        QueryExecution qexec = QueryExecutionFactory.sparqlService("https://dbpedia.org/sparql", query);
        ResultSet results = qexec.execSelect();
            for ( ; results.hasNext() ; )
            {
                ResultSetFormatter.out(System.out, results, query);
                result.add(a);
            }

        System.out.println(result) ;
      //  ResultSetFormatter.out(System.out, results);
            PrintStream out = null;
            try {
                out = new PrintStream(new FileOutputStream("./src/output.txt"));
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
          //  System.setOut(out);
            ResultSetFormatter.out(out, results, query);
        }
    }
}
*/