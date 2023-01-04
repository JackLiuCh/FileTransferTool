import java.net.Socket;
import java.net.SocketTimeoutException;

import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JSeparator;
import javax.swing.JTextField;
import javax.swing.border.TitledBorder;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

// TCP套接字客户端
// 1. ip地址和端口号 textfield
// 2. 用户名和密码 textfield
// 3. 连接按钮
// 4. 链接状态
// 5. 发送模式与接收模式 radio button
// 6. 文件接收路径 textfield
// 7. 文件发送路径 textfield
// 8. 文件发送进度 label
// 8. 发送按钮


public class Client extends JFrame {
    private static final long serialVersionUID = 1L;
    private static final String SERVER_IP = "localhost";
    private static final int SERVER_PORT = 8888;
    private static final String USERNAME = "user1";
    private static final String PASSWORD = "123";
    private static final String FILE_PATH = "D:\\test\\test.txt";
    private static final String SAVE_DIR = "D:\\test\\Client\\recv";

    private JTextField ipField;
    private JTextField portField;
    private JTextField usernameField;
    private JTextField passwordField;
    private JTextField filePathField;
    private JTextField saveDirField;
    private JRadioButton[] modeRadio;
    private JButton connectButton;
    private JButton sendButton;
    private JLabel statusLabel;
    private JLabel progressLabel;

    private Socket socket; 
    // mode -1: 默认模式，0: 接收模式，1: 发送模式
    private int mode = -1;
    private Thread receiverThread;

    public static void main(String[] args) {
        new Client();
    }

