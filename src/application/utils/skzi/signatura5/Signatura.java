package application.utils.skzi.signatura5;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;
import java.util.zip.ZipOutputStream;

import Pki1.LocalIface;
import Pki1.LocalIface.certificate_t;
import Pki1.LocalIface.decrypt_param_t;
import Pki1.LocalIface.decrypt_result_t;
import Pki1.LocalIface.encrypt_param_t;
import Pki1.LocalIface.find_param_t;
import Pki1.LocalIface.find_result_t;
import Pki1.LocalIface.mem_blk_t;
import Pki1.LocalIface.sign_param_t;
import Pki1.LocalIface.strdecrypt_handle_t;
import Pki1.LocalIface.strencrypt_handle_t;
import Pki1.LocalIface.verify_param_t;
import application.MainApp;
import application.models.ErrorFile;

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
		// encryptParameters.receiver_num = 1;
		encryptParameters.receivers = new LocalIface.certificate_t[MAX_ELEMENTS_NUM];
		// encryptParameters.receivers[0] = getMyCert();*/
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

	public int encrypt(InputStream fis, OutputStream fos) throws IOException {
		mem_blk_t memory1 = new mem_blk_t();
		mem_blk_t memory2 = new mem_blk_t();
		strencrypt_handle_t handler = new strencrypt_handle_t();

		result = iFace.VCERT_StrEncryptInitMem(encryptParameters, handler);
		byte[] buffer = new byte[1024];
		int len;
		while ((len = fis.read(buffer)) != -1) {
			memory1.buf = buffer;
			memory1.len = len;
			result = iFace.VCERT_StrEncryptUpdateMem(encryptParameters, handler, memory1, memory2);
		}
		result = iFace.VCERT_StrEncryptFinalMem(encryptParameters, handler, memory2);
		fos.write(memory2.buf, 0, memory2.len);
		return 0;
	}
	
	public int encrypt(String in, String out) {
		/*
		 * try { File file = new File(in + "_gz"); if (file.exists()) {
		 * file.delete(); } file.createNewFile(); FileUtils.compressGZIP(new
		 * File(in), file); FileUtils.copy(file, new File(in)); file.delete(); }
		 * catch (IOException e) { MainApp.error(e.getLocalizedMessage());
		 * e.printStackTrace(); result = -1; }
		 */
		// result = iFace.VCERT_EncryptFile(encryptParameters, in, out);
		if (result != -1) {

			mem_blk_t memory1 = new mem_blk_t();
			mem_blk_t memory2 = new mem_blk_t();
			strencrypt_handle_t handler = new strencrypt_handle_t();
			try (FileInputStream fis = new FileInputStream(in)) {
				try (FileOutputStream fos = new FileOutputStream(out)) {
					ZipOutputStream zipOut = new ZipOutputStream(fos);
					result = iFace.VCERT_StrEncryptInitMem(encryptParameters, handler);
					byte[] buffer = new byte[1024];
					int len;
					while ((len = fis.read(buffer)) != -1) {
						memory1.buf = buffer;
						memory1.len = len;
						result = iFace.VCERT_StrEncryptUpdateMem(encryptParameters, handler,
								memory1, memory2);
						if (memory2.len > 0) {
							zipOut.write(memory2.buf, 0, memory2.len);
							memory2.len = 0;
						}
					}
					result = iFace.VCERT_StrEncryptFinalMem(encryptParameters, handler, memory2);
					zipOut.write(memory2.buf, 0, memory2.len);
				}
				System.out.println("File " + in + " encrypted in " + out);
			} catch (Exception e) {
				MainApp.error("File " + in + " isn't encrypt to" + out + " cause "
						+ e.getLocalizedMessage());
				e.printStackTrace();
				return -1;
			} finally {
				
			}
			return 0;

			/*
			 * if (result == LocalIface.VCERT_OK) { System.out.println("File " +
			 * in + " encrypted in " + out); MainApp.info("File " + in +
			 * " is encrypted in " + out); } else { System.out.println(result);
			 * MainApp.error("File " + in + " isn't encrypted. Error code - " +
			 * Integer.toHexString(result)); }
			 */
		}
		return result;
	}

	public void setDecryptParameters() {
		decryptParameters = new decrypt_param_t();
		decryptParameters.flag = LocalIface.FLAG_PKCS7;
		MainApp.info("Decrypt parameters are set");
	}

	public int decrypt(InputStream is, OutputStream os) throws IOException {
		mem_blk_t memory1 = new mem_blk_t();
		mem_blk_t memory2 = new mem_blk_t();
		strdecrypt_handle_t handler = new strdecrypt_handle_t();
		int[] left = new int[] { 1024 };
		decrypt_result_t decryptResult = null;
		byte[] buffer = new byte[1024];
		int len;
		if ((len = is.read(buffer)) != -1) {
			memory1.buf = buffer;
			memory1.len = len;
			
		}
		result = iFace.VCERT_StrDecryptInitMem(decryptParameters, memory1, left, handler);
		while ((len = is.read(buffer)) != -1) {
			memory1.buf = buffer;
			memory1.len = len;
			result = iFace.VCERT_StrDecryptUpdateMem(decryptParameters, handler, memory1, left,
					memory2);
			if (memory2.len>0) {
				os.write(memory2.buf, 0, memory2.len);
				memory2.len = 0;
			}
		}
		result = iFace.VCERT_StrDecryptFinalMem(decryptParameters, handler, decryptResult);
		os.write(memory2.buf, 0, memory2.len);
		is.close();
		os.close();
		return 0;

	}

	public int decrypt(String in, String out) {
		/*
		 * try { File file = new File(in + "_gz"); if (file.exists()) {
		 * file.delete(); } file.createNewFile(); } catch (IOException e) {
		 * MainApp.error(e.getLocalizedMessage()); e.printStackTrace(); result =
		 * -1; }
		 */
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

	/*
	 * public ObservableList<ErrorFile>
	 * signFilesInPath(Collection<FileTransforming> files) {
	 * ObservableList<ErrorFile> errorFiles =
	 * FXCollections.observableArrayList(); File tmp = null; for
	 * (FileTransforming f : files) { if (f.getErrorCode() == 0) { tmp = new
	 * File(f.getCurrentFile().getAbsolutePath() + "_sign"); result =
	 * signFile(f.getCurrentFile().getAbsolutePath(),
	 * f.getCurrentFile().getAbsolutePath() + "_sign"); if (result != 0) {
	 * errorFiles.add(new ErrorFile(f.getCurrent(), result));
	 * f.setErrorCode(result); } else { if (f.getCurrentFile().delete()) {
	 * FileUtils.copy(tmp, f.getCurrentFile()); f.setSigned(f.getCurrentFile());
	 * tmp.delete(); } } } } return errorFiles; }
	 */

	/*
	 * public ObservableList<ErrorFile> verifyAndUnsignFilesInPath(
	 * Collection<FileTransforming> files) { ObservableList<ErrorFile>
	 * errorFiles = FXCollections.observableArrayList(); File tmp = null; for
	 * (FileTransforming f : files) { if (f.getErrorCode() == 0) {
	 * f.setSigned(f.getCurrentFile()); tmp = new
	 * File(f.getCurrentFile().getAbsolutePath() + "_unsign"); result =
	 * verifySign(f.getCurrentFile().getAbsolutePath(),
	 * f.getCurrentFile().getAbsolutePath() + "_unsign"); if (result == 0) { if
	 * (f.getCurrentFile().delete()) { FileUtils.copy(tmp, f.getCurrentFile());
	 * tmp.delete(); } else { System.out.println("!!!!!!!"); } } else {
	 * errorFiles.add(new ErrorFile(f.getCurrent(), result));
	 * f.setErrorCode(result); } } } return errorFiles; }
	 */

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
		findParameters.certTemplate.keyId = string;
		find_result_t findResult_t = new find_result_t();
		result = iFace.VCERT_FindCert(findParameters, findResult_t);
		if (findResult_t.num > 0) {
			cert = findResult_t.certs[0];
		}
		return cert;
	}

	/*
	 * public ObservableList<ErrorFile>
	 * encryptFilesInPath(Collection<FileTransforming> files, String to) throws
	 * InterruptedException, ExecutionException { ObservableList<ErrorFile>
	 * errorFiles = FXCollections.observableArrayList();
	 * setEncryptParameters(to); ExecutorService service =
	 * Executors.newWorkStealingPool();
	 * 
	 * OperationHandler handler = null; List<OperationHandler> handlers = new
	 * ArrayList<>(); for (FileTransforming f : files) { handler = new
	 * OperationHandler(f) {
	 * 
	 * @Override Integer operation(String source, String target) { return
	 * encrypt(source, target); } }; handlers.add(handler); }
	 * List<Future<ErrorFile>> futures = service.invokeAll(handlers);
	 * futures.forEach(future -> { try { if (future.get() != null)
	 * errorFiles.add(future.get()); } catch (InterruptedException |
	 * ExecutionException e) { // TODO Auto-generated catch block
	 * e.printStackTrace(); }
	 * 
	 * }); service.shutdown(); return errorFiles; }
	 */

	/*
	 * public ObservableList<ErrorFile>
	 * decryptFilesInPath(Collection<FileTransforming> files) {
	 * ObservableList<ErrorFile> errorFiles =
	 * FXCollections.observableArrayList(); File tmp = null; for
	 * (FileTransforming f : files) { if (f.getErrorCode() == 0) { tmp = new
	 * File(f.getCurrentFile().getAbsolutePath() + "_enc"); result =
	 * decrypt(f.getCurrentFile().getAbsolutePath(),
	 * f.getCurrentFile().getAbsolutePath() + "_enc"); if (result == 0) { try {
	 * result = decompressGzip(tmp, f.getCurrentFile()); tmp.delete(); } catch
	 * (IOException e) { MainApp.error(e.getLocalizedMessage());
	 * e.printStackTrace(); result = -1; } } if (result != 0) {
	 * errorFiles.add(new ErrorFile(f.getCurrentFile().getAbsolutePath(),
	 * result)); f.setErrorCode(result); } } } return errorFiles; }
	 */

	public List<ErrorFile> getErrorFiles() {
		return errorFiles;
	}

	public void setParameters() {
		setSignParameters();
		setVerifyParamaters();
		setEncryptParameters("");
		setDecryptParameters();
	}

}
