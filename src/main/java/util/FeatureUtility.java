package main.java.util;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import main.java.bean.FeatureRequest;
import main.java.core.StanfordCoreNlpDemo;
import edu.stanford.nlp.ling.CoreLabel;
import weka.core.Attribute;
import weka.core.Instance;
import weka.core.Instances;

public class FeatureUtility {

	static String excludeList = "given praise Closed_time closed_time index link open_time Open_time requestLink target Target targetLink title Title url Url";
	static String exportDir = "resource//train";
	static String outputPath = "resource//tag_data_";
	public static String excludeTags[] = { "_time" };
	public static final String QUESTION[] = { "why" };
	public static final String[] SYSTEM_NAMES = {"phpmyadmin","pma","mopidy","mpd"};

	public static final String WANT_MD[] = { "should", "can" };
	public static final String WANTS[] = { "sugg", "propose", "consider", "want", "would like", "\'d like", "��d like",
			"what about", "how about", "new" };
	public static final String GOOD[] = { "help", "helpful", "useful", "great", "nice", "good", "appreciate", "greatly",
			"appreciated", "appropriate", "better", "convenient", "cool", "worth" ,"make sense","interesting","a great deal",
			"a good deal","neat","safer","accurate","simplify","speed","sense","enhanced"};//,"possible"
	public static final String EXPLAINATION[] = { "why", "hint", "mean", "has to", "have to", "only", "same", "F.e.",
			"already", "etc" }; // like

	public static final String USELESSVERB[] = { "be", "wander", "wonder", "thank","appreciate" };
	public static final String[] USELESS = { "hi", "hello", "thank", "regards", "thanks" };

