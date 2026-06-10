package caret.data;

public class PluginData {
	
    String id;
    String version; 
    String name; 
    String description; 
    String provider;
    String pluginUrl;
	String jarUrl;
    String versionUrl;

	public PluginData(String id, String version, String name, String description, String provider, String jarUrl) {
        this.id = id; 
        this.version = version; 
        this.name = name; 
        this.description = description; 
        this.provider = provider;
        this.jarUrl = jarUrl;
    }

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getVersion() {
		return version;
	}

	public void setVersion(String version) {
		this.version = version;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getProvider() {
		return provider;
	}

	public void setProvider(String provider) {
		this.provider = provider;
	}
	
    public String getPluginUrl() {
		return pluginUrl;
	}

	public void setPluginUrl(String pluginUrl) {
		this.pluginUrl = pluginUrl;
	}

	public String getJarUrl() {
		return jarUrl;
	}

	public void setJarUrl(String jarUrl) {
		this.jarUrl = jarUrl;
	}
    
    public String getVersionUrl() {
		return versionUrl;
	}

	public void setVersionUrl(String versionUrl) {
		this.versionUrl = versionUrl;
	}
    
}