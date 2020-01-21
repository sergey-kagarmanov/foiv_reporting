package application.utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Collection;
import java.util.List;
import java.util.zip.GZIPInputStream;

import Pki1.LocalIface;
import Pki1.LocalIface.certificate_t;
import Pki1.LocalIface.decrypt_param_t;
import Pki1.LocalIface.decrypt_result_t;
import Pki1.LocalIface.encrypt_param_t;
import Pki1.LocalIface.find_param_t;
import Pki1.LocalIface.find_result_t;
import Pki1.LocalIface.sign_param_t;
import Pki1.LocalIface.verify_param_t;
import application.MainApp;
import application.models.ErrorFile;
import application.models.FileTransforming;
import application.models.ProcessStep;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

public class Signatura {

	private LocalIface iFace;
	private int result = 0;
	private sign_param_t signParameters;
	private verify_param_t verifyParameters;
	private static final int MAX_ELEMENTS_NUM = 32;
	private encrypt_param_t encryptParameters;
	private decrypt_param_t decryptParameters;
	private List<ErrorFile> errorFiles = null;

	public Signatura() {
		iFace = new LocalIface();
		MainApp.info("Local interface of signature is created");
	}

	public int initConfig() {
		String profile = "FOIV";
		int flag = LocalIface.FLAG_INIT_REGISTRY;

		result = iFace.VCERT_Initialize(profile, flag);
		if (result == LocalIface.VCERT_OK) {
			System.out.println("Init success");
			MainApp.info("Local interface of signature is initialized");
		}
		return result;
	}

	public int initConfig(String profile) {
		int flag = LocalIface.FLAG_INIT_REGISTRY;

		result = iFace.VCERT_Initialize(profile, flag);
		if (result == LocalIface.VCERT_OK) {
			System.out.println("Init success");
			MainApp.info("Local interface of signature is initialized with profile - " + profile);
		} else {
			MainApp.error("Local interface of signature isn't initialized with profile - " + profile
					+ ". Error code - " + Integer.toHexString(result));
		}
		return result;

	}

	public void setSignParameters() {
		signParameters = new sign_param_t();
		signParameters.flag = LocalIface.FLAG_PKCS7;
		signParameters.mycert = null; // for local skzi
		MainApp.info("Sign parameters are set");
	}

	public void setVerifyParamaters() {
		verifyParameters = new verify_param_t();
		verifyParameters.flag = LocalIface.FLAG_PKCS7 | LocalIface.FLAG_VERIFY_DELSIGN;
		verifyParameters.mycert = null; // for local skzi
		// verifyParameters.keyUsage = LocalIface.FLAG_VERIFY_EXTKEYUSAGE; //
		// use only this flag for check cert scope
		verifyParameters.policy_num = 0;
		LocalIface.policy_t[] polices = new LocalIface.policy_t[MAX_ELEMENTS_NUM];
		polices[0] = getSignPolicy();
		verifyParameters.policies = polices;
		verifyParameters.policies = new LocalIface.policy_t[MAX_ELEMENTS_NUM];
		verifyParameters.extKeyUsage = new LocalIface.extkeyusage_t[MAX_ELEMENTS_NUM];
		verifyParameters.info = LocalIface.FIELD_SUBJECT | LocalIface.FIELD_KEYID;
		verifyParameters.nSignToDelete = LocalIface.DELETE_ALL_SIGNS;
		verifyParameters.extkeyusage_num = 2;
		verifyParameters.extKeyUsage = new LocalIface.extkeyusage_t[] {
				getExtKeyUsage("1.3.6.1.4.1.10244.7.1.1.25"),
				getExtKeyUsage("1.3.6.1.4.1.10244.7.1.1.25.5") };

		MainApp.info("Verify parameters are set");
	}

	private LocalIface.policy_t getSignPolicy() {
		LocalIface.policy_t policy = new LocalIface.policy_t();
		policy.not_num = 1;
		policy.oid = "1.3.6.1.4.1.10244.7.1.1.25";
		policy.org_name = "Сдм-Банк";
		policy.text = "Some text for policy";
		return policy;
	}