    public Client() {
        super("Client");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(600, 400);
        setLocation(400, 200);
        setVisible(true);
        setLayout(new BorderLayout());


        JPanel panel = new JPanel();
        panel.setLayout(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(0, 0, 10, 0); 
        // 设置面板边框
        panel.setBorder(new TitledBorder("Client"));

        // 1. ip地址和端口号 textfield
        JLabel ipLabel = new JLabel("服务端IP:");
        c.gridx = 0;
        c.gridy = 0;
        c.gridwidth = 1;
        panel.add(ipLabel, c);

        ipField = new JTextField(SERVER_IP);
        ipField.setPreferredSize(new Dimension(200, 30));
        c.gridx = 1;
        c.gridy = 0;
        c.gridwidth = 2;
        panel.add(ipField, c);

        JLabel portLabel = new JLabel("端口:");
        c.gridx = 0;
        c.gridy = 1;
        c.gridwidth = 1;
        panel.add(portLabel, c);

        portField = new JTextField(String.valueOf(SERVER_PORT));
        portField.setPreferredSize(new Dimension(200, 30));
        c.gridx = 1;
        c.gridy = 1;
        c.gridwidth = 2;
        panel.add(portField, c);

        // 2. 用户名和密码 textfield
        JLabel usernameLabel = new JLabel("用户名:");
        c.gridx = 0;
        c.gridy = 2;
        c.gridwidth = 1;
        panel.add(usernameLabel, c);

        usernameField = new JTextField(USERNAME);
        usernameField.setPreferredSize(new Dimension(200, 30));
        c.gridx = 1;
        c.gridy = 2;
        c.gridwidth = 2;
        panel.add(usernameField, c);

        JLabel passwordLabel = new JLabel("密码:");
        c.gridx = 0;
        c.gridy = 3;
        c.gridwidth = 1;
        panel.add(passwordLabel, c);

        passwordField = new JTextField(PASSWORD);
        passwordField.setPreferredSize(new Dimension(200, 30));
        c.gridx = 1;
        c.gridy = 3;
        c.gridwidth = 2;
        panel.add(passwordField, c);

        // 3. 连接按钮
        connectButton = new JButton("连接服务端");
        connectButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if(connectButton.getText().equals("连接服务端")) {
                    // 连接服务端
                    String ip = ipField.getText();
                    int port = Integer.parseInt(portField.getText());
                    String username = usernameField.getText();
                    String password = passwordField.getText();
                    try {
                        socket = new Socket(ip, port);
                        // 发送用户名和密码
                        socket.getOutputStream().write((username + "\r" + password).getBytes());

                        // 等待服务器返回验证结果，如果是ok则表示验证成功，否则表示验证失败
                        byte[] buffer = new byte[1024];
                        int len = socket.getInputStream().read(buffer);
                        String result = new String(buffer, 0, len);
                        if (!"ok".equals(result)) {
                            statusLabel.setText("链接状态: 未连接");
                            connectButton.setText("连接服务端");
                            modeRadio[0].setEnabled(false);
                            modeRadio[1].setEnabled(false);
                            JOptionPane.showMessageDialog(null, "用户名或密码错误");
                            return;
                        }
                        statusLabel.setText("链接状态: 已连接");
                        connectButton.setText("断开连接");
                        modeRadio[0].setEnabled(true);
                        modeRadio[1].setEnabled(true);
                        modeRadio[0].doClick();


                    } catch (Exception e1) {
                        statusLabel.setText("链接状态: 未连接");
                        connectButton.setText("连接服务端");
                        modeRadio[0].setEnabled(false);
                        modeRadio[1].setEnabled(false);
                        JOptionPane.showMessageDialog(null, "连接服务端失败");
                    }
                } else {
                    // 断开连接
                    try {
                        if(socket != null) socket.close();
                        statusLabel.setText("链接状态: 未连接");
                        connectButton.setText("连接服务端");
                        modeRadio[0].setEnabled(false);
                        modeRadio[1].setEnabled(false);
                        sendButton.setEnabled(false);
                    } catch (Exception e1) {
                        e1.printStackTrace();
                        JOptionPane.showMessageDialog(null, "断开连接失败");
                    }
                }
            }
        });
        c.gridx = 0;
        c.gridy = 4;
        c.gridwidth = 1;
        panel.add(connectButton, c);

        // 4. 链接状态
        statusLabel = new JLabel("链接状态: 未连接");
        c.gridx = 2;
        c.gridy = 4;
        c.gridwidth = 1;
        panel.add(statusLabel, c);


        // 添加一个分割线
        JSeparator separator = new JSeparator();
        separator.setForeground(Color.BLACK);
        separator.setPreferredSize(new Dimension(150, 1));
        // 将分割线添加到面板
        c.gridx = 0;
        c.gridy = 5;
        c.gridwidth = 3;
        c.ipady = 10;
        c.fill = GridBagConstraints.HORIZONTAL; 
        panel.add(separator, c);
        c.ipady = 0;
        c.fill = GridBagConstraints.NONE;
        

        // 5. 发送模式与接收模式 radio button
        modeRadio = new JRadioButton[2];
        modeRadio[0] = new JRadioButton("发送文件");
        modeRadio[0].setEnabled(false);
        modeRadio[1] = new JRadioButton("接收文件");
        modeRadio[1].setEnabled(false);
        ButtonGroup group = new ButtonGroup();
        group.add(modeRadio[0]);
        group.add(modeRadio[1]);
        c.gridx = 0;
        c.gridy = 6;
        c.gridwidth = 1;
        panel.add(modeRadio[0], c);
        c.gridx = 1;
        c.gridy = 6;
        c.gridwidth = 1;
        panel.add(modeRadio[1], c);

        modeRadio[0].addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                mode = 1;
                saveDirField.setEnabled(false);
                filePathField.setEnabled(true);
                sendButton.setEnabled(true);

            }
        });

        modeRadio[1].addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if(mode == 0){
                    return;
                }
                mode = 0;
                saveDirField.setEnabled(true);
                filePathField.setEnabled(false);
                sendButton.setEnabled(false);
                
                receiverThread = new Thread(new Runnable() {
                    @Override
                    public void run(){
                        // 支持多次接收服务端发送的文件
                        // 1.接收文件名
                        // 2.接收文件大小
                        // 3.接收文件内容
                        loop1:while(true){
                            try{
                                if(socket == null) {
                                    try {
                                        Thread.sleep(100);
                                    } catch (InterruptedException e) {
                                        e.printStackTrace();
                                    }
                                    continue;
                                };
                                // 获取输入流
                                InputStream is = socket.getInputStream();

                                // 创建缓冲区
                                byte[] buffer = new byte[1024];

                                int len = -1;
                                socket.setSoTimeout(100);
                                while(true){
                                    if(mode == 1) {
                                        socket.setSoTimeout(0);
                                        System.out.println("接收线程退出");
                                        return;
                                    }

                                    try{
                                        len = is.read(buffer);
                                        if(len != -1) {
                                            socket.setSoTimeout(0);
                                            break;
                                        }
                                        else{
                                            System.out.println("检测到服务端断开");
                                            socket = null;
                                            statusLabel.setText("链接状态：未连接");
                                            connectButton.setText("连接服务端");
                                            modeRadio[0].setEnabled(false);
                                            modeRadio[1].setEnabled(false);
                                            continue loop1;
                                        }
                                    }catch(SocketTimeoutException e){
                                        Thread.sleep(100);
                                    }   
                                }

                                System.out.println("接收线程读了元信息");
                                // 将数据转换为字符串
                                String info = new String(buffer, 0, len);
                                // 将字符串按照换行符分隔
                                String[] infos = info.split("\r");
                                // 获取文件名
                                String fileName = infos[0];
                                // 获取文件大小
                                long fileSize = Long.parseLong(infos[1]);

                                // 反馈给服务端，已经接收到文件名和文件大小，可以开始发送文件内容
                                OutputStream os = socket.getOutputStream();
                                os.write("metaOK".getBytes());
                                System.out.println("接收线程写了metaOK");

                                // 另起线程
                                new Thread(new Runnable(){
                                    @Override
                                    public void run() {
                                        // 弹出提示框，显示文件名和文件大小
                                        JOptionPane.showMessageDialog(null, "接收到文件名：" + fileName + ",文件大小：" + fileSize);
                                    }
                                }).start();

                                // 获取文件内容
                                // 创建字节数组，用于存放文件内容
                                byte[] fileContent = new byte[(int) fileSize];
                                // 利用buffer读取文件内容
                                int readSize = 0;
                                // 用于记录已经读取的文件内容大小
                                int totalSize = 0;
                                // 读取文件内容
                                while (totalSize < fileSize) {
                                    // 读取文件内容
                                    readSize = is.read(buffer);
                                    // 将读取的内容写入到fileContent中
                                    System.arraycopy(buffer, 0, fileContent, totalSize, readSize);
                                    // 累加已经读取的文件内容大小
                                    totalSize += readSize;
                                }
                                
                                // 将文件内容写入到文件中
                                FileOutputStream fos = new FileOutputStream(saveDirField.getText() + "\\" + fileName);
                                fos.write(fileContent);
                                fos.close();
                                // 弹窗提示接收文件成功，提示文件保存路径
                                new Thread(){
                                    @Override
                                    public void run() {
                                        JOptionPane.showMessageDialog(null, "接收文件成功，文件保存路径为：" + saveDirField.getText() + "\\" + fileName);
                                    }
                                }.start();
                                

                            }catch (Exception e) {
                                e.printStackTrace();
                                statusLabel.setText("链接状态：未连接");
                                filePathField.setEnabled(false);
                                socket = null;
                            }
                        }

                    }
                });
                // 启动线程
                System.out.println("启动接收文件线程");
                receiverThread.start();
            }
        });

   
        // 6. 文件保存路径 textfield
        JLabel saveDirLabel = new JLabel("文件保存路径:");
        c.gridx = 0;
        c.gridy = 7;
        c.gridwidth = 1;
        panel.add(saveDirLabel, c);
        
        saveDirField = new JTextField(SAVE_DIR);
        saveDirField.setEnabled(false);
        saveDirField.setPreferredSize(new Dimension(200, 30));
        c.gridx = 1;
        c.gridy = 7;
        c.gridwidth = 2;
        panel.add(saveDirField, c);

        // 7. 文件发送路径 textfield
        JLabel filePathLabel = new JLabel("发送文件路径:");
        c.gridx = 0;
        c.gridy = 8;
        c.gridwidth = 1;
        panel.add(filePathLabel, c);

        filePathField = new JTextField(FILE_PATH);
        filePathField.setEnabled(false);
        filePathField.setPreferredSize(new Dimension(200, 30));
        c.gridx = 1;
        c.gridy = 8;
        c.gridwidth = 2;
        panel.add(filePathField, c);

        // 8. 文件发送进度
        JLabel progressLabel = new JLabel("发送进度: N/A");
        c.gridx = 0;
        c.gridy = 9;
        c.gridwidth = 1;
        panel.add(progressLabel, c);

        // 9. 发送按钮
        sendButton = new JButton("发送");
        sendButton.setEnabled(false);
        sendButton.addActionListener(new ActionListener(){
            @Override
            public void actionPerformed(ActionEvent e) {
                // 判断是否有客户端连接
                if (socket == null) {
                    JOptionPane.showMessageDialog(null, "请先连接客户端", "错误", JOptionPane.ERROR_MESSAGE);
                    return;
                }
                // 判断文件路径是否为空
                if (filePathField.getText().equals("")) {
                    JOptionPane.showMessageDialog(null, "请输入文件路径", "错误", JOptionPane.ERROR_MESSAGE);
                    return;
                }
                // 判断文件是否存在
                File file = new File(filePathField.getText());
                if (!file.exists()) {
                    JOptionPane.showMessageDialog(null, "文件不存在", "错误", JOptionPane.ERROR_MESSAGE);
                    return;
                }
                // 判断文件是否为文件
                if (!file.isFile()) {
                    JOptionPane.showMessageDialog(null, "请输入文件路径", "错误", JOptionPane.ERROR_MESSAGE);
                    return;
                }
                
                new Thread(){
                    public void run() {
                        // 通过socket给服务端发送文件名和文件大小元数据，用换行符分隔
                        try {
                            // 获取socket的输出流
                            OutputStream os = socket.getOutputStream();
                            // 将文件名和文件大小转换为字节数组
                            String metaData = file.getName() + "\r" + file.length();
                            byte[] metaDataBytes = metaData.getBytes();
                            // 将字节数组写入输出流
                            os.write(metaDataBytes);
                            os.flush();
                            // 等待服务端回复metaOK
                            System.out.println("等待回复");
                            InputStream is = socket.getInputStream();
                            byte[] buffer = new byte[1024];
                            int len = is.read(buffer);
                            String metaOK = new String(buffer, 0, len);
                            if (!metaOK.equals("metaOK")) {
                                JOptionPane.showMessageDialog(null, "服务端接收文件元数据失败", "错误", JOptionPane.ERROR_MESSAGE);
                                return;
                            }
                            // 通过buffer读取文件，然后通过socket发送，更新发送进度，发送成功后提示
                            FileInputStream fis = new FileInputStream(file);
                            BufferedInputStream bis = new BufferedInputStream(fis);
                            long total = file.length();
                            long sent = 0;
                            int n;
                            while ((n = bis.read(buffer)) != -1) {
                                os.write(buffer, 0, n);
                                os.flush();
                                sent += n;
                                progressLabel.setText("发送进度: " + sent * 100 / total + "%");
                            }
                            JOptionPane.showMessageDialog(null, "文件发送成功", "提示", JOptionPane.INFORMATION_MESSAGE);
                            progressLabel.setText("发送进度: N/A");

                        } catch (IOException e1) {
                            e1.printStackTrace();
                            statusLabel.setText("链接状态：未连接");
                            progressLabel.setText("发送进度:  N/A");
                            sendButton.setEnabled(false);
                        }
                    };
                }.start();
                
            }

        });
        c.gridx = 0;
        c.gridy = 9;
        c.gridwidth = 3;
        panel.add(sendButton, c);

        
        add(panel, BorderLayout.CENTER);
        pack();

        
    

    }
}