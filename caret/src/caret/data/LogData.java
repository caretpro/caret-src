package caret.data;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.resources.IProject;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import caret.tool.Util;

public class LogData {
	
	public static List<Interaction> getIteractionsJSON(IProject project) {
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

}
