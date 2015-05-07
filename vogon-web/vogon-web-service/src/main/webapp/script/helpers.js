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