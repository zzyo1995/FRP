package main.java.bean;

import main.java.parse.Parser;

import java.util.ArrayList;

/**
 * Created by zzyo on 2017/3/17.
 */
public class FR {

    private String systemName;
    private String title;
    //private ArrayList<BlockItem> blockItems = new ArrayList<>();
    private ArrayList<ArrayList<Integer>> blocks = new ArrayList<>();
    ArrayList<Sentence> sentences = new ArrayList<>();


    public FR() {
    }

    public FR(String systemName, String title, ArrayList<ArrayList<Integer>> blocks, ArrayList<Sentence> sentences) {
        this.systemName = systemName;
        this.title = title;
        this.blocks = blocks;
        this.sentences = sentences;
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

/*    public ArrayList<BlockItem> getBlockItems() {
        return blockItems;
    }

    public void setBlockItems(ArrayList<BlockItem> blockItems) {
        this.blockItems = blockItems;
    }*/

    public ArrayList<ArrayList<Integer>> getBlocks() {
        return blocks;
    }

    public void setBlocks(ArrayList<ArrayList<Integer>> blocks) {
        this.blocks = blocks;
    }

    public ArrayList<Sentence> getSentences() {
        return sentences;
    }

    public void setSentences(ArrayList<Sentence> sentences) {
        this.sentences = sentences;
    }

    public boolean isSameBlock(int s1, int s2){
        int b1 = getBlockIndex(s1);
        int b2 = getBlockIndex(s2);
        return b1 == b2;
    }

    public  int getBlockIndex(int sentence){
        int bIndex = 0;
        for(ArrayList<Integer> block : this.blocks){
            for(int index : block){
                if( sentence == index){
                    return bIndex;
                }
            }
            bIndex++;
        }
        return -1;
    }
}
