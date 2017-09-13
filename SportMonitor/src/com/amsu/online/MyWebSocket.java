package com.amsu.online;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;
 

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.concurrent.CopyOnWriteArraySet;

import javax.websocket.OnClose;
import javax.websocket.OnError;
import javax.websocket.OnMessage;
import javax.websocket.OnOpen;
import javax.websocket.Session;
import javax.websocket.server.ServerEndpoint;

import com.amsu.online.bean.OnlineUser;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
 
//该注解用来指定一个URI，客户端可以通过这个URI来连接到WebSocket。类似Servlet的注解mapping。无需在web.xml中配置。
@ServerEndpoint("/websocket")
public class MyWebSocket {
    //静态变量，用来记录当前在线连接数。应该把它设计成线程安全的。
    private static int onlineCount = 0;
    Gson gson = new Gson();
     
    //concurrent包的线程安全Set，用来存放每个客户端对应的MyWebSocket对象。若要实现服务端与单一客户端通信的话，
    //可以使用Map来存放，其中Key可以为用户标识
    private static CopyOnWriteArraySet<MyWebSocket> webSocketSet = new CopyOnWriteArraySet<MyWebSocket>();
    private static CopyOnWriteArraySet<MyWebSocket> browserWebSocketSet = new CopyOnWriteArraySet<MyWebSocket>();
    

    //与某个客户端的连接会话，需要通过它来给客户端发送数据
    private Session session;
   
    //private static Map<MyWebSocket, OnlineUser> onlineUserMap = new HashMap<MyWebSocket, OnlineUser>();  //声明为静态，不然每次都是新建onlineUserMap对象
    private static Map<MyWebSocket, Integer> onlineBrowserUserKey = new HashMap<MyWebSocket, Integer>();
    private static Map<MyWebSocket, Integer> onlineAppUserKey = new HashMap<MyWebSocket, Integer>();
    //private static List<OnlineUser> onlineAppUserList = new ArrayList<OnlineUser>();  
    
    private static Map<Integer,OnlineUser> onlineAppUserList = new HashMap<Integer,OnlineUser>(); 
    
    
    private static int startCountIndex = 0;
    private static int startBrowserCountIndex = 0;
    
    
    private int curUploadEcgWebSocketIndex = -1;
    
    /**
     * 连接建立成功调用的方法
     * @param session  可选的参数。session为与某个客户端的连接会话，需要通过它来给客户端发送数据
     */
    @OnOpen
    public void onOpen(Session session){
        this.session = session;
        webSocketSet.add(this);     //加入set中
        addOnlineCount();           //在线数加1
        System.out.println("有新连接加入！当前在线人数为=======================================" + getOnlineCount());
        System.out.println("app在线人数为=======================================" + onlineAppUserList.size());
        System.out.println("网页在线人数为=======================================" + browserWebSocketSet.size());
        
        Collection<OnlineUser> valueCollection = onlineAppUserList.values();
        //final int size = valueCollection.size();
     
        List<OnlineUser> onlineUser = new ArrayList<OnlineUser>(valueCollection);
        
        String json = "F3,"+gson.toJson(onlineUser);
        
        //json = "F3,[{\"iconUrl\":\"http://119.29.201.120:83/usericons/81eb228b0a1815ece2eb1a8508a9972d.png\",\"username\":\"小猩猩\",\"onlinestate\":1,\"province\":\"广东省深圳市\",\"sex\":\"男\",\"age\":\"23岁\"},{\"iconUrl\":\"http://119.29.201.120:83/usericons/81eb228b0a1815ece2eb1a8508a9972d.png\",\"username\":\"小猩猩\",\"onlinestate\":1,\"province\":\"广东省深圳市\",\"sex\":\"男\",\"age\":\"23岁\"}]";
        

        System.out.println("用户列表："+json);
        
		sendMessage(json);
		
    }
    
    /**
     * 连接关闭调用的方法
     */
    @OnClose
    public void onClose(){
        
        //onlineUserMap.remove(this); //在线用户删除
        
    	//System.out.println("用户列表："+onlineAppUserList);
    	//System.out.println("用户键值对："+onlineAppUserKey);
        Integer integer = onlineAppUserKey.get(this);
        System.out.println("integer:"+integer);
        if (integer!=null && integer>=0 && onlineAppUserList!=null ) {
        	int index = integer;
        	onlineAppUserList.remove(index);
        	System.out.println("移除app登录用户信息");
        	System.out.println("用户列表："+onlineAppUserList);
		}
        
        subOnlineCount();           //在线数减1    
        System.out.println("有一连接关闭！当前在线人数为" + getOnlineCount());
        
        webSocketSet.remove(this);  //从set中删除
        browserWebSocketSet.remove(this);  //从set中删除
        
      //给网页发消息
		Collection<OnlineUser> valueCollection = onlineAppUserList.values();
		List<OnlineUser> tempOnlineUser = new ArrayList<OnlineUser>(valueCollection);
        String json = "F3,"+gson.toJson(tempOnlineUser);
		for(MyWebSocket webSocket:browserWebSocketSet){
    		if (webSocket!=null) {
				webSocket.sendMessage(json);
			}
    	}
        
    }
     
