package application;

import java.io.IOException;

import application.db.Dao;
import application.models.AttributeDescr;
import application.models.Chain;
import application.models.FileType;
import application.models.Key;
import application.models.ProcessStep;
import application.models.Report;
import application.view.ActionViewController;
import application.view.AddEditAttributeDescrController;
import application.view.AddEditChainDialogController;
import application.view.AddReportController;
import application.view.AddeditActionDialogController;
import application.view.ArchiveOverviewController;
import application.view.AttributeEditController;
import application.view.AttributeLocationDialogController;
import application.view.ChooseKeyDialogController;
import application.view.EncryptOverviewController;
import application.view.FileTypeDialogController;
import application.view.KeyViewController;
import application.view.ReportChooserDialogController;
import application.view.RootLayoutController;
import application.view.SettingsDialogController;
import application.view.SingleActionController;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.BorderPane;
import javafx.stage.Modality;
import javafx.stage.Stage;

public class MainApp extends Application {

    private Stage primaryStage;
    private BorderPane rootLayout;
    private Dao dao;
    
    /**
     * Temp data
     */
    private Report currentReport;
    
    public static Boolean IN = true;
    public static Boolean OUT = false;

	@Override
	public void start(Stage primaryStage) {
		this.primaryStage = primaryStage;
		this.primaryStage.setTitle("Работа с шифрованными файлами");
		
		initDB();
		
		initRootLayout();
		
		showEncryptOverview();
	}

	public static void main(String[] args) {
		launch(args);
	}
	
	public Dao getDb(){
		return dao;
	}
	
	public void initDB(){
		dao = new Dao();
	}
	
