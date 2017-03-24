package main.java.bean;

import java.util.ArrayList;

public class FeatureRequest {
	
	String title;
	ArrayList<String> labels = new ArrayList<String>();
	ArrayList<String> sentences = new ArrayList<String>();
	ArrayList<Double> similarityToTitle = new ArrayList<Double>();
	ArrayList<Integer> ascOrder = new ArrayList<Integer>();
	ArrayList<Integer> descOrder = new ArrayList<Integer>();
	ArrayList<Integer> containMD = new ArrayList<Integer>();
	ArrayList<Integer> containWants = new ArrayList<Integer>();
	ArrayList<Integer> containShouldCan = new ArrayList<Integer>();
	ArrayList<Integer> startWithVB = new ArrayList<Integer>();
	ArrayList<Integer> matchMDGOOD = new ArrayList<Integer>();
	ArrayList<Integer> containNEG = new ArrayList<Integer>();
	ArrayList<Integer> question = new ArrayList<Integer>();
	ArrayList<Integer> numTrunk = new ArrayList<Integer>();
	ArrayList<Integer> numToken = new ArrayList<Integer>();
	ArrayList<Integer> containEXP = new ArrayList<Integer>();
	ArrayList<Integer> isRealFirst = new ArrayList<Integer>();
	ArrayList<Integer> matchMDGOODVB = new ArrayList<Integer>();
	ArrayList<Integer> matchVBDGOOD = new ArrayList<Integer>();
	ArrayList<Integer> numValidVerbs = new ArrayList<Integer>();
	ArrayList<Integer> matchMDGOODIF = new ArrayList<Integer>();
	ArrayList<Integer> matchGOODIF = new ArrayList<Integer>();
	ArrayList<Integer> matchSYSNEED = new ArrayList<Integer>();
	ArrayList<Integer> isPastTense = new ArrayList<Integer>();
	ArrayList<Integer> sentimentScore = new ArrayList<Integer>();
	ArrayList<Double> sentimentProbability = new ArrayList<Double>();
	ArrayList<Integer> numValidWords = new ArrayList<Integer>();
	ArrayList<String> subjects = new ArrayList<String>();
	ArrayList<String> actions = new ArrayList<String>();
	
	public FeatureRequest(String title) {
		this.title = title;
	}
	
	
	public void addSubjects(String label){
		subjects.add(label);
	}
	
	public String getSubjects(int index){
		return subjects.get(index);
	}
	
	public void addActions(String label){
		actions.add(label);
	}
	
	public String getActions(int index){
		return actions.get(index);
	}
	
	
	public void addSentimentScore(int score){
		sentimentScore.add(score);
	}
	
	public int getSentimentScore(int index){
		return sentimentScore.get(index);
	}
	
	
	public void addSentimentProbability(Double probability){
		sentimentProbability.add(probability);
	}
	
	public Double getSentimentProbability(int index){
		return sentimentProbability.get(index);
	}
	
	
	
	public String getTitle() {
		return title;
	}
	public void setTitle(String title) {
		this.title = title;
	}
	
	
	
	public void addSentence(String sentence){
		sentences.add(sentence);
	}
	
	public void addLabel(String label){
		labels.add(label);
	}
	
	public String getLabel(int index){
		return labels.get(index);
	}
	
	public String getSentence(int index){
		return sentences.get(index);
	}
	
	public void addSimilarity(Double score){
		similarityToTitle.add(score);
	}
	
	public Double getSimilairity(int index){
		return similarityToTitle.get(index);
	}
	
	public void addAscOrder(Integer pos){
		ascOrder.add(pos);
	}
	
	public Integer getAscOrder(int index){
		return ascOrder.get(index);
	}
	
	public void addDescOrder(Integer pos){
		descOrder.add(pos);
	}
	
	public Integer getDescOrder(int index){
		return descOrder.get(index);
	}
	
	public void addContainMD(Integer result){
		containMD.add(result);
	}
	
	public Integer getContainMD(int index){
		return containMD.get(index);
	}
	
	public void addContainWants(Integer result){
		containWants.add(result);
	}
	
	public Integer getContainWants(int index){
		return containWants.get(index);
	}
	
	public void addContainShouldCan(Integer result){
		containShouldCan.add(result);
	}
	
	public Integer getContainShouldCan(int index){
		return containShouldCan.get(index);
	}
	
	public void addsStartWithVB(Integer result){
		startWithVB.add(result);
	}
	
	public Integer getStartWithVB(int index){
		return startWithVB.get(index);
	}
	
	public void addMatchMDGOOD(Integer result){
		matchMDGOOD.add(result);
	}
	
	public Integer getMatchMDGOOD(int index){
		return matchMDGOOD.get(index);
	}
	
	public void addMatchMDGOODVB(Integer result){
		matchMDGOODVB.add(result);
	}
	
