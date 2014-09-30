angular.module('ftbot', ['ngRoute'])
  .config(function($routeProvider){
    $routeProvider
       .when('/', {
        controller: 'SchduleListCtrl',
        templateUrl:'/schedules/listform'
       })
       .when('/:accountId', {
        controller: 'SchduleListCtrl',
        templateUrl:'/schedules/listform'
       })
      .when('/:accountId/new',{
          controller : 'SchduleCreateCtrl',
          templateUrl : '/schedules/entryform'
       })
      .when('/:accountId/:scheduleId/edit',{
          controller : 'SchduleEditCtrl',
          templateUrl : '/schedules/entryform'
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
             params: {callback : 'JSON_CALLBACK'},
             url : '/accounts/all'
        });
     }

     api.findAccount = function(accountId){
        return $http({
             method : 'JSONP',
             params: {callback : 'JSON_CALLBACK'},
             url : '/accounts/' + accountId
        });
     }
     return api;

  })
  .service('schedulesStorage' , function($http){
     var api = {};

      api.loadSchdules = function(accountId){
          return $http({
              method : 'JSONP',
              params: { accountId : accountId ,
                       callback : 'JSON_CALLBACK'},
              url : '/schedules/all'
          });
      }

      api.remove = function(id){
        return $http({
          method : 'DELETE',
          url : '/schedules/' + id
        });
      }

      api.create = function(schedule){
        return this.post('/schedules/create', schedule)
      }

      api.find = function(id){
          return $http({
              method : 'JSONP',
              url : '/schedules/'+ id + '?callback=JSON_CALLBACK'
          });
      }

      api.update = function(id,schedule){
        return this.post('/schedules/'+ id + '/update', schedule)
      }

      api.validate = function(orginalSchdule, schedule){
        return $http({
          method : 'POST',
          data : {orgSchdule : orginalSchdule,
                  newSchdule : schedule},
          url : '/schedules/validate'
        });
      }

      api.withValidation = function(argSchdule, schedule,then){
          this.validate(argSchdule, schedule)
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
      api.post = function(url, schedule){
        return $http({
          method : 'POST',
          data: schedule,
          url : url
        });
      }

     return api;
  })

  .controller('SchduleListCtrl', function($scope, $location, $routeParams, schedulesStorage, accountsStorage) {

      $scope.accountsList = [];
      $scope.schedules = [];
      $scope.selectedAccount = {};
      accountsStorage.loadAccounts().success(function(data){
        $scope.accountsList = data.map(function(x){
          return { id :x.id , value : x.account_name}
          });

        if($scope.accountsList && $scope.accountsList.length > 0){

          if($routeParams.accountId){
            var initialAccount = null;
            angular.forEach($scope.accountsList, function(each) {
              if(each.id.toString() === $routeParams.accountId){
                initialAccount = each;
              }
            });
            if(initialAccount) $scope.selectedAccount = initialAccount;
            else $scope.selectedAccount = $scope.accountsList[0];
          }else{
            $scope.selectedAccount = $scope.accountsList[0];
          }
        }else{
          window.alert('アカウントが登録されていません。配信スケジュールを配信するアカウントを登録してください。');
          $location.redirect('/accounts');
        }
      }).error(function (data){
        window.alert('アカウントの取得に失敗しました。');
      });

      $scope.loadSchdules = function(accountId){
        schedulesStorage.loadSchdules(accountId).success(function(data){
           $scope.schedules = data;
        })
        .error(function(data){window.alert('配信スケジュールの読み込みに失敗しました。')});
      }


      $scope.$watch('selectedAccount', function(){
        if($scope.selectedAccount.id) {
          $scope.loadSchdules($scope.selectedAccount.id);
        }
      });
  })

  .controller('SchduleCreateCtrl', function($scope, $routeParams, $location, $q, accountsStorage, schedulesStorage) {
      $scope.selectedAccount = {}
      $scope.spanList = [
         {code: "d",value:"毎日"},
         {code: "w_mon",value:"毎週月曜日"},
         {code: "w_tue",value:"毎週火曜日"},
         {code: "w_wed",value:"毎週水曜日"},
         {code: "w_thu",value:"毎週木曜日"},
         {code: "w_fri",value:"毎週金曜日"},
         {code: "w_sat",value:"毎週土曜日"},
         {code: "w_sun",value:"毎週日曜日"},
      ];

      $scope.schedule = {
        span :  $scope.spanList[0],
        available : true
      };

      accountsStorage.findAccount($routeParams.accountId)
      .success(function(data){
         $scope.selectedAccount = data.account;
         $scope.schedule.account_id =  data.account.id;
      })
      .error(function(data){
         alert('アカウントの取得に失敗しました。');
      });

      $scope.formatModel = function (model){
          var r = angular.copy(model);
          r.schedule_span =  model.span.code;
          r.schedule_time =  model.time.replace(/:/i, '');
          return r;
      }

      $scope.save = function(){
        var entity =  $scope.formatModel($scope.schedule);
        schedulesStorage.withValidation({}, entity, function(){
          if(!window.confirm('この配信スケジュールを登録してよいですか?')){
            return;
          }

         schedulesStorage.create(entity)
          .success(
           function(data,status,header,config){
              window.alert('配信スケジュールを登録しました。');
              $location.path('/' + $routeParams.accountId);
           })
          .error(
           function(data,status,header,config){
              window.alert('配信スケジュールの登録に失敗しました。再度登録しなおしてください。');
           })
        });
      };
  })

  .controller('SchduleEditCtrl', function($scope, $routeParams, $location, accountsStorage,schedulesStorage) {

      $scope.spanList = [
         {code: "d",value:"毎日"},
         {code: "w_mon",value:"毎週月曜日"},
         {code: "w_tue",value:"毎週火曜日"},
         {code: "w_wed",value:"毎週水曜日"},
         {code: "w_thu",value:"毎週木曜日"},
         {code: "w_fri",value:"毎週金曜日"},
         {code: "w_sat",value:"毎週土曜日"},
         {code: "w_sun",value:"毎週日曜日"},
      ];

      $scope.schedule = {
        span :  $scope.spanList[0],
        available : true
      };
      $scope.org = {};

      $scope.removable = true;

      $scope.formatModel = function (model){
          var r = angular.copy(model);
          r.schedule_span =  model.span.code;
          r.schedule_time =  model.time.replace(/:/i, '');
          return r;
      }

      $scope.formatView = function (model){
          var r = angular.copy(model);
          angular.forEach($scope.spanList, function(x){
            if(x.code === model.schedule_span){
              r.span = x;
            }
          });

          r.time = model.schedule_time.substring(0,2) + ":" + model.schedule_time.substring(2)
          return r;
      }

      accountsStorage.findAccount($routeParams.accountId)
      .success(function(data){
         $scope.selectedAccount = data.account;
         $scope.schedule.account_id =  data.account.id;
      })
      .error(function(data){
         alert('アカウントの取得に失敗しました。');
      });


      schedulesStorage.find($routeParams.scheduleId)
      .success(
        function(data){
          var model = $scope.formatView(data.schedule);
          $scope.schedule = model
          $scope.org = angular.copy($scope.schedule)
        })
      .error(function(){
        window.alert('配信スケジュールの取得に失敗しました。')
      });

      $scope.save = function(){

        var entity =  $scope.formatModel($scope.schedule);
        schedulesStorage.withValidation($scope.org, entity, function(){
          if(!window.confirm('この配信スケジュールを更新してよいですか?')){
            return;
          }

         schedulesStorage.update($routeParams.scheduleId, entity)
          .success(
             function(data, status,header,config){
                window.alert('配信スケジュールを更新しました。');
                $location.path('/' + $routeParams.accountId);
           })
        .error(
           function(data, status, header,config){
              window.alert('配信スケジュールの更新に失敗しました。再度登録しなおしてください。');
           })
        });
      };

      $scope.remove = function(){
          if(!window.confirm('このスケジュールを削除してよいですか?')){
            return;
          }

         schedulesStorage.remove($routeParams.scheduleId)
          .success(
             function(data, status,header,config){
                window.alert('スケジュールを削除しました。');
                $location.path('/');
           })
        .error(
           function(data, status, header,config){
              window.alert('スケジュールの削除に失敗しました。');
           })
      };

  }).directive('time', function(){
    return{
      restrict: 'A',
      require: '?ngModel',
      link : function (scope, elem,attrs, ngModel){
        if(!ngModel) return;
        scope.$watch(attrs.ngModel, function(){
          validate();
        });

        var validate = function(){
          var isValid = false;
          var val = ngModel.$viewValue;
          var format = /^([0-2][0-9]:[0-5][0-9])$/
          if(!val){
            return;
          }

          if (!val.match(format)){
              isValid = false;
          }else {
            var hm  = val.split(':');
            var h = parseInt(hm[0])
            var m = parseInt(hm[1])
            if(h < 0  || h >= 24){
              isValid = false;
            }else if(m < 0 || m > 59){
              isValid = false;
            }else {
              isValid = true;
            }
          }
          ngModel.$setValidity('format', isValid);

        };
     }
  };
  });

