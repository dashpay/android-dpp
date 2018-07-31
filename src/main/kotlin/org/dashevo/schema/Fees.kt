package org.dashevo.schema

class Fees(feePerByte: Double, packetSize: Int) {

    val headerFee: Double
    val packetFee: Double

    init {
        val stHeaderSizeBytes = 220.0
        val curveParam = 8.8
        val curveThreshold = 1.0
        val curveMag = 5.0

        val core = Math.pow(packetSize.toDouble(), 3.0) / Math.pow((curveParam * Math.pow(10.0, curveMag)), 2.0)
        val multi = Math.ceil(Math.max(curveThreshold, core))

        headerFee = feePerByte * stHeaderSizeBytes
        packetFee = (multi * headerFee) - headerFee
    }

}