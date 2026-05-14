package com.example.water;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;

import java.util.Calendar;

public class AddRecordActivity extends AppCompatActivity {

    private TextInputEditText editAmount;
    private int savedAmount = 0;

    // Android 13+ 通知权限请求
    private final ActivityResultLauncher<String> notificationPermissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), granted -> {
                // 权限授予后执行后续逻辑
                afterSave();
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_record);

        editAmount = findViewById(R.id.edit_amount);
        MaterialButton btnSave = findViewById(R.id.btn_save);

        btnSave.setOnClickListener(v -> saveRecord());
    }

    /**
     * 读取用户输入，异步保存到 Room 数据库
     */
    private void saveRecord() {
        String amountStr = editAmount.getText().toString().trim();

        // 输入校验
        if (TextUtils.isEmpty(amountStr)) {
            Toast.makeText(this, "请输入饮水量", Toast.LENGTH_SHORT).show();
            return;
        }

        int amount = Integer.parseInt(amountStr);
        if (amount <= 0) {
            Toast.makeText(this, "饮水量必须大于 0", Toast.LENGTH_SHORT).show();
            return;
        }

        savedAmount = amount;

        // 创建记录实体
        WaterRecord record = new WaterRecord(
                amount,
                "白开水",       // 示例类别，可按需扩展
                System.currentTimeMillis()
        );

        // 异步保存到数据库
        AppDatabase db = AppDatabase.getInstance(this);
        AppDatabase.dbExecutor.execute(() -> {
            db.waterDao().insert(record);

            // 保存成功后回到主线程
            runOnUiThread(() -> {
                Toast.makeText(AddRecordActivity.this, "保存成功", Toast.LENGTH_SHORT).show();

                // 请求通知权限（Android 13+），然后执行后续操作
                requestNotificationPermissionIfNeeded();
            });
        });
    }

    /**
     * Android 13+ 需要动态请求通知权限
     */
    private void requestNotificationPermissionIfNeeded() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
                    != PackageManager.PERMISSION_GRANTED) {
                // 还未授权，请求权限；授权回调中会调用 afterSave()
                notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS);
                return;
            }
        }
        // 无需权限或已授权，直接执行后续
        afterSave();
    }

    /**
     * 保存完成后的后续操作：
     * 1. 异步获取健康贴士并弹窗展示
     * 2. 发送打卡通知
     */
    private void afterSave() {
        // 查询当日总量（用于通知显示）
        AppDatabase db = AppDatabase.getInstance(this);
        AppDatabase.dbExecutor.execute(() -> {
            Calendar cal = Calendar.getInstance();
            cal.set(Calendar.HOUR_OF_DAY, 0);
            cal.set(Calendar.MINUTE, 0);
            cal.set(Calendar.SECOND, 0);
            cal.set(Calendar.MILLISECOND, 0);
            long dayStart = cal.getTimeInMillis();

            cal.set(Calendar.HOUR_OF_DAY, 23);
            cal.set(Calendar.MINUTE, 59);
            cal.set(Calendar.SECOND, 59);
            cal.set(Calendar.MILLISECOND, 999);
            long dayEnd = cal.getTimeInMillis();

            int dailyTotal = db.waterDao().getDailyTotal(dayStart, dayEnd);

            int finalDailyTotal = dailyTotal;
            runOnUiThread(() -> {
                // 发送打卡通知
                NotificationHelper.showCheckInNotification(
                        AddRecordActivity.this, savedAmount, finalDailyTotal
                );

                // 异步获取健康贴士，成功后弹窗展示
                fetchAndShowTip();
            });
        });
    }

    /**
     * 异步获取健康贴士，并以 Dialog 形式展示
     */
    private void fetchAndShowTip() {
        HttpHelper.fetchHealthTip(new HttpHelper.Callback() {
            @Override
            public void onSuccess(String tip) {
                // 展示健康贴士弹窗
                new AlertDialog.Builder(AddRecordActivity.this)
                        .setTitle("💧 健康贴士")
                        .setMessage(tip)
                        .setPositiveButton("好的", (dialog, which) -> {
                            dialog.dismiss();
                            finish(); // 关闭添加页面
                        })
                        .setCancelable(false)
                        .show();
            }

            @Override
            public void onError(String error) {
                // 网络请求失败也关闭页面
                Toast.makeText(AddRecordActivity.this, error, Toast.LENGTH_SHORT).show();
                finish();
            }
        });
    }
}