	public void initRootLayout() {
        try {
            // Загружаем корневой макет из fxml файла.
            FXMLLoader loader = new FXMLLoader();
            loader.setLocation(MainApp.class.getResource("view/RootLayout.fxml"));
            rootLayout = (BorderPane) loader.load();

            // Отображаем сцену, содержащую корневой макет.
            Scene scene = new Scene(rootLayout);
            primaryStage.setScene(scene);
            primaryStage.getIcons().add(new Image(this.getClass().getResourceAsStream("key.png")));
            
            RootLayoutController controller = loader.getController();
            controller.setMainApp(this);
            primaryStage.setMaximized(true);
            primaryStage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
	
	public void showEncryptOverview(){
        try {
            // Загружаем сведения об адресатах.
            FXMLLoader loader = new FXMLLoader();
            loader.setLocation(MainApp.class.getResource("view/ReportDetails.fxml"));
            AnchorPane personOverview = (AnchorPane) loader.load();

            // Помещаем сведения об адресатах в центр корневого макета.
            rootLayout.setCenter(personOverview);
            
            EncryptOverviewController controller = loader.getController();
            controller.setMainApp(this);

        } catch (IOException e) {
            e.printStackTrace();
        }

	}
	
	public void showProcessStepDialog(ProcessStep step, Chain chain){
        try {
             FXMLLoader loader = new FXMLLoader();
            loader.setLocation(MainApp.class.getResource("view/AddeditActionDialog.fxml"));
            AnchorPane editOverview = (AnchorPane) loader.load();

			// Create the dialog Stage.
            Stage dialogStage = new Stage();
			dialogStage.setTitle("Действие");
			dialogStage.initModality(Modality.WINDOW_MODAL);
			dialogStage.initOwner(primaryStage);
			dialogStage.setMaximized(false);
			Scene scene = new Scene(editOverview);
			dialogStage.setScene(scene);

			// Set the company into the controller.
            AddeditActionDialogController controller = loader.getController();
			controller.setMainApp(this);
			controller.setDialogStage(dialogStage);
			controller.setData(step, chain);
			

			// Show the dialog and wait until the user closes it
			dialogStage.showAndWait();

			//return controller.isOkClicked();

        } catch (IOException e) {
            e.printStackTrace();
        }

	}
	
	public void showFileTypeDialog(FileType type, Integer reportId){
        try {
             FXMLLoader loader = new FXMLLoader();
            loader.setLocation(MainApp.class.getResource("view/FileTypeDialog.fxml"));
            AnchorPane editOverview = (AnchorPane) loader.load();

			// Create the dialog Stage.
            Stage dialogStage = new Stage();
			dialogStage.setTitle("Редактирование типа файла");
			dialogStage.initModality(Modality.WINDOW_MODAL);
			dialogStage.initOwner(primaryStage);
			dialogStage.setMaximized(false);
			Scene scene = new Scene(editOverview);
			dialogStage.setScene(scene);

			// Set the company into the controller.
            FileTypeDialogController controller = loader.getController();
			controller.setMainApp(this);
			controller.setDialogStage(dialogStage);
			controller.setData(type, reportId);
			

			// Show the dialog and wait until the user closes it
			dialogStage.showAndWait();

			//return controller.isOkClicked();

        } catch (IOException e) {
            e.printStackTrace();
        }

	}


	public void showChainDialog(Chain chain){
        try {
             FXMLLoader loader = new FXMLLoader();
            loader.setLocation(MainApp.class.getResource("view/AddeditChainDialog.fxml"));
            AnchorPane editOverview = (AnchorPane) loader.load();

			// Create the dialog Stage.
            Stage dialogStage = new Stage();
			dialogStage.setTitle("Последовательность команд");
			dialogStage.initModality(Modality.WINDOW_MODAL);
			dialogStage.initOwner(primaryStage);
			dialogStage.setMaximized(false);
			Scene scene = new Scene(editOverview);
			dialogStage.setScene(scene);

			// Set the company into the controller.
            AddEditChainDialogController controller = loader.getController();
			controller.setMainApp(this);
			controller.setDialogStage(dialogStage);
			controller.setData(chain);
			

			// Show the dialog and wait until the user closes it
			dialogStage.showAndWait();

			//return controller.isOkClicked();

        } catch (IOException e) {
            e.printStackTrace();
        }

	}

	
	public void showArchive(){
		try{
			FXMLLoader loader = new FXMLLoader();
			loader.setLocation(MainApp.class.getResource("view/ArchiveOverview.fxml"));
			AnchorPane archiveOverview = (AnchorPane) loader.load();
			Stage dialogStage = new Stage();
			
			// Create the dialog Stage.
			dialogStage.setTitle("Архив сообщений");
			dialogStage.initModality(Modality.WINDOW_MODAL);
			dialogStage.initOwner(primaryStage);
			dialogStage.setMaximized(true);
			Scene scene = new Scene(archiveOverview);
			dialogStage.setScene(scene);

			// Set the company into the controller.
            ArchiveOverviewController controller = loader.getController();
			controller.setMainApp(this);
			controller.setReports(dao.getReports());
			controller.setData(dao.getArchiveFiles());
			controller.setDialogStage(dialogStage);
			

			// Show the dialog and wait until the user closes it
			dialogStage.showAndWait();

			//return controller.isOkClicked();

		} catch (IOException e) {
            e.printStackTrace();
        }
	}
	
	public void showAddReportDialog(){
		try{
			FXMLLoader loader = new FXMLLoader();
			loader.setLocation(MainApp.class.getResource("view/AddReport.fxml"));
			AnchorPane reportDialog = (AnchorPane) loader.load();
			Stage dialogStage = new Stage();
			
			// Create the dialog Stage.
			dialogStage.setTitle("Добавление отчета");
			dialogStage.initModality(Modality.WINDOW_MODAL);
			dialogStage.initOwner(primaryStage);
			dialogStage.setMaximized(true);
			Scene scene = new Scene(reportDialog);
			dialogStage.setScene(scene);

			// Set the company into the controller.
            AddReportController controller = loader.getController();
			controller.setMainApp(this);
			//controller.setData(dao.getReportById(1));
			controller.setDialogStage(dialogStage);
			

			// Show the dialog and wait until the user closes it
			dialogStage.showAndWait();

			//return controller.isOkClicked();

		} catch (IOException e) {
            e.printStackTrace();
        }
		
	}
	
	public void showAttributeDialog(){
		try{
			FXMLLoader loader = new FXMLLoader();
			loader.setLocation(MainApp.class.getResource("view/AttributesEdit.fxml"));
			AnchorPane reportDialog = (AnchorPane) loader.load();
			Stage dialogStage = new Stage();
			
			// Create the dialog Stage.
			dialogStage.setTitle("Атрибуты");
			dialogStage.initModality(Modality.WINDOW_MODAL);
			dialogStage.initOwner(primaryStage);
			dialogStage.setMaximized(true);
			Scene scene = new Scene(reportDialog);
			dialogStage.setScene(scene);

			// Set the company into the controller.
            AttributeEditController controller = loader.getController();
			controller.setMainApp(this);
			controller.setData();
			controller.setDialogStage(dialogStage);

			// Show the dialog and wait until the user closes it
			dialogStage.showAndWait();

			//return controller.isOkClicked();

		} catch (IOException e) {
            e.printStackTrace();
        }
		
	}
	
	public void showKeyDialog(){
		try{
			FXMLLoader loader = new FXMLLoader();
			loader.setLocation(MainApp.class.getResource("view/KeyView.fxml"));
			AnchorPane reportDialog = (AnchorPane) loader.load();
			Stage dialogStage = new Stage();
			
			// Create the dialog Stage.
			dialogStage.setTitle("Ключи");
			dialogStage.initModality(Modality.WINDOW_MODAL);
			dialogStage.initOwner(primaryStage);
			dialogStage.setMaximized(true);
			Scene scene = new Scene(reportDialog);
			dialogStage.setScene(scene);

			// Set the company into the controller.
            KeyViewController controller = loader.getController();
			controller.setMainApp(this);
			controller.setData();
			controller.setDialogStage(dialogStage);

			// Show the dialog and wait until the user closes it
			dialogStage.showAndWait();

			//return controller.isOkClicked();

		} catch (IOException e) {
            e.printStackTrace();
        }
		
	}
	
	public void showActionDialog(){
		try{
			FXMLLoader loader = new FXMLLoader();
			loader.setLocation(MainApp.class.getResource("view/ActionsView.fxml"));
			AnchorPane reportDialog = (AnchorPane) loader.load();
			Stage dialogStage = new Stage();
			
			// Create the dialog Stage.
			dialogStage.setTitle("Шаги");
			dialogStage.initModality(Modality.WINDOW_MODAL);
			dialogStage.initOwner(primaryStage);
			dialogStage.setMaximized(true);
			Scene scene = new Scene(reportDialog);
			dialogStage.setScene(scene);

			// Set the company into the controller.
            ActionViewController controller = loader.getController();
			controller.setMainApp(this);
			controller.setData();
			controller.setDialogStage(dialogStage);

			// Show the dialog and wait until the user closes it
			dialogStage.showAndWait();

			//return controller.isOkClicked();

		} catch (IOException e) {
            e.printStackTrace();
        }
		
	}
	
	public Report showReportChooserDialog(){
		try{
			FXMLLoader loader = new FXMLLoader();
			loader.setLocation(MainApp.class.getResource("view/ReportChooserDialog.fxml"));
			AnchorPane reportDialog = (AnchorPane) loader.load();
			Stage dialogStage = new Stage();
			
			// Create the dialog Stage.
			dialogStage.setTitle("Шаги");
			dialogStage.initModality(Modality.WINDOW_MODAL);
			dialogStage.initOwner(primaryStage);
			dialogStage.setMaximized(false);
			Scene scene = new Scene(reportDialog);
			dialogStage.setScene(scene);

			// Set the company into the controller.
            ReportChooserDialogController controller = loader.getController();
			controller.setMainApp(this);
			controller.setData(getDb().getReports());
			controller.setDialogStage(dialogStage);

			// Show the dialog and wait until the user closes it
			dialogStage.showAndWait();

			return currentReport;

		} catch (IOException e) {
            e.printStackTrace();
        }
		return null;
	}

	public void showAttributeSettingsDialog(){
		try{
			FXMLLoader loader = new FXMLLoader();
			loader.setLocation(MainApp.class.getResource("view/AttributeLocationDialog.fxml"));
			AnchorPane reportDialog = (AnchorPane) loader.load();
			Stage dialogStage = new Stage();
			
			// Create the dialog Stage.
			dialogStage.setTitle("Настройка атрибутов");
			dialogStage.initModality(Modality.WINDOW_MODAL);
			dialogStage.initOwner(primaryStage);
			dialogStage.setMaximized(true);
			Scene scene = new Scene(reportDialog);
			dialogStage.setScene(scene);

			// Set the company into the controller.
            AttributeLocationDialogController controller = loader.getController();
			controller.setMainApp(this);
			controller.setData();
			controller.setDialogStage(dialogStage);

			// Show the dialog and wait until the user closes it
			dialogStage.showAndWait();

			//return controller.isOkClicked();

		} catch (IOException e) {
            e.printStackTrace();
        }
		
	}
	
	public void showSettingsDialog(){
		try{
			FXMLLoader loader = new FXMLLoader();
			loader.setLocation(MainApp.class.getResource("view/SettingsDialog.fxml"));
			AnchorPane reportDialog = (AnchorPane) loader.load();
			Stage dialogStage = new Stage();
			
			// Create the dialog Stage.
			dialogStage.setTitle("Настройка констант");
			dialogStage.initModality(Modality.WINDOW_MODAL);
			dialogStage.initOwner(primaryStage);
			dialogStage.setMaximized(true);
			Scene scene = new Scene(reportDialog);
			dialogStage.setScene(scene);

			// Set the company into the controller.
           SettingsDialogController controller = loader.getController();
			controller.setMainApp(this);
			controller.setData();
			controller.setDialogStage(dialogStage);

			// Show the dialog and wait until the user closes it
			dialogStage.showAndWait();

			//return controller.isOkClicked();

		} catch (IOException e) {
            e.printStackTrace();
        }
		
	}

	
	public void showAttributeSettingsEditDialog(AttributeDescr attr){
		try{
			FXMLLoader loader = new FXMLLoader();
			loader.setLocation(MainApp.class.getResource("view/AddEditAttributeDescr.fxml"));
			AnchorPane reportDialog = (AnchorPane) loader.load();
			Stage dialogStage = new Stage();
			
			// Create the dialog Stage.
			dialogStage.setTitle("Настройка атрибутов");
			dialogStage.initModality(Modality.WINDOW_MODAL);
			dialogStage.initOwner(primaryStage);
			dialogStage.setMaximized(false);
			dialogStage.setResizable(true);
			dialogStage.setMaxHeight(130f);
			dialogStage.setWidth(1000f);
			
			Scene scene = new Scene(reportDialog);
			dialogStage.setScene(scene);

			// Set the company into the controller.
            AddEditAttributeDescrController controller = loader.getController();
			controller.setMainApp(this);
			controller.setData(attr);
			controller.setDialogStage(dialogStage);

			// Show the dialog and wait until the user closes it
			dialogStage.showAndWait();

			//return controller.isOkClicked();

		} catch (IOException e) {
            e.printStackTrace();
        }
		
	}

	public void showSingleAction(){
		try{
			FXMLLoader loader = new FXMLLoader();
			loader.setLocation(MainApp.class.getResource("view/SingleAction.fxml"));
			AnchorPane simpleAction = (AnchorPane) loader.load();
			Stage dialogStage = new Stage();
			
			// Create the dialog Stage.
			dialogStage.setTitle("Базовые функции");
			dialogStage.initModality(Modality.WINDOW_MODAL);
			dialogStage.initOwner(primaryStage);
			dialogStage.setMaximized(true);
			Scene scene = new Scene(simpleAction);
			dialogStage.setScene(scene);

			// Set the company into the controller.
            SingleActionController controller = loader.getController();
			controller.setMainApp(this);
			controller.setDialogStage(dialogStage);
			

			// Show the dialog and wait until the user closes it
			dialogStage.showAndWait();

			//return controller.isOkClicked();

		} catch (IOException e) {
            e.printStackTrace();
        }
	}
	
	public void showChooseKeyDialog(){
        try {
             FXMLLoader loader = new FXMLLoader();
            loader.setLocation(MainApp.class.getResource("view/ChooseKeyDialog.fxml"));
            AnchorPane editOverview = (AnchorPane) loader.load();

			// Create the dialog Stage.
            Stage dialogStage = new Stage();
			dialogStage.setTitle("Выберите ключ");
			dialogStage.initModality(Modality.WINDOW_MODAL);
			dialogStage.initOwner(primaryStage);
			dialogStage.setMaximized(false);
			dialogStage.setResizable(false);
			Scene scene = new Scene(editOverview);
			dialogStage.setScene(scene);

			// Set the company into the controller.
            ChooseKeyDialogController controller = loader.getController();
			controller.setMainApp(this);
			controller.setDialogStage(dialogStage);
			controller.setKeys(dao.getKeys());
			

			// Show the dialog and wait until the user closes it
			dialogStage.showAndWait();

			//return controller.isOkClicked();

        } catch (IOException e) {
            e.printStackTrace();
        }

	}

	private Key currentKey;
	
    public Stage getPrimaryStage() {
        return primaryStage;
    }

    public Report getCurrentReport(){
    	return currentReport;
    }
    
    public void setCurrentReport(Report report){
    	this.currentReport = report;
    }

	public Key getCurrentKey() {
		return currentKey;
	}

	public void setCurrentKey(Key currentKey) {
		this.currentKey = currentKey;
	}
}
