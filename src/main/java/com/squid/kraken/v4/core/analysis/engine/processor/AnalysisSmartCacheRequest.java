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
package com.squid.kraken.v4.core.analysis.engine.processor;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.apache.commons.codec.digest.DigestUtils;

import com.squid.kraken.v4.core.analysis.datamatrix.DataMatrix;
import com.squid.kraken.v4.core.analysis.model.DashboardAnalysis;
import com.squid.kraken.v4.core.analysis.model.DashboardSelection;
import com.squid.kraken.v4.core.analysis.model.MeasureGroup;
import com.squid.kraken.v4.core.analysis.universe.Axis;
import com.squid.kraken.v4.core.analysis.universe.Universe;

/**
 * This is the object to request if an Analysis can be extracted from the Smart Cache
 * @author sergefantino
 *
 */
public class AnalysisSmartCacheRequest {

	private Universe universe;
	private AnalysisSmartCacheSignature key = null;
	
	/**
	 * 
	 */
	public AnalysisSmartCacheRequest(Universe universe, DashboardAnalysis analysis, MeasureGroup measures, String SQL) {
		this.universe = universe;
		this.key = new AnalysisSmartCacheSignature(analysis, measures, SQL);
	}
	/**
	 * @return
	 */
	public DashboardAnalysis getAnalysis() {
		return key.getAnalysis();
	}
	
	/**
	 * @return
	 */
	public MeasureGroup getMeasures() {
		return key.getMeasures();
	}
	
	/**
	 * @return the key
	 */
	public AnalysisSmartCacheSignature getKey() {
		return key;
	}
	
	/**
	 * set the signature row count
	 * @param dm
	 */
	public void setRowCount(DataMatrix dm) {
		key.setRowCount(dm!=null?dm.getRowCount():-1);
	}
	
	/**
	 * @return the axesSignature
	 */
	public String getAxesSignature() {
		if (key.getAxesSignature()==null) {
			key.setAxesSignature(universe);
		}
		return key.getAxesSignature();
	}

	/**
	 * @return the axesSignature
	 */
	public String getFiltersSignature() {
		if (key.getFiltersSignature()==null) {
			key.setFiltersSignature(computeFiltersSignature(universe, key.getAnalysis().getSelection()));
		}
		return key.getFiltersSignature();
	}
	
	public String computeFiltersSignature(Universe universe, DashboardSelection selection) {
		if (selection.isEmpty()) {
			return "#EMPTY#";
		} else {
			return computeFiltersSignature(universe, selection.getFilters());
		}
	}
	
	public String computeFiltersSignature(Universe universe, List<Axis> filters) {
		// sort the domains
		StringBuilder signature = new StringBuilder();
		Collections.sort(filters, new Comparator<Axis>() {
			@Override
			public int compare(Axis o1, Axis o2) {
				return o1.getId().compareTo(o2.getId());
			}
		});
		for (Axis axis : filters) {
			String hash = DigestUtils.sha256Hex(axis.getId());
			signature.append("#").append(hash);
		}
		//
		return signature.toString();
	}

}
