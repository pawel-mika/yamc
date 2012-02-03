package pl.wcja.yamc.sound.edit;

import pl.wcja.yamc.frame.IMainFrame;
import pl.wcja.yamc.sound.Tune;
import pl.wcja.yamc.sound.edit.TuneEditorGrid.GridDisplay;

public interface TuneEditor {

	public IMainFrame getMainFrame();
	
	public void setTune(Tune tune);
	
	public Tune getTune();
	
	public GridDisplay getGridDisplay();
	
	public void setGridDisplay(GridDisplay gd);
	
	public double getMarkerPosition();
	
	public void setMarkerLocation(double markerPosition);
}
