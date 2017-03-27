package main.java.exe;

import java.io.BufferedReader;
import java.io.FileReader;

import main.java.core.RequestClassifier;
import main.java.util.FeatureUtility;
import main.java.core.DataParser;
import main.java.core.RequestAnalyzer;
import weka.core.Instances;

public class Runner {
	
	public static void main(String[] args) throws Exception {
		//GroupTrainData.groupTrainData("resource//train", "resource//PMA.properties");
		String PMA_Tagged_File = "resource//PMA.properties";
		String ACTIVEMQ_Tagged_File = "resource//PMA-test.properties";//PMA+ActiveMQ+AspectJ+mopidy
		String rawDataFile = "resource//dataset_pma+actmq+aspectj+mopidy-0322-test.arff";

		//String rawDataFile = "resource//dataset_16-attributes-tfidf-0305.arff";
		//String filteredDataFile = "resource//dataFiltered_16-attributes-tfidf-actmq-0306.arff";
		//String filteredDataFile = "resource//dataFiltered_16-attributes-tfidf-0305.arff";
				
		Instances data = DataParser.readIntoFeatureRequests(ACTIVEMQ_Tagged_File);
		
		FeatureUtility.exportInstancesToFile(data,rawDataFile);
		
		
		//Instances data = new Instances(new BufferedReader(new FileReader(rawDataFile)));
		//RequestClassifier.filterData(data, filteredDataFile);
		//RequestClassifier.classify(filteredDataFile, rawDataFile);
		//RequestClassifier.predict("resource//exp1//dataFiltered2.arff",classIndex);
		//FeatureUtility.printInstancesByTag(data, "SENTENCE");
		
		System.out.println("Total Size of dataset = "+data.numInstances());
		RequestAnalyzer.predictTag(data);
		//RequestClassifier.printInstancesByTag(data);
		
	}
}
