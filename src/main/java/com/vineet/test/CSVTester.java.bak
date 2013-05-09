package mia.classifier.ch14;


import java.io.FileReader;
import java.io.IOException;
import java.io.FileNotFoundException;

import org.apache.mahout.math.Vector;
import org.apache.mahout.math.RandomAccessSparseVector;
import org.apache.mahout.math.SequentialAccessSparseVector;
import org.apache.mahout.vectorizer.encoders.FeatureVectorEncoder;
import org.apache.mahout.vectorizer.encoders.StaticWordValueEncoder;
import org.apache.mahout.math.DenseVector;

import org.apache.mahout.classifier.sgd.OnlineLogisticRegression;
import org.apache.mahout.classifier.sgd.L1;

import com.google.common.collect.ConcurrentHashMultiset;
import com.google.common.collect.HashMultiset;
import com.google.common.collect.Iterables;
import com.google.common.collect.Multiset;

import au.com.bytecode.opencsv.CSVReader;

public class CSVTester
{
	public static final int PRODUCT = 1;
	public static final int VERSION = 2;
	public static final int SBRS = 2;
	public static final int FEATURES = 1000;


	public static void main(String[] args) throws IOException
	{

		FeatureVectorEncoder productEncoder = new StaticWordValueEncoder("product");
		FeatureVectorEncoder versionEncoder = new StaticWordValueEncoder("version");
		FeatureVectorEncoder sbrsEncoder = new StaticWordValueEncoder("sbrs");

		Multiset<String> words = ConcurrentHashMultiset.create();

		Multiset<String> products = ConcurrentHashMultiset.create();
		Multiset<String> version = ConcurrentHashMultiset.create();
		Multiset<String> sbrs = ConcurrentHashMultiset.create();

		System.out.println("Now reading train.csv.");

		CSVReader reader = new CSVReader(new FileReader("train.csv"),',','"');
		String[] nextLine;

		while ((nextLine = reader.readNext()) != null) {

			products.add(nextLine[PRODUCT]);	
			version.add(nextLine[VERSION]);	
			sbrs.add(nextLine[SBRS]);	
		}

		System.out.print(" Read file");
		Vector v = new RandomAccessSparseVector(1000);
		for (String word: products.elementSet()) 
		{
			productEncoder.addToVector(word,Math.log(1 + products.count(word)), v);
		}

		for (String word: version.elementSet()) 
		{
			versionEncoder.addToVector(word,Math.log(1 + version.count(word)), v);
		}

		for (String word: sbrs.elementSet()) 
		{
			sbrsEncoder.addToVector(word,Math.log(1 + sbrs.count(word)), v);
		}

		System.out.print(" Vectorized.");

		OnlineLogisticRegression learningAlgorithm =
		new OnlineLogisticRegression(
		20, FEATURES, new L1())
		.alpha(1).stepOffset(1000)
		.decayExponent(0.9)
		.lambda(3.0e-5)
		.learningRate(20);


		learningAlgorithm.classifyFull(v);
		learningAlgorithm.close();
		System.out.println("Finished running learning algorithm.");
	}
}