	private LocalIface.extkeyusage_t getExtKeyUsage(String oid) {
		LocalIface.extkeyusage_t ext = new LocalIface.extkeyusage_t();
		ext.oid = oid;
		return ext;
	}

	public void unload() {
		result = iFace.VCERT_Uninitialize();
		if (result == LocalIface.VCERT_OK) {
			System.out.println("Uninit seccess");
			MainApp.info("Local interface Signatura unloads successfull");
		} else {
			MainApp.error("Local interface Signatura unloads with error - "
					+ Integer.toHexString(result));
		}
	}

	public int signFile(String in, String out) {
		result = iFace.VCERT_SignFile(signParameters, in, null, out);
		if (result == LocalIface.VCERT_OK) {
			System.out.println("File " + in + " is signed in " + out);
			MainApp.info("File " + in + " is signed in " + out);
		} else {
			System.out.println(result);
			MainApp.error(
					"File " + in + " isn't signed. Error code - " + Integer.toHexString(result));
		}
		return result;
	}

	public int verifySign(String in, String out) {
		LocalIface.verify_result_t verifyResult = new LocalIface.verify_result_t();
		result = iFace.VCERT_VerifyFile(verifyParameters, null, in, out, verifyResult);
		if (result == LocalIface.VCERT_OK) {
			System.out.println("File " + in + " is unsigned in " + out);
			MainApp.info("File " + in + " is unsigned in " + out);
		} else {
			MainApp.error(
					"File " + in + " isn't unsigned. Result code - " + Integer.toHexString(result));
			System.out.print(result);
		}
		return result;
	}

	public void setEncryptParameters() {
		encryptParameters = new encrypt_param_t();
		encryptParameters.flag = LocalIface.FLAG_PKCS7;
		encryptParameters.mycert = null;
		encryptParameters.receiver_num = 1;
		encryptParameters.receivers = new LocalIface.certificate_t[MAX_ELEMENTS_NUM];
		encryptParameters.receivers[0] = getMyCert();
		MainApp.info("Encrypt parameters are set");
	}

	public certificate_t getMyCert() {
		certificate_t cert = new LocalIface.certificate_t();
		find_param_t findParameters = new find_param_t();
		findParameters.flag = LocalIface.FLAG_FIND_MY;
		find_result_t findResult = new find_result_t();
		result = iFace.VCERT_FindCert(findParameters, findResult);
		if (findResult.num > 0)
			cert = findResult.certs[0];
		return cert;
	}

	public int encrypt(String in, String out) {
		try {
			File file = new File(in + "_gz");
			if (file.exists()) {
				file.delete();
			}
			file.createNewFile();
			FileUtils.compressGZIP(new File(in), file);
			FileUtils.copy(file, new File(in));
			file.delete();
		} catch (IOException e) {
			e.printStackTrace();
			result = -1;
		}
		if (result != -1) {
			result = iFace.VCERT_EncryptFile(encryptParameters, in, out);
			if (result == LocalIface.VCERT_OK) {
				System.out.println("File " + in + " encrypted in " + out);
				MainApp.info("File " + in + " is encrypted in " + out);
			} else {
				System.out.println(result);
				MainApp.error("File " + in + " isn't encrypted. Error code - "
						+ Integer.toHexString(result));
			}
		}
		return result;
	}

	public void setDecryptParameters() {
		decryptParameters = new decrypt_param_t();
		decryptParameters.flag = LocalIface.FLAG_PKCS7;
		MainApp.info("Decrypt parameters are set");
	}

