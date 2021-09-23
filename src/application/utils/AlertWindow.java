package application.utils;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

import application.errors.ReportError;
import application.models.ErrorFile;
import javafx.collections.ObservableList;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.TextArea;

public class AlertWindow {

	public static void show(String title, ObservableList<ErrorFile> list) {
		if (list == null || list.size() == 0) {
			Alert alert = new Alert(AlertType.INFORMATION);
			alert.setTitle(title);
			alert.setContentText("Обработка выполнена успешно");
			alert.show();
		} else {
			Alert alert = new Alert(AlertType.ERROR);
			alert.setTitle(title);
			StringBuilder sb = new StringBuilder();
			sb.append("Обработка выполнена c ошибками в файлах:" + System.lineSeparator());
			list.forEach(file -> {
				sb.append(file.getFile());
			});
			alert.setContentText(sb.toString());
			alert.show();

			File test = new File("errors.txt");
			List<String> tmp = new ArrayList<>();
			list.forEach(s -> {
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

	public static void show(ReportError error, Logger logger) {
		Alert alert = new Alert(AlertType.ERROR);
		alert.setTitle("Ошибка выполнения");
		StringBuilder sb = new StringBuilder();
		switch (error.getErrorCode()) {
		case ReportError.INIT_SIGNATURA:
			sb.append("Ошибка инициализации Сигнатуры");
			break;
		case ReportError.SIGN_ERROR:
			sb.append("Ошибка подписи. Обработка файла " + error.getFilename() + " выполнена c ошибками");
			break;
		case ReportError.UNSIGN_ERROR:
			sb.append("Ошибка верификации и снятия подписи. Обработка файла " + error.getFilename() + " выполнена c ошибками");
			break;
		case ReportError.ENCRYPT_ERROR:
			sb.append("Ошибка зашифрования. Обработка файла " + error.getFilename() + " выполнена c ошибками");
			break;
		case ReportError.DECRYPT_ERROR:
			sb.append("Ошибка расшифрования. Обработка файла " + error.getFilename() + " выполнена c ошибками");
			break;
		case ReportError.UNHANDLE_SIGNATURA_ERROR:
			sb.append("Ошибка работы функций Сигнатуры");
			break;
		default:
			sb.append(error.getMessage());
			break;
		}
		logger.error(sb.toString());
		logger.debug(error.getE());
		TextArea area = new TextArea(sb.toString());
		area.setWrapText(true);
		area.setEditable(false);

		alert.getDialogPane().setContent(area);
		alert.setResizable(true);
		alert.show();
	}

	public static void show(ObservableList<ReportError> errors, Logger logger) {
		Alert alert = new Alert(AlertType.ERROR);
		alert.setTitle("Ошибка выполнения");

		StringBuilder sb = new StringBuilder();
		errors.forEach(error -> {
			switch (error.getErrorCode()) {
			case ReportError.INIT_SIGNATURA:
				sb.append("Ошибка инициализации Сигнатуры");
				break;
			case ReportError.SIGN_ERROR:
				sb.append("Ошибка подписи. Обработка файла " + error.getFilename() + " выполнена c ошибками");
				break;
			case ReportError.UNSIGN_ERROR:
				sb.append("Ошибка верификации и снятия подписи. Обработка файла " + error.getFilename() + " выполнена c ошибками");
				break;
			case ReportError.ENCRYPT_ERROR:
				sb.append("Ошибка зашифрования. Обработка файла " + error.getFilename() + " выполнена c ошибками");
				break;
			case ReportError.DECRYPT_ERROR:
				sb.append("Ошибка расшифрования. Обработка файла " + error.getFilename() + " выполнена c ошибками");
				break;
			case ReportError.UNHANDLE_SIGNATURA_ERROR:
				sb.append("Ошибка работы функций Сигнатуры");
				break;
			default:
				sb.append(error.getMessage());
				break;
			}
			sb.append("\n");
			logger.debug(error.getE());
		});
		logger.error(sb.toString());
		TextArea area = new TextArea(sb.toString());
		area.setWrapText(true);
		area.setEditable(false);

		alert.getDialogPane().setContent(area);
		alert.setResizable(true);
		alert.show();

	}

}
