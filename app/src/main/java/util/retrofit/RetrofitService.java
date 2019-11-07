package util.retrofit;

import java.util.Map;

import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.PartMap;

public interface RetrofitService {


    @Multipart
    @POST("/adm/json/query.php")
    Call<ServerPost> FileUpload(

            @PartMap Map<String, RequestBody> params
    );



}