	public int decrypt(String in, String out) {
		decrypt_result_t decryptResult = new decrypt_result_t();
		result = iFace.VCERT_DecryptFile(decryptParameters, in, out, decryptResult);
		if (result == LocalIface.VCERT_OK) {
			System.out.println("File " + in + " decrypted in " + out);
			MainApp.info("File " + in + " is decrypted in " + out);
		} else {
			System.out.println(result);
			MainApp.error(
					"File " + in + " isn't decrypted. Error code - " + Integer.toHexString(result));
		}
		return result;
	}

	public ObservableList<ErrorFile> signFilesInPath(Collection<FileTransforming> files) {
		ObservableList<ErrorFile> errorFiles = FXCollections.observableArrayList();
		File tmp = null;
		for (FileTransforming f : files) {
			if (f.getErrorCode() == 0) {
				tmp = new File(f.getCurrentFile().getAbsolutePath() + "_sign");
				result = signFile(f.getCurrentFile().getAbsolutePath(),
						f.getCurrentFile().getAbsolutePath() + "_sign");
				if (result != 0) {
					errorFiles.add(new ErrorFile(f.getCurrent(), result));
					f.setErrorCode(result);
				} else {
					if (f.getCurrentFile().delete()) {
						FileUtils.copy(tmp, f.getCurrentFile());
						f.setSigned(f.getCurrentFile());
						tmp.delete();
					}
				}
			}
		}
		return errorFiles;
	}

	public ObservableList<ErrorFile> verifyAndUnsignFilesInPath(
			Collection<FileTransforming> files) {
		ObservableList<ErrorFile> errorFiles = FXCollections.observableArrayList();
		File tmp = null;
		for (FileTransforming f : files) {
			if (f.getErrorCode() == 0) {
				tmp = new File(f.getCurrentFile().getAbsolutePath() + "_unsign");
				result = verifySign(f.getCurrentFile().getAbsolutePath(),
						f.getCurrentFile().getAbsolutePath() + "_unsign");
				if (result == 0) {
					if (f.getCurrentFile().delete()) {
						FileUtils.copy(tmp, f.getCurrentFile());
						tmp.delete();
					} else {
						System.out.println("!!!!!!!");
					}
				} else {
					errorFiles.add(new ErrorFile(f.getCurrent(), result));
					f.setErrorCode(result);
				}
			}
		}
		return errorFiles;
	}

	public int encrypt(String in, String out, String to) {
		setEncryptParameters(to);
		result = iFace.VCERT_EncryptFile(encryptParameters, in, out);
		if (result == LocalIface.VCERT_OK) {
			System.out.println("File " + in + " encrypted in " + out);
			MainApp.info("File " + in + " is encrypted in " + out);
		} else {
			System.out.println(result);
			MainApp.error(
					"File " + in + " isn't encrypted. Error code - " + Integer.toHexString(result));
		}
		return result;
	}

	public void setEncryptParameters(String string) {
		encryptParameters = new encrypt_param_t();
		encryptParameters.flag = LocalIface.FLAG_PKCS7;
		encryptParameters.mycert = null;
		encryptParameters.receiver_num = 1;
		encryptParameters.receivers = new LocalIface.certificate_t[MAX_ELEMENTS_NUM];
		encryptParameters.receivers[0] = getCert(string);
		MainApp.info("Encrypt parameters are set");
	}

	public certificate_t getCert(String string) {
		certificate_t cert = new LocalIface.certificate_t();
		find_param_t findParameters = new find_param_t();
		findParameters.flag = LocalIface.FLAG_FIND_PARTIAL_SUBJECT | LocalIface.FLAG_FIND_SELECTUI;
		findParameters.certTemplate = new certificate_t();
		// findParameters.certTemplate.subject = "CN="+string;
		findParameters.certTemplate.keyId = string;
		find_result_t findResult_t = new find_result_t();
		result = iFace.VCERT_FindCert(findParameters, findResult_t);
		if (findResult_t.num > 0) {
			cert = findResult_t.certs[0];
		}
		return cert;
	}

