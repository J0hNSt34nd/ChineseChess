package main;

import Ai.Move;
import Tool.Network;
import Tool.PlayerRecord;
import Tool.UserProfile;
import com.fasterxml.jackson.databind.ObjectMapper;
import javafx.application.Platform;
import javafx.scene.image.Image;

import java.io.*;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

public class Game {
    public static final String[] LEVEL_NAMES = {"菜鸟", "入门", "棋士", "大师", "棋圣"};

    private static final String AVATAR_PATH = "/avatars/";
    private static final String DEFAULT_AVATAR = "level_1.png";
    private static final String FILE_PATH = "player_stats.json";

    private static final Map<String, Image> imageCache = new HashMap<>();
    private static final ObjectMapper mapper = new ObjectMapper();
    private static final Network network = new Network();

    private static boolean networkMode = false;

    private static UserProfile opponent = new UserProfile("AI - " + LEVEL_NAMES[0], "level_1.png");
    private static Data data;

    private static boolean isRed = false;

    private static class Data {
        private UserProfile userProfile;
        private String hostName;
        private int hostPort;
        private int aiLevel;
        private int countdownSeconds;
        private Map<String, PlayerRecord> stats;

        public Data() {
            LocalDateTime startTime = LocalDate.now().atTime(0, 0);
            Duration duration = Duration.between(startTime, LocalDateTime.now());

            userProfile = new UserProfile(String.format("ID - %d", duration.toSeconds()), "level_1.png");
            hostName = "127.0.0.1";
            hostPort = 54321;
            countdownSeconds = 30;
            stats = new HashMap<>();
        }

        public UserProfile getUserProfile() { return userProfile; }
        public void setUserProfile(UserProfile userProfile) { this.userProfile = userProfile; }

        public String getHostName() { return hostName; }
        public void setHostName(String hostName) { this.hostName = hostName; }

        public int getHostPort() { return hostPort; }
        public void setHostPort(int port) { hostPort = port; }

        public int getAiLevel() { return aiLevel; }
        public void setAiLevel(int level) { this.aiLevel = level; }

        public int getCountdownSeconds() { return countdownSeconds; }
        public void setCountdownSeconds(int seconds) { this.countdownSeconds = seconds; }

        public Map<String, PlayerRecord> getStats() { return stats; }
        public void setStats(Map<String, PlayerRecord> stats) { this.stats = stats; }
    }

    static {
        load();

        network.status.addListener((_, _, status) -> {
            if (status == Network.Status.CONNECTED)
                network.login(data.userProfile.getName(), data.userProfile.getAvatar());
        });

        network.setOnAccepted(client -> {
            System.out.println("全局监听：对方接受了挑战");

            setNetworkMode(true);
            setOpponent(client.profile());

            setisRed(true);
        });
    }

    public static void initNetworkListeners(Board board) {
        network.setOnMoved(move -> {
            System.out.println(move);
            board.handleNetworkMove(move);
        });

        //计时器废案
//        network.setOnAccepted(client -> {
//            setNetworkMode(true);
//            setOpponent(client.profile());
//            setisRed(false);
//
//            Platform.runLater(() -> {
//                board.reset();
//                board.setSide(false);
//            });
//        });

//        network.setOnUserOut(id -> {
//            System.out.println("User Left: " + id);
//        });

        network.setOnError(() -> {
            System.err.println("Network Error: " + network.getError());
        });
    }

    public static void load() {
        File file = new File(FILE_PATH);
        if (!file.exists()) {
            data = new Data();
            save();
            return;
        }

        try {
            data = mapper.readValue(file, Data.class);
            updateOpponent();
        } catch (IOException e) {
            System.err.println("警告：配置文件中的图片不存在，重置为默认头像。");
            data.userProfile.setAvatar(DEFAULT_AVATAR);
            data = new Data();
        }
    }

    public static void save() {
        try {
            mapper.writerWithDefaultPrettyPrinter().writeValue(new File(FILE_PATH), data);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void connectToHost(String hostName, int hostPort) {
        data.setHostName(hostName);
        data.setHostPort(hostPort);
        network.connect(hostName, hostPort);
        save();
    }

    public static Network getNetwork() { return network; }
    public static String getHostName() { return data.getHostName(); }
    public static int getHostPort() { return data.getHostPort(); }

    public static String getUserName() { return data.userProfile.getName(); }
    public static void setUserName(String name) {
        if (data.userProfile.getName().equals(name))
            return;
        data.userProfile.setName(name);
        save();
    }

    public static String getUserAvatar() { return data.userProfile.getAvatar(); }
    public static void setUserAvatar(String avatar) {
        if (data.userProfile.getAvatar().equals(avatar))
            return;
        data.userProfile.setAvatar(avatar);
        save();
    }

    public static Image getUserAvatarImage() { return getAvatarImage(data.userProfile.getAvatar()); }
    public static void setUserAvatarImage(Image image) {
        String url = image.getUrl();
        setUserAvatar(url.substring(url.lastIndexOf('/') + 1));
    }

    public static boolean isNetworkMode() { return networkMode; }

    public static void setNetworkMode(boolean network) {
        if (networkMode == network)
            return;

        networkMode = network;
        updateOpponent();
    }

    public static boolean getisRed() { return isRed; }
    public static void setisRed(boolean isred) { isRed = isred; }

    public static UserProfile getOpponent() { return opponent; }
    public static void setOpponent(UserProfile profile) { opponent = profile; }

    public static int getAILevel() { return data.aiLevel; }
    public static void setAILevel(int level) {
        data.aiLevel = level;
        if (!networkMode)
            updateOpponent();
        save();
    }

    public static Image getAIAvatarImage() { return getLevelImage(data.aiLevel); }

    public static Image getLevelImage(int level) {
        return getAvatarImage(String.format("level_%d.png", level + 1));
    }

    public static Image getAvatarImage(String avatar) {
        if (imageCache.containsKey(avatar))
            return imageCache.get(avatar);
        System.out.println(avatar);
        Image image = new Image(String.valueOf(Game.class.getResource(AVATAR_PATH + avatar)));
        imageCache.put(avatar, image);
        return image;
    }

    public static int getCountdownSeconds() { return data.getCountdownSeconds(); }
    public static void setCountdownSeconds(int seconds) {
        data.setCountdownSeconds(seconds);
        save();
    }

    public static PlayerRecord getRecord() {
        return data.getStats().getOrDefault(opponent.getName(), new PlayerRecord(0, 0, 0));
    }

    public static void updateRecord(int result) {
        PlayerRecord record = data.getStats().computeIfAbsent(opponent.getName(), k -> new PlayerRecord());
        switch (result) {
            case 1 -> record.addWin();
            case 0 -> record.addDraw();
            case -1 -> record.addLoss();
        }
        save();
    }

    private static void updateOpponent() {
        if (networkMode)
            opponent = null;
        else {
            if (opponent == null)
                opponent = new UserProfile();
            opponent.setName("AI - " + LEVEL_NAMES[data.aiLevel]);
            opponent.setAvatar(String.format("level_%d.png", data.aiLevel + 1));
        }
    }
}