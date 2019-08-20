package application.utils;

import java.security.InvalidKeyException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.PrivateKey;
import java.security.Signature;
import java.security.SignatureException;
import java.security.UnrecoverableKeyException;

import ru.CryptoPro.JCP.params.JCPProtectionParameter;

public class CryptoProTool {

	PrivateKey privateKey;
	Signature sig;

	public void init() {
		try {
			Signature sig = Signature.getInstance("CryptoProSignature_2012_256", "JCP");
			readKey("", "");
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

	private void readKey(String alias, String password) {
		JCPProtectionParameter protParam;
		KeyStore ks;
		try {
			ks = KeyStore.getInstance(KeyStore.getDefaultType());
			privateKey = (PrivateKey) ks.getKey(alias, password.toCharArray());

		} catch (KeyStoreException | UnrecoverableKeyException | NoSuchAlgorithmException e) {
			e.printStackTrace();
		}
	}

	public byte[] sign(byte[] data) {
		try {
			sig.update(data);
			byte[] sign = sig.sign();
			return sign;
		} catch (SignatureException e) {
			e.printStackTrace();
		}
		return null;
	}
	
	
}
