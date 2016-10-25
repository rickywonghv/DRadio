package xyz.damonwong.dradio.dradio;

/**
 * Created by damon on 9/13/16.
 */
public class Programme {
    private String title;
    private String desc;
    private String url;
    private String date;

    public Programme(String title, String desc, String url, String date) {
        this.title = title;
        this.desc = desc;
        this.url = url;
        this.date = date;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDesc() {
        return desc;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }
}
