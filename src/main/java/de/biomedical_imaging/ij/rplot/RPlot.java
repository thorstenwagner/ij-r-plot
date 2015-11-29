package de.biomedical_imaging.ij.rplot;
import ij.IJ;
import ij.ImagePlus;
import ij.gui.GUI;
import ij.gui.GenericDialog;
import ij.gui.ImageCanvas;

import java.awt.Canvas;
import java.awt.MenuItem;
import java.awt.PopupMenu;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

import javax.swing.JFrame;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.event.MenuDragMouseEvent;
import javax.swing.event.MenuDragMouseListener;

import org.rosuda.REngine.REXP;
import org.rosuda.REngine.Rserve.RConnection;


public class RPlot extends Canvas{

	
	String cmd;
	
	public RPlot(){
		if(IJ.isLinux()){
			cmd = "R";
		} 
		else if(IJ.isWindows()){
			String plugins = IJ.getDirectory("plugins");
			cmd = plugins+"ndef\\R.bat";
			//IJ.log("PFAD: " + cmd);
		}
		StartRserve.launchRserve(cmd);
	}
	
	public void hist(int column, String title, String xlab, String ylab, boolean addDensity, boolean addNormal,boolean addlognormal, boolean showbars) {
		HistogramFrame frame = new HistogramFrame(column, title, xlab, ylab, addDensity, addNormal, addlognormal, showbars); 

		//frame.pack();
		//GUI.center(frame);
		//frame.setVisible(true);
	}
	
