package de.biomedical_imaging.ij.rplot;

import java.util.HashSet;
import java.util.Set;

import ij.IJ;
import ij.ImagePlus;
import ij.gui.GenericDialog;
import ij.gui.HistogramWindow;
import ij.measure.ResultsTable;
import ij.plugin.Histogram;
import ij.plugin.PlugIn;
import ij.plugin.filter.Analyzer;
import ij.process.FloatProcessor;

public class rHistogram_ implements PlugIn {
	static Set<Integer> ignoreHeadings;
	static {
		ignoreHeadings = new HashSet<Integer>();
		ignoreHeadings.add(0);
	}
	@Override
	public void run(String arg) {
		// TODO Auto-generated method stub
		ResultsTable rt = Analyzer.getResultsTable();
		String[] rtHeadings = rt.getHeadings();
		String[] headings = new String[rtHeadings.length-ignoreHeadings.size()];
		int k = 0;
		for(int i = 0; i < rtHeadings.length;i++){
			if(!ignoreHeadings.contains(i)){
				headings[k] = rtHeadings[i];
				k++;
			}
		}
		GenericDialog gd = new GenericDialog("Distribution");
		if(headings==null||headings.length == 0){
			IJ.error("No data can be plotted");
			return;
		}
		gd.addChoice("Data_column", headings, headings[0]);

		gd.addStringField("Title", "");
		gd.addStringField("X-axis_label", headings[0]);
		gd.addStringField("Y-axis_label", "Frequency");
		gd.addNumericField("Bin width", -1, 0);
		
		String[] fitDistrItems = {"None","Normal fit","log-normal fit","Kernel density estimate"};
		gd.addChoice("Fit Distribution", fitDistrItems, fitDistrItems[2]);
		gd.addCheckbox("Show histogram bars", true);
		gd.showDialog();
		
		
		if (!gd.wasCanceled()) {
			int column = rt.getColumnIndex(gd.getNextChoice());
			//double[] data = rt.getColumnAsDoubles(column);
			
			String title = gd.getNextString();
			String xlab = gd.getNextString();
			if (xlab.isEmpty()) {
				xlab = headings[0];
			}
			String ylab = gd.getNextString();
			double bin_width = (double)gd.getNextNumber();
			
			String fitdistrChoice = gd.getNextChoice();
			boolean addNormal = false;
			boolean addlognormal = false;
			boolean addDensity = false;
			boolean showbarshelp = false;
			if(fitdistrChoice.equals(fitDistrItems[0])){
				addNormal = false;
				addlognormal = false;
				addDensity = false;
				showbarshelp = true;
			}
			else if(fitdistrChoice.equals(fitDistrItems[1])){
				addNormal = true;
				addlognormal = false;
				addDensity = false;
			}
			else if(fitdistrChoice.equals(fitDistrItems[2])){
				addNormal = false;
				addlognormal = true;
				addDensity = false;
			}else if(fitdistrChoice.equals(fitDistrItems[3])){
				addNormal = false;
				addlognormal = false;
				addDensity = true;
				showbarshelp = true;
			}
			boolean showbars = (gd.getNextBoolean()||showbarshelp);
			
	
	
			RPlot test = new RPlot();
			
			if(test.isStartSucceded() ==false){
				double[] data = rt.getColumnAsDoubles(column);
				FloatProcessor fp = new FloatProcessor(data.length, 1, data);
				ImagePlus imhelp = new ImagePlus("h", fp);
				int bins = (int)Math.ceil(Math.log(data.length)/Math.log(2)+1)*2;
				double fpMax = fp.getMax();
				double fpMin = fp.getMin();
				double max_padding = 0;
				if (bin_width!=-1) {
					
					bins = (int)((fpMax-fpMin)/bin_width + 1);
					max_padding = bins*bin_width + fpMin - fpMax;
					IJ.log("" + fp.getMax() +" "+fp.getMin()+" "+bins);
					
				}
				
				
				HistogramWindow hw = new HistogramWindow(title, imhelp, bins, fpMin,fpMax+max_padding);
				hw.setVisible(true);
			}
			else {
				test.hist(column, title, xlab, ylab, addDensity, addNormal, addlognormal,
						showbars);
			}
			
			
		}
		

	}

}
