package main.java.core;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;

import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.util.EditDistance;
import main.java.bean.FeatureRequestOL;
import main.java.util.FeatureUtility;
import weka.core.Attribute;
import weka.core.Instance;
import weka.core.Instances;

public class RequestAnalyzer {
	static String modelOutputDir = "resource//models//";
	static PrintWriter out = null;
	static String outputFile = "resource//precision_results_non-text.txt";
	static boolean printResult = false;
	static String[] rawAttributeNames;
	public static String[] tagNames = new String[] { "explanation", "want", "useless" };
	static int tagSize = 3;

	// TODO
	public static boolean classifyAsUseless(String content, double verbsAllInvalid) {

		if (verbsAllInvalid == 1)
			return true;

		if (FeatureUtility.checkContains(content, FeatureUtility.USELESS))
			return true;

		return false;
	}

	// TODO
	public static boolean classifyAsWant(Instance item, Instances data) {

		String content = item.stringValue(0);

		double isRealFirst = item.value(data.attribute("isRealFirst"));
		double matchMDGOODVB = item.value(data.attribute("matchMDGOODVB"));

		double startWithVB = item.value(data.attribute("startWithVB"));
		double question = item.value(data.attribute("question"));
		double matchMDGOOD = item.value(data.attribute("matchMDGOOD"));

		// "want to","can"
		String[] pattern = new String[] { "would like", "\'d like", "’d like", "would love to", "\'d love to",
				"’d love to", "appreciate", "suggest", "propose", "should", "add support", "pma may", "may want",
				"be able to", "need is", "phpmyadmin may", "would want to", "we need", "I need", "to support" };

		String[] pattern2 = new String[] { "how about", "what about" };

		String[] pattern3 = new String[] { "idea", "request", }; // "feature","option","consider",

		if (question == 1) {
			if (FeatureUtility.isContain(content, pattern2))
				return true;

			else
				return false;
		}

		if (FeatureUtility.isContain(content, pattern) || matchMDGOODVB == 1) {
			return true;
		}

		if (FeatureUtility.noSemicolonBeforePattern(content, pattern3)) {
			return true;
		}

		if (isRealFirst == 1 && startWithVB == 1)
			return true;

		if (classifyAsExp(item, data))
			return false;

		return false;
	}

