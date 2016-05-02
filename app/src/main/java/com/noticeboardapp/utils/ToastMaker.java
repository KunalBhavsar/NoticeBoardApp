package com.noticeboardapp.utils;

import android.content.Context;
import android.widget.Toast;

/**
 * Created by kiran on 22-Apr-16.
 */
public class ToastMaker {

    public static void createLongToast(String toastMessage, Context context) {
        Toast.makeText(context, toastMessage, Toast.LENGTH_LONG).show();
    }

    public static void createLongToast(int toastMessageId, Context context) {
        Toast.makeText(context, context.getString(toastMessageId), Toast.LENGTH_LONG).show();
    }

    public static void createShortToast(String toastMessage, Context context) {
        Toast.makeText(context, toastMessage, Toast.LENGTH_SHORT).show();
    }

    public static void createShortToast(int toastMessageId, Context context) {
        Toast.makeText(context, context.getString(toastMessageId), Toast.LENGTH_SHORT).show();
    }

}
