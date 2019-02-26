
$("#login-button").click(function(){
    var user = sparrow_form.encode( "login-form",0 ); //0 表示所有字段都提交， 2 表示有改变的才提交
    if ( sparrow.empty( user ) )
    {
        return;
    }
    verifyLogin(user.username,user.password,function(obj){
        if( obj.code == 0 ){
            window.parent.location.href="/";
        }else{
            sparrow_win.msg("user or password is error!");
        }
    });
});