    /**
     * 收到客户端消息后调用的方法
     * @param message 客户端发送过来的消息
     * @param session 可选的参数
     */
    @OnMessage
    public void onMessage(String message, Session session) {
        System.out.println("来自客户端的消息:" + message);
        
        /*实时心电数据格式：F0,1,28,18,6,-2,-3,0,3,5,1,-1  (1为网页id)
        app端在线（上传用户信息）：F1,http://119.29.201.120:83/usericons/f81241db11c869f3c8e57ff96538abbc.png,1,天空之城    (头像url,在线状态【1在线，0离线】,昵称)
        	服务器给app回发：F1,服务器正常
                    网页端在线：F2,网页在线*/
        
       

        if(message.startsWith("A1")){
        	//心电数据
        	String[] split = message.split(",");
        	if (split!=null && split.length>1) {
        		int browserClientID  = Integer.parseInt(split[1]);
        		MyWebSocket myWebSocketByIntegerValue = getMyWebSocketByIntegerValue(onlineBrowserUserKey,browserClientID);
        		if (myWebSocketByIntegerValue!=null) {
        			myWebSocketByIntegerValue.sendMessage(message);
				}
			} 
        	/*for(MyWebSocket webSocket:browserWebSocketSet){
        		if (webSocket!=null) {
    				webSocket.sendMessage(message);
				}
        	}*/
        }
        else if(message.startsWith("A2")){
        	//app在线
        	System.out.println("app在线成功" );
        	
        	//int webSocketIndex = onlineAppUserList.size();
        	startCountIndex++;
        	int webSocketIndex = startCountIndex;
        	System.out.println("startCountIndex:"+startCountIndex);
        	System.out.println("webSocketIndex:"+webSocketIndex);
        	if (startCountIndex>=Integer.MAX_VALUE-1) {
        		startCountIndex = 0;
			}
        	
        	onlineAppUserKey.put(this, webSocketIndex);
        	//F1,http://119.29.201.120:83/usericons/f81241db11c869f3c8e57ff96538abbc.png,1,天空之城
        	String[] split = message.split(",");
        	if (split!=null && split.length>=7) {
        		OnlineUser onlineUser = new OnlineUser(split[1], split[3], Integer.parseInt(split[2]),split[4],split[5],split[6]);
        		onlineUser.setWebSocketIndex(webSocketIndex);
        		onlineAppUserList.put(webSocketIndex,onlineUser);
        		String toAppClientMsg = "F2,"+webSocketIndex;  //给app返回当前app所在列表索引
        		this.sendMessage(toAppClientMsg);
        		
        		//给网页发消息
        		Collection<OnlineUser> valueCollection = onlineAppUserList.values();
				List<OnlineUser> tempOnlineUser = new ArrayList<OnlineUser>(valueCollection);
		        String json = "F3,"+gson.toJson(tempOnlineUser);
        		for(MyWebSocket webSocket:browserWebSocketSet){
	        		if (webSocket!=null) {
	    				webSocket.sendMessage(json);
					}
	        	}
			}
        }
        else if(message.startsWith("A3")) {
			//心率和卡路里更新数据
        	//"A3,4,100,200" 分别为  A3,app客户端所在索引,心率，卡路里
        	String[] split = message.split(",");
        	if (split!=null && split.length==4) {
        		//System.out.println("onlineAppUserList："+onlineAppUserList);
        		int parseInt = Integer.parseInt(split[1]);
				OnlineUser onlineUser = onlineAppUserList.get(parseInt);
        		if (onlineUser!=null) {
        			onlineUser.setHeartRate(Integer.parseInt(split[2]));
    				onlineUser.setKcal(Integer.parseInt(split[3]));
    				Collection<OnlineUser> valueCollection = onlineAppUserList.values();
    				List<OnlineUser> tempOnlineUser = new ArrayList<OnlineUser>(valueCollection);
    		        String json = "F3,"+gson.toJson(tempOnlineUser);
    		        System.out.println("用户列表："+json);
    		        for(MyWebSocket webSocket:browserWebSocketSet){
    	        		if (webSocket!=null) {
    	    				webSocket.sendMessage(json);
    					}
    	        	}
				}
				
				
		        
			}
		}
        else if(message.startsWith("A4")){
        	//app上传实时运动数据
        	//A4,速度,距离,时间,有氧无氧,心率,步频,卡路里,
        	for(MyWebSocket webSocket:browserWebSocketSet){
        		if (webSocket!=null) {
    				webSocket.sendMessage(message);
				}
        	}
        	
        }
        else if(message.startsWith("W1")){
        	//网页在线
        	System.out.println("网页端在线成功");
        	browserWebSocketSet.add(this);
        	
        	onlineBrowserUserKey.put(this,startBrowserCountIndex++);
        }
        else if(message.startsWith("W4")){
        	//网页端请求指定app数据
        	System.out.println("网页端请求指定app数据");
        	
        	//给app发送指令，切换app数据
        	String[] split = message.split(",");
        	if (split.length==2) {
        		if (curUploadEcgWebSocketIndex!=-1) {
					//有其他app正在传输数据,先终止其传输
        			String toAppClientMsg = "F5,stop data";
            		//Integer integer = onlineAppUserKey.get(curUploadEcgWebSocketIndex);
            		System.out.println("之前curUploadEcgWebSocketIndex:"+curUploadEcgWebSocketIndex);
            		MyWebSocket myWebSocketByIntegerValue = getMyWebSocketByIntegerValue(onlineAppUserKey,curUploadEcgWebSocketIndex);
            		
					myWebSocketByIntegerValue.sendMessage(toAppClientMsg);
					
				}
        		curUploadEcgWebSocketIndex = Integer.parseInt(split[1]);
        		
        		//给客户端发消息，开始实时数据传输
        		
        		Integer browserIndex = onlineBrowserUserKey.get(this);
        		if (browserIndex!=null) {
        			String toAppClientMsg = "F1,"+browserIndex;
        			System.out.println("curUploadEcgWebSocketIndex:"+curUploadEcgWebSocketIndex);
            		MyWebSocket myWebSocketByIntegerValue = getMyWebSocketByIntegerValue(onlineAppUserKey,curUploadEcgWebSocketIndex);
            		myWebSocketByIntegerValue.sendMessage(toAppClientMsg);
				}
        		
        		//Integer integer = onlineAppUserKey.get(curUploadEcgWebSocketIndex);
        		
        		
			}
        }
        
        
        /*JsonBase fromJson = gson.fromJson(message, JsonBase.class);
        
        //JsonBase<OnlineUser> jsonBase =  commonJsonParse(message,new TypeToken<JsonBase<OnlineUser>>() {}.getType());
        System.out.println("fromJson:" + fromJson.toString());
        
        if(fromJson.ret==0){
        	//在线,app端
        	System.out.println("app在线成功" );
        	JsonBase<OnlineUser> jsonBase =  commonJsonParse(message,new TypeToken<JsonBase<OnlineUser>>() {}.getType());
        	//onlineUserMap.put(this, jsonBase.errDesc);
        	onlineAppUserKey.put(this, onlineAppUserList.size());
        	onlineAppUserList.add(jsonBase.errDesc);
        	//String json = gson.toJson(onlineUserMap);
        	//System.out.println(json);
        	
        	try {
        		//给客户端发消息，开始实时数据传输
        		JsonBase base = new JsonBase();
        		base.setRet(1);
        		base.setErrDesc("开始实时数据传输");
        		
    			this.sendMessage(gson.toJson(base));
    		} catch (IOException e) {
    			// TODO Auto-generated catch block
    			e.printStackTrace();
    		}
        }
        else if(fromJson.ret==1){
        	//app传来数据
        	String errDesc = (String) fromJson.getErrDesc();
        	System.out.println("实时数据:"+errDesc);
        	
        	//给所有网页上登陆的WebSocketSet客户端发送实时数据
        	for(MyWebSocket webSocket:browserWebSocketSet){
        		if (webSocket!=null) {
        			try {
    					webSocket.sendMessage("F0"+errDesc);
    				} catch (IOException e) {
    					// TODO Auto-generated catch block
    					e.printStackTrace();
    				}
				}
        	}
        }
        else if(fromJson.ret==2){
        	//在线，网页端 
        	System.out.println("网页端:"+fromJson.getErrDesc());
        	browserWebSocketSet.add(this);
        	
        }*/
        
    }
    
