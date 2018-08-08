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

$("#login-button").click(function(){
    var user = sparrow_form.encode( "login-form",0 ); //0 表示所有字段都提交， 2 表示有改变的才提交
    console.log( user );
    if ( sparrow.empty( user ) )
    {
        return;
    }
    verifyLogin(user.username,user.password,function(obj){
        if( obj.code == 0 ){
            window.location.href="/index";
        }
    });
});