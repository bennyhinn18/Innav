//package com.example.innav
//
//
//
//class FloorPlanOverlay(context: Context) : View(context) {
//    private val pathPaint = Paint().apply {
//        color = Color.BLUE
//        strokeWidth = 8f
//        style = Paint.Style.STROKE
//    }
//
//    private val currentPosPaint = Paint().apply {
//        color = Color.RED
//        style = Paint.Style.FILL
//    }
//
//    private var currentPath: Path? = null
//    private var currentPosition: Pair<Int, Int> = Pair(0, 0)
//    private var landmarks = emptyList<Landmark>()
//
//    fun updatePosition(newPosition: Pair<Int, Int>) {
//        currentPosition = newPosition
//        invalidate()
//    }
//
//    fun drawNavigationPath(path: List<Pair<Int, Int>>) {
//        currentPath = Path().apply {
//            val first = path.first()
//            moveTo(first.first.toFloat(), first.second.toFloat())
//            path.drop(1).forEach { lineTo(it.first.toFloat(), it.second.toFloat()) }
//        }
//        invalidate()
//    }
//
//    override fun onDraw(canvas: Canvas) {
//        super.onDraw(canvas)
//
//        // Draw navigation path
//        currentPath?.let { canvas.drawPath(it, pathPaint) }
//
//        // Draw current position
//        canvas.drawCircle(
//            currentPosition.first.toFloat(),
//            currentPosition.second.toFloat(),
//            15f,
//            currentPosPaint
//        )
//
//        // Draw landmarks
//        landmarks.forEach { landmark ->
//            canvas.drawText(
//                landmark.name,
//                landmark.position.first.toFloat(),
//                landmark.position.second.toFloat(),
//                landmarkPaint
//            )
//        }
//    }
//}