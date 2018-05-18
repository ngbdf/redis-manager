
var win_body_css = 'sparrow_window_content_body';
var current_win_id = [];
var sparrow_win ={
        msg: function(message){
            layer.msg( message );
        },
        alert: function(title, option){
            if( !option ){
                option = {};
            }
            layer.alert( title, option);
        },
        confirm: function( title, callback ){
            layer.alert( title, {
            }, function(){
                if ( 'function' === typeof callback ){
                    callback();
                    layer.closeAll();
                }
            });
        },
        /**
		 * 弹出一个框,如果这个框存在了,将不会再打开, 而只是刷新内容
		 */
		open: function( content, option )
		{
			if ( 'object' !== typeof option )
			{
				option = {};
			}

			var attr = [ 'class="'+ win_body_css +'"' ];
			if ( option.id )
			{
				var win = sparrow.get_id( option.id );
				if ( win )
				{
					if ( win_body_css === win.className )
					{
						var content_div = $( '#' + option.id );
						content_div.find( '*' ).off();
						content_div.html( content );
					}
					else if ( sparrow.is_dev() )
					{
						this.alert( 'ID = ' + option.id + ' 的元素已经存在，请更换窗口ID' );
					}
					return;
				}
				attr.push( 'id="'+ option.id +'"' );
			}
			var arg = {type:1};
			//遮罩
			if ( true === option.modal )
			{
				arg.shade = [0.2, '#eeeeee'];
			}
			else
			{
				arg.shade = false;
			}
			if ( option.title )
			{
				arg.title = option.title;
				arg.closeBtn = 1;
			}
			else
			{
				arg.title = false;
				arg.closeBtn = false;
			}
			//空白
			if ( !sparrow.isset( option.padding ) )
			{
				option.padding = 0;
			}
			else
			{
				option.padding = sparrow.intval( option.padding );
			}
			attr.push( 'style="padding:'+ option.padding +'px"' );
			//高宽
			if ( option.width )
			{
				if ( option.height )
				{
					arg.area = [ arg_width( option.width ), arg_width( option.height ) ];
				}
				else
				{
					arg.area = arg_width( option.width );
				}
			}
			//没有设置宽度, 那就自动判断
			else
			{
				var tmp_div = $( '<span style="display:inline-block;visibility: hidden">'+ content +'</span>' ).appendTo( $(document.body) );
				var tmp_w = tmp_div.width();
				if ( tmp_w < 200 )
				{
					tmp_w = 200;
				}
				tmp_div.remove();
				tmp_div = null;
				arg.area = tmp_w + 'px';
			}
			if ( option.onclose )
			{
				arg.end = function(){
					sparrow.run( option.onclose );
				}
			}
			if ( 'function' === typeof option.beforeclose )
			{
				arg.cancel = option.beforeclose;
			}
			if ( option.zIndex )
			{
				arg.zIndex = option.zIndex;
			}
			arg.content = '<div '+ attr.join( ' ' ) +'>'+ content +'</div>';
			var wid = sparrow_layer.open( arg );
			//var wid = sparrow_layer.open( option );
			if ( arg.cancel )
			{
				current_win_id.push( [wid, option.beforeclose] );
			}
			else
			{
				current_win_id.push( wid );
			}
		}
}


