var $ = jQuery.noConflict();
var sparrow_form = {}

var error_css = 'error', ok_css = 'ok', info_css = 'info';
$( document ).on( 'focus', 'input,textarea,select', input_box_focus );
$( document ).on( 'blur', 'input,textarea,select', input_box_blur );
$( document ).on( 'change', 'input,textarea', input_box_change );

/**
 * 内容change
 */
function input_box_change( inputobj )
{
	var input = $( this );

	//如果是checkbox
	if ( 'checkbox' === this.type )
	{
		input = get_checkbox_input( input );
	}
	input.data( 'is_change', 1 );
	input.data( 'this_time_change', 1 );
	var require_for = input.data( 'require-for' );
	if ( require_for )
	{
		var tmp = input_obj_find( require_for );
		if ( false !== tmp )
		{
			tmp.trigger( 'blur' );
		}
	}
	if ( 'checkbox' === this.type )
	{
		if ( 1 !== input.data( 'this_time_focus' ) )
		{
			input.trigger( 'focus' );
			input.data( 'this_time_change', 1 );
		}
		input.trigger( 'blur' );
	}
}


var ignore_list = {
	button: true,
	submit: true,
	radio: true,
	reset: true,
	search: true,
	file: true,
	image: true
};

/**
 * 获取一个checkbox组的带data-require那个组
 */
function get_checkbox_input( input )
{
	var name = input.attr( 'name' );
	if ( !name )
	{
		return input;
	}
	var re = null;
	var items = $( '[name="'+ name +'"]:checkbox' );
	if ( 1 === items.length )
	{
		return input;
	}
	items.each( function( i, item ){
		if ( item.getAttribute( 'data-require' ) )
		{
			if ( !re )
			{
				re = item;
			}
		}
	} );
	if ( null === re )
	{
		return input;
	}
	return $( re );
}

/**
 * 获取焦点
 */
function input_box_focus()
{
	if ( this.type && sparrow.isset( ignore_list[ this.type ] ) )
	{
		return;
	}
	var input = $( this );
	var tmp = input_get_tip_box( this );
	var tip_box = tmp.box;
	if ( !tip_box )
	{
		return;
	}
	if ( 'checkbox' === this.type )
	{
		input = get_checkbox_input( input );
	}
	if ( !input.data( 'first-focus' ) )
	{
		input_first_forcus( input );
	}
	//还原input 输入框的 css
	else
	{
		input[ 0 ].className = input.data( 'default-css' );
	}
	input.data( 'this_time_focus', 1 );
	input.data( 'this_time_change', 0 );
	var focus_msg = input.data( 'focus-msg' );
	//获得焦点时提示消息
	if ( focus_msg && 3 !== tmp.type )
	{
		show_tip_msg( tip_box, tmp.type, focus_msg, info_css );
	}
	//获取焦点时还原成默认的样式
	else
	{
		reset_tip_box( tip_box, tmp.type );
	}
}

/**
 * 第一次focus
 */
function input_first_forcus( input )
{
	var tmp = input_get_tip_box( input[ 0 ] );
	var tip_box = tmp.box;
	if ( null === tip_box )
	{
		return;
	}
	var type = input.data( 'type' );
	switch( type )
	{
		case 'color':
			var sparrow_color = require( 'colorpicker' );
			if ( sparrow_color )
			{
				sparrow_color.init( input );
				input.trigger( 'mousedown' );
			}
			else
			{
				dev_tip_msg( input, '主页面的js没有引用 colorpicker' );
			}
		break;
	}
	input.data( 'first-focus', 1 );
	input.data( 'default-css', input[ 0 ].className );
	tip_box.data( 'default-css', tip_box[ 0 ].className );
	if ( 3 !== tmp.type )
	{
		tip_box.data( 'default-text', tip_box.html() );
	}
}

/**
 * 还原tip_box
 */
function reset_tip_box( tip_box, type )
{
	tip_box[ 0 ].className = tip_box.data( 'default-css' );
	tip_box.html( tip_box.data( 'default-text' ) );
}

/**
 * 显示成功消息
 */
function show_ok_tip( tip_box, type )
{
	if ( 3 === type )
	{
		return;
	}
	var msg = tip_box.data( 'default-text' );
	if ( sparrow.empty( msg ) )
	{
		reset_tip_box( tip_box, type );
		if ( 1 === type )
		{
			return;
		}
	}
	show_tip_msg( tip_box, type, msg, ok_css );
}

/**
 * 出错消息
 */
function show_tip_error( tip_box, tip_box_type, err_msg )
{
	show_tip_msg( tip_box, tip_box_type, err_msg, error_css );
}

/**
 * 需求验证
 */
