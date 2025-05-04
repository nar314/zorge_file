package com.zorgeFile.core;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

import com.zorgeFile.notify.Notifier;

// I do not add dedicated class for exception. No need for now.
public class FileCoder {

	private Boolean consoleMode = false;
	private Boolean overwriteMode = false;
	private Notifier notifier = null;
	private Boolean deleteFile = false;
	
	public FileCoder() {		
	}
	
	private byte keyToByte(final String key) {
		
		byte out = 0;
		byte[] data = key.getBytes();
		for(int i = 0; i < data.length; ++i)
			out += data[i];
		return out;
	}
	
	private byte[] keyToIV(final String key) {
		
		byte byteKey = keyToByte(key);
		
        byte[] iv = new byte[16];        
        for(int i = 0; i < 16; ++i, ++byteKey)
        	iv[i] = byteKey;
		return iv;
	}
	
	private byte[] IVtoSecretKey(final byte[] iv) {
		
		// there is no way iv[] will have size different from 16
        byte[] keyBytes = new byte[32];
        for(int i = 0; i < 16; ++i) { 
        	keyBytes[i] = iv[i];
        	keyBytes[i + 16] = iv[i];
        }
        return keyBytes;
	}
	
	private void enryptFile(final String fileName, final String key, final String fileNameEncr) throws Exception {
		
		if(key.isEmpty())
			throw new Exception("Password can not be empty.");
		
		File file = new File(fileName);
		if(!file.isFile())
			throw new Exception("Not a file : " + fileName);
		
		if(!overwriteMode) {
			if(new File(fileNameEncr).exists())
				throw new Exception("File already exist. " + fileNameEncr);
		}
		
		FileMD5 fmd5 = new FileMD5(notifier);
		byte[] fileMD5 = fmd5.calcFileMd5(fileName);

		long fileSize = file.length();
		if(fileSize == 0)
			throw new Exception("File is empty.");

		int bufSize = fileSize >= 65536 ? 65536 : (int)fileSize;
		byte[] buffer = new byte[bufSize];

		final String keyMD5 = FileMD5.stringToHexMD5(key);
		byte[] iv = keyToIV(keyMD5);
        IvParameterSpec ivParameterSpec = new IvParameterSpec(iv);

        byte[] keyBytes = IVtoSecretKey(iv);
        SecretKeySpec secretKeySpec = new SecretKeySpec(keyBytes, "AES");
        
        Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
        cipher.init(Cipher.ENCRYPT_MODE, secretKeySpec, ivParameterSpec);

		try (FileOutputStream fos = new FileOutputStream(fileNameEncr);
		     BufferedOutputStream bos = new BufferedOutputStream(fos);
			 InputStream fis =  new FileInputStream(fileName);
			) {
			
			int read = 0, percent = 0;
			long totalRead = 0;
	
			bos.write(fileMD5);
			deleteFile = true;
			do {
				read = fis.read(buffer);
				if (read > 0) {
					byte[] obuf = cipher.update(buffer, 0, read);
		            if (obuf != null)
		                bos.write(obuf);
				
					totalRead += read;
					percent = (int)(totalRead * 100 / fileSize);
					if(notifier != null)
						notifier.notify("Encrypting", percent);
				}
			} 
			while (read != -1);
			
			byte[] obuf = cipher.doFinal();
	        if (obuf != null)
	            bos.write(obuf);
		}
	}

