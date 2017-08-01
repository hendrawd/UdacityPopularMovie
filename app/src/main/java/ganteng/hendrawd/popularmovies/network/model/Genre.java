package ganteng.hendrawd.popularmovies.network.model;

import com.google.gson.annotations.SerializedName;

/**
 * @author hendrawd on 11/21/16
 */

public class Genre {
    @SerializedName("id")
    public int id;

    @SerializedName("name")
    public String name;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
