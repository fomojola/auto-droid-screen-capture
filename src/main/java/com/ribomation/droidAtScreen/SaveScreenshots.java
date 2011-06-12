package com.ribomation.droidAtScreen;

import com.ribomation.droidAtScreen.cmd.*;
import com.ribomation.droidAtScreen.dev.AndroidDevice;
import com.ribomation.droidAtScreen.dev.AndroidDeviceListener;
import com.ribomation.droidAtScreen.dev.AndroidDeviceManager;
import com.ribomation.droidAtScreen.gui.ApplicationFrame;
import com.ribomation.droidAtScreen.gui.DeviceFrame;
import org.apache.log4j.Logger;

import java.io.File;
import java.io.LineNumberReader;
import java.io.InputStreamReader;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;

import java.awt.image.BufferedImage;
import java.text.DecimalFormat;

import javax.imageio.ImageIO;

/**
 * Main entry point of this application
 * Syntax:
 * java -cp DroidScreen.jar com.ribomation.droidAtScreen.SaveScreenshots --adb "<path to adb>" --device "<device id>"
 *
 * @user jens
 * @date 2010-jan-17 11:00:39
 */
public class SaveScreenshots implements AndroidDeviceListener, Runnable
{
    private Logger                          log = Logger.getLogger(SaveScreenshots.class);
    private AndroidDeviceManager            deviceManager;
    private Map<String, DeviceFrame>        devices = new HashMap<String, DeviceFrame>();
    private List<AndroidDeviceListener>     deviceListeners = new ArrayList<AndroidDeviceListener>();
    private String adbPath;
    private String deviceId;
    private long interval;
    private long startName;
    private AndroidDevice currentDevice;
    private Thread thread;
    private boolean stopped;
    private String prefix;

    public static void main(String[] args) throws Exception
    {
        SaveScreenshots app = new SaveScreenshots(args);
	app.start();

	// wait for the stop signal
	LineNumberReader lnr = new LineNumberReader(new InputStreamReader(System.in));
	
	String line;
	do{
	    System.out.println("Type \"q\" to exit.");
	    line = lnr.readLine();
	    
	    // check to see if "quit" was passed in
	    if(line != null){
		line = line.trim();
		if("q".equals(line)){
		    System.out.println("Starting shutdown");
		    break;
		}else{
		    System.out.println("Invalid command ["+line+"]: please enter \"quit\" to exit.");
		}
	    }
	}while(line != null);

	// stop the tag listener
	app.stop();
    }
    

    public void start()
    {
	thread = new Thread(this);
	thread.start();
    }

    public void stop()
    {
	stopped = true;
    }

    public SaveScreenshots(String[] args)
    {
	interval = -1;
	deviceId = null;
	adbPath = null;
	currentDevice = null;
	stopped = false;
	startName = 1;
	prefix = "";
        parseArgs(args);
        initAndroid();
    }

    private void parseArgs(String[] args) {
        //log.debug("parseArgs: " + Arrays.toString(args));
	if(args.length > 0){
	    for(int i=0;i<args.length;i++){
		if("--adb".equals(args[i])){
		    ++i;
		    if(i<args.length) adbPath = args[i];
		}else if("--device".equals(args[i])){
		    ++i;
		    if(i<args.length) deviceId = args[i];
		}else if("--interval".equals(args[i])){
		    ++i;
		    if(i<args.length) interval = Long.parseLong(args[i]);
		}else if("--start".equals(args[i])){
		    ++i;
		    if(i<args.length) startName = Long.parseLong(args[i]);
		}else if("--prefix".equals(args[i])){
		    ++i;
		    if(i<args.length) prefix = args[i] + ".";
		}else if("--help".equals(args[i])){
		    System.out.println("Usage: \r\n");
		    System.out.println("java -jar DroidScreen.jar --adb \"<path to adb.exe>\" --device \"<android device id>\" --prefix \"<image file prefix>\" --interval <interval in milliseconds between images>\r\n");
		    System.out.println("For example: java -jar DroidScreen.jar --adb \"C:\\projects\\Android\\android-sdk-windows-1.5_r1\\platform-tools\\adb.exe\" --device \"HT097P800223\" --prefix \"ss1\" --interval 1000");
		    System.exit(0);
		}
	    }
	}
    }

    private void initAndroid() {
        log.debug("initAndroid");
        deviceManager = new AndroidDeviceManager();
        deviceManager.addAndroidDeviceListener(this);
	deviceManager.setAdbExecutable(new File(adbPath));
    }

    public void run() {
        log.debug("run");
	BufferedImage image;
	DecimalFormat df = new DecimalFormat("0000000000");
	AndroidDevice nowDevice;
	File lastFile;
	long current = startName;
	if(interval > 0 && adbPath != null && deviceId != null){
	    while(!stopped){
		try{ Thread.sleep(interval); }catch(InterruptedException ie){}
		if(stopped) break;
		nowDevice = currentDevice;
		if(nowDevice != null){
		    // save a screen shot
		    image = nowDevice.getScreenShot();
		    
		    // save it
		    lastFile = new File(prefix + df.format(current)+".png");
		    ++current;
		    try{
			ImageIO.write(image, "png", lastFile);
		    }catch(Exception e1){
			System.err.println("Exception writing file: "+e1.getMessage());
			e1.printStackTrace();
		    }
		}
	    }
	}
	deviceManager.close();
    }

    // --------------------------------------------
    // AndroidDeviceManager
    // --------------------------------------------

    @Override
    public void connected(final AndroidDevice dev) {
        log.debug("connected: dev="+dev+" ["+dev.getName()+"]");
	if(dev.getName().equals(deviceId)) currentDevice = dev;
    }

    @Override
    public void disconnected(final AndroidDevice dev) {
        log.debug("disconnected: dev="+dev);

	if(dev.getName().equals(deviceId)) currentDevice = null;
    }
}
