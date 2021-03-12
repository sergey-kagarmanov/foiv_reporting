package application.utils;

import application.MainApp;
import application.models.ReportFile;
import application.models.TransportFile;

public class Logger {

	
	public Logger(){
	}
	
	public void log(TransportFile transportFile){
			MainApp.getDb().saveTransportFile(transportFile);//TODO: test if it fills id
	}
	
	/**
	 * Logger for tickets
	 * @param ticket
	 */
	public void log(ReportFile ticket){
		MainApp.getDb().save(ticket);
	}
}
