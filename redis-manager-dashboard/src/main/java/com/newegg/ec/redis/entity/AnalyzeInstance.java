package com.newegg.ec.redis.entity;


public class AnalyzeInstance {
	private String host;
	private int port;
	
	public AnalyzeInstance(String host, int port) {
		this.host = host;
		this.port = port;
	}

	public String getHost() {
		return host;
	}

	public void setHost(String host) {
		this.host = host;
	}

	public int getPort() {
		return port;
	}

	public void setPort(int port) {
		this.port = port;
	}

	@Override
	public String toString() {
		return "AnalyzeInstance [host=" + host + ", port=" + port + "]";
	}
}
