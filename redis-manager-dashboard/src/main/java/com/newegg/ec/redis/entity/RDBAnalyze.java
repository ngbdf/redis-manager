package com.newegg.ec.redis.entity;


import java.util.List;

/**
 * @author Kyle.K.Zhao
 * @date 1/8/2020 16:26
 */
public class RDBAnalyze {
	private Long id;
	private boolean autoAnalyze;
	private String schedule;
	private String dataPath;
	private String prefixes;
	private boolean report;
	private String mailTo;
	private boolean manual;
	/**
	 *analyzer 例：0,5,6     0代表report  5 代表ExportKeyByPrefix  6代表ExportKeyByFilter
 	 */
	private String analyzer;
	private Long clusterId;

	private Cluster cluster;
	private List<String> nodes;

	private Long groupId;

	public List<String> getNodes() {
		return nodes;
	}


	public boolean isManual() {
		return manual;
	}

	public void setManual(boolean manual) {
		this.manual = manual;
	}

	public void setNodes(List<String> nodes) {
		this.nodes = nodes;
	}

	public Cluster getCluster() {
		return cluster;
	}

	public void setCluster(Cluster cluster) {
		this.cluster = cluster;
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public boolean isAutoAnalyze() {
		return autoAnalyze;
	}

	public void setAutoAnalyze(boolean autoAnalyze) {
		this.autoAnalyze = autoAnalyze;
	}

	public String getSchedule() {
		return schedule;
	}

	public void setSchedule(String schedule) {
		this.schedule = schedule;
	}

	public String getDataPath() {
		return dataPath;
	}

	public void setDataPath(String dataPath) {
		this.dataPath = dataPath;
	}

	public String getPrefixes() {
		return prefixes;
	}

	public void setPrefixes(String prefixes) {
		this.prefixes = prefixes;
	}

	public boolean isReport() {
		return report;
	}

	public void setReport(boolean report) {
		this.report = report;
	}

	public String getMailTo() {
		return mailTo;
	}

	public void setMailTo(String mailTo) {
		this.mailTo = mailTo;
	}

	public String getAnalyzer() {
		return analyzer;
	}

	public void setAnalyzer(String analyzer) {
		this.analyzer = analyzer;
	}

	public Long getClusterId() {
		return clusterId;
	}

	public void setClusterId(Long clusterId) {
		this.clusterId = clusterId;
	}

	public Long getGroupId() {
		return groupId;
	}

	public void setGroupId(Long groupId) {
		this.groupId = groupId;
	}
}
