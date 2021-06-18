package com.mycompany.app;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TimeZone;
import java.util.stream.Collectors;

import com.google.common.collect.ImmutableList;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

public class Dy {
    private static String cookie = "dy_did=e694c2fb9a876c14d5af8ea200061601; acf_did=e694c2fb9a876c14d5af8ea200061601; loginrefer=pt_e06b1cgm5ll7; Hm_lvt_e99aee90ec1b2106afe7ec3b199020a7=1623569502,1623636144,1623727764,1623841278; Hm_lvt_4dc4fb0549a56fe03ba53c022b1ff455=1623569501,1623636144,1623729200,1623841820; PHPSESSID=avc6vu0kene6ctt6eva94p3ha6; _dys_lastPageCode=page_studio_normal,page_studio_normal; acf_auth=f6f8hXgK%2FdIXv%2FEf9PD5jffkdpOiCJnG2DAW%2BvSyV1mGbt3EN7OfdkBLpfJtPuKFl6YZjS2oCEEaKAxIU%2B9U5vZhADsAkASlb5rZB9bQE58i%2BsKMJPw2; dy_auth=d8dazFJMf4sbfnll%2B%2F3HhYscEwGd2Xmpi5f3ACisA6fJlEv1rcpgxnC2ixVUAgOgoLnzCDXVjdHUCVIYx30Oo%2BqFOpHVqgRP9QJvOebnYDuogwuQFB4f; wan_auth37wan=b2e86df52be02Efunn678L2TfOnlxncsOSlerS5UmFNTJRtEhbyTMTfbbvXAtTcau%2FFOx86aQTcmzEC5r2McUEIFrMw17M9pLTzQm1A2PJFIZA3ixg; acf_uid=44867800; acf_username=44867800; acf_nickname=%E8%9C%97%E7%89%9B%E4%BE%A0c; acf_own_room=1; acf_groupid=1; acf_phonestatus=1; acf_avatar=https%3A%2F%2Fapic.douyucdn.cn%2Fupload%2Favatar_v3%2F201905%2F4f13ab195c434490a0c0d82daa2270ae_; acf_ct=0; acf_ltkid=90915315; acf_biz=1; acf_stk=871d8e3a631f433f; acf_ccn=b90a05cd2df31fdea87d2d4780aa5ddd; Hm_lpvt_4dc4fb0549a56fe03ba53c022b1ff455=1623934835; Hm_lpvt_e99aee90ec1b2106afe7ec3b199020a7=1623934835";
    private static HttpClient httpClient = HttpClient.newBuilder().build();
    private static String token = "";
    private static List<String> tokenArray = ImmutableList.of("acf_uid", "acf_biz", "acf_stk", "acf_ct", "acf_ltkid");
    private static int roomId = 8544405;
    private static SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss:SSS");
    private static long waitTime = 2000L;

    public static void main(String[] args) throws IOException, InterruptedException {

        df.setTimeZone(TimeZone.getTimeZone("Asia/Shanghai"));

        initToken();
        // List<Integer> followList = ImmutableList.of(8544405);

        while (true) {
            Set<Integer> followList = getFollowList();
            if (followList.contains(roomId)) {
                Thread.sleep(60_000L);
                checkin(roomId);
                Thread.sleep(100L);
                checkin(roomId);
                getRank(roomId);
                break;
            }
            Date day = new Date();
            System.out.println(df.format(day) + " 未开播等待" + waitTime + "ms");
            System.out.println("==================================================");
            Thread.sleep(waitTime);
        }
    }

    public static void initToken() {
        String tmpCookie = cookie.replaceAll(" ", "");
        Map<String, String> map = new HashMap<>();
        Arrays.stream(tmpCookie.split(";")).map(s -> s.split("=")).forEach(strings -> {
            map.put(strings[0], strings[1]);
        });

        token = tokenArray.stream().map(map::get).collect(Collectors.joining("_"));
        System.out.println("token: " + token);
    }