	public Integer getMatchMDGOODVB(int index){
		return matchMDGOODVB.get(index);
	}
	
	public void addMatchMDGOODIF(Integer result){
		matchMDGOODIF.add(result);
	}
	
	public Integer getMatchMDGOODIF(int index){
		return matchMDGOODIF.get(index);
	}
	
	public void addMatchGOODIF(Integer result){
		matchGOODIF.add(result);
	}
	
	public Integer getMatchGOODIF(int index){
		return matchGOODIF.get(index);
	}
	
	
	public void addMatchVBDGOOD(Integer result){
		matchVBDGOOD.add(result);
	}
	
	public Integer getMatchVBDGOODB(int index){
		return matchVBDGOOD.get(index);
	}
	
	public void addMatchSYSNEED(Integer result){
		matchSYSNEED.add(result);
	}
	
	public Integer getMatchSYSNEED(int index){
		return matchSYSNEED.get(index);
	}
	
	public void addContainNEG(int result){
		containNEG.add(result);
	}
	
	public Integer getContainNEG(int index){
		return containNEG.get(index);
	}
	
	public void addQuestion(int result){
		question.add(result);
	}
	
	public Integer getQuestion(int index){
		return question.get(index);
	}

	public void addNumTrunk(int result){
		numTrunk.add(result);
	}
	
	public Integer getNumTrunk(int index){
		return numTrunk.get(index);
	}
	public void addNumToken(int result){
		numToken.add(result);
	}
	
	public Integer getNumToken(int index){
		return numToken.get(index);
	}
	public void addContainEXP(int result){
		containEXP.add(result);
	}
	
	public Integer getContainEXP(int index){
		return containEXP.get(index);
	}
	
	
	public void addIsRealFirst(int result){
		isRealFirst.add(result);
	}
	
	public Integer getIsRealFirst(int index){
		return isRealFirst.get(index);
	}
	
	public void addIsPastTense(int result){
		isPastTense.add(result);
	}
	
	public Integer getIsPastTense(int index){
		return isPastTense.get(index);
	}
	
	
	
	
	
	
	public void addNumValidVerbs(int result){
		numValidVerbs.add(result);
	}
	
	public Integer getNumValidVerbs(int index){
		return numValidVerbs.get(index);
	}
	
	public void addNumValidWords(int result){
		numValidWords.add(result);
	}
	
	public Integer getNumValidWords(int index){
		return numValidWords.get(index);
	}
	
	
	
	public int getNumSentence(){
		return sentences.size();
	}

	@Override
	public String toString() {
		//String fr = "Title="+title+"\n";
		String fr = "\n\n";
		for(int i =0; i<labels.size();i++){
			fr+=sentences.get(i)+"\n";
			fr+=similarityToTitle.get(i)+"\n";
			fr+=ascOrder.get(i)+"\n";
			fr+=descOrder.get(i)+"\n";
			fr+=labels.get(i)+"\n";
			fr+="containMD"+containMD.get(i)+"\n";
			fr+="containWants"+containWants.get(i)+"\n";
			fr+="containShouldCan:"+containShouldCan.get(i)+"\n";
			fr+="startWithVB: "+startWithVB.get(i)+"\n";
			fr+="matchMDGOOD: "+matchMDGOOD.get(i)+"\n";
			fr+="containNEG: "+containNEG.get(i)+"\n";
			fr+="question: "+question.get(i)+"\n";
			fr+="numTrunk: "+numTrunk.get(i)+"\n";
			fr+="numToken: "+numToken.get(i)+"\n";
			fr+="containEXP: "+containEXP.get(i)+"\n";
			fr+="isRealFirst: "+isRealFirst.get(i)+"\n";
			fr+="matchMDGOODVB: "+matchMDGOODVB.get(i)+"\n";
			fr+="matchVBDGOOD: "+matchVBDGOOD.get(i)+"\n";
			fr+="numValidVerbs: "+numValidVerbs.get(i)+"\n";
			fr+="matchMDGOODIF: "+matchMDGOODIF.get(i)+"\n";
			fr+="matchGOODIF: "+matchGOODIF.get(i)+"\n";
			fr+="matchSYSNEED: "+matchSYSNEED.get(i)+"\n";
			fr+="isPastTense: "+isPastTense.get(i)+"\n";
			fr+="sentimentScore: "+sentimentScore.get(i)+"\n";
			fr+="sentimentProbability: "+sentimentProbability.get(i)+"\n";
			fr+="numValidWords: "+numValidWords.get(i)+"\n";
			fr+="subjects: "+subjects.get(i)+"\n";
			fr+="actions: "+actions.get(i)+"\n";
			
			
		}
		return fr;   
	}

	public boolean hasRealFirstBefore() {
		if(isRealFirst == null || isRealFirst.isEmpty())
			return false;
		
		for(int i : isRealFirst){
			if(i == 1)
				return true;
		}
		
		return false;
	}	
}
