package application.utils.skzi.signatura6;

import Pki1.LocalIface;
import Pki1.LocalIface.sign_param_t;
import Pki1.LocalIface.strcms_handle_t;
import application.MainApp;
import application.errors.ReportError;
import application.models.Key;

public class SignSignatura extends CommonSignatura {

	private static volatile sign_param_t signParameters;

	public SignSignatura(Key key) throws ReportError {
		super(key);
		handle = new strcms_handle_t();
	}

	public static synchronized void setSignParameters() {
		signParameters = new sign_param_t();
		//signParameters.flag = LocalIface.FLAG_CMS_SIGN_ENVELOPE;
		// LocalIface.FLAG_PKCS7;
		// signParameters.flag = LocalIface.FLAG_CMS_SIGN_ENVELOPE;
		signParameters.mycert = null; // for local skzi
		MainApp.info("Sign parameters are set");
	}

	@Override
	public void init() {
		setSignParameters();
	}

	/**
	 * Don't use it
	 */
	@Override
	public void start() throws ReportError {
	}

	@Override
	public void start(byte[] buffer, int length) throws ReportError {
		memory1.buf = new byte[length];
		for (int i = 0; i < length; i++) {
			memory1.buf[i] = buffer[i];
		}
		memory1.len = length;
		result = iFace.VCERT_CmsStrAttSignInitMem(signParameters, memory1, handle);
	}

	@Override
	public byte[] next(byte[] buffer, int length) throws ReportError {
		memory1.buf = new byte[length];
		for (int i = 0; i < length; i++) {
			memory1.buf[i] = buffer[i];
		}
		memory1.len = length;
		result = iFace.VCERT_CmsStrAttSignUpdateMem(signParameters, handle, memory1, memory2);
		if (result != 0)
			throw new ReportError(result + "");
		return memory2.buf;
	}

	@Override
	public byte[] end() throws ReportError {
		result = iFace.VCERT_CmsStrAttSignFinalMem(signParameters, handle, memory2);
		unload();
		return memory2.buf;
	}

}
