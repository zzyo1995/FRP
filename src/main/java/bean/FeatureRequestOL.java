package main.java.bean;

import main.java.parse.Parser;

import java.util.ArrayList;

/**
 * Created by zzyo on 2017/3/17.
 */
public class FeatureRequestOL extends FeatureRequest {

    private String systemName;
    private String title;
    private ArrayList<ArrayList<Integer>> blocks = new ArrayList<>();
    ArrayList<Sentence> sentences = new ArrayList<>();


    public FeatureRequestOL(String title) {
        super(title);
    }

    public FeatureRequestOL(String systemName, String title, ArrayList<ArrayList<Integer>> blocks, ArrayList<Sentence> sentences) {
        super(title);
        this.systemName = systemName;
        this.title = title;
        this.blocks = blocks;
        this.sentences = sentences;
        for (Sentence sentence : sentences) {
            this.addSentence(sentence.getResult());
        }
    }

    public String[] getSystemName() {
        return Parser.getTokens(systemName);
    }

    public void setSystemName(String systemName) {
        this.systemName = systemName;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public ArrayList<ArrayList<Integer>> getBlocks() {
        return blocks;
    }

    public void setBlocks(ArrayList<ArrayList<Integer>> blocks) {
        this.blocks = blocks;
    }

    public ArrayList<Sentence> getSentences() {
        return sentences;
    }

    public int getNumSentences() {
        return sentences.size();
    }

    public int getNumBlocks() {
        return blocks.size();
    }

    public void setSentences(ArrayList<Sentence> sentences) {
        this.sentences = sentences;
    }

    public boolean isSameBlock(int s1, int s2) {
        int b1 = getBlockIndex(s1);
        int b2 = getBlockIndex(s2);
        return b1 == b2;
    }

    public int getBlockIndex(int sentence) {
        int bIndex = 0;
        for (ArrayList<Integer> block : this.blocks) {
            for (int index : block) {
                if (sentence == index) {
                    return bIndex;
                }
            }
            bIndex++;
        }
        return -1;
    }

    public String toString() {
        String print = "";
        for (Sentence sentence : this.getSentences()) {
            print = print.concat(sentence.toString());
        }
        return print;
    }
}
