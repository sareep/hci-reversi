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
    var newHTML = "<p>" + payload.username + " has joined</p>";
    var newNode = $(newHTML);
    newNode.hide();
    $("#messages").prepend(newNode);
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
    $("#messages").prepend(newNode);
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

    $("#messages").prepend(newNode);
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

    console.log("*** Client Log Message: 'join_room' payload: " + JSON.stringify(payload));
    socket.emit("join_room", payload);

    $('#quit').append('<a href="lobby.html?username='+username+'" class="btn btn-danger btn-default" role="button" aria-pressed="true">Quit</a>')

});


var old_board = [
    ["?","?","?","?","?","?","?","?"],
    ["?","?","?","?","?","?","?","?"],
    ["?","?","?","?","?","?","?","?"],
    ["?","?","?","?","?","?","?","?"],
    ["?","?","?","?","?","?","?","?"],
    ["?","?","?","?","?","?","?","?"],
    ["?","?","?","?","?","?","?","?"],
    ["?","?","?","?","?","?","?","?"],
]

var my_color = " ";

socket.on('game_update',function(payload){
    console.log("*** Client Log Message: 'game_update' payload: " + JSON.stringify(payload));
    /* Check for good board update */
    if (payload.result == "fail") {
        console.log(payload.message)
        window.location.href = 'lobby.html?username='+username
        return;
    }

    /* Check for a good board in the payload */
    var board = payload.game.board;
    if(('undefined' === typeof board) || !board){
        console.log("Internal error: received malformed board update from the server")
        return;
    }

    /* Update my color */
    if(socket.id == payload.game.player_white.socket){
        my_color = "white"
    }else if(socket.id == payload.game.player_black.socket){
        my_color = "black"
    }else{
        /* Something weird happened, send client back to lobby */
        console.log('something weird happened, sending back to lobby')
        window.location.href = "lobby.html?username="+username
    }

    $('#my_color').html('<h3 id="my_color">I am ' + my_color+'</h3>')

    blacksum = 0;
    whitesum = 0;

    /* Animate changes to the board */
    var row, col;
    for(row = 0; row<8; row++){
        for(col = 0; col<8; col++){

            //tally score
            if(board[row][col] == 'b'){
                blacksum++
            }else if(board[row][col] == 'w'){
                whitesum++
            }

            /* if a board space has changed */
            if(old_board[row][col] != board[row][col]){
                if(old_board[row][col] == "?" && board[row][col] == " "){
                    $('#'+row+'_'+col).html('<img src="assets/images/empty.gif" alt="empty square"/>')
                }
                else if(old_board[row][col] == "?" && board[row][col] == "w"){
                    $('#'+row+'_'+col).html('<img src="assets/images/empty_to_white.gif" alt="white square"/>')
                }
                else if(old_board[row][col] == "?" && board[row][col] == "b"){
                    $('#'+row+'_'+col).html('<img src="assets/images/empty_to_black.gif" alt="black square"/>')
                }
                else if(old_board[row][col] == " " && board[row][col] == "w"){
                    $('#'+row+'_'+col).html('<img src="assets/images/empty_to_white.gif" alt="white square"/>')
                }
                else if(old_board[row][col] == " " && board[row][col] == "b"){
                    $('#'+row+'_'+col).html('<img src="assets/images/empty_to_black.gif" alt="black square"/>')
                }
                else if(old_board[row][col] == "w" && board[row][col] == " "){
                    $('#'+row+'_'+col).html('<img src="assets/images/white_to_empty.gif" alt="empty square"/>')
                }
                else if(old_board[row][col] == "b" && board[row][col] == " "){
                    $('#'+row+'_'+col).html('<img src="assets/images/black_to_empty.gif" alt="empty square"/>')
                }
                else if(old_board[row][col] == "w" && board[row][col] == "b"){
                    $('#'+row+'_'+col).html('<img src="assets/images/white_to_black.gif" alt="black square"/>')
                }
                else if(old_board[row][col] == "b" && board[row][col] == "w"){
                    $('#'+row+'_'+col).html('<img src="assets/images/black_to_white.gif" alt="white square"/>')
                }else{
                    $('#'+row+'_'+col).html('<img src="assets/images/error.gif" alt="error square"/>')
                }

                /* Interactivity */
                $('#'+row+'_'+col).off('click');
                if(board[row][col] == ' '){
                    $('#'+row+'_'+col).addClass('hovered_over')
                    $('#'+row+'_'+col).click(function(r,c){
                        return function(){
                            var payload = {}
                            payload.row = r
                            payload.column = c
                            payload.color = my_color;
                            console.log('*** Client log message: "play_token" payload: ' + JSON.stringify(payload))
                            socket.emit('play_token',payload)
                        };
                    }(row,col))
                }else{
                    $('#'+row+'_'+col).removeClass('hovered_over')
                }

            }

        }
    }
    $('#blacksum').html(blacksum)
    $('#whitesum').html(whitesum)

    old_board = board;
})

socket.on('play_token_response',function(payload){
    console.log("*** Client Log Message: 'play_token_response' payload: " + JSON.stringify(payload));
    /* Check for good play_token response */
    if (payload.result == "fail") {
        console.log(payload.message)
        return;
    }
})

socket.on('game_over',function(payload){
    console.log("*** Client Log Message: 'game_over' payload: " + JSON.stringify(payload));
    /* Check for good play_token response */
    if (payload.result == "fail") {
        console.log(payload.message)
        return;
    }

    /* Jump to new page */
    $('#game_over').html('<h1>Game Over</h1><h2>Congratulations '+payload.winner+'!!</h2>')
    $('#game_over').append('<a href="lobby.html?username='+username+'" class="btn btn-success btn-lg" role="button" aria-pressed="true">Return to lobby</a>')

})