function data_require_check( input, input_value )
{
	var value_require = input.data( 'require' );

	if ( !value_require )
	{
		var if_require = input.data( 'require-if' );
		if ( if_require )
		{
			var all_ok = true, val, tmp;
			var if_arr = if_require.split( '|' );
			for ( var i = 0; i < if_arr.length; i++ )
			{
				tmp = if_arr[ i ].split( ':' );
				val = sparrow_form.value( tmp[ 0 ] );
				if ( 1 === tmp.length )
				{
					if ( 0 === val.length )
					{
						all_ok = false;
						break;
					}
				}
				else if ( val != tmp[ 1 ] )
				{
					all_ok = false;
					break;
				}
			}
			if ( all_ok )
			{
				value_require = 1;
			}
		}
	}

	//必须填写
	if ( value_require )
	{
		if ( 'SELECT' === input[0].nodeName )
		{
			var def_value = input.data( 'select-default' );
			if ( def_value == input_value )
			{
				return false;
			}
		}
		if ( 'checkbox' === input.attr( 'type' ) )
		{
			var name = input.attr( 'name' );
			var check_num = 0;
			$( '[name="'+ name +'"]:checkbox' ).each( function( i, item ){
				if ( item.checked )
				{
					check_num++;
				}
			} );
			input.data( 'check-count', check_num );
			return check_num > 0;
		}
		var is_empty = ( input_value.length < 1 );
		//如果值为空, 检查是否是和其它input关联检查
		if ( is_empty && !input_require_check( input, value_require ) )
		{
			return false;
		}
	}
	return true;
}

/**
 * 类型验证
 */
function data_type_check( input, input_value )
{
	var type = input.data( 'type' );
	if ( 'string' !== typeof type )
	{
		type = input.attr( 'type' ) || 'text';
		//ie下 jquery给加的
		if ( 'select-one' === type )
		{
			type = 'text';
		}
	}
	if ( -1 !== type.indexOf( '|' ) )
	{
		var type_arr = type.split( '|' );
		var re_value = false;
		for ( var i = 0; i < type_arr.length; i++ )
		{
			var tmp = type_arr[ i ];
			if ( tmp.length <= 0 || !sparrow.isset( check_type[ tmp ] ) )
			{
				continue;
			}
			//只要有一个是正确的就可以了
			if ( check_type[ tmp ]( input, input_value ) )
			{
				re_value = true;
				break;
			}
		}
		return re_value;
	}
	//类型检查
	if ( !sparrow.isset( check_type[ type ] ) )
	{
		dev_tip_msg( input, '未知的type:' + type );
		return true;
	}
	return check_type[ type ]( input, input_value );
}

/**
 * 长度验证
 */
function data_len_check( input )
{
	//长度检查
	var len_str = input.data( 'len' );
	if ( len_str && !check_input_len( len_str, input.val() ) )
	{
		return false;
	}
	return true;
}

/**
 * 失去焦点
 */
