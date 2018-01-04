/*
 * Copyright (C) 2018 LuedeckeA
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

import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.layout.GridPane;

/**
 *
 * @author LuedeckeA
 */
public class ConfigDlg {
    // Dialog members
    ButtonType btnSave        = new ButtonType("Speichern");
    ButtonType btnCancel      = new ButtonType("Abbrechen");
    Dialog     dialog         = new Dialog();
    GridPane   grid           = new GridPane();
    Label      lblCharCode    = new Label("Zeichen Codierung:");
    Label      lblDefPath     = new Label("Standard Ordner:");
    Label      lblFtpPort     = new Label("FTP Port:");
    Label      lblFtpProtocol = new Label("FTP Protokoll:");
    Label      lblFtpServer   = new Label("FTP Server:");
    Label      lblFtpUser     = new Label("Benutzername:");
    Label      lblFtpPassword = new Label("Passwort:");
}
