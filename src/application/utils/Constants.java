package application.utils;

public class Constants {

	/*public static final String P1459 = "1459-П";
	public static final String P311 = "311-П";
	public static final String P440 = "440-П";
	public static final String P364 = "364-П";*/

	public static final String ALL = "Все";
	public static final int OUT_DB = 0;
	public static final int IN_DB = 1;
	public static final int TRANSPORT = 1;
	public static final int SIMPLE = 0;
	public static final String PARENT = "parent";
	

	/*public static final int P1459N = 1;
	public static final int P311N = 2;
	public static final int P440N = 3;
	public static final int P364N = 4;*/

	/*public static final String P1459D_PATTERN = "(e|E)(s|S)(d|D)(t|T).+\\.(a|A)(r|R)(j|J)";
	public static final String P1459E_PATTERN = ".+\\.(x|X)(m|M)(l|L)";

	public static final String P311E_PATTERN = "(s|S)(b|B)(c|C|f|F).+\\.(x|X)(m|M)(l|L)";

	public static final String P440D_PATTERN = "(a|A)(f|F)(n|N).+\\.(a|A)(r|R)(j|J)";
	public static final String P440E_PATTERN = "((p|P)(b|B)|((b|B)(((o|O)(s|S))|(v|V)|(n|N)))).+\\.(x|X)(m|M)(l|L)";

	public static final String P364E_PATTERN = ".+\\.(x|X)(m|M)(l|L)";*/

	public static final String[] ACTIONS = new String[] { "TO", // 0
			"ENCRYPT", // 1
			"SIGN", // 2
			"DECRYPT", // 3
			"UNSIGN", // 4
			"POST", // 5
			"PACK", // 6
			"RENAME", // 7
			"UNPACK", // 8
			"COPY" //9

	};

	//public static String VERBA_EXE = "\"c:\\Program Files (x86)\\MDPREI\\РМП Верба-OW\\FColseOW.exe\"";

	/*public static long FILE_SIZE = 7340032;
	public static Integer FILE_COUNT = 50;*/

	public static String OUT = "Отправлено";

	public static String IN = "Получено";
	public final static String ANSWER_ON = "Ответ на ";
	public final static String POSITIVE = "Положительный";
	public final static String NEGATIVE = "Отрицательный";

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
	public static String[] POSITIVE_CODE = new String[] {"0","01", "00", "1"};
	
	public static boolean isPositive(String code) {
		boolean result = false;
		for(String tmp : POSITIVE_CODE) {
			if (tmp.equals(code)) {
				result = true;
			}
		}
		return result;
	}
	
	/*public static boolean AUTO_KEY = true;
	
	public static String LOAD_KEY_STRING = "imdisk -a -m A: -f ";
	
	public static String UNLOAD_KEY_STRING = "imdisk -D -m A:";*/
	
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
