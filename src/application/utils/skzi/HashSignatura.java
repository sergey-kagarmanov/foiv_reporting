package application.utils.skzi;

import Pki1.LocalIface.strhash_handle_t;
import application.errors.ReportError;
import application.models.Key;

public class HashSignatura extends CommonSignatura {

	private strhash_handle_t handler; 
	private String algoritm;
	
	public HashSignatura(Key key) {
		super(key);
	}

	@Override
	public void init() {
		handler = new strhash_handle_t();
		algoritm = "1.2.643.7.1.1.2.2";
	}

	@Override
	public void start() throws ReportError {
		result = iFace.VCERT_StrHashInitMem(algoritm, handler);
	}

	@Override
	public void start(byte[] buffer, int length) throws ReportError {
		// TODO Auto-generated method stub
		
	}

	@Override
	public byte[] next(byte[] buffer, int length) throws ReportError {
		memory1.buf = buffer;
		memory1.len = length;
		result = iFace.VCERT_StrHashUpdateMem(handler, memory1);
		return null;
	}

	@Override
	public byte[] end() throws ReportError {
		result = iFace.VCERT_StrHashFinalMem(handler, memory2);
		return memory2.buf;
	}

}
