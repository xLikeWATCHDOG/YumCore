package pw.yumc.YumCore.plugin;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

import lombok.AllArgsConstructor;
import lombok.Data;
import pw.yumc.YumCore.kit.HttpKit;

/**
 * Created with IntelliJ IDEA
 *
 * @author 喵♂呜
 *         Created on 2017/7/25 10:04.
 */
public class MigrateCodingToGogs {
    //private String site = "http://git.yumc.pw";
    private String site = "https://try.gogs.io";
    private String username = "xxxxxxxxx";// Gogs账号
    private String password = "xxxxxxxxx";// Gogs密码
    private Map<String, String> gogs_header;
    private String coding_token;

    /**
     * 删除所有项目
     */
    public void clearProject() {
        JSONObject userInfo = getUser();
        System.out.println("Gogs用户ID: " + userInfo.getString("id"));
        updateAuthorization(username);
        List<String> list = getNameListFromGogs();
        clearList(userInfo.getString("username"), list);
    }

    private void clearList(String u, List<String> list) {
        list.forEach(s -> delete("/repos/" + u + "/" + s));
    }

    /**
     * 镜像所有项目
     */
    public void migrateProject() {
        JSONObject userInfo = getUser();
        System.out.println("Gogs用户ID: " + userInfo.getString("id"));
        updateAuthorization(username);
        List<RepoInfo> list = getListFromCoding(userInfo.getString("id"));
        List<String> exist = getNameListFromGogs();
        list.removeIf(repoInfo -> exist.contains(repoInfo.getName()));
        migrateList(list);
    }

    /**
     * 更新Basic为token
     */
    private void updateAuthorization(String user) {
        String token = getUserToken(user);
        System.out.println("Gogs用户Toekn: " + token);
        getHeader().put("Authorization", "token " + token);
    }

    /**
     * 获取或创建用户Token
     *
     * @return 用户Token
     */
    private String getUserToken(String user) {
        JSONArray tks = JSON.parseArray(get("/users/" + user + "/tokens"));
        if (tks.isEmpty()) {
            return JSON.parseObject(post("/users/" + user + "/tokens", "name=migrate")).getString("sha1");
        } else {
            return tks.getJSONObject(0).getString("sha1");
        }
    }

    public Map<String, String> getHeader() {
        if (gogs_header == null) {
            gogs_header = new HashMap<>();
            gogs_header.put("Content-Type", " application/x-www-form-urlencoded");
            gogs_header.put("Authorization", "Basic " + new String(Base64.getEncoder().encode((username + ":" + password).getBytes())));
        }
        return gogs_header;
    }

    private JSONObject getUser() {
        return JSON.parseObject(get("/user"));
    }

    private List<String> getNameListFromGogs() {
        List<String> exist = new ArrayList<>();
        JSONArray arr = JSON.parseArray(get("/user/repos"));
        for (int i = 0; i < arr.size(); i++) {
            JSONObject obj = arr.getJSONObject(i);
            exist.add(obj.getString("name"));
        }
        return exist;
    }

    public List<RepoInfo> getListFromCoding(String uid) {
        Map<String, String> coding_header = new HashMap<>();
        coding_header.put("Authorization", "token " + coding_token);
        String json = HttpKit.get("https://coding.net/api/projects?page=1&pageSize=1000&type=all", null, coding_header);
        JSONArray projs = JSON.parseObject(json).getJSONObject("data").getJSONArray("list");
        System.out.println("Coding仓库数量: " + projs.size());
        List<RepoInfo> list = new ArrayList<>();
        for (int i = 0; i < projs.size(); i++) {
            JSONObject proj = projs.getJSONObject(i);
            list.add(new RepoInfo(proj.getString("https_url"), proj.getString("name"), username, password, uid, !proj.getBooleanValue("is_public"), proj.getString("description")));
        }
        return list;
    }

    public void migrateList(List<RepoInfo> projs) {
        long start = System.currentTimeMillis();
        ExecutorService executor = Executors.newFixedThreadPool(10);
        for (RepoInfo repoInfo : projs) {
            executor.execute(() -> migrate(repoInfo));
            migrate(repoInfo);
        }
        executor.shutdown();
        try {
            boolean loop;
            do { //等待所有任务完成
                loop = !executor.awaitTermination(2, TimeUnit.SECONDS); //阻塞，直到线程池里所有任务结束
            } while (loop);
        } catch (InterruptedException ignored) {
        }
        System.out.println(String.format("迁移完成 耗时 %s 毫秒!", System.currentTimeMillis() - start));
    }

    public void migrate(RepoInfo info) {
        String data = new StringBuilder()
                .append("clone_addr=")
                .append(eu(info.getUrl()))
                .append("&auth_username=")
                .append(eu(info.getUser()))
                .append("&auth_password=")
                .append(eu(info.getPass()))
                .append("&uid=")
                .append(info.getUid())
                .append("&repo_name=")
                .append(eu(info.getName()))
                .append("&private=")
                .append(info.isPrivate() ? "on" : "off")
                .append("&mirror=on&description=")
                .append(eu(info.getDescription()))
                .toString();
        try {
            get("/repos/" + username + "/" + info.getName());
            System.out.println("仓库: " + info.getName() + " 已存在!");
        } catch (Exception ex) {
            try {
                post("/repos/migrate", data);
            } catch (Exception ignored) {
                while (ignored.getCause() != null) {
                    ignored = (Exception) ignored.getCause();
                }
                System.out.println("仓库: " + info.getName() + " 异常: " + ignored.getClass().getName() + ":" + ignored.getMessage());
                // 忽略超时以及其他异常
            }
            System.out.println("仓库: " + info.getName() + " 迁移完成!");
        }
    }

    public String get(String url) {
        String result = HttpKit.get(site + "/api/v1" + url, getHeader());
        System.out.println("[DEBUG] URL: " + site + "/api/v1" + url + "\n[DEBUG] Result: " + result);
        return result;
    }

    public String delete(String url) {
        String result = HttpKit.delete(site + "/api/v1" + url, getHeader());
        System.out.println("[DEBUG] URL: " + site + "/api/v1" + url + "\n[DEBUG] Result: " + result);
        return result;
    }

    public String post(String url, String data) {
        String result = HttpKit.post(site + "/api/v1" + url, data, getHeader());
        System.out.println("[DEBUG] URL: " + site + "/api/v1" + url + " DATA: " + data + "\n[DEBUG] Result: " + result);
        return result;
    }

    public static String eu(String str) {
        try {
            return URLEncoder.encode(str, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            return "";
        }
    }

    @Data
    @AllArgsConstructor
    public static class RepoInfo {
        private String url;
        private String name;
        private String user;
        private String pass;
        private String uid;
        private boolean isPrivate;
        private String description;
    }
}