	private static void createPrintWriter(String outputFile) {
		try {
			out = new PrintWriter(new FileOutputStream(new File(outputFile), true), true);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
	}

	public static void predictTag(Instances data) {

		// createPrintWriter("resource//precision_results_non-text.txt");

		Attribute tag = data.attribute("tag");
		data.setClassIndex(tag.index());

		ListIterator<Instance> list = data.listIterator();

		System.out.println("Negitive predictions: ");
		int count = 0;
		int[][] matrix = new int[3][3];
		while (list.hasNext()) {
			Instance item = list.next();
			int index = (int) item.classValue();
			String sentence = item.stringValue(0);

			int predict = predictTagIndex(item, data);

			if (index == predict) {
				matrix[index][index]++;
				continue;
			}

			if (index == 1) {
				count++;
				matrix[index][predict]++;
				// if(predict == 2)
				System.out.println(count + " - WANT - predict to be " + tagNames[predict] + ":" + sentence);
			}

			if (index == 0) {
				matrix[index][predict]++;
				count++;
				// if(predict==2)
				System.out.println(count + " - EXP - predict to be " + tagNames[predict] + ":" + sentence);
			}
			if (index == 2) {
				matrix[index][predict]++;
				count++;
				// if(predict==1)
				System.out.println(count + " - USELESS - predict to be " + tagNames[predict] + ":" + sentence);
			}
		}

		System.out.println(matrix);

		double[][] evaluation = new double[3][2];
		for (int i = 0; i < 3; i++) {
			evaluation[i][0] = matrix[i][i] / (double) (matrix[0][i] + matrix[1][i] + matrix[2][i]);
			evaluation[i][1] = matrix[i][i] / (double) (matrix[i][0] + matrix[i][1] + matrix[i][2]);
			System.out.printf("Class=%s precision=%.2f recall=%.2f\n", tagNames[i], evaluation[i][0], evaluation[i][1]);
		}

	}
	
	
	public static int predictTagIndex(FeatureRequestOL request, int index){
		String originContent =  request.getSentence(index);
		String subject = request.getSubjects(index);
		String action = request.getActions(index);

		double isRealFirst = request.getIsRealFirst(index);
		double matchMDGOODVB = request.getMatchMDGOODVB(index);
		
		double startWithVB = request.getStartWithVB(index);
		double question = request.getQuestion(index);
		double matchVBDGOOD = request.getMatchVBDGOODB(index);
		double numValidWords = request.getNumValidWords(index);
		double matchMDGOOD = request.getMatchMDGOOD(index);
		double containNEG = request.getContainEXP(index);
		double similarityToTitle = request.getSimilairity(index);
		double matchMDGOODIF = request.getMatchGOODIF(index);
		double matchGOODIF = request.getMatchGOODIF(index);
		double matchSYSNEED = request.getMatchSYSNEED(index);
		double isPastTense = request.getIsPastTense(index);
		double sentimentScore = request.getSentimentScore(index);
		double sentimentProbability = request.getSentimentProbability(index);
		double numValidVerbs = request.getNumValidVerbs(index);

		// "want to","can"
		String[] pattern = new String[] { "would like", "\'d like", "’d like", "would love to", "\'d love to",
				"’d love to", "appreciate", "suggest", "propose", "add support", "pma may", "phpmyadmin may",
				"may wish", "able to", "wish for", "there should be", "may want", "we need", "I need",
				"I would want to", "we want" };// "should","require",

		String[] pattern2 = new String[] { "how about", "what about" };

		String[] pattern3 = new String[] { "idea", };// "feature","request","consider",
														// "option",

		String[] expPattern = new String[] { "have to", "unfortunately", "possible", "suggestion", "only" };

		if (originContent.contains("This suite can help us measure performance and memory hotspots in 1.2 development"))
			System.out.println();

		String content = originContent.replaceAll("[(].*[)]", "");
		// content = originContent.replaceAll("[\"].*[\"]", "");
		// content = originContent.replaceAll("['].*[']", "");

		int result = 0;

		if (numValidWords == 0 || numValidVerbs == 0) {
			result = 2;
			if (sentimentScore < 2 && sentimentProbability > 0.6)
				result = 0;
		}

		if (FeatureUtility.checkContains(content, FeatureUtility.USELESS))
			result = 2;
		else if (matchMDGOODVB == 1 && isRealFirst == 1) {
			result = 1;
		} else if (matchVBDGOOD == 1 || containNEG == 1 || content.toLowerCase().startsWith("in addition")
				|| content.toLowerCase().startsWith("also ") || content.toLowerCase().startsWith("so ")
				|| content.toLowerCase().startsWith("perhaps") || content.toLowerCase().startsWith("by default")
				|| content.toLowerCase().startsWith("maybe") || content.toLowerCase().contains("for example")
				|| content.toLowerCase().startsWith("given") || content.toLowerCase().startsWith("unfortunately")
				|| content.toLowerCase().startsWith("similarly") || content.toLowerCase().contains("why")) {

			result = 0;
		} else if (question == 1 && FeatureUtility.isContain(content, pattern2)) {
			result = 1;

		} else if (question == 1 && content.toLowerCase().contains("reason")) {
			result = 0;
		} else if ((FeatureUtility.isContain(content, pattern) || matchMDGOODVB == 1) && numValidWords > 1) {
			result = 1;
		} else if (FeatureUtility.isContain(content, expPattern) && numValidWords > 1) {
			result = 0;
		} else if (matchMDGOOD == 1 || containNEG == 1) {
			result = 0;
		} else if (content.toLowerCase().contains("should") && question == 0)
			result = 1;
		else if (numValidVerbs == 0 && matchMDGOOD == 0 && containNEG == 0) {
			result = 2;
			if (numValidWords >= 2)
				result = 0;

			if (sentimentScore < 2 && sentimentProbability > 0.6)
				result = 0;
		} else if (numValidWords < 2) {
			result = 2;
			if (sentimentScore < 2 && sentimentProbability > 0.6)
				result = 0;
		} else if (FeatureUtility.noSemicolonBeforePattern(content, pattern3)) {
			result = 1;
		} else if (isRealFirst == 1 && startWithVB == 1) {
			result = 1;
		}

		if (result == 1) {
			if (similarityToTitle <= 0.1) {
				result = 0;
			}
		}

		if (content.toLowerCase().contains("there should be") || content.toLowerCase().contains("would like to")
				|| content.toLowerCase().contains("i’d like") || content.toLowerCase().contains("the request is:")) {
			if (!content.toLowerCase().contains("would like to work"))
				result = 1;
		}

		if (content.toLowerCase().startsWith("however") && isRealFirst == 1 && similarityToTitle <= 0.3)
			result = 0;

		if (content.toLowerCase().contains("would help") && matchMDGOODIF == 0)
			result = 0;

		if (content.toLowerCase().contains("would like to know why") || content.toLowerCase().contains("because")
				|| content.toLowerCase().contains("since") || content.toLowerCase().startsWith("but")
				|| content.toLowerCase().startsWith("maybe") || content.toLowerCase().startsWith("like"))
			result = 0;

		// if(matchMDGOODVB == 1) result = 0;

		// if ((FeatureUtility.isContain(content, pattern) || matchMDGOODVB ==
		// 1) && validWords>1 ) { result = 1;}

		if (content.toLowerCase().contains("suggestion") && question == 0)
			result = 0;

		if (matchMDGOODIF == 1)
			result = 1;

		if (isRealFirst == 1 && startWithVB == 1) {
			result = 1;
		}

		if (isRealFirst == 1 && (content.toLowerCase().startsWith("goal")))
			result = 1;

		if (isRealFirst == 1 && (content.toLowerCase().contains("we could")))
			result = 1;

		if (content.toLowerCase().contains("we should"))
			result = 1;

		if (content.toLowerCase().contains("whether we should")
				|| content.toLowerCase().contains("if you think we should"))
			result = 0;

		if (matchGOODIF == 1 && content.toLowerCase().contains("i think"))
			result = 1;

		if (FeatureUtility.matchFeature(content) || matchSYSNEED == 1)
			result = 1;

		if (matchMDGOODVB == 1 && isRealFirst == 1) {
			result = 1;
		}

		if (content.toLowerCase().contains("look forward") || content.toLowerCase().contains("would like to work")
				|| content.toLowerCase().contains("willing to contribute")
				|| content.toLowerCase().contains("please give your suggestion"))
			result = 2;

		if ((content.toLowerCase().contains("should") || (content.toLowerCase().startsWith("an option")))
				&& isRealFirst == 1 && !FeatureUtility.isContain(content, expPattern))
			result = 1;

		if (content.contains("<link-http>") || content.contains("<issue-link>") || content.contains("<COMMAND>")
				|| content.contains("<CODE>") || content.contains("<list>") || content.contains("<html-link>")
				|| content.contains("<PATH>") || content.contains("<FILE>") || content.contains("<http-link>")
				|| content.contains("<FILE-SYS>") || content.contains("<FILE-XML>") || content.contains("web-page-link")
				|| content.contains("<http>") || content.contains("LINK-HTTP") || content.contains("<file-path>"))
			result = 0;

		boolean exp1 = content.toLowerCase().contains("current") || content.toLowerCase().startsWith("to do this")
				|| content.toLowerCase().startsWith("possibly") || content.toLowerCase().contains("something like")
				|| content.toLowerCase().contains("just like") || content.toLowerCase().contains("benefits:")
				|| content.toLowerCase().endsWith(":") || content.toLowerCase().contains("i ever use")
				|| content.toLowerCase().startsWith("consequently") || content.toLowerCase().contains("limitation");
		if (exp1) {
			result = 0;

			if (matchMDGOODVB == 1 && isRealFirst == 1) {
				result = 1;
			}

		}

		boolean want1 = content.toLowerCase().contains("add") && content.toLowerCase().contains("support")
				|| content.toLowerCase().contains("needs to support") || content.toLowerCase().startsWith("let's")
				|| content.toLowerCase().contains("i'm asking for") || content.toLowerCase().contains("asked for")
				|| content.toLowerCase().contains("basic idea") || content.toLowerCase().contains("we need");
		if (want1) {
			result = 1;

		}

		if (content.toLowerCase().contains("really") && question == 1)
			result = 0;

		if (isPastTense == 1)
			result = 0;

		if ((content.toLowerCase().startsWith("is there")) && question == 1)
			result = 1;

		if ((content.toLowerCase().contains("i want")) && isRealFirst == 1)
			result = 1;

		if (content.toLowerCase().startsWith("there is a need") || content.toLowerCase().startsWith("would it")
				|| content.toLowerCase().startsWith("i needed this") || content.toLowerCase().startsWith("i will")
				|| content.toLowerCase().contains("i'm considering")
				|| content.toLowerCase().contains("i am considering") || content.toLowerCase().contains("i am planning")
				|| content.toLowerCase().contains("i'm planning") || content.toLowerCase().contains("why don't we")
				|| content.toLowerCase().contains("looking for a feature")
				|| content.toLowerCase().contains("need to be supported"))
			result = 1;

		if (content.toLowerCase().contains("to do this"))
			result = 0;

		if (action != null && action.length() != 0) {
			if (action.equalsIgnoreCase("mean"))
				result = 0;
			if (action.equalsIgnoreCase("propose"))
				result = 1;

			if (action.equalsIgnoreCase("support")) {
				if (subject != null && subject.length() != 0) {
					if (subject.equals("we"))
						result = 1;
				}
			}

			if (subject != null && subject.length() != 0) {
				if (subject.toLowerCase().equals("proposal"))
					result = 1;
			}

		}

		if (content.toLowerCase().contains("must")) {
			boolean prpmust = content.toLowerCase().contains("you must") || content.toLowerCase().contains("it must")
					|| content.toLowerCase().contains("that must") || content.toLowerCase().contains("which must")
					|| content.toLowerCase().contains("i must");

			if (prpmust)
				result = 0;
			else
				result = 1;
		}

		if (content.toLowerCase().startsWith("unfortunately") || content.toLowerCase().startsWith("actually"))
			result = 0;

		if (FeatureUtility.matchShouldBePossible(content))
			result = 0;
		
		if(FeatureUtility.matchNOTONLY(content))
			result = 1;

		// if(sentimentScore<2)
		// result = 0;

		return result;
	}

	private static int predictTagIndex(Instance item, Instances data) {

		String originContent = item.stringValue(0);
		String subject = item.stringValue(data.attribute("subjects"));
		String action = item.stringValue(data.attribute("actions"));

		double isRealFirst = item.value(data.attribute("isRealFirst"));
		double matchMDGOODVB = item.value(data.attribute("matchMDGOODVB"));
		double startWithVB = item.value(data.attribute("startWithVB"));
		double question = item.value(data.attribute("question"));
		double matchVBDGOOD = item.value(data.attribute("matchVBDGOOD"));
		double numValidWords = item.value(data.attribute("numValidWords"));
		double matchMDGOOD = item.value(data.attribute("matchMDGOOD"));
		double containNEG = item.value(data.attribute("containNEG"));
		double similarityToTitle = item.value(data.attribute("similarityToTitle"));
		double matchMDGOODIF = item.value(data.attribute("matchMDGOODIF"));
		double matchGOODIF = item.value(data.attribute("matchGOODIF"));
		double matchSYSNEED = item.value(data.attribute("matchSYSNEED"));
		double isPastTense = item.value(data.attribute("isPastTense"));
		double sentimentScore = item.value(data.attribute("sentimentScore"));
		double sentimentProbability = item.value(data.attribute("sentimentProbability"));
		double numValidVerbs = item.value(data.attribute("numValidVerbs"));

		// "want to","can"
		String[] pattern = new String[] { "would like", "\'d like", "’d like", "would love to", "\'d love to",
				"’d love to", "appreciate", "suggest", "propose", "add support", "pma may", "phpmyadmin may",
				"may wish", "able to", "wish for", "there should be", "may want", "we need", "I need",
				"I would want to", "we want" };// "should","require",

		String[] pattern2 = new String[] { "how about", "what about" };

		String[] pattern3 = new String[] { "idea", };// "feature","request","consider",
														// "option",

		String[] expPattern = new String[] { "have to", "unfortunately", "possible", "suggestion", "only" };

		if (originContent.contains("This suite can help us measure performance and memory hotspots in 1.2 development"))
			System.out.println();

		String content = originContent.replaceAll("[(].*[)]", "");
		// content = originContent.replaceAll("[\"].*[\"]", "");
		// content = originContent.replaceAll("['].*[']", "");

		int result = 0;

		if (numValidWords == 0 || numValidVerbs == 0) {
			result = 2;
			if (sentimentScore < 2 && sentimentProbability > 0.6)
				result = 0;
		}

		if (FeatureUtility.checkContains(content, FeatureUtility.USELESS))
			result = 2;
		else if (matchMDGOODVB == 1 && isRealFirst == 1) {
			result = 1;
		} else if (matchVBDGOOD == 1 || containNEG == 1 || content.toLowerCase().startsWith("in addition")
				|| content.toLowerCase().startsWith("also ") || content.toLowerCase().startsWith("so ")
				|| content.toLowerCase().startsWith("perhaps") || content.toLowerCase().startsWith("by default")
				|| content.toLowerCase().startsWith("maybe") || content.toLowerCase().contains("for example")
				|| content.toLowerCase().startsWith("given") || content.toLowerCase().startsWith("unfortunately")
				|| content.toLowerCase().startsWith("similarly") || content.toLowerCase().contains("why")) {

			result = 0;
		} else if (question == 1 && FeatureUtility.isContain(content, pattern2)) {
			result = 1;

		} else if (question == 1 && content.toLowerCase().contains("reason")) {
			result = 0;
		} else if ((FeatureUtility.isContain(content, pattern) || matchMDGOODVB == 1) && numValidWords > 1) {
			result = 1;
		} else if (FeatureUtility.isContain(content, expPattern) && numValidWords > 1) {
			result = 0;
		} else if (matchMDGOOD == 1 || containNEG == 1) {
			result = 0;
		} else if (content.toLowerCase().contains("should") && question == 0)
			result = 1;
		else if (numValidVerbs == 0 && matchMDGOOD == 0 && containNEG == 0) {
			result = 2;
			if (numValidWords >= 2)
				result = 0;

			if (sentimentScore < 2 && sentimentProbability > 0.6)
				result = 0;
		} else if (numValidWords < 2) {
			result = 2;
			if (sentimentScore < 2 && sentimentProbability > 0.6)
				result = 0;
		} else if (FeatureUtility.noSemicolonBeforePattern(content, pattern3)) {
			result = 1;
		} else if (isRealFirst == 1 && startWithVB == 1) {
			result = 1;
		}

		if (result == 1) {
			if (similarityToTitle <= 0.1) {
				result = 0;
			}
		}

		if (content.toLowerCase().contains("there should be") || content.toLowerCase().contains("would like to")
				|| content.toLowerCase().contains("i’d like") || content.toLowerCase().contains("the request is:")) {
			if (!content.toLowerCase().contains("would like to work"))
				result = 1;
		}

		if (content.toLowerCase().startsWith("however") && isRealFirst == 1 && similarityToTitle <= 0.3)
			result = 0;

		if (content.toLowerCase().contains("would help") && matchMDGOODIF == 0)
			result = 0;

		if (content.toLowerCase().contains("would like to know why") || content.toLowerCase().contains("because")
				|| content.toLowerCase().contains("since") || content.toLowerCase().startsWith("but")
				|| content.toLowerCase().startsWith("maybe") || content.toLowerCase().startsWith("like"))
			result = 0;

		// if(matchMDGOODVB == 1) result = 0;

		// if ((FeatureUtility.isContain(content, pattern) || matchMDGOODVB ==
		// 1) && validWords>1 ) { result = 1;}

		if (content.toLowerCase().contains("suggestion") && question == 0)
			result = 0;

		if (matchMDGOODIF == 1)
			result = 1;

		if (isRealFirst == 1 && startWithVB == 1) {
			result = 1;
		}

		if (isRealFirst == 1 && (content.toLowerCase().startsWith("goal")))
			result = 1;

		if (isRealFirst == 1 && (content.toLowerCase().contains("we could")))
			result = 1;

		if (content.toLowerCase().contains("we should"))
			result = 1;

		if (content.toLowerCase().contains("whether we should")
				|| content.toLowerCase().contains("if you think we should"))
			result = 0;

		if (matchGOODIF == 1 && content.toLowerCase().contains("i think"))
			result = 1;

		if (FeatureUtility.matchFeature(content) || matchSYSNEED == 1)
			result = 1;

		if (matchMDGOODVB == 1 && isRealFirst == 1) {
			result = 1;
		}

		if (content.toLowerCase().contains("look forward") || content.toLowerCase().contains("would like to work")
				|| content.toLowerCase().contains("willing to contribute")
				|| content.toLowerCase().contains("please give your suggestion"))
			result = 2;

		if ((content.toLowerCase().contains("should") || (content.toLowerCase().startsWith("an option")))
				&& isRealFirst == 1 && !FeatureUtility.isContain(content, expPattern))
			result = 1;

		if (content.contains("<link-http>") || content.contains("<issue-link>") || content.contains("<COMMAND>")
				|| content.contains("<CODE>") || content.contains("<list>") || content.contains("<html-link>")
				|| content.contains("<PATH>") || content.contains("<FILE>") || content.contains("<http-link>")
				|| content.contains("<FILE-SYS>") || content.contains("<FILE-XML>") || content.contains("web-page-link")
				|| content.contains("<http>") || content.contains("LINK-HTTP") || content.contains("<file-path>"))
			result = 0;

		boolean exp1 = content.toLowerCase().contains("current") || content.toLowerCase().startsWith("to do this")
				|| content.toLowerCase().startsWith("possibly") || content.toLowerCase().contains("something like")
				|| content.toLowerCase().contains("just like") || content.toLowerCase().contains("benefits:")
				|| content.toLowerCase().endsWith(":") || content.toLowerCase().contains("i ever use")
				|| content.toLowerCase().startsWith("consequently") || content.toLowerCase().contains("limitation");
		if (exp1) {
			result = 0;

			if (matchMDGOODVB == 1 && isRealFirst == 1) {
				result = 1;
			}

		}

		boolean want1 = content.toLowerCase().contains("add") && content.toLowerCase().contains("support")
				|| content.toLowerCase().contains("needs to support") || content.toLowerCase().startsWith("let's")
				|| content.toLowerCase().contains("i'm asking for") || content.toLowerCase().contains("asked for")
				|| content.toLowerCase().contains("basic idea") || content.toLowerCase().contains("we need");
		if (want1) {
			result = 1;

		}

		if (content.toLowerCase().contains("really") && question == 1)
			result = 0;

		if (isPastTense == 1)
			result = 0;

		if ((content.toLowerCase().startsWith("is there")) && question == 1)
			result = 1;

		if ((content.toLowerCase().contains("i want")) && isRealFirst == 1)
			result = 1;

		if (content.toLowerCase().startsWith("there is a need") || content.toLowerCase().startsWith("would it")
				|| content.toLowerCase().startsWith("i needed this") || content.toLowerCase().startsWith("i will")
				|| content.toLowerCase().contains("i'm considering")
				|| content.toLowerCase().contains("i am considering") || content.toLowerCase().contains("i am planning")
				|| content.toLowerCase().contains("i'm planning") || content.toLowerCase().contains("why don't we")
				|| content.toLowerCase().contains("looking for a feature")
				|| content.toLowerCase().contains("need to be supported"))
			result = 1;

		if (content.toLowerCase().contains("to do this"))
			result = 0;

		if (action != null && action.length() != 0) {
			if (action.equalsIgnoreCase("mean"))
				result = 0;
			if (action.equalsIgnoreCase("propose"))
				result = 1;

			if (action.equalsIgnoreCase("support")) {
				if (subject != null && subject.length() != 0) {
					if (subject.equals("we"))
						result = 1;
				}
			}

			if (subject != null && subject.length() != 0) {
				if (subject.toLowerCase().equals("proposal"))
					result = 1;
			}

		}

		if (content.toLowerCase().contains("must")) {
			boolean prpmust = content.toLowerCase().contains("you must") || content.toLowerCase().contains("it must")
					|| content.toLowerCase().contains("that must") || content.toLowerCase().contains("which must")
					|| content.toLowerCase().contains("i must");

			if (prpmust)
				result = 0;
			else
				result = 1;
		}

		if (content.toLowerCase().startsWith("unfortunately") || content.toLowerCase().startsWith("actually"))
			result = 0;

		if (FeatureUtility.matchShouldBePossible(content))
			result = 0;
		
		if(FeatureUtility.matchNOTONLY(content))
			result = 1;

		// if(sentimentScore<2)
		// result = 0;

		return result;
	}

	private static boolean classifyAsExp(Instance item, Instances data) {
		String content = item.stringValue(0);
		double matchVBDGOOD = item.value(data.attribute("matchVBDGOOD"));
		String[] expPattern = new String[] { "have to", "unfortunately", "possible", "suggestion", "maybe", "perhaps" };

		if (FeatureUtility.isContain(content, expPattern) || matchVBDGOOD == 1
				|| content.toLowerCase().startsWith("maybe"))
			return true;

		return false;
	}

	public static int minDistance(String word1, String word2) {
		int len1 = word1.length();
		int len2 = word2.length();

		// len1+1, len2+1, because finally return dp[len1][len2]
		int[][] dp = new int[len1 + 1][len2 + 1];

		for (int i = 0; i <= len1; i++) {
			dp[i][0] = i;
		}

		for (int j = 0; j <= len2; j++) {
			dp[0][j] = j;
		}

		// iterate though, and check last char
		for (int i = 0; i < len1; i++) {
			char c1 = word1.charAt(i);
			for (int j = 0; j < len2; j++) {
				char c2 = word2.charAt(j);

				// if last two chars equal
				if (c1 == c2) {
					// update dp value for +1 length
					dp[i + 1][j + 1] = dp[i][j];
				} else {
					int replace = dp[i][j] + 1;
					int insert = dp[i][j + 1] + 1;
					int delete = dp[i + 1][j] + 1;

					int min = replace > insert ? insert : replace;
					min = delete > min ? min : delete;
					dp[i + 1][j + 1] = min;
				}
			}
		}

		return dp[len1][len2];
	}

	public static void main(String[] args) {
		String content = "I have a bit of a wishlist item that I want to tentatively propose";

		String target = "Cannot configure the load balancing count when using Message Groups";
		String object = "The next 9 messages with different JMSXGroupIDs also go to consumer";
		EditDistance ed = new EditDistance();
		double score = minDistance(object, target);

		// System.out.println((FeatureUtility.isContain("control",
		// FeatureUtility.SMART_STOP_WORDS, true)));
		System.out.println("score = " + score);

	}
}