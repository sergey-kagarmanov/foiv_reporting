package application.utils.skzi.signatura6;

import Pki1.LocalIface.encrypt_param_t;
import Pki1.LocalIface.mem_blk_t;
import Pki1.LocalIface.strcms_handle_t;
import application.errors.ReportError;
import application.models.Key;

public class EncryptorSignatura extends CommonSignatura {

	private encrypt_param_t encryptParameters;
	private strcms_handle_t handle;

	public EncryptorSignatura(Key key) throws ReportError {
		super(key);
		handle = new strcms_handle_t();
		encryptParameters = LocalSignatura.getEncryptedParameters();
	}


	@Override
	public void init() {
	}

	/**
	 * Not used in this implementation, use with parameters
	 */
	@Override
	public void start() throws ReportError {
	}

	@Override
	public void start(byte[] buffer, int length) throws ReportError {
		memory1 = new mem_blk_t();
		byte[] bytes = new byte[length];
		for (int i = 0; i < length; i++) {
			bytes[i] = buffer[i];
		}
		memory1.buf = bytes;
		memory1.len = length;
		result = iFace.VCERT_CmsStrEncryptInitMem(encryptParameters, memory1, handle);
	}

	@Override
	public byte[] next(byte[] buffer, int length) throws ReportError {
		memory1 = new mem_blk_t();
		byte[] bytes = new byte[length];
		for (int i = 0; i < length; i++) {
			bytes[i] = buffer[i];
		}
		memory1.buf = bytes;
		memory1.len = length;
		memory2 = new mem_blk_t();

		result = iFace.VCERT_CmsStrEncryptUpdateMem(encryptParameters, handle, memory1, memory2);
		return memory2.buf;
	}

	@Override
	public byte[] end() throws ReportError {
		memory2 = new mem_blk_t();
		result = iFace.VCERT_CmsStrEncryptFinalMem(encryptParameters, handle, memory2);
		return memory2.buf;
	}

}
