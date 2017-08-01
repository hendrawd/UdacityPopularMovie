package ganteng.hendrawd.popularmovies.network.model;

import android.database.Cursor;
import android.os.Parcel;
import android.os.Parcelable;

import com.google.gson.Gson;
import com.google.gson.annotations.SerializedName;
import com.google.gson.reflect.TypeToken;

import java.util.ArrayList;
import java.util.List;

import ganteng.hendrawd.popularmovies.data.FavoriteMovieContract;

/**
 * @author hendrawd on 6/29/17
 */

public class Movie implements Parcelable {

    private String id;
    @SerializedName("poster_path")
    private String posterPath;
    @SerializedName("original_title")
    private String originalTitle;
    private String title;
    private String overview;
    @SerializedName("release_date")
    private String releaseDate;
    @SerializedName("backdrop_path")
    private String backdropPath;
    @SerializedName("vote_average")
    private Float voteAverage;
    @SerializedName("genre_ids")
    private List<Integer> genreIds;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getPosterPath() {
        return posterPath;
    }

    public void setPosterPath(String posterPath) {
        this.posterPath = posterPath;
    }

    public String getOriginalTitle() {
        return originalTitle;
    }

    public void setOriginalTitle(String originalTitle) {
        this.originalTitle = originalTitle;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getOverview() {
        return overview;
    }

    public void setOverview(String overview) {
        this.overview = overview;
    }

    public String getReleaseDate() {
        return releaseDate;
    }

    public void setReleaseDate(String releaseDate) {
        this.releaseDate = releaseDate;
    }

    public String getBackdropPath() {
        return backdropPath;
    }

    public void setBackdropPath(String backdropPath) {
        this.backdropPath = backdropPath;
    }

    public Float getVoteAverage() {
        return voteAverage;
    }

    public void setVoteAverage(Float voteAverage) {
        this.voteAverage = voteAverage;
    }

    public List<Integer> getGenreIds() {
        return genreIds;
    }

    public void setGenreIds(List<Integer> genreIds) {
        this.genreIds = genreIds;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.id);
        dest.writeString(this.posterPath);
        dest.writeString(this.originalTitle);
        dest.writeString(this.title);
        dest.writeString(this.overview);
        dest.writeString(this.releaseDate);
        dest.writeString(this.backdropPath);
        dest.writeValue(this.voteAverage);
        dest.writeList(this.genreIds);
    }

    public Movie() {
    }

    protected Movie(Parcel in) {
        this.id = in.readString();
        this.posterPath = in.readString();
        this.originalTitle = in.readString();
        this.title = in.readString();
        this.overview = in.readString();
        this.releaseDate = in.readString();
        this.backdropPath = in.readString();
        this.voteAverage = (Float) in.readValue(Float.class.getClassLoader());
        this.genreIds = new ArrayList<>();
        in.readList(this.genreIds, Integer.class.getClassLoader());
    }

    public static final Parcelable.Creator<Movie> CREATOR = new Parcelable.Creator<Movie>() {
        @Override
        public Movie createFromParcel(Parcel source) {
            return new Movie(source);
        }

        @Override
        public Movie[] newArray(int size) {
            return new Movie[size];
        }
    };

    public static Movie fromCursor(Cursor query) {
        Movie movie = new Movie();
        movie.setId(query.getString(query.getColumnIndex(FavoriteMovieContract.FavoriteMovieEntry.COLUMN_MOVIE_ID)));
        movie.setPosterPath(query.getString(query.getColumnIndex(FavoriteMovieContract.FavoriteMovieEntry.COLUMN_POSTER_PATH)));
        movie.setOriginalTitle(query.getString(query.getColumnIndex(FavoriteMovieContract.FavoriteMovieEntry.COLUMN_ORIGINAL_TITLE)));
        movie.setTitle(query.getString(query.getColumnIndex(FavoriteMovieContract.FavoriteMovieEntry.COLUMN_TITLE)));
        movie.setOverview(query.getString(query.getColumnIndex(FavoriteMovieContract.FavoriteMovieEntry.COLUMN_OVERVIEW)));
        movie.setReleaseDate(query.getString(query.getColumnIndex(FavoriteMovieContract.FavoriteMovieEntry.COLUMN_RELEASE_DATE)));
        movie.setBackdropPath(query.getString(query.getColumnIndex(FavoriteMovieContract.FavoriteMovieEntry.COLUMN_BACKDROP_PATH)));
        movie.setVoteAverage(query.getFloat(query.getColumnIndex(FavoriteMovieContract.FavoriteMovieEntry.COLUMN_VOTE_AVERAGE)));
        Gson gson = new Gson();
        String genreIds = query.getString(query.getColumnIndex(FavoriteMovieContract.FavoriteMovieEntry.COLUMN_GENRE_IDS));
        List<Integer> genreIdList = gson.fromJson(genreIds, new TypeToken<List<Integer>>() {
        }.getType());
        movie.setGenreIds(genreIdList);
        return movie;
    }

    @Override
    public String toString() {
        return "Movie{" +
                "id='" + id + '\'' +
                ", posterPath='" + posterPath + '\'' +
                ", originalTitle='" + originalTitle + '\'' +
                ", title='" + title + '\'' +
                ", overview='" + overview + '\'' +
                ", releaseDate='" + releaseDate + '\'' +
                ", backdropPath='" + backdropPath + '\'' +
                ", voteAverage=" + voteAverage +
                ", genreIds=" + genreIds +
                '}';
    }
}
