# Demeter

[![Maven Central](https://maven-badges.herokuapp.com/maven-central/org.devocative/demeter-module/badge.svg)](https://maven-badges.herokuapp.com/maven-central/org.devocative/demeter-module)

The `Demeter` project is founded based on the following requirements:
- Modular architecture with well-defined integration
- Each module has its own entities, services, and web parts
- A base container with all **common** and **important** services for all modules
- For deployment, a combination of modules can be deployed
- Common test cases can be executed for the container and the modules

So the Demeter project is:
- A container that finds its modules, called `DModule`, in deployment and initializes them based on the lifecycle, even the deployment, itself, is a separate module
- Provides base services for all modules, which are
	- `Person` and `User` Management
	- `Role`, `Privileges`, and`Security` Management, and the integration with web
	- Background and Scheduled `Tasks`, and pushing data back to web via `WebSocket`
	- `File Store` Management
	- Layout, Menu and Page Management for web
	- Object `Cache` Management and UI for clearing them
	- Show the list of defined `config` keys, and modify them at runtime
- Define conventions and standards for modules.
- Define a convenient platform and ecosystem for faster `development`, `test`, and `deployment`
- For generality, the Demeter, itself, has its own `DModule`

## Architecture
The architecture of Demeter and a DModule is presented in the following logical component diagram:
![Demeter Architecture](/doc/img/Demeter_Logical_Components_Relations.png)
In the architecture, the Demeter project is composed of four maven jar artifacts:
- `Common`: It has all the common and necessary classes and files which are shared between modules. They are
  - **Entities**: List of entities in DModule
  - **Value Objects**: Based on value object pattern, they are classes that are used for data transfer
  - **Service Interfaces**: The Spring beans' interfaces
  - **Privileges**: A privilege is defined by `IPrivilegeKey`. For the list of privileges, an enum is defined which implements the `IPrivilegeKey` interface. So the enum has all the privilege keys.
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

## Startup Process
The `DemeterCore` class goes through the following steps for startup:
![DemeterCore Steps](/doc/img/Demeter_StartUp_Steps.png)

## XML Config Files
Each DModule has two XML config files. The first one is for Spring bean definition, which is located in the `main/resoureces` of `Service` maven artifact,
and it is a standard Spring XML config file. Another one is DModule XML definition file, which is located in the `main/resources/dmodule` maven artifact.
For simplicity, a summary of Demeter's DModule XML file is presented:

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

## Environment
A convenient environment for development is very important. To reach the goal, one maven plugin and two archetypes are developed.
The following picture tries to show all the components that are involved in this project.
![Demeter Environment](/doc/img/Class_Diagram__demeter__DemeterComponent.png)

And finally, a simple screen from the Demeter
![Simple Demeter Screen](/doc/img/A_Simple_Demeter_Screen.png)

## How to Run
Running Demeter project is so simple. You can execute following commands:
```shell
git clone https://github.com/mbizhani/Demeter.git
cd Demeter
mvn clean install
cd module
mvn jetty:run
```
Now open your browser and go to this address: http://localhost:8080/ctx. The default `config.properties` uses the HSQLDB as the default database.

## How to Create a DModule
As mentioned before, for convenience, a maven archetype is created to setup an initial DModule project. Besides, a maven plugin is used for generating simple CRUD for entities.

**Note**: the generated project from archetype has a simple `Book` entity as an example.

To create an initial DModule project:
```shell
mvn -B archetype:generate \
	-DarchetypeGroupId=org.devocative \
	-DarchetypeArtifactId=dmodule-archetype \
	-DarchetypeVersion=1.0 \
	-DgroupId=my.pkg \
	-DartifactId=store \
	-Dpackage=my.pkg.store \
	-DModuleName=Store \
	-DModuleShortName=STR \
	-Dversion=1.0-SNAPSHOT
```

Now, by executing following steps, the CRUD classes and files are generated:
```shell
cd store
mvn clean install
cd module
mvn demeter:codegen
```
(Here, the codegen plugin finds data model added-changes and alerts on the screen. So the following will continue the procedure):
```shell
mvn demeter:codegen
cd ..
mvn clean install
cd module
mvn jetty:run
```

Calling `mvn demeter:codegen` the second time generates all the necessary classes and files. After installing the artifacts,
in the `module` artifact calling `mvn jetty:run` will commence the Jetty web server and `Demteter` startup lifecycle.
Now goto http://localhost:8080/ctx/books, which shows the generated list for `Book` entity. But before that, the login page is presented. The default username and password is:

Username | Password
-------- | --------
root | root

## How to Deploy
So far, a new DModule is created. It is time to deploy it as a `WAR`. Again, an archetype will come to help. The `Deploy` is a special
DModule to package other DModules besides itself as a deployable WAR. Another advantage of this approach helps us to alter and define code for target deployment.

**Note**: As said before, the common DModule has four artifacts, however the Deploy DModule just has one artifact, which is the single WAR.

So, lets create a Deploy DModule:
```shell
mvn -B archetype:generate \
	-DarchetypeGroupId=org.devocative \
	-DarchetypeArtifactId=deploy-archetype \
	-DarchetypeVersion=1.0 \
	-DgroupId=my.pkg \
	-DartifactId=store-dpl \
	-Dpackage=my.pkg.store \
	-DDeploymentTarget=Store\
	-Dversion=1.0
```

The basis of deployment project is created. Its main `pom.xml` just has the dependency to demeter. Add the following dependency to `dependencies` section:
```xml
<dependency>
	<groupId>my.pkg<groupId>
	<artifactId>store-module<artifactId>
	<version>1.0-SNAPSHOT</version>
</dependency>
```

To verify the result before deployment, execute `mvn jetty:run` and goto http://localhost:8080/ctx. To create the WAR, execute `mvn package`. If you have a running Tomcat,
 you can upload and deploy the WAR directly to it via maven tomcat7 plugin. Just execute `mvn tomcat7:redeploy`. The configs for tomcat can be altered in the `<properties>` of the pom.xml.

## Projects
Project | Homepage
------- | --------
Devolcano | https://github.com/mbizhani/Devolcano
DModuleArchetype | https://github.com/mbizhani/DModuleArchetype
DeployArchetype | https://github.com/mbizhani/DeployArchetype
