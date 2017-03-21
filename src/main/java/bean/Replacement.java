package main.java.bean;

/**
 * Created by zzyo on 2017/3/17.
 */
public class Replacement {

    private String replacement;
    private String origin;
    private int indexOfReplace;

    public Replacement() {
    }

    public Replacement(String replacement, String origin, int indexOfReplace) {
        this.replacement = replacement;
        this.origin = origin;
        this.indexOfReplace = indexOfReplace;
    }

    public String getReplacement() {
        return replacement;
    }

    public void setReplacement(String replacement) {
        this.replacement = replacement;
    }

    public String getOrigin() {
        return origin;
    }

    public void setOrigin(String origin) {
        this.origin = origin;
    }

    public int getIndexOfReplace() {
        return indexOfReplace;
    }

    public void setIndexOfReplace(int indexOfReplace) {
        this.indexOfReplace = indexOfReplace;
    }

    @Override
    public String toString() {
        return "Replacement{" +
                "replacement='" + replacement + '\'' +
                ", origin='" + origin + '\'' +
                ", indexOfReplace=" + indexOfReplace +
                "}\n";
    }
}
