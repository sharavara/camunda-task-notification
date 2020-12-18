package com.sharavara.camunda.TaskNotification;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.logging.Logger;

import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;
import org.camunda.bpm.engine.externaltask.ExternalTask;
import org.camunda.bpm.engine.externaltask.ExternalTaskQuery;
import org.camunda.bpm.model.dmn.instance.List;

import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.Channel;

public class SendToRabbitMQ implements JavaDelegate {
	private final static Logger LOGGER = Logger.getLogger(SendToRabbitMQ.class.getName());

	private static final String RMQ_EXCHANGE_NAME = "CAMUNDA.TASKS";
	private static final String RMQ_HOST = "192.168.1.115";
	private static final int RMQ_PORT = 5672;
	private static final String RMQ_USER = "rabbit";
	private static final String RMQ_PASSWORD = "RabbitHole";

	@Override
	public void execute(DelegateExecution execution) throws Exception {
		String processDefinitionId = (String) execution.getVariable("ProcessDefinitionId");
		String processInstanceId = (String) execution.getVariable("ProcessInstanceId");
		String currentActivityId = (String) execution.getVariable("CurrentActivityId");
		if (currentActivityId == null)
			currentActivityId = "NULL";
		String processBusinessKey = (String) execution.getVariable("ProcessBusinessKey");
		if (processBusinessKey == null)
			processBusinessKey = "NULL";

		LOGGER.info("Sending message to RabbitMQ. | Process Instance: " + processInstanceId + " | Task: " + currentActivityId);

		ExternalTaskQuery query =  execution.getProcessEngineServices().getExternalTaskService().createExternalTaskQuery();
		LOGGER.info("Count: " + query.activityId(currentActivityId).count());
		java.util.List<ExternalTask> extTasks = query.activityId(currentActivityId).list();
		Iterator<ExternalTask> iter = extTasks.iterator();
		while(iter.hasNext()){
			LOGGER.info(iter.next().getTopicName());
		}
		//String topic = extTask.getTopicName();
		//LOGGER.info("TOPIC: " + topic );

		ConnectionFactory factory = new ConnectionFactory();
		factory.setHost(RMQ_HOST);
		factory.setPort(RMQ_PORT);
		factory.setUsername(RMQ_USER);
		factory.setPassword(RMQ_PASSWORD);
		try (Connection connection = factory.newConnection(); Channel channel = connection.createChannel()) {
			AMQP.BasicProperties.Builder builder = new AMQP.BasicProperties().builder();
			Map<String, Object> headerMap = new HashMap<String, Object>();
			headerMap.put("created", currentTime());
			builder.headers(headerMap);
			String message = "{\"processDefinitionId\": \"" + processDefinitionId + 
					"\", \"processInstanceId\": \"" + processInstanceId + 
					"\", \"currentActivityId\": \"" + currentActivityId + 
					"\", \"processBusinessKey\": \"" + processBusinessKey + 
					"\"}";
			channel.basicPublish(RMQ_EXCHANGE_NAME, currentActivityId, builder.build(), message.getBytes("UTF-8"));
		}

		LOGGER.info("Message sent.");
	}

	public static String currentTime(){  
        Date date = Calendar.getInstance().getTime();  
        DateFormat dateFormat = new SimpleDateFormat("yyyy-mm-dd hh:mm:ss");  
        String strDate = dateFormat.format(date);
		return strDate;  
         
}  
}
