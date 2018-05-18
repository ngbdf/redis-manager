window.HOST_URL = "http://" + window.location.host + "/";
window.STATIC_URL = window.HOST_URL;
window.JSTPL_URL = window.STATIC_URL + "jstpl/";

var $ = jQuery.noConflict();
empty = sparrow.empty;
var current_tpl_name = null;

//模板编译之后的
window.JS_TPL = { };
//保留变量
window.smarty_keep_var = {
	href : 'href="javascript:void(0)"'
};
if ( sparrow.isset( window.STATIC_URL ) )
{
	smarty_keep_var[ 'STATIC_URL' ] = smarty_keep_var[ 'STATIC' ] = smarty_keep_var[ 'static' ] = window.STATIC_URL;
}
if ( sparrow.isset( window.OUT_DATA ) )
{
	smarty_keep_var[ 'out_data' ] = window.OUT_DATA;
}
//特殊变量
var special_var = {
	'$smarty' : 'smarty_keep_var'
};
//前缀, 后缀
var prefix = '{{', suffix = '}}';
//编辑结果变量
var result_var = '_STR_';
//保留关键字
var private_var = null;
//解析后的函数参数名称
var param_name = 'DATA';
//for循环的开始变量
var for_char_code = 65;
//模板内容
var tpl_code = '';
//临时变量
var tmp_end_pos;
//被包含的tpl
var include_tpl_arr = [];
//全局的编译模板的方式
var global_parse_eval;
window.smarty = {
	/**
	 * 生成获取模板文件的URL
	 * @param {string} tpl_name 模板名称
	 **/
	make_tpl_url : function( tpl_name )
	{
		//var url = window.SCRIPT_FILE.replace( 'index.php', 'static/jstpl/' );
		/* lzz */
		var url = window.JSTPL_URL;
		url += tpl_name + '.html?r=' + Math.random();
		return url;
	},
	/**
	 * 当前正在使用的tpl
	 */
	current_tpl : function()
	{
		return current_tpl_name;
	},
	/**
	 * 是否有某个模板
	 **/
	has_tpl : function( tpl_name )
	{
		return 'function' === typeof window.JS_TPL[ 'func_' + smarty_tpl_name( tpl_name ) ];
	},
	/**
	 * 手动编译模板
	 * @returns {undefined}
	 */
	parse_tpl : function( tpl, tpl_name )
	{
		tpl_name = tpl_name.replace( '.html', '' );
		tpl_name = smarty_tpl_name( tpl_name );
		return smarty_parse_tpl( tpl, tpl_name, false );
	},
	/**
	 * 加载一个html
	 * @returns {undefined}
	 */
	get_tpl : function( tpl_name, callback )
	{
		smarty_load_tpl( tpl_name, callback );
	},
	/**
	 * 解析一个模板, 返回html
	 */
	fetch : function( tpl_name, data )
	{
		return smarty_to_html( tpl_name, data );
	},
	/**
	 * 解析模板, 然后执行回调
	 */
	parse: function( tpl, data, callback ){
		smarty_load_tpl( tpl, function() {
			var content = smarty_to_html( tpl, data );
			callback( content );
		} );
	},
	/**
	 * 注册自定义smarty函数
	 */
	register_function : function( plugin_name, func_name )
	{
		if ( sparrow.isset( smartyPlugin.pluginFunc[ plugin_name ] ) )
		{
			return;
		}
		if ( 'function' !== typeof func_name && global_parse_eval )
		{
			throw 'smarty plugin ' + plugin_name + ' is not a function';
		}
		smartyPlugin.pluginFunc[ plugin_name ] = func_name;
	},
	/**
	 * 注册自定义修正器
	 */
	register_modifier : function( modifier_name, func_name )
	{
		if ( sparrow.isset( smartyGrep[ modifier_name ] ) )
		{
			return;
		}
		if ( 'function' !== typeof func_name )
		{
			sparrow.debug( 'register_modifier_error: ' + func_name + ' is not a function' );
			return;
		}
		smartyGrep[ modifier_name ] = func_name;
	},

	/**
	 * 加载一个模板, 并执行回调
	 */
	html : function( tpl, data, container, callback )
	{
		smarty_load_tpl( tpl, function() {
			var content = smarty_to_html( tpl, data );
			if ( container )
			{
				var div = sparrow.jq_dom( container );
				div.find( '*' ).remove();
				div.html( content );
			}
			if ( 'function' === typeof callback )
			{
				callback( content );
			}
		} );
	},
	/**
	 * 从服务器加载数据(json)组装成模板
	 */
	get : function( url, tpl, container, callback, is_loading )
	{
		var arg = { tpl : tpl, container : container, callback : callback };
		ajax.get( url, smarty_ajax_re, is_loading, arg );
	},
	/**
	 * 提交数据, 再将服务器返回的数据(json)组装成模板
	 */
	post : function( url, data, tpl, container, callback, is_loading )
	{
		var arg = { tpl : tpl, container : container, callback : callback };
		ajax.post( url, data, smarty_ajax_re, is_loading, arg );
	},
	/**
	 * 打开一个窗口
	 */
	open : function( tpl, data, window_option, callback )
	{
		smarty_load_tpl( tpl, function() {
			window_option = window_option || { };
			var content = smarty_to_html( tpl, data );
			sparrow_win.open( content, window_option );
			if ( 'function' === typeof callback )
			{
				callback( data );
			}
		} );
	},
	/**
	 * 服务器获取数据, 再组装成html, 打开一个窗口
	 */
	fopen : function( url, tpl, is_loading, window_option, callback )
	{
		window_option = window_option || { };
		var arg = { tpl : tpl, option : window_option, callback : callback };
		ajax.get( url, smarty_open_re, is_loading, arg );
	},
	/**
	 * 提交数据, 返回结果组装成html, 打开一个窗口
	 */
	popen : function( url, data, tpl, is_loading, window_option, callback )
	{
		window_option = window_option || { };
		var arg = { tpl : tpl, option : window_option, callback : callback };
		ajax.post( url, data, smarty_open_re, is_loading, arg );
	}
};
/**
 * 解析模板
 **/
