package com.b2s.rewards.common.exception;

public class ExceptionStackUtil {

	static public String toString(Exception e){
		StringBuffer sb = new StringBuffer();
		sb.append(e.getMessage()+"\r\n");
		StackTraceElement[] els = e.getStackTrace();
		if  (els!=null){
			for (int i=0;i<els.length;i++){
				sb.append(els[i].toString()+"\r\n");
			}
		}
		return sb.toString();
	}
}
