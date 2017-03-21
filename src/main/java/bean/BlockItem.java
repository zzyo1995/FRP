package main.java.bean;

import java.util.ArrayList;

/**
 * Created by zzyo on 2017/3/17.
 */
public class BlockItem {

    private ArrayList<Sentence> sentences = new ArrayList<>();

    public BlockItem() {
    }

    public ArrayList<Sentence> getSentences() {
        return sentences;
    }

    public void addSentence(Sentence sentence) {
        this.sentences.add(sentence);
    }
}
