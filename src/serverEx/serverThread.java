package serverEx;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

public class serverThread implements Runnable{
	private Logger log = Logger.getLogger(getClass());
	
	Map<String,String> resultData = new HashMap<String, String>();
	Socket soc;
	server sv;
	fileIO fio;
	DataInputStream dis;
	DataOutputStream dos;
	String readMsg;
	int waitNum;
	int connectNum;
	
	public serverThread( Socket soc,int connectNum,server sv,fileIO fio){
		this.soc = soc;
		this.connectNum = connectNum;
		this.sv = sv;
		this.fio = fio;
		setStream();	
	}
	public void setWaitNum(int waitNum){
		this.waitNum = waitNum;
	}
	public int getWaitNum(){
		return waitNum-1;
	}
	public void setStream(){
		try {
			dis = new DataInputStream(new BufferedInputStream(soc.getInputStream()));
			dos = new DataOutputStream(new BufferedOutputStream(soc.getOutputStream()));
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			endConnect();
		}
	}
	public DataOutputStream getDosStream(){
		return this.dos;
	}
	public void sendMsg(String msg){
		try {
			dos.writeUTF(msg);
			dos.flush();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			endConnect();
		}	
	}
	
	@Override
	public void run() {
		// TODO Auto-generated method stub
		try {
			while (true) {
				JSONObject receiveData =null;
				String type;
				String temptype;
				String key;

				readMsg = dis.readUTF();	
				log.info("[ServerSide] "+connectNum+"번 접속자  : "+readMsg);
				
				JSONParser jsonParser = new JSONParser();
				receiveData = (JSONObject) jsonParser.parse(readMsg);
				
				temptype = (String) receiveData.get("TYPE");
				if(temptype.equals("end")){
					break;
				}else if (temptype.equals("get")) {
					log.info("server get method");
					key = (String) receiveData.get("KEY");
					String content="";
					resultData = fio.readFileMap();
					log.debug("resultData : "+resultData);
					content = resultData.get(key);
					log.debug("result.get key : "+content);
					if(resultData.isEmpty()){
						content = "정보가없습니다.";
					}
					sendMsg("[get]파일 내용 출력");
					sendMsg(content);
				}else if (temptype.equals("getA")) {
					log.info("server get method");
					String contentA;
					contentA = fio.readFileContent();
					sendMsg("[get]파일 내용 출력");
					sendMsg(contentA);
				}else if (temptype.equals("put")) {
					log.info("server put method : ");
					fio.writeFileMap(receiveData);
				}else{
					log.info("잘못된 형식 : "+readMsg);
				}
				/*
				switch (type) {
				case "end":
					break;
				case "get":
					log.info("server get method");
					key = (String) receiveData.get("KEY");
					String content="";
					resultData = fio.readFileMap();
					log.debug("resultData : "+resultData);
					content = resultData.get(key);
					log.debug("result.get key : "+content);
					if(resultData.isEmpty()){
						content = "정보가없습니다.";
					}
					sendMsg("[get]파일 내용 출력");
					sendMsg(content);
					break;
				case "getA":
					log.info("server get method");
					String contentA;
					contentA = fio.readFileContent();
					sendMsg("[get]파일 내용 출력");
					sendMsg(contentA);
					break;
				case "put":
					log.info("server put method : ");
					fio.writeFileMap(receiveData);
					break;
				default:
					log.info("잘못된 형식 : "+readMsg);
					break;
				}*/
			}
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			endConnect();
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		log.info(connectNum+"번 접속자 접속종료");
		sv.broadCast();
	}

	public void endConnect(){
		try {
			dis.close();
			dos.close();
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
	}
}
