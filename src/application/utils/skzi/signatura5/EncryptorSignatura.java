package application.utils.skzi.signatura5;

import Pki1.LocalIface.encrypt_param_t;
import Pki1.LocalIface.mem_blk_t;
import Pki1.LocalIface.strencrypt_handle_t;
import application.errors.ReportError;
import application.models.Key;

public class EncryptorSignatura extends CommonSignatura {

	private encrypt_param_t encryptParameters;
	private strencrypt_handle_t handler;

	public EncryptorSignatura(Key key) {
		super(key);
	}

	/*private synchronized void setEncryptParameters() {
		if (encryptParameters == null) {
			encryptParameters = new encrypt_param_t();
			encryptParameters.flag = LocalIface.FLAG_PKCS7;
			encryptParameters.mycert = null;
			encryptParameters.receiver_num = 1;
			encryptParameters.receivers = new LocalIface.certificate_t[MAX_ELEMENTS_NUM];
			encryptParameters.receivers[0] = getCert("");
			MainApp.info("Encrypt parameters are set");
		}
	}*/

	@Override
	public void init() {
		encryptParameters = LocalSignatura.getEncryptedParameters();
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
		mem_blk_t memory = new mem_blk_t();
		memory.buf = buffer;
		memory.len = length;
		mem_blk_t memory_out = new mem_blk_t();
		
		result = iFace.VCERT_StrEncryptUpdateMem(encryptParameters, handler, memory, memory_out);
		if (result == 0)
			return memory_out.buf;
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
