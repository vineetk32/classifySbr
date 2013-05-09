/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.vin.test;

import com.google.common.base.Charsets;
import com.google.common.collect.Lists;
import com.google.common.io.Closeables;
import com.google.common.io.Resources;
import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;
import org.apache.commons.cli2.CommandLine;
import org.apache.commons.cli2.Group;
import org.apache.commons.cli2.Option;
import org.apache.commons.cli2.builder.ArgumentBuilder;
import org.apache.commons.cli2.builder.DefaultOptionBuilder;
import org.apache.commons.cli2.builder.GroupBuilder;
import org.apache.commons.cli2.commandline.Parser;
import org.apache.commons.cli2.util.HelpFormatter;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.Terms;
import org.apache.lucene.index.TermsEnum;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.BytesRef;
import org.apache.lucene.util.Version;
import org.apache.mahout.classifier.sgd.LogisticModelParameters;
import org.apache.mahout.classifier.sgd.OnlineLogisticRegression;
import org.apache.mahout.math.NamedVector;
import org.apache.mahout.math.RandomAccessSparseVector;
import org.apache.mahout.math.Vector;
import org.apache.mahout.utils.vectors.TermInfo;
import org.apache.mahout.utils.vectors.lucene.CachedTermInfo;
import org.apache.mahout.utils.vectors.lucene.TFDFMapper;
import org.apache.mahout.vectorizer.TF;
import org.apache.mahout.vectorizer.Weight;
import org.apache.mahout.vectorizer.encoders.FeatureVectorEncoder;
import org.apache.mahout.vectorizer.encoders.StaticWordValueEncoder;

/**
 * Train a logistic regression for the examples from Chapter 13 of Mahout in
 * Action
 */
public final class IndexTrainer {

    private static String directoryName;
    private static String predictorFieldName;
    private static String targetFieldName;
    private static String outputFile;
    private static LogisticModelParameters lmp;
    private static int passes;
    private static OnlineLogisticRegression model;
    private static String categoryFileName;
    private static final int minDF = 1;
    private static final int maxDFPercent = 99;
    private static String idField;

    private IndexTrainer() {
    }

    public static void main(String[] args) throws Exception {
        mainToOutput(args);
    }

