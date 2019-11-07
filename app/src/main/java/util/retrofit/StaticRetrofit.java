package util.retrofit;

import okhttp3.RequestBody;

public class StaticRetrofit {
    public static RequestBody toRequestBody (String value) {
        RequestBody body = RequestBody.create(okhttp3.MultipartBody.FORM, value);
        return body ;
    }
}
