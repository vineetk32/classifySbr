#!/bin/python
import sys,csv
from copy import deepcopy

lineNum = 0
trainOut = open(sys.argv[2],'w')
testOut = open(sys.argv[3],'w')
fin = open(sys.argv[1],'r')
csvReader = csv.reader(fin,delimiter=',',quotechar='"')
testWriter = csv.writer(testOut,delimiter=',',quotechar='"')
trainWriter = csv.writer(trainOut,delimiter=',',quotechar='"')
for inputRow in csvReader:
	testRow = deepcopy(inputRow)
	if lineNum == 0:
		trainWriter.writerow(inputRow)
		testRow.append('actual')
		testWriter.writerow(testRow)
	elif (lineNum % 5 == 0):
		testRow.append(inputRow[6])
		testRow[6] = ''
		testWriter.writerow(testRow)
	else:
		trainWriter.writerow(inputRow)
	lineNum += 1
