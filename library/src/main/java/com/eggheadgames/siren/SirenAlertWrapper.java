package com.eggheadgames.siren;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Build;

import java.lang.ref.WeakReference;

public class SirenAlertWrapper {

    private final WeakReference<Activity> mActivityRef;
    private final ISirenListener mSirenListener;
    private final SirenAlertType mSirenAlertType;
    private final String mMinAppVersion;
    private final SirenSupportedLocales mLocale;
    private final SirenHelper mSirenHelper;

    public SirenAlertWrapper(Activity activity, ISirenListener sirenListener, SirenAlertType sirenAlertType,
                             String minAppVersion, SirenSupportedLocales locale, SirenHelper sirenHelper) {
        this.mSirenListener = sirenListener;
        this.mSirenAlertType = sirenAlertType;
        this.mMinAppVersion = minAppVersion;
        this.mLocale = locale;
        this.mSirenHelper = sirenHelper;
        this.mActivityRef = new WeakReference<>(activity);
    }


    public void show() {
        Activity activity = mActivityRef.get();
        if (activity == null) {
            if (mSirenListener != null) {
                mSirenListener.onError(new NullPointerException("activity reference is null"));
            }
        } else if (Build.VERSION.SDK_INT >= 17 && !activity.isDestroyed() ||
                Build.VERSION.SDK_INT < 17 && !activity.isFinishing()) {

            initDialog(activity);

            if (mSirenListener != null) {
                mSirenListener.onShowUpdateDialog();
            }
        }
    }

    @SuppressLint("InflateParams")
    private AlertDialog initDialog(Activity activity) {
        AlertDialog.Builder alertBuilder = new AlertDialog.Builder(activity,
                                                                   android.R.style.Theme_Material_Dialog_Alert);

        alertBuilder.setTitle(mSirenHelper.getLocalizedString(mActivityRef.get(), R.string.update_available, mLocale));
        alertBuilder.setCancelable(false);

        alertBuilder.setMessage((mSirenHelper.getAlertMessage(mActivityRef.get(), mMinAppVersion, mLocale)));

        if (mSirenAlertType == SirenAlertType.FORCE
                || mSirenAlertType == SirenAlertType.OPTION
                || mSirenAlertType == SirenAlertType.SKIP) {
            alertBuilder.setPositiveButton(
                    mSirenHelper.getLocalizedString(mActivityRef.get(), R.string.update, mLocale),
                    new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            if (mSirenListener != null) {
                                mSirenListener.onLaunchGooglePlay();
                            }
                            mSirenHelper.openGooglePlay(mActivityRef.get());
                        }
                    });
        }

        if (mSirenAlertType == SirenAlertType.OPTION
                || mSirenAlertType == SirenAlertType.SKIP) {

            alertBuilder.setNegativeButton(
                    mSirenHelper.getLocalizedString(mActivityRef.get(), R.string.next_time, mLocale),
                    new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            if (mSirenListener != null) {
                                mSirenListener.onCancel();
                            }
                        }
                    });
        }

        AlertDialog alertDialog = alertBuilder.create();
        alertDialog.show();

        return alertDialog;
    }
}
