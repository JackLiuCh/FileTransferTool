// TCP server
// 提供文件传输服务
// UI设置监听端口，文件接收路径，服务器启动按钮
// 显示是否与客户端连接，客户端IP地址，文件传输进度
// UI可以选择文件，点击发送按钮，发送文件给客户端
// 提供基本身份验证功能，如果客户端的用户名和密码不正确，拒绝连接

import java.io.*;
import java.net.*;
import java.util.*;
import java.awt.*;
import java.awt.event.*;

import javax.naming.ldap.SortKey;
import javax.swing.*;
import javax.swing.event.*;
import javax.swing.border.*;

public class Server extends JFrame {
    private static final long serialVersionUID = 1L;
    private static final int PORT = 8888;

    private static final String[] USER_NAME = { "user1", "user2", "user3" };
    private static final String[] PASSWORD = { "123", "456", "789" };

    //输入框输入监听端口
    private JTextField portField;
    //输入框输入文件接收路径
    private JTextField pathField;
    //按钮启动服务器
    private JButton startButton;
    // 服务器是否启动状态
    private JLabel serverStatusLabel;
    //显示是否与客户端连接
    private JLabel statusLabel;
    //显示客户端IP地址
    private JLabel ipLabel;
    //显示文件传输进度
    private JLabel progressLabel;
    //选择文件路径
    private JTextField fileField;
    //发送文件给客户端
    private JButton sendButton;
    // 传输模式radio
    private JRadioButton[] modeRadio;

    private ServerSocket server;
    private Socket socket;
    private Thread receiverThread;
    // mode -1: 默认模式，0: 接收模式，1: 发送模式
    private int mode = -1;

    public static boolean checkUserPasswd(String userName, String passwd) {
        for (int i = 0; i < USER_NAME.length; i++) {
            if (USER_NAME[i].equals(userName) && PASSWORD[i].equals(passwd)) {
                return true;
            }
        }
        return false;
    }

