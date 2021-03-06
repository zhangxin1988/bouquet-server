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

import com.squid.core.expression.scope.ScopeException;
import com.squid.kraken.v4.core.analysis.datamatrix.DataMatrix;

/**
 * simple interface to define a dataMatrix transformation. The idea is to add a list of transformation while creating the query,
 * and to be able to apply them once we get the dataMatrix.
 * @author sergefantino
 *
 */
public interface DataMatrixTransform {

	/**
	 * apply the transformation to the matrix
	 * @param input
	 * @return
	 * @throws ScopeException 
	 */
	public DataMatrix apply(DataMatrix input) throws ScopeException;
	
}
