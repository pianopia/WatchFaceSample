package net.syarihu.android.watchfacesample

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Point
import android.graphics.Rect
import android.support.wearable.complications.ComplicationData
import android.support.wearable.complications.rendering.ComplicationDrawable
import android.support.wearable.watchface.CanvasWatchFaceService
import android.support.wearable.watchface.WatchFaceService
import android.support.wearable.watchface.WatchFaceStyle
import android.text.format.DateUtils
import android.util.SparseArray
import android.view.SurfaceHolder
import java.util.Calendar

private const val COMPLICATION_MARGIN = 10

class DigitalWatchFaceService : CanvasWatchFaceService() {

    override fun onCreateEngine(): Engine {
        return Engine()
    }

    inner class Engine : CanvasWatchFaceService.Engine() {
        // 時間を取得するためのCalendar
        private val calendar: Calendar = Calendar.getInstance()
        // 時計の背景色
        private var backgroundColor = Color.BLACK
        // 時間を描画するためのPaint
        private val timePaint: Paint = Paint().apply {
            color = Color.WHITE
            textSize = 75f
            isAntiAlias = true
        }
        // 日付を描画するためのPaint
        private val datePaint: Paint = Paint().apply {
            color = Color.WHITE
            textSize = 20f
            isAntiAlias = true
        }
        // 時間の位置を保持するためのPoint
        private val timePosition: Point = Point()
        // 日付の位置を保持するためのPoint
        private val datePosition: Point = Point()
        // 時間の文字の大きさを保持するためのRect
        private val timeBounds: Rect = Rect()
        // 日付の文字の大きさを保持するためのRect
        private val dateBounds: Rect = Rect()
        private var complicationDrawableSparseArray =
                SparseArray<ComplicationDrawable>(ComplicationLocation.getComplicationIds().size).apply {
                    put(ComplicationLocation.LEFT.complicationId, ComplicationDrawable(applicationContext))
                    put(ComplicationLocation.RIGHT.complicationId, ComplicationDrawable(applicationContext))
                }

        override fun onCreate(holder: SurfaceHolder?) {
            super.onCreate(holder)
            setWatchFaceStyle(
                    WatchFaceStyle.Builder(this@DigitalWatchFaceService)
                            .setAcceptsTapEvents(true)
                            .setHideNotificationIndicator(true)
                            .build())
            setActiveComplications(*ComplicationLocation.getComplicationIds())
        }

        override fun onAmbientModeChanged(inAmbientMode: Boolean) {
            super.onAmbientModeChanged(inAmbientMode)
            backgroundColor = if (inAmbientMode)
                Color.BLACK
            else
                applicationContext.getColor(android.R.color.holo_blue_light)
            invalidate()
        }

        override fun onTimeTick() {
            super.onTimeTick()
            // 分が変わった時
            invalidate()
        }

        override fun onDraw(canvas: Canvas, bounds: Rect) {
            super.onDraw(canvas, bounds)
            val now = System.currentTimeMillis()
            // 時間を更新
            calendar.timeInMillis = now
            drawWatchFace(canvas)
            drawComplications(canvas, now)
        }

        override fun onComplicationDataUpdate(watchFaceComplicationId: Int, data: ComplicationData) {
            super.onComplicationDataUpdate(watchFaceComplicationId, data)
            complicationDrawableSparseArray[watchFaceComplicationId].run {
                setComplicationData(data)
            }
            invalidate()
        }

        override fun onTapCommand(tapType: Int, x: Int, y: Int, eventTime: Long) {
            super.onTapCommand(tapType, x, y, eventTime)
            if (tapType == WatchFaceService.TAP_TYPE_TAP) {
                ComplicationLocation.getComplicationIds().forEach {
                    val successfulTap = complicationDrawableSparseArray[it].onTap(x, y)
                    if (successfulTap)
                        return
                }
            }
        }

        private fun drawWatchFace(canvas: Canvas) {
            // 時間
            val strTime = DateUtils.formatDateTime(
                    this@DigitalWatchFaceService,
                    calendar.timeInMillis,
                    DateUtils.FORMAT_SHOW_TIME or DateUtils.FORMAT_24HOUR
            )
            // 時間のテキストの大きさを再取得
            timePaint.getTextBounds(strTime, 0, strTime.length, timeBounds)
            // 中央に配置するための座標を計算する
            timePosition.set(
                    canvas.width / 2 - timeBounds.width() / 2,
                    canvas.height / 2
            )
            // 日付
            val strDate = DateUtils.formatDateTime(
                    this@DigitalWatchFaceService,
                    calendar.timeInMillis,
                    DateUtils.FORMAT_SHOW_DATE or DateUtils.FORMAT_SHOW_WEEKDAY
            )
            // 日付のテキストの大きさを取得
            datePaint.getTextBounds(strDate, 0, strDate.length, dateBounds)
            datePosition.set(canvas.width / 2 - dateBounds.width() / 2, canvas.height / 2 + timeBounds.height() / 2 + (10 / resources.displayMetrics.density).toInt())

            canvas.drawColor(backgroundColor)
            canvas.drawText(strTime, timePosition.x.toFloat(), timePosition.y.toFloat(), timePaint)
            canvas.drawText(strDate, datePosition.x.toFloat(), datePosition.y.toFloat(), datePaint)
        }

        private fun drawComplications(canvas: Canvas, currentTimeMillis: Long) {
            val complicationSize = canvas.width / 4
            complicationDrawableSparseArray[ComplicationLocation.LEFT.complicationId].run {
                setBounds(
                        canvas.width / 2 - COMPLICATION_MARGIN - complicationSize,
                        datePosition.y + dateBounds.height(),
                        canvas.width / 2 - COMPLICATION_MARGIN,
                        datePosition.y + dateBounds.height() + complicationSize
                )
            }
            complicationDrawableSparseArray[ComplicationLocation.RIGHT.complicationId].run {
                setBounds(
                        canvas.width / 2 + COMPLICATION_MARGIN,
                        datePosition.y + dateBounds.height(),
                        canvas.width / 2 + COMPLICATION_MARGIN + complicationSize,
                        datePosition.y + dateBounds.height() + complicationSize
                )
            }
            ComplicationLocation.getComplicationIds().forEach { complicationId ->
                complicationDrawableSparseArray[complicationId].draw(canvas, currentTimeMillis)
            }
        }
    }
}
