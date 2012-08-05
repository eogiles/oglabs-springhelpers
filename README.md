oglabs-springhelpers
====================

Useful classes extending Spring libraries.

Currently all are for Spring WS

- XslTransformingMarshaller extends Jaxb2Marshaller to transform incoming XML via an XSL spreadsheet before unmarshalling. It is a drop-in replacement for the standard Jaxb2Marshaller.
- PeoplesoftFaultMessageResolver is built to provide sensible exceptions when you're consuming a Peoplesoft web service and it throws a fault. Rather than getting really unhelpful fault messages like "null", this pulls the first MessageID, StatusCode, and DefaultMessage it runs across and puts it in an IOException. This allows the exception to provide at least basic info about the fault regardless of whether it is a custom fault or one of the low level faults (like password is wrong). Simply configure this class as the faultMessageResolver on your Spring WebServiceTemplate.
- FaultObjectResolver is an interface that can be used in conjunction with PeoplesoftFaultMessageResolver. You configure your FaultObjectResolver with a Jaxb2Marshaller that can handle your custom fault objects and hook it up to PeoplesoftFaultMessageResolver. Presto: fault objects.

These have been tested with the webservices provided by PeopleTools 8.52 but should be used as examples, not production-ready code. No guarantees or warantees.