	public ObservableList<ErrorFile> encryptFilesInPath(String path) {
		ObservableList<ErrorFile> errorFiles = FXCollections.observableArrayList();
		File fPath = new File(path);
		File[] files = fPath.listFiles();
		File tmp = null;
		for (File f : files) {
			tmp = new File(f.getAbsolutePath() + "_enc");
			result = encrypt(f.getAbsolutePath(), f.getAbsolutePath() + "_enc");
			if (result == 0) {
				if (f.delete())
					tmp.renameTo(f);
			} else {
				errorFiles.add(new ErrorFile(f.getName(), result));
			}

		}
		return errorFiles;
	}

	public ObservableList<ErrorFile> encryptFilesInPath(String path, String to, ProcessStep step) {
		ObservableList<ErrorFile> errorFiles = FXCollections.observableArrayList();
		setEncryptParameters(to);
		File fPath = new File(path);
		File[] files = fPath.listFiles(new FileFilter(step.getData()));
		File tmp = null;
		for (File f : files) {
			tmp = new File(f.getAbsolutePath() + "_enc");
			result = encrypt(f.getAbsolutePath(), f.getAbsolutePath() + "_enc");
			if (result != 0) {
				errorFiles.add(new ErrorFile(f.getName(), result));
			} else {
				if (f.delete())
					tmp.renameTo(f);
			}
		}
		return errorFiles;
	}

	public ObservableList<ErrorFile> encryptFilesInPath(Collection<FileTransforming> files,
			String to) {
		ObservableList<ErrorFile> errorFiles = FXCollections.observableArrayList();
		setEncryptParameters(to);
		File tmp = null;
		for (FileTransforming f : files) {
			if (f.getErrorCode() == 0) {
				tmp = new File(f.getCurrentFile().getAbsolutePath() + "_enc");
				result = encrypt(f.getCurrentFile().getAbsolutePath(),
						f.getCurrentFile().getAbsolutePath() + "_enc");
				if (result != 0) {
					f.setErrorCode(result);
					errorFiles.add(new ErrorFile(f.getCurrent(), result));
				} else {
					if (f.getCurrentFile().delete())
						tmp.renameTo(f.getCurrentFile());
				}
			}
		}
		return errorFiles;
	}

	public ObservableList<ErrorFile> decryptFilesInPath(Collection<FileTransforming> files) {
		ObservableList<ErrorFile> errorFiles = FXCollections.observableArrayList();
		File tmp = null;
		for (FileTransforming f : files) {
			if (f.getErrorCode() == 0) {
				tmp = new File(f.getCurrentFile().getAbsolutePath() + "_enc");
				result = decrypt(f.getCurrentFile().getAbsolutePath(),
						f.getCurrentFile().getAbsolutePath() + "_enc");
				if (result == 0) {
					try {
						result = decompressGzip(tmp, f.getCurrentFile());
						tmp.delete();
					} catch (IOException e) {
						e.printStackTrace();
						result = -1;
					}
				}
				if (result != 0) {
					errorFiles.add(new ErrorFile(f.getCurrentFile().getAbsolutePath(), result));
					f.setErrorCode(result);
				}
			}
		}
		return errorFiles;
	}

	public List<ErrorFile> getErrorFiles() {
		return errorFiles;
	}

	public static int decompressGzip(File input, File output) throws IOException {
		try (GZIPInputStream in = new GZIPInputStream(new FileInputStream(input))) {
			try (FileOutputStream out = new FileOutputStream(output)) {
				byte[] buffer = new byte[1024];
				int len;
				while ((len = in.read(buffer)) != -1) {
					out.write(buffer, 0, len);
				}
			}
			MainApp.info("File " + input + " is decompressed to " + output);
		} catch (Exception e) {
			MainApp.error("File " + input + " isn't decompressed to" + output + " cause "
					+ e.getLocalizedMessage());
			e.printStackTrace();
			return -1;
		}
		return 0;
	}

	public void setParameters() {
		setSignParameters();
		setVerifyParamaters();
		setEncryptParameters();
		setDecryptParameters();
	}

}
