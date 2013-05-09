package com.vin.test;

import java.io.BufferedReader;
import java.io.IOException;
import org.apache.mahout.classifier.sgd.CsvRecordFactory;
import org.apache.mahout.classifier.sgd.LogisticModelParameters;
import org.apache.mahout.math.Vector;

public final class CSVHelper
{
    BufferedReader in;
    LogisticModelParameters lmp;
    CsvRecordFactory csv;

    public CSVHelper()
    {}

    public void init(BufferedReader _in,LogisticModelParameters _lmp) throws IOException
    {
        in = _in;
        lmp = _lmp;
        csv = lmp.getCsvRecordFactory();

        // read variable names
        csv.firstLine(in.readLine());

    }

    public int getNextLine(Vector v)
    {
        int targetValue = 0;
		String line;

		try
		{
				line = in.readLine();
	    }
		catch (IOException ex)
		{
			System.out.println("Exception reading line.");
			return -1;
		}

        if (line == null)
        {
            return -1;
        }
        try
        {
            targetValue = csv.processLine(line, v);
        }
        catch (java.lang.IndexOutOfBoundsException ex)
        {
            System.out.println("Exception while processing line - " + line);
            targetValue = -1;
        }
		return targetValue;
    }
}
