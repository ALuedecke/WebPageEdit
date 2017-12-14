/*
 * Copyright (C) 2017 LuedeckeA
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package webpageedit;

import java.beans.PropertyChangeEvent;
import java.io.File;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.application.Application;
import javafx.concurrent.Task;
import javafx.concurrent.Worker.State;
import javafx.event.ActionEvent;
import javafx.geometry.Insets;
import javafx.scene.Cursor;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.input.InputEvent;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.CornerRadii;
import javafx.scene.paint.Color;
import javafx.scene.web.HTMLEditor;
import javafx.stage.Stage;
import javax.swing.JFileChooser;
import javax.swing.filechooser.FileFilter;
/**
 *
 * @author LuedeckeA
 */
public class WebPageEdit extends Application {
    // GUI members
    private final Button     btnClose = new Button();
    private final Button     btnOpen = new Button();
    private final Button     btnUpload = new Button();
    private final Button     btnSave = new Button();
    private final Label      lblFile = new Label();
    private final Label      lblOut = new Label();
    private final Label      lblUpload = new Label();
    private final TextField  txtFile = new TextField();
    private final HTMLEditor html = new HTMLEditor();
    private final Group      root = new Group();
    
    // Display member
    private Scene scene;
    
    // IO members
    private final FileIO htmlFile = new FileIO();
    
    // private methods
    private String chooseFilePath(int dlg_type, int sel_mode, String ini_path, FileFilter filter) {
        String name ="";
        
        final JFileChooser chooser = new JFileChooser(); 
        final File path; 

        path = new File(ini_path);
        chooser.setDialogType(dlg_type); 
        chooser.setFileSelectionMode(sel_mode);
        chooser.setCurrentDirectory(path); 

        chooser.addPropertyChangeListener((PropertyChangeEvent e) -> {
            if (e.getPropertyName().equals(JFileChooser.SELECTED_FILE_CHANGED_PROPERTY)
                || e.getPropertyName().equals(JFileChooser.DIRECTORY_CHANGED_PROPERTY)) {
                final File f = (File) e.getNewValue();
            }
        });
        
        try {
            chooser.addChoosableFileFilter(filter);
            chooser.setAcceptAllFileFilterUsed(false);
        } catch (NullPointerException ex) {
            Logger.getLogger(WebPageEdit.class.getName()).log(Level.INFO, "no FileFilter submitted");
        }

        chooser.setVisible(true); 
        final int result = chooser.showOpenDialog(null); 

        if (result == JFileChooser.APPROVE_OPTION) { 
            name = chooser.getSelectedFile().getPath(); 
        } 
        
        chooser.setVisible(false);
        
        return name;
    }

    private void handleBtnOpen() {
        final String ini_path; 
        final String name;

        ini_path = this.txtFile.getText();

        name = chooseFilePath(
                   JFileChooser.OPEN_DIALOG
                  ,JFileChooser.FILES_ONLY
                  ,ini_path
                  ,new FileFilter() {
                      @Override
                      public String getDescription() {
                          return "Web Documents (*.html)";
                      }
                      @Override
                      public boolean accept(File f) {
                          return (
                              f.getName().toLowerCase().endsWith(".html") ||
                              f.isDirectory()
                          );
                      }
                   }
               );        
        
        if(!name.equals("")) {
            txtFile.setText(name);
            try {
                html.setHtmlText(htmlFile.openFile(name));
            } catch (IOException ex) {
                Logger.getLogger(WebPageEdit.class.getName()).log(Level.INFO, null, ex);
            }
        }
    }
    
    private void handleBtnSave() {
        htmlFile.saveFile(txtFile.getText(), html.getHtmlText());
        btnUpload.setDisable(false);
        lblOut.textProperty().unbind();
        lblOut.setText("");
    }
    
