package caret;

import java.io.*;
import java.net.ConnectException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.HttpTimeoutException;
import java.nio.file.*;
import java.time.Duration;
import java.util.UUID;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.widgets.Display;

public class ZIPUploader {

	public static Path zipFolder(Path sourceFolder) throws IOException {
	    if (!Files.isDirectory(sourceFolder)) {
	        throw new IllegalArgumentException("Source path is not a directory");
	    }

	    Path zipPath = sourceFolder.resolveSibling(
	            sourceFolder.getFileName().toString() + ".zip"
	    );

	    try (ZipOutputStream zos = new ZipOutputStream(Files.newOutputStream(zipPath))) {
	        Files.walk(sourceFolder)
	                .filter(path -> !Files.isDirectory(path))
	                .forEach(path -> {
	                    String entryName = sourceFolder.relativize(path).toString();

	                    entryName = entryName.replace("\\", "/");

	                    ZipEntry zipEntry = new ZipEntry(entryName);

	                    try {
	                        zos.putNextEntry(zipEntry);
	                        Files.copy(path, zos);
	                        zos.closeEntry();
	                    } catch (IOException e) {
	                        throw new UncheckedIOException(e);
	                    }
	                });
	    }
	    return zipPath;
	}
	
    public static void uploadZipFile(
            String url,
            String bearerToken,
            Path zipFilePath,
            String extensionId,    
            String extensionVersion  
    ) throws IOException, InterruptedException {

        String boundary = UUID.randomUUID().toString();
        byte[] fileBytes = Files.readAllBytes(zipFilePath);

        String header =
                "--" + boundary + "\r\n" +
                "Content-Disposition: form-data; name=\"file\"; filename=\"" + zipFilePath.getFileName().toString() + "\"\r\n" +
                "Content-Type: application/zip\r\n\r\n";

        String footer = "\r\n--" + boundary + "--\r\n";

        byte[] requestBody = concatenate(
                header.getBytes(java.nio.charset.StandardCharsets.UTF_8),
                fileBytes,
                footer.getBytes(java.nio.charset.StandardCharsets.UTF_8)
        );

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .timeout(Duration.ofSeconds(60))
                .header("Authorization", "Bearer " + bearerToken)
                // Send metadata as custom headers
                .header("X-Extension-ID", extensionId)
                .header("X-Extension-Version", extensionVersion)
                .header("Content-Type", "multipart/form-data; boundary=" + boundary)
                .POST(HttpRequest.BodyPublishers.ofByteArray(requestBody))
                .build();

        HttpClient client = HttpClient.newBuilder()
                .version(HttpClient.Version.HTTP_1_1) // Force HTTP 1.1 for better compatibility with local servers
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        
        if (response.statusCode() >= 200 && response.statusCode() < 300) {
            System.out.println("Upload successful for: " + extensionId);
            MessageDialog.openInformation(Display.getCurrent().getActiveShell(), "Uploading", "Uploading " + extensionId+ " to repository");
        } else {
            System.err.println("Error " + response.statusCode() + ": " + response.body());
        }
    }

    // ---------------- UTILS ----------------

    private static byte[] concatenate(byte[]... arrays) {
        int totalLength = 0;
        for (byte[] arr : arrays) {
            totalLength += arr.length;
        }

        byte[] result = new byte[totalLength];
        int offset = 0;

        for (byte[] arr : arrays) {
            System.arraycopy(arr, 0, result, offset, arr.length);
            offset += arr.length;
        }

        return result;
    }
    
    /*public static void main(String[] args) {

    	String host = "127.0.0.1";
        int port = 7500;
        String bearerToken = "1f2a4f0e3b6c9d4b8c8a6a2f8f0b3c9e";
        Path folderPath = Path.of("files/folder");

        try {
            Path zipPath = zipFolder(folderPath);
            //uploadZipFile(host, port, bearerToken, zipPath);
        } catch (Exception e) {
            System.err.println("Process failed: " + e.getMessage());
        }
    }*/

}
