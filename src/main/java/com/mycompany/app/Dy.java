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
    private static String cookie = "acf_did=55ee239c2df3e06113b70c1700061601; dy_did=55ee239c2df3e06113b70c1700061601; PHPSESSID=5217rv28ndeo094eukta4pk4b3; acf_uid=44867800; acf_username=44867800; acf_nickname=%E8%9C%97%E7%89%9B%E4%BE%A0c; acf_own_room=1; acf_groupid=1; acf_phonestatus=1; acf_avatar=https%3A%2F%2Fapic.douyucdn.cn%2Fupload%2Favatar_v3%2F201905%2F4f13ab195c434490a0c0d82daa2270ae_; acf_ct=0; acf_ltkid=90915290; acf_biz=1; acf_auth=edcc22yzZ9Zyezd2eC3DgfFVUTOVE4DN%2FJUhNHNZdLUr4elBTtlESxH3A1pB%2Bq3ka%2Fz8yrDDLT%2FU5pdKMSBC0L1sM%2BYjNDORrzcUxgRpKfl%2F5B3Mlm%2F7; dy_auth=30574dJdMnXIQyrXdWAvc5wLw4tGYjnNK97DY2otQmoHbv2SCqGrR%2BZFoYRI%2BYRwnefes3UNQOUuqhdRy0nnkQk6NFBpqqEZhjhRwLFpL6%2FoJvKzKO%2Bf; wan_auth37wan=a46c3d2990012QVkd4d1Y3K18w3hBSK1QJUQIPAMBMYw0jrpE7ArlHBWa%2FEckcTkZxcJua%2BG5ftqUE%2FccURQ%2BWc06ABdqg7i0uhLreFXtHPglp2J2w; acf_stk=8123085ad781bca8; Hm_lvt_e99aee90ec1b2106afe7ec3b199020a7=1612099684,1612174338,1612325071,1612762766; acf_ccn=0d3f10acf0203c27ec60564ca62b0364; Hm_lvt_4dc4fb0549a56fe03ba53c022b1ff455=1612768316; Hm_lpvt_4dc4fb0549a56fe03ba53c022b1ff455=1612768316; Hm_lpvt_e99aee90ec1b2106afe7ec3b199020a7=1612768321";
    private static HttpClient httpClient = HttpClient.newBuilder().build();
    private static String token = "";
    private static List<String> tokenArray = ImmutableList.of("acf_uid", "acf_biz", "acf_stk", "acf_ct", "acf_ltkid");
    private static int roomId = 8544405;
    private static SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    public static void main(String[] args) throws IOException, InterruptedException {

        df.setTimeZone(TimeZone.getTimeZone("Asia/Shanghai"));

        initToken();
        // List<Integer> followList = ImmutableList.of(8544405);

        while (true) {
            Set<Integer> followList = getFollowList();
            if (followList.contains(roomId)) {
                checkin(roomId);
                Thread.sleep(100L);
                checkin(roomId);
                getRank(roomId);
                break;
            }
            Date day = new Date();
            System.out.println(df.format(day));
            System.out.println("未开播等待500ms");
            Thread.sleep(5000L);
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
        if(response.equals("") || response == null) {
            return new HashSet<>();
        }

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
        //System.out.println("response: " + response.body());
        return response.body();
    }

    public static String postResponse(String url, String value) throws IOException, InterruptedException {
        URI uri = URI.create(url);

        HttpRequest httpRequest = HttpRequest.newBuilder()
                .uri(uri)
                .header("content-Type", "application/x-www-form-urlencoded")
                .header("cookie", cookie)
                .header("token", token)
                .POST(HttpRequest.BodyPublishers.ofString(value))
                .build();

        HttpResponse<String> response = httpClient.send(httpRequest, HttpResponse.BodyHandlers.ofString());
        System.out.println("url: " + url + " code:" + response.statusCode());
        //System.out.println("body: " + value);
        //System.out.println("response: " + response.body());
        return response.body();
    }
}