	public static final String SMART_STOP_WORDS[] = { " ", "a", "able", "about", "above", "according", "accordingly",
			"across", "actually", "after", "afterwards", "again", "against", "all", "allow", "allows", "almost",
			"alone", "along", "already", "also", "although", "always", "am", "among", "amongst", "an", "and", "another",
			"any", "anybody", "anyhow", "anyone", "anything", "anyway", "anyways", "anywhere", "apart", "appear",
			"appreciate", "appropriate", "are", "around", "as", "aside", "ask", "asking", "associated", "at",
			"available", "away", "awfully", "b", "be", "became", "because", "become", "becomes", "becoming", "been",
			"before", "beforehand", "behind", "being", "believe", "below", "beside", "besides", "best", "better",
			"between", "beyond", "bit","both", "brief", "but", "by", "c", "came", "can", "cannot", "cant", "cause", "causes",
			"certain", "certainly", "changes", "clearly", "co", "com", "come", "comes", "concerning", "consequently",
			"consider", "considering", "contain", "containing", "contains", "corresponding", "could", "course",
			"currently", "d", "definitely", "described", "despite", "did", "different", "do", "does", "doing", "done",
			"down", "downwards", "during", "e", "each", "edu", "eg", "eight", "either", "else", "elsewhere", "enough",
			"entirely", "especially", "et", "etc", "even", "ever", "every", "everybody", "everyone", "everything",
			"everywhere", "ex", "exactly", "example", "except", "f", "far", "few", "fifth", "first", "five", "followed",
			"following", "follows", "for", "former", "formerly", "forth", "four", "from", "further", "furthermore", "g",
			"get", "gets", "getting", "given", "gives", "go", "goes", "going", "gone", "got", "gotten", "greetings",
			"h", "had", "happens", "hardly", "has", "have", "having", "he", "hello", "help", "hence", "her", "here",
			"hereafter", "hereby", "herein", "hereupon", "hers", "herself", "hi", "him", "himself", "his", "hither",
			"hopefully", "how", "howbeit", "however", "i", "ie", "if", "ignored", "immediate", "in", "inasmuch", "inc",
			"indeed", "indicate", "indicated", "indicates", "inner", "insofar", "instead", "into", "inward", "is", "it",
			"its", "itself", "j", "just", "k", "keep", "keeps", "kept", "know", "knows", "known", "l", "last", "lately",
			"later", "latter", "latterly", "least", "less", "lest", "let","lets", "like", "liked", "likely", "little", "look",
			"looking", "looks", "ltd", "m", "mainly", "many", "may", "maybe", "me", "mean", "meanwhile", "merely",
			"might", "more", "moreover", "most", "mostly", "much", "must", "my", "myself", "n", "name", "namely", "nd",
			"near", "nearly", "necessary", "need", "needs", "neither", "never", "nevertheless", "new", "next", "nine",
			"no", "nobody", "non", "none", "noone", "nor", "normally", "not", "nothing", "novel", "now", "nowhere", "o",
			"obviously", "of", "off", "often", "oh", "ok", "okay", "old", "on", "once", "one", "ones", "only", "onto",
			"or", "other", "others", "otherwise", "ought", "our", "ours", "ourselves", "out", "outside", "over",
			"overall", "own", "p", "particular", "particularly", "per", "perhaps", "placed", "please", "plus",
			"possible", "presumably", "probably", "provides", "q", "que", "quite", "qv", "r", "rather", "rd", "re",
			"really", "reasonably", "regarding", "regardless", "regards", "relatively", "respectively", "right", "s",
			"said",  "saw", "say", "saying", "says", "second", "secondly", "see", "seeing", "seem", "seemed",
			"seeming", "seems", "seen", "self", "selves", "sensible", "sent", "serious", "seriously", "seven",
			"several", "shall", "she", "should", "since", "six", "so", "some", "somebody", "somehow", "someone",
			"something", "sometime", "sometimes", "somewhat", "somewhere", "soon", "sorry", "specified", "specify",
			"specifying", "still", "sub", "such", "sup", "sure", "t", "take", "taken", "tell", "tends", "th", "than",
			"thank", "thanks", "thanx", "that", "thats", "the", "their", "theirs", "them", "themselves", "then",
			"thence", "there", "thereafter", "thereby", "therefore", "therein", "theres", "thereupon", "these", "they",
			"think", "third", "this", "thorough", "thoroughly", "those", "though", "three", "through", "throughout",
			"thru", "thus", "to", "together", "too", "took", "toward", "towards", "tried", "tries", "truly", "try",
			"trying", "twice", "two", "u", "un", "under", "unfortunately", "unless", "unlikely", "until", "unto", "up",
			"upon", "us", "use", "used", "useful", "uses", "using", "usually", "uucp", "v", "value", "various", "very",
			"via", "viz", "vs", "w", "want", "wants", "was", "way", "we", "welcome", "well", "went", "were", "what",
			"whatever", "when", "whence", "whenever", "where", "whereafter", "whereas", "whereby", "wherein",
			"whereupon", "wherever", "whether", "which", "while", "whither", "who", "whoever", "whole", "whom", "whose",
			"why", "will", "willing", "wish", "with", "within", "without", "wonder", "would", "would", "x", "y", "yes",
			"yet", "you", "your", "yours", "yourself", "yourselves", "z", "zero",
			"miss","contributing","?",".",":","\"",")","feature",":)","-RRB-","???","'m","willing","write","idea",
			",","provide","details" ,"greatly","comments","wishlist","propose","item","requirement","firmed","up","next",
			"couple","weeks","french","user","fan","used","before","don't","support","so","try","powerful","suggestion","any",
			"wandering","feature","available","actually","request","more","than","bug","exists","other","form","missed","while","going","through",
			"sources","code","enclosed","zip","time","change","write","attach","I'll","implementation","changes","release","patch","report","second",":",")",
			"happy","implement","these","them","can","do","fix","if","we're","agreed","care","taken","don't","feel","free","add","further","benefits",
			"drawbacks","comments","good","luck"}; //REMOVE same

	public static String[] splitByEqual(String line, int lineNumber, boolean exclude, boolean export)
			throws IOException {

		String[] results = line.split("=");
		String[] returns;

		String tag = results[0].trim();

		if (tag.contains(" ") || tag.length() > 15 || results.length < 2)
			return null;

		String content = line.substring(line.indexOf("=") + 1).trim();
		returns = new String[] { tag, content };

		if (exclude && excludeList.contains(tag))
			return null;

		if (export) {
			String exportFileDir = exportDir + "//" + tag;
			File exportDir = new File(exportFileDir);
			if (!exportDir.exists())
				exportDir.mkdirs();

			FileWriter writer = new FileWriter(exportFileDir + "//" + Integer.toString(lineNumber) + ".txt");
			writer.write(content);
			System.out.println("line " + lineNumber + ": " + content);
			writer.close();
		}

		return returns;
	}

