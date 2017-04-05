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
package com.squid.kraken.v4.writers;

import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.squid.core.export.Selection;
import com.squid.kraken.v4.caching.redis.datastruct.RawMatrix;
import com.squid.kraken.v4.caching.redis.datastruct.RedisCacheValuesList;
import com.squid.kraken.v4.core.analysis.engine.hierarchy.DimensionMember;
import com.squid.kraken.v4.core.analysis.engine.processor.ComputingException;
import com.squid.kraken.v4.core.analysis.model.DashboardSelection;
import com.squid.kraken.v4.core.analysis.model.IntervalleObject;
import com.squid.kraken.v4.core.analysis.universe.Axis;
import com.squid.kraken.v4.export.ExportSelectionWriter;
import com.squid.kraken.v4.export.ExportSourceWriter;
import com.squid.kraken.v4.export.ExportSourceWriterVelocity;
import com.squid.kraken.v4.model.Dimension.Type;
import com.squid.kraken.v4.model.Facet;
import com.squid.kraken.v4.model.FacetMember;
import com.squid.kraken.v4.model.FacetMemberInterval;
import com.squid.kraken.v4.model.FacetSelection;

/**
 * export a RawMatrix using a ExportSourceWriter
 * @author sergefantino
 *
 */
public class ExportQueryWriter extends QueryWriter {

	OutputStream out;
	ExportSourceWriter writer;
	long linesWritten;
	String jobID;
	List<Selection> selectedItems;
	private static SimpleDateFormat dateFormat = new java.text.SimpleDateFormat("yyyy-MM-dd");

	static final Logger logger = LoggerFactory.getLogger(QueryWriter.class);

	public ExportQueryWriter(ExportSourceWriter w, OutputStream out, String jobId, List<Selection> selectedItems) {
		super();
		this.out = out;
		this.writer = w;
		this.jobID = jobId;
		this.selectedItems = selectedItems;
	}

	private static String convertToDate(Date inputString)
	{
		return dateFormat.format(inputString);
	}



	public static List<Selection> buildExportSelection(DashboardSelection ds, FacetSelection fs) {
		List<Selection> filters = new ArrayList<Selection>();
		List<Axis> axes = ds.getFilters();
		for(Axis axis:axes) {
			Collection<DimensionMember> comparedMembers = ds.getCompareToSelection().getMembers(axis);
			for (Facet facet:fs.getFacets()) {
				if (facet.getDimensionId().getDimensionId().equals(axis.getDimension().getId().getDimensionId())) {
					List<String> values = getSelection(ds.getMembers(axis), facet.getSelectedItems());
					List<String> comparedWith = null;
					if (comparedMembers != null) {
						for (Facet compare:fs.getCompareTo()) {
							if (axis.getDimension().getId().getDimensionId().equals(compare.getDimensionId().getDimensionId())) {
								comparedWith = getSelection(comparedMembers, compare.getSelectedItems());
							}
						}
						Selection selection = new Selection(axis.getDimension().getName(), values, comparedWith);
						filters.add(selection);
						if (axis.getDimensionType().equals(Type.CONTINUOUS)) {
							while (filters.indexOf(selection)>0) {
								int i = filters.indexOf(selection);
								Collections.swap(filters, i, i - 1);
							}
						}
					}
				}
			}
		}
		return filters;
	}


	private static List<String> getSelection(Collection<DimensionMember> members, Collection<FacetMember> facetMembers) {
		List<String> values = new ArrayList<String>();
		Iterator<FacetMember> facetMemberIterator = facetMembers.iterator();
		for(DimensionMember member: members) {
			FacetMember facetMember = facetMemberIterator.next();
			if (member.getID() instanceof IntervalleObject) {
				Date from = (Date) ((IntervalleObject)member.getID()).getLowerBound();
				Date to = (Date) ((IntervalleObject)member.getID()).getUpperBound();
				String interval = "From " + convertToDate(from) + " to " + convertToDate(to);
				if (((FacetMemberInterval) facetMember).getLowerBound().startsWith("__")) {
					interval += " ("+ StringUtils.capitalize(((FacetMemberInterval) facetMember).getLowerBound().replaceAll("COMPARE_TO_", "").toLowerCase().replaceAll("_"," ").trim()) + ")";
				}
				values.add(interval);
			} else {
				values.add(member.getID().toString());
			}
		}
		return values;
	}



	@Override
	public void write() throws ComputingException {
		long startExport = System.currentTimeMillis();
		if (writer instanceof ExportSourceWriterVelocity) {
			((ExportSourceWriterVelocity) writer).setQueryMapper(this.mapper);
		} else if (writer instanceof ExportSelectionWriter) {
			((ExportSelectionWriter) writer).setSelection(selectedItems);
		}
		try {

			if (val instanceof RawMatrix) {
				this.linesWritten = writer.write((RawMatrix) val, out);
			}

			if (val instanceof RedisCacheValuesList) {
				this.linesWritten = writer.write((RedisCacheValuesList) val, out);
			}

			long stopExport = System.currentTimeMillis();
			logger.info("task=" + this.getClass().getName() + " method=compute.writeData" + " jobid=" + jobID
					+ " lineWritten=" + linesWritten + " duration=" + (stopExport - startExport)
					+ " error=false status=done");

		} catch (Throwable e) {
			logger.error("failed to export jobId=" + jobID + ":", e);
			throw e;
		}
	}

	public long getLinesWritten() {
		return this.linesWritten;
	}

}
