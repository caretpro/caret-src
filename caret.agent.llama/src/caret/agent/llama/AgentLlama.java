package caret.agent.llama;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;

import org.eclipse.jface.preference.IPreferenceStore;

import com.google.gson.Gson;

import caret.agent.AgentInterface;
import caret.agent.Response;
import caret.agent.llama.preferences.PreferenceConstants;
import caret.tool.Log;

public class AgentLlama implements AgentInterface {

	private IPreferenceStore store;
	private String ID = "Code Llama";
	private String name = "Code Llama";
	private String technology = "Code Llama";
	private String key="";
	private String url=""
			+ "/";
	private String model="codellama-7b-instruct";
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
		setKey(store.getString(PreferenceConstants.P_LLAMA_KEY));
		model = store.getString(PreferenceConstants.P_LLAMA_MODEL);
		url = store.getString(PreferenceConstants.P_LLAMA_URL);
		if(!(temperature >= 0.0 && temperature <=1.0)) {
			temperature = defaultTemperature;
		}
		Response response = new Response();
		try {
			Gson gson = new Gson();
			
			String jsonString = "{\"message\": \""+message+"\"}";
			Log.d("CODELLAMA INPUT: "+jsonString);
			URL _url = new URI(url).toURL();
			HttpURLConnection connection = (HttpURLConnection) _url.openConnection();
			connection.setConnectTimeout(5000);
			connection.setReadTimeout(40000);
			connection.setRequestProperty("X-API-Key", getKey());
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
	   	 	System.out.println(result.toString());

	   	 	caret.agent.llama.QueryResponse queryResponse = gson.fromJson(responseBody, caret.agent.llama.QueryResponse.class);
	   	    response.setText(queryResponse.getMessage());
	   	    response.setCode(getCode(queryResponse.getMessage()));
	   	    response.setFallbackIntent(false);
			
		} catch (Exception e) {
			Log.e("Error: "+e.getMessage());
			response.setError(true);
			response.setErrorMessage("Connection error");
   	 		return response;
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
			    Log.d("### Code - INIT ### CODELLAMA\n"+code+"\n### Code - END ### CODELLAMA");
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
		return this.technology;
	}

	@Override
	public boolean isLLM() {
		return true;
	}

}
