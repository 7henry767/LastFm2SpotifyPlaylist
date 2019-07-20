import com.wrapper.spotify.SpotifyApi;
import com.wrapper.spotify.exceptions.SpotifyWebApiException;
import com.wrapper.spotify.model_objects.credentials.ClientCredentials;
import com.wrapper.spotify.model_objects.specification.Track;
import com.wrapper.spotify.requests.authorization.client_credentials.ClientCredentialsRequest;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import jdk.nashorn.internal.runtime.regexp.RegExp;

public class Spotify {

    private static final Pattern CLEAN_NAME_PATTERN = Pattern.compile("[^a-zA-Z0-9]");

    private static final String SPT_CLIENT_ID = System.getenv("SPT_CLIENT_ID");
    private static final String SPT_CLIENT_SECRET = System.getenv("SPT_CLIENT_SECRET");

    private static SpotifyApi spotifyApi = new SpotifyApi.Builder()
            .setClientId(SPT_CLIENT_ID)
            .setClientSecret(SPT_CLIENT_SECRET)
            //.setRedirectUri("<your_redirect_uri>")
            .build();

    private static final ClientCredentialsRequest clientCredentialsRequest
            = spotifyApi.clientCredentials().build();

    static {
        try {
            final ClientCredentials clientCredentials = clientCredentialsRequest.execute();
            spotifyApi.setAccessToken(clientCredentials.getAccessToken());
            //System.out.println("Expires in: " + clientCredentials.getExpiresIn());
        } catch (IOException | SpotifyWebApiException e) {
            System.out.println("Error: " + e.getMessage());
        }
    }

    public static Track getSong(String songName) {
        final String cleanSongName = cleanName(songName);
        try {
            return Arrays.asList(spotifyApi.searchTracks(songName).build().execute().getItems())
                    .stream()
                    .filter(track -> cleanName(track.getArtists()[0].getName() + " - " + track.getName()).equals(cleanSongName))
                    .limit(1)
                    .collect(Collectors.toList())
                    .get(0);
        } catch (IOException | SpotifyWebApiException e) {
            e.printStackTrace();
        }
        return null;
    }

    private static String cleanName(String songName) {
        StringBuilder newName = new StringBuilder();
        char[] chars = songName.toLowerCase().toCharArray();
        for (int i=0; i<songName.length(); i++) {
            if (chars[i] >= 'a' && chars[i] <= 'z') {
                newName.append(chars[i]);
            } else if (chars[i] >= '0' && chars[i] <= '9') {
                newName.append(chars[i]);
            } else if (chars[i] == ' ') {
                newName.append(chars[i]);
            }
            return newName.toString();
        }
        String asd = songName
                .toLowerCase()
                .replaceAll("^[a-z0-9\\p{javaSpaceChar}]", "");
        System.out.println(asd);
        return asd;
    }


}
