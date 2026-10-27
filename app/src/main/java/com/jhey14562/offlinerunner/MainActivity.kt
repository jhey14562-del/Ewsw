package com.jhey14562.offlinerunner

import android.app.Activity
import android.os.Bundle
import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.RectF
import android.view.MotionEvent
import android.view.View
import android.view.WindowManager
import kotlin.math.max
import kotlin.random.Random

class MainActivity : Activity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN)
        setContentView(RunnerView(this))
    }
}

private data class Obstacle(var x: Float, val width: Float, val height: Float)

private class RunnerView(context: Context) : View(context) {
    private val prefs = context.getSharedPreferences("runner_scores", Context.MODE_PRIVATE)
    private val paint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val obstacles = mutableListOf<Obstacle>()
    private var playerY = 0f
    private var velocityY = 0f
    private var groundY = 0f
    private var playerSize = 0f
    private var speed = 0f
    private var spawnTimer = 0f
    private var distance = 0f
    private var best = prefs.getInt("best", 0)
    private var started = false
    private var gameOver = false
    private var lastTime = 0L
    private var stripeOffset = 0f
    private val gravity get() = height * 2.7f
    private val jumpPower get() = -height * 0.92f

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        val now = System.nanoTime()
        val dt = if (lastTime == 0L) 0.016f else ((now - lastTime) / 1_000_000_000f).coerceIn(0f, 0.035f)
        lastTime = now
        if (width == 0 || height == 0) return
        if (playerSize == 0f) {
            playerSize = width * 0.095f
            groundY = height * 0.78f
            playerY = groundY - playerSize
            speed = width * 0.62f
        }
        drawBackground(canvas)
        if (started && !gameOver) updateGame(dt)
        drawGround(canvas)
        drawPlayer(canvas)
        drawObstacles(canvas)
        drawHud(canvas)
        if (!started) drawCenterMessage(canvas, "OFFLINE RUNNER", "Tap to start • Tap to jump")
        else if (gameOver) drawCenterMessage(canvas, "GAME OVER", "Score: ${distance.toInt()}   •   Tap to retry")
        postInvalidateOnAnimation()
    }

    private fun drawBackground(c: Canvas) {
        c.drawColor(android.graphics.Color.rgb(16, 23, 42))
        paint.color = android.graphics.Color.rgb(27, 43, 69)
        c.drawCircle(width * .78f, height * .20f, width * .13f, paint)
        paint.color = android.graphics.Color.rgb(30, 48, 70)
        val base = height * .64f
        for (i in 0..5) {
            val x = i * width / 4f - (stripeOffset * .12f % (width / 4f))
            val p = android.graphics.Path().apply { moveTo(x, base); lineTo(x + width * .16f, base - height * (.08f + (i % 3) * .035f)); lineTo(x + width * .32f, base); close() }
            c.drawPath(p, paint)
        }
    }

    private fun drawGround(c: Canvas) {
        paint.color = android.graphics.Color.rgb(28, 39, 57)
        c.drawRect(0f, groundY, width.toFloat(), height.toFloat(), paint)
        paint.color = android.graphics.Color.rgb(0, 194, 168)
        c.drawRect(0f, groundY, width.toFloat(), groundY + height * .009f, paint)
        paint.color = android.graphics.Color.rgb(74, 94, 119)
        val dashW = width * .09f
        var x = -(stripeOffset % (dashW * 2))
        while (x < width) {
            c.drawRoundRect(x, groundY + height * .09f, x + dashW, groundY + height * .102f, 8f, 8f, paint)
            x += dashW * 2
        }
    }

    private fun updateGame(dt: Float) {
        stripeOffset += speed * dt
        distance += dt * 12f
        speed = (width * (.62f + distance / 1600f)).coerceAtMost(width * 1.05f)
        velocityY += gravity * dt
        playerY += velocityY * dt
        if (playerY > groundY - playerSize) { playerY = groundY - playerSize; velocityY = 0f }
        spawnTimer -= dt
        if (spawnTimer <= 0f) {
            val h = playerSize * Random.nextFloat().coerceIn(.65f, 1.35f)
            obstacles.add(Obstacle(width + playerSize, playerSize * Random.nextFloat().coerceIn(.55f, .9f), h))
            spawnTimer = Random.nextFloat().coerceIn(0.9f, 1.55f) * (width * .65f / speed).coerceIn(.65f, 1.25f)
        }
        obstacles.forEach { it.x -= speed * dt }
        obstacles.removeAll { it.x + it.width < 0 }
        val px = width * .19f
        val player = RectF(px + playerSize * .12f, playerY + playerSize * .12f, px + playerSize * .88f, playerY + playerSize * .92f)
        if (obstacles.any { RectF(it.x + it.width * .1f, groundY - it.height, it.x + it.width * .9f, groundY).intersect(player) }) endGame()
    }

    private fun drawPlayer(c: Canvas) {
        val x = width * .19f
        paint.color = android.graphics.Color.rgb(0, 194, 168)
        c.drawRoundRect(x, playerY, x + playerSize, playerY + playerSize, playerSize * .22f, playerSize * .22f, paint)
        paint.color = android.graphics.Color.WHITE
        c.drawCircle(x + playerSize * .68f, playerY + playerSize * .32f, playerSize * .075f, paint)
        paint.color = android.graphics.Color.rgb(16, 23, 42)
        c.drawCircle(x + playerSize * .70f, playerY + playerSize * .32f, playerSize * .035f, paint)
    }

    private fun drawObstacles(c: Canvas) {
        obstacles.forEach {
            val top = groundY - it.height
            paint.color = android.graphics.Color.rgb(255, 105, 97)
            c.drawRoundRect(it.x, top, it.x + it.width, groundY, 10f, 10f, paint)
            paint.color = android.graphics.Color.rgb(255, 174, 120)
            c.drawRect(it.x + it.width * .18f, top + it.height * .12f, it.x + it.width * .32f, groundY - it.height * .12f, paint)
        }
    }

    private fun drawHud(c: Canvas) {
        paint.color = android.graphics.Color.WHITE
        paint.textSize = width * .055f
        paint.typeface = android.graphics.Typeface.create("sans-serif-medium", android.graphics.Typeface.BOLD)
        c.drawText("SCORE  ${distance.toInt()}", width * .06f, height * .075f, paint)
        paint.textSize = width * .038f
        paint.color = android.graphics.Color.rgb(167, 185, 207)
        c.drawText("BEST  $best", width * .06f, height * .115f, paint)
    }

    private fun drawCenterMessage(c: Canvas, title: String, subtitle: String) {
        paint.color = 0xCC10172A.toInt()
        c.drawRoundRect(width * .07f, height * .34f, width * .93f, height * .57f, 28f, 28f, paint)
        paint.color = android.graphics.Color.WHITE
        paint.textAlign = Paint.Align.CENTER
        paint.typeface = android.graphics.Typeface.create("sans-serif-black", android.graphics.Typeface.BOLD)
        paint.textSize = width * .075f
        c.drawText(title, width / 2f, height * .425f, paint)
        paint.color = android.graphics.Color.rgb(0, 220, 190)
        paint.typeface = android.graphics.Typeface.create("sans-serif-medium", android.graphics.Typeface.NORMAL)
        paint.textSize = width * .038f
        c.drawText(subtitle, width / 2f, height * .49f, paint)
        paint.textAlign = Paint.Align.LEFT
    }

    private fun jumpOrStart() {
        if (!started || gameOver) {
            started = true
            gameOver = false
            distance = 0f
            speed = width * .62f
            spawnTimer = .8f
            obstacles.clear()
            playerY = groundY - playerSize
            velocityY = 0f
        } else if (playerY >= groundY - playerSize - 2f) velocityY = jumpPower
        invalidate()
    }

    private fun endGame() {
        gameOver = true
        val score = distance.toInt()
        if (score > best) {
            best = score
            prefs.edit().putInt("best", best).apply()
        }
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        if (event.action == MotionEvent.ACTION_DOWN) { jumpOrStart(); return true }
        return true
    }
}
