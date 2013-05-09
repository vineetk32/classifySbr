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
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.apache.commons.cli2.CommandLine;
import org.apache.commons.cli2.Group;
import org.apache.commons.cli2.Option;
import org.apache.commons.cli2.builder.ArgumentBuilder;
import org.apache.commons.cli2.builder.DefaultOptionBuilder;
import org.apache.commons.cli2.builder.GroupBuilder;
import org.apache.commons.cli2.commandline.Parser;
import org.apache.commons.cli2.util.HelpFormatter;
import org.apache.mahout.classifier.sgd.CsvRecordFactory;
import org.apache.mahout.classifier.sgd.LogisticModelParameters;
import org.apache.mahout.classifier.sgd.OnlineLogisticRegression;
import org.apache.mahout.math.SequentialAccessSparseVector;
import org.apache.mahout.math.Vector;

public final class CSVTester {

    private static String inputFile;
    private static String modelFile;
    private static String categoryFileName;
    private static final int actualValueIndex = 1;

    private CSVTester() {
    }

    public static void main(String[] args) throws Exception {
        mainToOutput(args, new PrintWriter(new OutputStreamWriter(System.out, Charsets.UTF_8), true));
    }

    static void mainToOutput(String[] args, PrintWriter output) throws Exception {
        String[] fields;
        int totalRecs = 0, correctPredictions = 0,totalCategories = 0;
        float accuracy;
        HashMap predictionMap = new HashMap();

        if (parseArgs(args)) {

            LogisticModelParameters lmp = LogisticModelParameters.loadFrom(new File(modelFile));

            CsvRecordFactory csv = lmp.getCsvRecordFactory();
            OnlineLogisticRegression lr = lmp.createRegression();
            BufferedReader in = CSVTrainer.open(inputFile);
            String line = in.readLine();
            List<String> targetCategories = Lists.newArrayList();
            //targetCategories = csv.getTargetCategories();
            csv.firstLine(line);
            line = in.readLine();

            BufferedReader fin = new BufferedReader(new FileReader(categoryFileName));
            String line2;

            while ((line2 = fin.readLine()) != null) {
                if (!targetCategories.contains(line2.trim())) {
                    totalCategories++;
                    targetCategories.add(line2.trim());
                }
            }
            fin.close();
            lmp.setTargetCategories(targetCategories);

            while (line != null) {
                Vector v = new SequentialAccessSparseVector(lmp.getNumFeatures());
                int target = csv.processLine(line, v);
                fields = line.split(",");

                Vector scores = lr.classifyFull(v);
                String predictedCategory = targetCategories.get(scores.maxValueIndex());

                try {
                    //System.out.printf("\nComparing %s and %s",predictedCategory,fields[actualValueIndex]);
                    //System.out.printf("Predicted %d\n", scores.maxValueIndex());
                    if (fields[actualValueIndex].indexOf(predictedCategory) != -1) {
                        correctPredictions++;
                    }
                    totalRecs++;
                } catch (java.lang.ArrayIndexOutOfBoundsException ex) {
                    //Nothing to check against!
                    ;
                }
                if (predictionMap.containsKey(predictedCategory)) {
                    predictionMap.put(predictedCategory, ((Integer) predictionMap.get(predictedCategory)).intValue() + 1);
                } else {
                    predictionMap.put(predictedCategory, new Integer(1));
                }

                line = in.readLine();
            }
            System.out.println("\nClassification Counts - \n");
            Set set = predictionMap.entrySet();
            Iterator i = set.iterator();
            while (i.hasNext()) {
                Map.Entry me = (Map.Entry) i.next();
                System.out.print(me.getKey() + ": ");
                System.out.println(me.getValue());
            }
            accuracy = (float) correctPredictions / totalRecs * 100;
            System.out.printf("\nTotal Records - %d, Correct Predictions - %d, Accuracy - %.2f\n", totalRecs, correctPredictions, accuracy);
        }
    }

    private static boolean parseArgs(String[] args) {
        DefaultOptionBuilder builder = new DefaultOptionBuilder();

        Option help = builder.withLongName("help").withDescription("print this list").create();

        Option quiet = builder.withLongName("quiet").withDescription("be extra quiet").create();

        ArgumentBuilder argumentBuilder = new ArgumentBuilder();
        Option inputFileOption = builder.withLongName("input")
                .withRequired(true)
                .withArgument(argumentBuilder.withName("input").withMaximum(1).create())
                .withDescription("where to get training data")
                .create();

        Option modelFileOption = builder.withLongName("model")
                .withRequired(true)
                .withArgument(argumentBuilder.withName("model").withMaximum(1).create())
                .withDescription("where to get a model")
                .create();

        Option categoryFile = builder.withLongName("categories")
                .withRequired(true)
                .withArgument(argumentBuilder.withName("categories").withMaximum(1).create())
                .withDescription("The file containing all the possible categories.")
                .create();

        Group normalArgs = new GroupBuilder()
                .withOption(help)
                .withOption(quiet)
                //.withOption(auc)
                //.withOption(scores)
                //.withOption(confusion)
                .withOption(inputFileOption)
                .withOption(modelFileOption)
                .withOption(categoryFile)
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

        inputFile = getStringArgument(cmdLine, inputFileOption);
        modelFile = getStringArgument(cmdLine, modelFileOption);
        categoryFileName = getStringArgument(cmdLine, categoryFile);
        /*showAuc = getBooleanArgument(cmdLine, auc);
         showScores = getBooleanArgument(cmdLine, scores);
         showConfusion = getBooleanArgument(cmdLine, confusion);*/

        return true;
    }

    private static boolean getBooleanArgument(CommandLine cmdLine, Option option) {
        return cmdLine.hasOption(option);
    }

    private static String getStringArgument(CommandLine cmdLine, Option inputFile) {
        return (String) cmdLine.getValue(inputFile);
    }
}
