package com.insrb.app.openservice.api;

import com.insrb.app.openservice.database.InsuroboDataBaseAccess;

import java.util.Map;

public class InsuroboOpenApi {
	private InsuroboDataBaseAccess dba;
	public InsuroboOpenApi(InsuroboDataBaseAccess dba) {
		this.dba=dba;
	}
	private String __licenceSerialKey=null;
	private int __key=-1;
	private void invalidate(String licenceSerialKey) {
		__licenceSerialKey=licenceSerialKey;
		__key= dba.getLicence().validateLicence(licenceSerialKey, "#debug|#version=0.0.0.1");
	}
	
	public void commit(String request,String resultKey, Map<String, Object> data, Object... parameters) {
		dba.getLicence().resultLogToLicence(__licenceSerialKey, __key, "request="+request+"&"+resultKey);
	}

	public static InsuroboOpenApi getInstance(InsuroboDataBaseAccess dba, String licenceSerialKey) {
		InsuroboOpenApi api= new InsuroboOpenApi(dba);
		api.invalidate(licenceSerialKey);
		return api;
	}

	public InsuroboDataBaseAccess getDataAccess() {
		return dba;
	}

	public boolean isValidated() {
		return __key!=-1;
	}


}
