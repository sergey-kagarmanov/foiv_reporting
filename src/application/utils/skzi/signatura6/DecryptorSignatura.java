package application.utils.skzi.signatura6;

import Pki1.LocalIface.decrypt_param_t;
import Pki1.LocalIface.decrypt_result_t;
import application.MainApp;
import application.errors.ReportError;
import application.models.Key;

public class DecryptorSignatura extends CommonSignatura {

	private decrypt_param_t decryptParameters;
	private decrypt_result_t decryptResult = null;

	public DecryptorSignatura(Key key) throws ReportError {
		super(key);
	}

	@Override
	public void init() {
		decryptParameters = new decrypt_param_t();
		MainApp.info("Decrypt parameters are set");
	}

	/**
	 * Don't use it, instead use start(byte[], int)
	 */
	@Override
	public void start() throws ReportError {
		// TODO Auto-generated method stub

	}

	@Override
	public void start(byte[] buffer, int length) throws ReportError {
		memory1.buf = buffer;
		memory1.len = length;
		result = iFace.VCERT_CmsStrDecryptInitMem(decryptParameters, memory1, handle);
	}

	@Override
	public byte[] next(byte[] buffer, int length) throws ReportError {
		memory1.buf = buffer;
		memory1.len = length;
		result = iFace.VCERT_CmsStrDecryptUpdateMem(decryptParameters, handle, memory1, memory2);
		return memory2.buf;
	}

	@Override
	public byte[] end() throws ReportError {
		result = iFace.VCERT_CmsStrDecryptFinalMem(decryptParameters, handle, memory2, decryptResult);
		return memory2.buf;
	}

}