function input_box_blur()
{
	if ( this.type && sparrow.isset( ignore_list[ this.type ] ) )
	{
		return true;
	}
	if ( 'none' === this.style.display )
	{
		return true;
	}
	var input = $( this );
	//必须先响应focus事件
	if ( 'checkbox' === this.type )
	{
		input = input = get_checkbox_input( input );
	}
	var is_encode_eve = input.data( 'encode' );
	if ( !is_encode_eve && 1 !== input.data( 'this_time_change' ) && false !== input.data( 'check_result' ) )
	{
		return;
	}
	input.data( 'this_time_focus', 0 );
	var input_value = input.val();
	if ( 'string' !== typeof input_value )
	{
		input_value = '';
	}
	input_value = input_value.trim();
	var tmp = input_get_tip_box( this );
	var tip_box = tmp.box;
	if ( !tip_box )
	{
		return true;
	}
	//如果获取焦点有提示消失, 在失去焦点时恢复之前的提示
	if ( input.data( 'focus-msg' ) )
	{
		reset_tip_box( tip_box, tip_box_type );
	}
	var tip_box_type = tmp.type;
	var require_check = data_require_check( input, input_value );
	var type_check = true, len_check = true, url_check = true, check_max_re = true,check_min_re = true;
	if ( input_value.length > 0 )
	{
		type_check = data_type_check( input, input_value );
		len_check = data_len_check( input );
	}
	//checkbox的特殊判断
	if ( 'checkbox' === input.attr( 'type' ) )
	{
		var require_num = input.data( 'require' );
		var check_count = input.data( 'check-count' );
		var max_num = parseInt( input.data( 'max-check') );
		var min_num = parseInt( input.data( 'min-check') );
		if ( is_encode_eve && require_num > check_count )
		{
			require_check = false;
		}
		else if ( max_num > 0 && check_count > max_num )
		{
			check_max_re = false;
		}
		else if ( min_num > 0 && check_count < min_num )
		{
			check_min_re = false;
		}
	}
	//sparrow-checkbox判断
	if ( input.data( 'checkbox' ) )
	{
		input_box_focus.apply( input[ 0 ] );
		var require_num = input.data( 'require' ); // require 数值表示可以填的数目
		var max_num = parseInt( input.data( 'max-check') );
		var min_num = parseInt( input.data( 'min-check') );
		var arr = input.val().split( ',' );
		if ( is_encode_eve && require_num > arr.length )
		{
			require_check = false;
		}
		else if ( max_num > 0 && arr.length > max_num )
		{
			check_max_re = false;
		}
		else if ( min_num > 0 && arr.length < min_num )
		{
			check_min_re = false;
		}
	}

	// lzz 检测关联字段
	var check_fields_str = input.data("check-fields");
	if( ("string" == typeof check_fields_str) && !is_encode_eve && require_check && type_check && len_check ){
		var check_fields = check_fields_str.split(",");
		for(var i in check_fields){
			var check_field = check_fields[i];
			var check_input = $("[name='" + check_field + "']");
			if( check_input.val() ){
				check_input.trigger( 'focus' );
				check_input.data("this_time_change", 1);
				check_input.trigger( 'blur' );
			}
		}
	}

	//服务端检测
	var urlstr = input.data( 'server-check' ), url_check_msg = '';
	//如果是调用 sparrow_form.encode方法, 不会执行这里
	if ( urlstr && !is_encode_eve && require_check && type_check && len_check )
	{
		var checking = input.data( 'checking' );
		if ( checking )
		{
			return;
		}
		var name = input_name_detect( this );
		if ( !name )
		{
			return;
		}
		input.data( 'checking', 1 );
		var data = {};
		data[ name ] = input.val().trim();
		var alias = input.data('alias');
		if( alias ){
			data[ alias ] = data[ name ];
		}
		var relationstr = input.data("relation");
		var ajax_type = input.data("ajax-type");
		var relations = [];
		if( relationstr ){
		    relations = relationstr.split(",");
		}
		var check_field = true;
		for(var i in relations){
			var relation = relations[i];
			var value = $("[name='" + relation+ "']").val();
			if( value == null || value == "" || typeof(value) == "undefined" ){
				input.data( 'checking', 0 );
				show_tip_error( tip_box, tip_box_type, relation + ' field prior');
				check_field = false;
				return;
			}else{
				var alias_name = $("[name='" + relation+ "']").data('alias');
				data[ alias_name ] = value;
				data[ relation ] = value;
			}
		}
		if( check_field ){
			if ( 3 !== tip_box_type )
			{
				show_tip_msg( tip_box, tip_box_type, 'checking ...', info_css );
			}
			function ajax_check (  re, arg ){
				if( url_check == true ){
					arg.data( 'checking', 0 );
					reset_tip_box( tip_box, tip_box_type );
					if ( re.code > 0 ){
						url_check = false;
						url_check_msg = re.msg;
					}
					show_check_result();
				}
			};
			var urlarr = urlstr.split(",");
			urlarr.forEach(function(url){
			    if( url_check == true ){
			        if( ajax_type != "get" ){
                        ajax.post( url, JSON.stringify(data), ajax_check, false, input);
                    }else{
                        var param_url = sparrow.url_format_param(url, data);
                        ajax.get( param_url, ajax_check, false, input);
                    }
			    }
			});
		}
	}
	else
	{
		show_check_result();
	}

	//如果是 包含 group-data lzz,检测关联字段
	var group_data = input.data( "group" );
	if( "string" == typeof group_data ){
		$("[data-require='" + group_data + "']").trigger( 'focus' ).trigger( 'blur' );
	}

	return require_check && type_check && len_check && check_max_re && check_min_re;
	/**
	 * 显示检查结果
	 */
	function show_check_result()
	{
		if ( require_check && type_check && len_check && url_check && check_max_re && check_min_re )
		{
			if ( 'SELECT' !== input[ 0 ].nodeName && 'checkbox' !== input.attr( 'type' ) && input_value.length > 0 )
			{
				show_ok_tip( tip_box, tip_box_type );
			}
			input.data( 'check_result', true );
			return;
		}
		input.data( 'check_result', false );
		var err_msg = 'error';
		//如果要求的没有填写
		if ( !require_check )
		{
			err_msg = tip_msg_detect( input, 'require-msg' );
		}
		else if ( !type_check )
		{
			err_msg = tip_msg_detect( input, 'format-msg' );
		}
		else if ( !len_check )
		{
			err_msg = tip_msg_detect( input, 'len-msg' );
		}
		else if ( !url_check )
		{
			err_msg = url_check_msg;
		}
		else if ( !check_max_re )
		{
			err_msg = tip_msg_detect( input, 'max-check-msg' );
		}
		else if ( !check_min_re )
		{
			err_msg = tip_msg_detect( input, 'min-check-msg' );
		}
		show_tip_error( tip_box, tip_box_type, err_msg );
	}
}

