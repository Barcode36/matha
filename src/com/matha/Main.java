package com.matha;

import java.net.UnknownHostException;

import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;

import com.mongodb.MongoClient;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;

@ComponentScan(basePackages = "com.matha")
@Configuration
@EnableMongoRepositories(basePackages = "com.matha.repository")
public class Main extends Application {

	private static ApplicationContext context;
	private static Stage mainStage;
	private static AnchorPane mainPane;

	public static AnchorPane getMainPane() {
		return mainPane;
	}

	public static void setMainPane(AnchorPane mainPane) {
		Main.mainPane = mainPane;
	}

	@Override
	public void start(Stage primaryStage) throws Exception {
		try {
			// mainStage = primaryStage;
			// FXMLLoader loader = new
			// FXMLLoader(getClass().getResource("view/MainPage.fxml"));
			// Parent root = loader.load();
			// Scene scene = new Scene(root, 600, 400);
			// //
			// scene.getStylesheets().add(getClass().getResource("application.css").toExternalForm());
			// mainStage.setScene(scene);
			// mainStage.show();

			mainStage = primaryStage;
			FXMLLoader loader = new FXMLLoader(getClass().getResource("view/landing.fxml"));
			Parent root = loader.load();
			Scene scene = new Scene(root);
			// scene.getStylesheets().add(getClass().getResource("application.css").toExternalForm());
			mainStage.setScene(scene);
			mainStage.show();
		} catch (Exception e) {
			e.printStackTrace();
			throw e;
		}
	}

	public static void loadScene(Scene arg0) {
		mainStage.setScene(arg0);
	}

	public static Scene getScene() {
		return mainStage.getScene();
	}

	public static void main(String[] args) {

		context = new AnnotationConfigApplicationContext(Main.class);

		launch(args);
	}

	public static ApplicationContext getContext() {
		return context;
	}

	public @Bean MongoClient mongo() throws UnknownHostException {
		return new MongoClient();
	}

	public @Bean MongoTemplate mongoTemplate() throws Exception {
		return new MongoTemplate(mongo(), "mydatabase");
	}
}
