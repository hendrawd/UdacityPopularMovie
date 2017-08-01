package ganteng.hendrawd.popularmovies.network.model;

import android.util.SparseArray;

/**
 * @author hendrawd on 6/29/16
 */

public class GenreMapper {
    private static SparseArray<String> sGenreMapper = new SparseArray<>();

    public static void put(int key, String genre) {
        sGenreMapper.put(key, genre);
    }

    public static String get(int key) {
        return sGenreMapper.get(key);
    }

    public static boolean isInitialized() {
        return sGenreMapper.size() > 0;
    }
}
