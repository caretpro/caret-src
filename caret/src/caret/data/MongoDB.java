package caret.data;

import java.util.ArrayList;
import java.util.List;

import org.bson.Document;

import com.google.gson.Gson;
import com.mongodb.MongoClientSettings;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import java.util.concurrent.TimeUnit;

public class MongoDB {

    private String DB_NAME = "db_caret";
    private static final String COLLECTION = "interactions";
    private String collection; 
    private MongoClient client;
    private MongoDatabase db;
    private MongoCollection<Document> mongoCollection;
    private Gson gson = new Gson();
    
    private String database;
    private String connectionURI;

	public MongoDB() {
    	
    }
	
	public MongoDB(String connectionURI, String database) {
    	this.connectionURI = connectionURI;
    	this.database = database;
    }
    
    public boolean connect() {
        System.out.println("Connecting to MongoDB...");

        try {
            MongoClientSettings settings = MongoClientSettings.builder()
                    .applyConnectionString(new com.mongodb.ConnectionString(getConnectionURI()))
                    .applyToSocketSettings(builder ->
                            builder.connectTimeout(10, TimeUnit.SECONDS)
                                   .readTimeout(10, TimeUnit.SECONDS))
                    .applyToClusterSettings(builder ->
                            builder.serverSelectionTimeout(10, TimeUnit.SECONDS))
                    .build();

            client = MongoClients.create(settings);
            db = client.getDatabase(DB_NAME);

            db.runCommand(new Document("ping", 1));

            //mongoCollection = db.getCollection(this.collection);

            System.out.println("Connected to database: " + DB_NAME);
            return true;

        } catch (Exception e) {
            System.err.println("MongoDB connection failed: " + e.getMessage());

            if (client != null) {
                try {
                    client.close();
                } catch (Exception ignored) {
                }
                client = null;
            }
        }

        return false;
    }

    public void setupCollection(String collection) {
    	this.collection = collection;
        ArrayList<String> names = db.listCollectionNames().into(new ArrayList<>());

        if (!names.contains(collection)) {
            db.createCollection(collection);
            System.out.println("Collection created: " + collection);
        } else {
            System.out.println("Collection already exists: " + collection);
        }

        mongoCollection = db.getCollection(collection);
    }

    public MongoCollection<Document> getMongoCollection() {
		return mongoCollection;
	}

	public void addDocument(Interaction interaction) {
        String json = gson.toJson(interaction);
        Document doc = Document.parse(json);

        mongoCollection.insertOne(doc);

        System.out.println("Document inserted successfully into " + DB_NAME + "." + COLLECTION);
    }

    public void getDocument(String hash) {

        Document query = new Document("hash", hash);

        Document result = mongoCollection.find(query).first();

        if (result != null) {
            System.out.println("Document found:");
            System.out.println(result.toJson());
        } else {
            System.out.println("No document found with hash: " + hash);
        }
    }
    
    public void getDocument(long timestamp) {

        Document query = new Document("timestamp", timestamp);

        Document result = mongoCollection.find(query).first();

        if (result != null) {
            System.out.println("Document found:");
            System.out.println(result.toJson());
        } else {
            System.out.println("No document found with timestamp: " + timestamp);
        }
    }

    public void close() {
        if (client != null) {
            client.close();
        }
    }
    
    public String getDatabase() {
        return database;
    }

    public void setDatabase(String database) {
        this.database = database;
    }

    public String executeQueryClean(String jsonQuery) {
	    if (mongoCollection == null) return "Error: No connection.";
	    
	    List<Document> results = new ArrayList<>();
	    StringBuilder response = new StringBuilder();
	
	    try {
	        String cleanQuery = jsonQuery.trim();
	        if (cleanQuery.startsWith("[")) {
	            List<Document> pipeline = Document.parse("{ \"p\": " + cleanQuery + " }")
	                                              .getList("p", Document.class);
	            mongoCollection.aggregate(pipeline).into(results);
	        } else {
	            mongoCollection.find(Document.parse(cleanQuery)).into(results);
	        }
	        for (Document doc : results) {
	            List<String> lineValues = new ArrayList<>();
	            
	            for (java.util.Map.Entry<String, Object> entry : doc.entrySet()) {
	                Object value = entry.getValue();
	                if (value != null) {
	                    lineValues.add(value.toString());
	                }
	            }
	            
	            if (!lineValues.isEmpty()) {
	                response.append(String.join(" - ", lineValues)).append("\n");
	            }
	        }
	    } catch (Exception e) {
	        return "Error: " + e.getMessage();
	    }
	
	    return response.toString().trim();
	}
    

    public String getConnectionURI() {
		return connectionURI;
	}


	public void setConnectionURI(String connectionURI) {
		this.connectionURI = connectionURI;
	}
	public MongoClient getClient() {
	    return this.client;
	}

}
