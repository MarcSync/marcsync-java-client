package MarcSync;

import MarcSync.classes.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.google.gson.*;

import java.io.*;
import java.net.*;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

public class Collection {

    private final String _accessToken;
    private final String _collectionName;
    private final ObjectMapper _mapper;

    /**
     * Creates a new instance of the MarcSync collection
     * @param accessToken The access token to use for communication with MarcSync
     * @param collectionName The name of the collection to use
     *
     * @see Client
     **/
    public Collection(String accessToken, String collectionName) {
        _accessToken = accessToken;
        _collectionName = collectionName;

        _mapper = new ObjectMapper();
        _mapper.enable(SerializationFeature.INDENT_OUTPUT);
        _mapper.disable(SerializationFeature.FAIL_ON_EMPTY_BEANS);
    }

    /**
     *
     * Warning:
     * @throws URISyntaxException
     * @throws IOException
     * <p>
     *     Note: This method will delete the collection and all of its entries. This action cannot be undone.
     * </p>
     *
     */
    public void drop() throws URISyntaxException, IOException {
        URL url = new URI("https://api.marcsync.dev/v0/collection/" + _collectionName).toURL();
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("DELETE");
        connection.setRequestProperty("accept", "application/json");
        connection.setRequestProperty("authorization", _accessToken);

        if (connection.getResponseCode() != 200) {
            throw new IOException("Failed to drop collection: " + connection.getResponseCode() + " " + connection.getResponseMessage());
        }
    }

    /**
     * @return The name of the collection
     */
    public String setName(String name) throws URISyntaxException, IOException {
        URL url = new URI("https://api.marcsync.dev/v0/collection/" + _collectionName).toURL();
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("PUT");
        connection.setRequestProperty("accept", "application/json");
        connection.setRequestProperty("authorization", _accessToken);
        connection.setRequestProperty("content-type", "application/json");
        connection.setDoOutput(true);

        try (OutputStream outputStream = connection.getOutputStream()) {
            outputStream.write(new Gson().toJson(new CollectionUpdatePayload(name)).getBytes(StandardCharsets.UTF_8));
        }

        if (connection.getResponseCode() != 200) {
            throw new IOException("Failed to rename collection: " + connection.getResponseCode() + " " + connection.getResponseMessage());
        }

        return name;
    }

    /**
     * @return The name of the collection
     */
    public String getName() {
        return _collectionName;
    }

    /**
     *
     * @return Whether or not the collection exists
     *
     * <p>
     *     Note: This method is useful if you want to fetch the collection from the server to check if it exists before using it.
     * </p>
     *
     */
    public boolean exists() {
        try {
            URL url = new URI("https://api.marcsync.dev/v0/collection/" + _collectionName).toURL();
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            connection.setRequestProperty("accept", "application/json");
            connection.setRequestProperty("authorization", _accessToken);

            return connection.getResponseCode() == 200;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Creates an entry in the collection
     *
     * @return A new instance of the MarcSync entry
     *
     */
    public Entry createEntry(EntryData entryData) throws URISyntaxException, IOException {
        URL url = new URI("https://api.marcsync.dev/v0/entries/" + _collectionName).toURL();
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("POST");
        connection.setRequestProperty("accept", "application/json");
        connection.setRequestProperty("authorization", _accessToken);
        connection.setRequestProperty("content-type", "application/json");
        connection.setDoOutput(true);

        try (OutputStream outputStream = connection.getOutputStream()) {
            outputStream.write(_mapper.writeValueAsString(new EntryDataPayload(entryData)).getBytes(StandardCharsets.UTF_8));
        }

        if (connection.getResponseCode() != 200) {
            throw new IOException("Failed to create entry: " + connection.getResponseCode() + " " + connection.getResponseMessage());
        }

        return new Entry(_accessToken, _collectionName, entryData);
    }


    /**
     *
     * @return The entry with the specified ID
     *
     */
    public Entry getEntryById(String id) throws URISyntaxException, IOException, InterruptedException {
        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder()
                .method("GET", HttpRequest.BodyPublishers.ofString(_mapper.writeValueAsString(new EntryFilterPayload(new EntryData() {{
                    put("_id", id);
                }}))))
                .uri(new URI("https://api.marcsync.dev/v1/entries/" + _collectionName))
                .header("accept", "application/json")
                .header("authorization", _accessToken)
                .header("content-type", "application/json")
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() != 200) {
            throw new IOException("Failed to get entry: " + response.statusCode() + " " + response.body());
        }

        return new Entry(_accessToken, _collectionName, new Gson().fromJson(response.body(), EntryResponse.class).entries[0]);
    }

    /**
     *
     * @return The entries with the specified filter
     *
     * <p>
     *     Note: This method is useful if you want to fetch multiple entries from the server at once.
     * </p>
     *
     * @see Entry
     * @see EntryData
     *
     */
    public Entry[] getEntries(EntryData filters) throws IOException, InterruptedException, URISyntaxException {
        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder()
                .method("GET", HttpRequest.BodyPublishers.ofString(_mapper.writeValueAsString(new EntryFilterPayload(filters))))
                .uri(new URI("https://api.marcsync.dev/v1/entries/" + _collectionName))
                .header("accept", "application/json")
                .header("authorization", _accessToken)
                .header("content-type", "application/json")
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() != 200) {
            throw new IOException("Failed to get entries: " + response.statusCode() + " " + response.body());
        }

        return Arrays.stream(new Gson().fromJson(response.body(), EntryResponse.class).entries).map(entryData -> new Entry(_accessToken, _collectionName, entryData)).toArray(Entry[]::new);
    }

