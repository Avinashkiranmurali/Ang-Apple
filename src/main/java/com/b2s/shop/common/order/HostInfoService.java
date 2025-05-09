package com.b2s.shop.common.order;

import java.net.InetAddress;
import java.net.UnknownHostException;

public class HostInfoService {
	
	static HostInfoService service;
	
	static{
		service  = new HostInfoService();
	}

	public static HostInfoService getService(){
		return service;
	}
	
	public String getIP(){
		StringBuffer res = new StringBuffer();
		try {
			InetAddress in = InetAddress.getLocalHost();
			 InetAddress[] all = InetAddress.getAllByName(in.getHostName());
			 for (int i=0; i<all.length; i++) {
				 res.append(all[i]+",");
			 }
		} catch (UnknownHostException e) {
			res.append("NA");
		}
		 return res.toString();
	}
	
}
