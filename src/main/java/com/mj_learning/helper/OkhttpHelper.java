package com.mj_learning.helper;


import com.squareup.okhttp.*;

import java.io.IOException;


public class OkhttpHelper {

    public  static String Get(String uri) throws IOException {
        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder()
                .url(uri)
                .build();
        Response response=client.newCall(request).execute();
        return response.body().string();
    }
    public static String Post(String url,String jsonData) throws IOException {
        MediaType mediaType=MediaType.parse("application/json; charset=utf-8");
        OkHttpClient client=new OkHttpClient();
        RequestBody body=RequestBody.create(mediaType,jsonData);
        Request request=new Request.Builder()
                        .url(url)
                        .post(body)
                        .build();
        Response response=client.newCall(request).execute();
        return  response.body().string();
    }
}
