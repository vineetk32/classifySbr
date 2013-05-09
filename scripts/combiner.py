#!/bin/python

import sys,csv

inputFile = sys.argv[1]
outputFile = sys.argv[2]

fin = open(inputFile,'r')
fout = open(outputFile,'w')

csvReader = csv.reader(fin,delimiter=',',quotechar='"')
csvWriter = csv.writer(fout,delimiter=',',quotechar='"')

for row in csvReader:
	textField = row[1] + ' ' + row[3] + ' ' + row[4] + ' ' + row[5]
	csvWriter.writerow([textField,row[10]])

