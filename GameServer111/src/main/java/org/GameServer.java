package org;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.*;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Consumer;

public class GameServer {
    private static final String NET_USER_IN = "USER_IN:";
    private static final String NET_USER_OUT = "USER_OUT:";
    private static final String NET_USER_INVITE = "USER_INVITE:";
    private static final String NET_USER_REJECT = "USER_REJECT:";
    private static final String NET_USER_ACCEPT = "USER_ACCEPT:";
    private static final String NET_USER_UPDATE = "USER_UPDATE:";

    // 游戏内指令转发协议头
    private static final String NET_USER_MOVE = "USER_MOVE:";
    private static final String NET_USER_REQUEST = "USER_REQUEST:";
    private static final String NET_USER_REPLY = "USER_REPLY:";
    private static final String NET_USER_CANCEL = "USER_CANCEL:";
    private static final String NET_USER_RETRACT = "USER_RETRACT:";
    private static final String NET_USER_RESTART = "USER_RESTART:";

    private final ExecutorService threadPool = Executors.newCachedThreadPool();
    private final int port;
    private final Consumer<String> onLog;
    private final Consumer<String> onUserConnected;
    private final Consumer<String> onUserDisconnected;

    // 使用线程安全的列表，防止并发修改异常
    private final List<Client> clients = new CopyOnWriteArrayList<>();
    private ServerSocket serverSocket;
    private volatile boolean isRunning = false;

    private static class Client {
        Socket socket;
        PrintWriter out;
        String id;
        String name;
        String avatar;
        Client opponent; // 对手

        public Client(Socket socket) {
            this.socket = socket;
            // 使用 IP:Port 作为唯一ID
            this.id = socket.getInetAddress().getHostAddress() + ":" + socket.getPort();
        }

        // 格式化输出：名字,头像,ID
        public String toString() {
            return (name == null ? "Unknown" : name) + "," + (avatar == null ? "level_1.png" : avatar) + "," + id;
        }
    }

    public GameServer(int port, Consumer<String> onLog, Consumer<String> onUserConnected, Consumer<String> onUserDisconnected) {
        this.port = port;
        this.onLog = onLog;
        this.onUserConnected = onUserConnected;
        this.onUserDisconnected = onUserDisconnected;
    }

    public void start() {
        if (isRunning) return;
        isRunning = true;
        threadPool.execute(() -> {
            try {
                serverSocket = new ServerSocket(port);
                log("=========================================");
                log("服务器启动成功！(版本: V2.0 Fix)");
                log("监听 IP: " + getLocalHostIP());
                log("监听 Port: " + port);
                log("=========================================");
                while (isRunning) {
                    Socket clientSocket = serverSocket.accept();
                    handleConnection(clientSocket);
                }
            } catch (IOException e) {
                log("服务器停止或错误: " + e.getMessage());
            }
        });
    }

    public void stop() {
        isRunning = false;
        try {
            if (serverSocket != null) serverSocket.close();
            // 关闭所有客户端socket
            for(Client c : clients) {
                if(c.socket != null && !c.socket.isClosed()) c.socket.close();
            }
            clients.clear();
            threadPool.shutdownNow();
        } catch (IOException e) { e.printStackTrace(); }
    }

    // 获取本机IP地址（优先获取非回环地址）
    private static String getLocalHostIP() {
        try {
            java.util.Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces();
            while (interfaces.hasMoreElements()) {
                NetworkInterface iface = interfaces.nextElement();
                if (iface.isLoopback() || !iface.isUp() || iface.isVirtual()) continue;
                java.util.Enumeration<InetAddress> addresses = iface.getInetAddresses();
                while (addresses.hasMoreElements()) {
                    InetAddress addr = addresses.nextElement();
                    if (addr instanceof Inet4Address && addr.isSiteLocalAddress()) {
                        return addr.getHostAddress();
                    }
                }
            }
        } catch (Exception e) { }
        return "127.0.0.1";
    }

    private void handleConnection(Socket socket) {
        Client client = new Client(socket);
        log("客户端连接: " + client.id);

        if (onUserConnected != null) onUserConnected.accept(client.id);

        threadPool.execute(() -> {
            try {
                BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
                client.out = out;

                // 此时再加入列表，确保输出流可用
                clients.add(client);

                String line;
                while (isRunning && (line = in.readLine()) != null) {

                    if (line.startsWith(NET_USER_IN)) {
                        String[] args = line.substring(NET_USER_IN.length()).split(",");
                        if (args.length > 0) client.name = args[0];
                        if (args.length > 1) client.avatar = args[1];

                        log(client.name + " (" + client.id + ") 登录成功");

                        for (Client c : clients) {
                            client.out.println(NET_USER_IN + c.toString());

                            if (c != client) {
                                c.out.println(NET_USER_IN + client.toString());
                            }
                        }
                    }
                    else if (line.startsWith(NET_USER_INVITE)) {
                        String targetId = line.substring(NET_USER_INVITE.length());
                        Client target = findClient(targetId);
                        if (target != null) {
                            // 简单的双向绑定
                            client.opponent = target;
                            target.opponent = client;
                            target.out.println(NET_USER_INVITE + client.id);
                        }
                    }
                    else if (line.startsWith(NET_USER_ACCEPT)) {
                        if (client.opponent != null) client.opponent.out.println(line);
                    }
                    else if (line.startsWith(NET_USER_REJECT)) {
                        if (client.opponent != null) {
                            client.opponent.out.println(line);
                            // 解绑
                            client.opponent.opponent = null;
                            client.opponent = null;
                        }
                    }
                    else if (client.opponent != null) {
                        client.opponent.out.println(line);
                    }
                }
            } catch (Exception e) {
            } finally {
                clients.remove(client);
                log("客户端断开: " + (client.name==null ? client.id : client.name));

                if (onUserDisconnected != null) onUserDisconnected.accept(client.id);
                for (Client c : clients) {
                    if (c.out != null) c.out.println(NET_USER_OUT + client.id);
                }
                try { socket.close(); } catch (IOException e) {}
            }
        });
    }

    private Client findClient(String id) {
        for (Client c : clients) {
            if (c.id.equals(id)) return c;
        }
        return null;
    }

    private void log(String msg) {
        if (onLog != null) onLog.accept(msg);
        System.out.println("[Server] " + msg);
    }
}