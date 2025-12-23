package Tool;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class UserProfile {
    private String name;
    private String avatar;
    private final String DEFULT_AVATAR = "male_1.png";

    public UserProfile() {}

    public UserProfile(String name, String avatar) {

        this.name = name;
        this.avatar = avatar;
    }

    public String getName() {return name;}
    public void setName(String name) {this.name = name; }

    public String getAvatar() {
        if (avatar == null) {
            return DEFULT_AVATAR;
        }
        return avatar;
    }

    private String getUsername()
    {
        try
        {
            InputStream input = new FileInputStream("chess.properties");
            Properties temp = new Properties();
            temp.load(input);
            return temp.getProperty("written_User","");

        }catch (IOException e)
        {
            System.out.println("检验用户名错误");
            e.printStackTrace();
            return "";
        }
    }

    private boolean isGuest()
   {
        String username = getUsername();
        if(username == null)
        {
            System.out.println("是游客");
            return true;
        }
        if(username.trim().isEmpty())
        {
            System.out.println("是游客");
            return true;
        }
        System.out.println("不是游客");
        return false;
   }



    public void setAvatar(String Avatar) {
        this.avatar = avatar;
    }
}
