package org.snrg_nyc.persistence;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

public class FileSystem {
	public static final Path basePath = Paths.get("snrg_data");
	public static final Path savePath = basePath.resolve("save");
	public static final Path templatePath = basePath.resolve("templates");
	
	static {
		Path[] paths = {basePath, savePath};
		for(Path p : paths){
			if(!Files.exists(p)){
				p.toFile().mkdir();
			}
		}
	}
	
	public static List<String> templates(){
		List<String> s = new ArrayList<>();
		for( File f : templatePath.toFile().listFiles()){
			if(!f.isDirectory() && f.getName().endsWith(".json")){
				s.add(f.getName().replace(".json", ""));
			}
		}
		return s;
	}

}
