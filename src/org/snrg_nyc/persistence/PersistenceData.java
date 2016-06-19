package org.snrg_nyc.persistence;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class PersistenceData {
	/**
	 * The path in which all experiments are saved and loaded
	 */
	public static final Path saveLocation = Paths.get("save_data").toAbsolutePath();
	
	/**
	 * Get all experiment directories in the save path.
	 * @return The experiments that are currently saved
	 */
	public static List<String> getFileNames(){
		File f = saveLocation.toFile();
		String[] experiments = f.list((File file, String name)->
			new File(file, name).isDirectory()
		);
		if(experiments != null && experiments.length > 0){
			return Arrays.asList(experiments);
		}
		else {
			return new ArrayList<>();
		}
		
	}
}