var sparrow ={
    /**
     * 调试
     */
    debug: function( str, is_error )
    {
        if ( typeof window.console == 'undefined' || 'function' != typeof window.console.debug )
        {
            return;
        }
        if ( true === is_error )
        {
            console.error( str );
        }
        else
        {
            if ( 'object' == typeof str && !$.isArray( str ) && null != str )
            {
                console.dir( str );
            }
            else
            {
                console.debug( str );
            }
        }
    },
    /**
     * 输出出错信息
     */
    error: function( str )
    {
        this.debug( str, true );
    },
    bind: function( fn, obj ){
        return function()
        {
            return 	fn.apply( obj, arguments );
        };
    },
    /**
     * 将传入的dom转换为jquery的dom对象
     */
    jq_dom: function( dom )
    {
        if ( 'string' === typeof dom )
        {
            dom = $( '#' + dom );
        }
        else if ( !( dom instanceof $ ) )
        {
            dom = $( dom );
        }
        return dom;
    },
    /**
     * 模仿的empty函数
     */
    empty: function( obj )
    {
        switch ( typeof obj )
        {
            case 'undefined':
                return true;
            break;
            case 'string':
                return 0 == obj.trim().length;
            break;
            case 'number':
                return 0 == obj;
            break;
            case 'object':
                if ( null == obj )
                {
                    return true;
                }
                if ( obj.constructor == Array )
                {
                    return 0 == obj.length;
                }
                else
                {
                    for ( var t in obj )
                    {
                        return false;
                    }
                    return true;
                }
            break;
        }
        return false;
    },
    /**
     * 检查一个对象是否有某个属性
     */
    isset: function( m )
    {
         return 'undefined' !== typeof m && null != m;
    },
    /**
     * 页面上是否有某个id
     */
    has_id: function( html_id )
    {
        return null !== document.getElementById( html_id )
    },
    /**
     * 获取id
     */
    get_id: function( html_id )
    {
        return document.getElementById( html_id );
    },
    /**
     * 简单的继承
     */
    extend: function( source, extend )
    {
        for ( var p in extend )
        {
            if ( this.isset( source[ p ] ) )
            {
                continue;
            }
            source[ p ] = extend[ p ];
        }
    },
    /**
     * 格式化为数字
     */
    intval: function( num )
    {
        var re = parseInt( num || 0, 10 );
        if ( true == isNaN( re ) )
        {
            re = 0;
        }
        return re;
    },
    /**
     * 截取字符器
     */
    substr: function( str, num, join_str )
    {
        //默认的省略符号
        join_str = join_str || '...';
        if ( this.strlen( str ) <= num )
        {
            return str;
        }
        var l = str.length, tl = this.strlen( join_str );
        while ( this.strlen( str ) > num - tl )
        {
            str = str.substring(0, l -1);
            l = l -1;
        }
        return str + join_str;
    },
    /**
     * 字符串长度，中文字符算2个长度
     */
    strlen: function( str )
    {
        str += '';
        return str.replace( /[^\x00-\xFF]/g, '**' ).length;
    },
    is_array: function( obj )
    {
        return this.isset( obj ) && typeof obj === "object" && obj.constructor === Array;
    },
    in_array: function( item, arr )
    {
        if ( !this.is_array( arr ) )
        {
            return -1;
        }
        for( var i = arr.length - 1; i >= 0; --i )
        {
            if ( item === arr[ i ] )
            {
                return i;
            }
        }
        return -1;
    },
    /**
     * 将数组里的指定值移除掉
     * @param {array} arr 数组
     * @param {mixed} val 移除的项, 如果是函数, 返回 true 表示要移除
     */
    array_unset: function( arr, val )
    {
    	var new_arr = [];
    	if ( 'function' === typeof val )
    	{
    		for (  var i = 0; i < arr.length; i++ )
    		{
    			if ( val( arr[ i ] ) )
    			{
    				continue;
    			}
    			new_arr.push( arr[ i ] );
    		}
    	}
    	else
    	{
    		for (  var i = 0; i < arr.length; i++ )
    		{
    			if ( arr[ i ] == val )
    			{
    				continue;
    			}
    			new_arr.push( arr[ i ] );
    		}
    	}
    	return new_arr;
    },
    url_format_param: function( url, param ){
        url += "?"
        for(var key in param){
            url += key + "=" + param[key] + "&";
        }
        return url;
    }
}

/**
 * 清理宽度
 */
function arg_width( width )
{
    if ( /^[\d]+$/.test( width ) )
    {
        return width + 'px';
    }
    return width;
}


window.sparrow = sparrow;