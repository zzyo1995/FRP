package main.java.bean;

import main.java.parse.Parser;

import java.util.ArrayList;

/**
 * Created by zzyo on 2017/3/17.
 */
public class FR {

    private String systemName;
    private String title;
    private ArrayList<BlockItem> blockItems = new ArrayList<>();


    public FR() {
    }

    public FR(String systemName, String title, ArrayList<BlockItem> blockItems) {
        this.systemName = systemName;
        this.title = title;
        this.blockItems = blockItems;
    }

    public String[] getSystemName() {
        String[] names = Parser.getTokens(systemName);
        return names;
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

    public ArrayList<BlockItem> getBlockItems() {
        return blockItems;
    }

    public void setBlockItems(ArrayList<BlockItem> blockItems) {
        this.blockItems = blockItems;
    }
}
