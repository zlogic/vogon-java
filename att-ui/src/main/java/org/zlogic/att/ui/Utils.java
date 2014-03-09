/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package org.zlogic.att.ui;

/**
 *
 * @author Zlogic
 */
public class Utils {
	
	public static String getStackTrace(String message){
		StringBuilder sb = new StringBuilder();
		sb.append(message);
		sb.append(" stack trace:");
		for (StackTraceElement ste : Thread.currentThread().getStackTrace())
			sb.append("\t").append(ste).append("\r\n");
		return sb.toString();
	}
}
