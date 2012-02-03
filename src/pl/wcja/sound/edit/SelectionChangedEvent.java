package pl.wcja.sound.edit;

import java.util.EventObject;

import pl.wcja.jcommon.Unit;

/**
 * 
 * @author <a href="mailto:pawel.mika@geomar.pl">Pawe³ Mika</a>, Geomar SA
 *
 */
public class SelectionChangedEvent extends EventObject {

	/**
	 * 
	 */
	private static final long serialVersionUID = -8019631146765531206L;
	private Selection selection = null;
	private Unit unit = Unit.SAMPLE;
	
	public SelectionChangedEvent(Object source, Selection selection, Unit unit) {
		super(source);
		this.selection = selection;
		this.unit = unit;
	}
	
	public Selection getSelection() {
		return selection;
	}
		/**
	 * @return the unit
	 */
	public Unit getUnit() {
		return unit;
	}
}
