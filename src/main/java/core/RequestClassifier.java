package main.java.core;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Random;

import edu.stanford.nlp.ling.CoreLabel;
import main.java.util.FeatureUtility;
import weka.attributeSelection.AttributeSelection;
import weka.attributeSelection.CfsSubsetEval;
import weka.attributeSelection.GainRatioAttributeEval;
import weka.attributeSelection.GreedyStepwise;
import weka.attributeSelection.InfoGainAttributeEval;
import weka.attributeSelection.Ranker;
import weka.attributeSelection.WrapperSubsetEval;
import weka.classifiers.AbstractClassifier;
import weka.classifiers.Classifier;
import weka.classifiers.Evaluation;
import weka.classifiers.bayes.NaiveBayes;
import weka.classifiers.bayes.NaiveBayesMultinomialText;
import weka.classifiers.functions.Logistic;
import weka.classifiers.functions.SMO;
import weka.classifiers.lazy.IBk;
import weka.classifiers.meta.AdaBoostM1;
import weka.classifiers.meta.AttributeSelectedClassifier;
import weka.classifiers.meta.Bagging;
import weka.classifiers.rules.DecisionTable;
import weka.classifiers.rules.JRip;
import weka.classifiers.trees.J48;
import weka.classifiers.trees.RandomForest;
import weka.core.Attribute;
import weka.core.Instance;
import weka.core.Instances;
import weka.core.Utils;
import weka.core.WekaPackageManager;
import weka.core.converters.TextDirectoryLoader;
import weka.core.converters.ConverterUtils.DataSource;
import weka.core.stemmers.IteratedLovinsStemmer;
import weka.core.stemmers.NullStemmer;
import weka.core.stopwords.StopwordsHandler;
import weka.core.tokenizers.NGramTokenizer;
import weka.core.tokenizers.WordTokenizer;
import weka.filters.Filter;
import weka.filters.supervised.attribute.Discretize;
import weka.filters.unsupervised.attribute.StringToWordVector;

public class RequestClassifier {
	static int tagSize = 3;
	static String[] tagNames = new String[] { "explaination", "want", "useless" };
	static boolean printResult = false;
	static String[] rawAttributeNames;
	static String outputFile = "resource//precision_results_non-text.txt";
	static PrintWriter out = null;
	static String modelOutputDir = "resource//models//";

