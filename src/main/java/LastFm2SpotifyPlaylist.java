import static sun.net.www.protocol.http.HttpURLConnection.userAgent;

import de.umass.lastfm.Caller;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;

public class LastFm2SpotifyPlaylist {

    private boolean changes = false;

    private LastFm2SpotifyPlaylist() {
        Caller.getInstance().setUserAgent(userAgent);
        Caller.getInstance().setDebugMode(true);

        Settings.init();

        final HashMap<String, String> savedSongs = Settings.getMap("savedSongs");
        final HashMap<String, String> ignoredSongs = Settings.getMap("ignoredSongs");
        System.out.println("OK...");
        LastFm.getSongs("this should be an account name on last.fm i guess", 1, songs -> {
            didSomethingChange(false);

            songs.stream()
                    .filter(song -> !savedSongs.keySet().contains(song))
                    .filter(song -> !ignoredSongs.getOrDefault(song, "song_is_null")
                            .equals("IndexOutOfBoundsException"))
                    .forEach(songName -> {
                        try {
                            String thisSong = Spotify.getSong(songName).getUri();
                            savedSongs.put(songName, thisSong);
                            ignoredSongs.remove(songName);
                            System.out.println(thisSong + " -> " + songName);
                        } catch (IndexOutOfBoundsException e) {
                            ignoredSongs.put(songName, "IndexOutOfBoundsException");
                            System.out.println("Song not found: \"" + songName + "\"");
                        }
                        didSomethingChange(true);
                    });
            if (changes) {
                Settings.save();
            }
        });
    }

    private void didSomethingChange(boolean b) {
        changes = b;
    }

    public static void main(String... args) {
        LastFm2SpotifyPlaylist l = new LastFm2SpotifyPlaylist();
    }

}
