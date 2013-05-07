package eu.brede.graspj.gui.tools;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.TimerTask;
import java.util.concurrent.Semaphore;

import eu.brede.common.util.SimpleLock;

public class SimTask extends TimerTask {
	FileInputStream in;
	FileOutputStream out;
	
	SimpleLock runLock = new SimpleLock();
	boolean paused = false;
	
	public SimTask(File srcFile, File dstFile) {
//		String srcName = "C:\\users\\nbrede\\Desktop\\mito-storm_000.dax";
//		String srcName = "D:\\nbrede\\3d\\movie_0_2.dax";
//		String dstName = "D:\\nbrede\\mito-storm_000_copy.dax";
		try {
			in = new FileInputStream(srcFile);
			out = new FileOutputStream(dstFile);
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	@Override
	public boolean cancel() {
		runLock.lock();
		try {in.close();} catch (IOException ex) {}
    	try {out.close();} catch (IOException ex) {}
    	paused = true;
    	runLock.unlock();
		return super.cancel();
	}
	
	public void setPaused(boolean paused) {
		this.paused = paused;
	}
	
	public boolean isPaused() {
		return paused;
	}
	
	@Override
	public void run() {
		runLock.lock();
		if(paused) {
			runLock.unlock();
			return;
		}
		int bytesToRead = 131072*4; // 1 frame, 4 frames
        byte[] readBytes = new byte[bytesToRead];
        try {
            int actuallyRead = in.read(readBytes);
            if(actuallyRead>0) {
            	out.write(readBytes, 0, actuallyRead);
            }
            else {
            	cancel();
//            	in.close();
//            	out.close();
            }
        }
        catch (IOException exception) {
        	exception.printStackTrace();
        	cancel();
        }
        runLock.unlock();
	}
	
}
