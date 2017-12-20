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

import java.util.Optional;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.ButtonType;

/**
 *
 * @author LuedeckeA
 */
public class ConfirmDlg {
    // Dialog members
    Alert      alert  = new Alert(AlertType.CONFIRMATION);
    ButtonType btnYes = new ButtonType("Ja");
    ButtonType btnNo  = new ButtonType("Nein");

    // Constructor
    public ConfirmDlg(String title, String text) {
        alert.setTitle(title);
        alert.setHeaderText(text);
        alert.setContentText(null);
        alert.getButtonTypes().setAll(btnYes, btnNo);
    }

    // Getters
    public ButtonType getBtnYes() {    
        return btnYes;
    }

    public ButtonType getBtnNo() {    
        return btnNo;
    }

    // Public methods
    public Optional<ButtonType> show() {
        return alert.showAndWait();
    }
}