package com.amsu.online.bean;

/**
 * Created by hai on 7/15/2017.
 */
public class OnlineUser {
    private String iconUrl;
    private String username;
    private int onlinestate;
    private String province;
	private String sex;
	private String age;
	private int webSocketIndex; 
	private int heartRate; 
	private int kcal; 
	private String state;

    public String getIconUrl() {
        return iconUrl;
    }

    public void setIconUrl(String iconUrl) {
        this.iconUrl = iconUrl;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public int getOnlinestate() {
        return onlinestate;
    }

    public void setOnlinestate(int onlinestate) {
        this.onlinestate = onlinestate;
    }

    public OnlineUser(String iconUrl, String username, int onlinestate) {
        this.iconUrl = iconUrl;
        this.username = username;
        this.onlinestate = onlinestate;
    }
    
    

    public String getProvince() {
		return province;
	}

	public void setProvince(String province) {
		this.province = province;
	}

	public String getSex() {
		return sex;
	}

	public void setSex(String sex) {
		this.sex = sex;
	}

	public String getAge() {
		return age;
	}

	public void setAge(String age) {
		this.age = age;
	}

	
	
	public int getWebSocketIndex() {
		return webSocketIndex;
	}

	public void setWebSocketIndex(int webSocketIndex) {
		this.webSocketIndex = webSocketIndex;
	}

	public int getHeartRate() {
		return heartRate;
	}

	public void setHeartRate(int heartRate) {
		this.heartRate = heartRate;
	}

	public int getKcal() {
		return kcal;
	}

	public void setKcal(int kcal) {
		this.kcal = kcal;
	}

	


	public String getState() {
		return state;
	}

	public void setState(String state) {
		this.state = state;
	}

	@Override
	public String toString() {
		return "OnlineUser [iconUrl=" + iconUrl + ", username=" + username
				+ ", onlinestate=" + onlinestate + ", province=" + province
				+ ", sex=" + sex + ", age=" + age + ", webSocketIndex="
				+ webSocketIndex + ", heartRate=" + heartRate + ", kcal="
				+ kcal + ", state=" + state + "]";
	}

	public OnlineUser(String iconUrl, String username, int onlinestate,
			String province, String sex, String age) {
		super();
		this.iconUrl = iconUrl;
		this.username = username;
		this.onlinestate = onlinestate;
		this.province = province;
		this.sex = sex;
		this.age = age;
	}
	
	
	
}
