var ws = null;
var transports = [];

function connect(data, url) {
    disconnect(); //链接直接先关闭一下
    if (!url) {
        alert('Select whether to use W3C WebSocket or SockJS');
        return;
    }

    ws = new SockJS( url );


    ws.onopen = function () {
        echo(data);
    };

    ws.onmessage = function (event) {
        echo_msg(event.data);
    };

    ws.onclose = function (event) {
        console.log('Info: connection closed.');
        console.log(event);
    };
}

function disconnect() {
    if (ws != null) {
        ws.close();
        console.log("close....!!");
        ws = null;
    }
}

function echo(data) {
    if (ws != null) {
        ws.send(data);
    } else {
        alert('connection not established, please connect.');
    }
}

function updateUrl(urlPath) {
    if (urlPath.indexOf('sockjs') != -1) {
        url = urlPath;
        document.getElementById('sockJsTransportSelect').style.visibility = 'visible';
    }
    else {
        if (window.location.protocol == 'http:') {
            url = 'ws://' + window.location.host + urlPath;
        } else {
            url = 'wss://' + window.location.host + urlPath;
        }
        document.getElementById('sockJsTransportSelect').style.visibility = 'hidden';
    }
}

function updateTransport(transport) {
    transports = (transport == 'all') ?  [] : [transport];
}

function echo_msg(message) {
    var msgs = message.split("'");
    if( msgs.length == 2 ){
        message = msgs[1];
    }
    try{
        var terminal = document.getElementById('console');
        var p = document.createElement('p');
        p.style.wordWrap = 'break-word';
        p.appendChild(document.createTextNode(message));
        terminal.appendChild(p);
        if (terminal.childNodes.length > 200) {
            terminal.removeChild( terminal.firstElementChild );
        }
        terminal.scrollTop = terminal.scrollHeight;
    }catch( e )
    {
        console.log( e );
        disconnect();
    }
}