    public static void getRank(int roomId) throws IOException, InterruptedException {
        String url = "https://www.douyu.com/japi/roomuserlevel/apinc/getSignInRankInfoList?rid=" + roomId;
        String response = getResponse(url);

        JsonParser jsonParser = new JsonParser();
        JsonElement element = jsonParser.parse(response);
        JsonArray jsonArray = element.getAsJsonObject().get("data").getAsJsonArray();
        int rank = -1;
        for (JsonElement jsonElement : jsonArray) {
            JsonObject jsonObject = jsonElement.getAsJsonObject();
            if (jsonObject.get("nickname").getAsString().equals("蜗牛侠c")) {
                rank = jsonObject.get("rank").getAsInt();
            }
        }
        System.out.println("roomId: " + roomId + ", rank: " + rank);
    }

    public static void checkin(int roomId) throws IOException, InterruptedException {
        String url = "https://apiv2.douyucdn.cn/japi/roomuserlevel/apinc/checkIn";
        String body = "rid=" + roomId;
        String response = postResponse(url, body);
        System.out.println(response);
    }

    public static Set<Integer> getFollowList() throws IOException, InterruptedException {
        String url = "https://www.douyu.com/wgapi/livenc/liveweb/follow/list?sort=0&cid1=0";
        String response = getResponse(url);
        if (response.equals("") || response == null) {
            return new HashSet<>();
        }

        // System.out.println(response);

        JsonParser jsonParser = new JsonParser();
        JsonElement element = jsonParser.parse(response);
        JsonArray jsonArray = element.getAsJsonObject().get("data").getAsJsonObject().get("list").getAsJsonArray();

        Set<Integer> roomId = new HashSet<>();
        List<String> nickname = new LinkedList<>();
        for (JsonElement jsonElement : jsonArray) {
            JsonObject jsonObject = jsonElement.getAsJsonObject();
            // 未开播
            int showStatus = jsonObject.get("show_status").getAsInt();
            if (showStatus != 1) {
                continue;
            }
            // 视频循环
            int videoLoop = jsonObject.get("videoLoop").getAsInt();
            if (videoLoop == 1) {
                continue;
            }
            nickname.add(jsonObject.get("nickname").getAsString());
            roomId.add(jsonObject.get("room_id").getAsInt());
        }
        String line = String.join(", ", nickname);
        System.out.println("开播中：" + line);
        return roomId;
    }

    public static boolean getSignStatus() throws IOException, InterruptedException {
        String url = "https://www.douyu.com/japi/roomuserlevel/apinc/levelInfo?rid=703747";
        String response = getResponse(url);

        JsonParser jsonParser = new JsonParser();
        JsonElement element = jsonParser.parse(response);
        int done = element.getAsJsonObject().get("data").getAsJsonObject().get("signInInfo").getAsJsonObject()
                .get("done").getAsInt();

        System.out.println("done: " + done);
        return done == 1;
    }

    public static String getResponse(String url) {
        URI uri = URI.create(url);

        HttpRequest httpRequest = HttpRequest.newBuilder().header("cookie", cookie).uri(uri).build();

        HttpResponse<String> response;
        try {
            response = httpClient.send(httpRequest, HttpResponse.BodyHandlers.ofString());
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            return "";
        }
        System.out.println("url: " + url + " code:" + response.statusCode());
        // System.out.println("response: " + response.body());
        return response.body();
    }

    public static String postResponse(String url, String value) throws IOException, InterruptedException {
        URI uri = URI.create(url);

        HttpRequest httpRequest = HttpRequest.newBuilder().uri(uri)
                .header("content-Type", "application/x-www-form-urlencoded").header("cookie", cookie)
                .header("token", token).POST(HttpRequest.BodyPublishers.ofString(value)).build();

        HttpResponse<String> response = httpClient.send(httpRequest, HttpResponse.BodyHandlers.ofString());
        System.out.println("url: " + url + " code:" + response.statusCode());
        // System.out.println("body: " + value);
        // System.out.println("response: " + response.body());
        return response.body();
    }
}
