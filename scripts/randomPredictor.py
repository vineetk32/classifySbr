#!/bin/python
import csv,sys,random

categoriesFile = open(sys.argv[1],'r')
testFile = open(sys.argv[2],'r')

categoriesList = []

for line in categoriesFile.readlines():
	categoriesList.append(line.strip())


csvReader = csv.reader(testFile,delimiter=',',quotechar='"')

random.seed()
correctPredictions = 0
totalRecords = 0
for row in csvReader:
	currPrediction = categoriesList[random.randrange(0,len(categoriesList))]
	if row[10].strip() == '':
		pass
	elif row[10].count(currPrediction) > 0:
		correctPredictions += 1
		totalRecords += 1
	else:
		totalRecords += 1

print 'Total Records - %d, Correct Predictions - %d, Accuracy - %.2f' % (totalRecords,correctPredictions, float(correctPredictions)/totalRecords * 100)
