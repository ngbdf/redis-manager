$(function(){
    window.setInterval("heartbeat()", 20000);
    autoGetUser(function(obj){
        var user = obj.res;
        if(user != null && user != '' && user != 'undefined') {
            var username = obj.res["username"];
            $("#username").text(username);
        } else {
            window.parent.location.href="/user/login";
        }

    });
});

$("#user-logout").click(function(){
    logout(function(){
        window.parent.location.href="/user/login";
    });
});

function heartbeat(){
    ajax.async_get('/heartbeat',function(obj){

    });
}