function smarty_parse_tpl( tpl, tpl_name, is_eval )
{
	if ( false !== is_eval )
	{
		is_eval = true;
	}
	global_parse_eval = is_eval;
	tpl_name = 'func_' + tpl_name;
	tpl_code = smarty_clean_tpl( tpl );
	smartySyntax.syntaxStack = [ ];					//初始化
	//for 循环中的变量ascII码值
	for_char_code = 65;
	//重置smarty的保留变量
	//smarty_reset_keep();
	private_var = { };				//初始化该保留关键字
	var tpl_result = smarty_do_parse( tpl_name );
	if ( is_eval )
	{
		eval( tpl_result );
	}
	return tpl_result;
}
/**
 * ajax返回结果
 */
function smarty_ajax_re( data, arg )
{
	smarty.html( arg.tpl, data, arg.container, arg.callback );
}

/**
 * fopen, popen结果
 */
function smarty_open_re( data, arg )
{
	smarty.open( arg.tpl, data, arg.option, arg.callback );
}

/**
 * 将模板转成html
 **/
function smarty_to_html( tpl_name, data )
{
	current_tpl_name = tpl_name;
	tpl_name = smarty_tpl_name( tpl_name );
	//模板不存在, 先去加载模板
	if ( !smarty.has_tpl( tpl_name ) )
	{
		sparrow.error( 'template:' + tpl_name + ' no exists' );
		return 'tpl not exist';
	}
	try
	{
		var tmp_html = JS_TPL[ 'func_' + tpl_name ]( data );
	}
	catch ( excp )
	{
		var msg = 'jssmarty ' + tpl_name + ' template runtime error: ' + excp;
		//IE下
		if ( excp.description )
		{
			msg += excp.description;
		}
		sparrow.error( msg );
		return msg;
	}
	return tmp_html;
}

/**
 * 加载一个模板, 然后执行回调
 */
function smarty_load_tpl( tpl_name, callback )
{
	function _on_tpl_load()
	{
		if ( 0 === include_tpl_arr.length )
		{
			callback();
		}
		else
		{
			smarty_load_tpl( include_tpl_arr.shift(), callback );
		}
	}
	var real_name = smarty_tpl_name( tpl_name );
	if ( smarty.has_tpl( real_name ) )
	{
		_on_tpl_load();
	}
	else
	{
		if ( window.FORCE_LOAD_TPL )
		{
			ajax.get_text( smarty.make_tpl_url( tpl_name ), function( tpl_str ) {
				smarty_parse_tpl( tpl_str, real_name );
				_on_tpl_load();
			} );
		}
		//正式的环境, 直接使用编译好的tpl
		else
		{
			ajax.get_text( smarty.make_tpl_url( tpl_name ), function( tpl_str ) {
				smarty_parse_tpl( tpl_str, real_name );
				_on_tpl_load();
			} );
		}
	}
}

/**
 * 模板名称
 */
