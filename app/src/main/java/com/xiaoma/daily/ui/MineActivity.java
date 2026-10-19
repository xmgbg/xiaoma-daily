package com.xiaoma.daily.ui;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Bundle;
import android.widget.Toast;

import com.xiaoma.daily.R;
import com.xiaoma.daily.data.AppDatabase;
import com.xiaoma.daily.data.DailyReport;
import com.xiaoma.daily.data.ReportJsonCodec;
import com.xiaoma.daily.databinding.ActivityMineBinding;
import com.xiaoma.daily.util.ReportDates;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class MineActivity extends Activity {
    private static final int PICK_AVATAR = 1;
    private static final int EXPORT_REPORTS = 2;
    private static final int IMPORT_REPORTS = 3;
    private ActivityMineBinding binding;

    @Override
    protected void onCreate(Bundle state) {
        super.onCreate(state);
        binding = ActivityMineBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        WindowInsetsHelper.apply(binding.getRoot());
        binding.navCalendar.setOnClickListener(view -> finish());
        binding.navMine.setSelected(true);
        binding.navMine.setContentDescription(getString(R.string.current_mine));
        binding.changeAvatar.setOnClickListener(view -> {
            Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
            intent.setType("image/*");
            intent.addCategory(Intent.CATEGORY_OPENABLE);
            startActivityForResult(intent, PICK_AVATAR);
        });
        binding.exportReports.setOnClickListener(view -> {
            Intent intent = new Intent(Intent.ACTION_CREATE_DOCUMENT);
            intent.setType("application/json");
            intent.addCategory(Intent.CATEGORY_OPENABLE);
            intent.putExtra(Intent.EXTRA_TITLE, "xiaoma-daily-" + ReportDates.today() + ".json");
            startActivityForResult(intent, EXPORT_REPORTS);
        });
        binding.importReports.setOnClickListener(view -> {
            Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
            intent.setType("application/json");
            intent.addCategory(Intent.CATEGORY_OPENABLE);
            startActivityForResult(intent, IMPORT_REPORTS);
        });
        File avatar = new File(getFilesDir(), "avatar.png");
        if (avatar.exists()) binding.avatar.setImageBitmap(BitmapFactory.decodeFile(avatar.getPath()));
    }

    @Override
    @SuppressWarnings("deprecation")
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode != RESULT_OK || data == null || data.getData() == null) return;
        Uri uri = data.getData();
        if (requestCode == PICK_AVATAR) saveAvatar(uri);
        if (requestCode == EXPORT_REPORTS) exportReports(uri);
        if (requestCode == IMPORT_REPORTS) importReports(uri);
    }

    private void saveAvatar(Uri uri) {
        AppDatabase.IO.execute(() -> {
            try {
                BitmapFactory.Options options = new BitmapFactory.Options();
                options.inJustDecodeBounds = true;
                try (InputStream stream = getContentResolver().openInputStream(uri)) {
                    BitmapFactory.decodeStream(stream, null, options);
                }
                int largest = Math.max(options.outWidth, options.outHeight);
                if (largest <= 0) throw new IOException("Invalid image");
                options.inSampleSize = 1;
                while (largest / options.inSampleSize > 512) options.inSampleSize *= 2;
                options.inJustDecodeBounds = false;
                Bitmap avatar;
                try (InputStream stream = getContentResolver().openInputStream(uri)) {
                    avatar = BitmapFactory.decodeStream(stream, null, options);
                }
                if (avatar == null) throw new IOException("Invalid image");
                int orientation = ExifInterface.ORIENTATION_NORMAL;
                try (InputStream stream = getContentResolver().openInputStream(uri)) {
                    orientation = new ExifInterface(stream).getAttributeInt(
                            ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL);
                } catch (IOException ignored) { }
                Matrix rotation = new Matrix();
                if (orientation == ExifInterface.ORIENTATION_ROTATE_90) rotation.postRotate(90);
                if (orientation == ExifInterface.ORIENTATION_ROTATE_180) rotation.postRotate(180);
                if (orientation == ExifInterface.ORIENTATION_ROTATE_270) rotation.postRotate(270);
                if (!rotation.isIdentity()) avatar = Bitmap.createBitmap(
                        avatar, 0, 0, avatar.getWidth(), avatar.getHeight(), rotation, true);
                try (FileOutputStream stream = openFileOutput("avatar.png", MODE_PRIVATE)) {
                    if (!avatar.compress(Bitmap.CompressFormat.PNG, 100, stream)) {
                        throw new IOException("Cannot save image");
                    }
                }
                Bitmap selected = avatar;
                runOnUiThread(() -> { if (!isFinishing()) binding.avatar.setImageBitmap(selected); });
            } catch (Exception exception) {
                runOnUiThread(() -> Toast.makeText(this, R.string.avatar_failed, Toast.LENGTH_SHORT).show());
            }
        });
    }

    private void exportReports(Uri uri) {
        binding.exportReports.setEnabled(false);
        AppDatabase.IO.execute(() -> {
            try {
                List<DailyReport> reports = AppDatabase.get(this).reports().allByDate();
                byte[] json = ReportJsonCodec.encode(reports).getBytes(StandardCharsets.UTF_8);
                try (OutputStream stream = getContentResolver().openOutputStream(uri, "w")) {
                    if (stream == null) throw new IOException("Cannot open file");
                    stream.write(json);
                }
                runOnUiThread(() -> Toast.makeText(this,
                        getString(R.string.export_success, reports.size()), Toast.LENGTH_LONG).show());
            } catch (Exception exception) {
                runOnUiThread(() -> Toast.makeText(this, R.string.export_failed, Toast.LENGTH_LONG).show());
            } finally {
                runOnUiThread(() -> binding.exportReports.setEnabled(true));
            }
        });
    }

    private void importReports(Uri uri) {
        binding.importReports.setEnabled(false);
        AppDatabase.IO.execute(() -> {
            try {
                StringBuilder json = new StringBuilder();
                try (InputStream stream = getContentResolver().openInputStream(uri)) {
                    if (stream == null) throw new IOException("Cannot open file");
                    try (InputStreamReader reader = new InputStreamReader(stream, StandardCharsets.UTF_8)) {
                        char[] buffer = new char[4096];
                        int count;
                        while ((count = reader.read(buffer)) != -1) json.append(buffer, 0, count);
                    }
                }
                List<DailyReport> reports = ReportJsonCodec.decode(json.toString());
                Set<String> existing = new HashSet<>();
                for (DailyReport report : AppDatabase.get(this).reports().allByDate()) {
                    existing.add(report.reportDate);
                }
                int duplicates = 0;
                for (DailyReport report : reports) if (existing.contains(report.reportDate)) duplicates++;
                int skipped = duplicates;
                runOnUiThread(() -> {
                    binding.importReports.setEnabled(true);
                    if (isFinishing()) return;
                    new AlertDialog.Builder(this)
                            .setMessage(getString(R.string.import_confirm, reports.size(), skipped))
                            .setNegativeButton(R.string.cancel, null)
                            .setPositiveButton(R.string.confirm_import, (dialog, which) -> applyImport(reports))
                            .show();
                });
            } catch (Exception exception) {
                runOnUiThread(() -> {
                    binding.importReports.setEnabled(true);
                    String reason = exception.getMessage() == null ? "文件无法读取" : exception.getMessage();
                    new AlertDialog.Builder(this)
                            .setMessage(getString(R.string.import_invalid, reason))
                            .setPositiveButton(android.R.string.ok, null).show();
                });
            }
        });
    }

    private void applyImport(List<DailyReport> reports) {
        binding.importReports.setEnabled(false);
        AppDatabase.IO.execute(() -> {
            try {
                int added = AppDatabase.get(this).runInTransaction(() -> {
                    int count = 0;
                    for (DailyReport report : reports) {
                        if (AppDatabase.get(this).reports().insertIfAbsent(report) != -1) count++;
                    }
                    return count;
                });
                runOnUiThread(() -> Toast.makeText(this,
                        getString(R.string.import_success, added, reports.size() - added), Toast.LENGTH_LONG).show());
            } catch (Exception exception) {
                runOnUiThread(() -> new AlertDialog.Builder(this)
                        .setMessage(getString(R.string.import_invalid, "写入失败，请重试"))
                        .setPositiveButton(android.R.string.ok, null).show());
            } finally {
                runOnUiThread(() -> binding.importReports.setEnabled(true));
            }
        });
    }
}
