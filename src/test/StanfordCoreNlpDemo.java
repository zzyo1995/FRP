package test;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Properties;

import org.ejml.simple.SimpleMatrix;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import edu.stanford.nlp.ling.CoreAnnotations;
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.ling.TaggedWord;
import edu.stanford.nlp.neural.rnn.RNNCoreAnnotations;
import edu.stanford.nlp.parser.lexparser.LexicalizedParser;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;
import edu.stanford.nlp.process.CoreLabelTokenFactory;
import edu.stanford.nlp.process.PTBTokenizer;
import edu.stanford.nlp.process.Tokenizer;
import edu.stanford.nlp.process.TokenizerFactory;
import edu.stanford.nlp.sentiment.SentimentCoreAnnotations;
import edu.stanford.nlp.trees.Tree;
import edu.stanford.nlp.util.CoreMap;
import main.java.util.FeatureUtility;
import main.java.util.SentimentClass;

public class StanfordCoreNlpDemo {

	static StanfordCoreNLP pipeline;
	public static int checkContains(String text, String[] list) {
		for (String word : list) {
			if (text.toLowerCase().contains(word.toLowerCase()))
				return 1;
		}
		return 0;
	}

	public static List<CoreLabel> getRawWords(String text) {
		TokenizerFactory<CoreLabel> tokenizerFactory = PTBTokenizer.factory(new CoreLabelTokenFactory(), "");
		Tokenizer<CoreLabel> tok = tokenizerFactory.getTokenizer(new StringReader(text));
		List<CoreLabel> rawWords = tok.tokenize();
		return rawWords;
	}
	public static void main(String[] args) throws IOException {
		// String text = "show database creation date, ability to sort (for
		// example, latest first) on the database list page.";

		// String text = "I hate it pretty much";
		// String text = "these are pretty much unusable without resorting to
		// ugly hacks like creating a temporary Extension instance just to get
		// access to the directory.";
		String text = "which you must find";
		
		String filteredContent = text.replaceAll("[(].*[)]", "");
		filteredContent = filteredContent.replaceAll("[\"].*[\"]", "");
		filteredContent = filteredContent.replaceAll("['].*[']", "");
		
		System.out.println(filteredContent);
		
		StanfordCoreNlpDemo nlp = new StanfordCoreNlpDemo(true,"");

		nlp.parseSingleSentence(text);
		nlp.exit();
	}
	boolean DEBUG = false;
	ArrayList<String> lemaList = new ArrayList<String>();
	LexicalizedParser lp = null;
	PrintWriter out = null;
	ArrayList<String> posList = new ArrayList<String>();
	Properties props;
	String[] sentimentText = { "Very Negative", "Negative", "Neutral", "Positive", "Very Positive" };

	String[] subjectandAction;

	String[] sysNeeds = { "need", "should", "can", "must" };

	ArrayList<String> wordList = new ArrayList<String>();

