package Tool;

import Ai.Move;
import javafx.application.Platform;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.function.Consumer;

public class Network {
    public enum Status {
        UNCONNECTED,
        CONNECTING,
        CONNECTED
    }

    public record Client(String id, UserProfile profile, boolean playing) {}

    private static final String USER_IN = "USER_IN:";
    private static final String USER_OUT = "USER_OUT:";
    private static final String USER_UPDATE = "USER_UPDATE:";
    private static final String USER_INVITE = "USER_INVITE:";
    private static final String USER_REJECT = "USER_REJECT:";
    private static final String USER_ACCEPT = "USER_ACCEPT:";
    private static final String USER_MOVE = "USER_MOVE:";
    private static final String USER_REQUEST = "USER_REQUEST:";
    private static final String USER_REPLY = "USER_REPLY:";
    private static final String USER_CANCEL = "USER_CANCEL:";
    private static final String USER_RETRACT = "USER_RETRACT:";
    private static final String USER_RESTART = "USER_RESTART:";

    public final ObjectProperty<Status> status = new SimpleObjectProperty<>(Status.UNCONNECTED);
    public final ObservableList<Client> clients = FXCollections.observableArrayList();

    private Socket socket;
    private PrintWriter out;
    private BufferedReader in;
    private String error;
    private Runnable onError;

    private Consumer<Client> onInvited;
    private Consumer<Client> onRejected;
    private Consumer<Client> onAccepted;
    private Consumer<Move> onMoved;
    private Consumer<String> onRequest;
    private Consumer<String> onReply;
    private Runnable onCancel;
    private Runnable onRetract;
    private Runnable onRestart;

    public Status getStatus() { return status.get(); }
    public String getError() {return error; }

    public Client getClient(String id) {
        for (Client client : clients) {
            if (client.id.equals(id))
                return client;
        }
        return null;
    }

    public void setOnError(Runnable handler) { onError = handler; }
    public void setOnInvited(Consumer<Client> handler) { onInvited = handler; }
    public void setOnRejected(Consumer<Client> handler) { onRejected = handler; }
    public void setOnAccepted(Consumer<Client> handler) { onAccepted = handler; }
    public void setOnMoved(Consumer<Move> handler) { onMoved = handler; }
    public void setOnRequest(Consumer<String> handler) { onRequest = handler; }
    public void setOnReply(Consumer<String> handler) { onReply = handler; }
    public void setOnCancel(Runnable handler) { onCancel = handler; }
    public void setOnRetract(Runnable handler) { onRetract = handler; }
    public void setOnRestart(Runnable handler) { onRestart = handler; }

    public void connect(String host, int port) {
        if (status.get() != Status.UNCONNECTED)
            return;

        error = null;
        status.setValue(Status.CONNECTING);

        new Thread(() -> {
            try {
                socket = new Socket(host, port);

                out = new PrintWriter(socket.getOutputStream(), true);
                in = new BufferedReader(new InputStreamReader(socket.getInputStream()));

                Platform.runLater(() -> status.setValue(Status.CONNECTED));

                String line;
                while (socket.isConnected() && (line = in.readLine()) != null) {
                    System.out.println("收到服务器消息：" + line);
                    if (line.startsWith(USER_IN)) {
                        String[] args = line.substring(USER_IN.length()).split(",");
                        Platform.runLater(() -> onUserIn(args));
                    } else if (line.startsWith(USER_OUT)) {
                        String id = line.substring(USER_OUT.length());
                        Platform.runLater(() -> onUserOut(id));
                    }
                    else if (line.startsWith(USER_UPDATE)) {
                        String[] args = line.substring(USER_UPDATE.length()).split(",");
                        Platform.runLater(() -> onUserUpdate(args));
                    }
                    else if (line.startsWith(USER_INVITE)) {
                        String id = line.substring(USER_INVITE.length());
                        Platform.runLater(() -> onUserAction(id, onInvited));
                    }
                    else if (line.startsWith(USER_REJECT)) {
                        String id = line.substring(USER_REJECT.length());
                        Platform.runLater(() -> onUserAction(id, onRejected));
                    }
                    else if (line.startsWith(USER_ACCEPT)) {
                        String id = line.substring(USER_ACCEPT.length());
                        Platform.runLater(() -> onUserAction(id, onAccepted));
                    }
                    else if (line.startsWith(USER_MOVE)) {
                        System.out.println("检测到移动指令");
                        String[] args = line.substring(USER_MOVE.length()).split(",");
                        Platform.runLater(() -> onUserMoved(args));
                    }
                    else if (line.startsWith(USER_REQUEST)) {
                        String request = line.substring(USER_REQUEST.length());
                        Platform.runLater(() -> onUserRequest(request));
                    }
                    else if (line.startsWith(USER_REPLY)) {
                        String reply = line.substring(USER_REPLY.length());
                        Platform.runLater(() -> onUserReply(reply));
                    }
                    else if (line.startsWith(USER_CANCEL) && onCancel != null)
                        Platform.runLater(onCancel);
                    else if (line.startsWith(USER_RETRACT) && onRetract != null)
                        Platform.runLater(onRetract);
                    else if (line.startsWith(USER_RESTART) && onRestart != null)
                        Platform.runLater(onRestart);
                }
            } catch (IOException e) {
                error = e.getMessage();
                if (onError != null)
                    Platform.runLater(onError);
            }finally {
                Platform.runLater(this::disconnect);
            }
        }).start();
    }

