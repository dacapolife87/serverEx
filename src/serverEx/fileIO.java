package serverEx;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Properties;
import java.util.TimerTask;

import org.apache.log4j.Logger;
import org.json.simple.JSONObject;

public class fileIO extends TimerTask{
	private Logger log = Logger.getLogger(getClass());
	
	HashMap<String,String> map = new HashMap<String, String>();
	private String filePath;
	int fileCheckTimer;
	Properties prop;
	
	FileWriter fw = null;
	public fileIO(Properties prop){
		this.prop = prop;
		Config();
	}
	public void Config(){
		filePath = prop.getProperty("filepath");
		fileCheckTimer = Integer.parseInt(prop.getProperty("filechecktimer"));
	}
	public synchronized void updateFileContent(){
		String content="";
		String sentence;

		long saveTime = System.currentTimeMillis ()/1000;
		log.info("[fileIO.updateFileContent] File update ");
		try {
			BufferedReader in = new BufferedReader(new FileReader(filePath));
			
			while ((sentence = in.readLine()) != null) {
				String contentTime = sentence.substring(0, 10);
				if(saveTime-Integer.parseInt(contentTime) <fileCheckTimer){
					content += (sentence + "\r\n" );
				}
			}
			FileWriter fw = new FileWriter(filePath);
			fw.write(content);
			in.close();
			fw.close();
		} catch (IOException e) {
			// TODO: handle exception
			log.error("[fileIO.updateFileContent] deleteFileContent", e);
		}
	}
	public synchronized void writeFileContent(String msg){
		long saveTime = System.currentTimeMillis ()/1000;
		BufferedWriter out = null;
		try {
			out = new BufferedWriter(new FileWriter(filePath, true));
			out.write(saveTime+"\t"+msg);
			out.newLine();
			out.close();		
		} catch (IOException e1) {
			log.error("[fileIO.writeFileContent] IOException", e1);
		}
	}
	
	public synchronized void writeFileMap(JSONObject data){
		long saveTime = System.currentTimeMillis ()/1000;
		BufferedWriter out = null;
		try{
			out = new BufferedWriter(new FileWriter(filePath, true));
			out.write(saveTime+" "+data.get("KEY")+" "+data.get("VALUE"));
			out.newLine();
			out.close();		
		}catch (IOException e1) {
			log.error("[fileIO.writeFileContent] IOException", e1);
		}
	}
	public synchronized HashMap<String, String> readFileMap(){
		String content="";
		String sentence;	
		String key;
		String value = null;
		try{
			File readFile = new File(filePath);
			if(!readFile.exists()){
				content = "notExist";
			}else{
				BufferedReader in = new BufferedReader(new FileReader(filePath));
				while ((sentence = in.readLine()) != null) {
					log.debug("[fileIO.readFileMap] sentence : "+sentence);
					String parts[] = sentence.split(" ");
					key = parts[1];
					value = parts[2];
					map.put(key, value);
				}
				in.close();	
			}
		}catch (IOException e) {
			log.error("[fileIO.readFileContent] IOException", e);
	    }
		log.debug("[fileIO.readFileContent] content : " + content);
		return map;
	}
	
	public synchronized String readFileContent(){
		String content="";
		String sentence;
		try{
			File readFile = new File(filePath);
			if(!readFile.exists()){
				content = "notExist";
			}else{
				BufferedReader in = new BufferedReader(new FileReader(filePath));
				while ((sentence = in.readLine()) != null) {
					content+=sentence;
					content += "/";
				}
				in.close();
			}
		}catch (IOException e) {
			log.error("[fileIO.readFileContent] IOException", e);
	    }
		log.debug("[fileIO.readFileContent] content : " + content);
		return content;
	}
	@Override
	public void run() {
		// TODO Auto-generated method stub
		updateFileContent();
	}
}
