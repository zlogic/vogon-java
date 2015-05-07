app.service("AlertService", function ($timeout) {
	var that = this;
	this.alerts = [];
	this.closeAlert = function (alertIndex) {
		that.alerts.splice(alertIndex, 1);
	};
	this.enabled = function () {
		return true;
	};
	this.addAlert = function (message) {
		if (!that.enabled())
			return;
		var alert = {msg: message, type: "danger"};
		that.alerts.push(alert);
		$timeout(function () {
			var alertIndex = that.alerts.indexOf(alert);
			if (alertIndex !== -1)
				that.closeAlert(alertIndex);
		}, 30000);
	};
});

app.service("HTTPService", function ($http, $q, AlertService) {
	var that = this;
	var tokenRegex = /^oauth\/token$/;
	this.pendingRequests = 0;
	this.isLoading = false;
	this.authorizationHeaders = {};
	this.authorized = false;
	var mergeHeaders = function (extraHeaders) {
		var headers = {};
		if (extraHeaders !== undefined)
			merge(headers, extraHeaders);
		merge(headers, that.authorizationHeaders);
		return headers;
	};
	var merge = function (a, b) {
		for (var prop in b)
			a[prop] = b[prop];
	};
	var startRequest = function () {
		that.pendingRequests++;
		that.isLoading = true;
	};
	var endRequest = function () {
		that.pendingRequests--;
		that.isLoading = that.pendingRequests > 0;
	};
	var retryRequest = function (config) {
		config.headers = mergeHeaders(config.headers);
		return $http(config).then(
				function (data) {
					that.updateAllData();
					return data;
				}
		);
	};
	var isTokenURL = function (url) {
		return tokenRegex.test(url);
	};
	var errorHandler = function (data) {
		endRequest();
		var deferred = $q.defer();
		if (data.status === 401) {
			var fixAuthCall = that.fixAuthorization();
			if (fixAuthCall !== undefined)
				fixAuthCall
						.then(function (authData) {
							if (that.authorized)
								retryRequest(data.config).then(deferred.resolve, deferred.reject);
							else
								deferred.reject(authData);
						}, deferred.reject);
			else
				deferred.reject();
			return deferred.promise;
		} else {
			AlertService.addAlert(complex_messages.HTTP_ERROR_FORMAT(data.status, data.data));
			if (that.authorized && data.config.customData.updateOnFailure)
				that.updateAllData();
			//TODO: else refresh page to reset state?
		}
		deferred.reject(data);
		return deferred.promise;
	};
	var successHandler = function (data) {
		endRequest();
		return data;
	};
	this.buildRequestParams = function (updateOnFailure) {
		return {
			updateOnFailure: updateOnFailure !== undefined ? updateOnFailure : true
		};
	};
	this.get = function (url, extraHeaders, requestParams) {
		startRequest();
		var headers = isTokenURL(url) ? extraHeaders : mergeHeaders(extraHeaders);
		if (requestParams === undefined)
			requestParams = that.buildRequestParams();
		return $http.get(url, {headers: headers, customData: requestParams}).then(successHandler, errorHandler);
	};
	this.delete = function (url, extraHeaders, requestParams) {
		startRequest();
		var headers = isTokenURL(url) ? extraHeaders : mergeHeaders(extraHeaders);
		if (requestParams === undefined)
			requestParams = that.buildRequestParams();
		return $http.delete(url, {headers: headers, customData: requestParams}).then(successHandler, errorHandler);
	};
	this.post = function (url, data, extraHeaders, requestParams, transformRequest) {
		startRequest();
		var headers = isTokenURL(url) ? extraHeaders : mergeHeaders(extraHeaders);
		var params = {headers: headers};
		if (transformRequest !== undefined)
			params.transformRequest = transformRequest;
		if (requestParams === undefined)
			requestParams = that.buildRequestParams();
		params.customData = requestParams;
		return $http.post(url, data, params).then(successHandler, errorHandler);
	};
	this.fixAuthorization = function () {
		throw messages.FIX_AUTHORIZATION_NOT_INITIALIZED;
	};
	this.setAccessToken = function (access_token) {
		if (access_token !== undefined)
			that.authorizationHeaders = {Authorization: "Bearer " + access_token};
		else
			that.authorizationHeaders = {};
	};
	this.updateAllData = function () {
		that.updateAccounts();
		that.updateTransactions();
		that.updateUser();
	};
	this.updateAccounts = function () {
		throw messages.UPDATE_ACCOUNTS_NOT_INITIALIZED;
	};
	this.updateTransactions = function () {
		throw messages.UPDATE_TRANSACTIONS_NOT_INITIALIZED;
	};
	this.updateUser = function () {
		throw messages.UPDATE_USER_NOT_INITIALIZED;
	};
});

