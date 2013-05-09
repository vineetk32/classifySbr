import csv,re

fin = open('closed_kcs_cases_Oct24_2012.csv','r')
fout = open('fixed.csv','w')
csvreader = csv.reader(fin,delimiter=',',quotechar='"')
csvwriter = csv.writer(fout,delimiter=',',quotechar='"')
for row in csvreader:
	if row[6].count('/') > 0 or row[6].count('&') > 0 or row[6].count('\r\n') > 0:
		newSpr = row[6].replace('/',',').replace('&',',').replace(' , ',',')
		#newSpr = re.findall(r"[\w ]+",row[6])
		newRow = row[0:6]
		#newRow.append(','.join(newSpr))
		#for spr in newSpr.split(','):
		#	print "'" + spr + "'"
		newRow.append(newSpr)
		newRow.extend(row[7:])
		csvwriter.writerow(newRow)
	else:
		csvwriter.writerow(row)
fout.close()
fin.close()
