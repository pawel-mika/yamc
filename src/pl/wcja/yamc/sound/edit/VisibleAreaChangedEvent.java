package pl.wcja.yamc.sound.edit;

import java.util.EventObject;

import pl.wcja.yamc.jcommon.Unit;

/**
 * 
 * @author <a href="mailto:ketonal80@gmail.com">Pablo</a>, wcja.pl
 *
 */
public class VisibleAreaChangedEvent extends EventObject {

	/**
	 * 
	 */
	private static final long serialVersionUID = 3153300909274235368L;
	private double visibleStart = 0;
	private double visibleEnd = 0;
	private Unit unit = Unit.SAMPLE;
	
	public VisibleAreaChangedEvent(Object source, double sampleStart, double sampleEnd, Unit unit) {
		super(source);
		this.visibleStart = sampleStart;
		this.visibleEnd = sampleEnd;
		this.unit = unit;
	}

	/**
	 * @return first visible 
	 */
	public double getVisibleStart() {
		return visibleStart;
	}

	/**
	 * @return last visible 
	 */
	public double getVisibleEnd() {
		return visibleEnd;
	}
	
	/**
	 * @return the unit
	 */
	public Unit getUnit() {
		return unit;
	}
}