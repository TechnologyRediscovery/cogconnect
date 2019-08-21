# Event subsystem

## Core objects
* Event (and its two main subclasses CECaseEvent and OccEvent)
* EventProposal
* EventChoice
* EventRule

## Key design principles
11. 'EventCategory' objects are used to configure 'Event' objects. They contain fields such as the overarching EventType, a bunch of switches for controlling what kind of user can generate an actual Event of that particular category. Most importantly to workflow management, an EventCategory can optionally contain an 'EventProposal' object which can contain one or more 'Choice' for the user to evaluate.
11. EventProposals contain a List of objects that implement the 'Proposable' interface. The database configuration of an EventProposal contains two FK columns which give the Proposal its contents: one for a FK to an EventCategory, the second points to an EventRule.
11. Users interact with 'EventRule' objects through the proposal system. An EventRule instance is injected into an 'EventChoice', which extends 'Proposable' and can therefore be exposed to the user through an 'EventProposal'. 

Since Event objects can contain an EventProposal, which, in turn, can generate new Event objects, chaining and even cycling workflow steps can be created from these rudimentary building blocks.

## Inheritance structure
'EventRuleAbstract' is the base class which specifies the conditions under which the rule--when implemented--can be passed. Its first subclass is 'EventRuleImplementation' which stores the timestamp when the rule became associated with either a 'CECase' or an 'OccupancyPeriod'. Implementations also know when the rule was last evauated and, if passed, when that occurred.

'EventRuleImplementation' has two children: 'EventRuleCECase' and 'EventRuleOccPeriod' which cotnain the ID of its parent BOB and a reference to an OccEvent associated with the rule's passing (which is used to determine BOB status).