    private void handleBtnUpload() {
        Task task = new Task() {
            @Override
            protected String call() throws Exception {
                String msg;
                
                scene.setCursor(Cursor.WAIT); //Change cursor to wait style
                if (htmlFile.getConfig().getFtp_port().equals("22") || htmlFile.getConfig().getFtp_protocol().equals("SFTP")) {
                    msg = htmlFile.uploadFileSFTP(txtFile.getText());
                } else {
                    msg = htmlFile.uploadFileFTP(txtFile.getText());
                }
                updateMessage(msg);
                scene.setCursor(Cursor.DEFAULT); //Change cursor to default style
                return msg;
            }
        };
        lblOut.textProperty().bind(task.messageProperty());
        new Thread(task).start();
        
        while (task.getState() == State.RUNNING) {
            //do nothing
        }
        if (task.getState() != State.RUNNING) {
            if (htmlFile.getError_msg().equals("")) {
                lblOut.setTextFill(Color.GREEN);
            } else {
                lblOut.setTextFill(Color.RED);
            }
        }
    }
    
    private void initGui() {
        btnClose.setLayoutX(1210);
        btnClose.setLayoutY(730);
        btnClose.setText("Beenden");
        btnClose.setOnAction((ActionEvent event) -> {
            System.exit(0);
        });

        btnOpen.setLayoutX(515);
        btnOpen.setLayoutY(10);
        btnOpen.setText("...");
        btnOpen.setOnAction((ActionEvent event) -> {
            handleBtnOpen();
        });

        btnSave.setLayoutX(10);
        btnSave.setLayoutY(730);
        btnSave.setText("Speichern");
        btnSave.setOnAction((ActionEvent event) -> {
            handleBtnSave();
        });

        btnUpload.setLayoutX(90);
        btnUpload.setLayoutY(730);
        btnUpload.setText("Upload");
        btnUpload.setDisable(true);
        btnUpload.setOnAction((ActionEvent event) -> {
            handleBtnUpload();
        });

        lblFile.setLayoutX(10);
        lblFile.setLayoutY(13);
        lblFile.setText("Lokale Datei:");
        
        lblOut.setBackground(new Background(new BackgroundFill(Color.BLACK, CornerRadii.EMPTY, Insets.EMPTY)));
        lblOut.setLayoutX(565);
        lblOut.setLayoutY(733);
        lblOut.setPrefWidth(635);
        lblOut.setTextFill(Color.GREEN);
        
        lblUpload.setBackground(new Background(new BackgroundFill(Color.BLACK, CornerRadii.EMPTY, Insets.EMPTY)));
        lblUpload.setLayoutX(155);
        lblUpload.setLayoutY(733);
        lblUpload.setPrefWidth(400);
        lblUpload.setText(
            htmlFile.getConfig().getFtp_protocol() +
            "://" +  htmlFile.getConfig().getFtp_user() +
            "@" + htmlFile.getConfig().getFpt_server() + 
            ":" + htmlFile.getConfig().getFtp_port()
        );
        lblUpload.setTextFill(Color.WHITE);

        txtFile.setLayoutX(80);
        txtFile.setLayoutY(10);
        txtFile.setPrefWidth(425);
        txtFile.setText(htmlFile.getConfig().getDefault_path());
        
        html.setLayoutX(10);
        html.setLayoutY(50);
        html.setPrefWidth(1260);
        html.addEventHandler(InputEvent.ANY, (InputEvent event) -> {
            lblOut.textProperty().unbind();
            lblOut.setText("");
            btnUpload.setDisable(true);
        });
        
        try {
            html.setHtmlText(htmlFile.openFile(txtFile.getText()));
        } catch (IOException ex) {
            Logger.getLogger(WebPageEdit.class.getName()).log(Level.SEVERE, null, ex);
        }

        root.getChildren().add(html);
        root.getChildren().add(lblFile);
        root.getChildren().add(txtFile);
        root.getChildren().add(btnOpen);
        root.getChildren().add(btnSave);
        root.getChildren().add(btnUpload);
        root.getChildren().add(lblUpload);
        root.getChildren().add(lblOut);
        root.getChildren().add(btnClose);
    }
    
    @Override
    public void start(Stage primaryStage) {
        initGui();

        scene = new Scene(root, 1280, 768, Color.LIGHTGREY);
        primaryStage.setTitle("WebPage Editor");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        launch(args);
    }
    
}
