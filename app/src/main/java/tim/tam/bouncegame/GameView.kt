package tim.tam.bouncegame

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.util.Log
import android.view.MotionEvent
import android.view.SurfaceHolder
import android.view.SurfaceView

class GameView(context: Context) : SurfaceView(context), SurfaceHolder.Callback {

    private var gameThread: GameThread? = null
    private var x = 0f
    private var y = 0f
    private var xSpeed = 10f
    private var ySpeed = 10f
    private val paint = Paint().apply {
        color = Color.BLACK
    }
    private val ballRadius = 50f

    init {
        holder.addCallback(this)
    }

    override fun surfaceCreated(holder: SurfaceHolder) {
        gameThread = GameThread(holder)
        gameThread?.setRunning(true)
        gameThread?.start()
    }

    override fun surfaceChanged(holder: SurfaceHolder, format: Int, width: Int, height: Int) {}

    override fun surfaceDestroyed(holder: SurfaceHolder) {
        var retry = true
        gameThread?.setRunning(false)
        while (retry) {
            try {
                gameThread?.join()
                retry = false
            } catch (e: InterruptedException) {
                e.printStackTrace()
            }
        }
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                // Get the touch coordinates
                val touchX = event.x
                val touchY = event.y

                // Calculate the distance between the touch coordinates and the ball's center
                val distanceX = touchX - x
                val distanceY = touchY - y

                // Adjust the ball's speed based on the distance from the touch point
                xSpeed = distanceX / 10f
                ySpeed = distanceY / 10f

                return true
            }
        }
        return super.onTouchEvent(event)
    }

    private inner class GameThread(private val surfaceHolder: SurfaceHolder) : Thread() {
        private var isRunning = false

        fun setRunning(isRunning: Boolean) {
            this.isRunning = isRunning
        }

        override fun run() {
            while (isRunning) {
                val canvas = surfaceHolder.lockCanvas()
                if (canvas != null) {
                    synchronized(surfaceHolder) {
                        update(canvas.width, canvas.height)
                        draw(canvas)
                    }
                    surfaceHolder.unlockCanvasAndPost(canvas)
                }

                try {
                    Thread.sleep(16)
                } catch (e: InterruptedException) {
                    e.printStackTrace()
                }
            }
        }

        private fun update(canvasWidth: Int, canvasHeight: Int) {
            x += xSpeed
            y += ySpeed

            if (x + ballRadius > canvasWidth || x - ballRadius < 0) {
                xSpeed = -xSpeed
            }

            if (y + ballRadius > canvasHeight || y - ballRadius < 0) {
                ySpeed = -ySpeed
            }

            Log.d("GameThread", "x: $x, y: $y")
        }

        private fun draw(canvas: Canvas) {
            canvas.drawColor(Color.WHITE)
            val centerX = canvas.width / 2f
            val centerY = canvas.height / 2f
            canvas.drawCircle(centerX + x, centerY + y, ballRadius, paint)
        }
    }

    fun pause() {
        gameThread?.setRunning(false)
        try {
            gameThread?.join()
        } catch (e: InterruptedException) {
            e.printStackTrace()
        }
    }

    fun resume() {
        gameThread?.setRunning(true)
        gameThread?.start()
    }
}