	public static boolean checkContains(String text, String[] list, boolean checkEqual) {
		
		List<CoreLabel> rawWords = StanfordCoreNlpDemo.getRawWords(text);
		for(CoreLabel item : rawWords){
			String word = item.word();
			
			for (String target : list) {
				
				if(checkEqual){
					if (word.equalsIgnoreCase(target))
						return true;
				}else{
					if (word.toLowerCase().contains(target.toLowerCase()))
						return true;
				}
				
			}
		}
		
		return false;
		
	}

	public static boolean checkContains(String text, String[] list) {

		List<CoreLabel> rawWords = StanfordCoreNlpDemo.getRawWords(text);
		for (CoreLabel item : rawWords) {
			String word = item.word();

			for (String target : list) {
				if (word.equalsIgnoreCase(target))
					return true;
			}
		}

		return false;
	}
	
	public static boolean isContain(String text, String[] list) {

			for (String target : list) {
				if (text.toLowerCase().contains(target.toLowerCase()))
					return true;
			}

		return false;
	}
	
	public static boolean isSysName(String name){
		return isContain(name, SYSTEM_NAMES,true);
	}
	
	public static boolean isContain(String text, String[] list, boolean equal) {

		for (String target : list) {
			if(equal){
				if (text.toLowerCase().equalsIgnoreCase(target))
					return true;
			}else{
				if (text.toLowerCase().contains(target.toLowerCase()))
					return true;
				}
		}

	return false;
}

	public static void exportInstancesToFile(Instances dataRaw, String filename) throws IOException {
		FileWriter fw = new FileWriter(filename);
		BufferedWriter bw = new BufferedWriter(fw);
		bw.write(dataRaw.toString());
		bw.close();
		fw.close();
	}
	
	public static void exportIFeatureRequestsToFile(ArrayList<FeatureRequest> featureRequestList, String filename) throws IOException {
		FileWriter fw = new FileWriter(filename);
		BufferedWriter bw = new BufferedWriter(fw);
		for(FeatureRequest fr : featureRequestList){
			bw.write("=============FR print===================\n");
			bw.write(fr.toString());
			bw.write("\n");
		}
		
		bw.close();
		fw.close();
	}

	public static ArrayList<Integer> getAllMatchedIndex(List<CoreLabel> rawWords, String[] good) {
		ArrayList<Integer> results = new ArrayList<Integer>();

		for (int i = 0; i < rawWords.size(); i++) {
			String word = rawWords.get(i).word();
			if (FeatureUtility.checkContains(word, good)) {
				results.add(i);
			}
		}

		return results;
	}

	public static int getFirstIndex(List<CoreLabel> rawWords, String[] pattern3) {
		int firstIndex = -1;

		for (int i = 0; i < rawWords.size(); i++) {
			String word = rawWords.get(i).word();
			if (FeatureUtility.isContain(word, pattern3)) {
				firstIndex = i;
			}
		}

		return firstIndex;
	}

	public static PrintWriter getPrintWriter(String outputFile) {
		PrintWriter printer = null;
		try {
			printer = new PrintWriter(new FileOutputStream(new File(outputFile), false));
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}

		return printer;
	}

	/**
	 * 
	 * @param data
	 * @param option
	 *            SENTENCE, INSTANCE
	 * @throws IOException
	 */
	public static void printInstancesByTag(Instances data, String option) throws IOException {

		Attribute tag = data.attribute("tag");
		int tagSize = tag.numValues();
		String[] tagNames = new String[tagSize];
		String[] outputfile = new String[tagSize];
		PrintWriter[] output = new PrintWriter[tagSize];
		data.setClassIndex(tag.index());

		for (int i = 0; i < tagSize; i++) {
			tagNames[i] = tag.value(i);
			outputfile[i] = outputPath + tagNames[i] + ".txt";
			output[i] = getPrintWriter(outputfile[i]);
		}

		ListIterator<Instance> list = data.listIterator();

		while (list.hasNext()) {
			Instance item = list.next();
			int index = (int) item.classValue();
			if (option.equals("SENTENCE"))
				output[index].println(item.stringValue(0));
			if (option.equals("INSTANCE"))
				output[index].println(item);
		}

		for (int i = 0; i < tagSize; i++) {
			output[i].close();
			System.out.println("Write to file: " + outputfile[i]);
		}

	}

