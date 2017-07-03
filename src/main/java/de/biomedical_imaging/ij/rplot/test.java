package de.biomedical_imaging.ij.rplot;

import org.rosuda.REngine.REXP;
import org.rosuda.REngine.REXPMismatchException;
import org.rosuda.REngine.REngineException;
import org.rosuda.REngine.Rserve.RConnection;
import org.rosuda.REngine.Rserve.RserveException;

import ij.IJ;

public class test {

	public static void main(String[] args) {
		RConnection c = StartRserve.c;
		if(c!=null){
			c.close();
			c = null;
		}
		StartRserve.launchRserve("R");
		//RConnection.getLastEngine()
				String tmp = "";
				String filename = "";
			//	updateData();
				
				try {

					c = StartRserve.c;//new RConnection();
					c.eval("library(grDevices)");
					//c.eval("setwd(\"/Users/share/\")");
					c.eval("setwd(\"/Users/twagner/\")");
					c.eval("print(getwd())");
					IJ.log("A");
					c.eval("png(filename=\"abx.png\")");
					c.close();

				} catch (RserveException e) {
					IJ.log("Close: " + c.close());
					c=null;
					e.printStackTrace();
					IJ.log(e.getMessage());
				} catch (REngineException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
					
					IJ.log(e.getMessage());
				}

	}
	
	private static String getStamp() {
		 java.util.Date date= new java.util.Date();
		 String stamp = ""+date.getYear()+""+date.getMonth()+""+date.getDay()+"-"+date.getHours()+""+date.getMinutes()+""+date.getSeconds();
		 return stamp;
	}

}
