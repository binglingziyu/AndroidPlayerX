package online.testdata.player.tf

import java.util.Collections

object NonMaxSuppression {

    fun iou(box1: FloatArray, box2: FloatArray): Float {
        val x1 = Math.max(box1[0], box2[0])
        val y1 = Math.max(box1[1], box2[1])
        val x2 = Math.min(box1[2], box2[2])
        val y2 = Math.min(box1[3], box2[3])
        val interArea = Math.max(0f, x2 - x1 + 1) * Math.max(0f, y2 - y1 + 1)
        val box1Area = (box1[2] - box1[0] + 1) * (box1[3] - box1[1] + 1)
        val box2Area = (box2[2] - box2[0] + 1) * (box2[3] - box2[1] + 1)
        return interArea / (box1Area + box2Area - interArea)
    }

    fun nonMaxSuppression(
        boxes: List<FloatArray>,
        scores: List<Float>,
        threshold: Float
    ): List<FloatArray> {
        var mBoxes = boxes.toMutableList()
        var mScores = scores.toMutableList()
        val result: MutableList<FloatArray> = ArrayList()
        while (mBoxes.isNotEmpty()) {
            // Find the index of the box with the highest score
            val bestScoreIdx = mScores.indexOf(Collections.max(mScores))
            val bestBox = mBoxes[bestScoreIdx]

            // Add the box with the highest score to the result
            result.add(bestBox)

            // Remove the box with the highest score from our lists
            mBoxes.removeAt(bestScoreIdx)
            mScores.removeAt(bestScoreIdx)

            // Get rid of boxes with high IoU overlap
            val newBoxes: MutableList<FloatArray> = ArrayList()
            val newScores: MutableList<Float> = ArrayList()
            for (i in mBoxes.indices) {
                if (iou(bestBox, mBoxes[i]) < threshold) {
                    newBoxes.add(mBoxes[i])
                    newScores.add(mScores[i])
                }
            }
            mBoxes = newBoxes
            mScores = newScores
        }
        return result
    }
}