    static void mainToOutput(String[] args) throws Exception {
        if (parseArgs(args)) {
            int targetValue, totalCategories = 0;
            int errorDocs, trainDocs_used = 0;
            ArrayList<String> categoryList = new ArrayList();
            TermInfo termInfo;
            Weight weight = new TF();

            Terms termFreqVector;
            TermsEnum it;
            BytesRef term;

            BufferedReader fin = new BufferedReader(new FileReader(categoryFileName));
            String line;

            while ((line = fin.readLine()) != null) {
                if (!categoryList.contains(line.trim())) {
                    totalCategories++;
                    categoryList.add(line.trim());
                }
            }
            fin.close();

            /*System.out.println("Target Categories - ");
             for (String category: categoryList)
             {
             System.out.println(category);
             }*/

            lmp.setTargetCategories(categoryList);
            OnlineLogisticRegression lr = lmp.createRegression();
            File indexDir = new File(directoryName);
            DirectoryReader reader = DirectoryReader.open(FSDirectory.open(indexDir));
            int maxDocs = reader.maxDoc();

            termInfo = new CachedTermInfo(reader, predictorFieldName, minDF, maxDFPercent);

            FeatureVectorEncoder encoder = new StaticWordValueEncoder("text");
            Analyzer analyzer = new StandardAnalyzer(Version.LUCENE_41);


            for (int pass = 0; pass < passes; pass++) {
                errorDocs = 0;

                System.out.printf("\nPass %d -\n", pass);

                for (int currDoc = 0; currDoc < maxDocs; currDoc++) {
                    termFreqVector = reader.getTermVector(currDoc, predictorFieldName);

                    if (currDoc % 100 == 0)
                    {
                        System.out.printf("\rProgress - %d/%d", currDoc, maxDocs);
                    }
                    
                    String currTarget = null;
                    try {
                        Document tempDoc = reader.document(currDoc);
                        currTarget = tempDoc.getField(targetFieldName).stringValue();
                    } catch (java.lang.NullPointerException e) {
                        //ex.printStackTrace();
                        errorDocs++;
                        continue;
                    }


                    if (categoryList.contains(currTarget)) {
                        targetValue = categoryList.indexOf(currTarget);
                    } else {
                        System.out.println("CategoryList does not contain " + currTarget);
                        continue;
                    }

                    /*TFDFMapper mapper = new TFDFMapper(numDocs, weight, termInfo);
                    mapper.setExpectations(predictorFieldName, termFreqVector.size());

                    it = termFreqVector.iterator(null);

                    while ((term = it.next()) != null) {
                        mapper.map(term, (int) it.totalTermFreq());
                        //System.out.println(term.toString() + ":" + String.valueOf( it.totalTermFreq()));
                        //System.out.printf("\nMapping %s:%d",term[i],termFrequencies[i]);
                    }
                    Vector result = mapper.getVector();
                    String id;
                    id = reader.document(currDoc).getField(idField).stringValue();*/

                    String contents = reader.document(currDoc).getField(predictorFieldName).stringValue();
                    //System.out.printf("\nPredictor field length - %d",contents.length());
                    //System.out.printf("\nPredictor field - %s",contents);

                    if (contents.length() == 0)
                    {
                        System.out.println("\nEmpty predictor field! Skipping.");
                        continue;
                    }

                    StringReader in = new StringReader(contents);
                    TokenStream ts = (TokenStream) analyzer.tokenStream(predictorFieldName,in);
                    CharTermAttribute termAtt = ts.addAttribute(CharTermAttribute.class);

                    Vector result = new RandomAccessSparseVector(lmp.getNumFeatures());
                    if (ts == null)
                    {
                        System.out.println("\nTokenStream is null!");
                        continue;
                    }
                    ts.reset();
                    while (ts.incrementToken())
                    {
                        char[] termBuffer = termAtt.buffer();
                        int termLen = termAtt.length();
                        String w = new String(termBuffer,0,termLen);
                        encoder.addToVector(w,1, result);
                        //ts.reset();
                    }


                    if (result != null) {
                        //result = new NamedVector(result,id);
                        trainDocs_used++;
                        lr.classifyFull(result);
                        lr.train(targetValue, result);
                    }
                }
                System.out.printf("\nTotal Documents - %d, Error Docs - %d, Documents Successfully used for training - %d", maxDocs, errorDocs,trainDocs_used);
            }
            OutputStream modelOutput = new FileOutputStream(outputFile);
            System.out.println("\nWriting model to " + outputFile);
            try {
                lmp.write(new DataOutputStream(modelOutput));
            } finally {
                Closeables.closeQuietly(modelOutput);
            }
            reader.close();

        }
    }

