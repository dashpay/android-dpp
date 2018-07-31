package org.dashevo.schema

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test

@DisplayName("Fees unit tests")
class FeesTest {

    private val medianTxSizeBytes = 250.0
    private val medianTxFee = 0.0006
    private val feePerByte = medianTxFee / medianTxSizeBytes

    @Test
    @DisplayName("ST Header Fee Calculation")
    fun stHeaderFeeTest() {
        val packetSize = 1000
        val res = Fees(feePerByte, packetSize)
        assertThat(res.headerFee).isEqualTo(0.0005279999999999999)
    }

    @Test
    @DisplayName("ST Packet Fee Calculation: 1,000 bytes")
    fun setPacketFeeTest1k() {
        val res = Fees(feePerByte, 1000)
        assertThat(res.packetFee).isEqualTo(0.0)
    }

    @Test
    @DisplayName("ST Packet Fee Calculation: 5,000 bytes")
    fun setPacketFeeTest5k() {
        val res = Fees(feePerByte, 5000)
        assertThat(res.packetFee).isEqualTo(0.0)
    }

    @Test
    @DisplayName("ST Packet Fee Calculation: 10,000 bytes")
    fun setPacketFeeTest10k() {
        val res = Fees(feePerByte, 10000)
        assertThat(res.packetFee).isEqualTo(0.0005279999999999999)
    }

    @Test
    @DisplayName("ST Packet Fee Calculation: 50,000 bytes")
    fun setPacketFeeTest50k() {
        val res = Fees(feePerByte, 50000)
        assertThat(res.packetFee).isEqualTo(0.08500799999999999)
    }

    @Test
    @DisplayName("ST Packet Fee Calculation: 100,000 bytes")
    fun setPacketFeeTest100k() {
        val res = Fees(feePerByte, 100000)
        assertThat(res.packetFee).isEqualTo(0.6816479999999999)
    }

    @Test
    @DisplayName("ST Packet Fee Calculation: 500,000 bytes")
    fun setPacketFeeTest500k() {
        val res = Fees(feePerByte, 500000)
        assertThat(res.packetFee).isEqualTo(85.22711999999999)
    }

    @Test
    @DisplayName("ST Packet Fee Calculation: 1,000,000 bytes")
    fun setPacketFeeTest1M() {
        val res = Fees(feePerByte, 1000000)
        assertThat(res.packetFee).isEqualTo(681.8180159999998)
    }

}