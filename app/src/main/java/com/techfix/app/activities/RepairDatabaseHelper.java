
package com.techfix.app.activities;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;
import java.util.List;

public class RepairDatabaseHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "techfix_offline.db";
    private static final int DATABASE_VERSION = 1;
    private static final String TABLE_REPAIRS = "cached_repairs";

    public RepairDatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(
                "CREATE TABLE " + TABLE_REPAIRS + " (" +
                        "appointment_id TEXT PRIMARY KEY, " +
                        "customer_id TEXT NOT NULL, " +
                        "category TEXT, " +
                        "brand TEXT, " +
                        "model TEXT, " +
                        "problem TEXT, " +
                        "branch TEXT, " +
                        "preferred_date TEXT, " +
                        "status TEXT, " +
                        "payment_amount REAL, " +
                        "payment_status TEXT, " +
                        "created_at INTEGER" +
                        ")"
        );
    }

    @Override
    public void onUpgrade(
            SQLiteDatabase db,
            int oldVersion,
            int newVersion
    ) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_REPAIRS);
        onCreate(db);
    }

    public static class CachedRepair {
        public String appointmentId;
        public String customerId;
        public String category;
        public String brand;
        public String model;
        public String problem;
        public String branch;
        public String preferredDate;
        public String status;
        public Double paymentAmount;
        public String paymentStatus;
        public long createdAt;
    }

    public void replaceCustomerRepairs(
            String customerId,
            List<CachedRepair> repairs
    ) {
        SQLiteDatabase db = getWritableDatabase();

        db.beginTransaction();

        try {
            db.delete(
                    TABLE_REPAIRS,
                    "customer_id = ?",
                    new String[]{customerId}
            );

            for (CachedRepair repair : repairs) {
                ContentValues values = new ContentValues();

                values.put("appointment_id", repair.appointmentId);
                values.put("customer_id", customerId);
                values.put("category", repair.category);
                values.put("brand", repair.brand);
                values.put("model", repair.model);
                values.put("problem", repair.problem);
                values.put("branch", repair.branch);
                values.put("preferred_date", repair.preferredDate);
                values.put("status", repair.status);

                if (repair.paymentAmount == null) {
                    values.putNull("payment_amount");
                } else {
                    values.put("payment_amount", repair.paymentAmount);
                }

                values.put("payment_status", repair.paymentStatus);
                values.put("created_at", repair.createdAt);

                db.insertOrThrow(TABLE_REPAIRS, null, values);
            }

            db.setTransactionSuccessful();

        } finally {
            db.endTransaction();
        }
    }

    public List<CachedRepair> getCustomerRepairs(String customerId) {

        List<CachedRepair> repairs = new ArrayList<>();
        SQLiteDatabase db = getReadableDatabase();

        try (Cursor cursor = db.query(
                TABLE_REPAIRS,
                null,
                "customer_id = ?",
                new String[]{customerId},
                null,
                null,
                "created_at DESC"
        )) {

            while (cursor.moveToNext()) {
                CachedRepair repair = new CachedRepair();

                repair.appointmentId = cursor.getString(
                        cursor.getColumnIndexOrThrow("appointment_id")
                );

                repair.customerId = cursor.getString(
                        cursor.getColumnIndexOrThrow("customer_id")
                );

                repair.category = cursor.getString(
                        cursor.getColumnIndexOrThrow("category")
                );

                repair.brand = cursor.getString(
                        cursor.getColumnIndexOrThrow("brand")
                );

                repair.model = cursor.getString(
                        cursor.getColumnIndexOrThrow("model")
                );

                repair.problem = cursor.getString(
                        cursor.getColumnIndexOrThrow("problem")
                );

                repair.branch = cursor.getString(
                        cursor.getColumnIndexOrThrow("branch")
                );

                repair.preferredDate = cursor.getString(
                        cursor.getColumnIndexOrThrow("preferred_date")
                );

                repair.status = cursor.getString(
                        cursor.getColumnIndexOrThrow("status")
                );

                int amountIndex = cursor.getColumnIndexOrThrow(
                        "payment_amount"
                );

                repair.paymentAmount = cursor.isNull(amountIndex)
                        ? null
                        : cursor.getDouble(amountIndex);

                repair.paymentStatus = cursor.getString(
                        cursor.getColumnIndexOrThrow("payment_status")
                );

                repair.createdAt = cursor.getLong(
                        cursor.getColumnIndexOrThrow("created_at")
                );

                repairs.add(repair);
            }
        }

        return repairs;
    }
}
