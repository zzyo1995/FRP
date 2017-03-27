package main.java.core;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;

import org.apache.lucene.search.spell.NGramDistance;

import main.java.bean.FeatureRequest;
import main.java.bean.FeatureRequestOL;
import main.java.util.FeatureUtility;
import weka.core.Attribute;
import weka.core.DenseInstance;
import weka.core.Instances;

public class DataParser {

	static boolean DEBUG = true;
	static File propertiesFile;
	static BufferedReader reader;
	static String exportDir;
	static ArrayList<FeatureRequest> featureRequestList = new ArrayList<FeatureRequest>();
	static HashSet<String> labels = new HashSet<String>();
	static ArrayList<String> labelArray = new ArrayList<String>();
	static StanfordCoreNlpDemo nlpTool;
	static String outputFR = "data//log//all-feature-requests.txt";
	
	public DataParser(String path){
		  nlpTool = new StanfordCoreNlpDemo(false,path);
		  outputFR = path+"data//log//all-feature-requests.txt";
	}

	public static void groupTrainData(String outputDir, String targetFileName) throws FileNotFoundException {
		exportDir = outputDir;
		loadTaggedFRFile(targetFileName);
		readFileByLines();
	}

	public static void loadTaggedFRFile(String fileName) throws FileNotFoundException {
		propertiesFile = new File(fileName);
		reader = new BufferedReader(new FileReader(propertiesFile));
	}

	public static void readFileByLines() {

		try {
			String tempString = null;
			int line = 1;

			while ((tempString = reader.readLine()) != null) {

				FeatureUtility.splitByEqual(tempString, line, true, true);
				line++;

			}
			reader.close();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if (reader != null) {
				try {
					reader.close();
				} catch (IOException e1) {
				}
			}
		}
	}
	


	public static Instances readIntoFeatureRequests(String targetFileName) throws Exception {

		loadTaggedFRFile(targetFileName);

		constructFeatureRequestList();

		if (DEBUG) {
			System.out.printf("============Reading from %s to FeatureRequestList ==============\n", targetFileName);
			FeatureUtility.exportIFeatureRequestsToFile(featureRequestList, outputFR);
			
			//System.out.println(featureRequestList);
			System.out.printf("\nExported %d feature request to file: %s\n",featureRequestList.size(),outputFR);
		}

		Instances data = createWekaInstances();

		return data;

	}

	private static Instances createWekaInstances()
			throws Exception {
		Instances data;
		double[] vals;
		
		ArrayList<Attribute> attributeList = constructAttributeList();
		data = new Instances("PMA Feature Requests", attributeList, 0);

		int numAttr = data.numAttributes();
		
		for (FeatureRequest fr : featureRequestList) {
			for (int i = 0; i < fr.getNumSentences(); i++) {
				vals = constructAttributeValues(numAttr,fr,i,data);
				data.add(new DenseInstance(1.0, vals));
			}
		}
		
		data.setClassIndex(numAttr - 1);

		return data;
	}

	//TODO update when add new attributes
	private static double[] constructAttributeValues(int numAttr, FeatureRequest fr, int i, Instances data) {
		
		Attribute stringAttr = data.attribute(0);
		double[] vals = new double[numAttr];
		vals[0] = stringAttr.addStringValue(fr.getSentence(i));
		vals[1] = fr.getSimilairity(i);
		vals[2] = fr.getAscOrder(i);
		vals[3] = fr.getDescOrder(i);
		vals[4] = fr.getContainMD(i);
		vals[5] = fr.getContainWants(i);
		vals[6] = fr.getContainShouldCan(i);
		vals[7] = fr.getStartWithVB(i);
		vals[8] = fr.getMatchMDGOOD(i);
		vals[9] = fr.getContainNEG(i);
		vals[10] = fr.getQuestion(i);
		vals[11] = fr.getNumTrunk(i);
		vals[12] = fr.getNumToken(i);
		vals[13] = fr.getContainEXP(i);
		vals[14] = fr.getIsRealFirst(i);
		vals[15] = fr.getMatchMDGOODVB(i);
		vals[16] = fr.getMatchVBDGOODB(i);
		vals[17] = fr.getNumValidVerbs(i);
		vals[18] = fr.getMatchMDGOODIF(i);
		vals[19] = fr.getMatchGOODIF(i);
		vals[20] = fr.getMatchSYSNEED(i);
		vals[21] = fr.getIsPastTense(i);
		vals[22] = fr.getSentimentScore(i);
		vals[23] = fr.getSentimentProbability(i);
		vals[24] = fr.getNumValidWords(i);
		vals[25] = data.attribute("subjects").addStringValue(fr.getSubjects(i));
		vals[26] = data.attribute("actions").addStringValue(fr.getActions(i));
		
		vals[27] = labelArray.indexOf(fr.getLabel(i));
		return vals;
	}

