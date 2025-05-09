package com.b2s.shop.common.constant;

public enum CaState {
AB("120","Alberta"),
BC("121","British Columbia"),	
MB("122","Manitoba"),
NB("123","New Brunswick"),
NL("133","Newfoundland and Labrador"),
NT("126","Northwest Territories"),
NS("125","Nova Scotia"),
NU("127","Nunavut"),
ON("128","Ontario"),
PE("129","Prince Edward Island"),
QC("130","Quebec"),
SK("131","Saskatchewan"),
YT("132","Yukon Territory");

String stateId;
String stateName;

public String getStateName() {
	return stateName;
}

public void setStateName(String stateName) {
	this.stateName = stateName;
}

public String getStateId() {
	return stateId;
}

public void setStateId(String stateId) {
	this.stateId = stateId;
}

private CaState(String stateId,String stateName){
	this.stateId=stateId;
	this.stateName=stateName;
}	
	
}