	public StanfordCoreNlpDemo(boolean debug, String path) {
		Properties props = new Properties();
		props.setProperty("annotators", "tokenize, ssplit, pos, lemma, ner, parse, dcoref, sentiment");
		pipeline = new StanfordCoreNLP(props);
		DEBUG = debug;

		String parserModel = "edu/stanford/nlp/models/lexparser/englishPCFG.ser.gz";
		lp = LexicalizedParser.loadModel(parserModel);

		try {
			String fullPath = "";
			if(path!=null)
				fullPath = path+"data\\StandordNLP-temp.txt";
			else
				fullPath = "data//StandordNLP-temp.txt";
			System.out.println("Loading file: "+fullPath);
			out = new PrintWriter(new FileOutputStream(new File(fullPath), true));
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
	}

	void exit() {
		out.close();

	}

	private void exportTagLemma(List<TaggedWord> taggedWords, List<CoreLabel> lemaWords) {
		for (int i = 0; i < taggedWords.size(); i++) {
			String word = taggedWords.get(i).word();
			String tag = taggedWords.get(i).tag();
			wordList.add(word);
			posList.add(tag);
			lemaList.add(lemaWords.get(i).lemma());
		}

	}

	public HashSet<String> getAllVerbs() {
		HashSet<String> allVerbs = new HashSet<String>();

		for (int i = 0; i < posList.size(); i++) {
			if (posList.get(i).contains("VB")) {
				allVerbs.add(lemaList.get(i));
			}
		}
		return allVerbs;
	}

	private int getCOPDependentValue(JsonNode basicDependenciesNode, String governorGloss) {
		for (int i = 0; i < basicDependenciesNode.size(); i++) {
			JsonNode depNode = basicDependenciesNode.get(i);

			if (governorGloss != null && !governorGloss.isEmpty()) {
				String depValue = depNode.path("governorGloss").asText();
				if (depValue.equalsIgnoreCase(governorGloss)) {
					String depType = depNode.path("dep").asText();
					if (depType.equals("cop")) {
						String dependent = depNode.path("dependent").asText();
						return Integer.valueOf(dependent);
					}
				}

			}

			else {
				String depType = depNode.path("dep").asText();
				if (depType.equals("cop")) {
					return depNode.path("dependent").asInt();
				}
			}

		}
		return -1;
	}

	private int getGoodIndexFrom(int start) {
		for (int i = start; i < wordList.size(); i++) {
			String word = wordList.get(i);
			if (checkContains(word, FeatureUtility.GOOD) == 1) {
				return i;
			}
		}
		return -1;
	}

	// TODO
	private int getNumValidVerbs() {
		int size = 0;
		HashSet<String> allVerbs = getAllVerbs();

		if (allVerbs == null || allVerbs.isEmpty())
			return 0;

		for (String verb : allVerbs) {
			if (FeatureUtility.isContain(verb, FeatureUtility.USELESSVERB, true) == false) {
				size++;
			}
		}

		return size;

	}

	private int getNumValidWords() {

		int size = wordList.size();

		for (int i = 0; i < wordList.size(); i++) {
			String token = wordList.get(i);
			if (FeatureUtility.isContain(token, FeatureUtility.SMART_STOP_WORDS, true)
					|| FeatureUtility.isContain(token, FeatureUtility.SYSTEM_NAMES, true)
					|| posList.get(i).equals("NNP"))
				// ||FeatureUtility.isContain(token,FeatureUtility.GOOD,true))
				size--;
		}

		return size;
	}

	private String[] getSubjectAction(JsonNode basicDependenciesNode) {
		String[] subAction = new String[2];
		boolean find = false;
		for (int i = 0; i < basicDependenciesNode.size(); i++) {
			JsonNode depNode = basicDependenciesNode.get(i);
			String depValue = depNode.path("dep").asText();
			if (depValue.equals("nsubj")) {
				int subIndex = depNode.path("dependent").asInt() - 1;
				int actionIndex = depNode.path("governor").asInt() - 1;
				subAction[0] = lemaList.get(subIndex);
				if (actionIndex < lemaList.size())
					subAction[1] = lemaList.get(actionIndex);
				else
					subAction[1] = "";
				find = true;
				break;
			}
			

		}
		
		if (find)
			return subAction;
		else{
			for (int i = 0; i < basicDependenciesNode.size(); i++) {
				JsonNode depNode = basicDependenciesNode.get(i);
				String depValue = depNode.path("dep").asText();
				if (!depValue.equals("nsubj")&&depValue.contains("nsubj")) {
					int subIndex = depNode.path("dependent").asInt() - 1;
					int actionIndex = depNode.path("governor").asInt() - 1;
					subAction[0] = lemaList.get(subIndex);
					if (actionIndex < lemaList.size())
						subAction[1] = lemaList.get(actionIndex);
					else
						subAction[1] = "";
					find = true;
					break;
				}
			}
		}
		
		if (find)
			return subAction;
		else
			return null;
	}

	private boolean hasDependency(JsonNode basicDependenciesNode, String string) {
		for (int i = 0; i < basicDependenciesNode.size(); i++) {
			JsonNode depNode = basicDependenciesNode.get(i);
			String depValue = depNode.path("dep").asText();
			if (depValue.equalsIgnoreCase("neg")) {
				return true;
			}
		}
		return false;
	}

	/**
	 * 
	 * @param text
	 * @return [0]=containMD; [1]=containWants; [2]containShouldCan;
	 *         [3]startWithVB; [4]matchMDGOOD; [5]containNEG;[6]question;
	 *         [7]numTrunk;[8]numToken;[9]containEXP [10]matchMDGOODVB
	 *         [11]matchVBDGOOD [12]containValidVerbs [13]matchMDGOODIF
	 *         [14]matchGOODIF [15]matchSYSNEED [16]isPastTense
	 *         [17]sentimentScore [18]sentimentProbability [19]numValidWords
	 * @throws IOException
	 */
	double[] parseSingleSentence(String text) throws IOException {

		double[] nlpValues = new double[20];

		posList = new ArrayList<String>();
		wordList = new ArrayList<String>();
		lemaList = new ArrayList<String>();

		nlpValues[1] = (double) checkContains(text, FeatureUtility.WANTS);
		nlpValues[2] = (double) checkContains(text, FeatureUtility.WANT_MD);
		nlpValues[9] = (double) checkContains(text, FeatureUtility.EXPLAINATION);

		nlpValues[8] = 0;
		nlpValues[7] = 0;
		nlpValues[5] = 0;
		nlpValues[13] = 0;

		List<CoreLabel> rawWords = getRawWords(text);

		Tree parse = lp.apply(rawWords);
		List<TaggedWord> taggedWords = parse.taggedYield();
		List<CoreLabel> lemaWords = lp.lemmatize(text);

		exportTagLemma(taggedWords, lemaWords);

		if (text.trim().endsWith("?"))
			nlpValues[6] = 1;
		else
			nlpValues[6] = 0;

		Annotation annotation = new Annotation(text);

		pipeline.annotate(annotation);
		StringWriter sw = new StringWriter();
		pipeline.jsonPrint(annotation, sw);

		ObjectMapper mapper = new ObjectMapper();
		JsonNode rootNode = mapper.readTree(sw.toString());
		JsonNode sentencesNode = rootNode.path("sentences");
		JsonNode corefsNode = rootNode.path("corefs");
		nlpValues[7] = corefsNode.size();

		JsonNode basicDependenciesNode = null;
		int sentencesSize = sentencesNode.size();

		if (sentencesSize == 1) {
			basicDependenciesNode = sentencesNode.get(0).path("basicDependencies");

			// System.out.println(text);
			// System.out.println("numDependencies:" +
			// basicDependenciesNode.size());

			if (hasDependency(basicDependenciesNode, "neg")) {
				nlpValues[5] = 1;
			}

			subjectandAction = getSubjectAction(basicDependenciesNode);
			
			if (true) {
				System.out.println();
				if (subjectandAction != null) {
					System.out.println("Subject = " + subjectandAction[0]);
					System.out.println("Action = " + subjectandAction[1]);
				}
			}
			

			if (subjectandAction != null && FeatureUtility.isSysName(subjectandAction[0])
					&& FeatureUtility.isContain(subjectandAction[1], sysNeeds, true))
				nlpValues[15] = 1;
			else
				nlpValues[15] = 0;

			

		} else {
			if (DEBUG)
				System.err.printf("There are %d sentences in : " + text, sentencesSize);
			return null;
		}
		// List<CoreMap> sentences =
		// annotation.get(CoreAnnotations.SentencesAnnotation.class);
		List<CoreMap> sentences = annotation.get(CoreAnnotations.SentencesAnnotation.class);
		float mainSentiment = 0;
		int longest = 0;
		for (CoreMap sentence : sentences) {
			String sentiment = sentence.get(SentimentCoreAnnotations.SentimentClass.class);
			Tree annotatedTree = sentence.get(SentimentCoreAnnotations.SentimentAnnotatedTree.class);
			int score = RNNCoreAnnotations.getPredictedClass(annotatedTree);
			SimpleMatrix mat = RNNCoreAnnotations.getPredictions(annotatedTree);
			double probability = mat.get(score);
			nlpValues[17] = score;
			nlpValues[18] = probability;
			if (DEBUG) {
				System.out.println("sentiment prediction = " + sentiment + "; probability = " + probability
						+ "; score = " + score + "\n" + sentence);
			}

			
			double[] probs = getProbabilityMatrix(mat);
					
			

		}

		/*
		 * if (sentences != null && sentences.size() > 0) { CoreMap sentence =
		 * sentences.get(0); Tree tree =
		 * sentence.get(TreeCoreAnnotations.TreeAnnotation.class);
		 * out.println(); out.println("The first sentence parsed is:");
		 * tree.pennPrint(out); }
		 */

		// out.println("\nExample output");

		if (FeatureUtility.getFirstIndex(rawWords, FeatureUtility.QUESTION) != -1) {
			nlpValues[6] = 1;
		}

		/*
		 * for (CoreMap sentence : sentences) { // a CoreLabel is a CoreMap with
		 * additional token-specific methods for (CoreLabel token :
		 * sentence.get(TokensAnnotation.class)) { String word =
		 * token.get(TextAnnotation.class); String pos =
		 * token.get(PartOfSpeechAnnotation.class); String ne =
		 * token.get(NamedEntityTagAnnotation.class); wordList.add(word);
		 * posList.add(pos);
		 * 
		 * if (word.equalsIgnoreCase("why")) { nlpValues[6] = 1; } }
		 * 
		 * if (sentence.get(SemanticGraphCoreAnnotations.
		 * EnhancedPlusPlusDependenciesAnnotation.class) != null) { String[]
		 * lines = sentence.get(SemanticGraphCoreAnnotations.
		 * EnhancedPlusPlusDependenciesAnnotation.class).toList().split(System.
		 * getProperty("line.separator")); for(String line: lines) out.println(
		 * "output: "+line); }
		 * 
		 * this is the parse tree of the current sentence Tree tree =
		 * sentence.get(TreeAnnotation.class); tree.pennPrint(out);
		 * 
		 * this is the Stanford dependency graph of the current sentence
		 * SemanticGraph dependencies =
		 * sentence.get(CollapsedCCProcessedDependenciesAnnotation.class);
		 * dependencies.prettyPrint(); }
		 */

		// This is the coreference link graph
		// Each chain stores a set of mentions that link to each other,
		// along with a method for getting the most representative mention
		// Both sentence and token offsets start at 1!
		// Map<Integer, CorefChain> graph =
		// annotation.get(CorefChainAnnotation.class);

		// for(Map.Entry<Integer, CorefChain> entry : graph.entrySet()){
		// out.println("Key = " + entry.getKey() + ", Value = " +
		// entry.getValue());
		// }

		String firstPos = "";
		if (posList.get(0).equalsIgnoreCase("-LRB-")) {
			for (int i = 1; i < posList.size(); i++) {
				if (posList.get(i).equalsIgnoreCase("-RRB-")) {
					if (i != (posList.size() - 1))
						firstPos = posList.get(i + 1);
					else
						firstPos = posList.get(1);
				}

			}
			if (firstPos == "") {
				firstPos = posList.get(1);
			}
		} else {
			firstPos = posList.get(0);
		}

		if (firstPos.equalsIgnoreCase("VB") || wordList.get(0).equalsIgnoreCase("show")) {
			nlpValues[3] = 1;
		} else
			nlpValues[3] = 0;

		int MDIndex = posList.indexOf("MD");
		if (MDIndex != -1) {
			nlpValues[0] = MDIndex;
		} else
			nlpValues[0] = 0;

		int goodIndex = getGoodIndexFrom(MDIndex + 1);
		if (goodIndex != -1) {
			if (MDIndex == -1)
				nlpValues[4] = 0;
			else
				nlpValues[4] = 1;
		}

		boolean matchMDGOODVB = false;
		boolean matchMDGOODIF = false;

		for (int i = goodIndex + 1; i < wordList.size() && MDIndex != -1 && goodIndex != -1; i++) {
			String tag = posList.get(i);
			if (tag.contains("VB")) {
				matchMDGOODVB = true;
			}
			if (lemaList.get(i).equalsIgnoreCase("if")) {
				matchMDGOODIF = true;
			}

		}

		boolean matchGOODIF = false;
		int firstGood = getGoodIndexFrom(0);
		if (firstGood != -1) {
			for (int i = firstGood + 1; i < wordList.size(); i++) {
				if (lemaList.get(i).equalsIgnoreCase("if")) {
					matchGOODIF = true;
				}
			}
		}

		if (matchMDGOODVB)
			nlpValues[10] = 1;
		else
			nlpValues[10] = 0;

		if (matchMDGOODIF)
			nlpValues[13] = 1;
		else
			nlpValues[13] = 0;

		if (matchGOODIF)
			nlpValues[14] = 1;
		else
			nlpValues[14] = 0;

		nlpValues[8] = wordList.size();

		nlpValues[19] = getNumValidWords();

		ArrayList<Integer> indexList = FeatureUtility.getAllMatchedIndex(rawWords, FeatureUtility.GOOD);

		nlpValues[11] = 0;

		if (indexList.isEmpty() || indexList.size() == 0) {
			nlpValues[11] = 0;
		} else {
			for (Integer index : indexList) {
				String goodWord = rawWords.get(index).word();
				int copIndex = getCOPDependentValue(basicDependenciesNode, goodWord);

				if (copIndex > 0 && posList.get(copIndex - 1).equals("VBD")) {
					nlpValues[11] = 1;
					break;
				}
			}
		}

		int copIndex = getCOPDependentValue(basicDependenciesNode, "");
		
		String copWord;
		if(copIndex >0){
			copWord = lemaList.get(copIndex-1);
		}
		

		
		if (copIndex > 0 && posList.get(copIndex - 1).equals("VBD")) {
			if (DEBUG)
				System.out.println("past tense!");
			nlpValues[16] = 1;

		}

		nlpValues[12] = getNumValidVerbs();

		if (DEBUG) {
			System.out.println("\nAnalyzing text: " + text);
			System.out.println(wordList);
			System.out.println(posList);
			System.out.println(lemaList);
			out.println("\n==============Pipeline Pretty Print==============");
			pipeline.prettyPrint(annotation, out);
			System.out.println("\n==============json print==============");
			System.out.print(sw.toString());
			// out.println("\n======================================");
			// System.out.println("\n=================nlpValues[]
			// output======================");

			String valNames[] = { "[0]containMD", "[1]containWants", "[2]containShouldCan", "[3]startWithVB",
					"[4]matchMDGOOD", "[5]containNEG", "[6]question", "[7]numTrunk", "[8]numToken", "[9]containEXP",
					"[10]matchMDGOODVB", "[11]matchVBDGOOD", "[12]containValidVerbs", "[13]matchMDGOODIF",
					"[14]matchGOODIF}", "[15]matchSYSNEED", "[16]isPastTense", "[17]sentimentScore",
					"[18]sentimentProbability", "[19]numValidWords" };

			String printPara = "";
			for (int i = 0; i < nlpValues.length; i++) {
				printPara = valNames[i] + " = %.2f\n";
				System.out.printf(printPara, nlpValues[i]);
			}

		}

		return nlpValues;
	}

	private double[] getProbabilityMatrix(SimpleMatrix mat) {
		double[] probs	= new double[SentimentClass.values().length];
		{
			for (int i = 0; i < SentimentClass.values().length; ++i) {
				probs[i] = mat.get(i);
				if (DEBUG) {
					// System.out.println("score = "+i+", probability =
					// "+probs[i]);
				}
			}
		}
		return probs;
	}

}
