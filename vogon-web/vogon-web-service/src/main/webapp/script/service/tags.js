var tagsToJson = function (tags) {
	var tagsJson = [];
	for (var tag in tags)
		tagsJson.push(tags[tag].text);
	return tagsJson;
};

app.service("TagsService", function ($q, AuthorizationService, HTTPService) {
	var that = this;
	this.tags = [];
	var convertTagsForAutocomplete = function (tags, query) {
		var tagsAutocomplete = [];
		tags.forEach(function (tag) {
			if (query === undefined || tag.toLowerCase().indexOf(query.toLowerCase()) >= 0)
				tagsAutocomplete.push({text: tag});
		});
		return tagsAutocomplete;
	};
	this.update = function () {
		if (AuthorizationService.authorized) {
			return HTTPService.get("service/analytics/tags", undefined, HTTPService.buildRequestParams(false)).then(function (data) {
				that.tags = data.data;
				convertTagsForAutocomplete(that.tags);
			});
		} else {
			var deferred = $q.defer();
			deferred.reject();
			return deferred.promise;
		}
	};
	this.mergeTags = function (newTags) {
		newTags.forEach(
				function (newTag) {
					if (!that.tags.some(function (tag) {
						return tag === newTag;
					}))
						that.tags.push(newTag);
				});
	};
	this.autocompleteQuery = function (query) {
		var deferred = $q.defer();
		deferred.resolve(convertTagsForAutocomplete(that.tags, query));
		return deferred.promise;
	};
});