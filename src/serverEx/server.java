package serverEx;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Iterator;
import java.util.Properties;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.apache.log4j.Logger;

public class server {
	private Logger log = Logger.getLogger(getClass());

	Socket cSocket;
	ServerSocket sSocket;
	serverThread serverthread;
	Thread tr;
	ThreadPoolExecutor tpe;
	int connectCount=1;
	int waitNum;
	fileIO fio;
	Properties prop;
	int portNum;
	int filescheduletime;
	int threadcount;
	
	public server(){
		Config();
	}
	public static void main(String[] args) {
		new server().runServer();
	}	
	public void Config(){
		prop = new Properties();
		InputStream input = null;
		try {
			input = new FileInputStream("server.properties");
			prop.load(input);
			
			portNum = Integer.parseInt(prop.getProperty("port"));
			filescheduletime = Integer.parseInt(prop.getProperty("filescheduletime"));
			threadcount = Integer.parseInt(prop.getProperty("threadcount"));
		} catch (IOException ex) {
			ex.printStackTrace();
		} finally {
			if (input != null) {
				try {
					input.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}
	public void runServer(){
		log.info("server is run!");
		
		try {
			sSocket = new ServerSocket(portNum);
			tpe = new ThreadPoolExecutor(threadcount,3,10,TimeUnit.MINUTES,new LinkedBlockingQueue<Runnable>());
			fio = new fileIO(prop);
			ScheduledExecutorService fileScheduler = Executors.newSingleThreadScheduledExecutor();
			fileScheduler.scheduleAtFixedRate(fio, 0,filescheduletime,TimeUnit.MINUTES);
			while (true) {	
				cSocket = sSocket.accept();
				serverthread = new serverThread(cSocket,connectCount,this,fio);
				tpe.execute(serverthread);
				waitNum = tpe.getQueue().size();
				if(waitNum>0){
					serverthread.setWaitNum(waitNum);
					waitNum-=1;
					serverthread.sendMsg("[wai][응답]"+waitNum+"의 대기자가 있습니다.");
				}else{
					log.debug(serverthread.getDosStream());
					serverthread.sendMsg("[con][응답]접속되었습니다.");
				}
				
				//log.debug("접속대기자수 : "+tpe.getQueue().size());
				connectCount++;
			}
		} catch (Exception e) {
			log.error(e);
			disconnect();
			
		}
	}
	
	public void broadCast(){
		serverThread st;
		
		Iterator<Runnable> it = tpe.getQueue().iterator();
		while(it.hasNext()){
			st = (serverThread) it.next();
			waitNum = st.getWaitNum();
			if(waitNum==0){
				log.info("다음순서 접속자 접속하였습니다.");
				st.sendMsg("[con][응답]접속되었습니다.");
			}else{
				waitNum-=1;
				st.sendMsg("[wai][응답]"+waitNum+" 명의 대기자가 있습니다.");
			}
		}
	}
	public void disconnect(){
		try {
			serverthread.getDosStream().close();
			sSocket.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
}
