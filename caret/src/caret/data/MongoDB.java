package caret.data;

import java.util.ArrayList;

import org.bson.Document;

import com.google.gson.Gson;
import com.mongodb.MongoClientSettings;
import com.mongodb.MongoTimeoutException;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import java.util.concurrent.TimeUnit;

public class MongoDB {

    private String CONNECTION_STRING = "mongodb+srv://albertcorome_db_user:xJ6M1LG69U40CgYe@caret-cluster.wefhfh9.mongodb.net/?appName=caret-cluster";

    private String DB_NAME = "db_caret";
    private static final String COLLECTION = "interactions";
    private MongoClient client;
    private MongoDatabase db;
    private MongoCollection<Document> collection;
    private Gson gson = new Gson();
    

    private String user;
    private String password;
    private String host;
    private String database;
    private String appName;

    public MongoDB() {
    }

    /*public void connect() {
        System.out.println("Connecting to MongoDB...");
        String connectionString = getConnectionString();
        client = MongoClients.create(connectionString);
        //client = MongoClients.create(CONNECTION_STRING);
        db = client.getDatabase(DB_NAME);
        collection = db.getCollection(COLLECTION);

        System.out.println("Connected to database: " + DB_NAME);
    }*/
    
    

    public boolean connect() {
        System.out.println("Connecting to MongoDB...");

        try {
            MongoClientSettings settings = MongoClientSettings.builder()
                    .applyConnectionString(new com.mongodb.ConnectionString(getConnectionString()))
                    .applyToSocketSettings(builder ->
                            builder.connectTimeout(10, TimeUnit.SECONDS)
                                   .readTimeout(10, TimeUnit.SECONDS))
                    .applyToClusterSettings(builder ->
                            builder.serverSelectionTimeout(10, TimeUnit.SECONDS))
                    .build();

            client = MongoClients.create(settings);
            db = client.getDatabase(DB_NAME);

            db.runCommand(new Document("ping", 1));

            collection = db.getCollection(COLLECTION);

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

    public void setupCollection() {
        ArrayList<String> names = db.listCollectionNames().into(new ArrayList<>());

        if (!names.contains(COLLECTION)) {
            db.createCollection(COLLECTION);
            System.out.println("Collection created: " + COLLECTION);
        } else {
            System.out.println("Collection already exists: " + COLLECTION);
        }

        collection = db.getCollection(COLLECTION);
    }

    public void addDocument(Interaction interaction) {
        String json = gson.toJson(interaction);
        Document doc = Document.parse(json);

        collection.insertOne(doc);

        System.out.println("Document inserted successfully into " + DB_NAME + "." + COLLECTION);
    }

    public void getDocument(String hash) {

        Document query = new Document("hash", hash);

        Document result = collection.find(query).first();

        if (result != null) {
            System.out.println("Document found:");
            System.out.println(result.toJson());
        } else {
            System.out.println("No document found with hash: " + hash);
        }
    }
    
    public void getDocument(long timestamp) {

        Document query = new Document("timestamp", timestamp);

        Document result = collection.find(query).first();

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
    
    public String getConnectionString() {
    	String connectionString = "mongodb+srv://"+getUser()+":"+getPassword()+"@"+getHost()+"/?appName="+getAppName();
    	return connectionString;
    }
    
    public String getUser() {
        return user;
    }

    public void setUser(String user) {
        this.user = user;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public String getDatabase() {
        return database;
    }

    public void setDatabase(String database) {
        this.database = database;
    }

    public String getAppName() {
        return appName;
    }

    public void setAppName(String appName) {
        this.appName = appName;
    }

}
