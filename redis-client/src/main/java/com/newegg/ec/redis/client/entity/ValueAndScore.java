package com.newegg.ec.redis.client.entity;

import java.io.Serializable;

/**
 * Created by lf52 on 2019/2/20.
 * sorted set  value - score 键值对
 */
public  class ValueAndScore implements Serializable{
	private static final long serialVersionUID = 1150921216754615002L;

	private String value;
	
	private double score;
	
	public ValueAndScore(String value, double score) {
		this.value = value;
		this.score = score;
	}

	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		this.value = value;
	}

	public double getScore() {
		return score;
	}

	public void setScore(double score) {
		this.score = score;
	}
}