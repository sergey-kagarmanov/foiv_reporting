package application.utils.skzi;

import Pki1.LocalIface;
import Pki1.LocalIface.certificate_t;
import Pki1.LocalIface.find_param_t;
import Pki1.LocalIface.find_result_t;
import Pki1.LocalIface.mem_blk_t;
import application.MainApp;
import application.models.Key;

public abstract class CommonSignatura implements SignaturaExecutor{

	protected LocalIface iFace;
	protected int result = 0;
	protected static final int MAX_ELEMENTS_NUM = 32;
	mem_blk_t memory1;
	mem_blk_t memory2;


	public CommonSignatura(Key key) {
		iFace = new LocalIface();
		MainApp.info("Local interface of signature is created");
		initConfig(key.getData());
		memory1 = new mem_blk_t();
		memory2 = new mem_blk_t();
		init();

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

	private int initConfig(String profile) {
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

	protected certificate_t getCert(String string) {
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
}
