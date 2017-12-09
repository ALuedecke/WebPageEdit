/*
 * Copyright (C) 2016 LuedeckeA
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

import com.jcraft.jsch.*;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.scene.control.Label;
import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPClient;

/**
 *
 * @author LuedeckeA
 */
public class FileIO {
    // Private members
    private final Config config = new Config();
    private Label output = new Label();
    
    // Getters

    public Config getConfig() {
        return config;
    }
    
    // Setters
    
    public void setOutput(Label output) {    
        this.output = output;
    }

    // Public methods
    public String openFile(String file_name) throws FileNotFoundException, IOException {
        boolean first_line = true;
        FileInputStream in = new FileInputStream(file_name);
        BufferedReader reader = new BufferedReader(new InputStreamReader(in));
        StringBuilder out = new StringBuilder();
        String line;

        while ((line = reader.readLine()) != null) {
            if (first_line) {
                out.append(line);
                first_line = false;
            } else {
                out.append("\n").append(line);
            }
        }

        return out.toString();
    }

    public void saveFile(String file_name, String content) {
        BufferedWriter writer;
        File target;
        FileOutputStream out;
        String text = content.replaceAll(" contenteditable=\"true\"", "");
        
        try {
            target = new File(file_name);
            out = new FileOutputStream(target);
            writer = new BufferedWriter(new OutputStreamWriter(out));

            writer.write(text);
            writer.flush();
            writer.close();
            out.flush();
            out.close();
        } catch (FileNotFoundException ex) {
            Logger.getLogger(FileIO.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(FileIO.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    public void uploadFileFTP(String file_name) {
        boolean complete = false;
        int port = Integer.parseInt(config.getFtp_port());
        FTPClient ftpClient = new FTPClient();
        String server = config.getFpt_server(); //"www.kleinhunde-berlin.com";
        String user = config.getFtp_user(); //"ftp_andreas@kleinhunde-berlin.com";
        String pass =  config.getFtp_password(); //"Alien!001";
        String up_name =  file_name.substring(file_name.lastIndexOf("\\") + 1);
        
      
        try {
            ftpClient.connect(server, port);
            ftpClient.login(user, pass);
            ftpClient.enterLocalPassiveMode();
            ftpClient.setFileType(FTP.ASCII_FILE_TYPE);
            ftpClient.setFileTransferMode(FTP.ASCII_FILE_TYPE);

            FileInputStream uploadFile = new FileInputStream(file_name);
            
            if (ftpClient.isConnected()) {
                complete = ftpClient.storeFile(up_name, uploadFile);
            }
            
            if (complete) {
                System.out.print("Upload complete.");
                output.setText("Upload complete.");
                uploadFile.close();
            }
        } catch (IOException ex) {
            Logger.getLogger(FileIO.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                if (ftpClient.isConnected()) {
                    ftpClient.logout();
                    ftpClient.disconnect();
                }
            } catch (IOException ex) {
                Logger.getLogger(FileIO.class.getName()).log(Level.SEVERE, null, ex);
            }
        }        
    }
    
    public void uploadFileSFTP(String file_name) {
        int port = Integer.parseInt(config.getFtp_port());
        JSch jsch = new JSch();
        String server = config.getFpt_server();
        String user = config.getFtp_user();
        String pass =  config.getFtp_password();
        String up_name =  file_name.substring(file_name.lastIndexOf("\\") + 1);
        
        try {
            Properties prop = new Properties();
            prop.put("StrictHostKeyChecking", "no");

            Session session = jsch.getSession(user, server, port);
            session.setPassword(pass);
            session.setConfig(prop);
            session.connect();
            
            Channel channel = session.openChannel("sftp");
            channel.connect();
            ChannelSftp channelSftp = (ChannelSftp) channel;
            
            FileInputStream uploadFile = new FileInputStream(file_name);
            channelSftp.put(uploadFile, up_name, ChannelSftp.OVERWRITE);
            output.setText("Upload complete.");

            channelSftp.exit();
            session.disconnect();
        } catch (JSchException ex) {
            Logger.getLogger(FileIO.class.getName()).log(Level.SEVERE, null, ex);
        } catch (FileNotFoundException ex) {
            Logger.getLogger(FileIO.class.getName()).log(Level.SEVERE, null, ex);
        } catch (SftpException ex) {
            Logger.getLogger(FileIO.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}
