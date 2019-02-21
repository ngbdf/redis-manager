package com.newegg.ec.redis.client.config;


import com.google.common.base.Strings;

import java.util.ArrayList;
import java.util.List;

public class RedisFactoryConfig {

	private static final int DEFAULT_CONNECTION = 8;
	private static final long DEFAULT_TIMEOUT = 3000;

	private long refreshPeriod;
	private boolean enablePeriodicRefresh;
	private long adaptiveRefreshTriggersTimeout;
	private int refreshTriggersReconnectAttempts;
	private int maxRedirects;


	private int StringMaxConnection;
	private int byteMaxConnection;
	private List<IpAndPort> nodeList;
	private long timeout;
	private String password;

	
	private RedisFactoryConfig(Builder builder) {
		this.byteMaxConnection = builder.byteMaxConnection;
		this.StringMaxConnection = builder.StringMaxConnection;
		this.nodeList = builder.nodeList;
		this.timeout = builder.timeout;
		this.refreshPeriod = builder.refreshPeriod;
		this.enablePeriodicRefresh = builder.enablePeriodicRefresh;
		this.adaptiveRefreshTriggersTimeout = builder.adaptiveRefreshTriggersTimeout;
		this.refreshTriggersReconnectAttempts = builder.refreshTriggersReconnectAttempts;
		this.maxRedirects = builder.maxRedirects;
		this.password = builder.password;
	}

	public int getStringMaxConnection() {
		return StringMaxConnection;
	}

	public void setStringMaxConnection(int stringMaxConnection) {
		StringMaxConnection = stringMaxConnection;
	}


	public int getByteMaxConnection() {
		return byteMaxConnection;
	}

	public void setByteMaxConnection(int byteMaxConnection) {
		this.byteMaxConnection = byteMaxConnection;
	}

	public List<IpAndPort> getNodeList() {
		return nodeList;
	}

	public void setNodeList(List<IpAndPort> nodeList) {
		this.nodeList = nodeList;
	}
	
	public void setNodeList(String nodeListStr) {
		this.nodeList = getHostsFromStr(nodeListStr);
	}

	public long getTimeout() {
		return timeout;
	}


	public void setTimeout(long timeout) {
		this.timeout = timeout;
	}


	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public static Builder builder(){
		return new Builder();
	}


	public long getRefreshPeriod() {
		return refreshPeriod;
	}

	public void setRefreshPeriod(long refreshPeriod) {
		this.refreshPeriod = refreshPeriod;
	}

	public boolean isEnablePeriodicRefresh() {
		return enablePeriodicRefresh;
	}

	public void setEnablePeriodicRefresh(boolean enablePeriodicRefresh) {
		this.enablePeriodicRefresh = enablePeriodicRefresh;
	}

	public long getAdaptiveRefreshTriggersTimeout() {
		return adaptiveRefreshTriggersTimeout;
	}

	public void setAdaptiveRefreshTriggersTimeout(long adaptiveRefreshTriggersTimeout) {
		this.adaptiveRefreshTriggersTimeout = adaptiveRefreshTriggersTimeout;
	}

	public int getRefreshTriggersReconnectAttempts() {
		return refreshTriggersReconnectAttempts;
	}

	public void setRefreshTriggersReconnectAttempts(int refreshTriggersReconnectAttempts) {
		this.refreshTriggersReconnectAttempts = refreshTriggersReconnectAttempts;
	}

	public int getMaxRedirects() {
		return maxRedirects;
	}

	public void setMaxRedirects(int maxRedirects) {
		this.maxRedirects = maxRedirects;
	}

	private static List<IpAndPort> getHostsFromStr(String nodeListStr){
		List<IpAndPort> nodeList = new ArrayList<>();
		if (!Strings.isNullOrEmpty(nodeListStr) ) {
			String [] nodeArray = nodeListStr.split(",");
			for (String node : nodeArray) {
				String [] ipAndPort = node.split(":");
				if (ipAndPort.length == 2) {
					nodeList.add(new IpAndPort(ipAndPort[0], Integer.parseInt(ipAndPort[1])));
				}
			}
		}
		return nodeList;
	}

