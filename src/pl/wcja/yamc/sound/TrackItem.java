package pl.wcja.yamc.sound;

import java.io.Serializable;

/**
 * 
 * @author <a href="mailto:pawel.mika@geomar.pl">Pawe³ Mika</a>, Geomar SA
 *
 */
public class TrackItem implements Serializable {

	//track that this item belongs to
	private Track track = null;
	private String filePath = null;
	private double timeFrom = 0;
	private double timeTo = 1;

	/**
	 * 
	 * @param track initial track for this item
	 * @param timeFrom time from this item starts on the track (seconds)
	 * @param timeTo time on the item ends (seconds)
	 */
	public TrackItem(Track track, double timeFrom, double timeTo) {
		this.track = track;
		this.timeFrom = timeFrom;
		this.timeTo = timeTo;
	}
	
	/**
	 * @return the timeFrom
	 */
	public double getTimeFrom() {
		return timeFrom;
	}
	/**
	 * @param timeFrom the timeFrom to set
	 */
	public void setTimeFrom(double timeFrom) {
		this.timeFrom = timeFrom;
	}
	/**
	 * @return the timeTo
	 */
	public double getTimeTo() {
		return timeTo;
	}
	/**
	 * @param timeTo the timeTo to set
	 */
	public void setTimeTo(double timeTo) {
		this.timeTo = timeTo;
	}

	/**
	 * @return the track
	 */
	public Track getTrack() {
		return track;
	}

	/**
	 * @param track the track to set
	 */
	public void setTrack(Track track) {
		this.track = track;
	}
	
	/**
	 * Sets this trackitem lenght by setting timeTo = timeFrom + seconds
	 * @param seconds
	 */
	public void setLenght(double seconds) {
		this.timeTo = timeFrom + seconds;
	}
	
	/**
	 * 
	 * @return
	 */
	public double getLenght() {
		return timeTo - timeFrom;
	}

	/**
	 * Path to the file that track item is bounded to
	 * @return the filePath
	 */
	public String getFilePath() {
		return filePath;
	}

	/**
	 * @param filePath the filePath to set
	 */
	public void setFilePath(String filePath) {
		this.filePath = filePath;
	}
}