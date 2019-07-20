import de.umass.lastfm.Artist;
import de.umass.lastfm.PaginatedResult;
import de.umass.lastfm.Session;
import de.umass.lastfm.Track;
import de.umass.lastfm.User;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import jdk.internal.util.EnvUtils;
import org.omg.CORBA.Environment;

public class LastFm {

    private static final String LFM_API_KEY = System.getenv("LFM_API_KEY");
    private static final String LFM_API_SECRET = System.getenv("LFM_API_SECRET");

    public static void getSongs(String user, int startPage, int endPage, Callback callback) {
        for (int i = startPage; i <= endPage; i++) {
            callback.call(getSongs(user, i));
        }
    }
    public static void getSongs(String user, int startPage, Callback callback) {
        for (int i = startPage; ; i++) {
            callback.call(getSongs(user, i));
        }
    }

    public static List<String> getSongs(String user, int startPage, int endPage) {
        List<String> songs = new ArrayList<>();
        for (int i = startPage; i <= endPage; i++) {
            List<String> tmp = getSongs(user, i).stream()
                    .filter(song -> !songs.contains(song))
                    .collect(Collectors.toList());
            songs.addAll(tmp);
        }
        return songs;
    }

    public static List<String> getSongs(String user, int page) {
        ArrayList<String> songs = new ArrayList<>();
        PaginatedResult<Track> tracks
                = User.getRecentTracks(user, page, 1000, LFM_API_KEY);
        System.out.println("Page " + page + " of " + tracks.getTotalPages());
        Collection<Track> pageResults = tracks.getPageResults();

        pageResults
                .stream()
                .map(res -> res.getArtist() + " - " + res.getName())
                .filter(song -> !IgnoredSongs.isIgnored(song))
                .filter(song -> !songs.contains(song))
                .forEach(songs::add);
        return songs;
    }

    interface Callback {

        void call(List<String> songs);

    }

}
