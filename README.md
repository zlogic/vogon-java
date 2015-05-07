#Vogon personal finance tracker

Simple web-based personal finance tracker using 

* AngularJS on client-side
* Spring MVC on server-side for AngularJS request handling
* JSP for page generation (single page with all data being retrieved with JSON calls)
* JPA and Spring Repositories for entity management
* H2 database for data storage, or a server-provided PostgreSQL (OpenShift/Heroku)

Named after the Vogons (http://en.wikipedia.org/wiki/Vogon) race who were known to be extremely boring accountants.

**Demos**

Check out the demo deployments:

* [OpenShift (WildFly)](http://vogon-zlogic42demo.rhcloud.com)
* [Heroku](http://vogon-demo.herokuapp.com) (may take a few minutes to load for the first time)
* [Azure (Tomcat)](http://vogon-tomcat.azurewebsites.net) (may take a few minutes to load for the first time)
* [Azure (Jetty)](http://vogon-jetty.azurewebsites.net) (may take a few minutes to load for the first time)

**Prepackaged releases are available on [Github](/../../releases).**

Requires Java 8 to build. [Releases](/../../releases) contain prepackaged WAR files for:

* WildFly (confirmed to run on the free [OpenShift](http://www.openshift.com/) Bronze account with *WildFly Application Server 8.1.0.Final* cartridge installed. Just follow the [instructions](http://help.openshift.com/hc/en-us/articles/202399740) to deploy the WAR file).
* Tomcat 7 (confirmed to run on the free [OpenShift](http://www.openshift.com/) Bronze account with *Tomcat 7 (JBoss EWS 2.0)* cartridge installed. Just follow the [instructions](http://help.openshift.com/hc/en-us/articles/202399740) to deploy the WAR file).
* Heroku - just fork this repository and copy into a new Heroku app [through Github](http://devcenter.heroku.com/articles/github-integration)!
* Azure (confirmed to work in the free web app tier) - create a Tomcat or Jetty web app from the marketplace and deploy the Tomcat war.

Check out the readme for more details. Default username/password are Default/Default, make sure to change them to something more secure.

2.0 and earlier versions also include a standalone version using Java FX for UI. This version is no longer maintained and may be completely removed in future releases. Requires Java 8 to run.

This project has a rich history of UI rewrites, including versions using

* [SWT](http://www.eclipse.org/swt/) 
* [Swing](http://en.wikipedia.org/wiki/Swing_%28Java%29)
* [Java FX](http://www.oracle.com/technetwork/java/javase/overview/javafx-overview-2158620.html)

Check out the history if you're interested!
