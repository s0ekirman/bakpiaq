package bakpiaq.com;

public class respon_login {
    private String status;
    private String message;
    private user data;

    // Constructor
    public respon_login() {}

    public respon_login(String status, String message, user data) {
        this.status = status;
        this.message = message;
        this.data = data;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public user getData() {
        return data;
    }

    public void setData(user data) {
        this.data = data;
    }
}
