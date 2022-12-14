package com.miraclegarden.smsmessage.Activity;

import android.Manifest;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.pm.PermissionInfo;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.miraclegarden.library.app.MiracleGardenActivity;
import com.miraclegarden.smsmessage.databinding.ActivityMainBinding;

import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends MiracleGardenActivity<ActivityMainBinding> {
    public static SharedPreferences sp;
    private static final String TAG = "MainActivity";

    private final String[] permissions = new String[]{
            Manifest.permission.READ_SMS,
            Manifest.permission.RECEIVE_SMS,
            Manifest.permission.RECEIVE_BOOT_COMPLETED
    };


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        sp = getSharedPreferences("server", MODE_PRIVATE);
        try {
            initPermission();
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        initData();
        initView();
    }

    private void initPermission() throws PackageManager.NameNotFoundException {
        List<String> permissionList=new ArrayList<>();
        if(ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.READ_SMS)!=PackageManager.PERMISSION_GRANTED){
            permissionList.add( Manifest.permission.READ_SMS);
        }
        if(ContextCompat.checkSelfPermission(MainActivity.this,Manifest.permission.SEND_SMS)!=PackageManager.PERMISSION_GRANTED){
            permissionList.add( Manifest.permission.SEND_SMS);
        }
        if(!permissionList.isEmpty()){
            String[] permissions=permissionList.toArray(new String[0]);
            ActivityCompat.requestPermissions(MainActivity.this,permissions,1);
        }

        PackageManager packageManager = this.getPackageManager();
        for (String permission : permissions) {
            PermissionInfo permissionInfo = packageManager.getPermissionInfo(permission, 0);
            CharSequence permissionName = permissionInfo.loadLabel(packageManager);
            if (ContextCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED) {
                // ???????????????
                Log.i(TAG, "???????????????" + permissionName + "???????????? ===>");
                if (ActivityCompat.shouldShowRequestPermissionRationale(this, permission)) {
                    // ?????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????
                    Log.i(TAG, "???????????????????????????" + permissionName + "??????????????????");
                } else {
                    int MY_REQUEST_CODE = 1000;
                    ActivityCompat.requestPermissions(this, permissions, MY_REQUEST_CODE);
                }
            } else {
                Log.i(TAG, "??????????????????" + permissionName + "????????????");
            }
        }
    }

    private void initData() {
        if (sp == null) {
            return;
        }
        binding.host.setText(sp.getString("host", ""));
    }

    private void initView() {
        binding.yes.setOnClickListener(v -> {
            if (sp == null) {
                return;
            }
            if (binding.host.getText().toString().length() == 0) {
                Toast.makeText(this, "????????????????????????????????????", Toast.LENGTH_SHORT).show();
                return;
            }

            if (!binding.host.getText().toString().startsWith("http")) {
                Toast.makeText(this, "?????????????????????", Toast.LENGTH_SHORT).show();
                return;
            }
            SharedPreferences.Editor edit = sp.edit();
            edit.putString("host", binding.host.getText().toString());
            edit.apply();
            startActivity(new Intent(MainActivity.this, NotificationActivity.class));
            finish();
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        PackageManager packageManager = this.getPackageManager();
        PermissionInfo permissionInfo = null;
        for (int i = 0; i < permissions.length; i++) {
            try {
                permissionInfo = packageManager.getPermissionInfo(permissions[i], 0);
            } catch (PackageManager.NameNotFoundException e) {
                e.printStackTrace();
            }
            CharSequence permissionName = permissionInfo.loadLabel(packageManager);
            if (grantResults[i] == PackageManager.PERMISSION_GRANTED) {
                Log.i(TAG, "???????????????" + permissionName + "?????????");
                Toast.makeText(this, "???????????????" + permissionName + "?????????", Toast.LENGTH_SHORT).show();
            } else {
                Log.i(TAG, "???????????????" + permissionName + "?????????");
                Toast.makeText(this, "???????????????" + permissionName + "?????????", Toast.LENGTH_SHORT).show();
            }
        }
    }

}
