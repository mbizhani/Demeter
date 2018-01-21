# Demeter

The `Demeter` project is founded based on the following requirements:
- Modular architecture with well-defined integration
- Each module has its own entities, services, and web parts
- A base container with all **common** and **important** services for all modules
- For deployment, a combination of modules can be deployed

So the Demeter project is:
- A container that finds its modules, called `DModule`, in deployment and initializes them based on the lifecycle, even the deployment, itself, is a separate module
- Provides base services for all modules, which are
	- `Person` and `User` Management
	- `Role`, `Privileges`, and`Security` Management, and the integration with web
	- Background and Scheduled `Tasks`, and pushing data basck to web via `WebSocket`
	- `File Store` Management
	- Layout, Menu and Page Management for web
- Define conventions and standards for modules
- Define a convenient platform and ecosystem for development and deployment
- For generality, the Demeter, itself, has its own `DModule`

The architecture of Demeter and a DModule is presented in the following logical component diagram:
![Demeter Architecture](/doc/img/Demeter_Logical_Components_Relations.png)
In the architecture, the Demeter project is composed of four maven jar artifacts:
- `Common`: It has all the common and necessary classes and files which are shared between modules. They are
  - **Entities**: List of entites in DModule
  - **Value Objects**: Based on value objects pattern, they are classes that are used for data transfer.
  - **Service Interfaces**: The Spring beans' interfaces
  - **Privileges**: A privilege is defined by `IPrivilegeKey`. For the list of privileges, and enum is defined which implements the `IPrivilegeKey` interface. So the enum has all the privilege keys.
  - **Config Keys**: There is only one `config.properties` file for the development or deployment. So each module can have its own config keys in the file. Each config entry is `IConfigKey`, so an enum implements the `IConfigKey` interface, and the enum has all the config keys for DModule.
  - **SQL Files**: Since each module has its own entities, it must have its own SQL change scripts that handle schema modification carefully.
- `Service`: It implements all the service interfaces defined in `Common` artifact and whatever is necessary for the service tier.
- `Core`: Its main class `DemeterCore` is responsible for all the lifecycle of Demeter and the deployed DModules.
- `Web`:
	- As Demeter, it has Wicket's `WebApplication` class and all the necessary classes for handling web-side UI, and calling `DemeterCore.init()`
	- As a DModule, it has all the `DPages` and panels for its pages, forms, and lists.
- `Module`:
	- Each DModule has an XML config file with all necessary information. This config file will be described later.
	- This artifact has direct dependencies to `Service` and `Web` with indirect one to `Common`. So it can represent the DModule.

The `DemeterCore` class goes through the following steps for startup:
![DemeterCore Steps](/doc/img/Demeter_StartUp_Steps.png)
 
Each DModule has two XML config files. The first one is for Spring bean definition, which is located in the `main/resoureces` of `Service` maven module, and it is a standard Spring XML config file. Another one is DModule XML definition file, which is located in the `main/resources/dmodule` maven module. For simplicity, some part of Demeter DModule is:
```xml
<?xml version="1.0" encoding="UTF-8"?>

<!DOCTYPE module PUBLIC
		"Devocative/Demeter Module"
		"http://www.devocative.org/dtd/demeter-module.dtd">

<module
		shortName="DMT"
		mainResource="org.devocative.demeter.web.DemeterDModule"
		privilegeKeyClass="org.devocative.demeter.DemeterPrivilegeKey"
		configKeyClass="org.devocative.demeter.DemeterConfigKey">

	<entities>
		<entity type="org.devocative.demeter.entity.DPageInfo"/>
		<entity type="org.devocative.demeter.entity.DPageInstance"/>
    ... 
	</entities>

	<tasks>
		<task type="org.devocative.demeter.service.task.SimpleDTask"/>

		<!-- Fire at 2:00 every day -->
		<task type="org.devocative.demeter.service.task.FileStoreDTask" cronExpression="0 0 2 * * ?"/>
	</tasks>

	<dPages>
		<dPage type="org.devocative.demeter.web.dpage.PersonListDPage"
       title="KEY:dPage.dmt.Person"
       uri="/persons"
       inMenu="true"
       roles="Admin" />

		<dPage type="org.devocative.demeter.web.dpage.LoginDPage"
       title="KEY:dPage.dmt.Login"
       uri="/login"
       inMenu="false" />
    ...
	</dPages>
</module>
```

A conviniet environment for development is very important. To reach the goal, other artifacts are under development. The following picture tries to show all the components that are involved in this project.
![Demeter Environment](/doc/img/Class_Diagram__demeter__DemeterComponent.png)
