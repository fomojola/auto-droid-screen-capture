package com.ribomation.droidAtScreen.gui;

import com.ribomation.droidAtScreen.Application;
import com.ribomation.droidAtScreen.cmd.Command;
import com.ribomation.droidAtScreen.cmd.QuitCommand;
import com.ribomation.droidAtScreen.dev.AndroidDevice;
import com.ribomation.droidAtScreen.dev.AndroidDeviceListener;
import org.apache.log4j.Logger;

import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

/**
 * DESCRIPTION
 *
 * @user jens
 * @date 2010-jan-18 17:44:12
 */
public class ApplicationFrame extends JFrame {
    private Logger                  log = Logger.getLogger(ApplicationFrame.class);
    private Application             application;
    private DefaultComboBoxModel    deviceListModel = new DefaultComboBoxModel();

    public ApplicationFrame() throws HeadlessException {
        super("Droid@Screen, version 0.1");
    }

    public ApplicationFrame(Application application) throws HeadlessException {
        this.application = application;
    }

    public Application getApplication() {
        if (application == null) {
            throw new IllegalStateException("Missing application ref. Must invoke setApplication(...) before use.");
        }
        return application;
    }

    public void setApplication(Application application) {
        this.application = application;
    }

    public void  initGUI() {
        setTitle(getApplication().getName()+", Version "+getApplication().getVersion());
        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        this.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                Command.find(QuitCommand.class).execute();
            }
        });
        
        setJMenuBar( createMenubar() );
        add( createDeviceControlPane() );
        pack();
    }

    public void placeInCenterScreen() {
        placeInCenterScreen(this);
    }

    public void  placeInUpperLeftScreen() {
        placeInUpperLeftScreen(this);
    }

    public static void placeInCenterScreen(Window win) {
        Dimension screen = Toolkit.getDefaultToolkit().getScreenSize();
        Dimension frame  = win.getSize();
        win.setLocation((screen.width - frame.width)/2, (screen.height - frame.height)/2);
    }

    public static void placeInUpperLeftScreen(Window win) {
        Dimension screen = Toolkit.getDefaultToolkit().getScreenSize();
        Dimension frame  = win.getSize();
        win.setLocation(screen.width/4 - frame.width/2, screen.height/4 - frame.height/2);
    }

    private JPanel createDeviceControlPane() {
        JPanel p = new JPanel(new GridLayout(1, 1, 0, 5));

        p.add(createDevicesList());

        return p;
    }

    private JPanel createDevicesList() {
        JComboBox devices = new JComboBox(deviceListModel);
        devices.setPreferredSize(new Dimension(200, 20));

        getApplication().addAndroidDeviceListener(new AndroidDeviceListener() {
            @Override
            public void connected(AndroidDevice dev) {
                log.debug("[devicesBox] connected: dev=" + dev);
                deviceListModel.addElement(dev.getName());
                deviceListModel.setSelectedItem(dev.getName());
            }

            @Override
            public void disconnected(AndroidDevice dev) {
                log.debug("[devicesBox] disconnected: dev=" + dev);
                deviceListModel.removeElement( dev.getName() );
            }
        });

        JPanel p = new JPanel(new FlowLayout(FlowLayout.LEFT));
        p.setBorder( BorderFactory.createTitledBorder("Devices") );
        p.add(devices);
        p.add( Command.get("Show").createButton() );
//        p.add( Command.get("Portrait").createButton() );

        return p;
    }

    public ComboBoxModel getDeviceList() {
        return deviceListModel;
    }

    protected JMenuBar createMenubar() {
        JMenuBar     mb = new JMenuBar();

        mb.add(createFileMenu());
        mb.add(createViewMenu());
        mb.add(createOptionsMenu());
        mb.add(createHelpMenu());

        return mb;
    }

    protected JMenu createFileMenu() {
        return createMenu("File", 'F',
                "ScreenShot", "-", "Quit");
    }

    protected JMenu createViewMenu() {
        return createMenu("View", 'V',
                "Portrait", "Scale");
    }

    protected JMenu createOptionsMenu() {
        return createMenu("Options", 'O',
                "AdbExePath", "-", "AutoShow", "SkipEmulator", "FrameRate", "-", "LookAndFeel", "-", "RemoveProperties");
    }

    protected JMenu createHelpMenu() {
        return createMenu("Help", 'H',
                "About");
    }

    public JMenu createMenu(String name, char mnemonicChar, String... commandNames) {
        JMenu   m = new JMenu(name);
        m.setMnemonic(mnemonicChar);

        for (String cmdName : commandNames) {
            if (cmdName.equals("-")) {
                m.addSeparator();
            } else {
                m.add( Command.get(cmdName).createMenuItem() );
            }
        }

        return m;
    }

}
