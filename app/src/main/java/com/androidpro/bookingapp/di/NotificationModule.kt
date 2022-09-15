package com.androidpro.bookingapp.di

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.androidpro.bookingapp.MainActivity
import com.androidpro.bookingapp.R
import com.androidpro.bookingapp.util.Constant
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.android.scopes.ServiceScoped

@Module
@InstallIn(NotificationModule::class)
object NotificationModule {

    @ServiceScoped
    @Provides
    fun provideNotificationManager(
        @ApplicationContext context: Context) = NotificationManagerCompat.from(context)

    @ServiceScoped
    @Provides
    fun providerNotificationBuilder(
        @ApplicationContext context: Context,
        pendingIntent: PendingIntent
    ) = NotificationCompat.Builder(
        context,
        Constant.NOTIFICATION_CHANNEL_ID,
    ).setAutoCancel(false)
        .setOngoing(true)
        .setSmallIcon(R.drawable.ic_launcher_background)
        .setContentTitle("Ongoing booking time")
        .setContentText("00:00:00").setContentIntent(pendingIntent)

    @ServiceScoped
    @Provides
    fun providerMainActivityPendingIntent(
        @ApplicationContext context: Context
    ):PendingIntent =
        PendingIntent.getActivity(
            context,
            143,
            Intent(context, MainActivity::class.java).apply {
                this.flags = Intent.FLAG_ACTIVITY_SINGLE_TOP
            },
            PendingIntent.FLAG_UPDATE_CURRENT
        )
}