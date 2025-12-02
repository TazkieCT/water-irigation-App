package com.project.deflood.data

import android.util.Log
import com.rabbitmq.client.ConnectionFactory
import com.rabbitmq.client.DeliverCallback
import org.json.JSONObject
import java.util.concurrent.Executors

class RabbitRepository {

    private val queueName = "iot_status"
    private val executor = Executors.newSingleThreadExecutor()

    fun start(onMessage: (String) -> Unit) {
        executor.execute {
            try {
                val factory = ConnectionFactory().apply {
                    setUri("amqps://ozgruhur:0XSUMw80ZGmyQCj-zl0WRgAG7FRiMgzJ@armadillo.rmq.cloudamqp.com/ozgruhur")
                    isAutomaticRecoveryEnabled = true
                    networkRecoveryInterval = 5000
                }

                val connection = factory.newConnection()
                val channel = connection.createChannel()
                channel.queueDeclare(queueName, true, false, false, null)

                val deliverCallback = DeliverCallback { _, delivery ->
                    val message = String(delivery.body)
                    val json = JSONObject(message)

                    val pump = json.getString("pump")
                    val levelA = json.getDouble("levelA")
                    val levelB = json.getDouble("levelB")

                    val result = "Pump: $pump | Level A: $levelA | Level B: $levelB"
                    onMessage(result)
                }

                channel.basicConsume(queueName, true, deliverCallback) { _ -> }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun sendCommand(pumpId: Int, isOn: Boolean) {
        executor.execute {
            try {
                val factory = ConnectionFactory().apply {
                    setUri("amqps://ozgruhur:0XSUMw80ZGmyQCj-zl0WRgAG7FRiMgzJ@armadillo.rmq.cloudamqp.com/ozgruhur")
                }

                val connection = factory.newConnection()
                val channel = connection.createChannel()

                val action = if (isOn) 1 else 0
                val jsonPayload = "{\"target\":$pumpId,\"action\":$action}"

                val commandRoutingKey = "abcdef.iot_command"
                channel.basicPublish("amq.topic", commandRoutingKey, null, jsonPayload.toByteArray())

                Log.d("RabbitRepository", "Perintah dikirim: $jsonPayload")

                channel.close()
                connection.close()
            } catch (e: Exception) {
                e.printStackTrace()
                Log.e("RabbitRepository", "Gagal kirim perintah: ${e.message}")
            }
        }
    }
}
