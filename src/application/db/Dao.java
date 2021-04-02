package application.db;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import application.MainApp;
import application.errors.ReportError;
import application.models.Action;
import application.models.AttributeDescr;
import application.models.Chain;
import application.models.FileAttribute;
import application.models.FileEntity;
import application.models.FileType;
import application.models.Key;
import application.models.ProcessStep;
import application.models.Report;
import application.models.ReportFile;
import application.models.TicketResults;
import application.models.TransportFile;
import application.models.WorkingFile;
import application.utils.Constants;
import application.utils.DateUtils;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.util.Pair;

public class Dao {

	private Connection connection;

	public Dao() {
		try {
			Class.forName("org.sqlite.JDBC");
			// connection =
			// DriverManager.getConnection(Configuration.get("db_path","jdbc:sqlite:fc_reports.db").toString());
			connection = DriverManager.getConnection("jdbc:sqlite:fc_reports.db");
			System.out.println("Database connection");
			MainApp.info("Database connected");
			checkDB();
		} catch (SQLException e) {
			e.printStackTrace();
			MainApp.error(e.getLocalizedMessage());
		} catch (ClassNotFoundException e1) {
			e1.printStackTrace();
			MainApp.error(e1.getLocalizedMessage());
		}
	}

	private void checkDB() {
		boolean updateFlag = false;
		String sql = "SELECT uuid FROM files";
		PreparedStatement ps = null;
		PreparedStatement ps2 = null;
		PreparedStatement ps3 = null;
		PreparedStatement ps4 = null;
		PreparedStatement ps5 = null;
		ResultSet rs = null;
		try {
			ps = connection.prepareStatement(sql);
			ps.executeQuery();
		} catch (Exception e) {
			updateFlag = true;
		} finally {
			try {
				if (ps != null)
					ps.close();
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}

		if (updateFlag) {
			List<String> updates = null;
			try {
				updates = Files.readAllLines(new File("sql_update.sql").toPath());
			} catch (IOException e) {
				e.printStackTrace();
			}
			try {
				for (String update : updates) {
					connection.createStatement().execute(update);
				}
				ps = connection.prepareStatement("Select id FROM files");
				rs = ps.executeQuery();
				List<Integer> ids = new ArrayList<>();
				while (rs.next()) {
					ids.add(rs.getInt("id"));
				}
				ps.close();
				ps = connection.prepareStatement("UPDATE files set uuid = ? WHERE id = ?");
				ps2 = connection.prepareStatement("UPDATE transport_files set parent_uuid = ? WHERE parent_id =?");
				ps3 = connection.prepareStatement("UPDATE transport_files set child_uuid = ? WHERE child_id =?");
				ps4 = connection.prepareStatement("UPDATE files set linked_uuid = ? WHERE linked_id = ?");
				ps5 = connection.prepareStatement("UPDATE file_attributes set file_uuid = ? WHERE file_id = ?");
				UUID uuid = null;
				for (Integer id : ids) {
					uuid = UUID.randomUUID();
					ps.setString(1, uuid.toString());
					ps.setInt(2, id);
					ps.addBatch();
					ps2.setString(1, uuid.toString());
					ps2.setInt(2, id);
					ps2.addBatch();
					ps3.setString(1, uuid.toString());
					ps3.setInt(2, id);
					ps3.addBatch();
					ps4.setString(1, uuid.toString());
					ps4.setInt(2, id);
					ps4.addBatch();
					ps5.setString(1, uuid.toString());
					ps5.setInt(2, id);
					ps5.addBatch();
				}
				ps.executeBatch();
				ps2.executeBatch();
				ps3.executeBatch();
				ps4.executeBatch();
				ps5.executeBatch();
			} catch (Exception e) {
				e.printStackTrace();
			} finally {
				try {
					if (ps != null)
						ps.close();
					if (ps2 != null)
						ps2.close();
					if (ps3 != null)
						ps3.close();
					if (ps4 != null)
						ps4.close();
					if (ps5 != null)
						ps5.close();
				} catch (SQLException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}

			try {
				updates = Files.readAllLines(new File("sql_update2.sql").toPath());
			} catch (IOException e) {
				e.printStackTrace();
			}
			for (String update : updates) {
				try {
					connection.createStatement().execute(update);
				} catch (SQLException e) {
					e.printStackTrace();
				}
			}

		}
	}

	public int saveTransport(Report report, String file, int encrypt, LocalDateTime dateTime) {
		int id = 0;
		try {
			String sql = "INSERT INTO transport(report_id, name, encrypt, datetime) VALUES (?,?,?,?)";
			PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
			ps.setInt(1, report.getId());
			ps.setString(2, file);
			ps.setInt(3, encrypt);
			ps.setString(4, DateUtils.format(dateTime));
			ps.executeUpdate();
			ResultSet rs = ps.getGeneratedKeys();
			if (rs.next()) {
				id = rs.getInt(1);
			}
			rs.close();
			ps.close();
		} catch (Exception e) {
			MainApp.error(e.getLocalizedMessage());
			e.printStackTrace();
		}
		return id;
	}

	public int saveFile(Report report, String file, LocalDateTime datetime, String container) {
		int id = 0;

		try {
			String sql = "INSERT INTO files(report_id, name, datetime) VALUES (?,?,?)";
			PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
			ps.setInt(1, report.getId());
			ps.setString(2, file);
			ps.setString(3, DateUtils.format(datetime));
			ps.executeUpdate();
			ResultSet rs = ps.getGeneratedKeys();
			if (rs.next()) {
				id = rs.getInt(1);
			}
			rs.close();
			ps.close();

			int transportId = 0;
			sql = "SELECT id FROM transport WHERE name LIKE ?";
			ps.setString(1, container);
			rs = ps.executeQuery();
			if (rs.next()) {
				transportId = rs.getInt("id");
			}
			rs.close();
			ps.close();

			if (id != 0 && transportId != 0) {
				sql = "INSERT INTO transport_contain(transport_id, file_id)VALUES(?,?)";
				ps = connection.prepareStatement(sql);
				ps.setInt(1, transportId);
				ps.setInt(2, id);
				ps.executeUpdate();
				ps.close();
			}
		} catch (SQLException e) {
			MainApp.error(e.getLocalizedMessage());
			e.printStackTrace();
		}
		return id;
	}

	/**
	 * Returns String[] of 2 elements: first path for encrypt, second to
	 * decrypt.
	 * 
	 * @param reportName
	 * @return
	 */
	@Deprecated
	public String[] getPathForReport(Report report) {

		String[] tmp = new String[6];
		try {
			PreparedStatement ps = connection
					.prepareStatement("SELECT cp.* FROM current_path cp LEFT JOIN reports r ON cp.report_id = r.id WHERE r.id = ?");
			ps.setInt(1, report.getId());
			ResultSet rs = ps.executeQuery();
			if (rs.next()) {
				tmp[0] = rs.getString("path_encrypt");
				tmp[1] = rs.getString("path_decrypt");
				tmp[2] = rs.getString("path_encrypt_archive");
				tmp[3] = rs.getString("path_decrypt_archive");
				tmp[4] = rs.getString("output_encrypt_path");
				tmp[5] = rs.getString("output_decrypt_path");
			}
			rs.close();
			ps.close();
		} catch (SQLException e) {
			MainApp.error(e.getLocalizedMessage());
			e.printStackTrace();
		}

		return tmp;
	}

	@Deprecated
	public void savePath(Report report, String path, boolean decrypt, boolean archive) {
		try {
			String sql = "UPDATE current_path SET path_";
			if (decrypt) {
				sql += "decrypt";
			} else {
				sql += "encrypt";
			}
			if (archive) {
				sql += "_archive";
			}
			sql += "=? where report_id=?";
			PreparedStatement ps = connection.prepareStatement(sql);
			ps.setString(1, path);
			ps.setInt(2, report.getId());
			ps.executeUpdate();
			ps.close();

		} catch (SQLException e) {
			MainApp.error(e.getLocalizedMessage());
			e.printStackTrace();
		}
	}

	public void savePath(Report report, String path, boolean direction, boolean archive, boolean output) {
		String sql = "UPDATE reports SET ";
		String p = "path";
		if (direction) {
			p = p + "_in";
		} else {
			p = p + "_out";
		}
		if (archive) {
			p = "archive_" + p;
		}
		if (output) {
			p = "output_" + p;
		}
		sql += p + "=? WHERE id = ?";
		PreparedStatement ps = null;
		try {
			ps = connection.prepareStatement(sql);
			ps.setString(1, path);
			ps.setInt(2, report.getId());
			ps.executeUpdate();
		} catch (SQLException e) {
			MainApp.error(e.getLocalizedMessage());
			e.printStackTrace();
		} finally {
			try {
				ps.close();
			} catch (SQLException e) {
				MainApp.error(e.getLocalizedMessage());
				e.printStackTrace();
			}
		}
	}

	@Deprecated
	public void saveOutputPath(Report report, String path, boolean decrypt) {
		try {
			String sql = "UPDATE current_path SET output_";
			if (decrypt) {
				sql += "decrypt";
			} else {
				sql += "encrypt";
			}
			sql += "_path =? WHERE report_id=?";
			PreparedStatement ps = connection.prepareStatement(sql);
			ps.setString(1, path);
			ps.setInt(2, report.getId());
			ps.executeUpdate();
			ps.close();
		} catch (SQLException e) {
			MainApp.error(e.getLocalizedMessage());
			e.printStackTrace();
		}
	}

	public ObservableList<Report> getReports() {
		List<Report> reports = new ArrayList<Report>();
		Report tmpReport = null;
		try {
			String sql = "SELECT r.id, r.name, r.output_path_in, r.output_path_out, r.path_out, r.archive_path_out, r.path_in, r.archive_path_in FROM reports r ";
			PreparedStatement ps = connection.prepareStatement(sql);
			ResultSet rs = ps.executeQuery();
			while (rs.next()) {
				tmpReport = new Report(rs.getInt("id"), rs.getString("name"), null, null, null, null, rs.getString("path_out"),
						rs.getString("path_in"), rs.getString("archive_path_out"), rs.getString("archive_path_in"), rs.getString("output_path_out"),
						rs.getString("output_path_in"), getTickets(rs.getInt("id")));
				/**
				 * Get transport decrypt pattern
				 */
				tmpReport.setTransportInPattern(getFileType(tmpReport.getId(), Constants.IN_DB, Constants.TRANSPORT));
				/**
				 * Get transport encrypt pattern
				 */
				tmpReport.setTransportOutPattern(getFileType(tmpReport.getId(), Constants.OUT_DB, Constants.TRANSPORT));
				/**
				 * Get report encrypt patterns
				 */
				tmpReport.setPatternOut(getFileTypeAsList(tmpReport.getId(), Constants.OUT_DB, Constants.SIMPLE));

				/**
				 * Get report decrypt patterns
				 */
				tmpReport.setPatternIn(getFileTypeAsList(tmpReport.getId(), Constants.IN_DB, Constants.SIMPLE));

				reports.add(tmpReport);

			}
			rs.close();
			ps.close();
		} catch (SQLException e) {
			MainApp.error(e.getLocalizedMessage());
			e.printStackTrace();
		}
		return FXCollections.observableArrayList(reports);
	}

	public ObservableList<FileType> getTickets(Integer reportId) {
		String sql = "SELECT * FROM file_types WHERE report_id = ? AND ticket = 1";
		PreparedStatement ps = null;
		ResultSet rs = null;
		ObservableList<FileType> list = FXCollections.observableArrayList();

		try {
			ps = connection.prepareStatement(sql);
			ps.setInt(1, reportId);
			rs = ps.executeQuery();
			while (rs.next()) {
				list.add(new FileType(rs.getInt("id"), rs.getString("name"), rs.getString("mask"), rs.getString("validation_schema"),
						rs.getInt("direction") == 1, rs.getInt("transport") == 1, rs.getInt("ticket") == 1, rs.getInt("file_type"),
						getResults(rs.getInt("id"))));
			}
		} catch (SQLException e) {
			MainApp.error(e.getLocalizedMessage());
			e.printStackTrace();
		} finally {
			try {
				rs.close();
				ps.close();
			} catch (SQLException e) {
				MainApp.error(e.getLocalizedMessage());
				e.printStackTrace();
			}
		}
		return list;
	}

	public ObservableList<TicketResults> getResults(Integer fileTypeId) {
		String sql = "SELECT tr.* FROM file_type_result ftr LEFT JOIN ticket_results tr ON ftr.ticket_result_id = tr.id WHERE ftr.file_type_id=?";
		PreparedStatement ps = null;
		ResultSet rs = null;
		ObservableList<TicketResults> results = FXCollections.observableArrayList();
		try {
			ps = connection.prepareStatement(sql);
			ps.setInt(1, fileTypeId);
			rs = ps.executeQuery();
			while (rs.next()) {
				results.add(new TicketResults(rs.getInt("id"), getFileAttribute(rs.getInt("attribute_id")), rs.getString("value"),
						rs.getInt("accept") == 1));
			}
		} catch (SQLException e) {
			MainApp.error(e.getLocalizedMessage());
			e.printStackTrace();
		} finally {
			try {
				rs.close();
				ps.close();
			} catch (SQLException e) {
				MainApp.error(e.getLocalizedMessage());
				e.printStackTrace();
			}
		}
		return results.size() != 0 ? results : null;
	}

	/**
	 * for encrypt - 1, for decrypt - 0
	 * 
	 * @param report
	 * @param encrypt
	 * @return
	 */
	@Deprecated
	public ProcessStep getActionForReport(Report report, int encrypt) {
		ProcessStep step = null;

		try {
			String sql = "SELECT k.id as kid, k.name as kname, k.data as kdata, t.id as tid, t.name as tname, a.data as adata, a.position, a.id, t.position FROM action a LEFT JOIN action_type t ON t.id = a.type LEFT JOIN key k ON a.key = k.id WHERE report_id = ? AND a.encrypt = ? ORDER BY position DESC";
			PreparedStatement ps = connection.prepareStatement(sql);
			ps.setInt(1, report.getId());
			ps.setInt(2, encrypt);
			ResultSet rs = ps.executeQuery();
			while (rs.next()) {
				Key k = new Key(rs.getInt("kid"), rs.getString("kname"), rs.getString("kdata"));
				Action a = new Action(rs.getInt("tid"), rs.getString("tname"));
				ProcessStep tmp = new ProcessStep(rs.getInt("id"), a, k, step, rs.getString("adata"), rs.getInt("position"));
				step = tmp;
			}
			rs.close();
			ps.close();
		} catch (SQLException e) {
			MainApp.error(e.getLocalizedMessage());
			e.printStackTrace();
		}
		return step;
	}

	/**
	 * Return files without linked files and attributes, but transport files
	 * with list it contains
	 * 
	 * @param report
	 * @param direction
	 * @return
	 */
	@Deprecated
	public ObservableList<TransportFile> getArchiveFiles(Report report, Boolean direction) {
		List<TransportFile> files = new ArrayList<TransportFile>();
		TransportFile tfile = null;
		UUID id = null;
		Map<String, ReportFile> listFiles = null;
		try {
			String sql = "SELECT f.uuid, f.name, f.datetime, f2.uuid as cuuid, f2.name as cname, f2.datetime as cdatetime, f.direction, f.report_id, f.type_id as parent_type, f2.type_id as child_type FROM files f LEFT JOIN transport_files tf ON f.uuid = tf.parent_uuid LEFT JOIN files f2 ON tf.child_uuid = f2.uuid WHERE f.report_id = ? AND f.direction = ? ORDER BY f.datetime DESC, f.uuid ASC";
			PreparedStatement ps = connection.prepareStatement(sql);
			ps.setInt(1, report.getId());
			ps.setInt(2, direction ? 1 : 0);
			ResultSet rs = ps.executeQuery();
			while (rs.next()) {
				if (id != UUID.fromString(rs.getString("uuid"))) {
					id = UUID.fromString(rs.getString("uuid"));
					listFiles = new HashMap<String, ReportFile>();
					tfile = new TransportFile(id, rs.getString("name"), DateUtils.fromSQLite(rs.getString("datetime")), report, direction, null,
							listFiles, getFileType(rs.getInt("parent_type")));
					files.add(tfile);
				}
				listFiles.put(rs.getString("cname"), new ReportFile(UUID.fromString(rs.getString("cuuid")), rs.getString("cname"),
						DateUtils.fromSQLite(rs.getString("cdatetime")), report, direction, null, null, getFileType(rs.getInt("child_type"))));
			}
			rs.close();
			ps.close();
		} catch (SQLException e) {
			MainApp.error(e.getLocalizedMessage());
			e.printStackTrace();
		}
		return FXCollections.observableArrayList(files);
	}

	public ObservableList<WorkingFile> getArchiveFilesLite(Report report, int direction) {
		ObservableList<WorkingFile> files = FXCollections.observableArrayList();
		String sql = "SELECT f.uuid, f.name, f.datetime from files f LEFT JOIN file_types ft ON f.type_id = ft.id WHERE f.report_id = ? AND f.direction = ? AND ft.transport = 1 ORDER BY f.datetime DESC";
		try (PreparedStatement ps = connection.prepareStatement(sql)) {
			ps.setInt(1, report.getId());
			ps.setInt(2, direction);
			ResultSet rs = ps.executeQuery();
			while (rs.next()) {
				WorkingFile file = new WorkingFile(WorkingFile.LOADED);
				file.setUUID(UUID.fromString(rs.getString("uuid")));
				file.setOriginalName(rs.getString("name"));
				file.setDatetime(DateUtils.fromSQLite(rs.getString("datetime")));
				files.add(file);
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return files;
	}

	public ObservableList<WorkingFile> getArchiveFilesChildLite(WorkingFile parent) {
		ObservableList<WorkingFile> files = FXCollections.observableArrayList();
		String sql = "SELECT f.uuid, f.name, f.datetime from files f  WHERE f.linked_uuid = ?";
		try (PreparedStatement ps = connection.prepareStatement(sql)) {
			ps.setString(1, parent.getUUID().toString());
			ResultSet rs = ps.executeQuery();
			while (rs.next()) {
				WorkingFile file = new WorkingFile(WorkingFile.LOADED);
				file.setUUID(UUID.fromString(rs.getString("uuid")));
				file.setOriginalName(rs.getString("name"));
				file.setDatetime(DateUtils.fromSQLite(rs.getString("datetime")));
				files.add(file);
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return files;

	}

	/**
	 * Return files without linked files and attributes, but transport files
	 * with list it contains
	 * 
	 * @param report
	 * @param direction
	 * @return
	 */
	public ObservableList<TransportFile> getArchiveFilesPerDay(Report report, Boolean direction, LocalDate date) {
		List<TransportFile> files = new ArrayList<TransportFile>();
		TransportFile tfile = null;
		UUID id = null;
		Map<String, ReportFile> listFiles = null;
		try {
			String sql = "SELECT f.name, f.datetime, f2.uuid as cuuid, f2.name as cname, f2.datetime as cdatetime, f.direction, f.report_id, f.type_id as parent_type, f2.type_id as child_type FROM files f LEFT JOIN transport_files tf ON f.uuid = tf.parent_uuid LEFT JOIN files f2 ON tf.child_uuid = f2.uuid WHERE f.report_id = ? AND f.direction = ? AND f.datetime >= ?  ORDER BY f.datetime ASC";
			PreparedStatement ps = connection.prepareStatement(sql);
			ps.setInt(1, report.getId());
			ps.setInt(2, direction ? 1 : 0);
			ps.setString(3, DateUtils.toSQLite(date.minusDays(1).atStartOfDay()));
			ResultSet rs = ps.executeQuery();
			while (rs.next()) {
				/*
				 * if (id != rs.getInt("uuid")) { id = rs.getInt("id");
				 * listFiles = new HashMap<String, ReportFile>(); tfile = new
				 * TransportFile(id, rs.getString("name"),
				 * DateUtils.fromSQLite(rs.getString("datetime")), report,
				 * direction, null, listFiles,
				 * getFileType(rs.getInt("parent_type"))); files.add(tfile); }
				 * listFiles.put(rs.getString("cname"), new
				 * ReportFile(rs.getInt("cid"), rs.getString("cname"),
				 * DateUtils.fromSQLite(rs.getString("cdatetime")), report,
				 * direction, null, null,
				 * getFileType(rs.getInt("child_type"))));
				 */
			}
			rs.close();
			ps.close();
		} catch (SQLException e) {
			MainApp.error(e.getLocalizedMessage());
			e.printStackTrace();
		}

		return FXCollections.observableArrayList(files);
	}

	public ObservableList<WorkingFile> getArchivesPerDay(Report report, int direction, LocalDate date) {
		String sql = "SELECT f.uuid, f.name, f.datetime FROM files f LEFT JOIN file_type ft ON f.type_id = ft.id WHERE f.report_id = ? AND f.direction = ? AND ft.id IS NOT NULL AND f.datetime > ? AND ft.transport=1 ORDER BY f.datetime ASC";
		ObservableList<WorkingFile> result = FXCollections.observableArrayList();
		try (PreparedStatement ps = connection.prepareStatement(sql)) {
			ps.setInt(1, report.getId());
			ps.setInt(2, direction);
			ps.setString(3, DateUtils.toSQLite(date));
			ResultSet rs = ps.executeQuery();
			while (rs.next()) {
				WorkingFile file = new WorkingFile(WorkingFile.LOADED);
				file.setUUID(UUID.fromString(rs.getString("uuid")));
				file.setOriginalName(rs.getString("name"));
				file.setDatetime(DateUtils.fromSQLite(rs.getString("datetime")));
				result.add(file);
			}
		} catch (Exception e) {
			// TODO: handle exception
		}
		return result;
	}

	public String getLastArchiveToday(Report report) {
		String result = null;
		String sql = "SELECT f.name FROM files f LEFT JOIN file_types ft ON f.type_id = ft.id WHERE f.report_id = ? AND f.direction = ? AND ft.id IS NOT NULL AND f.datetime > ? AND ft.transport=1 ORDER BY f.datetime DESC LIMIT 1";

		try (PreparedStatement ps = connection.prepareStatement(sql)) {
			ps.setInt(1, report.getId());
			ps.setInt(2, Constants.OUTPUT_INT);
			ps.setString(3, DateUtils.toSQLite(LocalDate.now()));
			ResultSet rs = ps.executeQuery();
			if (rs.next()) {
				result = rs.getString("name");
			}
		} catch (Exception e) {
			// TODO: handle exception
		}
		return result;
	}

	/**
	 * Return files without linked files and attributes, but transport files
	 * with list it contains
	 * 
	 * @param report
	 * @param direction
	 * @return
	 */
	@Deprecated
	public ObservableList<TransportFile> getArchiveFiles(Report report, Boolean direction, LocalDate date) {
		List<TransportFile> files = new ArrayList<TransportFile>();
		TransportFile tfile = null;
		Integer id = 0;
		Map<String, ReportFile> listFiles = null;
		try {
			String sql = "SELECT f.id, f.name, f.datetime, f2.id as cid, f2.name as cname, f2.datetime as cdatetime, f.direction, f.report_id, f.type_id as parent_type, f2.type_id as child_type FROM files f LEFT JOIN transport_files tf ON f.id = tf.parent_id LEFT JOIN files f2 ON tf.child_id = f2.id WHERE f.report_id = ? AND f.direction = ? AND tf.id IS NOT NULL AND f.datetime > ? ORDER BY f.id ASC , f.datetime ASC";
			PreparedStatement ps = connection.prepareStatement(sql);
			ps.setInt(1, report.getId());
			ps.setInt(2, direction ? 1 : 0);
			ps.setString(3, DateUtils.toSQLite(date));
			ResultSet rs = ps.executeQuery();
			while (rs.next()) {
				/*
				 * if (id != rs.getInt("id")) { id = rs.getInt("id"); listFiles
				 * = new HashMap<String, ReportFile>(); tfile = new
				 * TransportFile(id, rs.getString("name"),
				 * DateUtils.fromSQLite(rs.getString("datetime")), report,
				 * direction, null, listFiles,
				 * getFileType(rs.getInt("parent_type"))); files.add(tfile); }
				 * listFiles.put(rs.getString("cname"), new
				 * ReportFile(rs.getInt("cid"), rs.getString("cname"),
				 * DateUtils.fromSQLite(rs.getString("cdatetime")), report,
				 * direction, null, null,
				 * getFileType(rs.getInt("child_type"))));
				 */
			}
			rs.close();
			ps.close();
		} catch (SQLException e) {
			MainApp.error(e.getLocalizedMessage());
			e.printStackTrace();
		}

		return FXCollections.observableArrayList(files);
	}

	public Map<String, FileEntity> getArchiveFiles() {
		Map<String, FileEntity> files = new HashMap<String, FileEntity>();
		FileEntity tfile = null;
		Integer id = 0;
		Map<String, ReportFile> listFiles = null;
		try {
			String sql = "SELECT f.id, f.name, f.datetime, f.linked_id, f2.id as cid, f2.name as cname, f2.datetime as cdatetime,"
					+ " f.direction, f.report_id, f2.direction as cdir"
					+ ", r.id as rid, f.type_id as parent_type, f2.type_id as child_type FROM files f LEFT JOIN transport_files tf ON f.id = tf.parent_id LEFT JOIN files f2 ON tf.child_id = f2.id LEFT JOIN reports r ON f.report_id = r.id WHERE tf.id IS NOT NULL ORDER BY f.id ASC , f.datetime ASC";
			PreparedStatement ps = connection.prepareStatement(sql);
			ResultSet rs = ps.executeQuery();
			while (rs.next()) {
				if (id != rs.getInt("id")) {
					id = rs.getInt("id");
					listFiles = new HashMap<String, ReportFile>();
					/*
					 * tfile = new TransportFile(id, rs.getString("name"),
					 * DateUtils.fromSQLite(rs.getString("datetime")),
					 * getReportById(rs.getInt("rid")), rs.getInt("cdir") == 1,
					 * rs.getObject("linked_id") != null ?
					 * getReportFileById(rs.getInt("linked_id")) : null,
					 * listFiles, getFileType(rs.getInt("parent_type")));
					 */
					files.put(tfile.getName(), tfile);
				}
				listFiles.put(rs.getString("cname"), getReportFileById(rs.getInt("cid")));
			}
			rs.close();
			ps.close();
		} catch (SQLException e) {
			MainApp.error(e.getLocalizedMessage());
			e.printStackTrace();
		}

		return files;
	}
	
	public List<WorkingFile> getTicketFilesPerPeriod(LocalDate start, LocalDate end, Report report, Boolean direction){
		List<WorkingFile> result = new ArrayList<>();
		String sql = "select f.name, f.datetime, f.direction, f.uuid, f.type_id from files f left join file_types ft ON f.type_id = ft.id WHERE ticket = 1";
		String sqlEnd = " ORDER BY f.datetime ASC";
		if (start != null) {
			sql += " AND f.datetime > ?";
		}
		if (end != null) {
			sql += " AND f.datetime < ?";
		}
		if (report != null)
			sql += " AND f.report_id = ? ";
		if (direction!=null)
			sql += " AND f.direction = ? ";
		try(PreparedStatement ps = connection.prepareStatement(sql+sqlEnd)){
			int i = 1;
			if (start != null) {
				ps.setString(i, DateUtils.toSQLite(start.atStartOfDay()));
				i++;
			}
			if (end != null) {
				ps.setString(i, DateUtils.toSQLite(end.atTime(23, 59, 59)));
				i++;
			}
			if (report != null) {
				ps.setInt(i, report.getId());
				i++;
			}
			if (direction !=null) {
				ps.setInt(i, direction ? 1:0);
			}

			ResultSet rs = ps.executeQuery();
			while(rs.next()) {
				WorkingFile wFile = new WorkingFile(WorkingFile.LOADED);
				wFile.setUUID(UUID.fromString(rs.getString("uuid")));
				wFile.setOriginalName(rs.getString("name"));
				wFile.setDatetime(DateUtils.fromSQLite(rs.getString("datetime")));
				wFile.setDirection(rs.getInt("direction")==1);
				wFile.setType(getFileType(rs.getInt("type_id")));
				loadAttributesByFile(wFile);
				result.add(wFile);
			}
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return result;
	}
	
	public List<WorkingFile> getTransportFilesPerPeriod(LocalDate start, LocalDate end, Report report, Boolean direction){
		List<WorkingFile> result = new ArrayList<>();
		String sql = "select f.name, f.datetime, f.direction, f.uuid, f.type_id from files f left join file_types ft ON f.type_id = ft.id WHERE transport = 1";
		String sqlEnd = " ORDER BY f.datetime ASC";
		if (start != null) {
			sql += " AND f.datetime > ?";
		}
		if (end != null) {
			sql += " AND f.datetime < ?";
		}
		if (report != null)
			sql += " AND f.report_id = ? ";
		if (direction!=null)
			sql += " AND f.direction = ? ";
		try(PreparedStatement ps = connection.prepareStatement(sql+sqlEnd)){
			int i = 1;
			if (start != null) {
				ps.setString(i, DateUtils.toSQLite(start.atStartOfDay()));
				i++;
			}
			if (end != null) {
				ps.setString(i, DateUtils.toSQLite(end.atTime(23, 59, 59)));
				i++;
			}
			if (report != null) {
				ps.setInt(i, report.getId());
				i++;
			}
			if (direction !=null) {
				ps.setInt(i, direction ? 1:0);
			}

			ResultSet rs = ps.executeQuery();
			while(rs.next()) {
				WorkingFile wFile = new WorkingFile(WorkingFile.LOADED);
				wFile.setUUID(UUID.fromString(rs.getString("uuid")));
				wFile.setOriginalName(rs.getString("name"));
				wFile.setDatetime(DateUtils.fromSQLite(rs.getString("datetime")));
				wFile.setDirection(rs.getInt("direction")==1);
				wFile.setType(getFileType(rs.getInt("type_id")));
				loadAttributesByFile(wFile);
				loadChilds(wFile);
				loadLinkedFile(wFile);
				result.add(wFile);
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return result;
	}
	
	public List<WorkingFile> getNonTransportFilesPerPeriod(LocalDate start, LocalDate end, Report report, Boolean direction){
		List<WorkingFile> result = new ArrayList<>();
		String sql = "select f.name, f.datetime, f.direction, f.uuid, f.type_id from files f left join file_types ft ON f.type_id = ft.id WHERE transport = 0";
		String sqlEnd = " ORDER BY f.datetime ASC";
		if (start != null) {
			sql += " AND f.datetime > ?";
		}
		if (end != null) {
			sql += " AND f.datetime < ?";
		}
		if (report != null)
			sql += " AND f.report_id = ? ";
		if (direction!=null)
			sql += " AND f.direction = ? ";
		try(PreparedStatement ps = connection.prepareStatement(sql+sqlEnd)){
			int i = 1;
			if (start != null) {
				ps.setString(i, DateUtils.toSQLite(start.atStartOfDay()));
				i++;
			}
			if (end != null) {
				ps.setString(i, DateUtils.toSQLite(end.atTime(23, 59, 59)));
				i++;
			}
			if (report != null) {
				ps.setInt(i, report.getId());
				i++;
			}
			if (direction !=null) {
				ps.setInt(i, direction ? 1:0);
			}

			ResultSet rs = ps.executeQuery();
			while(rs.next()) {
				WorkingFile wFile = new WorkingFile(WorkingFile.LOADED);
				wFile.setUUID(UUID.fromString(rs.getString("uuid")));
				wFile.setOriginalName(rs.getString("name"));
				wFile.setDatetime(DateUtils.fromSQLite(rs.getString("datetime")));
				wFile.setDirection(rs.getInt("direction")==1);
				wFile.setType(getFileType(rs.getInt("type_id")));
				loadAttributesByFile(wFile);
				loadLinkedFile(wFile);
				result.add(wFile);
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return result;
	}

	public void loadChilds(WorkingFile file) {
		ObservableList<WorkingFile> childs = null;
		if(file.getChilds()!=null) {
			childs = file.getChilds();
		}else {
			childs = FXCollections.observableArrayList();
			file.setChilds(childs);
		}
		
		String sql = "select f.* from transport_files tf LEFT JOIN files f ON f.uuid = tf.child_uuid WHERE tf.parent_uuid = ?";
		try(PreparedStatement ps = connection.prepareStatement(sql)){
			ps.setString(1, file.getUUID().toString());
			ResultSet rs = ps.executeQuery();
			while(rs.next()) {
				WorkingFile wFile = new WorkingFile(WorkingFile.LOADED);
				wFile.setUUID(UUID.fromString(rs.getString("uuid")));
				wFile.setOriginalName(rs.getString("name"));
				wFile.setDatetime(DateUtils.fromSQLite(rs.getString("datetime")));
				wFile.setDirection(rs.getInt("direction")==1);
				wFile.setType(getFileType(rs.getInt("type_id")));
				loadAttributesByFile(wFile);
				loadLinkedFile(wFile);
				childs.add(wFile);
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	
	public void loadLinkedFile(WorkingFile file) {
		String sql = "SELECT * FROM files WHERE linked_uuid = ?";
		try(PreparedStatement ps = connection.prepareStatement(sql)){
			ps.setString(1, file.getUUID().toString());
			ResultSet rs = ps.executeQuery();
			if(rs.next()) {
				WorkingFile linked = new WorkingFile(WorkingFile.LOADED);
				linked.setUUID(UUID.fromString(rs.getString("uuid")));
				linked.setOriginalName(rs.getString("name"));
				linked.setDatetime(DateUtils.fromSQLite(rs.getString("datetime")));
				linked.setDirection(rs.getInt("direction")==1);
				linked.setType(getFileType(rs.getInt("type_id")));
				loadAttributesByFile(linked);
				file.setLinked(linked);
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	
	public void loadAttributesByFile(WorkingFile file){
		Map<String, FileAttribute> attributes = null;
		if (file.getAttributes() == null) {
			attributes = new HashMap<>();
			file.setAttributes(attributes);
		}else {
			attributes = file.getAttributes();
		}
		
		String sql = "select fa.value, a.name, a.id from file_attributes fa left join attributes a ON fa.attribute_id = a.id WHERE fa.file_uuid = ?";
		try(PreparedStatement ps = connection.prepareStatement(sql)){
			ps.setString(1, file.getUUID().toString());
			ResultSet rs = ps.executeQuery();
			while(rs.next()) {
				attributes.put(rs.getString("name"), new FileAttribute(rs.getInt("id"), rs.getString("name"), rs.getString("value")));
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	
	public List<WorkingFile> getArchiveByRange(LocalDate start, LocalDate end, Report report){
		List<WorkingFile> files = new ArrayList<>();
		String sql = "SELECT f.uuid, f.name, f.datetime, f.linked_uuid, f2.uuid as cid, f2.name as cname, f2.datetime as cdatetime, f.direction, f.report_id, f2.direction as cdir, r.id as rid, f.type_id as parent_type FROM files f LEFT JOIN transport_files tf ON f.uuid = tf.parent_uuid LEFT JOIN files f2 ON tf.child_uuid = f2.uuid LEFT JOIN reports r ON f.report_id = r.id WHERE f.uuid IS NOT NULL ";
		String sqlEnd = " ORDER BY f.datetime ASC, f.id ASC";
		
			if (start != null) {
				sql += " AND f.datetime > ?";
			}
			if (end != null) {
				sql += " AND f.datetime < ?";
			}
			if (report != null)
				sql += " AND f.report_id = ? ";
			
			try(PreparedStatement ps = connection.prepareStatement(sql+sqlEnd)){
				int i = 1;
				if (start != null) {
					ps.setString(i, DateUtils.toSQLite(start.atStartOfDay()));
					i++;
				}
				if (end != null) {
					ps.setString(i, DateUtils.toSQLite(end.atTime(23, 59, 59)));
					i++;
				}
				if (report != null)
					ps.setInt(i, report.getId());

				ResultSet rs = ps.executeQuery();

				while(rs.next()) {
					WorkingFile wFile = new WorkingFile(WorkingFile.LOADED);
					wFile.setUUID(UUID.fromString(rs.getString("uuid")));
					wFile.setDatetime(DateUtils.fromSQLite(rs.getString("datetime")));
				}
			} catch (SQLException e) {
				e.printStackTrace();
			}
			return files;
	}

	/**
	 * Return file with filter, if it is null, this parameter will not be used
	 * 
	 * @param start
	 * @param end
	 * @param report
	 * @return
	 */
	public Map<String, FileEntity> getArchiveFiles(LocalDate start, LocalDate end, Report report) {
		Map<String, FileEntity> files = new HashMap<String, FileEntity>();
		FileEntity tfile = null;
		Integer id = 0;
		Map<String, ReportFile> listFiles = null;
		String sql = "SELECT f.id, f.name, f.datetime, f.linked_id, f2.id as cid, f2.name as cname, f2.datetime as cdatetime, f.direction, f.report_id, f2.direction as cdir, r.id as rid, f.type_id as parent_type FROM files f LEFT JOIN transport_files tf ON f.id = tf.parent_id LEFT JOIN files f2 ON tf.child_id = f2.id LEFT JOIN reports r ON f.report_id = r.id WHERE tf.id IS NOT NULL";
		String sqlEnd = " ORDER BY f.datetime ASC, f.id ASC";
		try {
			if (start != null) {
				sql += " AND f.datetime > ?";
			}
			if (end != null) {
				sql += " AND f.datetime < ?";
			}
			if (report != null)
				sql += " AND f.report_id = ? ";

			PreparedStatement ps = connection.prepareStatement(sql + sqlEnd);
			int i = 1;
			if (start != null) {
				ps.setString(i, DateUtils.toSQLite(start.atStartOfDay()));
				i++;
			}
			if (end != null) {
				ps.setString(i, DateUtils.toSQLite(end.atTime(23, 59, 59)));
				i++;
			}
			if (report != null)
				ps.setInt(i, report.getId());

			ResultSet rs = ps.executeQuery();

			while (rs.next()) {
				if (id != rs.getInt("id")) {
					id = rs.getInt("id");
					listFiles = new HashMap<String, ReportFile>();
					/*
					 * tfile = new TransportFile(id, rs.getString("name"),
					 * DateUtils.fromSQLite(rs.getString("datetime")),
					 * getReportById(rs.getInt("rid")), rs.getInt("cdir") == 1,
					 * rs.getObject("linked_id") != null ?
					 * getReportFileById(rs.getInt("linked_id")) : null,
					 * listFiles, getFileType(rs.getInt("parent_type")));
					 */
					files.put(tfile.getName(), tfile);
				}
				listFiles.put(rs.getString("cname"), getReportFileById(rs.getInt("cid")));
			}
			rs.close();
			ps.close();
		} catch (SQLException e) {
			MainApp.error(e.getLocalizedMessage());
			e.printStackTrace();
		}

		return files;
	}

	
	
	public Report getReportById(Integer id) {
		Report tmpReport = null;
		try {
			String sql = "SELECT r.id, r.name, r.path_in, r.output_path_in, r.path_out, r.archive_path_in, r.output_path_out, r.archive_path_out FROM reports r WHERE r.id = ?";
			PreparedStatement ps = connection.prepareStatement(sql);
			ps.setInt(1, id);
			ResultSet rs = ps.executeQuery();
			if (rs.next()) {
				tmpReport = new Report(rs.getInt("id"), rs.getString("name"), null, null, null, null, rs.getString("path_out"),
						rs.getString("path_in"), rs.getString("archive_path_out"), rs.getString("archive_path_in"), rs.getString("output_path_out"),
						rs.getString("output_path_in"), getTickets(rs.getInt("id")));
				/**
				 * Get transport decrypt pattern
				 */
				tmpReport.setTransportInPattern(getFileType(tmpReport.getId(), Constants.IN_DB, Constants.TRANSPORT));
				/**
				 * Get transport encrypt pattern
				 */
				tmpReport.setTransportOutPattern(getFileType(tmpReport.getId(), Constants.OUT_DB, Constants.TRANSPORT));
				/**
				 * Get report encrypt patterns
				 */
				tmpReport.setPatternOut(getFileTypeAsList(tmpReport.getId(), Constants.OUT_DB, Constants.SIMPLE));
				/**
				 * Get report decrypt patterns
				 */
				tmpReport.setPatternIn(getFileTypeAsList(tmpReport.getId(), Constants.IN_DB, Constants.SIMPLE));

			}
			rs.close();
			ps.close();
		} catch (SQLException e) {
			MainApp.error(e.getLocalizedMessage());
			e.printStackTrace();
		}
		return tmpReport;
	}

	public ReportFile getReportFileById(Integer id) {
		ReportFile rf = null;
		try {

			String sql = "SELECT f.id, f.datetime, f.direction, f.linked_id, f.name, f.report_id, a.name as aname, fa.value, fa.attribute_id, f.type_id as file_type FROM files f LEFT JOIN file_attributes fa  ON f.id = fa.file_id LEFT JOIN attributes a ON a.id = fa.attribute_id WHERE f.id=?";
			PreparedStatement ps = connection.prepareStatement(sql);
			ps.setInt(1, id);
			ResultSet rs = ps.executeQuery();
			boolean newFile = true;
			while (rs.next()) {
				if (newFile) {
					Report rep = getReportById(rs.getInt("report_id"));
					ReportFile rftmp = null;
					if (rs.getObject("linked_id") != null) {
						rftmp = getReportFileById(rs.getInt("linked_id"));
					}
					/*
					 * rf = new ReportFile(rs.getInt("id"),
					 * rs.getString("name"),
					 * DateUtils.fromSQLite(rs.getString("datetime")), rep,
					 * rs.getInt("direction") == 1, rftmp, new HashMap<String,
					 * FileAttribute>(), getFileType(rs.getInt("file_type")));
					 */
					newFile = false;
				}
				if (rs.getObject("aname") != null) {
					rf.getAttributes().put(rs.getString("aname"),
							new FileAttribute(rs.getInt("attribute_id"), rs.getString("aname"), rs.getString("value")));
				}

			}

			rs.close();
			ps.close();
		} catch (SQLException e) {
			MainApp.error(e.getLocalizedMessage());
			e.printStackTrace();
		}
		return rf;
	}

	public ReportFile getReportFileByName(String name) {
		ReportFile rf = null;
		try {

			String sql = "SELECT f.id, f.datetime, f.direction, f.linked_id, f.name, f.report_id, a.name as aname, fa.value, fa.attribute_id, f.type_id as file_type FROM files f LEFT JOIN file_attributes fa  ON f.id = fa.file_id LEFT JOIN attributes a ON a.id = fa.attribute_id WHERE f.name LIKE (?)";
			PreparedStatement ps = connection.prepareStatement(sql);
			ps.setString(1, name + "%");
			ResultSet rs = ps.executeQuery();
			boolean newFile = true;
			while (rs.next()) {
				if (newFile) {
					Report rep = getReportById(rs.getInt("report_id"));
					ReportFile rftmp = null;
					if (rs.getObject("linked_id") != null) {
						rftmp = getReportFileById(rs.getInt("linked_id"));
					}
					/*
					 * rf = new ReportFile(rs.getInt("id"),
					 * rs.getString("name"),
					 * DateUtils.fromSQLite(rs.getString("datetime")), rep,
					 * rs.getInt("direction") == 1, rftmp, new HashMap<String,
					 * FileAttribute>(), getFileType(rs.getInt("file_type")));
					 */
					newFile = false;
				}
				if (rs.getObject("aname") != null) {
					rf.getAttributes().put(rs.getString("aname"),
							new FileAttribute(rs.getInt("attribute_id"), rs.getString("aname"), rs.getString("value")));
				}

			}

			rs.close();
			ps.close();
		} catch (SQLException e) {
			MainApp.error(e.getLocalizedMessage());
			e.printStackTrace();
		}
		return rf;
	}

	public TransportFile getTransportFileByName(String name, Report report, Boolean direction) {
		TransportFile tfile = null;
		Integer id = 0;
		Map<String, ReportFile> listFiles = null;
		try {
			String sql = "SELECT f.id, f.name, f.datetime, f2.id as cid, f2.name as cname, f2.datetime as cdatetime, f.direction, f.report_id, f.type_id as parent_type, f2.type_id as child_type FROM files f LEFT JOIN transport_files tf ON f.id = tf.parent_id LEFT JOIN files f2 ON tf.child_id = f2.id WHERE f.report_id = ? AND f.direction = ? AND tf.id IS NOT NULL ORDER BY f.id ASC , f.datetime ASC";
			PreparedStatement ps = connection.prepareStatement(sql);
			ps.setInt(1, report.getId());
			ps.setInt(2, direction ? 1 : 0);
			ResultSet rs = ps.executeQuery();
			while (rs.next()) {
				/*
				 * if (id != rs.getInt("id")) { id = rs.getInt("id"); listFiles
				 * = new HashMap<String, ReportFile>(); tfile = new
				 * TransportFile(id, rs.getString("name"),
				 * DateUtils.fromSQLite(rs.getString("datetime")), report,
				 * direction, null, listFiles,
				 * getFileType(rs.getInt("parent_type")));
				 * 
				 * } listFiles.put(rs.getString("cname"), new
				 * ReportFile(rs.getInt("cid"), rs.getString("cname"),
				 * DateUtils.fromSQLite(rs.getString("cdatetime")), report,
				 * direction, null, null,
				 * getFileType(rs.getInt("child_type"))));
				 */
			}
			rs.close();
			ps.close();
		} catch (SQLException e) {
			MainApp.error(e.getLocalizedMessage());
			e.printStackTrace();
		}

		return tfile;
	}

	/*
	 * public ObservableList<FileEntity> getFilesByTransport(Report report,
	 * FileEntity transport) { List<FileToDo> files = new ArrayList<FileToDo>();
	 * try { String sql =
	 * "SELECT f.* FROM transport_contain tc LEFT JOIN files f ON tc.file_id = f.id WHERE f.report_id = ? AND tc.transport_id = ? ORDER BY f.datetime ASC"
	 * ; PreparedStatement ps = connection.prepareStatement(sql); ps.setInt(1,
	 * report.getId()); ps.setInt(2, transport.getId()); ResultSet rs =
	 * ps.executeQuery(); while (rs.next()) { FileToDo file = new
	 * FileToDo(rs.getInt("id"), new File(rs.getString("name")),
	 * DateUtils.fromSQLite(rs.getString("datetime"))); files.add(file); }
	 * rs.close(); ps.close(); } catch (SQLException e) { e.printStackTrace(); }
	 * 
	 * return FXCollections.observableArrayList(files); }
	 */

	/*
	 * public void saveTransportFile(Report report, String name, LocalDateTime
	 * dateTime, Boolean encrypt) { try { String sql =
	 * "INSERT INTO transport(report_id, name, datetime, encrypt) VALUES ( ?, ?, ?, ?)"
	 * ; PreparedStatement ps = connection.prepareStatement(sql); ps.setInt(1,
	 * report.getId()); ps.setString(2, name); ps.setString(3,
	 * DateUtils.format(dateTime)); ps.setInt(4, encrypt ? 1 : 0);
	 * ps.executeUpdate(); ps.close(); } catch (SQLException e) {
	 * MainApp.error(e.getLocalizedMessage()); e.printStackTrace(); } }
	 */

	/*
	 * public List<FileType> getFileTypes(Report report, boolean encrypt) {
	 * List<FileType> types = new ArrayList<FileType>(); FileType fType = null;
	 * PreparedStatement ps = null; ResultSet rs = null; try { String sql =
	 * "SELECT * FROM file_types WHERE report_id = ? AND encrypt = ?"; ps =
	 * connection.prepareStatement(sql); ps.setInt(1, report.getId());
	 * ps.setInt(2, encrypt ? 1 : 0); rs = ps.executeQuery(); while (rs.next())
	 * { fType = new FileType(rs.getInt("id"), rs.getString("name"),
	 * rs.getString("mask"), report, encrypt, rs.getInt("transport") == 1);
	 * types.add(fType); } } catch (SQLException e) { e.printStackTrace(); }
	 * finally { try { rs.close(); ps.close(); } catch (SQLException e) { //
	 * 
	 * } return types; }
	 */

	public Map<String, AttributeDescr> getAttributes(FileType type) {
		Map<String, AttributeDescr> map = new HashMap<>();
		String sql = "SELECT as1.*, a.name, a.id as aid FROM attribute_settings as1 LEFT JOIN attributes a ON as1.attribute_id = a.id WHERE as1.type_id=?";
		PreparedStatement ps = null;
		try {
			ps = connection.prepareStatement(sql);
			ps.setInt(1, type.getId());
			ResultSet rs = ps.executeQuery();
			while (rs.next()) {
				map.put(rs.getString("name"), new AttributeDescr(rs.getInt("aid"), rs.getInt("in_name") == 1, rs.getString("etc"),
						getFileAttribute(rs.getInt("attribute_id")), rs.getString("place"), getFileType(rs.getInt("type_id"))));
			}

			rs.close();
			ps.close();
		} catch (SQLException e) {
			MainApp.error(e.getLocalizedMessage());
			e.printStackTrace();
			try {
				ps.close();
			} catch (SQLException e1) {
				MainApp.error(e.getLocalizedMessage());
				e1.printStackTrace();
			}
		}

		return map;
	}

	public void save(Report report, Integer direction, ObservableList<WorkingFile> files, UUID parent) {
		PreparedStatement ps = null;
		PreparedStatement ps2 = null;
		try {
			String sql = "INSERT INTO files(report_id, name, datetime, direction, linked_uuid, type_id, uuid) VALUES (?,?,?,?,?,?,?)";
			String sql2 = "INSERT INTO transport_files(parent_uuid, child_uuid) VALUES (?,?)";
			ps = connection.prepareStatement(sql);
			ps2 = connection.prepareStatement(sql2);
			for (WorkingFile file : files) {
				ps.setInt(1, report.getId());
				ps.setString(2, file.getOriginalName());
				ps.setString(3, DateUtils.toSQLite(LocalDateTime.now()));
				ps.setInt(4, direction);
				/*if (parent != null) {
					ps.setString(5, parent.toString());
				} else {
					ps.setNull(5, Types.VARCHAR);
				}*/
				if (file.getAttributes()!=null) {
					if (file.getAttributes().get(AttributeDescr.PARENT_ID)!=null) {
						ps.setString(5, file.getAttributes().get(AttributeDescr.PARENT_ID).getValue());
					}else if (file.getAttributes().get(AttributeDescr.PARENT)!=null) {
						WorkingFile parentFile = getFileByName(file.getAttributes().get(AttributeDescr.PARENT).getValue());
						if(parentFile!=null) {
							ps.setString(5, parentFile.getUUID().toString());
						}else {
							ps.setNull(5, Types.VARCHAR);
						}
					}else {
						ps.setNull(5, Types.VARCHAR);
					}
				}else {
					ps.setNull(5, Types.VARCHAR);
				}
				if (file.getType() != null && file.getType().getId() != null)
					ps.setInt(6, file.getType().getId());
				else
					ps.setNull(6, Types.INTEGER);
				ps.setString(7, file.getUUID().toString());
				ps.addBatch();

				if (parent != null) {
					ps2.setString(1, parent.toString());
					ps2.setString(2, file.getUUID().toString());
					ps2.addBatch();
				}
				
				if (file.getAttributes()!=null) {
					saveAttributes(file);
				}
			}
			ps.executeBatch();
			if (parent != null)
				ps2.executeBatch();

		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			try {
				ps.close();
				if (ps2 != null)
					ps2.close();
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}

		files.forEach(file -> {
			if (file.getChilds() != null) {
				save(report, direction, file.getChilds(), file.getUUID());
			}
		});

	}

	public WorkingFile getFileByName(String name) {
		WorkingFile result = null;
		String sql = "Select * from files WHERE name like ?";
		try(PreparedStatement ps = connection.prepareStatement(sql)){
			ps.setString(1, "%"+name+"%");
			ResultSet rs = ps.executeQuery();
			if(rs.next()) {
				result = new WorkingFile(WorkingFile.LOADED);
				result.setUUID(UUID.fromString(rs.getString("uuid")));
				result.setOriginalName(rs.getString("name"));
				result.setDatetime(DateUtils.fromSQLite(rs.getString("datetime")));
				result.setDirection(rs.getInt("direction")==1);
			}
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return result;
	}
	
	public void saveAttributes(WorkingFile wFile) {
		String sql = "INSERT INTO file_attributes(file_uuid, attribute_id, value) VALUES (?,?,?)";
		try(PreparedStatement ps = connection.prepareStatement(sql)){
			for(FileAttribute attr : wFile.getAttributes().values()) {
				ps.setString(1, wFile.getUUID().toString());
				ps.setInt(2, attr.getId());
				ps.setString(3, attr.getValue());
				ps.addBatch();
			}
			ps.executeBatch();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	
	public void saveReportFile(ReportFile file) {
		try {
			String sql = "INSERT INTO files(report_id, name, datetime, direction, linked_in, type_id) VALUES (?,?,?,?,?,?)";
			PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
			ps.setInt(1, file.getReport().getId());
			ps.setString(2, file.getName());
			ps.setString(3, DateUtils.toSQLite(file.getDatetime()));
			ps.setInt(4, file.getDirection() ? 1 : 0);
			if (file.getLinkedFile() != null) {
				// ps.setInt(5, file.getLinkedFile().getId());
			} else {
				ps.setNull(5, Types.INTEGER);
			}
			if (file.getFileType().getId() != null)// this is just check and i
													// hope it won't be null
													// ever
				ps.setInt(6, file.getFileType().getId());
			else
				ps.setNull(6, Types.INTEGER);
			ps.executeUpdate();
			ResultSet rs = ps.getGeneratedKeys();
			if (rs.next()) {
				// file.setId(rs.getInt(1));
			}
			rs.close();
			ps.close();
		} catch (SQLException e) {
			MainApp.error(e.getLocalizedMessage());
			e.printStackTrace();
		}

	}

	@SuppressWarnings("resource")
	public void save(ReportFile file) {
		String sql = "";
		PreparedStatement ps = null;
		ResultSet rs = null;
		try {
			sql = "INSERT INTO files(report_id, name, datetime, direction, linked_id, type_id) VALUES (?,?,?,?,?,?)";
			ps = connection.prepareStatement(sql);
			ps.setInt(1, file.getReport().getId());
			ps.setString(2, file.getName());
			ps.setString(3, DateUtils.toSQLite(file.getDatetime()));
			ps.setInt(4, file.getDirection() ? 1 : 0);
			if (file.getLinkedFile() != null && file.getLinkedFile().getId() != null) {
				// ps.setInt(5, file.getLinkedFile().getId());
			} else {
				ps.setNull(5, Types.INTEGER);
			}
			ps.setInt(6, file.getFileType().getId());
			ps.executeUpdate();
			rs = ps.getGeneratedKeys();
			if (rs.next()) {
				// file.setId(rs.getInt(1));
			}
			rs.close();
			ps.close();

			for (FileAttribute fattr : file.getAttributes().values()) {
				sql = "INSERT INTO file_attributes(file_id, attribute_id, value)VALUES(?,?,?)";
				ps = connection.prepareStatement(sql);
				// ps.setInt(1, file.getId());
				ps.setInt(2, fattr.getId());
				ps.setString(3, fattr.getValue());
				ps.executeUpdate();
				ps.close();
			}
			// have parent
			if (file.getAttributes().get(AttributeDescr.PARENT) != null || file.getAttributes().get(AttributeDescr.PARENT_ID) != null) {
				Integer parentId = null;
				PreparedStatement ps2 = null;
				if (file.getAttributes().get(AttributeDescr.PARENT_ID) != null) {
					ps2 = connection.prepareStatement("select file_id from file_attributes where attribute_id = 8 and value = ?");
					ps2.setString(1, file.getAttributes().get(AttributeDescr.PARENT_ID).getValue());
					ResultSet rs2 = ps2.executeQuery();
					if (rs2.next()) {
						parentId = rs2.getInt("file_id");
					}
					rs2.close();
					ps2.close();
				} else {
					ps2 = connection.prepareStatement("SELECT id FROM files WHERE name LIKE ?");
					ps2.setString(1, file.getAttributes().get(AttributeDescr.PARENT).getValue() + "%");
					ResultSet rs2 = ps2.executeQuery();
					if (rs2.next()) {
						parentId = rs2.getInt("id");
					}
					rs2.close();
					ps2.close();
				}

				if (parentId != null) {
					ps2 = connection.prepareStatement("UPDATE files SET linked_id = ? WHERE id = ?");
					// ps2.setInt(1, file.getId());
					ps2.setInt(2, parentId);
					ps2.executeUpdate();
				} else if (file.getAttributes() != null && file.getAttributes().get(AttributeDescr.PARENT) != null) {
					ps2 = connection.prepareStatement("UPDATE files SET linked_id = ? WHERE name LIKE ?");
					// ps2.setInt(1, file.getId());
					ps2.setString(2, file.getAttributes().get(AttributeDescr.PARENT).getValue() + ".arj");
					ps2.executeUpdate();
				}
				ps2.close();
			}

		} catch (Exception e) {
			MainApp.error(e.getLocalizedMessage());
			e.printStackTrace();
			try {
				ps.close();
			} catch (SQLException e1) {
				MainApp.error(e.getLocalizedMessage());
				e1.printStackTrace();
			}
		}
	}

	/*
	 * @Deprecated public void saveTransportFile(TransportFile tfile) { String
	 * sql = ""; PreparedStatement ps = null; ResultSet rs = null; try { sql =
	 * "INSERT INTO files(report_id, name, datetime, direction, linked_id, type_id) VALUES (?,?,?,?,?,?)"
	 * ; ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
	 * ps.setInt(1, tfile.getReport().getId()); ps.setString(2,
	 * tfile.getName()); ps.setString(3,
	 * DateUtils.toSQLite(tfile.getDatetime())); ps.setInt(4,
	 * tfile.getDirection() ? 1 : 0); if (tfile.getLinkedFile() != null &&
	 * tfile.getLinkedFile().getId() != 0) { ps.setInt(5,
	 * tfile.getLinkedFile().getId()); } else { ps.setNull(5, Types.INTEGER); }
	 * ps.setInt(6, tfile.getFileType().getId()); ps.executeUpdate(); rs =
	 * ps.getGeneratedKeys(); if (rs.next()) { tfile.setId(rs.getInt(1)); }
	 * rs.close(); ps.close();
	 * 
	 * for (ReportFile rf : tfile.getListFiles().values()) { save(rf); sql =
	 * "INSERT INTO transport_files(parent_id, child_id)VALUES(?,?)"; ps =
	 * connection.prepareStatement(sql); ps.setInt(1, tfile.getId());
	 * ps.setInt(2, rf.getId()); ps.executeUpdate(); ps.close(); }
	 * 
	 * } catch (SQLException e) { MainApp.error(e.getLocalizedMessage());
	 * e.printStackTrace(); } }
	 */
	public void deleteReport(Report report) {

		/**
		 * Delete chains
		 */
		ObservableList<Chain> chains = getChains(report, Constants.IN);
		chains.addAll(getChains(report, Constants.OUT));
		for (Chain chain : chains) {
			deleteChain(chain);
		}

		/**
		 * Delete File types
		 */
		for (FileType fileType : getFileTypes(report)) {
			deleteFileType(fileType);
		}

		/**
		 * Delete Files
		 */
		deleteFiles(report);

		String sql = "DELETE FROM reports WHERE id = ?";
		PreparedStatement ps = null;
		try {
			ps = connection.prepareStatement(sql);
			ps.setInt(1, report.getId());
			ps.executeUpdate();
		} catch (SQLException e) {
			MainApp.error(e.getLocalizedMessage());
			e.printStackTrace();
		} finally {
			try {
				ps.close();
			} catch (SQLException e) {
				MainApp.error(e.getLocalizedMessage());
				e.printStackTrace();
			}
		}
	}

	public void deleteFiles(Report report) {
		String sql = "DELETE from transport_files where parent_id = (select id from files where report_id = ?)";
		PreparedStatement ps = null;
		try {
			ps = connection.prepareStatement(sql);
			ps.setInt(1, report.getId());
			ps.executeUpdate();
		} catch (SQLException e) {
			MainApp.error(e.getLocalizedMessage());
			e.printStackTrace();
		} finally {
			try {
				ps.close();
			} catch (SQLException e) {
				MainApp.error(e.getLocalizedMessage());
				e.printStackTrace();
			}
		}

		sql = "DELETE FROM files WHERE report_id = ?";
		try {
			ps = connection.prepareStatement(sql);
			ps.setInt(1, report.getId());
			ps.executeUpdate();
		} catch (SQLException e) {
			MainApp.error(e.getLocalizedMessage());
			e.printStackTrace();
		} finally {
			try {
				ps.close();
			} catch (SQLException e) {
				MainApp.error(e.getLocalizedMessage());
				e.printStackTrace();
			}
		}
	}

	public void deleteFileType(FileType fileType) {
		for (TicketResults ticketResult : fileType.getResults()) {
			deleteTicketResult(ticketResult);
		}

		String sql = "DELETE FROM file_types WHERE id = ?";
		PreparedStatement ps = null;
		try {
			ps = connection.prepareStatement(sql);
			ps.setInt(1, fileType.getId());
			ps.executeUpdate();
		} catch (SQLException e) {
			MainApp.error(e.getLocalizedMessage());
			e.printStackTrace();
		} finally {
			try {
				ps.close();
			} catch (SQLException e) {
				MainApp.error(e.getLocalizedMessage());
				e.printStackTrace();
			}
		}
	}

	public void deleteTicketResult(TicketResults ticketResult) {
		String sql = "DELETE FROM ticket_results WHERE id = ?";
		PreparedStatement ps = null;
		try {
			ps = connection.prepareStatement(sql);
			ps.setInt(1, ticketResult.getId());
			ps.executeUpdate();
		} catch (SQLException e) {
			MainApp.error(e.getLocalizedMessage());
			e.printStackTrace();
		} finally {
			try {
				ps.close();
			} catch (SQLException e) {
				MainApp.error(e.getLocalizedMessage());
				e.printStackTrace();
			}
		}
	}

	public void deleteChain(Chain chain) {
		String sql = "DELETE FROM chain_arm WHERE chain_id = ?";
		PreparedStatement ps = null;
		try {
			ps = connection.prepareStatement(sql);
			ps.setInt(1, chain.getId());
			ps.executeUpdate();
		} catch (SQLException e) {
			MainApp.error(e.getLocalizedMessage());
			e.printStackTrace();
		} finally {
			try {
				ps.close();
			} catch (SQLException e) {
				MainApp.error(e.getLocalizedMessage());
				e.printStackTrace();
			}
		}

		for (ProcessStep step : chain.getSteps()) {
			deleteChainStep(step);
		}

		try {
			sql = "DELETE FROM chain WHERE id = ?";
			ps = connection.prepareStatement(sql);
			ps.setInt(1, chain.getId());
			ps.executeUpdate();
		} catch (SQLException e) {
			MainApp.error(e.getLocalizedMessage());
			e.printStackTrace();
		} finally {
			try {
				ps.close();
			} catch (SQLException e) {
				MainApp.error(e.getLocalizedMessage());
				e.printStackTrace();
			}
		}
	}

	public void deleteChainStep(ProcessStep step) {
		String sql = "DELETE FROM actions WHERE id = ?";
		PreparedStatement ps = null;
		try {
			ps = connection.prepareStatement(sql);
			ps.setInt(1, step.getId());
			ps.executeUpdate();
		} catch (SQLException e) {
			MainApp.error(e.getLocalizedMessage());
			e.printStackTrace();
		} finally {
			try {
				ps.close();
			} catch (SQLException e) {
				MainApp.error(e.getLocalizedMessage());
				e.printStackTrace();
			}
		}
	}

	public int saveReport(Report report) throws ReportError {
		if (report.getId() == 0) {
			return insertReport(report);
		} else {
			return updateReport(report);
		}
	}

	public int updateReport(Report report) {
		String sql = "UPDATE reports SET name =?, path_in=?, path_out = ?, output_path_in = ?, output_path_out=?, archive_path_in = ?, archive_path_out = ? WHERE id = ?";
		PreparedStatement ps = null;
		try {
			ps = connection.prepareStatement(sql);
			ps.setString(1, report.getName());
			ps.setString(2, report.getPathIn());
			ps.setString(3, report.getPathOut());
			ps.setString(4, report.getPathOutputIn());
			ps.setString(5, report.getPathOutputOut());
			ps.setString(6, report.getPathArchiveIn());
			ps.setString(7, report.getPathArchiveOut());
			ps.setInt(8, report.getId());
			ps.executeUpdate();

			saveFileTypes(report);
		} catch (SQLException e) {
			MainApp.error(e.getLocalizedMessage());
			e.printStackTrace();
		} finally {
			try {
				ps.close();
			} catch (SQLException e) {
				MainApp.error(e.getLocalizedMessage());
				e.printStackTrace();
			}
		}
		return report.getId();
	}

	public int insertReport(Report report) {
		String sql = "INSERT INTO reports(name, path_in, path_out, output_path_in, output_path_out, archive_path_in, archive_path_out)VALUES(?,?,?,?,?,?,?)";
		PreparedStatement ps = null;
		ResultSet rs = null;
		try {
			ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
			ps.setString(1, report.getName());
			ps.setString(2, report.getPathIn());
			ps.setString(3, report.getPathOut());
			ps.setString(4, report.getPathOutputIn());
			ps.setString(5, report.getPathOutputOut());
			ps.setString(6, report.getPathArchiveIn());
			ps.setString(7, report.getPathArchiveOut());
			ps.executeUpdate();
			rs = ps.getGeneratedKeys();
			if (rs.next()) {
				report.setId(rs.getInt(1));
			}
			saveFileTypes(report);

		} catch (SQLException e) {
			MainApp.error(e.getLocalizedMessage());
			e.printStackTrace();
		} finally {
			try {
				rs.close();
				ps.close();
			} catch (SQLException e) {
				MainApp.error(e.getLocalizedMessage());
				e.printStackTrace();
			}
		}
		return report.getId();
	}

	private void saveFileTypes(Report report) {
		/*
		 * ObservableList<FileType> toRemove =
		 * FXCollections.observableArrayList(); ObservableList<FileType>
		 * toUpdate = FXCollections.observableArrayList();
		 * ObservableList<FileType> toInsert =
		 * FXCollections.observableArrayList(); if (report.getPatternIn() !=
		 * null) { ObservableList<FileType> inBase =
		 * getFileTypeAsList(report.getId(), 1, 0); ObservableList<FileType>
		 * list = report.getPatternIn(); inBase.forEach((type) -> { if
		 * (list.contains(type)) { toUpdate.add(type); } else {
		 * toRemove.add(type); } }); list.forEach((type) -> { if
		 * (!inBase.contains(type)) { toInsert.add(type); } // Else is absent
		 * cause previous execute them }); } if (report.getPatternOut() != null)
		 * { ObservableList<FileType> inBase = getFileTypeAsList(report.getId(),
		 * 0, 0); ObservableList<FileType> list = report.getPatternOut();
		 * inBase.forEach((type) -> { if (list.contains(type)) {
		 * toUpdate.add(type); } else { toRemove.add(type); } });
		 * list.forEach((type) -> { if (!inBase.contains(type)) {
		 * toInsert.add(type); } // Else is absent cause previous execute them
		 * }); }
		 * 
		 * if (report.getTickets()!=null){ ObservableList<FileType> inBase =
		 * getTickets(report.getId()); ObservableList<FileType> list =
		 * report.getTickets(); inBase.forEach((type) -> { if
		 * (list.contains(type)) { toUpdate.add(type); } else {
		 * toRemove.add(type); } }); list.forEach((type) -> { if
		 * (!inBase.contains(type)) { toInsert.add(type); } // Else is absent
		 * cause previous execute them }); }
		 * 
		 * toRemove.forEach((type) -> remove(type)); toUpdate.forEach((type) ->
		 * updateFileType(type, report.getId())); toInsert.forEach((type) ->
		 * insertFileType(type, report.getId()));
		 */
		if (report.getTransportInPattern() != null) {
			if (report.getTransportInPattern().getId() == 0) {
				insertFileType(report.getTransportInPattern(), report.getId());
			} else {
				updateFileType(report.getTransportInPattern(), report.getId());
			}
		}

		if (report.getTransportOutPattern() != null) {
			if (report.getTransportOutPattern().getId() == 0) {
				insertFileType(report.getTransportOutPattern(), report.getId());
			} else {
				updateFileType(report.getTransportOutPattern(), report.getId());
			}
		}

	}

	public void clearFileTypes(int reportId, boolean direction, int transport) {
		String sql = "DELETE FROM file_types WHERE report_id = ? AND direction = ? AND transport=?";
		PreparedStatement ps = null;
		try {
			ps = connection.prepareStatement(sql);
			ps.setInt(1, reportId);
			ps.setInt(2, direction ? 1 : 0);
			ps.setInt(3, transport);
			ps.executeUpdate();
		} catch (SQLException e) {
			MainApp.error(e.getLocalizedMessage());
			e.printStackTrace();
		} finally {
			try {
				ps.close();
			} catch (SQLException e) {
				MainApp.error(e.getLocalizedMessage());
				e.printStackTrace();
			}
		}
	}

	public void insertFileType(FileType fType, int reportId) {
		String sql = "INSERT INTO file_types(report_id, direction, mask, name, transport, ticket, file_type, validation_schema)VALUES(?,?, ?,?,?,?,?,?)";
		PreparedStatement ps = null;
		ResultSet rs = null;
		try {
			ps = connection.prepareStatement(sql);
			ps.setInt(1, reportId);
			ps.setInt(2, fType.getDirection() ? 1 : 0);
			ps.setString(3, fType.getMask());
			ps.setString(4, fType.getName());
			ps.setInt(5, fType.getTransport() ? 1 : 0);
			ps.setInt(6, fType.getTicket() ? 1 : 0);
			ps.setInt(7, fType.getFileType());
			if (fType.getValidationSchema() == null || "".equals(fType.getValidationSchema())) {
				ps.setNull(8, java.sql.Types.VARCHAR);
			} else {
				ps.setString(8, fType.getValidationSchema());
			}
			ps.executeUpdate();
			rs = ps.getGeneratedKeys();
			if (rs.next()) {
				fType.setId(rs.getInt(1));
			}
		} catch (SQLException e) {
			MainApp.error(e.getLocalizedMessage());
			e.printStackTrace();
		} finally {
			try {
				ps.close();
			} catch (SQLException e) {
				MainApp.error(e.getLocalizedMessage());
				e.printStackTrace();
			}
		}
	}

	public void updateFileType(FileType fType, int id) {
		String sql = "UPDATE file_types SET report_id = ?, direction = ?, mask = ?, name = ?, transport = ?, ticket = ? ,file_type = ?, validation_schema = ? WHERE id = ?";
		PreparedStatement ps = null;
		try {
			ps = connection.prepareStatement(sql);
			ps.setInt(1, id);
			ps.setInt(2, fType.getDirection() ? 1 : 0);
			ps.setString(3, fType.getMask());
			ps.setString(4, fType.getName());
			ps.setInt(5, fType.getTransport() ? 1 : 0);
			ps.setInt(6, fType.getTicket() ? 1 : 0);
			ps.setInt(7, fType.getFileType());
			if (fType.getValidationSchema() == null || "".equals(fType.getValidationSchema())) {
				ps.setNull(8, java.sql.Types.VARCHAR);
			} else {
				ps.setString(8, fType.getValidationSchema());
			}
			ps.setInt(9, fType.getId());
			ps.executeUpdate();
		} catch (SQLException e) {
			MainApp.error(e.getLocalizedMessage());
			e.printStackTrace();
		} finally {
			try {
				ps.close();
			} catch (SQLException e) {
				MainApp.error(e.getLocalizedMessage());
				e.printStackTrace();
			}
		}
	}

	public void delete(FileType type) {
		String sql = "";
		PreparedStatement ps = null;
		try {
			sql = "DELETE FROM file_types WHERE id = ?";
			ps = connection.prepareStatement(sql);
			ps.setInt(1, type.getId());
			ps.executeUpdate();
		} catch (SQLException e) {
			MainApp.error(e.getLocalizedMessage());
			e.printStackTrace();
		} finally {
			try {
				ps.close();
			} catch (SQLException e) {
				MainApp.error(e.getLocalizedMessage());
				e.printStackTrace();
			}
		}
	}

	public void delete(Key key) {
		String sql = "DELETE FROM key WHERE id = ?";
		PreparedStatement ps = null;
		try {
			ps = connection.prepareStatement(sql);
			ps.setInt(1, key.getId());
			ps.executeUpdate();
		} catch (SQLException e) {
			MainApp.error(e.getLocalizedMessage());
			e.printStackTrace();
		} finally {
			try {
				ps.close();
			} catch (SQLException e) {
				MainApp.error(e.getLocalizedMessage());
				e.printStackTrace();
			}
		}
	}

	public void delete(FileAttribute fa) {
		String sql = "";
		PreparedStatement ps = null;
		try {
			sql = "DELETE FROM attributes WHERE id = ? ";
			ps = connection.prepareStatement(sql);
			ps.setInt(1, fa.getId());
			ps.executeUpdate();
		} catch (SQLException e) {
			MainApp.error(e.getLocalizedMessage());
			e.printStackTrace();
		} finally {
			try {
				ps.close();
			} catch (SQLException e) {
				MainApp.error(e.getLocalizedMessage());
				e.printStackTrace();
			}
		}
	}

	public ObservableList<String> getAttributesForChooser() {
		String sql = "SELECT * FROM attributes";
		PreparedStatement ps = null;
		ResultSet rs = null;
		ObservableList<String> list = FXCollections.observableArrayList();
		try {
			ps = connection.prepareStatement(sql);
			rs = ps.executeQuery();
			while (rs.next()) {
				list.add(rs.getString("name"));
			}
		} catch (SQLException e) {
			MainApp.error(e.getLocalizedMessage());
			e.printStackTrace();
		} finally {
			try {
				rs.close();
				ps.close();
			} catch (SQLException e) {
				MainApp.error(e.getLocalizedMessage());
				e.printStackTrace();
			}

		}
		return list;
	}

	public ObservableList<FileAttribute> getAttributes() {
		String sql = "SELECT * FROM attributes";
		PreparedStatement ps = null;
		ResultSet rs = null;
		ObservableList<FileAttribute> list = FXCollections.observableArrayList();
		try {
			ps = connection.prepareStatement(sql);
			rs = ps.executeQuery();
			while (rs.next()) {
				list.add(new FileAttribute(rs.getInt("id"), rs.getString("name"), null));
			}
		} catch (SQLException e) {
			MainApp.error(e.getLocalizedMessage());
			e.printStackTrace();
		} finally {
			try {
				rs.close();
				ps.close();
			} catch (SQLException e) {
				MainApp.error(e.getLocalizedMessage());
				e.printStackTrace();
			}

		}
		return list;
	}

	public FileAttribute getFileAttribute(Integer id) {
		String sql = "SELECT * FROM attributes WHERE id = ?";
		PreparedStatement ps = null;
		ResultSet rs = null;
		FileAttribute tmp = null;
		try {
			ps = connection.prepareStatement(sql);
			ps.setInt(1, id);
			rs = ps.executeQuery();
			if (rs.next()) {
				tmp = new FileAttribute(rs.getInt("id"), rs.getString("name"), "");
			}
		} catch (SQLException e) {
			MainApp.error(e.getLocalizedMessage());
			e.printStackTrace();
		} finally {
			try {
				rs.close();
				ps.close();
			} catch (SQLException e) {
				MainApp.error(e.getLocalizedMessage());
				e.printStackTrace();
			}
		}
		return tmp;
	}

	public FileAttribute getFileAttribute(String name) {
		String sql = "SELECT * FROM attributes WHERE name = ?";
		PreparedStatement ps = null;
		ResultSet rs = null;
		FileAttribute tmp = null;
		try {
			ps = connection.prepareStatement(sql);
			ps.setString(1, name);
			rs = ps.executeQuery();
			if (rs.next()) {
				tmp = new FileAttribute(rs.getInt("id"), rs.getString("name"), "");
			}
		} catch (SQLException e) {
			MainApp.error(e.getLocalizedMessage());
			e.printStackTrace();
		} finally {
			try {
				rs.close();
				ps.close();
			} catch (SQLException e) {
				MainApp.error(e.getLocalizedMessage());
				e.printStackTrace();
			}
		}
		return tmp;
	}

	public ObservableList<AttributeDescr> getAttributesDescriptions() {
		String sql = "SELECT * FROM attribute_settings";
		PreparedStatement ps = null;
		ResultSet rs = null;
		ObservableList<AttributeDescr> list = FXCollections.observableArrayList();
		try {
			ps = connection.prepareStatement(sql);
			rs = ps.executeQuery();
			while (rs.next()) {
				list.add(new AttributeDescr(rs.getInt("id"), rs.getInt("in_name") == 1, rs.getString("etc"),
						getFileAttribute(rs.getInt("attribute_id")), rs.getString("place"), getFileType(rs.getInt("type_id"))));
			}
		} catch (SQLException e) {
			MainApp.error(e.getLocalizedMessage());
			e.printStackTrace();
		} finally {
			try {
				rs.close();
				ps.close();
			} catch (SQLException e) {
				MainApp.error(e.getLocalizedMessage());
				e.printStackTrace();
			}
		}
		return list;
	}

	public ObservableList<AttributeDescr> getAttributesDescriptions(FileType type) {
		String sql = "SELECT * FROM attribute_settings WHERE type_id = ?";
		String sqlNull = "SELECT * FROM attribute_settings";
		PreparedStatement ps = null;
		ResultSet rs = null;
		ObservableList<AttributeDescr> list = FXCollections.observableArrayList();
		try {
			if (type != null) {
				ps = connection.prepareStatement(sql);
				ps.setInt(1, type.getId());
			} else {
				ps = connection.prepareStatement(sqlNull);
			}
			rs = ps.executeQuery();
			while (rs.next()) {
				list.add(new AttributeDescr(rs.getInt("id"), rs.getInt("in_name") == 1, rs.getString("etc"),
						getFileAttribute(rs.getInt("attribute_id")), rs.getString("place"), getFileType(rs.getInt("type_id"))));
			}
		} catch (SQLException e) {
			MainApp.error(e.getLocalizedMessage());
			e.printStackTrace();
		} finally {
			try {
				rs.close();
				ps.close();
			} catch (SQLException e) {
				MainApp.error(e.getLocalizedMessage());
				e.printStackTrace();
			}
		}
		return list;
	}

	public AttributeDescr getAttributeDescription(Integer id) {
		String sql = "SELECT * FROM attribute_settings WHERE id = ?";
		PreparedStatement ps = null;
		ResultSet rs = null;
		AttributeDescr tmp = null;
		try {
			ps = connection.prepareStatement(sql);
			ps.setInt(1, id);
			rs = ps.executeQuery();
			if (rs.next()) {
				tmp = new AttributeDescr(rs.getInt("id"), rs.getInt("in_name") == 1, rs.getString("etc"), getFileAttribute(rs.getInt("attribute_id")),
						rs.getString("place"), getFileType(rs.getInt("type_id")));
			}
		} catch (SQLException e) {
			MainApp.error(e.getLocalizedMessage());
			e.printStackTrace();
		} finally {
			try {
				rs.close();
				ps.close();
			} catch (SQLException e) {
				MainApp.error(e.getLocalizedMessage());
				e.printStackTrace();
			}
		}
		return tmp;
	}

	public FileType getFileType(Integer id) {
		String sql = "SELECT * FROM file_types WHERE id = ?";
		PreparedStatement ps = null;
		ResultSet rs = null;
		FileType tmp = null;
		try {
			ps = connection.prepareStatement(sql);
			ps.setInt(1, id);
			rs = ps.executeQuery();
			if (rs.next()) {
				tmp = new FileType(rs.getInt("id"), rs.getString("name"), rs.getString("mask"), rs.getString("validation_schema"),
						rs.getInt("direction") == 1, rs.getInt("transport") == 1, rs.getInt("ticket") == 1, rs.getInt("file_type"), getResults(id));
			}
		} catch (SQLException e) {
			MainApp.error(e.getLocalizedMessage());
			e.printStackTrace();
		} finally {
			try {
				rs.close();
				ps.close();
			} catch (SQLException e) {
				MainApp.error(e.getLocalizedMessage());
				e.printStackTrace();
			}
		}
		return tmp;
	}

	public void remove(FileType fileType) {
		String sql = "DELETE FROM file_types WHERE id = ? ";
		PreparedStatement ps = null;
		try {
			ps = connection.prepareStatement(sql);
			ps.setInt(1, fileType.getId());
			ps.executeUpdate();
		} catch (SQLException e) {
			MainApp.error(e.getLocalizedMessage());
			e.printStackTrace();
		} finally {
			try {
				ps.close();
			} catch (SQLException e) {
				MainApp.error(e.getLocalizedMessage());
				e.printStackTrace();
			}
		}
	}

	public void remove(AttributeDescr attribute) {
		String sql = "DELETE FROM attribute_settings WHERE id = ? ";
		PreparedStatement ps = null;
		try {
			ps = connection.prepareStatement(sql);
			ps.setInt(1, attribute.getId());
			ps.executeUpdate();
		} catch (SQLException e) {
			MainApp.error(e.getLocalizedMessage());
			e.printStackTrace();
		} finally {
			try {
				ps.close();
			} catch (SQLException e) {
				MainApp.error(e.getLocalizedMessage());
				e.printStackTrace();
			}
		}
	}

	public void remove(ProcessStep step) {
		String sql = "DELETE FROM action WHERE id = ? ";
		PreparedStatement ps = null;
		try {
			ps = connection.prepareStatement(sql);
			ps.setInt(1, step.getId());
			ps.executeUpdate();
			ps.close();

			sql = "DELETE FROM chain_arm WHERE action_id = ?";
			ps = connection.prepareStatement(sql);
			ps.setInt(1, step.getId());
			ps.executeUpdate();
		} catch (SQLException e) {
			MainApp.error(e.getLocalizedMessage());
			e.printStackTrace();
		} finally {
			try {
				ps.close();
			} catch (SQLException e) {
				MainApp.error(e.getLocalizedMessage());
				e.printStackTrace();
			}
		}
	}

	public void remove(Chain chain) {
		String sql = "DELETE FROM chains WHERE id = ?";
		PreparedStatement ps = null;
		try {
			chain.getSteps().forEach((step) -> remove(step));
			ps = connection.prepareStatement(sql);
			ps.setInt(1, chain.getId());
			ps.executeUpdate();
		} catch (SQLException e) {
			MainApp.error(e.getLocalizedMessage());
			e.printStackTrace();
		} finally {
			try {
				ps.close();
			} catch (SQLException e) {
				MainApp.error(e.getLocalizedMessage());
				e.printStackTrace();
			}
		}
	}

	public FileType addFileType(FileType fileType, Report report) throws ReportError {
		if (fileType != null) {
			String sql = "";
			if (fileType.getId() != 0) {
				sql = "UPDATE file_types SET mask = ?, name = ?, validation_schema = ? WHERE id= ?";
			} else {
				sql = "INSERT INTO file_types(mask, name, validation_schema, report_id, direction, transport)VALUES(?,?,?,?,?,?)";
			}
			PreparedStatement ps = null;
			ResultSet rs = null;

			try {
				ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
				ps.setString(1, fileType.getMask());
				ps.setString(2, fileType.getName());
				if (fileType.getValidationSchema() == null || "".equals(fileType.getValidationSchema())) {
					ps.setNull(3, Types.VARCHAR);
				} else {
					ps.setString(3, fileType.getValidationSchema());
				}
				if (fileType.getId() != 0) {
					ps.setInt(4, fileType.getId());
				} else {
					ps.setInt(4, fileType.getId());
					ps.setInt(5, fileType.getDirection() ? 1 : 0);
					ps.setInt(6, fileType.getTransport() ? 1 : 0);
				}
				ps.executeUpdate();
				rs = ps.getGeneratedKeys();
				if (rs.next()) {
					fileType.setId(rs.getInt(1));
				}
			} catch (SQLException e) {
				MainApp.error(e.getLocalizedMessage());
				e.printStackTrace();
				throw new ReportError("Не удалось сохранить маску для файла");
			} finally {
				try {
					rs.close();
					ps.close();
				} catch (SQLException e) {
					MainApp.error(e.getLocalizedMessage());
					e.printStackTrace();
				}
			}
		}
		return fileType;
	}

	public FileType getFileType(Integer reportId, Integer direction, Integer transport) {
		String sql = "";
		PreparedStatement ps = null;
		ResultSet rs = null;
		FileType ft = null;
		try {
			sql = "SELECT * from file_types WHERE report_id = ? AND direction = ? AND transport = ? AND ticket = 0 ORDER BY direction ASC";
			ps = connection.prepareStatement(sql);
			ps.setInt(1, reportId);
			ps.setInt(2, direction);
			ps.setInt(3, transport);
			rs = ps.executeQuery();
			if (rs.next()) {
				ft = new FileType(rs.getInt("id"), rs.getString("name"), rs.getString("mask"), rs.getString("validation_schema"),
						rs.getInt("direction") == 1, rs.getInt("transport") == 1, rs.getInt("ticket") == 1, rs.getInt("file_type"),
						getResults(rs.getInt("id")));
			}
		} catch (SQLException e) {
			MainApp.error(e.getLocalizedMessage());
			e.printStackTrace();
		} finally {
			try {
				rs.close();
				ps.close();
			} catch (SQLException e) {
				MainApp.error(e.getLocalizedMessage());
				e.printStackTrace();
			}
		}
		return ft;
	}

	public ObservableList<FileType> getFileTypeAsList(Integer reportId, Integer destination, Integer transport) {
		String sql = "";
		PreparedStatement ps = null;
		ResultSet rs = null;
		ObservableList<FileType> ft = FXCollections.observableArrayList();
		try {
			sql = "SELECT * from file_types WHERE report_id = ? AND direction = ? AND transport = ? AND ticket = 0 ORDER BY direction ASC";
			ps = connection.prepareStatement(sql);
			ps.setInt(1, reportId);
			ps.setInt(2, destination);
			ps.setInt(3, transport);
			rs = ps.executeQuery();
			while (rs.next()) {
				ft.add(new FileType(rs.getInt("id"), rs.getString("name"), rs.getString("mask"), rs.getString("validation_schema"),
						rs.getInt("direction") == 1, rs.getInt("transport") == 1, rs.getInt("ticket") == 1, rs.getInt("file_type"),
						getResults(rs.getInt("id"))));
			}
		} catch (SQLException e) {
			MainApp.error(e.getLocalizedMessage());
			e.printStackTrace();
		} finally {
			try {
				rs.close();
				ps.close();
			} catch (SQLException e) {
				MainApp.error(e.getLocalizedMessage());
				e.printStackTrace();
			}
		}
		return ft;
	}

	public ObservableList<FileType> getFileTypes() {
		return getFileTypes(null);
	}

	public ObservableList<FileType> getFileTypes(Report report) {
		String sql = "";
		PreparedStatement ps = null;
		ResultSet rs = null;
		ObservableList<FileType> ft = FXCollections.observableArrayList();
		try {
			sql = "SELECT * from file_types ";
			if (report != null) {
				sql += "WHERE report_id = ? ";
			}
			sql += " ORDER BY direction ASC";
			ps = connection.prepareStatement(sql);
			if (report != null)
				ps.setInt(1, report.getId());
			rs = ps.executeQuery();
			while (rs.next()) {
				ft.add(new FileType(rs.getInt("id"), rs.getString("name"), rs.getString("mask"), rs.getString("validation_schema"),
						rs.getInt("direction") == 1, rs.getInt("transport") == 1, rs.getInt("ticket") == 1, rs.getInt("file_type"),
						getResults(rs.getInt("id"))));
			}
		} catch (SQLException e) {
			MainApp.error(e.getLocalizedMessage());
			e.printStackTrace();
		} finally {
			try {
				rs.close();
				ps.close();
			} catch (SQLException e) {
				MainApp.error(e.getLocalizedMessage());
				e.printStackTrace();
			}
		}
		return ft;
	}

	public void saveAttribute(FileAttribute attr) {
		String sql = "";
		PreparedStatement ps = null;
		ResultSet rs = null;
		FileAttribute tmp = null;
		try {
			sql = "SELECT * FROM attributes WHERE id = ?";
			ps = connection.prepareStatement(sql);
			ps.setInt(1, attr.getId());
			rs = ps.executeQuery();
			if (rs.next()) {
				tmp = new FileAttribute(rs.getInt("id"), rs.getString("name"), null);
			}
			rs.close();
			ps.close();

			if (tmp != null) {
				sql = "UPDATE attributes SET name=? WHERE id=?";
			} else {
				sql = "INSERT INTO attributes(name) VALUES(?)";
			}
			ps = connection.prepareStatement(sql);
			ps.setString(1, attr.getName());
			if (tmp != null) {
				ps.setInt(2, tmp.getId());
			}
			ps.executeUpdate();
		} catch (SQLException e) {
			MainApp.error(e.getLocalizedMessage());
			e.printStackTrace();
		} finally {
			try {
				rs.close();
				ps.close();
			} catch (SQLException e) {
				MainApp.error(e.getLocalizedMessage());
				e.printStackTrace();
			}
		}
	}

	/**
	 * Saves process step, chain use to join
	 * 
	 * @param step
	 * @param chain
	 */
	public void save(ProcessStep step, Chain chain) {
		ProcessStep tmp = getProcessStep(step.getId());
		if (tmp != null) {
			updateStep(step, chain);
		} else {
			insertStep(step, chain);
		}
	}

	public void save(Chain chain) {
		if (chain != null) {
			if (chain.getId() != 0) {
				updateChain(chain);
			} else {
				insertChain(chain);
			}
		}
	}

	public void save(AttributeDescr attr) {
		if (attr != null) {
			if (attr.getId() != null && attr.getId() != 0) {
				updateAttr(attr);
			} else {
				insertAttr(attr);
			}
		}
	}

	public void updateAttr(AttributeDescr attr) {
		String sql = "UPDATE attribute_settings SET type_id = ?, in_name=?, place = ?, etc= ?, attribute_id = ? WHERE id = ?";
		PreparedStatement ps = null;
		try {
			ps = connection.prepareStatement(sql);
			ps.setInt(1, attr.getFile().getId());
			ps.setInt(2, attr.getInName() ? 1 : 0);
			ps.setString(3, attr.getLocation());
			ps.setString(4, attr.getEtc());
			ps.setInt(5, attr.getAttr().getId());
			ps.setInt(6, attr.getId());
			ps.executeUpdate();
		} catch (SQLException e) {
			MainApp.error(e.getLocalizedMessage());
			e.printStackTrace();
		} finally {
			try {
				ps.close();
			} catch (SQLException e) {
				MainApp.error(e.getLocalizedMessage());
				e.printStackTrace();
			}
		}
	}

	public void insertAttr(AttributeDescr attr) {
		String sql = "INSERT INTO attribute_settings(type_id, in_name, place, etc, attribute_id) VALUES (?,?,?,?,?)";
		PreparedStatement ps = null;
		try {
			ps = connection.prepareStatement(sql);
			ps.setInt(1, attr.getFile().getId());
			ps.setInt(2, attr.getInName() ? 1 : 0);
			ps.setString(3, attr.getLocation());
			ps.setString(4, attr.getEtc());
			ps.setInt(5, attr.getAttr().getId());
			ps.executeUpdate();
		} catch (SQLException e) {
			MainApp.error(e.getLocalizedMessage());
			e.printStackTrace();
		} finally {
			try {
				ps.close();
			} catch (SQLException e) {
				MainApp.error(e.getLocalizedMessage());
				e.printStackTrace();
			}
		}
	}

	public void updateChain(Chain chain) {
		String sql = "UPDATE chains SET name = ?, report_id = ?, direction = ? WHERE id = ?";
		PreparedStatement ps = null;
		try {
			ps = connection.prepareStatement(sql);
			ps.setString(1, chain.getName());
			ps.setInt(2, chain.getReport().getId());
			ps.setInt(3, chain.getDirection() ? 1 : 0);
			ps.executeUpdate();

		} catch (SQLException e) {
			MainApp.error(e.getLocalizedMessage());
			e.printStackTrace();
		} finally {
			try {
				ps.close();
			} catch (SQLException e) {
				MainApp.error(e.getLocalizedMessage());
				e.printStackTrace();
			}
		}
	}

	public void insertChain(Chain chain) {
		String sql = "INSERT INTO chains(name, report_id, direction)VALUES(?,?,?)";
		PreparedStatement ps = null;
		ResultSet rs = null;
		try {
			ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
			ps.setString(1, chain.getName());
			ps.setInt(2, chain.getReport().getId());
			ps.setInt(3, chain.getDirection() ? 1 : 0);
			ps.executeUpdate();
			rs = ps.getGeneratedKeys();
			if (rs.next()) {
				chain.setId(rs.getInt(1));
			}
		} catch (SQLException e) {
			MainApp.error(e.getLocalizedMessage());
			e.printStackTrace();
		} finally {
			try {
				rs.close();
				ps.close();
			} catch (SQLException e) {
				MainApp.error(e.getLocalizedMessage());
				e.printStackTrace();
			}
		}
	}

	public void updateStep(ProcessStep step, Chain chain) {
		String sql = "UPDATE action SET type=?, key=?, data=? WHERE id=?";
		PreparedStatement ps = null;
		try {
			ps = connection.prepareStatement(sql);
			ps.setInt(1, step.getAction().getId());
			ps.setInt(2, step.getKey().getId());
			ps.setString(3, step.getData());
			ps.setInt(4, step.getId());
			ps.executeUpdate();
			ps.close();

			sql = "UPDATE chain_arm SET position = ? WHERE action_id=? AND chain_id=?";
			ps = connection.prepareStatement(sql);
			ps.setInt(1, step.getPosition());
			ps.setInt(2, step.getId());
			ps.setInt(3, chain.getId());
			ps.executeUpdate();
		} catch (SQLException e) {
			MainApp.error(e.getLocalizedMessage());
			e.printStackTrace();
		} finally {
			try {
				ps.close();
			} catch (SQLException e) {
				MainApp.error(e.getLocalizedMessage());
				e.printStackTrace();
			}
		}
	}

	public void insertStep(ProcessStep step, Chain chain) {
		String sql = "INSERT INTO action(type, key, data) VALUES(?,?,?)";
		PreparedStatement ps = null;
		ResultSet rs = null;
		try {
			ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
			ps.setInt(1, step.getAction().getId());
			if (step.getKey() != null)
				ps.setInt(2, step.getKey().getId());
			else
				ps.setNull(2, java.sql.Types.INTEGER);
			if (step.getData() != null)
				ps.setString(3, step.getData());
			else
				ps.setNull(3, java.sql.Types.VARCHAR);
			ps.executeUpdate();
			rs = ps.getGeneratedKeys();
			if (rs.next())
				step.setId(rs.getInt(1));
			rs.close();
			ps.close();

			sql = "INSERT INTO chain_arm(action_id, chain_id, position)VALUES(?,?,?)";
			ps = connection.prepareStatement(sql);
			ps.setInt(1, step.getId());
			ps.setInt(2, chain.getId());
			ps.setInt(3, step.getPosition());
			ps.executeUpdate();
		} catch (SQLException e) {
			MainApp.error(e.getLocalizedMessage());
			e.printStackTrace();
		} finally {
			try {
				ps.close();
			} catch (SQLException e) {
				MainApp.error(e.getLocalizedMessage());
				e.printStackTrace();
			}
		}
	}

	public ObservableList<Key> getKeys() {
		String sql = "SELECT * FROM key";
		PreparedStatement ps = null;
		ResultSet rs = null;
		ObservableList<Key> list = FXCollections.observableArrayList();
		try {
			ps = connection.prepareStatement(sql);
			rs = ps.executeQuery();
			while (rs.next()) {
				list.add(new Key(rs.getInt("id"), rs.getString("name"), rs.getString("data")));
			}
		} catch (SQLException e) {
			MainApp.error(e.getLocalizedMessage());
			e.printStackTrace();
		} finally {
			try {
				rs.close();
				ps.close();
			} catch (SQLException e2) {
				MainApp.error(e2.getLocalizedMessage());
				e2.printStackTrace();
			}
		}
		return list;
	}

	public Key getKey(Integer id) {
		String sql = "SELECT * FROM key WHERE id = ?";
		PreparedStatement ps = null;
		ResultSet rs = null;
		Key key = null;
		try {
			ps = connection.prepareStatement(sql);
			ps.setInt(1, id);
			rs = ps.executeQuery();
			if (rs.next()) {
				key = new Key(rs.getInt("id"), rs.getString("name"), rs.getString("data"));
			}
		} catch (SQLException e) {
			MainApp.error(e.getLocalizedMessage());
			e.printStackTrace();
		} finally {
			try {
				rs.close();
				ps.close();
			} catch (SQLException e2) {
				MainApp.error(e2.getLocalizedMessage());
				e2.printStackTrace();
			}
		}
		return key;
	}

	public Key addKey(String name, String path) {
		String sql = "INSERT INTO key(name, data)VALUES(?,?)";
		PreparedStatement ps = null;
		ResultSet rs = null;
		Key key = null;
		try {
			ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
			ps.setString(1, name);
			ps.setString(2, path);
			ps.executeUpdate();
			rs = ps.getGeneratedKeys();
			if (rs.next()) {
				key = new Key(rs.getInt(1), name, path);
			}
		} catch (SQLException e) {
			MainApp.error(e.getLocalizedMessage());
			e.printStackTrace();
		} finally {
			try {
				rs.close();
				ps.close();
			} catch (SQLException e2) {
				MainApp.error(e2.getLocalizedMessage());
				e2.printStackTrace();
			}
		}
		return key;
	}

	public Key editKey(Key key) {
		String sql = "UPDATE key SET name = ?, data = ? WHERE id = ?";
		PreparedStatement ps = null;
		try {
			ps = connection.prepareStatement(sql);
			ps.setString(1, key.getName());
			ps.setString(2, key.getData());
			ps.setInt(3, key.getId());
			ps.executeUpdate();
		} catch (SQLException e) {
			MainApp.error(e.getLocalizedMessage());
			e.printStackTrace();
		} finally {
			try {
				ps.close();
			} catch (SQLException e2) {
				MainApp.error(e2.getLocalizedMessage());
				e2.printStackTrace();
			}
		}
		return key;
	}

	public ObservableList<Chain> getChains() {
		ObservableList<Chain> list = FXCollections.observableArrayList();
		String sql = "SELECT * FROM chains";
		PreparedStatement ps = null;
		ResultSet rs = null;
		try {
			ps = connection.prepareStatement(sql);
			rs = ps.executeQuery();
			while (rs.next()) {
				list.add(new Chain(rs.getInt("id"), rs.getString("name"), getReportById(rs.getInt("report_id")), rs.getInt("direction") == 1,
						getSteps(rs.getInt("id"))));
			}
		} catch (SQLException e) {
			MainApp.error(e.getLocalizedMessage());
			e.printStackTrace();
		} finally {
			try {
				rs.close();
				ps.close();
			} catch (SQLException e) {
				MainApp.error(e.getLocalizedMessage());
				e.printStackTrace();
			}
		}
		return list;
	}

	public ObservableList<Chain> getChains(Report report, String direction) {
		ObservableList<Chain> list = FXCollections.observableArrayList();
		String sql = "SELECT * FROM chains ";
		if ((report != null && report.getId() != 0) || (direction != null && !Constants.ALL.equals(direction))) {
			sql += "WHERE ";
		}
		if (report != null && report.getId() != 0) {
			sql += " report_id = ? AND";
		}
		if (direction != null && !Constants.ALL.equals(direction)) {
			sql += " direction = ? AND";
		}
		if ((report != null && report.getId() != 0) || (direction != null && !Constants.ALL.equals(direction))) {
			sql = sql.substring(0, sql.length() - 3);
		}
		PreparedStatement ps = null;
		ResultSet rs = null;
		try {
			ps = connection.prepareStatement(sql);
			if (report != null && report.getId() != 0) {
				ps.setInt(1, report.getId());
				if (direction != null && !Constants.ALL.equals(direction)) {
					ps.setInt(2, Constants.IN.equals(direction) ? 1 : 0);
				}
			} else if (direction != null && !Constants.ALL.equals(direction)) {
				ps.setInt(1, Constants.IN.equals(direction) ? 1 : 0);
			}
			rs = ps.executeQuery();
			while (rs.next()) {
				list.add(new Chain(rs.getInt("id"), rs.getString("name"), getReportById(rs.getInt("report_id")), rs.getInt("direction") == 1,
						getSteps(rs.getInt("id"))));
			}
		} catch (SQLException e) {
			MainApp.error(e.getLocalizedMessage());
			e.printStackTrace();
		} finally {
			try {
				rs.close();
				ps.close();
			} catch (SQLException e) {
				MainApp.error(e.getLocalizedMessage());
				e.printStackTrace();
			}
		}
		return list;
	}

	public Chain getChain(Integer chainId) {
		Chain chain = null;
		String sql = "SELECT * FROM chains WHERE id = ?";
		PreparedStatement ps = null;
		ResultSet rs = null;
		try {
			ps = connection.prepareStatement(sql);
			ps.setInt(1, chainId);
			rs = ps.executeQuery();
			if (rs.next()) {
				chain = new Chain(rs.getInt("id"), rs.getString("name"), getReportById(rs.getInt("report_id")), rs.getInt("direction") == 1,
						getSteps(rs.getInt("id")));
			}
		} catch (SQLException e) {
			MainApp.error(e.getLocalizedMessage());
			e.printStackTrace();
		} finally {
			try {
				rs.close();
				ps.close();
			} catch (SQLException e) {
				MainApp.error(e.getLocalizedMessage());
				e.printStackTrace();
			}
		}
		return chain;
	}

	public ObservableList<ProcessStep> getSteps(Integer chain_id) {
		ObservableList<ProcessStep> list = FXCollections.observableArrayList();
		ProcessStep step = null;
		ProcessStep tmp = null;
		String sql = "SELECT a.id, a.data as data, at.id as aid, at.name as aname, k.name as kname, k.data as kdata, k.id as kid, ca.position FROM chain_arm ca LEFT JOIN action a ON ca.action_id = a.id left join action_type at ON a.type = at.id LEFT JOIN key k ON a.\"key\"=k.id WHERE ca.chain_id = ? ORDER BY ca.position DESC";
		PreparedStatement ps = null;
		ResultSet rs = null;
		try {
			ps = connection.prepareStatement(sql);
			ps.setInt(1, chain_id);
			rs = ps.executeQuery();
			while (rs.next()) {
				step = new ProcessStep(rs.getInt("id"), new Action(rs.getInt("aid"), rs.getString("aname")),
						new Key(rs.getInt("kid"), rs.getString("kname"), rs.getString("kdata")), tmp, rs.getString("data"), rs.getInt("position"));
				list.add(0, step);
				tmp = step;
			}
		} catch (SQLException e) {
			MainApp.error(e.getLocalizedMessage());
			e.printStackTrace();
		} finally {
			try {
				rs.close();
				ps.close();
			} catch (SQLException e) {
				MainApp.error(e.getLocalizedMessage());
				e.printStackTrace();
			}

		}
		return list;
	}

	public ObservableList<Action> getActions() {
		String sql = "SELECT * FROM action_type";
		PreparedStatement ps = null;
		ResultSet rs = null;
		ObservableList<Action> list = FXCollections.observableArrayList();
		try {
			ps = connection.prepareStatement(sql);
			rs = ps.executeQuery();
			while (rs.next()) {
				list.add(new Action(rs.getInt("id"), rs.getString("name")));
			}

		} catch (SQLException e) {
			MainApp.error(e.getLocalizedMessage());
			e.printStackTrace();
		} finally {
			try {
				rs.close();
				ps.close();
			} catch (SQLException e) {
				MainApp.error(e.getLocalizedMessage());
				e.printStackTrace();
			}
		}
		return list;
	}

	public ProcessStep getProcessStep(Integer id) {
		String sql = "SELECT a.*, ca.position FROM action a LEFT JOIN chain_arm ca ON a.id = ca.action_id WHERE id = ?";
		PreparedStatement ps = null;
		ResultSet rs = null;
		ProcessStep step = null;
		try {
			ps = connection.prepareStatement(sql);
			ps.setInt(1, id);
			rs = ps.executeQuery();
			if (rs.next()) {
				step = new ProcessStep(rs.getInt("id"), getAction(rs.getInt("type")), getKey(rs.getInt("key")), null, rs.getString("data"),
						rs.getInt("position"));
			}
		} catch (SQLException e) {
			MainApp.error(e.getLocalizedMessage());
			e.printStackTrace();
		}
		return step;
	}

	public Action getAction(Integer id) {
		String sql = "SELECT * FROM action_type WHERE id = ?";
		PreparedStatement ps = null;
		ResultSet rs = null;
		Action action = null;
		try {
			ps = connection.prepareStatement(sql);
			ps.setInt(1, id);
			rs = ps.executeQuery();
			if (rs.next()) {
				action = new Action(rs.getInt("id"), rs.getString("name"));
			}
		} catch (SQLException e) {
			MainApp.error(e.getLocalizedMessage());
			e.printStackTrace();
		} finally {
			try {
				rs.close();
				ps.close();
			} catch (SQLException e) {
				MainApp.error(e.getLocalizedMessage());
				e.printStackTrace();
			}
		}
		return action;
	}

	public Action getAction(String name) {
		String sql = "SELECT * FROM action_type WHERE name = ?";
		PreparedStatement ps = null;
		ResultSet rs = null;
		Action action = null;
		try {
			ps = connection.prepareStatement(sql);
			ps.setString(1, name);
			rs = ps.executeQuery();
			if (rs.next()) {
				action = new Action(rs.getInt("id"), rs.getString("name"));
			}
		} catch (SQLException e) {
			MainApp.error(e.getLocalizedMessage());
			e.printStackTrace();
		} finally {
			try {
				rs.close();
				ps.close();
			} catch (SQLException e) {
				MainApp.error(e.getLocalizedMessage());
				e.printStackTrace();
			}
		}
		return action;
	}

	public Chain getChainByItemId(Integer id) {
		String sql = "SELECT * FROM chain_arm ca WHERE action_id=?";
		PreparedStatement ps = null;
		ResultSet rs = null;
		Integer chainId = 0;
		try {
			ps = connection.prepareStatement(sql);
			ps.setInt(1, id);
			rs = ps.executeQuery();
			if (rs.next()) {
				chainId = rs.getInt("chain_id");
			}
			rs.close();
			ps.close();
			return getChain(chainId);
		} catch (SQLException e) {
			MainApp.error(e.getLocalizedMessage());
			e.printStackTrace();
		}
		return null;
	}

	public List<Pair<String, String>> loadSettings() {
		List<Pair<String, String>> list = new ArrayList<Pair<String, String>>();
		String sql = "SELECT * FROM settings";
		PreparedStatement ps = null;
		ResultSet rs = null;
		try {
			ps = connection.prepareStatement(sql);
			rs = ps.executeQuery();
			while (rs.next()) {
				list.add(new Pair<String, String>(rs.getString("name"), rs.getString("value")));
			}
		} catch (SQLException e) {
			MainApp.error(e.getLocalizedMessage());
			e.printStackTrace();
		} finally {
			try {
				rs.close();
				ps.close();
			} catch (SQLException e) {
				MainApp.error(e.getLocalizedMessage());
				e.printStackTrace();
			}
		}
		return list;
	}

	PreparedStatement ps = null;

	public void saveSettings(List<Pair<String, String>> list) {
		String sql = "UPDATE settings SET value = ? WHERE name = ?";
		list.forEach(pair -> {
			try {
				ps = connection.prepareStatement(sql);
				ps.setString(1, pair.getValue());
				ps.setString(2, pair.getKey());
				ps.executeUpdate();
				ps.close();
			} catch (SQLException e) {
				MainApp.error(e.getLocalizedMessage());
				e.printStackTrace();
				try {
					ps.close();
				} catch (SQLException e1) {
					MainApp.error(e1.getLocalizedMessage());
					e1.printStackTrace();
				}
			}
		});
	}

	public ObservableList<ReportFile> getFilesByTransport(FileEntity transportFile) {
		if (transportFile == null) {
			return null;
		}
		ObservableList<ReportFile> list = FXCollections.observableArrayList();
		String sql = "SELECT tf.child_id from transport_files tf WHERE tf.parent_id = ?";
		PreparedStatement ps = null;
		ResultSet rs = null;
		try {
			ps = connection.prepareStatement(sql);
			// ps.setInt(1, transportFile.getId());
			rs = ps.executeQuery();
			while (rs.next()) {
				list.add(getReportFileById(rs.getInt("child_id")));
			}
		} catch (Exception e) {
			MainApp.error(e.getLocalizedMessage());
			e.printStackTrace();
		} finally {
			try {
				rs.close();
				ps.close();
			} catch (SQLException e) {
				MainApp.error(e.getLocalizedMessage());
				e.printStackTrace();
			}

		}
		return list;
	}

	public String getMessageByCode(String code) {
		String result = "";
		PreparedStatement ps = null;
		ResultSet rs = null;
		try {
			ps = connection.prepareStatement("SELECT result FROM result_codes WHERE code = ?");
			ps.setString(1, code);
			rs = ps.executeQuery();
			if (rs.next()) {
				result = rs.getString("result");
			}
		} catch (Exception e) {
			MainApp.error(e.getLocalizedMessage());
			e.printStackTrace();
		} finally {
			try {
				rs.close();
				ps.close();
			} catch (SQLException e) {
				MainApp.error(e.getLocalizedMessage());
				e.printStackTrace();
			}

		}
		return result;
	}
}
