package application.utils.skzi.signatura6;

import Pki1.LocalIface;
import Pki1.LocalIface.mem_blk_t;
import Pki1.LocalIface.strcms_handle_t;
import Pki1.LocalIface.verify_param_t;
import Pki1.LocalIface.verify_result_t;
import application.MainApp;
import application.errors.ReportError;
import application.models.Key;

public class UnsignSignatura extends CommonSignatura{

	private static volatile verify_param_t verifyParameters;
	private verify_result_t verifyResult;
	
	public UnsignSignatura(Key key) throws ReportError {
		super(key);
		handle = new strcms_handle_t();
		verifyResult = new verify_result_t();
	}


	private void setVerifyParamaters() {
		verifyParameters = new verify_param_t();
		verifyParameters.flag = LocalIface.FLAG_CMS_VERIFY_DELETESIGNATURES ^ LocalIface.FLAG_CMS_VERIFY_DONOTCHECKTIMES ^ LocalIface.FLAG_CMS_VERIFY_IGNOREATTACHEDSIGNER;
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
		memory1 = new mem_blk_t();
		memory1.buf = new byte[length];
		for(int i = 0; i<length; i++) {
			memory1.buf[i] = buffer[i];
		}
		memory1.len = length;
		result = iFace.VCERT_CmsStrAttVerifyInitMem(verifyParameters, memory1, handle);
		
	}

	@Override
	public byte[] next(byte[] buffer, int length) throws ReportError {
		memory1 = new mem_blk_t();
		memory1.buf = new byte[length];
		for(int i = 0; i<length; i++) {
			memory1.buf[i] = buffer[i];
		}
		memory1.len = length;
		result = iFace.VCERT_CmsStrAttVerifyUpdateMem(verifyParameters, handle, memory1, memory2);
		return memory2.buf;
	}

	@Override
	public byte[] end() throws ReportError {
		result = iFace.VCERT_CmsStrAttVerifyFinalMem(verifyParameters, handle, memory2, verifyResult);
		return memory2.buf;
	}
	
}