	public static boolean noSemicolonBeforePattern(String content, String[] pattern3) {
		boolean result = true;

		if (FeatureUtility.isContain(content, pattern3,true)) {
			List<CoreLabel> rawWords = StanfordCoreNlpDemo.getRawWords(content);
			int firstIndex = FeatureUtility.getFirstIndex(rawWords, pattern3);

			for (int i = 0; i < firstIndex; i++) {
				String word = rawWords.get(i).word();
				if (word.equals(":")) {
					result = false;
					break;
				}
			}
		} else {
			result = false;
		}
		return result;
	}

	public static String[] getTokens(String content) {
		List<CoreLabel> words = StanfordCoreNlpDemo.getRawWords(content);
		String[] results = new String[words.size()];
		for(int i = 0; i < words.size(); i++)
			results[i] = words.get(i).word();
		return results;
	}
	
	public static boolean matchFeature(String content){
		boolean result = false;
		
		Pattern pattern1 = Pattern.compile(".*would be[^,.;?\"'����]*feature.*");
		Pattern pattern2 = Pattern.compile(".*feature[^,.;?\"'����]*would be.*");
		Pattern pattern3 = Pattern.compile(".*this is[^,.;?\"'����]*feature.*");
		Matcher matcher1 = pattern1.matcher(content);
		Matcher matcher2 = pattern2.matcher(content);
		Matcher matcher3 = pattern3.matcher(content);
		if(matcher1.matches()||matcher2.matches()||matcher3.matches())
			result = true;
		
		return result;
		 
	}
	
	public static boolean matchShouldBePossible(String content){
		Pattern pattern = Pattern.compile(".*should[^,.;?\"'����]*be[^,.;?\"'����]*possible.*");
		Matcher matcher = pattern.matcher(content);
		if(matcher.matches())
			return true;
		
		return false;
		
	}
	
	public static boolean matchNOTONLY(String content){
		Pattern pattern = Pattern.compile(".*should[^,.;?\"'����]*not only[^,.;?\"'����]*but also.*");
		Matcher matcher = pattern.matcher(content);
		if(matcher.matches())
			return true;
		
		return false;
		
	}
	
	public static void main(String args[]){
		 Pattern pattern = Pattern.compile(".*feature.*");
		 Matcher matcher = pattern.matcher("\"find and replace feature\"");
		 System.out.println(matcher.matches());
		 
		 /*pattern = Pattern.compile(".*would be[^,.;?\"'����]*feature.*");
		 matcher = pattern.matcher("would be a great feature to have in that regard");
		 System.out.println(matcher.matches());
		 
		 pattern = Pattern.compile(".*feature[^,.;?\"'����]*would be.*");
		 matcher = pattern.matcher("A really useful feature would be a list where one can simply save SQL");
		 System.out.println(matcher.matches());*/
		 
		 pattern = Pattern.compile(".*should[^,.;?\"'����]*be[^,.;?\"'����]*possible.*");
		 matcher = pattern.matcher("This should be possible for <ant> as well and allow simplify forked testing");
		 System.out.println(matcher.matches());
		 
		 
		 pattern = Pattern.compile(".*should[^,.;?\"'����]*not only[^,.;?\"'����]*but also.*");
		 
		 //pattern = Pattern.compile(".*either[^,.;?\"'����]*but also.*");
		 matcher = pattern.matcher("SoftExceptions should print not only their trace but also that of the wrapped throwable.");
		 System.out.println(matcher.matches());
		 
		 
		 //String content = "it would be nice if the compiler emitted an error, since the two situations can be confusingly similar:example = <CODE>";
		 //String[] tokens = getTokens(content);
		 //System.out.println(tokens);
		 //index = replacement.index //= 22
		 //tokens[index] == "<CODE>";
		 
		  /*String content = "So many people have asked how to write a declare warning / error to detect empty catch blocks, that this is clearly a desirable feature";
	      System.out.println(matchFeature(content)); 
		  String[] tokens = getTokens(content);
	        for(String token : tokens){
	        	String target = getTokens("<For example0>")[0];
	        	System.out.println("target = "+target);
	            if(token.equals(target)){
	                System.out.println("ƥ��:-->"+token);
	            }
	        }*/

	}
	

}
