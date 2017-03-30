# Vogon personal finance tracker

[![Deploy](https://www.herokucdn.com/deploy/button.svg)](https://heroku.com/deploy)

See [details](#getting-started-on-heroku) below.

## Project description

Simple web-based personal finance tracker using 

* AngularJS on client-side
* Spring MVC on server-side for AngularJS request handling
* JSP for page generation (single page with all data being retrieved with JSON calls)
* JPA and Spring Repositories for entity management
* H2 database for data storage, or a server-provided PostgreSQL (OpenShift/Heroku)

Named after the Vogons (http://en.wikipedia.org/wiki/Vogon) race who were known to be extremely boring accountants.

**Demos**

Check out the demo deployments (may take a few minutes to load for the first time):

* [Heroku](http://vogon-demo.herokuapp.com)
* [Azure](http://vogon-demo.azurewebsites.net)

**Prepackaged standalone versions are available on [Github](/../../releases).**

Requires Java 8 to build. [Releases](/../../releases) contain prepackaged WAR files for:

* WildFly
* Standalone version which works in Azure and on Heroku

If all works well, the server should auto-redirect to HTTPS, however it's tricky and may not always work - some cloud environments require non-standard ports and/or unencrypted HTTP connections. Double-check that your deployment is redirecting to HTTPS by default!

## Configuration

If you do not want random people using your deployment, you may want to set the `VOGON_ALLOW_REGISTRATION` environment variable to `false`.

See the [documentation](https://devcenter.heroku.com/articles/config-vars) for more details on how to change configuration variables.

You should set `VOGON_ALLOW_REGISTRATION` to `false` only after registering yourself.

Set the `VOGON_TOKEN_EXPIRES_DAYS` variable to the number of days before authorization expires and the user has to re-login (e.g. `14`);

## Getting started on Heroku

You can either
- fork this repository and copy into a new Heroku app [through Github](http://devcenter.heroku.com/articles/github-integration)
- or use the [deployment button](#vogon-personal-finance-tracker) above
- or try the [demo version](https://vogon-demo.herokuapp.com) first

This app requires a PostgreSQL database, the deployment button will automatically create a free database and configure it.

Configuration of Vogon can be done via environment variables, as described in the [Configuration](#configuration) section above.

See the [documentation](https://devcenter.heroku.com/articles/config-vars) for more details on how to change configuration variables.

## Getting started on Azure

You can create a new Web App in Azure and deploy the prepackaged standalone version
as described in the [Azure documentation](https://docs.microsoft.com/en-us/azure/app-service-web/web-sites-java-custom-upload):

Create a new Web App, *without* selecting an application server like Tomcat or Jetty.

Set the following environment variables ("App Settings") in Application Settings:

Key | Value
--- | ---
`VOGON_JAVA_HOME` | `D:\Program Files (x86)\Java\jdk1.8.0_111`
`VOGON_ALLOW_REGISTRATION` | `true`

Upload the prepackaged standalone war to `site\wwwroot\webapps\ROOT.war`.

Finally, create a `site\wwwroot\web.config file with the following contents:

```
<?xml version="1.0" encoding="UTF-8"?>
<configuration>
  <system.webServer>
    <handlers>
      <add name="httpPlatformHandler" path="*" verb="*" modules="httpPlatformHandler" resourceType="Unspecified" />
    </handlers>
    <httpPlatform processPath="%VOGON_JAVA_HOME%\bin\java.exe"
        arguments="-Djava.net.preferIPv4Stack=true -Dserver.port=%HTTP_PLATFORM_PORT% -jar &quot;%HOME%\site\wwwroot\webapps\ROOT.war&quot;">
      <environmentVariables>
        <environmentVariable name="VOGON_DATABASE_DIR" value="%HOME%\site" />
      </environmentVariables>
    </httpPlatform>
    <rewrite>
      <rules>
        <rule name="Redirect to https">
          <match url="(.*)"/>
          <conditions>
            <add input="{HTTPS}" pattern="Off"/>
          </conditions>
          <action type="Redirect" url="https://{HTTP_HOST}/{R:1}"/>
        </rule>
      </rules>
    </rewrite>
  </system.webServer>
</configuration>
```

## Standalone deployment

Vogon can also run locally. Download the war file from [Github](/../../releases) and run it from the console:

```
SET AZURE_MODE=true
SET VOGON_DATABASE_DIR=%CD%
SET ALLOW_REGISTRATION=true
START java -jar ROOT.war
```

## Other info

2.0 and earlier versions also include a standalone version using Java FX for UI. This version is no longer maintained and may be completely removed in future releases. Requires Java 8 to run.

This project has a rich history of UI rewrites, including versions using

* [SWT](http://www.eclipse.org/swt/) 
* [Swing](http://en.wikipedia.org/wiki/Swing_%28Java%29)
* [Java FX](http://www.oracle.com/technetwork/java/javase/overview/javafx-overview-2158620.html)

Check out the history if you're interested!
