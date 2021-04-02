package application.utils;

public class Constants {

	public static final String ALL = "Все";
	public static final int OUT_DB = 0;
	public static final int IN_DB = 1;
	public static final int TRANSPORT = 1;
	public static final int SIMPLE = 0;
	public static final String PARENT = "parent";
	
	public static final boolean INPUT = true;
	public static final boolean OUTPUT = false;
	public static final int INPUT_INT = 1;
	public static final int OUTPUT_INT = 0;
	
	public static final String ENCRYPT = "ENCRYPT";
	public static final String SIGN ="SIGN";
	public static final String DECRYPT = "DECRYPT";
	public static final String UNSIGN = "UNSIGN";
	public static final String POST = "POST";
	public static final String PACK = "PACK";
	public static final String RENAME = "RENAME";
	public static final String UNPACK = "UNPACK";
	public static final String COPY = "COPY";
	public static final String CHECK = "CHECK";

	public static final String ENCRYPT_RUS = "Шифрование";
	public static final String SIGN_RUS ="Подпись";
	public static final String DECRYPT_RUS = "Расшифрование";
	public static final String UNSIGN_RUS = "Снятие подписи";
	public static final String POST_RUS = "Отправка";
	public static final String PACK_RUS = "Архивация";
	public static final String RENAME_RUS = "Переименование";
	public static final String UNPACK_RUS = "Разархивация";
	public static final String COPY_RUS = "Копирование";
	public static final String CHECK_RUS = "Проверка";


	public final static String OUT = "Отправлено";

	public final static String IN = "Получено";
	public final static String ANSWER_ON = "Ответ на ";
	public final static String POSITIVE = "Положительный ";
	public final static String NEGATIVE = "Отрицательный ";

	public final static String NO_ANSWER = "Ответ не получен ";
	public final static String UNKNOWN = "Неизвестный исходный файл ";
	
	public final static String[] FILETYPE = { "xml", "txt" };

	public static String direct(boolean b) {
		return b ? IN : OUT;
	}
	
	public static Integer getFileType(String type) {
		switch (type) {
		case "xml":
			return 0;
		case "txt":
			return 1;
		}
		return null;

	}
	
	public static String ACCEPT = "принят";
	public static String[] POSITIVE_CODE = new String[] {"0","01", "00", "1", "000"};
	
	public static boolean isPositive(String code) {
		boolean result = false;
		for(String tmp : POSITIVE_CODE) {
			if (tmp.equals(code)) {
				result = true;
			}
		}
		return result;
	}
	
	public final static String DATE_FORMAT = "date_format";
	
	public final static String DATE_FORMAT_DB = "date_format_db";
	
	public final static String DATE_FORMAT_ARCHIVE = "date_format_archive";
	
	public final static String AUTO_KEY = "auto_key";
	
	public final static String LOAD_KEY = "load_key";
	
	public final static String UNLOAD_KEY = "unload_key";

	public final static String WORK_DIR = "work_dir";
	
	public final static String TMP_DIR = "tmp_dir";

	public final static String VERBA_PATH = "verba_path";
	
	public final static String ARJ_PATH = "arj_path";
	
	public final static String FILE_SIZE = "file_size";
	
	public final static String FILE_COUNT = "file_count";
}
