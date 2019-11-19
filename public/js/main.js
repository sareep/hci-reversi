/* functions for general use */

//Get the URL value desired
function getUrlParams(whichParam) {
    var pageURL = window.location.search.substring(1);
    var pageURLVars = pageURL.split("&");

    for (var i = 0; i < pageURLVars.length; i++) {
        var paramName = pageURLVars[i].split("=");
        if (paramName[0] == whichParam) {
            return paramName[1];
        }
    }
}

//set username
var username = getUrlParams("username");
if ("undefined" == typeof username || !username) {
    username = "Anonymous_" + Math.random() * 10;
}else{ 
    username = username.split("%20").join("_");
}

//set char room
var chat_room = getUrlParams("game_id");
if ("undefined" == typeof chat_room || !chat_room) {
    chat_room = "lobby"
}

/* Connect to the socket server */
var socket = io.connect();

/* Log Message Actions */
socket.on("log", function (array) {
    console.log.apply(console, array);
});


/*** LISTENER FUNCTIONS ***/

/* Join Room Actions */
socket.on("join_room_response", function (payload) {
    console.log("join response: " + payload.username);
    if (payload.result == "fail") {
        alert(payload.message);
        return;
    }
    
    //Handle new player message
    if (payload.socket_id == socket.id) {
        return;
    }
    
    /* Add new lobby table row for new player */
    var dom_elements = $(".socket_" + payload.socket_id);

    if (dom_elements.length == 0) {
        //For new entries

        //Add the socket
        var nodeA = $("<div></div>")
        nodeA.addClass("socket_" + payload.socket_id);
        nodeA.addClass("w-100")

        //Add the username
        var nodeB = $("<div></div>")
        nodeB.addClass("socket_" + payload.socket_id);
        nodeB.addClass("col-9 text-right")
        nodeB.append("<h4>" + payload.username + "</h4>")

        //Add the invite button
        var nodeC = $("<div></div>")
        nodeC.addClass("socket_" + payload.socket_id);
        nodeC.addClass("col-3 text-left")
        var buttonC = makeInviteButton(payload.socket_id);
        nodeC.append(buttonC)

        //Add animation
        nodeA.hide();
        nodeB.hide();
        nodeC.hide();
        $("#players").append(nodeA, nodeB, nodeC);

        nodeA.slideDown(1000);
        nodeB.slideDown(1000);
        nodeC.slideDown(1000);
    } else {
        //For existing entries, just create the invite button
        uninvite(payload.socket_id)
        var buttonC = makeInviteButton(payload.socket_id);
        $(".socket_" + payload.socket_id + " button").replaceWith(buttonC)
        dom_elements.slideDown(1000);
    }


    //Message for new player joining
    var newHTML = "<p>" + payload.username + " has joined the lobby</p>";
    var newNode = $(newHTML);
    newNode.hide();
    $("#messages").append(newNode);
    newNode.slideDown(1000);

});

/* Leave Room Actions */
socket.on("player_disconnected", function (payload) {
    console.log("leave response: " + payload.username);
    if (payload.result == "fail") {
        alert(payload.message);
        return;
    }

    //If we're being told we're leaving, ignore message
    if (payload.socket_id == socket.id) {
        return;
    }

    /* Animate out all the leaving player's elements */
    var dom_elements = $(".socket_" + payload.socket_id);
    if (dom_elements.length != 0) {
        dom_elements.slideUp(1000);
    }

    //Message to show player leaving
    var newHTML = "<p>" + payload.username + " left</p>";
    var newNode = $(newHTML);
    newNode.hide();
    $("#messages").append(newNode);
    newNode.slideDown(1000);
    
});

/* Send a new message */
function send_message() {
    var payload = {};
    payload.room = chat_room;
    payload.username = username;
    payload.message = $("#send_message_holder").val();
    console.log(
        "*** Client Log Message: 'send_message' payload: " + JSON.stringify(payload)
    );    
    socket.emit("send_message", payload);

    //blank the message holder after the message is posted
    $("input[id=send_message_holder]").val("");
}    

