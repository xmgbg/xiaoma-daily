package com.xiaoma.daily.ui;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.net.Uri;
import android.view.View;
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

final class MinePage {
    private final Activity activity;
    private static final int PICK_AVATAR = 1;
    private static final int EXPORT_REPORTS = 2;
    private static final int IMPORT_REPORTS = 3;
    private final ActivityMineBinding binding;

    MinePage(Activity activity, Runnable onCalendar) {
        this.activity = activity;
        binding = ActivityMineBinding.inflate(activity.getLayoutInflater());
        binding.avatar.setClipToOutline(true);
        binding.navCalendar.setOnClickListener(view -> onCalendar.run());
        binding.navMine.setSelected(true);
        binding.navMine.setContentDescription(activity.getString(R.string.current_mine));
        binding.changeAvatar.setOnClickListener(view -> {
            Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
            intent.setType("image/*");
            intent.addCategory(Intent.CATEGORY_OPENABLE);
            activity.startActivityForResult(intent, PICK_AVATAR);
        });
        binding.exportReports.setOnClickListener(view -> {
            Intent intent = new Intent(Intent.ACTION_CREATE_DOCUMENT);
            intent.setType("application/json");
            intent.addCategory(Intent.CATEGORY_OPENABLE);
            intent.putExtra(Intent.EXTRA_TITLE, "xiaoma-daily-" + ReportDates.today() + ".json");
            activity.startActivityForResult(intent, EXPORT_REPORTS);
        });
        binding.importReports.setOnClickListener(view -> {
            Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
            intent.setType("application/json");
            intent.addCategory(Intent.CATEGORY_OPENABLE);
            activity.startActivityForResult(intent, IMPORT_REPORTS);
        });
        File avatar = new File(activity.getFilesDir(), "avatar.png");
        if (avatar.exists()) AppDatabase.IO.execute(() -> {
            Bitmap saved = BitmapFactory.decodeFile(avatar.getPath());
            activity.runOnUiThread(() -> {
                if (!activity.isFinishing() && saved != null) binding.avatar.setImageBitmap(saved);
            });
        });
    }

    View getRoot() {
        return binding.getRoot();
    }

