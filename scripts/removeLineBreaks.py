import csv,re,sys

fout = open(sys.argv[2],'w')
fin = open(sys.argv[1],'r')
csvreader = csv.reader(fin,delimiter=',',quotechar='"')
csvwriter = csv.writer(fout,delimiter=',',quotechar='"')
for row in csvreader:
	newRow = []
	for col in row:
		newRow.append(col.replace("\n","").replace('\r\n','').replace(',',' '))
	#if (newRow[8].strip() == 'en' or newRow[8].strip() == 'case_language'):
	csvwriter.writerow(newRow)
fout.close()
fin.close()
