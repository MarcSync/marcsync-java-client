package MarcSync;

import MarcSync.classes.EntryData;
import MarcSync.classes.EntryFilterPayload;
import MarcSync.classes.EntryUpdatePayload;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

import java.io.IOException;
import java.io.OutputStream;
import java.net.*;
import java.nio.charset.StandardCharsets;

public class Entry {

    private final String _accessToken;
    private final String _collectionName;
    private final EntryData _entryData;
    private final ObjectMapper _mapper;

    public Entry(String accessToken, String collectionName, EntryData entryData) {
        _accessToken = accessToken;
        _collectionName = collectionName;
        _entryData = entryData;

        _mapper = new ObjectMapper();
        _mapper.enable(SerializationFeature.INDENT_OUTPUT);
        _mapper.disable(SerializationFeature.FAIL_ON_EMPTY_BEANS);
    }

    /**
     *
     * @return The EntryData object of the entry
     *
     * <p>
     * Note: This method is useful if you want to get the values of the entry.
     * </p>
     *
     * @see EntryData
     *
     */
    public EntryData getValues() {
        return _entryData;
    }

    /**
     *
     * @param key The key of the value to get
     * @return The value of the specified key
     *
     * <p>
     * Note: This method is useful if you want to get the value of a specific key without specifying the type.
     * </p>
     *
     * @see EntryData
     *
     */
    public Object getValue(String key) {
        return _entryData.get(key);
    }

    /**
     *
     * @return The name of the collection of the entry
     *
     */
    public String getCollectionName() {
        return _collectionName;
    }

    /**
     *
     * @param key he key of the value to update
     * @param value The value to update
     * @return The values of the entry after update
     *
     *
     * <p>
     * Note: This method is useful if you want to update the value of a specific key.
     * </p>
     *
     */
    public EntryData updateValue(String key, Object value) throws URISyntaxException, IOException {
        URL url = new URI("https://api.marcsync.dev/v1/entries/" + _collectionName).toURL();
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("PUT");
        connection.setRequestProperty("accept", "application/json");
        connection.setRequestProperty("authorization", _accessToken);
        connection.setRequestProperty("content-type", "application/json");
        connection.setDoOutput(true);

        try (OutputStream outputStream = connection.getOutputStream()) {
            outputStream.write(_mapper.writeValueAsString(new EntryUpdatePayload(new EntryData() {{
                put("_id", _entryData.get("_id"));
            }}, new EntryData() {{
                put(key, value);
            }})).getBytes(StandardCharsets.UTF_8));
        }

        if (connection.getResponseCode() != 200) {
            throw new IOException("Failed to update entry: " + connection.getResponseCode() + " " + connection.getResponseMessage());
        }

        return _entryData;
    }

    /**
     *
     * @param entryData The values to update
     * @return The values of the entry after update
     *
     * <p>
     *     Note: This method is useful if you want to update multiple values of the entry.
     * </p>
     *
     *
     * @see EntryData
     *
     */
    public EntryData updateValues(EntryData entryData) throws URISyntaxException, IOException {
        URL url = new URI("https://api.marcsync.dev/v1/entries/" + _collectionName).toURL();
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("PUT");
        connection.setRequestProperty("accept", "application/json");
        connection.setRequestProperty("authorization", _accessToken);
        connection.setRequestProperty("content-type", "application/json");
        connection.setDoOutput(true);

        try (OutputStream outputStream = connection.getOutputStream()) {
            outputStream.write(_mapper.writeValueAsString(new EntryUpdatePayload(new EntryData() {{
                put("_id", _entryData.get("_id"));
            }}, entryData)).getBytes(StandardCharsets.UTF_8));
        }

        if (connection.getResponseCode() != 200) {
            throw new IOException("Failed to update entry: " + connection.getResponseCode() + " " + connection.getResponseMessage());
        }

        return _entryData;
    }

    /**
     *
     * Deletes the entry
     * <p>
     *     Note: Will delete the entry from the collection. This action cannot be undone.
     * </p>
     *
     */
    public void delete() throws IOException, URISyntaxException {
        URL url = new URI("https://api.marcsync.dev/v1/entries/" + _collectionName).toURL();
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("DELETE");
        connection.setRequestProperty("accept", "application/json");
        connection.setRequestProperty("authorization", _accessToken);
        connection.setRequestProperty("content-type", "application/json");
        connection.setDoOutput(true);

        try (OutputStream outputStream = connection.getOutputStream()) {
            outputStream.write(_mapper.writeValueAsString(new EntryFilterPayload(new EntryData() {{
                put("_id", _entryData.get("_id"));
            }})).getBytes(StandardCharsets.UTF_8));
        }

        if (connection.getResponseCode() != 200) {
            throw new IOException("Failed to delete entry: " + connection.getResponseCode() + " " + connection.getResponseMessage());
        }
    }
}