	private static String getHostsStr(List<IpAndPort> ipAndPorts){
		StringBuilder builder = new StringBuilder("");
		if(ipAndPorts.size() > 0){
			for (IpAndPort ipAndPort : ipAndPorts){
				builder.append(ipAndPort.getIp()+":"+ipAndPort.getPort()+",");
			}
		}
		return builder.toString();
	}

	public static class IpAndPort{
		private String ip;
		
		private int port;
		
		public IpAndPort(String ip, int port) {
			this.ip = ip;
			this.port = port;
		}

		public String getIp() {
			return ip;
		}

		public void setIp(String ip) {
			this.ip = ip;
		}

		public int getPort() {
			return port;
		}

		public void setPort(int port) {
			this.port = port;
		}
	}
	
	public static class Builder{
		private int StringMaxConnection = DEFAULT_CONNECTION;
		private int byteMaxConnection = DEFAULT_CONNECTION;
		private List<IpAndPort> nodeList = null;
		private long timeout = DEFAULT_TIMEOUT;
		private String password = null;

		private long refreshPeriod = 60;
		private boolean enablePeriodicRefresh = true;
		private long adaptiveRefreshTriggersTimeout = 30;
		private int refreshTriggersReconnectAttempts = 5;
		private int maxRedirects = 5;

		private Builder() {
			super();
		}

		public Builder setNodeList(String nodeListStr){
			this.nodeList = getHostsFromStr(nodeListStr);
			return this;
		}
		
		public Builder setStringMaxConnection(int connection){
			this.StringMaxConnection = connection;
			return this;
		}
		
		public Builder setByteMaxConnection(int connection){
			this.byteMaxConnection = connection;
			return this;
		}
		
		public Builder setRefreshPeriod(long millisecond){
			this.refreshPeriod = millisecond;
			return this;
		}

		public Builder setEnablePeriodicRefresh(boolean enablePeriodicRefresh){
			this.enablePeriodicRefresh = enablePeriodicRefresh;
			return this;
		}
		public Builder setAdaptiveRefreshTriggersTimeout(long adaptiveRefreshTriggersTimeout){
			this.adaptiveRefreshTriggersTimeout = adaptiveRefreshTriggersTimeout;
			return this;
		}
		public Builder setRefreshTriggersReconnectAttempts(int refreshTriggersReconnectAttempts){
			this.refreshTriggersReconnectAttempts = refreshTriggersReconnectAttempts;
			return this;
		}
		public Builder setMaxRedirects(int maxRedirects){
			this.maxRedirects = maxRedirects;
			return this;
		}
		public Builder setTimeout(long millisecond){
			this.timeout = millisecond;
			return this;
		}

		public Builder setPassword(String password){
			this.password = password;
			return this;
		}
		
		public RedisFactoryConfig build(){
			return new RedisFactoryConfig(this);
		}
	}

	/**
	 * reset factory config
	 * @param config
	 * @param password
	 * @return
	 */
	public RedisFactoryConfig resetFactoryConfig(RedisFactoryConfig config,String password){

		Builder builder = RedisFactoryConfig.builder().setNodeList(getHostsStr(config.getNodeList())).
				setByteMaxConnection(config.getByteMaxConnection()).
				setStringMaxConnection(config.getStringMaxConnection()).
				setPassword(password).
				setRefreshPeriod(config.getRefreshPeriod()).
				setAdaptiveRefreshTriggersTimeout(config.getAdaptiveRefreshTriggersTimeout()).
				setRefreshTriggersReconnectAttempts(config.getRefreshTriggersReconnectAttempts()).
				setEnablePeriodicRefresh(config.isEnablePeriodicRefresh()).
				setMaxRedirects(config.getMaxRedirects()).
		        setTimeout(config.getTimeout());
		return new RedisFactoryConfig(builder);

	}

}
