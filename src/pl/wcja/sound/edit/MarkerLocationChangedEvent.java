package pl.wcja.sound.edit;

import java.util.EventObject;

import pl.wcja.jcommon.Unit;

/**
 * 
 * @author <a href="mailto:ketonal80@gmail.com">Pablo</a>, wcja.pl
 *
 */
public class MarkerLocationChangedEvent extends EventObject {

	/**
	 * 
	 */
	private static final long serialVersionUID = -5980760850127149176L;
	private double markerLocation = 0;
	private Unit unit = Unit.SAMPLE;
	
	public MarkerLocationChangedEvent(Object source, double markerLocation, Unit unit) {
		super(source);
		this.markerLocation = markerLocation;
		this.unit = unit;
	}

	/**
	 * @return the time that the marker currently stands on
	 */
	public double getTime() {
		return markerLocation;
	}

	/**
	 * @return the unit
	 */
	public Unit getUnit() {
		return unit;
	}
}