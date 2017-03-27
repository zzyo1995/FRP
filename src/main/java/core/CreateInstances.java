package main.java.core;

import java.util.ArrayList;

import weka.core.Attribute;
import weka.core.DenseInstance;
import weka.core.Instances;

public class CreateInstances {

	  /**
	   * Generates the Instances object and outputs it in ARFF format to stdout.
	   *
	   * @param args	ignored
	   * @throws Exception	if generation of instances fails
	   */
	  public static void main(String[] args) throws Exception {
	    ArrayList<Attribute>	atts;
	    ArrayList<Attribute>	attsRel;
	    ArrayList<String>		attVals;
	    ArrayList<String>		attValsRel;
	    Instances			data;
	    Instances			dataRel;
	    double[]			vals;
	    double[]			valsRel;
	    int				i;

	    // 1. set up attributes
	    atts = new ArrayList<Attribute>();
	    // - numeric
	    atts.add(new Attribute("att1"));
	    // - nominal
	    attVals = new ArrayList<String>();
	    for (i = 0; i < 5; i++)
	      attVals.add("val" + (i+1));
	    atts.add(new Attribute("att2", attVals));
	    // - string
	    atts.add(new Attribute("att3", (ArrayList<String>) null));
	    // - date
	    atts.add(new Attribute("att4", "yyyy-MM-dd"));
	    

	    // 2. create Instances object
	    data = new Instances("MyRelation", atts, 0);

	    // 3. fill with data
	    // first instance
	    vals = new double[data.numAttributes()];
	    // - numeric
	    vals[0] = Math.PI;
	    // - nominal
	    vals[1] = attVals.indexOf("val3");
	    // - string
	    vals[2] = data.attribute(2).addStringValue("This is a string!");
	    // - date
	    vals[3] = data.attribute(3).parseDate("2001-11-09");
	    
	    //add
	    data.add(new DenseInstance(1.0, vals));

	    // second instance
	    vals = new double[data.numAttributes()];  // important: needs NEW array!
	    // - numeric
	    vals[0] = Math.E;
	    // - nominal
	    vals[1] = attVals.indexOf("val1");
	    // - string
	    vals[2] = data.attribute(2).addStringValue("And another one!");
	    // - date
	    vals[3] = data.attribute(3).parseDate("2000-12-01");
	    
	    // add
	    data.add(new DenseInstance(1.0, vals));

	    // 4. output data
	    System.out.println(data);
	  }
	}
