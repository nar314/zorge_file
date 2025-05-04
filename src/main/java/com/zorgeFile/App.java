package com.zorgeFile;

import java.io.Console;
import java.util.ArrayList;

import com.zorgeFile.core.FileCoder;
import com.zorgeFile.gui.MainWindow;
import com.zorgeFile.notify.NotifierConsole;

public class App {

	// [encr/decr] <password> <source file> <destination file> [-o]
	// java -jar zf.jar encr password file1.txt file1.encr
	// java -jar zf.jar encr password file1.txt file1.encr -o -i
	// java -jar zf.jar decr password file1.encr file1.txt	
	public static void help() {
		
		final String help = "\n\nZorge file. Utility to encrypt or decrypt files.\n" +
				"Usage: [encr/decr] <password> <source file> <destination file> [-o] [-i]\n" +
				"'-o' - overwrite target file, no overwrite by default.\n" +
				"'-i' - interactive mode to enter password. You have to provide fake password for interactive mode.\n" +
				"\n" +
				"java -jar zf.jar encr myPsw1 file1.txt file1.encr\n" +
				"java -jar zf.jar decr myPsw1 file1.encr file1.txt\n";
		System.out.println(help);
	}
	
	public static String enterPsw() throws Exception {

		Console c = System.console();
		System.out.print("Enter password : ");		
		String psw = new String(c.readPassword());		
		if(psw.isEmpty())
			throw new Exception("Password can not be empty.");

		System.out.print("Confirm password : ");		
		String pswConfir = new String(c.readPassword());		
		if(!pswConfir.equals(psw))
			throw new Exception("Password not match.");
		
		
		System.out.println("");
		return psw;
	}
	
	public static void consoleMode(final String[] args) {

		Boolean encr = true, overwriteMode = false, interactMode = false;
		String psw, source, target;

		try {
			ArrayList<String> noDashes = new ArrayList<String>(); 
			for(String arg : args) {
				if(arg.equals("-i"))
					interactMode = true;
				else if(arg.equals("-o"))
					overwriteMode = true;
				else
					noDashes.add(arg);
			}
			
			int n = noDashes.size();
			if(n != 4)
				throw new Exception("Invalid parameter count.\n");
			
			String s = noDashes.get(0); // <encr/decr>
			if(s.equals("encr"))
				encr = true;
			else if(s.equals("decr"))
				encr = false;
			else
				throw new Exception("Invalid encryption mode. Expected 'encr' or 'decr',");
			
			psw = noDashes.get(1); // <key>
			source = noDashes.get(2); // <source file>
			target = noDashes.get(3); // <destination file>
			
			if(interactMode)
				psw = enterPsw();
		}
		catch(Exception e) {
			System.out.println(e.getMessage());
			help();
			return;
		}
 
    	try {
    		FileCoder fc = new FileCoder();
    		fc.setConsoleMode(true);
    		fc.setNotifier(new NotifierConsole());
    		fc.setOverwriteMode(overwriteMode);
    		
    		if(encr)
    			fc.encrypt(source, psw, target);
    		else
    			fc.decrypt(source, psw, target);
    	}
    	catch(Exception e) {
    		System.out.println("\nError : " + e.getMessage());
    	}		
	}
	
	//------------------------------------------------------------------------
	//
	//------------------------------------------------------------------------
    public static void main(String[] args) {
    
    	if(args.length > 0)
    		consoleMode(args);
    	else {
    		MainWindow w = new MainWindow();
    		w.show();
    	}
    }
}
