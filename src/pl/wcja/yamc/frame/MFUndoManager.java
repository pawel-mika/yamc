package pl.wcja.yamc.frame;

import javax.swing.undo.UndoManager;

public class MFUndoManager extends UndoManager {

	/**
	 * 
	 */
	private static final long serialVersionUID = 6679326953791723584L;
	private IMainFrame mf = null;
	
	public MFUndoManager(IMainFrame mf) {
		this.mf = mf;
	}
}