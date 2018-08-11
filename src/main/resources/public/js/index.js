$(function(){
    window.setInterval("heartbeat()", 20000);
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

function heartbeat(){
    ajax.async_get('/heartbeat',function(obj){

    });
}