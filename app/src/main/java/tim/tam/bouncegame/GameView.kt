package tim.tam.bouncegame

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.view.SurfaceHolder
import android.view.SurfaceView

class GameView(context: Context) : SurfaceView(context), SurfaceHolder.Callback {

    private var gameThread: GameThread? = null
    private var x = 0f
    private var y = 0f
    private val paint = Paint().apply {
        color = Color.BLACK
    }
    private val ballRadius = 50f

    init {
        holder.addCallback(this)
    }

    override fun surfaceCreated(holder: SurfaceHolder) {
        gameThread = GameThread(holder)
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

    private inner class GameThread(private val surfaceHolder: SurfaceHolder) : Thread() {
        private var isRunning = false

        fun setRunning(isRunning: Boolean) {
            this.isRunning = isRunning
        }

        override fun run() {
            while (isRunning) {
                val canvas: Canvas? = surfaceHolder.lockCanvas()
                if (canvas != null) {
                    synchronized(surfaceHolder) {
                        update()
                        draw(canvas)
                    }
                    surfaceHolder.unlockCanvasAndPost(canvas)
                }
            }
        }

        private fun update() {
            x += 10f
            y += 10f

            if (x > width - ballRadius || x < 0 + ballRadius) {
                x = -x
            }

            if (y > height - ballRadius || y < 0 + ballRadius) {
                y = -y
            }
        }

        private fun draw(canvas: Canvas) {
            canvas.drawColor(Color.WHITE)
            canvas.drawCircle(x, y, ballRadius, paint)
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
