(function () {

  angular.module('temperatusApp', []);

  angular
    .module('temperatusApp')
    .controller('mainCtrl', mainCtrl);

  mainCtrl.$inject = ['$scope', '$http', '$timeout'];
  function mainCtrl ($scope, $http, $timeout) {
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
      $http.post('https://formspree.io/qa.alberto@gmail.com', vm.credentials)
      .then(function successCallback(response) {
        vm.formSuccess = "Thanks. We will get back to you as soon as possible.";
        $timeout(function(){vm.clear()}, 7000);
      }, function errorCallback(response) {
        vm.formError = "Error sending the form.";
      });
    };

    vm.clear = function() {
      vm.formSuccess = "";
      vm.credentials.name = "";
      vm.credentials._replyto = "";
      vm.credentials.comment = "";
    };
  }
})();
