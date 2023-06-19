package boardanalyzer;

import java.io.File;

import javax.swing.filechooser.FileFilter;

public class ImageFileFilter extends FileFilter {

	@Override
	public boolean accept(File f) {
		if (f.exists()) {
			String s = f.getName();
			return s.contains(".png") || s.contains(".jpeg") || s.contains("jpg");
		}
		return false;
	}

	@Override
	public String getDescription() {
		return "JPG and PNG images";
	}

}
