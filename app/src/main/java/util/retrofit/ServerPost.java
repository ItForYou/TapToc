package util.retrofit;

public class ServerPost {
    public String getSuccess() {
        return success;
    }

    public void setSuccess(String success) {
        this.success = success;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getMb_photo_url() {
        return mb_photo_url;
    }

    public void setMb_photo_url(String mb_photo_url) {
        this.mb_photo_url = mb_photo_url;
    }

    String success;
    String message;
    String mb_photo_url;
}
