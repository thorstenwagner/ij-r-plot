package de.biomedical_imaging.ij.rplot;
import ij.IJ;


import java.awt.Canvas;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import org.rosuda.REngine.REXP;
import org.rosuda.REngine.Rserve.RConnection;


public class RPlot extends Canvas{

	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	String cmd;
	
	public RPlot(){
		if(IJ.isLinux()){
			cmd = "R";
		} 
		else if(IJ.isWindows()){
			
			//Load R.bat from ressources and write it into temp directory of imagej
			try {
				cmd = ExportResource("/R.bat").replace('/', '\\');
			} catch (Exception e) {
				
				IJ.error(e.getMessage());
			}
			
			//cmd = plugins+"ndef\\R.bat";
			//IJ.log("PFAD: " + cmd);
		}
		StartRserve.launchRserve(cmd);
	}
	
	 /**
     * Export a resource embedded into a Jar file to the local file path.
     *
     * @param resourceName ie.: "/SmartLibrary.dll"
     * @return The path to the exported resource
     * @throws Exception
     */
    public String ExportResource(String resourceName) throws Exception {
        InputStream stream = null;
        OutputStream resStreamOut = null;
        String tmpFolder;
        try {
            stream = this.getClass().getResourceAsStream(resourceName);//note that each / is a directory down in the "jar tree" been the jar the root of the tree
            if(stream == null) {
            	IJ.error("Cannot get resource \"" + resourceName + "\" from Jar file.");
                throw new Exception("Cannot get resource \"" + resourceName + "\" from Jar file.");
            }

            int readBytes;
            byte[] buffer = new byte[4096];
            File folderDir = new File(IJ.getDirectory("imagej")+"/.particlesizer");

            // if the directory does not exist, create it
            if (!folderDir.exists()) {
            	folderDir.mkdir();
            }
            tmpFolder = folderDir.getPath().replace('\\', '/');
            resStreamOut = new FileOutputStream(tmpFolder + resourceName);
            while ((readBytes = stream.read(buffer)) > 0) {
                resStreamOut.write(buffer, 0, readBytes);
            }
        } catch (Exception ex) {
        	IJ.error(ex.getMessage());
            throw ex;
        } finally {
            stream.close();
            resStreamOut.close();
        }

        return tmpFolder + resourceName;
    }
	
	
	public void hist(int column, String title, String xlab, String ylab, boolean addDensity, boolean addNormal,boolean addlognormal, boolean showbars) {
		HistogramFrame frame = new HistogramFrame(column, title, xlab, ylab, addDensity, addNormal, addlognormal, showbars); 
		
		//frame.pack();
		//GUI.center(frame);
		//frame.setVisible(true);
	}
	
	
	public void density(double[] x, String title, String xlab, String ylab) {
		
		StartRserve.launchRserve(cmd);
		RConnection c;
		try {
			
			String tmp = IJ.getDirectory("temp");
			tmp = tmp.replace("\\", "\\\\");

			c = new RConnection();
			c.eval("setwd(\""+tmp+"\")");
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