	//TODO update when add new attribute
	private static ArrayList<Attribute> constructAttributeList() {
		ArrayList<Attribute> attributeList = new ArrayList<Attribute>();
		attributeList.add(new Attribute("sentence", (ArrayList<String>) null));
		attributeList.add(new Attribute("similarityToTitle"));
		attributeList.add(new Attribute("ascOrder"));
		attributeList.add(new Attribute("descOrder"));
		attributeList.add(new Attribute("containMD"));
		attributeList.add(new Attribute("containWants"));
		attributeList.add(new Attribute("containShouldCan"));
		attributeList.add(new Attribute("startWithVB"));
		attributeList.add(new Attribute("matchMDGOOD"));
		attributeList.add(new Attribute("containNEG"));
		attributeList.add(new Attribute("question"));
		attributeList.add(new Attribute("numTrunk"));
		attributeList.add(new Attribute("numToken"));
		attributeList.add(new Attribute("containEXP"));
		attributeList.add(new Attribute("isRealFirst"));
		attributeList.add(new Attribute("matchMDGOODVB"));
		attributeList.add(new Attribute("matchVBDGOOD"));
		attributeList.add(new Attribute("numValidVerbs"));
		attributeList.add(new Attribute("matchMDGOODIF"));
		attributeList.add(new Attribute("matchGOODIF"));
		attributeList.add(new Attribute("matchSYSNEED"));
		attributeList.add(new Attribute("isPastTense"));
		attributeList.add(new Attribute("sentimentScore"));
		attributeList.add(new Attribute("sentimentProbability"));
		attributeList.add(new Attribute("numValidWords"));
		attributeList.add(new Attribute("subjects",(ArrayList<String>) null));
		attributeList.add(new Attribute("actions",(ArrayList<String>) null));
		attributeList.add(new Attribute("tag", labelArray));
		
		return attributeList;
	}

	/**
	 * Split tagged file by empty line, and parse oneFeatureRequest one by one.
	 */
	private static void constructFeatureRequestList() {
		try {

			String tempString = null;
			ArrayList<String> oneFeatureRequest = new ArrayList<String>();

			int line = 1;
			while ((tempString = reader.readLine()) != null) {
				// parse oneFeatureRequest when line is empty
				if (tempString.trim().length() == 0) {
					if (!oneFeatureRequest.isEmpty()) {
						constructSingleFeatureRequest(oneFeatureRequest, line);
						oneFeatureRequest = new ArrayList<String>();
						line++;
					}
					continue;
				}
				oneFeatureRequest.add(tempString);
			}

			// parse the last one
			if (!oneFeatureRequest.isEmpty()) {
				constructSingleFeatureRequest(oneFeatureRequest, line);
				oneFeatureRequest = new ArrayList<String>();
				line++;
			}
			
			constructLabelArray();

			reader.close();
			nlpTool.exit();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if (reader != null) {
				try {
					reader.close();
				} catch (IOException e1) {
				}
			}
		}
	}

	private static void constructLabelArray() {
		//for (String tag : labels)
		//	labelArray.add(tag);
		labelArray.add("explanation");
		labelArray.add("want");
		labelArray.add("useless");
	}

	private static void constructSingleFeatureRequest(ArrayList<String> oneFeatureRequest, int line) throws IOException {
		int titleIndex = 0;
		int targetIndex = oneFeatureRequest.size();
		String title = null;

		for (int i = 0; i < oneFeatureRequest.size(); i++) {
			String item = oneFeatureRequest.get(i);
			if (item.contains("title=")) {
				titleIndex = i;
				String[] temp = FeatureUtility.splitByEqual(item, 0, false, false);
				if (temp != null)
					title = temp[1];
				else
					System.err.println("NullPointer Exception");
			}

			if (item.contains("target="))
				targetIndex = i;
		}

		FeatureRequest fr = constructSingleFeatureRequest(title, oneFeatureRequest, titleIndex, targetIndex);
		
		if (DEBUG) {
			System.out.printf("=============FR print #%d ===================\n",line);
			System.out.println(fr);
		}
		
		if(fr!=null)
			featureRequestList.add(fr);
	}

