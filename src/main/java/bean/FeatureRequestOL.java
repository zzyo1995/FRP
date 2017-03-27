package main.java.bean;

import java.util.ArrayList;

import main.java.parse.Parser;

/**
 * Created by zzyo on 2017/3/17.
 */
public class FeatureRequestOL extends FeatureRequest{

    private String systemName;
    private String title;
    private ArrayList<ArrayList<Integer>> blocks = new ArrayList<>();
    ArrayList<Sentence> fullSentences = new ArrayList<>();


    public FeatureRequestOL(String title) {
        super(title);
    }

    public FeatureRequestOL(String systemName, String title, ArrayList<ArrayList<Integer>> blocks, ArrayList<Sentence> sentences) {
        super(title);
        this.systemName = systemName;
        this.title = title;
        this.blocks = blocks;
        this.fullSentences = sentences;
        for(Sentence sentence : sentences){
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

    public ArrayList<Sentence> getFullSentences() {
        return fullSentences;
    }


    public int getNumBlocks(){
        return blocks.size();
    }

    public void setFullSentences(ArrayList<Sentence> sentences) {
        this.fullSentences = sentences;
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

	public Sentence getFullSentence(int i) {
		return fullSentences.get(i);
	}
	
    public String toString() {
    	
        String print = "";
        for (Sentence sentence : this.getFullSentences()) {
            print = print.concat(sentence.toString());
        }
        return super.toString()+"\n"+print;
    }
}
