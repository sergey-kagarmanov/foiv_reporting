package application.utils.skzi;

import Pki1.LocalIface;
import Pki1.LocalIface.decrypt_param_t;
import Pki1.LocalIface.decrypt_result_t;
import Pki1.LocalIface.strdecrypt_handle_t;
import application.MainApp;
import application.errors.ReportError;
import application.models.Key;

public class DecryptorSignatura extends CommonSignatura {

	private decrypt_param_t decryptParameters;
	private strdecrypt_handle_t handler;
	private int[] left = new int[] { 1024 };
	private decrypt_result_t decryptResult = null;

	public DecryptorSignatura(Key key) {
		super(key);
	}


	@Override
	public void init() {
		decryptParameters = new decrypt_param_t();
		decryptParameters.flag = LocalIface.FLAG_PKCS7;
		handler = new strdecrypt_handle_t();
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
		result = iFace.VCERT_StrDecryptInitMem(decryptParameters, memory1, left, handler);
		if (result != 0) {
			throw new ReportError(result + "");
		}
	}

	@Override
	public byte[] next(byte[] buffer, int length) throws ReportError {
		memory1.buf = buffer;
		memory1.len = length;
		result = iFace.VCERT_StrDecryptUpdateMem(decryptParameters, handler, memory1, left,
				memory2);
		if (result == 0)
			return memory2.buf;
		else {
			throw new ReportError(result + "");
		}
	}

	@Override
	public byte[] end() throws ReportError {
		result = iFace.VCERT_StrDecryptFinalMem(decryptParameters, handler, decryptResult);
		if (result == 0)
			return memory2.buf;
		else
			throw new ReportError(result + "");
	}

}
