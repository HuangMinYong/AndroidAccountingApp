package com.example.water;

import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;

public class AddRecordActivity extends AppCompatActivity {

    private TextInputEditText editAmount;

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

            // 保存成功后回到主线程，提示用户并关闭页面
            runOnUiThread(() -> {
                Toast.makeText(AddRecordActivity.this, "保存成功", Toast.LENGTH_SHORT).show();
                finish();
            });
        });
    }
}
