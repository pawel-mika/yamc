package pl.wcja.sound;

import java.io.Serializable;
import java.util.ArrayList;

/**
 * 
 * @author <a href="mailto:pawel.mika@geomar.pl">Pawe³ Mika</a>, Geomar SA
 *
 */
public class Tune implements Serializable {

	private String tuneName = "";
	
	private double BPM = 90;
	private double BPS = 90/60;
	
	private double viewFrom = 0;
	private double viewTo = 15;
	
	private ArrayList<Track> tracks = new ArrayList<Track>();

	public Tune(double bpm, String tuneName) {
		this.BPM = bpm;
		this.tuneName = tuneName;
		BPS = bpm / 60;
	}
	
	public Tune(double bpm) {
		this.BPM = bpm;
		BPS = bpm / 60;
	}
	
	public double getBPM() {
		return BPM;
	}
	
	public void setBPM(double bpm) {
		this.BPM = bpm;
		BPS = BPM / 60;
	}
	
	/**
	 * @return BPS - beats per second
	 */
	public double getBPS() {
		return BPS;
	}
	
	public void addTrack(Track track) {
		tracks.add(track);
	}
	
	public void addTrack() {
		tracks.add(new Track());
	}
	
	public ArrayList<Track> getTracks() {
		return tracks;
	}
	
	public void removeTrack(Track track) {
		tracks.remove(track);
	}

	/**
	 * @return the viewFrom
	 */
	public double getViewFrom() {
		return viewFrom;
	}

	/**
	 * @param viewFrom the viewFrom to set
	 */
	public void setViewFrom(double viewFrom) {
		this.viewFrom = viewFrom;
	}

	/**
	 * @return the viewTo
	 */
	public double getViewTo() {
		return viewTo;
	}

	/**
	 * @param viewTo the viewTo to set
	 */
	public void setViewTo(double viewTo) {
		this.viewTo = viewTo;
	}

	/**
	 * @return the tuneName
	 */
	public String getTuneName() {
		return tuneName;
	}

	/**
	 * @param tuneName the tuneName to set
	 */
	public void setTuneName(String tuneName) {
		this.tuneName = tuneName;
	}
}
