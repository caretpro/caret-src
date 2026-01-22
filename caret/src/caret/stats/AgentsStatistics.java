package caret.stats;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import caret.data.Extractor;
import caret.data.Interaction;
import caret.tool.Util;

public class AgentsStatistics implements Extractor{
	
	public AgentsStatistics (){
		
	}
  
    /*
     * Query: get best agent based on acceptance
     */
	@Override
	public String getData(String projectPath) {
        if (projectPath == null) {
            System.err.println("No project found.");
            return null;
        }

        List<Interaction> interactions = getIteractionsJSON(projectPath);
        if (interactions.isEmpty()) {
            System.err.println("No interactions found in " + projectPath);
            return null;
        }

        Map<String, int[]> agentStats = new HashMap<>();
        // [0] = total interactions, [1] = passed pre-validations

        for (Interaction interaction : interactions) {
            if (interaction.getResult() == null || interaction.getResult().getAgent() == null)
                continue;

            String agentName = interaction.getResult().getAgent().getName();
            agentStats.putIfAbsent(agentName, new int[2]);
            agentStats.get(agentName)[0]++; // total
            if (interaction.isPassedPreValidations()) {
                agentStats.get(agentName)[1]++; // passed
            }
        }

        String bestAgent = null;
        double bestRate = -1.0;

        for (Map.Entry<String, int[]> entry : agentStats.entrySet()) {
            String agent = entry.getKey();
            int total = entry.getValue()[0];
            int passed = entry.getValue()[1];
            double rate = (total > 0) ? (double) passed / total : 0.0;

            System.out.printf("Agent: %s | Passed: %d | Total: %d | Rate: %.2f%%%n",
                    agent, passed, total, rate * 100);

            if (rate > bestRate) {
                bestRate = rate;
                bestAgent = agent;
            }
        }

        return bestAgent;
	}
    
    public void print(String projectPath) {
        String bestAgent = getData(projectPath);
        System.out.println("# Best agent [Dynamic Mode]: " + bestAgent);
    }

}
