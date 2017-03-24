package main.java.bean;

import java.util.ArrayList;

/**
 * Created by zzyo on 2017/3/17.
 */
public class Sentence {

    private String origin;
    private String result;
    private ArrayList<Sentence> itemLists = new ArrayList<>();
    private ArrayList<Replacement> replacements = new ArrayList<>();

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

    public String toString() {
        String print = this.origin;
        for (Sentence list : this.getItemLists()) {
            print = print.concat(list.getOrigin());
        }
        return print;
    }
}
