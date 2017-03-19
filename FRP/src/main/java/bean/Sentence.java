package main.java.bean;

import java.util.ArrayList;

/**
 * Created by zzyo on 2017/3/17.
 */
public class Sentence {

    private String origin;
    private String result;
    private ArrayList<Sentence> itemLists;
    private ArrayList<Replacement> replacements;

    public Sentence() {
    }

    public String getOrigin() {
        return origin;
    }

    public void setOrigin(String origin) {
        this.origin = origin;
    }

    public String getResult() {
        return result;
    }

    public void setResult(String result) {
        this.result = result;
    }

    public ArrayList<Sentence> getItemLists() {
        return itemLists;
    }

    public void addItemList(Sentence sentence) {
        this.itemLists.add(sentence);
    }

    public ArrayList<Replacement> getReplacements() {
        return replacements;
    }

    public void addReplacements(Replacement replacement) {
        this.replacements.add(replacement);
    }
}