	public static FeatureRequestOL constructSFeatureRequestOL(FeatureRequestOL request) throws IOException{
		FeatureRequestOL fr = request;
		NGramDistance ng = new NGramDistance();
		
		int size = request.getNumSentences();
		
		for(int i = 0 ; i < size; i ++){
			double[] nlpValues = null;
			String content = request.getSentence(i);
			String title = request.getTitle();
			
			//判断内容是否为空
			if(title==null || content==null || title.isEmpty() || content.isEmpty()){
				System.err.println("Empty title or content input in : \n");
				System.err.println(fr);
				return null;
			}
			
			//计算NLP的属性值
			String filteredContent = content.replaceAll("[(].*[)]", "");
			nlpValues = nlpTool.parseSingleSentence(filteredContent);
			
			if (nlpValues == null) {
				System.err.println("NLP analysis error when parse: !" + content);
				continue;
			}
			
			//计算isRealFirst
			if(fr.hasRealFirstBefore())
				fr.addIsRealFirst(0);
			else{
				if (RequestClassifier.classifyAsUseless(content)) {
					fr.addIsRealFirst(0);
				} else {
					fr.addIsRealFirst(1);
				}
			}
			
		
		
		
		Double similarity = Double.valueOf(ng.getDistance(title, content));
		
		//TODO Update when add new attributes
		fr.addLabel("NA");
		//fr.addSentence(content);
		fr.addSimilarity(similarity);
		fr.addAscOrder(Integer.valueOf(i+1));
		fr.addContainMD(new Integer((int)nlpValues[0]));
		fr.addContainWants(new Integer((int)nlpValues[1]));
		fr.addContainShouldCan(new Integer((int)nlpValues[2]));
		fr.addsStartWithVB(new Integer((int)nlpValues[3]));
		fr.addMatchMDGOOD(new Integer((int)nlpValues[4]));
		fr.addContainNEG(new Integer((int)nlpValues[5]));
		fr.addQuestion(new Integer((int)nlpValues[6]));
		fr.addNumTrunk(new Integer((int)nlpValues[7]));
		fr.addNumToken(new Integer((int)nlpValues[8]));
		fr.addContainEXP(new Integer((int)nlpValues[9]));
		fr.addMatchMDGOODVB((int)nlpValues[10]);
		fr.addMatchVBDGOOD((int)nlpValues[11]);
		fr.addNumValidVerbs((int)nlpValues[12]); // [12]containValidVerbs
		fr.addMatchMDGOODIF((int)nlpValues[13]);
		fr.addMatchGOODIF((int)nlpValues[14]);
		fr.addMatchSYSNEED((int)nlpValues[15]);
		fr.addIsPastTense((int)nlpValues[16]);
		fr.addSentimentScore((int)nlpValues[17]);
		fr.addSentimentProbability(nlpValues[18]);
		fr.addNumValidWords((int)nlpValues[19]);
		
		String subject = "null";
		String action = "null";
		if(nlpTool.subjectandAction!=null && nlpTool.subjectandAction.length!=0){
			 subject = nlpTool.subjectandAction[0];
			 action = nlpTool.subjectandAction[1];
		}
		
		fr.addSubjects(subject);
		fr.addActions(action);
	}

	for (int i = size; i > 0; i--) {
		fr.addDescOrder(Integer.valueOf(i));

	}
	

	return fr;
		
		
	}
	
	
	private static FeatureRequest constructSingleFeatureRequest(String title, ArrayList<String> oneFeatureRequest, int titleIndex,
			int targetIndex) throws IOException {

		FeatureRequest fr = new FeatureRequest(title);
		NGramDistance ng = new NGramDistance();

		int size = 0;

		for (int i = titleIndex + 1; i < targetIndex; i++) {
			double[] nlpValues = null;
			String line = oneFeatureRequest.get(i);
			String[] temp = FeatureUtility.splitByEqual(line, 0, false, false);
			String tag = null;
			String content;
			
			tag = getTag(temp,oneFeatureRequest,i);
			
			if(tag==null)
				continue;
			
			content = getContent(temp[1]);
			labels.add(tag);
			
			size++;


			// TODO debug here
			String filteredContent = content.replaceAll("[(].*[)]", "");
			nlpValues = nlpTool.parseSingleSentence(filteredContent);

			if (nlpValues == null) {
				System.err.println("NLP analysis error when parse: !" + content);
				continue;
			}
			
			
			if(fr.hasRealFirstBefore())
				fr.addIsRealFirst(0);
			else{
				if (RequestClassifier.classifyAsUseless(content)) {
					fr.addIsRealFirst(0);
				} else {
					fr.addIsRealFirst(1);
				}
			}
			
			if(title==null || content==null || title.isEmpty() || content.isEmpty()){
				System.err.println("Empty title or content input in : \n");
				System.err.println(oneFeatureRequest);
				return null;
			}

			Double similarity = Double.valueOf(ng.getDistance(title, content));
			
			//TODO Update when add new attributes
			fr.addLabel(tag);
			fr.addSentence(content);
			fr.addSimilarity(similarity);
			fr.addAscOrder(Integer.valueOf(size));
			fr.addContainMD(new Integer((int)nlpValues[0]));
			fr.addContainWants(new Integer((int)nlpValues[1]));
			fr.addContainShouldCan(new Integer((int)nlpValues[2]));
			fr.addsStartWithVB(new Integer((int)nlpValues[3]));
			fr.addMatchMDGOOD(new Integer((int)nlpValues[4]));
			fr.addContainNEG(new Integer((int)nlpValues[5]));
			fr.addQuestion(new Integer((int)nlpValues[6]));
			fr.addNumTrunk(new Integer((int)nlpValues[7]));
			fr.addNumToken(new Integer((int)nlpValues[8]));
			fr.addContainEXP(new Integer((int)nlpValues[9]));
			fr.addMatchMDGOODVB((int)nlpValues[10]);
			fr.addMatchVBDGOOD((int)nlpValues[11]);
			fr.addNumValidVerbs((int)nlpValues[12]); // [12]containValidVerbs
			fr.addMatchMDGOODIF((int)nlpValues[13]);
			fr.addMatchGOODIF((int)nlpValues[14]);
			fr.addMatchSYSNEED((int)nlpValues[15]);
			fr.addIsPastTense((int)nlpValues[16]);
			fr.addSentimentScore((int)nlpValues[17]);
			fr.addSentimentProbability(nlpValues[18]);
			fr.addNumValidWords((int)nlpValues[19]);
			
			String subject = "";
			String action = "";
			if(nlpTool.subjectandAction!=null && nlpTool.subjectandAction.length!=0){
				 subject = nlpTool.subjectandAction[0];
				 action = nlpTool.subjectandAction[1];
			}
			
			fr.addSubjects(subject);
			fr.addActions(action);
		}

		for (int i = size; i > 0; i--) {
			fr.addDescOrder(Integer.valueOf(i));

		}
		

		return fr;
	}