/**
 * 检测错误提示
 */
function tip_msg_detect( input, msg_type )
{
	var str = input.data( msg_type );
	if ( str )
	{
		return str;
	}
	if ( 'format-msg' === msg_type )
	{
		var def_type_arr = {
			email: 'email格式填写不正确',
			mobile: '手机号码格式错误',
			tel: '电话号码格式错误',
			url: '网址格式错误',
			date: '日期格式错误',
			datetime: '时间格式错误',
			ip: 'IP地址格式错误'
		};
		var tp = input.data( 'type' ) || input[ 0 ].type;
		if ( tp && def_type_arr[ tp ] )
		{
			return def_type_arr[ tp ];
		}
	}
	var def = input.data( 'default' );
	if ( !sparrow.empty( def ) )
	{
		return def;
	}
	else
	{
		var tmp = input_get_tip_box( input[ 0 ] );
		if ( 1 === tmp.type && !sparrow.empty( tmp.box.html() ) )
		{
			return tmp.box.html();
		}
	}
	var placeholder = input.attr( 'placeholder' );
	if ( placeholder )
	{
		return placeholder;
	}
	var def_arr = {
		'format-msg': 'the format is incorrect',
		'len-msg' : 'the content length is illegal',
		'require-msg': 'this is required field',
		'max-check-msg': '你选中的项太多了'
	};
	return def_arr[ msg_type ] || '填写出错';
}

/**
 * 检查为空的情况
 */
function input_require_check( input, value_require )
{
	if ( 1 === value_require )
	{
		return false;
	}
	if ( 'string' !== typeof value_require )
	{
		return false;
	}
	var name = input_name_detect( input[ 0 ] );
	if ( false === name )
	{
		dev_tip_msg( input, '缺少 name 或者 id 属性' );
		return false;
	}
	var result = false;
	var tmp_arr = ( value_require ).split( ',' );
	for ( var i = tmp_arr.length - 1; i >= 0; --i )
	{
		var tmp = tmp_arr[ i ].trim();
		if ( sparrow.empty( tmp ) )
		{
			continue;
		}
		var inp = input_obj_find( tmp );
		if ( false === inp )
		{
			dev_tip_msg( input, 'require 属性 找不到:' + tmp );
			continue;
		}
		inp.data( 'require-for', name );
		var tmp_val = inp.val();
		if ( 'string' === typeof tmp_val && tmp_val.trim().length > 0 )
		{
			result = true;
		}
	}
	return result;
}

var check_type = {};

/**
 * 公用
 */
function input_get_tip_box( input )
{
	var tip_box = null, tip_box_type = 1, suffix = '_tip';
	//首先以 input 的 name 属性 + _tip找
	if ( input.name && sparrow.has_id( input.name + suffix ) )
	{
		tip_box = $( '#' + input.name + suffix );
	}
	//如果没有, 再以input 的 id 属性 + '_tip'找
	if ( null === tip_box && input.id && sparrow.has_id( input.id + suffix ) )
	{
		tip_box = $( '#' + input.id + suffix );
	}
	if ( tip_box )
	{
		if ( tip_box.hasClass( 'sparrow_input_tip_2' ) )
		{
			tip_box_type = 2;
		}
		else if ( tip_box.hasClass( 'sparrow_input_tip_3' ) )
		{
			tip_box_type = 3;
		}
	}
	return {
		box: tip_box,
		type: tip_box_type
	};
}

/**
 * 显示提示内容
 */
function show_tip_msg( tip_box, tip_box_type, msg, css )
{
	//如果是3, 只能显示错误消息
	if ( 3 === tip_box_type && error_css !== css )
	{
		return;
	}
	if ( 2 === tip_box_type && ok_css === css )
	{
		msg = '';
	}
	var pre_fix = 'sparrow_input_tip_';
	tip_box.removeClass( 'hide' );
	tip_box.removeClass( pre_fix + error_css );
	tip_box.removeClass( pre_fix + ok_css );
	tip_box.removeClass( pre_fix + info_css );
	if ( 3 === tip_box_type )
	{
		msg = '<b class="sparrow_validate_icon">'+ msg +'</b><a href="javascript:;" class="sparrow_input_tip_close">X</a><span class="sparrow_validate_arrow"><i></i><em></em></span>';
	}
	tip_box.html( msg );
	if ( !sparrow.empty( css ) )
	{
		tip_box.addClass( pre_fix + css );
	}
}

