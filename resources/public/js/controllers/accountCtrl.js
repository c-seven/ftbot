angular.module('ftbot', ['ngRoute'])
  .config(function($routeProvider){
    $routeProvider
       .when('/', {
        controller: 'AccountListCtrl',
        templateUrl:'/accounts/listform'
       })
      .when('/new',{
          controller : 'AccountCreateCtrl',
          templateUrl : '/accounts/entryform'
       })
      .when('/:accountId/edit',{
          controller : 'AccountEditCtrl',
          templateUrl : '/accounts/entryform'
       })
      .otherwise({
        redirectTo :'/'
      });
  })
  .service('accountsStorage' , function($http){
     var api = {};

      api.loadAccounts = function(){
          return $http({
              method : 'JSONP',
              url : '/accounts/all?callback=JSON_CALLBACK'
          });
      }


      api.create = function(account){
        return this.post('/accounts/create', account)
      }

      api.find = function(id){
          return $http({
              method : 'JSONP',
              url : '/accounts/'+ id + '?callback=JSON_CALLBACK'
          });
      }

      api.update = function(id,account){
        return this.post('/accounts/'+ id + '/update', account)
      }

      api.remove = function(id){
        return $http({
          method : 'DELETE',
          url : '/accounts/' + id
        });
      }

      api.validate = function(orginalAccount, account){
        return $http({
          method : 'POST',
          data : {orgAccount : orginalAccount,
                  newAccount : account},
          url : '/accounts/validate'
        });
      }

      api.fbAuthPage = function(account){
        return $http({
          method : 'POST',
          data : {account : account},
          url : '/accounts/getfbauthurl'
        });
      }

      api.withValidation = function(argAccount, account,then){
          this.validate(argAccount, account)
          .success(function(data){
            if(data.success){
              then();
            }else{
              window.alert(data.errmsgs)
            }
          })
          .error(function(data){
            window.alert('登録データチェックで通信エラーが発生しました');
          });
      };

      //TODO needs refactoring
      api.post = function(url, account){
        return $http({
          method : 'POST',
          data: account,
          url : url
        });
      }

     return api ;
  })
  .controller('AccountListCtrl', function($scope,  accountsStorage) {
      $scope.accounts = [];
      accountsStorage.loadAccounts().success(function(data){
         $scope.accounts  = data;
      });
  })

  .controller('AccountCreateCtrl', function($scope, $location, $q, accountsStorage) {
      $scope.account = {};


      $scope.openFbAuthPage =  function(){
          accountsStorage.fbAuthPage($scope.account)
          .success(function(data){
            window.open(data.url, '_blank')
          })
        .error(function(data){
          window.alert('FB App Idとシークレットが情報が入力されていません');
        });
      }

      $scope.save = function(){
        accountsStorage.withValidation({}, $scope.account, function(){
          if(!window.confirm('このアカウントを登録してよいですか?')){
            return;
          }

         accountsStorage.create($scope.account)
          .success(
           function(data,status,header,config){
              window.alert('アカウントを登録しました。');
              $location.path('/');
           })
        .error(
           function(data,status,header,config){
              window.alert('アカウントの登録に失敗しました。再度登録しなおしてください。');
           })
        });
      };
  })

  .controller('AccountEditCtrl', function($scope, $routeParams, $location, accountsStorage) {

      $scope.removable = true;

      $scope.account = {};
      $scope.org = {};
      accountsStorage.find($routeParams.accountId)
      .success(
        function(data){
          $scope.account = data.account;
          $scope.account.fb_password_verify  =  $scope.account.fb_password ;
          $scope.account.twitter_password_verify  =  $scope.account.twitter_password ;

          $scope.org = angular.copy($scope.account)

        })
      .error(function(){
        window.alert('アカウントの取得に失敗しました。')
      });

      $scope.openFbAuthPage =  function(){
          accountsStorage.fbAuthPage($scope.account)
          .success(function(data){
            window.open(data.url, '_blank')
          })
        .error(function(data){
          window.alert('FB App Idとシークレットが情報が入力されていません');
        });
      }


      $scope.save = function(){
        accountsStorage.withValidation($scope.org, $scope.account, function(){
          if(!window.confirm('このアカウントを更新してよいですか?')){
            return;
          }

         accountsStorage.update($routeParams.accountId, $scope.account)
          .success(
             function(data, status,header,config){
                window.alert('アカウントを更新しました。');
                $location.path('/');
           })
        .error(
           function(data, status, header,config){
              window.alert('アカウントの更新に失敗しました。再度登録しなおしてください。');
           })
        });
      };

      $scope.remove = function(){
        accountsStorage.withValidation($scope.org, $scope.account, function(){
          if(!window.confirm('このアカウントを削除してよいですか?(記事もすべて削除されます)')){
            return;
          }

         accountsStorage.remove($routeParams.accountId)
          .success(
             function(data, status,header,config){
                window.alert('アカウントを削除しました。');
                $location.path('/');
           })
        .error(
           function(data, status, header,config){
              window.alert('アカウントの削除に失敗しました。');
           })
        });
      };

  }).directive('equals', function(){
    return{
      restrict: 'A',
      require: '?ngModel',
      link : function (scope, elem,attrs, ngModel){
        if(!ngModel) return;
        scope.$watch(attrs.ngModel, function(){
          validate();
        });

        attrs.$observe('equals', function(val){
          validate();
        });

        var validate = function(){
          var self = ngModel.$viewValue;
          var that = attrs.equals;
          ngModel.$setValidity('equals', self === that);
        };
     }
  };
  });

