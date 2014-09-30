//http://www.ng-newsletter.com/posts/validations.html
//http://stackoverflow.com/questions/14012239/password-check-directive-in-angularjs
//angular.module('ftbot', []).directive('equals', function(){return {};});


//    return{
//      restrict: 'A',
//      require: '?ngModel',
//      link : function (scope, elem,attrs, ngModel){
//        if(!ngModel) return;
//
//        scope.$watch(attrs.ngModel, function(){
//          console.log('fuc'); // TODO
//          validate();
//        });
//
//        attrs.$observe('equals', function(val){
//          console.log('fuc'); // TODO
//          validate();
//        });
//
//
//        var validate = function(){
//          var self = ngModel.$viewValue;
//          var that = attrs.equals;
//          ngModel.$setValidity('equals', self === that);
//        };
//     }
//  };



//.directive('ensureUniqueUsername', ['$http',function($http){
//    return{
//      require : 'ngModel',
//      link: function(scope, elem, attrs, c){
//       scope.$watch(attrs.ngModel, function(){
//         $http({
//           method: 'GET',
//           url : '/api/checkunique/'+ attrs.publisher + '/' +
//                 attrs.ensureUnique,
//           data : {'field' : attrs.ensureUnique}
//         }).success(function(){
//           c.$setValiditity('unique', data.isUnique);
//         }).error(function(data,status,headers,cfg){
//           c.$setValiditity('unique', false);
//         })
//        });
//      }
//    };
//  }])
//
