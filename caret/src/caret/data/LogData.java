package caret.data;

import java.util.ArrayList;
import java.util.List;

import org.bson.Document;
import org.eclipse.core.resources.IProject;
import org.eclipse.jface.preference.IPreferenceStore;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;

import caret.Activator;
import caret.preferences.PreferenceConstants;
import caret.tool.Util;

public class LogData {
	
	public static List<Interaction> getInteractionsJSONLog(IProject project) {
		String projectPath = project.getLocation().toString();
		System.out.println("##getIteractionsJSON: " + projectPath+"/.log");
		List<Interaction> totalInteractions = new ArrayList<Interaction> ();
		List <String> listInteractionsJSON = Util.readFilesFromDirectory(projectPath+"/.log", ".json");
		Gson gson = new Gson();
		for (String interactionsJSON : listInteractionsJSON) {
			List<Interaction> interactions = gson.fromJson(interactionsJSON, new TypeToken<List<Interaction>>() {}.getType());
	        totalInteractions.addAll(interactions);
		}
		return totalInteractions;
	}
	
	public MongoDB getMongoDBInteractions() {
        IPreferenceStore store;
        String mongoURI;
        String mongoDatabase;
        store = Activator.getDefault().getPreferenceStore();
        mongoDatabase = store.getString(PreferenceConstants.P_MONGO_DATABASE);
        mongoURI  = store.getString(PreferenceConstants.P_MONGO_URI);
        
        if (mongoURI != null && !mongoURI.isEmpty()  && mongoDatabase != null && !mongoDatabase.isEmpty()) {
            MongoDB mongoDB = new MongoDB();
            mongoDB.setDatabase(mongoDatabase);
            mongoDB.setConnectionURI(mongoURI);
            
            if(mongoDB.connect()) {
                mongoDB.setupCollection("interactions");
                return mongoDB;
            }
        }
        return null;
	}
	
	public static List<Interaction> getInteractionsJSON(IProject project) {
		List<Interaction> totalInteractions = new ArrayList<Interaction>();
		
		// 1. Obtener la instancia conectada de tu clase MongoDB
		LogData logDataInstance = new LogData();
		MongoDB mongoDBInstance = logDataInstance.getMongoDBInteractions();
		
		if (mongoDBInstance == null) {
			System.err.println("## Error: No se pudo conectar a MongoDB.");
			return totalInteractions;
		}
		
		try {
			// 2. Obtener la colección usando el método exacto de tu clase: getMongoCollection()
			MongoCollection<Document> collection = mongoDBInstance.getMongoCollection(); 
			
			if (collection == null) {
				System.err.println("## Error: La colección no está inicializada.");
				return totalInteractions;
			}
			
			String projectName = project.getName();
			System.out.println("## getInteractionsFromMongoDB para el proyecto: " + projectName);
			
			// 3. Crear el filtro usando la estructura nativa Document (evita errores de imports de Filters)
			// Esto equivale a: { "context.resource.projectName": projectName, "role": "CARET" }
			Document query = new Document();
			query.append("context.resource.projectName", projectName);
			query.append("role", "CARET");
			
			FindIterable<Document> results = collection.find(query);
			
			Gson gson = new Gson();
			
			// 4. Iterar los resultados y convertirlos a objetos Interaction
			for (Document doc : results) {
				String json = doc.toJson();
				Interaction interaction = gson.fromJson(json, Interaction.class);
				totalInteractions.add(interaction);
			}
			
			System.out.println("## Total de interacciones CARET recuperadas: " + totalInteractions.size());
			
		} catch (Exception e) {
			System.err.println("## Error al leer de MongoDB: " + e.getMessage());
			e.printStackTrace();
		} finally {
			// 5. Cerrar la conexión usando el método close() de tu clase MongoDB
			if (mongoDBInstance != null) {
				mongoDBInstance.close();
			}
		}
		
		return totalInteractions;
	}

}
