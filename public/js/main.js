/* functions for general use */

//Get the URL value desired
function getUrlParams(whichParam){
    var pageURL = window.location.search.substring(1);
    var pageURLVars = pageURL.split('&');

    for(var i = 0; i<pageURLVars.length; i++){
        var paramName = pageURLVars[i].split('=');
        if(paramName[0] == whichParam){
            return paramName[1];
        }
    }
}

var username = getUrlParams('username').split('%20').join('_');
if('undefined' == typeof username || !username){
    username = "Anonymous_"+Math.random()*10;
}

var chat_room = 'Room_One';


/* Connect to the socket server */
var socket = io.connect();

socket.on('log',function (array){
    console.log.apply(console, array)
});

//TODO: This doesn't run when server.js emits to here.
socket.on('join_room_response', function(payload){
    console.log('join response: '+payload.username)
    if(payload.result == 'fail'){
        alert(payload.message);
        return;
    }
    $('#messages').append('<p><b>New user has joined:</b> '+payload.username+'</p>');
});

socket.on('send_message_response', function(payload){
    if(payload.result == 'fail'){
        alert(payload.message);
        return;
    }
    $('#messages').append('<p><b>'+payload.username+' says:</b> '+payload.message+'</p>');
});

function sendMessage(){
    var payload = {};
    payload.room = chat_room;
    payload.username = username;
    payload.message = $('#send_message_holder').val();
    console.log('*** Client Log Message: \'send_message\' payload: '+JSON.stringify(payload));
    socket.emit('send_message',payload)
    
    //blank the message holder after the message is posted
    $("input[id=send_message_holder]").val('');
}

$(function(){
    var payload = {};
    payload.room = chat_room;
    payload.username = username;

    console.log('*** Client Log Message: \'join_room\' payload: '+JSON.stringify(payload));
    socket.emit('join_room', payload);

})