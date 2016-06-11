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

  mainCtrl.$inject = ['$scope', '$http'];
  function mainCtrl ($scope, $http) {
    var vm = this;

    vm.credentials = {
      name : "",
      _replyto : "",
      comment : ""
    };

    vm.onSubmit = function () {
      vm.formError = "";
      if (!vm.credentials.name) {
        vm.formError = "All fields required, please try again.";
        return false;
      } else if(!vm.credentials._replyto) {
        vm.formError = "Incorrect email address, please try again.";
        return false;
      } else if(!vm.credentials.comment) {
        vm.formError = "Comments are mandatory, please try again.";
        return false;
      }else {
        vm.doSubmit();
      }
    };

    vm.doSubmit = function() {
      vm.formError = "";
      vm.formSuccess = "Thanks. We will get back to you as soon as possible."
      console.log(vm.credentials);
      $http.post('https://formspree.io/qa.alberto@gmail.com', vm.credentials);
    };
  }
})();