function smarty_tpl_name( tpl_name )
{
	return tpl_name.replace( /\//g, '_' );
}

/**
 * 清理模板的注释等内容
 */
function smarty_clean_tpl( tpl )
{
	//去掉行首尾的\t
	tpl = tpl.replace( /\r?\n[\t]+/g, '' );
	//去掉行首尾的空格
	tpl = tpl.replace( /\r?\n[\s]*/g, '' );
	//有注释内容
	if ( -1 != tpl.indexOf( '<!--' ) )
	{
		tpl = smarty_remove_comment( tpl );
	}
	return tpl;
}
/**
 * 清理注释
 */
function smarty_remove_comment( tpl )
{
	var beg_pos = tpl.indexOf( '<!--' );
	if ( -1 == beg_pos )
	{
		return tpl;
	}
	//-3是因为-->的长度为3
	var re_tpl = '', end_pos = -3;
	while ( -1 != beg_pos )
	{
		re_tpl += tpl.substring( end_pos + 3, beg_pos );
		end_pos = tpl.indexOf( '-->', beg_pos );
		if ( -1 == end_pos )
		{
			//注释没有闭合
			return re_tpl;
		}
		beg_pos = tpl.indexOf( '<!--', end_pos + 3 );
	}
	return re_tpl + tpl.substring( end_pos + 3 );
}

/**
 * 清理字符串
 */
function smarty_clean_str( str )
{
	return str.replace( /[\\']/g, '\\$&' );
}
/**
 * 异常处理
 */
function smarty_exception( err )
{
	var bPos = tpl_code.indexOf( '\\n' );
	var line = 1;
	while ( -1 != bPos )
	{
		line++;
		bPos = tpl_code.indexOf( '\\n', bPos + 1 );
		if ( -1 != tmp_end_pos )
		{
			if ( bPos > tmp_end_pos )
			{
				break;
			}
		}
	}
	throw err + 'error location: number' + line + 'row';
}


/**
 * 模板解析
 */
function smarty_do_parse( func_name )
{
	var re_func = [ 'JS_TPL.' + func_name + '=function(' + param_name + '){var ' + result_var + ' = [];' ];
	var prefix_len = prefix.length;
	var suffix_len = suffix.length;
	var normal_str = '', smarty_str = '';
	tmp_end_pos = -suffix_len;
	var beg_pos = tpl_code.indexOf( prefix );
	while ( -1 != beg_pos )
	{
		normal_str = tpl_code.substring( tmp_end_pos + suffix_len, beg_pos );
		if ( '' != normal_str )
		{
			re_func.push( result_var + ".push('" + smarty_clean_str( normal_str ) + "');" );
		}
		tmp_end_pos = tpl_code.indexOf( suffix, beg_pos );
		if ( tmp_end_pos < 0 )
		{
			smarty_exception( 'smarty syntax mismatch: ' + tpl_code.substring( beg_pos ) );
		}
		re_func.push( smartySyntax.doParse( tpl_code.substring( beg_pos + prefix_len, tmp_end_pos ) ) );
		beg_pos = tpl_code.indexOf( prefix, tmp_end_pos );
	}
	if ( 0 != smartySyntax.syntaxStack.length )
	{
		smarty_exception( 'syntax mismatch' );
	}
	re_func.push( result_var + ".push('" + smarty_clean_str( tpl_code.substring( tmp_end_pos + suffix_len ) ) +
			"');return " + result_var + ".join('');}" );
	return re_func.join( '' );
}

/**
 * 格式化括号
 */
function smarty_format_brackets( str )
{
	//在括号两边加上空格；
	str = str.replace( /(\(|\))/g, ' $1 ' );
	//把连续的空格替换为只有一个空格
	str = str.replace( /\s+/g, ' ' );
	return str.trim();
}

/**
 * 处理一条语句中所有的变量
 */
function smarty_parse_line_vars( str )
{
	//str = str.replace( /(\[^!]=+)/g, ' $1 ' );
	var str_arr = str.split( ' ' );
	for ( var i = str_arr.length - 1;i >= 0;i-- )
	{
		if ( '$' != str_arr[ i ].substring( 0, 1 ) )
		{
			continue;
		}
		str_arr[ i ] = smartySyntax.parseVar( str_arr[ i ] );
	}
	return str_arr.join( ' ' );
}

/**
 * 插入局部变量 pushPrivateVar
 */
function smarty_push_prviate( var_str )
{
	//将循环体内的变量插入保护变量
	var var_name;
	if ( -1 != var_str.indexOf( '.' ) )
	{
		var_name = var_str.substring( 0, var_str.indexOf( '.' ) );
	}
	else
	{
		var_name = var_str;
	}
	return param_name + '["' + var_name + '"]=' + var_name + ';';
}

var smartyReg = {
	db_qstr : '"[^"\\\\]*(?:\\\\.[^"\\\\]*)*"',
	si_qstr : '\'[^\'\\\\]*(?:\\\\.[^\'\\\\]*)*\''
};
smartyReg.qstr = '(?:' + smartyReg[ 'db_qstr' ] + '|' + smartyReg[ 'si_qstr' ] + ')';
//自定义smarty解析函数
var smartyPlugin = {
	pluginFunc : { },
	check_plugin : function( str )
	{
		var result = { };
		str = str.replace( /\s*=\s*/g, '=' );
		//@todo 此处还有bug 如果传过来的字符串中间有空格, 会解析出错, 应该做严格 字符串解析
		var tmp = sparrow.array_unset( str.split( ' ' ), '' );
		var tmp_name = tmp.shift();
		result.func = tmp_name;
		result.arg = tmp;
		result.re = sparrow.isset( this.pluginFunc[ tmp_name ] );
		return result;
	}
};
window.smartyPlugin = smartyPlugin;
//smarty语法分析器
var smartySyntax = {
	syntaxStack : [ ],
	doParse : function( str )
	{
		this.str = str;
		str = str.trim();
		str = str.replace( /\t/g, ' ' ); //替换表达式中的 \t 为空格
		//debug('parse:'+str);
		if ( /^\$/.test( str ) )
		{
			tmp_str = str;
			if ( -1 != str.indexOf( '|' ) )
			{
				tmp_str = str.replace( /["'][^"']+["']/g, 1 );
			}
			if ( /^\$[^+\-*/]+$/.test( tmp_str ) )
			{
				//变量分析
				return result_var + '.push(' + this.parseVar( str ) + ');';
			}
		}
		else if ( /^(if|section|foreach|assign|cycle|include|elseif)\s+(.*)$/.test( str ) )
		{
			var func_name = this['parse' + RegExp.$1];
			//将连续的空格替换为只有一个空格//语句分析
			return func_name( RegExp.$2.replace( /\s+/g, ' ' ) );
		}
		else if ( 'foreachelse' == str || 'sectionelse' == str )
		{
			this.syntaxStack.push( 'elseflag' ); //有这个标志，就可以确定foreach和section里有foreachelse和sectionelse
			return '}}else{';
		}
		else if ( 'else' == str )
		{
			return '}else{';
		}
		else if ( /\/([a-zA-Z]+)/.test( str ) )
		{
			return this.closeBlock( RegExp.$1 ); //结束语句块
		}
		else if ( 'literal' == str )
		{
			return result_var + ".push('" + this.parseLiteral() + "');";
		}
		else if ( '*' == str.substring( 0, 1 ) )
		{
			return this.parseComment( str ); //smarty 注释
		}
		if ( /[+\-*/]+/.test( str ) )
		{
			//如果有+-*/,先把所有不是+-*/='" 号的去掉
			var tmp_str = str.replace( /[^+\-*/='"]/g, '' );
			//sparrow.debug( str );
			if ( /^[+\-*/]+$/.test( tmp_str ) )
			{
				return this.parseFormula( str );
			}
			//return result_var + '.push(' + this.parseFormula(str) +');';
		}
		var plugin_re = smartyPlugin.check_plugin( str );
		if ( !plugin_re[ 're' ] && global_parse_eval )
		{
			smarty_exception( 'error syntax' + str );
		}
		else
		{
			var tmp_str = [ ];
			var tmp, tmp_value, var_name;
			var_name = '_plugin_arg_' + plugin_re[ 'func' ] + '_';
			tmp_str.push( 'var ' + var_name + ' = {};' );
			var args = plugin_re[ 'arg' ], tmp_name;
			for ( var i = 0;i < args.length;++i )
			{
				tmp = args[ i ].split( '=' );
				tmp_name = tmp.shift();
				tmp_value = tmp.join( '=' );
				if ( '$' == tmp_value.substring( 0, 1 ) )
				{
					tmp_value = smartySyntax.parseVar( tmp_value );
				}
				tmp_str.push( var_name + '["' + tmp_name + '"]=' + tmp_value + ';' );
			}
			tmp_str.push( result_var + '.push(smartyPlugin.pluginFunc["' + plugin_re[ 'func' ] + '"](' + var_name + '));' );
			return tmp_str.join( '' );
		}
	},
	parseif : function( str )
	{
		smartySyntax.syntaxStack.push( 'if' );
		return smartyIf.init( str );
	},
	parseforeach : function( str )
	{
		return smartyForeach.init( str );
	},
	parseFormula : function( str )
	{
		return smartyFormula.init( str );
	},
	parsesection : function( str )
	{
		return smartySection.init( str );
	},
	parseassign : function( str )
	{
		var tmp_value;
		if ( !/^name\s*=\s*"([^\"]+)"\svalue\s*=\s*([^\s]+)\s*$/.test( str ) )
		{
			smarty_exception( 'error assign expression:' + str );
		}
		tmp_value = RegExp.$2;
		if ( '$' == tmp_value.substring( 0, 1 ) )
		{
			tmp_value = smartySyntax.parseVar( tmp_value );
		}
		return param_name + '["' + RegExp.$1 + '"]=' + tmp_value + ';';
	},
	parsecycle : function( )
	{
		//todo
		return '';
	},
	parseinclude : function( str )
	{
		if ( !/^file\s*=\s*"([^\.]+)\.html"$/.test( str ) )
		{
			smarty_exception( 'error include expresion:' + str );
		}
		var tmp = RegExp.$1;
		if ( global_parse_eval )
		{
			include_tpl_arr.push( tmp );
		}
		var func_name = 'JS_TPL.func_' + smarty_tpl_name( tmp );
		var re_str = 'if("function" !== typeof ' + func_name + ') throw "no include template :' + tmp + '";';
		return re_str + result_var + '.push(JS_TPL.func_' + smarty_tpl_name( tmp ) + '(' + param_name + '));';
	},
	parseelseif : function( str )
	{
		return '}else ' + smartyIf.init( str );
	},
	parseComment : function( str )
	{
		if ( '*' == str.substring( str.length - 1 ) )
		{
			return ''; //正规的smarty注释
		}
		var hasFound = false;
		var begPos = tpl_code.indexOf( '*', tmp_end_pos ); //找*的位置
		var tmpStr = '';
		while ( -1 != begPos && false == hasFound )
		{
			tmpStr = tpl_code.substring( begPos + 1 ).replace( /^\s*/g, '' ); //找到*号以后的内容
			if ( 0 == tmpStr.indexOf( suffix ) )
			{
				hasFound = true;
			}
			else
			{
				begPos = tpl_code.indexOf( '*', begPos + 1 ); //1为*号的长度
			}
			break;
		}
		if ( true == hasFound )
		{
			tmp_end_pos = tpl_code.indexOf( suffix, begPos );
		}
		else
		{
			throw 'without {* pair match';
		}
		return '';
	},
	//匹配Literal块
	parseLiteral : function( )
	{
		var endLitPos = tpl_code.indexOf( '/literal', tmp_end_pos );
		var hasFound = false;
		var tmpBef, tmpAft;
		while ( -1 != endLitPos && false == hasFound )
		{
			tmpBef = tpl_code.substring( tmp_end_pos + suffix.length, endLitPos ).trim();
			tmpAft = tpl_code.substring( endLitPos + 8 ).trim(); //8为 'literal'的长度
			if ( prefix == tmpBef.substring( tmpBef.length - prefix.length ) && suffix == tmpAft.substring( 0, suffix.length ) )
			{
				//找到的literal前为befBrks后为endBrks
				hasFound = true;
			}
			else
			{
				endLitPos = tpl_code.indexOf( '/literal', endLitPos + 8 );
			}
		}
		if ( false == hasFound )
		{
			throw 'without literal pair match';
		}
		else
		{
			var reStr = smarty_clean_str( tmpBef.substring( 0, tmpBef.length - prefix.length ) );
			tmp_end_pos = tpl_code.indexOf( suffix, endLitPos ); //跳过整个literal包含内容
			return reStr;
		}
	},
	parseVar : function( str )
	{
		//匹配单一变量
		//debug('smartyVar：'+ str);
		var re_str = '';
		var tmp_qua = [ ];
		while ( new RegExp( '(' + smartyReg.qstr + ')' ).test( str ) )
		{
			str = str.replace( RegExp.$1, 'SMARTY_QUATO_' + tmp_qua.length );
			tmp_qua.push( RegExp.$1 );
		}
		if ( -1 == str.indexOf( '|' ) )
		{
			re_str = smartyVar.init( str );
		}
		else
		{
			var greps = str.split( '|' );
			var tmpGrep, tmpArr, parm;
			re_str = smartyVar.init( greps[ 0 ] );
			var m;

			for ( var i = 1, l = greps.length;i < l;++i )
			{
				parm = [ ];
				if ( '' == greps[ i ] )
				{
					throw 'error pip expression: ' + str;
				}
				if ( -1 != greps[ i ].indexOf( ':' ) )
				{
					//有：号，第一个：号以后作为参数parm传递
					tmpArr = greps[i].split( ':' );
					tmpGrep = tmpArr[ 0 ];

					for ( m = 1;m < tmpArr.length;++m )
					{
						if ( '$' == tmpArr[ m ].substring( 0, 1 ) )
						{
							parm.push( smartySyntax.parseVar( tmpArr[ m ] ) );
						}
						else
						{
							parm.push( tmpArr[ m ] );
						}
					}
					re_str = 'smartyGrep["' + tmpGrep + '"](' + re_str + ',' + parm.join( ',' ) + ')';
				}
				else
				{
					//无：号
					tmpGrep = greps[ i ];
					re_str = 'smartyGrep["' + tmpGrep + '"](' + re_str + ')';
				}
				if ( 'function' !== typeof( smartyGrep[ tmpGrep ] ) && global_parse_eval )
				{
					throw 'Smarty no support pip function "' + tmpGrep + '" in "' + str + '.';
				}
			}
		}
		if ( 0 != tmp_qua.length )
		{
			for ( i = 0;i < tmp_qua.length;++i )
			{
				re_str = re_str.replace( 'SMARTY_QUATO_' + i, tmp_qua[ i ] );
			}
		}
		return re_str;
	},
	closeBlock : function( block )
	{
		//结束标志
		var tmp = this.syntaxStack.pop( );
		var isBrks = false;
		var reStr = '}';
		if ( 'elseflag' == tmp )
		{
			tmp = this.syntaxStack.pop();
			isBrks = true;
		}
		if ( block != tmp )
		{
			this.exception( block + 'no match' );
		}

		if ( 'foreach' == block || 'section' == block )
		{
			if ( false == isBrks )
			{
				reStr += '}';
			}
		}
		return reStr;
	},
	exception : function( err )
	{
		smarty_exception( 'syntax: ' + this.str + 'parsing error\n\n mabay reson is: ' + err );
	}
};

var smartyForeach = {
	init : function( str )
	{
		this.forStr = str;
		//this.hasName = false; //是否有name=xx这样的属性
		this.forStr = this.forStr.replace( /(from|name|key|item)\s?=\s?/g, '$1=' ); //将XX = XX换成 XX=XX
		if ( /(^|\s)(?!name=|from=|key=|item=)/.test( this.forStr ) )
		{
			smartySyntax.exception( 'error foreach expression' )
		}
		/*if ( /(\s|^)?name=([^\s]+)(\s|$)/.test( this.forStr ) )
		 {
		 this.hasName = RegExp.$2; //记录这个name值
		 this.forStr = this.forStr.replace( /(\s|^)?name=[^\s]+(\s|$)/, ' ' ).trim( );//取消掉name
		 if ( false == sparrow.isset( smarty_keep_var[ 'foreach' ] ) )
		 {
		 smarty_keep_var[ 'foreach' ] = {};
		 }
		 }*/
		smartySyntax.syntaxStack.push( 'foreach' );
		return this.doParse();
	},
	doParse : function()
	{
		var forArr = this.forStr.split( ' ' ); //按空格切割
		var arg = { };
		var tmp;
		for ( var i = forArr.length - 1;i >= 0;--i )
		{
			tmp = forArr[ i ].split( '=' );
			arg[ tmp[ 0 ].toLowerCase() ] = tmp[ 1 ];
		}
		tmp = this.parseFromVar( arg[ 'from' ] );
		var forVar;
		if ( false == sparrow.isset( arg[ 'key' ] ) )
		{
			forVar = '_' + String.fromCharCode( for_char_code++ );
		}
		else
		{
			forVar = arg[ 'key' ];
		}
		var itemVar = arg[ 'item' ];
		var reStr;
		var fromVar = tmp[ 0 ];
		reStr = tmp[ 1 ] + ';';
		reStr += 'if(false==sparrow.empty(' + fromVar + ')){var ' + itemVar + ';for(var ' + forVar + ' in ' + fromVar + '){' + itemVar + '=' + fromVar + '[' + forVar + '];';
		reStr += smarty_push_prviate( itemVar );
		reStr += smarty_push_prviate( forVar );
		return reStr;
	},
	//
	parseFromVar : function( str )
	{
		var reStr = '';
		var fromVar = str.replace( '$', '' );
		var varValue = smartySyntax.parseVar( '$' + fromVar );
		if ( /\./.test( fromVar ) )
		{
			fromVar = fromVar.replace( /\./g, '__' );
		}
		if ( /^([^\[]+)\[/.test( fromVar ) )
		{
			reStr = 'var ' + RegExp.$1 + '={};';
			reStr += fromVar + ' = ' + varValue;
		}
		else
		{
			reStr = 'var ' + fromVar + ' = ' + varValue;
		}
		return [ fromVar, reStr ];
	}
};

var smartyIf = {
	init : function( ifStr )
	{
		this.ifStr = ifStr;
		this.formatIf( );
		if ( -1 != this.ifStr.indexOf( '(' ) )
		{
			this.checkIsPair(); //检查括号
		}
		if ( /\sis\s/i.test( this.ifStr ) )
		{
			this.getIsStr(); //解析 is 表达式
		}
		this.ifStr = smarty_parse_line_vars( this.ifStr );
		return 'if(' + this.ifStr + '){';
	},
	formatIf : function()
	{
		this.ifStr = this.ifStr.replace( /\seq\s/gi, ' == ' );
		this.ifStr = this.ifStr.replace( /\snq\s/gi, ' != ' );
		this.ifStr = this.ifStr.replace( /\sor\s/gi, ' || ' );
		this.ifStr = this.ifStr.replace( /\sand\s/gi, ' && ' );
		this.ifStr = this.ifStr.replace( /\sgt\s/gi, ' > ' );
		this.ifStr = this.ifStr.replace( /\slt\s/gi, ' < ' );
	},
	checkIsPair : function()
	{
		if ( /\(\s+\)/.test( this.ifStr ) )
		{
			smartySyntax.exception( 'parentheses is not  can content' );
		}
		this.ifStr = smarty_format_brackets( this.ifStr ); //处理括号,在括号前后加空格
		var arrIf = this.ifStr.split( ' ' );
		var isPair = 0;
		for ( var i = 0, l = arrIf.length;i < l;i++ )
		{
			if ( isPair < 0 )
			{
				break;
			}
			if ( ')' == arrIf[ i ] )
			{
				isPair--;
			}
			else if ( '(' == arrIf[ i ] )
			{
				isPair++;
			}
		}
		if ( 0 != isPair )
		{
			smartySyntax.exception( '{ is not match' );
		}
	},
	getIsStr : function( )
	{
		//解析smarty的 if is语句
		/*下边的过程太混乱了
		 *基础思想是把字符串1。把大写的IS替换成is；2。把大写的NOT替换成not；
		 *把字符串按空格切割成数组，循环数组，如果发现is，就把从is前边的字符串（参数）到is结束的字符串提取分析，并push到返回数组。所有不满足的字符串也push到返回数组，最后输出
		 *如果is前边为)那就把从(到)内的全部内容当成参数
		 */
		this.ifStr = this.ifStr.replace( /\sIS\s/g, 'is' );
		this.ifStr = this.ifStr.replace( /\sNOT\s/g, 'not' );
		var ifArr = this.ifStr.split( /\s/ );
		var resultArr = [ ];
		var isFlag = false;
		var begInd = 0;
		var tmpStr = tmpItem = '';
		for ( var i = 0, l = ifArr.length;i < l;i++ )
		{
			// 从1开始的话 if就不用分析了
			if ( 'is' == ifArr[ i ] )
			{
				if ( false != isFlag )
				{
					smartySyntax.exception( 'is use method error' );
				}
				begInd = i - 1; //从前一个位置开始
				if ( ')' == ifArr[ begInd ] )
				{
					//如果前面是)号，就要将整个()内的串作为参数
					tmpItem = resultArr.pop( );
					tmpStr = '';
					while ( '(' != tmpItem )
					{
						//debug('resultArr' + resultArr);
						tmpStr = tmpItem + tmpStr;
						//debug('tmpStr:'+tmpStr);
						tmpItem = resultArr.pop();
					}
					ifArr[begInd] = '(' + tmpStr;
				}
				else
				{
					resultArr.pop(); //如果不是)，那就将前面的字符串去除掉
				}
				isFlag = true;
				i++;
				while ( i < l && true == isFlag )
				{ //找从is到 is可能的结束符之间的内容
					if ( /^[)=!|&><]/.test( ifArr[ i ] ) )
					{
						isFlag = false; //找到结束位了
						resultArr.push( this.parseIs( ifArr.slice( begInd, i ) ) );
						i--;
					}
					else
					{
						i++;
					}
				}
				if ( true == isFlag )
				{
					//如果找到最后，表示从is到最后的内容都为is表达式
					resultArr.push( this.parseIs( ifArr.slice( begInd, i ) ) );
				}
			}
			else
			{
				//普通字符串压入结果数组
				resultArr.push( ifArr[i] );
			}
		}
		this.ifStr = resultArr.join( ' ' );
		this.ifStr = smarty_format_brackets( this.ifStr ); //处理括号,在括号前后加空格
	},
	parseIs : function( isArr )
	{
		//debug('var: ' + isArr[0]);
		if ( isArr.length < 3 || isArr.length > 6 )
		{
			smartySyntax.exception( 'Unrecognized expression :' + isArr.join( ' ' ) );
		}
		var isType = isArr[ 2 ];
		if ( 'not' == isType )
		{
			isType = isArr[ 3 ];
		}
		if ( !/^(odd|div|even)$/.test( isType ) )
		{
			smartySyntax.exception( 'Unrecognized expression :' + isType );
		}
		//alert(typeof this[ 'parse' + RegExp.$1 ] );
		return this[ 'parse' + RegExp.$1 ]( isArr );
	},
	parsediv : function( isArr )
	{
		var parm = isArr.shift( );
		isArr.shift( );
		var flag;
		if ( 'not' == isArr.shift( ) )
		{
			isArr.shift( );//把odd除掉
			flag = '!'
		}
		else
		{
			flag = '=';
		}
		if ( 2 != isArr.length || 'by' != isArr[ 0 ] )
		{
			smartySyntax.exception( 'can not parsing is expression' );
		}
		return '0 ' + flag + '= ' + parm + ' % ' + isArr[ 1 ];
	},
	parseOddEven : function( isArr, flag )
	{
		var parm = isArr.shift( );
		isArr.shift(); //除掉is
		if ( 'not' == isArr.shift() )
		{
			isArr.shift( );//把odd除掉
			flag = ( '=' == flag ) ? '!' : '=';
		}
		if ( 0 == isArr.length )
		{
			return '0 ' + flag + '= ' + parm + ' % 2';
		}
		else if ( 2 == isArr.length && 'by' == isArr[ 0 ] )
		{
			return '0 ' + flag + '= ' + parm + ' / ' + isArr[1] + ' % 2';
		}
		else
		{
			smartySyntax.exception( 'can not parsing is expression' );
		}
	},
	parseodd : function( isArr )
	{
		return this.parseOddEven( isArr, '!' );
	},
	parseeven : function( isArr )
	{
		return this.parseOddEven( isArr, '=' );
	}
};

var smartySection = {
	init : function( str )
	{
		//alert(str);
		this.str = str;
		this.str = this.str.replace( /(name|loop|start|step|max|show)\s?=\s?/g, '$1=' ); //将XX = XX换成 XX=XX
		if ( /(^|\s)(?!name=|loop=|start=|step=|max=|show=)/.test( this.str ) )
		{
			smartySyntax.exception( 'error section expression' )
		}
		smartySyntax.syntaxStack.push( 'section' );
		return this.doParse( );
	},
	doParse : function()
	{
		if ( !/loop\s*=\s*([^\s]+)/.test( this.str ) )
		{
			smartySyntax.exception( 'section is without loop' );
		}
		else
		{
			var loopVar = RegExp.$1;
		}
		if ( !/name\s*=\s*([^\s]+)/.test( this.str ) )
		{
			smartySyntax.exception( 'section expression is without name' );
		}
		else
		{
			var nameVar = RegExp.$1;
		}
		var maxVar = ( /max\s*=\s*(\d+)/.test( this.str ) ) ? RegExp.$1 : -1;
		var stepVar = ( /step\s*=\s*(\d+)/.test( this.str ) ) ? RegExp.$1 : 1;
		var startVar = ( /start\s*=\s*(\d+)/.test( this.str ) ) ? RegExp.$1 : 0;
		var lenVar = String.fromCharCode( for_char_code++ );
		if ( /^\$/.test( loopVar ) )
		{
			loopVar = smartySyntax.parseVar( loopVar ); //如果loop是变量,解析该变量
		}
		var reStr = [ 'var _section_loop_tmp_ = ' + loopVar + ';var ' + lenVar + ';if($.isArray(_section_loop_tmp_))' + lenVar + '=_section_loop_tmp_.length;else ' + lenVar + '=sparrow.intval(_section_loop_tmp_,true);' ];
		if ( -1 != maxVar )
		{
			reStr.push( 'if(' + lenVar + ' > ' + maxVar + ')' + lenVar + '=' + maxVar + ';' );
		}
		reStr.push( 'if(' + lenVar + '){for(var ' + nameVar + '=' + startVar + '; ' + nameVar + '<' + lenVar + '; ' + nameVar + '+=' + stepVar + '){' );
		reStr.push( param_name + '["' + nameVar + '"]=' + nameVar + ';' );
		//alert( reStr.join( '' ) );
		return reStr.join( '' );
	}
};

var smartyFormula = {
	init : function( str )
	{
		this.str = this.formatFormula( str );
		var s_len = this.str.length,
				pp_s = this.str.substring( s_len - 2, s_len ),
				pp_e = this.str.substring( 0, 2 ),
				tmp_re,
				has_self_add = 0;
		if ( '++' == pp_s || '--' == pp_s )
		{
			this.str = this.str.substring( 0, s_len - 2 );
			has_self_add = 1;
		}
		if ( '++' == pp_e || '--' == pp_e )
		{
			this.str = this.str.substring( 2, s_len );
			has_self_add = 2;
		}
		tmp_re = smarty_parse_line_vars( this.str );
		if ( has_self_add > 0 )
		{
			var parse_re;
			if ( 1 == has_self_add )
			{
				parse_re = tmp_re + pp_s;
			}
			else
			{
				parse_re = pp_e + tmp_re;
			}
			return parse_re + ';';
		}
		else
		{
			return result_var + '.push((' + tmp_re + '));';
		}

	},
	formatFormula : function( str )
	{
		return str.replace( /(\s*[+\-*/]+\s* )/, ' $1 ' );
	}
};

var smartyVar = { //smarty变量表达式分析器
	init : function( varStr )
	{
		this.varStr = varStr;
		var tmpStr = varStr;
		if ( -1 != varStr.indexOf( '.' ) )
		{
			tmpStr = varStr.substring( 0, varStr.indexOf( '.' ) );
		}
		//特殊变更
		if ( sparrow.isset( special_var[ tmpStr ] ) )
		{
			return varStr.replace( tmpStr, special_var[ tmpStr ] );
		}
		//私有变量
		if ( true == private_var[ tmpStr ] )
		{
			return varStr.replace( '$', '' );
		}
		if ( true == this.checkValid() )
		{
			smartySyntax.exception( 'expression is error' );
		}
		this.reStr = '';
		this.dotFlag = false;
		this.isPair = 0;//中括号是否配对
		this.doParse();
		if ( 0 != this.isPair )
		{
			smartySyntax.exception( '[] is not match' );
		}
		return this.reStr;
	},
	checkValid : function()
	{
		//检查变量表达式是否正确
		return true == /\$[^a-zA-Z_]/.test( this.varStr ) || //$后面只能给[a-zA-Z_]
				//true == /\.((\.|$)|((0|([1-9]\d*))[^.\[\]]))/.test(this.varStr) || //1：不能两个..或者以.结束 2：.后面接数字，数字后面必须再接'.','[',']'之一
				true == /\[(\[|\]|\.|((0|([1-9]\d*))[^\]]))/.test( this.varStr ); //以[开始，后面不能接 [, ], .，如果是数字，后面只能是]
	},
	doParse : function()
	{
		var dotBef = this.dotAft = ''; //用于记录当遇到.时[]内是否需要加"号
		var strArr = this.varStr.split( '' );
		var endVar = false, c, tmp_re;
		for ( var i = 0, l = strArr.length;i < l;i++ )
		{
			if ( this.isPair < 0 )
			{
				smartySyntax.exception( '[] is not match' );
			}
			c = strArr[ i ];
			switch ( c )
			{
				case '$':
					tmp_re = '';
					i++;
					while ( false == endVar && i < l )
					{
						//查找变量结束位置
						if ( '.' == strArr[ i ] || '[' == strArr[ i ] || ']' == strArr[ i ] )
						{
							endVar = true;
							i--; //很重要
							break;
						}
						else
						{
							tmp_re += strArr[ i ];
						}
						i++;
					}
					if ( true == private_var[ '$' + tmp_re ] )
					{
						this.reStr += tmp_re;
					}
					else
					{
						this.reStr += param_name + '["' + tmp_re + '"]';
					}
					endVar = false; //还原标志位
					break;
				case '.':
					dotBef = this.dotAft; //互换这个标志
					this.dotAft = ( '$' == strArr[ i + 1 ] ) ? '' : '"'; //检查后面出现的字符是否是变量，如果是，则[]，else [""]
					if ( false == this.dotFlag )
					{
						this.dotFlag = true;
						this.reStr += '[' + this.dotAft;
					}
					else
					{
						this.reStr += dotBef + '][' + this.dotAft;
					}
					break;
				case '[':
					this.isPair++;
					this.cleanDotFlag();
					this.reStr += '[';
					break;
				case ']':
					this.isPair--;
					this.cleanDotFlag();
					this.reStr += ']';
					break;
				default:
					this.reStr += c;
			}
		}
		this.cleanDotFlag();
	},
	cleanDotFlag : function()
	{
		if ( false == this.dotFlag )
		{
			return;
		}
		this.dotFlag = false;
		this.reStr += this.dotAft + ']';
		this.dotAft = '';
	}
};

var smartyGrep = { //Smarty管道
	upper : function( str )
	{
		return str.toUpperCase();
	},
	lower : function( str )
	{
		return str.toLowerCase();
	},
	strip : function( str )
	{
		return str.trim();
	},
	strip_tags : function( str )
	{
		return str.replace( /<\/?[^>]+>/gi, '' );
	},
	truncate : function( str, len, end_str )
	{
		if ( sparrow.isset( len ) )
		{
			len = sparrow.intval( len );
		}
		else
		{
			len = 80;
		}
		end_str = end_str || '...';
		return sparrow.substr( str, len, end_str );
	},
	intval : function( num )
	{
		return sparrow.intval( num );
	}
};
window.smartyGrep = smartyGrep;

//默认的插件
function default_url( url, page_arg )
{
	var reg = new RegExp( '&?' + page_arg + '=[0-9]+&?' );
	var re = url.replace( reg, function( tmp_str ) {
		var beg = '&' === tmp_str.substr( 0, 1 );
		var end = '&' === tmp_str.substr( -1, 1 );
		//前后都有&
		if ( beg && end )
		{
			return '&';
		}
		return '';
	} );
	//如果没有=号
	if ( -1 == re.indexOf( '=' ) )
	{
		if ( '/' !== re.substr( -1, 1 ) )
		{
			//re += '/';
			/* lzz */
			re += '';
		}
	}
	else
	{
		re += '&';
	}
	return re;
}

/**
 * 截取字符串修正器
 */
smarty.register_modifier( 'truncate', function( str, len, joinstr ){
	len = len || 20;
	joinstr = joinstr || '...';
	return sparrow.substr( str, len, joinstr );
} );

/**
 * 增长
 */
smarty.register_modifier( 'plus', function( id, step ){
	step |= 0;
	if ( step < 1 )
	{
		step = 1;
	}
	return parseInt( id, 10 ) + step;
} );

/**
 * 是否存在
 */
smarty.register_modifier( 'isset', function( id, data ){
	data = data || window.OUT_DATA;
	return sparrow.isset( data[ id ] );
} );

/**
 * 是否在数据中
 */
smarty.register_modifier( 'in_array', function( id, array ){
	return -1 !== sparrow.in_array( id, array );
} );

smarty.register_modifier( 'fix_static', sparrow.fix_static );

/**
 * 强转数字
 */
smarty.register_modifier( 'intval', function( val ) {
	return sparrow.intval( val );
} );

/**
 * 其它参数和验证参数属性
 */
function param_ext_string( params, normal_arr, validate_arr )
{
	var ext_str = '', val_str = '';
	for ( var p in params )
	{
		if ( sparrow.isset( normal_arr[ p ] ) )
		{
			continue;
		}
		var tmp_str = ' ' + p + '="' + params[ p ] + '"';
		if ( sparrow.isset( validate_arr ) )
		{
			val_str += tmp_str;
		}
		else
		{
			ext_str += tmp_str;
		}
	}
	return { ext_str : ext_str, val_str : val_str };
}


/**
 * 格式化时间
 */
smarty.register_modifier( 'date', sparrow.date );