app.service("AuthorizationService", function ($q, AlertService, HTTPService) {
	var that = this;
	var clientId = "vogonweb";
	var postHeaders = {"Content-Type": "application/x-www-form-urlencoded"};
	var defaultRememberToken = false;
	this.authorized = false;
	this.access_token = undefined;
	this.username = undefined;
	this.password = undefined;
	this.rememberToken = defaultRememberToken;
	var setToken = function (access_token, username, password) {
		if (access_token !== undefined) {
			that.access_token = access_token;
			if (that.rememberToken)
				localStorage.setItem("access_token", access_token);
			HTTPService.setAccessToken(access_token);
			setAuthorized(true);
			if (username !== undefined)
				that.username = username;
			if (password !== undefined)
				that.password = password;
		}
	};
	var setAuthorized = function (authorized) {
		that.authorized = authorized;
		HTTPService.authorized = authorized;
	};
	this.performAuthorization = function (username, password) {
		var params = {username: username, password: password, client_id: clientId, grant_type: "password"};
		return HTTPService.post("oauth/token", encodeForm(params), postHeaders)
				.then(function (data) {
					data = data.data;
					setToken(data.access_token, username, password);
					return data;
				}, function (data) {
					that.resetAuthorization(messages.UNABLE_TO_AUTHENTICATE);
					var deferred = $q.defer();
					deferred.reject(data);
					return deferred.promise;
				});
	};
	this.logout = function () {
		if (that.access_token !== undefined) {
			var params = {token: that.access_token};
			return HTTPService.post("logout", encodeForm(params), postHeaders)
					.then(that.resetAuthorization(), that.resetAuthorization());
		} else {
			var deferred = $q.defer();
			deferred.reject({data: {error_description: messages.ALREADY_LOGGED_OUT}});
			that.resetAuthorization();
			return deferred.promise;
		}
	};
	this.fixAuthorization = function () {
		if (that.username !== undefined && that.password !== undefined) {
			return that.performAuthorization(that.username, that.password);
		} else {
			var message;
			if (that.authorized)
				if (that.username !== undefined && that.password !== undefined)
					message = messages.USERNAME_PASSWORD_NOT_ACCEPTED;
				else if (that.access_token !== undefined)
					message = messages.ACCESS_TOKEN_REJECTED;
			that.resetAuthorization(that.authorized ? messages.CANT_FIX_AUTHORIZATION : undefined);
		}
	};
	this.resetAuthorization = function (message) {
		that.username = undefined;
		that.password = undefined;
		that.access_token = undefined;
		HTTPService.setAccessToken();
		setAuthorized(false);
		localStorage.removeItem("access_token");
		this.rememberToken = defaultRememberToken;
		if (message !== undefined)
			AlertService.addAlert(message);
	};
	HTTPService.fixAuthorization = this.fixAuthorization;
	AlertService.enabled = function () {
		return that.authorized;
	};

	this.access_token = localStorage["access_token"];
	if (this.access_token !== undefined) {
		HTTPService.setAccessToken(this.access_token);
		setAuthorized(true);
	} else {
		setAuthorized(false);
	}
});