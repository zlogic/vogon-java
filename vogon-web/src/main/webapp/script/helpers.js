var dateToJson = function (date) {
	if (date instanceof Date)
		return new Date(Date.UTC(date.getFullYear(), date.getMonth(), date.getDate())).toJSON().split("T")[0];
	else
		return date;
};

var encodeForm = function (data) {
	var buffer = [];
	for (var name in data)
		buffer.push([encodeURIComponent(name), encodeURIComponent(data[name])].join("="));
	return buffer.join("&");
};

var updateHelper = function (updateFunction) {
	var update = {
		updateRequested: false,
		updateFunctionPromise: undefined
	};
	var updateCompleted = function () {
		update.updateFunctionPromise = undefined;
		if(update.updateRequested)
			update.doUpdate();
	};
	update.inProgress = function () {
		return update.updateFunctionPromise !== undefined;
	}
	update.doUpdate = function () {
		update.updateRequested = false;
		update.updateFunctionPromise = updateFunction();
		if(update.updateFunctionPromise !== undefined && update.updateFunctionPromise.then !== undefined)
			return update.updateFunctionPromise.then(updateCompleted, updateCompleted);
		else
			return undefined;
	};
	update.update = function () {
		update.updateRequested = true;
		if(!update.inProgress())
			return update.doUpdate();
		return update.updateFunctionPromise;
	};
	return update;
};
