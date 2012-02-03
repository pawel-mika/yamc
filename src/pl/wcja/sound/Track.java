package pl.wcja.sound;

import java.io.Serializable;
import java.util.LinkedList;
import java.util.List;

/**
 * 
 * @author <a href="mailto:pawel.mika@geomar.pl">Pawe³ Mika</a>, Geomar SA
 *
 */
public class Track implements Serializable {

	private String trackName = "";
	
	private List<TrackItem> items = new LinkedList<TrackItem>();
	
	public Track() {
		
	}
	
	public Track(String trackName) {
		this.trackName = trackName;
	}
	
	public void addItem(TrackItem item) {
		items.add(item);
		item.setTrack(this);
	}
	
	public void removeItem(TrackItem item) {
		items.remove(item);
		item.setTrack(null);
	}

	public List<TrackItem> getItems() {
		return items;
	}
	
	public String toString() {
		return trackName;
	}
	
}