package socketserver;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Iterator;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created by orange on 16/6/8.
 */
public class TCPLongConnectionServer {
	//4c37987f8e13461dbf0c133af338c039小米        04fb00752bc94d84b718d9a41e024c7a三星
	private static boolean isStart=true;
	private static ServerResponseTask serverResponseTask;
	private static ConcurrentHashMap<String, Socket> onLineClient=new ConcurrentHashMap<String, Socket>();
    public TCPLongConnectionServer(){

    }
    public static void main(String[] args){
        ServerSocket serverSocket=null;
        ExecutorService executorService=Executors.newCachedThreadPool();

        try {
            serverSocket=new ServerSocket(9013);
            	while(isStart){
                Socket socket=serverSocket.accept();
                serverResponseTask=new ServerResponseTask(socket,new TCPLongConnectServerHandlerData.TCPResultCallBack() {
					
					@Override
					public void connectSuccess(String clientId,String clientMessage) {
						//新加入的客户端成功连接后 
						System.out.println("目标客户ID:"+clientId+"   数据："+clientMessage);
						onLineClient.put(clientId, socket);
						Procotol procotol=new Procotol();
						procotol.setContent(clientMessage);
						procotol.setUuid(clientId);
						Socket targetClient=getConnectClient(clientId);
						if(targetClient!=null){
							serverResponseTask.addWriteTask(procotol,targetClient);//TODO
						}else{
							System.out.println("Newclient is null");
						}
					}
				});
                executorService.execute(serverResponseTask);
                printAllClient();
            }
            serverSocket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }finally {
            if (serverSocket!=null){
                try {
                	isStart=false;
                    serverSocket.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    /**
     *   发送消息给指定用户
     * @param clientId
     * @return
     */
    public boolean sendToClient(String clientId){
    	if(onLineClient.contains(clientId)){
    		return true;
    	}
    	return false;
    }
    
    /**
     * 打印已经链接的客户端
     */
    public static void printAllClient(){
    	if (onLineClient==null) {
			return ;
		}
    	Iterator inter=onLineClient.keySet().iterator();
    	while(inter.hasNext()){
    		System.out.println("client:"+inter.next());
    	}
    }
    
    public static Socket getConnectClient(String clientID){
    	if(onLineClient.size()>=2){
    		return onLineClient.get("4c37987f8e13461dbf0c133af338c039");//与小米手机进行通信
    	}
    	return null;
    }
}
