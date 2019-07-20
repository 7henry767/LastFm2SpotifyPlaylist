import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author rom
 */
public class Settings {

    private static HashMap<String,Object> settings = null;
    private static final Object LOCK_SETTINGS = new Object();

    private static final String valueSeparatorRegex  = "\\|";
    private static final String valueSeparatorEscape = "&pipe;";
    private static final String valueSeparator       = "|";

    private static final String breakRegex = System.getProperty("line.separator");
    private static final String breakEscape = "&break;";

    private static String workingDirectory = null;

    // all values are seconds
    private static int lastSave; // time when the last save was
    private static int lastChange; // time when the settings were changed latrest
    private static final int AUTO_SAVE_WAIT_TIME = 5; // after this time without a change the settings will be saved
    private static final int FORCE_SAVE_WAIT_TIME = 30; // after this time settings will be saved if there were any changes


    // IMPORTANT: this needs to be called before starting!
    public static void init(HashMap<String,Object> map) {
        lastSave = getCurrentTimeInSeconds();
        lastChange = lastSave;
        String OS = System.getProperty("os.name").toUpperCase();
        if ( OS.contains("WIN") ) {
            workingDirectory = System.getenv("AppData") + File.separator + ".LastFm2SpotifyPlaylist/";
        } else {
            workingDirectory = System.getProperty("user.home") + File.separator + ".LastFm2SpotifyPlaylist/";
        }
        synchronized (LOCK_SETTINGS) {
            settings = new HashMap<>();
            settings.putAll(map);
        }
        load();
    }

    public static void init() {
        init(new HashMap());
    }

    private static int getCurrentTimeInSeconds() {
        return (int) (System.currentTimeMillis()/1000);
    }

    private static void refreshLastChange() {
        lastChange = getCurrentTimeInSeconds();
    }

    private static void refreshLastSave() {
        lastSave = getCurrentTimeInSeconds();
    }

    private static String escape(String s) {
        return s.replaceAll(valueSeparatorRegex, valueSeparatorEscape)
                .replaceAll(breakRegex, breakEscape);
    }
    private static String unescape(String s) {
        return s.replaceAll(valueSeparatorEscape, valueSeparatorRegex)
                .replaceAll(breakEscape, breakRegex);
    }

    public static void set(String key, String val){
        if (!exists(key)) {
            synchronized (LOCK_SETTINGS) {
                settings.put(key.toUpperCase(), val);
            }
        } else {
            synchronized (LOCK_SETTINGS) {
                settings.replace(key.toUpperCase(), val);
            }
        }
        refreshLastChange();
    }

    public static void set(String key, int val){
        set(key, String.valueOf(val));
    }

    public static void set(String key, double val){
        set(key, String.valueOf(val));
    }

    public static void set(String key, float val){
        set(key, String.valueOf(val));
    }

    public static void set(String key, boolean val){
        set(key, String.valueOf(val));
    }

    public static String get(String key){
        synchronized (LOCK_SETTINGS) {
            return exists(key) ? settings.get(key.toUpperCase()).toString() : "";
        }
    }

    public static boolean exists(String key){
        synchronized (LOCK_SETTINGS) {
            return settings.containsKey(key.toUpperCase());
        }
    }

    public static int getInt(String key){
        String value = get(key);
        return "".equals(value) ? 0 : Integer.parseInt(value);
    }

    public static boolean getBool(String key){
        return "true".equals(get(key));
    }

    public static double getDouble(String key){
        String value = get(key);
        return "".equals(value) ? 0.0 : Double.parseDouble(value);
    }


    /* ##################################################
     *                 LIST Functions
     * ################################################## */
    public static void setList(String key, ArrayList<String> list) {
        if (exists(key)) {
            synchronized (LOCK_SETTINGS) {
                settings.replace(key.toUpperCase(), list);
            }
        } else {
            synchronized (LOCK_SETTINGS) {
                settings.put(key.toUpperCase(), list);
            }
        }
        refreshLastChange();
    }

    public static void newList(String key) {
        setList(key, new ArrayList<>());
    }

    public static ArrayList<String> getList(String key) {
        synchronized (LOCK_SETTINGS) {
            return (ArrayList<String>) settings.get(key.toUpperCase());
        }
    }

    public static void addToList(String key, String val) {
        ArrayList<String> list = getList(key);
        if ( !list.contains(val) ) {
            list.add(val);
            setList(key, list);
        }
    }

    public static boolean isList(String key) {
        synchronized (LOCK_SETTINGS) {
            return settings.get(key).getClass().getSimpleName().equals("ArrayList");
        }
    }

    public static void deleteFromList(String key, String val) {
        synchronized (LOCK_SETTINGS) {
            settings.put( key, ((ArrayList<String>) settings.get(key)).remove(val) );
        }
        refreshLastChange();
    }


    /* ##################################################
     *                 Map Functions
     * ################################################## */
    public static void setMap(String key, HashMap<String,String> map) {
        if (exists(key)) {
            synchronized (LOCK_SETTINGS) {
                settings.replace(key.toUpperCase(), map);
            }
        } else {
            synchronized (LOCK_SETTINGS) {
                settings.put(key.toUpperCase(), map);
            }
        }
        refreshLastChange();
    }