	public static void classify(String filename, String rawfileName)
			throws IOException, InstantiationException, IllegalAccessException, ClassNotFoundException {
		Instances rawData;
		Instances data;

		// Read data and rawdata
		BufferedReader reader = new BufferedReader(new FileReader(filename));
		data = new Instances(reader);
		// data.deleteStringAttributes();
		reader = new BufferedReader(new FileReader(rawfileName));
		rawData = new Instances(reader);
		// rawData.deleteStringAttributes();
		reader.close();

		// set class index
		int numAttr = rawData.numAttributes() - 3;
		data.setClassIndex(numAttr + 1);

		// int numAttr = rawData.numAttributes() - 2;
		// data.setClassIndex(numAttr + 1);
		// delete sentence attr

		createPrintWriter(outputFile);

		rawAttributeNames = new String[numAttr];
		for (int i = 0; i < numAttr; i++) {
			rawAttributeNames[i] = data.attribute(i).name();
		}

		// range 0-12
		int[] removeAttributes = new int[numAttr];
		for (int i = 0; i < numAttr; i++)
			removeAttributes[i] = i;

		// TODO
		// start 10 to 4
		for (int tbd = 1; tbd <= 1; tbd++) {
			List list = combine(removeAttributes, tbd);
			/*
			 * List list = new ArrayList(); int[] delete1 = new int[]{2,3,6};
			 * int[] delete2 = new int[]{4,6,10,11}; int[] delete3 = new
			 * int[]{3,4,7,8,10}; int[] delete4 = new int[]{3,4,6,7,11};
			 * list.add(delete1); list.add(delete2); list.add(delete3);
			 * list.add(delete4);
			 */

			ArrayList<Classifier> classifiers = getAllClassifiers();

			double[][] maxPrecision = new double[tagSize][2];
			String[] deleteAttrForMaxPrec = new String[tagSize];
			double[][] maxRecall = new double[tagSize][2];
			String[] deleteAttrForMaxRec = new String[tagSize];

			int[] maxAttributes = null;
			String classiferName = null;
			int count = 0;

			int size = list.size();
			System.out.printf("\nFind %d combinations, need classify %d times\n", size, size * classifiers.size());
			for (int i = 0; i < list.size(); i++) {
				Instances copyData = new Instances(data);
				int[] a = (int[]) list.get(i);
				// out.println("\n=============================================");
				// out.println("To be remove: "+a.length);
				for (int j = 0; j < a.length; j++) {
					String deleteName = rawAttributeNames[a[j]];
					int deleteIndex = copyData.attribute(deleteName).index();
					// out.print(a[j]+","+deleteIndex + " - "+deleteName+"; ");
					copyData.deleteAttributeAt(deleteIndex);
				}
				// out.print("\n\n");
				// out.flush();

				for (Classifier c : classifiers) {
					try {
						count++;
						System.out.println("#classify:" + count + "\\" + size * classifiers.size());

						double[][] results = RequestClassifier.classify(copyData, c, rawData);

						if (results == null)
							System.err.println("NULL return from classify");

						for (int j = 0; j < tagSize; j++) {
							// Find highest Precision for each tag
							maxAttributes = a;
							classiferName = c.getClass().getName();

							if (results[j][0] > maxPrecision[j][0]) {
								maxPrecision[j][0] = results[j][0];
								maxPrecision[j][1] = results[j][1];

								deleteAttrForMaxPrec[j] = "";
								for (int m = 0; m < maxAttributes.length; m++)
									deleteAttrForMaxPrec[j] += rawAttributeNames[maxAttributes[m]] + " ";

								printConsole(tagNames[j], count, maxPrecision[j][0], maxPrecision[j][1],
										"\nFind MAX precision when train and test for %d times.\n",
										"Highest Precsion = %.2f, Recall = %.2f\n", classiferName,
										deleteAttrForMaxPrec[j]);

							} else if (results[j][0] == maxPrecision[j][0]) {
								if (results[j][1] > maxPrecision[j][1]) {
									maxPrecision[j][1] = results[j][1];
									deleteAttrForMaxPrec[j] = "";
									for (int k = 0; k < maxAttributes.length; k++)
										deleteAttrForMaxPrec[j] += rawAttributeNames[maxAttributes[k]] + " ";

									printConsole(tagNames[j], count, maxPrecision[j][0], maxPrecision[j][1],
											"\nUpdate MAX recall when train and test for %d times.\n",
											"Highest Precsion = %.2f, Recall = %.2f\n", classiferName,
											deleteAttrForMaxPrec[j]);
								}
							}

							// Find highest Recall for each tag
							if (results[j][1] > maxRecall[j][1]) {
								maxRecall[j][1] = results[j][1];
								maxRecall[j][0] = results[j][0];

								deleteAttrForMaxRec[j] = "";
								for (int k = 0; k < maxAttributes.length; k++)
									deleteAttrForMaxRec[j] += rawAttributeNames[maxAttributes[k]] + " ";

								printConsole(tagNames[j], count, maxRecall[j][1], maxRecall[j][0],
										"\nFind MAX Recall when train and test for %d times.\n",
										"Highest Recall = %.2f, Precision = %.2f\n", classiferName,
										deleteAttrForMaxRec[j]);
							} else if (results[j][1] == maxRecall[j][1]) {
								if (results[j][0] > maxRecall[j][0]) {
									maxRecall[j][0] = results[j][0];
									deleteAttrForMaxRec[j] = "";
									for (int k = 0; k < maxAttributes.length; k++)
										deleteAttrForMaxRec[j] += rawAttributeNames[maxAttributes[k]] + " ";

									printConsole(tagNames[j], count, maxRecall[j][1], maxRecall[j][0],
											"\nUpdate MAX precision when train and test for %d times.\n",
											"Highest Recall = %.2f, Precision = %.2f\n", classiferName,
											deleteAttrForMaxRec[j]);
								}
							}
						}

					} catch (Exception e) {
						e.printStackTrace();
						continue;
					}
				}

			}

			if (maxAttributes != null) {

				out.printf("\n\n\nRomoved #attributes = %d ", maxAttributes.length);
				out.printf("\nTrain and Test for %d times", count);
				out.println("\nClassifier: " + classiferName);

				for (int j = 0; j < tagSize; j++) {
					out.printf("\n=============Tag: %s================", tagNames[j]);
					out.printf("\nHighest Precision = %.2f, Recall = %.2f", maxPrecision[j][0], maxPrecision[j][1]);
					out.print("\nRemoved attributes names: " + deleteAttrForMaxPrec[j]);
					out.print("\nRemoved attributes index: " + getRemovedIndex(deleteAttrForMaxPrec[j]));
					out.printf("\nHighest Recall = %.2f, Precision = %.2f", maxRecall[j][1], maxRecall[j][0]);
					out.print("\nRemoved attributes names: " + deleteAttrForMaxRec[j]);
					out.print("\nRemoved attributes index: " + getRemovedIndex(deleteAttrForMaxRec[j]));
					out.println();
					out.flush();
				}
				System.out.println("Flushed to file");
			}
		}

		out.close();
		System.out.println("END, writed to file: " + outputFile);

	}

