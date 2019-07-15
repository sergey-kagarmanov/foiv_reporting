package application.utils;

import java.io.File;
import java.io.IOException;

import Pki1.LocalIface;
import Pki1.LocalIface.certificate_t;
import Pki1.LocalIface.decrypt_param_t;
import Pki1.LocalIface.decrypt_result_t;
import Pki1.LocalIface.encrypt_param_t;
import Pki1.LocalIface.find_param_t;
import Pki1.LocalIface.find_result_t;
import Pki1.LocalIface.sign_param_t;
import Pki1.LocalIface.verify_param_t;

public class Signatura {

	private LocalIface iFace;
	private int result = 0;
	private sign_param_t signParameters;
	private verify_param_t verifyParameters;
	private static final int MAX_ELEMENTS_NUM = 32;
	private encrypt_param_t encryptParameters;
	private decrypt_param_t decryptParameters;

	public Signatura() {
		iFace = new LocalIface();
	}

	public void initConfig() {
		String profile = "FOIV";
		int flag = LocalIface.FLAG_INIT_REGISTRY;

		result = iFace.VCERT_Initialize(profile, flag);
		if (result == LocalIface.VCERT_OK) {
			System.out.println("Init success");
		}
	}

	public void setSignParameters() {
		signParameters = new sign_param_t();
		signParameters.flag = LocalIface.FLAG_PKCS7;
		signParameters.mycert = null; // for local skzi
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
		}
	}

	public void signFile(String in, String out) {
		result = iFace.VCERT_SignFile(signParameters, in, null, out);
		if (result == LocalIface.VCERT_OK) {
			System.out.println("File " + in + " signed in " + out);
		} else {
			System.out.println(result);
		}

	}

	public void verifySign(String in, String out) {
		LocalIface.verify_result_t verifyResult = new LocalIface.verify_result_t();
		result = iFace.VCERT_VerifyFile(verifyParameters, null, in, out, verifyResult);
		if (result == LocalIface.VCERT_OK) {
			System.out.println("File " + in + " unsigned in " + out);
		} else {
			System.out.print(result);
		}
	}

	public void setEncryptParameters() {
		encryptParameters = new encrypt_param_t();
		encryptParameters.flag = LocalIface.FLAG_PKCS7;
		encryptParameters.mycert = null;
		encryptParameters.receiver_num = 1;
		encryptParameters.receivers = new LocalIface.certificate_t[MAX_ELEMENTS_NUM];
		encryptParameters.receivers[0] = getMyCert();
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

	public void encrypt(String in, String out) {
		try {
			File file = new File(in + "_gz");
			if (file.exists()) {
				file.delete();
			}
			file.createNewFile();
			FileUtils.compressGZIP(new File(in), file);
			FileUtils.copy(file, new File(in));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		result = iFace.VCERT_EncryptFile(encryptParameters, in, out);
		if (result == LocalIface.VCERT_OK) {
			System.out.println("File " + in + " encrypted in " + out);
		} else {
			System.out.println(result);
		}
	}

	public void setDecryptParameters() {
		decryptParameters = new decrypt_param_t();
		decryptParameters.flag = LocalIface.FLAG_PKCS7;
	}

	public void decrypt(String in, String out) {
		decrypt_result_t decryptResult = new decrypt_result_t();
		result = iFace.VCERT_DecryptFile(decryptParameters, in, out, decryptResult);
		if (result == LocalIface.VCERT_OK) {
			System.out.println("File " + in + " decrypted in " + out);
		} else {
			System.out.println(result);
		}
	}

	public void signFilesInPath(String path) {
		File fPath = new File(path);
		File[] files = fPath.listFiles();
		File tmp = null;
		for (File f : files) {
			tmp = new File(f.getAbsolutePath() + "_sign");
			signFile(f.getAbsolutePath(), f.getAbsolutePath() + "_sign");
			if (f.delete())
				tmp.renameTo(f);
		}
	}

	public void verifyAndUnsignFilesInPath(String path) {
		File fPath = new File(path);
		File[] files = fPath.listFiles();
		File tmp = null;
		for (File f : files) {
			tmp = new File(f.getAbsolutePath() + "_unsign");
			verifySign(f.getAbsolutePath(), f.getAbsolutePath() + "_unsign");
			if (f.delete())
				tmp.renameTo(f);
		}
	}

	public void encrypt(String in, String out, String to) {
		setEncryptParameters(to);
		result = iFace.VCERT_EncryptFile(encryptParameters, in, out);
		if (result == LocalIface.VCERT_OK) {
			System.out.println("File " + in + " encrypted in " + out);
		} else {
			System.out.println(result);
		}
	}

	public void setEncryptParameters(String string) {
		encryptParameters = new encrypt_param_t();
		encryptParameters.flag = LocalIface.FLAG_PKCS7;
		encryptParameters.mycert = null;
		encryptParameters.receiver_num = 1;
		encryptParameters.receivers = new LocalIface.certificate_t[MAX_ELEMENTS_NUM];
		encryptParameters.receivers[0] = getCert(string);
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

	public void encryptFilesInPath(String path) {
		File fPath = new File(path);
		File[] files = fPath.listFiles();
		File tmp = null;
		for (File f : files) {
			tmp = new File(f.getAbsolutePath() + "_enc");
			encrypt(f.getAbsolutePath(), f.getAbsolutePath() + "_enc");
			if (f.delete())
				tmp.renameTo(f);
		}
	}

	public void encryptFilesInPath(String path, String to) {
		setEncryptParameters(to);
		File fPath = new File(path);
		File[] files = fPath.listFiles();
		File tmp = null;
		for (File f : files) {
			tmp = new File(f.getAbsolutePath() + "_enc");
			encrypt(f.getAbsolutePath(), f.getAbsolutePath() + "_enc");
			if (f.delete())
				tmp.renameTo(f);
		}
	}

	public void decryptFilesInPath(String path) {
		File fPath = new File(path);
		File[] files = fPath.listFiles();
		File tmp = null;
		for (File f : files) {
			tmp = new File(f.getAbsolutePath() + "_enc");
			decrypt(f.getAbsolutePath(), f.getAbsolutePath() + "_enc");
			if (f.delete())
				tmp.renameTo(f);
		}
	}

	public void setParameters() {
		setSignParameters();
		setVerifyParamaters();
		setEncryptParameters();
		setDecryptParameters();
	}

}
