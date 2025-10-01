package caret.agent.gemini;

import com.google.gson.Gson;

import caret.agent.AgentInterface;
import caret.agent.Response;
import caret.agent.gemini.preferences.PreferenceConstants;
import caret.tool.Log;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.jface.preference.IPreferenceStore;

public class AgentGemini implements AgentInterface{
	
	private IPreferenceStore store;
	private String ID = "Gemini";
	private String name = "Gemini";
	private String technology = "Gemini";
	private String key="";
	private String url="https://generativelanguage.googleapis.com/v1beta/openai/chat/completions";
	private String model="gemini-2.0-flash";
	private float defaultTemperature = 0.7F;
	
	@Override
	public void setKey(String key) {
		this.key = key;
	}

	@Override
	public String getKey() {
		return this.key;
	}

	@Override
	public Response processMessage(String message, float temperature) {
		store = Activator.getDefault().getPreferenceStore();
		key = store.getString(PreferenceConstants.P_GEMINI_KEY);
		model = store.getString(PreferenceConstants.P_GEMINI_MODEL);
		url = store.getString(PreferenceConstants.P_GEMINI_URL);
		if(!(temperature >= 0.0 && temperature <=2.0)) {
			temperature = defaultTemperature;
		}
		Response response = new Response();
		try {
			Gson gson = new Gson();
			
			String jsonString = createPromptJson(model, "user", message, temperature, false);
			Log.d("GEMINI INPUT: "+jsonString);
			URL _url = new URI(url).toURL();
			HttpURLConnection connection = (HttpURLConnection) _url.openConnection();
			connection.setConnectTimeout(5000);
			connection.setReadTimeout(40000);
			connection.setRequestProperty("Authorization", "Bearer "+getKey());
			connection.setRequestProperty("Content-type", "application/json");
			connection.setDoOutput(true);
			connection.setDoInput(true);
			connection.setRequestMethod("POST");
			OutputStream os = connection.getOutputStream();
			os.write(jsonString.getBytes("UTF-8"));
			connection.connect();
			int responseStatusCode = connection.getResponseCode();
			System.out.println("##HTTP RESPONSE CODE:"+responseStatusCode);
			if (responseStatusCode < 200 || responseStatusCode >= 300) {
	   	 		response.setError(true);
	   	 		response.setErrorMessage("Connection error ("+responseStatusCode+")");
	   	 		return response;
	   	 	}     
			BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
			StringBuilder result = new StringBuilder();
			String line;
			while ((line = reader.readLine()) != null) {
			    result.append(line);
			}   
			String responseBody = result.toString();

	   	 	caret.agent.gemini.openai.QueryResponse queryResponse = gson.fromJson(responseBody, caret.agent.gemini.openai.QueryResponse.class);
	   	    response.setText(queryResponse.getChoices()[0].getMessage().getContent());
	   	    response.setCode(getCode(queryResponse.getChoices()[0].getMessage().getContent()));
	   	    response.setFallbackIntent(false);
			
		} catch (Exception e) {
			Log.e("Error: "+e.getMessage());
			response.setError(true);
			response.setErrorMessage("Connection error");
		}
		return response;
	}

	@Override
	public boolean hasIntent() {
		return false;
	}

	@Override
	public String getId() {
		return this.ID;
	}

	public String getCode(String text) {
		String code = null;
		int indexCode;
		int endCode;
		try {
			if((text.indexOf("import ") == 0 || text.indexOf("@Override") == 0|| text.indexOf("@RunWith") == 0 || text.indexOf("/**") == 0 || text.indexOf("//") == 0 || text.indexOf("private ") == 0 || text.indexOf("public ") == 0|| text.indexOf("public class ") == 0 || text.indexOf("public interface ") == 0 || text.indexOf("public abstract class ") == 0) 
					&&(text.substring(text.length()-1).equals("}") || text.substring(text.length()-3).indexOf("}")>=0)) {
				code = text;
			}else {
				indexCode = text.indexOf("```java");
				if(indexCode>=0) {
					indexCode += 7;
				}else {
					indexCode = text.indexOf("```");
					if(indexCode>=0) {
						indexCode += 3;
					}
				}
			    endCode = text.indexOf("```",indexCode);
			    if(endCode>indexCode) {
				    code = text.substring(indexCode, endCode-1);
			    }else {
					indexCode = text.indexOf(":");
					if(indexCode>=0 && text.indexOf("{",indexCode+6)>=0) {
						indexCode += 1;
						endCode = text.lastIndexOf("}");
						if(endCode>indexCode) {
							code = text.substring(indexCode, endCode+1);
						}
					}
				}
			}
			if(code!=null){
				Log.d("### Code - INIT ### GEMINI\n"+code+"\n### Code - END ### GEMINI");
			}
		} catch (Exception e) {
			Log.e("Error: "+e.getMessage());
		}
	    return code;
	}

	@Override
	public String getName() {
		return this.name;
	}

	@Override
	public String getTechnology() {
		return this.model.toUpperCase();
	}

	@Override
	public boolean isLLM() {
		// TODO Auto-generated method stub
		return true;
	}
	
	public static String createPromptJson(String model, String role, String content, float temperature, boolean stream) {
	    Map<String, Object> jsonMap = new LinkedHashMap<>();
	    jsonMap.put("model", model);

	    List<Map<String, String>> messages = new ArrayList<>();
	    Map<String, String> message = new HashMap<>();
	    message.put("role", role);
	    message.put("content", content);
	    messages.add(message);

	    jsonMap.put("messages", messages);
	    jsonMap.put("temperature", temperature); 
	    jsonMap.put("stream", stream); 

	    Gson gson = new Gson();
	    return gson.toJson(jsonMap);
	}
}
