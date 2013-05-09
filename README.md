The purpose of this document in to provide as much information as possible to understand, run and modfify the Mahout-SBRS classification.

Files
=====

IndexTrainer.java
-----------------
This is the source file that is used to train the model from an existing Solr Index.
Usage - 
	mvn exec:java  -Dexec.mainClass="com.vin.test.IndexTrainer" -D^Cec.args="--directory /home/vineet/Downloads/solr-4.1.0/example/caseTest/collection1/data/index --categories /home/vineet/mahout/mine/data/allCategories.txt --output ./out2 --predictor text --target sbrs --idField id --features 10000 --passes 5"

If you want to use a Solr 4.1 Index, you *must* use Mahout 0.8.
The 'categories' parameter takes a text file containing all possible categories (separated by newLines) which the target variable can have.
Only a single predictor variable is allowed. This can however be a concatenation of all the required fields, which can be achieved by Solr's copyField directive.


CSVTrainer.java
---------------
This file can be used to train the model based on an input CSV file.
Usage - 

	mvn exec:java  -Dexec.mainClass="com.vin.test.CSVTrainer" -Dexec.args="--input /home/vineet/mahout/MiA/data/english_cases_train.csv --categories /home/vineet/mahout/MiA/data/allCategories.txt --output ./out2 --predictors subject description --target sbrs --types text"

The 'predictors' variable contains all the columns in the CSV which can be used to classify the target.
The 'types' variable specifies the data types of the predictor variables.

Because of the eccentricities of Mahouts CSV processor, make sure the CSV matches the following spec - 
1. The column names need to enclosed in quotes, like - "Product","Subject","Description".
2. Fields cannot be quoted, so fields cannot contain line breaks, or the delimiter.


CSVTester.java
--------------
This file can be used to test the generated model against the test data.
Usage - 
	mvn exec:java  -Dexec.mainClass="com.vin.test.CSVTester" -Dexec.args="--input /home/vineet/mahout/MiA/data/english_combined.csv --model ./out2 --categories /home/vineet/mahout/MiA/data/allCategories.txt"

Although the name of the class is CSVTester, in its current committed state, it is wired to test the output from IndexTrainers model against a test CSV file.
CSVTester expects the actual column to be classified to be blank in the test file. However, to verify the accuracy of the classified value, it needs the actual value, which should be the last field in the CSV.


Other files
-----------
The data folder contains the file used to build the solr index, the categories file and the solrconfig.xml
The scripts folder contains a few scripts used to pre-processing the data - All these scripts were quick 10 min jobs, so dont expect any error handling or comments in them :-)
The source folder contains a few .bak files which contain some test code.


