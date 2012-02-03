package pl.wcja.yamc.gui;

import java.awt.BorderLayout;

import pl.wcja.yamc.frame.IMainFrame;

public class AnalyzerDialog extends MFOkCancelDialog {

	MFSpectrumAnalyzer mfs = null;
	
	public AnalyzerDialog(IMainFrame mf) {
		super(mf);
		mfs = new MFSpectrumAnalyzer(mf);
		add(mfs, BorderLayout.CENTER);
		pack();
	}

	@Override
	protected void okClicked() {
		// TODO Auto-generated method stub

	}

	@Override
	protected void cancelCliked() {
		// TODO Auto-generated method stub

	}

}
