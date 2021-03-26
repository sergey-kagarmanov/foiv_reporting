package application.utils;

import application.MainApp;
import application.models.WorkingFile;
import javafx.collections.ObservableList;

/**
 * Class check if file is full load, and load rest if necessary
 * @author SKV
 *
 */
public class DataLoader {

	public static ObservableList<WorkingFile> loadChildsLite(WorkingFile file){
		if (file.getChilds()!=null) {
			return file.getChilds();
		}else {
			return MainApp.getDb().getArchiveFilesChildLite(file);
		}
	}
}
