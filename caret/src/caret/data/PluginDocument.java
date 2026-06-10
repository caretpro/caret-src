package caret.data;

public class PluginDocument {

    private String id;
    private String version;
    private String name;
    private String provider;
    private String description;
    private String pluginUrl;
    private String jarUrl;
    private String versionUrl;
    private int installCount;
    private int rate;

	public PluginDocument() {
    }

    public PluginDocument(String id, String version, String name, String provider,
                          String description, String jarUrl, int installCount, int uninstallCount) {
        this.id = id;
        this.version = version;
        this.name = name;
        this.provider = provider;
        this.description = description;
        this.jarUrl = jarUrl;
        this.installCount = installCount;
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

    public String getProvider() {
        return provider;
    }

    public void setProvider(String provider) {
        this.provider = provider;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
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

    public int getInstallCount() {
        return installCount;
    }

    public void setInstallCount(int installCount) {
        this.installCount = installCount;
    }

    public int getRate() {
		return rate;
	}

	public void setRate(int rate) {
		this.rate = rate;
	}
}