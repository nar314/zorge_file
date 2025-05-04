package com.zorgeFile.core;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.security.MessageDigest;

import com.zorgeFile.notify.Notifier;

public class FileMD5 {
	
	private Notifier notifier = null;
	
	private void notify(final String o, int percent) {
		
		if(notifier != null)
			notifier.notify(o, percent);
	}
	
	public FileMD5(Notifier n) {
		notifier = n;
	}
	
	//------------------------------------------------------------------
	//
	//------------------------------------------------------------------	
	public byte[] calcFileMd5(final String fileName) throws Exception {
		
		File file = new File(fileName);
		if(!file.isFile())
			throw new Exception("Not a file : " + fileName);
		
		long fileSize = file.length(); // Empty file is also OK.
		int bufSize = fileSize >= 65536 ? 65536 : (int)fileSize;		
		byte[] buffer = new byte[bufSize];
		
		InputStream fis =  new FileInputStream(fileName);
		MessageDigest md5 = MessageDigest.getInstance("MD5");
		int read = 0, percent = 0;
		long totalRead = 0;

		do {
			read = fis.read(buffer);
			if (read > 0) {
				md5.update(buffer, 0, read);

				totalRead += read;
				percent = (int)(totalRead * 100 / fileSize);
				notify("Calculating hash", percent);
			}
		} 
		while (read != -1);
		
		fis.close();
		return md5.digest();
	}

	//------------------------------------------------------------------
	//
	//------------------------------------------------------------------		
	static public String md5ToString(final byte[] md5) {
		
		StringBuffer sb = new StringBuffer();
	    for(int i = 0; i < md5.length; ++i) // do I trust optimizer ?
	    	sb.append(String.format("%02X", md5[i]));	    
	    return sb.toString();		
	}

	//------------------------------------------------------------------
	//
	//------------------------------------------------------------------			
	static public Boolean compareMD5(final byte[] md51, final byte[] md52) {
		
		int n = md51.length;
		if(md52.length != n)
			return false;
		
		for(int i = 0; i < n; ++i)
			if(md51[i] != md52[i])
				return false;
		
		return true;
	}

	//------------------------------------------------------------------
	//
	//------------------------------------------------------------------	
	static public String stringToHexMD5(final String input) throws Exception {
	
		if(input.isEmpty())
			throw new Exception("String is empty.");
		
		MessageDigest md5 = MessageDigest.getInstance("MD5");		
		byte[] bytesMD5 = md5.digest(input.getBytes());
		
		StringBuilder sb = new StringBuilder();
		for(int i = 0; i < bytesMD5.length; ++i)
			sb.append(String.format("%02X", bytesMD5[i]));
		return sb.toString();
	}
	
}
