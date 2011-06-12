package com.ribomation.droidAtScreen.cmd;

import com.ribomation.droidAtScreen.Application;

import javax.swing.*;
import java.util.prefs.BackingStoreException;

/**
 * DESCRIPTION
 *
 * @user jens
 * @date 2010-jan-18 10:35:20
 */
public class RemovePropertiesCommand extends Command {
    public RemovePropertiesCommand() {
        setLabel("Remove application properties");
        setTooltip("Removes all saved application properties from this host.");
//        setIcon("quit_16x16");
//        setMnemonic('Q');
    }

    @Override
    protected void doExecute(Application app) {
        int rc = JOptionPane.showConfirmDialog(app.getAppFrame(),
                "Are you sure you want to remove all persisted properties of this application from this host?",
                "Remove app properties?",
                JOptionPane.YES_NO_OPTION
        );
        if (rc == JOptionPane.YES_OPTION) {
            app.destroyPreferences();
        }
    }
}