//Handle New Message
socket.on("send_message_response", function (payload) {
    if (payload.result == "fail") {
        alert(payload.message);
        return;
    }

    var newHTML = "<p><b>" + payload.username + " says:</b> " + payload.message + "</p>"
    var newNode = $(newHTML)
    newNode.hide()

    $("#messages").append(newNode);
    newNode.slideDown(1000)
});

/* Invitations */

//Handle send invite
socket.on("invite_response", function(payload){
    if (payload.result == "fail"){
        alert(payload.message);
        return;
    }

    var newNode = makeUninviteButton(payload.socket_id);
    $('.socket_'+payload.socket_id+' button').replaceWith(newNode)
})

/* Invite a player to a game */
function invite(who){
    var payload = {};
    payload.requested_user = who;

    console.log("*** Client Log Message: 'invite' payload: " + JSON.stringify(payload) );
    socket.emit('invite',payload)
}

//Notify that we have been invited
socket.on("invited", function(payload){
    if (payload.result == "fail"){
        alert(payload.message);
        return;
    }
    
    var newNode = makePlayButton(payload.socket_id);
    $('.socket_'+payload.socket_id+' button').replaceWith(newNode)
})


/* Uninvitations */

//Handle uninvite response
socket.on("uninvite_response", function(payload){
    if (payload.result == "fail"){
        alert(payload.message);
        return;
    }
    
    var newNode = makeInviteButton(payload.socket_id);
    $('.socket_'+payload.socket_id+' button').replaceWith(newNode)
})

/* Uninvite a player from a game */
function uninvite(who){
    var payload = {};
    payload.requested_user = who;

    console.log("*** Client Log Message: 'uninvite' payload: " + JSON.stringify(payload) );
    socket.emit('uninvite',payload)
}

//Notify that we have been uninvited
socket.on("uninvited", function(payload){
    if (payload.result == "fail"){
        alert(payload.message);
        return;
    }

    var newNode = makeInviteButton(payload.socket_id);
    $('.socket_'+payload.socket_id+' button').replaceWith(newNode)
})

/* Accepting Invites */

//Handle game_start response
socket.on("game_start_response", function(payload){
    if (payload.result == "fail"){
        alert(payload.message);
        return;
    }
    
    var newNode = makePlayButton(payload.socket_id);
    $('.socket_'+payload.socket_id+' button').replaceWith(newNode)

    window.location.href = "game.html?username="+username+"&game_id="+payload.game_id;
})

/* Notification we have had a game begin */
function game_start(who){
    var payload = {};
    payload.requested_user = who;

    console.log("*** Client Log Message: 'game_start' payload: " + JSON.stringify(payload) );
    socket.emit('game_start',payload)
}


/*** HTML FUNCTIONS ***/

/* Create an Invite Button */
function makeInviteButton(socket_id) {
    var newHTML = "<button type='button' class='btn btn-outline-primary'>Invite</button>"
    var newNode = $(newHTML)
    newNode.click(function(){
        invite(socket_id)
    })
    return (newNode)
}

/* Create an Uninvite Button */
function makeUninviteButton(socket_id) {
    var newHTML = "<button type='button' class='btn btn-info'>Uninvite</button>"
    var newNode = $(newHTML)
    newNode.click(function(){
        uninvite(socket_id)
    })
    return (newNode)
}

/* Create a Play Button */
function makePlayButton(socket_id) {
    var newHTML = "<button type='button' class='btn btn-success'>Play</button>"
    var newNode = $(newHTML)
    newNode.click(function(){
        game_start(socket_id)
    })
    return (newNode)
}



/*** Ready Load ***/
$(function () {
    var payload = {};
    payload.room = chat_room;
    payload.username = username;

    console.log(
        "*** Client Log Message: 'join_room' payload: " + JSON.stringify(payload)
    );
    socket.emit("join_room", payload);
});