    /**
     *
     * Deletes the entry with the specified ID
     * <p>
     *     Note: Will delete the entry from the collection. This action cannot be undone.
     * </p>
     *
     */
    public void deleteEntryById(String id) throws URISyntaxException, IOException {
        URL url = new URI("https://api.marcsync.dev/v1/entries/" + _collectionName).toURL();
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("DELETE");
        connection.setRequestProperty("accept", "application/json");
        connection.setRequestProperty("authorization", _accessToken);
        connection.setRequestProperty("content-type", "application/json");
        connection.setDoOutput(true);

        try (OutputStream outputStream = connection.getOutputStream()) {
            outputStream.write(new Gson().toJson(new EntryFilterPayload(new EntryData() {{
                put("_id", id);
            }})).getBytes(StandardCharsets.UTF_8));
        }

        if (connection.getResponseCode() != 200) {
            throw new IOException("Failed to delete entry: " + connection.getResponseCode() + " " + connection.getResponseMessage());
        }
    }

    /**
     *
     * Deletes the entries matching with the specified filter
     * <p>
     *     Note: Will delete the matching entries from the collection. This action cannot be undone.
     * </p>
     *
     * @see EntryData
     */
    public void deleteEntries(EntryData filters) throws URISyntaxException, IOException {
        URL url = new URI("https://api.marcsync.dev/v1/entries/" + _collectionName).toURL();
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("DELETE");
        connection.setRequestProperty("accept", "application/json");
        connection.setRequestProperty("authorization", _accessToken);
        connection.setRequestProperty("content-type", "application/json");
        connection.setDoOutput(true);

        try (OutputStream outputStream = connection.getOutputStream()) {
            outputStream.write(_mapper.writeValueAsString(new EntryFilterPayload(filters)).getBytes(StandardCharsets.UTF_8));
        }

        if (connection.getResponseCode() != 200) {
            throw new IOException("Failed to delete entries: " + connection.getResponseCode() + " " + connection.getResponseMessage());
        }
    }

    /**
     *
     * Updates the entry with the specified ID
     *
     * @see Entry
     * @see EntryData
     */
    public void updateEntryById(String id, EntryData entryData) throws URISyntaxException, IOException {
        URL url = new URI("https://api.marcsync.dev/v1/entries/" + _collectionName).toURL();
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("PUT");
        connection.setRequestProperty("accept", "application/json");
        connection.setRequestProperty("authorization", _accessToken);
        connection.setRequestProperty("content-type", "application/json");
        connection.setDoOutput(true);

        try (OutputStream outputStream = connection.getOutputStream()) {
            outputStream.write(_mapper.writeValueAsString(new EntryUpdatePayload(new EntryData() {{
                put("_id", id);
            }}, entryData)).getBytes(StandardCharsets.UTF_8));
        }

        if (connection.getResponseCode() != 200) {
            throw new IOException("Failed to update entry: " + connection.getResponseCode() + " " + connection.getResponseMessage());
        }
    }

    /**
     *
     * Updates the entries matching with the specified filter
     *
     * @see Entry
     * @see EntryData
     */
    public void updateEntries(EntryData filters, EntryData entryData) throws URISyntaxException, IOException {
        URL url = new URI("https://api.marcsync.dev/v1/entries/" + _collectionName).toURL();
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("PUT");
        connection.setRequestProperty("accept", "application/json");
        connection.setRequestProperty("authorization", _accessToken);
        connection.setRequestProperty("content-type", "application/json");
        connection.setDoOutput(true);

        try (OutputStream outputStream = connection.getOutputStream()) {
            outputStream.write(_mapper.writeValueAsString(new EntryUpdatePayload(filters, entryData)).getBytes(StandardCharsets.UTF_8));
        }

        if (connection.getResponseCode() != 200) {
            throw new IOException("Failed to update entries: " + connection.getResponseCode() + " " + connection.getResponseMessage());
        }
    }

    private static String readResponse(InputStream inputStream) throws IOException {
        StringBuilder response = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream))) {
            String line;
            while ((line = reader.readLine()) != null) {
                response.append(line);
            }
        }
        return response.toString();
    }
}