    public void disconnect() {
        if (status.get() == Status.UNCONNECTED)
            return;

        status.setValue((Status.UNCONNECTED));
        try {
            if (socket != null) {
                socket.close();
                socket = null;
            }
            if (out != null) {
                out.close();
                out = null;
            }
            if (in != null) {
                in.close();
                in = null;
            }
        }catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void login(String name, String avatar) {
        if (out != null) {
            String data = String.format("%s%s,%s", USER_IN, name, avatar);
            out.println(data);
            System.out.println("[Client Debug] 真实发送数据: " + data);
        } else {
            System.err.println("[Client Debug] 严重错误！Socket输出流(out)为null，无法发送数据！");
        }
    }

    public void invite(String userId) {
        if (out != null)
            out.println(USER_INVITE + userId);
    }

    public void rejectInvite(String userId) {
        if (out != null)
            out.println(USER_REJECT+ userId);
    }

    public void acceptInvite(String userId) {
        if (out != null)
            out.println(USER_ACCEPT + userId);
    }

    public void postMove(Move move) {
        if (out != null) {
            String msg = String.format("%s%d,%d,%d,%d", USER_MOVE, move.getOriRow(), move.getOriCol(), move.getNewRow(), move.getNewCol());
            out.println(msg);
            System.out.println("【底层发送成功】: " + msg);
        } else {
            System.err.println("【发送失败】out 流是 null，未连接服务器！");
        }
    }

    public void postRequest(String request) {
        if (out != null)
            out.println(USER_REQUEST + request);
    }

    public void postReply(String reply) {
        if (out != null)
            out.println(USER_REPLY + reply);
    }

    public void cancelRequest() {
        if (out != null)
            out.println(USER_CANCEL);
    }

    public void retract() {
        if (out != null)
            out.println(USER_RETRACT);
    }

    public void restart() {
        if (out != null)
            out.println(USER_RESTART);
    }

    public void separate() {
        if (out != null)
            out.println(USER_REJECT);
    }

    private void onUserIn(String[] args) {
        if (args.length > 2)
            clients.add(new Client(args[2], new UserProfile(args[0], args[1]), false));
        System.out.println("UserIn");
    }

    private void onUserOut(String id) {
        clients.removeIf(client -> client.id.equals(id));
    }

    private void onUserUpdate(String[] args) {
        if (args.length > 2) {
            String id = args[2];
            boolean playing = args.length > 3;
            for (int i = 0; i < clients.size(); ++i) {
                if (clients.get(i).id.equals(id)) {
                    clients.set(i, new Client(id, new UserProfile(args[0], args[1]), playing));
                    break;
                }
            }
        }
    }

    private void onUserAction(String id, Consumer<Client> handler) {
        if (handler == null)
            return;

        for (Client client : clients) {
            if (client.id.equals(id)) {
                handler.accept(client);
                break;
            }
        }
    }

    private void onUserMoved(String[] args) {
        System.out.println("[Debug] Network收到移动指令: " + java.util.Arrays.toString(args));

        if (args.length > 3) {
            if (onMoved != null) {
                onMoved.accept(new Move(
                        Integer.parseInt(args[0]), Integer.parseInt(args[1]),
                        Integer.parseInt(args[2]), Integer.parseInt(args[3]))
                );
            } else {
                System.err.println("[Error] onMoved 监听器是空的！Game.initNetworkListeners 可能没被调用！");
            }
        }
    }

    private void onUserRequest(String request) {
        if (onRequest != null)
            onRequest.accept(request);
    }

    private void onUserReply(String reply) {
        if (onReply != null)
            onReply.accept(reply);
    }
}
