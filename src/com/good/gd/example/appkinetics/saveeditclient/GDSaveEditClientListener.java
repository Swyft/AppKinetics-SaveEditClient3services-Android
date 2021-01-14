/**
 * GDSaveEditClientListener.java
 */
package com.good.gd.example.appkinetics.saveeditclient;

import android.util.Log;

import com.good.gd.icc.GDServiceClientListener;
import com.good.gd.icc.GDServiceError;
import com.good.gd.icc.GDServiceListener;

public class GDSaveEditClientListener implements GDServiceClientListener, GDServiceListener {

    private static final String TAG = GDSaveEditClientListener.class.getSimpleName();

    private static GDSaveEditClientListener instance;

    private OnReceiveAttachmentsEventListener onReceiveAttachmentsEventListener;

    private String[] pendingAttachments;

    public static GDSaveEditClientListener getInstance() {
        if (instance == null) {
            synchronized (GDSaveEditClientListener.class) {
                instance = new GDSaveEditClientListener();
            }
        }
        return instance;
    }

    /**
     * Used to set new OnReceiveMessageEventListener for handling event,
     * when application received edited files from SaveEdit Service application.
     *
     * @param listener OnReceiveMessageEventListener instance.
     */
    public void setOnReceiveAttachmentsEventListener(final OnReceiveAttachmentsEventListener listener) {
        this.onReceiveAttachmentsEventListener = listener;
    }

    /**
     * Used to get pending attachments.
     *
     * @return String array with pending attachments.
     */
    public String[] getPendingAttachments() {
        return pendingAttachments;
    }

    @Override
    public void onMessageSent(final String application, final String requestID,
                              final String[] attachments) {
        Log.d(TAG, "Message was successfully sent!");
    }

    @Override
    public void onReceivingAttachments(String application, int numberOfAttachments, String requestID) {
        Log.d(TAG, "onReceivingAttachments number of attachments: " + numberOfAttachments + " for requestID: " + requestID + "\n");
    }

    @Override
    public void onReceivingAttachmentFile(String application, String path, long size, String requestID) {
        Log.d(TAG, "onReceivingAttachmentFile attachment: " + path + " size: " + size + " for requestID: " + requestID + "\n");
    }

    @Override
    public void onReceiveMessage(final String application, final String service,
                                 final String version, final String method, final Object params,
                                 final String[] attachments, final String requestID) {
        Log.d(TAG, "Received message from: " + application);
    }

    @Override
    public void onReceiveMessage(final String application, final Object params,
                                 final String[] attachments, final String requestID) {
        if (params instanceof GDServiceError) {
            GDServiceError serviceError = (GDServiceError)params;
            Log.d(TAG, "onReceiveMessage: application:" + application +
                    ", requestID:" + requestID +
                    ", errCode:" + serviceError.getErrorCode() +
                    ", message:" + serviceError.getMessage() + "\n");
        } else if (onReceiveAttachmentsEventListener != null) {
            onReceiveAttachmentsEventListener.onReceiveAttachments(attachments);
        } else {
            pendingAttachments = attachments;
        }
    }

    /**
     * Public interface, which is used to handle received attachments.
     */
    public interface OnReceiveAttachmentsEventListener {
        /**
         * Called when attachments received in the GDServiceClientListener.
         *
         * @param attachments Array with attachments.
         */
        void onReceiveAttachments(final String[] attachments);
    }
}