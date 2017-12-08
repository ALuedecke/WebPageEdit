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

import java.io.FileReader;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

/**
 *
 * @author LuedeckeA
 */
public class Config {

    // Variable members
    private String config_file = "config.json";

    private String ftp_port;
    private String default_path;
    private String fpt_server;    
    private String ftp_user;
    private String ftp_password;
    
    // Constructors
    
    public Config(String config_file) {
        this.config_file = config_file;
        setConfiguration();
    }
    
    public Config() {
        setConfiguration();
    }

    // Getters

    public String getConfig_file() {
        return config_file;
    }

    public String getFtp_port() {
        return ftp_port;
    }

    public String getDefault_path() {
        return default_path;
    }

    public String getFpt_server() {
        return fpt_server;
    }

    public String getFtp_user() {
        return ftp_user;
    }

    public String getFtp_password() {
        return ftp_password;
    }

    // Private methods
    
    private JSONObject loadConfigFile() throws IOException, ParseException {
        JSONParser parser = new JSONParser();
        
        Object obj = parser.parse(new FileReader(config_file));
        
        return (JSONObject) ((JSONArray) obj).get(0);
    }
    
    private void setConfiguration() {
        try {
            JSONObject config = loadConfigFile();
            
            this.ftp_port     = (String) config.get("ftp_port");
            this.default_path = (String)  config.get("default_path");
            this.fpt_server   = (String)  config.get("fpt_server");
            this.ftp_user     = (String)  config.get("ftp_user");
            this.ftp_password = (String)  config.get("ftp_password");
        } catch (IOException ex) {
            Logger.getLogger(Config.class.getName()).log(Level.INFO, "no Config file");
        } catch (ParseException ex) {
            Logger.getLogger(Config.class.getName()).log(Level.SEVERE, null, ex);
        }      
    }
}
