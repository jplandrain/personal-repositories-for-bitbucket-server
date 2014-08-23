define('RepositoriesLoader', [ 'backbone', 'underscore', 'jquery' ], function(
		Backbone, _, $) {
	return Backbone.View.extend({
		template : org.networkedassets.personalRepos.repositories.loader,
		className : 'loading-screen',

		render : function() {
			this.el.innerHTML = this.template();
			return this;
		}
	});
});