package caret.data;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import caret.tool.Util;

public interface Extractor {

	public default List<Interaction> getIteractionsJSON(String projectPath) {
		List<Interaction> totalInteractions = new ArrayList<Interaction> ();
		List <String> listInteractionsJSON = Util.readFilesFromDirectory(projectPath+File.separator+".log", ".json");
		Gson gson = new Gson();
		for (String interactionsJSON : listInteractionsJSON) {
			List<Interaction> interactions = gson.fromJson(interactionsJSON, new TypeToken<List<Interaction>>() {}.getType());
	        totalInteractions.addAll(interactions);
		}
		return totalInteractions;
	}
	
    /**
     * Returns the result of a query as a String.
     * @return query result in String format
     */
    public String getData(String projectPath);
}
