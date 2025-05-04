package com.zorgeFile.notify;

public class NotifierConsole extends Notifier {

	private String curOperation = "";
	
	public void notify(final String operation, int percent) {
		
		StringBuilder sb = new StringBuilder();
		if(!curOperation.equals(operation)) {
			curOperation = operation;
			sb.append("\n");
		}
		else
			sb.append("\r");
		
		sb.append(operation).append(" : ").append(percent).append(" %");
		System.out.print(sb.toString());
	}
}
