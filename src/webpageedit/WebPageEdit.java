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
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.application.Application;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.geometry.Insets;
import javafx.scene.Cursor;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyEvent;
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
    private final Button     btnDown = new Button();
    private final Button     btnOpen = new Button();
    private final Button     btnUpload = new Button();
    private final Button     btnSave = new Button();
    private final Label      lblCopyRight = new Label();
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

    private boolean confirmSvrLoad(String title, String text) {
        boolean confirm = false;
        Optional<ButtonType> dlg_result;
        ConfirmDlg dlg = new ConfirmDlg(
                                 title,
                                 text
                             );
        dlg_result = dlg.show();
            
        if (dlg_result.get() == dlg.getBtnYes()) {
            confirm = true;
        }
        
        return confirm;
    }
    
    private boolean discardChanges() {
        boolean discard = true;

        if (!btnSave.isDisabled()) {
            Optional<ButtonType> dlg_result;
            ConfirmDlg dlg = new ConfirmDlg(
                                     "Änderungen verwerfen",
                                     "Sollen die Änderungen verworfen werden?"
                                 );
            dlg_result = dlg.show();
            
            if (dlg_result.get() == dlg.getBtnNo()) {
                discard = false;
            }
        }
        
        return discard;
    }

    private void handleBtnClose() {
        if (!discardChanges()) {
            return;
        }
        if (!btnUpload.isDisabled()) {
            if (!confirmSvrLoad("Upload ausstehend", "Änderungen wurden nicht hochgeladen.\nTrotzdem beenden?")) {
                return;
            }
        }
        System.exit(0);
    }
    
    private void handleBtnDown() {
        String file_name = txtFile.getText();
        String up_name   = file_name.substring(file_name.lastIndexOf("\\") + 1);
        
        if (!confirmSvrLoad("Download", "Aktuelle Version \"" + up_name + "\" vom Server holen?")) {
            return;
        } else {
            html.setDisable(true);
        }
        
        Task task = new Task() {
            @Override
            protected String call() throws Exception {
                String msg;

                scene.setCursor(Cursor.WAIT); //Change cursor to wait style
                if (    htmlFile.getConfig().getFtp_port().equals("22")
                     || htmlFile.getConfig().getFtp_protocol().equals("SFTP")) {
                    msg = htmlFile.downloadFileSFTP(file_name);
                } else {
                    msg = htmlFile.downloadFileFTP(file_name);
                }
                updateMessage(msg);
                if (htmlFile.getError_msg().equals("")) {
                    lblOut.setTextFill(Color.LAWNGREEN);
                } else {
                    lblOut.setTextFill(Color.RED);
                }
                if (html.isDisabled()) {
                    html.setDisable(false);
                }
                scene.setCursor(Cursor.DEFAULT); //Change cursor to default style
                return msg;
            }
        };
        lblOut.textProperty().bind(task.messageProperty());
        new Thread(task).start();
    }
    
    private void handleBtnOpen() {
        if (!discardChanges()) {
            return;
        }
        if (!btnUpload.isDisabled()) {
            if (!confirmSvrLoad("Upload ausstehend", "Änderungen wurden nicht hochgeladen.\nTrotzdem fortfahren?")) {
                return;
            }
        }
        final String ini_path; 
        final String name;
        ini_path = txtFile.getText();
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
                setButtons(false, false, true);
            } catch (FileNotFoundException ex) {
                Logger.getLogger(WebPageEdit.class.getName()).log(Level.INFO, null, ex);
            } catch (IOException ex) {
                Logger.getLogger(WebPageEdit.class.getName()).log(Level.INFO, null, ex);
            }
        }
    }
    
    private void handleBtnSave() {
        try {
            htmlFile.saveFile(txtFile.getText(), html.getHtmlText());
            setButtons(false, true, true);
        } catch (IOException ex) {
            Logger.getLogger(WebPageEdit.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    private void handleBtnUpload() {
        String file_name = txtFile.getText();
        
        if (!confirmSvrLoad("Upload", "Änderungen in  \"" + file_name + "\" auf den Server hochladen?")) {
            return;
        } else {
            html.setDisable(true);
        }

        Task task;
        task = new Task() {
            @Override
            protected String call() throws Exception {
                String msg;
                
                scene.setCursor(Cursor.WAIT); //Change cursor to wait style
                if (    htmlFile.getConfig().getFtp_port().equals("22")
                     || htmlFile.getConfig().getFtp_protocol().equals("SFTP")) {
                    msg = htmlFile.uploadFileSFTP(file_name);
                } else {
                    msg = htmlFile.uploadFileFTP(file_name);
                }
                updateMessage(msg);
                if (htmlFile.getError_msg().equals("")) {
                    lblOut.setTextFill(Color.LAWNGREEN);
                    setButtons(false, false, false) ;
                } else {
                    lblOut.setTextFill(Color.RED);
                }
                if (html.isDisabled()) {
                    html.setDisable(false);
                }
                scene.setCursor(Cursor.DEFAULT); //Change cursor to default style
                return msg;
            }
        };
        lblOut.textProperty().bind(task.messageProperty());
        new Thread(task).start();
    }
    
    private void initGui() {
        btnClose.setLayoutX(1210);
        btnClose.setLayoutY(720);
        btnClose.setText("Beenden");
        btnClose.setOnAction((ActionEvent event) -> {
            handleBtnClose();
        });

        btnDown.setLayoutX(545);
        btnDown.setLayoutY(10);
        btnDown.setText(" Download from Server");
        btnDown.setOnAction((ActionEvent event) -> {
            handleBtnDown();
        });

        btnOpen.setLayoutX(515);
        btnOpen.setLayoutY(10);
        btnOpen.setText("...");
        btnOpen.setOnAction((ActionEvent event) -> {
            handleBtnOpen();
        });

        btnSave.setLayoutX(10);
        btnSave.setLayoutY(720);
        btnSave.setText("Speichern");
        btnSave.setOnAction((ActionEvent event) -> {
            handleBtnSave();
        });

        btnUpload.setLayoutX(90);
        btnUpload.setLayoutY(720);
        btnUpload.setText("Upload");
        btnUpload.setOnAction((ActionEvent event) -> {
            handleBtnUpload();
        });

        lblCopyRight.setLayoutX(10);
        lblCopyRight.setLayoutY(755);
        lblCopyRight.setStyle("-fx-font: normal 10px 'arial'");
        lblCopyRight.setText("Copyright (c)  A. Luedecke 12/2017");

        lblFile.setLayoutX(10);
        lblFile.setLayoutY(13);
        lblFile.setText("Lokale Datei:");
        
        lblOut.setBackground(new Background(new BackgroundFill(Color.BLACK, CornerRadii.EMPTY, Insets.EMPTY)));
        lblOut.setLayoutX(565);
        lblOut.setLayoutY(723);
        lblOut.setPrefWidth(635);
        lblOut.setTextFill(Color.LIGHTGREEN);
        
        lblUpload.setBackground(new Background(new BackgroundFill(Color.BLACK, CornerRadii.EMPTY, Insets.EMPTY)));
        lblUpload.setLayoutX(155);
        lblUpload.setLayoutY(723);
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
        html.setPrefHeight(660);
        html.setPrefWidth(1260);
        html.addEventHandler(KeyEvent.KEY_TYPED,  (KeyEvent event) -> {
            setButtons(true, false, true);
        });
        html.addEventHandler(KeyEvent.KEY_PRESSED,  (KeyEvent event) -> {
            setButtons(true, false, true);
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
        root.getChildren().add(btnDown);
        root.getChildren().add(btnSave);
        root.getChildren().add(btnUpload);
        root.getChildren().add(lblUpload);
        root.getChildren().add(lblOut);
        root.getChildren().add(btnClose);
        root.getChildren().add(lblCopyRight);
        
        setButtons(false, false, true);
    }
    
    private void setButtons(boolean save, boolean upload, boolean reset_output) {
        if (reset_output) {
            lblOut.textProperty().unbind();
            lblOut.setText("");
        }
        btnSave.setDisable(!save);
        btnUpload.setDisable(!upload);
    }
    
    @Override
    public void start(Stage primaryStage) {
        initGui();
        
        scene = new Scene(root, 1280, 768, Color.LIGHTGREY);
        
        primaryStage.setTitle("WebPage Editor - Version 1.0.1");
        primaryStage.setScene(scene);
        primaryStage.setResizable(false);
        primaryStage.show();
    }

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        launch(args);
    }
    
}