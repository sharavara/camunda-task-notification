# Camunda micro-service architecture (event driven approach)


## Node Start Listener

Script:

```groovy
topic = "TASK.2"

currentActivityId = execution.getCurrentActivityId()
processDefinitionId = execution.getProcessDefinitionId()
processBusinessKey = execution.getProcessBusinessKey()
processInstanceId = execution.getProcessInstanceId()

execution.getProcessEngineServices().getRuntimeService().createMessageCorrelation("CAMUNDANEWTASK").setVariable("CurrentActivityId", currentActivityId).setVariable("ProcessDefinitionId",processDefinitionId).setVariable("ProcessBusinessKey",processBusinessKey).setVariable("ProcessInstanceId",processInstanceId).setVariable("activityTopic", topic).correlateWithResult()
```

Variable topic has to be the same with Activity topic