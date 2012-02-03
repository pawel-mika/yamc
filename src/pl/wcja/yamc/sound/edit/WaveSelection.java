package pl.wcja.yamc.sound.edit;

/**
 * 
 * @author <a href="mailto:ketonal80@gmail.com">Pablo</a>, wcja.pl
 *
 */
public class WaveSelection extends Selection {

	private byte[] selectedData = null;
	
	public WaveSelection(double selectionStart, double selectionEnd, byte[] selectedData) {
		super(selectionStart, selectionEnd);
		this.selectedData = selectedData;
	}

	public byte[] getSelectedData() {
		return selectedData;
	}
}