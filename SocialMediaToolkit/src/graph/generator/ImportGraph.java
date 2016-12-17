package graph.generator;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Scanner;

import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Relationship;
import org.neo4j.graphdb.RelationshipType;
import org.neo4j.graphdb.Transaction;
import org.neo4j.graphdb.index.Index;
import org.neo4j.kernel.AbstractGraphDatabase;
import org.neo4j.kernel.EmbeddedGraphDatabase;

public class ImportGraph {

	public static final String ID = "id";

	public static enum RelationshipTypes implements RelationshipType {
		A,
		B
	}

	private static AbstractGraphDatabase graphDB = null;
	private static Index<Node> nodeIndex = null;

	public static void main(String[] args) throws IOException {

		String database = "primary-test";
		String filename = "edges.csv";

		graphDB = new EmbeddedGraphDatabase(database);
		nodeIndex = graphDB.index().forNodes("nodes");

		File file = new File(filename);
		int numLines = count(filename);
		
		Scanner scanner = new Scanner(file);
		/*
		int counter = 0; 
		while(scanner.hasNextLine()){
			String line = scanner.nextLine();
			try {
				String[] data = line.split(",");
				Integer.parseInt(data[0]); // this will break if the line is invalid
				Integer.parseInt(data[1]); // this will break if the line is invalid
				if(data[2].equals("A")){
					createNode("A" + data[0], data[3]);
				} else if(data[2].equals("B")){
					createNode("B" + data[0], data[3]);
				} else {
					System.err.println(data);
				}
				
			} catch (Exception e){
				System.err.println("Could not parse line: " + line);
			}
			counter++;
			if(counter % 1000 == 0){
				System.out.println((((double)counter)/numLines)*100 + " %");
			}
		}
		*/
		
		int counter = 0; 
		while(scanner.hasNextLine()){
			String line = scanner.nextLine();
			try {
				String[] data = line.split(",");
				if(data[2].equals("A")){
					createRelationship(data[0], data[1], RelationshipTypes.A);
					Node source = getNode(data[0]);
					Transaction tx = graphDB.beginTx();
					try {
						source.setProperty("id", data[3]);
						tx.success();
					} catch (Exception e) {
						e.printStackTrace();
					} finally {
						tx.finish();
					}
				} else if(data[2].equals("B")){
					createRelationship(data[0], data[1], RelationshipTypes.B);
					Node source = getNode(data[0]);
					Transaction tx = graphDB.beginTx();
					try {
						source.setProperty("id", data[3]);
						tx.success();
					} catch (Exception e) {
						e.printStackTrace();
					} finally {
						tx.finish();
					}
				} else {
					System.err.println(data);
				}
				
			} catch (Exception e){
				System.err.println("Could not parse line: " + line);
			}
			counter++;
			if(counter % 1000 == 0){
				System.out.println((((double)counter)/numLines)*100 + " %");
			}
		}
		
		graphDB.shutdown();
		System.out.println("Finished.");
	}

	public static int count(String filename) throws IOException {
	    InputStream is = new BufferedInputStream(new FileInputStream(filename));
	    try {
	        byte[] c = new byte[1024];
	        int count = 0;
	        int readChars = 0;
	        while ((readChars = is.read(c)) != -1) {
	            for (int i = 0; i < readChars; ++i) {
	                if (c[i] == '\n')
	                    ++count;
	            }
	        }
	        return count;
	    } finally {
	        is.close();
	    }
	}
	
	private static Node getNode(final String id) {
		return nodeIndex.get(ID, (id)).getSingle();
	}

	private static Node findOrCreateNode(final String id) {
		Node node = getNode(id);
		if (node == null) {
			node = graphDB.createNode();
			node.setProperty(ID, id);
			nodeIndex.add(node, ID, id);
		}
		return node;
	}
	
	private static void createRelationship(String idA, String idB, RelationshipType releationship) {
		Transaction tx = graphDB.beginTx();
		try {
			Node a = findOrCreateNode(idA);
			Node b = findOrCreateNode(idB);

			// add relationship
			a.createRelationshipTo(b,releationship);

			tx.success();
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			tx.finish();
		}
	}

}
