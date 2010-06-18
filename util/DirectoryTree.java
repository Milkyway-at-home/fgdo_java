package fgdo_java.util;

import com.twmacinta.util.MD5;

import java.io.BufferedWriter;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileWriter;
import java.io.FileInputStream;
import java.io.IOException;

import java.util.HashMap;


public class DirectoryTree {

	public static void setResultsDirectory(String resultsDirectory) { DirectoryTree.resultsDirectory = resultsDirectory; }
	public static void setBaseDirectory(String baseDirectory) { DirectoryTree.baseDirectory = baseDirectory; }
	public static void setBaseURL(String baseURL) { DirectoryTree.baseURL = baseURL; }

	private static String resultsDirectory;
	private static String baseDirectory;
	private static String baseURL;

	public static String getBaseDirectory() {
		return baseDirectory;
	}

	public static String getResultsDirectory() {
		return resultsDirectory;
	}

	public static String getUploadPath(String filename) {
		return getUploadDirectory(filename) + "/" + filename;
	}

	public static String getDownloadPath(String filename) {
		return getDownloadDirectory(filename) + "/" + filename;
	}

	public static String getDownloadURL(String filename) {
		return baseURL + "download/" + getDirectoryPath(filename) + "/" + filename;
	}

	public static String getDownloadDirectory(String filename) {
		return baseDirectory + "download/" + getDirectoryPath(filename);
	}

	public static String getUploadDirectory(String filename) {
		return baseDirectory + "upload/" + getDirectoryPath(filename);
	}

	public static String getDirectoryPath(String filename) {
		String hex = new MD5(filename).asHex();
//		System.out.println("hex: " + hex);
		long value = Long.parseLong(hex.substring(1,8), 16) % 1024;
//		System.out.println("value: " + value);
		return Long.toHexString(value);
	}


	public static String fileToString(String filename) throws IOException {
		File f = new File(filename);
		if (f.length() > Runtime.getRuntime().freeMemory()) {
			throw new IOException("Not enough memory to load " + f.getAbsolutePath());
		}
		byte[] data = new byte[(int)f.length()];
		new DataInputStream(new FileInputStream(f)).readFully(data);
		return new String(data);
	}

	private static HashMap<String,String> cachedMD5Hashes = new HashMap<String,String>();

	public static String getMD5Hash(String filename) throws IOException {
		String md5Hash = cachedMD5Hashes.get(filename);
		if (md5Hash != null) return md5Hash;

		md5Hash = new MD5(fileToString(filename)).asHex();
		cachedMD5Hashes.put(filename, md5Hash);
		return md5Hash;
	}

	public static void copyToDownloadDirectory(File file) throws IOException {
		String downloadPathName = getDownloadPath( file.getName() );
		File downloadFile = new File(downloadPathName);

		if (downloadFile.exists()) {
			String fileMD5 = new MD5(fileToString(file.toString())).asHex();
			String downloadMD5 = new MD5(fileToString( downloadPathName )).asHex();

			if (fileMD5.equals(downloadMD5)) {
				System.out.println(file.toString() + " already exists at " + downloadPathName + " with the same MD5 hash so it does not need to be copied.");
			} else {
				throw new IOException("Could not write file: " + downloadPathName + ", already exists with different contents than: " + file.toString());
			}

		} else {
			String fileString = fileToString(file.toString());

			BufferedWriter out = new BufferedWriter( new FileWriter(downloadPathName) );
			out.write(fileString);
			out.close();
		}


	}
}
