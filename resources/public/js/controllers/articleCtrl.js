angular.module('ftbot', ['ngRoute', 'angularFileUpload'])
  .config(function($routeProvider){
    $routeProvider
       .when('/', {
        controller: 'ArticleListCtrl',
        templateUrl:'/articles/listform'
       })
       .when('/:accountId', {
        controller: 'ArticleListCtrl',
        templateUrl:'/articles/listform'
       })
      .when('/:accountId/new',{
          controller : 'ArticleCreateCtrl',
          templateUrl : '/articles/entryform'
       })
      .when('/:accountId/:articleId/edit',{
          controller : 'ArticleEditCtrl',
          templateUrl : '/articles/entryform'
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
  .service('articlesStorage' , function($http){
     var api = {};

      api.loadArticles = function(accountId){
          return $http({
              method : 'JSONP',
              params: { accountId : accountId ,
                       callback : 'JSON_CALLBACK'},
              url : '/articles/all'
          });
      }

      api.create = function(article){
        return this.post('/articles/create', article)
      }

      api.find = function(id){
          return $http({
              method : 'JSONP',
              url : '/articles/'+ id + '?callback=JSON_CALLBACK'
          });
      }

      api.remove = function(id){
        return $http({
          method : 'DELETE',
          url : '/articles/' + id
        });
      }

      api.update = function(id,article){
        return this.post('/articles/'+ id + '/update', article)
      }

      api.validate = function(orginalArticle, article){
        return $http({
          method : 'POST',
          data : {orgArticle : orginalArticle,
                  newArticle : article},
          url : '/articles/validate'
        });
      }

      api.postfb = function(article){
        return $http({
          method : 'POST',
          data : {article : article},
          url : '/articles/fbpost'
        });
      }

      api.tweet = function(article){
        return $http({
          method : 'POST',
          data : {article : article},
          url : '/articles/tweet'
        });
      }

      api.withValidation = function(argArticle, article,then){
          this.validate(argArticle, article)
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
      api.post = function(url, article){
        return $http({
          method : 'POST',
          data: article,
          url : url
        });
      }

     return api;
  })

  .controller('ArticleListCtrl', function($scope, $location, $routeParams, articlesStorage, accountsStorage, $fileUploader) {

      $scope.uploader =  $fileUploader.create({
        scope:$scope,
        url : 'accounts/fileupload'
       });
       $scope.uploader.bind('success', function (event, xhr, item, response) {
            alert("記事をアップロードしました。")
        });
       $scope.uploader.bind('error', function (event, xhr, item, response) {
            alert("記事のアップロードに失敗しました。")
        });
        $scope.uploader.bind('beforeupload', function (event, item) {

        });

       $scope.fileUpload = function(){
          if(!window.confirm('記事ファイルをアップロードしますか?')){
            return;
          }

       };

      $scope.accountsList = [];
      $scope.articles = [];
      $scope.selectedAccount = {};

      $scope.fileUpload = function(call){

        $scope.uploadFile = "";

      };

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
          window.alert('アカウントが登録されていません。記事を配信するアカウントを登録してください。');
          $location.redirect('/accounts');
        }
      }).error(function (data){
        window.alert('アカウントの取得に失敗しました。');
      });

      $scope.loadArticles = function(accountId){
        articlesStorage.loadArticles(accountId).success(function(data){
           $scope.articles = data;
        })
        .error(function(data){window.alert('記事の読み込みに失敗しました。')});
      }


      $scope.$watch('selectedAccount', function(){
        if($scope.selectedAccount.id) {
          $scope.loadArticles($scope.selectedAccount.id);
        }
      });

  })

  .controller('ArticleCreateCtrl', function($scope, $routeParams, $location, $q, accountsStorage, articlesStorage) {

      $scope.selectedAccount = {}

      $scope.article = {
        can_auto_publish : true,
        can_publish_fb: true,
        can_publish_twitter : true
      };

      accountsStorage.findAccount($routeParams.accountId)
      .success(function(data){
         $scope.selectedAccount = data.account;
         $scope.article.account_id =  data.account.id;
      })
      .error(function(data){
         alert('アカウントの取得に失敗しました。');
      });

      $scope.save = function(){
        articlesStorage.withValidation({}, $scope.article, function(){
          if(!window.confirm('この記事を登録してよいですか?')){
            return;
          }

         articlesStorage.create($scope.article)
          .success(
           function(data,status,header,config){
              window.alert('記事を登録しました。');
              $location.path('/' + $routeParams.accountId);
           })
        .error(
           function(data,status,header,config){
              window.alert('記事の登録に失敗しました。再度登録しなおしてください。');
           })
        });
      };
  })

  .controller('ArticleEditCtrl', function($scope, $routeParams, $location, articlesStorage) {
      $scope.article = {};
      $scope.org = {};
      $scope.removable = true;
      $scope.canPostFb = true;
      $scope.canTweet = true;

      $scope.postfb = function(){
          if(!window.confirm('今すぐにFacebookに投稿しますか?')){
            return;
          }

          articlesStorage.postfb($scope.article)
          .success(function(data){
            window.alert('Facebookに投稿しました');
          })
          .error(function(data){
            window.alert('Facebookへの投稿に失敗しました。');
          });
      }

      $scope.tweet = function(){
          if(!window.confirm('今すぐにツイッターに投稿しますか?')){
            return;
          }

          articlesStorage.tweet($scope.article)
          .success(function(data){
            window.alert('Twitterに投稿しました');
          })
          .error(function(data){
            window.alert('Twitterへの投稿に失敗しました。');
          });
      }

      articlesStorage.find($routeParams.articleId)
      .success(
        function(data){
          $scope.article = data.article;
          $scope.org = angular.copy($scope.article)

        })
      .error(function(){
        window.alert('記事の取得に失敗しました。')
      });

      $scope.save = function(){
        articlesStorage.withValidation($scope.org, $scope.article, function(){
          if(!window.confirm('この記事を更新してよいですか?')){
            return;
          }

         articlesStorage.update($routeParams.articleId, $scope.article)
          .success(
             function(data, status,header,config){
                window.alert('記事を更新しました。');
                $location.path('/' + $routeParams.accountId);
           })
        .error(
           function(data, status, header,config){
              window.alert('記事の更新に失敗しました。再度登録しなおしてください。');
           })
        });
      };

      $scope.remove = function(){
          if(!window.confirm('この記事を削除してよいですか?')){
            return;
          }

         articlesStorage.remove($routeParams.articleId)
          .success(
             function(data, status,header,config){
                window.alert('記事を削除しました。');
                $location.path('/');
           })
        .error(
           function(data, status, header,config){
              window.alert('記事の削除に失敗しました。');
           })
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

