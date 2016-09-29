package io.openbouquet.api.model;

import java.io.Serializable;

/**
 * Class that holds the information of a user's profile in Auth0
 */
public interface User extends Serializable {
	
	public String getId() ;

	public String getName();

	public String getNickname();

	public String getEmail();

	public String getPictureURL();

	public String getGivenName();

	public String getFamilyName();
	
}