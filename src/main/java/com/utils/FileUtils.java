package com.utils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

public class FileUtils {
	public static List<String> readLines(File file)throws Exception{
		return readLines(file, "UTF-8");
	}
	
	public static List<String> readLines(File file, String charSetName)throws Exception{
		InputStreamReader read = null;
		BufferedReader br = null;
		try {
			List<String> list = new ArrayList<String>();
			read = new InputStreamReader(new FileInputStream(file), charSetName);
			br = new BufferedReader(read);
			String line = null;
			while((line = br.readLine()) != null) {
				list.add(line);
			}
			return list;
		}catch (Exception e) {
			e.printStackTrace();
			throw e;
		}finally {
			if(br != null) {
				br.close();
			}
			if(read != null) {
				read.close();
			}
		}
	}
	
	public static void write(File file, String data, String charsetName, boolean append)throws Exception{
		if(!file.getParentFile().exists()) {
			file.getParentFile().mkdirs();
		}
		if(!file.exists()) {
			file.createNewFile();
		}
		FileWriter writter = new FileWriter(file, append);
		try {
			writter.write(data);
		}catch (Exception e) {
			e.printStackTrace();
			throw e;
		}finally {
			writter.close();
		}
	}
	
	public static void writeStringToFile(File file, String data, boolean append)throws Exception{
		write(file, data, "UTF-8", append);
	}
}
