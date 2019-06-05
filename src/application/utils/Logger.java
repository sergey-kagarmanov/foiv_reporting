package application.utils;

import application.db.Dao;
import application.models.ReportFile;
import application.models.TransportFile;

public class Logger {

	private Dao dao;
	
	public Logger(Dao dao){
		this.dao = dao;
	}
	
	public void log(TransportFile transportFile){
			dao.saveTransportFile(transportFile);//TODO: test if it fills id
	}
	
	/**
	 * Logger for tickets
	 * @param ticket
	 */
	public void log(ReportFile ticket){
		dao.save(ticket);
	}
}
