package application.utils.skzi.signatura6;

import Pki1.LocalIface;
import Pki1.LocalIface.mem_blk_t;
import Pki1.LocalIface.strcms_handle_t;
import application.MainApp;
import application.errors.ReportError;
import application.models.Key;
import application.utils.skzi.SignaturaExecutor;

/**
 * Add functionality to get signatura interface, load user certificate and load recipients certificates
 * Add local fields like memory, result
 * @author Sergey Kagarmanov
 *
 */
public abstract class CommonSignatura implements SignaturaExecutor{

	protected LocalIface iFace;
	protected int result = 0;
	protected static final int MAX_ELEMENTS_NUM = 32;
	protected strcms_handle_t handle;
	mem_blk_t memory1;
	mem_blk_t memory2;


	public CommonSignatura(Key key) throws ReportError {
		iFace = LocalSignatura.getLocaIFace();
		//LocalSignatura.initSignatura(key.getData());
		MainApp.info("Work on process start");
		memory1 = new mem_blk_t();
		memory2 = new mem_blk_t();
		handle = new strcms_handle_t();
		init();

	}
	
	/*public void unload() {
		result = iFace.VCERT_Uninitialize();
		if (result == LocalIface.VCERT_OK) {
			MainApp.info("Local interface Signatura unloads successfully");
		} else {
			MainApp.error("Local interface Signatura unloads with error - "
					+ Integer.toHexString(result));
		}
	}*/
	public int getResult() {
		return result;
	}
}
