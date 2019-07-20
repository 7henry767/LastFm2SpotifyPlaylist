import java.util.HashSet;
import java.util.stream.Collectors;

public class IgnoredSongs {

    private static final HashSet<String> NAMES = new HashSet();
    private static final HashSet<String> STARTS_WITH = new HashSet();

    static {
        name("example band - this is an example song");
    }

    private static void name(String name) {
        NAMES.add(name.toLowerCase());
    }

    private static void starts(String name) {
        STARTS_WITH.add(name.toLowerCase());
    }

    public static boolean isIgnored(String songInput) {
        final String song = songInput.toLowerCase();
        if (NAMES.contains(song)) {
            return true;
        }
        if (STARTS_WITH.stream()
                .filter(start -> song.startsWith(start))
                .collect(Collectors.toList())
                .size() != 0
        ) {
            return true;
        }
        return false;
    }

}
