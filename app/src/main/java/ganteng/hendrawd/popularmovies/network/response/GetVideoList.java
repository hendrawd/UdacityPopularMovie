package ganteng.hendrawd.popularmovies.network.response;

import com.google.gson.annotations.SerializedName;

import java.util.List;

import ganteng.hendrawd.popularmovies.network.model.Video;

/**
 * @author hendrawd on 11/17/16
 */

public class GetVideoList {
    @SerializedName("id")
    private int id;

    @SerializedName("results")
    private List<Video> results;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public List<Video> getResults() {
        return results;
    }

    public void setResults(List<Video> results) {
        this.results = results;
    }
}
