package com.newegg.ec.redis.entity;

import java.util.List;

public class ExcelData {
	
	public ExcelData() {
		
	}
	
	public ExcelData(int startColumn, List<List<String>> tableData) {
		this.startColumn = startColumn;
		this.tableData = tableData;
	}

	/**
	 * 在 Excel中从第几列开始写数据
	 */
	private int startColumn;
	
	private List<List<String>> tableData;

	public int getStartColumn() {
		return startColumn;
	}

	public void setStartColumn(int startColumn) {
		this.startColumn = startColumn;
	}

	public List<List<String>> getTableData() {
		return tableData;
	}

	public void setTableData(List<List<String>> tableData) {
		this.tableData = tableData;
	}
	
	
}
