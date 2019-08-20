package application.utils;

import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.PrivateKey;
import java.security.Signature;
import java.security.SignatureException;

public class CryptoProTool {

	PrivateKey privateKey;
	Signature sig;
	
	public void init() {
		try {
			Signature sig = Signature.getInstance("CryptoProSignature_2012_256", "JCP");
			readKey();
			sig.initSign(privateKey);
		} catch (NoSuchAlgorithmException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (NoSuchProviderException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InvalidKeyException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
		
		
	private void readKey() {
		
	}
	
	public void sign() {
		byte[] data = null;
		try {
			sig.update(data);
		} catch (SignatureException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}

