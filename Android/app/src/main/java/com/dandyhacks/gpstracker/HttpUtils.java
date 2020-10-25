package com.dandyhacks.gpstracker;

import java.io.IOException;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class HttpUtils {
    OkHttpClient client = new OkHttpClient();

    public static final MediaType JSON = MediaType.parse("application/json; charset=utf-8");
    public static final String BASE_URL = "http://18.222.146.208/";

    String newUserJSON(String uuid){ //uuid is 32 digits long (no hyphens)
        return "{\"uuid\":"+uuid+"}";
    }

    String newLocationJSON(String uuid, String address, String city, String state, String zip_code){
        return "{\"uuid\":"+uuid+","
                + "\"address\":\""+address+"\","
                + "\"city\":\""+city+"\","
                + "\"state\":\""+state+"\","
                + "\"zip_code\":\""+zip_code+"\"}";
    }

    String newSymptomsJSON(String uuid, int score, boolean close_contact){
        return "{\"uuid\":"+uuid+","
                + "\"score\":"+score+","
                + "\"close_contact\":"+close_contact+"}";
    }

    String newAtRiskJSON(String address, String city, String state, String zip_code, int risk_level){
        return "{\"address\":\""+address+"\","
                + "\"city\":\""+city+"\","
                + "\"state\":\""+state+"\","
                + "\"zip_code\":\""+zip_code+"\","
                + "\"risk_level\""+risk_level+"}";
    }

    String getRequest(String url) throws IOException {
        Request request = new Request.Builder()
                .url(url)
                .build();

        Response response = client.newCall(request).execute();
        return response.body().string();
    }

    String postRequest(String url, String json) throws IOException {
        RequestBody body = RequestBody.create(JSON, json);
        Request request = new Request.Builder()
                .url(url)
                .post(body)
                .build();
        Response response = client.newCall(request).execute();
        return response.body().string();
    }

    String buildGetRequest(String type) throws IOException { //type is 'user','gps','symptoms', or 'at_risk'
        return getRequest(BASE_URL+type+"/");
    }


    String buildUserPostRequest(String uuid) throws IOException {
        return postRequest(BASE_URL+"user/", newUserJSON(uuid));
    }

    String buildLocationPostRequest(String uuid, String address, String city, String state, String zip_code) throws IOException {
        return postRequest(BASE_URL+"user/", newLocationJSON(uuid, address, city, state, zip_code));
    }

    String buildSymptomsPostRequest(String uuid, int score, boolean close_contact) throws IOException {
        return postRequest(BASE_URL+"user/", newSymptomsJSON(uuid, score, close_contact));
    }

    String buildAtRiskPostRequest(String address, String city, String state, String zip_code, int risk_level) throws IOException {
        return postRequest(BASE_URL+"user/", newAtRiskJSON(address, city, state, zip_code, risk_level));
    }
}