/**
 * 为input增加class
 */
function add_input_class( input, css )
{
	var prefix = 'sparrow_input_';
	input.removeClass( prefix + error_css );
	input.removeClass( prefix + ok_css );
	if ( css )
	{
		input.addClass( prefix + css );
	}
}

/**
 * 日期格式 2017-03-09
 */
check_type.date = function( input, date_str )
{
	if ( !/^[\d]{4}-[\d]{1,2}-[\d]{1,2}$/.test( date_str ) )
	{
		return false;
	}
	var tmp = date_str.split( '-' );
	if ( tmp[ 1 ] > 12 || tmp[ 2 ] > 31 )
	{
		return false;
	}
	return true;
}

/**
 * 时间格式 2017-06-22 23:08:22
 */
check_type.datetime = function( input, date_str )
{
	var tmp = date_str.split( ' ' );
	if ( 2 !== tmp.length )
	{
		return false;
	}
	if ( !check_type.date( input, tmp[ 0 ] ) )
	{
		return false;
	}
	if ( !/^[\d]{1,2}:[\d]{1,2}:[\d]{1,2}$/.test( tmp[ 1 ] ) )
	{
		return false;
	}
	var arr = tmp[ 1 ].split( ':' );
	return ( arr[ 0 ] < 24 && arr[ 1 ] < 60 && arr[ 2 ] < 60 );
}


/**
 * 验证email
 */
check_type.email = function( input, email_str )
{
	var value = sbc_case_convert( email_str );
	input.val( value );
	var tmp = value.split( '@' );
	if ( 2 !== tmp.length )
	{
		return false;
	}
	if ( !/^[\w.]+$/.test( tmp[ 0 ] ) )
	{
		return false;
	}
	if ( tmp[ 0 ].length > 36 )
	{
		return false;
	}
	if ( !/^[a-zA-Z\d]+\.[a-zA-Z]{2,4}(\.[a-zA-Z]{2,4})?$/.test( tmp[ 1 ] ) )
	{
		return false;
	}
	if ( tmp[ 1 ].length > 30 )
	{
		return false;
	}
	return true;
}

/**
 * 全角转半角
 */
function sbc_case_convert( str )
{
	var tmp = '';
	//全角转半角
	for( var i = 0; i < str.length; i++ )
	{
		var c = str.charCodeAt( i );
		if( c > 65248 && c < 65375 )
		{
			c -= 65248;
		}
		tmp += String.fromCharCode( c );
	}
	return tmp;
}



/**
 * 数字检查
 */
check_type.number = function( input, str )
{
	var tmp = sbc_case_convert( str );
	input.val( tmp );
	return /^-?[\d]+$/.test( tmp );
}

/**
 * 价格检查
 */
check_type.price = function( input, str )
{
	if ( !check_type.float( input, str ) )
	{
		return false;
	}
	var precision = parseInt( input.data( 'precision' ) );
	if ( !precision )
	{
		precision = 2;
	}
	var value = parseFloat( input.val() ).toFixed( precision );
	input.val( value );
	var min = input.data( 'min' );
	var max = input.data( 'max' );
	if ( undefined !== min )
	{
		min = parseFloat( min );
		if ( value < min )
		{
			input.val( min );
		}
	}
	if ( undefined !== max )
	{
		max = parseFloat( max );
		if ( value > max )
		{
			input.val( max );
		}
	}
	return true;
}

/**
 * 符点数检查
 */
check_type.float = function( input, str )
{
	var tmp = sbc_case_convert( str );
	input.val( tmp );
	return /^-?[\d]+(\.[\d]+)?$/.test( tmp );
}

/**
 * 检查范围
 */
check_type.range = function( input, value )
{
	var re = check_type.number( input, value );
	if ( !re )
	{
		return false;
	}
	var min = input.data( 'min' );
	var max = input.data( 'max' );
	if ( undefined !== min )
	{
		min = parseInt( min, 10 );
		if ( value < min )
		{
			return false;
		}
	}
	if ( undefined !== max )
	{
		max = parseInt( max, 10 );
		if ( value > max )
		{
			return false;
		}
	}
	return re;
}

/**
 * 检查字符串
 */
