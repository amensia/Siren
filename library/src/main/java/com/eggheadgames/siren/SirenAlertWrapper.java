package com.eggheadgames.siren;

import android.app.Activity;
import android.app.Dialog;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import java.lang.ref.WeakReference;

public class SirenAlertWrapper {

    final WeakReference<Activity> mActivityRef;
    private final ISirenListener mSirenListener;
    private final SirenAlertType mSirenAlertType;
    private final String mMinAppVersion;
    private int mTheme;

    public SirenAlertWrapper(Activity activity, ISirenListener sirenListener, SirenAlertType sirenAlertType, String minAppVersion) {
        this.mSirenListener = sirenListener;
        this.mSirenAlertType = sirenAlertType;
        this.mMinAppVersion = minAppVersion;
        mActivityRef = new WeakReference<>(activity);
    }

    public SirenAlertWrapper(Activity activity, ISirenListener sirenListener, SirenAlertType sirenAlertType, String minAppVersion, int theme) {
        this(activity, sirenListener, sirenAlertType, minAppVersion);
        this.mTheme = theme;
    }

    public void show() {
        if (mActivityRef.get() != null) {
            Dialog dialog;
            if (mTheme > 0) {
                dialog = new Dialog(mActivityRef.get(), mTheme);
            } else {
                dialog = new Dialog(mActivityRef.get());
            }
            setupDialog(dialog);
            dialog.setCancelable(false);
            dialog.show();

            if (mSirenListener != null) {
                mSirenListener.onShowUpdateDialog();
            }


        } else {
            if (mSirenListener != null) {
                mSirenListener.onError(new NullPointerException("activity reference is null"));
            }
        }
    }

    private void setupDialog(final Dialog dialog) {
        dialog.setTitle(R.string.update_available);
        dialog.setContentView(R.layout.siren_dialog);
        TextView message = (TextView) dialog.findViewById(R.id.tvSirenAlertMessage);
        Button update = (Button) dialog.findViewById(R.id.btnSirenUpdate);
        Button nextTime = (Button) dialog.findViewById(R.id.btnSirenNextTime);
        final Button skip = (Button) dialog.findViewById(R.id.btnSirenSkip);

        message.setText(SirenHelper.getAlertMessage(mActivityRef.get(), mMinAppVersion));

        if (mSirenAlertType == SirenAlertType.FORCE
                || mSirenAlertType == SirenAlertType.OPTION
                || mSirenAlertType == SirenAlertType.SKIP) {
            update.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (mSirenListener != null) {
                        mSirenListener.onLaunchGooglePlay();
                    }
                    dialog.dismiss();
                    SirenHelper.openGooglePlay(mActivityRef.get());
                }
            });
        }

        if (mSirenAlertType == SirenAlertType.OPTION
                || mSirenAlertType == SirenAlertType.SKIP) {
            nextTime.setVisibility(View.VISIBLE);
            nextTime.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if(mSirenListener != null) {
                        mSirenListener.onCancel();
                    }
                    dialog.dismiss();
                }
            });
        }
        if (mSirenAlertType == SirenAlertType.SKIP) {
            skip.setVisibility(View.VISIBLE);
            skip.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (mSirenListener != null) {
                        mSirenListener.onSkipVersion();
                    }

                    SirenHelper.setVersionSkippedByUser(mActivityRef.get(), mMinAppVersion);
                    dialog.dismiss();
                }
            });
        }
    }
}