package pl.wcja.yamc.event;

import java.util.EventObject;

/**
 * 
 * @author <a href="mailto:ketonal80@gmail.com">Pablo</a>, wcja.pl
 *
 */
public class ProgressEvent extends EventObject {

	/**
	 * 
	 */
	private static final long serialVersionUID = -3058437611585437042L;
	private String description = null;
	private int totalParts = 0;
	private int doneParts = 0;
	
	public ProgressEvent(Object source, String description, int totalParts, int doneParts) {
		super(source);
		this.description = description;
		this.totalParts = totalParts;
		this.doneParts = doneParts;
	}

	public String getDescription() {
		return description;
	}

	public int getTotalParts() {
		return totalParts;
	}

	public int getDoneParts() {
		return doneParts;
	}
}