package io.github.defective4.sdr.owrxdesktop.ui;

import java.awt.BorderLayout;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.imageio.ImageIO;
import javax.swing.BoxLayout;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.KeyStroke;

import io.github.defective4.sdr.owrxdesktop.ui.component.ReceiverEntryComponent;
import io.github.defective4.sdr.owrxdesktop.ui.rx.ReceiverEntry;

public class ApplicationWindow extends JFrame {

    private final BufferedImage rxPlaceholder;
    private final ExecutorService updateExecutor = Executors.newFixedThreadPool(1);

    public ApplicationWindow() {
        try (InputStream is = getClass().getResourceAsStream("/rx-null.png")) {
            rxPlaceholder = ImageIO.read(is);
        } catch (IOException e1) {
            throw new IllegalStateException(e1);
        }
        setBounds(100, 100, 768, 512);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        {
            JMenuBar menuBar = new JMenuBar();
            setJMenuBar(menuBar);

            JMenu mnApplication = new JMenu("Application");
            menuBar.add(mnApplication);

            JMenuItem mntmQuit = new JMenuItem("Quit");
            mntmQuit.addActionListener(e -> dispose());
            mntmQuit.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_Q, InputEvent.CTRL_DOWN_MASK));
            mnApplication.add(mntmQuit);
        }

        JTabbedPane tabbedPane = new JTabbedPane(JTabbedPane.TOP);
        getContentPane().add(tabbedPane, BorderLayout.CENTER);

        {
            JScrollPane scrollPane = new JScrollPane();
            tabbedPane.addTab("Personal", null, scrollPane, null);

            {
                JPanel rxContainer = new JPanel();
                scrollPane.setViewportView(rxContainer);
                rxContainer.setLayout(new BoxLayout(rxContainer, BoxLayout.Y_AXIS));

                ReceiverEntry entry = new ReceiverEntry("https://radio.raspberry.local/");
                ReceiverEntryComponent component = new ReceiverEntryComponent(entry, rxPlaceholder);
                rxContainer.add(component);

                entry.setQuerying(true);
                updateExecutor.submit(() -> {
                    entry.query();
                    component.updateEntry();
                });
                component.updateEntry();
            }
        }
    }
}