    public Server(){
        // 设置窗口标题
        super("Server");
        // 设置窗口大小
        setSize(500, 500);
        // 设置窗口位置
        setLocation(200, 200);
        // 设置窗口关闭方式
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        // 设置窗口布局
        setLayout(new BorderLayout());

        // 创建面板
        JPanel panel = new JPanel();
        // 设置面板布局
        // panel.setLayout(new GridLayout(5, 10));
        panel.setLayout(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(0, 0, 10, 0); 

        
        // 设置面板边框
        panel.setBorder(new TitledBorder("Server"));

        // 创建标签
        JLabel portLabel = new JLabel("监听端口:");
        // 设置标签对齐方式
        portLabel.setHorizontalAlignment(SwingConstants.LEFT);
        // 将标签添加到面板
        c.gridx = 0;
        c.gridy = 0;
        c.gridwidth = 1;
        panel.add(portLabel, c);
        
        // 创建输入框
        portField = new JTextField();
        // 设置输入框默认值
        portField.setText(String.valueOf(PORT));
        // 设置固定宽度
        portField.setPreferredSize(new Dimension(200, 30));
        // 将输入框添加到面板
        c.gridx = 1;
        c.gridy = 0;
        c.gridwidth = 1;
        panel.add(portField, c);


        // 创建标签
        JLabel pathLabel = new JLabel("文件接收路径:");
        // 设置标签对齐方式
        pathLabel.setHorizontalAlignment(SwingConstants.LEFT);
        // 将标签添加到面板
        c.gridx = 0;
        c.gridy = 1;
        c.gridwidth = 1;
        panel.add(pathLabel, c);

        // 创建输入框
        pathField = new JTextField();
        // 设置输入框默认值
        // pathField.setText(System.getProperty("user.dir"));
        pathField.setText("D:\\test\\Server\\recv");
        // 设置固定宽度
        pathField.setPreferredSize(new Dimension(200, 30));
        // 将输入框添加到面板
        c.gridx = 1;
        c.gridy = 1;
        c.gridwidth = 1;
        panel.add(pathField, c);



        // 创建按钮
        startButton = new JButton("启动服务");
        // 设置按钮监听
        startButton.addActionListener(new ActionListener(){
            @Override
            public void actionPerformed(ActionEvent e) {
                // 启动服务或者停止服务
                if (startButton.getText().equals("启动服务")) {
                    startServer();
                    modeRadio[0].doClick();
                } else {
                    stopServer();
                }
            }

            private void stopServer() {
                try {
                    if(socket != null) socket.close();
                    server.close();
                    startButton.setText("启动服务");
                    serverStatusLabel.setText("未启动");
                    serverStatusLabel.setForeground(Color.BLACK);
                    statusLabel.setText("链接状态：未连接");
                    ipLabel.setText("客户端IP: N/A");
                    progressLabel.setText("发送进度:  N/A");
                    fileField.setEnabled(false);
                    sendButton.setEnabled(false);
                } catch (Exception e) {
                    e.printStackTrace();
                }

            }

            private void startServer() {
                // 获取输入框输入的端口
                int port = Integer.parseInt(portField.getText());
                // 创建服务器
                try {
                    server = new ServerSocket(port);
                    startButton.setText("停止服务");
   
                    // 设置服务器状态
                    serverStatusLabel.setText("服务器已启动");
                    // 设置服务器状态颜色
                    serverStatusLabel.setForeground(Color.GREEN);
                    // 设置按钮状态
                    portField.setEnabled(false);
                    // 设置按钮状态
                    pathField.setEnabled(false);
                    // 设置按钮状态
                    fileField.setEnabled(false);
                    sendButton.setEnabled(false);


                    // 创建线程，用于接收客户端的连接
                    Thread clientThread = new Thread(new Runnable(){
                        @Override
                        public void run() {
                            while (true) {
                                try {
                                    // 接收客户端的连接
                                    socket = server.accept();
                                    System.out.println("客户端正尝试连接...");
                                    // 验证客户端身份信息，客户端与服务端建立连接后，客户端会立即发送用户名和密码，服务端接收到后，进行验证，如果验证通过，则建立连接，否则断开连接
                                    // 用户名和密码由两行组成，第一行为用户名，第二行为密码，两行之间使用换行符分隔
                                    // 超过1s未验证通过，则断开连接
                                    // 获取输入流
                                    InputStream is = socket.getInputStream();
                                    // 创建缓冲区
                                    byte[] buffer = new byte[1024];
                                    // 读取数据
                                    int len = is.read(buffer);
                                    // 将数据转换为字符串
                                    String info = new String(buffer, 0, len);

                                    // 将字符串按照换行符分隔
                                    String[] infos = info.split("\r");
                                    
                                    // 验证用户名和密码
                                    if (infos.length == 2 && checkUserPasswd(infos[0], infos[1])) {
                                        // 给客户端发送验证通过的信息ok
                                        OutputStream os = socket.getOutputStream();
                                        os.write("ok".getBytes());

                                        // 设置按钮状态
                                        portField.setEnabled(false);
                                        // 设置按钮状态
                                        pathField.setEnabled(false);
                                        // 设置标签状态
                                        statusLabel.setText("链接状态：客户端已连接");
                                        // 设置标签状态
                                        ipLabel.setText("客户端IP:" + socket.getInetAddress().getHostAddress());
                                        // 设置标签状态
                                        progressLabel.setText("发送进度:  N/A");
                                        if(mode == 1){
                                            sendButton.setEnabled(true);
                                        }else{
                                            sendButton.setEnabled(false);
                                        }

                                    }else {
                                        // 客户端身份验证失败，关闭连接，并提示错误信息
                                        // 给客户端发送错误信息
                                        OutputStream os = socket.getOutputStream();
                                        os.write("身份验证失败".getBytes());

                                        socket.close();

                                        new Thread(){
                                            @Override
                                            public void run() {
                                                JOptionPane.showMessageDialog(null, "客户端身份验证失败！", "错误", JOptionPane.ERROR_MESSAGE);
                                            }
                                        }.start();
                                    }

  
                                } catch (IOException e) {
                                    e.printStackTrace();
                                    // 客户端连接已经断开，更新链接状态
                                    statusLabel.setText("链接状态：客户端已断开");
                                    ipLabel.setText("客户端IP：N/A");
                                    fileField.setEnabled(false);
                                    sendButton.setEnabled(false);

                                    return;
                                }
                            }
                        }
                    });
                    // 启动线程
                    clientThread.start();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });

        // 将按钮添加到面板
        c.gridx = 0;
        c.gridy = 2;
        c.gridwidth = 1;
        panel.add(startButton, c);

        serverStatusLabel = new JLabel("停止");
        // 设置标签对齐方式
        serverStatusLabel.setHorizontalAlignment(SwingConstants.RIGHT);
        // 将标签添加到面板
        c.gridx = 1;
        c.gridy = 2;
        c.gridwidth = 1;
        panel.add(serverStatusLabel, c);


        // 添加一个分割线
        JSeparator separator = new JSeparator();
        separator.setForeground(Color.BLACK);
        separator.setPreferredSize(new Dimension(150, 1));
        // 将分割线添加到面板
        c.gridx = 0;
        c.gridy = 3;
        c.gridwidth = 3;
        c.ipady = 10;
        c.fill = GridBagConstraints.HORIZONTAL; 
        panel.add(separator, c);
        c.ipady = 0;
        c.fill = GridBagConstraints.NONE;

        // 添加接收文件与发送文件两个模式进modeRadio数组
        modeRadio = new JRadioButton[2];
        modeRadio[0] = new JRadioButton("接收文件");
        modeRadio[1] = new JRadioButton("发送文件");
        // 将两个模式添加到按钮组
        ButtonGroup modeGroup = new ButtonGroup();
        modeGroup.add(modeRadio[0]);
        modeGroup.add(modeRadio[1]);
        // 将两个模式添加到面板
        c.gridx = 0;
        c.gridy = 4;
        c.gridwidth = 1;
        panel.add(modeRadio[0], c);
        c.gridx = 1;
        c.gridy = 4;
        c.gridwidth = 1;
        panel.add(modeRadio[1], c);
        // radio模式按钮添加监听器
        modeRadio[0].addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                // 重复点击接收文件模式，不做任何操作
                if (mode == 0) {
                    return;
                }
                // 设置模式为接收文件模式
                mode = 0;
                // 接收文件模式
                fileField.setEnabled(false);
                sendButton.setEnabled(false);
                // 创建线程，用于接收客户端发送的文件
                receiverThread = new Thread(new Runnable(){
                    @Override
                    public void run() {
                        // 支持多次接收客户端发送的文件
                        // 1.接收文件名
                        // 2.接收文件大小
                        // 3.接收文件内容
                        loop1:while (true) {
                            try {
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
                                
                                // 读取数据
                                int len = -1;
                                // 设置is.read()方法的超时时间
                                socket.setSoTimeout(100);
                                while(true){
                                    if(mode == 1) {
                                        socket.setSoTimeout(0);
                                        System.out.println("接收线程退出");
                                        return;
                                    }

                                    try {
                                        len = is.read(buffer);
                                        if(len != -1) {
                                            socket.setSoTimeout(0);
                                            break;
                                        }else{
                                            System.out.println("检测到客户端断开");
                                            socket = null;
                                            statusLabel.setText("链接状态：客户端已断开");
                                            ipLabel.setText("客户端IP：N/A");
                                            continue loop1;
                                        }
                                    } catch (SocketTimeoutException e) {
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

                                // 反馈给客户端，已经接收到文件名和文件大小，可以开始发送文件内容
                                OutputStream os = socket.getOutputStream();
                                os.write("metaOK".getBytes());

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
                                FileOutputStream fos = new FileOutputStream
                                        (new File(pathField.getText() + "\\" + fileName));
                                fos.write(fileContent);
                                fos.close();
                                // 弹窗提示接收文件成功，提示文件保存路径
                                new Thread(){
                                    @Override
                                    public void run() {
                                        JOptionPane.showMessageDialog(null, "接收文件成功，文件保存路径为：" + pathField.getText() + "\\" + fileName);
                                    }
                                }.start();
                            } catch (Exception e) {
                                e.printStackTrace();
                                // 客户端连接已经断开，更新链接状态
                                statusLabel.setText("链接状态：客户端已断开");
                                ipLabel.setText("客户端IP：N/A");
                                fileField.setEnabled(false);
                                sendButton.setEnabled(false);
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

        modeRadio[1].addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                // 设置发送文件模式
                mode = 1;
                // 发送文件模式
                fileField.setEnabled(true);
                sendButton.setEnabled(true);
            }
        });
        
        
        // 创建标签
        statusLabel = new JLabel("链接状态: 未连接");
        // 设置标签对齐方式
        statusLabel.setHorizontalAlignment(SwingConstants.RIGHT);
        // 将标签添加到面板
        c.gridx = 0;
        c.gridy = 5;
        c.gridwidth = 1;
        panel.add(statusLabel, c);


        // 创建标签
        ipLabel = new JLabel("客户端IP: N/A");
        // 设置标签对齐方式
        ipLabel.setHorizontalAlignment(SwingConstants.RIGHT);
        // 将标签添加到面板
        c.gridx = 0;
        c.gridy = 6;
        c.gridwidth = 1;
        panel.add(ipLabel, c);

        // 创建标签
        progressLabel = new JLabel("发送进度: N/A");
        // 设置标签对齐方式
        progressLabel.setHorizontalAlignment(SwingConstants.RIGHT);
        // 将标签添加到面板
        c.gridx = 0;
        c.gridy = 7;
        c.gridwidth = 1;
        panel.add(progressLabel, c);

        // 创建标签
        JLabel fileLabel = new JLabel("发送文件路径:");
        // 设置标签对齐方式
        fileLabel.setHorizontalAlignment(SwingConstants.RIGHT);
        // 将标签添加到面板
        c.gridx = 0;
        c.gridy = 8;
        c.gridwidth = 1;
        panel.add(fileLabel, c);


        fileField = new JTextField();
        fileField.setText("D:\\test\\test.txt");
        fileField.setEnabled(false);
        fileField.setPreferredSize(new Dimension(200, 30));
        // 将输入框添加到面板
        c.gridx = 1;
        c.gridy = 8;
        c.gridwidth = 1;
        panel.add(fileField, c);


        // 创建按钮
        sendButton = new JButton("发送");
        sendButton.setEnabled(false);
        // 设置按钮监听
        sendButton.addActionListener(new ActionListener(){
            @Override
            public void actionPerformed(ActionEvent e) {
                // 判断是否有客户端连接
                if (socket == null) {
                    JOptionPane.showMessageDialog(null, "请先连接客户端", "错误", JOptionPane.ERROR_MESSAGE);
                    return;
                }
                // 判断文件路径是否为空
                if (fileField.getText().equals("")) {
                    JOptionPane.showMessageDialog(null, "请输入文件路径", "错误", JOptionPane.ERROR_MESSAGE);
                    return;
                }
                // 判断文件是否存在
                File file = new File(fileField.getText());
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
                        // 通过socket给客户端发送文件名和文件大小元数据，用换行符分隔
                        try {
                            // 获取socket的输出流
                            OutputStream os = socket.getOutputStream();
                            // 将文件名和文件大小转换为字节数组
                            String metaData = file.getName() + "\r" + file.length();
                            byte[] metaDataBytes = metaData.getBytes();
                            // 将字节数组写入输出流
                            os.write(metaDataBytes);
                            os.flush();
                            // 等待客户端回复metaOK
                            System.out.println("等待回复");
                            InputStream is = socket.getInputStream();
                            byte[] buffer = new byte[1024];
                            int len = is.read(buffer);
                            String metaOK = new String(buffer, 0, len);
                            if (!metaOK.equals("metaOK")) {
                                JOptionPane.showMessageDialog(null, "客户端接收文件元数据失败", "错误", JOptionPane.ERROR_MESSAGE);
                                System.out.println("客户端接收文件元数据失败");
                                return;
                            }
                            System.out.println("客户端接收文件元数据成功");
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
                            ipLabel.setText("客户端IP: N/A");
                            progressLabel.setText("发送进度:  N/A");
                            sendButton.setEnabled(false);
                        }
                    };
                }.start();
                
            }

        });
        // 将按钮添加到面板
        c.gridx = 0;
        c.gridy = 9;
        c.gridwidth = 1;
        panel.add(sendButton, c);

        // 将面板添加到窗口
        add(panel, BorderLayout.CENTER);
        
        pack();
        // 设置窗口可见
        setVisible(true);



    }

    public static void main(String[] args) {
        new Server();
    }



}

