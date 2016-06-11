(function () {

  angular.module('temperatusApp', ['ngRoute']);

  function config($routeProvider) {
    $routeProvider
    .when('/', {
      templateUrl : '/web/views/main.view.html',
      controller: 'mainCtrl',
      controllerAs: 'vm'
    })
    .otherwise({redirectTo: '/'});
  }

  angular
    .module('temperatusApp')
    .config(['$routeProvider', config]);

})();

(function () {

  angular
    .module('temperatusApp')
    .controller('mainCtrl', mainCtrl);

  mainCtrl.$inject = ['$scope'];
  function mainCtrl ($scope) {
    var vm = this;
  }

})();
