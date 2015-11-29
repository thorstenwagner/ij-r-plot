package de.biomedical_imaging.ij.rplot;
import java.util.Random;

import ij.IJ;
import ij.measure.ResultsTable;
import ij.plugin.PlugIn;
import ij.plugin.filter.Analyzer;



public class ExampleResultTable_ implements PlugIn {

	@Override
	public void run(String arg) {
		// TODO Auto-generated method stub
		ResultsTable rt = Analyzer.getResultsTable();

		if(rt==null)
		{
			 rt = new ResultsTable();
			 
			 Analyzer.setResultsTable(rt);
		}
		
		int[] x = {1,2,3,4,5,6,7,8,9,10,2,3,4,5,6,7,8,9,10,2,3,4,5,6,7,8,9,10};
		int[] y = {10,9,8,7,6,5,4,3,2,1,9,8,7,6,5,4,3,2,1,9,8,7,6,5,4,3,2,1};
		Random rng = new Random();
		for(int i = 0; i < x.length; i++){
			rt.incrementCounter();
			rt.addValue("X", x[i]);
			rt.addValue("Y", y[i]);
			rt.addValue("Normal",5+rng.nextGaussian());
		}
		
		rt.show("Data");
	}

}