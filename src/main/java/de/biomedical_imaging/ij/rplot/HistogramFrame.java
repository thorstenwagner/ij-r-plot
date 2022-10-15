package de.biomedical_imaging.ij.rplot;

import java.awt.Frame;
import java.awt.Point;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.font.FontRenderContext;
import java.awt.geom.AffineTransform;

import ij.IJ;
import ij.ImagePlus;
import ij.gui.GUI;
import ij.gui.GenericDialog;
import ij.gui.ImageCanvas;
import ij.gui.ImageWindow;
import ij.io.Opener;
import ij.io.SaveDialog;
import ij.measure.ResultsTable;
import ij.Prefs;

import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;

import org.rosuda.REngine.REXP;
import org.rosuda.REngine.REXPMismatchException;
import org.rosuda.REngine.REngine;
import org.rosuda.REngine.REngineException;
import org.rosuda.REngine.Rserve.RConnection;
import org.rosuda.REngine.Rserve.RserveException;

public class HistogramFrame extends Frame {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private int column;
	private double[] data;
	private String title;
	private String xlab;
	private String ylab;
	private boolean addDensity;
	private boolean addNormal;
	private boolean addlognormal;
	private boolean showbars;
	private int numberOfBins;
	private String filename;
	private ImagePlus imp;
	private String unit;

	
	public HistogramFrame(int column, String title, String xlab, String ylab,
			boolean addDensity, boolean addNormal, boolean addlognormal,
			boolean showbars) {
		// TODO Auto-generated constructor stub
		this.column = column;
		this.title = title;
		this.xlab = xlab;
		this.ylab = ylab;
		this.addDensity = addDensity;
		this.addNormal = addNormal;
		this.addlognormal = addlognormal;
		this.showbars = showbars;
		this.unit = IJ.getImage().getCalibration().getUnit();
		updateData(true);
		
		filename = plotHist();
		Opener opener = new Opener();  
	
		imp =  opener.openImage(filename);
		
		imp.show();
		imp.setTitle("Histogram");
		addSettingsText(imp);
		imp.getCanvas().disablePopupMenu(true);
		configureListeners(imp.getCanvas());
	}
	
	private void updateData(boolean updatebins){
		data = ResultsTable.getResultsTable().getColumnAsDoubles(column);
		if(updatebins){
			numberOfBins = getNumberOfBins();
		}
	}
	
	public void updatePlot(){
		updateData(false);
		filename = plotHist();
		//this.remove(ic);
		Point p = imp.getWindow().getLocation(); //Save the position
		imp.close();
		imp = IJ.openImage(filename); 
		ImageWindow.setNextLocation(p); //Restore it at the same position
		imp.show();
		addSettingsText(imp);
		imp.getCanvas().disablePopupMenu(true);
		configureListeners(imp.getCanvas());
		pack();
		GUI.center(this);
		/*
		ic = new ImageCanvas(imp); 
		ic.disablePopupMenu(true);
		configureListeners(ic);
		this.add(ic);
		this.setVisible(false);
		this.pack(); 
		this.setLocationRelativeTo(null); 
		this.setVisible(true); 
		*/
	}
	
