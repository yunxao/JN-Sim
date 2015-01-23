package tid.utils;

import java.io.File;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.filechooser.FileFilter;

public class ExtFilter extends FileFilter { 
	private String description;
	private ArrayList<Pattern> filters;
	public ExtFilter(String descrition){
		super();
		this.description = descrition;
		filters = new ArrayList<Pattern>();
		
	}
	public ExtFilter(String descrition,String regex){
		this(descrition);
		addFilter(regex);
		
	}
	public void addFilter(String regex){
		Pattern pattern = Pattern.compile(regex);
		filters.add(pattern);
	}
// Accept all directories and all gif, jpg, or tiff files. 
	public boolean accept(File f) {
		if (f.isDirectory()) {
			return true;
		}
		
		 
		String fileName = f.getName();
		for (int i = 0; i< filters.size();i++){
			Matcher encaja = filters.get(i).matcher(fileName);
			if (encaja.find())
				return true;
		}
		return false;
	}

	// The description of this filter
	public String getDescription() {
		return description;
	}
}