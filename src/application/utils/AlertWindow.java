package application.utils;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;

import application.models.ErrorFile;
import javafx.collections.ObservableList;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;

public class AlertWindow {

	public static void show(String title, ObservableList<ErrorFile> list) {
		if (list==null || list.size() == 0) {
			Alert alert = new Alert(AlertType.INFORMATION);
			alert.setTitle(title);
			alert.setContentText("Обработка выполнена успешно");
			alert.show();
		}else {
			Alert alert = new Alert(AlertType.ERROR);
			alert.setTitle(title);
			StringBuilder sb  = new StringBuilder();
			sb.append("Обработка выполнена c ошибками в файлах:"+System.lineSeparator());
			list.forEach(file->{
				sb.append(file.getFile());
			});
			alert.setContentText(sb.toString());
			alert.show();
			
			File test = new File("errors.txt");
			List<String> tmp = new ArrayList<>();
			list.forEach(s->{
				tmp.add(s.getFile());
			});
			try {
				Files.write(test.toPath(), tmp);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

		}
	}
	
}
