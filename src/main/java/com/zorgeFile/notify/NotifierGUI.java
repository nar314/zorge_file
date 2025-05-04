package com.zorgeFile.notify;

import javax.swing.JLabel;
import javax.swing.JProgressBar;

public class NotifierGUI extends Notifier {

	private JProgressBar pb = null;
	private JLabel lb = null;
	
	public NotifierGUI(final JProgressBar pb, final JLabel lb) {
		
		this.pb = pb;
		this.lb = lb;
	}
	
	public void notify(final String operation, int percent) {
		
		if(pb == null || lb == null)
			return;
		
		lb.setText(operation);
		pb.setValue(percent);
	}
}
