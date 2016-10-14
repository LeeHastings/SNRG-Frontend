package org.snrg_nyc.persistence;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public final class FileSystem {
	public static final Path basePath = Paths.get("snrg_data");
	public static final Path savePath = basePath.resolve("save");
	public static final Path templatePath = basePath.resolve("templates");
	
	static {
		Path[] paths = {basePath, savePath, templatePath};
		for(Path p : paths){
			if(!Files.exists(p)){
				p.toFile().mkdir();
			}
		}
	}

}