    public static boolean isMap(String key) {
        synchronized (LOCK_SETTINGS) {
            return settings.get(key).getClass().getSimpleName().equals("HashMap");
        }
    }

    public static void newMap(String key) {
        setMap(key, new HashMap<>());
    }

    public static HashMap<String,String> getMap(String key) {
        synchronized(LOCK_SETTINGS) {
            return (HashMap<String,String>) settings.get(key.toUpperCase());
        }
    }

    public static void addToMap(String key, String mapKey, String val) {
        HashMap<String,String> map = getMap(key);
        if ( !map.containsKey(mapKey) ) {
            map.put(mapKey, val);
        } else {
            map.replace(mapKey, val);
        }
        setMap(key, map);
    }

    public static void deleteFromMap(String key, String mapKey) {
        HashMap<String,String> map = getMap(key);
        if ( map.containsKey(mapKey) ) {
            map.remove(mapKey);
        }
        setMap(key, map);
    }



    public static void load() {
        // TODO: Load from ZIP
        synchronized (LOCK_SETTINGS) {
            String filePath = workingDirectory + "settings.txt";
            if (new File(filePath).exists()) {
                try {
                    BufferedReader br = new BufferedReader(new FileReader(filePath));
                    String line = br.readLine();
                    while (line != null) {
                        String[] keyVal = line.split(valueSeparatorRegex);
                        if (keyVal.length == 2) {
                            // put LIST
                            if ( "<LIST>".equals(keyVal[1]) ) {
                                line = br.readLine();
                                ArrayList<String> settingsList = new ArrayList<>();
                                while ( !line.equals("</LIST>") && null != line ) {
                                    settingsList.add(unescape(line));
                                    line = br.readLine();
                                }
                                if (null == line) {
                                    break;
                                }
                                settings.put(keyVal[0], settingsList);
                                // put MAP
                            } else if ( "<MAP>".equals(keyVal[1]) )  {
                                line = br.readLine();
                                HashMap<String,String> settingsMap = new HashMap<>();
                                while ( !"</MAP>".equals(line) && null != line ) {
                                    String[] splittedLine = line.split(valueSeparatorRegex);
                                    if ( splittedLine.length == 2 ) {
                                        settingsMap.put( unescape(splittedLine[0]), unescape(splittedLine[1]) );
                                    } else {
                                        System.out.println("ERROR: Settings.java: splittedLine.length != 2 (= " + splittedLine.length + "); line: " + line);
                                    }
                                    line = br.readLine();
                                }
                                if (null == line) {
                                    break;
                                }
                                settings.put(unescape(keyVal[0]), settingsMap);
                            } else { // put string
                                settings.put(unescape(keyVal[0]), unescape(keyVal[1]));
                            }
                        }
                        line = br.readLine();
                    }
                    br.close();
                } catch (Exception e) {
                    Logger.getLogger(Settings.class.getName()).log(Level.SEVERE, null, e);
                }
            }
        }
    }


    public static void save(){
        // TODO: save to ZIP
        synchronized (LOCK_SETTINGS) {
            String fileToSave = workingDirectory + "settings.txt";
            new File(workingDirectory).mkdirs();

            PrintWriter writer;
            try {
                writer = new PrintWriter(fileToSave, "UTF-8");
                settings.keySet().stream().forEach((key) -> {
                    // SAVE LISTS
                    if ( isList(key) ) {
                        StringBuilder sb = new StringBuilder();
                        sb.append(escape(key))
                                .append(valueSeparator)
                                .append("<LIST>");
                        writer.println(sb.toString());
                        ArrayList<String> settingsList = getList(key);
                        settingsList.forEach((el) -> {
                            writer.println(escape(el));
                        });
                        writer.println("</LIST>");
                        // SAVE MAPS
                    } else if ( isMap(key) ) {
                        StringBuilder sb = new StringBuilder();
                        sb.append(escape(key))
                                .append(valueSeparator)
                                .append("<MAP>");
                        writer.println(sb.toString());
                        getMap(key).forEach( (mkey, val) -> {
                            writer.println(
                                    new StringBuilder()
                                            .append(escape(mkey))
                                            .append(valueSeparator)
                                            .append(escape(val))
                                            .toString()
                            );
                        });
                        writer.println("</MAP>");
                        // SAVE VALUES
                    } else {
                        StringBuilder sb = new StringBuilder();
                        sb.append(escape(key))
                                .append(valueSeparator)
                                .append(escape((String) settings.get(key)));
                        writer.println(sb.toString());
                    }
                });
                writer.close();
                refreshLastSave();
            } catch (Exception e) {
                Logger.getLogger(Settings.class.getName()).log(Level.SEVERE, null, e);
            }
        }
    }

    public static void saveIfRequired(boolean force){
        if (
                lastChange > lastSave // if there was a change
                        && (
                        ( getCurrentTimeInSeconds() - AUTO_SAVE_WAIT_TIME > lastChange ) // save after 5 senconds without change and without saving
                                || getCurrentTimeInSeconds() - FORCE_SAVE_WAIT_TIME > lastSave // save after 30 senconds without saving
                                || force // save anyway
                )
        ) {
            save();
        }
    }

    public static void saveIfRequired(){
        saveIfRequired(false);
    }

}