	private String plotHist(double[] x, String title, String xlab, String ylab, boolean addDensity, boolean addNormal,boolean addlognormal, boolean showbars){
		
		RConnection c;
		String tmp = "";
		String filename = "";
		try {
			tmp = IJ.getDirectory("temp");
			tmp = tmp.replace("\\", "\\\\");
			c = new RConnection();
			c.eval("setwd(\""+tmp+"\")");
			c.assign("datax",x);
			filename = "histogram_"+getStamp()+".png";
			
			c.eval("png(file=\""+filename+"\", 640, 480)");
			String Rcode ="library(\"MASS\");";
			
			if(addNormal){
				Rcode += "f <- fitdistr(datax, \"normal\");";
				Rcode += "xfit<-seq(min(datax),max(datax),length.out=300);";
				Rcode += "yfit<-dnorm(xfit,mean=f$estimate[\"mean\"],sd=f$estimate[\"sd\"]);";
			}
			if(addlognormal){
				Rcode += "f <- fitdistr(datax, \"lognormal\");";
				Rcode += "xfit<-seq(min(datax),max(datax),length.out=300);";
				Rcode += "meanlog <- f$estimate[\"meanlog\"];";
				Rcode += "sdlog <- f$estimate[\"sdlog\"];";
				Rcode += "yfit<-dlnorm(xfit,meanlog=meanlog,sdlog=sdlog);";
			}

			if(showbars && (addNormal || addlognormal)){
				Rcode += "h<-hist(datax,breaks=nclass.Sturges(datax)*2,density=10,main=\""+title+"\",xlab=\""+xlab+"\",ylab=\""+ylab+"\",prob=TRUE,cex=2,plot=FALSE);";
				Rcode += "h<-hist(datax,breaks=nclass.Sturges(datax)*2,density=10,ylim=c(0,max(max(yfit),max(h$density))),main=\""+title+"\",xlab=\""+xlab+"\",ylab=\""+ylab+"\",prob=TRUE,cex=2);";
			} else if(showbars && !addNormal && !addlognormal){
				Rcode += "h<-hist(datax,breaks=nclass.Sturges(datax)*2,density=10,main=\""+title+"\",xlab=\""+xlab+"\",ylab=\""+ylab+"\",prob=TRUE,cex=2,plot=FALSE);";
				Rcode += "h<-hist(datax,breaks=nclass.Sturges(datax)*2,density=10,main=\""+title+"\",xlab=\""+xlab+"\",ylab=\""+ylab+"\",prob=TRUE,cex=2);";
				Rcode += "mtext(paste(\"From Data: median=\",median,\"\"))";
			}
			else if(!showbars && !addNormal && !addlognormal){
				Rcode += "h<-hist(datax,breaks=nclass.Sturges(datax)*2,density=10,main=\""+title+"\",xlab=\""+xlab+"\",ylab=\""+ylab+"\",prob=TRUE,cex=2,plot=FALSE);";
				Rcode += "mtext(paste(\"From Data: median=\",median,\"\"))";
			}
			else{
				Rcode += "h<-hist(datax,breaks=nclass.Sturges(datax)*2,density=10,main=\""+title+"\",xlab=\""+xlab+"\",ylab=\""+ylab+"\",plot=FALSE);";
				Rcode += "plot(h$mids,h$density,pch=18,ylim=c(0,max(max(yfit),max(h$density))),main=\""+title+"\",xlab=\""+xlab+"\",ylab=\""+ylab+"\",cex=2);";
			}
			
			if(addDensity){
				Rcode += "lines(density(datax),col=\"darkblue\",lwd=4)";
			}
			if(addNormal) {
				Rcode += "lines(xfit, yfit, col=\"darkblue\", lwd=4);";
				Rcode += "mean <- round(f$estimate[\"mean\"],3);";
				Rcode += "meanerr <- round(f$sd[\"mean\"],4);";
				Rcode += "sd <- round(f$estimate[\"sd\"],3);";
				Rcode += "sderr <- round(f$sd[\"sd\"],4);";
				Rcode += "mtext(paste(\"From Data: median=\",median,\" ML Fitted Parameters (normal): mean = \",mean,\" (\",meanerr,\") sd = \",sd,\" (\",sderr,\")\"));";
			}
			if(addlognormal) {
				Rcode += "lines(xfit, yfit, col=\"darkblue\", lwd=4);";
				Rcode += "emode <- round(exp(meanlog-sdlog^2),3);"; //Exponential of the mode
				Rcode += "emean <- round(exp(meanlog+0.5*sdlog^2),3);"; //Exponential of mean
				Rcode += "emeanerr <- round(f$sd[\"meanlog\"],4);";
				Rcode += "esd <- round(exp(meanlog+0.5*sdlog^2)*sqrt(exp(sdlog^2)-1),3);"; //Exponential of the standard deviation
				Rcode += "esderr <- round(f$sd[\"sdlog\"],4);";
				Rcode += "median <- median(datax);";
				Rcode += "mtext(paste(\"From Data: median=\",median,\" Fitted Parameters (lognormal): mode = \",emode,\" mean = \",emean,\" (\",emeanerr,\") sd = \",esd,\" (\",esderr,\")\"));";
			}
			
			c.eval(Rcode);
			c.eval("dev.off()");
		}catch(Exception e){
			// TODO Auto-generated catch block
						e.printStackTrace();
		}
		
		return tmp+filename;
	}
	
	public void density(double[] x, String title, String xlab, String ylab) {
		
		StartRserve.launchRserve(cmd);
		RConnection c;
		try {
			
			String tmp = IJ.getDirectory("temp");
			tmp = tmp.replace("\\", "\\\\");

			c = new RConnection();
			REXP p = c.eval("setwd(\""+tmp+"\")");
			c.assign("datax",x);
			String filename = "density_"+getStamp()+".png";

			c.eval("png(file=\""+filename+"\" , 640, 480)");
			c.eval("plot(density(datax),main=\""+title+"\",xlab=\""+xlab+"\",ylab=\""+ylab+"\",adjust=2)");
			c.eval("dev.off()");
			IJ.open(tmp+filename);
			
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 
		
		
	}

	private String getStamp() {
		 java.util.Date date= new java.util.Date();
		 String stamp = ""+date.getYear()+""+date.getMonth()+""+date.getDay()+"-"+date.getHours()+""+date.getMinutes()+""+date.getSeconds();
		 return stamp;
	}
	/*
	@Override
	public void paint(Graphics g) {
		g.drawImage(img, 0, 0, null);
	}
	*/
	

}


