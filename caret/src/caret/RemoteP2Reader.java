package caret;

import java.io.InputStream;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import javax.xml.parsers.DocumentBuilderFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import caret.repository.PluginInstaller;
import caret.data.PluginData;

public class RemoteP2Reader {

    private final HttpClient httpClient = HttpClient.newBuilder()
            .followRedirects(HttpClient.Redirect.ALWAYS)
            .build();

    private final Map<String, PluginData> plugins = new HashMap<>();

    public  Map<String, PluginData> getTasksPlugins(String baseUrl) throws Exception {
        System.out.println("Connecting to p2 server: " + baseUrl);
        plugins.clear();

        List<String> children = getCompositeChildren(baseUrl + "/compositeContent.xml");
        
        for (String childPath : children) {
            String versionUrl = baseUrl + (baseUrl.endsWith("/") ? "" : "/") + childPath;
            parseContentJar(versionUrl);
        }

        return plugins;
    }

    private void parseContentJar(String versionUrl) {
    	String jarUrl = versionUrl + "/content.jar";
        try {
            HttpRequest request = HttpRequest.newBuilder().uri(URI.create(jarUrl)).build();
            HttpResponse<InputStream> response = httpClient.send(request, HttpResponse.BodyHandlers.ofInputStream());

            if (response.statusCode() != 200) return;

            try (ZipInputStream zis = new ZipInputStream(response.body())) {
                ZipEntry entry;
                while ((entry = zis.getNextEntry()) != null) {
                    if (entry.getName().equals("content.xml")) {
                        collectUnits(zis, versionUrl);
                        break; 
                    }
                    zis.closeEntry();
                }
            }
        } catch (Exception e) {
            System.err.println("  Error processing JAR: " + jarUrl);
        }
    }

    private void collectUnits(InputStream xmlStream, String versionUrl) throws Exception {
        Document doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(xmlStream);
        NodeList units = doc.getElementsByTagName("unit");

        for (int i = 0; i < units.getLength(); i++) {
            Element unit = (Element) units.item(i);
            
            if (isRealPlugin(unit)) {
                String id = unit.getAttribute("id");
                String version = unit.getAttribute("version");
                
                String name = getP2Property(unit, "org.eclipse.equinox.p2.name", id);
                String desc = getP2Property(unit, "org.eclipse.equinox.p2.description", "No description provided.");
                String provider = getP2Property(unit, "org.eclipse.equinox.p2.provider", "Unknown Provider");

            	String jarName = id + "_" + version + ".jar";
            	String jarUrl = versionUrl+"/plugins/"+jarName;
                PluginData current = new PluginData(id, version, name, desc, provider, jarUrl);

                if (!plugins.containsKey(id) || isNewer(version, plugins.get(id).getVersion())) {
                    plugins.put(id, current);
                }
            }
        }
    }

    private boolean isNewer(String newV, String oldV) {
        return newV.compareTo(oldV) > 0;
    }

    private void printLatestPlugins() {
        System.out.println("\n--- Final List: Latest Versions of Plugins ---");
        for (PluginData plugin : plugins.values()) {
            System.out.println("  --------------------------------------------------");
            System.out.printf("  > ID:          %s%n", plugin.getId());
            System.out.printf("    VERSION:     %s%n", plugin.getVersion());
            System.out.printf("    NAME:        %s%n", plugin.getName());
            System.out.printf("    PROVIDER:    %s%n", plugin.getName());
            System.out.printf("    DESCRIPTION: %s%n", plugin.getDescription());
            System.out.printf("    JAR_URL: %s%n", plugin.getJarUrl());
            PluginInstaller pluginInstaller = new PluginInstaller();
            pluginInstaller.installPlugin(plugin.getJarUrl(),plugin.getId());
        }
    }

    private boolean isRealPlugin(Element unit) {
        NodeList providedCaps = unit.getElementsByTagName("provided");
        for (int i = 0; i < providedCaps.getLength(); i++) {
            Element cap = (Element) providedCaps.item(i);
            if ("osgi.bundle".equals(cap.getAttribute("namespace"))) {
                return true;
            }
        }
        return false;
    }

    private String getP2Property(Element unit, String propName, String defaultValue) {
        NodeList properties = unit.getElementsByTagName("property");
        for (int j = 0; j < properties.getLength(); j++) {
            Element prop = (Element) properties.item(j);
            if (propName.equals(prop.getAttribute("name"))) {
                String val = prop.getAttribute("value");
                if (val != null && !val.startsWith("%")) return val;
            }
        }
        return defaultValue;
    }

    private List<String> getCompositeChildren(String url) throws Exception {
        List<String> locations = new ArrayList<>();
        HttpRequest request = HttpRequest.newBuilder().uri(URI.create(url)).build();
        HttpResponse<InputStream> response = httpClient.send(request, HttpResponse.BodyHandlers.ofInputStream());
        if (response.statusCode() != 200) return locations;
        Document doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(response.body());
        NodeList nodes = doc.getElementsByTagName("child");
        for (int i = 0; i < nodes.getLength(); i++) {
            locations.add(((Element) nodes.item(i)).getAttribute("location"));
        }
        return locations;
    }
}