	private static void printConsole(String tagName, int count, double output1, double output2, String countString,
			String performanceString, String classiferName, String deleteAttrNames) {
		System.out.printf("=============Tag: %s================", tagName);
		System.out.printf(countString, count);
		System.out.printf(performanceString, output1, output2);
		System.out.println("Removed attributes: " + deleteAttrNames);
		System.out.println("Classifier: " + classiferName);

	}

	private static ArrayList<Classifier> getAllClassifiers() {
		ArrayList<Classifier> classifiers = new ArrayList<Classifier>();
		classifiers.add(new RandomForest());
		// classifiers.add(new NaiveBayes());

		// classifiers.add(new J48());

		// classifiers.add(new DecisionTable());
		// classifiers.add(new JRip());

		// classifiers.add(new IBk());

		// classifiers.add(new AdaBoostM1());
		// classifiers.add(new Bagging());

		// classifiers.add(new Logistic());
		// classifiers.add(new SMO());

		/*
		 * WekaPackageManager.loadPackages( false, true, false );
		 * AbstractClassifier classifier = ( AbstractClassifier ) Class.forName(
		 * "weka.classifiers.functions.LibSVM" ).newInstance();
		 */
		return classifiers;
	}

	private static void createPrintWriter(String outputFile) {
		try {
			out = new PrintWriter(new FileOutputStream(new File(outputFile), true), true);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
	}
	
	private static PrintWriter getPrintWriter(String outputFile) {
		PrintWriter printer = null;
		try {
			printer = new PrintWriter(new FileOutputStream(new File(outputFile), false));
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		
		return printer;
	}

	private static String getRemovedIndex(String input) {
		String results = "";

		if (input == null || input.length() == 0)
			return null;

		String[] words = input.trim().split(" ");

		for (String word : words) {
			for (int j = 0; j < rawAttributeNames.length; j++) {
				if (word.equals(rawAttributeNames[j])) {
					results += j + ",";
				}
			}
		}
		return results;
	}

	public static void filterData(Instances dataRaw, String filename) throws Exception {

		// exportInstancesToFile(dataRaw,
		// "D:\\FeatureTrac\\github\\WekaDemo\\WekaDemo\\resource\\dataRaw.arff");

		StringToWordVector filter;
		NGramTokenizer token = new NGramTokenizer();
		token.setNGramMaxSize(2);
		token.setNGramMinSize(2);

		filter = getStopWordFilter(true);
		filter.setTFTransform(true);
		filter.setIDFTransform(true);
		filter.setInputFormat(dataRaw);
		filter.setTokenizer(new WordTokenizer());
		filter.setStemmer(new NullStemmer());
		filter.setLowerCaseTokens(true);

		Instances dataFiltered = Filter.useFilter(dataRaw, filter);
		dataFiltered.setClassIndex(dataRaw.numAttributes() - 2);

		FeatureUtility.exportInstancesToFile(dataFiltered, filename);
		System.out.println("Exported to file: " + filename);
	}



	public static double[][] classify(Instances data, Classifier classifier, Instances rawData) throws Exception {

		Attribute tag = data.classAttribute();
		tagSize = tag.numValues();
		tagNames = new String[tagSize];
		double[][] evalResult = new double[tagSize][2];
		boolean outputModel = false;

		for (int i = 0; i < tagSize; i++)
			tagNames[i] = tag.value(i);

		if (classifier.getClass().getName().contains("bayes")) {
			Discretize filter = new Discretize();
			filter.setInputFormat(data);
			data = Filter.useFilter(data, filter);
		}

		// select attributes
		/*WrapperSubsetEval evalForAttr = new WrapperSubsetEval();
		AttributeSelection attselector = new AttributeSelection();
		evalForAttr.setClassifier(new RandomForest());
		GreedyStepwise search = new GreedyStepwise();
		search.setSearchBackwards(true);
		evalForAttr.setFolds(10);

		attselector.setEvaluator(evalForAttr);
		attselector.setSearch(search);
		attselector.setInputFormat(data);*/

		//System.out.println("#Attributes before selection: " + data.numAttributes());

		//Instances newData = Filter.useFilter(data, attselector);

		/*System.out.println("Attributes after selection: ");
		Enumeration<Attribute> enumerator = newData.enumerateAttributes();
		while (enumerator.hasMoreElements()) {
			System.out.println(enumerator.nextElement().name());
		}

		System.out.println("Attributes after selection: END");*/

		// TODO
		//classifier.buildClassifier(data);

		if (outputModel) {
			String filename = modelOutputDir + classifier.getClass().getName() + ".model";
			weka.core.SerializationHelper.write(filename, classifier);
			System.out.println("Export Model: " + filename);
		}

		//useLowLevel(data);
		Evaluation eval = getEvaluation(data, classifier);

		for (int i = 0; i < tagSize; i++) {
			evalResult[i][0] = Double.valueOf(eval.precision(i));
			evalResult[i][1] = Double.valueOf(eval.recall(i));
		}

		if (printResult) {
			testClassifier(10, data, rawData, classifier);
		}

		return evalResult;
	}

	private static Evaluation getEvaluation(Instances data, Classifier classifier) throws Exception {

		 weka.filters.supervised.attribute.AttributeSelection filter = new weka.filters.supervised.attribute.AttributeSelection();
		    CfsSubsetEval eval = new CfsSubsetEval();
		    GreedyStepwise search = new GreedyStepwise();
		    search.setSearchBackwards(true);
		    filter.setEvaluator(eval);
		    filter.setSearch(search);
		    filter.setInputFormat(data);
		    Instances newData = Filter.useFilter(data, filter);

		Evaluation evaluation = new Evaluation(data);

		System.out.println(evaluation.toSummaryString());
		evaluation.crossValidateModel(classifier, newData, 10, new Random(1));
		return evaluation;
	}
	
	  protected static void useLowLevel(Instances data) throws Exception {
		    System.out.println("\n3. Low-level");
		    AttributeSelection attsel = new AttributeSelection();
		    CfsSubsetEval eval = new CfsSubsetEval();
		    GreedyStepwise search = new GreedyStepwise();
		    search.setSearchBackwards(true);
		    attsel.setEvaluator(eval);
		    attsel.setSearch(search);
		    
		    attsel.SelectAttributes(data);
		    int[] indices = attsel.selectedAttributes();
		    System.out.println("selected attribute indices (starting with 0):\n" + Utils.arrayToString(indices));
		  }

	private static void testClassifier(int folds, Instances data, Instances rawData, Classifier classifier)
			throws Exception {
		Instances train = null;
		Instances test = null;
		Evaluation eval = null;
		for (int n = 0; n < folds; n++) {

			train = data.trainCV(folds, n);
			test = data.testCV(folds, n);
			Classifier clsCopy = AbstractClassifier.makeCopy(classifier);
			clsCopy.buildClassifier(train);
			eval = new Evaluation(test);
			eval.evaluateModel(clsCopy, test);

			// output predictions

			out.println("# - actual - predicted - error - distribution - content");
			for (int i = 0; i < test.numInstances(); i++) {
				boolean err;
				Instance testInstance = test.instance(i);
				double pred = clsCopy.classifyInstance(testInstance);
				double[] dist = clsCopy.distributionForInstance(testInstance);
				if (pred != test.instance(i).classValue()) {
					err = true;
				} else {
					err = false;
				}

				if (err) {
					out.print((i + 1));
					out.print(" - ");
					out.print(testInstance.toString(test.classIndex()));
					out.print(" - ");
					out.print(test.classAttribute().value((int) pred));
					out.print(" - ");
					if (err) {
						out.print("yes");
					} else {
						out.print("no");
					}
					out.print(" - ");
					out.print(Utils.arrayToString(dist));
					out.print(" - ");
					int pos = findPostion(data, testInstance);
					out.println(pos);
					out.println(testInstance);
					out.print(rawData.get(pos));
					out.println();
				}

				out.println();

			}

			out.flush();
			// System.out.println("Flushed to file: # - actual - predicted -
			// error - distribution - content");

		}
		out.println("==============================");
		out.println(classifier.getClass().getName());

		out.println(eval.toClassDetailsString());
		out.println("ClassIndex=1, precision=" + eval.precision(1));
		out.println("ClassIndex=1, recall=" + eval.recall(1));
		out.printf("Train set = %d\n ", train.size());
		out.printf("Test set = %d\n ", test.size());
		out.println(eval.toSummaryString());
		out.println(eval.toMatrixString());
		out.flush();
	}

	private static int findPostion(Instances data, Instance testInstance) {
		for (int i = 0; i < data.numInstances(); i++) {
			Instance fetch = data.get(i);
			String fetchS = fetch.toString();
			String targetS = testInstance.toString();
			if (fetchS.equals(targetS))
				return i;
		}
		return -1;
	}

	private static StringToWordVector getStopWordFilter(boolean flag) { 
		StringToWordVector filter = new StringToWordVector();
		if (flag) {
			final List<String> list = Arrays.asList(SMART_STOP_WORDS);
			filter.setStopwordsHandler(new StopwordsHandler() {
				@Override
				public boolean isStopword(String word) {
					String ss[] = word.split(" ");
					// System.out.println("=>"+word);
					for (String s : ss) {
						if (list.contains(s.trim())) {
							return true;
						}
					}
					return false;
				}
			});
		}
		return filter;
	}
	
	public static final String QUESTION[] = {"why"};

	public static final String WANT_MD[] = { "should", "can" };
	public static final String WANTS[] = { "sugg", "propose", "consider", "want", "would like", "\'d like", "’d like",
			"what about", "how about", "new" };
	public static final String GOOD[] = { "help", "helpful", "useful", "great", "nice", "good", "appreciate", "greatly",
			"appreciated", "appropriate", "better", "convenient","cool","worth"};
	public static final String EXPLAINATION[] = { "why", "hint", "mean", "has to", "have to", "only", "same", "F.e.",
			"already", "etc" }; // like
	
	public static final String USELESSVERB[] = {"be","wander","wonder"};

	public static final String SMART_STOP_WORDS[] = { "鈥�", "...", "鈥�", " ", "a", "able", "about", "above",
			"according", "accordingly", "across", "actually", "after", "afterwards", "again", "against", "all", "allow",
			"allows", "almost", "alone", "along", "already", "also", "although", "always", "am", "among", "amongst",
			"an", "and", "another", "any", "anybody", "anyhow", "anyone", "anything", "anyway", "anyways", "anywhere",
			"apart", "appear", "appreciate", "appropriate", "are", "around", "as", "aside", "ask", "asking",
			"associated", "at", "available", "away", "awfully", "b", "be", "became", "because", "become", "becomes",
			"becoming", "been", "before", "beforehand", "behind", "being", "believe", "below", "beside", "besides",
			"best", "better", "between", "beyond", "both", "brief", "but", "by", "c", "came", "can", "cannot", "cant",
			"cause", "causes", "certain", "certainly", "changes", "clearly", "co", "com", "come", "comes", "concerning",
			"consequently", "consider", "considering", "contain", "containing", "contains", "corresponding", "could",
			"course", "currently", "d", "definitely", "described", "despite", "did", "different", "do", "does", "doing",
			"done", "down", "downwards", "during", "e", "each", "edu", "eg", "eight", "either", "else", "elsewhere",
			"enough", "entirely", "especially", "et", "etc", "even", "ever", "every", "everybody", "everyone",
			"everything", "everywhere", "ex", "exactly", "example", "except", "f", "far", "few", "fifth", "first",
			"five", "followed", "following", "follows", "for", "former", "formerly", "forth", "four", "from", "further",
			"furthermore", "g", "get", "gets", "getting", "given", "gives", "go", "goes", "going", "gone", "got",
			"gotten", "greetings", "h", "had", "happens", "hardly", "has", "have", "having", "he", "hello", "help",
			"hence", "her", "here", "hereafter", "hereby", "herein", "hereupon", "hers", "herself", "hi", "him",
			"himself", "his", "hither", "hopefully", "how", "howbeit", "however", "i", "ie", "if", "ignored",
			"immediate", "in", "inasmuch", "inc", "indeed", "indicate", "indicated", "indicates", "inner", "insofar",
			"instead", "into", "inward", "is", "it", "its", "itself", "j", "just", "k", "keep", "keeps", "kept", "know",
			"knows", "known", "l", "last", "lately", "later", "latter", "latterly", "least", "less", "lest", "let",
			"like", "liked", "likely", "little", "look", "looking", "looks", "ltd", "m", "mainly", "many", "may",
			"maybe", "me", "mean", "meanwhile", "merely", "might", "more", "moreover", "most", "mostly", "much", "must",
			"my", "myself", "n", "name", "namely", "nd", "near", "nearly", "necessary", "need", "needs", "neither",
			"never", "nevertheless", "new", "next", "nine", "no", "nobody", "non", "none", "noone", "nor", "normally",
			"not", "nothing", "novel", "now", "nowhere", "o", "obviously", "of", "off", "often", "oh", "ok", "okay",
			"old", "on", "once", "one", "ones", "only", "onto", "or", "other", "others", "otherwise", "ought", "our",
			"ours", "ourselves", "out", "outside", "over", "overall", "own", "p", "particular", "particularly", "per",
			"perhaps", "placed", "please", "plus", "possible", "presumably", "probably", "provides", "q", "que",
			"quite", "qv", "r", "rather", "rd", "re", "really", "reasonably", "regarding", "regardless", "regards",
			"relatively", "respectively", "right", "s", "said", "same", "saw", "say", "saying", "says", "second",
			"secondly", "see", "seeing", "seem", "seemed", "seeming", "seems", "seen", "self", "selves", "sensible",
			"sent", "serious", "seriously", "seven", "several", "shall", "she", "should", "since", "six", "so", "some",
			"somebody", "somehow", "someone", "something", "sometime", "sometimes", "somewhat", "somewhere", "soon",
			"sorry", "specified", "specify", "specifying", "still", "sub", "such", "sup", "sure", "t", "take", "taken",
			"tell", "tends", "th", "than", "thank", "thanks", "thanx", "that", "thats", "the", "their", "theirs",
			"them", "themselves", "then", "thence", "there", "thereafter", "thereby", "therefore", "therein", "theres",
			"thereupon", "these", "they", "think", "third", "this", "thorough", "thoroughly", "those", "though",
			"three", "through", "throughout", "thru", "thus", "to", "together", "too", "took", "toward", "towards",
			"tried", "tries", "truly", "try", "trying", "twice", "two", "u", "un", "under", "unfortunately", "unless",
			"unlikely", "until", "unto", "up", "upon", "us", "use", "used", "useful", "uses", "using", "usually",
			"uucp", "v", "value", "various", "very", "via", "viz", "vs", "w", "want", "wants", "was", "way", "we",
			"welcome", "well", "went", "were", "what", "whatever", "when", "whence", "whenever", "where", "whereafter",
			"whereas", "whereby", "wherein", "whereupon", "wherever", "whether", "which", "while", "whither", "who",
			"whoever", "whole", "whom", "whose", "why", "will", "willing", "wish", "with", "within", "without",
			"wonder", "would", "would", "x", "y", "yes", "yet", "you", "your", "yours", "yourself", "yourselves", "z",
			"zero" };

	public static void predict(String datasource, int index) throws Exception {
		// load data
		Instances test = DataSource.read(datasource);
		test.setClassIndex(index);
		Classifier cls = (Classifier) weka.core.SerializationHelper
				.read("resource//exp1//weka.classifiers.trees.RandomForest.model");

		// output predictions
		System.out.println("# - actual - predicted - error - distribution");
		for (int i = 0; i < test.numInstances(); i++) {
			double pred = cls.classifyInstance(test.instance(i));
			double[] dist = cls.distributionForInstance(test.instance(i));
			System.out.print((i + 1));
			System.out.print(" - ");
			System.out.print(test.instance(i).toString(test.classIndex()));
			System.out.print(" - ");
			System.out.print(test.classAttribute().value((int) pred));
			System.out.print(" - ");
			if (pred != test.instance(i).classValue())
				System.out.print("yes");
			else
				System.out.print("no");
			System.out.print(" - ");
			System.out.print(Utils.arrayToString(dist));
			System.out.println();
		}

	}

	public static List combine(int[] a, int m) {
		int n = a.length;
		if (m > n) {
			System.err.println("错误！数组a中只�?" + n + "个元素�??" + m + "大于" + 2 + "!!!");
			return null;
		}

		List result = new ArrayList();

		int[] bs = new int[n];
		for (int i = 0; i < n; i++) {
			bs[i] = 0;
		}
		// 初始�?
		for (int i = 0; i < m; i++) {
			bs[i] = 1;
		}
		boolean flag = true;
		boolean tempFlag = false;
		int pos = 0;
		int sum = 0;
		// 首先找到第一�?10组合，然后变�?01，同时将左边�?有的1移动到数组的�?左边
		do {
			sum = 0;
			pos = 0;
			tempFlag = true;
			result.add(print(bs, a, m));

			for (int i = 0; i < n - 1; i++) {
				if (bs[i] == 1 && bs[i + 1] == 0) {
					bs[i] = 0;
					bs[i + 1] = 1;
					pos = i;
					break;
				}
			}
			// 将左边的1全部移动到数组的�?左边

			for (int i = 0; i < pos; i++) {
				if (bs[i] == 1) {
					sum++;
				}
			}
			for (int i = 0; i < pos; i++) {
				if (i < sum) {
					bs[i] = 1;
				} else {
					bs[i] = 0;
				}
			}

			// �?查是否所有的1都移动到了最右边
			for (int i = n - m; i < n; i++) {
				if (bs[i] == 0) {
					tempFlag = false;
					break;
				}
			}
			if (tempFlag == false) {
				flag = true;
			} else {
				flag = false;
			}

		} while (flag);
		result.add(print(bs, a, m));

		return result;
	}

	private static int[] print(int[] bs, int[] a, int m) {
		int[] result = new int[m];
		int pos = 0;
		for (int i = 0; i < bs.length; i++) {
			if (bs[i] == 1) {
				result[pos] = a[i];
				pos++;
			}
		}
		return result;
	}

	private static void print(List l) {
		for (int i = 0; i < l.size(); i++) {
			int[] a = (int[]) l.get(i);
			for (int j = 0; j < a.length; j++) {
				System.out.print(a[j] + "/t");
			}
			System.out.println();
		}
	}

	public static void printInstancesByTag(Instances data) throws IOException {
		
		createPrintWriter("resource//precision_results_non-text.txt");
		
		Attribute tag = data.attribute("tag"); 
		String[] outputfile = new String[tagSize];
		PrintWriter[] output = new PrintWriter[tagSize];
		data.setClassIndex(tag.index());
		
		for (int i = 0; i < tagSize; i++){
			tagNames[i] = tag.value(i);
			outputfile[i]="resource//tag_data_"+tagNames[i]+".txt";
			output[i] = getPrintWriter(outputfile[i]);
			}
		
		ListIterator<Instance> list = data.listIterator();
		
		System.out.println("Negitive predictions: ");
		int count = 0;
		while(list.hasNext()){
			Instance item = list.next();
			int index = (int)item.classValue();
			String sentence = item.stringValue(0);
			
			if(index == 1){
				boolean predict = classifyAsWant(item,data);
				if(!predict){
					count++;
					System.out.println(count+" - WANT - predict no but yes: " +sentence);
					}
			}
			
			if(index == 0){
				
				boolean predict = classifyAsWant(item,data);
				if(predict){
					count++;
					System.out.println(count+" - EXP - predict to be WANT: " +sentence);
					}
			}if(index == 2){
				boolean predict = classifyAsWant(item,data);
				if(predict){
					count++;
					System.out.println(count+" - USELESS - predict to be WANT: " +sentence);
					}
			}
			output[index].println(item.stringValue(0));
		}
		
		for (int i = 0; i < tagSize; i++){
			output[i].close();
			System.out.println("Write to file: "+outputfile[i]);
			}
		
	}


	
	//TODO
	public static boolean classifyAsWant(Instance item, Instances data) throws IOException {
		
		
		String content = item.stringValue(0);
		
		double isRealFirst = item.value(data.attribute("isRealFirst"));
		double matchMDGOODVB = item.value(data.attribute("matchMDGOODVB")); 
		double startWithVB = item.value(data.attribute("startWithVB"));
		double question = item.value(data.attribute("question"));
		double matchVBDGOOD = item.value(data.attribute("matchVBDGOOD"));
		double verbsAllInvalid = item.value(data.attribute("verbsAllInvalid"));
		
		//"want to","can"
		String[] pattern = new String[]{ "would like", "\'d like", "’d like", "would love to", "\'d love to", "’d love to",
				"appreciate","suggest","propose",
				"should",
				"add support",
				"pma may","phpmyadmin may",
				};
		
		String[] pattern2 = new String[]{"how about","what about"};
		
		String[] pattern3 = new String[]{"feature","idea","request","option","consider",};
		
		String[] expPattern = new String[]{"have to","unfortunately","possible","suggestion"
				};
		
		if(checkContains(content, expPattern) || matchVBDGOOD == 1 || classifyAsUseless(content) || verbsAllInvalid == 1 ||content.toLowerCase().startsWith("maybe"))
			return false;
		
		if(question == 1){
			if(checkContains(content, pattern2))
				return true;
			
			else
				return false;
		}
		
		if(checkContains(content, pattern) || matchMDGOODVB==1){
			return true;
		}
		
		if(noSemicolonBeforePattern(content, pattern3)){
			return true;
		}
		

		
		if(isRealFirst==1 && startWithVB ==1)
			return true;
		
		return false;
	}
	
	//TODO
	public static boolean classifyAsUseless(String content) throws IOException {
		
		//List<CoreLabel> rawWords = StanfordCoreNlpDemo.getRawWords(content);
		//StanfordCoreNlpDemo nlp = new StanfordCoreNlpDemo(false);
		//HashSet<String> allVerbs = nlp.getAllVerbs(content);
		//System.out.println(allVerbs);
		
		String[] patterns = new String[]{"hi","hello","thank","regards"};
		String[] splits = content.split(" |,");
		if(splits[0].equalsIgnoreCase("hi") || splits[0].equalsIgnoreCase("hello"))
			return true;
		
		if(content.toLowerCase().contains("thank"))
			return true;
		
		return false;
	}
	
	private static boolean noSemicolonBeforePattern(String content, String[] pattern3) {
		boolean result = true;
		
		if(checkContains(content, pattern3)){
			List<CoreLabel> rawWords = StanfordCoreNlpDemo.getRawWords(content);
			int firstIndex = getFirstIndex(rawWords, pattern3);
			
			for(int i = 0; i < firstIndex; i++){
				String word = rawWords.get(i).word();
				if(word.equals(":")){
					result = false;
					break;
				}
			}
		}else{
			result = false;
		}
		return result;
	}

	public static int getFirstIndex(List<CoreLabel> rawWords, String[] pattern3) {
		int firstIndex = -1;
		
		for(int i = 0; i < rawWords.size();i++){
			String word = rawWords.get(i).word();
			if(checkContains(word,pattern3)){
				firstIndex = i;
			}
		}
		
		return firstIndex;
	}

	public static boolean checkContains(String text, String[] list) {
		for (String word : list) {
			if (text.toLowerCase().contains(word.toLowerCase()))
				return true;
		}
		return false;
	}
	
	
	public static void main(String[] args){
		try {
			System.out.println(classifyAsUseless("This is actually a feature request more than a bug report"));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public static ArrayList<Integer> getAllMatchedIndex(List<CoreLabel> rawWords, String[] good) {
		ArrayList<Integer> results = new ArrayList<Integer>();
		
		for(int i = 0; i < rawWords.size();i++){
			String word = rawWords.get(i).word();
			if(checkContains(word, good)){
				results.add(i);
			}
		}
		
		return results;
	}
}