	public void addSettingsText(ImagePlus imp){
		if(!imp.isVisible()){
			return;
		}
		String imagepath = Prefs.get("ndef.used.path", "Not readable");
		String rollingBallRadius = Prefs.get("ndef.used.rollingBallRadius", "Not readable");
		String localThresholdWindowSize = Prefs.get("ndef.used.localThresholdWindowSize", "Not readable");
		String sizeRange = Prefs.get("ndef.used.sizeRange", "Not readable");
		String feretMinRange = Prefs.get("ndef.used.feretMinRange", "Not readable");
		String filterSolidity = Prefs.get("ndef.used.filterSolidity", "Not readable");
		String convexity = Prefs.get("ndef.used.convexity", "Not readable");
		String objectIntensityThreshold = Prefs.get("ndef.used.objectIntensityThreshold", "Not readable");
		String smoothingFactor = Prefs.get("ndef.used.smoothingFactor","Not readable");
		String doIrregularWatershed = Prefs.get("ndef.doIrregularWatershed","Not readable");
		String iwsConvexityThreshold = Prefs.get("ndef.ConvexityThreshold","Not readable");
		String numberOfParticles = Prefs.get("ndef.NumberOfParticles","Not readable");
		String useSingleParticleMode = Prefs.get("ndef.useSingleParticleMode", "Not readable");
		IJ.run(imp, "Canvas Size...", "width=640 height=580 position=Top-Left zero");
		IJ.setForegroundColor(255, 255, 255);
		
		AffineTransform affinetransform = new AffineTransform();     
		FontRenderContext frc = new FontRenderContext(affinetransform,true,true); 
		int width = (int)imp.getProcessor().getFont().getStringBounds("Path: "+imagepath, frc).getWidth();
		
		String threepoints = "";
		while(width>630){
			imagepath = imagepath.substring(2);
			width = (int)imp.getProcessor().getFont().getStringBounds("Path: "+imagepath, frc).getWidth();
			threepoints="...";
		}
		imp.getProcessor().drawString("Path: " + threepoints+imagepath, 2, 500);
		imp.getProcessor().drawString("Number of particles: " + numberOfParticles, 2, 520);
		imp.getProcessor().drawString("Circular window size: " + localThresholdWindowSize, 2, 540);
		imp.getProcessor().drawString("Rolling ball radius: " + rollingBallRadius, 2, 560);
		imp.getProcessor().drawString("Averaged minimal object intensity: " + objectIntensityThreshold, 2, 580);
		
		imp.getProcessor().drawString("Area Range: " + sizeRange, 250, 520);
		imp.getProcessor().drawString("Feret Min Range.: " + feretMinRange, 250, 540);
		imp.getProcessor().drawString("Convexity Range: " + convexity, 250, 560);
		imp.getProcessor().drawString("Solidity Range: " + filterSolidity, 250, 580);
		
		imp.getProcessor().drawString("Smoothing factor: " + smoothingFactor, 450, 520);
		imp.getProcessor().drawString("Irregular Watershed (IWS): " + doIrregularWatershed , 450, 540);
		imp.getProcessor().drawString("IWS Convextiy Threshold: " + iwsConvexityThreshold, 450, 560);
		imp.getProcessor().drawString("Singe particle mode: " + useSingleParticleMode, 450, 580);
		imp.repaintWindow();
	}
	