check_type.password = check_type.text = check_type.hidden = function( input, value )
{
	var format = input.data( 'format' );
	//有一些特殊情况
	if ( 'string' === typeof format )
	{
		if ( -1 !== format.indexOf( '::' ) && format.length > 6 )
		{
			//自定义函数检查
			if ( 0 === format.indexOf( 'func::' ) )
			{
				var func = format.substring( 6 );
				if ( 'function' !== typeof window[ func ] )
				{
					dev_tip_msg( input, '自定义检查函数:' + func + '不存在' );
					return false;
				}
				return window[ func ]( value );
			}
			//必须和另外一个表单相同
			if ( 0 === format.indexOf( 'same::' ) )
			{
				var name = format.substring( 6 );
				var tmp_inp = input_obj_find( name );
				if ( false !== tmp_inp )
				{
					var tmp_name = input_name_detect( input[ 0 ] );
					if ( false !== tmp_name )
					{
						tmp_inp.data( 'require-for', tmp_name );
					}
					return value === tmp_inp.val().trim();
				}
			}
		}
		var reg = new RegExp( '^' + format + '$' );
		return reg.test( value );
	}
	return true;
}

/**
 * 长度检查
 */
function check_input_len( allow_len, value )
{
	if ( 'number' === typeof allow_len )
	{
		return value.length >= allow_len;
	}
	else if ( 'string' === typeof allow_len )
	{
		var tmp = allow_len.split( '-' );
		if ( 2 === tmp.length )
		{
			var min = sparrow.intval( tmp[ 0 ] );
			var max = sparrow.intval( tmp[ 1 ] );
			var strlen = value.length;
			if ( strlen < min || strlen > max )
			{
				return false;
			}
		}
	}
	return true;
}

/**
 * 检查手机号码
 */
check_type.mobile = function( input, mobile )
{
	mobile = sbc_case_convert( mobile );
	input.val( mobile );
	return /^1[\d]{10}$/.test( mobile );
}

/**
 * 检查电话号码
 */
check_type.tel = function( input, tel )
{
	tel = sbc_case_convert( tel );
	input.val( tel );
	return /^((\d{7,8})|(\d{4}|\d{3})-?(\d{7,8})|(\d{4}|\d{3})-?(\d{7,8})-(\d{1,4})|(\d{7,8})-(\d{1,4}))$/.test( tel );
}

/**
 * 网址检查
 */
check_type.url = function( input, url_str )
{
	url_str = sbc_case_convert( url_str );
	input.val( url_str );
	//简单校验一下就是了
	return /^https?:\/\/.{4,}/.test( url_str );
}

check_type.checkbox = function(){return true;}
/**
 * ip检查
 */
check_type.ip = function( input, ip_str )
{
	ip_str = sbc_case_convert( ip_str );
	var tmp = ip_str.split( '.' );
	if ( 4 != tmp.length )
	{
		return false;
	}
	for ( var i = tmp.length - 1; i >= 0; --i )
	{
		var t = sparrow.intval( tmp );
		if ( t < 1 || t > 255 )
		{
			return false;
		}
		tmp[ i ] = t;
	}
	input.val( tmp.join( '.' ) );
}

/**
 * 在指定的input上显示错误消息
 */
sparrow_form.error = function( name, msg )
{
	var input = input_obj_find( name );
	if ( !input )
	{
		return;
	}
	input.data( 'check_result', false );
	form_tip_msg( name, msg, error_css );
}

/**
 * 获取一个name 或者 id 的表单
 */
sparrow_form.item = function( name )
{
	return input_obj_find( name );
}

/**
 * 获取指定元素的值
 */
sparrow_form.value = function( name )
{
	var inp = input_obj_find( name );
	if ( !inp )
	{
		return '';
	}
	if ( 'INPUT' !== inp[ 0 ].nodeName )
	{
		var tmp = inp[ 0 ].getAttribute( 'data-value' );
		if ( undefined === tmp )
		{
			return '';
		}
		return tmp;
	}
	var type = inp[ 0 ].type;
	switch( type )
	{
		case 'select':
			var tmp_val = [];
			inp.each( function(i, item){
				if ( item.checked ) {
					tmp_val.push( item.value );
				}
			} );
			return tmp_val.join( ',' );
		break;
		case 'radio':
			var tmp_val;
			inp.each( function(i, item){
				if ( item.checked ) {
					tmp_val = item.value;
				}
			} );
			return tmp_val;
		break;
		default:
			return inp.val().trim();
		break;
	}
}

/**
 * 在指定的input上显示提示消息
 */
sparrow_form.info = function( name, msg )
{
	form_tip_msg( name, msg, info_css );
}

/**
 * 手动执行一个表单的验证
 */
sparrow_form.check = function( name ){
	var inp = input_obj_find( name );
	if ( !inp )
	{
		return;
	}
	inp.trigger( 'focus' ).trigger( 'blur' );
}

/**
 * 初始化radio
 */
sparrow_form.init_radio = function( name, value ){
	$( ':radio[name="'+ name +'"]' ).each( function( i, item ){
		if ( item.value == value )
		{
			$( item ).trigger( 'focus' ).trigger( 'click' ).trigger( 'blur' );
		}
	} );
}

/**
 * 初始化checkbox
 */
