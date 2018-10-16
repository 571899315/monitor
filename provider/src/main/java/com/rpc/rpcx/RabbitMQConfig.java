package com.rpc.rpcx;


import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.rabbit.connection.CachingConnectionFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.listener.SimpleMessageListenerContainer;
import org.springframework.amqp.rabbit.listener.adapter.MessageListenerAdapter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

	final static String queueName = "monitor";

	final static String HOST = "localhost";

	final static String USERNAME = "guest";

	final static String PASSWORD = "guest";

	final static int PORT = 5672;

	@Bean
	public Queue queue() {
		return new Queue(queueName, true);
	}

	@Bean
	public TopicExchange exchange() {
		return new TopicExchange("spring-boot-exchange");
	}

	@Bean
	public Binding binding(Queue queue, TopicExchange exchange) {
		return BindingBuilder.bind(queue).to(exchange).with(queueName);
	}

	@Bean
	public ConnectionFactory connectionFactory() {
		CachingConnectionFactory connectionFactory = new CachingConnectionFactory();
		connectionFactory.setHost(HOST);
		connectionFactory.setPort(PORT);
		connectionFactory.setUsername(USERNAME);
		connectionFactory.setPassword(PASSWORD);
		connectionFactory.setVirtualHost("/");
		// ����Ҫ����,��Ϣ�Ļص�
		connectionFactory.setPublisherConfirms(true);
		return connectionFactory;
	}

	@Bean
	public SimpleMessageListenerContainer container(ConnectionFactory connectionFactory, MessageListenerAdapter listenerAdapter) {
		SimpleMessageListenerContainer container = new SimpleMessageListenerContainer();
		container.setConnectionFactory(connectionFactory);
		container.setQueueNames(queueName);
		container.setMessageListener(listenerAdapter);
		return container;
	}

	@Bean
	public MessageListenerAdapter listenerAdapter(Receiver receiver) {
		return new MessageListenerAdapter(receiver, "receiveMessage");
	}
}
