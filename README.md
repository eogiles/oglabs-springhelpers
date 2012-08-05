oglabs-springhelpers
====================

Useful classes extending Spring libraries

For those consuming Peoplesoft web services with Spring WS...

Are you like me: Tired of monkeying around in Peoplesoft fault message XML? Stumped by friendly fault messages like "null."?
Attached are a couple classes to drop into your Spring WS client that might help:

PeoplesoftFaultMessageResolver will pull the first MessageID, StatusCode, and DefaultMessage it runs across and put it in an IOException. This allows the exception to provide at least basic info about the fault regardless of whether it is a custom fault or one of the low level faults (like password is wrong). Simply configure this class as the faultMessageResolver on your Spring WebServiceTemplate.

FaultObjectResolver is an interface that works with PeoplesoftFaultMessageResolver. You configure your FaultObjectResolver with a Jaxb2Marshaller that can handle your custom fault objects and hook it up to PeoplesoftFaultMessageResolver. Presto: fault objects.