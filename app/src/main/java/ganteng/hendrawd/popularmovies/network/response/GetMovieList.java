package ganteng.hendrawd.popularmovies.network.response;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.ArrayList;

import ganteng.hendrawd.popularmovies.network.model.Movie;

/**
 * @author hendrawd on 6/29/16
 */

public class GetMovieList implements Parcelable {

    private int page;
    private ArrayList<Movie> results;
    private int total_results;
    private int total_pages;

    public int getPage() {
        return page;
    }

    public void setPage(int page) {
        this.page = page;
    }

    public ArrayList<Movie> getResults() {
        return results;
    }

    public void setResults(ArrayList<Movie> results) {
        this.results = results;
    }

    public int getTotal_results() {
        return total_results;
    }

    public void setTotal_results(int total_results) {
        this.total_results = total_results;
    }

    public int getTotal_pages() {
        return total_pages;
    }

    public void setTotal_pages(int total_pages) {
        this.total_pages = total_pages;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(this.page);
        dest.writeTypedList(this.results);
        dest.writeInt(this.total_results);
        dest.writeInt(this.total_pages);
    }

    public GetMovieList() {
    }

    protected GetMovieList(Parcel in) {
        this.page = in.readInt();
        this.results = in.createTypedArrayList(Movie.CREATOR);
        this.total_results = in.readInt();
        this.total_pages = in.readInt();
    }

    public static final Creator<GetMovieList> CREATOR = new Creator<GetMovieList>() {
        @Override
        public GetMovieList createFromParcel(Parcel source) {
            return new GetMovieList(source);
        }

        @Override
        public GetMovieList[] newArray(int size) {
            return new GetMovieList[size];
        }
    };
}
