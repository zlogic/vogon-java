var messages = {
	TAGS_SEPARATOR: ",",
	FIX_AUTHORIZATION_NOT_INITIALIZED: "fixAuthorization not properly initialized",
	UPDATE_ACCOUNTS_NOT_INITIALIZED: "updateAccounts not properly initialized",
	UPDATE_TRANSACTIONS_NOT_INITIALIZED: "updateTransactions not properly initialized",
	UPDATE_USER_NOT_INITIALIZED: "updateUser not properly initialized",
	UNABLE_TO_AUTHENTICATE: "Unable to authenticate",
	USERNAME_PASSWORD_NOT_ACCEPTED: "Username/password not accepted",
	ACCESS_TOKEN_REJECTED: "Access token rejected",
	CANT_FIX_AUTHORIZATION: "Can't fix authorization",
	ALREADY_LOGGED_OUT: "Already logged out",
	BALANCE: "Balance",
	EXPENSEINCOME: "Expense/income",
	TRANSFER: "Transfer"
};
var complex_messages = {
	HTTP_ERROR_FORMAT: function (httpStatus, data) {
		return "HTTP error: " + httpStatus + "(" + angular.toJson(data) + ")";
	}
};
