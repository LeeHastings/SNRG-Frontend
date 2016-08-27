package org.snrg_nyc.test;

import java.util.HashMap;
import java.util.Map;

import org.snrg_nyc.ui.components.ButtonList;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class UI_ElementsTest extends Application{
	public static void 
	main(String[] args){
		UI_ElementsTest.launch(args);
	}

	@Override
	public void start(Stage stage1) throws Exception {
		Map<Integer, String> items = new HashMap<>();
		items.put(0, "banana");
		items.put(1, "apple");
		items.put(2, "grapes");
		
		ButtonList<Integer> b = new ButtonList<>(items.keySet());
		b.setLabelFactory( item -> items.get(item) );
		b.addClickListener(item -> 
			System.out.println("Selected "+items.get(item)) );
			
		Scene scene = new Scene(b, 100, 100);
		stage1.setScene(scene);
		stage1.show();
	}
}