	public void configureListeners(ImageCanvas ic){
		final JPopupMenu popup = new JPopupMenu();
		
		JMenuItem menuItem = new JMenuItem("Modify Plot");
		menuItem.addMouseListener(new MouseListener() {
			
			@Override
			public void mouseReleased(MouseEvent arg0) {
				// TODO Auto-generated method stub
				if(arg0.getButton() ==MouseEvent.BUTTON1){
					GenericDialog changePlot = new GenericDialog("Change Plot");
					changePlot.addStringField("Title", title);
					changePlot.addStringField("X-axis label", xlab);
					changePlot.addStringField("Y-axis label", ylab);
					changePlot.addNumericField("Number of bins", numberOfBins, 0);
					
					
					String[] fitDistrItems = {"None","Normal fit","log-normal fit","Kernel density estimate"};
					changePlot.addChoice("Fit Distribution", fitDistrItems, fitDistrItems[0]);
					changePlot.addCheckbox("Show histogram bars", showbars);
					changePlot.showDialog();
					boolean showbarshelp = false;
					if(changePlot.wasOKed()){
						title = changePlot.getNextString();
						xlab = changePlot.getNextString();
						ylab = changePlot.getNextString();
						numberOfBins = (int)changePlot.getNextNumber();
						
						String fitdistrChoice = changePlot.getNextChoice();
						
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
						showbars = (changePlot.getNextBoolean()||showbarshelp);
						updatePlot();
					}
				}
			}
			
			@Override
			public void mousePressed(MouseEvent arg0) {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void mouseExited(MouseEvent arg0) {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void mouseEntered(MouseEvent arg0) {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void mouseClicked(MouseEvent arg0) {
				
		
				//String newxlab = changePlot.getNextString();
				
				
				
			}
		});
		popup.add(menuItem);
		
		JMenuItem saveItem = new JMenuItem("Save image");
		saveItem.addMouseListener(new MouseListener() {
			
			@Override
			public void mouseReleased(MouseEvent arg0) {
				// TODO Auto-generated method stub
				if(arg0.getButton() ==MouseEvent.BUTTON1){
					SaveDialog sd = new SaveDialog("Save Histogram", "histogram", ".tif");
					if(sd.getDirectory()!=null && sd.getFileName() != null ){
						IJ.save(imp, sd.getDirectory()+sd.getFileName());
					}
				}
			}
			
			@Override
			public void mousePressed(MouseEvent arg0) {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void mouseExited(MouseEvent arg0) {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void mouseEntered(MouseEvent arg0) {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void mouseClicked(MouseEvent arg0) {
				
				
				//String newxlab = changePlot.getNextString();
				
				
				
			}
		});
		     
		popup.add(saveItem);
		
		JMenuItem refreshItem = new JMenuItem("Refresh");
		refreshItem.addMouseListener(new MouseListener() {
			
			@Override
			public void mouseReleased(MouseEvent arg0) {
				if(arg0.getButton() ==MouseEvent.BUTTON1){
					updatePlot();
				}
				
			}
			
			@Override
			public void mousePressed(MouseEvent arg0) {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void mouseExited(MouseEvent arg0) {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void mouseEntered(MouseEvent arg0) {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void mouseClicked(MouseEvent arg0) {
				// TODO Auto-generated method stub
				
			}
		});
		popup.add(refreshItem);
		
		     
		ic.addMouseListener(new MouseListener() {
				
		@Override
		public void mouseReleased(MouseEvent e) {
			// TODO Auto-generated method stub
				
			showPopup(e);
		}
				
		@Override
		public void mousePressed(MouseEvent e) {
			// TODO Auto-generated method stub
			showPopup(e);
		}
		
		@Override
		public void mouseExited(MouseEvent e) {
			// TODO Auto-generated method stub
			
		}
		
		@Override
		public void mouseEntered(MouseEvent e) {
			// TODO Auto-generated method stub
					
		}
				
		@Override
		public void mouseClicked(MouseEvent arg0) {
			// TODO Auto-generated method stub
					
		}
				
		private void showPopup(MouseEvent e) {
		     
		    if (e.isPopupTrigger()) {
		         popup.show(e.getComponent(),
		           e.getX(), e.getY());
		   }
		}
	   });
	}
	
	private int getNumberOfBins(){
		RConnection c = null;
		String tmp = "";
		int nob = 0;
		double[] data = ResultsTable.getResultsTable().getColumnAsDoubles(column);
		try {
			tmp = IJ.getDirectory("temp");
			tmp = tmp.replace("\\", "\\\\");
			c = StartRserve.c; //new RConnection();
			c.eval("setwd(\"" + tmp + "\")");
			c.assign("datax", data);
			nob  = c.eval("nclass.Sturges(datax)*2").asInteger();
		}
		catch(Exception e){
			e.printStackTrace();
		}
		return nob;
		
	}

	private String plotHist() {
		RConnection c;
		//RConnection.getLastEngine()
		String tmp = "";
		String filename = "";
	//	updateData();
		try {
			tmp = IJ.getDirectory("temp");
			tmp = tmp.replace("\\", "\\\\");
			c = StartRserve.c;//new RConnection();
			c.eval("setwd(\"" + tmp + "\")");
			c.assign("datax", data);
			filename = "histogram_" + getStamp() + ".png";
			c.eval("png(file=\"" + filename + "\", 640, 480)");
			String Rcode = "library(\"MASS\");";
			if (addNormal) {
				Rcode += "f <- fitdistr(datax, \"normal\");";
				Rcode += "xfit<-seq(min(datax),max(datax),length.out=300);";
				Rcode += "yfit<-dnorm(xfit,mean=f$estimate[\"mean\"],sd=f$estimate[\"sd\"]);";
			}
			if (addlognormal) {
				Rcode += "f <- fitdistr(datax, \"lognormal\");";
				Rcode += "xfit<-seq(min(datax),max(datax),length.out=300);";
				Rcode += "meanlog <- f$estimate[\"meanlog\"];";
				Rcode += "sdlog <- f$estimate[\"sdlog\"];";
				Rcode += "yfit<-dlnorm(xfit,meanlog=meanlog,sdlog=sdlog);";
			}
			
			if (showbars && (addNormal || addlognormal)) {
				Rcode += "h<-hist(datax,breaks="
						+ numberOfBins
						+ ",density=10,main=\""
						+ title
						+ "\",xlab=\""
						+ xlab
						+ "\",ylab=\""
						+ ylab
						+ "\",prob=TRUE,cex=2,plot=FALSE);";
				Rcode += "h<-hist(datax,breaks="
						+ numberOfBins
						+ ",density=10,ylim=c(0,max(max(yfit),max(h$density))),main=\""
						+ title
						+ "\",xlab=\""
						+ xlab
						+ "\",ylab=\""
						+ ylab
						+ "\",prob=TRUE,cex=2);";
			} else if (showbars && !addNormal && !addlognormal) {
				Rcode += "h<-hist(datax,breaks="
						+ numberOfBins
						+ ",density=10,main=\""
						+ title
						+ "\",xlab=\""
						+ xlab
						+ "\",ylab=\""
						+ ylab
						+ "\",prob=TRUE,cex=2,plot=FALSE);";
				Rcode += "h<-hist(datax,breaks="
						+ numberOfBins
						+ ",density=10,main=\""
						+ title
						+ "\",xlab=\""
						+ xlab
						+ "\",ylab=\""
						+ ylab
						+ "\",prob=TRUE,cex=2);";
				
			} else if (!showbars && !addNormal && !addlognormal) {
				Rcode += "h<-hist(datax,breaks="
						+ numberOfBins
						+ ",density=10,main=\""
						+ title
						+ "\",xlab=\""
						+ xlab
						+ "\",ylab=\""
						+ ylab
						+ "\",prob=TRUE,cex=2,plot=FALSE);";
			} else {
				Rcode += "h<-hist(datax,breaks="
						+ numberOfBins
						+ ",density=10,main=\""
						+ title
						+ "\",xlab=\""
						+ xlab
						+ "\",ylab=\""
						+ ylab
						+ "\",plot=FALSE);";
				Rcode += "plot(h$mids,h$density,pch=18,ylim=c(0,max(max(yfit),max(h$density))),main=\""
						+ title
						+ "\",xlab=\""
						+ xlab
						+ "\",ylab=\""
						+ ylab
						+ "\",cex=2);";
			}
			if (addDensity) {
				Rcode += "lines(density(datax),col=\"darkblue\",lwd=4);";
				Rcode += "median <- round(median(datax),3);";
				Rcode += "mtext(substitute(paste('x'[50],\"=\",med),list(med=median)));";
			}
			else if (addNormal) {
				Rcode += "lines(xfit, yfit, col=\"darkblue\", lwd=4);";
				Rcode += "mean <- round(f$estimate[\"mean\"],3);";
				Rcode += "meanerr <- round(f$sd[\"mean\"],4);";
				Rcode += "sd <- round(f$estimate[\"sd\"],3);";
				Rcode += "sderr <- round(f$sd[\"sd\"],4);";
				Rcode += "median <- round(median(datax),3);";
				//Rcode += "mtext(paste(\"median=\",median,\" Fit (normal): mean = \",mean,\" (\",meanerr,\") sd = \",sd,\" (\",sderr,\")\"));";
				Rcode += "mtext(substitute(paste('x'[50],\"=\",med,\" \",mu,\"=\",m,\" (\",mr,\") \",sigma,\"=\",sd,\" (\",sdr,\")\"),list(med=median,m=mean,mr=meanerr,sd=sd,sdr=sderr)),side=3);";

			}
			else if (addlognormal) {
				Rcode += "lines(xfit, yfit, col=\"darkblue\", lwd=4);";
				Rcode += "emode <- exp(meanlog-sdlog^2);"; // Exponential
																	// of the
																	// mode
				Rcode += "emode <- as.numeric(prettyNum(emode, digits=3));";
				Rcode += "emean <- exp(meanlog+0.5*sdlog^2);"; // Exponential
																		// of
																		// mean
				Rcode += "emean <- as.numeric(prettyNum(emean, digits=3));";
				Rcode += "emeanerr <- exp(f$sd[\"meanlog\"]);";
				Rcode += "emeanerr <- as.numeric(prettyNum(emeanerr, digits=3));";
				Rcode += "esd <- exp(meanlog+0.5*sdlog^2)*sqrt(exp(sdlog^2)-1);"; // Exponential
																							// of
																							// the
																							// standard
																							// deviation
				
				Rcode += "esd <- as.numeric(prettyNum(esd, digits=3));";
				Rcode += "esderr <- exp(f$sd[\"sdlog\"]);";
				Rcode += "esderr <- as.numeric(prettyNum(esderr, digits=3));";
				Rcode += "median <- median(datax);";
				Rcode += "median <- as.numeric(prettyNum(median, digits=3));";
				//Rcode += "mtext(paste(\"median=\",median,\" Fit (log-normal): mode = \",emode,\" mean = \",emean,\" (\",emeanerr,\") sd = \",esd,\" (\",esderr,\")\"));";
				Rcode += "mtext(substitute(paste('x'[50],"
						+ "\"=\","
						+ "med,"
						+ "\" \","
						+ "\" "+unit+"\","
						+ "\" mode=\","
						+ "mo,"
						+ "\" \","
						+ "\" "+unit+"\","
						+ "\" \","
						+ "mu,"
						+ "\"=\","
						+ "m,"
						+ "\"  \","
						+ "\" "+unit+"\","
						+ "\"  \","
						+ "sigma,"
						+ "\"=\","
						+ "sd,\" \",\""+unit+"\"),list(med=format(median,scientific=TRUE),mo=format(emode,scientific=TRUE),m=format(emean,scientific=TRUE),sd=format(esd,scientific=TRUE))),side=3)";
			}
			else{
				Rcode += "median <- median(datax);";
				Rcode += "median <- as.numeric(prettyNum(median, digits=3));";
			//	Rcode += "mtext(substitute(paste('x'[50],\"=\",med),list(med=median)));";
				Rcode += "mtext(substitute(paste('x'[50],\"=\",medtxt,\" \",\""+unit+"\"),list(medtxt=format(median,scientific=TRUE))));";
			
			}
			c.eval(Rcode);
			c.eval("dev.off()");
			
		} catch (Exception e) {
			// TODO Auto-generated catch block
			IJ.log("LM " + e.getLocalizedMessage());
			IJ.log("M " + e.getMessage());
			IJ.log("S " + e.toString());
			//e.printStackTrace();
		}

		return tmp + filename;
	}
	
	
	private String getStamp() {
		 java.util.Date date= new java.util.Date();
		 String stamp = ""+date.getYear()+""+date.getMonth()+""+date.getDay()+"-"+date.getHours()+""+date.getMinutes()+""+date.getSeconds();
		 return stamp;
	}

}
