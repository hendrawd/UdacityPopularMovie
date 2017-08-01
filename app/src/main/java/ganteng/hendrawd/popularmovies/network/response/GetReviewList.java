package ganteng.hendrawd.popularmovies.network.response;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.gson.annotations.SerializedName;

import java.util.List;

import ganteng.hendrawd.popularmovies.network.model.Review;

/**
 * @author hendrawd on 11/17/16
 */

public class GetReviewList implements Parcelable {
    @SerializedName("page")
    private int page;

    @SerializedName("results")
    private List<Review> results;

    @SerializedName("total_results")
    private int total_results;

    @SerializedName("total_pages")
    private int total_pages;

    public int getPage() {
        return page;
    }

    public void setPage(int page) {
        this.page = page;
    }

    public List<Review> getResults() {
        return results;
    }

    public void setResults(List<Review> results) {
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

    public GetReviewList() {
    }

    protected GetReviewList(Parcel in) {
        this.page = in.readInt();
        this.results = in.createTypedArrayList(Review.CREATOR);
        this.total_results = in.readInt();
        this.total_pages = in.readInt();
    }

    public static final Creator<GetReviewList> CREATOR = new Creator<GetReviewList>() {
        @Override
        public GetReviewList createFromParcel(Parcel source) {
            return new GetReviewList(source);
        }

        @Override
        public GetReviewList[] newArray(int size) {
            return new GetReviewList[size];
        }
    };
}
