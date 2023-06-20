package boardanalyzer.ui.basic_elements;

import boardanalyzer.BoardFrame;

import java.io.File;

import javax.swing.filechooser.FileFilter;

public class BoardFileFilter extends FileFilter {

	@Override
	public boolean accept(File f) {
		if (f.exists()) {
			String s = f.getName();
			return s.contains(BoardFrame.BOARD_EXTENSION);
		}
		return false;
	}

	@Override
	public String getDescription() {
		return "Board saves";
	}

}
