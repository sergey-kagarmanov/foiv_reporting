package application.utils.skzi;

import Pki1.LocalIface;
import Pki1.LocalIface.sign_param_t;
import Pki1.LocalIface.strsign_handle_t;
import application.MainApp;
import application.errors.ReportError;
import application.models.Key;

public class SignSignatura extends CommonSignatura {

	private static volatile sign_param_t signParameters;
	private strsign_handle_t handler;
	
	public SignSignatura(Key key) {
		super(key);
	}
	
	public static synchronized void setSignParameters() {
		signParameters = new sign_param_t();
		signParameters.flag = LocalIface.FLAG_PKCS7;
		signParameters.mycert = null; // for local skzi
		MainApp.info("Sign parameters are set");
	}

	@Override
	public void init() {
		setSignParameters();
		handler = new strsign_handle_t();
	}

	/**
	 * Don't use it
	 */
	@Override
	public void start() throws ReportError {
	}

	/**
	 * Don't use it
	 */
	@Override
	public void start(byte[] buffer, int length) throws ReportError {
	}

	@Override
	public byte[] next(byte[] buffer, int length) throws ReportError {
		memory1.buf = buffer;
		memory1.len = length;
		result = iFace.VCERT_SignMem(signParameters, null, memory1, memory2);
		if (result != 0)
			throw new ReportError(result + "");
		return memory2.buf;
	}

	/**
	 * Don't use it
	 */
	@Override
	public byte[] end() throws ReportError {
		return null;
	}

}