	private static String getContent(String content) {
		String refinedContent = content;
		if (content != null && content.length() > 0) {
			refinedContent = content.trim();
			char ch = (refinedContent.charAt(refinedContent.length() - 1));

			if (ch == '.') {
				refinedContent = refinedContent.substring(0, refinedContent.length() - 1);
			}
		}
		return refinedContent;
	}

	private static String getTag(String[] temp, ArrayList<String> oneFeatureRequest, int line) {
		String tag = null;
		if (temp != null)
			tag = temp[0];
		else {
			System.err.println("Null input at line: "+line);
			System.err.println(oneFeatureRequest);
		}

		if (FeatureUtility.checkContains(tag, FeatureUtility.excludeTags,false))
			tag =null;
		else if(!tag.equals("want") && !tag.equals("useless"))
			tag = "explanation";
		
		return tag;
	}

	public static void main(String[] args) {
		String title = "Autofill and more flexible sql tab";
		ArrayList<String> oneFeatureRequest = new ArrayList<String>();
		oneFeatureRequest.add("title=Autofill and more flexible sql tab");
		oneFeatureRequest.add("useless=Good Luck ");
		oneFeatureRequest.add("useless=Thanks you for this brilliant project. ");
		oneFeatureRequest.add("want=Make auto fill to finish table names or column. ");
		oneFeatureRequest.add("explaination=Like tab do inside bash, finishing dir and file names. ");
		oneFeatureRequest.add("want=I would appreciate a feature that allows to control some User Interface functionality from phpMyAdmin");

		int titleIndex = 0;
		int targetIndex = oneFeatureRequest.size();

		try {
			FeatureRequest fr = constructSingleFeatureRequest(title, oneFeatureRequest, titleIndex, targetIndex);
			System.out.println(fr);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
