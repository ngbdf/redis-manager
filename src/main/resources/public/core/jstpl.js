String.prototype.format = function(args) {
    var result = this;
    if (arguments.length > 0) {
        if (arguments.length == 1 && typeof (args) == "object") {
            for (var key in args) {
                if(args[key]!=undefined){
                    var reg = new RegExp("({" + key + "})", "g");
                    result = result.replace(reg, args[key]);
                }
            }
        }
        else {
            for (var i = 0; i < arguments.length; i++) {
                if (arguments[i] != undefined) {
                    var reg= new RegExp("({)" + i + "(})", "g");
                    result = result.replace(reg, arguments[i]);
                }
            }
        }
    }
    return result;
};

function tpl(template, data){
    var url = "/tpl/" + template + ".tpl";
    var tpl_con = "";
    $.ajax({
        url: url,
        type:'GET',
        async:false,
        timeout:5000,
        dataType:'text',
        success:function( result ){
            tpl_con = result;
        }
    });
    return tpl_con.format( data );
}