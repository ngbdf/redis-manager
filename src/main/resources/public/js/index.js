$(function(){
    autoGetUser(function(obj){
        var username = obj.res["username"];
        $("#username").text(username);
    });
});

$("#user-logout").click(function(){
    logout(function(){
        window.location.href="/user/login";
    });
});
