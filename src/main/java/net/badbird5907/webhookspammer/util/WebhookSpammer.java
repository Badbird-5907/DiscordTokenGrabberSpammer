package net.badbird5907.webhookspammer.util;

import com.google.gson.JsonObject;
import lombok.SneakyThrows;
import net.badbird5907.webhookspammer.MainGUI;
import okhttp3.*;
import org.apache.commons.lang3.time.StopWatch;

import javax.swing.*;
import java.util.Base64;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

public class WebhookSpammer {
    private String url,message,name,avatar;
    long error,ratelimit,delay;
    public WebhookSpammer(String url,String message,String name,String avatar,long error,long ratelimit,long delay){
        this.url = url;
        this.message = message;
        this.name = name;
        this.avatar = avatar;
        this.error = error;
        this.ratelimit = ratelimit;
        this.delay = delay;
        spam();
    }
    private void spam(){
        StopWatch watch = new StopWatch();
        watch.start();
        String webhook = url;
        OkHttpClient client = new OkHttpClient().newBuilder()
                .build();
        MediaType mediaType = MediaType.parse("application/json");
        new Thread("spammer thread"){
            @SneakyThrows
            @Override
            public void run() {
                while (true){
                    SwingUtilities.invokeLater(()->{
                        MainGUI.getFrame().repaint();
                    });
                    sleep(delay);
                    JsonObject jsonObject = new JsonObject();
                    jsonObject.addProperty("content",message.replace("%token%",genRandomBase64()));
                    if (name != null && name != "")
                        jsonObject.addProperty("username",name);
                    if (avatar != null && avatar != "")
                        jsonObject.addProperty("avatar_url",avatar);
                    String json = jsonObject.toString();
                    RequestBody body = RequestBody.create(mediaType, json);
                    Request request = new Request.Builder()
                            .url(webhook)
                            .method("POST", body)
                            .addHeader("Content-Type", "application/json")
                            .build();
                    Response response = client.newCall(request).execute();
                    if (response.code() == 429) {
                        System.out.println("Received code 429, sleeping for " + ratelimit + " ms.");
                        sleep(ratelimit);
                    }
                    else if (!response.isSuccessful()) {
                        System.out.println("Received error, sleeping for " + error + " ms.");
                        sleep(error);
                    }
                    System.out.println(response.code() + " | " + response.isSuccessful());
                    if (response.code() == 404){
                        watch.stop();
                        long millis = watch.getTime();
                        JOptionPane.showMessageDialog(MainGUI.getFrame(),"Took them " + String.format("%d min, %d sec to delete lmao",
                                TimeUnit.MILLISECONDS.toMinutes(millis),
                                TimeUnit.MILLISECONDS.toSeconds(millis) -
                                        TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(millis))));
                        break;
                    }
                }
            }
        }.start();
    }
    public static String genRandomBase64(){
        String s = UUID.randomUUID().toString();
        return Base64.getEncoder().encodeToString(s.getBytes());
    }
}
