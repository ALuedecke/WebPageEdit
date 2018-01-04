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

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
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

    private String char_code;
    private String default_path;
    private String ftp_port;
    private String ftp_protocol;
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

    public String getChar_code() {
        return char_code;
    }

    public String getDefault_path() {
        return default_path;
    }

    public String getFtp_port() {
        return ftp_port;
    }
    
    public String getFtp_protocol() {
        return ftp_protocol;
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

    // Public methods
    
    public String saveConfigFile() {
        String out_msg = "   ... Configuration saved successfully to \"" + config_file + "\".";
        
        // Creating json
        JSONArray  cfg_out  = new JSONArray();
        JSONObject config   = new JSONObject();
        
        config.put("char_code",    char_code);
        config.put("default_path", default_path);
        config.put("ftp_port",     ftp_port);
        config.put("ftp_protocol", ftp_protocol);
        config.put("fpt_server",   fpt_server);
        config.put("ftp_user",     ftp_user);
        config.put("ftp_password", ftp_password);
        
        cfg_out.add(config);
        
        // Saving the json to file
        try {
            BufferedWriter writer;
            File target;
            FileOutputStream out;
            String text = cfg_out.toString();
            
            target = new File(config_file);
            out = new FileOutputStream(target);
            writer = new BufferedWriter(new OutputStreamWriter(out, getChar_code()));
            
            writer.write(text);
            writer.flush();
            writer.close();
            out.flush();
            out.close();
        } catch (FileNotFoundException | UnsupportedEncodingException ex) {
            out_msg = "  ... " + ex.getMessage();
            Logger.getLogger(Config.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            out_msg = "  ... " + ex.getMessage();
            Logger.getLogger(Config.class.getName()).log(Level.SEVERE, null, ex);
        }

        return out_msg;
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
            
            char_code    = (String) config.get("char_code");
            default_path = (String) config.get("default_path");
            ftp_port     = (String) config.get("ftp_port");
            ftp_protocol = (String) config.get("ftp_protocol");
            fpt_server   = (String) config.get("fpt_server");
            ftp_user     = (String) config.get("ftp_user");
            ftp_password = (String) config.get("ftp_password");
        } catch (IOException | ParseException ex) {
            Logger.getLogger(Config.class.getName()).log(Level.SEVERE, null, ex);
        }      
    }
}