	private void decryptFile(final String fileNameEncr, final String key, final String fileNameDecr) throws Exception {
		
		File file = new File(fileNameEncr);
		if(!file.isFile())
			throw new Exception("Not a file : " + fileNameEncr);
		
		if(!overwriteMode) {
			if(new File(fileNameDecr).exists())
				throw new Exception("File already exist. " + fileNameDecr);
		}

		long fileSize = file.length();
		if(fileSize == 0)
			throw new Exception("File is empty.");
		
		int bufSize = fileSize >= 65536 ? 65536 : (int)fileSize;
		
		byte[] buffer = new byte[bufSize];

		final String keyMD5 = FileMD5.stringToHexMD5(key);
		byte[] iv = keyToIV(keyMD5);
        IvParameterSpec ivParameterSpec = new IvParameterSpec(iv);

        byte[] keyBytes = IVtoSecretKey(iv);
        SecretKeySpec secretKeySpec = new SecretKeySpec(keyBytes, "AES");
        
        Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
        cipher.init(Cipher.DECRYPT_MODE, secretKeySpec, ivParameterSpec);

		byte[] md5Original = new byte[16];
		int read = 0, percent = 0;
		long totalRead = 0;
		
		try(FileOutputStream fos = new FileOutputStream(fileNameDecr);
				BufferedOutputStream bos = new BufferedOutputStream(fos);
				InputStream fis = new FileInputStream(fileNameEncr);
				) {
			
			totalRead = fis.read(md5Original);
			
			do {
				read = fis.read(buffer);
				if (read > 0) {
					byte[] obuf = cipher.update(buffer, 0, read);
		            if (obuf != null) {
		        		deleteFile = true;
		                bos.write(obuf);
		            }
				
					totalRead += read;
					percent = (int)(totalRead * 100 / fileSize);
					if(notifier != null)
						notifier.notify("Decrypting", percent);
				}
			} 
			while (read != -1);
			
			byte[] obuf = cipher.doFinal();
	        if (obuf != null)
	            bos.write(obuf);
		}
		catch(IllegalBlockSizeException e) {
			throw new Exception("Invalid password or file was not encrypted by me.");
		}
		catch(BadPaddingException e) {
			throw new Exception("Invalid password or file was not encrypted by me.");
		}
		
        FileMD5 fmd5 = new FileMD5(notifier);
        byte[] md5e = fmd5.calcFileMd5(fileNameDecr);
        if(!FileMD5.compareMD5(md5Original, md5e))
        	throw new Exception("Decrypted file corrupted.");
	}

	public void setNotifier(Notifier n) {
		notifier = n;
	}
	
	public void setOverwriteMode(Boolean b) {
		overwriteMode = b;
	}
	
	public void setConsoleMode(Boolean b) {
		consoleMode = b;
	}
	
	private void deleteFile(final String path) {
		
		if(!deleteFile)
			return;
		File file = new File(path);
		if(file.isFile() && file.exists())
			file.delete();
	}

	/**
	 * Encrypt file.
	 * 
	 * @param fileName
	 * @param key
	 * @param fileNameEncr
	 * @throws Exception
	 */
	public void encrypt(final String fileName, final String key, final String fileNameEncr) throws Exception {

		if(fileName.equals(fileNameEncr))
			throw new Exception("Source and target files are the same.");
		
		if(consoleMode) {
			System.out.println("Encrypting file.");
			System.out.println("Source : " + fileName);
			System.out.println("Target : " + fileNameEncr);
		}
		
		try {
			enryptFile(fileName, key, fileNameEncr);
		}
		catch(Exception e) {
			deleteFile(fileNameEncr);
			throw e;
		}
	    
	    if(consoleMode)
	    	System.out.println("\nFile encrypted.");
	}

	/**
	 * Decrypt file.
	 * 
	 * @param fileNameEncr
	 * @param key
	 * @param fileNameDecr
	 * @throws Exception
	 */
	public void decrypt(final String fileNameEncr, final String key, final String fileNameDecr) throws Exception {

		if(fileNameEncr.equals(fileNameDecr))
			throw new Exception("Source and target files are the same.");
		
		if(consoleMode) {
			System.out.println("Decrypting file.");
			System.out.println("Source : " + fileNameEncr);
			System.out.println("Target : " + fileNameDecr);		
		}

		try {
			decryptFile(fileNameEncr, key, fileNameDecr);
		}
		catch(Exception e) {
			deleteFile(fileNameDecr);
			throw e;
		}
		
	    if(consoleMode)
	    	System.out.println("\nFile decrypted.");
	}	
}
