package org.snrg_nyc.test;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import org.snrg_nyc.model.EditorException;
import org.snrg_nyc.model.NodeEditor;
import org.snrg_nyc.model.PropertiesEditor;
import org.snrg_nyc.ui.UI_Main;
import org.snrg_nyc.ui.components.ButtonList;
import org.snrg_nyc.ui.components.DynamicTable;
import org.snrg_nyc.ui.components.DynamicTableBuilder;

import javafx.application.Application;
import javafx.beans.property.SimpleStringProperty;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.util.StringConverter;

public class UI_ElementsTest extends Application{
	
	public static void 
	main(String[] args){
		UI_ElementsTest.launch(args);
	}

	@Override
	public void 
	start(Stage stage1){
		testDynamicTable(stage1);
	}
	
	public void 
	testBiDistMaker() throws EditorException{
		PropertiesEditor model = new NodeEditor();
		model.load("pwid_demo");
		
	}
	
	public void 
	testButtonList(Stage stage1) {
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
	public void 
	testDynamicTable(Stage stage1){
		Map<String, Map<String, Float>> map = new HashMap<>();
		List<String> labels = Arrays.asList(
				"single", "double", "triple", "quad", "unknown", "bacon", 
				"lover","whyhere", "whyme", "thereisnogod", "please", "help",
				"unloveable", "thisisus");
		Random rand = new Random();
		for(String s : labels){
			map.put(s, new HashMap<>());
			for(String s2 : labels){
				map.get(s).put(s2, rand.nextFloat()*15);
			}
		}
		
		StringConverter<Float> con = new StringConverter<Float>(){
			@Override
			public Float fromString(String string) {
				if(string == null){
					return null;
				}
				try{
					return Float.parseFloat(string);
				}
				catch (Exception e){
					e.printStackTrace();
					return null;
				}
			}
			@Override
			public String toString(Float object) {
				if(object == null){
					return null;
				}
				return object.toString();
			}
		};
		
		DynamicTable<String, String, Float> t= 
			new DynamicTableBuilder<String, String, Float>()
			    .build(map, con);
		
		t.rowLabelRegion().setMinWidth(80);
		t.setRowLabelFactory((data)->{
			return new SimpleStringProperty(data.getValue());
		});
		
		Button b = new Button("Print Map");
		b.setOnMouseClicked(event->{
			System.out.println(t.getMap());
		});
		
		VBox v = new VBox();
		v.getChildren().addAll(t, b);
		
		t.tableRegion().getStyleClass().add("dynamictable");
		t.rowLabelRegion().getStyleClass().add("dynamictable");
		
		Scene scene = new Scene(v, 500, 300);
		scene.getStylesheets().add(
				UI_Main.class
				.getResource("ui_style.css").toExternalForm());
		
		stage1.setScene(scene);
		stage1.show();
	}

}
