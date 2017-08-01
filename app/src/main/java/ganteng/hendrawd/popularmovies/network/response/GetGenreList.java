package ganteng.hendrawd.popularmovies.network.response;

import com.google.gson.annotations.SerializedName;

import java.util.List;

import ganteng.hendrawd.popularmovies.network.model.Genre;

/**
 * @author hendrawd on 11/21/16
 */

public class GetGenreList {

    @SerializedName("genres")
    public List<Genre> genres;
}
