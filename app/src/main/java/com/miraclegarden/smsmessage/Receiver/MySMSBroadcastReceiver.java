package com.miraclegarden.smsmessage.Receiver;

import static android.provider.Telephony.Sms.Intents.getMessagesFromIntent;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.telephony.SmsMessage;

import androidx.annotation.NonNull;

import com.miraclegarden.smsmessage.Activity.MainActivity;
import com.miraclegarden.smsmessage.Activity.NotificationActivity;

import org.json.JSONObject;

import java.io.IOException;
import java.math.BigInteger;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.Map;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class MySMSBroadcastReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        // 从intent中获取消息
        SmsMessage[] smsMessages = getMessagesFromIntent(intent);
        // 获取短信发送者号码
        String senderNumber = smsMessages[0].getOriginatingAddress();
        // 组装短信内容
        StringBuilder text = new StringBuilder();
        for (SmsMessage smsMessage : smsMessages) {
            text.append(smsMessage.getMessageBody());
        }

        Submit(context, senderNumber, text);
        sendMessage("广播接收:" + "号码:" + senderNumber + "内容:" + text);
        // 获取卡槽位置
        //Bundle bundle = intent.getExtras();
        //int slot = bundle.getInt("android.telephony.extra.SLOT_INDEX", -1);
    }

    private void Submit(Context mContext, String title, StringBuilder context) {
        OkHttpClient client = new OkHttpClient();
        long time = System.currentTimeMillis();
        FormBody.Builder formBody = new FormBody.Builder();
        String sign = sign(String.valueOf(time), mContext, context.toString(), title);
        formBody.add("sign", sign);
        formBody.add("timestamp", String.valueOf(time));
        formBody.add("context", context.toString()).add("source", title).build();
        Request request = new Request.Builder().url(MainActivity.sp.getString("host", "")).post(formBody.build()).build();
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                sendMessage("提交失败: 网络异常" + e);
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                if (response.isSuccessful()) {
                    String str = response.body().string();
                    sendMessage(title + "短信提交成功:" + str);
                    return;
                }
                sendMessage("提交失败: 服务端异常" + response.code());
            }
        });
    }


    public void sendMessage(String msg) {
        try {
            NotificationActivity.sendMessage(msg);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private String sign(String time, Context mContext, String context, String source) {
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("context", context);
            jsonObject.put("source", source);
            String key = StingToMD5(getKey(mContext));
            //从字符串获取key
            jsonObject.put("key", key);
            jsonObject.put("time", time);
        } catch (Exception e) {
            e.printStackTrace();
        }
        sendMessage("密匙:"+ jsonObject.toString());
        return StingToMD5(jsonObject.toString());
    }

    /**
     * 获取分钟的时间戳（13位）
     *
     * @return
     */

    private static long getTimeMills() {
        return System.currentTimeMillis() / 1000 / 60;
    }


    private String getKey(Context context) throws MalformedURLException {
        SharedPreferences sp = context.getSharedPreferences("server", Context.MODE_PRIVATE);
        return sp.getString("host", "");
    }


    private String StingToMD5(String text) {
        try {
            byte[] s = MessageDigest.getInstance("md5").digest(text.getBytes(StandardCharsets.UTF_8));
            //16位
            return new BigInteger(1, s).toString(16);
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        return null;
    }

    /*@Override
    public void onReceive(Context context, Intent intent) {
        String dString = SmsHelper.getSmsBody(intent);
        String address = SmsHelper.getSmsAddress(intent);
        NotificationActivity.sendMessage("号码:" + address + "内容:" + dString);
        Log.i("dimos", dString + "," + address);
        //阻止广播继续传递，如果该receiver比系统的级别高，
        //那么系统就不会收到短信通知了
        abortBroadcast();
    }*/

}
