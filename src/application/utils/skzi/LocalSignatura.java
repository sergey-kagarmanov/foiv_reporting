package application.utils.skzi;

import Pki1.LocalIface;
import Pki1.LocalIface.certificate_t;
import Pki1.LocalIface.encrypt_param_t;
import Pki1.LocalIface.find_param_t;
import Pki1.LocalIface.find_result_t;
import application.MainApp;

public class LocalSignatura {

	private static LocalIface iFace;
	protected static final int MAX_ELEMENTS_NUM = 32;
	private static encrypt_param_t encryptParameters;
	
	public static void initSignatura(String profile) {
		iFace = new LocalIface();
		int flag = LocalIface.FLAG_INIT_REGISTRY;
		int result = iFace.VCERT_Initialize(profile, flag);
		if (result == LocalIface.VCERT_OK) {
			System.out.println("Init success");
			MainApp.info("Local interface of signature is initialized with profile - " + profile);
		} else {
			MainApp.error("Local interface of signature isn't initialized with profile - " + profile
					+ ". Error code - " + Integer.toHexString(result));
		}

	}
	
	public static encrypt_param_t getEncryptedParameters() {
		if (encryptParameters == null) {
			setEncryptParameters();
		}
		return encryptParameters;
	}
	
	public static void setEncryptParameters() {
			encryptParameters = new encrypt_param_t();
			encryptParameters.flag = LocalIface.FLAG_PKCS7;
			encryptParameters.mycert = null;
			encryptParameters.receiver_num = 1;
			encryptParameters.receivers = new LocalIface.certificate_t[MAX_ELEMENTS_NUM];
			encryptParameters.receivers[0] = getCert("");
			MainApp.info("Encrypt parameters are set");
	}

	public static certificate_t getMyCert() {
		certificate_t cert = new LocalIface.certificate_t();
		find_param_t findParameters = new find_param_t();
		findParameters.flag = LocalIface.FLAG_FIND_MY;
		find_result_t findResult = new find_result_t();
		int result = iFace.VCERT_FindCert(findParameters, findResult);
		if (findResult.num > 0)
			cert = findResult.certs[0];
		return cert;
	}

	protected static certificate_t getCert(String string) {
		certificate_t cert = new LocalIface.certificate_t();
		find_param_t findParameters = new find_param_t();
		findParameters.flag = LocalIface.FLAG_FIND_PARTIAL_SUBJECT | LocalIface.FLAG_FIND_SELECTUI;
		findParameters.certTemplate = new certificate_t();
		findParameters.certTemplate.keyId = string;
		find_result_t findResult_t = new find_result_t();
		int result = iFace.VCERT_FindCert(findParameters, findResult_t);
		if (findResult_t.num > 0) {
			cert = findResult_t.certs[0];
		}
		return cert;
	}
	
	public static synchronized LocalIface getLocaIFace() {
		return iFace;
	}
	
	public static void uninitilize() {
		iFace.VCERT_Uninitialize();
	}
}
