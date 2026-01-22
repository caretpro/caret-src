package caret.stats;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import caret.data.Interaction;
import caret.tool.Util;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.eclipse.core.resources.IProject;

public class StatisticsQuery {

    private String query;
    private String javaMethod;
    private long timestamp;
    private String user;

    public StatisticsQuery() {}

    public StatisticsQuery(String query, String javaMethod, String user) {
        this.query = query;
        this.javaMethod = javaMethod;
        this.user = user;
        this.timestamp = new Date().getTime();
    }

    public String getQuery() {
        return query;
    }

    public void setQuery(String query) {
        this.query = query;
    }

    public String getJavaMethod() {
        return javaMethod;
    }

    public void setJavaMethod(String javaMethod) {
        this.javaMethod = javaMethod;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public String getUser() {
        return user;
    }

    public void setUser(String user) {
        this.user = user;
    }
    
    public static List<StatisticsQuery> getQueriesJSON(IProject project) {
		String projectPath = project.getLocation().toString();
		List<StatisticsQuery> totalInteractions = new ArrayList<StatisticsQuery> ();
		List <String> listInteractionsJSON = Util.readFilesFromDirectory(projectPath+"/.log", ".gson");
		Gson gson = new Gson();
		for (String interactionsJSON : listInteractionsJSON) {
			List<StatisticsQuery> interactions = gson.fromJson(interactionsJSON, new TypeToken<List<StatisticsQuery>>() {}.getType());
	        totalInteractions.addAll(interactions);
		}
		return totalInteractions;
	}

}
