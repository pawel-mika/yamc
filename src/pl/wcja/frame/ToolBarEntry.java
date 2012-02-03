package pl.wcja.frame;

import java.awt.Component;

/**
 * 
 * @author <a href="mailto:ketonal80@gmail.com">Pablo</a>, wcja.pl
 *
 */
public interface ToolBarEntry extends MenuEntry {

	public Component getToolbarComponent();
	
	public int getOffsetModifier();
	
}
