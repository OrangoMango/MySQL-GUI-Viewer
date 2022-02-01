package com.orangomango.mysqlgui;

import javafx.stage.Stage;
import javafx.scene.Scene;
import javafx.scene.layout.GridPane;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.control.PasswordField;
import javafx.geometry.Insets;

import java.sql.SQLException;
import java.io.*;

public class LoginInterface {
	
	private Main main;
	private Stage stage;
	private String[] data;

	public LoginInterface(Main main) throws IOException{
		this.main = main;
		this.stage = new Stage();
		this.stage.setTitle("MySQL Login");
		
		GridPane layout = new GridPane();
		layout.setPadding(new Insets(10, 10, 10, 10));
		layout.setHgap(5);
		layout.setVgap(5);
		
		File f = new File("credentials.txt");
		String[] data = new String[3];
		if (f.exists()){
			BufferedReader reader = new BufferedReader(new FileReader(f));
			String d = reader.readLine();
			data = d.split(" ");
			reader.close();
		}
		
		Label hostL = new Label("Host: ");
		Label userL = new Label("User: ");
		Label databaseL = new Label("Database: ");
		Label passwordL = new Label("Password: ");
		TextField hostT = new TextField(data[0]);
		TextField userT = new TextField(data[1]);
		TextField databaseT = new TextField(data[2]);
		TextField passwordT = new PasswordField();
		
		Button login = new Button("Login");
		login.setOnAction(ev -> {
			try {
				this.data = new String[]{hostT.getText(), userT.getText(), databaseT.getText(), passwordT.getText()};
				finish();
			} catch (SQLException | IOException e){
				e.printStackTrace();
			}
		});
		
		layout.add(hostL, 0, 0);
		layout.add(hostT, 1, 0);
		layout.add(userL, 0, 1);
		layout.add(userT, 1, 1);
		layout.add(databaseL, 0, 2);
		layout.add(databaseT, 1, 2);
		layout.add(passwordL, 0, 3);
		layout.add(passwordT, 1, 3);
		layout.add(login, 0, 4);
		
		this.stage.setScene(new Scene(layout, 300, 200));		
		this.stage.setResizable(false);
		this.stage.show();
	}
	
	public void finish() throws SQLException, IOException{
		boolean output = this.main.setup(this.data[0], this.data[1], this.data[2], this.data[3]);
		if (output) {
			this.stage.hide();
		}
	}
}
