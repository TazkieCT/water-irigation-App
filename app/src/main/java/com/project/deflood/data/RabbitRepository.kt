package com.project.deflood.data

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
                    setUri("amqps://username:password@penguin.rmq.cloudamqp.com/vhost")
                    isAutomaticRecoveryEnabled = true
                    networkRecoveryInterval = 5000
                }

                val connection = factory.newConnection()
                val channel = connection.createChannel()
                channel.queueDeclare(queueName, true, false, false, null)

                val deliverCallback = DeliverCallback { _, delivery ->
                    val message = String(delivery.body)
                    val json = JSONObject(message)

                    val status = json.getString("status")
                    val level = json.getInt("level")

                    val result = "Status: $status | Level: $level"
                    onMessage(result)
                }

                channel.basicConsume(queueName, true, deliverCallback) { _ -> }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}