    private static boolean parseArgs(String[] args) {
        DefaultOptionBuilder builder = new DefaultOptionBuilder();

        Option help = builder.withLongName("help").withDescription("print this list").create();

        Option quiet = builder.withLongName("quiet").withDescription("be extra quiet").create();

        ArgumentBuilder argumentBuilder = new ArgumentBuilder();

        Option directoryName = builder.withLongName("directory")
                .withRequired(true)
                .withArgument(argumentBuilder.withName("directory").withMaximum(1).create())
                .withDescription("Location of the lucene index.")
                .create();

        Option outputFile = builder.withLongName("output")
                .withRequired(true)
                .withArgument(argumentBuilder.withName("output").withMaximum(1).create())
                .withDescription("where to get training data")
                .create();

        Option predictor = builder.withLongName("predictor")
                .withRequired(true)
                .withArgument(argumentBuilder.withName("predictor").withMaximum(1).create())
                .withDescription("The Predictor Field name.")
                .create();

        Option target = builder.withLongName("target")
                .withRequired(true)
                .withArgument(argumentBuilder.withName("target").withMaximum(1).create())
                .withDescription("the name of the target variable")
                .create();

        Option features = builder.withLongName("features")
                .withArgument(
                argumentBuilder.withName("numFeatures")
                .withDefault("1000")
                .withMaximum(1).create())
                .withDescription("the number of internal hashed features to use")
                .create();

        Option passes = builder.withLongName("passes")
                .withArgument(
                argumentBuilder.withName("passes")
                .withDefault("2")
                .withMaximum(1).create())
                .withDescription("the number of times to pass over the input data")
                .create();
        Option rate = builder.withLongName("rate")
                .withArgument(argumentBuilder.withName("learningRate").withDefault("1e-3").withMaximum(1).create())
                .withDescription("the learning rate")
                .create();

        Option targetCategories = builder.withLongName("categories")
                .withRequired(true)
                .withArgument(argumentBuilder.withName("categories").withMaximum(1).create())
                .withDescription("The file containing all the possible categories.")
                .create();

        Option idFieldName = builder.withLongName("idField")
                .withRequired(true)
                .withArgument(argumentBuilder.withName("idField").withMaximum(1).create())
                .withDescription("The ID Field in the index.")
                .create();

        Group normalArgs = new GroupBuilder()
                .withOption(help)
                .withOption(quiet)
                .withOption(directoryName)
                .withOption(outputFile)
                .withOption(target)
                .withOption(targetCategories)
                .withOption(predictor)
                .withOption(passes)
                .withOption(rate)
                .withOption(features)
                .withOption(idFieldName)
                .create();

        Parser parser = new Parser();
        parser.setHelpOption(help);
        parser.setHelpTrigger("--help");
        parser.setGroup(normalArgs);
        parser.setHelpFormatter(new HelpFormatter(" ", "", " ", 130));
        CommandLine cmdLine = parser.parseAndHelp(args);

        if (cmdLine == null) {
            return false;
        }

        IndexTrainer.directoryName = getStringArgument(cmdLine, directoryName);
        IndexTrainer.outputFile = getStringArgument(cmdLine, outputFile);
        IndexTrainer.predictorFieldName = getStringArgument(cmdLine, predictor);
        IndexTrainer.targetFieldName = getStringArgument(cmdLine, target);
        IndexTrainer.categoryFileName = getStringArgument(cmdLine, targetCategories);
        IndexTrainer.idField = getStringArgument(cmdLine, idFieldName);

        List<String> typeList = Lists.newArrayList();
        List<String> predictorList = Lists.newArrayList();
        typeList.add("text");
        predictorList.add(getStringArgument(cmdLine, predictor));

        lmp = new LogisticModelParameters();
        lmp.setTargetVariable(getStringArgument(cmdLine, target));
        //lmp.setMaxTargetCategories(getIntegerArgument(cmdLine, targetCategories));
        lmp.setNumFeatures(getIntegerArgument(cmdLine, features));
        lmp.setTypeMap(predictorList, typeList);

        lmp.setLearningRate(getDoubleArgument(cmdLine, rate));

        IndexTrainer.passes = getIntegerArgument(cmdLine, passes);

        return true;
    }

    private static String getStringArgument(CommandLine cmdLine, Option inputFile) {
        return (String) cmdLine.getValue(inputFile);
    }

    private static boolean getBooleanArgument(CommandLine cmdLine, Option option) {
        return cmdLine.hasOption(option);
    }

    private static int getIntegerArgument(CommandLine cmdLine, Option features) {
        return Integer.parseInt((String) cmdLine.getValue(features));
    }

    private static double getDoubleArgument(CommandLine cmdLine, Option op) {
        return Double.parseDouble((String) cmdLine.getValue(op));
    }

    public static OnlineLogisticRegression getModel() {
        return model;
    }

    public static LogisticModelParameters getParameters() {
        return lmp;
    }

    static BufferedReader open(String inputFile) throws IOException {
        InputStream in;
        try {
            in = Resources.getResource(inputFile).openStream();
        } catch (IllegalArgumentException e) {
            in = new FileInputStream(new File(inputFile));
        }
        return new BufferedReader(new InputStreamReader(in, Charsets.UTF_8));
    }
}