    private MyWebSocket getMyWebSocketByIntegerValue(Map<MyWebSocket, Integer> onlineUserKey,int value) {
    	Set<MyWebSocket> webSockets=onlineUserKey.keySet();
    	for(MyWebSocket myWebSocket:webSockets){
    	    if(value==onlineUserKey.get(myWebSocket)){
    	         System.out.println(myWebSocket);
    	         return myWebSocket;
    	    }
    	}
		return null;
	}
     
    /**
     * 发生错误时调用
     * @param session
     * @param error
     */
    @OnError
    public void onError(Session session, Throwable error){
        System.out.println("发生错误");
        error.printStackTrace();
    }
     
    /**
     * 这个方法与上面几个方法不一样。没有用注解，是根据自己需要添加的方法。
     * @param message
     * @throws IOException
     */
    public synchronized void sendMessage(String message) {
    	if (this.session.isOpen()) {
    		try {
				this.session.getBasicRemote().sendText(message);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
        //this.session.getAsyncRemote().sendText(message);
    }
 
    public static synchronized int getOnlineCount() {
        return onlineCount;
    }
 
    public static synchronized void addOnlineCount() {
        MyWebSocket.onlineCount++;
    }
    
    public static synchronized void subOnlineCount() {
        MyWebSocket.onlineCount--;
    }
    
    
}