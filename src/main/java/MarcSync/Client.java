package MarcSync;

import java.io.IOException;
import java.net.*;

public class Client {

    private final String _accessToken;

    /**
     * Creates a new MarcSync client.
     * @param accessToken The access token to use for communication with MarcSync
     */
    public Client(String accessToken) {
        _accessToken = accessToken;
    }

    /**
     * @param collectionName The name of the collection to use
     * @return A new instance of the MarcSync collection
     *
     * @see Collection
     */
    public Collection getCollection(String collectionName) {
        return new Collection(_accessToken, collectionName);
    }

    /**
     * @param collectionName The name of the collection to use
     * @return A new instance of the MarcSync collection
     *
     * @throws IOException
     * @throws URISyntaxException
     * @see Collection
     *
     * <p>
     *     Note: This method is useful if you want to fetch the collection from the server to check if it exists before using it.
     * </p>
     */
    public Collection fetchCollection(String collectionName) throws IOException, URISyntaxException {
        URL url = new URI("https://api.marcsync.dev/v0/collection/" + collectionName).toURL();
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestProperty("accept", "application/json");
        connection.setRequestProperty("authorization", _accessToken);

        if (connection.getResponseCode() != 200) {
            throw new IOException("Failed to fetch collection: " + connection.getResponseCode() + " " + connection.getResponseMessage());
        }

        return new Collection(_accessToken, collectionName);
    }

    /**
     *
     * @param collectionName The name of the collection to create
     * @return A new instance of the MarcSync collection
     *
     * @throws IOException
     * @throws URISyntaxException
     */
    public Collection createCollection(String collectionName) throws IOException, URISyntaxException {
        URL url = new URI("https://api.marcsync.dev/v0/collection/" + collectionName).toURL();
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("POST");
        connection.setRequestProperty("accept", "application/json");
        connection.setRequestProperty("authorization", _accessToken);

        if (connection.getResponseCode() != 200) {
            throw new IOException("Failed to create collection: " + connection.getResponseCode() + " " + connection.getResponseMessage());
        }

        return new Collection(_accessToken, collectionName);
    }
}
