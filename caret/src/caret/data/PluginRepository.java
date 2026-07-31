package caret.data;

import com.google.gson.Gson;

import java.util.Arrays;

import org.bson.Document;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.Accumulators;
import com.mongodb.client.model.Aggregates;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.ReplaceOptions;
import com.mongodb.client.model.Updates;

import caret.tool.Log;

import org.bson.conversions.Bson;

public class PluginRepository {

    private final MongoDB mongoDb;
    private final MongoCollection<Document> collection;
    private final Gson gson;

    public PluginRepository(MongoDB mongoDb) {
        this.mongoDb = mongoDb;
        this.gson = new Gson();
        // Accessing the specific database and collection
        this.collection = mongoDb.getMongoCollection();
    }
    
    public void savePlugin(PluginDocument plugin) {
        Log.d("saving " + plugin.getName() + " (v" + plugin.getVersion() + ")" + ": " + plugin.getInstallCount());
        try {
            String json = gson.toJson(plugin);
            Document doc = Document.parse(json);

            Bson filter = Filters.and(
                Filters.eq("id", plugin.getId()),
                Filters.eq("version", plugin.getVersion())
            );

            com.mongodb.client.result.UpdateResult result = collection.replaceOne(
                filter, 
                doc, 
                new ReplaceOptions().upsert(true)
            );
            
            if (result.getUpsertedId() != null) {
                Log.d("Plugin CREATED (Upsert) - MongoDB ID: " + result.getUpsertedId());
            } else if (result.getMatchedCount() > 0) {
                if (result.getModifiedCount() > 0) {
                    Log.d("Plugin UPDATED - Counter/data was modified.");
                } else {
                    Log.d("Plugin FOUND - But NO changes made (data was already identical).");
                }
            } else {
                Log.d("No changes were made to the database.");
            }

            Log.d("Plugin processed: " + plugin.getName() + " (v" + plugin.getVersion() + ")");
        } catch (Exception e) {
            Log.d("Error saving plugin: " + e.getMessage());
        }
    }
    /**
     * Retrieves a PluginDocument from the database by its unique ID string.
     * Note: If you have multiple versions, this returns the first one found.
     */
    public PluginDocument getPluginById(String id) {
        try {
            Document doc = collection.find(Filters.eq("id", id)).first();
            
            if (doc != null) {
                return gson.fromJson(doc.toJson(), PluginDocument.class);
            }
        } catch (Exception e) {
            System.err.println("Error retrieving plugin: " + e.getMessage());
        }
        return null;
    }

    /**
     * Retrieves a specific version of a plugin.
     */
    public PluginDocument getPluginByIdAndVersion(String id, String version) {
        try {
            Bson filter = Filters.and(Filters.eq("id", id), Filters.eq("version", version));
            Document doc = collection.find(filter).first();
            
            if (doc != null) {
                return gson.fromJson(doc.toJson(), PluginDocument.class);
            }
        } catch (Exception e) {
            System.err.println("Error retrieving specific plugin version: " + e.getMessage());
        }
        return null;
    }
    
    public void incrementInstallCount(String id, String version) {
        try {
            // Filter to find the specific document
            Bson filter = Filters.and(
                Filters.eq("id", id),
                Filters.eq("version", version)
            );

            // Atomic increment of the "installCount" field
            Bson update = Updates.inc("installCount", 1);

            collection.updateOne(filter, update);
            
            System.out.println("Incremented install count for plugin: " + id + " v" + version);
        } catch (Exception e) {
            System.err.println("Error incrementing install count: " + e.getMessage());
        }
    }
    
    public int getTotalInstallCount(String id) {
        try {
            Bson match = Aggregates.match(Filters.eq("id", id));
            Bson group = Aggregates.group(null, Accumulators.sum("totalInstalls", "$installCount"));
            Document result = collection.aggregate(Arrays.asList(match, group)).first();
            if (result != null && result.containsKey("totalInstalls")) {
                return result.getInteger("totalInstalls", 0);
            }
        } catch (Exception e) {
            Log.d("Error al calcular el contador total para " + id + ": " + e.getMessage());
        }
        return 0;
    }
}