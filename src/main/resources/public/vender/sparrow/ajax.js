var $ = jQuery.noConflict();

/**----------------------------------ajax loading------------------------**/
var show_total = 0, is_append = false;
var shade_div = null, main_div = null, sparrow_layer = layer;
var ajax_loading= {
	show: function()
	{
		show_total++;
		if ( is_append )
		{
			shade_div.removeClass( 'hide' );
			main_div.removeClass( 'hide' );
		}
		else
		{
			var id = sparrow_layer.load( 2, {zIndex: 100000} );
			is_append = true;
			shade_div = $( '#layui-layer-shade' + id );
			main_div = $( '#layui-layer' + id );
		}
	},
	hide: function()
	{
		if ( 0 == show_total )
		{
			return;
		}
		if ( --show_total <= 0 && null !== shade_div )
		{
			shade_div.addClass( 'hide' );
			main_div.addClass( 'hide' );
			show_total = 0;
		}
	}
};
/*------------------------------------ajax_loading--------------------------end*/

var is_ajaxing = false;
var ajax_request_pool = [];
function ajax_push_pool( ajax_object )
{
	if ( is_ajaxing )
	{
		ajax_request_pool.push( ajax_object );
	}
	else
	{
		is_ajaxing = true;
		ajax_object.send();
	}
}

/**
 * 检查
 */
function ajax_pool_check()
{
	if ( 0 === ajax_request_pool.length )
	{
		is_ajaxing = false;
		return;
	}
	var tmp_obj = ajax_request_pool.shift();
	tmp_obj.send();
}

/**
 * 错误处理
 */
function sparrow_error_handle( data )
{
	ajax_loading.hide();
	console.log( data );
}
function bind( fn, obj ){
	return function()
	{
		return 	fn.apply( obj, arguments );
	};
}
/**
 * 改良的ajax对象
 */
function sparrow_ajax( url, callback, data, is_loading, callback_arg )
{
	this.ajax_data = data;
	this.url = url;
	this.callback_arg = callback_arg;
	if ( 'function' === typeof callback )
	{
		this.callback = callback;
	}
	else
	{
		this.callback = null;
	}
	if ( false === is_loading )
	{
		this.is_loading = false;
	}
}
sparrow_ajax.prototype = {
	is_loading: true,
	data_type: 'text',
	type: 'GET',
	async: true,
	set_data_type: function( data_type )
	{
		this.data_type = data_type;
	},
	set_async: function( async )
    {
        this.async = async;
    },
	set_type: function( type )
	{
		this.type = type;
	},
	send: function()
	{
		var options = {
			url : this.url,
			dataType : this.data_type,
			contentType: 'application/json',
			type : this.type,
			async: this.async,
			error : bind( this.error_handle, this ),
			success: bind( this.success_handle, this ),
			complete: bind( this.complete_handle, this )
		};
		if ( this.ajax_data && 'POST' === this.type )
		{
			options.data = this.ajax_data;
		}
		if ( this.is_loading )
		{
			ajax_loading.show();
		}
		$.ajax( options );
	},
	complete_handle: function()
	{
		if ( this.is_loading )
		{
			ajax_loading.hide();
		}
		ajax_pool_check();
	},
	error_handle: function( ajax_obj )
	{
		var title = '服务器返回错误';
		//需要ajax数据, 返回的不是ajax数据
		if ( 'json' === this.data_type && 4 === ajax_obj.readyState && ajax_obj.status < 300 )
		{
			title += '，返回结果非JSON';
		}
		title += ' status:' + ajax_obj.status +' ' + ajax_obj.statusText;
		layer.alert( title, {
          icon: 1,
          skin: 'layer-ext-moon',
          title: 'System error'
        });
		ajax_loading.hide();
	},
	success_handle: function( data )
	{
		var has_error = false;
		if ( 'json' === this.data_type )
		{
		    if( data.code == 1 ){
		        sparrow_error_handle( data.msg );
		    }else if( data.code == -1 ){
		        layer.msg( data.msg );
		    }else if( data.code == 2 ){
		        layer.alert( data.msg, {
                  icon: 1,
                  skin: 'layer-ext-moon',
                  title: 'aaa'
                });
		    }
		}
		if ( !has_error && this.callback ){
			try{
				this.callback( data, this.callback_arg );
			}catch ( err ){
				console.error( err );
				console.error( this );
			}
		}
	}
};


var ajax = {
	get: function( url, callback, is_loading, callback_arg ){
		var tmp_req = new sparrow_ajax( url, callback, null, is_loading, callback_arg );
		tmp_req.set_data_type( 'json' );
		ajax_push_pool( tmp_req );
	},
	post: function( url, data, callback, is_loading, callback_arg ){
		var tmp_req = new sparrow_ajax( url, callback, data, is_loading, callback_arg );
		tmp_req.set_data_type( 'json' );
		tmp_req.set_type( 'POST' );
		ajax_push_pool( tmp_req );
	},
	get_text: function( url, callback, is_loading, callback_arg ){
		var tmp_req = new sparrow_ajax( url, callback, null, is_loading, callback_arg );
		ajax_push_pool( tmp_req );
	},
	post_text: function( url, data, callback, is_loading, callback_arg ){
		var tmp_req = new sparrow_ajax( url, callback, data, is_loading, callback_arg );
		tmp_req.set_type( 'POST' );
		ajax_push_pool( tmp_req );
	},
	sync_get: function( url, callback, is_loading, callback_arg ){
        var tmp_req = new sparrow_ajax( url, callback, null, is_loading, callback_arg );
        tmp_req.set_data_type( 'json' );
        tmp_req.set_async(false);
        ajax_push_pool( tmp_req );
    },
    sync_post: function( url, data, callback, is_loading, callback_arg ){
        var tmp_req = new sparrow_ajax( url, callback, data, is_loading, callback_arg );
        tmp_req.set_data_type( 'json' );
        tmp_req.set_type( 'POST' );
        tmp_req.set_async(false);
        ajax_push_pool( tmp_req );
    },
	async_post: function (url, data, callback) {
		var tmp_req = new sparrow_ajax( url, callback, JSON.stringify(data), true, null );
		tmp_req.set_data_type( 'json' );
		tmp_req.set_type( 'POST' );
		tmp_req.send();
	},
	async_get: function( url, callback){
		var tmp_req = new sparrow_ajax( url, callback, null, true, null );
		tmp_req.set_data_type( 'json' );
		tmp_req.send();
	}
};