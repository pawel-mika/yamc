package pl.wcja.yamc.sound.edit;

/**
 * 
 * @author <a href="mailto:ketonal80@gmail.com">Pablo</a>, wcja.pl
 *
 */
public class Selection {
	private double selectionStart = 0;
	private double selectionEnd = 0;
	
	public Selection(double selectionStart, double selectionEnd) {
		super();
		this.selectionStart = selectionStart;
		this.selectionEnd = selectionEnd;
	}
	/**
	 * @return the selectionStart
	 */
	public double getSelectionStart() {
		return selectionStart;
	}
	/**
	 * @return the selectionEnd
	 */
	public double getSelectionEnd() {
		return selectionEnd;
	}
}