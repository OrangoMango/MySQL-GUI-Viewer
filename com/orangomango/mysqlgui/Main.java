package com.orangomango.mysqlgui;

import javafx.application.Application;
import javafx.stage.Stage;
import javafx.scene.Scene;
import javafx.scene.layout.GridPane;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Alert;
import javafx.scene.control.ListView;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TablePosition;
import javafx.collections.ObservableList;
import javafx.collections.FXCollections;
import javafx.beans.property.SimpleStringProperty;
import javafx.geometry.Insets;

import java.util.*;
import java.sql.*;
import java.io.*;

public class Main extends Application{

	private ObservableList<ObservableList<String>> list;
	private TableView<ObservableList<String>> tableview;
	private String[] DBdata = new String[]{"NULL", "NULL"};
	private Label info;
	private Stage stage;
	private ListView<String> queryList;

	public static void main(String[] args){
		launch(args);
	}
	
	private TableView<ObservableList<String>> createTable(ResultSet rs, Statement stmt) throws SQLException{
		TableView<ObservableList<String>> tableview = new TableView<>();
		if (rs == null){
			if (!DBdata[1].equals("NULL")){
				rs = stmt.executeQuery("SELECT * FROM "+DBdata[1]);
			} else {
				return tableview;
			}
		}
		ResultSetMetaData meta = rs.getMetaData();
		for (int i = 1; i <= meta.getColumnCount(); i++){
			if (i == 1){
				DBdata[1] = meta.getTableName(i);
				this.info.setText(String.format("Database: %s, Table: %s", DBdata[0], DBdata[1]));
			}
			TableColumn<ObservableList<String>, String> column = new TableColumn<ObservableList<String>, String>(meta.getColumnName(i));
			final int now = i;
			column.setCellValueFactory(p -> new SimpleStringProperty(p.getValue().get(now-1)));
			tableview.getColumns().add(column);
		}
		
		this.list = FXCollections.observableArrayList();
		
		while (rs.next()){
			ObservableList<String> sublist = FXCollections.observableArrayList();
			for (int i = 1; i <= meta.getColumnCount(); i++){
				Object o = rs.getObject(i);
				sublist.add(o != null ? o.toString() : "NULL");
			}
			this.list.add(sublist);
		}
		
		tableview.setItems(this.list);
		return tableview;
	}
	
	public void start(Stage stage) throws SQLException, IOException{
	
		File f = new File("credentials.txt");
		if (!f.exists()){
			f.createNewFile();
		}
	
		this.stage = stage;
		LoginInterface li = new LoginInterface(this);
	}
	
	public boolean setup(String host, String user, String database, String password) throws SQLException, IOException{
		Connection conn;
		try {
			conn = DriverManager.getConnection(String.format("jdbc:mysql://%s:3306/%s", host, database), user, password);
		} catch (SQLException ex){
			Alert alert = new Alert(Alert.AlertType.ERROR);
			alert.setTitle("SQL Error");
			alert.setHeaderText("Authentication error");
			alert.getDialogPane().setContent(new Label(ex.getMessage()));
			alert.showAndWait();
			return false;
		}
		
		BufferedWriter writer = new BufferedWriter(new FileWriter(new File("credentials.txt")));
		writer.write((host.equals("") ? "localhost" : host)+" "+user+" "+database);
		writer.close();
		
		Statement stmt = conn.createStatement();
		ResultSet rs = null;
		DBdata[0] = database;
		this.info = new Label(String.format("Database: %s, Table: %s", DBdata[0], DBdata[1]));
		this.tableview = createTable(rs, stmt);
		
		GridPane layout = new GridPane();
		this.tableview.setPrefHeight(500);
		this.tableview.setPrefWidth(800);
		
		TextArea queryArea = new TextArea();
		queryArea.setPromptText("Insert QUERY");
		queryArea.setMaxWidth(350);
		Button update = new Button("Execute\nquery");
		update.setOnAction(e -> {
			try {
				String data = queryArea.getText();
				if (!data.equals("")){
					this.queryList.getItems().add(0, data);
				}
				System.out.println("> "+data);
				ResultSet res = null;
				boolean o = stmt.execute(data);
				if (o){
					res = stmt.getResultSet();
				}
				this.tableview = createTable(res, stmt);
				this.tableview.setPrefHeight(500);
				this.tableview.setPrefWidth(800);
				layout.getChildren().set(0, this.tableview);
				GridPane.setRowSpan(this.tableview, 3);
				Alert alert = new Alert(Alert.AlertType.INFORMATION);
				alert.setTitle("SQL Info");
				alert.setHeaderText("Query executed successfully");
				alert.setContentText(null);
				alert.showAndWait();
			} catch (SQLException ex){
				Alert alert = new Alert(Alert.AlertType.ERROR);
				alert.setTitle("SQL Error");
				alert.setHeaderText("SQL Error");
				alert.getDialogPane().setContent(new Label(ex.getMessage()));
				alert.showAndWait();
			}
		});
		
		this.queryList = new ListView<>();
		this.queryList.setPlaceholder(new Label("No queries used"));
		this.queryList.getSelectionModel().selectedItemProperty().addListener((obs, oldValue, newValue) -> queryArea.setText(newValue));
		
		layout.setPadding(new Insets(10, 10, 10, 10));
		layout.setHgap(5);
		layout.setVgap(5);
		layout.add(this.tableview, 0, 0, 1, 3);
		layout.add(queryArea, 1, 0, 2, 1);
		layout.add(info, 1, 1, 2, 1);
		layout.add(update, 1, 2);
		layout.add(this.queryList, 2, 2);
		
		stage.setScene(new Scene(layout, 1200, 550));
		stage.setResizable(false);
		stage.setTitle("MySQL GUI");
		stage.show();
		return true;
	}
}
