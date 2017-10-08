package ninja.andrey.smslink;

/**
 * Created by Andrey on 10/6/2017.
 */

public class UserModel {
    public String email;
    public String phoneToken;

    public UserModel() {}

    public UserModel(String email, String phoneToken) {
        this.email = email;
        this.phoneToken = phoneToken;
    }
}