sparrow_form.init_checkbox = function( name, values ){
	$( ':checkbox[name="'+ name +'"]' ).each( function( i, item ){
		if ( -1 !== sparrow.in_array( item.value, values) )
		{
			$( item ).trigger( 'focus' ).trigger( 'click' ).trigger( 'blur' );
		}
	} );
}

/**
 * 初始化 select
 */
sparrow_form.init_select = function( name, value ){
	var select = $('select[name="' + name + '"]');
	select.children().each( function( i, item ){
		if ( item.value == value )
		{
			$( item ).attr("selected",true);
		}
	} );
}

/**
 * 在表单指示区显示内容
 */
function form_tip_msg( name, msg, css )
{
	var input = input_obj_find( name );
	if ( false === input )
	{
		return false;
	}
	var tmp = input_get_tip_box( input[ 0 ] );
	var tip_box = tmp.box;
	if ( !tip_box )
	{
		return true;
	}
	var tip_box_type = tmp.type;
	show_tip_msg( tip_box, tip_box_type, msg, css );
	add_input_class( input, css );
}

/**
 * 直接验证所有表单, 并且收集表单数据
 * @param {type} form_id
 * @returns {undefined}
 */
sparrow_form.encode = function( form_id, mod, result_arg ){
	var form_dom = $( '#' + form_id );
	//lzz modify
	if( form_dom.find(".sparrow_input_tip_error:visible").length > 0 || form_dom.find(".sparrow_input_tip_info:visible").length > 0 ){
		return;
	}
	var tmp_list = [], is_valid = true, check_box = [];
	//mod:1 值不为空 mod:2 值发生改变的 3: 值发生改变 或者 不为空
	if ( !mod )
	{
		mod = 0;
	}
	//lzz modify 4
	var form_obj = "input:visible,textarea:visible,select:visible,checkbox:visible,radio:visible";
	if( mod == 4 ){
		form_obj = "input,textarea,select,checkbox,radio";
	}
	var black_list = { button: true, submit: true, reset: true, search: true};
	form_dom.find( form_obj ).each( function( i, item ){
		if ( 'TEXTAREA' === item.nodeName && 'ke-edit-textarea' === item.className )
		{
			return;
		}
		if ( sparrow.isset( black_list[ item.type ] ) )
		{
			return;
		}
		//特殊处理的两项
		if ( 'checkbox' === item.type )
		{
			check_box.push( item );
		}
		if ( 'radio' === item.type )
		{
			if ( item.checked )
			{
				tmp_list.push( item );
			}
			return;
		}
		var inp_item = $( item );
		if ( !inp_item.data( 'first-focus' ) )
		{
			if ( 'checkbox' === item.type )
			{
				if ( inp_item.data( 'require' ) )
				{
					inp_item.trigger( 'focus' );
				}
			}
			else
			{
				input_first_forcus( inp_item );
			}
		}
		inp_item.data( 'encode', 1 );
		var re = input_box_blur.apply( item );
		if ( !re )
		{
			is_valid = false;
		}
		else
		{
			if ( inp_item.data( 'server-check' ) && 0 === inp_item.data( 'check_re' ) )
			{
				is_valid = false;
			}
			else if ( 'checkbox' !== item.type )
			{
				tmp_list.push( item );
			}
		}
		inp_item.data( 'encode', 0 );
	} );
	var result = {};
	//如果有checkbox 要特殊处理
	if ( check_box.length > 0 )
	{
		var tmp_check_box_re = {};
		for ( var i = check_box.length - 1; i >= 0; --i )
		{
			var tmp_box = check_box[ i ];
			var name = input_name_detect( tmp_box );
			if ( false === name )
			{
				dev_tip_msg( $( tmp_box ), '缺少name 或者  id 属性' );
				continue;
			}
			if ( tmp_box.checked )
			{
				if ( !sparrow.isset( tmp_check_box_re[ name ] ) )
				{
					tmp_check_box_re[ name ] = [];
				}
				tmp_check_box_re[ name ].push( tmp_box.value );
			}
		}
		if ( !is_valid )
		{
			return result;
		}
		for ( var p in tmp_check_box_re )
		{
			var n = p.replace( /[\s\[\]]/g, '' ).trim();
			result[ n ] = tmp_check_box_re[ p ].join( ',' );
		}
	}
	if ( !is_valid )
	{
		return result;
	}
	for ( var i = tmp_list.length - 1; i >= 0; --i )
	{
		var tmp_inp = $( tmp_list[ i ] ), value = tmp_inp.val(), name = input_name_detect( tmp_list[ i ] );
		if ( false === name )
		{
			continue;
		}
		if ( 0 === mod )
		{
			result[ name ] = value;
		}
		else if( 4 === mod )
		{
			result[ name ] = value;
		}
		else
		{
			//值发生改变的
			if ( ( mod & 2 ) && tmp_inp.data( 'is_change' ) )
			{
				result[ name ] = value;
			}
			//值不为空的
			if ( ( mod & 1 ) && !sparrow.empty( value ) )
			{
				result[ name ] = value;
			}
		}
	}
	if ( 'object' === typeof result_arg )
	{
		result_arg.result = 1;
	}
	return result;
};