    void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode != Activity.RESULT_OK || data == null || data.getData() == null) return;
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
                try (InputStream stream = activity.getContentResolver().openInputStream(uri)) {
                    BitmapFactory.decodeStream(stream, null, options);
                }
                int largest = Math.max(options.outWidth, options.outHeight);
                if (largest <= 0) throw new IOException("Invalid image");
                options.inSampleSize = 1;
                while (largest / options.inSampleSize > 512) options.inSampleSize *= 2;
                options.inJustDecodeBounds = false;
                Bitmap avatar;
                try (InputStream stream = activity.getContentResolver().openInputStream(uri)) {
                    avatar = BitmapFactory.decodeStream(stream, null, options);
                }
                if (avatar == null) throw new IOException("Invalid image");
                int orientation = ExifInterface.ORIENTATION_NORMAL;
                try (InputStream stream = activity.getContentResolver().openInputStream(uri)) {
                    orientation = new ExifInterface(stream).getAttributeInt(
                            ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL);
                } catch (IOException ignored) { }
                Matrix rotation = new Matrix();
                if (orientation == ExifInterface.ORIENTATION_ROTATE_90) rotation.postRotate(90);
                if (orientation == ExifInterface.ORIENTATION_ROTATE_180) rotation.postRotate(180);
                if (orientation == ExifInterface.ORIENTATION_ROTATE_270) rotation.postRotate(270);
                if (!rotation.isIdentity()) avatar = Bitmap.createBitmap(
                        avatar, 0, 0, avatar.getWidth(), avatar.getHeight(), rotation, true);
                try (FileOutputStream stream = activity.openFileOutput("avatar.png", Activity.MODE_PRIVATE)) {
                    if (!avatar.compress(Bitmap.CompressFormat.PNG, 100, stream)) {
                        throw new IOException("Cannot save image");
                    }
                }
                Bitmap selected = avatar;
                activity.runOnUiThread(() -> { if (!activity.isFinishing()) binding.avatar.setImageBitmap(selected); });
            } catch (Exception exception) {
                activity.runOnUiThread(() -> Toast.makeText(activity, R.string.avatar_failed, Toast.LENGTH_SHORT).show());
            }
        });
    }

    private void exportReports(Uri uri) {
        binding.exportReports.setEnabled(false);
        AppDatabase.IO.execute(() -> {
            try {
                List<DailyReport> reports = AppDatabase.get(activity).reports().allByDate();
                byte[] json = ReportJsonCodec.encode(reports).getBytes(StandardCharsets.UTF_8);
                try (OutputStream stream = activity.getContentResolver().openOutputStream(uri, "w")) {
                    if (stream == null) throw new IOException("Cannot open file");
                    stream.write(json);
                }
                activity.runOnUiThread(() -> Toast.makeText(activity,
                        activity.getString(R.string.export_success, reports.size()), Toast.LENGTH_LONG).show());
            } catch (Exception exception) {
                activity.runOnUiThread(() -> Toast.makeText(activity, R.string.export_failed, Toast.LENGTH_LONG).show());
            } finally {
                activity.runOnUiThread(() -> binding.exportReports.setEnabled(true));
            }
        });
    }

    private void importReports(Uri uri) {
        binding.importReports.setEnabled(false);
        AppDatabase.IO.execute(() -> {
            try {
                StringBuilder json = new StringBuilder();
                try (InputStream stream = activity.getContentResolver().openInputStream(uri)) {
                    if (stream == null) throw new IOException("Cannot open file");
                    try (InputStreamReader reader = new InputStreamReader(stream, StandardCharsets.UTF_8)) {
                        char[] buffer = new char[4096];
                        int count;
                        while ((count = reader.read(buffer)) != -1) json.append(buffer, 0, count);
                    }
                }
                List<DailyReport> reports = ReportJsonCodec.decode(json.toString());
                Set<String> existing = new HashSet<>();
                for (DailyReport report : AppDatabase.get(activity).reports().allByDate()) {
                    existing.add(report.reportDate);
                }
                int duplicates = 0;
                for (DailyReport report : reports) if (existing.contains(report.reportDate)) duplicates++;
                int skipped = duplicates;
                activity.runOnUiThread(() -> {
                    binding.importReports.setEnabled(true);
                    if (activity.isFinishing()) return;
                    new AlertDialog.Builder(activity)
                            .setMessage(activity.getString(R.string.import_confirm, reports.size(), skipped))
                            .setNegativeButton(R.string.cancel, null)
                            .setPositiveButton(R.string.confirm_import, (dialog, which) -> applyImport(reports))
                            .show();
                });
            } catch (Exception exception) {
                activity.runOnUiThread(() -> {
                    binding.importReports.setEnabled(true);
                    String reason = exception.getMessage() == null ? "文件无法读取" : exception.getMessage();
                    new AlertDialog.Builder(activity)
                            .setMessage(activity.getString(R.string.import_invalid, reason))
                            .setPositiveButton(android.R.string.ok, null).show();
                });
            }
        });
    }

    private void applyImport(List<DailyReport> reports) {
        binding.importReports.setEnabled(false);
        AppDatabase.IO.execute(() -> {
            try {
                int added = AppDatabase.get(activity).runInTransaction(() -> {
                    int count = 0;
                    for (DailyReport report : reports) {
                        if (AppDatabase.get(activity).reports().insertIfAbsent(report) != -1) count++;
                    }
                    return count;
                });
                activity.runOnUiThread(() -> Toast.makeText(activity,
                        activity.getString(R.string.import_success, added, reports.size() - added), Toast.LENGTH_LONG).show());
            } catch (Exception exception) {
                activity.runOnUiThread(() -> new AlertDialog.Builder(activity)
                        .setMessage(activity.getString(R.string.import_invalid, "写入失败，请重试"))
                        .setPositiveButton(android.R.string.ok, null).show());
            } finally {
                activity.runOnUiThread(() -> binding.importReports.setEnabled(true));
            }
        });
    }
}
