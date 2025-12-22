package tinker.sample.android.app;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.Settings;
import androidx.core.content.ContextCompat;

public class FilePermissionManager {

    private static final int STORAGE_PERMISSION_CODE = 100;
    private static final int MANAGE_STORAGE_PERMISSION_CODE = 101;

    private Context context;
    private Activity activity;

    public FilePermissionManager(Context context) {
        this.context = context;
        if (context instanceof Activity) {
            this.activity = (Activity) context;
        }
    }

    public FilePermissionManager(Activity activity) {
        this.context = activity;
        this.activity = activity;
    }

    // 检查文件管理权限
    public boolean checkFilePermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            // Android 11+ 检查 MANAGE_EXTERNAL_STORAGE 权限
            return Environment.isExternalStorageManager();
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            // Android 6-10 检查存储权限
            return ContextCompat.checkSelfPermission(context,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED;
        } else {
            // Android 5.1 及以下版本默认有权限
            return true;
        }
    }

    // 请求文件管理权限
    public void requestFilePermission() {
        if (activity == null) {
            throw new IllegalStateException("Activity is required for requesting permission");
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            requestManageStoragePermission();
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            requestStoragePermission();
        }
        // Android 5.1 及以下不需要请求权限
    }

    // 请求存储权限（Android 6-10）
    private void requestStoragePermission() {
        if (activity == null) return;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            activity.requestPermissions(
                    new String[] {
                            Manifest.permission.READ_EXTERNAL_STORAGE,
                            Manifest.permission.WRITE_EXTERNAL_STORAGE
                    },
                    STORAGE_PERMISSION_CODE
            );
        }
    }

    // 请求 MANAGE_EXTERNAL_STORAGE 权限（Android 11+）
    private void requestManageStoragePermission() {
        if (activity == null) return;

        try {
            Intent intent = new Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION);
            intent.addCategory("android.intent.category.DEFAULT");
            intent.setData(Uri.parse("package:" + activity.getPackageName()));
            activity.startActivityForResult(intent, MANAGE_STORAGE_PERMISSION_CODE);
        } catch (Exception e) {
            Intent intent = new Intent();
            intent.setAction(Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION);
            activity.startActivityForResult(intent, MANAGE_STORAGE_PERMISSION_CODE);
        }
    }

    // 处理权限请求结果（需要在 Activity 中调用）
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        if (requestCode == STORAGE_PERMISSION_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // 存储权限已授予
                onPermissionGranted();
            } else {
                // 存储权限被拒绝
                onPermissionDenied();
            }
        }
    }

    // 处理 Activity 返回结果（需要在 Activity 中调用）
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == MANAGE_STORAGE_PERMISSION_CODE) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                if (Environment.isExternalStorageManager()) {
                    // MANAGE_EXTERNAL_STORAGE 权限已授予
                    onPermissionGranted();
                } else {
                    // MANAGE_EXTERNAL_STORAGE 权限被拒绝
                    onPermissionDenied();
                }
            }
        }
    }

    // 权限授予回调
    private void onPermissionGranted() {
        // 在这里处理权限授予后的逻辑
        // 可以添加回调接口或 EventBus 通知
    }

    // 权限拒绝回调
    private void onPermissionDenied() {
        // 在这里处理权限拒绝后的逻辑
        // 可以显示提示信息或引导用户去设置
    }

    // 获取权限请求代码
    public static int getManageStoragePermissionCode() {
        return MANAGE_STORAGE_PERMISSION_CODE;
    }

    public static int getStoragePermissionCode() {
        return STORAGE_PERMISSION_CODE;
    }
}