/**
 * 检查名称
 */
function input_name_detect( input )
{
	if ( input.name )
	{
		return input.name;
	}
	if ( input.id )
	{
		return input.id;
	}
	if ( 'TEXTAREA' !== input.nodeName && 'none' !== input.style.display )
	{
		dev_tip_msg( $( input ), 'name 和 id 属性至少需要一个' );
	}
	return false;
}

/**
 * 获取一个input
 */
function input_obj_find( name )
{
	var inp = $( 'input[name='+ name +']' );
	if ( inp.length > 0 )
	{
		return inp;
	}
	inp = $( '#' + name );
	if ( inp.length > 0 )
	{
		return inp;
	}
	inp = $( 'select[name='+ name +']' );
	if ( inp.length > 0 )
	{
		return inp;
	}
	inp = $( 'textarea[name='+ name +']' );
	if ( inp.length > 0 )
	{
		return inp;
	}
	return false;
}

/**
 * 开发使用函数
 */
function dev_tip_msg( input, msg )
{
	if ( !sparrow.is_dev() )
	{
		return;
	}
	sparrow_win.tiny_error( input, msg );
}

/**
 * 获取默认提示
 */
function tip_box_default_text( tip_box )
{
	return tip_box.data( 'default-text' ) || '';
}

/**
 * textarea input显示输入的字数 text_name_input_num
 */
sparrow_form.init_count_input_num = function (target) {
	$( 'textarea[name="' + target + '"]' ).bind('input selectionchange propertychange',function () {
		var input = $( this );
		var name = input.attr( 'name' );
		var text = input.val();
		var counter = text.length;
		var input_num_tip = $( "#" + name + "_input_num" ).text(counter);
		// 如果长度超过给定的大小 data-len 那么提示错误
		var len = input.data('len');
		var data_range = len.split("-");
		var max_len = 0;
		if( data_range.length == 1 )
		{
			max_len = data_range[0];
		}
		else if( data_range.length == 2 )
		{
			max_len = data_range[1];
		}

		if( max_len && counter > max_len ){
			sparrow_form.error( name, input.data("len-msg") );
		}else{
			sparrow_form.info(name, input.data("len-msg"));
			//tip_box_default_text($(this));
		}
	});
}

/**
 * 有多个 data-grpup
 */
sparrow_form.init_group_input = function ( target ) {
	var input_str = target;
	var input_list = input_str.split(",");
	var target_tip_id = input_list.join("_") + "_tip";
	var target_tip = $("#" + target_tip_id );
	var target_count = parseInt( $("#" + target_tip_id ).data("require-count") );
	var msg = target_tip.text();
	$("[data-group='" + target + "']").on('blur',function () {
		var is_error = false;
		var input_count = 0;

		for(var i = 0; i < input_list.length; i++ )
		{
			if( $( "#" + input_list[i] + "_tip" ).hasClass("sparrow_input_tip_error"))
			{
				is_error = true;
				break;
			}
			if( $('[name="' + input_list[i] + '"]').val() )
			{
				input_count++;
			}

		}
		if( !is_error && (input_count >= target_count) )
		{
			show_tip_msg( target_tip, 0, msg, ok_css );
		}
		else
		{
			show_tip_msg( target_tip, 0, msg, error_css );
		}
	});

	$("[data-group='" + target + "']").on('focus',function () {
		show_tip_msg( target_tip, 0, msg, info_css );
	});
}

$( document ).on( 'click', '.sparrow_input_tip_close', function(){
	$( this ).parent().addClass( 'hide' );
} );

$( document ).ready(function () {
	// checkbox 默认选择
	$("input[type='checkbox'][data-require='1']").each( function( i, item ){
		var name = item.name;
		var default_checked = String( $(item).data("select-default") );
		var checked_values = default_checked.split(",");
		sparrow_form.init_checkbox( name, checked_values );
	} );
	// select  默认选择
	$("select").each(function ( i, item ) {
		var name = item.name;
		var default_value = $(item).data("select-default");
		sparrow_form.init_select( name, default_value);
	})
	// radio 默认选择

	$("input[type='radio']").each(function (i, item) {
		var name = item.name;
		var default_value = $(item).data("select-default");
		sparrow_form.init_radio( name, default_value );
	})

});