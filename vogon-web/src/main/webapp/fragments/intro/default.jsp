<%@ page session="false" trimDirectiveWhitespaces="true" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="core" uri="http://java.sun.com/jsp/jstl/core" %>
<fmt:setBundle basename="org.zlogic.vogon.web.webmessages" />
<core:choose>
	<core:when test="${configuration.allowRegistration}">
		<h3>Registration</h3>
		<p>To register, select the "<fmt:message key="REGISTER"/>" tab and enter a username and password.</p>
		<p>Press "<fmt:message key="REGISTER"/>" and you'll be registered and logged into your account.</p>
		<p>
			<span class="label label-info">Notice</span>
			If you have entered an already existing username, you'll see an error and should use another username and password.
		</p>
		<p>
			<span class="label label-danger">Warning</span>
			Registration is simple, we don't ask your contact details or stupid questions like your favorite food :)
			And so there's no way of recovering passwords!
		</p>
		<p>
			<span class="label label-danger">Warning</span>
			This may be a non-production deployment of Vogon and may be reset or closed by the site admin without notice.
			Consider exporting your data frequently or contacting the website administrator to confirm your data won't be destroyed without notice.
		</p>
	</core:when>
	<core:otherwise>
		<h3>Private deployment</h3>
		<p>This is a private Vogon deployment with registration disabled. You can try a public deployment at any of the following deployments:</p>
		<ul>
			<li><a href="http://vogon-demo.herokuapp.com">Heroku</a> (may take a few minutes to load for the first time)</li>
			<li><a href="http://vogon-demo.azurewebsites.net">Azure</a> (may take a few minutes to load for the first time)</li>
		</ul>
		<p>
			<span class="label label-info">Warning</span>
			The links above are provided as demos only and are not guaranteed to keep running. You can deploy Vogon on most free Java EE cloud providers.
		</p>
	</core:otherwise>
</core:choose>
<h3>About Vogon</h3>
<p>Vogon is an open-source personal finance tracker.</p>
<p>It runs on most cloud providers, even on free tiers.</p>
<p>For more information, visit the project's <a href="http://github.com/zlogic/vogon">Github</a> page.</p>
<p>Developed by <a href="https://plus.google.com/+DmitryZolotukhin">Dmitry Zolotukhin</a>.</p>
<p class="text-center"><img src="images/vogon-logo-large.png" alt="Vogon logo"/></p>