package application.utils;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

public class DateUtils {
	/** Шаблон даты, используемый для преобразования. Можно поменять на свой. */
	/*private static final String DATE_SQLITE = "yyyy-MM-dd HH:mm:ss";
	private static final String DATE_PATTERN = "dd.MM.yyyy HH:mm:ss";
	private static final String DATE_PATTERN_ARCHIVE = "yyyyMMdd";*/
	
    private static final DateTimeFormatter DATE_SQLITE_FORMATTER = DateTimeFormatter.ofPattern(Settings.DATE_FORMAT_DB);

    private static final String PATH_FORMAT = "yyyy.MM.dd";
    
	/** Форматировщик даты. */
	private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter
			.ofPattern(Settings.DATE_FORMAT);

	private static final DateTimeFormatter DATE_FORMATTER_ARCHIVE = DateTimeFormatter
			.ofPattern(Settings.DATE_FORMAT_ARCHIVE);

	/**
	 * Возвращает полученную дату в виде хорошо отформатированной строки.
	 * Используется определённый выше {@link DateUtil#DATE_PATTERN}.
	 * 
	 * @param date
	 *            - дата, которая будет возвращена в виде строки
	 * @return отформатированную строку
	 */
	public static String formatGUI(LocalDateTime date) {
		if (date == null) {
			return null;
		}
		return DATE_FORMATTER.format(date);
	}

	public static String format(LocalDateTime date) {
		if (date == null) {
			return null;
		}
		return DATE_SQLITE_FORMATTER.format(date);
	}

	public static String formatReport(LocalDate date) {
		if (date == null) {
			return null;
		}
		return DATE_FORMATTER_ARCHIVE.format(date);
	}

	/**
	 * Преобразует строку, которая отформатирована по правилам шаблона
	 * {@link DateUtil#DATE_PATTERN} в объект {@link LocalDate}.
	 * 
	 * Возвращает null, если строка не может быть преобразована.
	 * 
	 * @param dateString
	 *            - дата в виде String
	 * @return объект даты или null, если строка не может быть преобразована
	 */
	public static LocalDate parse(String dateString) {
		try {
			return DATE_FORMATTER.parse(dateString, LocalDate::from);
		} catch (DateTimeParseException e) {
			return null;
		}
	}

	/**
	 * Проверяет, является ли строка корректной датой.
	 * 
	 * @param dateString
	 * @return true, если строка является корректной датой
	 */
	public static boolean validDate(String dateString) {
		// Пытаемся разобрать строку.
		return DateUtils.parse(dateString) != null;

	}
	
    public static LocalDateTime fromSQLite(String dateString){
        try {
        	if (dateString!=null && dateString.length()>=23)
            return DateTimeFormatter.ISO_LOCAL_DATE_TIME.parse(dateString, LocalDateTime::from);
        	else
        		return LocalDateTime.now();
        } catch (DateTimeParseException e) {
        	e.printStackTrace();
        	System.out.println(dateString);
            return null;
        }
   	
    }
    
    public static String toSQLite(LocalDateTime datetime){
    	return DateTimeFormatter.ISO_LOCAL_DATE_TIME.format(datetime);
    }
    
    public static String toSQLite(LocalDate datetime){
    	return DateTimeFormatter.ISO_LOCAL_DATE.format(datetime);
    }
    

    public static LocalDateTime fromDeclare(String pattern, String dateString){
   	return LocalDateTime.parse(dateString, DateTimeFormatter.ofPattern(pattern));
    }
    
    public static String toDeclareString(String pattern, LocalDateTime datetime){
    	return DateTimeFormatter.ofPattern(pattern).format(datetime);
    }
    public static String toPath(LocalDateTime ldt) {
    	return DateTimeFormatter.ofPattern(PATH_FORMAT).format(ldt);
    }
    
}
