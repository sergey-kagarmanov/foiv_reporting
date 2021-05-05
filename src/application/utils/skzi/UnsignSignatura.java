package application.utils.skzi;

import Pki1.LocalIface;
import Pki1.LocalIface.verify_param_t;
import application.MainApp;
import application.errors.ReportError;
import application.models.Key;

public class UnsignSignatura extends CommonSignatura{

	private static volatile verify_param_t verifyParameters;
	
	public UnsignSignatura(Key key) {
		super(key);
	}


	private void setVerifyParamaters() {
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

	@Override
	public void init() {
		setVerifyParamaters();
	}

	@Override
	public void start() throws ReportError {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void start(byte[] buffer, int length) throws ReportError {
		// TODO Auto-generated method stub
		
	}

	@Override
	public byte[] next(byte[] buffer, int length) throws ReportError {
		memory1.buf = buffer;
		memory1.len = length;
		result = iFace.VCERT_VerifyMem(verifyParameters, null, memory1, memory2, null);
		if (result!=0) {
			throw new ReportError("Unsign error");
		}
		return memory2.buf;
	}

	@Override
	public byte[] end() throws ReportError {
		// TODO Auto-generated method stub
		return null;
	}

}
