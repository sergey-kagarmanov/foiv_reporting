package application.utils.skzi;

import Pki1.LocalIface;
import Pki1.LocalIface.encrypt_param_t;
import Pki1.LocalIface.strencrypt_handle_t;
import application.MainApp;
import application.errors.ReportError;
import application.models.Key;

public class EncryptorSignatura extends CommonSignatura {

	private static volatile encrypt_param_t encryptParameters;
	private strencrypt_handle_t handler;

	public EncryptorSignatura(Key key) {
		super(key);
	}

	private synchronized void setEncryptParameters() {
		if (encryptParameters == null) {
			encryptParameters = new encrypt_param_t();
			encryptParameters.flag = LocalIface.FLAG_PKCS7;
			encryptParameters.mycert = null;
			encryptParameters.receiver_num = 1;
			encryptParameters.receivers = new LocalIface.certificate_t[MAX_ELEMENTS_NUM];
			encryptParameters.receivers[0] = getCert("");
			MainApp.info("Encrypt parameters are set");
		}
	}

	@Override
	public void init() {
		setEncryptParameters();
		handler = new strencrypt_handle_t();
	}

	@Override
	public void start() throws ReportError {
		result = iFace.VCERT_StrEncryptInitMem(encryptParameters, handler);
		if (result != 0) {
			throw new ReportError(result + "");
		}
	}

	/**
	 * Not used in this implementation, use with empty parameter
	 */
	@Override
	public void start(byte[] buffer, int length) throws ReportError {

	}

	@Override
	public byte[] next(byte[] buffer, int length) throws ReportError {
		memory1.buf = buffer;
		memory1.len = length;
		result = iFace.VCERT_StrEncryptUpdateMem(encryptParameters, handler, memory1, memory2);
		if (result == 0)
			return memory2.buf;
		else
			throw new ReportError(result + "");

	}

	@Override
	public byte[] end() throws ReportError {
		result = iFace.VCERT_StrEncryptFinalMem(encryptParameters, handler, memory2);
		if (result == 0)
			return memory2.buf;
		else
			throw new ReportError(result + "");
	}

}