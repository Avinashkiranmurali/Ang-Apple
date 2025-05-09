package com.b2s.shop.common.constant;

public enum UsState {
AK("55","Alaska"),
AL("54","Alabama"),
AR("56","Arkansas"),
AZ("51","Arizona"),
CA("52","California"),
CO("53","Colorado"),
CT("48","Connecticut"),
DC("49","District of Columbia"),
DE("50","Delaware"),
FL("45","Florida"),
GA("46","Georgia"),
HI("47","Hawaii"),
IA("42","Iowa"),
ID("43","Idaho"),
IL("39","Illinois"),
IN("44","Indiana"),
KS("40","Kansas"),
KY("41","Kentucky"),
LA("36","Louisiana"),
MA("37","Massachusetts"),
MD("38","Maryland"),
ME("33","Maine"),
MI("34","Michigan"),
MN("35","Minnesota"),
MO("30","Missouri"),
MS("31","Mississippi"),
MT("32","Montana"),
NC("27","North Carolina"),
ND("28","North Dakota"),
NE("29","Nebraska"),
NH("24","New Hampshire"),
NJ("25","New Jersey"),
NM("26","New Mexico"),
NV("21","Nevada"),
NY("22","New York"),
OH("23","Ohio"),
OK("18","Oklahoma"),
OR("19","Oregon"),
PA("20","Pennsylvania"),
RI("58","Rhode Island"),
SC("15","South Carolina"),
SD("16","South Dakota"),
TN("17","Tennessee"),
TX("12","Texas"),
UT("13","Utah"),
VA("14","Virginia"),
VT("9","Vermont"),
WA("10","Washington"),
WI("11","Wisconsin"),
WV("7","West Virginia"),
WY("8","Wyoming");		

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

private UsState(String stateId,String stateName){
		this.stateId=stateId;
		this.stateName=stateName;
	}		
	
}
