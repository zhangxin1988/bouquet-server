/*******************************************************************************
 * Copyright © Squid Solutions, 2016
 *
 * This file is part of Open Bouquet software.
 *  
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation (version 3 of the License).
 *
 * There is a special FOSS exception to the terms and conditions of the 
 * licenses as they are applied to this program. See LICENSE.txt in
 * the directory of this program distribution.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 *
 * Squid Solutions also offers commercial licenses with additional warranties,
 * professional functionalities or services. If you purchase a commercial
 * license, then it supersedes and replaces any other agreement between
 * you and Squid Solutions (above licenses and LICENSE.txt included).
 * See http://www.squidsolutions.com/EnterpriseBouquet/
 *******************************************************************************/
package com.squid.kraken.v4.model;

/**
 * @author sergefantino
 *
 */
public class AnalyticsResult {

	private DataHeader header = null;
	
	private Object data = null;
	
	private ResultInfo info = null;
	
	private DataLayout dataLayout = null;

	public DataHeader getHeader() {
		return header;
	}

	public void setHeader(DataHeader header) {
		this.header = header;
	}

	public Object getData() {
		return data;
	}

	public void setData(Object data) {
		this.data = data;
	}

	public ResultInfo getInfo() {
		return info;
	}

	public void setInfo(ResultInfo info) {
		this.info = info;
	}
	
	public DataLayout getDataLayout() {
		return dataLayout;
	}
	
	public void setDataLayout(DataLayout dataLayout) {
		this.dataLayout = dataLayout;
	}

}
