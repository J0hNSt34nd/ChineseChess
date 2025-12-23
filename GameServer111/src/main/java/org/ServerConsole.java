package org;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.text.SimpleDateFormat;
import java.util.Date;

public class ServerConsole extends JFrame {
    private final DefaultListModel<String> listModel;
    private final JTextArea logArea;
    private final JTextField portField;
    private final JButton startButton;
    private final JButton stopButton;

    private GameServer server;

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (Exception e) {
                e.printStackTrace();
            }
            new ServerConsole().setVisible(true);
        });
    }

    public ServerConsole() {
        setTitle("中国象棋服务器控制台");
        setSize(700, 500);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        setLayout(new BorderLayout(10, 10));
        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBorder(new EmptyBorder(10, 10, 10, 10)); // 设置内边距
        setContentPane(mainPanel);

        JPanel leftPanel = new JPanel(new BorderLayout(0, 5));
        leftPanel.add(new JLabel("在线用户:"), BorderLayout.NORTH);

        listModel = new DefaultListModel<>();
        JList<String> userList = new JList<>(listModel);
        JScrollPane userScroll = new JScrollPane(userList);
        userScroll.setPreferredSize(new Dimension(200, 0));
        leftPanel.add(userScroll, BorderLayout.CENTER);

        add(leftPanel, BorderLayout.WEST);

        JPanel centerPanel = new JPanel(new BorderLayout(0, 5));
        centerPanel.add(new JLabel("系统日志:"), BorderLayout.NORTH);

        logArea = new JTextArea();
        logArea.setEditable(false);
        logArea.setLineWrap(true);
        JScrollPane logScroll = new JScrollPane(logArea);
        centerPanel.add(logScroll, BorderLayout.CENTER);

        add(centerPanel, BorderLayout.CENTER);

        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        portField = new JTextField("54321");
        Dimension size = portField.getPreferredSize();
        size.width *= 2;
        portField.setPreferredSize(size);
        startButton = new JButton("启动服务器");
        stopButton = new JButton("停止服务器");
        stopButton.setEnabled(false);

        bottomPanel.add(new JLabel("端口："));
        bottomPanel.add(portField);
        bottomPanel.add(new JSeparator(JSeparator.VERTICAL));
        bottomPanel.add(startButton);
        bottomPanel.add(stopButton);
        add(bottomPanel, BorderLayout.SOUTH);

        startButton.addActionListener(e -> startServer());
        stopButton.addActionListener(e -> stopServer());

        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                stopServer();
            }
        });
    }

    public void appendLog(String msg) {
        SwingUtilities.invokeLater(() -> {
            String time = new SimpleDateFormat("HH:mm:ss").format(new Date());
            logArea.append("[" + time + "] " + msg + "\n");
            logArea.setCaretPosition(logArea.getDocument().getLength());
        });
    }

    public void addClientUI(String clientInfo) {
        SwingUtilities.invokeLater(() -> listModel.addElement(clientInfo));
    }

    public void removeClientUI(String clientInfo) {
        SwingUtilities.invokeLater(() -> listModel.removeElement(clientInfo));
    }

    private void startServer() {
        if (server== null) {
            server = new GameServer(
                    Integer.parseInt(portField.getText()),
                    this::appendLog,
                    this::addClientUI,
                    this::removeClientUI
            );

            server.start();
            startButton.setEnabled(false);
            stopButton.setEnabled(true);
            appendLog("正在初始化服务器核心...");
        }
    }

    private void stopServer() {
        if (server!= null) {
            server.stop();
            server = null;
            startButton.setEnabled(true);
            stopButton.setEnabled(false);
            appendLog("服务器已停止。");
            listModel.clear();
        }
    }
}
