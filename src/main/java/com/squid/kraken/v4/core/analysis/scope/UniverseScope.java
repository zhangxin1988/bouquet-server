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
package com.squid.kraken.v4.core.analysis.scope;

import com.google.common.base.Optional;
import com.squid.core.expression.ExpressionAST;
import com.squid.core.expression.reference.ColumnReference;
import com.squid.core.expression.scope.IdentifierType;
import com.squid.core.expression.scope.ScopeException;
import com.squid.kraken.v4.core.analysis.engine.bookmark.BookmarkManager;
import com.squid.kraken.v4.core.analysis.universe.Space;
import com.squid.kraken.v4.core.analysis.universe.Universe;
import com.squid.kraken.v4.model.Bookmark;
import com.squid.kraken.v4.model.BookmarkConfig;
import com.squid.kraken.v4.model.BookmarkPK;
import com.squid.kraken.v4.model.Domain;
import com.squid.kraken.v4.model.DomainPK;

public class UniverseScope 
extends AnalysisScope
{
	
	private Universe universe;

	public UniverseScope(Universe universe) {
		this.universe = universe;
	}
	
	@Override
	public ExpressionAST createCompose(ExpressionAST first, ExpressionAST second)
	        throws ScopeException {
	    if (first instanceof SpaceExpression && second instanceof ColumnReference) {
	        //if (((ColumnReference)second).getSourceDomain().isInstanceOf(UniverseDomain.DOMAIN)) {
	            // it is ok to reference a column directly through the Space/Domain
	            return second;
	        //}
	    }
	    // else
	    return super.createCompose(first, second);
	}
	
	@Override
	public Object lookupObject(IdentifierType identifierType, String name) throws ScopeException {
		// lookup a domain
		if (identifierType==IdentifierType.DEFAULT || identifierType==DOMAIN) {
			for (Domain domain : universe.getDomains()) {
				if (domain.getName().equals(name)) {
					return new Space(universe, domain);
				}
			}
		}
		// lookup domain by ID
        else if (identifierType==IdentifierType.IDENTIFIER) {
            for (Domain domain : universe.getDomains()) {
                if (domain.getOid().equals(name)) {
                    return new Space(universe, domain);
                }
            }
        }
		// lookup bookmark by ID
        else if (identifierType==BOOKMARK) {
        	BookmarkPK bookmarkPk = new BookmarkPK(universe.getProject().getId(), name);
        	Optional<Bookmark> obookmark = BookmarkManager.INSTANCE.readBookmark(universe.getContext(), bookmarkPk);
        	if (obookmark.isPresent()) {
        		Bookmark bookmark = obookmark.get();
        		BookmarkConfig config = BookmarkManager.INSTANCE.readConfig(bookmark);
        		String domainId = config.getDomain();
        		DomainPK domainPk = new DomainPK(bookmark.getId().getParent(), domainId);
        		Domain domain = universe.getDomain(domainPk);
        		return new Space(universe, domain, obookmark.get());
        	}
        }
		// else
		throw new ScopeException("identifier not found: "+name);
	}

}
